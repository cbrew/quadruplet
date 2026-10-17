"""A MASC Penn Treebank tree as a Prolog program, after odd_one_out's TIGER
programs (dep2tiger/prolog/tiger2pl.py there). See docs/masc-prolog.md and
tools/masc/prolog/README.md.

    python3 ptb2pl.py MASC_DATA_DIR ID [-o FILE]        # one tree, by its id
    python3 ptb2pl.py MASC_DATA_DIR --all OUT_DIR       # every tree

An id is verbframes.py's: genre/file#index, court-transcript/Day3PMSession#13.
With --all, a file per tree goes to OUT_DIR/genre/file/index.pl.

The program holds the sentence's words, one fact, and a rule per constituent
in the --> notation of tools/masc/prolog/ptb.pl:

    sentence([w(prp,'I'), w(vbd,found,find), ...]).
    root(s1).
    s(s1) ---> [sbj:np(np1), ^'--':vp(vp1), '--':t(su)].

* A word is w(Tag, Form), or w(Tag, Form, Lemma) for a verb whose lemma
  (verblemmas.py) is not its form. Every leaf of the tree but an empty
  element is a word, CODE and SU leaves included, so the words are numbered
  as verbframes.py numbers them.
* A constituent is named by its category and its number in a top-down scan,
  np1, np2. Its daughters are Label:Daughter. The label is the node's
  function tags that say what it is to its parent: grammatical role and
  adverbial function, SBJ LGS PRD CLR DTV PUT BNF DIR EXT LOC MNR PRP TMP VOC
  ADV, lower-cased; a list where there are several, [loc,prd]; '--' where
  there are none. The other tags, which say what the node is (NOM HLN TTL TPC
  CLF SEZ IMP UNF ETC ...), are a fact about it, tags(s2, [nom]).
* The head daughter wears ^ on its label: Collins's head table (1999,
  appendix A), with NML read as NP and empty elements passed over.
* An empty element is Label:e(Kind), or Label:e(Kind, Antecedent) where it
  is co-indexed: *T*-1 pointing at the constituent labelled -1 is
  e('*T*', whnp1). The numeric index is resolved, so the program names the
  antecedent. A constituent marked for gapping, NP=2, gives a fact
  gap(np5, np2), naming the constituent labelled -2; where there is none, as
  in most of MASC, where parallel conjuncts share =N, the number itself,
  gap(np1, 1), gap(np3, 1).
* The 34 old WSJ files follow the Penn Treebank II guidelines (a controlled
  subject is *-1, not *PRO*-1; see docs/masc-provenance.md): their programs
  say guidelines(ii), the rest guidelines(revised).
"""
import argparse
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from masctrees import base, is_leaf, label, leaves, masc_files, masc_trees, read_trees, unwrap
from verblemmas import collect_bases, lemma

# function tags that say what a node is to its parent (Bies et al. 1995)
EDGE_TAGS = {'SBJ', 'LGS', 'PRD', 'CLR', 'DTV', 'PUT', 'BNF', 'DIR', 'EXT', 'LOC', 'MNR',
             'PRP', 'TMP', 'VOC', 'ADV'}
VERB_TAGS = {'VB', 'VBD', 'VBG', 'VBN', 'VBP', 'VBZ'}

# Collins (1999), appendix A: the direction of search, and the categories in
# order of priority; failing all, the first daughter in that direction
HEADS = {
    'ADJP': ('left', 'NNS QP NN $ ADVP JJ VBN VBG ADJP JJR NP JJS DT FW RBR RBS SBAR RB'),
    'ADVP': ('right', 'RB RBR RBS FW ADVP TO CD JJR JJ IN NP JJS NN'),
    'CONJP': ('right', 'CC RB IN'),
    'FRAG': ('right', ''),
    'INTJ': ('left', ''),
    'LST': ('right', 'LS :'),
    'NAC': ('left', 'NN NNS NNP NNPS NP NAC EX $ CD QP PRP VBG JJ JJS JJR ADJP FW'),
    'PP': ('right', 'IN TO VBG VBN RP FW'),
    'PRN': ('left', ''),
    'PRT': ('right', 'RP'),
    'QP': ('left', '$ IN NNS NN JJ RB DT CD NCD QP JJR JJS'),
    'RRC': ('right', 'VP NP ADVP ADJP PP'),
    'S': ('left', 'TO IN VP S SBAR ADJP UCP NP'),
    'SBAR': ('left', 'WHNP WHPP WHADVP WHADJP IN DT S SQ SINV SBAR FRAG'),
    'SBARQ': ('left', 'SQ S SINV SBARQ FRAG'),
    'SINV': ('left', 'VBZ VBD VBP VB MD VP S SINV ADJP NP'),
    'SQ': ('left', 'VBZ VBD VBP VB MD VP SQ'),
    'UCP': ('right', ''),
    'VP': ('left', 'TO VBD VBN MD VBZ VB VBG VBP VP ADJP NN NNS NP'),
    'WHADJP': ('left', 'CC WRB JJ ADJP'),
    'WHADVP': ('right', 'CC WRB'),
    'WHNP': ('left', 'WDT WP WP$ WHADJP WHPP WHNP'),
    'WHPP': ('right', 'IN TO FW'),
    'NX': ('left', ''),
    'X': ('right', ''),
}
PUNCT = {',', '.', ':', '``', "''", '-LRB-', '-RRB-', 'HYPH', 'NFP'}


