# Complements and modifiers in MASC: how promiscuous is each?

In MASC the verb predicts its complements far better than its modifiers.
After a bias correction, the verb lemma removes 38% of the uncertainty about
its complement frame but only 10% of the uncertainty about its modifier set
(7% once modifiers above the lexical verb phrase are counted). This holds
with and without empty elements, at matched granularity, and in spoken and
written genres alike. It does not hold in two other senses: within one verb,
complement frames are about as varied as modifier sets; and promiscuity is a
property of individual types, not of the two classes. The NP object is as
widely spread as LOC or MNR, while DIR and EXT modifiers are as selective as
complements. About one verb-phrase daughter in six has a doubtful
complement/modifier status in the annotation.

## Question

Are complements much less promiscuous than modifiers, per type (over how
many verb lemmas, how evenly) and per verb (how varied its frames and
modifier sets are, and how much the verb tells us about each: H(Y | verb)
vs H(Y))? How much do empty elements matter, how often is the assignment
itself doubtful, and how do spoken and written genres differ?

## Data and method

The script is `tools/masc/verbs/complements_modifiers.py`. It uses only the
standard library and imports `masctrees.py` and `verbframes.py` unchanged.

    D=/tmp/claude-0/-home-user/32748c59-1ad9-5140-855d-e9a1edeaf50a/scratchpad
    python3 tools/masc/verbs/complements_modifiers.py $D/masc/data     # all tables, 20 s
    python3 tools/masc/verbs/complements_modifiers.py $D/masc/data --lemma send
    python3 tools/masc/verbs/complements_modifiers.py $D/masc/data --show 'ficlets/1401#79' 7

The script re-reads the raw trees with `verbframes.verb_record`, producing
the same 70,101 records as `verbs.jsonl`. It also keeps each verb-phrase
node, from which it reads the PP prepositions, the inside of S complements,
NP-LGS inside PPs, and the modifiers above the lexical VP.

**Lemmas.** Word forms are lower-cased and looked up in a table of about 150
irregular forms. For VB/VBP forms the table is used only for be/have/do, so
base-form *lay*, *saw* and *found* stay as they are. *'s*, *'re*, *'m* and
the mangled *RSQUOs* (`face-to-face/interview_nathan_hole`) become *be*.
Other forms have -s/-ies/-ed/-ied/-ing stripped, with doubling and
e-restoration giving several candidates. The candidate most often tagged
VB/VBP in MASC wins; failing that, a candidate attested as a VBZ stem;
failing that, a suffix heuristic. The result is 3,209 lemmas: 457 have at
least 20 tokens (60,224 tokens, 85.9%), and 102 have at least 100. For 2.5%
of tokens the lemma is not attested as a base form (*incubate*, *kid*,
*devot*, *jok*, *proven*). A token-weighted sample of 150 tokens showed no
errors. Known errors: *found* conflates two verbs, lexical *'s* is always
*be*, and `=` (VBZ, 24 tokens) counts as a lemma.

**Units and measures.** A *complement frame* is the non-subject part of
`frame_full` (or `frame_backbone`), `0` if empty: 83 element types, or 12
coarse ones (NP, PRD, CLR, DTV/PUT, S, SBAR, PRT, their empty variants,
other). A *modifier type* is the first semantic tag (TMP LOC MNR PRP DIR ADV
EXT BNF VOC), else u+category (uPP, uADVP, uRB); a *modifier set* is the
sorted multiset of a verb's types, "full" with empty modifiers, "incl.
above VP" adding those of the auxiliary VPs above the verb and of its
clause. U = (I(verb;Y) − I with Y shuffled, mean of 5 runs) / H(Y), the
bias-corrected share of Y's entropy the verb removes. A type's spread is
its number of lemmas against the number expected under independence
(Σ_v 1−(1−p_v)^n), the lemmas covering 80% of it, and KL(P(verb|type) ‖
P(verb)) in bits (0 = spread like verbs in general). Per-verb entropies are
rarefied to 100 tokens (mean of 50 subsamples).

