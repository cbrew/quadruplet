"""Read a context-free grammar off the MASC Penn Treebank trees, for
timing the parsers: no features, no semantics.

    python3 treebank.py MASC_DATA_DIR OUT_DIR

writes OUT_DIR/tb.fcfg (the grammar, start symbol Top), OUT_DIR/sents.txt
(id, length and words of each sentence) and OUT_DIR/trees.jsonl (each
sentence's tree as the grammar derives it, under Top: a JSON object with its
id and the tree as nested lists, [label, child, ...], a leaf being
[tag, word], labels as the grammar writes them, NP[]), and
OUT_DIR/annotated.jsonl (the same trees with the function tags MASC gives
their phrases: a node is {"c": label, "f": [tag, ...], "k": [child, ...]},
a leaf {"c": tag, "w": word}; where a chain of phrases is collapsed, the
tags are those of its top, which relate the chain to its parent). Punctuation is kept; empty elements
and function tags are dropped; a chain of single-child phrases becomes one
symbol (S over VP is SxVP), which removes the unary cycles; a phrase label
that is also a part of speech somewhere gets the suffix ph. That last rule
is too blunt (NP is a tag somewhere, so every NP is NPph), but it is the
same grammar for every parser timed.
"""
import collections, json, os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from masctrees import *

ROOT, OUT = sys.argv[1], sys.argv[2]
NAMES = {',': 'Comma', '.': 'Period', ':': 'Colon', '``': 'LQuote', "''": 'RQuote', '"': 'Quote', "'": 'Apos',
         '#': 'Hash', '$': 'Dollar', '-LRB-': 'LRB', '-RRB-': 'RRB', '-LSB-': 'LSB', '-RSB-': 'RSB',
         'PRP$': 'PRPS', 'WP$': 'WPS', 'PRO$': 'PROS'}
def cat(l): return NAMES.get(l, l)
def tok(w): return w.replace('"', "''")

FUNCTIONS = {}  # id of a phrase's list -> its function tags

def functions(l):
    """The function tags of a label: NP-SBJ-1 -> [SBJ], PP-LOC=2 -> [LOC]."""
    if l in ('-NONE-', '-LRB-', '-RRB-', '-LSB-', '-RSB-'):
        return []
    return [p for p in re.split(r'[-=]', l)[1:] if p and not p.isdigit()]

def norm(n):
    if is_leaf(n):
        if n[0] == '-NONE-':
            return None
        return [cat(base(n[0])), tok(n[1])]
    kids = [k for k in (norm(c) for c in n[1:] if not isinstance(c, str)) if k]
    if not kids:
        return None
    l = cat(base(label(n)) or 'ROOT')
    if len(kids) == 1 and not is_leaf(kids[0]):  # collapse unary chains of phrases
        k = kids[0]
        out = [l if l == k[0] else l + 'x' + k[0]] + k[1:]
    else:
        out = [l] + kids
    FUNCTIONS[id(out)] = functions(label(n))
    return out

trees = []
for g, fid, i, t in masc_trees(ROOT):
    n = norm(unwrap(t))
    if n:
        trees.append(('%s/%s#%d' % (g, fid, i), n))
postags = {tag for _, n in trees for tag, w in leaves(n)}
def fix(n):
    if is_leaf(n):
        return n
    l = n[0]
    out = [l + 'ph' if l in postags else l] + [fix(k) for k in n[1:]]
    FUNCTIONS[id(out)] = FUNCTIONS[id(n)]  # every phrase norm made has an entry
    return out
trees = [(i, fix(n)) for i, n in trees]
prods = collections.Counter(); lex = collections.defaultdict(set); roots = set()
for _, n in trees:
    roots.add(n[0])
    for tag, w in leaves(n):
        lex[w].add(tag)
    ps = []
    productions(n, ps)
    prods.update(ps)
bad = [x for x in {l for l, _ in prods} | postags if not re.fullmatch(r'[A-Z][A-Za-z0-9]*', x)]
assert not bad, bad
with open(os.path.join(OUT, 'tb.fcfg'), 'w') as f:
    for l, r in sorted(prods):
        f.write('%s[] -> %s\n' % (l, ' '.join(x + '[]' for x in r)))
    for r in sorted(roots):
        f.write('Top[] -> %s[]\n' % r)
    for w in sorted(lex):
        f.write('"%s": %s\n' % (w, ' | '.join(t + '[]' for t in sorted(lex[w]))))
with open(os.path.join(OUT, 'sents.txt'), 'w') as f:
    for i, n in trees:
        f.write('%s\t%d\t%s\n' % (i, len(leaves(n)), ' '.join(w for _, w in leaves(n))))

def grammar_labels(n):
    if is_leaf(n):
        return [n[0] + '[]', n[1]]
    return [n[0] + '[]'] + [grammar_labels(k) for k in n[1:]]

with open(os.path.join(OUT, 'trees.jsonl'), 'w') as f:
    for i, n in trees:
        f.write(json.dumps({'id': i, 'tree': ['Top[]', grammar_labels(n)]}) + '\n')

def annotated(n):
    if is_leaf(n):
        return {'c': n[0] + '[]', 'w': n[1]}
    node = {'c': n[0] + '[]', 'k': [annotated(k) for k in n[1:]]}
    if FUNCTIONS[id(n)]:
        node['f'] = FUNCTIONS[id(n)]
    return node

with open(os.path.join(OUT, 'annotated.jsonl'), 'w') as f:
    for i, n in trees:
        f.write(json.dumps({'id': i, 'tree': {'c': 'Top[]', 'k': [annotated(n)]}}) + '\n')
print('%d trees, %d rules, %d words' % (len(trees), len(prods) + len(roots), len(lex)))
