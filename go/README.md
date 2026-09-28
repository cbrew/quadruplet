# quadruplet in Go

A Go port of quadruplet's feature-based chart parser: feature structures
with lambda-calculus semantics, their unification, the grammar notations,
and the chart parser. It reproduces the Kotlin implementation's output
exactly on the test grammars (see [Testing](#testing)).

```bash
cd go
go test ./...
go run ./cmd/quadruplet -grammar ../src/test/resources/sem2.fcfg -trees 1 \
    "every boy chases a dog with Fido"
```

## Packages

| package | contents |
|---|---|
| `term` | feature structures and lambda terms, the simplifying constructors, beta reduction, bindings, substitution and unification, and `Pretty` for reading terms |
| `grammar` | rules, grammars and lexicons |
| `notation` | parsers for the logic language, the FeatureNotation style (`demo.fcfg`) and the IntegratedParser style (`patio.fcfg`, `sem2.fcfg`) |
| `chart` | the chart parser (sequential agenda, or parallel wavefront), tree counting and enumeration, `FeatureGrammar`, and the `TreeGrammar` benchmark grammar |
| `cfg` | a fast parser for context-free grammars, whose categories are plain symbols ([below](#context-free-grammars)) |
| `cmd/quadruplet` | command-line parser (`-workers`, `-trees`) |
| `cmd/forests` | parses a whole corpus with a context-free grammar, a line per sentence ([below](#all-of-masc)) |
| `cmd/prototype` | the earlier prototype comparing agenda and wavefront parsing, with and without goroutines ([below](#prototype)) |

## Design

The design follows the Kotlin version after its unifier work:

* **Terms are immutable.** Each constructor computes, once, a 64-bit hash,
  whether the term contains unification variables, and for lambda terms the
  depth of their free de Bruijn variables and whether a Box occurs.
* **Structure is shared.** Substitution, renaming and beta reduction use
  those facts to return unchanged subterms as they are; only the paths to
  what changes are rebuilt. Beta reduction is a single pass.
* **Bindings are an immutable association list**, so extending them shares
  the old list and a failed unification is simply dropped.
* **Small collections are slices**: a feature map's features and an
  And/Or's elements are kept in insertion order with linear lookup.
* **Chart edges are interned**, so each distinct edge is one pointer and
  chart membership and predecessor lookup are by pointer.

Because nothing is mutated, terms and edges can be shared between
goroutines without locks.

### Parallel parsing

`Chart.Parse` is the Kotlin agenda algorithm. `Chart.ParseParallel` builds
the same chart as a CYK-style table of cells, cell (i,j) holding the edges
over words i..j. A cell is made from shorter cells only: lexical entries,
partial edges over i..k extended by complete edges over k..j, and then,
within the cell, the rules its complete edges spawn.

Each cell gets a goroutine that waits on the done channels of its left and
lower neighbours, (i,j-1) and (i+1,j); by induction, once those are finished
so is every cell it reads. A finished cell closes its own channel, waking
the two cells waiting on it. Unlike building one span length at a time with
a barrier between lengths, cheap parts of the table run ahead while
expensive cells are still working. Cells need no locks: edges over different
spans are never equal, so each cell interns its own, and the cells sharing a
start position's spawned edges are ordered by their left-neighbour
dependencies.

Most of the work falls in the few widest cells, so each cell also spreads
its fundamental-rule applications over up to `workers` goroutines, handing
out chunks through an atomic counter (cheaper than a channel for items this
small). Results are added in the order a sequential build would add them,
so the chart, down to the order of its edges, does not depend on
scheduling.

Keeping the rules spawned at i to the cell that spawned them relies on
`Spawn` returning every rule whose first category unifies with the edge, as
`FeatureGrammar` and `TreeGrammar` do.

## Context-free grammars

A grammar whose categories have no features to unify is context-free, and
package `cfg` parses it without the feature machinery. The method is that
of the LCFRS parser in
[cbrew/odd_one_out](https://github.com/cbrew/odd_one_out) (`internal/lcfrs`),
specialised to context-free rules
([`docs/fast-parser.md`](../docs/fast-parser.md) explains it at length):

* symbols are integers, and rules are binarized
  by pairing up the daughters that occur together most often, as in
  BitPar, with the pairs shared between rules (`{DT {JJ NN}}`);
* a bottom-up pass (CKY) records which symbols are derivable over which
  spans, in bit vectors over positions, so that one AND tests all the
  split points of a step, as in BitPar;
* a top-down pass from the start symbol keeps only the items on a
  derivation of the whole input, and records every way of building each
  as a hyperedge. No dead-end item is ever stored.

The forest's tree counts are those of the grammar's own rules;
`Forest.Trees` enumerates trees with the auxiliary symbols spliced out, and
`Forest.Contains` checks a given tree without enumerating. Its tests compare
it with package `chart` on random grammars (the same items, reachable from a
parse, and the same tree counts) and with `treeas.golden`.

`cfg.FromGrammar` compiles a grammar in either notation, provided its
categories are ground and no two different ones unify. On the command line,
`-fast -start SYMBOL` uses it. On a treebank grammar read off all of MASC
(21,273 rules; 24,042 binary and 6,744 unary steps), one core:

| tokens | package `chart` | package `cfg` | trees |
|---|---|---|---|
| 5 | 0.9 s | 1.5 ms | 675,831 |
| 10 | 7.6 s | 7 ms | 6.8 × 10¹² |
| 15 | 49 s | 33 ms | 3.6 × 10²³ |
| 30 | | 0.27 s | 2.2 × 10⁵¹ |

[`docs/fast-parser.md`](../docs/fast-parser.md) §6 compares it with BitPar
on sentences of up to 80 words.

### All of MASC

`cmd/forests` parses every sentence of a corpus, shortest first, one at a
time, and writes a tab-separated line for each: its words, the forest's
items and hyperedges, what the first pass found derivable, the time of each
pass, the heap in use, whether the corpus's own tree is in the forest, and
the number of trees. For MASC,

```bash
tools/masc/forests.sh MASC_DATA_DIR OUT_DIR
```

reads the grammar off the trees (`tools/masc/treebank.py`), builds the
command, and runs it over all 34,582 sentences into `OUT_DIR/forests.tsv`,
with progress in `OUT_DIR/forests.log`. A 139-word sentence took 5.5
minutes, 50 s to parse and the rest to count its trees exactly, and 11.8 GB
at its peak (its forest, of 422 million hyperedges, took 6 GB). From that,
the whole run should take two to three hours on one core, and the longest
sentence, of 174 words, 22 to 25 GB, so it wants a 32 GB machine.
`-count=false` saves the time and memory of counting, and `-maxwords 60`
makes a quick first run. Interrupted, it carries on where it stopped when
run again.

## Differences from the Kotlin version

* **Parsers are strict.** They are hand-written recursive descent following
  the ANTLR grammars token for token, but they report any syntax error or
  leftover input where ANTLR would recover or stop early.
* **Multiple CFG rules (`=>`) are kept** in the IntegratedParser notation;
  the Kotlin `IntegratedVisitor` drops them.
* **`NormalOrderReduce` handles negation**; the Kotlin `betaReduce` throws on
  `Not`.
* **Printing is total**: `Box` prints as `☐` (Kotlin prints an object
  hash) and a feature map without `cat` prints as `[k=v, ...]` (Kotlin
  recurses forever).
* **Agenda ties are first-come first-served.** The Kotlin agenda is a
  `PriorityQueue` on start and end whose ties depend on its heap layout.
  The finished chart is the same.

## Testing

The Go tests compare against golden files written by the Kotlin
implementation, in `testdata/golden`:

| file | contents |
|---|---|
| `logic.golden` | 136 logic expressions: parsed, and normalized |
| `fs.golden` | feature terms in the FeatureNotation style |
| `unify.golden` | unification of pairs of feature terms |
| `grammar.golden` | the rules and lexicons of the six test grammars, raw and normalized |
| `parse.golden` | 31 sentences over the six grammars: edge counts, tree counts, readings, and trees |
| `treeas.golden` | `TreeGrammar` edge and tree counts for 1 to 20 words |
| `masc.golden` | the MASC benchmark ([`src/test/resources/masc`](../src/test/resources/masc)): chart sizes and readings for 299 treebank sentences, printed by `term.Pretty` |

To regenerate them after changing the inputs (`*.in`) or the Kotlin code:

```bash
mvn test -Dtest=GoldenDumpTest -Dgolden.dir=$PWD/go/testdata/golden
```

There are also property tests comparing the sharing versions of `Shift`,
`QShift`, `PlaceBoxes`, `SubstBoxes`, quantifier binding and one-pass beta
reduction with full-traversal reference versions on random terms, and unit
tests for bindings, equality, hashing and normalization.

## Performance

On 4 cores, best of three (`go test ./chart -bench .`):

| | Kotlin | Go, agenda | Go, 4 workers | Go, 4 workers, `GOGC=400` |
|---|---|---|---|---|
| sem2, 12 PPs (40 words, 4,096 readings) | 74–82 ms | 99 ms | 54 ms | 36 ms |
| sem2, 14 PPs (46 words, 16,384 readings) | 0.30–0.44 s | 0.43 s | 0.22 s | 0.17 s |
| `TreeGrammar`, 170 words | ~2.1 s | 0.97 s | 0.59 s | 0.35 s |
| MASC sample, 299 sentences | 0.90 s | 1.44 s | 0.83 s | 0.58 s |

Sequentially, Go and the JVM are close on sem2: the chart keeps every term
alive, so each Go garbage collection re-marks a large, pointer-heavy heap,
where the JVM's generational collector does less work. The collector also
competes with parsing for cores, which is why a higher `GOGC` (less frequent
collection, more memory) helps the parallel parser most.

## Prototype

`cmd/prototype` is the earlier experiment that motivated the port. It
parses with `TreeGrammar`-style ground categories interned to integers, and
compares four strategies:

| mode | what it does |
|---|---|
| `agenda` | the Kotlin algorithm |
| `agenda-par` | each batch of fundamental-rule applications fanned out over goroutines |
| `wave` | builds cells (i,j) in order of span length; each cell reads only shorter cells |
| `wave-par` | `wave` with one goroutine per cell and a barrier between span lengths |

Fine-grained parallelism never paid; the wavefront gave about 2.3× on 4
cores once unification was expensive. The wavefront needs no locks, because
cells of earlier span lengths are never written again, and it relies on
`Spawn` using the same test as the fundamental rule, which `FeatureGrammar`
does.

```bash
go run ./cmd/prototype -n 60 -work 3000 -mode wave-par
```
