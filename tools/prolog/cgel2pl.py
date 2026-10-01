"""A CGELBank tree (nert-nlp/cgel's .cgel format) as a Prolog program, loaded
with tools/prolog/cgel.pl. See tools/prolog/README.md.

    python3 cgel2pl.py FILE.cgel SENT_ID [-o OUT]      # one tree, by its sent_id
    python3 cgel2pl.py FILE.cgel --all OUT_DIR         # every tree, OUT_DIR/SENT_ID.pl

The program is in the ---> notation of tools/masc/prolog/ptb.pl, one rule per
phrasal node, CGEL's functions as the daughters' labels:

    sentence([w(d,which), w(n,'Liz'), w(v,bought,buy)]).
    clause(clause1) ---> [prenucleus:np(np1), ^head:clause(clause2)].
    np(np1) ---> [^head:nom(nom1)].
    nom(nom1) ---> [^det_head:dp(dp1)].
    dp(dp1) ---> [^head:t(d)].
    clause(clause2) ---> [subj:np(np2), ^head:vp(vp1)].
    vp(vp1) ---> [^head:t(v), obj:e(gap, np1)].
    fused(dp1, np1, det).

* A node is named by its category and its number in a top-down scan. The
  category is CGEL's, lower-cased, with + and _ as _: clause_rel, n_pro,
  v_aux, pp_strand, np_pp (the nonce NP+PP).
* A lexical node (one with a word, :t) is a preterminal, t(Cat), under its
  function's label, and its word is w(Cat, Form), or w(Cat, Form, Lemma) where
  :l gives the lemma.
* A function is a label: CGEL's, lower-cased, with - as _ (det_head,
  head_prenucleus, obj_ind); a nonce function keeps its + and /, quoted
  ('obj+comp'). The root has none.
* The head daughter wears ^: the one whose function is Head, or a fused
  function ending in Head (Det-Head, Mod-Head, Marker-Head).
* A gap is Label:e(gap, Antecedent), the antecedent the overt node carrying
  the gap's variable (x / NP): its name, or a word's position where the
  antecedent is a word (a fronted auxiliary, x / V_aux). A word that the
  writer left out and the annotators supplied (:correct with no :t) reads no
  words either: Label:e(omitted(Cat, Form)).
* Fusion: CGELBank stores one of a fused node's two incoming edges, the one
  from the lower parent, labelled with both functions; tree2tex.py and the
  manual (sect. 3.2) say where the other goes. fused(Node, Upper, Function)
  restores it: for Det-Head, the NP above the Nom layers; for the others the
  grandparent; Function the first part of the label (det, mod, marker, head).
  Node is a name, or a position for a lexical node.
* Facts: sent_id/1, text/1 (# text), sent/1 (# sent); for word I, xpos(I, X),
  correct(I, Form) for a corrected word, subtokens(I, Parts) for :subt and
  :subp, punct(I, before, Marks) and punct(I, after, Marks); note(Node, Note),
  Node a name or a position.
"""
import argparse
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from deptree import atom, filename  # noqa: E402

TOKEN = re.compile(r'\(|\)|:[A-Za-z_+/-]*|"(?:[^"\\]|\\.)*"|[^\s()":]+(?:/[^\s()":]+)*|/')
FUSED_UP = {'Det-Head': 'det', 'Mod-Head': 'mod', 'Marker-Head': 'marker', 'Head-Prenucleus': 'head'}


def unquote(s):
    return re.sub(r'\\(.)', r'\1', s[1:-1])


def parse(text):
    """The trees of a block of .cgel text: nodes as dicts with cat, var (the
    co-indexation variable), func, attrs [(key, value)] in order, kids."""
    toks = TOKEN.findall(text)
    pos = 0

    def node(func):
        nonlocal pos
        assert toks[pos] == '('
        pos += 1
        head = []
        while toks[pos] not in ('(', ')') and not toks[pos].startswith(':'):
            head.append(toks[pos])
            pos += 1
        if len(head) == 3 and head[1] == '/':
            var, cat = head[0], head[2]
        elif len(head) == 1 and re.fullmatch(r'[a-z]{1,2}', head[0]):
            var, cat = head[0], 'GAP'          # an old-style gap, (x)
        else:
            var, cat = None, ' '.join(head)
        n = {'cat': cat, 'var': var, 'func': func, 'attrs': [], 'kids': []}
        while toks[pos] != ')':
            t = toks[pos]
            if t.startswith(':') and toks[pos + 1] == '(':
                pos += 1
                n['kids'].append(node(t[1:]))
            elif t.startswith(':'):
                n['attrs'].append((t[1:], unquote(toks[pos + 1])))
                pos += 2
            else:
                raise ValueError('unexpected %r' % t)
        pos += 1
        return n

    trees = []
    while pos < len(toks):
        if toks[pos] == '(':
            trees.append(node(None))
        else:
            pos += 1
    return trees


