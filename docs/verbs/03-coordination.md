# Coordination and the other non-local annotations of MASC

In MASC, coordination changes verb frames mostly through one structurally
visible relation, the subject shared by conjoined verb phrases. A reading of
the backbone that follows heads loses that subject for 3,537 verbs in a
non-first conjunct, and for 2,145 verbs (3.1%) this is the only thing wrong
with the frame. Right-node raising (214 traces), gapping (204 conjuncts that
have no verb) and verbs coordinated inside one verb phrase (438 verbs
`verbframes.py` never records) are rarer, but each loses whole arguments or
whole events. Extraposition (*ICH*, *EXP*) involves no sharing, yet it puts a
constituent that is not an argument into the frames of 831 verbs, in the full
annotation as much as in the backbone. About six kinds of sharing link, much
like TIGER's secondary edges, would restore nearly all of these frames, and
three of the six can be read off the backbone's structure alone.

## Question

What do coordination and the other non-local annotations of MASC (VP
coordination, *RNR*, gapping `=N`, *ICH*, *EXP*, UCP, clause coordination) do
to verb frames, in the full annotation and in the context-free backbone? For
each: how often it occurs, which arguments are shared and must be copied to
recover each event's participants, whether `verbframes.py` gets it right, how
often a backbone frame is wrong *only* because of coordination, and whether a
bounded set of sharing relations would suffice.

## Data and method

Script: `tools/masc/verbs/coordination.py` (standard library; imports
`../masctrees.py` and `../verbframes.py`). It rebuilds every verb record exactly
as `verbframes.py` does (70,101 records, identical to `verbs.jsonl`) and adds:

* **frame_local**, the frame that a head-driven reading of the backbone gives:
  the overt subject only if each VP between the verb and its clause passes the
  verb up as its head (Collins's VP rule over overt daughters; a VP whose head
  word is followed by a VP counts as transparent, as in `is_aux_vp`), plus the
  overt complements, as in `frame_backbone`. In `VP → VP CC VP` the first VP is
  the head, as in `go/interp/heads.go`, so only the first conjunct sees the
  subject. `frame_backbone` in `verbframes.py` is more generous, since it walks
  up through all VPs.
* **reasons** why frame_local differs from frame_full: an empty subject of a
  given kind, an overt subject shared into a non-head conjunct, or an empty
  complement of a given kind.
* for each coordination on the way up: the conjunct's position, whether it is
  the head, and the coordination's other daughters (before the first conjunct,
  between conjuncts, after the last), with any trace they are antecedent to.
* for each tree: gapped conjuncts (an S/VP/SINV/SQ with `=N` daughters and no
  verb, MD, TO or VP daughter), VPs with more than one verb daughter, UCPs,
  coordinated clauses and their other daughters, and every *RNR*/*ICH*/*EXP*
  trace with its antecedent's position.

```
S=/tmp/claude-0/-home-user/32748c59-1ad9-5140-855d-e9a1edeaf50a/scratchpad
python3 tools/masc/verbs/coordination.py $S/masc/data $S/coord   # ~10 s
python3 tools/masc/verbs/coordination.py --tables $S/coord       # all tables below
```

I checked the flat meanings (`interp.Flat`) of the examples with
`cd go && go run ./cmd/readings -annotated SUBSET.jsonl -sem`, where SUBSET is
the matching lines of `$S/ann/annotated.jsonl`. Every example below was
inspected in the raw tree.

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

**Table 5. *RNR* traces** (214 in 103 trees; every one has an antecedent, 4 of them word-level `NN-4`)

| trace stands for | NP 130, PP 35, VP 18, SBAR 12, other 19 |
|---|---|
| trace's host | VP 127, PP 59, NP 15, other 13 |
| antecedent's parent | a coordination in 176 (VP 129, NP 25, S 18, SBAR 4); VP 16, S 6, other 16 not coordinated |

**Table 6. Gapped conjuncts** (204)

