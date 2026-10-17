# Discourse Representation Theory in quadruplet: design

Status: draft for review, 2026-10-17.

## Intent

quadruplet's feature grammars build first-order formulas. NLTK's
`book_grammars/drt.fcfg` builds Discourse Representation Structures
(DRSs) instead: boxes of discourse referents and conditions, which let a
pronoun in one sentence pick up a referent introduced in an earlier one.
The aim is to reproduce the behaviours the NLTK book shows for DRT
(chapter 10, section 5), in quadruplet's own notation, as part of the
wider goal of supporting what NLTK's grammars demonstrate without copying
NLTK's notation ([nltk-semantics.md](../../nltk-semantics.md), section 5,
which this document supersedes for DRT).

## Decided by the user

* **Scope is the NLTK book's examples** (listed under Success).
* **A declaration in the grammar file switches the semantic formalism.**
  A grammar is either a logic grammar, as all grammars are today, or a
  DRT grammar. One grammar uses one formalism throughout.
* **The whole discourse is one input**, for example
  `Angus owns a dog . It bites him .`, parsed as one chart. There is no
  separate per-sentence parse and no DiscourseTester-style threading of
  separately parsed sentences.
* **Pronoun resolution gives one reading that lists all candidates**, as
  the book's `resolve_anaphora` does: `PRO(u)` becomes `u = [x, y, z]`,
  with every accessible referent. It does not give one reading per
  antecedent.
* **DRT readings have scope ambiguity**, from a storage-style DRT
  grammar, so that `Every dog chases a boy . He runs .` has the book's
  valid reading (items 16 and 17).
* Standing decisions from earlier work: NLTK's notation is not copied
  verbatim; semantics stays inside `<...>`; nested feature maps are not
  supported (CLAUDE.md); the MASC grammar is untouched.

## Goals

1. A DRT grammar can be written and parsed, and its readings are DRSs.
2. DRS operations the book uses: printing, merge (`+`) with referents
   kept apart, translation to first-order logic (`fol()`), and anaphora
   resolution with accessibility.
3. A discourse of several sentences, joined by a grammar rule, gives one
   merged DRS in which later pronouns can see earlier referents.
4. Readings in which some pronoun has no accessible antecedent are
   dropped, as the book's `filter=True` does.
5. Scope ambiguity in DRT grammars written in the Cooper storage style,
   reusing `retrieve` from `CooperStorage.kt`.

## Non-goals

* Consistency and informativeness checks (book items 12 and 14): they
  need the Prover9 and Mace4 theorem prover and model builder.
* Glue semantics and the Malt dependency parser (book item 15). Cooper
  storage takes Glue's place as the source of scope readings.
* `draw()`, the graphical box display (item 3).
* Gender and number filtering of antecedents. NLTK does none; the
  candidate list is every accessible referent.
* The Go port (`go/`). Golden files cover the existing six grammars; DRT
  is Kotlin only for now.
* Changes to the chart's tree or reading accounting. Resolution, like
  Cooper storage retrieval, is a function applied to a complete edge.

## Anti-goals (the builder's reading; check with the user)

* A logic grammar behaves differently in any way, or an existing
  test's assertions change. Without the declaration, nothing changes.
* A DRT that only works on whole sentences: referents from one sentence
  must be visible to the next, or the discourse examples fail.

## Success: the book's examples

Each book example that needs nothing outside NLTK becomes a test, in
quadruplet's notation and printer. NLTK prints are shown here for the
shape. quadruplet's prints will differ in variable names (x1, x2, ... by
depth, as `pretty()` prints today) and spacing; the tests match on the
strings quadruplet produces, checked by hand against these.

