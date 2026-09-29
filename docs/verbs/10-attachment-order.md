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
```