| | count |
|---|---|
| VP within VP coordination / S within S coordination / other | 148 / 45 / 11 |
| remnants: one / two / three | 94 / 106 / 4 |
| correlates in the full conjunct marked `-N` (PTB convention) | 24 |
| correlates also marked `=N` (no `-N` anywhere) | 168 |
| mixed or missing | 12 |

**Table 7. Extraposition: VP daughters that are antecedents of a trace elsewhere**

| | traces | host of trace | verbs counting the antecedent as a **complement** |
|---|---|---|---|
| *EXP* | 615 | NP-SBJ 585 (`it`) | 520 |
| *ICH* | 709 | NP 334, NP-SBJ 184, ADJP 67, WHNP 38, ADVP 38 | 314 |
| either | | | **831 verbs (1.2%)**, e.g. `SBJ ADJP-PRD SBAR` 162, `SBJ ADJP-PRD S` 141, `SBJ NP SBAR` 70 |

*ICH* antecedents are also counted as modifiers of 194 verbs.

**Table 8. The 113 verbs whose walk up stops at a UCP**

| UCP's parent | subject of the clause above | verbs |
|---|---|---|
| VP (e.g. `was [ADJP …] and [VP …]`) | overt 45, *T* 2, * 1 | 48 |
| S (predicate UCP) | *PRO* 15, overt 8 | 23 |
| NP (reduced relative) | the head noun is the subject | 7 |
| ADJP, SQ, UCP | — | 3 |
| none: the root is labelled UCP (834 root UCPs, 670 without any CC) | — | 32 |

**Table 9. Clause coordination** (2,653 coordinated S/SQ/SINV/SBARQ; 5,737 verbs flagged)

Subjects of flagged verbs: overt 5,583 (97.3%), *PRO* 121, *T* 24, * 1, none 8.
Of the 450 coordinations with other daughters, those daughters are CODE 219,
modifiers 156, INTJ 76, REF 52, EDITED 42, PRN 25, vocatives 23, VP 11,
NP-SBJ 5, and other material.

**Table 10. Flags against what they should mark** (a verb is "affected" if its own VP has the trace or its antecedent as a daughter)

| flag | flagged | affected | both |
|---|---|---|---|
| rnr | 263 | 108 | 108 |
| ich | 714 | 552 | 358 |
| exp | 262 | 522 | 19 |
| gapping | 261 | — | 134 have a `=N` correlate among their own daughters |

## Examples

Trees are trimmed. "local" is frame_local.

1. **blog/How_soon-Fans#22**, shared subject. `(S (NP-SBJ People) (VP (VP (VBD came) (PRT in)) (CC and) (VP (VBD ate) (NP their food))))`.
   *ate*: full `SBJ NP`, backbone `SBJ NP`, local `NP`. Flat gives `sbj(came,people)`, `and(came,ate)`, `obj(ate,food)`, and no subject for *ate*.
2. **court-transcript/Day3PMSession#1237**, shared controlled subject. `(S-IMP (NP-SBJ *PRO*) (VP (VP (VB State) (NP your name)) (CC and) (VP (VB spell) …)))`.
   Both verbs have full `SBJ(*PRO*) NP` and backbone `NP`. The empty subject is shared as well.
3. **journal/VOL15_3#274**, RNR of an object. `(VP (VP (VBZ quotes) (NP *RNR*-1)) (CC and) (ADVP presumably) (VP (VBZ accepts) (NP *RNR*-1)) (NP-1 another source which …))`.
   *quotes* and *accepts*: full `SBJ NP(*RNR*)`, backbone `SBJ`, local `SBJ` and `` (empty). Flat gives `and(quotes, source)`: the shared object becomes a conjunct.
4. **court-transcript/Day3PMSession#210**, RNR across clauses. `(S (S (NP-SBJ I) (VP understand (NP *RNR*-1))) and (S (NP-SBJ I) (VP respect (NP *RNR*-1))) (NP-1 that argument))`.
   Both are `SBJ NP(*RNR*)` in full and `SBJ` in the backbone. Flat: `and(understand, argument)`.
