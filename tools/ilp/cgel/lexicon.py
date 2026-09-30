"""The prepositional verbs of CGEL ch. 4 §6.1.2 (prepositional_verbs.tsv)."""
import os

PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'prepositional_verbs.tsv')


def prepositional_verbs(path=PATH):
    """{(lemma, preposition): structure}"""
    out = {}
    for line in open(path, encoding='utf-8'):
        if line.startswith('#') or not line.strip():
            continue
        lemma, prep, structure, _marks, _section = line.rstrip('\n').split('\t')
        out[lemma, prep] = structure
    return out
