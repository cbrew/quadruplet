"""Aleph's input for learning which PPs are complements, from the examples
pp_examples.pl draws off MASC's Prolog programs; and the baselines.

    uv run python build.py EXAMPLES_TSV NLTK_DATA_DIR OUT_DIR

NLTK_DATA_DIR holds corpora/wordnet.zip and corpora/verbnet3/ (VerbNet 3.3).
It writes, in OUT_DIR, for four sets of background predicates:

    classes/complement.{b,f,n}          WordNet supersenses and syntax, no words
    lexical/complement.{b,f,n}          those and the verb's lemma and the object's head
    verbnet/complement.{b,f,n}          classes and VerbNet's, no words
    lexical_verbnet/complement.{b,f,n}  all of them

and test.pl (the held-out examples' facts and classes, and the (lemma, prep)
pairs training saw, for evaluate.pl), and
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
    vn_class(E, C)      a VerbNet class of the verb, its top class: give-13.1  (verbnet)
    vn_group(E, G)      the number of that class's Levin group: 13             (verbnet)
    vn_prep(E)          a frame of one of the verb's classes (or their          (verbnet)
                        superclasses) has a PP with this very preposition
    vn_spatial(E)       such a frame has a spatial PP slot (a preposition       (verbnet)
                        restricted by type, spatial, loc, dir ...), and this
                        preposition is spatial
    vn_none(E)          VerbNet does not have the verb                         (verbnet)
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
MODES_VERBNET = """\
:- modeb(*, vn_class(+ex, #vnclass)).
:- modeb(*, vn_group(+ex, #vngroup)).
:- modeb(1, vn_prep(+ex)).
:- modeb(1, vn_spatial(+ex)).
:- modeb(1, vn_none(+ex)).
"""
VERBNET = ['vn_class', 'vn_group', 'vn_prep', 'vn_spatial', 'vn_none']
UNARY = {'next', 'obj_before', 'other_pp', 'passive', 'vn_prep', 'vn_spatial', 'vn_none'}
SPATIAL = {'in', 'on', 'at', 'into', 'onto', 'to', 'from', 'toward', 'towards', 'through',
           'across', 'along', 'around', 'over', 'under', 'above', 'below', 'behind', 'between',
           'among', 'near', 'inside', 'outside', 'out', 'off', 'up', 'down', 'upon', 'within',
           'beside', 'beyond', 'past', 'via', 'against', 'throughout', 'underneath', 'by'}

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


class VerbNet:
    """Each verb's VerbNet classes and the prepositions their frames license."""

    def __init__(self, nltk_dir):
        from nltk.corpus.reader import VerbnetCorpusReader
        self.vn = VerbnetCorpusReader(os.path.join(nltk_dir, 'corpora', 'verbnet3'),
                                      r'(?!\.).*\.xml')
        self.cache = {}

    def frames(self, classid):
        """The frames of a class and of the classes above it."""
        parts = classid.split('-')
        out = []
        for k in range(2, len(parts) + 1):
            try:
                out += self.vn.frames(self.vn.vnclass('-'.join(parts[:k])))
            except Exception:
                pass
        return out

    def of(self, lemma):
        """(top classes, groups, explicit prepositions, has a spatial PP slot)"""
        if lemma not in self.cache:
            ids = self.vn.classids(lemma=lemma)
            tops = sorted({'-'.join(c.split('-')[:2]) for c in ids})
            groups = sorted({c.split('-')[1].split('.')[0] for c in ids})
            preps, spatial = set(), False
            for c in ids:
                for fr in self.frames(c):
                    for syn in fr['syntax']:
                        if syn['pos_tag'] != 'PREP':
                            continue
                        value = syn['modifiers'].get('value') or ''
                        if value:
                            preps.update(value.lower().split())
                        elif syn['modifiers'].get('selrestrs'):
                            spatial = True
            self.cache[lemma] = (tops, groups, preps, spatial)
        return self.cache[lemma]


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


def examples(tsv, senses, verbnet):
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
        tops, groups, preps, spatial = verbnet.of(lemma)
        facts.update({'vn_class': tops, 'vn_group': groups, 'vn_prep': prep in preps,
                      'vn_spatial': spatial and prep in SPATIAL, 'vn_none': not tops})
        yield doc, path, pp, cls, facts


def fact_lines(eid, facts, preds):
    for p in preds:
        v = facts[p]
        if isinstance(v, list):
            for x in v:
                yield '%s(%s, %s).' % (p, eid, atom(x))
        elif v is True:
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


def arity(p):
    return 1 if p in UNARY else 2


def main(tsv, nltk_dir, out):
    senses = Senses(nltk_dir)
    data = list(examples(tsv, senses, VerbNet(nltk_dir)))
    train = [d for d in data if not held_out(d[0])]
    test = [d for d in data if held_out(d[0])]
    print('examples: %d train (%d complements), %d test (%d complements)' % (
        len(train), sum(d[3] == 'complement' for d in train),
        len(test), sum(d[3] == 'complement' for d in test)))

    for name, preds, modes in (
            ('lexical', PREDICATES + LEXICAL, MODES_COMMON + MODES_LEXICAL),
            ('classes', PREDICATES, MODES_COMMON),
            ('verbnet', PREDICATES + VERBNET, MODES_COMMON + MODES_VERBNET),
            ('lexical_verbnet', PREDICATES + LEXICAL + VERBNET,
             MODES_COMMON + MODES_LEXICAL + MODES_VERBNET)):
        d = os.path.join(out, name)
        os.makedirs(d, exist_ok=True)
        with open(os.path.join(d, 'complement.b'), 'w') as b, \
                open(os.path.join(d, 'complement.f'), 'w') as fpos, \
                open(os.path.join(d, 'complement.n'), 'w') as fneg:
            b.write(SETTINGS + modes)
            for p in preds:
                b.write(':- determination(complement/1, %s/%d).\n' % (p, arity(p)))
            b.write(':- discontiguous %s.\n' % ', '.join('%s/%d' % (p, arity(p)) for p in preds))
            for i, (_, _, _, cls, facts) in enumerate(train):
                eid = 'e%d' % i
                for line in fact_lines(eid, facts, preds):
                    b.write(line + '\n')
                (fpos if cls == 'complement' else fneg).write('complement(%s).\n' % eid)

    with open(os.path.join(out, 'test.pl'), 'w') as t:
        t.write(':- discontiguous example/4, %s.\n' % ', '.join(
            '%s/%d' % (p, arity(p)) for p in PREDICATES + LEXICAL + VERBNET))
        # the (lemma, prep) pairs training saw, to single out those it did not
        for lemma, prep in sorted({(f['lemma'], f['prep']) for *_, f in train}):
            t.write('trained(%s, %s).\n' % (atom(lemma), atom(prep)))
        for i, (doc, path, pp, cls, facts) in enumerate(test):
            eid = 't%d' % i
            t.write('example(%s, %s, %s, %s).\n' % (eid, cls, atom(doc), pp))
            for line in fact_lines(eid, facts, PREDICATES + LEXICAL + VERBNET):
                t.write(line + '\n')

    # baselines on the held-out examples, and the table's prediction for each,
    # with whether training saw its (lemma, prep) pair, for combine.py
    p = prior(train)
    seen = {(f['lemma'], f['prep']) for *_, f in train}
    with open(os.path.join(out, 'table.tsv'), 'w') as t:
        for i, (*_, cls, f) in enumerate(test):
            t.write('t%d\t%s\t%d\t%d\n' % (i, cls, p(f['lemma'], f['prep']) >= 0.5,
                                            (f['lemma'], f['prep']) in seen))
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
