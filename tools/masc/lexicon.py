"""Generates the lexicon of masc.fcfg from the sample's normalised trees.

    python3 tools/masc/lexicon.py src/test/resources/masc

Reads sample.mrg and rewrites everything after the "generated lexicon" line of
masc.fcfg. Closed-class words (pronouns, determiners, auxiliaries, wh-words,
conjunctions, ...) come from the tables below. Open-class words get a
semantic template for their part of speech; a verb gets one entry per
subcategorisation frame it has in the sample, read from the complements
beside it in the tree. Needs lemminflect (pip install lemminflect).
"""
import collections
import re
import sys

from lemminflect import getLemma

from masctrees import is_leaf, normalise, read_trees, unwrap

MARKER = '# generated lexicon'

# ---------------------------------------------------------------------------
# closed classes

EXISTS = r'\P Q.exists x.(P(x) & Q(x))'
EVERY = r'\P Q.all x.(P(x) -> Q(x))'
NO = r'\P Q.-exists x.(P(x) & Q(x))'
THE = r'\P Q.exists x.((P(x) & all y.(P(y) -> (x = y))) & Q(x))'


def demonstrative(name):
    return r'\P Q.exists x.((P(x) & %s(x)) & Q(x))' % name


def gq(name):
    """A generalised quantifier as a relation between properties."""
    return r'\P Q.%s(P, Q)' % name


DETERMINERS = {
    'a': ('3sg', EXISTS), 'an': ('3sg', EXISTS), 'another': ('3sg', EXISTS),
    'some': (None, EXISTS), 'any': (None, EXISTS), 'either': ('3sg', EXISTS),
    'the': (None, THE),
    'every': ('3sg', EVERY), 'each': ('3sg', EVERY), 'all': ('non3sg', EVERY),
    'both': ('non3sg', EVERY),
    'no': (None, NO), 'neither': ('3sg', NO),
    'this': ('3sg', demonstrative('this')), 'that': ('3sg', demonstrative('that')),
    'these': ('non3sg', demonstrative('this')), 'those': ('non3sg', demonstrative('that')),
    'many': ('non3sg', gq('many')), 'few': ('non3sg', gq('few')),
    'several': ('non3sg', gq('several')), 'most': ('non3sg', gq('most')),
    'half': (None, gq('half')),
}

# determiners standing alone as noun phrases: demonstratives name what
# they point at; the rest quantify over things
DEMONSTRATIVES = {'this', 'that'}

# personal pronouns: constant, agreement, case (None: either)
PRONOUNS = {
    'I': ('speaker', 'non3sg', 'nom'), 'i': ('speaker', 'non3sg', 'nom'),
    'me': ('speaker', 'non3sg', 'acc'), 'myself': ('speaker', 'non3sg', 'acc'),
    'you': ('hearer', 'non3sg', None), 'yourself': ('hearer', 'non3sg', 'acc'),
    'ya': ('hearer', 'non3sg', None),
    'he': ('he', '3sg', 'nom'), 'him': ('he', '3sg', 'acc'), 'himself': ('he', '3sg', 'acc'),
    'she': ('she', '3sg', 'nom'), 'her': ('she', '3sg', 'acc'), 'herself': ('she', '3sg', 'acc'),
    'it': ('it', '3sg', None), 'itself': ('it', '3sg', 'acc'),
    'we': ('we', 'non3sg', 'nom'), 'us': ('we', 'non3sg', 'acc'), "'s": ('we', 'non3sg', 'acc'),
    'ourselves': ('we', 'non3sg', 'acc'),
    'they': ('they', 'non3sg', 'nom'), 'them': ('they', 'non3sg', 'acc'),
    "'em": ('they', 'non3sg', 'acc'), 'themselves': ('they', 'non3sg', 'acc'),
    'one': ('one', '3sg', None),
}

POSSESSIVES = {'my': 'speaker', 'your': 'hearer', 'his': 'he', 'her': 'she',
               'its': 'it', 'our': 'we', 'their': 'they'}

# quantified pronouns, tagged NN: determiner and restriction
QUANT_NOUNS = {
    'everything': (EVERY, 'thing'), 'everyone': (EVERY, 'person'), 'everybody': (EVERY, 'person'),
    'something': (EXISTS, 'thing'), 'someone': (EXISTS, 'person'), 'somebody': (EXISTS, 'person'),
    'anything': (EXISTS, 'thing'), 'anyone': (EXISTS, 'person'), 'anybody': (EXISTS, 'person'),
    'nothing': (NO, 'thing'), 'nobody': (NO, 'person'), 'noone': (NO, 'person'),
}

