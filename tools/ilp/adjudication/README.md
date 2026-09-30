# Adjudicating complements

A sample of 56 PPs, to be judged complement or adjunct by CGEL's
criteria. It is meant to show how far MASC's labels (CLR, PUT, DTV or none),
VerbNet, and the learned rules can each be trusted. Every score in
`../README.md` is agreement with MASC. This is the check on MASC itself.

* `sample.md` is for reading: each PP in its sentence, the verb in *stars*
  and the PP in [brackets].
* `sample.tsv` is for filling in: the columns `judgment` (complement,
  adjunct or unclear) and `note` (the criterion that decided it).

## The strata

| stratum | what | PPs sampled | of |
|---|---|---|---|
| A | MASC says complement; the verbnet theory and VerbNet both say not | 15 | 392 |
| B | MASC says adjunct; the theory and VerbNet both say complement | 15 | 187 |
| C | a verb and preposition pair MASC splits: one PP of each label, from 8 pairs | 16 | 58 pairs |
| D | all three agree, as a control: 5 complements, 5 adjuncts | 10 | 3,043 |

* **Source.** A, B and D are drawn from the held-out documents, so the
  theory had not seen them. C is drawn from all documents.
* **The rule.** `theories/verbnet.pl`, the theory learned with VerbNet and
  no lemmas.
* **VerbNet** says complement when some frame of the verb's classes has a
  PP with this very preposition (`vn_prep`).

## Criteria

These are CGEL's, from ch. 4 §1.2 ("Complements vs adjuncts"), §5.2
(location, goal and source) and §6.1 (prepositional verbs). A PP is a
complement when the verb licenses it: an adjunct, such as *for this reason*
or *at that time*, is not restricted to a particular kind of verb (§1.2(a)).
The tests, roughly in order of weight:

1. **Specified preposition** (§1.2(a), §6.1). The verb selects the
   preposition: *consist of*, *look at*, *depend on*, *give … to*, *supply
   … with*, *blame … on*. It cannot be replaced without ungrammaticality or
   an unsystematic change of meaning (*look at* against *look for*).
   * `../cgel/prepositional_verbs.tsv` has §6.1.2's lists, which the book
     calls a small sample.
   * *as* with a predicative is included: *regard it as*, *count as*,
     *use it as*.
2. **Obligatoriness** (§1.2(b)). A PP whose omission is ungrammatical, or
   changes the verb's sense, is a complement: *put the money \*(in her
   account)*, *Lunch was followed \*(by the speech)*.
   * The analysis generalises to the same verb class where the PP is
     optional: *deposit* the money in her account.
   * The passive's *by*-phrase is a complement, though a "somewhat
     peripheral" one.
   * *Be* is the exception. *Jill is in her study* and *The meeting was on
     Monday* have complements, but the same PPs with other verbs are
     "prototypical adjuncts" (*signed it in her study*).
3. **Role** (§1.2(h)). A complement's role depends on the verb. An adjunct's
   comes from its own content, the same with any verb: time, place of the
   event, reason, manner.
   * With a contrastive preposition the preposition fixes the role: *pushed
     it to/toward/past the house*.
   * With a determined one the verb does: the recipient of *give … to*.
4. ***Do so*** (§1.2(c)). A PP that can combine with *do so* is an adjunct:
   *Jill washes her car in the garage but Pam does so in the road*. A
   complement cannot: *\*Jill keeps her car in the garage but Pam does so
   in the road*.
   * The test works one way only. Failing *do so* does not show a
     complement, because *do so* has semantic restrictions of its own:
     *\*Kim died in 1995 and Pat did so last year* fails even though both
     are adjuncts.
5. **Locatives, goals and sources** (§5.2, §1.2(c–d)).
   * A location licensed by the verb is a complement: *keep … in*, *live
     in*, *remain outside*.
   * So are goals and sources of motion: *rode her bicycle to school*,
     *ran from the scene*, *took the bed downstairs*.
   * The same roles extend to states (*turn into a prince*, *go to sleep*)
     and to possession (*sell … to*, *buy … from*, *belong to*).
   * A location of the event is an adjunct: *slept on the floor*, *slept
     downstairs*.
6. **Preposition + clause** (§1.2(d)). These are predominantly adjuncts
   (*left because the baby was sick*). As complements they are largely
   limited to copular clauses (*That was long before we were married*).

