"""Write sentences (one per line on stdin) as BitPar input: a word per
line, followed by its tags from the lexicon, then a blank line.

    python3 input.py DIR/bp.lex < sentences > input

Giving the tags stops BitPar's sentence-initial decapitalization from adding
the tags of the lowercase word (I gets those of i until a lowercase word
is seen), so that both parsers use the same lexicon.
"""
import sys
lex = {}
for line in open(sys.argv[1]):
    w, *ts = line.rstrip('\n').split('\t')
    lex[w] = [t.split(' ')[0] for t in ts]
for s in sys.stdin:
    for w in s.split():
        print(w + '\t' + ' '.join(lex[w]))
    print()
