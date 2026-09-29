# Traces *T* and * at MASC's verbs: extraction, passive and raising

About a fifth of MASC's lexical verbs (14,139 of 70,101) have a subject or
complement that is a *T* or * trace, and the backbone loses that argument.
Of the 31,423 verbs with an NP object in the full frame, 7,629 (24.3%) look
intransitive to the backbone, and 6,000 verbs lose their subject. The causes
are few and systematic: passives, relative clauses, questions, raising and
quotation. Coindexation inside the sentence tree links 89.5% of the traces
to an antecedent. Two structural steps (relativizer to head noun, reduced
relative to its head) bring about 94% to an overt filler. Annotation errors,
mostly clashing or missing indices, touch about 1% of the traces.

## Question

What do *T* (wh-movement, topicalization) and * (NP movement: passive,
raising) do to verb frames when the backbone strips them? Which
constructions are responsible, how often, and how reliably can coindexation
restore the full argument structure?

## Data and method

`tools/masc/verbs/traces.py` reuses `verbframes.py`'s definitions (lexical
verb, clause, subject, complement/modifier) and writes one record per *T* or
* subject, complement or modifier of a lexical verb (17,784 records):

```
S=/tmp/claude-0/-home-user/32748c59-1ad9-5140-855d-e9a1edeaf50a/scratchpad
python3 tools/masc/verbs/traces.py $S/masc/data $S/traces       # traces.jsonl, traces_extra.json
python3 tools/masc/verbs/traces.py --report $S/traces $S/verbs/verbs.jsonl   # prints the tables
```

**Chains.** From a trace's index (`*T*-1`) the script goes to the labelled
node carrying `-1`, never `=1`, which marks gapping. While that node is
itself empty (`NP-SBJ-2 (-NONE- *T*-1)`) it continues. It stops at an overt
node, at a null operator `(WHNP-1 (-NONE- 0))`, or at an unindexed empty
element.

**Clashes.** An index on two nodes is a clash. The script settles it when
exactly one candidate both has the right sort (WH or topic for *T*, neither
for *) and c-commands the trace.

**Structural step.** From a WH phrase or null operator in a relative clause
(SBAR under NP, after an NP), one step leads to the head NP.

**SBAR(0).** An SBAR of `(-NONE- 0)` plus `(S (-NONE- *T*-n))` is counted as
*T*.

Further counts in the text (asterisks by genre, * against *PRO* under given
verbs, reduced-relative heads) come from short ad hoc scans of the same
trees.

## Counts

**Table 1. *T* and * arguments of lexical verbs.** Not included: traces
inside a PP daughter of the verb, which `verbframes.py` counts as overt.
These are 332 stranded prepositions (233 in complement PPs) and 61
prepositional passives.

| slot | *T* | * |
|---|---:|---:|
| subject | 3,775 | 2,225 |
| NP object | 2,148 | 5,639 |
| predicative (-PRD) / clausal (S, SBAR, SQ, SBARQ) / other (-CLR, -DTV) | 478 / 498 / 45 | 1 / 14 / 0 |
| modifier (ADVP-TMP, -MNR, -LOC, -PRP ...) | 2,961 | 0 |

**Table 2. *T*: the antecedent**

| antecedent | subject | NP object | predicative | clausal | modifier |
|---|---:|---:|---:|---:|---:|
| WHNP, overt | 3,464 | 1,113 | 123 | 1 | 40 |
| WHNP, null operator 0 | 297 | 971 | 31 | 0 | 346 |
| WHADVP (overt / 0) | 0 | 1 | 58 / 1 | 0 | 2,231 / 121 |
| WHPP, WHADJP (overt / 0) | 0 | 0 | 53 / 31 | 0 | 160 / 3 |
| non-WH topic (S-TPC, NP-TPC, ADVP-LOC-PRD-TPC ...) | 10 | 60 | 180 | 496 | 59 |
| unindexed or unresolved clash | 4 | 3 | 1 | 1 | 1 |

**Table 3. *T* subjects and objects by construction.** The construction is
read off the antecedent's position.

