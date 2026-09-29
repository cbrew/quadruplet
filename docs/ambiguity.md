# Why is a treebank grammar so ambiguous?

A context-free grammar read off MASC's trees gives the median 30-word
sentence some 10^52 trees, and its most ambiguous sentence, of 160 words,
10^294 ([`fast-parser.md`](fast-parser.md) §6). No reader considers more
alternatives than there are atoms in the universe, so something that a
reader has, and the grammar lacks, keeps the ambiguity from mattering. This
note records what that might be, the measurements made so far to find out,
and the ideas not yet followed up.

Three assumptions of the usual answer are open to doubt, and the note tries
not to lean on them: that a sentence has one true tree; that meaning is
composed rule by rule (the rule-to-rule hypothesis); and that the missing
piece is probability.

## Counting is not exploring

The argument from the atoms rules out enumerating the trees, not holding
them. The parser here does not explore 10^110 alternatives either: at 80
words it builds some 69 million hyperedges, and the count is a sum of
products over them. A packed forest is a polynomial-sized, underspecified
representation of the whole set. So the puzzle is better put two ways:
why readers commit, as garden paths show they do and early; and which
distinctions they ever draw, since good-enough processing (Ferreira;
Christianson and others) suggests many are never drawn.

## Why the grammar is ambiguous

* It records coverage, not constraint. Each rule is licensed by one local
  tree somewhere in the corpus; nothing states subcategorization,
  agreement or selection, and nothing makes one rule depend on another. Its
  language is nearly any string of tags, bracketed any way the rules allow.
* The lexicon is noisy: a word's tags are every tag the corpus ever gave
  it, *the* being DT, NNP, PRP or RB.
* Flat rules of many daughters bracket the same material many ways, and
  many trees differ only in how adjuncts group or in the inside of a noun
  phrase: distinctions without a difference in meaning.

## Measurements

All on MASC, with the tools in `go/cmd/ambiguity` (see the end), on 300
sentences of 5 to 25 words drawn at random unless said otherwise.

### Where the ambiguity comes from

Each sentence parsed with variants of the grammar (median log10 trees per
word):

| grammar | trees per word, log10 | sentences' own trees still among the parses |
|---|---|---|
| as read off the treebank | 1.67 | 100% |
| each word given only its gold tag | 1.25 | 100% |
| only rules seen twice or more | 1.37 | 72% |
| only rules seen 5 times or more | 1.10 | 56% |
| gold tags, rules seen 5 times or more | 0.68 | 56% |

Lexical noise is a quarter of the ambiguity, measured in logs. The rare
rules are not noise: two-thirds of the 21,273 rules are seen once, and 28%
of the attested trees need one, yet without them every sentence still
parses, only wrongly. With the tags given and only frequent rules, a
20-word sentence still has some 10^14 trees.

Over the whole corpus (all 34,582 sentences, [`fast-parser.md`](fast-parser.md)
§6), the trees per word level off at about 10^1.82, some 66: the number of
trees grows as a constant raised to the length, which is what local
ambiguities multiplied together independently produce.

### What sets the attested tree apart

Among 200 trees drawn from each forest at random, every tree as likely as
any other (`cfg.Forest.Sampler`), the sentence's own tree has:

| measure | mean percentile | in the lowest 5% | in the highest 5% |
|---|---|---|---|
| phrases | 0.0 | 100% of sentences | 0% |
| depth | 9.4 | 66% | 0% |
| mean dependency length | 21.5 | 35% | 0.3% |

It has fewer phrases than every drawn tree in every sentence, and is
shallower, with shorter dependencies. But measured exactly, over every tree
of each forest (`interp.PhraseCounts`, `DepthCounts`,
`ShortestDependencies`):

* **Phrases.** The trees with the fewest phrases possible are few, some
  10^0.18 per word, but the attested tree is one of them in only 3% of
  sentences, and has five or more phrases beyond the fewest in half. It is
  in the far low tail, yet some 10^0.86 trees per word have exactly as many
  phrases, and 10^0.73 per word fewer: Minimal Attachment, taken literally
  as fewest nodes, is a strong bias but far from a choice.
* **Centre-embedding.** The deepest left-corner stack any word needs (Resnik
  1992, flat rules read as branching right) is 1 to 3 for the attested
  trees, and 81% of each forest (median) is no deeper: memory, measured
  this way, does not discriminate.
* **Dependency length.** The attested tree is never the one with the
  shortest total, and is 19 words longer than the shortest (median); 10^1.1
  trees per word reach the shortest. A pressure, then, not a minimum.

Uniform sampling is dominated by over-articulated trees, since unary chains
and binary splits multiply, so "fewer phrases than any tree drawn" is
partly a fact about the drawing. And some of what sets the attested tree
apart is the Penn Treebank's bracketing guidelines, not a reader's
preferences.