AND = r'\p q.(p & q)'
OR = r'\p q.(p | q)'
CONJUNCTIONS = {'and': AND, 'but': AND, 'yet': AND, 'so': AND, 'plus': AND,
                'or': OR, 'nor': OR}

NEGATIONS = {'not', "n't", 'n?t', 'nt', 'never_'}

WH = {  # word: (restriction, can head a free relative)
    'who': ('person', 'no'), 'whom': ('person', 'no'), 'what': ('thing', 'yes'),
    'which': ('thing', 'no'), 'that': ('thing', 'no'), 'whatever': ('thing', 'yes'),
}

MODALS = {'can': 'can', 'ca': 'can', 'could': 'could', 'will': 'will', 'wo': 'will',
          "'ll": 'will', 'rsquoll': 'will', 'would': 'would', "'d": 'would',
          'shall': 'shall', 'should': 'should', 'may': 'may', 'might': 'might',
          'must': 'must', 'ought': 'ought'}

# be, have and do: vform and agreement of each form (agreement None: any)
BE = {'be': ('base', None), 'am': ('fin', 'non3sg'), "'m": ('fin', 'non3sg'),
      'is': ('fin', '3sg'), "'s": ('fin', '3sg'), 'rsquos': ('fin', '3sg'),
      'are': ('fin', 'non3sg'), "'re": ('fin', 'non3sg'),
      'was': ('fin', None), 'were': ('fin', None), 'been': ('en', None), 'being': ('ing', None)}
HAVE = {'have': [('base', None), ('fin', 'non3sg')], "'ve": [('fin', 'non3sg')],
        'has': [('fin', '3sg')], "'s": [('fin', '3sg')], 'had': [('fin', None), ('en', None)],
        "'d": [('fin', None)], 'having': [('ing', None)]}
DO = {'do': [('base', None), ('fin', 'non3sg')], 'does': [('fin', '3sg')],
      'did': [('fin', None)], 'done': [('en', None)], 'doing': [('ing', None)]}

VFORM = {'VB': ('base', None), 'VBP': ('fin', 'non3sg'), 'VBZ': ('fin', '3sg'),
         'VBD': ('fin', None), 'VBN': ('en', None), 'VBG': ('ing', None)}

SUBORDINATORS = {'if': r'\p q.(p -> q)'}

LOCATIVES = {'here', 'there', 'nowhere', 'everywhere', 'somewhere', 'home', 'inside',
             'outside', 'around', 'away', 'out', 'up', 'down', 'in', 'on', 'off', 'back',
             'near', 'together', 'abroad', 'upstairs', 'downstairs', 'enough'}

RESERVED = {'all', 'forall', 'exists'}


def const(word):
    """A logic constant for a word: lower case letters, digits and _."""
    c = re.sub(r'[^a-z0-9_]+', '_', word.lower()).strip('_') or 'w'
    if not c[0].isalpha():
        c = 'w_' + c
    if c in RESERVED or re.fullmatch(r'[a-z][0-9]*', c):
        c += '_'
    return c


def lemma(word, upos):
    ls = getLemma(word.lower(), upos=upos)
    return ls[0] if ls else word.lower()


# ---------------------------------------------------------------------------
# reading the sample

def parse_word(tag, w):
    return w if tag == 'NNP' or w == 'I' else w.lower()


class Node:
    def __init__(self, t, parent=None):
        self.label = t[0]
        self.parent = parent
        if is_leaf(t):
            self.word, self.kids = parse_word(t[0], t[1]), []
        else:
            self.word, self.kids = None, [Node(k, self) for k in t[1:]]

    def leaf(self):
        return self.word is not None

    def siblings_after(self):
        ks = self.parent.kids
        return ks[ks.index(self) + 1:]

    def head_tag(self):
        """The tag of a VP's verb, or of the first word."""
        for k in self.kids:
            if k.leaf() and k.label in VFORM or k.label in ('MD', 'TO'):
                return k.label
        n = self
        while not n.leaf():
            n = n.kids[0]
        return n.label


def walk(n):
    yield n
    for k in n.kids:
        yield from walk(k)


