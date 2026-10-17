"""Gold frames: MASC's verb records (tools/masc/verbframes.py) as English
frames (frames.inventory).

    python -m frames.gold VERBS_JSONL OUT_JSONL

writes a frame record per verb occurrence, keyed by the sentence id and the
verb's word position, and prints the commonest frames.

The mapping, record field by field:

* subject: n; *there*, or *it* where the verb's dependents hold an *EXP*
  trace, x. After existential *there*, the first NP complement (the
  treebank's NP-PRD or NP) is the displaced subject, n. An empty subject is a trace (*T*, *), controlled (*PRO*), or,
  with none, the addressee of an imperative or unsaid.
* a passive (verbframes.py's flag: a VBN with an empty NP object): the
  surface subject is the object, which the empty NP already stands for in
  its place; the *by* phrase, or an unnamed agent, is n.
* complements, in the order of the tree:
  - a particle is a PP complement, p, with the particle as its marker;
  - anything tagged PRD: o after an object, else k;
  - NPs: one is a, two are d and a (the passive's empty one among them); a
    reflexive pronoun is r; a free relative (SBAR-NOM) is an NP;
  - PPs (PP-DTV among them) and ADVP-CLR/-PUT: p, with the preposition;
  - clauses by their type: nonfinite, i; verbless, o; either with an
    overt subject and no *for*, that subject is a (*want him to go*,
    *make it better*); finite, s-that, s-2 (no complementizer) or s-X; a
    question or wh-clause, s-if or s-w. An empty clause is typed by its
    antecedent; one with none (*?*) is s-2, or s-that for an SBAR.
* an extraposed clause whose expletive is the verb's subject (*it appears
  that ...*) is placed as a clause, beside x (Schulte im Walde's xs-dass);
  other extraposed dependents (*ICH*) are left out.
* modifiers: the verb phrase's own, its auxiliaries' and its clause's; the
  auxiliaries and modals, by lemma (*to* is not one); negation.
"""
from __future__ import annotations

import collections
import json
import sys

from frames.inventory import AUXILIARY_LEMMA, REFLEXIVES, Argument, Frame, Modifier, place

SEMANTIC_TAGS = ("TMP", "LOC", "MNR", "PRP", "DIR", "ADV", "EXT", "BNF")
CLAUSAL = {"S", "SBAR", "SQ", "SBARQ", "SINV", "UCP"}


def _span(d: dict) -> tuple[int, int] | None:
    s = d.get("span")
    return tuple(s) if s else None


def _source(d: dict) -> str:
    return "overt" if d["realization"] != "empty" else "trace"


def _head(d: dict) -> str | None:
    if d.get("head"):
        return d["head"]
    a = d.get("antecedent")
    return a["head"] if a else None


def subject(rec: dict, args: dict[str, Argument]) -> None:
    """The subject as the verb's: n or x, and where it comes from."""
    s = rec["subject"]
    passive = "passive" in rec["flags"]
    if passive:
        agent = rec["agent"][0] if rec["agent"] else None
        if agent is not None:
            place(args, "n", label=agent["label"], marker="by", head=_head(agent), span=_span(agent))
        else:
            place(args, "n", label="", source="agent")
        return
    if s is None:
        place(args, "n", label="", source="addressee" if "imperative" in rec["flags"] else "unsaid")
        return
    words = s["words"].lower()
    if s["realization"] == "overt" and (words == "there" or words == "it" and "exp" in rec["flags"]):
        place(args, "x", label=s["label"], head=words, span=_span(s))
        return
    source = "overt" if s["realization"] == "overt" else \
        "controlled" if s["empty"] == "*PRO*" else "trace"
    place(args, "n", label=s["label"], source=source, head=_head(s), span=_span(s))


def clause_slot(c: dict, info: dict | None) -> str:
    """The s- slot of a finite clause, question or wh-clause."""
    if info is None:
        return "s-that" if c["cat"] == "SBAR" else "s-2"
    marker = info.get("marker")
    if info["type"] == "W":
        if marker in ("if", "whether") or c["cat"] == "SQ" or \
                c.get("antecedent", {}).get("label", "").startswith("SQ"):
            return "s-if"
        return "s-w"
    if marker is None:
        return "s-2"
    return "s-" + marker


def existential(rec: dict) -> bool:
    s = rec["subject"]
    return s is not None and s["realization"] == "overt" and s["words"].lower() == "there" \
        and "passive" not in rec["flags"]


