# An active chart parser in pure Prolog

`agenda.pl` has the architecture of the Kotlin parser
(`src/main/kotlin/com/cbrew/chart/Chart.kt`). Edges come off an agenda; an
edge new to the chart is added to it, and then a complete edge `spawn`s the
rules it is the left corner of and is paired with the partial edges that need
it (`pairwithpartials`), and a partial edge is paired with the complete edges
it needs (`pairwithcompletes`). The `fundamental` rule makes the new edges,
each recording the pair it came from, so the chart is a packed forest.

It is pure: no assert, and none of cut, if-then-else or negation in its own
clauses (library code is used as it is). The chart and agenda are terms passed
along, and choices are made by clause selection.

Categories are Prolog terms, so features and semantics come from Prolog's
unification (with the occurs check):

```prolog
?- use_module(tools/agenda/agenda).
?- G = [rule(s(S), [np(X), vp(X^S)]), rule(vp(X^S), [tv(Y^X^S), np(Y)]),
        lex(john, np(john)), lex(mary, np(mary)),
        lex(sees, tv(Y^X^see(X, Y)))],
   parse(G, [john, sees, mary], s(Sem), Tree).
Sem = see(john, mary), ...
```

| predicate | what |
|---|---|
| `parse(+G, +Words, ?Cat, -Tree)` | each solution and tree, on backtracking |
| `chart(+G, +Words, -Chart)` | the finished chart |
| `solutions(+Chart, +N, -Edges)` | the complete edges over all `N` words |
| `gettrees(+Chart, +Edge, -Tree)` | an edge's trees: `node(Cat, Children)`, `leaf(Cat, Word)` |
| `count_trees(+Chart, +Edge, -Count)` | how many, memoized over the forest |

A grammar is a list of `rule(Cat, [Cat|Cats])` and `lex(Word, Cat)` terms.
Edges are kept ground, their variables numbered, so that variants are the same
edge and edges are renamed apart before they combine.

Not carried over from Kotlin: multi-word lexical entries, the agenda's
priority order (a list does; the order does not change the finished chart),
and indexing rules by their left corner.

## Tests

```bash
cd tools/agenda && swipl -g run_tests -t halt test_agenda.pl
```

The tree counts for 1 to 20 words under Kotlin's `TreeAsFeatureGrammar`
match `go/testdata/golden/treeas.golden`. The other tests cover the textbook
sentence of `docs/fast-parser.md`, semantics and agreement by unification, left
recursion, and an edge that is both lexical and built by a rule.
