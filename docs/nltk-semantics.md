# The semantics of NLTK's grammars: what quadruplet has, and what it would need

NLTK ships 21 feature grammars (`.fcfg`) across its `book_grammars`,
`sample_grammars` and `large_grammars` packages. quadruplet began as a
reimplementation of NLTK's feature-grammar parser, and
[issue #2](https://github.com/cbrew/quadruplet/issues/2) asked for those
grammars to be supported. The goal has since narrowed: quadruplet keeps
its own notation, in particular semantics written as `sem=<...>`, and
what we want is the *behaviour* the NLTK grammars demonstrate, not their
spelling. This note surveys that behaviour, grammar by grammar, against
what quadruplet has, and sizes the gaps. It was written in October 2026
from the grammars in `nltk_data` and the NLTK modules behind them
(`nltk.sem.util`, `cooper_storage`, `hole`, `evaluate`, `drt`, `logic`).

The short version: the grammars demonstrate eight behaviours. quadruplet
has one in full, two are small additions on top of what exists, two are
new components of real size, and three depend on things outside a
parser. Nothing in the list conflicts with the `<...>` notation or with
the unifier's separation of feature-structure variables from
semantic-term variables.

## 1. The behaviours, grammar by grammar

| NLTK grammars | behaviour | quadruplet today | needed |
|---|---|---|---|
| `simple-sem`, `sem0`, `sem1`, `sem2`, `event`; the syntax of `chat80` and `discourse` | compositional Montague readings in first-order logic with lambda terms | complete; `sem2.fcfg` is a passing test, and the MASC grammar goes further | nothing |
| `simple-sem` with `model0`, `sem2` in the book, `chat80` against the Chat-80 world | truth of a reading in a finite model, and the satisfiers of a wh-question | nothing | a model evaluator (section 3) |
| `storage`, `bindop` | scope ambiguity by Cooper storage: a core term and a store of binding operators, retrieved in every order | the term language, tuples with `+`, `@x` variables, and retrieval; `storage.fcfg` is a passing test | nothing for `storage`; `bindop` untried (section 2) |
| `hole` | underspecified scope: one formula of holes, labels and dominance constraints, then every legal plugging | the formula itself builds today | a plugging module (section 4) |
| `drt` | Discourse Representation Structures with merge, translation to logic, and pronoun resolution | boxes, merge, `fol()`, anaphora resolution and discourse readings; `drt.fcfg` and `drt-storage.fcfg` are passing tests (section 5) | provers and Glue, out of scope (section 6) |
| `discourse` | consistency and informativeness of a discourse, checked by a theorem prover | the readings | an external prover (section 6) |
| `gluesemantics` | syntax only; the meanings live in NLTK's glue lexicon and a linear-logic prover | nothing | out of scope (section 6) |
| `sql0`, `sql1`, `sql` | an SQL string built by tuple concatenation, run on a city database | tuple `+` expressions | string values and a join (section 6) |

The remaining grammars, `feat0`, `feat1`, `german`, `np`, `alvey` and
`chat_pnames`, have no semantics. `feat1` uses slash categories
(`VP/?x`) and `german`, `np`, `storage` and `bindop` use nested feature
maps, both of which are notation questions outside this note.

The first row needs a word. NLTK's `interpret_sents` returns one
`(tree, semantic representation)` pair per parse tree, read off the
`SEM` feature of the root. quadruplet's chart does the same per complete
edge, with the difference that trees whose meanings simplify to the same
term share an edge, so it reports readings rather than trees. For the
grammars in that row the two agree reading for reading; `sem2.fcfg` in
`src/test/resources` is NLTK's grammar in quadruplet's notation, and
`ChartTest` checks its readings. NLTK's spellings, `app(f, x)`, `(f x)`,
`and`, `implies`, `not`, and a bare `SEM=?x` for `sem=<?x>`, are
spellings only. NLTK's logic also has `iota` and a type checker; no
grammar uses `iota`, and quadruplet's lambda calculus is untyped by
design (the types in [semantics.md](semantics.md) are a discipline the
grammar follows, not something the parser checks).

## 2. Cooper storage

This is the smallest piece, and [semantics.md](semantics.md) already
names it as the obvious route to scope ambiguity. NLTK's `storage.fcfg`
gives every category a semantics with two parts:

    NP[SEM=[CORE=<@x>, STORE=((<bo(?det(?n),@x)>)+?b1+?b2)]] -> Det[SEM=[CORE=?det, STORE=?b1]] N[SEM=[CORE=?n, STORE=?b2]]

The core is the meaning with each quantified noun phrase replaced by a
placeholder variable `@x`; the store collects, for each such phrase, a
*binding operator* `bo(Q, @x)` pairing the quantifier with its
placeholder. Retrieval (`CooperStore.s_retrieve`) takes every
permutation of the store and, starting from the core, applies each
operator in turn: abstract the current term over `@x`, apply `Q` to the
abstraction, simplify. Each permutation is one scoping; "every girl
chases a dog" gets both.

Retrieval now exists (`retrieve` in `com.cbrew.logic`, with
`abstractOver` in `Unify.kt`), applied by the caller to a complete edge;
`src/test/resources/storage.fcfg` is the grammar below, and the rest of
this section is the design as it was planned.

quadruplet can express the grammar today. Nested feature maps are
unsupported, so `SEM=[CORE=…, STORE=…]` becomes two features,
`core=<…>` and `store=(…)`; the store is a tuple built with the `+`
expressions the notation already has; and `<@x>` is read as a semantic
variable (`FstructVar("@x")`) that renaming apart makes distinct per
noun phrase and that unification leaves free. What is missing:

* **Abstraction over a free named variable** in a de Bruijn term: turn
  `t` into `λ.t'` where `t'` has the bound index in place of every free
  occurrence of `@x`, shifted correctly under existing binders. This is a
  cousin of `placeBoxes` in `Unify.kt`, a few dozen lines.
* **Retrieval**: permutations of the store, the fold above, using the
  existing `createApp` and simplification. Perhaps a hundred lines with
  a convenience that takes a complete edge's feature map.
* **Where it runs.** Readings are per edge; retrieval multiplies them per
  tree, so "reading" would gain a level below "tree" in the chart's
  accounting, or retrieval stays a function the caller applies to a
  complete edge. The latter is the smaller first step.

It does not apply to the MASC grammar as written. MASC's verbs carry
their own event quantifier and a condition argument (Champollion's
style, semantics.md section 3), and a storage grammar would have to be
written in the storage style from the start. Storage is for grammars
that want scope ambiguity, which MASC, by design, does not yet.

## 3. A model evaluator

NLTK's `evaluate_sents` takes each reading and asks whether it is true in
a model: a domain of entities, a valuation mapping constants to entities
and relations, and an assignment to free variables. `Model.satisfy` is
the textbook recursive definition, iterating the domain under each
quantifier; `satisfiers` returns the set of entities that make an open
formula true, which is how a wh-question is answered, and a lambda
abstraction evaluates to a set. The book uses it with a ten-entity
`model0`; the `chat80` grammar is evaluated against a valuation built
from the Chat-80 Prolog fact files (`nltk.sem.chat80`).

quadruplet has nothing of this. On de Bruijn terms the evaluator is an
environment stack indexed by binder depth, which is natural to write, in
the order of three or four hundred lines with a reader for NLTK's
`Valuation.fromstring` format. It would also give the test suite a second
oracle, truth in `model0`, independent of the golden files. Two things
to decide: the generalised quantifiers MASC uses, such as `many(…)`, are
not first-order and would need a stated convention or a refusal; and
the Chat-80 world needs its own loader if that demo is wanted.

## 4. Hole semantics

`hole.fcfg` produces one formula per sentence, an *underspecified
representation* in which quantifiers and predicates are fragments with
labels, scope positions are holes, and `LEQ(label, hole)` constraints
say what may go where:

    Det[SEM=<\P Q h l.exists h1 l1 l2 l3 x.(ALL(l2,x,l3) & IMP(l3,l1,h1) & LEQ(l,h1) & LEQ(l2,h) & P(x)(h)(l1) & Q(x)(h)(l) & HOLE(h) & HOLE(h1) & LABEL(l) & LABEL(l1) & LABEL(l2) & LABEL(l3))>] -> 'every'

NLTK's `HoleSemantics` then breaks the conjunction into holes, labels,
fragments and constraints, enumerates the legal *pluggings* of labels
into holes, and rebuilds a formula per plugging.

The formula itself builds in quadruplet today: the logic notation accepts
curried application `P(x)(h)(l1)`, several variables under one `exists`,
and upper-case constants. The work is a port of the plugging algorithm,
about four hundred lines of Python and probably three hundred of Kotlin,
operating on the built term. One caution: the simplifier sorts, flattens
and deduplicates conjunctions and drops vacuous quantifiers, which should
be harmless for a USR but wants a test.

## 5. Discourse Representation Theory

Done. The design is in
[the DRT design](superpowers/specs/2026-10-17-drt-design.md). A grammar
whose first line is `%semantics drt` reads `<...>` as DRSs. The boxes
`Drs`, `DrsImp` and `Resolved` are nodes of the existing term language,
and a box's referents are de Bruijn binders, so accessibility is scope
and no alpha-conversion is needed. `fol()`, `resolveAnaphora` and the
discourse readings are in `com.cbrew.logic/Drt.kt`. What follows is the
analysis made before the work, kept for the reasoning.

`drt.fcfg` builds DRSs rather than formulas:

    PropN[-LOC,NUM=sg,SEM=<\P.(DRS([x],[Angus(x)])+P(x))>] -> 'Angus'
    Det[NUM=sg,SEM=<\P Q.DRS([],[((DRS([x],[])+P(x)) implies Q(x))])>] -> 'every'
    PropN[-LOC,NUM=sg,SEM=<\P.(DRS([x],[PRO(x)])+P(x))>] -> 'he'

A DRS is a box of discourse referents and conditions; `+` merges boxes,
with alpha-conversion so referents do not clash; boxes can be negated and
stand in an implication; `fol()` translates a box to logic; and
`resolve_anaphora` replaces each `PRO(x)` by an equation with an
accessible antecedent, offering every candidate. This is what makes
"a dog barks. it walks." work across sentences.

This is the large piece. It is a second term language beside `Lambda`:
its own sealed family, its own reduction (beta reduction that merges
boxes), a translation to the existing logic, a printer, and the
resolution search. De Bruijn indices fit it, since a box's referents are
binders, but little of the existing code is reused. NLTK's `drt.py` is
1,500 lines including a box drawer; the core would be perhaps six to nine
hundred lines of Kotlin. The decision to make first is whether DRSs would
replace the logical forms or sit beside them, and whether the flat,
PMB-style meanings sketched in [flat-semantics.md](flat-semantics.md)
are the version actually wanted, since the Parallel Meaning Bank is DRT
underneath.

## 6. What to leave out

* **Discourse checking.** `discourse.fcfg` is an ordinary first-order
  grammar; the behaviour is NLTK's `DiscourseTester`, which hands the
  readings of a sequence of sentences, plus background knowledge, to
  Prover9 and Mace4 to check consistency and informativeness. The
  readings are already there. A checker would shell out to a prover; it
  is not a parser feature.
* **Glue semantics.** `gluesemantics.fcfg` has no semantics in it. The
  meanings come from NLTK's glue lexicon and a linear-logic prover, and
  the demo uses a dependency parser. Out of scope.
* **SQL.** The `sql` grammars build a query string by tuple concatenation
  (`SEM=(?np + WHERE + ?vp)`) and run it on a SQLite city database.
  quadruplet has the tuple expressions; the parser-side gaps are a
  quoted-string feature value (`'Country="greece"'`) and joining a tuple
  into a string. Small, and only worth doing for the book's example.

## 7. Order of work

1. **Cooper storage** (section 2): smallest, already planned for, reuses
   everything. Done.
2. **The model evaluator** (section 3): unlocks `evaluate_sents` on the
   simple grammars and the chat80 queries, and doubles as a test oracle.
3. **Hole semantics** (section 4): self-contained, medium.
4. **DRT** (section 5): large; decide against the PMB-style alternative
   first. Done.
