# Coordination and the other non-local annotations of MASC

In MASC, coordination changes verb frames mostly through one structurally visible relation, the
subject shared by conjoined verb phrases. A reading of the backbone that follows heads loses that
subject for 3,537 verbs in a non-first conjunct, and for 2,145 verbs (3.1%) this is the only thing
wrong with the frame. Right-node raising (214 traces), gapping (204 conjuncts that have no verb) and
verbs coordinated inside one verb phrase (438 verbs `verbframes.py` never records) are rarer, but
each loses whole arguments or whole events. Extraposition (*ICH*, *EXP*) involves no sharing, yet it
puts a constituent that is not an argument into the frames of 831 verbs, in the full annotation as
much as in the backbone. About six kinds of sharing link, much like TIGER's secondary edges, would
restore nearly all of these frames, and three of the six can be read off the backbone's structure
alone.

## Question

What do coordination and the other non-local annotations of MASC (VP coordination, *RNR*, gapping
`=N`, *ICH*, *EXP*, UCP, clause coordination) do to verb frames, in the full annotation and in the
context-free backbone? For each: how often it occurs, which arguments are shared and must be copied
to recover each event's participants, whether `verbframes.py` gets it right, how often a backbone
frame is wrong *only* because of coordination, and whether a bounded set of sharing relations would
suffice.

## Data and method

Script: `tools/masc/verbs/coordination.py` (standard library; imports `../masctrees.py` and
`../verbframes.py`). It rebuilds every verb record exactly as `verbframes.py` does (70,101 records,
identical to `verbs.jsonl`) and adds:

* **frame_local**, the frame that a head-driven reading of the backbone gives: the overt subject
  only if each VP between the verb and its clause passes the verb up as its head (Collins's VP rule
  over overt daughters; a VP whose head word is followed by a VP counts as transparent, as in
  `is_aux_vp`), plus the overt complements, as in `frame_backbone`. In `VP → VP CC VP` the first VP
  is the head, as in `go/interp/heads.go`, so only the first conjunct sees the subject.
  `frame_backbone` in `verbframes.py` is more generous, since it walks up through all VPs.
* **reasons** why frame_local differs from frame_full: an empty subject of a given kind, an overt
  subject shared into a non-head conjunct, or an empty complement of a given kind.
* for each coordination on the way up: the conjunct's position, whether it is the head, and the
  coordination's other daughters (before the first conjunct, between conjuncts, after the last),
  with any trace they are antecedent to.
* for each tree: gapped conjuncts (an S/VP/SINV/SQ with `=N` daughters and no verb, MD, TO or VP
  daughter), VPs with more than one verb daughter, UCPs, coordinated clauses and their other
  daughters, and every *RNR*/*ICH*/*EXP* trace with its antecedent's position.

```
S=/tmp/claude-0/-home-user/32748c59-1ad9-5140-855d-e9a1edeaf50a/scratchpad
python3 tools/masc/verbs/coordination.py $S/masc/data $S/coord   # ~10 s
python3 tools/masc/verbs/coordination.py --tables $S/coord       # all tables below
```

I checked the flat meanings (`interp.Flat`) of the examples with `cd go && go run ./cmd/readings
-annotated SUBSET.jsonl -sem`, where SUBSET is the matching lines of `$S/ann/annotated.jsonl`. Every
example below was inspected in the raw tree.

## Counts

**Table 1. Verbs under VP coordination** (flag `vp-coordination`; 3,331 coordination nodes)

| | verbs | overt SBJ | *PRO* | *T* | * | none |
|---|---|---|---|---|---|---|
| head (first) conjunct | 3,319 | 2,158 | 822 | 175 | 83 | 81 |
| non-head conjunct | 3,537 | 2,276 | 912 | 186 | 84 | 79 |
| all | 6,856 (9.8%) | 4,434 | 1,734 | 361 | 167 | 160 |

Two conjuncts: 5,845 verbs; three or more: 977. Nested coordinations on the way up: 119 verbs.

**Table 2. Why frame_local differs from frame_full** (a verb may have several reasons)

