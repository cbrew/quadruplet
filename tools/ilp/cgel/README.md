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

### More room for Aleph

Two of Aleph's settings were loosened, one at a time and then together. The
settings are compared on the relabelled held-out MASC documents; CGELBank
was scored once, for all of them.
* **Clause length.** Four body literals are allowed, against three
  (`clauselength` 5).
* **Minimum coverage.** A clause may cover as few as 5 training positives,
  against 15 (`minpos` 5).

Each run takes 2 to 3 CPU minutes.

| background | settings | clauses | held-out MASC acc / F | CGELBank alone, gold / trial acc | as fallback, gold / trial acc |
|---|---|---|---|---|---|
| lexical | as before | 25 | 76.0 / 78.9 | 70.3 / 67.4 | 79.0 / 78.3 |
| lexical | 4 literals | 26 | 76.0 / 79.0 | 70.3 / 67.4 | 79.0 / 78.3 |
| lexical | minpos 5 | 95 | 77.3 / 80.6 | 70.3 / 73.9 | 79.0 / 78.3 |
| lexical | both | 110 | 77.4 / 80.7 | 71.0 / 69.6 | 79.0 / 76.1 |
| verbnet | as before | 27 | 85.7 / 86.8 | 71.7 / 65.2 | 79.7 / 73.9 |
| verbnet | 4 literals | 27 | 85.5 / 86.7 | 71.7 / 65.2 | 79.7 / 73.9 |
| verbnet | 4 literals, 50,000 nodes | 27 | 85.5 / 86.7 | – | – |
| verbnet | minpos 5 | 71 | 85.7 / 87.1 | 72.5 / 69.6 | 79.7 / 78.3 |
| verbnet | both | 79 | 85.5 / 86.9 | 72.5 / 69.6 | 79.7 / 78.3 |
| relabelled rate table | – | – | 81.4 / 82.9 | 76.1 / 82.6 | 79.7 / 82.6 |

"As fallback" means `cgel_tests2` with that source deciding the PPs no
test decides.

* **A fourth literal buys nothing.**
  * The new clauses mostly add `obj_cat(A,np)`, which narrows them by
    almost nothing, or swap a VerbNet class for a Levin group and a
    WordNet sense.
  * On CGELBank the theories decide exactly as before.
  * A search five times larger (50,000 nodes) finds the same theory, so
    the search was not cramped.
* **A lower minpos buys little.**
  * The theories grow three- to fourfold.
  * On held-out MASC, the lexical theory gains 1.3 points of accuracy;
    the verbnet theory gains nothing.
  * On CGELBank's trial files, the theories alone gain 3 PPs (lexical) and
    2 PPs (verbnet) of 46. As a fallback, the verbnet theory gains 2 PPs;
    on gold nothing changes. That is within noise.
* **The relabelled table is still as good as any theory.**
* **The limit is the features, not the search.**
  * The cases that remain are single verbs with specified prepositions
    that no class names: *decide between*, *renege on*, *check with*.
  * On held-out MASC the theories are at the table's level, which is
    near what the labels allow.

The minpos 5 theories are
`../theories/cgel_{lexical,verbnet}_minpos5.pl`.

### Version 2: the relabeller revised from CGEL ch. 4

