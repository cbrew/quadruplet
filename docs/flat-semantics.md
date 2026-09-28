# Meanings from parse trees, by folds

The feature grammars in this repository build meanings as they parse: a
category carries a λ-term, and unification composes them. That is slow on a
treebank grammar (§6 of [`fast-parser.md`](fast-parser.md): 49 s against
0.03 s for a 15-word sentence), because the meanings ride on every item of
the chart. Package `go/interp` separates the two. A tree is parsed once,
fast, by `go/cfg`, or taken from the treebank. Everything else anyone wants
from it is a *reading*: a fold over the tree, one function for a word and
one for a phrase, a homomorphism from derivations to some algebra. The
dependencies are one reading; a flat logical form is another.

The idea comes from ÉTUDE, in cbrew/odd_one_out (`dep2tiger/prolog`), which
compiles each TIGER tree into a deterministic Prolog grammar with one rule
per node, so that the annotation is the grammar's only derivation, and gets
facts, dependencies, secondary edges and verb frames from it by walks that
share one scan step, `daughters/3`. Formally this is the view of interpreted
regular tree grammars (Koller and Kuhlmann 2011): a language of derivations,
with homomorphisms into the algebras of whatever is derived. ÉTUDE needs
LCFRS, since German order is discontinuous; MASC's trees are context-free,
so here a derivation is simply a tree, and the walk is `interp.Fold`.

## What a tree needs

A reading of a phrase needs to know which daughter heads it and how the
others relate to it.

**Heads** come from Collins's head table (1999, appendix A), with MASC's NML
and NX headed as NP is, and punctuation never chosen while anything else is
there. Over all 34,582 MASC trees, the dependencies these heads give form a
tree every time. The table's default (the first daughter from the rule's
side) decides the head where Collins gives no priority list, FRAG, UCP,
INTJ and PRN, and for MASC's own categories, EDITED, REF and EQUA.

**Function tags**, SBJ, TMP, LOC and the rest, are in the treebank:
`tools/masc/treebank.py` now writes each tree with them to
`annotated.jsonl`, 106,590 on 491,324 phrases. A tree the parser makes has
none, so `interp.FunctionTable` learns them: for each phrase it counts the
tags seen in its configuration (its parent's category, its own, its side of
the head and distance from it, the head's category, and the word heading
it), backing off to coarser configurations where a fine one was seen fewer
than three times. Learned from nine-tenths of MASC's documents and tested on
the rest (`go/cmd/functions`):

| | |
|---|---|
| phrases whose tags are exactly right | 91.8% (78.1% have none) |
| tags: precision, recall, F1 | 78.9%, 71.7%, 75.2 |

| tag | in the test documents | F1 |
|---|---|---|
| SBJ | 4,249 | 98.5 |
| PRD | 1,203 | 72.2 |
| TMP | 1,137 | 69.8 |
| LOC | 680 | 43.9 |
| CLR | 611 | 45.6 |
| ADV | 449 | 74.6 |
| MNR | 439 | 45.4 |
| PRP | 413 | 58.4 |
| NOM | 359 | 82.2 |
| DIR | 288 | 40.8 |

Structure decides SBJ, NOM and PRD; the semantic tags need the head word,
*in* the morning or *yesterday*, and without it TMP scored 18.6 and LOC 7.6.

## Flat meanings

`interp.Flat` is a neo-Davidsonian reading in the manner of Hobbs's
ontological promiscuity. Every content word introduces a referent, an
event for a verb, and says of it the word. A determiner or particle says
itself of its head's referent. An auxiliary or modal followed by a verb
phrase says itself of that phrase's event, rather than being its head as it
is syntactically. A phrase headed by a preposition, complementizer or the
possessive is about what that word governs, and the word names its relation
to its parent. Every other daughter with a referent of its own stands in a
relation to its phrase's referent, named by its marker, by its function
tags, or by its configuration: `obj`, `nn`, `num`, `poss`, `comp`, `mod`.
The formula quantifies each referent existentially, in word order, over the
conjunction.

*My leg hurts*:

    ∃x1.∃x2.∃x3.(hurts(x3) ∧ leg(x2) ∧ my(x1) ∧ poss(x2, x1) ∧ sbj(x3, x2))

*This is running into a narrative*:

    ∃x1.∃x2.∃x3.(a(x3) ∧ clr_into(x2, x3) ∧ is(x2) ∧ narrative(x3) ∧ running(x2) ∧ sbj(x2, x1) ∧ this(x1))

Over all of MASC (`go/cmd/readings -sem`) that is 701,274 atoms in 2.5 s,
every formula closed. On the test documents, the relations built on the
learned table's tags agree with those built on the treebank's on 89.6% of
31,872; built on no tags, on 70.8%.

## What it leaves out

* **Scope.** A determiner is a condition like any other; *every* and *a* do
  not take scope. The v0 grammar's quantificational event semantics does
  this properly, at the cost of a type for every constituent.
* **Traces.** The backbone grammar has none, so what a relative pronoun or
  a controlled subject is in its clause is not known: *the dog which
  barked* relates the dog to the barking by `which`, not `sbj`. MASC has
  39,212 empty elements that could be kept for gold trees.
* **Lemmas and morphology.** Predicates are word forms, lower-cased: *was*
  and *is* differ, and so do *dog* and *dogs*.
* **Parser output.** A reading of a tree from `go/cfg` needs one tree, and
  the forests hold astronomically many; choosing one needs a probability
  model, the next step for the parser.

## References

Collins, M. (1999). *Head-Driven Statistical Models for Natural Language
Parsing*. PhD thesis, University of Pennsylvania.

Hobbs, J. R. (1985). Ontological promiscuity. *Proceedings of the 23rd
Annual Meeting of the Association for Computational Linguistics*, 61–69.

Koller, A. and Kuhlmann, M. (2011). A generalized view on parsing and
translation. *Proceedings of the 12th International Conference on Parsing
Technologies*, 2–13.
