# Complements and modifiers in MASC: how promiscuous is each?

In MASC, the verb predicts its complements far better than its modifiers. After
correcting for estimation bias, the verb lemma removes 38% of the uncertainty
about the complement frame but only 10% about the set of modifiers (7% once
modifiers attached above the lexical verb phrase are included). The result
holds with or without empty elements, at matched granularity, and in both
spoken and written genres. The claim does not hold in two other senses.
Within one verb, complement frames are about as varied as modifier sets. And
promiscuity belongs to individual types, not to the two classes: the plain
NP object is about as promiscuous as LOC or MNR, while DIR and EXT modifiers
are as selective as complements. Roughly one verb-phrase daughter in six has
a doubtful complement/modifier status in the annotation.

## Question

The claim tested is that complements are much less promiscuous than
modifiers. It is tested (1) per type: across how many verb lemmas each
complement or modifier type is spread, and how evenly; and (2) per verb: how
varied a verb's complement frames are compared with its modifier sets, and
how much the verb tells us about each (H(Y | verb) against H(Y), i.e. mutual
information). The report also asks how much the empty elements matter, how
often the complement/modifier assignment is itself doubtful, and how the
picture differs between spoken and written genres.

## Data and method

Script: `tools/masc/verbs/complements_modifiers.py` (standard library only;
it imports `masctrees.py` and `verbframes.py` unchanged).

    D=/tmp/claude-0/-home-user/32748c59-1ad9-5140-855d-e9a1edeaf50a/scratchpad
    python3 tools/masc/verbs/complements_modifiers.py $D/masc/data            # all tables (20 s)
    python3 tools/masc/verbs/complements_modifiers.py $D/masc/data --lemma send
    python3 tools/masc/verbs/complements_modifiers.py $D/masc/data --show 'ficlets/1401#79' 7

The script reads the raw trees again with `verbframes.verb_record`. It gets the
same 70,101 records as `verbs.jsonl` and also keeps each verb phrase node.
From these it takes the preposition of each PP, the subject and predicate
inside each S complement, `NP-LGS` inside PPs, and the modifiers above the
lexical verb phrase.

