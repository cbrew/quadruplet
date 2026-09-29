# Where the ambiguity is, seen from the verbs

The question: how much of a treebank grammar's ambiguity lies in the verbs'
choices, and how much would a lexicon of verb frames remove? Two answers,
depending on how likely the trees are taken to be.

* **Every tree equally likely.** The ambiguity is almost all elsewhere. Even
  an oracle that gives every verb exactly its own use removes only a
  quarter of the log count. A learned lexicon removes about 10^0.4 trees a
  sentence out of about 10^18. The reason: most of the trees do not give
  the verbs their dependents at all. In the average tree, 93% of the words
  other than verbs lie outside every verb's phrase (40% in the gold trees),
  and most verbs are read as auxiliaries or as words inside other phrases.
* **Trees weighted by the treebank's rule frequencies.** The words fall
  under verb phrases as in the gold trees, and the verbs' uses match the
  gold trees'. The entropy is about 1.7 decimal digits, some 47 equally
  likely trees' worth. The verbs' own choices are 39% of it: the complement
  frame 16%, the rest of the verb phrase's rule (modifiers, PPs) 16%. A
  lexicon of uses removes a third of the complement frame's part.

## Data and method

300 sentences of 5 to 25 words, with gold tags, from the tenth of MASC's
documents that cmd/functions also tests on. Everything was learned from the
other nine tenths. The exception is the grammar's rules and, for the
weighted figures from `counts.tsv`, their frequencies; `-train` uses the
training documents' frequencies instead, and gives the same picture.

**A verb's use** (package `go/frames`) is read off the rule the verb is a
daughter of:
* in a lexical verb phrase rule (VP, or a chain ending in VP, with a verb and
  no VP daughter), its frame, at three grains:
  * core: NP, clauses, ADJP, PRT, UCP daughters, in order;
  * pp: those and PP;
  * rule: every daughter;
* in a verb phrase with a verb phrase daughter, `(aux)`;
* in any other phrase X, `(in X)`: a participle in a noun phrase, say.

A lexicon allows a lemma (tools/masc/verblemmas.py) the uses seen with it in
training, if it was seen 5 times or more; other verbs are unrestricted.

**The filter is exact.** Every verb daughter of every rule has its tag
renamed by its use (VBD → VBD~F7). A tree of the grammar then corresponds to
at most one tree of the renamed grammar, and to one exactly when the lexicon
allows every verb's use. So the parser's count is the number of trees the
lexicon allows, with no sampling. With no lexicon, `framelex` checks that
the renamed grammar counts as many trees as the grammar. The **oracle**
allows each verb token only the use its own tree gives it: an upper bound on
what any lexicon of uses could do.

**The decomposition is exact too** (`cfg.Forest.Entropy`). Take a
distribution over a forest's trees: uniform, or proportional to the product
of the rules' relative frequencies P(rule | parent). Its entropy is the
expected sum of the entropies of the local choices a tree makes:

    H = sum over items x and context states s of mu(x, s) H(choice at x)

mu(x, s) is how often a tree uses item x in context s, from inside and
outside values. The context is a small top-down automaton. Here it counts
the lexical verb phrases above an item, and records which rule an
auxiliary item of binarization belongs to. A choice at a verb phrase is
split by the chain rule into:
* the kind of rule (lexical, auxiliary, coordination, adjunction, other);
* the complement frame (core grain) given the kind;
* the rest of the rule given both;
* the daughters' spans.

The engine is tested against enumeration on random grammars, uniform and
weighted. On MASC the parts sum to log10 of the count to within 1e-13.

## The lexicon filter (`framelex`, uniform trees)

Coverage: of the 9,107 verb tokens in all the test documents, 91.8% belong
to lemmas seen 5+ times in training. For those:

| grain | uses in the grammar | own use allowed, uses seen 1+ / 2+ times | uses allowed a verb, median, 1+ / 2+ |
|---|---|---|---|
| core | 267 | 95.3% / 92.3% | 15 / 10 |
| pp | 478 | 92.1% / 87.6% | 24 / 13 |
| rule | 2,322 | 86.8% / 81.5% | 40 / 16 |

On the 300 sentences (log10 trees a word 1.29 without a lexicon; log10 trees
cut per sentence, median and mean; uses a verb has in some tree of its
forest, median):

| grain | lexicon | own tree kept | log10 trees cut | uses a verb can have |
|---|---|---|---|---|
| core | none | 100% | – | 59 |
| core | uses seen 1+ times | 93.0% | 0.40 (0.48) | 11 |
| core | uses seen 2+ times | 87.7% | 0.53 (0.66) | 8 |
| core | oracle | 100% | 3.00 (3.35) | 1 |
| pp | uses seen 1+ times | 87.7% | 0.41 (0.55) | 15 |
| pp | oracle | 100% | 4.21 (4.70) | 1 |
| rule | uses seen 1+ times | 80.3% | 0.45 (0.64) | 18 |
| rule | uses seen 2+ times | 71.7% | 0.61 (0.91) | 11 |
| rule | oracle | 100% | 4.76 (5.13) | 1 |