def function_tags(l):
    """A label's function tags, its index and its gapping index:
    NP-SBJ-1 -> ['SBJ'], '1', None; NP=2 -> [], None, '2'."""
    if l in ('-NONE-', '-LRB-', '-RRB-', '-LSB-', '-RSB-'):
        return [], None, None
    m = re.search(r'=(\d+)$', l)
    gap = m.group(1) if m else None
    l = re.sub(r'=\d+$', '', l)
    parts = l.split('-')[1:]
    index = parts.pop() if parts and parts[-1].isdigit() else None
    return [p for p in parts if p and not p.isdigit()], index, gap


def atom(s):
    """A Prolog atom, quoted where it must be."""
    if re.fullmatch(r'[a-z][A-Za-z0-9_]*', s):
        return s
    return "'" + s.replace('\\', '\\\\').replace("'", "''") + "'"


def empty_word(n):
    return is_leaf(n) and n[0] == '-NONE-'


def overt(n):
    """Whether a node has a word that is not an empty element."""
    if is_leaf(n):
        return not empty_word(n)
    return any(overt(k) for k in n[1:] if not isinstance(k, str))


def head_index(n):
    """The index among n's daughters of the one it is headed by, Collins's
    way; empty daughters are passed over unless there is nothing else."""
    kids = [k for k in n[1:] if not isinstance(k, str)]
    cats = [base(label(k)) for k in kids]
    cands = [i for i, k in enumerate(kids) if overt(k)] or list(range(len(kids)))
    cat = base(label(n))
    if cat in ('NP', 'NML'):
        return np_head(kids, cats, cands)
    direction, prio = HEADS.get(cat, ('left', ''))
    order = cands if direction == 'left' else cands[::-1]
    for p in prio.split():
        for i in order:
            if cats[i] == p:
                return i
    return order[0]


def np_head(kids, cats, cands):
    """Collins's rule for NPs."""
    words = [i for i in cands if cats[i] not in PUNCT] or cands
    if cats[words[-1]] == 'POS':
        return words[-1]
    for i in reversed(words):
        if cats[i] in ('NN', 'NNP', 'NNPS', 'NNS', 'NX', 'POS', 'JJR', 'NML'):
            return i
    for i in words:
        if cats[i] == 'NP':
            return i
    for group in (('$', 'ADJP', 'PRN'), ('CD',), ('JJ', 'JJS', 'RB', 'QP')):
        for i in reversed(words):
            if cats[i] in group:
                return i
    return words[-1]


class Converter:
    """One tree's program."""

    def __init__(self, tree, guidelines):
        self.top = unwrap(tree)
        self.guidelines = guidelines
        self.counts = {}
        self.names = {}          # id(node) -> name
        self.by_index = {}       # index -> name
        self.words = []
        self.rules = []
        self.facts = []
        self.unresolved = []
        self._name_all(self.top)

    def _name_all(self, n):
        # top-down scan: a node is named before its daughters
        if is_leaf(n):
            return
        cat = base(label(n)).lower() or 'x'
        cat = re.sub(r'[^a-z0-9]', '', cat) or 'x'
        self.counts[cat] = self.counts.get(cat, 0) + 1
        name = '%s%d' % (cat, self.counts[cat])
        self.names[id(n)] = (cat, name)
        _, index, _ = function_tags(label(n))
        if index is not None and index not in self.by_index and overt(n):
            self.by_index[index] = name
        for k in n[1:]:
            if not isinstance(k, str):
                self._name_all(k)
        if index is not None and index not in self.by_index:
            self.by_index[index] = name      # an empty antecedent, WHNP-1 over 0

    def program(self):
        for tag, form in leaves(self.top):
            if tag != '-NONE-':
                self._word(tag, form)
        self._rules(self.top)
        cat, name = self.names[id(self.top)]
        return self.words, name, self.rules, self.facts

    def _edge(self, n):
        tags, _, _ = function_tags(label(n))
        edge = [t.lower() for t in tags if t in EDGE_TAGS]
        if not edge:
            return "'--'"
        return edge[0] if len(edge) == 1 else '[' + ','.join(edge) + ']'

    def _rules(self, n):
        cat, name = self.names[id(n)]
        tags, _, gap = function_tags(label(n))
        other = [t.lower() for t in tags if t not in EDGE_TAGS]
        if other:
            self.facts.append('tags(%s, [%s]).' % (name, ','.join(atom(t) for t in other)))
        if gap is not None:
            self.facts.append('gap(%s, %s).' % (name, self.by_index.get(gap, gap)))
        kids = [k for k in n[1:] if not isinstance(k, str)]
        h = head_index(n) if kids else None
        items, below = [], []
        for i, k in enumerate(kids):
            mark = '^' if i == h else ''
            if is_leaf(k) and empty_word(k):
                items.append('%s%s:%s' % (mark, "'--'", self._empty(k[1])))
            elif is_leaf(k):
                items.append('%s%s:t(%s)' % (mark, "'--'", atom(k[0].lower())))
            else:
                kc, kn = self.names[id(k)]
                items.append('%s%s:%s(%s)' % (mark, self._edge(k), kc, kn))
                below.append(k)
        # an empty element is a leaf under a phrase, (NP-SBJ (-NONE- *PRO*-1)): the
        # phrase is a constituent of its own, and its one daughter the empty element
        self.rules.append('%s(%s) ---> [%s].' % (cat, name, ', '.join(items)))
        for k in below:
            self._rules(k)

    def _empty(self, w):
        m = re.fullmatch(r'(.*?)-(\d+)', w)
        if m and m.group(1):
            kind, index = m.group(1), m.group(2)
            ante = self.by_index.get(index)
            if ante is not None:
                return 'e(%s, %s)' % (atom(kind), ante)
            self.unresolved.append(w)
            return 'e(%s)' % atom(kind)
        return 'e(%s)' % atom(w)

    def _word(self, tag, form):
        if tag in VERB_TAGS:
            lem = lemma(form, tag)
            if lem != form:
                self.words.append('w(%s,%s,%s)' % (atom(tag.lower()), atom(form), atom(lem)))
                return
        self.words.append('w(%s,%s)' % (atom(tag.lower()), atom(form)))


