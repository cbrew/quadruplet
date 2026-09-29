# Traces *T* and * at MASC's verbs: extraction, passive and raising

About a fifth of MASC's lexical verbs (14,139 of 70,101, 20.2%) have a subject
or complement that is a *T* or * trace, and in the backbone those arguments
are simply missing. 7,629 verbs whose full frame has an NP object (24.3% of
the 31,423 such verbs) look intransitive to the backbone, and 6,000 lose
their subject. Nearly all of this damage is systematic and can be undone:
passives, relative clauses, questions, raising and quotation. Coindexation
within the sentence tree links 89.5% of these traces to an antecedent. Adding
two simple structural steps (from a relative pronoun or null operator to the
head noun, and from a reduced relative to its head) brings about 94% of them
to an overt word or phrase. Annotation errors, mostly index clashes and
missing indices, affect well under 1% of the traces.

## Question

What do the empty elements *T* (wh-movement and topicalization) and *
(NP movement: passive, raising) do to verb frames when the context-free
backbone strips them? Which constructions are responsible, how often, and
how reliably can the full argument structure be restored from the
coindexation MASC provides?

## Data and method

- Input: `verbs.jsonl` from `tools/masc/verbframes.py` (70,101 verbs) and
  the raw MASC `.mrg` trees, with indices intact.
- Script: `tools/masc/verbs/traces.py`. It reuses `verbframes.py`'s own
  definitions of lexical verb, clause, subject and complement/modifier, and
  writes one record for each *T* or * argument of a verb:
  ```
  S=<scratchpad>
  python3 tools/masc/verbs/traces.py $S/masc/data $S/traces      # traces.jsonl, traces_extra.json
  python3 tools/masc/verbs/traces.py --report $S/traces $S/verbs/verbs.jsonl   # all tables below
  ```
- Chains. The script follows a trace's index (`*T*-1`) to the one labelled
  node carrying `-1` (never `=1`, which marks gapping). It keeps going while
  that node is itself empty (`NP-SBJ-2 (-NONE- *T*-1)`), and stops at an
  overt node, a null operator `(WHNP-1 (-NONE- 0))`, or an unindexed empty
  element. An index that appears on two nodes is a clash. The script settles
  a clash when exactly one candidate is of the right sort (a WH phrase or
  topic for *T*, neither for *) and c-commands the trace. From a WH phrase
  or null operator in a relative clause (SBAR daughter of NP, after an NP),
  it takes one structural step to that head NP.
- An SBAR made only of `(-NONE- 0)` and `(S (-NONE- *T*-n))` is counted as
  *T* here. `verbframes.py` reports it as `SBAR(0)`.
- Supporting checks: ad hoc scripts over the same files, all shown in the
  counts. They count asterisks by genre, the distribution of * and *PRO*
  under control and raising verbs, and reduced-relative heads.

## Counts

**Table 1. *T* and * arguments of lexical verbs** (17,784 in all)

| slot | *T* | * |
|---|---:|---:|
| subject | 3,775 | 2,225 |
| NP object | 2,148 | 5,639 |
| predicative (-PRD) | 478 | 1 |
| clausal complement (S, SBAR, SQ, SBARQ) | 498 | 14 |
| other complement (-CLR ADVP/PP, -DTV) | 45 | 0 |
| modifier (ADVP-TMP, -MNR, -LOC, -PRP ...) | 2,961 | 0 |

The table omits traces inside a PP daughter of the verb, because
`verbframes.py` counts such a PP as overt. There are 332 stranded
prepositions (`talk to *T*`: 233 in complement PPs, 99 in modifier PPs) and
61 prepositional passives (`was dealt with *`).

**Table 2. *T*: the antecedent**

| antecedent | subject | NP object | predicative | clausal | modifier |
|---|---:|---:|---:|---:|---:|
| WHNP, overt | 3,464 | 1,113 | 123 | 1 | 40 |
| WHNP, null operator 0 | 297 | 971 | 31 | 0 | 346 |
| WHADVP (overt / 0) | 0 | 1 | 58 / 1 | 0 | 2,231 / 121 |
| WHPP, WHADJP (overt / 0) | 0 | 0 | 53 / 31 | 0 | 160 / 3 |
| non-WH topic (S-TPC, NP-TPC, ADVP-LOC-PRD-TPC ...) | 10 | 60 | 180 | 496 | 59 |
| unindexed, unresolved clash | 4 | 3 | 1 | 1 | 1 |

