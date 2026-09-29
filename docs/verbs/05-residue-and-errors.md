# The residue of subjectless verbs, and annotation errors in MASC verb frames

Of the 146 verbs whose missing subject `verbframes.py` could not account
for, 136 (93%) are annotation errors, 9 are genuinely subjectless (subject
drop in speech and tweets, *see?*, *remember?*), and 1 is a construction
the classifier misses (a right-node-raised VP). Unlike TIGER, MASC's odd
subjectless frames are not a trace of complex coordination: they are
mostly a missing or untagged empty subject in a non-finite clause, which
the backbone would have dropped anyway. Beyond subjects, a hand check of
299 random verbs finds a clear frame error in 4 (1.3%, 95% interval
0.4–3.4%); errors are about eight times as frequent among frames that occur
only once.

## Question

When the stripped material has explained all it can about missing subjects,
what is left (unexplained 119, question or inversion 10, clause
coordination 8, imperative 7, fragment or headline 2), and how much of it,
and of other odd frames, is annotation error rather than grammar?

## Data and method

Inputs as in the brief, with `$S` =
`/tmp/claude-0/-home-user/32748c59-1ad9-5140-855d-e9a1edeaf50a/scratchpad`:
`$S/verbs/verbs.jsonl` (70,101 verbs) and the raw trees in `$S/masc/data`.
All counts come from one helper script, which reuses `verbframes.py`'s
`Node`, `number`, `nodes`, `is_aux_vp` and `empty_kind`:

```
cd tools/masc/verbs; R="python3 residue.py $S/masc/data $S/verbs/verbs.jsonl"
$R residue; $R classify; $R table     # the 146 with trees; mechanical; hand
$R rates; $R wacky; $R singletons     # -SBJ over all clauses; other searches
$R sample 150 20260928; $R sample 150 7; $R show 'essays/Ohio_Steel#17' 18 2
```

`classify` sorts each residue verb by its clause's daughters: an untagged
empty NP; an untagged overt NP before the VP (after the verb in SQ); a
preceding sister S holding only an -SBJ; or no subject position at all. I
then read every one of the 146 trees. `HAND` in the script records the 21
where I overrode the mechanical class; `OTHER_ERRORS` lists the 54 errors
found outside the residue. The error-rate sample is 2 × 150 verbs drawn
with fixed seeds (one verb drawn twice), each read with its clause. I counted as an error only
what contradicts the Penn Treebank bracketing guidelines (Bies et al. 1995)
as MASC otherwise applies them: missing or wrong -SBJ/-PRD, wrong
attachment, wrong or malformed empty elements, a verb mistagged. I did not
count doubtful -CLR decisions or disfluent speech rendered faithfully.

## Counts

Hand classification of the residue (rows: verbframes' cause; columns: my
class):

| cause (verbframes) | untagged subject | no empty subject | bracketing | classifier gap | genuine | total |
|---|---|---|---|---|---|---|
| unexplained | 65 | 36 | 15 | 0 | 3 | 119 |
| question or inversion, no SBJ | 1 | 0 | 4 | 0 | 5 | 10 |
| clause coordination | 6 | 1 | 0 | 1 | 0 | 8 |
| imperative | 3 | 4 | 0 | 0 | 0 | 7 |
| fragment or headline | 0 | 1 | 0 | 0 | 1 | 2 |
| total | 75 | 42 | 19 | 1 | 9 | 146 |

Untagged subjects: 53 empty (37 `*-n`, 13 `*PRO*`, 2 `*T*`, 1 tagged
NP-ADV) and 22 overt (18 NPs, 2 clausal subjects, 1 SINV subject, 1 title).
Missing empty subjects: 22 infinitives (10 of them extraposed, 8 infinitival
relatives or purposes), 14 gerunds or participles, 5 imperatives, 1 bare
infinitive.

How consistently MASC marks subjects (`rates`; every S, SQ, SINV with a VP):

