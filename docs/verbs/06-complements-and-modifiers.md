# Complements and modifiers in MASC: how promiscuous is each?

In MASC the verb predicts its complements far better than its modifiers.
After a bias correction, the verb lemma removes 38% of the uncertainty about
its complement frame but only 10% about its set of modifiers, and 7% once
modifiers above the lexical verb phrase are counted. This holds with and
without empty elements, at matched granularity, and in spoken and written
genres alike. It does not hold in two other senses:
- within a single verb, complement frames are about as varied as modifier
  sets;
- promiscuity is a property of individual types, not of the two classes.
  The plain NP object is spread across verbs about as widely as LOC or MNR,
  while DIR and EXT modifiers are as selective as complements.

Roughly one verb-phrase daughter in six has a doubtful complement/modifier
status in the annotation.

## Question

Are complements much less promiscuous than modifiers? The question is asked
two ways:
- **per type**: over how many verb lemmas, and how evenly, each complement
  and modifier type is spread;
- **per verb**: how varied its complement frames are compared with its
  modifier sets, and how much the verb tells us about each (H(Y|verb)
  against H(Y), i.e. mutual information).

Also: how much do the empty elements matter, how often is the assignment
itself doubtful, and how do spoken and written genres differ?

## Data and method

The analysis script is `tools/masc/verbs/complements_modifiers.py`. It uses
only the standard library and imports `masctrees.py` and `verbframes.py`
unchanged.

    D=/tmp/claude-0/-home-user/32748c59-1ad9-5140-855d-e9a1edeaf50a/scratchpad
    python3 tools/masc/verbs/complements_modifiers.py $D/masc/data     # all tables, 20 s
    python3 tools/masc/verbs/complements_modifiers.py $D/masc/data --lemma send
    python3 tools/masc/verbs/complements_modifiers.py $D/masc/data --show 'ficlets/1401#79' 7

The script re-reads the raw trees with `verbframes.verb_record` and gets the
same 70,101 records as `verbs.jsonl`. It also keeps each verb-phrase node, so
it can read what the records leave out:
- the preposition of each PP;
- the subject and predicate inside each S complement;
- an NP-LGS inside a PP;
- the modifiers above the lexical VP.

**Lemmas.** Lemmatization works as follows:
1. The word is lower-cased.
2. It is looked up in a table of about 150 irregular forms. For VB/VBP the
   table is used only for be/have/do, so base-form *lay*, *saw* and *found*
   stay as they are. *'s*, *'re*, *'m* and the mangled *RSQUOs*
   (`face-to-face/interview_nathan_hole`) map to *be*.
3. Otherwise the -s/-ies/-ed/-ied/-ing ending is stripped, and doubling and
   e-restoration give candidates.
4. The candidate most often tagged VB/VBP in MASC is chosen; failing that,
   one attested as a VBZ stem; failing that, a suffix heuristic.

The result is 3,209 lemmas. 457 lemmas have at least 20 tokens (60,224
tokens, 85.9%); 102 have at least 100. For 2.5% of tokens the lemma is
unattested as a base form, e.g. *incubate*, *kid*, *devot*, *jok*,
*proven*. A token-weighted sample of 150 tokens had no errors. Known
errors:
- *found* conflates *find* and *found*;
- lexical *'s* is always *be*;
- `=` (VBZ, 24 tokens) is a lemma.

**Units of analysis.**
- **Complement frame**: the non-subject part of `frame_full` (or of
  `frame_backbone`), written `0` when empty. It has 83 element types.
- **Coarse complement types** (12): NP, PRD, CLR, DTV/PUT, S, SBAR, PRT,
  their empty variants, and other.
- **Modifier type**: the first semantic tag (TMP LOC MNR PRP DIR ADV EXT
  BNF VOC), or else u+category (uPP, uADVP, uRB).
- **Modifier set**: a verb's modifier types as a sorted multiset. "Full"
  includes empty modifiers. "Incl. above VP" adds the modifiers in the
  auxiliary VPs above the verb and the non-subject modifier daughters of
  its clause.

**Measures.**
- **U** = (I(verb;Y) − I after shuffling Y) / H(Y), with the shuffle
  averaged over 5 runs. It is the share of Y's entropy that the verb
  removes, corrected for estimation bias.
