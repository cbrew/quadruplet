"""Verbs with no clause above them: which constructions they are.

    python3 noclause.py MASC_DATA_DIR OUT.jsonl

Walks the raw MASC trees exactly as ../verbframes.py does and, for each
lexical verb whose verb phrase has no S, SQ, SINV or SBARQ above it (through
auxiliary and coordinated verb phrases), writes a JSON object with the
context needed to classify it: the attachment node (the first non-VP
ancestor) and its parent, the sisters to the left and right of the topmost
verb phrase, empty elements inside the verb phrase, and a rule-based class.
Prints counts by class and by genre.
"""
import collections, json, os, re, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))
from masctrees import *
import verbframes as vf

SPOKEN = {'face-to-face', 'telephone', 'court-transcript', 'debate-transcript'}
NOMINAL = {'NN', 'NNS', 'NNP', 'NNPS', 'PRP', 'CD', 'NP', 'NML', 'EX', 'DT', 'QP', 'JJ', 'ADJP', 'NX'}


def lab(n):
    return n.label if n is not None else 'ROOT'


def empties(vp):
    """Empty elements inside the verb phrase (not inside embedded clauses'
    subjects: all of them, as strings)."""
    return [x.word for x in vf.nodes(vp) if x.cat == '-NONE-' and x.word]


def direct_empty_objects(vp):
    out = []
    for k in vp.kids:
        kind = vf.empty_kind(k.raw) if k.word is None else None
        if kind and k.cat == 'NP':
            out.append(k.label + ':' + ''.join(w for t, w in leaves(k.raw)))
    return out


def classify(att, top, head, root):
    """A first, rule-based class; refined by hand on a sample."""
    if att is None:
        return 'root'
    c = att.cat
    tl = set(att.tags)
    if c == 'EDITED' or any(a.cat == 'EDITED' for a in ancestors(att)):
        return 'edited'
    if c == 'NP' or c == 'NML' or c == 'NX':
        i = att.kids.index(top)
        left = [k for k in att.kids[:i] if k.cat not in vf_punct and k.cat != '-NONE-']
        if not left:
            return 'np-initial-vp'
        if any(k.cat in NOMINAL or k.cat.startswith('NN') for k in left):
            return 'np-postnominal:' + head.cat
        return 'np-other'
    if c == 'FRAG':
        return 'frag'
    if c == 'UCP':
        return 'ucp'
    if c == 'SBAR':
        return 'sbar'
    if c == 'PRN':
        return 'prn'
    return 'other:' + c


vf_punct = PUNCT | {',', '.', ':'}


def ancestors(n):
    while n is not None:
        yield n
        n = n.parent


def main(root_dir, out_path):
    out = open(out_path, 'w')
    by = collections.Counter(); bygenre = collections.Counter()
    for g, fid, i, t in masc_trees(root_dir):
        raw = unwrap(t)
        if is_leaf(raw):
            continue
        root = vf.Node(raw)
        vf.number(root)
        tid = '%s/%s#%d' % (g, fid, i)
        for vp in vf.nodes(root):
            if vp.cat != 'VP' or vf.is_aux_vp(vp):
                continue
            head = next((k for k in vp.kids if k.word is not None and k.cat in vf.VERB_TAGS), None)
            if head is None:
                continue
            n, child = vp.parent, vp
            chain = [vp.label]
            while n is not None and n.cat == 'VP':
                chain.append(n.label)
                child, n = n, n.parent
            if n is not None and n.cat in vf.CLAUSES:
                continue
            att, top = n, child
            cls = classify(att, top, head, root)
            if att is not None:
                i = att.kids.index(top)
                left = ' '.join(k.label for k in att.kids[:i])
                right = ' '.join(k.label for k in att.kids[i + 1:])
                attwords = ' '.join(vf.words(att.raw))
            else:
                left = right = attwords = ''
            rec = {'id': tid, 'genre': g, 'mode': 'spoken' if g in SPOKEN else 'written',
                   'verb': head.word, 'tag': head.cat, 'pos': head.pos,
                   'att': lab(att), 'attparent': lab(att.parent) if att is not None else None,
                   'chain': chain, 'left': left, 'right': right,
                   'vpwords': ' '.join(vf.words(top.raw)), 'attwords': attwords[:200],
                   'empty_objects': direct_empty_objects(vp), 'empties': empties(top),
                   'ancestors': [a.label for a in ancestors(att)] if att is not None else [],
                   'class': cls,
                   'attpretty': pretty(att.raw)[:600] if att is not None else pretty(raw)[:600],
                   'sentence': ' '.join(vf.words(raw))[:300]}
            out.write(json.dumps(rec) + '\n')
            by[cls] += 1
            bygenre[rec['mode'], cls] += 1
    out.close()
    for c, k in by.most_common():
        print('%5d  %-28s spoken %4d  written %4d' % (k, c, bygenre['spoken', c], bygenre['written', c]))
    print(sum(by.values()))




def show(root_dir, ids):
    """python3 noclause.py MASC_DATA_DIR --show ID [ID ...]: print each tree."""
    want = set(ids)
    for g, fid, i, t in masc_trees(root_dir):
        tid = '%s/%s#%d' % (g, fid, i)
        if tid in want:
            print(tid)
            print(pretty(unwrap(t)))
            print()


if __name__ == '__main__':
    if sys.argv[2] == '--show':
        show(sys.argv[1], sys.argv[3:])
    else:
        main(sys.argv[1], sys.argv[2])