| reason | verbs |
|---|---|
| empty subject *PRO* / *T* / * | 13,458 / 3,775 / 2,225 |
| empty complement * / *T* / *?* / 0 | 5,653 / 3,111 / 118 / 89 |
| **overt subject shared into a non-head VP conjunct** | **2,276** |
| **empty complement *RNR*** | **94** (+1 written `*RNR`) |
| empty complement *ICH* | 7 |
| frame_local ≠ frame_full, all reasons | 29,033 (41.4%) |
| … **only** because of coordination sharing | **2,145 (3.1%)**: 2,086 subject, 36 *RNR*, 23 both |
| … coordination sharing together with something else | 202 |
| frame_backbone ≠ frame_full only because of *RNR* | 59 |

**Table 3. Events that get no verb record at all**

| | count |
|---|---|
| VPs with two or more verb daughters (`V CC V NP`), 352 with CC/CONJP | 381 |
| verbs after the first in those VPs: never recorded | 438 |
| gapped conjuncts (an event with no verb of its own) | 204 in 182 trees |

**Table 4. Daughters of a VP coordination other than conjuncts, CC and punctuation**

| position | kind | count |
|---|---|---|
| after last conjunct | complement, antecedent of *RNR* | 55 |
| after | complement, no trace (16 are `NP-ETC` "or whatever"; 3 are errors) | 19 |
| after | complement, antecedent of *ICH* | 6 |
| after | modifier, no trace | 83 |
| after | modifier, antecedent of *RNR* | 6 |
| before first conjunct | auxiliary (MD, TO) | 29 |
| before | modifier | 16 |
| between conjuncts | modifier (mostly `and then`, `and also`) | 371 |
| between | CODE, SYM, PRN, EDITED, INTJ … | 171 |

**Table 5. *RNR* and gapping.** *RNR*: 214 traces in 103 trees, each with an antecedent (4 of
them word-level, `NN-4`). The trace stands for NP 130, PP 35, VP 18, SBAR 12, other 19. Its host is
VP 127, PP 59, NP 15, other 13. The antecedent's parent is a coordination in 176 cases (VP 129,
NP 25, S 18, SBAR 4). **Gapping**: 204 gapped conjuncts, of which 148 are VPs in VP coordination,
45 Ss in S coordination and 11 other. They have one remnant (94), two (106) or three (4). The
correlates in the full conjunct are marked `-N` as in the PTB in 24; 168 have `=N` on both sides
and no `-N` anywhere; 12 are mixed or missing.

**Table 6. Extraposition: VP daughters that are antecedents of a trace elsewhere**

| | traces | host of trace | verbs counting the antecedent as a **complement** |
|---|---|---|---|
| *EXP* | 615 | NP-SBJ 585 (`it`) | 520 |
| *ICH* | 709 | NP 334, NP-SBJ 184, ADJP 67, WHNP 38, ADVP 38 | 314 |
| either | | | **831 verbs (1.2%)**, e.g. `SBJ ADJP-PRD SBAR` 162, `SBJ ADJP-PRD S` 141, `SBJ NP SBAR` 70 |

*ICH* antecedents are also counted as modifiers of 194 verbs.

**Table 7. The 113 verbs whose walk up stops at a UCP**

| UCP's parent | subject of the clause above | verbs |
|---|---|---|
| VP (e.g. `was [ADJP …] and [VP …]`) | overt 45, *T* 2, * 1 | 48 |
| S (predicate UCP) | *PRO* 15, overt 8 | 23 |
| NP (reduced relative) | the head noun is the subject | 7 |
| ADJP, SQ, UCP | — | 3 |
| none: the root is labelled UCP (834 root UCPs, 670 without any CC) | — | 32 |

**Table 8. Clause coordination** (2,653 coordinated S/SQ/SINV/SBARQ; 5,737 verbs flagged)

Subjects of flagged verbs: overt 5,583 (97.3%), *PRO* 121, *T* 24, * 1, none 8. Of the 450
coordinations with other daughters, those daughters are CODE 219, modifiers 156, INTJ 76, REF 52,
EDITED 42, PRN 25, vocatives 23, VP 11, NP-SBJ 5, and other material.

**Table 9. Flags against what they should mark.** A verb is "affected" if its own VP has the
trace or its antecedent as a daughter. `rnr`: 263 flagged, 108 affected, 108 both. `ich`: 714
flagged, 552 affected, 358 both. `exp`: 262 flagged, 522 affected, 19 both. `gapping`: 261 flagged,
of which 134 have a `=N` correlate among their own daughters.

## Examples

Trees are trimmed. "local" is frame_local.

