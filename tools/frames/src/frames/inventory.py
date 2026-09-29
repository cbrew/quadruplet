"""The English frame inventory: what one verb occurrence subcategorises for,
and what modifies it.

It follows odd_one_out's German frames (dep2tiger/frames/frames.py), whose
inventory is Schulte im Walde's:

* A frame is a set of arguments, each under a symbol. A verb has one
  argument of each type, so a second one of a type is a second thing and is
  numbered: a, a2; p, p2.
* The frame's string lists the symbols in a canonical order, a repeated
  symbol spelled again ("naa"), and the clausal slots last ("nas-that").
* A frame describes the verb, not its clause. A passive's surface subject is
  its object and the agent its subject, whether the clause names one or not;
  a controlled infinitive's subject is its controller; an imperative's is
  its addressee. So every frame has n, x or k.
* A copula-like verb with a predicative is "k", its subject in the frame
  but not in the string.
* A particle belongs to the lemma: *pick up* is pick_up.
* Modifiers are a multiset. A modifier is kept with what can be seen of it
  (its kind, its preposition or complementizer, its lexical head), not with
  a semantic label: a scope-bearing modifier is not told apart from others.

The symbols:

    n      subject (every frame has n, x or k)
    x      expletive subject: *there*, or *it* standing for an extraposed clause
    k      predicative of a copula-like verb: *is happy*, *became president*
    a      direct object; a passive's surface subject; the subject of an
           infinitive or small clause the verb takes an object of (*want him
           to go*, *make it better*: a with i or o, as odd_one_out takes
           *lässt ihn kommen*)
    d      indirect object: the first NP of two, or a dative *to*/*for* PP
    o      object predicative: *consider him foolish*, *elected him president*
    p      prepositional object (PP-CLR, -PUT; a stranded preposition)
    i      nonfinite clause: to-infinitive, bare infinitive, gerund, participle
    r      reflexive object: *availed themselves of*
    s-that finite clause with *that*
    s-2    finite clause with no complementizer (reported or quoted)
    s-if   yes/no question, or a clause with *if* or *whether*
    s-w    wh-question or wh-clause
    s-X    finite clause with another complementizer X (*looks like*)
"""
from __future__ import annotations

import collections
import dataclasses
import itertools
from typing import Literal

ORDER = "nxk" + "adopir"          # the canonical order; the s- slots go last
CLAUSE_SLOTS = ("s-that", "s-2", "s-if", "s-w")

# Where an argument's filler comes from. "overt": written in the sentence;
# "trace": an empty element whose antecedent is written elsewhere;
# "controlled": the subject an infinitive takes from its controller;
# "agent": the subject of a passive that names no agent; "addressee": the
# subject of an imperative; "unsaid": a subject with nothing to supply it.
Source = Literal["overt", "trace", "controlled", "agent", "addressee", "unsaid"]

# The kinds of modifier, as can be seen in a tree or a dependency parse.
Kind = Literal["aux", "neg", "pp", "adv", "clause", "np", "adj", "other"]

# Adverbs kept by name in a modifier's key, for they bear on agentivity
# (Dowty's volition) and aspect: the diagnostics of a class like Volitional.
DIAGNOSTIC = frozenset({
    "deliberately", "intentionally", "purposely", "purposefully", "voluntarily",
    "willingly", "reluctantly", "unwillingly", "knowingly", "consciously",
    "carefully", "carelessly", "cautiously", "eagerly", "accidentally",
    "unintentionally", "inadvertently", "unwittingly", "unknowingly", "wilfully", "willfully", "grudgingly", "gladly",
    "happily", "sadly", "slowly", "quickly", "suddenly", "gradually",
    "again", "still", "already", "always", "never", "often", "sometimes",
    "almost", "completely", "partly", "finally",
})

REFLEXIVES = frozenset({
    "myself", "yourself", "himself", "herself", "itself", "oneself",
    "ourselves", "yourselves", "themselves", "themself",
})

# Auxiliaries and modals by lemma: the forms written, contracted and not.
AUXILIARY_LEMMA = {
    "will": "will", "'ll": "will", "wo": "will", "would": "would", "'d": "would",
    "shall": "shall", "should": "should", "can": "can", "ca": "can",
    "could": "could", "may": "may", "might": "might", "must": "must",
    "ought": "ought", "need": "need", "dare": "dare",
    "have": "have", "has": "have", "had": "have", "having": "have", "'ve": "have",
    "be": "be", "is": "be", "are": "be", "am": "be", "was": "be", "were": "be",
    "been": "be", "being": "be", "'s": "be", "'re": "be", "'m": "be",
    "do": "do", "does": "do", "did": "do", "get": "get", "got": "get",
    "gets": "get", "getting": "get", "gotten": "get", "used": "used",
    "going": "going", "gon": "going", "gonna": "going",
}


