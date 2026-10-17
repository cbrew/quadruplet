"""spaCy's dependency parses (ClearNLP-style labels, as en_core_web_trf gives
them) as Prolog programs, loaded with tools/prolog/dep.pl. See
tools/prolog/README.md and deptree.py.

    python3 clear2pl.py TEXT_FILE [-m MODEL]              # every sentence, to stdout
    python3 clear2pl.py TEXT_FILE --all OUT_DIR [-m MODEL]  # OUT_DIR/NAME-N.pl

Run it with tools/frames' environment, which has spaCy and the model:
tools/frames/.venv/bin/python tools/prolog/clear2pl.py ...

Each non-blank line of TEXT_FILE is parsed as a document, and each sentence
spaCy finds in it is a program, numbered through the file: NAME#1, NAME#2.
The program is as ud2pl.py's, with the parser's labels (nsubj, dobj, prep,
pobj, relcl, ...) and the fine-grained tag in place of the UPOS:

    sentence([w(prp,she), w(vbd,bought,buy), w(nn,yesterday)]).
    vbd(w2) ---> [nsubj:t(prp), ^'--':t(vbd), npadvmod:t(nn)].

* A word is w(Tag, Form), or w(Tag, Form, Lemma) where the lemma is not the
  form; Tag is spaCy's tag_ (Penn Treebank) lower-cased.
* Facts: sent_id/1 (NAME#N), text/1, upos(I, Upos), feats(I, Feats) from
  token.morph, and entity(First, Last, Label) for each named entity that lies
  within the sentence. guidelines(clear), and a comment naming the model.
"""
import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from deptree import DependencyTree, atom, filename, write  # noqa: E402


def convert(sent, sid):
    """(DependencyTree, facts) for a spaCy sentence span."""
    start = sent.start
    words, tags, heads, labels = [], [], [], []
    facts = ['sent_id(%s).' % atom(sid), 'text(%s).' % atom(sent.text)]
    for k, t in enumerate(sent, 1):
        tag = (t.tag_ or t.pos_ or 'x').lower()
        tags.append(tag)
        if t.lemma_ and t.lemma_ != t.text:
            words.append('w(%s,%s,%s)' % (atom(tag), atom(t.text), atom(t.lemma_)))
        else:
            words.append('w(%s,%s)' % (atom(tag), atom(t.text)))
        heads.append(0 if t.head.i == t.i else t.head.i - start + 1)
        labels.append(t.dep_ or 'dep')
        if t.pos_:
            facts.append('upos(%d, %s).' % (k, atom(t.pos_.lower())))
        morph = str(t.morph)
        if morph:
            facts.append('feats(%d, [%s]).' % (k, ','.join(atom(f) for f in morph.split('|'))))
    for e in sent.ents:
        facts.append('entity(%d, %d, %s).' % (e.start - start + 1, e.end - start, atom(e.label_)))
    return DependencyTree(words, tags, heads, labels), facts


def programs(nlp, path):
    """(sid, sentence span) for every sentence of the file."""
    name = os.path.splitext(os.path.basename(path))[0]
    lines = [l.strip() for l in open(path, encoding='utf-8') if l.strip()]
    n = 0
    for doc in nlp.pipe(lines):
        for sent in doc.sents:
            n += 1
            yield '%s#%d' % (name, n), sent


def write_one(sid, sent, model, out):
    tree, facts = convert(sent, sid)
    write(out, 'spaCy %s %s, written by tools/prolog/clear2pl.py' % (model, sid),
          'tools/prolog/dep.pl', tree, 'clear', facts)
    return tree


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('text')
    ap.add_argument('-m', '--model', default='en_core_web_trf')
    ap.add_argument('--all')
    a = ap.parse_args()
    import spacy
    nlp = spacy.load(a.model)
    if a.all:
        os.makedirs(a.all, exist_ok=True)
        n = disc = 0
        for sid, sent in programs(nlp, a.text):
            with open(os.path.join(a.all, filename(sid.replace('#', '-')) + '.pl'), 'w', encoding='utf-8') as out:
                disc += bool(write_one(sid, sent, a.model, out).discontinuous())
            n += 1
        print('%d programs, %d with a discontinuous constituent' % (n, disc))
        return
    for sid, sent in programs(nlp, a.text):
        write_one(sid, sent, a.model, sys.stdout)
        sys.stdout.write('\n')


if __name__ == '__main__':
    main()