def blocks(path):
    """(metadata dict, tree) for each tree of a .cgel file."""
    return blocks_text(open(path, encoding='utf-8').read())


def blocks_text(text):
    """(metadata dict, tree) for each tree of .cgel text."""
    for b in re.split(r'\n(?=# sent_id)', text):
        if '# sent_id' not in b:
            continue
        meta = dict(re.findall(r'^# (\w+) = (.*)$', b, re.M))
        body = '\n'.join(l for l in b.split('\n') if not l.startswith('#'))
        trees = parse(body)
        if len(trees) != 1:
            raise ValueError('%s: %d trees' % (meta.get('sent_id'), len(trees)))
        yield meta, trees[0]


def get(n, key):
    return next((v for k, v in n['attrs'] if k == key), None)


def is_lexical(n):
    return not n['kids'] and n['cat'] != 'GAP'


def category(cat):
    f = re.sub(r'[^a-z0-9]+', '_', cat.lower()).strip('_')
    return f if f and f[0].isalpha() else 'x' + f


def function(func):
    if not func:
        return "'--'"
    return atom(func.lower().replace('-', '_'))


def heads(func):
    return func == 'Head' or (func or '').endswith('-Head')


class Converter:
    def __init__(self, meta, tree):
        self.meta, self.tree = meta, tree
        self.counts, self.names, self.positions, self.var = {}, {}, {}, {}
        self.words, self.rules, self.facts = [], [], []
        self._scan(tree)
        self._antecedents(tree)
        self.words = [None] * len(self.positions)      # filled in at each word's position

    def _scan(self, n):
        # top-down: a node is named before its daughters; words are numbered in order
        if n['cat'] == 'GAP':
            return
        if is_lexical(n):
            if get(n, 't') is not None:
                self.positions[id(n)] = len(self.positions) + 1
            return
        c = category(n['cat'])
        self.counts[c] = self.counts.get(c, 0) + 1
        self.names[id(n)] = (c, '%s%d' % (c, self.counts[c]))
        for k in n['kids']:
            self._scan(k)

    def _antecedents(self, n):
        if n['var'] and n['cat'] != 'GAP':
            self.var[n['var']] = self.ref(n)
        for k in n['kids']:
            self._antecedents(k)

    def ref(self, n):
        """A node's name, or a lexical node's position."""
        if id(n) in self.names:
            return self.names[id(n)][1]
        return str(self.positions[id(n)]) if id(n) in self.positions else None

    def program(self):
        for key in ('sent_id', 'text', 'sent'):
            if key in self.meta:
                self.facts.append('%s(%s).' % (key, atom(self.meta[key].strip())))
        self._node(self.tree, [])
        return self.words, self.names[id(self.tree)][1], self.rules, self.facts

    def _word(self, n):
        i = self.positions[id(n)]
        cat, form, lem = category(n['cat']), get(n, 't'), get(n, 'l')
        if lem is not None and lem != form:
            self.words[i - 1] = 'w(%s,%s,%s)' % (atom(cat), atom(form), atom(lem))
        else:
            self.words[i - 1] = 'w(%s,%s)' % (atom(cat), atom(form))
        if get(n, 'xpos') is not None:
            self.facts.append('xpos(%d, %s).' % (i, atom(get(n, 'xpos'))))
        if get(n, 'correct') is not None:
            self.facts.append('correct(%d, %s).' % (i, atom(get(n, 'correct'))))
        subs = [v for k, v in n['attrs'] if k in ('subt', 'subp')]
        if subs:
            self.facts.append('subtokens(%d, [%s]).' % (i, ','.join(atom(s) for s in subs)))
        before, after, seen_t = [], [], False
        for k, v in n['attrs']:
            if k == 't':
                seen_t = True
            elif k == 'p':
                (after if seen_t else before).append(v)
        if before:
            self.facts.append('punct(%d, before, [%s]).' % (i, ','.join(atom(p) for p in before)))
        if after:
            self.facts.append('punct(%d, after, [%s]).' % (i, ','.join(atom(p) for p in after)))

    def _note(self, n):
        if get(n, 'note') is not None and self.ref(n) is not None:
            self.facts.append('note(%s, %s).' % (self.ref(n), atom(get(n, 'note'))))

    def _node(self, n, ancestors):
        """The rule for a phrasal node n; ancestors are the nodes above it, nearest last."""
        c, name = self.names[id(n)]
        self._note(n)
        items, below = [], []
        for k in n['kids']:
            mark = '^' if heads(k['func']) else ''
            lab = function(k['func'])
            if k['cat'] == 'GAP':
                ante = self.var.get(k['var'])
                items.append('%s%s:%s' % (mark, lab, 'e(gap, %s)' % ante if ante else 'e(gap)'))
            elif is_lexical(k) and get(k, 't') is None:
                items.append('%s%s:e(omitted(%s, %s))' % (mark, lab, atom(category(k['cat'])),
                                                          atom(get(k, 'correct') or '')))
            elif is_lexical(k):
                self._word(k)
                self._note(k)
                items.append('%s%s:t(%s)' % (mark, lab, atom(category(k['cat']))))
            else:
                kc, kn = self.names[id(k)]
                items.append('%s%s:%s(%s)' % (mark, lab, kc, kn))
                below.append(k)
            if k['func'] in FUSED_UP:
                upper = self._upper(k['func'], n, ancestors)
                if upper is not None and self.ref(k) is not None:
                    self.facts.append('fused(%s, %s, %s).' % (self.ref(k), self.names[id(upper)][1],
                                                              FUSED_UP[k['func']]))
        self.rules.append('%s(%s) ---> [%s].' % (c, name, ', '.join(items)))
        for k in below:
            self._node(k, ancestors + [n])

    @staticmethod
    def _upper(func, parent, ancestors):
        """The other parent of a node fused under parent (tree2tex.py's rule)."""
        if func == 'Det-Head':
            chain = ancestors[::-1]
            node = parent
            while node['cat'] == 'Nom' and chain:
                node = chain.pop(0)
            return node if node is not parent else None
        return ancestors[-1] if ancestors else None