| book item | input | operation | NLTK output |
|---|---|---|---|
| 2 | `([x, y], [angus(x), dog(y), own(x, y)])` | read and print | `([x,y],[angus(x), dog(y), own(x,y)])` |
| 4 | the same | `fol()` | `exists x y.(angus(x) & dog(y) & own(x,y))` |
| 5, 6 | `([x], [walk(x)]) + ([y], [run(y)])` | print, then merge | `([x,y],[walk(x), run(y)])` |
| 7 | `([], [(([x], [dog(x)]) -> ([y], [ankle(y), bite(x, y)]))])` | `fol()` | `all x.(dog(x) -> exists y.(ankle(y) & bite(x,y)))` |
| 8 | `([x, y], [angus(x), dog(y), own(x, y)]) + ([u, z], [PRO(u), irene(z), bite(u, z)])` | merge | `([u,x,y,z],[angus(x), dog(y), own(x,y), PRO(u), irene(z), bite(u,z)])` |
| 9 | item 8's result | resolve anaphora | `([u,x,y,z],[angus(x), dog(y), own(x,y), (u = [x,y,z]), irene(z), bite(u,z)])` |
| 10 | "Angus owns a dog" with drt.fcfg | parse | `([x,z2],[Angus(x), dog(z2), own(x,z2)])` |
| 16, 17 | "Every dog chases a boy. He runs." | readings, then filter | see below |

Items 16 and 17 need care. In the book the two readings of "Every dog
chases a boy" come from Glue semantics, which is out of scope; here they
come from Cooper storage, with a storage-style DRT grammar (see
"Scope ambiguity" below). The book's results, with its variable names:

    d0: ['s0-r0', 's1-r0'] : INVALID: AnaphoraResolutionException
    d1: ['s0-r1', 's1-r0'] : ([z6,z10],[boy(z6), ((([x],[dog(x)]) -> ([],[chases(x,z6)])), (z10 = z6), runs(z10)])

So the tests are:

* With the storage-style DRT grammar, `Every dog chases a boy . He runs .`
  has two readings before filtering, one per scoping of the first
  sentence, and exactly one after: the book's d1, in which "a boy"
  outscopes "every dog" and "He" has the single candidate "a boy". In
  the other reading "a boy" is inside the implication, so "He" has no
  accessible antecedent.
* With the plain `drt.fcfg`, which has surface scope only, the same
  discourse has no reading after filtering.

Also: `Angus owns a dog . It bites him .` (the book's motivating
discourse, built with the plain `drt.fcfg` rather than by hand as in
items 8 and 9) gives one reading in which each pronoun lists the referents accessible
to it.

## The declaration

A DRT grammar starts with a declaration line, `%semantics drt`. A logic
grammar may say `%semantics logic`, or nothing, which means logic.
The declaration is needed for a concrete reason as well as a conceptual
one: inside `<...>` the island lexer reads `[` and `]` as parts of
constants (`FeatLexer.g4`, the `Constant` token), so box notation cannot
be read in logic mode. The declaration selects how `<...>` is read, how
readings print, and which after-parse steps apply (`fol()`, resolution).

How the reader switches is the builder's call. Two workable ways: a
second island mode in the lexer chosen by a flag the declaration sets
(ANTLR lexer modes are static, so the choice is a semantic predicate on
`<`); or capturing `<...>` as one raw token and handing its text to a
term parser chosen by the declaration. The second keeps the DRT notation
in its own small grammar file.

## Where boxes live: two options

This design builds option A. Option B is recorded as the alternative
considered. (Choosing A is the designer's call, for the user to confirm
when approving this document.)

**Option A (chosen): boxes are new nodes in the existing term
language.** `Lambda` (the sealed class in `Unifiable.kt`) gains a box
node and a merge node, perhaps a pronoun-resolution condition node too.
Everything else keeps working on DRT terms unchanged in kind: λ, `?x` and
`@x` variables, unification (which compares semantic terms for
equality), substitution, renaming apart, de Bruijn indices, `createApp`
and beta reduction, `abstractOver`. Each exhaustive `when` over `Lambda`
gains cases: 13 in `Unify.kt` (`subterms`, used to find variables;
`mapVariablesLam`, used for substitution and renaming; `quantifierBinds`;
`shiftQuantifiers`; `betaReducible`; `Instantiation.run`, the beta
reduction step; `placeBoxes`; `abstractOver`; `substBoxes`; `qshift`;
`shift`; `closed`; `get`), one in `Pretty.kt`, and the non-exhaustive
`betaReduce`, which falls through to an error. The cached de Bruijn facts
(`freeVarDepth`, `freeQVarDepth`, `hasBox`) need defining for the new
nodes so that sharing keeps working.

**Option B: a separate term family for DRT.** Cleaner in isolation, but
DRT terms contain λ-terms (`\x.([], [walk(x)])`) and need everything
listed above, so B duplicates the λ-calculus machinery.
[nltk-semantics.md](../../nltk-semantics.md) estimated six to nine
hundred lines of Kotlin with little reuse; that estimate was for B.

A makes the declaration a switch of notation, printing and after-parse
steps over one term language. B makes it a switch of term language. A is
less code and gets correct variable handling for free from code that is
already tested; its cost is that the core term code grows new cases that
logic grammars never use.

## Design under option A

### Referents are binders, and accessibility is scope

In NLTK, `\P.(DRS([x],[Angus(x)]) + P(x))` works by capture: the `x` in
`P(x)` is outside the box, and merge makes it the box's referent. That
is dynamic binding: in `K1 + K2`, the referents of `K1` bind in `K2`;
in `K1 -> K2`, the referents of `K1` bind in `K2`. With de Bruijn
indices this becomes ordinary scope:

* a box with *k* referents binds *k* indices over its conditions;
* a merge `K1 + K2` binds the referents `K1` introduces over `K2`;
* an implication between boxes binds the antecedent's referents over the
  consequent.

The builder's call, recommended: referents use the quantified-variable
numbering (`QVar`), since `fol()` turns them into quantifiers and
`pretty()` already names QVars x1, x2, ... by depth. The notation reader
binds a name like `x` to the nearest enclosing referent binder, as
`LogicVisitor.binarize` already does for quantifiers with its binding
stack.

Two consequences. First, NLTK's alpha conversion on merge (renaming `z2`
and so on) disappears: indices cannot clash. Second, accessibility needs
no separate definition. The referents accessible at a pronoun are exactly
the referent binders in scope there, which the resolution walk collects
on its way down.

