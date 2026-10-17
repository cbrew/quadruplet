"""A dependency tree as a Prolog program in the ---> notation of
tools/masc/prolog/ptb.pl: the shared part of ud2pl.py and clear2pl.py.

Each word that has dependents, and the root, heads a constituent, named w
and its position (w5) under the functor of its tag (verb(w5), vbd(w5)). Its
rule's daughters are the word itself, ^'--':t(Tag), and its dependents in
order, each under its relation: a word with no dependents as t(Tag), one with
dependents as its own constituent.

    verb(w5) ---> [nsubj:t(pron), ^'--':t(verb), obj:noun(w7), punct:t(punct)].

A non-projective arc makes a constituent discontinuous: its words are several
runs of the sentence, and its rule has a list of daughters per run, the
daughter that is split appearing as D@K in each run that holds its K-th run,
as in odd_one_out's TIGER programs:

    verb(w7) ---> [[obj:noun(w2)], [nsubj:t(pron), ^'--':t(verb)]].
    verb(w5) ---> [verb(w7)@1, aux:t(aux), nsubj:t(propn), ^'--':t(verb), ccomp:verb(w7)@2].
"""
import re


def atom(s):
    """A Prolog atom, quoted where it must be."""
    if re.fullmatch(r'[a-z][A-Za-z0-9_]*', s):
        return s
    return "'" + s.replace('\\', '\\\\').replace("'", "''") + "'"


def filename(sid):
    """A sentence id as a file name: letters, digits, . # - and _, the rest _."""
    return re.sub(r'[^A-Za-z0-9.#_-]+', '_', sid).strip('_') or 'sentence'


def functor(tag):
    """A tag as the functor of a constituent: lower case, letters, digits and _."""
    f = re.sub(r'[^a-z0-9]+', '_', tag.lower()).strip('_')
    return f if f and f[0].isalpha() else 'x' + f


def stretches(positions):
    """Maximal runs of consecutive positions, (first, last)."""
    runs = []
    for p in sorted(positions):
        if runs and p == runs[-1][1] + 1:
            runs[-1][1] = p
        else:
            runs.append([p, p])
    return [tuple(r) for r in runs]


class DependencyTree:
    """words[i-1] is the i-th word's term, w(Tag,Form) or w(Tag,Form,Lemma);
    tags[i-1] its tag as in that term; heads[i-1] its head, 0 for the root;
    labels[i-1] its relation."""

    def __init__(self, words, tags, heads, labels):
        self.words, self.tags, self.heads, self.labels = words, tags, heads, labels
        n = len(words)
        self.deps = {i: [] for i in range(0, n + 1)}
        for i, h in enumerate(heads, 1):
            self.deps[h].append(i)
        roots = self.deps[0]
        if len(roots) != 1:
            raise ValueError('%d roots' % len(roots))
        self.root = roots[0]
        self._check_acyclic()
        self.yields = {}
        self._yield(self.root)

    def _check_acyclic(self):
        for i in range(1, len(self.words) + 1):
            seen, j = set(), i
            while j != 0:
                if j in seen:
                    raise ValueError('a cycle through word %d' % i)
                seen.add(j)
                j = self.heads[j - 1]

    def _yield(self, i):
        ys = [i]
        for d in self.deps[i]:
            ys += self._yield(d)
        self.yields[i] = sorted(ys)
        return ys

    def constituent(self, i):
        return bool(self.deps[i]) or i == self.root

    def name(self, i):
        return '%s(w%d)' % (functor(self.tags[i - 1]), i)

    def runs(self, i):
        return stretches(self.yields[i])

    def rules(self):
        """The rules, top-down, the root's first."""
        out = []
        for i in self._order(self.root):
            out.append(self._rule(i))
        return out

    def _order(self, i):
        order = [i]
        for d in self.deps[i]:
            if self.constituent(d):
                order += self._order(d)
        return order

    def _rule(self, i):
        runs = self.runs(i)
        pieces = [(i, "^'--':t(%s)" % atom(self.tags[i - 1]))]
        for d in self.deps[i]:
            lab = atom(self.labels[d - 1])
            if not self.constituent(d):
                pieces.append((d, '%s:t(%s)' % (lab, atom(self.tags[d - 1]))))
                continue
            druns = self.runs(d)
            for k, (first, _) in enumerate(druns, 1):
                at = '@%d' % k if len(druns) > 1 else ''
                pieces.append((first, '%s:%s%s' % (lab, self.name(d), at)))
        by_run = [[] for _ in runs]
        for pos, item in pieces:
            r = next(j for j, (a, b) in enumerate(runs) if a <= pos <= b)
            by_run[r].append((pos, item))
        items = [[it for _, it in sorted(r)] for r in by_run]
        if len(items) == 1:
            rhs = '[' + ', '.join(items[0]) + ']'
        else:
            rhs = '[' + ', '.join('[' + ', '.join(r) + ']' for r in items) + ']'
        return '%s ---> %s.' % (self.name(i), rhs)

    def discontinuous(self):
        """The constituents with more than one run."""
        return [i for i in self.yields if self.constituent(i) and len(self.runs(i)) > 1]


def write(out, header, rules_file, tree, guidelines, facts):
    """The program: a comment, the words, guidelines/1, root/1, the rules and the
    other facts, each a string ending in a full stop."""
    out.write('%% %s\n' % header)
    out.write('%% load %s first: it defines the operators this reads with.\n' % rules_file)
    out.write(':- encoding(utf8).\n\n')
    out.write('sentence([\n  ' + ',\n  '.join(tree.words) + '\n]).\n\n')
    out.write('guidelines(%s).\n' % guidelines)
    out.write('root(w%d).\n' % tree.root)
    for r in tree.rules():
        out.write(r + '\n')
    for f in facts:
        out.write(f + '\n')