**Genres.** Spoken means the four genres under `data/spoken` (court and
debate transcripts, face-to-face, telephone; 15,151 verbs). Written means
the 20 genre ids under `data/written` (54,950 verbs), including movie-script
and the 46 verbs in `newspaper/unknown`. Court and debate each come from two
files.

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
| one complement per daughter (n=58,334), 83 types | 83 | 3.33 | 1.90 | 1.43 | 0.14 | 0.389 |
| one complement, 12 coarse types | 12 | 2.64 | 1.33 | 1.31 | 0.05 | **0.477** |
| one modifier per daughter (n=25,174), 13 types | 13 | 3.06 | 2.50 | 0.56 | 0.13 | **0.139** |
| one modifier, tag+category, 74 types | 74 | 4.40 | 3.50 | 0.90 | 0.39 | 0.115 |

For frames and modifier sets respectively, U is 0.340 and 0.102 without
*be*; 0.343 and 0.106 without be/have/do/get/go/make; and 0.386 and 0.091
for lemmas ≥ 100 only.

**Spoken vs written.** U for full frames is 0.370 spoken, 0.378 written
and 0.337 on a written sample of spoken size (the shuffle correction depends
on n); for modifier sets 0.112, 0.101 and 0.085; incl. above VP 0.078, 0.072
and 0.062; per daughter, coarse complement 0.507, 0.467, 0.446 vs modifier
0.130, 0.141, 0.114.

**Table 2. Spread of types over the 457 lemmas (selected rows, ordered by
KL).**

| type | tokens | lemmas | expected | lemmas for 80% | KL | top lemmas |
|---|---|---|---|---|---|---|
| M:TMP | 5351 | 403 | 436 | 114 | 0.31 | be, have, go, get |
| M:LOC | 3152 | 357 | 400 | 118 | 0.71 | be, find, see, get |
| M:MNR | 2679 | 389 | 385 | 153 | 0.72 | do, be, work, make |
| **C:NP** | 19487 | 430 | 457 | 142 | **0.76** | have, give, take, do |
| C:S | 6425 | 199 | 444 | 32 | 1.40 | have, want, go, make |
| C:PP-CLR | 4797 | 305 | 431 | 78 | 1.47 | look, talk, think, put |
| M:uRB | 955 | 48 | 263 | 1 | 1.76 | be 809 (*not*) |
| C:NP-PRD | 5129 | 27 | 434 | 1 | 2.21 | be 4884, become |
| **M:DIR** | 1954 | 140 | 351 | 29 | **2.78** | go, come, walk, move |
| **M:EXT** | 174 | 56 | 92 | 27 | **3.85** | increase, rise, reduce |
| C:PP-DTV | 236 | 28 | 114 | 10 | 4.42 | give, send, bring |

Not shown: uPP 0.50, ADV 0.50, PRP 0.55, NP(\*T\*) 1.22, NP(\*) 1.49, SBAR
1.72, PRT 2.10.

**Table 3. The ten most frame-promiscuous and ten most frame-faithful verbs
(≥ 100 tokens).** H is the rarefied entropy of the full complement frame,
bb of the frame without empties, and mod of the modifier set.

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
1.87 for frames without empties, and 1.98 for modifier sets. In 42 of the 102
verbs the modifier set is the more varied. The correlation between frame
entropy and modifier entropy is r = 0.04; between frame entropy and log
frequency, r = 0.16. The verbs with the most varied modifiers are *move*
(3.85), *return* (3.84), *run*, *spend*, *come*, *walk*, *send* and *work*.
The least varied, unmodified in 87–93% of tokens, are *hope* (0.51),
*believe*, *mean*, *let*, *want*, *think*, *suggest* and *like*.

**Empty and unrecorded material.** 9,083 of 66,718 complements (13.6%) are
empty (NP \* 5,640, NP \*T\* 2,307, S \*T\* 353), and 2,982 of 31,212
VP-internal modifiers (9.6%), almost all \*T\*: TMP 1,211 (19.3% of TMP),
MNR 690 (20.1%), LOC 459 (11.8%), PRP 351 (11.4%). Removing empties changes
the complement frame of 12.9% of verbs and the modifier set of 4.2%. A
further 17,213 modifiers lie above the lexical VP: 11,457 at clause level
(TMP 3,465, uADVP 2,823, ADV 1,610, uPP 1,334, VOC 553) and 5,756 in
auxiliary VPs (uRB 2,697, uADVP 1,172, TMP 1,124), so `verbs.jsonl` records
64% of the modifiers of the verbs' clauses; 36% of verbs have a VP-internal
modifier, 50% one anywhere in the clause.

