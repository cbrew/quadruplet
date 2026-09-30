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

The sample is small and the tests are few.

## Relabel, then learn

The rate table and the rules above learned MASC's notion of complement,
which is narrower than CGEL's. So the second experiment relabels MASC by
CGEL's tests and learns from the new labels. CGELBank stays purely for
evaluation.

**Extraction.** `pp_examples.pl`'s `cgel_tsv` takes every PP of a lexical
verb in CGEL's sense, as `ud_pps.py` does on UD:
* PP daughters, now including predicatives (PRD) and passive agents (LGS);
* SBARs led by a preposition (*before, because, if* …);
* ADVPs headed by an intransitive preposition (*home, away, back* …).

Particles are left out on both sides. That gives 26,510 PPs, against
pp_examples' 20,330.

**Relabelling.** `relabel.py` takes the first rule that applies. These
are CGEL's tests, with MASC's function tags as evidence:

| rule | label | PPs | of which MASC tags CLR/PUT/DTV |
|---|---|---|---|
| agent (LGS) | Comp | 982 | 0 |
| predicative (PRD) | Comp | 1,566 | 0 |
| time (TMP, or an object of time) | Mod | 3,061 | 62 |
| adverbial clause (*before, because* …) | Mod | 1,104 | 0 |
| *now, then, so* | Mod | 154 | 0 |
| intransitive P with a verb of motion | Comp | 757 | 94 |
| MASC CLR, PUT or DTV | Comp | 5,558 | 5,558 |
| VerbNet licenses the preposition (not for *be*) | Comp | 3,635 | 0 |
| goal of motion (DIR, or untagged *to/into/from* …) | Comp | 1,335 | 0 |
| default | Mod | 8,358 | 0 |

13,833 PPs (52%) are Comp, against 5,714 (22%) under MASC's tags;
CGELBank's gold is 54% Comp.
* **How the rules were fixed.** They were fixed before any CGELBank score
  of the relabelled data was seen. Two refinements came from reading MASC's
  trees, not CGELBank:
  * VerbNet is not consulted for *be*: its PPs after an NP-PRD, as in
    *were not alone in the spree*, are not its complements.
  * Untagged goals with a verb of motion count, as in *go to the closing
    statements*. MASC tags 137 *go to* PPs DIR and leaves 35 untagged.
* **The VerbNet rule** rests on the adjudication sample: 13 of 14 PPs that
  VerbNet licenses and MASC leaves untagged were judged complements.

**Learning.** Aleph was run with the same settings, the same split by
document, and the same four backgrounds, plus `copula/1` (the verb is
*be*). The theories are `../theories/cgel_{classes,lexical,verbnet,lexical_verbnet}.pl`.

Against the relabelled held-out MASC documents (6,835 PPs), which measures
how well each theory reproduces the relabeller:

| source | clauses | accuracy | Comp F | F on unseen (lemma, prep) |
|---|---|---|---|---|
| relabelled rate table | – | 81.4 | 82.9 | 56.4 |
| learned, classes | 20 | 75.7 | 78.5 | 55.9 |
| learned, lexical | 25 | 76.0 | 78.9 | 56.0 |
| learned, verbnet | 27 | 85.7 | 86.8 | 74.3 |
| learned, lexical_verbnet | 27 | 85.7 | 86.9 | 74.7 |

The VerbNet theories fit best partly by circularity. The relabeller uses
`vn_prep`, and the verbnet theory's first clause is `vn_prep(A)` (88% of
2,129).

Against CGELBank (accuracy / Comp F):

| source | gold (138) | trial, held out (46) |
|---|---|---|
| cgel_tests2 (fallback: MASC table or VerbNet), as above | 75.4 / 76.1 | 80.4 / 83.6 |
| relabelled rate table alone | 76.1 / 78.1 | 82.6 / 85.7 |
| learned, classes alone | 71.0 / 74.0 | 65.2 / 73.3 |
| learned, lexical alone | 70.3 / 73.9 | 67.4 / 75.4 |
| learned, verbnet alone (lexical_verbnet the same) | 71.7 / 74.2 | 65.2 / 71.4 |
| cgel_tests2, fallback the relabelled table | **79.7 / 81.3** | **82.6 / 85.7** |
| cgel_tests2, fallback learned lexical | 79.0 / 81.3 | 78.3 / 84.4 |
| cgel_tests2, fallback learned verbnet | 79.7 / 81.6 | 73.9 / 78.6 |