**Table 3. *T* subjects and objects: construction** (from the antecedent's
position; "relative" = SBAR under NP after a head NP)

| construction | subject | NP object |
|---|---:|---:|
| relative clause | 2,901 | 1,246 |
| free relative (SBAR-NOM) | 210 | 327 |
| embedded question or free relative under VP | 227 | 194 |
| direct question (SBARQ) | 136 | 151 |
| adverbial SBAR (mostly sentential *which*, SBAR-ADV) | 122 | 20 |
| under ADJP (tough, *too/enough*, adjective + question) | 11 | 60 |
| topicalization (non-WH antecedent) | 10 | 60 |
| 0 operator under VP (cleft, purpose) | 20 | 26 |
| other (SBAR under SBAR, fragments, roots) | 138 | 64 |

Relativizers in relative clauses. Subject gaps (2,901): *that* 1,328, *who*
772, *which* 480, 0 222, *whose N*
57, *Q of which/whom* 26, other 16. Object gaps (1,246): 0 866 (69.5%), *that*
327, *which* 42, other 11. Over all MASC, 232 of the 236 relatives with a
null operator and a *T* subject are infinitival (*a person to stand by my
side*). The finite contact relative with a subject gap barely occurs.

**Table 4. * objects (passives), 5,639 verbs** (5,624 tagged VBN)

| antecedent of the object trace | n | with by-phrase |
|---|---:|---:|
| own clause's subject, overt | 3,285 | |
| own subject *PRO* (*to be seen*) | 492 | |
| own subject *T* (*who was seen*) | 432 | |
| own subject * (*seems to be seen*) | 109 | |
| **own subject, total** | **4,318** | **621 (14.4%)** |
| unindexed, VP inside NP (reduced relative) | 1,119 | 271 (24.2%) |
| unindexed, elsewhere (absolutes, small clauses) | 146 | 32 |
| another node; index clash; dangling | 56 | 8 |
| **all** | **5,639** | **932 (16.5%)** |

A "by-phrase" is a PP daughter of the verb's VP containing NP-LGS. MASC
puts LGS on the NP inside the PP: 1,006 NP-LGS against 7 PP-LGS. In 76
passives an overt NP remains beside the trace (*was given * a copy*). Of the
1,119 reduced relatives, 1,113 have an NP sister before the VP, which is the
understood object.

**Table 5. * subjects, 2,225 verbs: what the clause is the complement of**

| matrix | n |
|---|---:|
| semi-modal: *have to, need to, going to/gonna, ought to, used to, have got to* | 1,105 |
| aspectual: *begin, start, continue, stop, keep* | 425 |
| raising verb: *seem, appear, tend, happen, fail, turn out* | 199 |
| passive matrix: *be supposed/expected/allowed/said/meant to* | 166 |
| other matrix verb (many are control verbs, see Finding 7) | 148 |
| unindexed * (imperatives, reduced relatives, WSJ arbitrary PRO) | 115 |
| antecedent a matrix object (*help NP-2 [*-2 build]*) or other | 31 |
| raising adjective (*likely, about, sure*) | 19 |
| clause under S, SQ, SINV | 17 |

**Table 6. What the backbone does to the frames** (all 70,101 verbs)

