"""English frames from spaCy dependency parses (ClearNLP labels, as
en_core_web_trf gives them), in the inventory of frames.inventory.

A verb is a token tagged VB* that is not an auxiliary (aux, auxpass): the
same verbs as the treebank's, a copula *be* included. Its frame and
modifiers come from its dependents:

    nsubj, csubj          n (x for *there*, and *it* before a clause)
    nsubjpass, csubjpass  a, the passive's subject; agent (by) n
    expl                  x; after existential *there*, the first attr,
                          nsubj or dobj is the displaced subject, n
    dobj                  a; r if reflexive; with a dative, the dative d
    dative                d if an NP; p if a to/for PP
    attr, acomp           k; for a main-verb *be* without them, its first PP,
                          clause, or locative or wh adverb
    oprd                  o
    xcomp                 i (a verb), o (an adjective or noun)
    ccomp                 by its head: finite s-that, s-2, s-if, s-w, s-X;
                          nonfinite i and verbless o, with a for its subject
    prep                  p or a modifier, by the treebank's rate for the verb
                          and preposition (a prior; the score is kept)
    prt                   p, the particle its marker
    aux, auxpass          modifiers aux:lemma (not *to*); neg neg
    advmod, npadvmod,     modifiers adv, np, clause (with its marker or
    advcl, prep           form), pp

A verb with no subject of its own takes one: a conjunct's from the verb it
is conjoined to, an xcomp's from its controller, an imperative's is the
addressee, a passive's the unnamed agent; else unsaid. A passive's patient
is a: its nsubjpass, or the noun a passive participle modifies. A relative
clause with no relative pronoun has the noun it modifies as its gap: its
object, if it has a subject, else its subject.
"""
from __future__ import annotations

import collections
import json
from collections.abc import Callable

from spacy.tokens import Doc, Token

from frames.inventory import AUXILIARY_LEMMA, REFLEXIVES, Argument, Frame, Modifier, place

FINITE = {"VBD", "VBZ", "VBP", "MD"}
WH = {"WDT", "WP", "WP$", "WRB"}
SUBJECT = {"nsubj", "csubj"}
PASSIVE_SUBJECT = {"nsubjpass", "csubjpass"}
PREDICATIVE_ADVERBS = {"here", "there", "where", "everywhere", "nowhere", "somewhere", "anywhere",
                       "home", "away", "out", "in", "up", "down", "off", "on", "over", "back",
                       "so", "how", "like"}
IGNORED = {"punct", "cc", "conj", "relcl", "intj", "parataxis", "dep", "discourse", "meta",
           "preconj", "appos", "mark", "case", "det", "predet", "nmod", "poss", "compound"}

# P(argument | verb lemma, preposition): the chance a PP is a complement.
PPPrior = Callable[[str, str], float]


def span(t: Token) -> tuple[int, int]:
    """A dependent's words: its subtree's."""
    return (t.left_edge.i, t.right_edge.i + 1)


def head_of(t: Token) -> str:
    """A dependent's lexical head: a preposition's object's, else its own."""
    if t.dep_ in ("prep", "agent", "dative") and t.tag_ in ("IN", "TO"):
        obj = next((c for c in t.children if c.dep_ in ("pobj", "pcomp")), None)
        return (obj.lower_ if obj is not None else None)
    return t.lower_


def verbs(doc: Doc) -> list[Token]:
    return [t for t in doc if t.tag_.startswith("VB") and t.dep_ not in ("aux", "auxpass")]


def form(t: Token) -> str:
    """A clause's verb form, from its head and its auxiliaries."""
    auxes = [c for c in t.children if c.dep_ in ("aux", "auxpass")]
    if t.tag_ in FINITE or any(a.tag_ in FINITE for a in auxes):
        return "finite"
    if any(a.tag_ == "TO" for a in auxes):
        return "to"
    if not t.tag_.startswith("VB"):
        return "verbless"
    return {"VB": "bare", "VBG": "ing", "VBN": "en"}.get(t.tag_, "finite")


def marker(t: Token) -> str | None:
    """A clause's complementizer, or the wh-word that opens it."""
    m = next((c for c in t.children if c.dep_ == "mark"), None)
    if m is not None:
        return m.lower_
    wh = [x for x in t.subtree if x.tag_ in WH and x.i <= t.i]
    return wh[0].lower_ if wh else None


def clause_slot(t: Token) -> str:
    m = marker(t)
    if m in ("if", "whether"):
        return "s-if"
    if m is not None and any(x.tag_ in WH and x.lower_ == m for x in t.subtree):
        return "s-w"
    return "s-" + m if m is not None else "s-2"


def own_subject(v: Token) -> Token | None:
    return next((c for c in v.children if c.dep_ in SUBJECT | PASSIVE_SUBJECT | {"expl"}), None)