**Table 4. Role by category, all VP daughters (verbframes.py's
classification).**

| category | n | compl. % | modif. % | overt tags (top) |
|---|---|---|---|---|
| NP | 39,079 | 95.8 | 4.2 | none 24,004; PRD 5,109; TMP 806; ADV 178 |
| PP | 23,097 | 30.1 | 69.9 | none 6,673; CLR 5,033; LOC 2,900; TMP 1,899; DIR 1,739 |
| S | 10,210 | 76.6 | 23.4 | none 6,439; ADV 1,302; PRP 1,072; CLR 442 |
| SBAR | 8,393 | 70.1 | 29.9 | none 4,979; TMP 1,052; ADV 680; PRP 610 |
| ADVP | 8,161 | 8.4 | 91.6 | none 1,637; TMP 1,290; MNR 1,038; DIR 623 |
| ADJP | 4,777 | 99.3 | 0.7 | PRD 4,597 |

**Table 5. Doubtful assignments among the 97,930 complement and modifier
daughters of lexical VPs.**

| class | n | % |
|---|---|---|
| untagged overt PP (a modifier by default) | 6,673 | 6.8 |
| of which *by*-PP containing NP-LGS (passive agent) | 974 | 1.0 |
| any -CLR daughter (a complement) | 6,157 | 6.3 |
| S complement with an overt subject | 1,996 | 2.0 |
| RB *not*/*n't* as a modifier | 859 | 0.9 |
| -DIR modifier (verb-selective, Table 2) | 2,471 | 2.5 |
| -DTV or -PUT PP (PUT: 5 in all of MASC) | 290 | 0.3 |

**PP consistency.** Of the 399 (verb, preposition) pairs with at least 10
PPs (10,920 PPs in all), 49 pairs (8.2% of the PPs) are tagged CLR at least
20% of the time and also left untagged at least 20% of the time. In 132
pairs (40.9% of the PPs), the complement share lies between 20% and 80%.
Assigning each pair its majority tag reproduces 73.0% of the tags.

**S complements.** Of 7,411 overt S complements the subject is \*PRO\* in
3,199, \* in 2,043 and overt in 1,996. The overt share is high after
perception and causative verbs: *see* 88% of 145, *hear* 93% of 44, *watch*
97% of 36, *make* 88% of 352 (166 ADJP small clauses, 78 bare VPs), *let*
99% of 315; lower after *have* 34% of 573 (346 *have to*), *help* 36% of
248, *want* 17% of 478, *expect* 47% of 55, *go* 1% of 414 (393 *going
to*).

**Table 6. Modifier types per 100 verbs, by genre** (lexical VP only, overt
and empty; "all" counts every type).

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
| **SPOKEN** | 15,151 | 70.3 | 7.6 | 4.3 | 2.0 | 4.0 | 4.3 | 1.7 | 6.4 | 35.4 |
| **WRITTEN** | 54,950 | 62.3 | 9.3 | 6.0 | 3.9 | 5.2 | 4.5 | 3.5 | 10.6 | 47.0 |

The script prints all 24 genres. Modifiers above the VP add 24.0 per 100
verbs in spoken and 24.7 in written. Including them, spoken has TMP 12.5,
uADVP 9.9, uPP 7.9, uRB 7.6 and MNR 4.8 per 100 verbs; written has TMP 16.3,
uPP 12.8, uADVP 7.6, LOC 6.8 and MNR 6.7.

## Examples

Trees trimmed from `--show`; F = complement part of `frame_full`, B = of
`frame_backbone`, M = modifiers.

1. `ficlets/1401#79`: `(NP-SBJ-1 the door) (VP was (VP opened (NP *-1) (PP-PRP for us) (PP by (NP-LGS Noon himself))))`.
   F `NP(*)`, B `0`; the agent is a modifier. To the backbone this is the
   inchoative of `fiction/Nathans_Bylichka#839`, `(NP-SBJ my eyes) (VP opened)`, F `0`.
2. `debate-transcript/2nd_Gore-Bush#297`: `(VP sent (NP troops) (PP-DIR to Haiti))`. F `NP`, M DIR.
3. `face-to-face/Bmr021#184`: `(VP send (NP the data) (PP-DTV to Sony))`.
   F `NP PP-DTV`. With (2): goal or recipient decides complement or modifier.
4. `debate-transcript/3rd_Bush-Kerry#836`: `(VP work (PP-CLR with allies))`,
   F `PP-CLR`; but `2nd_Gore-Bush#286`: `(VP to (VP work (PP with Nigeria)))`,
   F `0`, M uPP. Same sense, opposite classification.
5. `debate-transcript/2nd_Gore-Bush#392`: `(VP seen (S (NP-SBJ them) (VP make (NP some calls ...))))`.
   F `S`: the perceived entity is buried in the small clause.
6. `debate-transcript/2nd_Gore-Bush#53`: `(VP keep (NP-2 our military) (S-CLR (NP-SBJ *PRO*-2) (ADJP-PRD strong)))`.
   F `NP S-CLR`: a secondary predicate made a complement by -CLR and control.
7. `court-transcript/Day3PMSession#1327`: `(VP ask (NP-2 you) (S (NP-SBJ *PRO*-2) (VP to (VP read ...))))`.
   F `NP S`; *ask* needs 13 frames to cover 80% of its tokens.
8. `court-transcript/Day3PMSession#353`: `(VP got (NP this job) (ADVP-TMP *T*-2))`
   (*when I got this job*). M TMP, empty: the backbone loses *when*–*got*.
9. `court-transcript/Day3PMSession#340`: `(VP leaving (NP-TMP tomorrow))`.
    M TMP; untagged, `VP → VBG NP` looks transitive.
10. `court-transcript/Day3PMSession#83`: `(VP 's (RB not) (NP-PRD my intention))`.
    M uRB, only because copular *be* is the lexical verb; elsewhere *not*
    sits in the auxiliary VP (*n't* in 2).
11. `debate-transcript/3rd_Bush-Kerry#265`: `(VP increased (NP Pell Grants) (PP-EXT by a million students))`.
    M EXT: the difference argument of a degree verb.
12. `debate-transcript/2nd_Gore-Bush#86`: `(VP put (NP our troops) (PP-LOC-CLR all around the world))`.
    F `NP PP-CLR`: CLR, not PUT. Of *put*'s 199 PP/ADVP daughters tagged
    CLR, LOC or untagged, 32 are LOC or untagged (modifiers).
13. `movie-script/JurassicParkIV-Scene_3#120`: `(VP place (NP you) (PP under arrest))`.
    M uPP. *place*'s PPs: 15 untagged, 12 LOC, 11 LOC-CLR, 9 CLR.
14. `court-transcript/Day3PMSession#280`: `(VP (VBZ clarifies) (CC and) (VBZ reasserts) (SBAR what I have ...))`.
    *reasserts* gets no record; *clarifies* is not flagged as coordinated.
15. `debate-transcript/2nd_Gore-Bush#883`: `(VP need (NP gas pipelines) (S-PRP (NP-SBJ *PRO*-1) (VP to bring the gas down)))`.
    M PRP: the purpose is the needed thing's, not an agent's.

## Findings

1. **In the information-theoretic sense the claim holds, robustly.** The
   verb removes 38% of the bias-corrected entropy of its complement frame
   and 10% of its modifier set's (Table 1); at matched granularity 48% vs
   14% (12 vs 13 types) and 39% vs 12% (83 vs 74). The gap survives
   dropping *be* or the six commonest verbs and restricting to lemmas ≥ 100;
   with modifiers above the VP it widens (7%).
2. **Within a verb, complements are not less varied than modifiers.** Mean
   rarefied entropy is 2.12 bits for frames and 1.98 for modifier sets; 42
   of 102 verbs have the more varied modifiers; the two are uncorrelated
   (r = 0.04). What sets complements apart is that the verb decides which
   frames vary; for modifiers it hardly matters which verb it is.
3. **Promiscuity belongs to types, not classes** (Table 2). The NP object
   meets 430 of 457 lemmas (94% of the independence expectation, KL 0.76),
   like LOC (0.71) and MNR (0.72). PRD, SBAR, S, PRT, CLR and DTV are
   selective (1.4–4.4 bits); so are the modifiers DIR (2.78: *go*, *come*,
   *walk*, *move*) and EXT (3.85: *increase*, *rise*). uRB is selective only
   through the artefact of example 10.
4. **Frame-faithfulness depends on the tagging convention.** *walk* is
   frame-faithful (`0` in 148 of 198) but modifier-promiscuous because its
   paths are DIR; *talk* is faithful because its PPs are CLR. The
   promiscuous verbs have many CLR, particle and dative options (*bring*,
   *set*, *put*, *pay*), control or small clauses (*ask*, *call*, *keep*),
   or frames split by empties (*say*: S vs S(\*T\*) quotations; *write*,
   *call*: passives).
5. **Empty elements add verb-specific distinctions at the verb's own rate.**
   They change 12.9% of complement frames and raise H(frame) from 3.46 to
   4.03 bits, but U is unchanged (0.382 vs 0.383). NP(\*) concentrates on
   *use*, *call*, *base*, *make*; NP(\*T\*) on *do* and *say*; most changed
   are *describe* (46%), *require* (41%), *build*, *do*, *say*, *call*. The
   empty modifiers are the \*T\* of *when/how/where/why*, a fifth of all TMP
   and MNR, which the backbone loses (example 8).
6. **Function tags carry much of the verb-specific information.** With
   categories only, frame U falls from 0.38 to 0.31; category predicts role
   poorly for PP (30% complement), SBAR (70%) and S (77%), well for NP (96%),
   ADJP and PRT (Table 4).
7. **About one VP daughter in six has a doubtful role** (Table 5): untagged
   PPs, CLR daughters, overt-subject S complements and negation are 16.0% of
   daughters, 18.5% with DIR. 15% of untagged PPs are passive agents. Verb
   plus preposition fixes the tag for only 73% of PPs (*work with* 33 CLR /
   29 untagged, *focus on* 12 / 31, *describe as* 21 / 7, *attribute to* 4 /
   12), and PUT is practically unused. Part of this is real ambiguity
   (comitative vs collaborative *with*; goal vs recipient *to*: *send* DIR
   26, DTV 30), part annotator variation (example 4).
8. **Levin and Dowty, where the data allow.** *Causative/inchoative*
   (Levin 1993 §1.1.2.1, from memory): *break*, *open*, *close*, *change*
   are 27–40% without an NP complement and 55–65% transitive; unaccusative
   *arrive*, *die*, *happen*, *come* 95–100% without; *take*, *buy* 4–13%.
   Without traces, passives are 8% of the backbone's NP-less *break*, 18%
   *open*, 22% *close*, 20% *change*, 47% *develop*, 85% *fill*. *Dative*:
   *give* NP NP 253 vs NP PP(*to*/*for*) 66 DTV + 9 CLR + 4 untagged (+11
   PRP/BNF/TMP); *tell* 55 vs 1. *Spray/load* is untestable (*load* 17,
   *spray* 4, *pour* 12 tokens; no PUT). *Proto-roles* (Dowty 1991) are not
   annotated. The clearest Proto-Agent outside the subject, the by-phrase,
   is classed as a modifier. PRP+MNR modifiers, classic agentivity
   diagnostics (Lakoff 1966, from memory), are rarest with statives and
   attitude verbs (per 100 tokens: *agree*, *suggest* 1.5, *seem* 2.2,
   *mean* 2.3, *believe* 3.2, *be*, *think* 3.5) and commonest with
   activities (*work* 38.6, *send* 27.9, *pay* 23.6, *move* 22.6, *use*
   21.8); exceptions (*need* PRP 14.6, example 15; *thank* MNR *so much*)
   make the fit rough.