def verb_frame(v):
    """The subcategorisation frame of a verb, from its complements, or
    ('aux', comp) when it takes a VP."""
    comps = [s for s in v.siblings_after() if s.label in ('NP', 'S', 'SBAR', 'VP', 'ADJP')
             and not (s.label == 'SBAR' and s.kids[0].label == 'IN' and s.kids[0].word != 'that')]
    labels = [c.label for c in comps]
    if 'VP' in labels:
        tag = comps[labels.index('VP')].head_tag()
        return ('aux', {'VB': 'base', 'VBN': 'pass', 'VBG': 'ing', 'TO': 'to'}.get(tag, 'base'))
    if not labels:
        return 'intr'
    if labels[:2] == ['NP', 'NP']:
        return 'ditr'
    first = comps[0]
    if first.label == 'ADJP':
        return 'pred'
    if first.label == 'SBAR':
        return 'qcomp' if first.kids[0].label in ('WHNP', 'WHADVP') else 'scomp'
    if first.label == 'S':
        return clause_frame(first, '')
    if first.label == 'NP':
        if len(comps) > 1 and comps[1].label == 'S':
            f = clause_frame(comps[1], 'np')
            if f in ('npvpto', 'npvpbase'):
                return f
        return 'tr'
    return 'intr'


def clause_frame(s, prefix):
    """The frame for a verb taking the clause s (after an object if prefix)."""
    kids = [k for k in s.kids if not empty(k)]  # a controlled subject
    ls = [k.label for k in kids]
    if ls == ['VP']:
        tag = kids[0].head_tag()
        f = {'TO': 'vpto', 'VBG': 'vping', 'VB': 'vpbase'}.get(tag)
        if f:
            return 'np' + f if prefix and f != 'vping' else f
    if ls == ['NP', 'VP'] and not prefix:
        tag = kids[1].head_tag()
        if tag == 'TO':
            return 'npvpto'
        if tag == 'VB':
            return 'npvpbase'
        if tag in ('VBZ', 'VBP', 'VBD', 'MD'):
            return 'scomp'
    return 'tr'


def empty(n):
    return n.label == 'NP' and len(n.kids) == 1 and n.kids[0].label == '-NONE-'


def is_passive(v):
    """A participle heading a VP that is the complement of be or get,
    possibly through a coordination of VPs."""
    if v.label != 'VBN':
        return False
    outer = v.parent.parent
    while outer is not None and outer.label in ('VP', 'SQ'):
        for k in outer.kids:
            if k.leaf() and (k.word in BE or lemma(k.word, 'VERB') in ('get', 'become')):
                return True
        if any(k.leaf() and (k.label in VFORM or k.label == 'MD') for k in outer.kids):
            return False
        outer = outer.parent
    return False


# ---------------------------------------------------------------------------
# entries

def fmap(cat, **fs):
    sem = fs.pop('sem', None)
    parts = ['%s=%s' % (k, v) for k, v in fs.items() if v is not None]
    if sem is not None:
        parts.append('sem=<%s>' % sem)
    return '%s[%s]' % (cat, ', '.join(parts))


def verb_sem(frame, pred):
    return {
        'intr': r'\x.%s(x)',
        'tr': r'\X y.X(\x.%s(y, x))',
        'ditr': r'\X Y z.X(\x.Y(\y.%s(z, x, y)))',
        'scomp': r'\p x.%s(x, p)',
        'qcomp': r'\Q x.%s(x, Q)',
        'vpto': r'\P x.%s(x, P(x))',
        'vping': r'\P x.%s(x, P(x))',
        'vpbase': r'\P x.%s(x, P(x))',
        'npvpto': r'\P X y.X(\x.%s(y, x, P(x)))',
        'npvpbase': r'\P X y.X(\x.%s(y, x, P(x)))',
        'pred': r'\P x.%s(x, P(x))',
    }[frame] % pred