5. **nyt/NYTnewswire8#15**, RNR into a PP. `(VP (VP (VBG developing) (NP *RNR*-1)) (CC and) (VP (VBG lobbying) (PP for (NP *RNR*-1))) (NP-1 prescription-privileges legislation))`.
   *lobbying*'s frame `SBJ(*PRO*)` is unchanged, but its PP modifier has lost its object. That can only be restored inside the conjunct.
6. **court-transcript/Day3PMSession#322**, RNR of a VP across clauses. `(S (S (NP-SBJ he) (VP did n't (VP *RNR*-5))) or (S (NP-SBJ she) (VP did n't (VP *RNR*-5))) (VP-5 (VB say) (NP it)))`.
   *say*: full `NP`, no subject; cause "clause coordination". One record stands for two events with two subjects.
7. **non-fiction/rybczynski-ch3#209**, verbs coordinated inside one VP. `(VP (VBZ surrounds) (CC and) (VBZ shelters) (NP us))`.
   *surrounds*: `SBJ NP`. *shelters* has no record. Flat: `and(surrounds, shelters)`, `and(surrounds, us)`, so neither verb gets *us* as its object.
8. **debate-transcript/2nd_Gore-Bush#751**, gapping in VP. `(VP (VP (VBZ ranks) (ADJP-CLR-1 49th …) (PP-2 in children …)) , (VP (ADJP-CLR=1 49th) (PP=2 for women …)) , and (VP (ADJP-CLR=1 50th) (PP=2 for families …)))`.
   *ranks*: `SBJ ADJP-CLR`. The two ranking events for women and families have no verb record. In Flat they become `comp(ranks, 49th)` and `and(ranks, 50th)`.
9. **debate-transcript/3rd_Bush-Kerry#150**, gapping in S, `=` on both sides. `(S (S (NP-SBJ=1 Health-care costs …) (VP is (VP skyrocketing))) , (S (NP-SBJ=1 the cost of the war)))`.
   The full conjunct's subject carries `=1` instead of `-1`. The second event (the war's cost is skyrocketing) has no record.
10. **court-transcript/Day3PMSession#1320**, gapping with two remnants. `(VP (VP (VBN e-mailed) (NP-1 a letter …) (PP-DTV-2 to the board president …)) , (VP (NP=1 a copy) (PP-DTV=2 to Dr. Nilsen)))`.
    The verb and the subject have to be copied into the second conjunct. Its remnants pair with NP-1 and PP-DTV-2.
11. **debate-transcript/3rd_Bush-Kerry#311**, *EXP*. `(S (NP-SBJ (NP It) (SBAR *EXP*-1)) (VP (VBZ 's) (ADJP-PRD important) (SBAR-1 that we do that)))`.
    *'s*: full = backbone = local = `SBJ ADJP-PRD SBAR`. The SBAR is the logical subject, not a third argument.
12. **face-to-face/ReidSandra#42**, *ICH* from the subject. `(S (NP-SBJ (NP That) (S *ICH*-1)) (VP (VBD was) (NP-PRD my dream) , (S-1 *PRO* to teach school)))`.
    *was*: `SBJ NP-PRD S`, where S belongs to the subject NP.
13. **face-to-face/ReidSandra#32**, predicate UCP. `(S-ADV (NP-SBJ *PRO*-1) (UCP-PRD (VP (VBG stomping) (CC and) (VBG crying)) and (ADJP mad) and (VP (VBG taking) (NP …))))`.
    *stomping* and *taking* are "no clause above (UCP)" and have no subject, though the S right above has `*PRO*-1`. *crying* has no record.
14. **court-transcript/Lessig-court-transcript#347**, UCP under a copula. `(S (NP-SBJ-2 that) (VP (VBZ 's) (UCP (VP (VBN distributed) (NP *-2) (ADVP widely)) (CC and) (ADJP-PRD available))))`.
    *distributed*: full `NP(*)`, no subject. Its subject is the clause's *that*.
