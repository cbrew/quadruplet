"""The residue of verbframes.py, and odd verb frames that may be annotation
errors: tools for looking at them by hand.

    python3 residue.py MASC_DATA_DIR VERBS_JSONL residue
        every verb whose missing subject verbframes.py could not explain
        (unexplained, question or inversion, clause coordination,
        imperative, fragment or headline), with its clause's raw tree
    python3 residue.py MASC_DATA_DIR VERBS_JSONL classify
        the same verbs, classified mechanically by their clause's daughters
    python3 residue.py MASC_DATA_DIR VERBS_JSONL table
        the same verbs with the hand classification (HAND overrides the
        mechanical class), and counts; OTHER_ERRORS lists errors found
        beyond the residue
    python3 residue.py MASC_DATA_DIR VERBS_JSONL rates
        for every S, SQ, SINV with a VP: -SBJ daughter, untagged NP, or none
    python3 residue.py MASC_DATA_DIR VERBS_JSONL show ID POS [UP]
        one verb: its record and the raw tree around it
    python3 residue.py MASC_DATA_DIR VERBS_JSONL wacky
        the candidate error classes beyond subjects, with counts and ids
    python3 residue.py MASC_DATA_DIR VERBS_JSONL singletons
        frames that occur once, by shape
    python3 residue.py MASC_DATA_DIR VERBS_JSONL sample N SEED
        N random verbs (seeds used: 20260928 and 7, N=150) for hand-checking
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


def subclass(raw, pos):
    """A first, mechanical classification of a residue verb by what its
    clause's daughters are; checked by hand afterwards."""
    root, vp = find_vp(raw, pos)
    n = vp
    while n.parent is not None and n.parent.cat == 'VP':
        n = n.parent
    cl = n.parent
    if cl is None:
        return 'no clause', ''
    i = cl.kids.index(n)
    before = cl.kids[:i]
    after = cl.kids[i + 1:]
    nps = [k for k in cl.kids if k.cat == 'NP' and not k.tags]
    if any(vf.empty_kind(k.raw) for k in nps):
        return 'empty NP subject without -SBJ', vf.empty_kind(next(k.raw for k in nps if vf.empty_kind(k.raw)))
    if any(k.cat == 'NP' and not k.tags for k in before):
        return 'overt NP subject without -SBJ', ''
    if cl.cat == 'SQ' and any(k.cat == 'NP' and not k.tags for k in after):
        return 'overt NP subject without -SBJ', 'after'
    sib = [k for k in before if k.cat in vf.CLAUSES and any('SBJ' in x.tags for x in k.kids)
           and not any(x.cat == 'VP' for x in k.kids)]
    if sib:
        return 'subject in a sister S without a VP', ''
    first = next((x for x in vf.nodes(n) if x.word is not None and x.cat != '-NONE-'), None)
    kind = first.cat if first is not None else '?'
    return 'no subject position', kind + ' ' + cl.label + ' under ' + (cl.parent.label if cl.parent else 'root')


def subject_rates(root_dir):
    """For every S whose verb phrase daughter is non-finite (headed by TO,
    VBG or VBN through auxiliaries) and every finite S, SQ, SINV: whether
    it has an -SBJ daughter, an untagged NP daughter that is empty or
    overt, or no NP at all."""
    c = collections.Counter()
    ex = collections.defaultdict(list)
    for g, fid, i, t in masc_trees(root_dir):
        raw = unwrap(t)
        if is_leaf(raw):
            continue
        root = vf.Node(raw)
        tid = '%s/%s#%d' % (g, fid, i)
        for n in vf.nodes(root):
            if n.cat not in ('S', 'SQ', 'SINV'):
                continue
            vps = [k for k in n.kids if k.cat == 'VP']
            if not vps:
                continue
            first = next((x for x in vf.nodes(vps[0]) if x.word is not None and x.cat != '-NONE-'), None)
            if first is None:
                continue
            kind = 'to' if first.cat == 'TO' else 'ing/en' if first.cat in ('VBG', 'VBN') else 'finite/bare'
            if any('SBJ' in k.tags for k in n.kids):
                st = 'SBJ'
            elif any(k.cat == 'NP' and not k.tags and vf.empty_kind(k.raw) for k in n.kids):
                st = 'untagged empty NP'
            elif any(k.cat == 'NP' and not k.tags for k in n.kids):
                st = 'untagged overt NP'
            elif 'IMP' in n.tags:
                st = 'none (IMP)'
            else:
                st = 'none'
            c[(n.cat, kind, st)] += 1
            if st != 'SBJ' and len(ex[(n.cat, kind, st)]) < 5:
                ex[(n.cat, kind, st)].append(tid)
    return c, ex


