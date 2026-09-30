"""PropBank's annotation of the English Web Treebank, aligned with UD EWT.

The PropBank release (github.com/propbank/propbank-release, data/google/ewt)
gives each EWT document as a .gold_skel file: one token a line, the word
masked as [WORD], with its PTB tag and tree, the predicates' lemmas and
rolesets, and a column of bracketed argument spans for each predicate.
UD EWT has the same documents and tokens: file GENRE/00/NAME.xml.gold_skel
is UD's GENRE-NAME, and its sentence k is UD's GENRE-NAME-%04d % (k + 1).

read(ewt_dir) gives {sent_id: sentence}; a sentence is (tags, predicates),
a predicate (token index from 0, lemma, roleset, [(label, first, last)]).
"""
import os
import re


def spans(column):
    """[(label, first, last)] from a column of bracketed spans: (ARG0* * *)"""
    out, open_ = [], []
    for i, cell in enumerate(column):
        for label in re.findall(r'\(([^*()]+)', cell):
            open_.append((label, i))
        for _ in range(cell.count(')')):
            label, first = open_.pop()
            out.append((label, first, i))
    return out


def read_file(path):
    sents, rows = [], []
    for line in list(open(path, encoding='utf-8')) + ['\n']:
        cols = line.split()
        if cols:
            rows.append(cols)
        elif rows:
            tags = [r[4] for r in rows]
            preds = [(i, r[6], r[7]) for i, r in enumerate(rows) if len(r) > 7 and r[7] != '-']
            args = []
            for k, (i, lemma, roleset) in enumerate(preds):
                column = [r[8 + k] if len(r) > 8 + k else '*' for r in rows]
                args.append((i, lemma, roleset, [s for s in spans(column) if s[0] != 'V']))
            sents.append((tags, args))
            rows = []
    return sents


def read(ewt_dir):
    out = {}
    for genre in sorted(os.listdir(ewt_dir)):
        for root, _, files in os.walk(os.path.join(ewt_dir, genre)):
            for f in files:
                if f.endswith('.gold_skel'):
                    doc = '%s-%s' % (genre, f[:-len('.xml.gold_skel')])
                    for k, s in enumerate(read_file(os.path.join(root, f))):
                        out['%s-%04d' % (doc, k + 1)] = s
    return out


def kind(label):
    """Comp for a numbered argument (the verb gives its role), Mod for a
    modifier (ARGM-: the role is the phrase's own); R- and C- stripped"""
    label = re.sub(r'^[RC]-', '', label)
    return 'Mod' if label.startswith('ARGM') else 'Comp'
