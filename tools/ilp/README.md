# Which PPs are complements: ILP over MASC's Prolog programs

This is a first experiment with inductive logic programming (ILP) over the
sentence programs of `tools/masc/prolog`. The question is the open item 4 of
`tools/frames/README.md`: which of a verb's PPs are complements.

The learner is Aleph (Srinivasan), in Riguzzi's SWI-Prolog port
(github.com/friguzzi/aleph, `prolog/aleph_orig.pl`). The target is
`complement/1`.

## The data

**The examples.** `pp_examples.pl` is a transformation over the sentence
programs. It takes every PP daughter of a lexical verb's VP, meaning a VP
headed by a word tagged VB* with no VP daughter:
* **complement** where the treebank labels the PP CLR, PUT or DTV;
* **adjunct** where it has none of these;
* **left out:** predicative PPs and passive agents.

That gives 20,330 examples, 5,565 of them complements. They are split by
document, with the same held-out fifth as `tools/frames/src/frames/evaluate.py`:
* training: 15,189 examples, 4,012 complements;
* test: 5,141 examples, 1,553 complements.

**The background facts** of an example (`build.py`):
* `prep` (the preposition);
* `verb_sense` and `obj_sense`: WordNet supersenses, from the first sense;
  `obj_sense` also has the values pronoun, clause and trace (stranded);
* `obj_cat`, `vtag`;
* `next` (the PP comes straight after the verb);
* `obj_before` (an NP object comes before the PP);
* `other_pp`, `passive`.

Four backgrounds are used:
* **classes:** the facts above, with no words;
* **lexical:** the facts above, plus the verb's `lemma` and the object's
  head word, `obj_head`;