* **Lemmas.** The word is lower-cased and looked up in a table of about 150
  irregular forms. For VB/VBP forms the table is consulted only for
  be/have/do, so base-form *lay*, *saw* and *found* stay as they are. The
  -s, -ies, -ed, -ied and -ing endings are stripped, and consonant doubling
  and e-restoration give several candidates. The chosen candidate is the
  one most often tagged VB/VBP in MASC; failing that, the one attested as a
  VBZ stem; failing that, a suffix heuristic. The contractions *'s*, *'re*,
  *'m* and the mangled *RSQUOs* (`face-to-face/interview_nathan_hole`) map to
  *be*. Result: 3,209 lemmas. 457 lemmas have 20 or more tokens and cover
  60,224 tokens (85.9%); 102 lemmas have 100 or more. For 2.5% of tokens the
  lemma is not attested as a base form (e.g. *incubate*, *kid*, *devot*,
  *jok*, *proven*). A token-weighted random sample of 150 lemmatized tokens
  had no errors. Known errors: *found* conflates *found*/*find* in the 1 VB
  case; *'s* as a lexical verb is always taken as *be*; *=* (VBZ, 24 tokens,
  technical texts) is a lemma of its own.
* **Complement frame**: the non-subject part of `frame_full`, or of
  `frame_backbone`, written `0` when empty. **Complement type**: one element
  of such a frame (83 types). **Coarse complement type** (12): NP, NP-empty,
  PRD, CLR, DTV/PUT, S, SBAR, PRT, their empty variants, and other.
* **Modifier type**: the first semantic tag (TMP LOC MNR PRP DIR ADV EXT BNF
  VOC). Untagged modifiers are named by category: uPP, uADVP, uRB, uADJP.
  **Modifier set**: the sorted multiset of a verb's modifier types.
  "Full" includes empty modifiers; "backbone" does not. `mx` adds the
  modifiers of the auxiliary verb phrases above the verb and the non-subject
  modifier daughters of its clause.
* **Mutual information.** I(verb; Y) is computed over tokens of the 457
  lemmas. Its bias is estimated as the mean I over five random shufflings of
  Y. The report gives U = (I − shuffled I) / H(Y): the share of Y's entropy
  that the verb removes, bias-corrected.
* **Type spread.** For each type with at least 100 tokens: how many lemmas
  it occurs with; how many it would meet if it were drawn independently of
  the verb (Σ_v 1 − (1 − p_v)^n); how many lemmas cover 80% of its tokens;
  and KL(P(verb | type) ‖ P(verb)) in bits (0 = spread exactly like verbs in
  general).
* **Per-lemma entropy** is rarefied to 100 tokens (mean of 50 subsamples),
  so frequent and less frequent verbs are comparable.
* **Genres.** Spoken genres are those under `data/spoken`: court-transcript,
  debate-transcript, face-to-face and telephone (15,151 verbs). Everything
  under `data/written` is written, 20 genre ids (54,950 verbs). This includes
  movie-script and the 46 verbs in `newspaper/unknown`. The court and debate
  genres come from two files each, so genre differences partly reflect
  single documents.

## Counts

**Table 1. How much the verb tells us (lemmas ≥ 20, 60,224 tokens).**

| Y | values | H(Y) | H(Y\|verb) | I | shuffled I | U |
|---|---|---|---|---|---|---|
| complement frame, full | 276 | 4.03 | 2.23 | 1.80 | 0.26 | **0.383** |
| complement frame, no empty elements | 145 | 3.46 | 1.96 | 1.50 | 0.17 | **0.382** |
| complement frame, categories only (no tags, no empties) | 83 | 2.96 | 1.92 | 1.05 | 0.12 | 0.313 |
| modifier set, full | 256 | 2.41 | 1.96 | 0.45 | 0.20 | **0.103** |
| modifier set, no empty elements | 240 | 2.22 | 1.80 | 0.43 | 0.19 | 0.107 |
| modifier set incl. aux-VP and clause level | 500 | 3.42 | 2.83 | 0.58 | 0.34 | 0.073 |
| one complement, 83 types (per daughter, n=58,334) | 83 | 3.33 | 1.90 | 1.43 | 0.14 | 0.389 |
| one complement, 12 coarse types | 12 | 2.64 | 1.33 | 1.31 | 0.05 | **0.477** |
| one modifier, 13 types (per daughter, n=25,174) | 13 | 3.06 | 2.50 | 0.56 | 0.13 | **0.139** |
| one modifier, tag+category, 74 types | 74 | 4.40 | 3.50 | 0.90 | 0.39 | 0.115 |

The U values change little when frequent verbs are excluded: without *be*,
frames 0.340 and modifier sets 0.102. Without be/have/do/get/go/make they
are 0.343 and 0.106. With lemmas ≥ 100 only, 0.386 and 0.091.

**Table 2. Spoken vs written (U).** The written column is repeated on a
random sample of the spoken size, because the shuffle correction depends on
n.

| Y | spoken (14,115) | written (46,109) | written, sample of 14,115 |
|---|---|---|---|
| complement frame, full | 0.370 | 0.378 | 0.337 |
| complement frame, categories only | 0.306 | 0.311 | 0.279 |
| modifier set, full | 0.112 | 0.101 | 0.085 |
| modifier set incl. above VP | 0.078 | 0.072 | 0.062 |
| one complement, coarse | 0.507 | 0.467 | 0.446 |
| one modifier, 13 types | 0.130 | 0.141 | 0.114 |

**Table 3. Spread of types over the 457 lemmas.** Sorted by KL; a selection
of rows.

| type | tokens | lemmas | expected if independent | obs/exp | lemmas for 80% | KL (bits) | top lemmas |
|---|---|---|---|---|---|---|---|
| M:TMP | 5351 | 403 | 436 | 0.92 | 114 | 0.31 | be, have, go, get |
| M:uADVP | 1496 | 250 | 319 | 0.78 | 64 | 0.47 | be, go, do, come |
| M:uPP | 4976 | 427 | 433 | 0.99 | 171 | 0.50 | be, go, make, have |
| M:ADV | 1762 | 326 | 339 | 0.96 | 109 | 0.50 | be, go, have, say |
| M:PRP | 2557 | 331 | 380 | 0.87 | 87 | 0.55 | be, go, do, have |
| M:LOC | 3152 | 357 | 400 | 0.89 | 118 | 0.71 | be, find, see, get |
| M:MNR | 2679 | 389 | 385 | 1.01 | 153 | 0.72 | do, be, work, make |
| **C:NP** | 19487 | 430 | 457 | 0.94 | 142 | **0.76** | have, give, take, do |
| C:NP(\*T\*) | 1970 | 247 | 352 | 0.70 | 61 | 1.22 | do, say, have, make |
| C:S | 6425 | 199 | 444 | 0.45 | 32 | 1.40 | have, want, go, make |
| C:PP-CLR | 4797 | 305 | 431 | 0.71 | 78 | 1.47 | look, talk, think, put |
| C:NP(\*) | 3885 | 347 | 417 | 0.83 | 151 | 1.49 | use, call, base, make |
| C:SBAR | 4909 | 221 | 432 | 0.51 | 31 | 1.72 | think, know, say, be |
| M:uRB | 955 | 48 | 263 | 0.18 | 1 | 1.76 | be 809 (*not*, *n't*) |
| C:ADJP-PRD | 4582 | 24 | 428 | 0.06 | 1 | 2.01 | be 4045, feel, become |
| C:PRT | 2172 | 172 | 363 | 0.47 | 52 | 2.10 | come, go, take, pick |
| C:NP-PRD | 5129 | 27 | 434 | 0.06 | 1 | 2.21 | be 4884, become |
| **M:DIR** | 1954 | 140 | 351 | 0.40 | 29 | **2.78** | go 373, come 177, walk 138, move |
| **M:EXT** | 174 | 56 | 92 | 0.61 | 27 | **3.85** | increase, rise, go, reduce |
| C:S-CLR | 399 | 43 | 162 | 0.27 | 6 | 4.37 | use, keep, call, leave |
| C:PP-DTV | 236 | 28 | 114 | 0.24 | 10 | 4.42 | give 70, send 32, bring |

**Table 4. Per verb (102 lemmas ≥ 100 tokens), entropy rarefied to 100
tokens.** Mean H: complement frame (full) 2.12, complement frame (no
empties) 1.87, modifier set 1.98. The modifier set is the more varied for 42
of the 102 lemmas. Pearson r between the two entropies is 0.04; between
frame entropy and log frequency, 0.16.

| most frame-promiscuous | n | H frame | H frame, no empties | H mods | top frames |
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

| most frame-faithful | n | H frame | H frame, no empties | H mods | top frames |
|---|---|---|---|---|---|
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

Modifier-promiscuous verbs are verbs of motion and activity: *move* 3.85,
*return* 3.84, *run*, *spend*, *come*, *walk*, *send*, *work*. The most
modifier-faithful verbs, unmodified in 87–93% of tokens, take clausal
complements: *hope* 0.51, *believe*, *mean*, *let*, *want*, *think*,
*suggest*, *like*.

**Table 5. Empty elements.** Of 66,718 complements, 9,083 (13.6%) are empty:
NP \* 5,640, NP \*T\* 2,307, S \*T\* 353. Of 31,212 VP-internal modifiers,
2,982 (9.6%) are empty, almost all \*T\*: TMP 1,211 (19.3% of TMP), MNR 690
(20.1%), LOC 459 (11.8%), PRP 351 (11.4%). The complement frame changes when
empties are removed in 12.9% of tokens; the modifier set, in 4.2%. A further
17,213 modifiers sit above the lexical VP: 11,457 at clause level (TMP
3,465, uADVP 2,823, ADV 1,610, uPP 1,334, VOC 553) and 5,756 in auxiliary
VPs (uRB 2,697, mostly negation; uADVP 1,172; TMP 1,124). `verbs.jsonl`
therefore records 64% of the modifiers of the verbs' clauses. 36% of verbs
have a VP-internal modifier, and 50% have one when modifiers above the VP
are counted.

**Table 6. What a category alone says about role (all VP daughters, as
classified by verbframes.py).**

| category | n | complement % | modifier % | tags, overt (top) |
|---|---|---|---|---|
| NP | 39,079 | 95.8 | 4.2 | none 24,004; PRD 5,109; TMP 806; ADV 178 |
| PP | 23,097 | 30.1 | 69.9 | none 6,673; CLR 5,033; LOC 2,900; TMP 1,899; DIR 1,739 |
| S | 10,210 | 76.6 | 23.4 | none 6,439; ADV 1,302; PRP 1,072; CLR 442 |
| SBAR | 8,393 | 70.1 | 29.9 | none 4,979; TMP 1,052; ADV 680; PRP 610 |
| ADVP | 8,161 | 8.4 | 91.6 | none 1,637; TMP 1,290; MNR 1,038; DIR 623 |
| ADJP | 4,777 | 99.3 | 0.7 | PRD 4,597 |

**Table 7. Doubtful assignments (97,930 complement and modifier daughters of
lexical VPs).**

| class | n | % of daughters |
|---|---|---|
| untagged overt PP (a modifier by default) | 6,673 | 6.8 |
| of which *by*-PP whose NP is NP-LGS (passive agent) | 974 | 1.0 |
| any -CLR daughter (a complement) | 6,157 | 6.3 |
| S complement with an overt subject (small clause, ECM, perception, causative) | 1,996 | 2.0 |
| RB *not*/*n't* counted as a modifier (809 of all uRB are with *be*) | 859 | 0.9 |
| -DIR modifier (verb-selective, Table 3) | 2,471 | 2.5 |
| -DTV or -PUT PP | 290 (PUT: 5) | 0.3 |

