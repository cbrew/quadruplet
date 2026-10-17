"""Reading and normalising MASC Penn Treebank (.mrg) trees.

A tree is a nested list [label, child, ...]; a leaf is [tag, word].
"""
import os
import re

PUNCT = {',', '.', ':', '``', "''", '"', "'", '-LRB-', '-RRB-', '-LSB-', '-RSB-',
         'HYPH', 'NFP'}


def tokenize(s):
    # a no-break space inside a word is part of it (vis-\u00c3\u00a0-vis, a mangled
    # vis-a-vis in the blog Effing-Idiot); elsewhere it is space (indentation in
    # the letter 117CWL009)
    return re.findall(r'\(|\)|[^\s()]+(?:\u00a0[^\s()]+)*', s)


def read_trees(text):
    trees, stack = [], []
    for t in tokenize(text):
        if t == '(':
            stack.append([])
        elif t == ')':
            node = stack.pop()
            if stack:
                stack[-1].append(node)
            else:
                trees.append(node)
        elif stack:
            stack[-1].append(t)
    return trees


def is_leaf(n):
    return len(n) == 2 and isinstance(n[1], str)


def label(n):
    return n[0] if n and isinstance(n[0], str) else ''


def base(l):
    """Strips function tags and indices: NP-SBJ-1 -> NP, NP=2 -> NP."""
    if l in ('-NONE-', '-LRB-', '-RRB-', '-LSB-', '-RSB-'):
        return l
    return re.split(r'[-=]', l)[0] or l


def unwrap(t):
    """The top constituent of a tree, without the unlabelled outer brackets."""
    while len(t) == 1 and not isinstance(t[0], str):
        t = t[0]
    return t


def normalise(n, keep_empty_np=False):
    """Strips traces, punctuation and function tags, removes constituents
    left empty, and collapses unary chains of one label. Returns None if
    nothing is left. With keep_empty_np, an NP with only traces in it (a gap,
    a passive object or a controlled subject) is kept as (NP (-NONE- *))."""
    if keep_empty_np and not is_leaf(n) and base(label(n)) == 'NP' and \
            all(tag == '-NONE-' for tag, _ in leaves(n)):
        return ['NP', ['-NONE-', '*']]
    if is_leaf(n):
        tag = base(n[0])
        if n[0] == '-NONE-' or tag in PUNCT:
            return None
        return [tag, n[1]]
    kids = [k for k in (normalise(c, keep_empty_np) for c in n[1:] if not isinstance(c, str)) if k]
    if not kids:
        return None
    l = base(label(n))
    if len(kids) == 1 and not is_leaf(kids[0]) and kids[0][0] == l:
        return kids[0]
    return [l] + kids


def leaves(n):
    """The (tag, word) pairs of a tree, traces included."""
    if is_leaf(n):
        return [(n[0], n[1])]
    return [lv for c in n[1:] for lv in leaves(c)]


def productions(n, acc):
    if is_leaf(n):
        return
    acc.append((n[0], tuple(k[0] for k in n[1:])))
    for c in n[1:]:
        productions(c, acc)


def spans(n, start=0, acc=None):
    """The (label, start, end) of each phrasal constituent."""
    if acc is None:
        acc = []
    if is_leaf(n):
        return start + 1, acc
    end = start
    for c in n[1:]:
        end, _ = spans(c, end, acc)
    acc.append((n[0], start, end))
    return end, acc


def pretty(n):
    if is_leaf(n):
        return '(%s %s)' % (n[0], n[1])
    return '(%s %s)' % (n[0], ' '.join(pretty(c) for c in n[1:]))


def masc_files(root):
    """Yields (genre, path, id) for each .mrg file under root, in order."""
    for d, _, fs in sorted(os.walk(root)):
        for f in sorted(fs):
            if f.endswith('.mrg') and not f.startswith('._'):
                genre = os.path.basename(d)
                yield genre, os.path.join(d, f), f[:-4]


def masc_trees(root):
    """Yields (genre, file id, tree index, raw tree) for every sentence tree."""
    for genre, path, fid in masc_files(root):
        with open(path, errors='replace') as fh:
            trees = read_trees(fh.read())
        for i, t in enumerate(trees):
            top = unwrap(t)
            if base(label(top)) in ('CODE', ''):
                continue
            yield genre, fid, i, t