With the chapter at hand (a 2009 revision draft of ch. 4; section numbers
are the draft's), the relabeller was checked against it.

**What the chapter confirms:**
* the passive's by-phrase is a complement (§1.2(b));
* *be* with a PP of place or time is a complement (§1.2(b));
* preposition + clause is predominantly an adjunct, except in copular
  clauses (§1.2(d));
* the motion test, *took the bed downstairs* against *slept downstairs*
  (§1.2(d));
* goals and sources of motion, state goals, and possessional source and
  goal are complements (§5.2).

It showed two divergences:

1. **Predicative *as*.** §6.1.2 has *count as*, *regard/use/see/describe …
   as* and *think of … as* taking a predicative complement. Version 1
   called 338 *as*-PPs Mod by default.
2. **Locatives of position.** A location is a complement only where the
   verb licenses it: *keep her car in the garage*, but not *washes her car
   in the garage* (§1.2(b–c)).
   * Version 1's VerbNet rule labelled 1,055 *in/on/at* PPs Comp.
   * Only 333 had a VerbNet role of place: *live in*, *include in*,
     *spend on*.
   * The rest came from frames for other senses. The NP's role was
     Attribute (*die in*, from a frame for *dropped in value*; *say in*,
     *appear in*), Theme (*work in*), Result (*go in*) or Topic (*show in*).

**Version 2 (`relabel.py`; `--v1` gives the first):**
* It adds the prepositional verbs of §6.1.2 as a test,
  `prepositional_verbs.tsv`: 187 pairs, with the book's citation. It also
  adds them as the background predicate `cgel_lex/1`.
* It counts VerbNet for *in/on/at* only where the frame gives the NP a role
  of place.
* These changes came from the chapter, after CGELBank's gold errors had
  been seen. The trial files had been seen only as totals, so they are the
  fair test.

**What the lexicon does.** It decides 1,431 MASC PPs, and MASC tags 996 of
them CLR or DTV. The labels change on 832 PPs:
* Mod to Comp, 160: *use as* 36, *refer to … as* 9, *act as* 8,
  *establish as* 8.
* Comp to Mod, 672: *work in* 47, *show in* 42, *say in* 32, *die in* 17.

The Comp-to-Mod changes cost some real complements that neither MASC nor
the §6.1.2 sample covers: *focus on* 32, *click on* 12, *work on* 10.

**Results.** Accuracy, gold / trial:

| source | version 1 | version 2 |
|---|---|---|
| cgel_tests2 (no lexicon) | 75.4 / 80.4 | – |
| cgel_tests3 (with the §6.1.2 test) | – | 76.1 / 80.4 |
| relabelled table alone | 76.1 / 82.6 | 76.1 / 78.3 |
| learned verbnet alone | 71.7 / 65.2 | 73.9 / 69.6 |
| learned lexical alone | 70.3 / 67.4 | 68.8 / 67.4 |
| cgel_tests2 + relabelled table | 79.7 / 82.6 | **81.9 / 82.6** |
| cgel_tests2 + learned verbnet | 79.7 / 73.9 | 81.2 / 78.3 |
| cgel_tests3 + learned verbnet | – | 81.2 / 78.3 |

* **The lexicon test is precise on UD.**
  * On gold it decides 10 PPs and is right on 9: *serve as*, *describe as*,
    *see to*, *consist of*, *help with* …
  * The miss is *call in for a look*: the verb has a particle, and *for a
    look* is a purpose adjunct. The lexicon ignores sense and particles.
  * On trial it decides 5 and is right on all 5 (*boast about*, *refer to*,
    *depend on* …). But the evidence had already got those right, so trial
    accuracy does not move.
* **The revised labels help on gold and hold on trial.**
  * As the fallback, the relabelled table goes from 79.7 to 81.9 on gold
    (3 PPs) and stays at 82.6 on trial.
  * Alone, on trial, it drops 2 PPs.
* **The learned verbnet theory improves most**, by 1.5 on gold and 4.4 on
  trial (2 PPs) as the fallback. It still does not beat the table.
* **The overall picture is unchanged.**
  * The best combination is still the CGEL tests with the relabelled rate
    table: 81.9 on gold, 82.6 on trial.
  * The differences between versions are 0 to 3 PPs, within noise.
  * The version 2 labels are the ones to prefer, because they follow the
    chapter, not because they score better.

The version 2 theories are
`../theories/cgel2_{classes,lexical,verbnet,lexical_verbnet}.pl`.

## Semantic roles: PropBank against CGELBank

CGEL ch. 4 §1.2(h) makes role a criterion: a complement's role depends on
the verb, while an adjunct's is its own, the same with any verb. PropBank
draws the same line, by other means:
* **Numbered arguments (ARG0–5)** are defined per roleset, that is, per
  verb sense.
* **Modifiers (ARGM-TMP, -LOC, -MNR, -PRP …)** carry their own meaning.

The PropBank release (github.com/propbank/propbank-release) annotates the
English Web Treebank, which is where CGELBank's EWT sentences come from.
`propbank_ewt.py` reads its `.gold_skel` files and aligns them with UD EWT
by sentence id and token position:
* 92% of UD EWT sentences match PropBank token for token, tags included;
* 7.7% match in length, with some tags revised since;
* 30 sentences differ in length.

`evaluate_cgel.py --propbank=DIR` gives each gold PP the label of the
argument of its verb that covers it. A numbered argument counts as Comp and
ARGM as Mod. Twitter and CGELBank's other non-EWT sentences have no PropBank.

| | gold | trial |
|---|---|---|
| PPs PropBank covers | 111 of 138 | 25 of 46 |
| on those: PropBank, ARGn = Comp | 82.0 | 80.0 |
| on those: PropBank, with ARGM-DIR/GOL as Comp | **87.4** | 84.0 |
| on those: cgel_tests2 + relabelled table | 79.3 | **88.0** |
| all: cgel_tests2 + relabelled table | 81.9 | **82.6** |
| all: cgel_tests2, then PropBank (DIR/GOL as Comp), then the table | **88.4** | 80.4 |

Accuracy is agreement with CGELBank. For scale, CGELBank's two annotators
agreed on 19 of 28 verb PPs (68%) before adjudication.

**How the labels line up.** On gold, PropBank's label against CGELBank's
function:
* ARG2 is Comp 25 times and Mod once.
* ARGM-TMP is Mod 18 times and Comp once.
* ARGM-ADV, -CAU and -PRP are always Mod.
* ARGM-LOC is Mod 10 times and Comp 3 times. The Comps are *kept in a run*
  and *land in a terminal*: locations the verb licenses.
* ARGM-DIR and ARGM-GOL are Comp all 6 times. These are goals, sources and
  directions of motion: *drive to Tacoma*, *come back out*, *come in*,
  *launch from*, *transition away*.

That last group is a known difference between the two traditions. PropBank
treats direction as a modifier for many motion verbs; CGEL §5.2 counts
goals and sources of motion as complements. Reading ARGM-DIR/GOL as Comp
follows the chapter, and it was expected before this comparison was run. It
was scored after the gold's disagreements had been seen, however, so its
figure on gold is not a clean test.

**The rest of the disagreements are divided:**
* **PropBank says argument, CGELBank says Mod:** *live in harmony*, *work
  with my insurance*, *cut from the budget*, *make art out of them*, *differ
  in the fact that*. Several of these are arguable.
* **PropBank says ARGM, CGELBank says Comp:** *check with* (ARGM-COM),
  *talk down to*, *start as a joke*.

**What this shows:**
* **On gold, a role-based source is the best single source yet.** On the
  PPs no CGEL test decides, it is right 80.3% of the time, against 72.4% for
  the relabelled table.
* **Used after the CGEL tests,** PropBank (with DIR/GOL as Comp) lifts gold
  accuracy to 88.4.
* **On trial it does not help.** It covers only 25 PPs, the tests and table
  are already right on 88% of them, and PropBank gets 1 fewer right. With
  25 PPs that is no evidence either way.
* **PropBank is gold annotation here.** On new text its labels would come
  from a semantic role labeller, which would be less accurate.

PropBank is another group's judgment, not CGEL's. Like MASC's function
tags, it is best treated as one more annotator of known biases. The biases
here are goals of motion and licensed locations.

The PropBank release also annotates part of MASC (`data/oanc/masc`, 97
documents), which is the corpus the relabelling and learning use. That
would put roles into the training data itself.

## Semantic roles in the training data: MASC's PropBank

The PropBank release also annotates MASC (`data/oanc/masc`).
* **Alignment.** 92 of its 97 documents are among our 391.
  * Its sentence k is our program `k.pl`.
  * Its tokens are our words, since it has no empty elements.
  * 4,439 sentences match ours tag for tag; 379 more match in length.
  * `pp_examples.pl`'s `cgel_tsv` now also writes each row's verb position
    and PP span, so a MASC PP can be looked up in PropBank.
* **Coverage.** PropBank covers 3,720 of the 26,510 PPs (14%).

**Roles in CGEL's terms** (`propbank_roles.py`).
* **Numbered arguments** take their role from the roleset's frame file
  (github.com/propbank/propbank-frames). The VerbNet role it links to
  (*give.01* ARG2 → recipient) is used where there is one, else PropBank's
  function tag (GOL → goal, PAG → causer, PPT → theme).
* **Modifiers** take the role their label names: time, location, manner,
  purpose, reason …
* **The vocabulary is CGEL §2.2's.** Following §2.1, each PP gets its role
  at every level of a small hierarchy (`agent < causer`, `recipient <
  goal`, `goal, source, path, location < place`), and the learner picks the
  level.
* Two frame files (`rend.xml`, `check.xml`) are not well-formed XML and are
  skipped.

**Relabelling, version 3** (`relabel.py --propbank=… --frames=…
--programs=…`). Where PropBank covers a PP, its label decides, read as CGEL
would: a numbered argument, ARGM-DIR or ARGM-GOL is Comp. It comes after
the CGEL tests and before the other evidence, and decides 2,268 PPs.

**The experiment.** Aleph learns on the 3,720 covered PPs (2,362 training,
1,358 held out, split by document), with and without `role/2` facts. The
background and settings are otherwise as in version 2. The theories are
`../theories/cgel3_roles.pl` and `../theories/cgel3_noroles.pl`.

With roles, all four backgrounds learn the same 10 clauses:

```
 1  72.4% of 764  next(A)
 2  99.2% of 240  role(A,theme)
 3  92.9% of  28  role(A,attribute)
 4 100.0% of  23  role(A,source)
 5  57.1% of  14  obj_cat(A,np), obj_before(A), role(A,predicative)
 6 100.0% of 166  role(A,goal)
 7 100.0% of  28  role(A,path)
 8  92.0% of 112  cgel_lex(A)
 9 100.0% of   9  role(A,patient)
10 100.0% of  82  role(A,causer)
```

This reads as CGEL's criterion (h):
* **Roles the verb assigns make complements:** theme, patient, causer
  (which covers the passive's agent), goal, source, path.
* **Roles a phrase carries by itself never appear:** time, manner,
  purpose, reason.
* **Aleph chose the level of generality.** It took goal, source and path
  separately, never their parent `place`, because location, the fourth
  member, goes both ways. That is CGEL's own split: goals and sources of
  motion are licensed by the verb, while a location is licensed only by
  some verbs (§1.2, §5.2).
* **Some of this is circular.** Where PropBank decides the label, numbered
  arguments are Comp and their roles come from the frame files, so
  `role(A,theme)` at 99% partly restates the labelling.

Without roles, the theory is 6 clauses of prepositions and position
(`prep(A,to)`, `obj_before(A), vn_prep(A)` …).

On held-out MASC (1,358 PPs):

| | accuracy | Comp F |
|---|---|---|
| rate table | 77.9 | 81.1 |
| learned, without roles | 75.9 | 79.9 |
| learned, with roles | **80.6** | **84.1** |

On CGELBank, a PP's roles come from PropBank's annotation of EWT, through
the same frame files. They exist only for the PPs PropBank covers. Accuracy,
gold / trial:

| | gold, 111 covered | trial, 25 covered | gold, all 138 | trial, all 46 |
|---|---|---|---|---|
| learned, without roles | 64.9 | 68.0 | 69.6 | 67.4 |
| learned, with roles | **73.9** | **84.0** | **76.8** | **73.9** |
| PropBank read directly, DIR/GOL as Comp | 87.4 | 84.0 | – | – |
| cgel_tests2 + relabelled table | 79.3 | 88.0 | 81.9 | 82.6 |
| cgel_tests2 + learned with roles | 79.3 | 84.0 | 81.2 | 78.3 |

**What this shows:**
* **Roles help the learner, on both CGELBank sets.** On the covered PPs the
  role theory gets 10 more right than the theory without roles on gold, and
  4 more on trial. This is the first learned source whose gain holds on
  both.
* **The learned rules are coarser than reading PropBank directly.**
  PropBank's ARGn/ARGM split, with DIR/GOL as Comp, is 87.4 on gold. The
  rules generalise over roles, and for a location they cannot tell an
  argument from a modifier.
* **Overall, the CGEL tests with the relabelled table are still best on
  trial.** A role theory needs roles: gold PropBank here, a semantic role
  labeller on new text.
* **The training set is small:** 2,362 PPs from 92 documents.

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

`CGELBANK` is a clone of nert-nlp/cgel. For the PropBank comparison, add
`--propbank=PROPBANK/data/google/ewt`, where `PROPBANK` is a clone of
propbank/propbank-release. For roles, add `--frames=FRAMES`, a clone of
propbank/propbank-frames; for version 3 relabelling, `relabel.py
--propbank=PROPBANK/data/oanc/masc --frames=FRAMES --programs=$S/masc-prolog`
(on the TSV `cgel_tsv` now writes, with verb positions and spans). `pp_examples.tsv` and `nltk_data`
are as for `../build.py`. The output TSV has one row per aligned PP: the
gold label, each source's decision, the deciding test, and the sentence.