# ---- the hand classification (checked tree by tree, 2026-09-28) ----

# Residue verbs whose mechanical class (subclass) was overridden by hand.
# Classes: E-SBJ-TAG subject present but not tagged -SBJ (or mis-tagged);
# E-NO-SUBJ clause lacks the (empty) subject PTB conventions require;
# E-BRACKET subject and verb phrase in the wrong constituents;
# C-RNR a construction verbframes.py should recognise; G genuinely
# subjectless.
HAND = {
    ('court-transcript/Day3PMSession#322', 27): ('C-RNR', 'right-node-raised VP-5, subjects he/she in the conjuncts'),
    ('essays/Black_and_white#50', 25): ('E-BRACKET', 'subject NP-SBJ inside the WHPP'),
    ('ficlets/1399#289', 17): ('E-SBJ-TAG', 'clausal subject tagged S-NOM-DIR, not S-NOM-SBJ'),
    ('philanthropic-fundraising/110CYL068#14', 4): ('E-SBJ-TAG', 'clausal subject S-NOM without -SBJ'),
    ('technical/pmed.0010029#9', 22): ('E-SBJ-TAG', 'empty subject tagged NP-ADV'),
    ('travel-guides/WhereToHongKong#460', 14): ('E-SBJ-TAG', 'post-verbal subject of SINV untagged'),
    ('twitter/tweets1#483', 12): ('E-SBJ-TAG', 'title subject S-IMP-NOM-TTL without -SBJ'),
    ('jokes/jokes1#172', 5): ('E-BRACKET', 'subject "the man" inside the VP, untagged'),
    ('journal/Article247_328#5', 11): ('E-BRACKET', 'NP-SBJ "Time" inside the VP'),
    ('nyt/NYTnewswire2#19', 27): ('E-BRACKET', 'subject trace placed as object of passive put; by-agent PP-LOC'),
    ('non-fiction/rybczynski-ch3#70', 4): ('E-BRACKET', 'NP-SBJ inside the VP'),
    ('non-fiction/rybczynski-ch3#195', 18): ('E-BRACKET', '"for granted" analysed as SBAR with a clause; cf. #42 PP-CLR'),
    ('blog/sucker#1', 11): ('G', 'byline fragment "Posted by ...", attached inside a title S'),
    ('ficlets/1403#627', 7): ('G', 'discourse "see?"'),
    ('ficlets/1403#672', 8): ('G', 'subject drop in fragmentary speech'),
    ('fiction/Nathans_Bylichka#328', 0): ('G', 'abandoned start "Are ?" under EDITED'),
    ('fiction/hotel-california#468', 6): ('G', 'discourse "remember?"'),
    ('fiction/hotel-california#511', 1): ('G', '"Mind if I join you?", auxiliary and subject dropped'),
    ('twitter/tweets1#562', 0): ('G', 'diary-style subject drop'),
    ('twitter/tweets1#883', 1): ('G', 'diary-style subject drop'),
    ('twitter/tweets1#898', 2): ('G', 'source typo "What do think"'),
}

MECHANICAL = {
    'empty NP subject without -SBJ': 'E-SBJ-TAG',
    'overt NP subject without -SBJ': 'E-SBJ-TAG',
    'subject in a sister S without a VP': 'E-BRACKET',
    'no subject position': 'E-NO-SUBJ',
}

