# Complements in UD English, judged against CGELBank

[CGELBank](https://github.com/nert-nlp/cgel) (Reynolds, Arora and Schneider,
LAW 2023) has about 300 sentences of EWT and Twitter with gold trees in
CGEL's analysis and parallel UD. Each PP of a VP is a `Comp` or a `Mod`.
UD does not make this distinction: `cgel2ud` maps both to `obl`, and
`ud-to-cgel.ini` has `;TODO: obl`, mapping every `obl` to `Mod`.

This directory asks how well the sources built on MASC (`../README.md`), and
a short decision list of CGEL's own tests, fill that TODO. Every score is
agreement with CGELBank. CGELBank is not perfect either: its two annotators
agreed on 19 of 28 verb PPs' `Comp`/`Mod` before adjudication.

## What counts as a PP

CGEL's prepositions include many of UD's subordinators and adverbs.
`ud_pps.py` finds a verb's PPs in a UD tree as:

| kind | in UD | example |
|---|---|---|
| `obl` | `obl` or `obl:agent` with a `case` child | *rely **on** Pat* |
| `advcl` | `advcl` with a `mark` other than *that, whether, to, for* | *left **before** they closed* |
| `advmod` | `advmod` whose lemma is an intransitive preposition (`INTRANSITIVE_P`) | *went **home*** |
| `cop` | a predicate with copula *be* and a `case` child, or an intransitive preposition | *has been **to** shops* |

`gold()` reads the PPs of VPs from the `.cgel` files; `align()` matches each
to a UD PP of the same sentence by the verb lemma and the first word of the
preposition. A gold verb that is an auxiliary matches any verb (CGEL makes
auxiliaries heads).

* **Gold** is `ewt`, `twitter`, `ewt-test_iaa50` and `ewt-test_pilot5`: 138
  PPs aligned (75 Comp, 63 Mod), 23 not.
* **Trial** is `trial/ewt-trial` and `trial/twitter-etc-trial`: 46 aligned
  (31 Comp, 15 Mod), 14 not. These are not adjudicated. Nothing here was
  fitted to them.

## The sources

* `masc_table`: MASC's rate for the (verb, preposition) pair is at least 0.5.
* `verbnet`: a frame of one of the verb's VerbNet classes has this
  preposition.
* `masc_rules`: the Aleph theory learned on MASC with VerbNet
  (`../theories/verbnet.pl`).
* `cgel_tests`: a decision list of CGEL's criteria, the first test that
  applies deciding:
  1. a passive's by-phrase (`obl:agent`): Comp;
  2. *be* with a PP: Comp;
  3. an object of time (WordNet `noun.time`): Mod;
  4. a preposition of time, cause or condition taking a clause (*before,
     after, because, if, although* …): Mod;
  5. *now, then, so*: Mod;
  6. an intransitive preposition with a verb of motion or contact, or of a
     spatial VerbNet class: Comp;
  7. otherwise the evidence: `masc_table` or `verbnet`.
* `cgel_tests2`: the same, but test 1 also takes a *by*-PP of a past
  participle. EWT mostly labels a passive's by-phrase plain `obl`, not
  `obl:agent`, so in the gold test 1 never fired. **This change was found by
  looking at the gold.** The trial files are the check on it.

## Results

Gold (138):

| source | accuracy | Comp P | R | F |
|---|---|---|---|---|
| all Mod | 45.7 | | | 0.0 |
| masc_table | 57.2 | 80.8 | 28.0 | 41.6 |
| verbnet | 60.1 | 70.8 | 45.3 | 55.3 |
| masc_rules | 55.1 | 81.0 | 22.7 | 35.4 |
| masc_table or verbnet | 61.6 | 69.6 | 52.0 | 59.5 |
| cgel_tests | 71.0 | 78.7 | 64.0 | 70.6 |
| cgel_tests2 | **75.4** | 80.6 | 72.0 | **76.1** |

Trial, held out (46):

| source | accuracy | Comp P | R | F |
|---|---|---|---|---|
| all Mod | 32.6 | | | 0.0 |
| masc_table | 52.2 | 100.0 | 29.0 | 45.0 |
| verbnet | 56.5 | 86.7 | 41.9 | 56.5 |
| masc_rules | 54.3 | 100.0 | 32.3 | 48.8 |
| masc_table or verbnet | 63.0 | 88.9 | 51.6 | 65.3 |
| cgel_tests | 76.1 | 95.5 | 67.7 | 79.2 |
| cgel_tests2 | **80.4** | 95.8 | 74.2 | **83.6** |

The tests of `cgel_tests2` one by one (decided / agreeing with CGELBank):

| test | gold | trial |
|---|---|---|
| agent | 6 / 6 | 4 / 4 |
| be + PP | 6 / 6 | 2 / 2 |
| time object | 10 / 9 | 1 / 1 |
| adverbial clause | 8 / 7 | 5 / 5 |
| now / then / so | 6 / 6 | 1 / 1 |
| locative with motion verb | 7 / 7 | 3 / 3 |
| evidence (fallback) | 95 / 63 | 30 / 21 |

## What this shows

* **MASC transfers badly on its own.** Its rate table is precise but finds
  under a third of CGEL's complements. This matches the adjudication
  (`../adjudication/`): MASC's CLR is right where asserted, but MASC
  under-labels complements, and CGEL's notion is broader still (locatives
  and goals licensed by the verb, *be* + PP, the passive's by-phrase).
* **CGEL's structural tests are nearly always right.** The six tests decide
  43 of 138 gold PPs and 16 of 46 trial PPs, and are wrong twice in all.
  Most of these are not `obl` in UD at all: they are `advcl`, `advmod`, and
  predicates of *be*, which a converter must already look at to find CGEL's
  PPs.
* **The remaining errors are in ordinary `obl`,** where the fallback
  (MASC or VerbNet) is 66% right on gold and 70% on trial.
  * Missed complements: *decide between*, *renege on*, *officiate at*, *go
    to the bathroom*, *file against*, *check with*, *race past*, *see to*,
    *launch from*, *dig in*.
  * VerbNet over-licenses: *live in harmony*, *join in space*, *cut from*,
    *recite from*, *fix for $90*.

The sample is small and the tests are few. The next steps are a better
fallback for `obl` (object senses against the verb's VerbNet roles, *do so*
proxies from corpus counts), and an `obl:arg`/Comp layer over all of EWT
with the deciding test recorded for each PP.

## Running

```bash
cd tools/ilp
uv run python cgel/evaluate_cgel.py CGELBANK/datasets $S/pp_examples.tsv $S/nltk_data $S/cgel_eval.tsv
uv run python cgel/evaluate_cgel.py CGELBANK/datasets $S/pp_examples.tsv $S/nltk_data $S/cgel_trial.tsv --trial
```

`CGELBANK` is a clone of nert-nlp/cgel. `pp_examples.tsv` and `nltk_data`
are as for `../build.py`. The output TSV has one row per aligned PP: the
gold label, each source's decision, the deciding test, and the sentence.
