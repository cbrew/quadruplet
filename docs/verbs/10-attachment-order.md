# Order of attachment within a head's projection

The proposal: treat as one class all the trees that differ only in the
order in which a verb's dependents attach within its projection. Examples
are [VP [VP saw her] [PP on Tuesday]] against [VP saw her [PP on Tuesday]],
two orders of adjunction, and a modifier attached to any layer of one verb's
chain. Scope-taking modifiers are exempt (*knocked twice intentionally*
against *knocked intentionally twice*). The words are fixed, so a class keeps
a projection's top category and its dependents in surface order, each with
its own class. Report 09 found that 65% of short sentences' entropy does not
change their flat meaning. This asks how much of that the order of
attachment is, before the parser is changed to count classes directly.

Code: `go/quotient` (`Key`, with options for verbs, auxiliaries, nouns, the
scope exemption, and forgetting phrase labels); `go/cmd/quotient`, which
enumerates forests.

## Definitions

* **Verb projection.** The base is a lexical verb phrase
  (`frames.LexicalVP`). A layer is a verb phrase with exactly one verb phrase
  daughter and no conjunction: adjunction, and with `Aux` auxiliaries.
  Coordination is not a layer.
* **Noun projection**, for comparison. The base is a noun phrase with no
  noun phrase daughter; a layer is a noun phrase with exactly one.
* **Scope.** A layer or base with a daughter that is an adverb or adverb
  phrase containing a scope word (`quotient.Scope`) closes a segment. It and
  what is below it stay one dependent of the segment above. The scope words
  are negation, frequency and quantificational adverbs, *again*, focus
  particles, and a few subject- and speaker-oriented adverbs.

`go/quotient/quotient_test.go` checks the equivalences on hand-made trees.

## Results

The same sample as the exact measure of report 09: held-out sentences of 3
to 7 words with gold tags, 83 of them with forests of at most 300,000 trees,
all enumerated. Means per sentence, in decimal digits.

Trees: log10 3.96. Entropy of the trees: 3.96 uniform, 0.36 weighted by rule
probabilities. Entropy of the flat meanings: 1.41 and 0.18. So the
don't-care entropy is 2.56 and 0.18.

| quotient | log10 classes | H(class), uniform | H(class), weighted | don't care accounted for, uniform | weighted | H(meaning given class), uniform | weighted |
|---|---|---|---|---|---|---|---|
| verbs | 3.95 | 3.95 | 0.36 | 1% | 0% | 0.000 | 0.000 |
| verbs, no scope exemption | 3.95 | 3.95 | 0.36 | 1% | 0% | 0.000 | 0.000 |
| verbs and auxiliaries | 3.95 | 3.95 | 0.36 | 1% | 0% | 0.000 | 0.000 |
| nouns | 3.90 | 3.87 | 0.35 | 3% | 6% | 0.031 | 0.010 |
| verbs, auxiliaries and nouns | 3.89 | 3.86 | 0.35 | 4% | 6% | 0.031 | 0.010 |
| phrase labels forgotten | 1.85 | 1.38 | 0.19 | 101% | 95% | 0.909 | 0.050 |
| phrase labels as counts of S, NP, PP | 3.48 | 3.33 | 0.26 | 25% | 57% | 0.179 | 0.009 |
| counts, unary chains collapsed | 3.19 | 3.00 | 0.24 | 38% | 68% | 0.322 | 0.017 |

* **The verb quotient is sound but, here, small.** It never merges trees of
  different meaning (H(meaning given class) is 0). It accounts for 1% of the
  don't-care entropy in these sentences, and the scope exemption makes no
  difference to that.
* **The noun quotient is larger and not quite sound.** It merges a few
  trees of different flat meaning, because the flat semantics names some
  relations by configuration.
* **Nearly all the don't-care is the choice of phrase labels.** Forgetting
  the labels, and keeping the bracketing and the words' tags, leaves about
  as many classes as there are meanings (10^1.85 each). They are different
  classes, though: labels decide relation names, so H(meaning given class)
  is 0.9.