The mean sentence has 10^18.3 trees. A lexicon cuts each verb's uses five-
to ninefold, yet removes only half a digit of eighteen. Even the oracle at
the finest grain removes under five: telling the parser exactly what every
verb does leaves some 10^13 trees.

Two earlier versions of the lexicon show why "uses", not frames:
* **Frames of lexical verb phrases only.** The plain tag stays open for
  every other reading, and the lexicon cut a median of 10^0.02.
* **Frames plus a single "heads no verb phrase".** Nearly every lemma has
  such a use somewhere, a participle inside a noun phrase for example, so
  it still cut only 10^0.02 (mean 10^0.2).

## Where the uniform trees put things (`verbentropy`)

Words other than verbs, by the number of lexical verb phrases above them:

| verb phrases above | all trees, uniform | own trees |
|---|---|---|
| 0 | 92.9% | 39.6% |
| 1 | 6.9% | 44.0% |
| 2 | 0.2% | 12.7% |
| 3+ | 0.0% | 3.7% |

The verbs' uses, core grain, in the uniform trees and in their own trees.
Without a lexicon, verbs heading no lexical verb phrase are 58% of the
uniform trees' verbs, against 16% of the gold trees'; NP objects are 3%
against 34%. With the core lexicon the trees find another way around it:
`(in NP)`, verbs read as parts of noun phrases, becomes 27% (gold 3%).

The decomposition of log10 T (mean over sentences, and share):

| where | kind of VP rule | complement frame | rest of rule | other phrases' rules | spans | total |
|---|---|---|---|---|---|---|
| top-layer verbs' phrases | 0.53 (3%) | 0.16 (1%) | 0.92 (5%) | – | 0.47 (3%) | 2.07 (11%) |
| outside any verb | – | – | – | 12.31 (67%) | 3.03 (17%) | 15.34 (84%) |
| verbs' phrases, depth 1 | 0.02 | 0.01 | 0.04 | – | 0.02 | 0.08 (0%) |
| inside dependents, depth 1 | – | – | – | 0.63 (3%) | 0.13 (1%) | 0.75 (4%) |
| all | 0.55 (3%) | 0.16 (1%) | 0.96 (5%) | 12.94 (71%) | 3.64 (20%) | 18.26 |

With the core lexicon, 17.78 digits, 82% outside any verb; with the rule
lexicon, 17.62.

## Weighted by rule frequency (`verbentropy -pcfg -train`)

Weights from the training documents' rule counts, plus one. With all of
`counts.tsv` the figures differ by a few hundredths (1.55 digits against
1.63 on 40 sentences).

The entropy of a sentence's trees is 1.67 decimal digits on average, about
47 equally likely trees' worth, although there are 10^18.3 trees. The trees
now look like the gold trees:

| verb phrases above a non-verb word | weighted trees | own trees |
|---|---|---|
| 0 | 39.3% | 39.6% |
| 1 | 43.9% | 44.0% |
| 2 | 14.1% | 12.7% |
| 3+ | 2.7% | 3.7% |

The verbs' uses (core grain) agree to a point or two. For example: NP
object 31.1% (own trees 33.8%); no complement 23.3% (24.9%); `(aux)` 7.4%
(8.9%); SxVP 6.4% (5.5%); `(in NP)` 3.9% (3.3%).

The decomposition (mean digits, and share of the entropy):

| where | kind of VP rule | complement frame | rest of rule | other phrases' rules | spans | total |
|---|---|---|---|---|---|---|
| top-layer verbs' phrases | 0.07 (4%) | 0.20 (12%) | 0.20 (12%) | – | 0.03 (2%) | 0.50 (30%) |
| outside any verb | – | – | – | 0.55 (33%) | 0.05 (3%) | 0.59 (36%) |
| verbs' phrases, depth 1 | 0.02 (1%) | 0.05 (3%) | 0.06 (4%) | – | 0.01 (1%) | 0.14 (8%) |
| inside dependents, depth 1 | – | – | – | 0.30 (18%) | 0.02 (1%) | 0.32 (19%) |
| verbs' phrases, depth 2 | – | 0.01 (1%) | 0.01 (1%) | – | – | 0.02 (1%) |
| inside dependents, depth 2+ | – | – | – | 0.09 (5%) | 0.01 | 0.09 (6%) |
| all | 0.09 (6%) | 0.27 (16%) | 0.27 (16%) | 0.93 (56%) | 0.11 (7%) | 1.67 |

So the verbs' own choices, at every depth, are 39% of the entropy. The
complement frame and the rest of the rule are 16% each, and the kind of
verb phrase rule is 6%. The choices outside any verb are 36%, and those
inside the verbs' dependents (noun phrases, mostly) 25%.

With a lexicon of uses, weighted:

