"""MASC's verb PPs relabelled Comp or Mod by CGEL's tests, and Aleph's input
for learning from the new labels.

    uv run python cgel/relabel.py CGEL_EXAMPLES_TSV NLTK_DATA_DIR OUT_DIR [--v1]
        [--propbank=PROPBANK_MASC_DIR --frames=PROPBANK_FRAMES_DIR --programs=MASC_PROLOG_DIR]

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
    CGEL prepositional verb  the verb and preposition are in the lists of CGEL
                           ch. 4 §6.1.2 (prepositional_verbs.tsv): *see to*,
                           *accuse of*, *regard as* ...                       Comp
    locative with motion   an intransitive preposition, a verb of motion or
                           a VerbNet class with a spatial PP                   Comp
    MASC CLR               CLR, PUT or DTV                                     Comp
    VerbNet                a frame of the verb's classes has this preposition
                           (not for be, whose complement is its predicative;
                           for in, on and at, only where the frame gives its NP
                           a role of place: Location, Destination, Goal,
                           Source, Initial_Location)                          Comp
    goal of motion         with a verb of motion or a spatial class, a PP
                           tagged DIR, or untagged with to, into, onto,
                           toward(s) or from                                   Comp
    default                                                                     Mod

The rules of the first version were fixed before any CGELBank score of the
relabelled data was seen; the two refinements in brackets came from reading MASC's trees (PPs
after be's NP-PRD, which VerbNet cannot speak to; goals MASC leaves
untagged: *go to the closing statements*). The VerbNet rule rests on the
adjudication sample, where PPs VerbNet licenses and MASC leaves untagged
were complements 13 times in 14; CGEL counts goals of motion as
complements.

It writes OUT_DIR/relabelled.tsv (each example with its new label and the
rule that gave it) and, as build.py does, Aleph's input for the four
backgrounds (with copula/1, the verb is be, and in the second version
cgel_lex/1, the pair is in the lexicon, added to the common predicates),
test.pl and table.tsv; and it prints the counts.
"""
import collections
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
from build import (MODES_COMMON, PREDICATES, Senses, VerbNet, doc_of,  # noqa: E402
                   held_out, make_facts, write)
from evaluate_cgel import ADVERBIAL_P, MOTION, TIME_P  # noqa: E402
from lexicon import prepositional_verbs  # noqa: E402

LEXICON = prepositional_verbs()
POSITION_P = {'in', 'on', 'at'}
PLACE_ROLES = {'Location', 'Destination', 'Goal', 'Source', 'Initial_Location'}

GOAL_P = {'to', 'into', 'onto', 'toward', 'towards', 'from'}
RULES = ['agent', 'predicative', 'time', 'adverbial clause', 'now/then/so',
         'CGEL prepositional verb', 'locative with motion', 'PropBank', 'MASC CLR', 'VerbNet', 'goal of motion', 'default']


def pb_comp(label):
    """A PropBank label read as CGEL would: numbered arguments, and goals and
    directions of motion (ARGM-DIR, ARGM-GOL; CGEL §5.2), are complements"""
    label = re.sub(r'^[RC]-', '', label)
    return not label.startswith('ARGM') or label in ('ARGM-DIR', 'ARGM-GOL')


def relabel(kind, labels, f, v1=False):
    """(complement?, rule). f has vn_roles, the VerbNet roles of the NP after
    this preposition, for the second version."""
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
    if not v1 and f['cgel_lex']:
        return True, 'CGEL prepositional verb'
    if kind == 'advp' and motion:
        return True, 'locative with motion'
    if f.get('pb'):
        return pb_comp(f['pb']), 'PropBank'
    if {'clr', 'put', 'dtv'} & labels:
        return True, 'MASC CLR'
    if f['vn_prep'] and not f['copula'] and \
            (v1 or f['prep'] not in POSITION_P or f['vn_roles'] & PLACE_ROLES):
        return True, 'VerbNet'
    if motion and ('dir' in labels or not labels and f['prep'] in GOAL_P):
        return True, 'goal of motion'
    return False, 'default'