# Errors found beyond the residue: (id, verb position or None, what is wrong).
OTHER_ERRORS = [
    ('non-fiction/CUP1#189', 5, 'superscript "2" of km2 tagged VBN, given a VP and a passive trace'),
    ('non-fiction/CUP1#189', 12, 'the same'),
    ('philanthropic-fundraising/116CUL032#16', 14, 'S complement has two subjects, NP-SBJ *-1 and NP-SBJ *PRO*'),
    ('journal/VOL15_3#124', 1, 'two clauses flat in one S: two NP-SBJ, two VPs, no S brackets'),
    ('debate-transcript/2nd_Gore-Bush#734', 9, 'object of convince tagged NP-SBJ-1'),
    ('journal/VOL15_3#7', 23, 'archaic post-verbal subject "thou" as NP-SBJ inside the VP of a *PRO* infinitive'),
    ('journal/VOL15_3#136', 23, 'left-dislocated NP tagged SBJ besides the resumptive NP-SBJ "that"'),
    ('fiction/Nathans_Bylichka#735', 14, 'adverbial "tonight" tagged NP-TMP-PRD: a second predicate'),
    ('movie-script/pirates#1153', 4, '"What \'s that over there": NP-PRD and ADVP-LOC-PRD, two predicates'),
    ('court-transcript/Day3PMSession#748', 34, 'title "Of Pandas and People" bracketed PP-TTL: put loses its object'),
    ('court-transcript/Day3PMSession#767', 20, 'the same'),
    ('essays/Black_and_white#140', 12, '*RNR* object trace inside the PRT'),
    ('twitter/tweets2#666', 2, 'object "God" outside the VP of Thank'),
    ('travel-guides/WhereToHongKong#23', 4, 'object of "carrying out" made subject of "guard"'),
    ('twitter/tweets2#19', 3, 'object NP outside the VP of gain'),
    ('solicitation-brochures/aspca1#39', 9, '"too large for X to handle": no object gap'),
    ('court-transcript/Day3PMSession#1151', 2, 'SBAR-1 (0) separated from its S'),
    ('debate-transcript/3rd_Bush-Kerry#28', 5, 'malformed empty element *PRO-1'),
    ('twitter/tweets1#134', 26, 'malformed empty element *RNR-2'),
    ('face-to-face/NapierDianne#158', 9, '*T* object of "with" outside the PP'),
    ('blog/Effing-Idiot#56', 2, 'object (SBAR (-NONE- *PRO*)) of keep'),
    ('enron/9085#12', 3, 'location of "take place" untagged NP, read as an object'),
    ('essays/Ant_Robot#238', 110, 'ADJP-PRD holds (CD (-NONE- 0))'),
    ('fiction/Nathans_Bylichka#364', 6, 'duplicated tag PP-LOC-PRD-PRD'),
    ('fiction/cable_spool_fort#36', 1, 'duplicated tag PP-DIR-CLR-CLR'),
    ('fiction/cable_spool_fort#61', 6, 'passive object trace labelled UCP'),
    ('fiction/hotel-california#52', 3, 'predicate trace in small clause lacks -PRD'),
    ('govt-docs/fcic_final_report_conclusions#167', 12, 'passive object trace is *PRO*, not *'),
    ('govt-docs/chapter-10#157', 28, 'verb "refining" tagged NN as NP-PRD of been'),
    ('journal/Article247_3500#3', 23, 'subject trace NP-SBJ inside the VP'),
    ('non-fiction/ch5#81', 7, 'object of transforms tagged NP-PRD'),
    ('non-fiction/rybczynski-ch3#256', 17, 'conjuncts of the PP object attached outside the PP'),
    ('movie-script/pirates#1823', 5, 'small clause S holds only its subject trace; PP-PRD outside'),
    ('twitter/tweets1#386', 13, 'NP "tomorrow" untagged, read as a second object'),
    ('spam/ucb45#6', 2, '"Not only are we the best place": NP-PRD outside the VP of are'),
    ('face-to-face/Bmr021#655', 13, 'object of got tagged NP-PRD'),
    ('blog/Anti-Terrorist#42', 8, 'NP-SBJ and VP directly under UCP, no S'),
    ('movie-script/pirates#808', 2, '"bodes ill": adverb ill as an NP object'),
    ('spam/ucb26#9', None, '"goods advertised" analysed as SBAR (for) + clause with subject'),
    ('fiction/easy_money#41', None, 'verb "push" (look at X push) tagged NN'),
    ('wsj/wsj_0027#0', None, 'verb resigned tagged JJ as VP head'),
    ('blog/Fermentation_HR5034#19', None, 'verb shocked tagged JJ as VP head'),
    ('journal/Article247_328#2', None, 'verb exact tagged JJ as VP head'),
    ('spam/FBI_urgent#33', None, 'verb advice(d) tagged NN as VP head'),
    ('journal/VOL15_3#312', None, 'verb effect tagged NN as VP head'),
    ('wsj/wsj_0151#7', None, 'verb scared tagged JJ as VP head'),
    ('wsj/wsj_0173#0', None, 'verb peaked tagged JJ as VP head'),
    ('wsj/wsj_0120#11', None, 'verb set tagged NN as VP head'),
    ('fiction/captured_moments#512', None, 'verb island-hopping tagged NN NN as VP head'),
    ('blog/Effing-Idiot#22', 22, 'clause labelled IP-IMP-TTL (not a PTB label)'),
    ('solicitation-brochures/defenders5#26', 20, 'clause labelled RS'),
    ('fiction/The_Black_Willow#43', 1, 'NP-SBJ and VP under INTJ, no S'),
    ('twitter/tweets2#603', 15, 'NP-SBJ and VP under ADJP, no S'),
    ('journal/VOL15_3#317', 19, '"likely to know": VP under ADJP, no S'),
    ('essays/A_defense_of_Michael_Moore#48', 12, 'whole quoted clause inside PP "According to"; no S'),
    ('essays/Ant_Robot#283', 31, 'QP "a fraction ( modeled at ...) of" brackets a VP'),
]


