# quadruplet

Feature-based chart parser written in Kotlin.

Modeled on NLTK's FeatureParser: grammars are feature grammars whose rules
carry lambda-calculus semantics, and the parser packs every parse of a
sentence into a chart and reads each distinct meaning off it as a logical
form.

## What is here

| where | what |
|---|---|
| `src/main/kotlin/com/cbrew/unify` | feature structures and lambda terms, unification, beta reduction and simplification |
| `src/main/kotlin/com/cbrew/chart` | the chart parser, tree counting and enumeration |
| `src/main/kotlin/com/cbrew/fstruct`, `logic` | the grammar and logic notations, over parsers ANTLR generates |
| `src/main/antlr4` | the ANTLR grammars for those notations |
| `src/test/resources` | test grammars, from `tiny.cfg` to `sem2.fcfg` (NLTK's) and the MASC grammar |

## Building and testing

The build is Maven, on Kotlin 2.4.21 and JDK 25. `pom.xml` pins Kotlin
and the libraries (ANTLR 4.13.2, JUnit 4, Hamcrest 3, SLF4J and Logback);
the JDK and Maven that run it are pinned by devenv, in `devenv.nix` and
`devenv.lock`:

```bash
devenv shell        # a shell with JDK 25 and Maven on the path
devenv test         # mvn -B test
```

Inside that shell, or with a JDK 25 and Maven of your own:

```bash
mvn test            # build and run the tests
mvn package         # also target/parser-1.0-SNAPSHOT-jar-with-dependencies.jar
mvn antlr4:antlr4   # regenerate the ANTLR parsers alone
```

The ANTLR grammars generate Java under `target/generated-sources`, which
the Kotlin code refers to, so the Kotlin compiler runs first over both
and javac follows. Both compile for Java 25 (`java.version` in the pom).
CI (`.github/workflows/package.yml`) runs `mvn package` on Temurin 25.

## Scope

Feature values may be atoms, `?x` variables, lists and tuples, or
semantic terms in `<...>`. **Nested feature maps (a feature whose value is
itself a feature map) are not supported, and there are no current plans to
support them.**

* The `FeatureNotation` notation (`FeatureTerms.g4`) cannot express them.
* The `IntegratedParser` notation (`FeatParser.g4`) accepts them
  syntactically, but no grammar or test uses them and they are not tested.
* The unifier happens to handle them structurally, but nothing relies on
  that and it may change.

Unification is not term unification in the strict sense: feature maps are
open records, so a feature that only one side has is copied into the
result, where two terms of different arity would fail. The mechanism is
Prolog's rather than a graph unifier's: named `?x` variables and a
substitution threaded through the computation, with an occurs check.
Reentrancy is written by repeating a variable, e.g.
`S[num=?n] -> Np[num=?n] Vp[num=?n]`; two equal values are never shared.
Lists are fixed-length and unify element by element. Tuples, and semantic
terms in `<...>`, unify only if they are equal, though a `?x` inside
`<...>` binds to a whole term. A variable in feature position ranges over
feature structures and one in semantic position over terms, and a binding
that crosses the two fails.

## Licence

quadruplet is free software: you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or (at your
option) any later version. See [LICENSE](LICENSE). Versions before
October 2026 were released under the Apache License 2.0.

The grammars in `src/test/resources` adapted from NLTK's grammars
(`sem2.fcfg`, `storage.fcfg`, `drt.fcfg`, `drt-storage.fcfg`) keep their
original authors' credits in their headers.