| effect | cause | n |
|---|---|---:|
| subject missing | *T* / * | 3,775 / 2,225 |
| every NP object missing, frame looks intransitive | * (passive) | 5,555 |
| | *T* | 2,067 |
| | both | 7 |
| two NP objects, one left: looks monotransitive | * / *T* | 76 / 74 |
| predicative complement missing (*what it is*, *Here's ...*) | *T* | 478 |
| clausal complement missing (*"...," he said*, *X, I think*) | *T* | 523 |
| | * (quote contains the verb) | 13 |
| preposition left without object | *T* / * | 332 / 61 |

Of the 7,707 VBN verbs with no overt NP object in the backbone, 5,547
(72.0%) are passives with a * object. 1,942 (25.2%) have no NP object in the
full frame either (perfect intransitives, passives of clausal verbs,
prepositional passives), and 210 have a *T* object.

**Table 7. Restoring the argument.** Chain ends as defined above. The last
column counts chains that met an index clash which the script settled.

| trace, slot | n | overt non-WH | WH phrase itself | rel. head via WH | rel. head via 0 | 0, no head | unindexed | broken | clash settled |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| *T* subject | 3,775 | 10 | 774 | 2,690 | 259 | 38 | 3 | 1 | 26 |
| *T* object | 2,148 | 60 | 727 | 387 | 874 | 97 | 0 | 3 | 21 |
| *T* other compl. | 1,021 | 680 | 231 | 45 | 27 | 36 | 1 | 1 | 10 |
| *T* modifier | 2,961 | 59 | 1,874 | 557 | 409 | 61 | 0 | 1 | 19 |
| * subject | 2,225 | 1,888 | 30 | 67 | 1 | 2 | 234 | 3 | 17 |
| * object | 5,639 | 3,556 | 92 | 334 | 30 | 6 | 1,602 | 19 | 46 |

"WH phrase itself" covers questions, free relatives and sentential *which*,
where the WH phrase is the argument. For * the "unindexed" column includes
chains that end in an unindexed *PRO*: 119 subjects and 337 objects, as in
*to be seen* with an arbitrary controller. Directly unindexed * objects
number 1,265, and 1,113 of them are reduced relatives with a recoverable
head.

Distance. Measured in clauses between the trace and its antecedent's parent:
- *T* subjects: 97.5% cross 1 clause, 91 cross 2 or more.
- *T* objects: 61 cross 0 (topicalization inside the clause), 85.0% cross
  1, 256 (11.9%) cross 2 or more (*what we have to do *T**).
- Indexed * objects: all but 5 cross 0. Indexed * subjects: all but one
  cross 1.

## Examples

Trees are trimmed. Frames are `frame_full` → `frame_backbone` from
`verbs.jsonl`.

1. `debate-transcript/2nd_Gore-Bush#29`, *called*: `SBJ(*T*) NP(*) S` → `S`
   ```
   (NP (NP dedicated citizens) (SBAR (WHNP-1 who) (S (NP-SBJ-2 (-NONE- *T*-1))
     (VP are (VP (VBN called) (NP-3 (-NONE- *-2)) (PP by (NP-LGS the president))
       (S (NP-SBJ (-NONE- *PRO*-3)) (VP to serve the country)))))))
   ```
   Passive inside a subject relative. The object chain `*-2 → *T*-1 → who`
   plus one step reaches *dedicated citizens*. In the backbone both *who
   are called ...* and *to serve* become the same unary-collapsed symbol
   `SxVP`.
2. `court-transcript/Lessig-court-transcript#181`, *solving*: `SBJ NP(*T*)` → `SBJ`
   `(NP (NP the problem) (SBAR (WHNP-2 (-NONE- 0)) (S (NP-SBJ they) (VP were (VP solving (NP (-NONE- *T*-2)))))))`.
   A contact relative. Nothing overt marks the gap, and the backbone VP is `VP → VBG`.
3. `debate-transcript/2nd_Gore-Bush#125`, *doing* `SBJ NP(*T*)` → `SBJ`; *produced* `SBJ NP(*)` → `SBJ`
   `(S (NP-SBJ-1 a lot of the energy) (VP is (VP produced (NP (-NONE- *-1)) ...))) , and (S ... (SBAR (WHNP-1 what) (S ... (VBG doing) (NP (-NONE- *T*-1)))))`.
   A free relative. Index 1 is used twice. Sort (WH for *T*, non-WH for *)
   and c-command settle both chains.
4. `court-transcript/Day3PMSession#705`, *interviewed*: `SBJ NP(*)` → `SBJ`
   `(SQ Have (NP-SBJ-1 you) (VP been (VP interviewed (NP (-NONE- *-1)) (PP by (NP-LGS news reporters)) (ADVP-TMP before))))`.
   An ordinary passive. The agent is a "modifier" PP.
5. `jokes/jokes1#260`, *operated*: `NP(*)` → (empty)
   `(NP (NP a people trap) (VP (VBN operated) (NP (-NONE- *)) (PP by (NP-LGS a mouse))))`.
   A reduced relative. The * has no index, and the head is the NP sister.
6. `debate-transcript/2nd_Gore-Bush#350`, *do*: `SBJ(*) NP(*T*)` → (empty)
   `(SBAR-PRD (WHNP-1 what) (S (NP-SBJ-2 it) (VP 's (VP (VBN meant) (S (NP-SBJ (-NONE- *-2)) (VP to (VP do (NP (-NONE- *T*-1)))))))))`.
   Both arguments are traces: raising through a passive matrix, and
   long-distance object extraction (2 clauses). The backbone frame of *do*
   is empty.
7. `court-transcript/Day3PMSession#1121`, *given*: `SBJ NP(*) NP` → `SBJ NP`
   `(S (NP-SBJ-1 She) (VP was n't (VP given (NP (-NONE- *-1)) (NP a copy of ...))))`.
   A double-object passive. The backbone shows an ordinary transitive
   frame, with the theme where the recipient belongs.
8. `w3c/lists-003-2180740#6`, *think*: `SBJ SBAR(*T*)` → `SBJ`;
   `face-to-face/Bed012#507`, *guess*: `SBJ SBAR(0)` → `SBJ`
   `(S (S-TPC-1 That 's true) , (NP-SBJ I) (VP guess (SBAR (-NONE- 0) (S (-NONE- *T*-1)))))`.
   A fronted complement clause. `verbframes.py` names the second case
   `SBAR(0)` after its first leaf.
9. `ficlets/1400#528`, *get*: `SBJ(*PRO*) NP(*T*)` → (empty)
   `(S (NP-SBJ She) (VP was (ADJP-PRD (ADJP easy) (SBAR (WHNP-1 (-NONE- 0)) (S (NP-SBJ (-NONE- *PRO*)) (VP to (VP get (NP (-NONE- *T*-1)))))))))`.
   A tough construction. The chain stops at a null operator, and *She* is
   related to it only by the structure. This is one of the 240 "0, no head"
   cases.
10. `debate-transcript/3rd_Bush-Kerry#484`, *'s*: `SBJ ADVP-PRD(*T*)` → `SBJ`
    `(SINV (ADVP-PRD-TPC-1 Here) (VP 's (ADVP-PRD (-NONE- *T*-1))) (SBAR-NOM-SBJ (WHNP-1 what) (S I (VP do (NP (-NONE- *T*-1))))))`.
    A copula with a fronted predicate. Index 1 is on two nodes. For *'s*,
    c-command picks the topic. For *do*, both candidates c-command the
    object trace, so the chain stays unresolved: an annotation error.
11. `court-transcript/Day3PMSession#697`, *talk*: `SBJ PP-CLR` → `SBJ PP-CLR`
    `(SBARQ (WHNP-1 Who) (SQ did (NP-SBJ you) (VP talk (PP-CLR (TO to) (NP (-NONE- *T*-1))))))`.
    A stranded preposition. The frames look identical, but the backbone
    PP is `PP → TO` alone.
12. `court-transcript/Lessig-court-transcript#149`, *allows*: `SBJ(*T*) S` → `S`
    `(S ... (VP apply that ... , (SBAR-ADV (WHNP-1 which) (S (NP-SBJ (-NONE- *T*-1)) (VP allows (S us to make ...))))))`.
    Sentential *which*. The subject is a proposition, not the preceding NP.
13. `wsj/wsj_0026#4`, *grant*: `SBJ(*) NP` → `NP`
    `(S (NP-SBJ-1 Mr. Bush) (VP (VBD decided) (S (NP-SBJ (-NONE- *-1)) (VP to grant ...))))`.
    Subject control, which the WSJ files mark * where the rest of MASC
    writes *PRO* (Finding 7).
14. `debate-transcript/2nd_Gore-Bush#972`, *Go*: `SBJ(*)` → (empty)
    `(S-IMP (NP-SBJ (-NONE- *)) (VP Go (ADVP ahead)))`. An imperative
    marked * instead of *PRO*.
15. `govt-docs/Postal_Rate_Comm-ReportToCongress2002WEB#48`, *afforded*: `SBJ NP(*)` → `SBJ`
    `(S (NP-SBJ=2 an opportunity for ... hearings) (VP is (VP=3 afforded (NP (-NONE- *-2)) ...)))`.
    The antecedent carries the gapping mark `=2` where `-2` was meant, so
    the index dangles.
16. `essays/Black_and_white#41`, *seen*: `SBJ NP(*)` → `SBJ`
    `(S (NP-SBJ (NP color) (VP (-NONE- *ICH*-1))) (VP became ... , (VP-1 seen (NP (-NONE- *)) (PP as ...))))`.
    An extraposed reduced relative. `verbframes.py` gives it the host
    clause's subject (Finding 9).

## Findings

1. **Traces account for about half of all frame changes.** Of the 26,947
   verbs whose frame changes when empty elements go, 14,139 (52%) have a *T*
   or * argument; 12,596 of the rest have a *PRO* argument (Table 1, Table 6). The
   single largest effect is passive: 5,555 transitive verbs look
   intransitive.
2. **Passive is regular and nearly always marked.** 5,624 of 5,639 * objects
   are on VBN (the other 15 are mostly tagging slips, e.g. `spam/111344#4` *re-set*
   VB). 4,318 (76.6%) are coindexed with their own clause's subject. That
   subject is itself a trace in 1,033 cases (*PRO* 492, *T* 432, * 109), so
   the chain runs one or two steps further. Reduced relatives (1,119) carry
   an unindexed * and are resolved by position. Only 16.5% of passives
   have a by-phrase (14.4% of full passives, 24.2% of reduced relatives).
3. **Subject extraction is almost all relative clauses with overt
   relativizers. Object extraction is split, and half of it is invisible.**
   76.8% of *T* subjects are in relative clauses, and 91.8% have an overt
   WHNP, mostly *that* (Table 3). Of the *T* objects, 45.2% have a null
   operator (Table 2), and 69.5% of object relatives are contact relatives
   (*the problem they were solving*, Example 2). In these, nothing in the
   backbone string or tree marks the gap.
4. **Most *T* at verbs is adverbial.** 2,961 *T* are modifiers: WHADVP
   traces for *when, where, how, why* (2,231 overt, 121 null). 890 of them
   are in adverbial clauses (SBAR-TMP and the like), where the annotation
   posits an ADVP-TMP trace in the lower clause (e.g.
   `debate-transcript/3rd_Bush-Kerry#1039`, *When I met her ... *T**). These
   add modifiers, not arguments, so they do not change the frames.
5. **Clausal and predicative *T* are quotation and inversion.** 496 of 498
   clausal *T* have a topicalized clause as antecedent (S-TPC 362, SBARQ-TPC
   53, S-IMP-TPC 40). This is the quotative *"...," he said* and the
   parenthetical *X, I think* (Example 8). The 478 predicative *T* are
   copulas with fronted or questioned predicates: *Here's ...* (63
   ADVP-LOC-PRD-TPC), *what X is*, *how are you*. For verbs of saying and
   the copula, the backbone often shows a bare `SBJ` frame.