def main(out_dir):
    # The trees keep their empty NPs, which show where a verb's object or a
    # clause's subject has gone.
    with open(out_dir + '/sample.mrg') as fh:
        trees = [Node(normalise(unwrap(t), keep_empty_np=True)) for t in read_trees(fh.read())]
    lex = collections.defaultdict(set)

    def add(word, entry):
        lex[word].add(entry)

    for w in BE:
        for f, a in [BE[w]]:
            add(w, fmap('Be', vform=f, agr=a))
            add(w, fmap('Aux', vform=f, agr=a, comp='ing', sem=r'\p.prog(p)'))
            add(w, fmap('Aux', vform=f, agr=a, comp='pass', sem=r'\p.p'))
    for w, forms in HAVE.items():
        for f, a in forms:
            add(w, fmap('Aux', vform=f, agr=a, comp='en', sem=r'\p.perf(p)'))
            add(w, fmap('Aux', vform=f, agr=a, comp='to', sem=r'\p.must(p)'))
    for w, forms in DO.items():
        for f, a in forms:
            if f in ('base', 'fin'):
                add(w, fmap('Aux', vform=f, agr=a, comp='base', sem=r'\p.p'))
    for w, m in MODALS.items():
        add(w, fmap('Aux', vform='fin', comp='to' if m == 'ought' else 'base',
                    sem=r'\p.%s(p)' % m))
    sample_words = set()
    verbs = collections.defaultdict(lambda: (set(), set()))

    for tree in trees:
        for n in walk(tree):
            if not n.leaf() or n.label == '-NONE-':
                continue
            w, tag = n.word, n.label
            sample_words.add(w)
            lw = w.lower()
            parent = n.parent.label if n.parent else ''
            if tag in ('NN', 'NNS'):
                if lw in QUANT_NOUNS:
                    d, r = QUANT_NOUNS[lw]
                    add(w, fmap('QPro', agr='3sg', restr=r'<\x.%s(x)>' % r, sem=d))
                else:
                    add(w, fmap('N', agr='3sg' if tag == 'NN' else 'non3sg',
                                sem=r'\x.%s(x)' % const(lemma(w, 'NOUN'))))
            elif tag == 'NNP':
                pass  # below, by maximal sequence
            elif tag == 'PRP':
                c, a, case = PRONOUNS.get(w, PRONOUNS.get(lw, (const(lw), '3sg', None)))
                add(w, fmap('NP', agr=a, case=case, expl='no', sem=r'\P.P(%s)' % c))
            elif tag == 'PRP$':
                add(w, fmap('Det', sem=r'\P Q.exists x.((P(x) & of(x, %s)) & Q(x))'
                            % POSSESSIVES.get(lw, const(lw))))
            elif tag == 'DT':
                if parent == 'NP' and len(n.parent.kids) == 1:
                    if lw in DEMONSTRATIVES:
                        add(w, fmap('NP', agr='3sg', expl='no', sem=r'\P.P(%s)' % lw))
                    else:
                        a, sem = DETERMINERS.get(lw, (None, EXISTS))
                        add(w, fmap('QPro', agr=a, restr=r'<\x.thing(x)>', sem=sem))
                if lw in DETERMINERS or not (parent == 'NP' and len(n.parent.kids) == 1):
                    a, sem = DETERMINERS.get(lw, (None, EXISTS))
                    add(w, fmap('Det', agr=a, sem=sem))
            elif tag == 'EX':
                add(w, fmap('NP', expl='there', sem=r'\P.P(there)'))
            elif tag in ('JJ', 'JJR', 'JJS'):
                pred = const(lw)
                add(w, fmap('A', subcat='none', sem=r'\x.%s(x)' % pred))
                if any(s.label == 'S' for s in n.siblings_after()) or \
                        (parent == 'ADJP' and any(s.label == 'S' for s in n.parent.siblings_after())):
                    add(w, fmap('A', subcat='vpto', sem=r'\P x.%s(x, P(x))' % pred))
            elif tag in ('RB', 'RBR', 'RBS'):
                if lw in NEGATIONS:
                    add(w, fmap('Neg', sem=r'\p.-p'))
                    continue
                add(w, fmap('Adv', sem=r'\p.%s(p)' % const(lw)))
                if lw in LOCATIVES:
                    add(w, fmap('A', subcat='none', sem=r'\x.%s(x)' % const(lw)))
            elif tag == 'RP':
                add(w, fmap('Prt', sem=r'\p.%s(p)' % const(lw)))
            elif tag in ('IN', 'TO'):
                if parent == 'PP':
                    add(w, fmap('P', sem=r'\x y.%s(x, y)' % const(lw)))
                elif parent == 'SBAR' and lw != 'that':
                    add(w, fmap('Sub', sem=SUBORDINATORS.get(lw, r'\p q.%s(q, p)' % const(lw))))
            elif tag == 'CC':
                add(w, fmap('Conj', sem=CONJUNCTIONS.get(lw, AND)))
            elif tag in ('WP', 'WDT'):
                r, free = WH.get(lw, ('thing', 'no'))
                add(w, fmap('Wh', free=free, sem=r'\x.%s(x)' % r))
            elif tag == 'WRB':
                add(w, fmap('WhAdv', sem=r'\p y.%s(p, y)' % const(lw)))
                if n.parent.parent is not None and n.parent.parent.label == 'SBAR' and \
                        n.parent.parent.parent is not None and \
                        n.parent.parent.parent.label in ('S', 'VP'):
                    add(w, fmap('Sub', sem=r'\p q.%s(q, p)' % const(lw)))
            elif tag == 'POS':
                add(w, fmap('Poss'))
            elif tag == 'MD':
                pass  # the MODALS table
            elif tag in VFORM:
                verb_entries(n, add, verbs)
        # proper names: maximal runs of NNP within a phrase
        for n in walk(tree):
            if n.leaf():
                continue
            run = []
            for k in n.kids + [None]:
                if k is not None and k.leaf() and k.label == 'NNP':
                    run.append(k)
                    continue
                if run:
                    phrase = ' '.join(r.word for r in run)
                    name = const('_'.join(r.word for r in run))
                    add(phrase, fmap('NP', agr='3sg', expl='no', sem=r'\P.P(%s)' % name))
                    first = n.kids.index(run[0])
                    after = k is not None and k.leaf() and k.label in ('NN', 'NNS', 'NNP')
                    if (first > 0 and n.kids[first - 1].label == 'DT') or after:
                        add(phrase, fmap('N', agr='3sg', sem=r'\x.%s(x)' % name))
                    run = []

    add_verbs(verbs, add)
    # phrases entered whole, when the sample has them
    phrases = {
        'a few': fmap('Det', agr='non3sg', sem=gq('few')),
        'had better': fmap('Aux', vform='fin', comp='base', sem=r'\p.should(p)'),
        "'d better": fmap('Aux', vform='fin', comp='base', sem=r'\p.should(p)'),
    }
    for tree in trees:
        text = ' ' + ' '.join(n.word for n in walk(tree) if n.leaf() and n.label != '-NONE-') + ' '
        for phrase, entry in phrases.items():
            if ' %s ' % phrase in text:
                add(phrase, entry)
    body = []
    for w in sorted(lex, key=lambda s: (s.lower(), s)):
        entries = sorted(lex[w])
        if w not in sample_words and ' ' not in w:
            continue  # table entries for words the sample lacks
        body.append('"%s": %s' % (w, ' | '.join(entries)))
    path = out_dir + '/masc.fcfg'
    with open(path) as fh:
        text = fh.read()
    head_end = text.index('\n', text.index(MARKER))
    head = text[:head_end + 1]
    rule_line = '#' * 76 + '\n'
    with open(path, 'w') as fh:
        fh.write(head + rule_line + '\n' + '\n'.join(body) + '\n')
    print('%d words and phrases, %d entries' % (len(body), sum(len(lex[w]) for w in lex)))


