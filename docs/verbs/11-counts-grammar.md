# A counts grammar: phrase labels as counts of basic types

Two suggestions from the project's owner, taken together.

1. **Counts instead of labels.** Write a phrase's label categorially, as the
   counts of a few basic types in its category, without slashes or their
   order.
2. **Auxiliaries and modals as modifiers.** On the event view, they modify
   the event rather than head it. When *be* is a main verb (the copula, with
   no verb phrase after it), it still counts.

Report 10 found that, of the entropy which does not change a short
sentence's flat meaning, the order of attachment within verb projections
is 1% and phrase-label choice is nearly all. This report builds the counts
representation into the treebank itself and measures the grammar read off
it.

Code: `go/counts` (the conversion), `go/cmd/countbank` (grammar and
measures).

## The conversion

Counts of the basic types S, NP, PP and AP. By the count invariant of
categorial grammar, function application adds counts, (X − Y) + Y = X. A
phrase's counts are the sum of its daughters', and a modifier X/X counts
nothing.

The counts are assigned top down, from the gold trees with their function
tags and head rules:
* **Complements** get their category's counts: a clause S, a verb phrase
  S − NP, a noun phrase NP, a prepositional phrase PP, a predicative
  adjective phrase AP. Complements are subjects; anything tagged PRD, CLR,
  DTV or PUT; untagged NP, S and SBAR under a verb phrase; and the objects
  of prepositions.
* **Modifiers** count 0.
* **Heads** get their mother's counts less their sisters'.

So the sum holds at every node by construction, and a word's type is what
is left for it:
* *saw* in "saw her" is S − 2NP;
* *is* in "is happy" is S − NP − AP;
* *will* in "will go" is 0;
* *on* in a modifying PP is −NP;
* a determiner is 0;
* in a coordination of k conjuncts, each conjunct gets what the head would,
  and the conjunction −(k−1) times it.

A phrase over a single daughter has that daughter's counts, so unary
chains of phrases vanish. Of the phrase labels, only the counts remain.

All 34,582 MASC trees convert, and the sum holds at every node. The
grammar has 23,258 rules, 39 phrase symbols and 611 lexical types (a tag
with counts) over 74 tags, a median of 4 types a tag.

## The ambiguity

The same 300 held-out sentences of 5 to 25 words as reports 07 to 09. The
weights are P(rule | parent) and P(word | tag and type), from all the
documents.

| grammar and lexicon | own tree among the parses | log10 trees per word, median | log10 trees, mean | weighted entropy, digits |
|---|---|---|---|---|
| treebank grammar, gold POS tags | 100% | 1.29 | 18.26 | 1.60 |
| counts grammar, each word its gold type (oracle) | 100% | 0.28 | 4.07 | 0.99 |
| counts grammar, types seen with the word and its tag in training | 64.3% | 0.54 | 7.70 | 1.53 |
| counts grammar, types seen with the tag in training | 99.3% | 0.65 | 9.27 | 2.63 |

* **With the same information as the treebank grammar**, POS tags only, the
  counts grammar has some 10^9 trees a sentence where the treebank grammar
  has 10^18, and still keeps the sentence's own tree 99.3% of the time. The
  label ambiguity is gone. The lexical ambiguity it becomes (which type a
  word has) is much smaller.
* **With each word's gold type**, a supertagger's best case, there are
  10^4.
* **The types seen with the word are too sparse** to keep the right one
  for a third of the sentences. A real supertagger would need smoothing.
* **The weighted entropy is higher with tag-level types** (2.63 digits)
  than the treebank grammar's (1.60). The distribution now includes the
  choice of type, and 39 phrase symbols make a weaker model of rule
  frequencies than the treebank's thousands.

## The order of attachment, with auxiliaries as modifiers

Modifiers and auxiliaries count zero, so a layer of adjunction is simply a
phrase whose one daughter has its own counts and whose other daughters all
count zero. The quotient of report 10 becomes: splice such a daughter into
its mother. It is not spliced if it has a scope-taking adverb of its own,
from the list in `quotient.Scope`.

The oracle forests are small enough to enumerate for 206 of the 300
sentences. Their entropy is 2.85 digits uniform and 0.63 weighted.

| projections flattened | H(class) uniform | share removed | H(class) weighted | share removed |
|---|---|---|---|---|
| every projection | 1.94 | 32% | 0.47 | 25% |
| verbs' (counts with S) | 2.47 | 13% | 0.55 | 13% |
| nouns' (counts NP) | 2.56 | 10% | 0.61 | 4% |

So in the counts grammar the order of attachment matters. It is a quarter
to a third of what remains, and 13% for verbs alone. In the treebank
grammar it was negligible (report 10), because label choice swamped
everything else. The 206 sentences are those with the smaller forests, so
these shares are for the easier sentences.

## What next

The parser change that counts classes directly is now simpler to state.
Adjunction layers share their mother's label, so the class of a projection
is its label, its head, and its zero-count dependents in surface order. The
constructions:
* **Exact:** a deterministic automaton per label over the dependent
  sequences the grammar allows, as proposed for report 10.
* **Normal form, a model change:** zero-count dependents attach in one
  canonical order, all on the right first and then on the left, as in
  split-head dependency parsing. It is exact only if the grammar's rules
  let modifiers attach at any layer, which a treebank grammar only roughly
  does.

## Reproducing

```bash
cd go
go run ./cmd/countbank -annotated $S/ann/annotated.jsonl -n 300 [-o counts.tsv] [-enumerate 200000]
```