| construction | subject | NP object |
|---|---:|---:|
| relative clause | 2,901 | 1,246 |
| free relative (SBAR-NOM) | 210 | 327 |
| embedded question or free relative under VP | 227 | 194 |
| direct question (SBARQ) | 136 | 151 |
| adverbial SBAR (mostly sentential *which*) | 122 | 20 |
| under ADJP (tough, *too/enough*, adjective + question) | 11 | 60 |
| topicalization (non-WH antecedent) | 10 | 60 |
| 0 operator under VP (cleft, purpose) / other | 20 / 138 | 26 / 64 |

**Relativizers.**

| relative clause | 0 | *that* | *who* | *which* | *whose N* / *Q of which* / other |
|---|---:|---:|---:|---:|---:|
| subject gap (2,901) | 222 | 1,328 | 772 | 480 | 57 / 26 / 16 |
| object gap (1,246) | 866 (69.5%) | 327 | 3 | 42 | 1 / 0 / 7 |

Over all MASC, 232 of the 236 relatives with a null operator and a *T*
subject are infinitival (*a person to stand by my side*).

**Table 4. * objects (passives): 5,639 verbs, 5,624 of them VBN.** A
by-phrase is a PP daughter of the VP that contains NP-LGS. MASC has 1,006
NP-LGS and only 7 PP-LGS.

| antecedent of the object trace | n | with by-phrase |
|---|---:|---:|
| own subject: overt 3,285, *PRO* 492, *T* 432, * 109 | 4,318 | 621 (14.4%) |
| unindexed, VP inside NP (reduced relative) | 1,119 | 271 (24.2%) |
| unindexed elsewhere (absolutes, small clauses) | 146 | 32 |
| another node; index clash; dangling | 56 | 8 |
| all | 5,639 | 932 (16.5%) |

- In 76 passives an overt NP remains (*was given * a copy*).
- In 1,113 of the 1,119 reduced relatives, an NP sister before the VP
  supplies the object.

**Table 5. * subjects (2,225): what the clause is the complement of**

| matrix | n |
|---|---:|
| semi-modal: *have to, need to, going to/gonna, ought to, used to, have got to* | 1,105 |
| aspectual: *begin, start, continue, stop, keep* | 425 |
| raising verb: *seem, appear, tend, happen, fail, turn out* | 199 |
| passive matrix: *be supposed/expected/allowed/said/meant to* | 166 |
| other verb (many are control verbs, Finding 6) | 148 |
| unindexed * (imperatives, reduced relatives, WSJ arbitrary PRO) | 115 |
| matrix object antecedent (*help NP-2 [*-2 build]*) / raising adjective / under S, SQ, SINV | 31 / 19 / 17 |

**Table 6. What stripping *T* and * does to the frames** (70,101 verbs)