### Reduction

Merge has a simplifying factory, in the style of `createAnd`:
`([r1], [c1]) + ([r2], [c2])` becomes `([r1 r2], [c1 c2])`, with the
indices in `c1` and `c2` renumbered for the combined binder. Merging is
done whenever both sides are boxes, so a reduced reading is a single box
(with boxes nested only inside conditions such as implication and
negation). The existing quantifier factories drop vacuous quantifiers;
box referents must not be dropped that way before a merge, since
`([x], []) + P(x)` uses the referent only after the merge. Whether a box
ever drops an unused referent is the builder's call; NLTK keeps them.

Box conditions are an ordered list, printed in order, as NLTK prints
them. (Conjunctions in the logic are sets, and `pretty()` sorts them;
boxes deliberately do not.)

### Printing

`pretty()` prints a box as `([x1, x2], [angus(x1), dog(x2), own(x1, x2)])`,
negation as `¬K`, implication between boxes as `(K1 → K2)`, and a
resolved pronoun as `(x3 = [x1, x2])`. Exact spacing and symbols are the
builder's call, consistent with `pretty()` today.

### `fol()`

A function from a DRT term to the existing logic: a box with referents
`r` and conditions `c` becomes `∃r.(c1 ∧ ... ∧ cn)`; `K1 → K2` becomes
`∀r1.(c1 → fol(K2))`; negation maps to negation. It should produce the
same terms the logic factories produce (`createExistential` and so on),
so the result prints with `pretty()` and compares with logic readings.
A resolved pronoun's candidate list translates to a disjunction of
equalities, `(u = x ∨ u = y)`; this is the builder's call, since the book
never applies `fol()` to a resolved DRS.

### Anaphora resolution

A function applied to a complete edge's DRS, like `retrieve` for Cooper
storage. It walks the term, keeping the referents in scope. A condition
`PRO(u)` is replaced by `u = [candidates]`: every referent in scope
except `u` itself, including referents of the same box (book item 9 lists
`z`, introduced in the same box as `u`). A DRS in which some pronoun has
an empty candidate list is rejected: resolution returns nothing for it,
and the caller drops that reading. The order of candidates is the
builder's call; nearest first, or outermost first, either is fine if
fixed.

### Discourse

The translated grammar adds a discourse category and a full stop:

    D[sem=<?s + ?d>] -> S[sem=<?s>] "." D[sem=<?d>]
    D[sem=<?s>] -> S[sem=<?s>] "."

