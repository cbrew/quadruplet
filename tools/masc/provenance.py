"""When MASC's Penn Treebank files were made, as far as the files say.

    python3 provenance.py MASC_DATA_DIR

Groups the .mrg files by their modification time (year and month), and
prints for each group the files, the words, and the rates of the annotation
conventions that tell guideline generations apart: *PRO* (a subject
controlled or arbitrary, distinct from other NP traces), NML (a nominal
inside an NP) and HYPH (a hyphen tokenized apart), per thousand words; the
English Web Treebank's tags ADD, NFP and GW; Switchboard's slash units, SU;
and the {TEXT:...} codes that keep an original spelling. Then the genres of
each group. See docs/masc-provenance.md.

Modification times are only as good as the copy: they survive zip and
Dropbox, not every other route.
"""
import collections
import os
import re
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from masctrees import masc_files

WORD = re.compile(r'\((?!-NONE-)[^\s()]+ [^\s()]+\)')
CONVENTIONS = [('*PRO*', r'\*PRO\*'), ('NML', r'\(NML'), ('HYPH', r'\(HYPH ')]
COUNTED = [('ADD/NFP/GW', r'\((?:ADD|NFP|GW) '), ('SU', r'\(SU '), ('{TEXT:', r'\{TEXT:')]


def main(root):
    groups = collections.defaultdict(collections.Counter)
    genres = collections.defaultdict(collections.Counter)
    for genre, path, _ in masc_files(root):
        month = time.strftime('%Y-%m', time.gmtime(os.path.getmtime(path)))
        text = open(path, encoding='utf-8', errors='replace').read()
        g = groups[month]
        g['files'] += 1
        g['words'] += len(WORD.findall(text))
        for name, pat in CONVENTIONS + COUNTED:
            g[name] += len(re.findall(pat, text))
        genres[month][genre + (' (-NEW)' if 'NEW' in os.path.basename(path) else '')] += 1
    print('| modified | files | words | ' + ' | '.join(n + ' /1k' for n, _ in CONVENTIONS) +
          ' | ' + ' | '.join(n for n, _ in COUNTED) + ' |')
    print('|---' * (3 + len(CONVENTIONS) + len(COUNTED)) + '|')
    total = 0
    for month in sorted(groups):
        g = groups[month]
        total += g['words']
        rates = ' | '.join('%.1f' % (1000 * g[n] / g['words']) for n, _ in CONVENTIONS)
        counts = ' | '.join(str(g[n]) for n, _ in COUNTED)
        print('| %s | %d | %d | %s | %s |' % (month, g['files'], g['words'], rates, counts))
    print('\n%d words in all' % total)
    print('\ngenres by month modified:')
    for month in sorted(genres):
        print('  %s  %s' % (month, ', '.join('%s %d' % kv for kv in sorted(genres[month].items()))))


if __name__ == '__main__':
    main(sys.argv[1])