| effect | *T* | * |
|---|---:|---:|
| subject missing | 3,775 | 2,225 |
| every NP object missing: looks intransitive (7 have both kinds) | 2,067 | 5,555 |
| one of two NP objects missing: looks monotransitive | 74 | 76 |
| predicative missing (*what it is*, *Here's ...*) | 478 | 1 |
| clausal complement missing (*"...," he said*; *X, I think*) | 523 | 13 |
| preposition left without its object | 332 | 61 |

Of the 7,707 VBN verbs with no overt NP object in the backbone:
- 5,547 (72.0%) are passives with a * object;
- 1,942 have no NP object in the full frame either;
- 210 have a *T* object.

The verb forms that lose their object most often: *do* 224 of 570, *used*
132/182, *called* 80/111, *based* 76/78, *known* 64/73, *shown* 57/58.

**Table 7. How the chain ends.** "WH itself" means the WH phrase is the
argument (questions, free relatives, sentential *which*). "Rel. head" means
one structural step to the head NP. "Broken" means an unresolved clash, a
dangling index or a cycle. The last column counts clashes settled by sort
and c-command.

| trace, slot | n | overt non-WH | WH itself | rel. head via WH | rel. head via 0 | 0, no head | unindexed | broken | clash settled |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| *T* subject | 3,775 | 10 | 774 | 2,690 | 259 | 38 | 3 | 1 | 26 |
| *T* object | 2,148 | 60 | 727 | 387 | 874 | 97 | 0 | 3 | 21 |
| *T* other compl. | 1,021 | 680 | 231 | 45 | 27 | 36 | 1 | 1 | 10 |
| *T* modifier | 2,961 | 59 | 1,874 | 557 | 409 | 61 | 0 | 1 | 19 |
| * subject | 2,225 | 1,888 | 30 | 67 | 1 | 2 | 234 | 3 | 17 |
| * object | 5,639 | 3,556 | 92 | 334 | 30 | 6 | 1,602 | 19 | 46 |

For *, "unindexed" also counts chains that end in an unindexed *PRO* (119
subjects and 337 objects, as in *wants to be seen*). Of the 1,265 * objects
with no index at all, 1,113 are reduced relatives.

**Distance** is the number of clauses between the trace and its antecedent's
parent:
- *T* subjects: 97.5% cross 1 clause, 91 cross 2 or more.
- *T* objects: 61 cross 0 (topicalization), 85.0% cross 1, and 256 (11.9%)
  cross 2 or more.
- Indexed * objects: 4,350 of 4,355 cross 0.
- Indexed * subjects: all but one cross 1.

## Examples

Frames are `frame_full` → `frame_backbone`; the trees are trimmed.

1. `debate-transcript/2nd_Gore-Bush#29`, *called*: `SBJ(*T*) NP(*) S` → `S`.
   `(NP (NP dedicated citizens) (SBAR (WHNP-1 who) (S (NP-SBJ-2 (-NONE- *T*-1)) (VP are (VP called (NP-3 (-NONE- *-2)) (PP by (NP-LGS the president)) (S (NP-SBJ (-NONE- *PRO*-3)) (VP to serve ...)))))))`.
   A passive in a subject relative. The chain `*-2 → *T*-1 → who`, plus one
   step, reaches *dedicated citizens*. In the backbone, *who are called ...*
   and *to serve ...* are both `SxVP`.
2. `court-transcript/Lessig-court-transcript#181`, *solving*: `SBJ NP(*T*)` → `SBJ`.
   `(NP (NP the problem) (SBAR (WHNP-2 (-NONE- 0)) (S (NP-SBJ they) (VP were (VP solving (NP (-NONE- *T*-2)))))))`.
   A contact relative: nothing in the backbone marks the gap.
3. `debate-transcript/2nd_Gore-Bush#125`, *produced* `SBJ NP(*)` → `SBJ`; *doing* `SBJ NP(*T*)` → `SBJ`.
   `(S (NP-SBJ-1 a lot of the energy) (VP is (VP produced (NP (-NONE- *-1)) ...))) , and (S ... (SBAR (WHNP-1 what) (S ... (VBG doing) (NP (-NONE- *T*-1)))))`.
   Index 1 is used twice in the tree. Sort settles both chains.
4. `court-transcript/Day3PMSession#705`, *interviewed*: `SBJ NP(*)` → `SBJ`.
   `(SQ Have (NP-SBJ-1 you) (VP been (VP interviewed (NP (-NONE- *-1)) (PP by (NP-LGS news reporters)) (ADVP-TMP before))))`.
   A plain passive. Its agent is filed as a modifier PP.
5. `jokes/jokes1#260`, *operated*: `NP(*)` → (empty).
   `(NP (NP a people trap) (VP operated (NP (-NONE- *)) (PP by (NP-LGS a mouse))))`.
   A reduced relative: an unindexed *, whose head is the NP sister.
6. `debate-transcript/2nd_Gore-Bush#350`, *do*: `SBJ(*) NP(*T*)` → (empty).
   `(SBAR-PRD (WHNP-1 what) (S (NP-SBJ-2 it) (VP 's (VP meant (S (NP-SBJ (-NONE- *-2)) (VP to (VP do (NP (-NONE- *T*-1)))))))))`.
   Raising through a passive matrix, plus an object extracted across two
   clauses. The backbone frame of *do* is empty.
7. `court-transcript/Day3PMSession#1121`, *given*: `SBJ NP(*) NP` → `SBJ NP`.
   `(S (NP-SBJ-1 She) (VP was n't (VP given (NP (-NONE- *-1)) (NP a copy of ...))))`.
   A double-object passive that looks like an ordinary transitive.
8. `face-to-face/Bed012#507`, *guess*: `SBJ SBAR(0)` → `SBJ`.
   `(S (S-TPC-1 That 's true) , (NP-SBJ I) (VP guess (SBAR (-NONE- 0) (S (-NONE- *T*-1)))))`.
   A parenthetical with a fronted clause. `verbframes.py` names the gap
   `SBAR(0)` after its first leaf (compare `w3c/lists-003-2180740#6`,
   `SBAR(*T*)`).
9. `ficlets/1400#528`, *get*: `SBJ(*PRO*) NP(*T*)` → (empty).
   `(S (NP-SBJ She) (VP was (ADJP-PRD (ADJP easy) (SBAR (WHNP-1 (-NONE- 0)) (S (NP-SBJ (-NONE- *PRO*)) (VP to (VP get (NP (-NONE- *T*-1)))))))))`.
   A tough construction. The chain stops at the null operator, and nothing
   indexes *She*.
10. `debate-transcript/3rd_Bush-Kerry#484`, *'s* `SBJ ADVP-PRD(*T*)` → `SBJ`; *do* `SBJ NP(*T*)` → `SBJ`.
    `(SINV (ADVP-PRD-TPC-1 Here) (VP 's (ADVP-PRD (-NONE- *T*-1))) (SBAR-NOM-SBJ (WHNP-1 what) (S I (VP do (NP (-NONE- *T*-1))))))`.
    Index 1 is on two nodes. For *'s*, c-command picks the topic. For *do*,
    both candidates c-command the trace, so the chain stays unresolved.
11. `court-transcript/Day3PMSession#697`, *talk*: `SBJ PP-CLR` → `SBJ PP-CLR`.
    `(SBARQ (WHNP-1 Who) (SQ did (NP-SBJ you) (VP talk (PP-CLR (TO to) (NP (-NONE- *T*-1))))))`.
    A stranded preposition. The two frames agree, but the backbone PP is
    just `PP → TO`.
12. `court-transcript/Lessig-court-transcript#149`, *allows*: `SBJ(*T*) S` → `S`.
    `(... (SBAR-ADV (WHNP-1 which) (S (NP-SBJ (-NONE- *T*-1)) (VP allows (S us to make ...)))))`.
    Sentential *which*: the subject is the preceding proposition.
13. `wsj/wsj_0026#4`, *grant*: `SBJ(*) NP` → `NP`.
    `(S (NP-SBJ-1 Mr. Bush) (VP decided (S (NP-SBJ (-NONE- *-1)) (VP to grant ...))))`.
    Control marked * in a WSJ file.
14. `govt-docs/Postal_Rate_Comm-ReportToCongress2002WEB#48`, *afforded*: `SBJ NP(*)` → `SBJ`.
    `(S (NP-SBJ=2 an opportunity ...) (VP is (VP=3 afforded (NP (-NONE- *-2)) ...)))`.
    The antecedent is marked `=2` (gapping) instead of `-2`, so the trace
    dangles.
15. `essays/Black_and_white#41`, *seen*: `SBJ NP(*)` → `SBJ`.
    `(S (NP-SBJ (NP color) (VP (-NONE- *ICH*-1))) (VP became ... , (VP-1 seen (NP (-NONE- *)) (PP as ...))))`.
    An extraposed reduced relative, which `verbframes.py` gives the host
    clause's subject.

## Findings

1. **Traces cause about half of all frame changes.** Of the 26,947 verbs
   whose frame changes, 14,139 (52%) have a *T* or * argument, and 12,596 of
   the rest have a *PRO* one. The largest single effect is the passive:
   5,555 transitive verbs look intransitive (Table 6).
2. **The passive is regular and nearly always visible.**
   - 5,624 of 5,639 * objects are on VBN; the rest are mostly tagging slips.
   - 76.6% are coindexed with their own clause's subject. That subject is
     itself empty in 1,033 cases, so the chain goes further (Example 1).
   - The 1,119 reduced relatives are unindexed and resolved by position.
   - Only 16.5% have a by-phrase (Table 4).
3. **Subject extraction is relativization with an overt relativizer.**
   76.8% of *T* subjects are in relative clauses, and 91.8% have an overt
   WHNP, *that* most often.
4. **Object extraction is half invisible.** 45.2% of *T* objects have a null
   operator. In object relatives, 69.5% are contact relatives (Example 2),
   which leave no WH word in the backbone.
5. **The rest of *T* is modifiers, quotation and inversion.**
   - 2,961 *T* are WHADVP-type modifiers, 890 of them in adverbial clauses
     (*When I met her *T**, `debate-transcript/3rd_Bush-Kerry#1039`). These
     do not touch frames.
   - 496 of 498 clausal *T* are fronted quotations or parentheticals
     (S-TPC 362, SBARQ-TPC 53, S-IMP-TPC 40).
   - The 478 predicative *T* are copulas with a fronted or questioned
     predicate (*Here's ...*, *what X is*, *how are you*).
   - For verbs of saying and the copula, the backbone often shows a bare
     `SBJ`.
6. **The * subject is mostly semi-modal, and the line between * and *PRO*
   varies.**
   - Half of the * subjects (1,105) are under *have/need/going/ought to*;
     only 199 are under classic raising verbs.
   - The WSJ files follow PTB-II (18 *PRO* against 211 *): 62 of their 102
     * subjects are arbitrary or controlled (Example 13).
   - Elsewhere the split leaks: *like* has 102 *PRO* and 8 *, *help* 97 and
     13, *get* 13 and 15, *used* 53 and 31, *seems* 35 * and 9 *PRO*.
   - 21 S-IMP clauses have * where 2,274 have *PRO*.
   - Raising against control, read off these labels, therefore mixes
     convention with grammar.
7. **Restoration is local and nearly always unambiguous** (Table 7).
   - Apart from 2 dangling indices, every index resolves inside its tree.
   - 15,914 traces (89.5%) reach an overt phrase or a null operator through
     indices alone.
   - The relative-head step (5,680) and the reduced-relative head (1,113)
     bring about 16,800 (94%) to overt words.
   - Still unresolved:
     - 240 null operators with no head (tough, *too/enough*, clefts,
       purpose), which need construction rules (Example 9);
     - about 450 chains that end in an arbitrary *PRO*;
     - about 270 unindexed * and *T* outside reduced relatives;
     - 28 broken chains.
   - 139 chains met a clash (147 trees reuse an index) that sort and
     c-command settled.
8. **`verbframes.py` misses or misreads some traces** (see Examples 8, 11
   and 15).
   - PPs whose only object is empty count as overt: 332 stranded
     prepositions and 61 prepositional passives.
   - `SBAR(0)` hides 58 *T* gaps, and 29 *?* gaps.
   - The NP-LGS agent is a modifier, so `frame_full` never shows it.
   - An extraposed VP (`VP-1` linked by `*ICH*-1`) takes the host clause's
     subject; see also `non-fiction/ch5#15`.
9. **Literal asterisks are annotated as empty elements:** 301 cases of
   `(SYM (-NONE- *))`, 215 of them in `movie-script/pirates` and 26 in
   twitter. None is at a verb, but they inflate raw counts of *.

## What it means

**(a) The backbone and the fast parser.**
- The backbone grammar learns 7,629 transitive uses as intransitive (VP →
  VBN PP, VP → VBG), 150 ditransitives as transitives, and 6,000 subjectless
  clauses. It loses no string coverage.
- For passives little is lost, since VBN under *be/get* marks them.
- Unary collapse makes `SxVP` a crude, unlabelled slash category for
  subject gaps. It is shared with *PRO* and * subjects (Example 1).
- Object gaps have no mark at all. Keeping `(NP *)`, which
  `normalise(keep_empty_np=True)` already offers, or a GPSG-style slash
  feature would separate them.
- Speculatively, the cost is small for NP-movement: 4,350 of 4,355 indexed
  * objects are clause-local. Long-distance *T* (about 370 argument traces
  and 165 modifier traces crossing two or more clauses) is what a slash
  feature must thread.

**(b) Verb frames and complements versus modifiers.**
- Backbone frames undercount transitivity, most for verbs often passive or
  object-relativized (*used, called, based, known, shown*). Subcategorization
  should be read from `frame_full`, or from a backbone with traces restored.
- The traces are mostly argument traces: 14,823 of 17,784 are subjects or
  complements. The 2,961 modifier traces are almost all *when/where/how/why*.
- On Dowty's proto-roles (from memory), the NP-LGS by-phrase carries the
  proto-agent. The function-tag scheme calls it a modifier, yet it is
  optional (16.5%) and not promiscuous (only *by*). A frame inventory should
  arguably treat it as an oblique argument once the passive trace is
  restored.
- Adverbial *T* records temporal and locative modification that the
  annotators chose to represent as movement. It is part of the
  communicative work that modifiers do.

**(c) Flat neo-Davidsonian semantics.**
- `interp.Flat` names relations by tag, marker or configuration
  (`go/interp/semantics.go`, `relation`). It therefore gives `sbj(e,
  patient)` and `by(e, agent)` for a passive, and `which(x, e)` for a
  relative.
- On gold trees, coindexation would allow:
  - `obj(e, x)` for a passive subject;
  - `sbj`/`obj` of the relative head;
  - `sbj(e2, x)` for raising;
  - the quoted clause as the object of *say*.
- By Table 7, about 94% of the traces at verbs would get an overt filler.
  The rest needs rules for tough constructions, clefts and purpose clauses,
  and arbitrary *PRO* has no filler.
- On parser output, which has no indices, rules keyed on `SxVP`, WHNP
  siblings and VBN after *be* would be exact only for subject relatives and
  simple passives.

## Annotation errors found

| id | what is wrong |
|---|---|
| debate-transcript/3rd_Bush-Kerry#484, #679; blog/Effing-Idiot#239; jokes/jokes1#68 | index 1 on both a -PRD-TPC topic and a WH phrase (*Here's what I do*); the object trace is ambiguous |
| debate-transcript/2nd_Gore-Bush#47, #125; court-transcript/Lessig-court-transcript#281 | index reused within one tree (147 trees; 25 chains stay ambiguous) |
| blog/blog-jet-lag#35 | NP-SBJ-1 (*) and SBAR-1 (the *EXP* target) share index 1 |
| govt-docs/Postal_Rate_Comm-ReportToCongress2002WEB#48; non-fiction/rybczynski-ch3#362 | antecedent marked `=n` instead of `-n`; the * trace dangles |
| journal/ArticleIP_1059#10 | `(NP-SBJ-1 (-NONE- *T*-1))`: the *T* points to its own NP |
| fiction/The_Black_Willow#190; court-transcript/Day3PMSession#429 | passive * in an absolute or small clause with an overt subject but no index (about 30 such) |
| debate-transcript/2nd_Gore-Bush#972 and 20 other S-IMP | imperative subject `*` instead of `*PRO*` |
| wsj/wsj_0026#4, wsj_0106#4, wsj_0006#1 (and the WSJ files generally) | control marked `*` (PTB-II convention), unlike the rest of MASC |
| spam/111344#4, spam/114423#20, journal/Article247_500#20, twitter/tweets1#394 | passive participle tagged VB (*re-set, transfer, reset, cut*) |
| enron/175448#16 | *Amount Due Employee*: *Due* as a VBN passive with an NP object |
| movie-script/pirates (215), twitter (26), blog/Italy#20 | literal asterisks annotated `(SYM (-NONE- *))` (301 in all) |

## Open questions

1. Should `verbframes.py` treat NP-LGS as an argument, and look inside PPs
   for stranded and passive traces?
2. Should the backbone keep NP gaps or thread a slash feature? The cost in
   rules and parse time is unmeasured.
3. Can * versus *PRO* serve as a raising/control test after normalizing the
   WSJ files? Or is a lexical list safer?
4. What linking rules do tough constructions, *too/enough*, clefts and
   purpose clauses (240 null operators) need? I have not checked any.
5. Could a restorer recover contact relatives and the roughly 370
   long-distance argument traces from parser output?