Of the 399 (lemma, preposition) pairs with at least 10 PP daughters, 49
(8.2% of their 10,920 PPs) are tagged CLR at least 20% of the time and
untagged at least 20% of the time. In 132 pairs (40.9% of the PPs) the
share of complement tags lies between 20% and 80%. Taking the majority tag
of each pair would reproduce 73.0% of the tags.

S complements (7,411 overt ones) by the subject inside them: \*PRO\* 3,199,
\* 2,043, overt 1,996. The share with an overt subject is:

| verb | S compl. | overt subject | commonest shape |
|---|---|---|---|
| see | 145 | 88% | VBG 41, bare VB 32, PP small clause 32 |
| hear / watch | 44 / 36 | 93% / 97% | bare VB 26 / 33 |
| make | 352 | 88% | ADJP small clause 166, bare VB 78, NP 54 |
| let | 315 | 99% | bare VB 290 |
| have | 573 | 34% | \* to-VP 346 (*have to*), VBN 58 |
| help | 248 | 36% | \*PRO\* bare VB 117, overt bare VB 76 |
| want / expect | 478 / 55 | 17% / 47% | \*PRO\* to-VP 385 / overt to-VP 26 |
| go | 414 | 1% | \* to-VP 393 (*going to*) |

**Table 8. Modifier types per 100 verbs, by genre**, counting only the
lexical VP's own modifiers, overt and empty. "Above" gives the modifiers
above the VP per 100 verbs.