6. **The * subject is mostly semi-modal "raising".** Half of the 2,225 *
   subjects are under *have to, need to, going to, ought to* (1,105). Then
   come aspectuals (425), true raising verbs (199) and passive matrices
   (166). 94.7% are coindexed with the matrix subject one clause up.
7. **The line between * and *PRO* is not uniform across MASC.**
   - The WSJ files have 18 *PRO* against 211 * and follow PTB-II, where *
     also covers control. 62 of their 102 * subjects are unindexed (arbitrary
     PRO) or under non-raising verbs (*decided*, *declined*, *expects*, *wants*;
     Example 13).
   - Elsewhere the split is mostly consistent but leaky. *like* has 102
     *PRO* and 8 *; *help* 97 and 13; *get* 13 and 15; *used* 53 and 31;
     *seems* 35 * and 9 *PRO*.
   - 21 imperative clauses (S-IMP) have * instead of *PRO* (Example 14);
     2,274 have *PRO*.

   Counts of "raising" versus "control" read off these labels therefore mix
   annotation convention with grammar.
8. **Restoration is local and nearly always unambiguous** (Table 7).
   - Apart from 2 dangling indices (both annotation errors), every index
     resolves inside the sentence tree.
   - Of the 17,784 traces, 15,914 (89.5%) reach an overt phrase or a null
     operator through indices alone.
   - With the structural step from relativizer to head NP (5,680 cases) and
     the reduced-relative head (1,113), about 16,800 (94%) end at overt
     words.
   - The rest: 240 null operators outside relatives (tough, *too/enough*,
     clefts, purpose) need a construction-specific rule (Example 9); about
     450 end in an unindexed *PRO* (arbitrary control); 25 end at an
     unresolved index clash; 3 dangle or loop; and about 270 have no index
     and are not in reduced relatives (153 objects, 115 subjects, 4 *T*).
   - 139 chains met a clash (an index used on two nodes; 147 trees in MASC
     have one) that sort and c-command settled.