Printing the trees of one meaning (`quotient -show N`) shows why:

    twitter/tweets1#338: Chillen in west; 21 meanings, the largest with 660 trees
      (ADJPph (ADJPph (VBG Chillen)) (ADJPph (ADJPph (IN in)) (ADVPph (NN west))))
      (ADJPph (ADJPph (VBG Chillen)) (NPph (ADJPph (IN in)) (NMLph (NN west))))
      (ADJPph (ADJPph (VBG Chillen)) (NPph (ADVPph (IN in)) (NN west)))
      ...

The grammar lets almost any word or short span project to almost any phrase
category, because the treebank has unary phrases over single words. The
flat semantics mostly relates the words the same way whatever the label.

## Counts instead of labels

A suggestion from the project's owner: write a phrase's label categorially,
as counts of a few basic types (S, NP, PP), with no slashes and no order.
This is the count invariant of categorial grammar. Function application
adds counts, (X − Y) + Y = X, so a mother's counts are the sum of its
daughters'. A modifier X/X counts nothing, so every modifier label
(ADJP, ADVP, PRN, ...) is the same zero. `quotient.CountLabel` maps a label
by its category alone:
* clauses are S;
* anything ending in VP, SxVP included, is S − NP;
* noun phrases are NP, prepositional phrases PP;
* everything else is 0.

The two rows above that use it show:
* **Counts alone** account for 25% of the don't-care entropy (57% weighted),
  merging few meanings (H(meaning given class) 0.18, weighted 0.009).
* **Collapsing unary chains too**, so that a word carries the counts of the
  top of its single-word projection (its type, categorially), takes this to
  38% (68% weighted).
* **What remains** is mostly a word or short phrase labelled NP in one tree
  and ADJP or ADVP in another: argument against modifier. That can't be
  decided by category alone. It is where lexical types would come in.

## Long sentences, estimated

With `quotient -estimate K`, K trees are drawn uniformly from each forest,
and the size of each one's class is counted exactly. The size is the product
over its projections of the parses of the projection's dependents by the
lexical and layer rules. On forests small enough to enumerate, the mean of
log10 class size agrees with log10 T − H(class) to 1e-12.

On the 300 sentences of 5 to 25 words (log10 T 18.26), with 100 draws each:

| quotient | entropy removed (digits, uniform) | share | log10 class size of the sentence's own tree |
|---|---|---|---|
| verbs, no scope exemption | 0.03 | 0.2% | 0.25 |
| verbs and auxiliaries, no scope exemption | 0.03 | 0.2% | 0.48 |

Uniform trees rarely give verbs several dependents, so the quotient removes
almost nothing from them. The gold trees are the realistic case, and there a
tree's class holds on average about 1.8 bracketings, or 3 with auxiliaries.
The weighted entropy is 1.67 digits, and trees drawn by rule frequency look
like the gold ones. So the verb quotient's part of the weighted entropy may
be a tenth to a quarter. That is not measured: it needs a sampler weighted
by rule frequency.

## Caveats, and what it means for the parser change

* **These are short sentences,** mostly tweets and fragments, with few
  adjuncts to stack. The order of attachment should matter more in long
  sentences, where verbs have several modifiers. Enumeration cannot reach
  them.
* **Long sentences can still be estimated without changing the parser.**
  Take a tree drawn uniformly from the forest. Its class is fixed except
  inside its verb projections, so the size of its class is the product, over
  its projections, of the number of ways the grammar's layer rules bracket
  that projection's dependents: a tiny parse of a few symbols. Under uniform
  weighting the entropy the quotient removes is the mean of log10 of that
  size over drawn trees. This would say whether the parser transform is
  worth building for long sentences.
* **Labels matter more than attachment order.** The larger source of
  spurious ambiguity is label choice. A quotient by label classes (the
  categories the flat semantics treats alike) is a different transform. It
  is not exact by relabelling the grammar's symbols alone, since the
  relabelled grammar can combine rules the original could not.

## Reproducing

```bash
cd go
go run ./cmd/quotient -counts $S/ann/counts.tsv -annotated $S/ann/annotated.jsonl            # the table (about 3 minutes)
go run ./cmd/quotient -counts $S/ann/counts.tsv -annotated $S/ann/annotated.jsonl -n 40 -max 6 -show 5 -limit 20000
go run ./cmd/quotient -counts $S/ann/counts.tsv -annotated $S/ann/annotated.jsonl -n 300 -min 5 -max 25 -limit 20000 -estimate 100
```
