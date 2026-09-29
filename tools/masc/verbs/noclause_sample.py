"""Hand judgements of samples of the clauseless verbs, and their tallies.

    python3 noclause_sample.py NC.jsonl [--show]

NC.jsonl is the output of noclause.py. Two samples are drawn from it,
reproducibly: (1) a simple random sample of 150 of all clauseless verbs;
(2) a random sample of 50 of the reduced relatives headed by VBG with a comma
before the verb phrase ("NP , V-ing ..."). The judgements below were made by
reading each item's tree and sentence (--show prints them). Codes:

  G  a genuine grammatical construction, annotated as the guidelines intend
     (reduced relative, unlike coordination, compound modifier ...)
  F  a non-sentential unit of text or talk: list item, heading, title,
     caption, citation formula, stage direction, answer or echo fragment
  D  a disfluency: a reparandum under EDITED
  E  an annotation error: a missing S, an S closed before its VP, or a
     participle attached to the wrong phrase (so the understood subject the
     structure implies is wrong)
  e  as E, but arguable
"""
import collections, json, math, random, sys

MAIN = {  # item number in the sample of 150 -> code; unlisted items are G
    'E': [8, 26, 28, 33, 56, 77, 83, 106, 127, 136, 138], 'e': [69, 90],
    'D': [41, 45, 55, 81, 134],
    'F': [5, 10, 24, 25, 35, 38, 42, 46, 48, 63, 67, 68, 96, 98, 99, 100,
          104, 109, 122, 126, 137, 144, 146, 148],
}
COMMA = {  # item number in the sample of 50 comma-VBG reduced relatives
    'E': [0, 10, 12, 15, 17, 18, 21, 26, 27, 32, 33, 34, 40, 43, 49],
    'e': [13, 14, 31, 36],
}


def wilson(k, n, z=1.96):
    p = k / n
    d = 1 + z * z / n
    c = p + z * z / (2 * n)
    h = z * math.sqrt(p * (1 - p) / n + z * z / (4 * n * n))
    return 100 * (c - h) / d, 100 * (c + h) / d


def codes(table, n):
    out = ['G'] * n
    for code, items in table.items():
        for i in items:
            out[i] = code
    return out


def tally(name, items, judged):
    n = len(items)
    cnt = collections.Counter(judged)
    print('%s: n=%d' % (name, n))
    for k in 'GFDEe':
        if cnt[k]:
            lo, hi = wilson(cnt[k], n)
            print('  %s %4d  %5.1f%%  [%.1f, %.1f]' % (k, cnt[k], 100 * cnt[k] / n, lo, hi))
    k = cnt['E'] + cnt['e']
    lo, hi = wilson(k, n)
    print('  E+e %2d  %5.1f%%  [%.1f, %.1f]' % (k, 100 * k / n, lo, hi))
    by = collections.defaultdict(collections.Counter)
    for r, j in zip(items, judged):
        by[r['class'].split(':')[0]][j] += 1
    for c, v in sorted(by.items(), key=lambda x: -sum(x[1].values())):
        print('    %-28s %s' % (c, dict(v)))


def main(path, show):
    R = [json.loads(l) for l in open(path)]
    random.seed(20260928)
    main_items = [R[i] for i in random.sample(range(len(R)), 150)]
    rr = [r for r in R if r['class'] == 'reduced relative: VBG' and
          (r['left'].split()[-1:] == [','] or r['vpwords'].startswith(','))]
    random.seed(7)
    comma_items = random.sample(rr, 50)
    for name, items, table in (('random sample of all clauseless verbs', main_items, MAIN),
                               ('comma-VBG reduced relatives (of %d)' % len(rr), comma_items, COMMA)):
        judged = codes(table, len(items))
        if show:
            for n, (r, j) in enumerate(zip(items, judged)):
                print(n, j, r['id'], r['verb'], '[' + r['class'] + ']')
                print('   ', r['attpretty'][:300])
                print('    S:', r['sentence'][:200])
        tally(name, items, judged)


if __name__ == '__main__':
    main(sys.argv[1], '--show' in sys.argv[2:])