def hand_class(raw, rec):
    key = (rec['id'], rec['pos'])
    if key in HAND:
        return HAND[key]
    k, detail = subclass(raw, rec['pos'])
    return MECHANICAL[k], k + (' ' + detail if detail else '')


def main():
    masc, vpath, cmd = sys.argv[1:4]
    verbs = load_verbs(vpath)
    if cmd == 'residue':
        rs = [r for r in verbs if r['backbone_subjectless'] in RESIDUE]
        trees = load_trees(masc, {r['id'] for r in rs})
        for r in rs:
            show(trees, r)
    elif cmd == 'classify':
        rs = [r for r in verbs if r['backbone_subjectless'] in RESIDUE]
        trees = load_trees(masc, {r['id'] for r in rs})
        c = collections.Counter()
        for r in rs:
            k, detail = subclass(trees[r['id']], r['pos'])
            c[(r['backbone_subjectless'], k)] += 1
            print('%s\t%d\t%s\t%s\t%s\t%s' % (r['id'], r['pos'], r['verb'], r['backbone_subjectless'], k, detail))
        for (a, b), n in sorted(c.items()):
            print('# %4d  %s | %s' % (n, a, b))
    elif cmd == 'rates':
        c, ex = subject_rates(masc)
        for k in sorted(c):
            print('%-6s %-12s %-20s %7d  %s' % (k + (c[k], ' '.join(ex.get(k, [])))))
    elif cmd == 'sample':
        import random
        n = int(sys.argv[4]) if len(sys.argv) > 4 else 150
        seed = int(sys.argv[5]) if len(sys.argv) > 5 else 20260928
        rs = random.Random(seed).sample(verbs, n)
        trees = load_trees(masc, {r['id'] for r in rs})
        for k, r in enumerate(rs):
            print('[%d]' % k, end=' ')
            show(trees, r, 1)
    elif cmd == 'table':
        rs = [r for r in verbs if r['backbone_subjectless'] in RESIDUE]
        trees = load_trees(masc, {r['id'] for r in rs})
        c = collections.Counter()
        for r in rs:
            cls, note = hand_class(trees[r['id']], r)
            c[(r['backbone_subjectless'], cls)] += 1
            print('%s\t%d\t%s\t%s\t%s\t%s' % (r['id'], r['pos'], r['verb'], r['backbone_subjectless'], cls, note))
        for (a, b), n in sorted(c.items()):
            print('# %4d  %s | %s' % (n, a, b))
        print('# other errors: %d' % len(OTHER_ERRORS))
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