9. **Spoken vs written.** Spoken verbs have fewer VP-internal modifiers
   (35.4 vs 47.0 per 100; 70% vs 62% have none) but as many above the VP
   (24.0 vs 24.7). TMP is the commonest semantic type in every genre except
   movie-script (DIR: stage directions), non-fiction (LOC, MNR) and
   solicitation-brochures (PRP; commonest *to help ...*, 16 of 162).
   Untagged PPs lead in expository writing (technical 24.5, essays 18.3,
   govt-docs 15.6; 14–22% of them passive agents); LOC and TMP peak in
   travel-guides; ADV in fiction and ficlets (81% S-ADV clauses). Verb
   predictability is similar in both, slightly higher in spoken at equal n
   (see Counts); a small effect from one random sample.

## What it means

**(a) Backbone and fast parser.** Without tags the backbone cannot tell
complement from modifier among PPs and clauses, and NP-TMP looks like an
object (example 9); without traces, passives merge with intransitives and
hide the causative alternation. The data favour conditioning complements on
the head verb and generating modifiers largely independently of it, as
Collins's Model 2 does with complement marking and subcategorization frames
(Collins 1999, from memory). The bare backbone offers nothing to condition
on except the learned function-tag table (`docs/flat-semantics.md`, CLR F1
45.6), and the doubtful PPs of Table 5 cap what can be learned from MASC.

