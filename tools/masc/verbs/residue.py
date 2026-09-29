"""The residue of verbframes.py, and odd verb frames that may be annotation
errors: tools for looking at them by hand.

    python3 residue.py MASC_DATA_DIR VERBS_JSONL residue
        every verb whose missing subject verbframes.py could not explain
        (unexplained, question or inversion, clause coordination,
        imperative, fragment or headline), with its clause's raw tree
    python3 residue.py MASC_DATA_DIR VERBS_JSONL show ID POS
        one verb: its record and the raw tree around it
    python3 residue.py MASC_DATA_DIR VERBS_JSONL wacky
        the candidate error classes beyond subjects, with counts and ids
    python3 residue.py MASC_DATA_DIR VERBS_JSONL singletons
        frames that occur once, by shape
"""
import collections, json, os, re, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))
from masctrees import *
import verbframes as vf

RESIDUE = ('unexplained', 'question or inversion, no SBJ',
           'clause coordination', 'imperative', 'fragment or headline')


def load_verbs(path):
    with open(path) as fh:
        return [json.loads(l) for l in fh]


def load_trees(root, ids=None):
    out = {}
    for g, fid, i, t in masc_trees(root):
        tid = '%s/%s#%d' % (g, fid, i)
        if ids is None or tid in ids:
            out[tid] = unwrap(t)
    return out


def find_vp(raw, pos):
    """The lexical verb phrase whose head is the overt word at pos."""
    root = vf.Node(raw)
    vf.number(root)
    for vp in vf.nodes(root):
        if vp.cat != 'VP' or vf.is_aux_vp(vp):
            continue
        for k in vp.kids:
            if k.word is not None and k.cat in vf.VERB_TAGS and k.pos == pos:
                return root, vp
    return root, None


def context(raw, pos, up=2):
    """The raw tree from the verb phrase's clause (or `up` nodes above the
    verb phrase when there is none), pretty-printed."""
    root, vp = find_vp(raw, pos)
    if vp is None:
        return '(verb not found)'
    n = vp
    while n.parent is not None and n.parent.cat == 'VP':
        n = n.parent
    for _ in range(up):
        if n.parent is not None:
            n = n.parent
    return pretty(n.raw)


def show(trees, rec, up=2):
    print('== %s pos %d  %s/%s  clause=%s  cause=%s' % (
        rec['id'], rec['pos'], rec['verb'], rec['tag'], rec['clause'],
        rec['backbone_subjectless']))
    print('   full: [%s]  backbone: [%s]  flags: %s' % (
        rec['frame_full'], rec['frame_backbone'], ','.join(rec['flags'])))
    print('   ' + rec['sentence'][:300])
    print('   ' + context(trees[rec['id']], rec['pos'], up)[:1500])


# ---- candidate error classes beyond subjects ----

TRANSITIVE_SAMPLE = None  # filled from the data: verbs almost always with NP object


def np_objects(rec, overt=True):
    return [c for c in rec['complements'] if c['cat'] == 'NP' and not c['tags']
            and (c['realization'] == 'overt') == overt]