- **Type spread**: the number of lemmas a type occurs with, against the
  number expected under independence, Σ_v 1−(1−p_v)^n; the number of
  lemmas covering 80% of its tokens; and KL(P(verb|type) ‖ P(verb)) in
  bits. KL = 0 means the type is spread like verbs in general.
- **Per-verb entropy** is rarefied to 100 tokens (mean of 50 subsamples).

**Genres.** Spoken covers the four genres under `data/spoken`:
court-transcript, debate-transcript, face-to-face and telephone (15,151
verbs). Written covers the 20 genre ids under `data/written` (54,950 verbs),
including movie-script and 46 verbs in `newspaper/unknown`. Court and debate
come from two files each.

## Counts

**Table 1. What the verb tells us (lemmas ≥ 20, 60,224 tokens).**

| Y | values | H(Y) | H(Y\|verb) | I | shuffled I | U |
|---|---|---|---|---|---|---|
| complement frame, full | 276 | 4.03 | 2.23 | 1.80 | 0.26 | **0.383** |
| complement frame, no empty elements | 145 | 3.46 | 1.96 | 1.50 | 0.17 | **0.382** |
| complement frame, categories only (no tags, no empties) | 83 | 2.96 | 1.92 | 1.05 | 0.12 | 0.313 |
| modifier set, full | 256 | 2.41 | 1.96 | 0.45 | 0.20 | **0.103** |
| modifier set, no empty elements | 240 | 2.22 | 1.80 | 0.43 | 0.19 | 0.107 |
| modifier set incl. above VP | 500 | 3.42 | 2.83 | 0.58 | 0.34 | 0.073 |
| one complement (per daughter, n=58,334), 83 types | 83 | 3.33 | 1.90 | 1.43 | 0.14 | 0.389 |
| one complement, 12 coarse types | 12 | 2.64 | 1.33 | 1.31 | 0.05 | **0.477** |
| one modifier (per daughter, n=25,174), 13 types | 13 | 3.06 | 2.50 | 0.56 | 0.13 | **0.139** |
| one modifier, tag+category, 74 types | 74 | 4.40 | 3.50 | 0.90 | 0.39 | 0.115 |

U for frames and modifier sets under other selections:

| selection | frames | modifier sets |
|---|---|---|
| without *be* | 0.340 | 0.102 |
| without be/have/do/get/go/make | 0.343 | 0.106 |
| lemmas ≥ 100 only | 0.386 | 0.091 |

**Table 2. U, spoken vs written.** The last column is a written sample of
the spoken size, because the shuffle correction depends on n.

| Y | spoken (14,115) | written (46,109) | written sample |
|---|---|---|---|
| complement frame, full | 0.370 | 0.378 | 0.337 |
| complement frame, categories only | 0.306 | 0.311 | 0.279 |
| modifier set, full / incl. above VP | 0.112 / 0.078 | 0.101 / 0.072 | 0.085 / 0.062 |
| one complement (coarse) / one modifier | 0.507 / 0.130 | 0.467 / 0.141 | 0.446 / 0.114 |

**Table 3. Spread of types over the 457 lemmas (selected rows, by KL).**

| type | tokens | lemmas | expected | lemmas for 80% | KL | top lemmas |
|---|---|---|---|---|---|---|
| M:TMP | 5351 | 403 | 436 | 114 | 0.31 | be, have, go, get |
| M:uPP | 4976 | 427 | 433 | 171 | 0.50 | be, go, make, have |
| M:ADV | 1762 | 326 | 339 | 109 | 0.50 | be, go, have, say |
| M:PRP | 2557 | 331 | 380 | 87 | 0.55 | be, go, do, have |
| M:LOC | 3152 | 357 | 400 | 118 | 0.71 | be, find, see, get |
| M:MNR | 2679 | 389 | 385 | 153 | 0.72 | do, be, work, make |
| **C:NP** | 19487 | 430 | 457 | 142 | **0.76** | have, give, take, do |
| C:NP(\*T\*) | 1970 | 247 | 352 | 61 | 1.22 | do, say, have |
| C:S | 6425 | 199 | 444 | 32 | 1.40 | have, want, go, make |
| C:PP-CLR | 4797 | 305 | 431 | 78 | 1.47 | look, talk, think, put |
| C:NP(\*) | 3885 | 347 | 417 | 151 | 1.49 | use, call, base, make |
| C:SBAR | 4909 | 221 | 432 | 31 | 1.72 | think, know, say |
| M:uRB | 955 | 48 | 263 | 1 | 1.76 | be 809 (*not*) |
| C:NP-PRD | 5129 | 27 | 434 | 1 | 2.21 | be 4884, become |
| C:PRT | 2172 | 172 | 363 | 52 | 2.10 | come, go, take, pick |
| **M:DIR** | 1954 | 140 | 351 | 29 | **2.78** | go, come, walk, move |
| **M:EXT** | 174 | 56 | 92 | 27 | **3.85** | increase, rise, reduce |
| C:PP-DTV | 236 | 28 | 114 | 10 | 4.42 | give, send, bring |