9. **Some traces sit where `verbframes.py` does not look, or it reads them
   differently.**
   - PPs with only an empty object count as overt complements or modifiers:
     332 stranded prepositions and 61 prepositional passives (Example 11).
   - `(SBAR (-NONE- 0) (S (-NONE- *T*-n)))` appears in frames as
     `SBAR(0)` (58 cases), and 29 SBARs of `0` + `*?*` look the same.
   - The by-phrase agent is classed as a modifier, so `frame_full` of a
     passive never shows its logical subject.
   - An extraposed VP (`VP-1` with `*ICH*-1` in the subject NP) is a
     daughter of the host S and takes that clause's subject (Examples 16;
     also `non-fiction/ch5#15`).
10. **Literal asterisks are annotated as empty elements.** 301
    `(SYM (-NONE- *))` in total: 215 in `movie-script/pirates`, 26 in
    twitter, plus LS and NN cases. They are emphasis or bullet characters
    of the text. They are not at verbs, but they inflate any raw count of
    unindexed *.

## What it means

**(a) The context-free backbone and the fast parser.**
- Stripping traces makes the backbone grammar treat 7,629 transitive uses
  as intransitive, 150 ditransitives as transitives, and 6,000 clauses as
  subjectless. For passives the loss is mostly cosmetic: VBN under a form
  of *be* or *get* already marks the construction. The fast parser loses no
  string coverage, but the VP rules it learns (VP → VBN PP) conflate
  passive with active intransitive.