| genre | verbs | none % | TMP | LOC | DIR | MNR | PRP | ADV | uPP | uADVP | all | above |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| court-transcript | 4704 | 70.1 | 8.5 | 5.1 | 0.4 | 3.6 | 4.5 | 2.0 | 5.8 | 2.4 | 35.5 | |
| debate-transcript | 5589 | 69.3 | 6.9 | 4.2 | 1.1 | 5.0 | 4.8 | 1.8 | 8.5 | 2.2 | 36.8 | |
| face-to-face | 3689 | 70.9 | 7.4 | 3.5 | 4.7 | 3.4 | 3.9 | 1.5 | 5.6 | 3.5 | 35.2 | |
| telephone | 1169 | 74.6 | 7.7 | 3.9 | 4.0 | 2.1 | 2.4 | 1.3 | 0.9 | 4.0 | 29.0 | |
| fiction | 5518 | 61.9 | 9.4 | 7.4 | 6.9 | 5.2 | 3.1 | 5.2 | 6.1 | 2.7 | 47.9 | |
| ficlets | 4739 | 61.0 | 8.8 | 5.1 | 7.8 | 6.1 | 2.7 | 6.0 | 6.6 | 2.8 | 48.1 | |
| movie-script | 3715 | 61.1 | 5.9 | 7.1 | **11.5** | 4.0 | 3.9 | 4.3 | 7.2 | 2.3 | 47.9 | |
| travel-guides | 2513 | 55.2 | **12.9** | **12.0** | 5.8 | 3.9 | 4.3 | 3.4 | 11.1 | 1.9 | 56.7 | |
| technical | 1790 | 50.7 | 11.3 | 9.9 | 0.4 | 5.1 | 4.4 | 5.0 | **24.5** | 2.1 | 65.4 | |
| essays | 3653 | 58.1 | 8.0 | 4.9 | 2.1 | 6.5 | 4.5 | 3.6 | 18.3 | 2.2 | 51.8 | |
| govt-docs | 2836 | 64.8 | 9.2 | 2.7 | 1.0 | 5.3 | 3.9 | 2.6 | 15.6 | 1.4 | 43.4 | |
| solicitation-brochures | 1617 | 62.8 | 9.2 | 6.2 | 1.5 | 5.2 | **10.1** | 2.2 | 10.3 | 2.0 | 47.3 | |
| spam | 3000 | 59.2 | 9.9 | 5.9 | 1.8 | 6.9 | 8.7 | 2.7 | 13.2 | 2.1 | 52.5 | |
| enron | 1603 | 60.1 | **14.4** | 4.7 | 1.6 | 4.7 | 5.6 | 2.1 | 13.3 | 2.7 | 50.0 | |
| **SPOKEN** | 15,151 | 70.3 | 7.6 | 4.3 | 2.0 | 4.0 | 4.3 | 1.7 | 6.4 | 2.7 | 35.4 | 24.0 |
| **WRITTEN** | 54,950 | 62.3 | 9.3 | 6.0 | 3.9 | 5.2 | 4.5 | 3.5 | 10.6 | 2.3 | 47.0 | 24.7 |