**Table 4. The ten most frame-promiscuous and most frame-faithful verbs
(≥ 100 tokens).** Entropies are rarefied to 100 tokens. "H" is the
complement frame (full), "bb" the frame without empty elements, "mod" the
modifier set.

| verb | n | H | bb | mod | top frames (full) |
|---|---|---|---|---|---|
| ask | 433 | 4.10 | 3.18 | 1.47 | NP S 52; NP 46; PP-CLR 46; SBAR 33; SBARQ(\*T\*) 33 |
| call | 329 | 3.43 | 2.74 | 2.12 | NP 96; S 53; NP(\*) S 32; NP(\*) S-CLR 28 |
| set | 173 | 3.41 | 3.10 | 2.40 | NP 46; PRT NP 34; NP PP-CLR 15; PRT 12 |
| bring | 181 | 3.40 | 3.15 | 3.01 | NP 63; PRT NP 17; NP PRT 12; NP PP-DTV 11; NP NP 11 |
| pass | 122 | 3.22 | 2.70 | 2.69 | NP 43; 0 26; PRT NP 7 |
| write | 255 | 3.16 | 2.50 | 2.28 | NP 92; 0 33; NP(\*) 27; NP(\*T\*) 19 |
| pay | 157 | 3.14 | 2.54 | 2.50 | NP 60; PP-CLR 27; 0 16 |
| say | 1589 | 3.13 | 2.24 | 1.07 | SBAR 466; S 216; S(\*T\*) 201; 0 185; NP 154 |
| put | 251 | 3.12 | 2.88 | 1.96 | NP PP-CLR 111; PRT NP 21; NP(\*) PP-CLR 20 |
| turn | 236 | 3.11 | 2.88 | 2.56 | 0 93; PP-CLR 25; NP PP-CLR 22; PRT NP 17 |
| let | 340 | 0.53 | 0.50 | 0.60 | S 314; NP 8 |
| thank | 150 | 0.55 | 0.55 | 2.16 | NP 135; NP PP-CLR 11 |
| die | 101 | 0.82 | 0.82 | 2.92 | 0 87; PRT 6; PP-CLR 5 |
| support | 144 | 0.86 | 0.74 | 1.79 | NP 121; NP(\*) 14 |
| talk | 243 | 0.90 | 0.92 | 1.54 | PP-CLR 201; 0 26 |
| want | 613 | 1.05 | 0.99 | 0.72 | S 478; NP 75; NP(\*T\*) 48 |
| walk | 198 | 1.08 | 1.08 | 3.37 | 0 148; PRT 35; NP 12 |
| include | 166 | 1.13 | 0.99 | 1.33 | NP 132; NP(\*) 20 |
| save | 117 | 1.14 | 1.00 | 1.63 | NP 95; NP NP 6 |
| happen | 172 | 1.15 | 1.13 | 2.13 | 0 130; PP-CLR 19; S 18 |

Over all 102 verbs, the mean rarefied entropy is 2.12 for complement frames,
1.87 for frames without empties, and 1.98 for modifier sets.
- 42 of the 102 verbs have more varied modifiers than complements.
- r(complement H, modifier H) = 0.04; r(complement H, log frequency) = 0.16.
- The most modifier-varied verbs are *move* 3.85, *return* 3.84, *run*,
  *spend*, *come*, *walk*, *send* and *work*.
- The least modifier-varied, with no modifier in 87–93% of tokens, are
  *hope* 0.51, *believe*, *mean*, *let*, *want*, *think*, *suggest* and
  *like*.

**Table 5. Empty and unrecorded modifiers.**