def wacky(verbs, trees):
    out = collections.OrderedDict()
    # 1. two or more overt NP objects plus a PRD
    out['two NP objects + PRD'] = [r for r in verbs
        if len(np_objects(r)) >= 2 and any('PRD' in c['tags'] for c in r['complements'])]
    # 1b. three or more overt NP objects
    out['three+ overt NP objects'] = [r for r in verbs if len(np_objects(r)) >= 3]
    # 2. contradictory function tags on one complement or modifier
    def contradictory(c):
        ts = set(c['tags'])
        comp = ts & vf.COMPLEMENT_TAGS
        mod = ts & vf.MODIFIER_TAGS
        return (len(comp) > 1) or (comp and mod) or ('SBJ' in ts)
    out['contradictory tags on a VP daughter'] = [r for r in verbs
        if any(contradictory(c) for c in r['complements'] + r['modifiers'])]
    # 2b. more than one PRD in a VP
    out['two or more PRD'] = [r for r in verbs
        if sum('PRD' in c['tags'] for c in r['complements']) >= 2]
    # 3. VB*-tagged word that is plainly not a verb: closed-class or
    # punctuation-like forms
    nonverb = re.compile(r"^([^A-Za-z]+|the|a|an|of|and|or|to|in|on|for|with|"
                         r"that|this|it|he|she|they|we|i|you|not|very|so)$", re.I)
    out['VB* tag on a non-verb form'] = [r for r in verbs if nonverb.match(r['verb'])]
    # 4. S complement with an overt subject whose subject is coindexed as a
    # *PRO* site: found in the raw tree below
    out['S complement: overt SBJ and empty SBJ both'] = []
    # 5. subject errors: two SBJ daughters in one clause
    out['clause with two -SBJ daughters'] = []
    for r in verbs:
        raw = trees[r['id']]
        root, vp = find_vp(raw, r['pos'])
        if vp is None:
            continue
        for d in vp.kids:
            if d.cat == 'S':
                sbjs = [k for k in d.kids if 'SBJ' in k.tags]
                if len(sbjs) > 1:
                    out['S complement: overt SBJ and empty SBJ both'].append(r)
        n = vp
        while n.parent is not None and n.parent.cat == 'VP':
            n = n.parent
        cl = n.parent
        if cl is not None and cl.cat in vf.CLAUSES:
            if sum('SBJ' in k.tags for k in cl.kids) > 1:
                out['clause with two -SBJ daughters'].append(r)
    return out


def transitive_no_object(verbs, min_n=20, share=0.95):
    """Verbs whose occurrences in an active clause almost always have an NP
    object, and the occurrences that have neither an object nor a trace."""
    def active(r):
        return r['tag'] != 'VBN'
    stats = collections.defaultdict(lambda: [0, 0])
    for r in verbs:
        if not active(r):
            continue
        v = r['verb'].lower()
        stats[v][0] += 1
        if any(c['cat'] == 'NP' and not c['tags'] for c in r['complements']):
            stats[v][1] += 1
    trans = {v for v, (n, k) in stats.items() if n >= min_n and k / n >= share}
    hits = []
    for r in verbs:
        if active(r) and r['verb'].lower() in trans and not any(
                c['cat'] in ('NP', 'S', 'SBAR', 'UCP') for c in r['complements']):
            hits.append(r)
    return trans, hits


def shape(frame):
    """A frame's shape: empty kinds and CLR/PRD details kept, counts kept."""
    return frame


def singletons(verbs):
    fc = collections.Counter(r['frame_full'] for r in verbs)
    singles = [r for r in verbs if fc[r['frame_full']] == 1]
    # shape: the categories only, without tags and empty kinds
    def coarse(f):
        return ' '.join(sorted(set(re.sub(r'\(.*?\)', '', p).split('-')[0]
                                   for p in f.split())))
    return fc, singles, collections.Counter(coarse(r['frame_full']) for r in singles)


def main():
    masc, vpath, cmd = sys.argv[1:4]
    verbs = load_verbs(vpath)
    if cmd == 'residue':
        rs = [r for r in verbs if r['backbone_subjectless'] in RESIDUE]
        trees = load_trees(masc, {r['id'] for r in rs})
        for r in rs:
            show(trees, r)
    elif cmd == 'show':
        tid, pos = sys.argv[4], int(sys.argv[5])
        up = int(sys.argv[6]) if len(sys.argv) > 6 else 2
        trees = load_trees(masc, {tid})
        for r in verbs:
            if r['id'] == tid and r['pos'] == pos:
                show(trees, r, up)
    elif cmd == 'wacky':
        trees = load_trees(masc)
        for name, rs in wacky(verbs, trees).items():
            print('## %s: %d' % (name, len(rs)))
            for r in rs[:60]:
                show(trees, r, 0)
        trans, hits = transitive_no_object(verbs)
        print('## transitive verbs (%d) with no object, S or trace: %d' % (len(trans), len(hits)))
        print('   verbs: ' + ' '.join(sorted(trans)))
        for r in hits:
            show(trees, r, 0)
    elif cmd == 'singletons':
        fc, singles, shapes = singletons(verbs)
        print('%d distinct full frames; %d occur once' % (len(fc), len(singles)))
        for s, k in shapes.most_common(30):
            print('  %5d  %s' % (k, s))
        trees = load_trees(masc, {r['id'] for r in singles})
        for r in singles:
            show(trees, r, 0)


if __name__ == '__main__':
    main()