| clause, first word of VP | -SBJ | untagged empty NP | untagged overt NP | none |
|---|---|---|---|---|
| S, finite or bare | 48,987 | 4 | 14 | 24 (2 IMP) |
| S, to-infinitive | 8,107 | 31 | 0 | 24 |
| S, -ing/-en | 5,263 | 13 | 1 | 14 |
| SQ | 1,373 | 0 | 1 | 5 |
| SINV | 494 | 0 | 1 | 2 |

So 99.8% of clauses have an -SBJ daughter. Extraposed infinitives (indexed
S under VP) lack a subject in 9 of 259 (3.5%), other infinitives in 46 of
7,903 (0.6%); no genre stands out (0–1.7% per genre).

Searches for other odd frames (`wacky`, `singletons`):

| search | hits | confirmed errors |
|---|---|---|
| two overt NP objects plus a PRD | 0 | 0 |
| three or more overt NP objects | 1 | 0 (*ICH* extraposition) |
| two or more PRD complements | 2 | 2 |
| VB* on a non-verb form | 28 | 2 (the rest: 24 × "=" as VBZ; dialect "it" = *hit*) |
| S complement with two -SBJ | 1 | 1 |
| clause with two -SBJ daughters | 4 verbs, 3 sentences | 3 sentences |
| VP daughter with more than one function tag | 1,408 | 5 NP-SBJ inside VP, 2 duplicated tags; the rest legitimate (LOC-PRD, CLR-LOC, PRD-PRP ...) |
| verbs with an NP object in ≥95% of ≥20 active uses (62), used with no object, clause or trace | 47 | 7 |
| frames occurring once (208 of 559 distinct full frames) | 208 | 23 (11%) |
| random sample of verbs (2 × 150 draws, 1 repeat) | 299 | 4 (1.3%; 95% CI 0.4–3.4%) |

Singletons are unusual chiefly by an empty complement (93: *ICH*, *RNR*,
*?*, *T* PP), three or more complements (50), a rare category (35: quoted
SQ/SBARQ/SINV, UCP, EQUA), or a malformed tag (4).

## Examples

1. `debate-transcript/3rd_Bush-Kerry#233` *blame*: extraposed infinitive with no subject.
   `(SQ (VBZ is) (NP-SBJ (NP it) (S *EXP*-1)) (ADJP-PRD fair) (S-1 (VP (TO to) (VP (VB blame) (NP ...) ...))))`
   full [NP PP-CLR], backbone [NP PP-CLR]. Should be `(S-1 (NP-SBJ *PRO*) (VP to ...))`.
2. `ficlets/1402#74` *kissing*: control subject present but untagged.
   `(VP (VBP say) (S *T*-1) , (S-ADV (NP (-NONE- *-2)) (VP (VBG kissing) (NP his cheek))))`
   full [NP]. `NP` should be `NP-SBJ`; one of 37 such `*-n`.
3. `jokes/jokes12#67` *was* ("clause coordination"): overt subject untagged.
   `(S (LST 3.) (S (NP (PRP He)) (VP (VBD was) (ADJP-PRD sure ...))) , (CC and) (S (NP-SBJ his Mother) ...))`
   full [ADJP-PRD]. The coordination is irrelevant; *He* lacks -SBJ.
4. `debate-transcript/2nd_Gore-Bush#608` *made*: subject closed off in its own S.
   `(S (S (NP-SBJ the governor)) (VP (VBD made) (NP an extensive statement ...)) (CC and) (S (NP-SBJ I) ...))`
   full [NP]. The S bracket should include the VP.
5. `essays/Ohio_Steel#17` *fail*: VP outside the SQ that holds its subject.
   `(SBARQ (CC But) (WHADVP-1 why) (SQ (VBD did) (NP-SBJ the brave attempts ...)) (ADVP-TMP ultimately) (VP (VB fail) (ADVP-PRP *T*-1)))`
   full []. Same error in `jokes/jokes4#32`.
6. `journal/Article247_328#5` *cried*: subject inside the VP.
   `(SINV (S-TPC-1 " Giuliani makes ... ") (VP (VBD cried) (S *T*-1) (NP-SBJ (NNP Time)) (NP-TMP this week)) .)`
   full [S(*T*) NP] (the subject is read as an object). Should be `(SINV S-TPC (VP cried S) (NP-SBJ Time) ...)`.