15. **journal/ArticleIP_1059#12**, annotation error. `(VP (VP (VBD acquired) (NP Palestine)) (CC and) (VP (VBD promised) (PP-LOC in the Balfour Declaration)) (NP the creation of a Jewish state))`.
    *promised*: full `SBJ`. Its object is attached to the coordination and has no trace.
16. **face-to-face/Bmr021#279**, annotation error. `(VP (VP (VBD did) (RB n't)) (VP (VBP know) (NP that)))`.
    This gives a spurious lexical verb *did* (`SBJ`) and a spurious VP coordination. *know* is local `NP`.
17. **blog/blog-jet-lag#13**, annotation error. `(S (S (PP-TMP …) , (NP this signal) (VP (VBZ subsides))) , and (S (NP-SBJ s$$he) …))`.
    *subsides* has no subject and its cause is given as "clause coordination", but the NP simply lacks `-SBJ`.

## Findings

1. **VP coordination is the main case, and it is structural.** 9.8% of verbs sit
   under a VP coordination (Table 1). The shared subject is a sister of the
   coordinated VP, so it is present in the backbone. What is lost is only the
   link from it to non-first conjuncts: 3,537 verbs, 2,276 of them with an overt
   subject. For 2,145 verbs (3.1%) this, or RNR, is the frame's only defect
   (Table 2). `verbframes.py` walks up through VPs, so it assigns the right
   subject to every conjunct of an ordinary VP coordination, including nested
   ones (119) and ones under shared auxiliaries. Its `frame_backbone` therefore
   already contains the sharing, and it differs from `frame_full` for
   coordination reasons in only 59 verbs (*RNR*). The `frame_local` column
   shows what a local reading of the backbone actually gets (examples 1, 2).
2. **The subject walk fails in four places.** (a) It stops at UCP. 71 verbs in
   predicate UCPs under VP or S have no subject, though the clause above has
   one (Table 8; examples 13, 14). (b) When a VP is right-node raised out of
   coordinated clauses, it gets no subject, where it should get one subject per
   clause (example 6; the only genuine case among the 8 "clause coordination"
   causes). (c) Verbs coordinated inside one VP get no record after the first
   (438 verbs; example 7). (d) Gapped conjuncts have no record (204 events;
   examples 8–10). Together these are about 720 events that `verbs.jsonl`
   misses or gets without a subject.
