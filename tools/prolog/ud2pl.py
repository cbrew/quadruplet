"""A Universal Dependencies tree (CoNLL-U) as a Prolog program, loaded with
tools/prolog/dep.pl. See tools/prolog/README.md and deptree.py.

    python3 ud2pl.py FILE.conllu SENT_ID [-o OUT]      # one sentence, by its sent_id
    python3 ud2pl.py FILE.conllu --all OUT_DIR         # every sentence, OUT_DIR/SENT_ID.pl

The program holds the words, the basic tree as one rule per constituent (a
word with dependents, or the root), and what the tree leaves out as facts:

    sentence([w(pron,'I'), w(verb,bought,buy), ...]).
    verb(w2) ---> [nsubj:t(pron), ^'--':t(verb), obj:noun(w4)].
    xpos(2, 'VBD').  feats(2, ['Mood=Ind','Tense=Past','VerbForm=Fin']).

* A word is w(Upos, Form), or w(Upos, Form, Lemma) where the lemma is not the
  form; the UPOS is lower-cased, and is the tag of t(Tag) in the rules.
* Facts: sent_id/1 and text/1 from the comments; xpos(I, Xpos) and
  feats(I, Feats) where given; mwt(First, Last, Form) for a multiword token;
  edep(I, Head, Rel) for each arc of the enhanced graph (DEPS), where it is
  given, Head a word's position, 0, or an empty node's id as an atom, '8.1';
  empty_node(Id, Form, Lemma, Upos) for each empty node; misc(I, Misc) for
  MISC other than SpaceAfter.
"""
import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from deptree import DependencyTree, atom, filename, write  # noqa: E402


def sentences(path):
    """(comments, rows) for each sentence of a CoNLL-U file."""
    comments, rows = [], []
    with open(path, encoding='utf-8') as f:
        for line in f:
            line = line.rstrip('\n')
            if not line:
                if rows:
                    yield comments, rows
                comments, rows = [], []
            elif line.startswith('#'):
                comments.append(line)
            else:
                rows.append(line.split('\t'))
    if rows:
        yield comments, rows


def comment(comments, key):
    for c in comments:
        if c.startswith('# %s = ' % key):
            return c.split(' = ', 1)[1]
    return None


def head_atom(h):
    return h if h.isdigit() else atom(h)


def convert(comments, rows):
    """(sent_id, DependencyTree, facts)."""
    words, tags, heads, labels, facts = [], [], [], [], []
    sid = comment(comments, 'sent_id')
    text = comment(comments, 'text')
    if sid is not None:
        facts.append('sent_id(%s).' % atom(sid))
    if text is not None:
        facts.append('text(%s).' % atom(text))
    for r in rows:
        tid, form, lemma, upos, xpos, feats, head, deprel, deps, misc = (r + ['_'] * 10)[:10]
        if '-' in tid:
            a, b = tid.split('-')
            facts.append('mwt(%s, %s, %s).' % (a, b, atom(form)))
            continue
        if '.' in tid:
            facts.append('empty_node(%s, %s, %s, %s).' % (atom(tid), atom(form), atom(lemma), atom(upos.lower())))
            for arc in ([] if deps == '_' else deps.split('|')):
                h, rel = arc.split(':', 1)
                facts.append('edep(%s, %s, %s).' % (atom(tid), head_atom(h), atom(rel)))
            continue
        i = int(tid)
        tag = upos.lower() if upos != '_' else 'x'
        tags.append(tag)
        if lemma not in ('_', form):
            words.append('w(%s,%s,%s)' % (atom(tag), atom(form), atom(lemma)))
        else:
            words.append('w(%s,%s)' % (atom(tag), atom(form)))
        heads.append(int(head))
        labels.append(deprel)
        if xpos != '_':
            facts.append('xpos(%d, %s).' % (i, atom(xpos)))
        if feats != '_':
            facts.append('feats(%d, [%s]).' % (i, ','.join(atom(f) for f in feats.split('|'))))
        for arc in ([] if deps == '_' else deps.split('|')):
            h, rel = arc.split(':', 1)
            facts.append('edep(%d, %s, %s).' % (i, head_atom(h), atom(rel)))
        rest = [m for m in ([] if misc == '_' else misc.split('|')) if not m.startswith('SpaceAfter=')]
        if rest:
            facts.append('misc(%d, [%s]).' % (i, ','.join(atom(m) for m in rest)))
    return sid, DependencyTree(words, tags, heads, labels), facts


def write_one(path, comments, rows, out):
    sid, tree, facts = convert(comments, rows)
    write(out, 'UD %s %s, written by tools/prolog/ud2pl.py' % (os.path.basename(path), sid),
          'tools/prolog/dep.pl', tree, 'ud', facts)
    return tree


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('conllu')
    ap.add_argument('sent_id', nargs='?')
    ap.add_argument('-o')
    ap.add_argument('--all')
    a = ap.parse_args()
    if a.all:
        os.makedirs(a.all, exist_ok=True)
        n = disc = bad = 0
        for k, (comments, rows) in enumerate(sentences(a.conllu), 1):
            sid = comment(comments, 'sent_id') or str(k)
            safe = filename(sid)
            try:
                with open(os.path.join(a.all, safe + '.pl'), 'w', encoding='utf-8') as out:
                    disc += bool(write_one(a.conllu, comments, rows, out).discontinuous())
                n += 1
            except ValueError as e:
                bad += 1
                os.remove(os.path.join(a.all, safe + '.pl'))
                print('%s: %s' % (sid, e), file=sys.stderr)
        print('%d programs, %d with a discontinuous constituent; %d left out' % (n, disc, bad))
        return
    for comments, rows in sentences(a.conllu):
        if comment(comments, 'sent_id') == a.sent_id:
            if a.o:
                with open(a.o, 'w', encoding='utf-8') as out:
                    write_one(a.conllu, comments, rows, out)
            else:
                write_one(a.conllu, comments, rows, sys.stdout)
            return
    sys.exit('no sentence %s' % a.sent_id)


if __name__ == '__main__':
    main()