def mended(n):
    """The tree with two slips of the source mended: a node whose daughters are
    all bare strings is a word (two strings under one tag are a word split by
    the reader), and an empty bracket, ( ), or a node left with nothing under
    it, is dropped (None)."""
    if isinstance(n, str) or is_leaf(n):
        return n
    if not n or not isinstance(n[0], str):
        return None
    if len(n) > 1 and all(isinstance(k, str) for k in n[1:]):
        return [n[0], ' '.join(n[1:])]
    kids = [m for m in (mended(k) for k in n[1:] if not isinstance(k, str)) if m is not None]
    return [n[0]] + kids if kids else None


def old_guidelines(text):
    """The Penn Treebank II files: their original layout, spaces and no tabs."""
    return '\t' not in text and ') )' in text


def trees(root):
    """(id, tree, guidelines) for every tree, as verbframes.py numbers them."""
    for genre, path, fid in masc_files(root):
        with open(path, errors='replace') as fh:
            text = fh.read()
        g = 'ii' if old_guidelines(text) else 'revised'
        for i, t in enumerate(read_trees(text)):
            top = unwrap(t)
            if base(label(top)) in ('CODE', '') or is_leaf(top):
                continue
            top = mended(top)
            if top is None or is_leaf(top) or not any(tag != '-NONE-' for tag, _ in leaves(top)):
                continue        # no words: the (3225) of an annotation log line in enron 52555
            yield '%s/%s#%d' % (genre, fid, i), top, g


def write(tid, tree, guidelines, out):
    c = Converter(tree, guidelines)
    words, root, rules, facts = c.program()
    out.write('%% MASC %s, written by tools/masc/ptb2pl.py\n' % tid)
    out.write('% load tools/masc/prolog/ptb.pl first: it defines the operators this reads with.\n')
    out.write(':- encoding(utf8).\n\n')
    out.write('sentence([\n  ' + ',\n  '.join(words) + '\n]).\n\n')
    out.write('guidelines(%s).\n' % guidelines)
    out.write('root(%s).\n' % root)
    for r in rules:
        out.write(r + '\n')
    for f in facts:
        out.write(f + '\n')
    for u in c.unresolved:
        out.write('%% unresolved index: %s\n' % u)
    return c


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('masc')
    ap.add_argument('id', nargs='?')
    ap.add_argument('-o')
    ap.add_argument('--all')
    a = ap.parse_args()
    collect_bases(masc_trees(a.masc))
    if a.all:
        n = unresolved = 0
        for tid, t, g in trees(a.masc):
            genre, rest = tid.split('/', 1)
            fid, i = rest.rsplit('#', 1)
            d = os.path.join(a.all, genre, fid)
            os.makedirs(d, exist_ok=True)
            with open(os.path.join(d, i + '.pl'), 'w', encoding='utf-8') as out:
                unresolved += len(write(tid, t, g, out).unresolved)
            n += 1
        print('%d programs; %d empty elements whose index names no constituent' % (n, unresolved))
        return
    for tid, t, g in trees(a.masc):
        if tid == a.id:
            if a.o:
                with open(a.o, 'w', encoding='utf-8') as out:
                    write(tid, t, g, out)
            else:
                write(tid, t, g, sys.stdout)
            return
    sys.exit('no tree %s' % a.id)


if __name__ == '__main__':
    main()
