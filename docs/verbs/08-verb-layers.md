# The entropy of a forest by verb layers

Where does the ambiguity of a treebank grammar sit with respect to the
verbs? This note takes a coarse cut suggested by the project's owner: the
top-layer verbs a tree has, the bottom-layer verbs, what lies outside any
verb, and what lies in between. It shows that the cut is a well-defined
quantity (a chain rule, exact, computed by four linear passes over the
packed forest), gives the code, and reports it on MASC, first with every
tree of a forest as likely as any other and then with the trees weighted by
the treebank's rule probabilities. It is task 4 of
[`COORDINATION.md`](COORDINATION.md), and the finer decomposition of
`go/explore/cmd/verbentropy` is its companion.

Code: `go/explore/frames/layers.go` (`VerbLayers`, `VerbNodes`), tested against
enumeration of every tree in `layers_test.go`; `go/explore/cmd/layers` runs it.

## The object

A **verb node** in a tree is a lexical verb phrase, `frames.LexicalVP`: a
node whose label is or ends in VP, with a verb-tagged daughter and no VP
daughter. That makes it a property of the rule that builds the node, so in
a forest it is a property of a hyperedge, and every tree of the forest has
a definite set of verb nodes. A **top** verb node has no verb node above
it; a **bottom** verb node has none below it; a lone verb node in its
clause is both.

Five things are settled in turn about a tree, each a function of the tree:

1. **Outside**: the skeleton, the tree outside its top verb nodes, with the
   top verb nodes as its leaves. It fixes where they are.
2. **Top**: each top verb node's expansion (its rule and the spans of its
   daughters), given the skeleton.
3. **Between**: everything from there down to the bottom verb nodes: which
   items are bottom, the verb nodes in between and their expansions, and
   the material beside them, given the above.
4. **Bottom**: each bottom verb node's expansion, given the above and given
   that nothing below it has a verb.
5. **Inside**: the verb-free trees under the bottom verb nodes' daughters.

By the chain rule the entropy of the distribution over trees is the sum of
the five conditional entropies, each at least zero:

    H(tree) = H(skeleton) + H(top | skeleton) + H(between | ...) + H(bottom | ...) + H(inside | ...)

So the cut is a well-defined mathematical object for any distribution over
the trees. What makes it *computable* without enumeration is
context-freeness: given an item of the forest, the tree under it is
independent of the tree around it. Then

