# quadruplet

A chart parser for feature grammars whose rules carry lambda-calculus
semantics. Given a sentence, it finds every parse, packs them into a
chart, and gives every distinct reading as a logical form. It began as a
Kotlin reimplementation of NLTK's feature-grammar parser; there is now also
a Go port with a parallel parser, checked against the Kotlin one.

```bash
cd go
go run ./cmd/quadruplet -grammar ../src/test/resources/masc/masc.fcfg -start Top -pretty \
    "every architect who knows Dave found a house"
```

    every architect who knows Dave found a house
      1 readings, 1 trees, 109 complete and 323 partial edges, 1.186ms
      Top: ∀x1.((architect(x1) ∧ ∃x2.(Experiencer(x2, x1) ∧ Theme(x2, dave) ∧ know(x2))) → ∃x2.(house(x2) ∧ ∃x3.(Agent(x3, x1) ∧ Theme(x3, x2) ∧ find(x3))))

## What is here

| where | what |
|---|---|
| `src/main/kotlin/com/cbrew/unify` | feature structures and lambda terms, unification, beta reduction and simplification |
| `src/main/kotlin/com/cbrew/chart` | the chart parser, tree counting and enumeration |
| `src/main/kotlin/com/cbrew/fstruct`, `logic`, `src/main/antlr4` | the grammar and logic notations (ANTLR) |
| [`go/`](go/README.md) | the Go port: the same parser, a parallel version of it, and a command-line tool |
| `src/test/resources` | test grammars, from `tiny.cfg` to `sem2.fcfg` (NLTK's) |
| [`src/test/resources/masc`](src/test/resources/masc/README.md) | the MASC grammar v0 and its benchmark: 299 treebank sentences, 284 of which it parses, and a held-out sample of 299 more |
| `tools/masc` | the scripts that chose the MASC sample and generate the grammar's lexicon and correctness suite |
| [`tools/prolog`](tools/prolog/README.md) | CGELBank, UD and spaCy trees as Prolog programs, in the `--->` notation of `tools/masc/prolog/ptb.pl` |
| [`docs/prolog-framework.md`](docs/prolog-framework.md) | the design for a common Prolog framework over the treebank traditions: anchoring, modules, bridge rules, shared grammars with per-sentence controls |
| [`docs/semantics.md`](docs/semantics.md) | what kind of semantics the grammars have, and where the ideas come from |
| [`docs/fast-parser.md`](docs/fast-parser.md) | how the fast context-free parser (`go/cfg`) works, and what a hyperedge is |
| [`docs/flat-semantics.md`](docs/flat-semantics.md) | meanings read off parse trees by folds: heads, learned function tags, flat neo-Davidsonian forms |
| [`docs/ambiguity.md`](docs/ambiguity.md) | why a treebank grammar is so ambiguous, what sets the attested tree apart, and ideas to follow up |
| [`docs/verbs/`](docs/verbs/README.md) | what the empty elements and function tags a context-free backbone drops do to MASC's verbs; complements against modifiers |

## Building and testing

```bash
mvn test                                  # Kotlin: build and run the tests
cd go && go vet ./... && go test ./...    # Go
```

The Go tests compare against golden files the Kotlin implementation
writes (`go/testdata/golden`), for the logic, feature terms, unification,
grammars and parses. So the two implementations must agree, reading for
reading. To regenerate the golden files after changing the Kotlin code or
their inputs:

```bash
mvn test -Dtest=GoldenDumpTest -Dgolden.dir=$PWD/go/testdata/golden
```

The MASC grammar also has a correctness suite, `readings.txt`, which lists
every reading of 61 sentences, checked by hand. Both implementations test
against it.

## Grammars

Grammars are usually written in the integrated notation
(`src/main/antlr4/com/cbrew/fstruct/notation/FeatParser.g4`). Categories
carry features in brackets, `?x` variables express reentrancy, and
semantics goes in `<...>` in the logic language:

```
S[sem=<\F.?np(\x.?vp(x, F))>] -> NP[case=nom, agr=?a, sem=<?np>] VP[vform=fin, agr=?a, sem=<?vp>]
VP[sem=<?v(?np)>] -> V[subcat=tr, sem=<?v>] NP[case=acc, sem=<?np>]
"every": Det[agr=3sg, sem=<\P Q.all x.(P(x) -> Q(x))>]
NP[sem=<\P.P(mary_jane)>] -> "Mary" "Jane"
```

* Brackets are required on every category, even `Conj[]`. `+f` and `-f`
  abbreviate `f=true` and `f=false`.
* Alternatives separated by `|` are separate rules. A quoted word among
  categories becomes a category of its own, so `VP -> VP "and" VP` keeps
  its "and". A rule of words only is a lexical entry for the phrase.
* In the logic, `\x y.` is λ, `exists x.` and `all x.` are the
  quantifiers, and `&`, `|`, `->`, `<->`, `=`, `!=` and `-` (not) are the
  connectives. Application is `f(x, y)`, and `true` is the unit of
  conjunction. Single lower-case letters, optionally followed by digits,
  are individual variables, and single capitals are predicate variables.
  `?x` is a variable that unification fills with another category's
  semantics.

The older `FeatureNotation` (`FeatureTerms.g4`, used by `demo.fcfg`) is
also supported. Multiple context-free grammar (MCFG) rules, written with
`=>`, parse, but the chart parser does not use them.

The semantics of the MASC grammar is an extensional Montague semantics with
quantificational event semantics: noun phrases are generalised
quantifiers, and verbs quantify over events whose participants have
thematic roles. [`docs/semantics.md`](docs/semantics.md) explains what
that means, and what the grammar leaves out.

### Scope

Feature values may be atoms, `?x` variables, lists and tuples, or
semantic terms in `<...>`. **Nested feature maps (a feature whose value is
itself a feature map) are not supported, and there are no current plans to
support them.**

* The `FeatureNotation` notation (`FeatureTerms.g4`) cannot express them.
* The integrated notation (`FeatParser.g4`) accepts them syntactically, but
  no grammar or test uses them and they are not tested.
* The unifier happens to handle them structurally, but nothing relies on
  that and it may change.

Unification is term unification with named variables: reentrancy is written
by repeating a variable, e.g. `S[num=?n] -> Np[num=?n] Vp[num=?n]`. Where a
grammar needs something like a nested structure, for instance to record a
gap, it uses flat features or a category of its own (the MASC grammar's
`VPgap`).

## Performance

On 4 cores, the whole MASC sample (299 sentences, 161,954 edges) parses in
0.90 s in Kotlin, 1.44 s in Go with the sequential agenda parser, and
0.58 s with the Go parallel parser at `GOGC=400`. See
[`go/README.md`](go/README.md) for more, including why the Go garbage
collector is the limit.