| | total | empty | commonest empty kinds |
|---|---|---|---|
| complements | 66,718 | 9,083 (13.6%) | NP \* 5,640; NP \*T\* 2,307; S \*T\* 353 |
| VP-internal modifiers | 31,212 | 2,982 (9.6%) | TMP \*T\* 1,211 (19.3% of TMP); MNR 690 (20.1%); LOC 459 (11.8%); PRP 351 (11.4%) |

- Removing empty elements changes the complement frame for 12.9% of verbs
  and the modifier set for 4.2%.
- A further 17,213 modifiers lie above the lexical VP:
  - 11,457 at clause level: TMP 3,465, uADVP 2,823, ADV 1,610, uPP 1,334,
    VOC 553;
  - 5,756 in auxiliary VPs: uRB 2,697, uADVP 1,172, TMP 1,124.
- `verbs.jsonl` therefore records 64% of the modifiers in the verbs'
  clauses.
- 36% of verbs have a VP-internal modifier; 50% have one when those above
  the VP are counted.

**Table 6. Role by category, all VP daughters (verbframes.py's
classification).**

| category | n | compl. % | modif. % | overt tags (top) |
|---|---|---|---|---|
| NP | 39,079 | 95.8 | 4.2 | none 24,004; PRD 5,109; TMP 806; ADV 178 |
| PP | 23,097 | 30.1 | 69.9 | none 6,673; CLR 5,033; LOC 2,900; TMP 1,899; DIR 1,739 |
| S | 10,210 | 76.6 | 23.4 | none 6,439; ADV 1,302; PRP 1,072; CLR 442 |
| SBAR | 8,393 | 70.1 | 29.9 | none 4,979; TMP 1,052; ADV 680; PRP 610 |
| ADVP | 8,161 | 8.4 | 91.6 | none 1,637; TMP 1,290; MNR 1,038; DIR 623 |
| ADJP | 4,777 | 99.3 | 0.7 | PRD 4,597 |

**Table 7. Doubtful assignments, out of 97,930 complement and modifier
daughters of lexical VPs.**