7. `court-transcript/Day3PMSession#322` *say*: right-node-raised VP, correctly annotated.
   `(S (S (NP-SBJ he) (VP did n't (VP *RNR*-5))) (CC or) (S (NP-SBJ she) (VP did n't (VP *RNR*-5))) (VP-5 (VB say) (NP it)))`
   full [NP]. Subjects *he*, *she* are in the conjuncts; verbframes should follow the index.
8. `fiction/hotel-california#468` *remember*, genuine: discourse tag *remember?*
   `(SQ-TPC-1 (S She does n't drink) , (SQ (VP (VB remember))))`.
9. `fiction/hotel-california#511` *Mind*, genuine: subject and auxiliary dropped.
   `(S " (VP (VB Mind) (SBAR-ADV if I join you)) ?)`.
10. `philanthropic-fundraising/116CUL032#16` *call*: two subjects in one clause.
   `(VP (VB hesitate) (S (NP-SBJ (-NONE- *-1)) (NP-SBJ (-NONE- *PRO*)) (VP (TO to) (VP (VB call) (NP me) ...))))`
11. `non-fiction/CUP1#189` "2": a superscript tagged as a verb.
   `(PRN (NP (NP (CD 1324) (NNS km)) (VP (VBN 2) (NP (-NONE- *)))))`, full [NP(*)]: a passive verb *2*.
12. `court-transcript/Day3PMSession#748` *put*: the object lost to a title.
   `(VP (VB put) (PP-TTL (IN Of) (NP Pandas and People)) (PP-LOC-CLR in the science class))`
   full [SBJ(*PRO*) PP-CLR]; the title *Of Pandas and People* is an NP object. Same in #767.
13. `travel-guides/WhereToHongKong#23` *carrying*: object made subject of the next verb.
   `(S (S (NP-SBJ Two bronze lions) (VP , (VBG carrying) (PRT out))) (S (NP-SBJ feng shui principles) (VP , (VBP guard) (NP its doors))))`
14. `face-to-face/Bmr021#655` *got* (random sample): object tagged predicate.
   `(S (ADVP-LOC here) (NP-SBJ I) (VP (VBD got) (NP-PRD forty-six point five)))`, full [SBJ NP-PRD].
15. `spam/ucb45#6` *are* (random sample): predicate outside the VP.
   `(SINV (ADVP-TMP Not only) (VP (VBP are)) (NP-SBJ we) (NP-PRD the best place ...))`, full [SBJ].

## Findings

1. **The residue is annotation error.** 136 of 146 (93%, 95% CI 88–97%)
   are errors; 9 are genuinely subjectless; 1 is the classifier's gap (ex.
   7). The errors are of three kinds: a subject present but without -SBJ
   (75; ex. 2, 3), a non-finite clause or imperative with no empty subject
   at all (42; ex. 1), and a subject bracketed away from its VP (19; ex.
   4–6).
2. **Most errors are in empty subjects.** 95 of the 136 concern only an
   empty subject (53 untagged, 42 missing); the backbone, which deletes empty
   elements, sees the same frame either way. The other 41 (22 overt
   untagged subjects, 19 bracketing errors) change what the backbone sees.
   The untagged empty subjects are mostly controlled `*-n` in purpose and
   adverbial clauses (`S-PRP (NP (-NONE- *-1))`, ex. 2); missing ones are
   concentrated in extraposed infinitives (3.5% of them vs 0.6% of other
   infinitives; ex. 1).
3. **Genuine subjectless verbs are few and are not coordination.** The 9
   are discourse tags (*see?*, *remember?*), dropped subjects in speech,
   fiction dialogue and tweets (*Mind if I join you?*, *Cleaned out my
   closet*, *is working*), an abandoned start under EDITED, a byline
   fragment, and a source typo (*What do think about ...*). In MASC a
   subject shared by conjoined predicates is annotated by VP coordination
   under one S, so sharing never produces a subjectless frame; the one
   coordination case is right-node raising of a VP (ex. 7). This is the
   answer to the TIGER analogy: here odd subjectless frames mark annotation
   error, not coordination or ignored secondary edges.
