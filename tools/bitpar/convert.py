"""Write the treebank grammar in BitPar's formats.

    python3 convert.py DIR

reads DIR/tb.fcfg and writes DIR/bp.gram and DIR/bp.lex. BitPar requires a
count on every rule and lexical entry, so each gets 1 (with -W the counts
are weights and are not smoothed). Every symbol gets the suffix ~, because
BitPar matches tags given in the input as prefixes (NN would admit NNS).
"""
import os, re, sys
d = sys.argv[1]
rules, lex = [], {}
for line in open(os.path.join(d, 'tb.fcfg')):
    line = line.rstrip('\n')
    if line.startswith('"'):
        m = re.match(r'^"(.*)": (.*)$', line)
        lex[m.group(1)] = [t.strip()[:-2] for t in m.group(2).split(' | ')]
    elif ' -> ' in line:
        l, r = line.split(' -> ')
        rules.append((l[:-2], [x[:-2] for x in r.split()]))
with open(os.path.join(d, 'bp.gram'), 'w') as f:
    for l, r in sorted(rules, key=lambda x: x[0] != 'Top'):  # the first rule's parent is the start symbol
        f.write('1 %s~ %s\n' % (l, ' '.join(x + '~' for x in r)))
with open(os.path.join(d, 'bp.lex'), 'w') as f:
    for w, ts in lex.items():
        f.write(w + '\t' + '\t'.join(t + '~ 1' for t in ts) + '\n')
print('%d rules, %d words' % (len(rules), len(lex)))