| class | n | % |
|---|---|---|
| untagged overt PP (a modifier by default) | 6,673 | 6.8 |
| of which a *by*-PP containing NP-LGS (passive agent) | 974 | 1.0 |
| any -CLR daughter (a complement) | 6,157 | 6.3 |
| S complement with an overt subject | 1,996 | 2.0 |
| RB *not*/*n't* as a modifier | 859 | 0.9 |
| -DIR modifier (verb-selective, Table 3) | 2,471 | 2.5 |
| -DTV or -PUT PP (PUT: 5 in all MASC) | 290 | 0.3 |

**(Verb, preposition) consistency.** 399 pairs have at least 10 PP
daughters, 10,920 PPs in all.
- In 49 pairs (8.2% of those PPs), CLR and untagged each occur at least
  20% of the time.
- In 132 pairs (40.9%), the share of complement tags lies between 20% and
  80%.
- The majority tag of each pair reproduces 73.0% of the tags.

**S complements.** There are 7,411 overt S complements. Their subject is
\*PRO\* in 3,199, \* in 2,043 and overt in 1,996. By verb, the share with
an overt subject is:
- *see* 88% of 145, *hear* 93% of 44, *watch* 97% of 36;
- *make* 88% of 352 (ADJP small clause 166, bare VP 78);
- *let* 99% of 315;
- *have* 34% of 573 (346 are *have to*), *help* 36% of 248;
- *want* 17% of 478, *expect* 47% of 55;
- *go* 1% of 414 (393 are *going to*).

**Table 8. Modifier types per 100 verbs by genre** (lexical VP only, overt
and empty; "all" = all types; "above" = modifiers above the VP per 100
verbs).

| genre | verbs | none % | TMP | LOC | DIR | MNR | PRP | ADV | uPP | all |
|---|---|---|---|---|---|---|---|---|---|---|
| court-transcript | 4704 | 70.1 | 8.5 | 5.1 | 0.4 | 3.6 | 4.5 | 2.0 | 5.8 | 35.5 |
| debate-transcript | 5589 | 69.3 | 6.9 | 4.2 | 1.1 | 5.0 | 4.8 | 1.8 | 8.5 | 36.8 |
| face-to-face | 3689 | 70.9 | 7.4 | 3.5 | 4.7 | 3.4 | 3.9 | 1.5 | 5.6 | 35.2 |
| telephone | 1169 | 74.6 | 7.7 | 3.9 | 4.0 | 2.1 | 2.4 | 1.3 | 0.9 | 29.0 |
| fiction | 5518 | 61.9 | 9.4 | 7.4 | 6.9 | 5.2 | 3.1 | 5.2 | 6.1 | 47.9 |
| movie-script | 3715 | 61.1 | 5.9 | 7.1 | **11.5** | 4.0 | 3.9 | 4.3 | 7.2 | 47.9 |
| travel-guides | 2513 | 55.2 | **12.9** | **12.0** | 5.8 | 3.9 | 4.3 | 3.4 | 11.1 | 56.7 |
| technical | 1790 | 50.7 | 11.3 | 9.9 | 0.4 | 5.1 | 4.4 | 5.0 | **24.5** | 65.4 |
| solicitation-brochures | 1617 | 62.8 | 9.2 | 6.2 | 1.5 | 5.2 | **10.1** | 2.2 | 10.3 | 47.3 |
| enron | 1603 | 60.1 | **14.4** | 4.7 | 1.6 | 4.7 | 5.6 | 2.1 | 13.3 | 50.0 |
| **SPOKEN** (above: 24.0) | 15,151 | 70.3 | 7.6 | 4.3 | 2.0 | 4.0 | 4.3 | 1.7 | 6.4 | 35.4 |
| **WRITTEN** (above: 24.7) | 54,950 | 62.3 | 9.3 | 6.0 | 3.9 | 5.2 | 4.5 | 3.5 | 10.6 | 47.0 |

The script prints all 24 genres. With modifiers above the VP included,
spoken has TMP 12.5, uADVP 9.9, uPP 7.9, uRB 7.6, MNR 4.8, LOC 4.7 per 100
verbs; written has TMP 16.3, uPP 12.8, uADVP 7.6, LOC 6.8, MNR 6.7,
ADV 6.0.

## Examples

Trees are trimmed from `--show`. F = complement part of `frame_full`,
B = of `frame_backbone`, M = modifiers.

1. `ficlets/1401#79` *opened*: `(NP-SBJ-1 the door) (VP was (VP opened (NP *-1) (PP-PRP for us) (PP by (NP-LGS Noon himself))))`.
   F `NP(*)`, B `0`; M PRP uPP. The backbone makes this the inchoative in
   (2), and the agent counts as a modifier.
2. `fiction/Nathans_Bylichka#839` *opened*: `(NP-SBJ my eyes) (VP opened)`.
   F `0`. A true inchoative.
3. `debate-transcript/2nd_Gore-Bush#297` *sent*: `(VP sent (NP troops) (PP-DIR to Haiti))`.
   F `NP`; M DIR.
4. `face-to-face/Bmr021#184` *send*: `(VP send (NP the data) (PP-DTV to Sony))`.
   F `NP PP-DTV`. Read with (3): the same pattern is complement or modifier
   depending on whether the *to*-phrase is taken as goal or recipient.
5. `debate-transcript/3rd_Bush-Kerry#836` *work*: `(VP work (PP-CLR with allies))`. F `PP-CLR`.
6. `debate-transcript/2nd_Gore-Bush#286` *work*: `(VP to (VP work (PP with Nigeria)))`.
   F `0`; M uPP. Same sense as (5), opposite classification.
7. `debate-transcript/2nd_Gore-Bush#392` *seen*: `(VP seen (S (NP-SBJ them) (VP make (NP some calls ...))))`.
   F `S`. A perception small clause: the perceived entity is hidden inside
   S.
8. `debate-transcript/2nd_Gore-Bush#53` *keep*: `(VP keep (NP-2 our military) (S-CLR (NP-SBJ *PRO*-2) (ADJP-PRD strong)))`.
   F `NP S-CLR`. The secondary predicate is a complement via -CLR and
   object control.
9. `court-transcript/Day3PMSession#1327` *ask*: `(VP ask (NP-2 you) (S (NP-SBJ *PRO*-2) (VP to (VP read ...))))`.
   F `NP S`. Object control; *ask* needs 13 frames to cover 80% of its
   tokens.
10. `court-transcript/Day3PMSession#353` *got*: `(VP got (NP this job) (ADVP-TMP *T*-2))`, in *when I got this job*.
    M TMP (empty). The backbone loses the relation between *when* and
    *got*.
11. `court-transcript/Day3PMSession#340` *leaving*: `(VP leaving (NP-TMP tomorrow))`.
    F `0`; M TMP. Without tags this is `VP → VBG NP`, which looks
    transitive.
12. `court-transcript/Day3PMSession#83` *'s*: `(VP 's (RB not) (NP-PRD my intention))`.
    M uRB. Negation is a "modifier" only because copular *be* is the
    lexical verb. Elsewhere *not* sits in the auxiliary VP, e.g. *n't* in
    (3).
13. `debate-transcript/3rd_Bush-Kerry#265` *increased*: `(VP increased (NP Pell Grants) (PP-EXT by a million students))`.
    M EXT. The difference argument of a degree verb.
14. `debate-transcript/2nd_Gore-Bush#86` *put*: `(VP put (NP our troops) (PP-LOC-CLR all around the world))`.
    F `NP PP-CLR`. MASC uses CLR, not PUT. Of *put*'s 199 PP/ADVP
    daughters tagged CLR, LOC or untagged, 32 are LOC or untagged, i.e.
    modifiers.
15. `movie-script/JurassicParkIV-Scene_3#120` *place*: `(VP place (NP you) (PP under arrest))`.
    F `NP`; M uPP. An argument PP left untagged. *place*'s PPs are split:
    untagged 15, LOC 12, LOC-CLR 11, CLR 9.
16. `court-transcript/Day3PMSession#280`: `(VP (VBZ clarifies) (CC and) (VBZ reasserts) (SBAR what I have ...))`.
    *reasserts* gets no record, and *clarifies* is not flagged as
    coordinated.
17. `debate-transcript/2nd_Gore-Bush#883` *need*: `(VP need (NP gas pipelines) (S-PRP (NP-SBJ *PRO*-1) (VP to bring the gas down)))`.
    M PRP. The purpose belongs to the thing needed, not to an agent.

## Findings

1. **In the information-theoretic sense the claim holds, robustly.**
   - The verb removes 38% of the bias-corrected entropy of its complement
     frame and 10% of that of its modifier set (Table 1).
   - At matched granularity the figures are 48% vs 14% (12 vs 13 types)
     and 39% vs 12% (83 vs 74 types).
   - The gap survives excluding *be* or the six commonest verbs, and
     restricting to lemmas ≥ 100.
   - With modifiers above the VP counted, the modifier figure falls to 7%.
2. **Within a verb, complements are not less varied than modifiers.**
   - Mean rarefied entropies are 2.12 bits (frames) and 1.98 bits
     (modifier sets); 42 of 102 verbs have the more varied modifiers.
   - The two entropies are uncorrelated (r = 0.04).
   - What distinguishes complements is that *which* frames vary depends on
     the verb. For modifiers it hardly does.
3. **Promiscuity belongs to types** (Table 3).
   - The NP object meets 430 of 457 lemmas, 94% of the number expected
     under independence, with KL 0.76. That is close to LOC (0.71) and MNR
     (0.72); TMP, uPP, ADV and PRP are 0.31–0.55.
   - PRD, SBAR, S, PRT, CLR and DTV complements are selective (1.4–4.4
     bits).
   - DIR (2.78: *go*, *come*, *walk*, *move*) and EXT (3.85: *increase*,
     *rise*, *reduce*) are modifiers as selective as complements.
   - uRB is selective only through the artefact of example 12.
4. **Frame-faithfulness depends on the tagging convention.**
   - *walk* is frame-faithful (`0` in 148 of 198 tokens) but
     modifier-promiscuous, because its paths are DIR; *talk* is faithful
     because its PPs are CLR.
   - The promiscuous verbs fall into three groups:
     - many CLR, particle and dative options: *bring*, *set*, *put*, *pay*;
     - control or small-clause complements: *ask*, *call*, *keep*,
       *leave*;
     - frames split by empty elements: *say* (S vs S(\*T\*) quotations),
       *write* and *call* (passives).
5. **Empty elements add verb-specific distinctions at the verb's own rate.**
   - Removing them changes the complement frame for 12.9% of verbs and
     lowers H(frame) from 4.03 to 3.46, but leaves U unchanged (0.383 vs
     0.382). Passive and extraction are as predictable from the verb as
     the rest of the frame.
   - NP(\*) concentrates on *use*, *call*, *base* and *make*; NP(\*T\*) on
     *do* and *say*.
   - The verbs most changed are *describe* (46%), *require* (41%), *build*,
     *do*, *say* and *call*.
   - Empty modifiers are the \*T\* of *when*, *how*, *where* and *why*,
     about a fifth of all TMP and MNR modifiers. The backbone loses them
     (example 10).
6. **Function tags carry much of the verb-specific information.**
   - With categories only, U for frames falls from 0.38 to 0.31.
   - Category is a poor guide to role for PP (30% complement), SBAR (70%)
     and S (77%), and a good one for NP (96%), ADJP and PRT (Table 6).
7. **About one VP daughter in six has a doubtful role** (Table 7).
   - Untagged PPs, CLR daughters, overt-subject S complements and negation
     make up 16.0% of daughters; adding DIR gives 18.5%.
   - Passive agents are 974 of the 6,673 untagged PPs (15%).
   - (Verb, preposition) fixes the tag for only 73% of PPs: *work with* 33
     CLR vs 29 untagged, *focus on* 12 vs 31, *describe as* 21 vs 7,
     *attribute to* 4 vs 12.
   - PUT is practically unused.
   - Some of this reflects real ambiguity: comitative vs collaborative
     *with*; goal vs recipient *to*, where *send* has DIR 26 and DTV 30.
     Some is annotator variation (examples 5 and 6).
8. **Levin and Dowty, where the data allow.**
   - **Causative/inchoative** (Levin 1993 §1.1.2.1, from memory). "No NP"
     below means no NP complement; S and PP complements are allowed.
     - *break*, *open*, *close* and *change* are 27–40% no-NP and 55–65%
       transitive.
     - The unaccusatives *arrive*, *die*, *happen* and *come* are 95–100%
       no-NP; *take* and *buy* are 4–13%.
     - The backbone merges passive with inchoative. Among no-NP tokens of
       the backbone, passives are 8% for *break*, 18% for *open*, 22% for
       *close*, 20% for *change*, 47% for *develop* and 85% for *fill*.
   - **Dative.**
     - *give*: NP NP 253 against NP PP(*to*/*for*) 66 DTV + 9 CLR + 4
       untagged, plus 11 PRP/BNF/TMP.
     - *tell*: NP NP 55 against 1.
     - *send*: split, as in Finding 7.
   - **Spray/load** is untestable: *load* 17, *spray* 4, *pour* 12 tokens,
     and no PUT.
   - **Dowty's proto-roles** (Dowty 1991) are not annotated, but two
     observations bear on them:
     - The clearest Proto-Agent outside the subject, the by-phrase, is
       classified as a modifier.
     - PRP+MNR modifiers, classic agentivity diagnostics (Lakoff 1966,
       from memory), are rarest with statives and attitude verbs: *agree*
       and *suggest* 1.5 per 100, *seem* 2.2, *mean* 2.3, *believe* 3.2,
       *be* and *think* 3.5. They are commonest with activities: *work*
       38.6, *send* 27.9, *pay* 23.6, *move* 22.6, *use* 21.8.
     - The exceptions (*need* PRP 14.6, example 17; *thank* MNR from *so
       much*) make this only a rough fit.