- Unary collapse gives subject gaps a symbol of their own, `SxVP`, but that
  symbol also covers *PRO* and * subjects (Example 1). It is a crude,
  unlabelled slash category.
- Object gaps have no such marker. `VP → VBG` inside a contact relative is
  the same rule as an intransitive VP. A GPSG-style slash feature, or
  keeping `(NP *)` as `normalise(keep_empty_np=True)` already offers, would
  distinguish them.
- My estimate (speculative) is that keeping the NP gaps costs few rules,
  because they are mostly adjacent to the verb (4,350 of 4,355 indexed *
  objects cross no clause). Long-distance *T* are what a slash feature
  would have to thread: about 370 argument and 165 modifier traces cross
  two or more clauses.

**(b) Verb frames and the complement/modifier distinction.**
- Frames read off the backbone undercount transitivity, most for verbs that
  are often passive or often relativized on the object (*said*, *done*,
  *called*, *given*). Frames for subcategorization should come from
  `frame_full`, or from the backbone with passive and extraction restored.
- Traces are mostly argument traces. 14,823 of the 17,784 are subjects or
  complements. The 2,961 modifier traces are almost all WHADVP traces of
  *when/where/how/why*.
- In Dowty's terms (from memory), the passive's surface subject is the
  proto-patient. The NP-LGS by-phrase, which the function-tag scheme calls
  a modifier, carries the proto-agent argument of the verb. It is optional
  (16.5%) and non-promiscuous (only *by*). A complement/modifier split for
  frame work should arguably treat it as an oblique argument of the verb
  with the passive trace restored.