(names and exact form the builder's call). Because merge binds the left
referents over the right, pronouns in later sentences see earlier
referents with no further machinery. The full stop is an ordinary token,
already supported as a quoted word in a rule. This is the plain
`drt.fcfg`'s discourse rule; the storage-style grammar collects
sentences instead (see "Scope ambiguity").

### The grammar file

`src/test/resources/drt.fcfg`: NLTK's `drt.fcfg` translated, as
`storage.fcfg` was: lower-case feature names (`sem`, `num`, `loc`),
`+loc`/`-loc` for NLTK's `+LOC`, quadruplet's box notation, plus the
discourse rules and the object pronoun "him", which `drt.fcfg` lacks.
`app(?subj,?vp)` in NLTK is application, written `?subj(?vp)` in
quadruplet.

### Scope ambiguity: a storage-style DRT grammar

A second grammar file (`drt-storage.fcfg`, name the builder's call)
writes `drt.fcfg` in the storage style of `storage.fcfg`: each category
has `core` and `store` features; a noun phrase's core is a placeholder
`@x` and its store holds `bo(Q, @x)`, where `Q` is the DRT quantifier
`drt.fcfg` gives its determiner (or proper name, or pronoun) applied to
the noun. `retrieve` (`CooperStorage.kt`) then works unchanged on DRT
terms, provided `abstractOver` and beta reduction handle the new nodes:
retrieving "a boy" applies `\Q.(([x], [boy(x)]) + Q(x))` to the core
abstracted over its placeholder, and the placeholder becomes the box's
referent.

**Retrieval is per sentence, not per discourse.** If stores were pooled
across the whole discourse, a quantifier from one sentence could be
retrieved with scope over the next ("every dog" outscoping "He runs"),
which DRT does not allow and the book does not produce. So the discourse
rule does not merge sentences' cores in the grammar; it collects one
(core, store) pair per sentence, in order, as a tuple of tuples (tuples
of semantic values and tuples are already supported; no nested feature
map is needed). For example:

    D[sents=((<?c>, ?s) + ?d)] -> S[core=<?c>, store=?s] "." D[sents=?d]
    D[sents=((<?c>, ?s))] -> S[core=<?c>, store=?s] "."

The after-parse function for a storage DRT grammar then, for a complete
discourse edge:

1. retrieves each sentence's readings with `retrieve(core, store)`,
   keeping the distinct ones;
2. forms every combination of one reading per sentence (the cartesian
   product, in sentence order; these are the book's threads d0, d1, ...);
3. merges each combination left to right, so earlier referents bind in
   later sentences;
4. resolves anaphora, and drops the combinations in which some pronoun
   has no candidate.

The exact feature names and rule shapes are the builder's call. Whether
the plain and storage DRT grammars share one after-parse entry point
(say, a function that looks for `sem` or `sents` on the edge) is also
the builder's call.

Tests beyond the discourse ones: `abstractOver` and `retrieve` on terms
containing boxes, merges and implications, with the placeholder ending up
bound by the right referent.

### Reading DRT terms directly

Book items 2 to 9 build DRSs by hand. A function that reads one DRT term
from a string, the counterpart of NLTK's `DrtExpression.fromstring`, is
needed for those tests.

## Constraints

* Code style per CLAUDE.md: 4-space indent, KDoc on public API,
  functional style, immutable data.
* All existing tests' assertions still pass; the MASC grammar and the
  chart are untouched. Test code may grow cases for the new nodes:
  `LambdaSharingTest.kt` has five reference implementations
  (`refPlaceBoxes`, `refSubstBoxes`, `refQshift`, `refShift`,
  `refQuantifierBinds`) written as exhaustive `when`s over `Lambda`,
  which stop compiling under option A until they gain the new cases, and
  its random-term generator should produce the new nodes so that the
  sharing checks cover them.
* Build and test with `devenv shell -- mvn -B test`.
* Terms stay immutable, with cached hashes and de Bruijn facts, and the
  sharing of unchanged subterms (see `LambdaSharingTest`) extends to the
  new nodes.

## Left to the builder

Marked above where they arise: how the declaration switches the reader;
the exact box notation inside `<...>` (close to NLTK's
`([x], [dog(x)])`, `+`, `->`, and negation); node names; printing
details; the order of candidates; `fol()` of candidate lists; whether
unused referents are ever dropped; the discourse rule's form; which
package resolution and `fol()` live in (`com.cbrew.logic`, beside
`CooperStorage.kt`, is the obvious place).

## Open questions for the user

None.
