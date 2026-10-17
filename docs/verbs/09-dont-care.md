# The "don't care" share: how much of the ambiguity the meaning cannot see

Task 5 of [`COORDINATION.md`](COORDINATION.md). A treebank grammar's forest
has 10^18 trees for a 15-word sentence; [`../ambiguity.md`](../ambiguity.md)
argues that many of them are distinctions without a difference in meaning.
This note measures that: how much of the entropy of the trees survives in
the entropy of their meanings, where a tree's meaning is its flat semantics
(`interp.Flat`: each content word says itself of its referent, and each
daughter of a phrase stands in a relation to the phrase's referent named by
its marker, its function tags, or its configuration).

Two measures, one exact and one local:

* **Exact**, by enumeration, for forests small enough: the entropy of the
  trees less the entropy of the distribution over meanings they induce,
  H(tree) − H(meaning) = H(tree | meaning). This is the true "don't care"
  share. It is possible only for forests of a few hundred thousand trees:
  sentences of 3 to 7 words here.
* **Local**, for any forest: at each item of the forest, the hyperedges
  that build it are grouped by what the phrase would contribute to the
  meaning (the head daughter's position and category, each other
  daughter's relation class, the spans the top step of binarization gives
  the daughters), and `entropy.Of`'s chain rule splits the choice
  at the item into the choice of a group (visible) and the choice of a
  rule within it (don't care). The remaining span choices, at the
  auxiliary items, are reported apart and taken to be visible, since they
  move words between relations.

Code: `go/explore/cmd/dontcare`. The relation classes follow `interp.Flat`'s
`relation`: a marker-headed daughter (PP, WHPP, SBAR with a complementizer)
is named by its marker; an NP under a VP is `obj`; a clause is `comp`; a
noun or NML under a noun phrase is `nn`; QP and CD `num`; possessives
`poss`; anything else `mod`; a determiner or particle is an atom on the
phrase's referent; a conjunction names the relation of what follows it. An
MD or verb heading a phrase with a VP daughter is an auxiliary, and the
phrase is about the VP's event.

## Results

Held-out MASC sentences, gold tags, the grammar as read off the treebank.
Decimal digits; shares of all the sentences' entropy.

### 300 sentences of 5 to 25 words

Every tree as likely as any other (mean 18.26 digits of trees):

| where | meaning | don't care | spans | total |
|---|---|---|---|---|
| outside any verb | 10.06 (55%) | 2.91 (16%) | 3.03 (17%) | 16.01 (88%) |
| verb phrases' own rules | 1.56 (9%) | 0.14 (1%) | 0.45 (2%) | 2.16 (12%) |
| inside verbs' dependents | 0.06 (0%) | 0.02 (0%) | 0.02 (0%) | 0.10 (1%) |
| all | 11.69 (64%) | 3.07 (17%) | 3.50 (19%) | 18.26 |

Weighted by the treebank's rule probabilities (mean 1.60 digits of
entropy):

| where | meaning | don't care | spans | total |
|---|---|---|---|---|
| outside any verb | 0.75 (47%) | 0.13 (8%) | 0.07 (4%) | 0.94 (59%) |
| verb phrases' own rules | 0.53 (33%) | 0.08 (5%) | 0.03 (2%) | 0.64 (40%) |
| inside verbs' dependents | 0.01 (1%) | 0.00 (0%) | 0.00 (0%) | 0.01 (1%) |
| all | 1.28 (80%) | 0.21 (13%) | 0.10 (7%) | 1.60 |

### The exact measure: 200 sentences of 3 to 7 words

83 of them have forests of at most 300,000 trees, and those were
enumerated. Means per sentence:

| | log10 trees | log10 meanings | H(tree) | H(meaning) | don't care, exact | don't care, local |
|---|---|---|---|---|---|---|
| uniform | 3.96 | 1.85 | 3.96 | 1.41 | 2.56 (65%) | 0.50 (13%) |
| PCFG | 3.96 | 1.85 | 0.36 | 0.18 | 0.18 (50%) | 0.03 (7%) |

On all 200 short sentences the local split is 74% meaning, 15% don't care,
11% spans under uniform weighting, and 88 / 10 / 2 under the PCFG.

## What it says

* **Most of the trees mean the same as some other tree.** A short
  sentence's forest has 10^3.96 trees and 10^1.85 distinct meanings: one
  tree in 130 brings a meaning of its own. And the meanings are not
  equally shared out: the entropy of the meaning is 1.41 digits, so the
  uniform trees put most of their mass on a few meanings.
* **The true "don't care" share is about half to two thirds** of the
  entropy: 65% under uniform trees, 50% under the PCFG, for short
  sentences. Under the PCFG the meaning of a short sentence is nearly
  settled, 0.18 digits, about one and a half meanings' worth.
* **The local approximation misses most of it**: 13% and 7% against 65%
  and 50%. The reason is that the equivalences are between trees that
  differ at several items at once, not between two hyperedges of one item:
  NP → DT JJ NN and NP → DT (NP JJ NN), a modifier attached to any of the
  nested phrases with the same head, a unary chain against a flat rule, or
  two orders of adjunction. These are exactly the distinctions
  [`../ambiguity.md`](../ambiguity.md) suspected of making no difference.
  A per-item grouping sees only whether two rules at one item make the
  same relations. So the local figure is a lower bound, and a loose one.
* **Where the local measure does see it**: almost all outside the verbs
  under uniform trees (16 of the 17 points), since that is where the
  entropy is; under the PCFG 8 points outside and 5 in the verb phrases'
  own rules. The verb phrases' choices are the more meaning-visible: 9 of
  their 12 points uniform, 33 of 40 under the PCFG, against 55 of 88 and
  47 of 59 outside.

## Caveats

* The local grouping reads the relation off the daughter's category, not
  its head word, and ignores function tags (which the parser's trees get
  from a learned table); it says nothing of the referents, which are head
  words the item does not fix. The exact measure uses `interp.Flat` as it
  is, without the function table, so its relations are by marker and
  configuration only.
* The exact measure covers sentences of 3 to 7 words only, chosen for
  their forest size, so they are the least ambiguous short sentences.
* The PCFG is unsmoothed and its counts include the test documents, as in
  [`08-verb-layers.md`](08-verb-layers.md).

## Not done

* An exact "don't care" for long sentences. It needs the number of
  distinct meanings of a forest without enumeration. The flat meaning is
  a labelled head-dependency structure, so a forest split by head word
  (items as symbol, span and head), read as a dependency forest, might
  count them: many constituency derivations map to one dependency tree,
  and it is that many-to-one map that the local grouping cannot see.
* Whether the meaning-visible part of the verb phrases' choices is what a
  frame lexicon or the lexically conditioned weights of task 7 remove.