def read(tsv, senses, vn, propbank=None, fr=None):
    """(doc, path, pp, kind, labels, facts) for each row; with propbank (from
    propbank_roles.masc) and fr (propbank_roles.frames), facts has pb, the
    PropBank label of the PP, and role, its roles"""
    from propbank_roles import covering, roles
    for line in open(tsv, encoding='utf-8'):
        cols = line.rstrip('\n').split('\t')
        (path, kind, pp, lemma, vtag, prep, ocat, ohead, otag, nxt, obefore, nsib, voice,
         labels) = cols[:14]
        labels = set(labels.strip('[]').split(',')) - {''}
        f = make_facts(senses, vn, lemma, vtag, prep, ocat, ohead, otag, nxt, obefore, nsib, voice)
        f['cgel_lex'] = (lemma, prep) in LEXICON
        f['vn_roles'] = vn.roles(lemma, prep) if f['vn_prep'] else set()
        f['pb'], f['role'] = None, []
        if propbank is not None and len(cols) >= 17:
            hit = covering(propbank.get((path, int(cols[14])), []), int(cols[15]), int(cols[16]))
            if hit:
                f['pb'], f['role'] = hit[0], roles(hit[0], hit[1], fr)
        yield doc_of(path), path, pp, kind, labels, f


def main(tsv, nltk_dir, out, v1=False, propbank=None, frames_dir=None, programs=None):
    senses, vn = Senses(nltk_dir), VerbNet(nltk_dir)
    os.makedirs(out, exist_ok=True)
    pb = fr = None
    if propbank:
        from propbank_roles import frames, masc
        pb, fr = masc(propbank, programs), frames(frames_dir)
    data, counts, covered = [], collections.Counter(), []
    with open(os.path.join(out, 'relabelled.tsv'), 'w') as o:
        o.write('doc\tpath\tpp\tkind\tlemma\tprep\tmasc\tcgel\trule\n')
        for doc, path, pp, kind, labels, f in read(tsv, senses, vn, pb, fr):
            comp, rule = relabel(kind, labels, f, v1)
            masc = bool({'clr', 'put', 'dtv'} & labels)
            counts[rule, masc] += 1
            data.append((doc, path, pp, 'complement' if comp else 'adjunct', f))
            if f['pb']:
                covered.append(data[-1])
            o.write('\t'.join((doc, path, pp, kind, f['lemma'], f['prep'],
                               ','.join(sorted(labels)) or '-', 'Comp' if comp else 'Mod',
                               rule)) + '\n')
    print('%d examples, %d Comp by the new labels, %d by MASC (CLR, PUT, DTV)' % (
        len(data), sum(d[3] == 'complement' for d in data), sum(n for (_, m), n in counts.items() if m)))
    print('\n%-22s %7s %s' % ('rule', 'decides', 'of which MASC tags CLR/PUT/DTV'))
    for r in RULES:
        print('%-22s %7d %7d' % (r, counts[r, True] + counts[r, False], counts[r, True]))
    print()
    if pb is not None:
        print('\nthe %d PPs PropBank covers, for the role experiment:' % len(covered))
        common = PREDICATES + ['copula', 'cgel_lex']
        modes = MODES_COMMON + ':- modeb(1, copula(+ex)).\n:- modeb(1, cgel_lex(+ex)).\n'
        write(covered, os.path.join(out, 'roles', 'without'), common, modes)
        write(covered, os.path.join(out, 'roles', 'with'), common + ['role'],
              modes + ':- modeb(*, role(+ex, #role)).\n')
        print()
    if v1:
        write(data, out, PREDICATES + ['copula'], MODES_COMMON + ':- modeb(1, copula(+ex)).\n')
    else:
        write(data, out, PREDICATES + ['copula', 'cgel_lex'],
              MODES_COMMON + ':- modeb(1, copula(+ex)).\n:- modeb(1, cgel_lex(+ex)).\n')


if __name__ == '__main__':
    opts = dict(a[2:].split('=', 1) for a in sys.argv[1:] if a.startswith('--') and '=' in a)
    main(*[a for a in sys.argv[1:] if not a.startswith('--')][:3], v1='--v1' in sys.argv,
         propbank=opts.get('propbank'), frames_dir=opts.get('frames'), programs=opts.get('programs'))