9. **Spoken vs written.**
   - Spoken verbs carry fewer VP-internal modifiers: 35.4 vs 47.0 per 100
     verbs, with 70% vs 62% unmodified. Above the VP the two are equal
     (24.0 vs 24.7).
   - TMP is the commonest semantic type in every genre except movie-script
     (DIR, stage directions), non-fiction (LOC, MNR) and
     solicitation-brochures (PRP; the commonest is *to help ...*, 16 of
     162).
   - Untagged PPs lead in expository writing: technical 24.5, essays 18.3,
     govt-docs 15.6. Passive agents are 14–22% of these.
   - LOC and TMP peak in travel-guides; ADV in fiction and ficlets, where
     81% of ADV are S-ADV clauses. Above the VP, spoken has more uADVP and
     negation.
   - Verb predictability is similar in the two modes, slightly higher in
     spoken at equal n (Table 2). This is a small effect from one random
     sample.

## What it means

**(a) The context-free backbone and the fast parser.**
- Without tags, the backbone's categories do not separate complements from
  modifiers for PPs and clauses. NP-TMP looks like an object (example 11).
- Removing traces merges passives into intransitives, hiding the causative
  alternation.
- The data favour conditioning complements on the head verb and generating
  modifiers largely independently of it, as Collins's Model 2 does with its
  complement marking and subcategorization frames (Collins 1999, from
  memory).
