"""Verbs with no clause above them: which constructions they are.

    python3 noclause.py MASC_DATA_DIR OUT.jsonl

Walks the raw MASC trees exactly as ../verbframes.py does and, for each
lexical verb whose verb phrase has no S, SQ, SINV or SBARQ above it (through
auxiliary and coordinated verb phrases), writes a JSON object with the
context needed to classify it: the attachment node (the first non-VP
ancestor) and its parent, the sisters to the left and right of the topmost
verb phrase, empty elements inside the verb phrase, and a rule-based class.
Prints counts by class, spoken and written, and a table of constructions
(grouped) by genre, with the rate per 1000 lexical verbs of the genre.

    python3 noclause.py MASC_DATA_DIR --show ID [ID ...]

prints the raw trees of the sentences with those ids.
"""
import collections, json, os, re, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))
from masctrees import *
import verbframes_v1 as vf

SPOKEN = {'face-to-face', 'telephone', 'court-transcript', 'debate-transcript'}
NOMINAL = {'NN', 'NNS', 'NNP', 'NNPS', 'PRP', 'CD', 'NP', 'NML', 'EX', 'DT', 'QP', 'JJ', 'ADJP', 'NX'}


def lab(n):
    return n.label if n is not None else 'ROOT'


def empties(vp):
    """Empty elements inside the verb phrase (not inside embedded clauses'
    subjects: all of them, as strings)."""
    return [x.word for x in vf.nodes(vp) if x.cat == '-NONE-' and x.word]


def direct_empty_objects(vp):
    out = []
    for k in vp.kids:
        kind = vf.empty_kind(k.raw) if k.word is None else None
        if kind and k.cat == 'NP':
            out.append(k.label + ':' + ''.join(w for t, w in leaves(k.raw)))
    return out


def classify(att, top, head, chain, genre):
    """The construction a clauseless verb phrase is in, by rule; see the
    report docs/verbs/04-no-clause.md for what each class is."""
    anc = list(ancestors(att)) if att is not None else []
    if any(a.cat == 'EDITED' for a in anc):
        return 'disfluency (EDITED)'
    if att is not None:
        sisters = [k for k in att.kids if k is not top]
        if any('SBJ' in k.tags for k in sisters):
            return 'error: subject is a sister of the VP (no S)'
        i = att.kids.index(top)
        left = [k for k in att.kids[:i] if k.cat not in vf_punct]
        if left and left[-1].cat in vf.CLAUSES:
            inner = [k for k in left[-1].kids if k.cat not in vf_punct]
            if inner and all('SBJ' in k.tags for k in inner):
                return 'error: S closed before its VP'
        if genre == 'movie-script' and any(k.cat == 'CODE' for k in att.kids[:i]) \
                or "CONT'D" in head.word:
            return 'stage direction'
        if att.cat == 'REF':
            return 'citation formula (REF)'
    ttl = any(t in ('TTL', 'HLN') for a in anc for t in a.tags) or \
        any(re.search(r'-(TTL|HLN)', l) for l in chain)
    if ttl:
        return 'title or headline'
    if att is None:
        if head.cat in ('VBG', 'VBN'):
            return 'root: gerund or participle'
        return 'root: bare VP, other (imperative etc.)'
    c = att.cat
    i = att.kids.index(top)
    left = [k for k in att.kids[:i] if k.cat not in vf_punct and k.cat not in ('-NONE-', 'CODE', 'SYM')]
    nominal_left = any(k.cat in NOMINAL or k.cat.startswith('NN') for k in left)
    if c in ('NP', 'NML', 'NX', 'QP', 'WHNP', 'PP', 'ADJP', 'RRC', 'NAC') and (nominal_left or c == 'RRC'):
        if head.cat == 'VBN':
            return 'reduced relative: VBN'
        if head.cat == 'VBG':
            return 'reduced relative: VBG'
        if head.cat == 'VBD':
            return 'reduced relative: VBD (tag error)'
        return 'finite or base VP inside a phrase'
    if c in ('NP', 'NML', 'NX') and not left:
        return 'VP as nominal or compound modifier'
    if c == 'UCP':
        up = att.parent
        while up is not None and up.cat in ('UCP', 'VP'):
            up = up.parent
        if up is not None and up.cat in vf.CLAUSES:
            return 'UCP: unlike coordination under a clause'
        return 'UCP: in fragment or list'
    if c == 'FRAG':
        if any(k.cat in ('NP', 'ADVP', 'PP', 'ADJP', 'INTJ', 'NN') for k in left):
            return 'FRAG: VP with other material'
        return 'FRAG: bare VP'
    if c == 'SBAR':
        return 'SBAR without S'
    return 'other'


vf_punct = PUNCT | {',', '.', ':'}


def ancestors(n):
    while n is not None:
        yield n
        n = n.parent