On the PPs no CGEL test decides (95 gold, 30 trial), the fallback's
accuracy:

| fallback | gold | trial |
|---|---|---|
| MASC table or VerbNet (before) | 66.3 | 70.0 |
| relabelled table | 72.6 | 73.3 |
| learned lexical | 71.6 | 66.7 |
| learned verbnet | 72.6 | 60.0 |

**What this shows:**
* **Relabelling helps.** The relabelled table alone does as well as
  `cgel_tests2`. As its fallback, it lifts accuracy from 75.4 to 79.7 on
  gold (6 PPs) and from 80.4 to 82.6 on trial (1 PP). The improvement is
  in recall of complements, which is what MASC's labels lacked.
* **Learning adds no accuracy over the relabelled table.** On gold the
  learned theories tie the table as a fallback. On trial they are worse.
  The trial differences are a few PPs out of 30, so they are within noise.
* **What the learning gives instead is a readable theory.** It is 20 to 27
  clauses in place of a table of thousands of pairs. It also shows where
  the relabelled labels come from:
  * `prep(A,by), obj_cat(A,np), vtag(A,vbn)` is right 97.9% of 235 times.
    Aleph found the passive-agent test from MASC's LGS tags. It is the rule
    that `cgel_tests2` added by hand after looking at CGELBank's gold, so
    it is now supported independently of CGELBank.
  * `verb_sense(A,'verb.motion'), obj_cat(A,none)` (94.5% of 181) is the
    locative-with-motion test. `prep(A,to), vn_spatial(A)` (96.2% of 400)
    is the goal test.
  * The lexical theory's first clause, `next(A)` (72.3% of 3,355), says a
    PP straight after the verb is usually a complement. That is CGEL's
    observation that complements precede adjuncts.
* **The verbnet and lexical_verbnet theories are the same up to naming.**
  Seven clauses name a lemma in one where the other names its VerbNet
  class: `lemma(A,ask)` for `vn_class(A,'inquire-37.1.2')`, and so on. On
  CGELBank they decide identically.

## Running

```bash
cd tools/ilp
uv run python cgel/evaluate_cgel.py CGELBANK/datasets $S/pp_examples.tsv $S/nltk_data $S/cgel_eval.tsv
uv run python cgel/evaluate_cgel.py CGELBANK/datasets $S/pp_examples.tsv $S/nltk_data $S/cgel_trial.tsv --trial
```

The relabelling and learning:

```bash
cd tools/ilp
find $S/masc-prolog -name '*.pl' | sort \
  | swipl -q -g cgel_tsv -t 'halt(1)' ../masc/prolog/ptb.pl pp_examples.pl > $S/cgel_examples.tsv
uv run python cgel/relabel.py $S/cgel_examples.tsv $S/nltk_data $S/ilp_cgel
cd $S/ilp_cgel/verbnet && swipl -g "consult('aleph_orig.pl'), read_all(complement), induce, write_rules('theory.pl'), halt"
cd tools/ilp && swipl -q -g "evaluate('$S/ilp_cgel/verbnet/theory.pl', '$S/ilp_cgel/test.pl')" -t 'halt(1)' evaluate.pl
uv run python cgel/evaluate_cgel.py CGELBANK/datasets $S/pp_examples.tsv $S/nltk_data OUT.tsv [--trial] \
    --relabelled=$S/cgel_examples.tsv --theories=$S/ilp_cgel
```

`CGELBANK` is a clone of nert-nlp/cgel. `pp_examples.tsv` and `nltk_data`
are as for `../build.py`. The output TSV has one row per aligned PP: the
gold label, each source's decision, the deciding test, and the sentence.