1. **blog/How_soon-Fans#22**, shared subject. `(S (NP-SBJ People) (VP (VP (VBD came) (PRT in)) (CC
   and) (VP (VBD ate) (NP their food))))`. *ate*: full `SBJ NP`, backbone `SBJ NP`, local `NP`. Flat
   gives `sbj(came,people)`, `and(came,ate)`, `obj(ate,food)`, and no subject for *ate*.
2. **journal/VOL15_3#274**, RNR of an object. `(VP (VP (VBZ quotes) (NP *RNR*-1)) (CC and) (ADVP
   presumably) (VP (VBZ accepts) (NP *RNR*-1)) (NP-1 another source which …))`. *quotes* and
   *accepts*: full `SBJ NP(*RNR*)`, backbone `SBJ`, local `SBJ` for *quotes* and nothing for *accepts*. Flat gives
   `and(quotes, source)`: the shared object becomes a conjunct.
3. **court-transcript/Day3PMSession#210**, RNR across clauses. `(S (S (NP-SBJ I) (VP understand (NP
   *RNR*-1))) and (S (NP-SBJ I) (VP respect (NP *RNR*-1))) (NP-1 that argument))`. Both are `SBJ
   NP(*RNR*)` in full and `SBJ` in the backbone. Flat: `and(understand, argument)`.
4. **nyt/NYTnewswire8#15**, RNR into a PP. `(VP (VP (VBG developing) (NP *RNR*-1)) (CC and) (VP (VBG
   lobbying) (PP for (NP *RNR*-1))) (NP-1 prescription-privileges legislation))`. *lobbying*'s frame
   `SBJ(*PRO*)` is unchanged, but its PP modifier has lost its object. That can only be restored
   inside the conjunct.
5. **court-transcript/Day3PMSession#322**, RNR of a VP across clauses. `(S (S (NP-SBJ he) (VP did
   n't (VP *RNR*-5))) or (S (NP-SBJ she) (VP did n't (VP *RNR*-5))) (VP-5 (VB say) (NP it)))`.
   *say*: full `NP`, no subject; cause "clause coordination". One record stands for two events with
   two subjects.
6. **non-fiction/rybczynski-ch3#209**, verbs coordinated inside one VP. `(VP (VBZ surrounds) (CC
   and) (VBZ shelters) (NP us))`. *surrounds*: `SBJ NP`. *shelters* has no record. Flat:
   `and(surrounds, shelters)`, `and(surrounds, us)`, so neither verb gets *us* as its object.
7. **debate-transcript/2nd_Gore-Bush#751**, gapping in VP. `(VP (VP (VBZ ranks) (ADJP-CLR-1 49th …)
   (PP-2 in children …)) , (VP (ADJP-CLR=1 49th) (PP=2 for women …)) , and (VP (ADJP-CLR=1 50th)
   (PP=2 for families …)))`. *ranks*: `SBJ ADJP-CLR`. The two ranking events for women and families
   have no verb record. In Flat they become `comp(ranks, 49th)` and `and(ranks, 50th)`.
8. **debate-transcript/3rd_Bush-Kerry#150**, gapping in S, `=` on both sides. `(S (S (NP-SBJ=1
   Health-care costs …) (VP is (VP skyrocketing))) , (S (NP-SBJ=1 the cost of the war)))`. The full
   conjunct's subject carries `=1` instead of `-1`. The second event (the war's cost is
   skyrocketing) has no record.
9. **court-transcript/Day3PMSession#1320**, gapping with two remnants. `(VP (VP (VBN e-mailed)
    (NP-1 a letter …) (PP-DTV-2 to the board president …)) , (VP (NP=1 a copy) (PP-DTV=2 to Dr.
    Nilsen)))`. The verb and the subject have to be copied into the second conjunct. Its remnants
    pair with NP-1 and PP-DTV-2.
10. **debate-transcript/3rd_Bush-Kerry#311**, *EXP*. `(S (NP-SBJ (NP It) (SBAR *EXP*-1)) (VP (VBZ
    's) (ADJP-PRD important) (SBAR-1 that we do that)))`. *'s*: full = backbone = local = `SBJ
    ADJP-PRD SBAR`. The SBAR is the logical subject, not a third argument.
11. **face-to-face/ReidSandra#42**, *ICH* from the subject. `(S (NP-SBJ (NP That) (S *ICH*-1)) (VP
    (VBD was) (NP-PRD my dream) , (S-1 *PRO* to teach school)))`. *was*: `SBJ NP-PRD S`, where S
    belongs to the subject NP.
12. **face-to-face/ReidSandra#32**, predicate UCP. `(S-ADV (NP-SBJ *PRO*-1) (UCP-PRD (VP (VBG
    stomping) (CC and) (VBG crying)) and (ADJP mad) and (VP (VBG taking) (NP …))))`. *stomping* and
    *taking* are "no clause above (UCP)" and have no subject, though the S right above has
    `*PRO*-1`. *crying* has no record.
13. **journal/ArticleIP_1059#12**, annotation error. `(VP (VP (VBD acquired) (NP Palestine)) (CC
    and) (VP (VBD promised) (PP-LOC in the Balfour Declaration)) (NP the creation of a Jewish
    state))`. *promised*: full `SBJ`. Its object is attached to the coordination and has no trace.
14. **face-to-face/Bmr021#279**, annotation error. `(VP (VP (VBD did) (RB n't)) (VP (VBP know) (NP
    that)))`. This gives a spurious lexical verb *did* (`SBJ`) and a spurious VP coordination.
    *know* is local `NP`.
15. **blog/blog-jet-lag#13**, annotation error. `(S (S (PP-TMP …) , (NP this signal) (VP (VBZ
    subsides))) , and (S (NP-SBJ s$$he) …))`. *subsides* has no subject and its cause is given as
    "clause coordination", but the NP simply lacks `-SBJ`.

## Findings

1. **VP coordination is the main case, and it is structural.** 9.8% of verbs sit under a VP
   coordination (Table 1). The shared subject is a sister of the coordinated VP, so it survives in
   the backbone; only its link to non-first conjuncts is lost (3,537 verbs, 2,276 with an overt
   subject). For 2,145 verbs (3.1%) this or RNR is the frame's only defect (Table 2). `verbframes.py`
   walks up through VPs and so assigns the right subject to every conjunct of an ordinary VP
   coordination, nested (119) or under a shared auxiliary. Its `frame_backbone` already contains
   the sharing, and differs from `frame_full` for coordination reasons in only 59 verbs (*RNR*).
2. **The subject walk fails in four places.** (a) It stops at UCP. 71 verbs in predicate UCPs under
   VP or S have no subject, though the clause above has one (Table 7; example 12). (b) A VP
   right-node raised out of coordinated clauses gets no subject instead of one per clause
   (example 5). (c) Verbs coordinated inside one VP get no record after the first (438; example 6).
   (d) Gapped conjuncts get no record (204 events; examples 7–9). Together, about 720 events are
   missing from `verbs.jsonl` or recorded without a subject.
3. **Clause coordination hardly shares arguments.** Coordinated clauses carry their own subject
   (97.3% overt, Table 8), and share modifiers (156) far more often than arguments (5 NP-SBJ,
   11 VP). Of the 8 verbs whose cause is "clause coordination", only 1 is sharing (example 5). The
   other 7 are annotation errors: a subject without `-SBJ`, or an omitted subject (see Annotation
   errors).
4. **RNR is small and always within a coordination.** Of 214 traces (Table 5), 61% stand for an NP
   and 16% for a PP. In 82% the antecedent is a daughter of the coordination itself. 59 traces sit
   inside a PP (example 4), so restoring them means reaching into the conjunct.
5. **The PTB uses a trace for a shared complement and plain attachment for a shared modifier.**
   After the last conjunct, complements carry *RNR* 55 times and appear without a trace 19 times:
   16 `NP-ETC` pseudo-conjuncts ("or whatever") and 3 attachment errors. Modifiers appear without a
   trace 83 times and with *RNR* only 6 times (Table 4). So the annotation encodes the
   complement/modifier distinction at exactly this point. A complement must be in each
   conjunct's VP and is traced there. A modifier may modify the coordinated whole, and whether it
   distributes is left open.
6. **Word-level verb coordination is the unmarked alternative to RNR.** "Saw and heard X" is
   `(VP V CC V NP)` 381 times (Table 3) and RNR over VPs about 60 times. The flat analysis is used
   when the conjuncts are bare verbs, RNR when a conjunct has material of its own (example 2,
   *presumably*). Both encode the same sharing.
7. **Gapping loses events, and its indices are inconsistent.** 73% of the 204 gapped conjuncts are
   VP gapping inside VP coordination. The full conjunct's verb is most often a copula (51 of 204).
   168 conjuncts mark the correlates `=N` on both sides instead of `-N` (example 8). The `gapping`
   flag fires for 261 verbs, but 127 of them only dominate a gapped coordination further down. It
   misses full-conjunct verbs whose correlates carry `-N`.
8. **Extraposition is not sharing, but it corrupts frames in the full annotation too.** For 831
   verbs (Table 6), a VP daughter is the antecedent of an *EXP* trace (almost always in the subject
   *it*) or an *ICH* trace (in an NP inside the VP or in the subject). `role()` counts this daughter
   as a complement, being an untagged S or SBAR. So *it's important that …* is
   `SBJ ADJP-PRD SBAR` in both frames (example 10), and the SBAR is really the logical subject. The
   flags test the subtree below the verb's VP, not its daughters or its subject. `exp` therefore
   marks 19 of the 522 affected verbs, while `ich` and `rnr` over-mark (Table 9).
9. **UCP at the root is mostly not coordination.** Of 1,210 UCPs, 834 are roots, and 670 of those
   have no conjunction: they label juxtaposed pieces. In 19 of them an NP-SBJ and a VP should
   form an S (e.g. blog/Anti-Terrorist#41). Where a UCP really coordinates a VP with an ADJP, PP or
   NP (79 verbs, Table 7), it shares the subject exactly as VP coordination does.
10. **A bounded set of sharing relations would suffice.** Per event, the needed relations are:
    (i) the subject into every VP/UCP predicate conjunct (3,537 + 71);
    (ii) the dependents of a `V CC V` phrase into every verb (438);
    (iii) an RNR'd dependent into the right edge of each conjunct (214, 59 of them inside a PP);
    (iv) the verb, auxiliaries and unmatched dependents into a gapped conjunct, with remnants
    paired to correlates (204);
    (v) optionally, shared modifiers and auxiliaries distributed over the conjuncts (about 280),
    though the distributive reading is not guaranteed;
    (vi) outside coordination, a *belongs-to* link from an extraposed constituent to its host NP or
    expletive (1,324 traces, 831 frames).
    (i), (ii) and (v) follow from the backbone's structure. (iii) is visible there too, as a
    non-conjunct daughter after the last conjunct, except where the gap is inside a PP. (iv) needs
    the `=N` pairing or a heuristic by category and order. (vi) needs the index. These are the MASC
    counterparts of TIGER's secondary edges, which (from memory) TIGER uses only for constituents
    shared in coordination. The difference is that the PTB leaves the commonest case, (i),
    implicit in the structure.

## What it means

**(a) The context-free backbone and the fast parser.** Coordination adds nothing that is not
context-free, but it adds rules and misleading lexical statistics. A transitive verb whose object is
right-node raised appears as `VP → VBZ`, and its object as an extra daughter of the coordination
(`VP → VP CC VP NP`). Gapped conjuncts become verbless VPs and Ss. Of the 286 VPs in the raw trees
with no overt verbal daughter, 154 are gapped conjuncts (225 such VP nodes remain in
`annotated.jsonl` after unary collapse). These rules let the parser build a VP from `NP PP`
anywhere. Split auxiliaries (`VP → VP VP`) and `NP-ETC` conjuncts add further spurious
coordinations. The grammar formalism does not need to change. Relations (i), (ii) and most of
(iii) and (v) are structural, so a sharing step after the parse can run on parser output.

**(b) Verb frames and the complement/modifier distinction.** If sharing is restored, as
`frame_backbone` does for subjects, coordination corrupts only 59 frames. Counted locally, it
corrupts 2,145 (3.1%), and roughly 640 events have no frame at all. The owner's distinction is
written into the annotation (finding 5): shared complements are traced into each conjunct, and
shared modifiers are attached once. Frame statistics should therefore copy shared complements into
each conjunct and keep shared modifiers apart. Whether those modifiers distribute is open
(speculative: *sang and danced for three hours*). Independently of coordination, `role()` should not
count an *EXP*/*ICH* antecedent as a complement. Otherwise frames like `SBJ ADJP-PRD SBAR` (162)
are artefacts of extraposition.

**(c) The flat neo-Davidsonian semantics.** `interp.Flat` takes the first conjunct as the head. The
subject is therefore predicated only of the first event (`sbj(came, people)`, nothing for *ate*),
and every later conjunct, and every untagged daughter after the conjunction, is related to the
first event by `and`. The RNR'd object becomes `and(quotes, source)` (example 2), the shared object
of `V CC V` becomes `and(surrounds, us)` (example 6), and gapping remnants become `comp`/`and`
dependents of the full verb's event (example 7). Treating *and* as a relation between events is
defensible, but the participant relations of every non-first event are lost. Relations (i)–(iv)
supply them, and gapping also needs one new event variable per gapped conjunct. A flat conjunction
cannot leave the distributive/collective question for shared modifiers open (speculative).

## Annotation errors found

| id | what is wrong |
|---|---|
| journal/ArticleIP_1059#12 | object of *promised* attached to the VP coordination with no trace |
| nyt/20020731-nyt#122 | object of *earn* split: `(VP earn (NP (DT a)))`, and the rest of the NP attached to the coordination |
| technical/1468-6708-3-3#8 | NP *80 mg or placebo once daily* attached to the coordination; it belongs in the second conjunct |
| ficlets/1401#251 | `(NP (VBN hurt))` conjoined with VP *shocked*; the root is labelled UCP |
| enron/21257#12 | index 2 used twice (`NP-2 *-1` and `NP-2 the Policies…`), so `*PRO*-2` and `*RNR*-2` are ambiguous |
| face-to-face/Bmr021#279, ficlets/1399#427, movie-script/pirates#929, twitter/tweets1#149 | `(VP (VP did n't) (VP know …))`: the auxiliary is bracketed as a VP conjunct, giving a spurious verb *did* |
| debate-transcript/2nd_Gore-Bush#383 | the same with `(VP (VP has) (VP got …))` |
| blog/blog-jet-lag#13, fiction/hotel-california#156, jokes/jokes12#67, face-to-face/Bed012#144 | subject NP of a coordinated clause lacks `-SBJ` |
| ficlets/1399#289 | subject clause tagged `S-NOM-DIR`, should be `S-NOM-SBJ` |
| travel-guides/WhereToHongKong#460 | postposed subject of SINV lacks `-SBJ` |
| debate-transcript/2nd_Gore-Bush#611 | gapped clause: remnant `NP-SBJ-2`, full subject `NP-SBJ=3-1`; indices garbled |
| blog/Effing-Idiot#52 | vocative *Pea* tagged `NP-SBJ` as a daughter of the clause coordination |
| debate-transcript/3rd_Bush-Kerry#28 | `*PRO-1` (final `*` missing) |
| twitter/tweets1#134 | `*RNR-2` (final `*` missing) |
| 168 gapped conjuncts, e.g. debate-transcript/3rd_Bush-Kerry#150 | correlates marked `=N` on both sides, not `-N` (systematic deviation from Bies et al. 1995) |
| 19 root UCPs, e.g. blog/Anti-Terrorist#41 | NP-SBJ and VP under a root UCP, not bracketed as S |

Questionable classifications in `verbframes.py`, not fixed here: (1) the walk to the clause stops at
UCP (71 verbs lose a recoverable subject); (2) the second and later verbs of `(VP V CC V …)` get no
record (438); (3) `role()` counts *EXP*/*ICH* antecedents as complements (831 verbs); (4) the
`exp`/`ich`/`rnr`/`gapping` flags test the whole subtree below the verb's VP, not the verb's own
daughters, and the subject, so they neither find nor exclude the affected verbs (Table 10); (5) the
"clause coordination" cause is mostly missing `-SBJ` tags (finding 3); (6) `vp-coordination` also
fires for 920 coordinations without a conjunction. Most of these are comma lists, but they also
include the split-auxiliary errors and 34 "coordinations" with a single VP (`VP CC NP-ETC`, `MD CC
MD VP`).

## Open questions

* Do shared modifiers after a VP coordination (83) and before or around clause coordinations (156)
  distribute over the conjuncts? This needs inspection by hand. The annotation does not say.
* Should a right-node-raised VP across clauses (example 5) count as one verb occurrence or as one
  per conjunct? The same question arises for the event count of a gapped conjunct.
* Can gapping remnants be paired with correlates without indices, by category and function tag, well
  enough for parser output? The 24 PTB-style and 168 `=`-style cases could serve as a test set.
* How many of the 670 root UCPs without a conjunction hide an S (NP-SBJ + VP)? 19 do so visibly. The
  rest need a closer look (another report may cover fragments).
* Would a learned table of function tags, as in `docs/flat-semantics.md`, recover *RNR* sites from
  the backbone? The site is the right edge of each conjunct, but 59 of 214 are inside PPs.
