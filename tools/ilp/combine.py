"""The rate table where training saw the (lemma, prep) pair, a learned theory
where it did not.

    python3 combine.py TABLE_TSV PREDICTIONS_TSV

TABLE_TSV is build.py's table.tsv; PREDICTIONS_TSV what evaluate.pl's
predictions/3 writes for a theory.
"""
import sys

from build import scores


def main(table, predictions):
    theory = {}
    for line in open(predictions):
        eid, cls, p = line.split()
        theory[eid] = p == '1'
    rows = []
    for line in open(table):
        eid, cls, t, seen = line.split()
        rows.append((cls == 'complement', t == '1', seen == '1', theory[eid]))
    print('table alone:          ', scores([(g, t) for g, t, _, _ in rows]))
    print('theory alone:         ', scores([(g, h) for g, _, _, h in rows]))
    print('table, theory unseen: ', scores([(g, t if s else h) for g, t, s, h in rows]))
    unseen = [(g, h) for g, _, s, h in rows if not s]
    print('  on the %d unseen:    ' % len(unseen), scores(unseen))


if __name__ == '__main__':
    main(*sys.argv[1:3])