def complements(rec: dict, args: dict[str, Argument]) -> None:
    """Place the complements."""
    comps = rec["complements"]
    displaced = next((c for c in comps if c["cat"] == "NP"), None) if existential(rec) else None
    nps = [c for c in comps if (c["cat"] == "NP" or c["cat"] == "SBAR" and "NOM" in c["tags"])
           and "PRD" not in c["tags"] and c is not displaced]
    for c in comps:
        tags = set(c["tags"])
        cat = c["cat"]
        info = c.get("clause")
        common = dict(label=c["label"], source=_source(c), span=_span(c))
        if c is displaced:
            place(args, "n", head=_head(c), **common)
        elif cat == "PRT":
            place(args, "p", marker=c["words"].lower(), **common)
        elif "PRD" in tags:
            symbol = "o" if any(k.startswith(("a", "d", "r")) for k in args) else "k"
            place(args, symbol, head=_head(c), form=info["form"] if info else None, **common)
        elif c in nps:
            head = _head(c)
            if c["realization"] == "overt" and c["words"].lower() in REFLEXIVES:
                place(args, "r", head=head, **common)
            elif len(nps) > 1 and c is nps[0]:
                place(args, "d", head=head, **common)
            else:
                place(args, "a", head=head, **common)
        elif cat == "PP":
            place(args, "p", marker=c.get("marker"), head=_head(c), **common)
        elif cat in ("ADVP", "ADJP"):
            place(args, "p", head=_head(c), **common)
        elif cat in CLAUSAL:
            if info is None and c["realization"] != "empty":
                place(args, "a", head=_head(c), **common)          # a UCP of NPs, say
                continue
            if info is not None and info["type"] == "I":
                if info.get("subject") == "overt" and info.get("marker") != "for":
                    sp = info.get("subject_span")
                    place(args, "a", label=c["label"] + ":subject", source=common["source"],
                          head=info.get("subject_head"), span=tuple(sp) if sp else None)
                symbol = "o" if info["form"] == "verbless" else "i"
                place(args, symbol, marker=info.get("marker"), head=_head(c), form=info["form"], **common)
            else:
                place(args, clause_slot(c, info), marker=info.get("marker") if info else None,
                      head=_head(c), form=info["form"] if info else None, **common)
        else:
            place(args, "a", head=_head(c), **common)


def modifier(m: dict, where: str = "verb") -> Modifier:
    cat = m["cat"]
    info = m.get("clause")
    tag = next((t for t in m["tags"] if t in SEMANTIC_TAGS), None)
    common = dict(tag=tag, where=m.get("where", where), span=_span(m), head=_head(m))
    if cat in ("PP", "WHPP"):
        return Modifier("pp", marker=m.get("marker"), **common)
    if cat in ("ADVP", "WHADVP") or cat.startswith("RB"):
        return Modifier("adv", **common)
    if cat in ("S", "SBAR", "SQ", "SBARQ", "SINV"):
        marker = (info or {}).get("marker") or m.get("marker")
        return Modifier("clause", marker=marker, form=(info or {}).get("form"), **common)
    if cat == "NP":
        return Modifier("np", **common)
    if cat == "ADJP":
        return Modifier("adj", **common)
    return Modifier("other", **common)


def frame(rec: dict) -> Frame:
    args: dict[str, Argument] = {}
    subject(rec, args)
    complements(rec, args)
    if "x" in args:
        for e in rec["extraposed"]:
            info = e.get("clause")
            if info is not None and info["type"] in ("S", "W"):
                place(args, clause_slot(e, info), label=e["label"], marker=info.get("marker"),
                      head=_head(e), form=info["form"], span=_span(e))
            elif info is not None:
                place(args, "i", label=e["label"], marker=info.get("marker"), head=_head(e),
                      form=info["form"], span=_span(e))
    mods = [modifier(m) for m in rec["modifiers"]]
    mods += [modifier(m) for m in rec["modifiers_above"]]
    mods += [Modifier("aux", marker=AUXILIARY_LEMMA.get(w.lower(), w.lower()), where="auxiliary")
             for w in rec["auxiliaries"] if w.lower() != "to"]
    if rec["negated"]:
        mods.append(Modifier("neg"))
    return Frame(rec["id"], rec["pos"], rec["verb"], rec["lemma"], "passive" in rec["flags"],
                 tuple(args.values()), tuple(mods), tuple(rec["flags"]))


def main(src: str, dst: str) -> None:
    frames = collections.Counter()
    refined = collections.Counter()
    n = 0
    with open(src) as f, open(dst, "w") as out:
        for line in f:
            fr = frame(json.loads(line))
            out.write(json.dumps(fr.to_json()) + "\n")
            frames[fr.symbols()] += 1
            refined[fr.refined()] += 1
            n += 1
    print(f"{n} verbs, {len(frames)} frames, {len(refined)} with prepositions")
    for s, k in frames.most_common(40):
        print(f"  {k:6d}  {100 * k / n:5.1f}%  {s}")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