4. **Classifier issues in `verbframes.py`** (not fixed, as instructed):
   - *"clause coordination" never explains a missing subject.* Lines
     147–149 set the flag when the clause's parent is a clause with any CC
     daughter, and line 196 then gives it as the cause. In PTB `S → S CC S`
     there is no subject above to share. 0 of the 8 are shared subjects
     (6 untagged, 1 missing, 1 RNR). The flag also fires on 164 verbs whose
     clause is the only clause daughter of its parent, e.g. an adverbial
     clause after a sentence-initial *So* (`court-transcript/Day3PMSession#71`,
     *So having said that, ...*) or an SQ under SBARQ (`#74`).
   - *Right-node-raised VPs.* The `rnr` flag looks for *RNR* traces inside
     the VP, not at the VP's own index (`VP-5`), so ex. 7 is unexplained.
   - *Verbs outside a VP are not counted.* 677 verbs are direct daughters of
     an SQ or SINV with no VP, mostly copulas (`(SQ (VBD were) (NP-SBJ they)
     (PP-LOC-PRD at the meeting))`, `court-transcript/Day3PMSession#260`);
     their frames are missing from the 70,101.
   - *Only the first verb of a VP is recorded* (line 126): 437 further verbs
     in VPs like `(VP (VB say) (CC or) (VB imply) SBAR)`
     (`essays/A_defense_of_Michael_Moore#76`) get no record.
   - *Lexical VPs with no VB\* head are skipped*: 802, mostly ellipsis
     (346 `*?*`, 147 bare MD) and gapping (NP=1 ...), but also 11 whose
     head is tagged JJ (5), NN (4) or the non-PTB BES (2); the JJ and NN
     ones are mistagged verbs (see the error table).
   - *Malformed data become categories*: `*PRO-1` gives the cause "empty
     subject *PRO" and `*RNR-2` the frame `NP(*RNR)`; duplicated tags give
     frames like `PP-CLR-CLR`.
   - A suggestion, untested: accepting an untagged NP daughter before the
     VP (after the verb in SQ/SINV) as the subject would absorb 70 of the
     75 untagged subjects.
5. **Beyond subjects, frame errors are rare and cluster in rare frames.**
   The targeted searches confirm 54 further errors (table below): spurious
   or missing -PRD (object tagged PRD, ex. 14; adverbial *tonight* as a
   second PRD), NPs misplaced outside their VP or PP (ex. 12, 13, 15),
   untagged temporal or locative NPs read as objects (*tomorrow*,
   `twitter/tweets1#386`), two subjects in a clause (ex. 10), a verb
   mistagged (ex. 11; *refining* as NN), malformed empty elements, and
   clauses without an S node (labels IP, RS; NP-SBJ + VP under INTJ, ADJP,
   UCP). Of the 47 "transitive verbs with no object", 40 are real
   (unspecified objects, *provide for*, *protect against*, *gave in*,
   unfinished speech) and 7 are errors. Of the 208 singleton frames, 23
   (11%) contain a confirmed error, against 1.3% in the random sample.
6. **Estimated error rate.** In 299 random verbs I found 4 clear frame
   errors (ex. 14, 15; `blog/Anti-Terrorist#42`, NP-SBJ and VP under UCP;
   `movie-script/pirates#808`, *bodes ill* with *ill* as an NP object):
   1.3%, 95% interval 0.4–3.4% (Clopper–Pearson). The exhaustive searches
   give a lower bound of about 190 verbs (136 + 54), 0.27% of 70,101; they
   find only the kinds searched for, so the sample figure is the better
   estimate. Excluded: -CLR decisions (too inconsistent in PTB practice to
   call errors verb by verb) and faithful renderings of disfluent speech.
   So roughly one verb frame in a hundred is wrong, at most about one in
   thirty; the residue's subject errors (0.19% of verbs) are a small part.
