"""Count the trees in BitPar's printed parse forests (bitpar -o), one per sentence:

    bitpar -o ... | python3 count.py


A forest is a node, (Label child ...), or a set of alternatives,
{(Label ...)#i(Label ...)}; #n= names a subforest and #n refers back to it.
Children are forests, references, or words.
"""
import re, sys
sys.setrecursionlimit(1000000)
TOK = re.compile(r'#\d+=|#\d+|#i|[(){}]|[^\s(){}#]+|\s+')
def count(text):
    toks = [t for t in TOK.findall(text) if not t.isspace()]
    named = {}
    i = 0
    def forest():
        nonlocal i
        t = toks[i]
        if t.endswith('=') and t.startswith('#'):
            i += 1
            v = forest()
            named[t[:-1]] = v
            return v
        if t.startswith('#') and t != '#i':
            i += 1
            return named[t]
        if t == '{':
            i += 1
            total = node()
            while toks[i] == '#i':
                i += 1
                total += node()
            assert toks[i] == '}'
            i += 1
            return total
        if t == '(':
            return node()
        i += 1        # a word
        return 1
    def node():
        nonlocal i
        assert toks[i] == '(', toks[i:i+5]
        i += 2        # ( Label
        p = 1
        while toks[i] != ')':
            p *= forest()
        i += 1
        return p
    out = []
    while i < len(toks):
        out.append(forest())
    return out
for n in count(sys.stdin.read()):
    print(n)