- Adverbial *T* supports the owner's point that modifiers carry
  communicative weight. A *when*-clause makes the event a temporal
  modifier of the matrix, and the treebank records that with a trace.

**(c) The flat neo-Davidsonian semantics (docs/flat-semantics.md).**
- As `interp.Flat` names relations (by function tag, marker or
  configuration), a passive gives `sbj(e, patient)` and `by(e, agent)`, and
  a relative gives `which(x, e)`, as that document notes.
- With coindexation, a gold-tree reading could emit instead:
  - `obj(e, x)` for the surface subject of a passive, from the * trace;
  - `sbj(e, x_head)` or `obj(e, x_head)` for the relative head;
  - `sbj(e2, x)` for raising;
  - the right argument for the quoted clause under *say*.
- Table 7 bounds what that buys: about 94% of the traces at verbs would get
  an overt filler. The remaining cases need rules for tough constructions,
  clefts, purpose clauses, and arbitrary *PRO* (which has no filler).
- For parser output there are no indices. Recovering these relations would
  need either a trained empty-element restorer or rules keyed on SxVP,
  WHNP siblings and VBN after *be*. On MASC those rules would be exact only
  for subject relatives and simple passives.

## Annotation errors found

| id | what is wrong |
|---|---|
| debate-transcript/3rd_Bush-Kerry#484 (also #679, blog/Effing-Idiot#239, jokes/jokes1#68) | index 1 on both ADVP-PRD-TPC and WHNP in *Here's what I do*; object trace of *do* ambiguous |
| debate-transcript/2nd_Gore-Bush#125, #47; court-transcript/Lessig-court-transcript#281 | an index reused in one tree (147 trees in MASC; 25 chains stay ambiguous after sort and c-command) |
| blog/blog-jet-lag#35 | NP-SBJ-1 (*) and SBAR-1 (the *EXP* target) share index 1 |
| govt-docs/Postal_Rate_Comm-ReportToCongress2002WEB#48; non-fiction/rybczynski-ch3#362 | antecedent labelled `=n` (gapping) where `-n` is needed; the * trace dangles |
| journal/ArticleIP_1059#10 | `(NP-SBJ-1 (-NONE- *T*-1))`: the *T* points to its own NP; no WH operator carries index 1 |
| fiction/The_Black_Willow#190; court-transcript/Day3PMSession#429 | passive * in an absolute / small clause with an overt subject but no index (about 30 such unindexed * with an overt own subject) |
| debate-transcript/2nd_Gore-Bush#972 and 20 other S-IMP clauses | imperative with NP-SBJ `*` instead of `*PRO*` |
| wsj/* (e.g. wsj_0026#4, wsj_0106#4, wsj_0006#1) | control marked `*` (PTB-II convention), unlike the rest of MASC |
| spam/111344#4, spam/114423#20, journal/Article247_500#20, twitter/tweets1#394 | passive participle tagged VB/VBD (*re-set, transfer, reset, cut*) |
| enron/175448#16 | *Amount Due Employee* annotated as a passive of VBN *Due* with an NP object |
| movie-script/pirates (215), twitter, blog/Italy#20 etc. | 301 literal asterisks annotated `(SYM (-NONE- *))` |

## Open questions

1. Should `verbframes.py` treat the NP-LGS by-phrase as an argument and look
   inside PPs for stranded and passive traces? Both change the
   complement/modifier counts for frames.
2. Should the backbone keep NP gaps, as `normalise(keep_empty_np=True)`
   does, or thread a slash feature? The cost in rules and parse time has not
   been measured.
3. The * versus *PRO* split cannot be used as a raising/control diagnostic
   without normalizing the WSJ files and the leaky verbs (*like, help, get,
   used*). A lexical list (Levin-style, from memory) may be safer than the
   labels.
4. Tough constructions, *too/enough*, clefts and purpose clauses (240 null
   operators without a head) need construction-specific linking rules, which
   I have not written or checked.
5. How well could a parser-side restorer do on MASC? Subject relatives and
   simple passives look close to deterministic, but contact relatives and
   long-distance *T* (about 370 argument traces) do not.