7. **Duplicate text.** `blog/Uprooted_Bike` and `blog/Uprooted_Farming-on-Sand`
   are one document twice (79 of 79 sentences), so 8 residue items are 4
   pairs; counts here are not deduplicated.

## What it means

**(a) The context-free backbone and the fast parser.** The errors that
survive stripping are the 41 overt-subject and bracketing errors and the
misattachments of finding 5. They give the backbone rules that exist only
because of them (`S → S VP CC S`, `SBARQ → CC WHADVP SQ ADVP VP`, `VP → NP
ADVP VBZ NP`, `INTJ → VP`, `ADJP → ADJP VP`, categories IP and RS), or hide
in ordinary rules: *cried Time this week* becomes `VP → VBD NP NP`. Each is seen a handful of times; they add
spurious ambiguity to the forests rather than affecting common analyses
(speculative: I have not measured parser output). Pruning rules seen once
or twice, or applying a short correction list to the gold trees before
reading off the grammar, would remove most of them. The untagged subjects do
not matter to the CFG, which has no function tags, but they are wrong
training data for `interp.FunctionTable`'s SBJ (a few dozen of 4,249 test
SBJs is negligible for its 98.5 F1).

**(b) Verb frames and the complement/modifier distinction.** At about 1%,
annotation error is well below the variation that -CLR and -PRD judgments
already introduce, so it does not threaten frame statistics for common
verbs. It does dominate the tail: a frame seen once is roughly eight times
as likely as a random one to be an error, so any frame inventory should use
a frequency threshold or hand-checking before admitting singletons. For the
complement/modifier line specifically, the errors that cross it are (i)
temporal and locative NPs without their tag, which the tag-based rule reads
as NP objects, (ii) -PRD on objects or adverbials, and (iii) titles and
misplaced NPs that remove an object. A cheap check is an NP "object" headed
by a temporal noun or by a place name after *take place*. Subject errors
hardly touch complements, since nearly all are in controlled clauses whose
subject is empty in any case.

**(c) The flat neo-Davidsonian semantics.** `interp.Flat` names the relation
of a daughter by its tags; an overt subject without -SBJ gets a
configurational relation (probably `mod` or `obj`), not `sbj`, so the 22
overt-untagged cases lose the agent atom of their event, and the 19
bracketing errors relate the subject to the wrong referent or to none (ex.
6 makes *Time* an object of *cried*). Missing or untagged empty subjects do
not matter to Flat as it stands, which ignores traces; they would matter as
soon as traces are kept for gold trees (the 39,212 empty elements of
flat-semantics.md), where 95 controlled subjects would lack their `sbj`
link. On parser output the learned function table would probably tag most
clause-initial NPs SBJ regardless (speculative), so these gold-tree errors
matter most for gold-tree readings.

## Annotation errors found

The 136 residue errors (verb occurrence: `id:verb`; positions in
`residue.py table`):

