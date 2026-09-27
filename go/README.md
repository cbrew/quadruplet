# Go prototype of the chart parser core

An experiment to see what a Go port of `com.cbrew.chart.Chart` would look
like, and whether goroutines buy anything. It is **not** a full port: the
categories are ground (no variables), so "unification" is identity on
interned category IDs. It parses with `TreeAsFeatureGrammar` and reproduces
the Kotlin edge counts and tree counts exactly (see `chart_test.go`).

```bash
cd go
go test -race ./...                 # strategies agree with Kotlin
go run . -n 170                     # all four strategies
go run . -n 60 -work 3000 -mode wave-par
```

`-work N` burns N LCG steps per unification to simulate the cost of real
feature-structure unification (`-work 3000` ≈ 3.4 µs, close to what the
Kotlin unifier costs per call).

## Strategies

| mode | what it does |
|---|---|
| `agenda` | the Kotlin algorithm: agenda loop, completes indexed by start, partials by end |
| `agenda-par` | same, but each batch of fundamental-rule applications is fanned out over goroutines |
| `wave` | builds cells (i,j) in order of span length; each cell reads only shorter cells |
| `wave-par` | `wave` with one goroutine per cell and a barrier between span lengths |

The wavefront needs no locks: cells from earlier levels are never written
again. The left-corner closure stays local to a cell because `spawn`
returns exactly the rules whose first needed category unifies with the
left corner. That holds for `FeatureGrammar` and `TreeAsFeatureGrammar`.

## Design points

* `Edge` is a small comparable struct, so it keys Go maps directly. That
  needs hash-consing: categories are interned IDs, and `needed` lists are
  interned cons cells. So, as with the Kotlin data class, a partial is
  identified by (LHS, start, end, remaining needed), and dotted rules that
  share a suffix collapse into one edge.
* Tree counting is memoised over the packed chart and uses `math/big`.

## Measurements (4 cores)

Parse time only, TreeAsFeatureGrammar:

| n=170 | time |
|---|---|
| Kotlin `Chart.parse`, originally | 7–10 s |
| Kotlin, with shared structure and cached hashes | 2.1 s |
| Go `agenda` | 0.63 s |
| Go `wave` | 0.24 s |
| Go `wave-par` | 0.12 s |

n=60 with simulated unification cost:

| cost / unify | agenda | agenda-par | wave | wave-par |
|---|---|---|---|---|
| ~0 | 32 ms | 109 ms | 11.5 ms | 9 ms |
| ~1 µs | 288 ms | 355 ms | 264 ms | 163 ms |
| ~3.4 µs | 851 ms | 857 ms | 771 ms | 336 ms |

Fine-grained parallelism (`agenda-par`) never pays: each batch is too
small. The wavefront gives ~2.3× on 4 cores once unification is expensive.
Scaling is limited by the long-span levels, which have few cells. For
ordinary sentence lengths, parsing different sentences in parallel is the
simpler win.

## What a full port would need

The Kotlin unifier is purely functional. Feature structures are immutable
data classes, bindings are threaded through as immutable maps, and failure
is just `null`. A Go port has two options:

1. Keep that discipline: persistent bindings and an interned term store.
   Chart edges then stay safe to share across goroutines. The intern table
   becomes the shared state, so it needs sharding or `sync.Map`.
2. Use a destructive unifier with a trail (undo on failure). This is
   faster, but every edge category must be copied before unification or
   protected by a quasi-destructive scheme, because packed-chart edges are
   shared by many parents.