Mark **unclear** where the tests disagree, and say which way each points in
`note`. The unclear cases matter as much as the clear ones.

## What the judgments are for

1. **Per-stratum agreement.** In each stratum, how often each source agrees
   with the judgments:
   * A tests MASC's CLR where nothing else supports it;
   * B tests the rules and VerbNet where MASC withholds CLR;
   * C tests MASC's consistency;
   * D is the baseline for how often full agreement is right.
2. **Reweighting.** Those rates, weighted by the strata's sizes, give a first
   estimate of each source's reliability. That is the start of treating
   MASC, VerbNet, the rules and an LLM applying these tests as annotators of
   unknown reliability (Dawid and Skene 1979).
3. **An LLM annotator.** An LLM given the same criteria can judge the same
   PPs, and its agreement with the human judgments says how far it can be
   used on the rest.

## A first pass: an LLM's judgments

`sample.tsv` has three more columns:
* `llm_judgment` and `llm_note`: the judgments of the LLM in the session
  that built this, by the criteria above;
* `llm_saw_labels`: whether it had seen the item's MASC, VerbNet and rule
  labels before judging.

The judgments were made under an earlier version of the criteria above,
written from memory before the chapter was at hand. That version stated
*do so* as a test both ways, and did not separate locatives of position
from licensed locations.

The judge worked from a shuffled list of verb, PP and sentence. It had seen
the labels of 19 items: 9 printed earlier in the session, and 10 exposed
when a reader mangled the list. The `judgment` column is left for a human.

Agreement with the LLM on the items it judged clear:

| stratum | judged | complement / adjunct / unclear | MASC | VerbNet | rule |
|---|---|---|---|---|---|
| A: MASC alone says complement | 15 | 12 / 1 / 2 | **12/13** | 1/13 | 1/13 |
| B: rules and VerbNet against MASC | 15 | 13 / 1 / 1 | 1/14 | **13/14** | **13/14** |
| C: pairs MASC splits | 16 | 11 / 3 / 2 | 10/14 | 11/14 | 6/14 |
| D: all agree (control) | 10 | 6 / 4 / 0 | 9/10 | 9/10 | 9/10 |

**If these judgments hold up:**
* **MASC's CLR is trustworthy where it is asserted** (stratum A). When MASC
  marks a PP CLR, PUT or DTV, it is a complement: *wait for*, *laugh at*,
  *hold to*, *do away with*.
* **MASC's lack of CLR is not.** Where the rules and VerbNet say complement
  and MASC is silent (stratum B), the PP is a complement 13 times in 14.
  The silent cases are:
  * a topic of a verb of communication: *interviewed about this*;
  * an addressee: *says to me*, *reply to*;
  * a goal: *brought to court*, *taking to Bingo*;
  * a specified preposition: *vote for*, *prohibit from*, *furnish with*,
    *allow for*;
  * the predicative *treat as*.

  So the "adjunct" class of this experiment contains many complements. The
  rules' and VerbNet's losses against MASC are, in stratum B, MASC's
  misses.
* **The split pairs are mostly complements.** *come to*, *remove from*,
  *benefit from* and *fall into* are complements on both sides of MASC's
  split. The real sense differences are *go after* (pursue vs. time) and
  *leave with* (*be left with* vs. company), with *begin with* in between.
* **The control.** Its one disagreement is *lectured at*, a prepositional
  passive, which only a complement allows. All three sources call it an
  adjunct.

**How much to trust this:**
* One judge, an LLM, applying a broad notion of complement. Topics of verbs
  of communication, addressees, and the goals of motion verbs all count as
  complements here. A narrower reading of CGEL would move some of stratum
  B.
* The samples are small: 15 or 16 a stratum.
* 19 of the 56 labels were seen before judging. On the 35 clear items
  judged unseen, agreement is MASC 18, VerbNet 24, rule 19.
* A human pass in `judgment`, and a scripted LLM run with a fixed prompt,
  would test all three points.

## Redrawing

```bash
cd tools/ilp
uv run python adjudication/sample.py $S/pp_examples.tsv $S/nltk_data theories/verbnet.pl adjudication
```

The seed is fixed, so this redraws the same sample.
