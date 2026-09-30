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

## Redrawing

```bash
cd tools/ilp
uv run python adjudication/sample.py $S/pp_examples.tsv $S/nltk_data theories/verbnet.pl adjudication
```

The seed is fixed, so this redraws the same sample.