def write(meta, tree, out, source):
    c = Converter(meta, tree)
    words, root, rules, facts = c.program()
    out.write('%% CGELBank %s %s, written by tools/prolog/cgel2pl.py\n' % (source, meta.get('sent_id')))
    out.write('% load tools/prolog/cgel.pl first: it defines the operators this reads with.\n')
    out.write(':- encoding(utf8).\n\n')
    out.write('sentence([\n  ' + ',\n  '.join(words) + '\n]).\n\n')
    out.write('guidelines(cgelbank).\n')
    out.write('root(%s).\n' % root)
    for r in rules:
        out.write(r + '\n')
    for f in facts:
        out.write(f + '\n')
    return c


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('cgel')
    ap.add_argument('sent_id', nargs='?')
    ap.add_argument('-o')
    ap.add_argument('--all')
    a = ap.parse_args()
    source = os.path.basename(a.cgel)
    if a.all:
        os.makedirs(a.all, exist_ok=True)
        n = 0
        for meta, tree in blocks(a.cgel):
            with open(os.path.join(a.all, filename(meta['sent_id']) + '.pl'), 'w', encoding='utf-8') as out:
                write(meta, tree, out, source)
            n += 1
        print('%d programs' % n)
        return
    for meta, tree in blocks(a.cgel):
        if meta.get('sent_id') == a.sent_id:
            if a.o:
                with open(a.o, 'w', encoding='utf-8') as out:
                    write(meta, tree, out, source)
            else:
                write(meta, tree, sys.stdout, source)
            return
    sys.exit('no tree %s' % a.sent_id)


if __name__ == '__main__':
    main()
