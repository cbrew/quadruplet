"""Parse MASC's sentences with spaCy, on MASC's own tokens.

    python -m frames.parse VERBS_JSONL OUT.spacy [--model en_core_web_trf]

reads the sentences of tools/masc/verbframes.py's records (the overt words
of each tree), and writes their greedy parses to a DocBin, each Doc's
user_data["id"] the tree's id. A Doc is built from the tokens, so its word
positions are the records' (the treebank's escapes, -LRB- and ``, are
written as the characters), and it is one sentence.
"""
from __future__ import annotations

import argparse
import json
import sys
import time

import spacy
from spacy.tokens import Doc, DocBin

ESCAPES = {"-LRB-": "(", "-RRB-": ")", "-LCB-": "{", "-RCB-": "}", "-LSB-": "[", "-RSB-": "]",
           "``": '"', "''": '"'}


def sentences(verbs_jsonl: str) -> dict[str, list[str]]:
    """Each tree's id and its overt words, in corpus order."""
    out: dict[str, list[str]] = {}
    with open(verbs_jsonl) as f:
        for line in f:
            r = json.loads(line)
            out.setdefault(r["id"], r["sentence"].split())
    return out


def doc_of(nlp, words: list[str], doc_id: str) -> Doc:
    d = Doc(nlp.vocab, words=[ESCAPES.get(w, w) for w in words],
            sent_starts=[True] + [False] * (len(words) - 1))
    d.user_data["id"] = doc_id
    return d


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("verbs")
    ap.add_argument("out")
    ap.add_argument("--model", default="en_core_web_trf")
    ap.add_argument("--limit", type=int)
    a = ap.parse_args()
    nlp = spacy.load(a.model, exclude=["ner"])
    items = list(sentences(a.verbs).items())[: a.limit]
    docs = (doc_of(nlp, w, i) for i, w in items)
    db = DocBin(store_user_data=True)
    t = time.time()
    for k, d in enumerate(nlp.pipe(docs, batch_size=64), 1):
        db.add(d)
        if k % 2000 == 0:
            print(f"{k}/{len(items)} {time.time() - t:.0f}s", file=sys.stderr, flush=True)
    db.to_disk(a.out)
    print(f"{len(items)} sentences in {time.time() - t:.0f}s", file=sys.stderr)


if __name__ == "__main__":
    main()