def base(symbol: str) -> str:
    """The symbol a key was numbered from: a2 is a second a, s-that#2 a
    second s-that; s-2 is not a number."""
    if symbol.startswith("s-"):
        return symbol.split("#")[0]
    stem = symbol[:-1]
    return stem if symbol[-1:].isdigit() and stem in ORDER else symbol


@dataclasses.dataclass(frozen=True, slots=True)
class Argument:
    """One argument: its symbol, the label it had, and what fills it.

    `marker` is a prepositional object's preposition or a clause's
    complementizer or wh-word; `head` the filler's lexical head; `form` a
    clause's verb form (finite, to, bare, ing, en, verbless). `span` is the
    filler's words, [first, last + 1], where it has any. `score` is an
    analyzer's confidence that it is an argument at all (a prepositional
    phrase's, say); None where that was not in question.
    """

    symbol: str
    label: str
    source: Source = "overt"
    marker: str | None = None
    head: str | None = None
    form: str | None = None
    span: tuple[int, int] | None = None
    score: float | None = None


@dataclasses.dataclass(frozen=True, slots=True)
class Modifier:
    """One modifier of the verb, with what can be seen of it. `tag` is the
    treebank's function tag (TMP, LOC ...), which a parser does not give;
    `where` says whether it stood in the verb's own phrase, an auxiliary's,
    or the clause's. `score` is as an argument's: the confidence that it is
    an argument, for a modifier that might have been one."""

    kind: Kind
    marker: str | None = None
    head: str | None = None
    form: str | None = None
    tag: str | None = None
    where: str = "verb"
    span: tuple[int, int] | None = None
    score: float | None = None

    def key(self) -> str:
        """The modifier as the multiset counts it: its kind, and its
        preposition, complementizer or auxiliary; an adverb by name if it is
        diagnostic."""
        if self.kind in ("pp", "clause", "aux") and self.marker:
            return f"{self.kind}:{self.marker}"
        if self.kind == "clause" and self.form:
            return f"clause:{self.form}"
        if self.kind == "adv" and self.head in DIAGNOSTIC:
            return f"adv:{self.head}"
        return self.kind


@dataclasses.dataclass(frozen=True, slots=True)
class Frame:
    """One verb occurrence: its lemma (particle joined), its arguments and
    modifiers, whether it is passive, and flags for what lies around it."""

    id: str
    pos: int
    verb: str
    lemma: str
    passive: bool
    arguments: tuple[Argument, ...]
    modifiers: tuple[Modifier, ...]
    flags: tuple[str, ...] = ()

    def symbols(self) -> str:
        """The frame's string: the symbols in canonical order, a repeated
        one spelled again, the clausal slots last and named once; "k" for a
        copula-like verb with its predicative."""
        if any(a.symbol == "k" for a in self.arguments):
            return "k"
        counts = collections.Counter(base(a.symbol) for a in self.arguments
                                     if not a.symbol.startswith("s-"))
        body = "".join(s * counts[s] for s in ORDER)
        slots = sorted({base(a.symbol) for a in self.arguments if a.symbol.startswith("s-")})
        return body + "+".join(slots)

    def refined(self) -> str:
        """The string with each prepositional object's and dative's
        preposition: "np.on", "nad.to"."""
        s = self.symbols()
        if s == "k":
            return s
        preps = [a.marker for a in self.arguments if base(a.symbol) in ("p", "d") and a.marker]
        return s + ("." + ".".join(sorted(preps)) if preps else "")

    def modifier_keys(self) -> list[str]:
        """The modifier multiset, sorted."""
        return sorted(m.key() for m in self.modifiers)

    def to_json(self) -> dict:
        d = dataclasses.asdict(self)
        d["frame"] = self.symbols()
        d["refined"] = self.refined()
        return d

    @classmethod
    def from_json(cls, d: dict) -> Frame:
        span = lambda x: tuple(x) if x is not None else None
        args = tuple(Argument(**{**a, "span": span(a.get("span"))}) for a in d["arguments"])
        mods = tuple(Modifier(**{**m, "span": span(m.get("span"))}) for m in d["modifiers"])
        return cls(d["id"], d["pos"], d["verb"], d["lemma"], d["passive"], args, mods,
                   tuple(d.get("flags", ())))


def place(args: dict[str, Argument], symbol: str, **filler) -> str:
    """Put an argument in the frame under its symbol, numbering it if that
    symbol is taken (a, a2, a3; s-that, s-that#2). The key used."""
    key = symbol
    for n in itertools.count(2):
        if key not in args:
            break
        key = f"{symbol}#{n}" if symbol.startswith("s-") else f"{symbol}{n}"
    args[key] = Argument(key, **filler)
    return key