- The bare backbone gives no complement/modifier signal to condition on
  beyond the learned function-tag table (`docs/flat-semantics.md`, CLR F1
  45.6).
- The doubtful PPs of Table 7 cap what any model can learn of the
  distinction from MASC.

**(b) Verb frames and the complement/modifier distinction.**
- The PTB tags draw a line that correlates well with selection: U ≈
  0.4–0.5 for complements, ≈ 0.1 for modifiers.
- There are systematic exceptions. DIR with motion verbs and EXT with degree
  verbs behave like complements. The plain NP object is spread across verbs
  almost as widely as TMP or LOC.
- Frame inventories inherit annotation choices: CLR vs untagged, unused
  PUT, DIR vs DTV. Where tags are inconsistent, they record annotator
  decisions as much as lexical facts.
- Modifiers are nearly verb-independent in distribution, but they are the
  part that varies most by genre (Table 8). That fits the owner's point that
  they carry communicative function.

**(c) The flat neo-Davidsonian semantics.**
- Modifiers' near-independence of the verb supports treating them as
  independent conjuncts on the event variable (Davidson/Parsons). Nothing is
  lost by keeping them out of verb entries.
- Complements are where lexical entries matter, and several relations
  `interp.Flat` would build on gold trees without traces are wrong:
  - passive subjects (5,640 NP \* objects) become `sbj`;
  - passive agents become `by`;
  - the 2,982 \*T\* adverbials are not related to their verb at all.
