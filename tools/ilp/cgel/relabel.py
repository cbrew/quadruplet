"""MASC's verb PPs relabelled Comp or Mod by CGEL's tests, and Aleph's input
for learning from the new labels.

    uv run python cgel/relabel.py CGEL_EXAMPLES_TSV NLTK_DATA_DIR OUT_DIR

CGEL_EXAMPLES_TSV is what pp_examples.pl's cgel_tsv writes: every PP of a
lexical verb in CGEL's sense (PPs, adverbial clauses led by a preposition,
intransitive prepositions), with MASC's function tags. Each is labelled by
the first of these that applies, CGEL's tests (those of evaluate_cgel.py)
with MASC's tags as evidence where the tests need it:

    agent                  the passive's by-phrase (LGS)                       Comp
    predicative            a predicative PP (PRD): *be in the room*            Comp
    time                   a PP of time (TMP), or an object of time             Mod
    adverbial clause       before, after, because, if ... with a clause         Mod
    now/then/so                                                                 Mod
    locative with motion   an intransitive preposition, a verb of motion or
                           a VerbNet class with a spatial PP                   Comp
    MASC CLR               CLR, PUT or DTV                                     Comp
    VerbNet                a frame of the verb's classes has this preposition
                           (not for be, whose complement is its predicative)   Comp
    goal of motion         with a verb of motion or a spatial class, a PP
                           tagged DIR, or untagged with to, into, onto,
                           toward(s) or from                                   Comp
    default                                                                     Mod

The rules were fixed before any CGELBank score of the relabelled data was
seen; the two refinements in brackets came from reading MASC's trees (PPs
after be's NP-PRD, which VerbNet cannot speak to; goals MASC leaves
untagged: *go to the closing statements*). The VerbNet rule rests on the
adjudication sample, where PPs VerbNet licenses and MASC leaves untagged
were complements 13 times in 14; CGEL counts goals of motion as
complements.

It writes OUT_DIR/relabelled.tsv (each example with its new label and the
rule that gave it) and, as build.py does, Aleph's input for the four
backgrounds (with copula/1, the verb is be, added to the common predicates),
test.pl and table.tsv; and it prints the counts.
"""
import collections
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
from build import (MODES_COMMON, PREDICATES, Senses, VerbNet, doc_of,  # noqa: E402
                   held_out, make_facts, write)
from evaluate_cgel import ADVERBIAL_P, MOTION, TIME_P  # noqa: E402

GOAL_P = {'to', 'into', 'onto', 'toward', 'towards', 'from'}
RULES = ['agent', 'predicative', 'time', 'adverbial clause', 'now/then/so',
         'locative with motion', 'MASC CLR', 'VerbNet', 'goal of motion', 'default']


def relabel(kind, labels, f):
    """(complement?, rule)"""
    motion = f['verb_sense'] in MOTION or f['vn_spatial']
    if 'lgs' in labels:
        return True, 'agent'
    if 'prd' in labels:
        return True, 'predicative'
    if 'tmp' in labels or f['obj_sense'] == 'noun.time':
        return False, 'time'
    if kind == 'sbar' and f['prep'] in ADVERBIAL_P:
        return False, 'adverbial clause'
    if f['prep'] in TIME_P:
        return False, 'now/then/so'
    if kind == 'advp' and motion:
        return True, 'locative with motion'
    if {'clr', 'put', 'dtv'} & labels:
        return True, 'MASC CLR'
    if f['vn_prep'] and not f['copula']:
        return True, 'VerbNet'
    if motion and ('dir' in labels or not labels and f['prep'] in GOAL_P):
        return True, 'goal of motion'
    return False, 'default'


def read(tsv, senses, vn):
    """(doc, path, pp, kind, labels, facts) for each row"""
    for line in open(tsv, encoding='utf-8'):
        (path, kind, pp, lemma, vtag, prep, ocat, ohead, otag, nxt, obefore, nsib, voice,
         labels) = line.rstrip('\n').split('\t')
        labels = set(labels.strip('[]').split(',')) - {''}
        yield doc_of(path), path, pp, kind, labels, make_facts(
            senses, vn, lemma, vtag, prep, ocat, ohead, otag, nxt, obefore, nsib, voice)


def main(tsv, nltk_dir, out):
    senses, vn = Senses(nltk_dir), VerbNet(nltk_dir)
    os.makedirs(out, exist_ok=True)
    data, counts = [], collections.Counter()
    with open(os.path.join(out, 'relabelled.tsv'), 'w') as o:
        o.write('doc\tpath\tpp\tkind\tlemma\tprep\tmasc\tcgel\trule\n')
        for doc, path, pp, kind, labels, f in read(tsv, senses, vn):
            comp, rule = relabel(kind, labels, f)
            masc = bool({'clr', 'put', 'dtv'} & labels)
            counts[rule, masc] += 1
            data.append((doc, path, pp, 'complement' if comp else 'adjunct', f))
            o.write('\t'.join((doc, path, pp, kind, f['lemma'], f['prep'],
                               ','.join(sorted(labels)) or '-', 'Comp' if comp else 'Mod',
                               rule)) + '\n')
    print('%d examples, %d Comp by the new labels, %d by MASC (CLR, PUT, DTV)' % (
        len(data), sum(d[3] == 'complement' for d in data), sum(n for (_, m), n in counts.items() if m)))
    print('\n%-22s %7s %s' % ('rule', 'decides', 'of which MASC tags CLR/PUT/DTV'))
    for r in RULES:
        print('%-22s %7d %7d' % (r, counts[r, True] + counts[r, False], counts[r, True]))
    print()
    write(data, out, PREDICATES + ['copula'], MODES_COMMON + ':- modeb(1, copula(+ex)).\n')


if __name__ == '__main__':
    main(*sys.argv[1:4])