The remaining written genres (blog, jokes, journal, non-fiction, nyt,
philanthropic-fundraising, twitter, w3c, wsj) lie within these ranges; the
script prints all 24. Including the modifiers above the VP, spoken has per
100 verbs TMP 12.5, uADVP 9.9, uPP 7.9, uRB 7.6, MNR 4.8, LOC 4.7, PRP 4.7.
Written has TMP 16.3, uPP 12.8, uADVP 7.6, LOC 6.8, MNR 6.7, ADV 6.0.

## Examples

Trees are trimmed with `--show`. F = complement part of `frame_full`, B = of
`frame_backbone`, M = modifiers.

1. `ficlets/1401#79` *opened*: `(NP-SBJ-1 the door) (VP was (VP opened (NP *-1) (PP-PRP for us) (PP by (NP-LGS Noon himself))))`.
   F `NP(*)`, B `0`; M PRP, uPP. Without the trace, this passive has the
   same frame as the inchoative in (2). The agent is an untagged PP, so
   verbframes counts it as a modifier.
2. `fiction/Nathans_Bylichka#839` *opened*: `(NP-SBJ my eyes) (VP opened)`. F `0`, B `0`. A true inchoative.
3. `debate-transcript/2nd_Gore-Bush#297` *sent*: `(VP sent (NP troops) (PP-DIR to Haiti))`. F `NP`; M DIR.
4. `face-to-face/Bmr021#184` *send*: `(VP send (NP the data) (PP-DTV to Sony))`. F `NP PP-DTV`.
   With (3): the same *send NP to NP* is a complement in one case and a
   modifier in the other, depending on whether the annotator read the goal
   or the recipient.
5. `debate-transcript/3rd_Bush-Kerry#836` *work*: `(VP work (PP-CLR with allies))`. F `PP-CLR`.
6. `debate-transcript/2nd_Gore-Bush#286` *work*: `(VP to (VP work (PP with Nigeria)))`. F `0`; M uPP.
   Same verb, preposition and sense in the same kind of text, but one PP is a
   complement and the other a modifier.
7. `debate-transcript/2nd_Gore-Bush#392` *seen*: `(VP seen (S (NP-SBJ them) (VP make (NP some calls ...))))`. F `S`.
   A perception verb's small clause. Its subject is the perceived entity, but
   the frame shows only S.
8. `debate-transcript/2nd_Gore-Bush#53` *keep*: `(VP keep (NP-2 our military) (S-CLR (NP-SBJ *PRO*-2) (ADJP-PRD strong)))`.
   F `NP S-CLR`. The resultative/depictive predicate is a complement through
   -CLR and an object-controlled \*PRO\*.
9. `court-transcript/Day3PMSession#1327` *ask*: `(VP ask (NP-2 you) (S (NP-SBJ *PRO*-2) (VP to (VP read ...))))`.
   F `NP S`. Object control; one of *ask*'s 13 frames needed to reach 80%.
10. `court-transcript/Day3PMSession#353` *got*: `(VP got (NP this job) (ADVP-TMP *T*-2))` in *when I got this job*.
    F `NP`; M TMP, empty. The backbone drops the TMP relation: *when* sits
    in SBAR and is not linked to *got*.
11. `court-transcript/Day3PMSession#340` *leaving*: `(VP leaving (NP-TMP tomorrow))`. F `0`; M TMP.
    Without function tags the backbone sees `VP → VBG NP`, which looks like
    a transitive verb.
12. `court-transcript/Day3PMSession#83` *'s*: `(VP 's (RB not) (NP-PRD my intention))`. F `NP-PRD`; M uRB.
    Negation counts as a modifier only because copular *be* is the lexical
    verb. With other verbs, *not* sits in the auxiliary VP (as *n't* does in
    3) and is not recorded.
13. `debate-transcript/3rd_Bush-Kerry#265` *increased*: `(VP increased (NP Pell Grants) (PP-EXT by a million students))`.
    M EXT. The measure of change of a degree verb; EXT is as verb-selective
    as a complement (Table 3).
14. `debate-transcript/2nd_Gore-Bush#86` *put*: `(VP put (NP our troops) (PP-LOC-CLR all around the world))`.
    F `NP PP-CLR`. MASC uses CLR, not PUT, for *put*'s locative (PUT occurs 5
    times in the corpus). Of *put*'s 199 PP/ADVP daughters tagged CLR, LOC
    or untagged, 32 are PP-LOC or untagged, i.e. modifiers.