**(b) Verb frames and the complement/modifier distinction.** The PTB tags
draw a line that correlates well with selection (U ≈ 0.4–0.5 vs ≈ 0.1), with
systematic exceptions: DIR with motion verbs, EXT with degree verbs, and a
promiscuous NP object. Frame inventories inherit annotation choices (CLR vs
untagged, unused PUT, DIR vs DTV); where the tags are inconsistent they
record annotator decisions as much as lexical facts. Modifiers are nearly
verb-independent, yet they are what varies most by genre (Table 6), as
their communicative role leads one to expect.

**(c) Flat neo-Davidsonian semantics.** Modifiers' near-independence of the
verb supports treating them as independent event conjuncts
(Davidson/Parsons), kept out of verb entries. Complements are where lexical
entries matter, and on gold trees without traces `interp.Flat` gets several
wrong: passive subjects (5,640 NP \* objects) become `sbj`, passive agents
`by`, and the 2,982 \*T\* adverbials relate to no verb. Keeping NP-LGS and
the adverbial \*T\* traces for gold trees would repair most of this
cheaply.

## Annotation errors found

| id | what is wrong |
|---|---|
| `debate-transcript/3rd_Bush-Kerry#28` | `(-NONE- *PRO-1)` for `*PRO*-1`; `empty_kind` then yields a subject kind `*PRO` |
| `twitter/tweets1#134` | `(-NONE- *RNR-2)` for `*RNR*-2` |
| `spam/ucb31#5` and others | 30 leaves of literal asterisks tagged `-NONE-`: `**` ×16 (e.g. `(LS (-NONE- **))`), `***` ×5, `****` ×3 (`jokes/jokes10#99`), longer in `enron/53536#30`, `enron/9159#15`, `spam/221197#12`, `w3c/lists-003-2148080#16` |
| `debate-transcript/2nd_Gore-Bush#286` vs `3rd_Bush-Kerry#836` | *work with* NP untagged vs PP-CLR in the same sense (one of 49 mixed pairs) |

