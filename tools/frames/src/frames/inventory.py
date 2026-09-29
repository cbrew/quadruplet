"""The English frame inventory: what one verb occurrence subcategorises for,
and what modifies it.

The analysis of the clause is that of Huddleston and Pullum, *The Cambridge
Grammar of the English Language* (CGEL), chapter 4, "The clause:
complements". Where the inventory departs from CGEL, the departure has to be
argued for; the README lists the ones that stand. The notation is that of
odd_one_out's German frames (dep2tiger/frames/frames.py), after Schulte im
Walde:

* A frame is a set of arguments, each under a symbol. A verb has one
  argument of each type, so a second one of a type is a second thing and is
  numbered: a, a2; p, p2.
* The frame's string lists the symbols in a canonical order, a repeated
  symbol spelled again ("naa"), and the clausal slots last ("nas-that").
* A frame describes the verb, not its clause. A passive's surface subject is
  its object and the agent its subject, whether the clause names one or not;
  a controlled infinitive's subject is its controller; an imperative's is
  its addressee. So every frame has n or x.
* The subject is a complement (CGEL), and is in the string: a copular
  clause is "nk" (complex-intransitive), not "k".
* The indirect object is an NP (CGEL): *gave Mary books* is "nad"; in *gave
  books to Mary*, *to Mary* is a PP complement, "nap".
* A particle is a preposition with no object, functioning as a complement
  (CGEL): *pick up the book* is pick with "nap", the particle the p's
  marker ("nap.up"). The verb and particle together are an idiom of the
  lexicon, not a unit of the syntax, and the lemma is the verb's.
* Existential *there* is a dummy subject, x, and the NP after the verb is
  the displaced subject, n (CGEL): *there is a problem* is "nx". An
  extraposed subject clause stands beside its dummy *it*: "xs-that".
* Modifiers (CGEL's adjuncts) are a multiset. A modifier is kept with what
  can be seen of it (its kind, its preposition or complementizer, its
  lexical head), not with a semantic label: a scope-bearing modifier is not
  told apart from others.

The symbols:

    n      subject; with existential *there*, the displaced subject
    x      dummy subject: existential *there*, or *it* standing for an
           extraposed subject
    k      subjective predicative complement: *is happy*, *became president*
    a      direct object; a passive's surface subject; the object of a
           catenative verb taking an infinitive or small clause (*want him
           to go*, *persuade him to go*, *make it better*: a with i or o;
           CGEL's raised and ordinary objects alike)
    d      indirect object: the first of two NP objects
    o      objective predicative complement: *consider him foolish*
    p      PP complement: a specified preposition (PP-CLR, -PUT, a stranded
           preposition), a dative *to* or *for* PP (PP-DTV), or a particle
    i      nonfinite clause: to-infinitive, bare infinitive, gerund, participle
    r      reflexive object: *availed themselves of*
    s-that finite declarative clause with *that*
    s-2    finite declarative clause with no subordinator (reported or quoted)
    s-if   closed interrogative: a yes/no question, or *if* or *whether*
    s-w    open interrogative: a wh-question or wh-clause
    s-X    finite clause with another subordinator X (*looks like*)
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
        one spelled again, the clausal slots last and named once."""
        counts = collections.Counter(base(a.symbol) for a in self.arguments
                                     if not a.symbol.startswith("s-"))
        body = "".join(s * counts[s] for s in ORDER)
        slots = sorted({base(a.symbol) for a in self.arguments if a.symbol.startswith("s-")})
        return body + "+".join(slots)

    def refined(self) -> str:
        """The string with its PP complements' prepositions and particles,
        sorted: "np.on", "nap.up"."""
        s = self.symbols()
        preps = [a.marker for a in self.arguments if base(a.symbol) == "p" and a.marker]
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