| kind | ids |
|---|---|
| subject without -SBJ (75) | 2nd_Gore-Bush#94:resolve, 2nd_Gore-Bush#174:think, 3rd_Bush-Kerry#19:signal, 3rd_Bush-Kerry#350:learning, 3rd_Bush-Kerry#579:raise, Bed012#137:make, Bed012#144:is, Bmr021#205:make, Bmr021#258:reorganize, Anti-Terrorist#49:do, Effing-Idiot#294:lose, Fermentation_Eminent-Domain#47:make, Uprooted_Bike#37:say, Uprooted_Farming-on-Sand#37:say, blog-jet-lag#13:subsides, blog-monastery#61:learning, detroit#209:ask, enron/12176#30:chronicle, enron/175841#139:meet, spam/111344#7:re-validate, spam/111367#12:help, Dear_e-mail_user#4:login, Dear_e-mail_user#4:authenticate, lists-003-2152883#5:is (×2), Ant_Robot#166:enter, Black_and_white#48:be, ficlets/1399#122:sitting, ficlets/1399#289:was (S-NOM-DIR), ficlets/1399#573:ringing, ficlets/1400#8:remembering, ficlets/1400#412:talk, ficlets/1401#139:going, ficlets/1401#232:looking, ficlets/1401#265:throwing, ficlets/1401#276:be, ficlets/1401#467:looking, ficlets/1402#74:kissing, ficlets/1403#397:think, captured_moments#5:visit, captured_moments#5:send, hotel-california#120:listening, hotel-california#156:end, Env_Prot_Agency-nov1#10:develop, #35:ensure, #104:develop, #197:reduce, jokes11#47:raising, jokes12#67:was, jokes2#15:is, jokes2#66:makes, jokes2#218:grant, jokes4#37:sitting, jokes4#37:photocopying, 110CYL068#14:was (S-NOM), NWF1#33:save, JurassicParkIV-INT#34:is, pirates#510:stealin', pirates#1836:see, NYTnewswire2#57:scare, NYTnewswire4#47:get, NYTnewswire6#15:rolled, NYTnewswire9#10:line, CUP1#128:demonstrate, rybczynski-ch3#298:have, 1471-2091-2-9#121:be, pmed.0010029#9:calculate (NP-ADV), WhatToHongKong#113:be, WhereToHongKong#460:overlooking, WhereToHongKong#496:relax, :dine, :play, tweets1#483:work, tweets1#624:hanging, tweets1#914:vote |
| no empty subject (42) | 2nd_Gore-Bush#152:rebuild, 2nd_Gore-Bush#314:acting, 3rd_Bush-Kerry#233:blame, #345:vote, #811:see, #858:protect, Bmr021#751:combine, Anti-Terrorist#49:invite, Uprooted_Bike#36:tuning, #66:riding, #66:driving, Uprooted_Farming-on-Sand#36:tuning, #66:riding, #66:driving, blog-jet-lag#53:reset, detroit#125:take, lessig_blog-carbon#94:see, enron/54263#8:coming, ucb40#5:spend, ucb43#4:spend, lists-046-11493928#76:cover, Ant_Robot#231:assign, ficlets/1401#61:worry, ficlets/1402#82:Grabbing, The_Black_Willow#208:walk, #219:Emerging, captured_moments#114:remember, hotel-california#81:realize, jokes5#145:Drive, jokes5#146:Fill, :hit, :let, jokes8#31:right, ArticleIP_1059#15:Following, pirates#160:Recover (imperative, no IMP), pirates#704:putting, NYTnewswire6#56:keep, NYTnewswire8#10:Intending, :ease, rybczynski-ch3#181:divine, tweets1#288:watching, tweets2#645:go |
| subject and VP in wrong constituents (19) | 2nd_Gore-Bush#608:made, Uprooted_Bike#12:are, Uprooted_Farming-on-Sand#12:are, Fastest_Reader#21:is, lists-046-12119260#4:was, A_defense_of_Michael_Moore#76:is, Madame_White_Snake#53:harmed, jokes11#32:rushed, 20020731-nyt#166:are, tweets1#510:hosting, tweets2#542:play (all: subject alone in a sister S); Ohio_Steel#17:fail, jokes4#32:make (VP outside SQ); Black_and_white#50:lifted (subject inside WHPP); jokes1#172:screamed, Article247_328#5:cried, rybczynski-ch3#70:is (subject inside VP); NYTnewswire2#19:put (subject trace as object, by-phrase PP-LOC); rybczynski-ch3#195:granted ("for granted" as SBAR + S; cf. #42) |

The 54 errors found outside the residue (`OTHER_ERRORS` in the script has one
line each, with verb positions):

