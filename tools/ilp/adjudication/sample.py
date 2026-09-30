"""A sample of the PPs where MASC, VerbNet and the learned rules disagree, for
adjudication by CGEL's criteria (see README.md).

    uv run python adjudication/sample.py EXAMPLES_TSV NLTK_DATA_DIR THEORY OUT_DIR

THEORY is a learned theory of complement/1 (theories/verbnet.pl). Writes
OUT_DIR/sample.tsv, to be filled in, and OUT_DIR/sample.md, to be read.
Four strata, drawn with a fixed seed:

  A  MASC says complement; the theory and VerbNet both say not   (held-out docs)
  B  MASC says not; the theory and VerbNet both say complement   (held-out docs)
  C  verb and preposition pairs MASC splits (ten or more PPs, the minority
     at least a third): one PP of each label                      (all docs)
  D  all three agree, as a control: half complements             (held-out docs)
"""
import collections
import os
import random
import re
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from build import Senses, VerbNet, examples, held_out  # noqa: E402

HERE = os.path.dirname(os.path.abspath(__file__))
PTB = os.path.join(HERE, '..', '..', 'masc', 'prolog', 'ptb.pl')
SIZES = {'A': 15, 'B': 15, 'C': 16, 'D': 10}


def theory_clauses(path):
    """The theory's clauses as lists of (predicate, argument or None)."""
    clauses = []
    text = open(path).read()
    for m in re.finditer(r'complement\(A\)\s*:-(.*?)\.\s*$', text, re.S | re.M):
        body = []
        for lit in re.finditer(r"(\w+)\(A(?:,('[^']*'|[^)]*))?\)", m.group(1)):
            arg = lit.group(2)
            body.append((lit.group(1), arg.strip("'") if arg else None))
        clauses.append(body)
    return clauses


def covers(clauses, facts):
    def holds(p, arg):
        v = facts[p]
        if arg is None:
            return v is True
        return arg in v if isinstance(v, list) else str(v) == arg
    return any(all(holds(p, a) for p, a in body) for body in clauses)


def main(tsv, nltk_dir, theory, out):
    rng = random.Random(7)
    data = list(examples(tsv, Senses(nltk_dir), VerbNet(nltk_dir)))
    clauses = theory_clauses(theory)
    rows = []
    for doc, path, pp, cls, f in data:
        rows.append(dict(doc=doc, path=path, pp=pp, masc=cls, lemma=f['lemma'], prep=f['prep'],
                         vn=f['vn_prep'], rule=covers(clauses, f), test=held_out(doc)))
    strata = collections.defaultdict(list)
    for r in rows:
        if not r['test']:
            continue
        c = r['masc'] == 'complement'
        if c and not r['rule'] and not r['vn']:
            strata['A'].append(r)
        elif not c and r['rule'] and r['vn']:
            strata['B'].append(r)
        elif c == r['rule'] == r['vn']:
            strata['D' + ('c' if c else 'a')].append(r)
    pairs = collections.defaultdict(list)
    for r in rows:
        pairs[r['lemma'], r['prep']].append(r)
    split = [k for k, v in pairs.items() if len(v) >= 10 and
             min(sum(r['masc'] == c for r in v) for c in ('complement', 'adjunct')) >= len(v) / 3]
    chosen = []
    for s in 'AB':
        chosen += [('ABCD'.index(s), s, r) for r in rng.sample(strata[s], SIZES[s])]
    for k in rng.sample(sorted(split), SIZES['C'] // 2):
        for c in ('complement', 'adjunct'):
            chosen.append((2, 'C', rng.choice([r for r in pairs[k] if r['masc'] == c])))
    for c in 'ca':
        chosen += [(3, 'D', r) for r in rng.sample(strata['D' + c], SIZES['D'] // 2)]
    items = '\n'.join('%s%d\t%s\t%s' % (s, i, r['path'], r['pp'])
                      for i, (_, s, r) in enumerate(chosen, 1))
    shown = subprocess.run(['swipl', '-q', '-g', 'show', '-t', 'halt(1)', PTB,
                            os.path.join(HERE, 'show.pl')],
                           input=items, capture_output=True, text=True, check=True).stdout
    text = {}
    for line in shown.splitlines():
        key, verb, ppw, sent = line.split('\t')
        text[key] = (verb, ppw, sent)
    os.makedirs(out, exist_ok=True)
    cols = ['key', 'stratum', 'lemma', 'prep', 'masc', 'verbnet', 'rule', 'pp', 'sentence', 'source',
            'judgment', 'note']
    with open(os.path.join(out, 'sample.tsv'), 'w') as t, open(os.path.join(out, 'sample.md'), 'w') as m:
        t.write('\t'.join(cols) + '\n')
        m.write('# PPs to adjudicate\n\nSee README.md for the criteria. *verb*, [PP].\n')
        last = None
        for i, (_, s, r) in enumerate(chosen, 1):
            key = '%s%d' % (s, i)
            verb, ppw, sent = text[key]
            src = re.sub(r'^.*masc-prolog/', '', r['path'])[:-3] + ' ' + r['pp']
            vals = [key, s, r['lemma'], r['prep'], r['masc'], 'yes' if r['vn'] else 'no',
                    'complement' if r['rule'] else '-', ppw, sent, src, '', '']
            t.write('\t'.join(vals) + '\n')
            if s != last:
                m.write('\n## Stratum %s\n\n' % s)
                last = s
            m.write('**%s** %s + *%s*. MASC: %s; VerbNet lists it: %s; rule: %s.  \n%s  \n'
                    '<sub>%s</sub>\n\n' % (key, r['lemma'], r['prep'], r['masc'], vals[5], vals[6],
                                         sent, src))
    counts = collections.Counter(s for _, s, _ in chosen)
    print('sampled', dict(counts), 'from strata sizes',
          {k: len(v) for k, v in strata.items()}, 'split pairs', len(split))


if __name__ == '__main__':
    main(*sys.argv[1:5])
