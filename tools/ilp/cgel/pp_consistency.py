"""How consistently do CGELBank's trees label a VP's PPs Comp vs Mod (vs Supplement)?

    cd $CGEL && uv run --with-requirements requirements.txt \
        python $QUADRUPLET/tools/ilp/cgel/pp_consistency.py $CGEL OUT_JSON

CGEL is a clone of nert-nlp/cgel; the script reads the trees with its cgel.py.
It prints, for the gold trees (ewt, twitter, ewt-test_iaa50, ewt-test_pilot5):
the PPs attached in a VP and their functions; the verb+preposition pairs seen
twice or more and those with mixed labels; Comp/Mod/Supplement by preposition;
two checks of the manual's VP branching rules (a complement sister to a
modifier; a complement above a modifier layer, which the manual allows for a
pre-complement modifier); and, in the agreement study (datasets/iaa), the PPs
whose labels differ between the two annotators and the adjudicated version.
OUT_JSON holds the rows behind each count.

Results, 2026-10-01 (nert-nlp/cgel at d0a2c2d): of the 426 PPs in the gold
trees, 163 are Comp or Mod of a VP (82 Mod, 81 Comp, counted by parent and
function); this script finds the verb and preposition of 150 VP PPs (72 Comp,
67 Mod, 9 Particle, 2 PredComp). 5 verb+preposition pairs repeat, 2 with mixed
labels, both principled; no breach of the branching rules (the 20 complements above a
modifier layer are the sanctioned case); in the agreement study 28 VP PPs
match across the three versions, 12 differ, 10 of them Comp vs Mod between
the annotators.
"""
import collections
import json
import os
import sys

REPO = sys.argv[1]
sys.path.insert(0, REPO)
import cgel  # noqa: E402

GOLD = ['datasets/ewt.cgel', 'datasets/twitter.cgel', 'datasets/ewt-test_iaa50.cgel', 'datasets/ewt-test_pilot5.cgel']
TRIAL = ['datasets/trial/ewt-trial.cgel', 'datasets/trial/twitter-etc-trial.cgel']
IAA = {w: 'datasets/iaa/ewt-test_iaa50.%s.cgel' % w
       for w in ('nschneid.validator', 'brettrey.validator', 'adjudicated')}
FUNCS = ('Comp', 'Mod', 'Supplement', 'Particle', 'Obj', 'PredComp')


def kids(t, n):
    return t.children.get(n, [])


def head_child(t, n):
    return next((c for c in kids(t, n) if t.tokens[c].deprel in ('Head', 'Head-Prenucleus', 'Marker-Head')), None)


def lexical_head(t, n, cats):
    while n is not None and t.tokens[n].constituent not in cats:
        n = head_child(t, n)
    return n


def leaves(t, n):
    if t.tokens[n].text is not None or t.tokens[n].constituent == 'GAP':
        return [n]
    return [x for c in kids(t, n) for x in leaves(t, c)]


def words(t, n):
    return ' '.join(t.tokens[x].text or '_' for x in leaves(t, n))


def lemma(node):
    return (node.lemma or node.text or '').lower()


def pps(t):
    """(PP node, its function, the VP it attaches to, verb lemma, preposition lemma, has object)."""
    for n, node in t.tokens.items():
        if node.constituent != 'PP' or node.deprel not in FUNCS:
            continue
        par = node.head
        if par is None or par < 0 or par not in t.tokens or t.tokens[par].constituent != 'VP':
            continue
        v = lexical_head(t, par, ('V', 'Vaux'))
        p = lexical_head(t, n, ('P',))
        if v is None or p is None:
            continue
        has_obj = any(t.tokens[c].deprel in ('Obj', 'Comp') for c in kids(t, n))
        yield n, node.deprel, par, lemma(t.tokens[v]), lemma(t.tokens[p]), has_obj