15. `movie-script/JurassicParkIV-Scene_3#120` *place*: `(VP place (NP you) (PP under arrest))`. F `NP`; M uPP.
    An untagged PP that is an argument of *place*.
16. `court-transcript/Day3PMSession#280` *clarifies*: `(VP (VBZ clarifies) (CC and) (VBZ reasserts) (SBAR what I have ...))`.
    Flat coordination of verbs: *reasserts* gets no record, and
    *clarifies* is not flagged as coordinated.
17. `debate-transcript/2nd_Gore-Bush#883` *need*: `(VP need (NP gas pipelines) (S-PRP (NP-SBJ *PRO*-1) (VP to bring the gas down)))`.
    M PRP. *need* has 14.6 PRP modifiers per 100 tokens, although it is not
    agentive (see Finding 8).

## Findings

1. **In the information-theoretic sense the claim holds, and holds
   robustly.** Knowing the verb removes 38% of the (bias-corrected)
   uncertainty of its complement frame but 10% of that of its modifier set
   (Table 1). At matched granularity the figures are 48% vs 14% (12 coarse
   complement types vs 13 modifier types) and 39% vs 12% (83 vs 74 types).
   The gap remains without *be* or the six commonest verbs, and with only
   lemmas ≥ 100. When modifiers above the lexical VP are included, the
   modifier figure falls to 7%.
2. **Within one verb, complements are not less varied than modifiers.** The
   rarefied per-verb entropy is 2.12 bits for complement frames and 1.98
   for modifier sets. 42 of 102 verbs have more varied modifiers than
   complements, and the two entropies are uncorrelated (r = 0.04; Table 4).
   What distinguishes complements is that the verb decides which frames
   occur. With modifiers the variation is much the same whatever the verb.
3. **Promiscuity belongs to types, not to the two classes** (Table 3). The
   plain NP object occurs with 430 of 457 lemmas, 94% of the number expected
   under independence, with KL 0.76 bits. That is close to LOC (0.71) and
   MNR (0.72), and TMP, uPP, ADV and PRP are only slightly more promiscuous
   (0.31–0.55). PRD, SBAR, S, PRT, CLR and DTV complements are selective
   (1.4–4.4 bits). Two modifier types are as selective as complements: DIR
   (2.78; *go*, *come*, *walk*, *move*) and EXT (3.85; *increase*, *rise*,
   *reduce*). uRB looks selective (1.76) only because of the artefact in
   example 12.
