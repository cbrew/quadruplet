"""Aleph's input for learning which PPs are complements, from the examples
pp_examples.pl draws off MASC's Prolog programs; and the baselines.

    uv run python build.py EXAMPLES_TSV WORDNET_DIR OUT_DIR

writes, in OUT_DIR, for two sets of background predicates:

    lexical/complement.{b,f,n}   with the verb's lemma and the object's head word
    classes/complement.{b,f,n}   without them: WordNet supersenses and syntax only

and test.pl (the held-out examples' facts and classes, for evaluate.pl), and
prints the baselines on the held-out examples. The documents are split as
tools/frames/src/frames/evaluate.py splits them: a fifth held out, by a hash
of the document's name.

An example is a PP that is a daughter of a lexical verb's VP. It is a
complement where the treebank labels it CLR, PUT or DTV; an adjunct where it
has none of these (predicative PPs and passive agents are left out). The
background facts of an example E:

    lemma(E, L)         the verb's lemma                     (lexical only)
    obj_head(E, H)      the head word of the PP's object     (lexical only)
    prep(E, P)          the preposition
    verb_sense(E, S)    the verb's WordNet supersense, first sense: verb.motion ...
    obj_sense(E, S)     the object's: noun.time ...; pronoun; clause (a clausal
                        object); trace (a stranded preposition); none
    obj_cat(E, C)       np, s, sbar, ...
    vtag(E, T)          the verb's tag
    next(E)             the PP comes straight after the verb
    obj_before(E)       an NP object stands between the verb and the PP
    other_pp(E)         the VP has another PP
    passive(E)          the verb is a passive participle
"""
import collections
import hashlib
import os
import re
import sys

import nltk

MODES_COMMON = """\
:- modeh(1, complement(+ex)).
:- modeb(1, prep(+ex, #prep)).
:- modeb(1, verb_sense(+ex, #vsense)).
:- modeb(1, obj_sense(+ex, #osense)).
:- modeb(1, obj_cat(+ex, #cat)).
:- modeb(1, vtag(+ex, #vtag)).
:- modeb(1, next(+ex)).
:- modeb(1, obj_before(+ex)).
:- modeb(1, other_pp(+ex)).
:- modeb(1, passive(+ex)).
"""
MODES_LEXICAL = """\
:- modeb(1, lemma(+ex, #lemma)).
:- modeb(1, obj_head(+ex, #head)).
"""
PREDICATES = ['prep', 'verb_sense', 'obj_sense', 'obj_cat', 'vtag', 'next', 'obj_before',
              'other_pp', 'passive']
LEXICAL = ['lemma', 'obj_head']

SETTINGS = """\
:- set(clauselength, 4).
:- set(minpos, 15).
:- set(minacc, 0.6).
:- set(noise, 1000000).
:- set(nodes, 10000).
:- set(verbose, 0).
"""


def held_out(doc, folds=5):
    return int(hashlib.md5(doc.encode()).hexdigest(), 16) % folds == 0


def atom(s):
    s = str(s)
    if re.fullmatch(r'[a-z][A-Za-z0-9_]*', s):
        return s
    return "'" + s.replace('\\', '\\\\').replace("'", "\\'") + "'"


class Senses:
    """First-sense WordNet supersenses, cached."""

    def __init__(self, wordnet_dir):
        nltk.data.path.insert(0, wordnet_dir)
        from nltk.corpus import wordnet
        self.wn = wordnet
        self.cache = {}

    def of(self, word, pos):
        key = (word, pos)
        if key not in self.cache:
            ss = self.wn.synsets(word.replace(' ', '_'), pos)
            self.cache[key] = ss[0].lexname() if ss else 'unknown'
        return self.cache[key]


def examples(tsv, senses):
    for line in open(tsv, encoding='utf-8'):
        f = line.rstrip('\n').split('\t')
        path, cls, pp, lemma, vtag, prep, ocat, ohead, otag, nxt, obefore, nsib, voice, labels = f
        doc = re.sub(r'^.*masc-prolog/', '', path).rsplit('/', 1)[0]
        if ohead == 'trace':
            osense = 'trace'
        elif ocat in ('s', 'sbar', 'sq', 'sbarq', 'sinv'):
            osense = 'clause'
        elif otag.startswith('prp') or otag in ('wp', 'wdt', 'ex'):
            osense = 'pronoun'
        elif otag.startswith('nn') or otag in ('cd',):
            osense = senses.of(ohead, 'n')
        else:
            osense = 'none'
        facts = {
            'lemma': lemma, 'obj_head': ohead, 'prep': prep,
            'verb_sense': senses.of(lemma, 'v'), 'obj_sense': osense, 'obj_cat': ocat,
            'vtag': vtag, 'next': nxt == 'next', 'obj_before': obefore == 'np',
            'other_pp': int(nsib) > 0, 'passive': voice == 'passive',
        }
        yield doc, path, pp, cls, facts