| kind | id: verb, and what is wrong |
|---|---|
| verb that is not a verb | non-fiction/CUP1#189: "2" (×2), superscript of km² tagged VBN with VP and passive trace |
| verb not tagged as a verb | JJ heading a VP: wsj/wsj_0027#0 resigned, wsj/wsj_0151#7 scared, wsj/wsj_0173#0 peaked, blog/Fermentation_HR5034#19 shocked, journal/Article247_328#2 exact; NN heading a VP: spam/FBI_urgent#33 advice(d), journal/VOL15_3#312 effect, wsj/wsj_0120#11 set, fiction/captured_moments#512 island-hopping; NN elsewhere: govt-docs/chapter-10#157 refining (as NP-PRD of *been*), fiction/easy_money#41 push |
| two subjects | philanthropic-fundraising/116CUL032#16 call (NP-SBJ *-1 and NP-SBJ *PRO*); journal/VOL15_3#124 is ×2 (two clauses flat in one S); journal/VOL15_3#136 has (dislocated NP tagged SBJ) |
| wrong or extra -PRD | fiction/Nathans_Bylichka#735 looked (NP-TMP-PRD *tonight*); movie-script/pirates#1153 's (NP-PRD and ADVP-LOC-PRD); non-fiction/ch5#81 transforms and face-to-face/Bmr021#655 got (object tagged NP-PRD); fiction/hotel-california#52 called (predicate trace without -PRD) |
| object lost or misplaced | court-transcript/Day3PMSession#748, #767 put (title bracketed PP-TTL); twitter/tweets2#666 Thank and twitter/tweets2#19 gain (object outside VP); travel-guides/WhereToHongKong#23 carrying (object made subject of *guard*); non-fiction/rybczynski-ch3#256 judging (conjuncts outside the PP); solicitation-brochures/aspca1#39 handle (no object gap); spam/ucb45#6 are (NP-PRD outside VP) |
| adverbial read as object | twitter/tweets1#386 do (*tomorrow* untagged); enron/9085#12 take (location of *take place* untagged); movie-script/pirates#808 bodes (*ill* as NP) |
| traces misplaced or malformed | face-to-face/NapierDianne#158 ran (*T* outside PP); essays/Black_and_white#140 fill (*RNR* inside PRT); journal/Article247_3500#3 is (NP-SBJ trace inside VP); movie-script/pirates#1823 had (small clause holds only its subject trace); debate-transcript/3rd_Bush-Kerry#28 set (`*PRO-1`); twitter/tweets1#134 Download (`*RNR-2`); govt-docs/fcic_final_report_conclusions#167 filed (passive trace `*PRO*`); fiction/cable_spool_fort#61 called (passive trace labelled UCP); blog/Effing-Idiot#56 keep (object `SBAR *PRO*`); essays/Ant_Robot#238 is (`CD (-NONE- 0)`) |
| clause without S | blog/Anti-Terrorist#42 Insult (UCP); fiction/The_Black_Willow#43 Damn (INTJ); twitter/tweets2#603 be and journal/VOL15_3#317 know (ADJP); essays/A_defense_of_Michael_Moore#48 looking (PP *According to*); essays/Ant_Robot#283 modeled (QP); blog/Effing-Idiot#22 Point (label IP-IMP-TTL); solicitation-brochures/defenders5#26 killed (label RS) |
| other bracketing | court-transcript/Day3PMSession#1151 take (SBAR 0 separated from its S); spam/ucb26#9 advertised (reduced relative as SBAR + clause) |
| duplicated tag | fiction/Nathans_Bylichka#364 were (PP-LOC-PRD-PRD); fiction/cable_spool_fort#36 took (PP-DIR-CLR-CLR) |

## Open questions

- Are the 42 missing and 53 untagged empty subjects one annotator's or one
  batch's habit? They are spread over genres; per-file annotator
  information, if MASC has it, would tell.
- How many of the 677 verbs directly under SQ/SINV are lexical (copula,
  possessive *have*) rather than auxiliaries with an elided VP? Their
  frames are absent from every count in this series.
- Would correcting the ~90 frame-changing errors measurably change the
  backbone's ambiguity or parse accuracy? I expect not; unmeasured.
- A second reader on the same 299 verbs would show how much of the 1.3% is
  my judgment, especially for -PRD.
- -CLR consistency, excluded here, is probably the larger source of noise
  in the complement/modifier split and deserves its own study.