- Keeping NP-LGS and the adverbial \*T\* traces for gold trees would repair
  most of this cheaply.

## Annotation errors found

| id | what is wrong |
|---|---|
| `debate-transcript/3rd_Bush-Kerry#28` | `(-NONE- *PRO-1)` for `*PRO*-1`. `empty_kind` then yields a subject kind `*PRO` |
| `twitter/tweets1#134` | `(-NONE- *RNR-2)` for `*RNR*-2` |
| `spam/ucb31#5` and others | literal asterisks (list bullets, rules) tagged `-NONE-`, 30 leaves: `**` ×16 (e.g. `(LS (-NONE- **))`), `***` ×5, `****` ×3 (`jokes/jokes10#99`), longer runs in `enron/53536#30`, `enron/9159#15`, `spam/221197#12`, `w3c/lists-003-2148080#16` |
| `debate-transcript/2nd_Gore-Bush#286` vs `3rd_Bush-Kerry#836` | *work with* NP untagged vs PP-CLR in the same sense; one of the 49 mixed pairs |

Questionable classifications in `tools/masc/verbframes.py` (not fixed):

1. `role()` never looks inside a PP, so 974 passive by-phrases with NP-LGS
   become modifiers (example 1).
2. RB daughters are modifiers. Negation is thus recorded only under lexical
   *be* (809 of 955 uRB, lemmas ≥ 20); 2,697 uRB in auxiliary VPs are not
   recorded (example 12).
3. Only the lexical VP's daughters are collected, which misses 17,213
   modifiers (36%) in auxiliary VPs and at clause level (Table 5).
4. A flat verb coordination `(VP V CC V ...)` gives one record. 438 verbs
   get none, and the first verb is not flagged `vp-coordination`
   (example 16).
5. `frame_backbone` keeps PRD/CLR/DTV, which the backbone does not have. It
   is the frame without empties, not what the parser sees; hence the
   "categories only" row in Table 1.
6. VOC is in `MODIFIER_TAGS`, so vocatives count as verb modifiers (71 in
   the VP, 553 at clause level).

## Open questions

- How much of the CLR/untagged variation is disagreement and how much is
  real ambiguity? Double annotation of the 49 mixed pairs would tell.
- Would reclassifying DIR and PP-LGS as complements give a cleaner
  selectional split? The script can test this by editing `mtype` or `role`
  locally.
- Would Levin classes predict per-verb modifier entropy, which tracks motion
  and activity, better than frequency does? That needs a class mapping for
  the 457 lemmas, which was not built.
- Clause-level modifiers shared by coordinated verbs are counted once per
  verb. Does that bias the spoken/written comparison above the VP?