def structure(t):
    """Breaches of the manual's VP rules: a Comp sister to a Mod; a Comp attached
    above a VP layer that holds a Mod (allowed only for postposed complements)."""
    sister, above = [], []
    for n, node in t.tokens.items():
        if node.constituent != 'VP':
            continue
        fx = [t.tokens[c].deprel for c in kids(t, n)]
        if 'Mod' in fx and any(f in ('Comp', 'Obj', 'PredComp') for f in fx):
            sister.append(words(t, n))
        if any(f == 'Comp' for f in fx):
            h = head_child(t, n)
            while h is not None and t.tokens[h].constituent == 'VP':
                if any(t.tokens[c].deprel == 'Mod' for c in kids(t, h)):
                    above.append(words(t, n))
                    break
                h = head_child(t, h)
    return sister, above


def load(paths):
    out = []
    for p in paths:
        with open(os.path.join(REPO, p), encoding='utf-8') as f:
            for t in cgel.trees(f):
                out.append((p, t))
    return out


def main(out_path):
    gold = load(GOLD)
    rows, sisters, aboves = [], [], []
    for p, t in gold:
        for n, f, par, v, prep, obj in pps(t):
            rows.append(dict(file=p, sent=t.sentid, func=f, verb=v, prep=prep, obj=obj, pp=words(t, n),
                             vp=words(t, par)))
        s, a = structure(t)
        sisters += [(t.sentid, x) for x in s]
        aboves += [(t.sentid, x) for x in a]
    funcs = collections.Counter(r['func'] for r in rows)
    by_prep = collections.defaultdict(collections.Counter)
    by_pair = collections.defaultdict(list)
    for r in rows:
        if r['func'] in ('Comp', 'Mod', 'Supplement'):
            by_prep[r['prep']][r['func']] += 1
            by_pair[(r['verb'], r['prep'])].append(r)
    repeated = {k: v for k, v in by_pair.items() if len(v) >= 2}
    mixed = {k: v for k, v in repeated.items() if len({r['func'] for r in v}) > 1}
    print('trees', len(gold), '| PPs in VPs', len(rows), dict(funcs))
    print('verb+prep pairs seen twice or more:', len(repeated), '| with mixed labels:', len(mixed))
    print('prepositions (Comp/Mod/Supplement), n >= 8:')
    for prep, c in sorted(by_prep.items(), key=lambda x: -sum(x[1].values())):
        if sum(c.values()) >= 8:
            print('   %-8s %3d  Comp %3d  Mod %3d  Supp %2d' % (prep, sum(c.values()), c['Comp'], c['Mod'], c['Supplement']))
    print('structure: Comp/Obj sister to Mod in one VP:', len(sisters), '| Comp above a Mod layer:', len(aboves))

    # the agreement study: the same PP (by its words and position) in each annotator's tree
    iaa = {w: load([p]) for w, p in IAA.items()}
    labels = collections.defaultdict(dict)
    for w, ts in iaa.items():
        for _, t in ts:
            for n, f, par, v, prep, obj in pps(t):
                span = tuple(t.tokens[x].text for x in leaves(t, n))
                labels[(t.sentid, span)][w] = (f, v, prep)
    dis = []
    for k, d in labels.items():
        fs = {w: x[0] for w, x in d.items()}
        if len(fs) == 3 and len(set(fs.values())) > 1:
            dis.append(dict(sent=k[0], pp=' '.join(s or '_' for s in k[1]), verb=d['adjudicated'][1], **fs))
    both = sum(1 for d in labels.values() if len(d) == 3)
    cm = [x for x in dis if {x['nschneid.validator'], x['brettrey.validator']} == {'Comp', 'Mod'}]
    print('agreement study: PPs in VPs found in all three versions:', both, '| labels differ:', len(dis),
          '| of which Comp vs Mod between annotators:', len(cm))
    json.dump(dict(rows=rows, mixed={'%s+%s' % k: v for k, v in mixed.items()}, sisters=sisters,
                   aboves=aboves, iaa=dis), open(out_path, 'w'), ensure_ascii=False, indent=1)


if __name__ == '__main__':
    main(sys.argv[2])
