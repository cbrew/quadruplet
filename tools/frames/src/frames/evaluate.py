"""The spaCy frame analyzer against MASC's gold frames.

    python -m frames.evaluate GOLD_JSONL PARSES.spacy [--out FRAMES_JSONL]

Gold frames come from frames.gold; parses from frames.parse, on MASC's own
tokens, so a verb is the same word position on both sides. The documents
are split (a fifth held out, by a hash of the document's name). The
prepositional-object prior is read off the training documents' gold
frames, and everything is measured on the held-out ones:

* verbs: which word positions are verbs, gold against spaCy;
* per occurrence, on the verbs both find: the frame string exact, with
  prepositions, and each symbol's precision and recall (the lemma, with
  its particles, too);
* modifiers: the multiset of keys, precision and recall;
* per lemma: for the lemmas with at least --min held-out occurrences, the
  total variation distance between the gold and the analyzer's frame
  distributions, against the distance between two halves of the gold
  itself (sampling noise alone);
* the commonest confusions.
"""
from __future__ import annotations

import argparse
import collections
import hashlib
import json

import spacy
from spacy.tokens import DocBin

from frames.analyze import frames_of, pp_prior, read_frames
from frames.inventory import Frame, base


def held_out(tree_id: str, folds: int = 5) -> bool:
    doc = tree_id.split("#")[0]
    return int(hashlib.md5(doc.encode()).hexdigest(), 16) % folds == 0


def tvd(p: collections.Counter, q: collections.Counter) -> float:
    n, m = sum(p.values()), sum(q.values())
    return 0.5 * sum(abs(p[k] / n - q[k] / m) for k in set(p) | set(q))


def prf(tp: int, fp: int, fn: int) -> str:
    p = tp / (tp + fp) if tp + fp else 0.0
    r = tp / (tp + fn) if tp + fn else 0.0
    f = 2 * p * r / (p + r) if p + r else 0.0
    return f"P {100 * p:5.1f}  R {100 * r:5.1f}  F {100 * f:5.1f}"


def symbols(f: Frame) -> collections.Counter:
    return collections.Counter(base(a.symbol) for a in f.arguments)


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("gold")
    ap.add_argument("parses")
    ap.add_argument("--out")
    ap.add_argument("--min", type=int, default=20)
    ap.add_argument("--threshold", type=float, default=0.5)
    ap.add_argument("--all", action="store_true", help="measure on every document, not the held-out ones")
    a = ap.parse_args()

    gold = read_frames(a.gold)
    prior = pp_prior(f for f in gold if not held_out(f.id))
    test = (lambda i: True) if a.all else held_out
    gold_by = {(f.id, f.pos): f for f in gold if test(f.id)}

    nlp = spacy.blank("en")
    sys_by: dict[tuple[str, int], Frame] = {}
    out = open(a.out, "w") if a.out else None
    for doc in DocBin().from_disk(a.parses).get_docs(nlp.vocab):
        if not test(doc.user_data["id"]):
            continue
        for f in frames_of(doc, prior, a.threshold):
            sys_by[f.id, f.pos] = f
            if out:
                out.write(json.dumps(f.to_json()) + "\n")
    if out:
        out.close()

    both = sorted(set(gold_by) & set(sys_by))
    print(f"held-out verbs: gold {len(gold_by)}, spaCy {len(sys_by)}, both {len(both)}")
    print("verb identification:", prf(len(both), len(sys_by) - len(both), len(gold_by) - len(both)))

    exact = refined = lemma_ok = 0
    per = collections.defaultdict(lambda: [0, 0, 0])
    mods = [0, 0, 0]
    confusion = collections.Counter()
    for k in both:
        g, s = gold_by[k], sys_by[k]
        exact += g.symbols() == s.symbols()
        refined += g.refined() == s.refined()
        lemma_ok += g.lemma == s.lemma
        if g.symbols() != s.symbols():
            confusion[g.symbols(), s.symbols()] += 1
        gs, ss = symbols(g), symbols(s)
        for sym in set(gs) | set(ss):
            t = min(gs[sym], ss[sym])
            per[sym][0] += t
            per[sym][1] += ss[sym] - t
            per[sym][2] += gs[sym] - t
        gm, sm = collections.Counter(g.modifier_keys()), collections.Counter(s.modifier_keys())
        t = sum((gm & sm).values())
        mods[0] += t
        mods[1] += sum(sm.values()) - t
        mods[2] += sum(gm.values()) - t
    n = len(both)
    print(f"frame exact {100 * exact / n:.1f}%, with prepositions {100 * refined / n:.1f}%, "
          f"lemma {100 * lemma_ok / n:.1f}%")
    print("symbols:")
    for sym, (tp, fp, fn) in sorted(per.items(), key=lambda x: -(x[1][0] + x[1][2])):
        print(f"  {sym:8s} gold {tp + fn:6d}  {prf(tp, fp, fn)}")
    print("modifiers:", prf(*mods))

    # per lemma distributions
    g_dist, s_dist = collections.defaultdict(collections.Counter), collections.defaultdict(collections.Counter)
    for k in both:
        lemma = gold_by[k].lemma
        g_dist[lemma][gold_by[k].symbols()] += 1
        s_dist[lemma][sys_by[k].symbols()] += 1
    rows = []
    for lemma, g in g_dist.items():
        total = sum(g.values())
        if total < a.min:
            continue
        # two halves of the gold, alternating occurrences: sampling noise
        occ = [gold_by[k].symbols() for k in both if gold_by[k].lemma == lemma]
        noise = tvd(collections.Counter(occ[0::2]), collections.Counter(occ[1::2]))
        rows.append((lemma, total, tvd(g, s_dist[lemma]), noise))
    if rows:
        mean = lambda xs: sum(xs) / len(xs)
        print(f"lemmas with at least {a.min} occurrences: {len(rows)}; "
              f"total variation, gold against spaCy {mean([r[2] for r in rows]):.3f}, "
              f"gold half against half {mean([r[3] for r in rows]):.3f}")
        for lemma, total, d, noise in sorted(rows, key=lambda r: -r[1])[:25]:
            top_g = ", ".join(f"{k} {v}" for k, v in g_dist[lemma].most_common(3))
            top_s = ", ".join(f"{k} {v}" for k, v in s_dist[lemma].most_common(3))
            print(f"  {lemma:12s} {total:5d}  {d:.2f} ({noise:.2f})  gold: {top_g}  |  spaCy: {top_s}")
    print("commonest confusions (gold -> spaCy):")
    for (g, s), k in confusion.most_common(25):
        print(f"  {k:5d}  {g} -> {s}")


if __name__ == "__main__":
    main()