3. **The rest of the "clause coordination" cause is annotation error.** Of its
   8 verbs, 1 is RNR (finding 2b). The other 7 are unrelated to sharing: an
   NP subject without `-SBJ` (blog/blog-jet-lag#13,
   fiction/hotel-california#156, jokes/jokes12#67, face-to-face/Bed012#144),
   `S-NOM-DIR` for `S-NOM-SBJ` (ficlets/1399#289), a postposed subject without
   `-SBJ` (travel-guides/WhereToHongKong#460), and an S-ADV with no subject
   (debate-transcript/2nd_Gore-Bush#314). Coordinated clauses almost always
   carry their own subject (97.3% overt, Table 9). Clause coordination shares
   modifiers (156) far more often than arguments (5 NP-SBJ, 11 VP).
4. **RNR is small and mostly shares complements.** 214 traces (Table 5): the
   trace stands for NP in 61% of them and PP in 16%, and in 82% the antecedent
   is a daughter of the coordination itself. So RNR is always sharing
   within a coordination, never general movement. 59 of the traces sit inside
   a PP (example 5), so restoring them means reaching into the conjunct.
5. **The PTB uses a trace for a shared complement and plain attachment for a
   shared modifier.** After the last conjunct, complements carry *RNR* 55
   times and appear without a trace 19 times. Those 19 are 16 `NP-ETC` ("or
   whatever"), which are pseudo-conjuncts, and 3 attachment errors (Annotation
   errors). Modifiers appear without a trace 83 times and with *RNR* only 6
   times (Table 4). The annotation encodes the complement/modifier distinction
   in exactly this place. A complement has to be in each conjunct's VP, so it
   is traced. A modifier may modify the coordinated whole, and whether it
   distributes is left open.
6. **Word-level verb coordination is the unmarked alternative to RNR.** "Saw and
   heard X" is annotated `(VP V CC V NP)` 381 times (Table 3; patterns
   `VB CC VB NP` 49, `VBG CC VBG NP` 25, `VBN CC VBN NP` 23) and as RNR over VPs
   only about 60 times. The flat analysis is chosen when the conjuncts are bare
   verbs. RNR is chosen when a conjunct has material of its own (example 3,
   *presumably*). The two encode the same sharing.
7. **Gapping loses events, and its indices are inconsistent.** 204 gapped
   conjuncts (Table 6), 73% of them VP gapping inside VP coordination. The full
   conjunct's verb is most often a copula (*is/was/are/be/were*: 51 of 204).
   In 168 conjuncts MASC marks the correlates with `=N` on both sides instead of
   `-N` in the full conjunct (example 9). A resolver has to accept both. The
   `gapping` flag fires for 261 verbs, but 127 of them only dominate a gapped
   coordination further down (e.g. *think* over one). The verb of the full
   conjunct is missed when its correlates carry `-N`.
8. **Extraposition is not sharing, but it corrupts frames on both sides.** In
   831 verbs (Table 7) a VP daughter is the antecedent of an *EXP* or *ICH*
   trace located elsewhere. For *EXP* the trace is almost always in the
   subject *it*. For *ICH* it is an NP inside the VP or the subject. `role()` counts it as a
   complement because it is an untagged S or SBAR. `frame_full` and
   `frame_backbone` therefore both show a spurious clausal complement:
   `SBJ ADJP-PRD SBAR` for *it's important that …* (example 11), where the
   SBAR is the logical subject. The `exp` flag marks only 19 of the 522
   affected verbs, because it looks for the trace inside the verb's own VP,
   while *EXP* traces sit in NP-SBJ (585 of 615). The `ich` and `rnr` flags
   over-mark for the same reason (Table 10): they fire for any trace anywhere
   below the VP.
9. **UCP is mostly not coordination at the root.** Of 1,210 UCPs, 834 are
   roots and 670 of those have no conjunction. They label juxtaposed pieces
   (`NP : NP-SBJ VP`; 19 roots contain an NP-SBJ and a VP that should form an
   S, e.g. blog/Anti-Terrorist#41). 104 UCPs have a VP daughter, some of them
   such roots. Where a UCP really coordinates a VP with an ADJP, PP or NP
   under a VP or S (79 verbs, Table 8), it shares the subject exactly as VP
   coordination does (finding 2a).
10. **A bounded set of sharing relations would suffice.** Per event, the
    sharing needed is:
    (i) subject into every VP/UCP predicate conjunct (3,537 + 71);
    (ii) the head verb and its dependents into every verb of a `V CC V`
    phrase (438);
    (iii) an RNR'd dependent into the right edge of each conjunct (214, 59 of
    them inside a PP);
    (iv) verb, auxiliaries and unmatched dependents into a gapped conjunct,
    pairing remnants with correlates (204);
    (v) optionally, shared modifiers and auxiliaries distributed over
    conjuncts (about 280; Tables 4 and 9), where the distributive reading is
    not guaranteed;
    and, outside coordination, (vi) a *belongs-to* link from an extraposed
    constituent to its host NP or expletive (1,324 traces, 831 frames).
    Relations (i), (ii) and (v) are determined by the backbone's structure.
    (iii) is visible in the backbone as a non-conjunct daughter after the last
    conjunct, except for its landing site inside PPs. (iv) needs the `=N`
    pairing or a heuristic by category and order. (vi) needs the index. These
    are the MASC counterparts of TIGER's secondary edges, which (from memory)
    TIGER uses only for constituents shared in coordination. The difference is
    that the PTB leaves the commonest case, (i), implicit in the structure.

## What it means

**(a) The context-free backbone and the fast parser.** Coordination adds nothing
that is not context-free. It adds rules and misleading lexical statistics. A
transitive verb whose object is right-node raised appears as `VP → VBZ`,
and its object as an extra daughter of the coordination (`VP → VP CC VP NP`).
Gapped conjuncts become verbless VPs and Ss. In the raw trees, 154 of the 286
VPs with no overt verbal daughter are gapped conjuncts; `annotated.jsonl` has
225 such VP nodes after unary collapse. These
rules let the parser build a VP out of `NP PP` anywhere, which adds ambiguity.
Pseudo-coordinations from annotation errors (`VP → VP VP` from a split
auxiliary; `NP-ETC` conjuncts) add more. None of this needs a change to the
grammar formalism. A post-parse sharing step can run on parser output, since
relations (i), (ii) and most of (iii) and (v) are structural.

**(b) Verb frames and the complement/modifier distinction.** Counted with
sharing restored (as `frame_backbone` does for subjects), coordination
corrupts only 59 frames. Counted locally, it corrupts 2,145 (3.1%), plus
roughly 640 events that have no frame at all. The owner's distinction shows
up directly in the annotation (finding 5). Shared complements are traced into
each conjunct and so belong to each verb's frame. Shared modifiers are
attached once and may be collective (speculative: e.g. *sang and danced for
three hours* allows both readings). Frame statistics should copy shared
complements into each conjunct but keep shared modifiers separate. Separately
from coordination, `role()` should not count an *EXP* or *ICH* antecedent as a
complement of the verb whose VP holds it. Otherwise copular frames like
`SBJ ADJP-PRD SBAR` (162) are artefacts of extraposition.

**(c) The flat neo-Davidsonian semantics.** `interp.Flat` makes the first
conjunct the head, so the subject is predicated only of the first event
(`sbj(came, people)`, nothing for *ate*). Every later conjunct, and every
untagged daughter after the conjunction, is related to the first event by
`and`. The RNR'd object thus becomes `and(quotes, source)` (example 3), the
shared object of `V CC V` becomes `and(surrounds, us)` (example 7), and gapped
conjuncts become `comp`/`and` attachments of their remnants to the full
verb's event (example 8). The coordination is represented as a relation
between events, which is defensible for sentential *and*. What is lost are the
participant relations of every non-first event. Relations (i)–(iv) of
finding 10 are what a reading needs to give each event its own `sbj` and
`obj` atoms. Gapping also needs new event variables, one per gapped
conjunct. Shared modifiers raise the distributive/collective question, which
a flat conjunction of atoms cannot leave open (speculative).

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

Questionable classifications in `verbframes.py`, not fixed here:
(1) the walk to the clause stops at UCP (71 verbs lose a recoverable subject);
(2) the second and later verbs of `(VP V CC V …)` get no record (438); 
(3) `role()` counts *EXP*/*ICH* antecedents as complements (831 verbs); 
(4) the `exp`/`ich`/`rnr`/`gapping` flags test the whole subtree below the
verb's VP, not the verb's own daughters, and the subject, so they neither
find nor exclude the affected verbs (Table 10); 
(5) the "clause coordination" cause is mostly missing `-SBJ` tags (finding 3);
(6) `vp-coordination` also fires for 920 coordinations without a conjunction.
Most of these are comma lists, but they also include the split-auxiliary
errors and 34 "coordinations" with a single VP (`VP CC NP-ETC`,
`MD CC MD VP`).

## Open questions

* Do shared modifiers after a VP coordination (83) and before or around
  clause coordinations (156) distribute over the conjuncts? This needs
  inspection by hand. The annotation does not say.
* Should a right-node-raised VP across clauses (example 6) count as one verb
  occurrence or as one per conjunct? The same question arises for the event
  count of a gapped conjunct.
* Can gapping remnants be paired with correlates without indices, by category
  and function tag, well enough for parser output? The 24 PTB-style and 168
  `=`-style cases could serve as a test set.
* How many of the 670 root UCPs without a conjunction hide an S (NP-SBJ + VP)?
  19 do so visibly. The rest need a closer look (another report may cover
  fragments).
* Would a learned table of function tags, as in `docs/flat-semantics.md`,
  recover *RNR* sites from the backbone? The site is the right edge of each
  conjunct, but 59 of 214 are inside PPs.