Questionable classifications in `tools/masc/verbframes.py` (not fixed):
1. `role()` never looks inside a PP: 974 by-phrases with NP-LGS count as
   modifiers (example 1).
2. RB daughters are modifiers, so negation is recorded only under lexical
   *be* (809 of 955 uRB, lemmas ≥ 20); 2,697 uRB in auxiliary VPs are not.
3. Only the lexical VP's daughters are collected: 17,213 modifiers (36%) in
   auxiliary VPs and at clause level are missed.
4. Flat verb coordination `(VP V CC V ...)` gives one record: 438 verbs get
   none, and the first is not flagged `vp-coordination` (example 14).
5. `frame_backbone` keeps PRD/CLR/DTV, which the backbone lacks; it is the
   frame without empties, not what the parser sees (hence Table 1's
   "categories only" row).
6. VOC is in `MODIFIER_TAGS`: vocatives count as verb modifiers (71 in the
   VP, 553 at clause level).

## Open questions

- How much CLR/untagged variation is disagreement, how much ambiguity?
  Double annotation of the 49 mixed pairs would tell.
- Would treating DIR and PP-LGS as complements sharpen the selectional
  split? Testable by changing `mtype`/`role` locally.
- Do Levin classes predict per-verb modifier entropy (which tracks motion
  and activity) better than frequency? Needs a class mapping for the 457
  lemmas, not built.
- Clause-level modifiers shared by coordinated verbs are counted once per
  verb; does this bias the comparison above the VP?