* **verbnet:** the classes background plus VerbNet 3.3 (NLTK's `verbnet3`):
  * `vn_class`: the verb's top VerbNet classes, e.g. `peer-30.3`;
  * `vn_group`: their Levin groups, e.g. `37`, communication;
  * `vn_prep`: some frame of the verb's classes, or of the classes above
    them, has a PP with this very preposition;
  * `vn_spatial`: such a frame has a spatial PP slot, and this preposition
    is spatial;
  * `vn_none`: VerbNet lacks the verb;
* **lexical_verbnet:** all of the above.

**The Aleph settings:**
* a clause has at most three body literals;
* each clause must cover at least 15 training positives, at a precision of
  at least 0.6;
* the covering algorithm (`induce`) is used.

Each run takes four to six minutes.

## Results on the held-out documents

| | accuracy | complement P | R | F |
|---|---|---|---|---|
| all adjuncts | 69.8 | – | 0 | – |
| rate table: P(complement \| lemma, prep), backed off to the preposition (`tools/frames`) | **85.3** | **80.2** | 68.2 | **73.7** |
| ILP, classes: 26 clauses | 77.5 | 69.4 | 45.7 | 55.1 |
| ILP, lexical: 44 clauses | 81.3 | 75.6 | 56.1 | 64.4 |
| ILP, verbnet: 44 clauses, no words | 82.0 | 74.9 | 60.8 | 67.1 |
| ILP, lexical_verbnet: 45 clauses | 82.4 | 75.3 | 62.0 | 68.0 |
| rate table, with ILP lexical where training never saw the pair | 84.8 | 77.8 | **69.3** | 73.3 |
| rate table, with ILP verbnet where training never saw the pair | 84.7 | 77.6 | **69.3** | 73.2 |

On the 1,221 held-out examples (24%) whose (lemma, prep) pair training
never saw:

| | accuracy | complement P | R | F |
|---|---|---|---|---|
| rate table (backed off to the preposition) | **89.8** | **43.4** | 19.7 | 27.1 |
| ILP, classes | 84.8 | 26.1 | 31.6 | 28.6 |
| ILP, lexical | 87.6 | 35.0 | **35.0** | **35.0** |
| ILP, verbnet | 87.1 | 33.3 | 34.2 | 33.8 |
| ILP, lexical_verbnet | 87.1 | 32.5 | 32.5 | 32.5 |

Read plainly:

* **VerbNet classes do what the words did, without the words.** With no
  lemma at all, the verbnet theory scores above the lexical theory: F 67.1
  against 64.4, and 55.1 for WordNet alone. Adding the lemmas to VerbNet
  gains only 0.9 more. The verb's class carries most of what the verb
  itself carried.
* **The table is still not beaten.** It holds thousands of (lemma, prep) rates;
  the lexical theory has 44 clauses and reaches 87% of the table's F. As a
  compression of the treebank's decisions, the theory does well. As a
  classifier, it is not better.
* **On unseen pairs the rules trade precision for recall.** Most PPs there
  are adjuncts, which is why the table's accuracy is high. The rules find
  more of the complements, at a cost in precision. Combining the two changes
  little overall.
* **The labels limit everything.** The treebank's CLR is a judgment of
  "closely related", which Bies et al. (1995) admit is applied
  inconsistently. So the ceiling is unknown.

## The rules

The value is in the clauses, which can be read and checked. Precision and
coverage below are on the held-out examples.

**Specified prepositions, in CGEL's sense:**
* `prep(A,about)`: 88% of 222;
* `prep(A,to), verb_sense(A,'verb.communication'), next(A)` (*talk to*):
  87% of 112;
* `prep(A,at), verb_sense(A,'verb.perception'), next(A)` (*look at*): 91%
  of 69;
* lexically: *listen to* 100% of 23, *add to* 100% of 21, *ask for* 94%
  of 18, *work on* 92% of 26, *pay for* 87% of 15, *depend* 75% of 12.

**Stranded prepositions:**
* `obj_sense(A,trace), obj_cat(A,np)` (*the man we referred to*): 73% of
  139.

**Posture verbs with a locative:**
* `next(A), lemma(A,stand)`: 88% of 16;
* `sit`: 76% of 21;
* `lie`: 75% of 4.

The treebank tags these LOC-CLR. CGEL counts locative PPs licensed by a
verb as complements, which is the open question of goal and locative
complements (item 4 in `tools/frames/README.md`). Here the treebank's own
labels already lean that way for these verbs.

**With VerbNet**, the clauses name classes instead of verbs:
* `prep(A,to), vn_group(A,'37'), vn_prep(A)`: a communication verb with its
  VerbNet *to* PP, 87% of 151;
* `next(A), vn_class(A,'peer-30.3'), vn_class(A,'rummage-35.5')`, which is
  *look at/for*: 96% of 112;
* `prep(A,for), vn_class(A,'inquire-37.1.2')` (*ask for*): 94% of 18;
* `prep(A,on), vn_class(A,'rely-70')`: 83% of 12;
* `prep(A,into), vn_class(A,'convert-26.6.2')` (*turn into*): 94% of 18;
* `prep(A,on), next(A), vn_class(A,'assuming_position-50')` (*sit on*,
  *lie on*): 100% of 12;
* `vn_class(A,'mix-22.1'), vn_prep(A)` (*combine with*, *add to*): 80% of
  39;
* `next(A), vn_group(A,'13'), vn_prep(A)`, change of possession: 61% of
  126.

The last clause is where VerbNet and the treebank part. VerbNet lists many
PPs of the change-of-possession verbs (*from*, *for*, *with*) that the
treebank leaves as adjuncts. Over all training PPs, `vn_prep` alone has a
precision of 40% and a recall of 58%: it is evidence, not a rule.

**Weak rules that need a second look:**
* `lemma(A,give)`: 38% of 34;
* `prep(A,to), obj_sense(A,'noun.person'), other_pp(A)`: 44% of 25;
* several `verb.contact`/`verb.change` + passive rules below 15%, in the
  classes theory.

These are where WordNet's first sense is a poor stand-in for a verb class.

## What next

* **The rules are hypotheses to put to the triads.** A clause such as
  `prep(A,to), verb_sense(A,'verb.communication')` names a class (verbs of
  communication taking *to*). Two members and a verb outside it make a
  triad, and an LLM's answer and reason test the class. This is the loop of
  corpus rules checked by sparse judgments.
* **More background.** VerbNet classes did better than WordNet's
  first-sense supersenses. What remains untried is the frame of the verb's
  other occurrences, a relational feature that ILP handles naturally and a
  table does not.
* **Other learners.** Popper (with noise and predicate invention) would
  need SWI-Prolog 9.1 or later for its Python bridge; this machine has
  9.0.4. Predicate invention is the route to verb classes the background
  does not already name.

## Reproducing

```bash
S=scratch; ptb=tools/masc/prolog/ptb.pl
python3 tools/masc/ptb2pl.py $MASC/data --all $S/masc-prolog
find $S/masc-prolog -name '*.pl' | sort \
  | swipl -q -g pp_tsv -t 'halt(1)' $ptb tools/ilp/pp_examples.pl > $S/pp_examples.tsv
# WordNet: raw.githubusercontent.com/nltk/nltk_data/gh-pages/packages/corpora/wordnet.zip
#   unpacked as $S/nltk_data/corpora/wordnet.zip
cd tools/ilp && uv run python build.py $S/pp_examples.tsv $S/nltk_data $S/ilp
cd $S/ilp/lexical && swipl -g "consult('aleph_orig.pl'), read_all(complement), induce, write_rules('theory.pl'), halt"
cd tools/ilp && swipl -q -g "evaluate('$S/ilp/lexical/theory.pl', '$S/ilp/test.pl')" -t 'halt(1)' evaluate.pl
swipl -q -g "predictions('$S/ilp/lexical/theory.pl', '$S/ilp/test.pl', 'p.tsv')" -t 'halt(1)' evaluate.pl
uv run python combine.py $S/ilp/table.tsv p.tsv
```

The learned theories are in `theories/`: `classes.pl`, `lexical.pl`,
`verbnet.pl` and `lexical_verbnet.pl`. VerbNet 3.3 is NLTK's `verbnet3`
corpus, unpacked as `$S/nltk_data/corpora/verbnet3/`.