| lexicon | entropy | complement frame | rest of rule | other phrases' rules |
|---|---|---|---|---|
| none | 1.67 | 0.27 | 0.27 | 0.93 |
| core, uses seen 1+ times | 1.59 | 0.18 | 0.28 | 0.94 |
| rule, uses seen 1+ times | 1.53 | 0.15 | 0.22 | 0.97 |

The core lexicon removes a third of the complement frame's entropy, and
leaves the rest alone. The rule lexicon removes a little of the rest as
well. Both cut the whole entropy by only 5–8%, because the lexicon learned
from nine tenths of MASC still allows most verbs many uses (a median of
11–18).

## The lexicon as probabilities (`verbentropy -pcfg -train -lexweights`)

A filter can only say yes or no. Weights say how likely each use is for a
given lemma. So each verb's use (its tag renamed at a grain, as above) also
carries a factor of P(use | lemma, tag) / P(use | tag), learned from the
training documents and smoothed toward P(use | tag) with alpha
pseudo-counts. This is exactly the grammar with split verb tags and lexical
emission probabilities: P(word | tag~use) is proportional to
P(use | word, tag) / P(use | tag), and P(word) is the same in every tree of a
sentence.

| weights | entropy | kind of VP rule | complement frame | rest of rule | other phrases' rules | spans |
|---|---|---|---|---|---|---|
| rule frequencies | 1.67 | 0.09 | 0.27 | 0.27 | 0.93 | 0.11 |
| and uses, core grain | 1.52 | 0.07 | 0.17 | 0.27 | 0.90 | 0.10 |
| and uses, pp grain | 1.50 | 0.07 | 0.17 | 0.24 | 0.92 | 0.10 |
| and uses, rule grain | 1.47 | 0.07 | 0.16 | 0.22 | 0.92 | 0.10 |

(alpha 5; with alpha 1 and 20 the core grain gives 1.51 and 1.55.)

As probabilities, the lexicon removes about 40% of the complement frame's
entropy, and none of the rest of the rule's until its uses are whole rules.
Even then it removes only a fifth. The choices outside the verbs and inside
their dependents do not move. Where the words fall and what the verbs do
stay as close to the gold trees as before; for example, NP objects are 32.0%
against 33.8%.

## What it means

* **The uniform count is the wrong quantity to put a verb lexicon against.**
  Almost all of the 10^18 trees are ones in which the verbs head nothing:
  they are auxiliaries over whatever follows, or words inside noun phrases,
  and the rest of the sentence is bracketed by the grammar's flat rules
  outside any verb. A lexicon can only rule out verb uses, and the trees
  route around it. This is the product of independent local choices of
  `docs/ambiguity.md`, seen from the verbs: most of it is not the verbs'.
* **Under the treebank's own rule frequencies the verbs matter.** The trees
  look like the gold trees in where the words fall and in what the verbs
  do. Of the remaining 1.7 digits, the verbs' choices are about two-fifths,
  split evenly between the complement frame and the rest of the rule. That is the place for a frame lexicon, and for the Levin-style
  alternations and the complement/modifier line of reports 01–06.
* **The lexicon knows complements, not modifiers.** Given the verb's
  lemma, the complement frame's entropy falls by 40%, and the rest of the
  rule's (modifiers, PPs, their order) hardly at all. This is report 06's
  finding (the verb removes 38% of the uncertainty about its complement
  frame and 10% about its modifiers), now measured in the parser's own
  forests rather than in the gold trees.
* **Complement frame versus modifier choice**, in these terms, is the
  split of each verb phrase's rule entropy into its frame part and its
  rest. Weighted, the two are equal (16% each). The rest includes
  the PPs, which are a complement a third of the time (06), so the modifier
  share is somewhat less than it looks.

## Open questions

* The entropies are of one sentence's trees given its words and gold tags.
  How they change with the tags open is untested.
* The weighted figures depend on the PCFG, with lexical conditioning only
  on the verbs' uses. Heads of other phrases (nouns, prepositions) are not
  lexicalised, and the "inside the dependents" share is theirs.
* The bottom-up layers (task 4 in `COORDINATION.md`) and the "don't care"
  share (task 5) are open.

## Reproducing

```bash
S=SCRATCH       # treebank.py's output in ann/, verbframes.py's lemmas.tsv in v2/
cd go
go run ./cmd/framelex -counts $S/ann/counts.tsv -annotated $S/ann/annotated.jsonl -lemmas $S/v2/lemmas.tsv -n 300
go run ./cmd/verbentropy -counts $S/ann/counts.tsv -annotated $S/ann/annotated.jsonl -lemmas $S/v2/lemmas.tsv -n 300 [-lexicon core|rule] [-pcfg [-train] [-lexweights [-grain core|pp|rule] [-alpha 5]]]
```

The outputs of these runs are in [`runs/`](runs/). One caveat: `runs/verbentropy.txt`
(uniform, no lexicon) was made before the verbs' uses were split into `(aux)`
and `(in X)`, so its table of uses shows both together as `-`. Its
decomposition does not depend on the labels.