def main(root_dir, out_path):
    out = open(out_path, 'w')
    by = collections.Counter(); bygenre = collections.Counter()
    bygenre_full = collections.defaultdict(collections.Counter)
    totals = collections.Counter()  # lexical verbs per genre
    for g, fid, i, t in masc_trees(root_dir):
        raw = unwrap(t)
        if is_leaf(raw):
            continue
        root = vf.Node(raw)
        vf.number(root)
        tid = '%s/%s#%d' % (g, fid, i)
        for vp in vf.nodes(root):
            if vp.cat != 'VP' or vf.is_aux_vp(vp):
                continue
            head = next((k for k in vp.kids if k.word is not None and k.cat in vf.VERB_TAGS), None)
            if head is None:
                continue
            n, child = vp.parent, vp
            chain = [vp.label]
            while n is not None and n.cat == 'VP':
                chain.append(n.label)
                child, n = n, n.parent
            totals[g] += 1
            if n is not None and n.cat in vf.CLAUSES:
                continue
            att, top = n, child
            cls = classify(att, top, head, chain, g)
            if att is not None:
                i = att.kids.index(top)
                left = ' '.join(k.label for k in att.kids[:i])
                right = ' '.join(k.label for k in att.kids[i + 1:])
                attwords = ' '.join(vf.words(att.raw))
            else:
                left = right = attwords = ''
            rec = {'id': tid, 'genre': g, 'mode': 'spoken' if g in SPOKEN else 'written',
                   'verb': head.word, 'tag': head.cat, 'pos': head.pos,
                   'att': lab(att), 'attparent': lab(att.parent) if att is not None else None,
                   'chain': chain, 'left': left, 'right': right,
                   'vpwords': ' '.join(vf.words(top.raw)), 'attwords': attwords[:200],
                   'empty_objects': direct_empty_objects(vp), 'empties': empties(top),
                   'ancestors': [a.label for a in ancestors(att)] if att is not None else [],
                   'class': cls,
                   'attpretty': pretty(att.raw)[:600] if att is not None else pretty(raw)[:600],
                   'sentence': ' '.join(vf.words(raw))[:300]}
            out.write(json.dumps(rec) + '\n')
            by[cls] += 1
            bygenre[rec['mode'], cls] += 1
            bygenre_full[g][group(cls)] += 1
    out.close()
    for c, k in by.most_common():
        print('%5d  %-28s spoken %4d  written %4d' % (k, c, bygenre['spoken', c], bygenre['written', c]))
    print(sum(by.values()))
    genre_table(bygenre_full, totals)


GROUPS = ['RR', 'UCP', 'NOM', 'ROOT', 'FRAG', 'EDIT', 'ERR', 'OTH']


def group(c):
    """The group a class belongs to, for the genre table."""
    if c.startswith('reduced relative'):
        return 'RR'
    if c.startswith('UCP: unlike'):
        return 'UCP'
    if c in ('VP as nominal or compound modifier', 'title or headline', 'citation formula (REF)'):
        return 'NOM'
    if c.startswith('root'):
        return 'ROOT'
    if c.startswith('FRAG') or c.startswith('UCP: in') or c == 'stage direction':
        return 'FRAG'
    if c.startswith('disfluency'):
        return 'EDIT'
    if c.startswith('error') or c in ('SBAR without S', 'finite or base VP inside a phrase'):
        return 'ERR'
    return 'OTH'


def genre_table(t, totals):
    print('| genre | verbs | ' + ' | '.join(GROUPS) + ' | all | per 1000 |')
    def row(name, c, n):
        k = sum(c.values())
        return '| %s | %d | %s | %d | %.1f |' % (name, n, ' | '.join(str(c[x]) for x in GROUPS), k, 1000 * k / n)
    everything = collections.Counter()
    for mode in ('spoken', 'written'):
        gs = sorted((g for g in totals if (g in SPOKEN) == (mode == 'spoken')),
                    key=lambda g: -sum(t[g].values()) / totals[g])
        agg = collections.Counter()
        for g in gs:
            print(row(g, t[g], totals[g]))
            agg.update(t[g])
        everything.update(agg)
        print(row('**%s total**' % mode, agg, sum(totals[g] for g in gs)))
    print(row('**all**', everything, sum(totals.values())))


def show(root_dir, ids):
    """python3 noclause.py MASC_DATA_DIR --show ID [ID ...]: print each tree."""
    want = set(ids)
    for g, fid, i, t in masc_trees(root_dir):
        tid = '%s/%s#%d' % (g, fid, i)
        if tid in want:
            print(tid)
            print(pretty(unwrap(t)))
            print()


if __name__ == '__main__':
    if sys.argv[2] == '--show':
        show(sys.argv[1], sys.argv[3:])
    else:
        main(sys.argv[1], sys.argv[2])