* the probability of a skeleton is the product, over the hyperedges it
  uses, of each hyperedge's probability at its item, times, at each top
  verb item, the probability that the item expands by a verb rule at all
  (that it *is* a top verb node is the skeleton's information; *which* verb
  rule is stage 2). Stage 1 is a sum over items of outside(no verb above)
  × inside × the local surprisal, and stage 2 likewise with the surprisal
  of the verb rule given a verb rule. This needs the outside sums split by
  whether a verb node lies above, a top-down automaton with three states
  (no verb above; within a top verb node's binarization chain; below).
* given a bottom verb node and its expansion, the trees under its daughters
  are the verb-free trees under those items. Stage 5 is a sum over verb
  hyperedges of outside × verb-free inside of the tails × the entropy of
  the verb-free trees under each daughter; stage 4 is the entropy of the
  choice among the verb rules and chains at a bottom item, each weighted by
  the verb-free mass under its daughters. This needs the inside sums split
  by whether a verb node lies below: the "bit per item" of task 4.
* stage 3 is the remainder.

The fine decomposition of `entropy.Of` (Li and Eisner's sum over
items of occupancy × local entropy) attributes every local choice to a
top-down state. The layer cut coarsens it, but not by grouping those
states: "bottom" and "inside" are not properties of the path from the root,
and cannot be had from a top-down automaton at all. That is why task 4 asked
for a bottom-up bit. The only places the two accountings differ in what
they charge to whom are the two above: the *existence* of a top verb node
goes to the skeleton here, and the *absence* of verbs below a bottom one
goes to "between" (for a lone verb node, which was expanded at stage 2,
this is the whole of what "between" says about it).

**Weights.** Nothing above needs the trees to be equally likely. With a
weight on each rule, the same passes give the entropy of the distribution
that weights a tree by the product of its rules' weights; the only additions
are an expected-log-weight sum alongside each inside sum, so that the
entropy of the verb-free trees under a daughter is log of their mass less
their expected log weight, and the same for the total. With no weights the
total is log10 of the number of trees, as in the other measurements.

**Auxiliaries and coordination.** A VP with a VP daughter (an auxiliary's,
or a coordination or adjunction) is not a verb node. So *has [gone home]*
has one verb node, *gone home*, and the auxiliary phrase above it is
skeleton or "between" like any other phrase. *[ate] and [drank]* under one
VP are two verb nodes side by side.

**Choices left open.** The definition of a verb node is the one choice
that shapes the answer; a definition by head rules (a phrase whose head
daughter is a verb) wrongly makes every auxiliary phrase a verb node, and
was rejected. Whether an SQ or SINV that has the verb as a daughter (an
inverted copula) should be a verb node is left as it is: it is not.

## Results

300 held-out MASC sentences of 5 to 25 words, gold tags, the same sample as
`go/explore/cmd/verbentropy`. Every sentence parses. The forests have a mean of
10^18.26 trees.

```
S=SCRATCH   # tools/masc/treebank.py's output in $S/tb, tools/masc/verbframes.py's in $S/verbs
cd go && go run ./explore/cmd/layers -counts $S/tb/counts.tsv -annotated $S/tb/annotated.jsonl -lemmas $S/verbs/lemmas.tsv -n 300 [-pcfg] [-lexicon core]
```

### Every tree as likely as any other

| stage | share of all the entropy | median share per sentence | median digits per word |
|---|---|---|---|
| outside | 94.3% | 94.7% | 1.212 |
| top | 1.1% | 1.1% | 0.014 |
| between | 2.1% | 1.1% | 0.014 |
| bottom | 0.0% | 0.0% | 0.000 |
| inside | 2.5% | 2.4% | 0.030 |

| verb nodes per sentence | expected over the trees | in the sentence's own tree |
|---|---|---|
| all | 0.98 | 1.98 |
| top | 0.94 | 1.37 |
| bottom | 0.94 | 1.41 |

A third of the trees (33.2%) have no verb node at all. Under uniform
weighting the verbs hardly organise the trees: half the verbs are not heads
of a lexical verb phrase in a typical tree but daughters of something else
(the treebank has rules such as NP → NP VBD or S → NP VBZ NP, read off
mis-annotated sentences, and the count of trees does not care how rare they
are), and the ones that are have few dependents. This agrees with
`go/explore/cmd/verbentropy` on the same sample: 84% of the entropy outside any verb
plus 11% in top-layer verb phrases, and 93% of the non-verb words outside
every lexical verb phrase against 40% in the gold trees. (The 94% here
against 84% there: `verbentropy` counts the choices at every VP item at
depth 0 as the top-layer verbs', including VP → VP PP and the auxiliaries'
rules; here those are skeleton, and only the lexical rule and its daughters
are the verb node's.)

### Trees weighted by the treebank's rule probabilities

Each rule's weight is its count over its left-hand side's count, from
`counts.tsv` (the whole corpus, so not held out from the test sentences;
unsmoothed). The count of trees is unchanged, but the entropy of the
distribution is 1.60 decimal digits per sentence (median 0.11 per word):
the PCFG puts nearly all its mass on some 40 trees of the 10^18.

| stage | share of all the entropy | median share per sentence | median digits per word |
|---|---|---|---|
| outside | 41.1% | 43.2% | 0.039 |
| top | 23.9% | 22.1% | 0.025 |
| between | 16.8% | 2.2% | 0.002 |
| bottom | 4.9% | 0.3% | 0.000 |
| inside | 13.3% | 6.2% | 0.006 |

| verb nodes per sentence | expected over the trees | in the sentence's own tree |
|---|---|---|
| all | 1.96 | 1.98 |
| top | 1.35 | 1.37 |
| bottom | 1.40 | 1.41 |

Only 2.4% of the mass is on trees with no verb node, and the expected
numbers of verb nodes, top and bottom, match the gold trees to a few
hundredths. Now the verbs' own expansions carry 29% of the uncertainty (24%
top, 5% bottom), the material inside the bottom verbs' dependents 13%, the
skeleton 41%, and the middle layers 17%. The medians show where the middle
sits: "between" and "bottom" are near zero for the typical sentence (one
or two verbs, so nothing lies between top and bottom) and large in the
sentences with three or more verb nodes (78 of the 300), where they hold
the choice of which verb embeds which.

By the verb nodes in the sentence's own tree, the verbs' own expansions
(top + bottom) take a median 25% of the entropy with one verb node, 32%
with two, 31% with three and 21% with four or more.

### With the frame lexicon

Filtering the trees by the core-grain frame lexicon of `go/explore/cmd/framelex`
(lemmas seen 5 or more times in training) removes 0.03 of the 18.26 digits
of trees, and 0.05 of the 1.60 digits of PCFG entropy. Under the PCFG the
top verbs' share goes from 23.9% to 21.4% and the skeleton's from 41.1% to
42.8%: the lexicon takes a little from exactly the stage it addresses, and
nothing else moves.

## What it says

* The layer cut is exact and cheap. Under uniform weighting it says only
  that a treebank grammar's count of trees is dominated by trees that are
  not verb-structured at all, which the other measurements found too.
* Under the PCFG the picture is the one the question wanted: the
  uncertainty the grammar leaves about a sentence is 41% in how the clauses
  are assembled around the top verbs, 29% in what the verbs take, 13% in
  the insides of their dependents, and 17% in how verbs embed one another,
  the last concentrated in the multi-clause sentences.
* A PCFG's entropy over a forest, 1.6 digits, is a different order of
  magnitude from the count, 18 digits; which of the two is the right
  measure of "the ambiguity a reader faces" is the question
  [`../ambiguity.md`](../ambiguity.md) leaves open. Both are now available
  for every decomposition here.

## Not done

* Only the top-level cut. The same passes give the cut at each depth of
  embedding (top, second, ..., bottom) with more states; not needed yet.
* The PCFG is unsmoothed and includes the test documents in its counts.
  Task 6 in `COORDINATION.md` is the place for a held-out, smoothed
  version, and for weighting `go/explore/cmd/verbentropy`'s finer decomposition.
* No breakdown of the top verbs' 24% into kind, frame and modifiers; that
  is what `go/explore/cmd/verbentropy` does, and it would carry over to the PCFG
  weighting directly.