4. **Frame-faithfulness depends on the tagging convention.** *walk* is among
   the most frame-faithful verbs (0 in 148 of 198 tokens) and among the most
   modifier-promiscuous, because its directional PPs are tagged DIR. *talk*
   is faithful because its *to*/*about* PPs are CLR. The promiscuous verbs
   in Table 4 are those with many CLR/particle/dative options (*bring*,
   *set*, *put*, *pay*), those with control or small-clause complements
   (*ask*, *call*, *keep*, *leave*), and those split by empty elements
   (*say*: S vs S(\*T\*) quotations; *write*, *call*: passives).
5. **Empty elements add verb-specific distinctions at the verb's own rate.**
   Removing them changes the complement frame for 12.9% of tokens and lowers
   H(frame) from 4.03 to 3.46 bits. U does not change (0.383 vs 0.382), so
   the passive and extraction distinctions they encode are as predictable
   from the verb as the rest of the frame. NP(\*) concentrates on *use*,
   *call*, *base*, *make*; NP(\*T\*) on *do* (*what do you do*) and *say*.
   The verbs most changed are *describe* (46%), *require* (41%), *build*,
   *do*, *say* and *call*. For modifiers the empty elements are the \*T\*
   traces of *when*, *how*, *where* and *why*: about a fifth of all TMP and
   MNR modifiers. The backbone loses them entirely (example 10).
6. **Function tags carry much of the verb-specific information.** With
   categories only, as the fast parser sees them, U for complement frames
   falls from 0.38 to 0.31. Category is a poor guide to role for PP (30%
   complement), SBAR (70%) and S (77%), and a good one for NP (96%), ADJP
   and PRT (Table 6).
7. **The complement/modifier assignment is doubtful for roughly one VP
   daughter in six** (Table 7): untagged PPs, CLR PPs, overt-subject S
   complements and negation make up 16.0% of daughters, and 18.5% if DIR is
   added. Three problems stand out:
   - Passive agents: *by*-PPs with NP-LGS are 974, or 15% of the untagged PPs.
   - Inconsistency: the (verb, preposition) pair fixes the coarse tag for
     only 73% of PPs. Examples: *work with* 33 CLR / 29 untagged, *focus on*
     12 / 31, *describe as* 21 / 7, *attribute to* 4 / 12.
   - Unused tags: PUT occurs 5 times in all of MASC, and *place*'s
     locatives are split among untagged 15, LOC 12, LOC-CLR 11 and CLR 9.

   Some of this variation is real ambiguity: comitative vs collaborative
   *with*, and goal vs recipient *to* (*send*: DIR 26, DTV 30). Some is
   annotator variation (examples 5–6).
8. **Levin and Dowty, where the data allow.**
   - *Causative/inchoative* (Levin 1993, from memory: §1.1.2.1): *break*,
     *open*, *close* and *change* are 27–40% intransitive and 55–65%
     transitive. The non-alternating unaccusatives *arrive*, *die*,
     *happen* and *come* are 95–100% intransitive, and *take*/*buy* 4–13%
     intransitive. (Intransitive here means no NP complement; S and PP
     complements are allowed.) Without traces the backbone merges passive with
     inchoative: among backbone NP-less tokens, passives are 8% for *break*,
     18% for *open*, 22% for *close*, 20% for *change*, 47% for *develop*
     and 85% for *fill*.
   - *Dative*: *give* has NP NP 253 vs NP PP(*to*/*for*) 66 DTV + 9 CLR +
     4 untagged (+ 11 PRP/BNF/TMP). *tell* has NP NP 55 vs 1. *send* splits
     as in Finding 7.
   - *Spray/load*: not testable. *load*, *spray* and *pour* have 17, 4 and
     12 tokens, and PUT is absent.
   - *Dowty's proto-roles* (Dowty 1991): the treebank does not label roles.
     Two indirect observations:
     - The by-phrase, the clearest Proto-Agent outside the subject, is
       classified as a modifier (Finding 7).
     - Purpose and manner modifiers, classic agentivity diagnostics (Lakoff
       1966, from memory), are rarest with stative and attitude verbs: PRP+MNR
       per 100 tokens is 1.5 for *agree* and *suggest*, 2.2 *seem*, 2.3
       *mean*, 3.2 *believe*, 3.5 *be* and *think*. They are commonest with
       activities: *work* 38.6, *send* 27.9, *pay* 23.6, *move* 22.6, *use*
       21.8.
     - The exceptions are *need* (PRP 14.6: the purpose belongs to the thing
       needed, example 17) and *thank* (MNR from *so much*). These rates
       therefore fit agentivity only roughly.
9. **Spoken vs written.**
   - Spoken verbs have fewer VP-internal modifiers: 35.4 vs 47.0 per 100
     verbs, and 70% vs 62% unmodified. Above the VP the two are equal (24.0
     vs 24.7).
   - TMP is the commonest semantic modifier type in every genre except
     movie-script (DIR), non-fiction (LOC, MNR) and solicitation-brochures
     (PRP). It leads outright, ahead of untagged PPs as well, in court,
     face-to-face, telephone, fiction, ficlets, jokes, journal, twitter,
     enron, travel-guides and philanthropic-fundraising.
   - Untagged PPs lead in expository writing (technical 24.5, essays 18.3,
     govt-docs 15.6); passive agents make up 14–22% of these in technical,
     govt-docs and essays.
   - Genre peaks: DIR in movie-script (11.5, stage directions); LOC and TMP
     in travel-guides (12.0, 12.9); PRP in solicitation-brochures and spam
     (10.1, 8.7; the commonest brochure PRP is *to help ...*, 16 of 162);
     ADV in fiction and ficlets (5–6; 81% are S-ADV clauses). Above the VP,
     spoken has more uADVP and negation.
   - Verb predictability is similar in both, slightly higher in spoken at
     equal sample size (Table 2: frames 0.370 vs 0.337, modifier sets
     0.112 vs 0.085). This is a small effect from one random sample.

## What it means

**(a) The context-free backbone and the fast parser.**
- The backbone's categories do not separate complements from modifiers for
  PPs and clauses (Table 6). NP-TMP (*leaving tomorrow*) looks like an
  object.
- Removing traces merges passives with intransitives, which hides the
  causative alternation (Finding 8).
- The data favour a parser that conditions complement choices on the head
  verb and generates modifiers largely independently of it. Collins's
  Model 2 does this with complement marking and subcategorisation frames
  (Collins 1999, from memory).
- The bare backbone offers no complement/modifier distinction to condition
  on; the learned function-tag table (`docs/flat-semantics.md`, CLR F1 45.6)
  is the only source.
- PPs whose status is doubtful (Table 7) set a ceiling on how well any
  model can learn the distinction from MASC.

**(b) Verb frames and the complement/modifier distinction.**
- The distinction as PTB tags draw it is a good but imperfect correlate of
  selection. Complements are verb-selected (U ≈ 0.4–0.5) and modifiers
  largely not (≈ 0.1).
- The exceptions are systematic: DIR with motion verbs and EXT with degree
  verbs behave like complements; the plain NP object is spread across verbs
  almost as widely as TMP or LOC.
- Frame inventories are sensitive to annotation choices (CLR vs untagged,
  unused PUT, DIR vs DTV). They should be read as the annotators' decisions,
  not as lexical facts, where the tags are inconsistent (Finding 7).
- The treebank's modifiers are verb-independent in distribution, but not
  functionless. They are the part that varies most by genre (Table 8).

**(c) The flat neo-Davidsonian semantics.**
- The result supports treating modifiers as independent conjuncts on the
  event variable, in the Davidson/Parsons manner. Their occurrence is
  nearly independent of the verb, so nothing is lost by not listing them in
  the verb's entry.
- Complements are where the verb's lexical entry matters. Several
  relations `interp.Flat` names would come out wrong on gold trees without
  traces:
  - passive subjects (5,640 NP \* objects) would be `sbj`, not the
    underlying object;
  - passive agents would be `by`, not the agent;
  - the 2,982 \*T\* modifiers (*when*, *where*, *how*, *why*) would not be
    related to their verb at all.
- Keeping NP-LGS and the \*T\* adverbial traces for gold trees would repair
  most of these at little cost.

## Annotation errors found

| id | what is wrong |
|---|---|
| `debate-transcript/3rd_Bush-Kerry#28` | `(-NONE- *PRO-1)` for `*PRO*-1`. verbframes' `empty_kind` then yields a subject kind `*PRO` of its own |
| `twitter/tweets1#134` | `(-NONE- *RNR-2)` for `*RNR*-2` |
| `spam/ucb31#5` and 26 others | literal asterisks (list bullets, rules) tagged `-NONE-`: `**` ×16, `***` ×5, longer runs in `enron/53536#30`, `enron/9159#15`, `spam/221197#12`, `w3c/lists-003-2148080#16` |
| `debate-transcript/2nd_Gore-Bush#286` vs `3rd_Bush-Kerry#836` | *work with* NP: untagged vs PP-CLR for the same sense (one instance of the 49 mixed pairs; inconsistency, not a single error) |

Questionable classifications in `tools/masc/verbframes.py` (not fixed):

1. `role()` classifies an untagged PP by category and never looks inside it.
   The 974 passive by-phrases whose NP is NP-LGS become modifiers (example 1).
2. RB daughters are modifiers. Negation is therefore recorded only when *be*
   is the lexical verb (809 of the 955 uRB among lemmas ≥ 20); elsewhere it
   is in the auxiliary VP and not recorded (2,697 uRB there; example 12).
3. Only the lexical VP's own daughters are collected. 17,213 modifiers in
   auxiliary VPs and at clause level, 36% of the total, are not in the
   record (Table 5).
4. Flat verb coordination `(VP V CC V ...)` yields one record: 438 verbs
   (VB* daughters with role "other") get none, and the first verb is not
   flagged `vp-coordination` (example 16).
5. `frame_backbone` keeps the function tags (PRD, CLR, DTV) although the
   backbone has none. It is the frame without empty elements, not what the
   parser sees; Table 1 therefore adds a "categories only" row.
6. VOC is in `MODIFIER_TAGS`, so vocatives count as verb modifiers (71 in
   the VP, 553 at clause level).

## Open questions

- How much of the CLR/untagged variation is annotator disagreement and how
  much is real ambiguity? A double annotation of a sample of the 49 mixed
  (verb, preposition) pairs would settle it.
- Would counting DIR (and PP-LGS) as complements, as many lexicalist
  accounts would, bring MASC closer to a clean selectional split? The script
  can test this by changing `mtype`/`role` locally.
- The per-verb entropy of modifier sets correlates with motion and activity
  semantics. Would Levin classes predict it better than frequency does? This
  needs a Levin-class mapping for the 457 lemmas, which was not built.
- Clause-level modifiers are shared by coordinated verbs and were counted
  once per verb. Does this affect the spoken/written comparison above the
  VP?