def is_passive(v: Token) -> bool:
    return any(c.dep_ in PASSIVE_SUBJECT | {"auxpass"} for c in v.children) or \
        v.tag_ == "VBN" and v.dep_ == "acl" and not any(c.dep_ == "aux" for c in v.children)


def subject_of(v: Token) -> tuple[Token | None, str]:
    """The verb's subject, and where it comes from."""
    s = own_subject(v)
    if s is not None:
        return s, "overt"
    if v.dep_ == "conj":
        return subject_of(v.head)
    if v.dep_ in ("xcomp", "advcl") and v.head.tag_.startswith("VB"):
        ctrl = next((c for c in v.head.children if c.dep_ in ("dobj", "dative")), None)
        return (ctrl or subject_of(v.head)[0]), "controlled"
    if v.dep_ in ("acl", "relcl") and v.head.pos_ in ("NOUN", "PROPN", "PRON"):
        return v.head, "trace"
    if v.tag_ == "VB" and v.dep_ == "ROOT":
        return None, "addressee"
    return None, "unsaid"


def pp_modifier(p: Token, where: str = "verb", score: float | None = None) -> Modifier:
    return Modifier("pp", marker=p.lower_, head=head_of(p), span=span(p), where=where, score=score)


def modifier(c: Token) -> Modifier | None:
    d = c.dep_
    if d in ("aux", "auxpass"):
        if c.tag_ == "TO":
            return None
        return Modifier("aux", marker=AUXILIARY_LEMMA.get(c.lower_, c.lemma_.lower()),
                        where="auxiliary", span=span(c))
    if d == "neg":
        return Modifier("neg", span=span(c))
    if d == "advmod":
        return Modifier("adv", head=c.lower_, span=span(c))
    if d == "npadvmod":
        return Modifier("np", head=c.lower_, span=span(c))
    if d == "advcl":
        return Modifier("clause", marker=marker(c), form=form(c), head=c.lower_, span=span(c))
    if d == "prep":
        return pp_modifier(c)
    return None