def verb_entries(v, add, verbs):
    """Adds entries for auxiliary-like uses of v at once, and records v's
    form and frames under its lemma in verbs."""
    w = v.word
    lw = w.lower()
    if lw in BE:
        return
    frame = verb_frame(v)
    vform, agr = VFORM[v.label]
    pred = const(lemma(w, 'VERB'))
    if isinstance(frame, tuple):
        if lw in HAVE or lw in DO:
            return  # the tables
        # a main verb taking a VP: "get graded", "had better watch"
        add(w, fmap('Aux', vform=vform, agr=agr, comp=frame[1], sem=r'\p.%s(p)' % pred))
        return
    forms, frames = verbs[pred]
    forms.add((w, vform, agr))
    if is_passive(v):
        # the object has become the subject
        frame = {'intr': 'tr', 'vpto': 'npvpto', 'vpbase': 'npvpbase'}.get(frame, frame)
    frames.add(frame)


def add_verbs(verbs, add):
    """Each form of a verb gets every frame its lemma has in the sample."""
    for pred, (forms, frames) in verbs.items():
        for w, vform, agr in forms:
            for f in frames:
                add(w, fmap('V', vform=vform, agr=agr, subcat=f, sem=verb_sem(f, pred)))


if __name__ == '__main__':
    main(sys.argv[1])