def fact_lines(eid, facts, preds):
    for p in preds:
        v = facts[p]
        if v is True:
            yield '%s(%s).' % (p, eid)
        elif v not in (False, None):
            yield '%s(%s, %s).' % (p, eid, atom(v))


def prior(train):
    """The rate table of tools/frames: P(complement | lemma, prep), smoothed
    towards P(complement | prep), and that towards P(complement)."""
    args, total = collections.Counter(), collections.Counter()
    for _, _, _, cls, f in train:
        for key in ((f['lemma'], f['prep']), (None, f['prep'])):
            total[key] += 1
            args[key] += cls == 'complement'
    overall = sum(cls == 'complement' for *_, cls, _ in train) / len(train)

    def p(lemma, prep):
        by_prep = (args[None, prep] + overall) / (total[None, prep] + 1)
        return (args[lemma, prep] + by_prep) / (total[lemma, prep] + 1)
    return p


def scores(pairs):
    tp = sum(1 for g, s in pairs if g and s)
    fp = sum(1 for g, s in pairs if not g and s)
    fn = sum(1 for g, s in pairs if g and not s)
    acc = sum(1 for g, s in pairs if g == s) / len(pairs)
    p = tp / (tp + fp) if tp + fp else 0.0
    r = tp / (tp + fn) if tp + fn else 0.0
    f = 2 * p * r / (p + r) if p + r else 0.0
    return 'accuracy %.1f  complement P %.1f R %.1f F %.1f' % (100 * acc, 100 * p, 100 * r, 100 * f)


def main(tsv, wordnet_dir, out):
    senses = Senses(wordnet_dir)
    data = list(examples(tsv, senses))
    train = [d for d in data if not held_out(d[0])]
    test = [d for d in data if held_out(d[0])]
    print('examples: %d train (%d complements), %d test (%d complements)' % (
        len(train), sum(d[3] == 'complement' for d in train),
        len(test), sum(d[3] == 'complement' for d in test)))

    for name, preds, modes in (('lexical', PREDICATES + LEXICAL, MODES_COMMON + MODES_LEXICAL),
                               ('classes', PREDICATES, MODES_COMMON)):
        d = os.path.join(out, name)
        os.makedirs(d, exist_ok=True)
        with open(os.path.join(d, 'complement.b'), 'w') as b, \
                open(os.path.join(d, 'complement.f'), 'w') as fpos, \
                open(os.path.join(d, 'complement.n'), 'w') as fneg:
            b.write(SETTINGS + modes)
            for p in preds:
                arity = 1 if p in ('next', 'obj_before', 'other_pp', 'passive') else 2
                b.write(':- determination(complement/1, %s/%d).\n' % (p, arity))
            b.write(':- discontiguous %s.\n' % ', '.join(
                '%s/%d' % (p, 1 if p in ('next', 'obj_before', 'other_pp', 'passive') else 2)
                for p in preds))
            for i, (_, _, _, cls, facts) in enumerate(train):
                eid = 'e%d' % i
                for line in fact_lines(eid, facts, preds):
                    b.write(line + '\n')
                (fpos if cls == 'complement' else fneg).write('complement(%s).\n' % eid)

    with open(os.path.join(out, 'test.pl'), 'w') as t:
        t.write(':- discontiguous %s.\n' % ', '.join(
            '%s/%d' % (p, 1 if p in ('next', 'obj_before', 'other_pp', 'passive') else 2)
            for p in PREDICATES + LEXICAL))
        for i, (doc, path, pp, cls, facts) in enumerate(test):
            eid = 't%d' % i
            t.write('example(%s, %s, %s, %s).\n' % (eid, cls, atom(doc), pp))
            for line in fact_lines(eid, facts, PREDICATES + LEXICAL):
                t.write(line + '\n')

    # baselines on the held-out examples
    p = prior(train)
    seen = {(f['lemma'], f['prep']) for *_, f in train}
    gold = [cls == 'complement' for *_, cls, _ in test]
    print('baselines on the held-out examples:')
    print('  all adjuncts:           ', scores([(g, False) for g in gold]))
    print('  rate table (lemma, prep):', scores([(cls == 'complement', p(f['lemma'], f['prep']) >= 0.5)
                                                  for *_, cls, f in test]))
    unseen = [(cls == 'complement', p(f['lemma'], f['prep']) >= 0.5)
              for *_, cls, f in test if (f['lemma'], f['prep']) not in seen]
    print('  rate table, on the %d examples whose (lemma, prep) training never saw:' % len(unseen),
          scores(unseen))


if __name__ == '__main__':
    main(*sys.argv[1:4])