def frame_of(v: Token, prior: PPPrior | None = None, threshold: float = 0.5) -> Frame:
    args: dict[str, Argument] = {}
    mods: list[Modifier] = []
    kids = list(v.children)
    passive = is_passive(v)
    lemma = v.lemma_.lower()

    # the subject
    subj, source = subject_of(v)
    if passive:
        agent = next((c for c in kids if c.dep_ == "agent"), None)
        if agent is not None:
            place(args, "n", label="agent", marker="by", head=head_of(agent), span=span(agent))
        else:
            place(args, "n", label="", source="agent")
        if subj is not None:
            place(args, "a", label=subj.dep_, source=source, head=subj.lower_, span=span(subj))
    elif subj is not None and (subj.dep_ == "expl" or subj.tag_ == "EX") or \
            subj is not None and source == "overt" and subj.lower_ == "it" and \
            any(c.dep_ in ("ccomp", "xcomp") and c.i > v.i for c in kids):
        place(args, "x", label=subj.dep_, head=subj.lower_, span=span(subj))
    else:
        place(args, "n", label=subj.dep_ if subj is not None else "", source=source,
              head=subj.lower_ if subj is not None else None,
              span=span(subj) if subj is not None and source == "overt" else None)

    # existential *there*: the NP after the verb is the displaced subject
    displaced = None
    if subj is not None and not passive and subj.lower_ == "there" and "x" in args:
        displaced = next((c for c in kids if c.dep_ in ("attr", "nsubj", "dobj") and c.i > v.i), None)
        if displaced is not None:
            place(args, "n", label=displaced.dep_, head=displaced.lower_, span=span(displaced))

    # a relative clause with no relative pronoun: its gap is the noun it modifies
    if v.dep_ == "relcl" and not passive and \
            not any(x.tag_ in WH or x.lower_ == "that" and x.dep_ in ("nsubj", "dobj", "pobj")
                    for x in v.subtree if x.i < v.i) and \
            own_subject(v) is not None and not any(c.dep_ in ("dobj", "ccomp", "xcomp") for c in kids):
        place(args, "a", label="relcl", source="trace", head=v.head.lower_)

    # a main-verb *be*: its predicative, where spaCy has no attr or acomp, is
    # its first prepositional phrase, clause, or locative or wh adverb
    # (*is in chambers*, *that's why*, *where is he*)
    predicative = None
    if lemma == "be" and not passive and not any(c.dep_ in ("attr", "acomp") for c in kids):
        predicative = next((c for c in kids if c.dep_ in ("prep", "ccomp", "xcomp") and c.i > v.i
                            or c.dep_ == "advmod" and (c.tag_ == "WRB" or c.lower_ in PREDICATIVE_ADVERBS)),
                           None)
    for c in kids:
        d = c.dep_
        if c is subj or c is displaced or d in IGNORED or d in SUBJECT | PASSIVE_SUBJECT | {"expl", "agent"}:
            continue
        common = dict(label=d, span=span(c))
        if c is predicative:
            place(args, "k", marker=c.lower_ if d == "prep" else marker(c) if d == "ccomp" else None,
                  head=head_of(c), form=form(c) if d in ("ccomp", "xcomp") else None, **common)
        elif d == "prt":
            place(args, "p", marker=c.lower_, **common)
        elif d == "dobj":
            if c.lower_ in REFLEXIVES:
                place(args, "r", head=c.lower_, **common)
            else:
                place(args, "a", head=c.lower_, **common)
        elif d == "dative":
            if c.tag_ in ("IN", "TO"):
                place(args, "p", marker=c.lower_, head=head_of(c), **common)
            else:
                place(args, "d", head=c.lower_, **common)
        elif d in ("attr", "acomp"):
            symbol = "o" if any(k.startswith(("a", "d", "r")) for k in args) else "k"
            place(args, symbol, head=c.lower_, **common)
        elif d == "oprd":
            place(args, "o", head=c.lower_, **common)
        elif d == "xcomp":
            f = form(c)
            place(args, "o" if f == "verbless" else "i", head=c.lower_, form=f, **common)
        elif d in ("ccomp", "csubj"):
            f = form(c)
            if f == "finite":
                place(args, clause_slot(c), marker=marker(c), head=c.lower_, form=f, **common)
            else:
                s = own_subject(c)
                m = marker(c)
                if s is not None and m != "for":
                    place(args, "a", label=d + ":subject", head=s.lower_, span=span(s))
                if m is not None and any(x.tag_ in WH and x.lower_ == m for x in c.subtree):
                    place(args, "s-w", marker=m, head=c.lower_, form=f, **common)
                else:
                    place(args, "o" if f == "verbless" else "i", marker=m, head=c.lower_,
                          form=f, **common)
        elif d == "prep":
            score = prior(lemma, c.lower_) if prior is not None else 0.0
            if score >= threshold:
                place(args, "p", marker=c.lower_, head=head_of(c), score=score, **common)
            else:
                mods.append(pp_modifier(c, score=score))
        elif d == "pcomp":
            place(args, "p", marker=None, head=c.lower_, **common)
        else:
            m = modifier(c)
            if m is not None:
                mods.append(m)
    # a conjunct shares the auxiliaries of the verb it is conjoined to
    if v.dep_ == "conj" and not any(c.dep_ in ("aux", "auxpass") for c in kids):
        mods += [m for c in v.head.children if c.dep_ in ("aux", "auxpass", "neg")
                 and (m := modifier(c)) is not None]
    if passive and "a" not in args and not any(k.startswith(("d", "s-", "i")) for k in args):
        if v.dep_ == "acl":
            place(args, "a", label="acl", source="trace", head=v.head.lower_)
    doc_id = v.doc.user_data.get("id", "")
    return Frame(doc_id, v.i, v.text, lemma, passive, tuple(args.values()), tuple(mods))


def frames_of(doc: Doc, prior: PPPrior | None = None, threshold: float = 0.5) -> list[Frame]:
    return [frame_of(v, prior, threshold) for v in verbs(doc)]


def pp_prior(gold_frames, alpha: float = 1.0) -> PPPrior:
    """P(argument | verb lemma, preposition) from gold frames, as the share
    of a verb's PPs with that preposition that are its PP complements (p);
    smoothed towards the
    preposition's share over all verbs, and that towards the overall share.
    """
    args, total = collections.Counter(), collections.Counter()
    for f in gold_frames:
        lemma = f.lemma.split("_")[0]
        for a in f.arguments:
            if a.symbol[0] == "p" and a.marker and a.label.lower() != "prt":   # PPs, not particles
                args[lemma, a.marker] += 1
                total[lemma, a.marker] += 1
                args[None, a.marker] += 1
                total[None, a.marker] += 1
        for m in f.modifiers:
            if m.kind == "pp" and m.marker:
                total[lemma, m.marker] += 1
                total[None, m.marker] += 1
    overall = sum(v for (l, _), v in args.items() if l is None) / \
        max(1, sum(v for (l, _), v in total.items() if l is None))

    def p(lemma: str, prep: str) -> float:
        by_prep = (args[None, prep] + alpha * overall) / (total[None, prep] + alpha)
        return (args[lemma, prep] + alpha * by_prep) / (total[lemma, prep] + alpha)
    return p


def read_frames(path: str) -> list[Frame]:
    with open(path) as f:
        return [Frame.from_json(json.loads(line)) for line in f]