### The view through verbs

The ambiguity per sentence is astronomical; the ambiguity per event need
not be. In a context-free forest the independence of the parts is not an
extra assumption but the grammar itself: each item's analyses combine with
its neighbours' in every way, so trees are a product of local choices, and
that is what makes a packed forest small and its count huge. If each of
five clauses had twenty analyses, the forest would report 3.2 million
readings, most of them combinations no reader would entertain. Treating a
sentence's events as independent is cognitively absurd: they share
participants (control, coordination, relative clauses), time and place,
and must cohere as one situation. Any constraint across events cuts the
product; and because such constraints are not local, they are just what a
context-free grammar cannot state.

A first look, by sampling: 1,000 trees drawn from each forest, under the
grammar with gold tags, on the 273 sentences with verbs (654 verbs, a
median of 2 a sentence); a verb's local analysis is its dependents in a
tree.

| after draws | 250 | 500 | 1,000 |
|---|---|---|---|
| distinct analyses of a verb (dependents with their categories), median | 90 | 157 | 278 |
| distinct sets of words depending on a verb, punctuation aside, median | 46 | 72 | 114 |

Neither saturates, so under uniform sampling a verb's local ambiguity is
not small: tens to hundreds of distinct dependent sets, and growing. The
attested set of dependents was among those drawn for 84% of verbs (the
labelled analysis for 54%). Over a sentence, the 1,000 draws gave a median
of 312 distinct combinations of the verbs' dependent sets: much of each
forest's variety is elsewhere than in the verbs' frames, inside the noun
phrases and among the modifiers. Uniform draws over-represent implausible
trees, so these are upper bounds on what a verb's analyses would be under
any sensible constraint; how far a subcategorization lexicon alone would
cut them is the next measurement.

What the treebank's empty elements and function tags say about verb frames,
and how far complements are predictable from the verb where modifiers are
not, is in [`verbs/`](verbs/README.md).

### Counting against weighing, and where the verbs are

The same 300 held-out sentences, gold tags, measured exactly
([`verbs/07-frame-lexicon.md`](verbs/07-frame-lexicon.md) and
[`verbs/08-verb-layers.md`](verbs/08-verb-layers.md)). The entropy of a
forest's trees is split exactly, as an expected sum over the local choices
a tree makes (`cfg.Forest.Entropy`), or by layers of verbs
(`frames.VerbLayers`).

* **Every tree equally likely**, the entropy is log10 of the count, 18.3
  digits for the mean sentence. It is almost all outside the verbs: 84–94%,
  depending on the cut. The trees that make up the count mostly give the
  verbs no dependents:
  * 93% of the words other than verbs lie outside every lexical verb
    phrase, against 40% in the gold trees;
  * a third of the trees have no lexical verb phrase at all.
  A lexicon of verb uses learned from the other documents cuts each verb's
  uses from 59 to 11, yet removes about 10^0.4 trees of 10^18. An oracle
  that gives every verb its own use still leaves about 10^13.
* **Trees weighted by the treebank's rule frequencies**, the entropy is 1.6
  to 1.7 digits: some 40 to 50 trees' worth, not 10^18. The trees now look
  like the gold ones: where the words fall, what the verbs do, and how many
  verb phrases there are, top and bottom, all match to a point or two. The
  entropy splits:
  * finely: 39% the verbs' own choices (complement frame 16%, the rest of
    the verb phrase's rule 16%); 36% outside any verb; 25% inside their
    dependents;
  * by layers: 41% the skeleton around the top verbs, 24% the top verbs'
    expansions, 17% how verbs embed one another, 5% the bottom verbs', 13%
    inside their dependents.
* **The lexicon as probabilities** (each verb's use weighted by P(use |
  lemma) from training, over the rule frequencies): LEXW

So the astronomical count and the reader's problem come apart. The count
is dominated by trees the grammar's own rule frequencies make negligible.
Under those frequencies, the uncertainty that remains is a couple of
digits a sentence, and two-fifths of it is the verbs'.

## Candidate missing pieces

With what the measurements say so far:

