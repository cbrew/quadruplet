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

These are CGEL's, chapter 4, as summarised here from memory; check them
against the book. A PP is a complement when it is licensed by the verb, and
an adjunct when it would combine with any verb of the kind. The tests,
roughly in order of weight:

1. **Specified preposition.** The verb selects the preposition: *rely on*,
   *consist of*, *look at*, *listen to*. Another preposition changes the
   verb's sense or is impossible.
2. **Obligatoriness.** Leaving the PP out is ungrammatical, or changes the
   verb's sense: *put it \*(on the table)*, *live \*(in Paris)*. Optional
   complements exist too (*talk to her*), so a PP that can be left out is
   not thereby an adjunct.
3. **The role comes from the verb.** A complement's semantic role is fixed
   by the verb: the recipient of *give … to*, the goal of *put*. An adjunct's
   comes from the preposition alone: time, place of the event, reason,
   instrument.
4. **Do so.** An adjunct can follow *do so*, a complement cannot. *Kim slept
   in the bed and Lee did so on the sofa* is fine, so that PP is an
   adjunct. *\*Kim relied on Pat and Lee did so on Sam* is not, so that PP
   is a complement.
5. **Locatives and goals.** A locative or goal PP licensed by the verb is a
   complement (*live in*, *go to*, *put on*). MASC usually does not tag
   these CLR.

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