* **Bounded memory, incrementally** (Chomsky and Miller; Resnik; Schuler
  and others' parsing under a bounded stack). Most of the forest is shallow
  already; the depth bound, as measured, does not discriminate.
* **Structural economy** (Frazier's Minimal Attachment). A strong bias, not
  a selection: the attested tree is far flatter than a random one, but
  rarely the flattest.
* **Dependency locality** (Gibson; Futrell, Mahowald and Gibson). A
  moderate pressure, not a minimum.
* **The lexicon, per event.** Supertagging (Bangalore and Joshi's "almost
  parsing"; Clark and Curran's CCG parsing, on Hockenmaier's CCGbank) shows
  that local lexical decisions remove most structural ambiguity; the
  treebank grammar has thrown that information away. Measured for verbs:
  against the count, a verb lexicon is almost powerless; against the
  weighted distribution it removes a real part of the verbs' share (see
  "Counting against weighing" above).
* **Frequency itself.** The treebank's rule frequencies alone take the
  entropy from 18 digits to under 2, and make the trees look like the gold
  ones. They are not a filter but a weight, and the question the
  introduction raised (is the missing piece probability?) now has a partial
  answer: probability does most of the work of the count, if not of the
  choice.
* **Interpretation as a filter, not a weight** (Crain and Steedman;
  Altmann and Steedman): syntax proposes and the discourse model disposes,
  word by word, by referential success. It couples events, and is the
  natural cure for their independence. Not yet measured.
* **Underspecification**: distinctions that do not matter for the task are
  never made. A packed forest is one; the question is which distinctions a
  reader draws at all.

## Ideas not yet followed up

* **Unpacking from the minimal analysis.** The (fewest phrases, how many)
  pass is a Viterbi pass, and best-first unpacking from it is well studied
  (Huang and Chiang's lazy k-best; selective unpacking for HPSG, Carroll and
  Oepen, and Zhang, Oepen and Carroll; and, if memory serves, Henry
  Thompson's best-first enumeration of paths through a lattice). In order of
  cost it is hopeless here, with some 10^15 trees ahead of the attested one
  in a 20-word sentence; in order of how many decisions differ from a
  minimal tree it may not be. To measure first: over the trees with the
  fewest phrases, the one sharing most constituents with the attested
  tree, by one more pass in a (fewest phrases, most shared) semiring.
* **Events, not sentences.** Enumerate each verb's local analyses, then
  combine them under constraints across events (shared participants,
  coherence), a sentence becoming a small constraint problem over its events
  rather than a product. First, exactly rather than by sampling: which
  words can depend on each verb at all, by an inside-outside pass over the
  forest split by head word. (How much a verb lexicon cuts each verb's
  analyses is now measured: see above.)
* **Lexicalization.** The same measurements on a lexicalized grammar, CCG
  categories as in CCGbank, to see how much of the product the lexicon
  removes.
* **Meaning as the filter.** The flat meanings of
  [`flat-semantics.md`](flat-semantics.md) as the image of each tree: how
  many distinct meanings a forest has, against how many trees, for
  sentences short enough to enumerate.

## Reproducing

From tools/masc/treebank.py's output (`counts.tsv`, `annotated.jsonl`):

```bash
cd go
go run ./cmd/ambiguity -counts OUT/counts.tsv -annotated OUT/annotated.jsonl -n 300             # where it comes from
go run ./cmd/ambiguity -counts OUT/counts.tsv -annotated OUT/annotated.jsonl -n 300 -samples 200 # against random trees
go run ./cmd/ambiguity -counts OUT/counts.tsv -annotated OUT/annotated.jsonl -n 300 -exact       # against all trees
go run ./cmd/ambiguity -counts OUT/counts.tsv -annotated OUT/annotated.jsonl -n 300 -verbs 1000  # through the verbs
```

## References

Cited from memory, to be checked before they are relied on.

Altmann, G. and Steedman, M. (1988). Interaction with context during human
sentence processing. *Cognition* 30, 191–238.

Bangalore, S. and Joshi, A. K. (1999). Supertagging: an approach to almost
parsing. *Computational Linguistics* 25(2), 237–265.

Carroll, J. and Oepen, S. (2005). High efficiency realization for a
wide-coverage unification grammar. *IJCNLP 2005*.

Crain, S. and Steedman, M. (1985). On not being led up the garden path. In
Dowty, Karttunen and Zwicky (eds.), *Natural Language Parsing*, Cambridge.

Frazier, L. (1979). *On Comprehending Sentences: Syntactic Parsing
Strategies*. PhD thesis, University of Connecticut.

Futrell, R., Mahowald, K. and Gibson, E. (2015). Large-scale evidence of
dependency length minimization in 37 languages. *PNAS* 112(33),
10336–10341.

Hockenmaier, J. and Steedman, M. (2007). CCGbank. *Computational
Linguistics* 33(3), 355–396.

Huang, L. and Chiang, D. (2005). Better k-best parsing. *Proceedings of
IWPT 2005*.

Resnik, P. (1992). Left-corner parsing and psychological plausibility.
*Proceedings of COLING 1992*.

Schuler, W., AbdelRahman, S., Miller, T. and Schwartz, L. (2010).
Broad-coverage parsing using human-like memory constraints. *Computational
Linguistics* 36(1), 1–30.
