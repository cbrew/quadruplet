# Empty *PRO* subjects in MASC: what they are and what the backbone makes of them

The 13,459 lexical verbs of MASC whose subject is *PRO* are not one phenomenon. About two fifths are
control in the classical sense (a subjectless clause that is the complement of a verb or an
adverbial of it), 18% are imperatives, 8% are dropped subjects of main clauses (mostly tweets), 14%
are gerunds, and the rest are infinitives in nominal, adjectival, relative and extraposed positions.
Only a third of these *PRO*s are coindexed, and whether they are depends more on the construction
than on whether there is a controller; where there is an index, the controller is a lexically
predictable subject or object. In the backbone the subjectless clause becomes a fused SxVP symbol in
81% of cases; the clause is never lost from the label, but the complement/adjunct distinction
between "encouraged them to do it" and "sent me to settle his debt" is.

## Question

What constructions do the *PRO* subjects of MASC come from; how often, and by what, are they
controlled (per the treebank's coindexation); what does the context-free backbone (empty elements
and function tags removed, unary chains collapsed) make of the clauses they head; and what does all
this mean for the backbone parser, for verb frames and for the flat event semantics?

## Data and method

Inputs (read-only) are those of the brief: the raw MASC trees, `verbs.jsonl` from
`tools/masc/verbframes.py`, and the backbone trees in `annotated.jsonl`. Everything below comes from
one script, which reuses verbframes.py's definitions of lexical verb, clause and subject:

    S=/tmp/claude-0/-home-user/32748c59-1ad9-5140-855d-e9a1edeaf50a/scratchpad
    python3 tools/masc/verbs/pro_subjects.py $S/masc/data $S/ann/annotated.jsonl $S/pro/pro.jsonl > $S/pro/tables.txt

(8 s). For every lexical verb whose clause has an -SBJ daughter consisting of `*PRO*` or `*PRO*-n`
it writes one JSON record and tallies:

* **construction group**, from the clause's function tags and its parent, in this order: S-IMP, or
  an untagged root-level clause with a bare VB and no index (imperative); a clause that an `*EXP*`,
  `*ICH*` or `*RNR*` points to (displaced); S-NOM (gerund; if under VP, counted as complement of
  verb); S-ADV/PRP/MNR/TMP/... or an SBAR with a subordinator (adverbial); S-PRD, S-SBJ, or an
  untagged S after *be* (predicative / subject); untagged S or S-CLR under VP (complement of verb);
  SBAR with a wh-word (wh- or relative infinitive); under ADJP; under NP; a root-level or
  coordinated clause with a finite verb or VBG/VBN (subject drop); anything else (other).
* **controller**: the node whose label carries the *PRO*'s index (preferring, where an index occurs
  more than once, one outside the clause that c-commands it), classified by its relation to the
  nearest clause above: its subject; an NP daughter of a verb phrase between the two (object;
  "passive trace" if that NP is itself `*-n`); other. Chains of empty antecedents are followed to an
  overt phrase for display only.
* **backbone label**: the label of the outermost backbone node spanning the clause's overt words.
* extra tables: the subject of the S complement of 31 raising and control verbs; of adjectives; the
  subject of every S-IMP; a test of the Minimal Distance Principle; and the subject of every S node
  against its backbone label.

13,459 = verbframes.py's 13,458 `empty subject *PRO*` plus one `*PRO-1` typo (see the errors table).
The verbs sit in 12,540 distinct clauses.

## Counts

**Table 1. Construction group, coindexation, controller, backbone** (verbs)

| group | verbs | % | no index | subject of nearest clause | object in governing VP (via passive trace) | other indexed | S fused with VP |
|---|---|---|---|---|---|---|---|
| adverbial | 2918 | 21.7 | 781 (27%) | 2008 | 99 (39) | 30 | 95% |
| complement of verb | 2442 | 18.1 | 374 (15%) | 1462 | 578 (156) | 28 | 98% |
| imperative | 2387 | 17.7 | 2385 (100%) | 1 | 0 | 1 | 28% |
| gerund (S-NOM) | 1857 | 13.8 | 1755 (95%) | 78 | 23 (5) | 1 | 96% |
| subject drop (main clause) | 1111 | 8.3 | 1103 (99%) | 6 | 0 | 2 | 53% |
| wh- or relative infinitive | 744 | 5.5 | 720 (97%) | 12 | 0 | 12 | 99% |
| complement of noun | 699 | 5.2 | 691 (99%) | 5 | 0 | 3 | 99% |
| complement of adjective | 524 | 3.9 | 452 (86%) | 72 | 0 | 0 | 99% |
| displaced (*EXP* 277, *ICH* 44, *RNR* 1) | 322 | 2.4 | 321 (100%) | 0 | 0 | 1 | 98% |
| predicative / subject | 232 | 1.7 | 198 (85%) | 33 | 1 | 0 | 97% |
| other | 223 | 1.7 | 207 (93%) | 11 | 0 | 5 | 86% |
| **total** | **13459** | 100 | **8987 (67%)** | **3688** | **701 (201)** | **83** | **81%** |

Main sub-constructions: S-IMP 2,274 and untagged imperatives 113 ("Let 's go"); S-ADV 1,405, S-PRP
1,222, SBAR with *as/while/after/before/if/than* etc. 278; untagged S complement 2,093, S-CLR 222,
gerund complement 127; gerund as object of a preposition 1,520, as subject 197; subject drop with
VBG at the root 395 ("Watching the podcast"), finite at the root 292, VBN at the root or in
coordinated and fragmentary clauses 424. Twitter alone has 750 of the 1,111 subject drops; spam
(365), jokes (284) and movie scripts (239) have the most imperatives.

**Table 2. Complements of verbs: controller by governing verb** (indexed and not; lemmas merged by
hand from word forms)

| verb | n | subject | object | no index |
|---|---|---|---|---|
| want (incl. *wanna*) | 403 | 401 | 0 | 1 |
| try | 240 | 225 | 0 | 13 |
| help | 152 | 34 | 42 | 75 |
| use (*use X to do*) | 134 | 94 | 7 | 31 |
| like | 107 | 96 | 10 | 1 |
| ask | 71 | 3 | 68 | 0 |
| decide | 68 | 68 | 0 | 0 |
| require | 43 | 1 | 37 | 5 |
| tell | 43 | 1 | 41 | 0 |
| force | 23 | 0 | 23 | 0 |

**Table 3. Minimal Distance Principle** (controller = the nearest NP in the governing VP before the
clause, else the subject; indexed clauses under VP)

| group | predicted right | predicted wrong | accuracy |
|---|---|---|---|
| complement of verb | 1906 | 160 | 92% |
| adverbial | 1101 | 721 | 60% |
| adverbial, if always "subject" | 2008 | 129 | 94% |

**Table 4. Raising vs control annotation** (subject of the S complement)

| governor | `*-n` (raising) | `*PRO*-n` / `*PRO*` (control) |
|---|---|---|
| seem/seems/seemed | 101 | 8 (6 extraposed: *It seems a shame to ...*) |
| begin(s)/began/start(ed)/continue(s/d) | 285 | 2 |
| going (*to*) | 328 | 2 |
| want/wanted | 0 | 311 |
| try/tried | 1 | 117 |
| adjective *likely* | 5 | 27 |
| adjective *able* | 0 | 147 (126 without index) |
| adjective *about* (*to*) | 12 | 16 |

**Table 5. What the backbone makes of every S node, by its subject**

| subject of S | fused into ...SxVP | kept as S | other (SxADJP, SxNP ...) |
|---|---|---|---|
| overt | 0 | 45,098 | 94 |
| `*PRO*` | 10,249 | 2,415 | 558 |
| `*T*` | 3,271 | 199 | 39 |
| `*` | 2,122 | 60 | 76 |
| none | 85 | 4,191 | 828 |

Of the 15,729 fused S+VP symbols, 65% come from *PRO*, 21% from *T*, 13.5% from `*`. Where a *PRO*
clause keeps its S (19% of verbs), the S has another daughter: final punctuation (root imperatives
and subject drops: "VP Period" 826), an S-level adverb, mostly *not* or *never* (ADVP/RB VP, 328),
INTJ, CC, a REF speaker label, a fronted SBAR or PP. The 2,103 S-IMP clauses have subject `*PRO*`
2,016, none 65, `*` 20, overt 2.

## Examples

Trees trimmed; `BB` is the backbone. Frames are verbframes.py's.

1. `face-to-face/Bed012#139` *know*: subject control, the ordinary case. `(S (NP-SBJ-1 He) (VP wants
   (S (NP-SBJ *PRO*-1) (VP to (VP know (SBAR where ...))))))`; BB `(VP wants (SxVP to (VP know (SBAR
   ...))))`. full `SBJ(*PRO*) SBAR`, backbone `SBAR`; *wants* is `SBJ S` in both, though the grammar
   sees SxVP.
2. `debate-transcript/3rd_Bush-Kerry#882` *do*: object control. `(VP encouraged (NP-1 them) (S
   (NP-SBJ *PRO*-1) (VP to (VP do (NP it)))))`; BB `(VP encouraged NP SxVP)`. full `SBJ(*PRO*) NP`,
   backbone `NP`.
3. `ficlets/1402#490` *be*: object control through a passive. `(NP-SBJ-1 I) (VP was (VP programmed
   (NP-2 *-1) (S (NP-SBJ *PRO*-2) (VP to (VP be (ADJP-PRD scared))))))`. The index goes to the
   object trace, whose chain ends in *I*.
4. `movie-script/pirates#1059` *settle*: object-controlled purpose clause. `(VP sent (NP-1 me)
   (S-PRP (NP-SBJ *PRO*-1) (VP to (VP settle ...))))`; BB `(VP sent NP SxVP)`, the same shape as
   example 2. *sent* is `SBJ NP` (S-PRP is a modifier); *encouraged* in 2 is `SBJ NP S`.
5. `blog/blog-monastery#4` *touching*: subject-controlled S-ADV inside VP. `(NP-SBJ-1 Orange-gold
   light) (VP filters (PP-DIR ...) , (S-ADV (NP-SBJ *PRO*-1) (VP touching ...)))`; BB `(VP filters
   PP Comma (SxVP touching NP ADVP))`.
6. `debate-transcript/3rd_Bush-Kerry#298` *increase*: obligatory control, no index. `(NP-SBJ You)
   (VP voted (S (NP-SBJ *PRO*) (VP to (VP increase (NP taxes)))) (NP-TMP 98 times))`.
7. `court-transcript/Day3PMSession#13` *Let*: imperative not tagged IMP. `(S (NP-SBJ *PRO*) (VP Let
   (S (NP-SBJ 's) (VP go ...))) (. .))`; BB `(S (VP Let (S NP VP)) Period)`: S survives only because
   of the period.
8. `debate-transcript/2nd_Gore-Bush#972` *Go*: S-IMP with `*`, not `*PRO*`. `(S-IMP (NP-SBJ *) (VP
   Go (ADVP ahead)) (. .))`; counted by verbframes.py as `empty subject *`, not as imperative or
   *PRO*.
9. `movie-script/pirates#656` *get*: control by an imperative's *PRO*. `(S-IMP (NP-SBJ-1 *PRO*) (VP
   Go (S-PRP (NP-SBJ *PRO*-1) (VP get (NP them)))) (. !))`. One of 429 indexed *PRO*s whose chain
   ends in another empty element.
10. `twitter/tweets1#856` *Bought*: finite subject drop annotated as *PRO*. `(S (NP-SBJ *PRO*) (VP
    Bought (NP an awesome suit) (NP-TMP last night) ...))`; BB `(SxVP Bought NP NP PP)` at the root.
11. `face-to-face/Bmr021#95` *do*: wh-infinitive, arbitrary. `(VP know (SBAR (WHNP-1 what) (S
    (NP-SBJ *PRO*) (VP to (VP do (NP *T*-1))))))`; full `SBJ(*PRO*) NP(*T*)`, backbone empty frame.
12. `ficlets/1402#229` *sit*: infinitival relative. `(NP (NP A place) (SBAR (WHNP-1 0) (S (NP-SBJ
    *PRO*) (VP to (VP sit (NP-LOC-CLR *T*-1))))))`; BB `(NP NP (SBARxSxVP to (VP sit)))`: SBAR, S
    and VP are one symbol.
13. `jokes/jokes6#51` *soar*: extraposed subject clause. `(NP-SBJ (NP It) (S *EXP*-1)) (VP is
    (ADJP-PRD hard) (S-1 (NP-SBJ *PRO*) (VP to (VP soar ...))))`; verbframes.py gives *is* the frame
    `SBJ ADJP-PRD S`, the extraposed subject counted as a complement.
14. `debate-transcript/2nd_Gore-Bush#34` *put*: *able*, no index. `(NP-SBJ I) (VP 've (VP been
    (ADJP-PRD (ADJP able) (S (NP-SBJ *PRO*) (VP to (VP put ...))))))`.
15. `journal/Article247_500#19` *back*: the raising adjective *likely* annotated as control.
    `(NP-SBJ-1 Americans) (VP are (ADJP-PRD (ADJP twice as likely (S (NP-SBJ *PRO*-1) (VP to (VP
    back ...))))))`.
16. `debate-transcript/3rd_Bush-Kerry#653` *be*: expletive *there* with *ought*, but *PRO*. `(NP-SBJ
    there) (VP ought (S (NP-SBJ *PRO*) (VP to (VP be (NP-PRD a temporary worker card ...)))))`.
17. `telephone/sw2015-ms98-a-trans#46` *working*: dangling index. `(ADJP-PRD stuck) (S-CLR (NP-SBJ-3
    *PRO*-4) (VP working ...))`: there is no node with index 4; the index 3 on the *PRO* is itself
    the antecedent of a lower `*PRO*-3`.

## Findings

1. **Only about 40% of *PRO* is control proper.** Verb complements (18.1%) and adverbials (21.7%)
   are the constructions that control theory is about; imperatives (17.7%) and dropped main-clause
   subjects (8.3%) are not control at all but are annotated with the same element (Table 1).
   Gerunds, infinitival relatives, and complements of nouns and adjectives make up most of the rest;
   322 verbs are in extraposed or displaced clauses.
2. **Imperatives are 17.7% of the *PRO* mass** (2,387 verbs; 3.4% of all 70,101 verbs): 2,274 in
   S-IMP, 113 untagged ("Let 's go", ex. 7). The convention is not uniform: 20 S-IMP have `*` (ex.
   8) and 65 have no subject, so imperatives are also scattered over verbframes.py's `*` and
   "imperative" causes.
3. **Dropped subjects of finite and participial main clauses are annotated as *PRO*** (1,111 verbs,
   750 in Twitter; ex. 10: *Bought an awesome suit*, *is learning svn*, *Posted by: ...*). Neither
   control nor PRO in the theoretical sense, they are ellipsis of a pragmatically given subject
   (usually the writer). From memory, and uncertain: the Web Treebank guidelines have the same
   convention.
4. **Coindexation depends on the construction, not only on control.** Indexed: complements of verbs
   85%, adverbials 73%, complements of adjectives 14%, gerunds 5%, complements of nouns 1%,
   imperatives and subject drops ~0% (Table 1). "No index" is therefore not "arbitrary": obligatory
   control verbs without an index include *voted* (ex. 6), *consider trying*
   (`nyt/NYTnewswire4#23`), *avoid killing* (`fiction/hotel-california#474`), *intend to say*
   (`face-to-face/Bed012#464`); *able* is indexed 21 times of 147 (ex. 14); *look forward to
   hearing* is indexed (`philanthropic-fundraising/115CVL035#15`) but most gerunds after
   prepositions are not. The genre difference is small (verb complements and adverbials indexed in
   66–91% per genre), except technical (33%).
5. **Where there is an index, the controller is lexically predictable.** In verb complements the
   controller is the matrix subject 71% of the time and the object 28% (156 via a passive trace, ex.
   3). By verb it is almost categorical (Table 2): *want* 401 of 402 indexed cases subject, *decide*
   68/68, *ask* 68/71 and *tell* 41/43 object, *force* 23/23 object; *help* is split and often
   unindexed, as its semantics lead one to expect. Rosenbaum's Minimal Distance Principle (from
   memory: Rosenbaum 1967) predicts 92% of verb complements; its exceptions are *use X to*, *spend X
   doing*, *take X to*, *promise* (`w3c/lists-046-11489622#26`). For adverbials it is wrong (60%);
   "the matrix subject" is right for 94%, with object-controlled purpose clauses (ex. 4) the main
   exception.
6. **Raising and control are distinguished consistently for verbs, not for adjectives** (Table 4):
   *seem*, *begin*, *going to* take `*-n` with a handful of exceptions; *want*, *try* take
   `*PRO*-n`. The raising adjective *likely* is *PRO* 27 times out of 32 (ex. 15), *about to* is
   split. MASC's WSJ files use the older convention (control as `*-n`, only 19 *PRO* in 856 verbs),
   so there control is counted under `*`.
7. **The backbone fuses the subjectless S with its VP in 81% of *PRO* verbs** (SxVP 10,288;
   SBARxSxVP 475; other chains ending in SxVP or SQxVP 118). The clause never disappears from the
   label (every label still contains S), but it becomes a distinct atomic symbol. The S survives
   (19%) only when something else is its daughter: a final period (root imperatives, subject drops)
   or an S-level *not*/*never*. So *to go* and *not to go* get different categories (SxVP vs S), and
   a root imperative is S or SxVP depending on its punctuation (ex. 7 vs 10).
8. **SxVP means "S with an empty subject", not "control clause".** No S with an overt subject is
   ever fused; of all fused S+VP symbols 65% are *PRO*, 21% subject extraction (*T*: *the man who
   left*), 13.5% raising and passive (`*`) (Table 5). The symbol is thus a useful, if accidental,
   finiteness/subjectlessness feature.
9. **In the backbone, control complements and purpose/adverbial clauses look alike.** Of 5,015 SxVP
   daughters of VP that come from *PRO* clauses, 2,389 are complements, 2,083 adverbials (S-PRP
   1,074, S-ADV 996), 283 extraposed subjects, 212 predicatives. Examples 2 and 4 have the same
   backbone VP (V NP SxVP) but different frames; the only evidence is the function tag, which the
   backbone drops.
10. **Annotation errors are few but real**: 147 trees use an index on more than one node (69 of them
    affect a *PRO*'s antecedent), one dangling index, one typo, a few *PRO*s under raising
    predicates (table below).

## What it means

**(a) The context-free backbone and the fast parser.** *PRO* does no damage to context-freeness:
removing it leaves a well-formed subjectless clause, and the fused SxVP symbol even keeps the
information that the subject was empty. What the backbone loses is (i) the difference between
complement and adjunct clauses under VP (finding 9), which the parser must now get from the verb,
and (ii) the controller. It also acquires spurious ambiguity from the collapse: S vs SxVP depends on
punctuation and on *not* (finding 7), so the grammar needs duplicate rules for the same
construction. A backbone that kept an explicit subjectless-clause category (say S with a feature,
rather than a collapsed chain) would lose nothing and generalise better; that is a suggestion, not a
tested result.

**(b) Verb frames and the complement/modifier distinction.** For the controlled verb, *PRO* only
removes the subject: its complements are untouched, so its frame is informative and should be
counted with the subject restored (`SBJ(*PRO*) NP` is a transitive, not an intransitive, frame). For
the controlling verb the frame is sharp and lexical: a verb takes either a subject-controlled
(*want, try, decide*) or an object-controlled (*ask, tell, force, require*) infinitive, almost
without exception (Table 2), which fits the view that control complements are selected, while
adverbial clauses are not. Purpose clauses (S-PRP) are the hard boundary case: they attach inside
VP, are infinitival, are mostly subject-controlled, and are distinguishable from complements only by
the tag; the function-tag learner's F1 for PRP is 58.4 (docs/flat-semantics.md), so this is where
frame extraction from parser output will be least reliable. verbframes.py's `frame_backbone` writes
the complement as `S` where the grammar has SxVP, and counts extraposed subject clauses as
complements of the matrix verb (ex. 13).

**(c) The flat neo-Davidsonian semantics.** At present the event of a controlled verb gets no `sbj`
at all ("Traces" in docs/flat-semantics.md). The data suggest a recovery in layers, from gold trees
or from a parse: (1) where there is an index, `sbj(e2, x)` with x the controller's referent,
following chains through `*-n` traces (201 via passives); (2) otherwise, for a complement of a verb,
a small control lexicon (subject vs object control) backed off to the Minimal Distance Principle
(92%); (3) for S-ADV and S-PRP, the matrix subject (94%); (4) for imperatives, a constant for the
addressee; (5) for dropped main-clause subjects, a constant for the speaker/writer (this is
pragmatics, and should be marked as a default); (6) for the rest, a fresh existentially bound (or
generic) referent, which is what the flat semantics does anyway. Semantically, sharing the referent
is the right move for obligatory control in a Davidsonian setting (from memory: Dowty 1985 and Sag
and Pollard 1991 argue that control is determined by the matrix predicate's meaning; Jackendoff and
Culicover 2003 likewise), but it is wrong for partial and split control (*we decided to meet*),
which a flat formula cannot express without a group referent (I did not look for such cases).

## Annotation errors found

| id | what is wrong |
|---|---|
| debate-transcript/3rd_Bush-Kerry#28 | `*PRO-1` for `*PRO*-1`; verbframes.py reports cause `empty subject *PRO` |
| telephone/sw2015-ms98-a-trans#46 | `*PRO*-4` with no node indexed 4; the *PRO* node itself is NP-SBJ-3 |
| debate-transcript/3rd_Bush-Kerry#653 | *there ought to be*: `*PRO*` where raising `*-n` is expected; index 2 used on NP-2 and PP-PRP-TPC-2 |
| debate-transcript/3rd_Bush-Kerry#60 | *strategy to chase ...*: `*PRO*-1` coindexed with WHADVP-1 *wherever* inside its own clause |
| debate-transcript/2nd_Gore-Bush#266 | index 3 on two nodes (NP-SBJ-3 `*-2` and NP-SBJ-3 *countries in Africa*) |
| debate-transcript/3rd_Bush-Kerry#483 | index 2 on two nodes (NP-SBJ-2 `*-1` and WHNP-2 *what*) |
| journal/Article247_500#19 | raising adjective *likely* with `*PRO*-1` (27 of 32 *likely* complements have *PRO*) |
| debate-transcript/2nd_Gore-Bush#972, face-to-face/Bed012#843, ficlets/1402#39 | S-IMP with subject `*` instead of `*PRO*` (20 in all) |
| movie-script/pirates#1499 | S-IMP on a declarative with an overt subject (*Oh, I think you do*) |
| ficlets/1402#130 | *I needed him to breath*: NP object plus S-PRP controlled by *I*; the reading is surely *needed [him to breathe]* (questionable, not certain) |
| debate-transcript/3rd_Bush-Kerry#298, nyt/NYTnewswire4#23, fiction/hotel-california#474, face-to-face/Bed012#464 | obligatory subject control without an index (one of many; finding 4) |

## Open questions

* Is the uneven coindexation (verbs yes, adjectives, nouns and gerunds mostly no) a deliberate
  guideline of the MASC annotation or an effect of its tools? I do not know the MASC guidelines well
  enough to say.
* How much of the controller could be recovered automatically from parser output: the lexicon of
  Table 2 is small, but it needs lemmas, which the flat semantics does not yet have.
* The 558 verbless *PRO* clauses (SxADJP, SxNP: *keep taxes low*, *call it woman's intuition*), not
  counted here, look like object-controlled small clauses (not checked beyond these two); they bear
  on the matrix verb's frame in the same way. Partial and split control were not looked for.

## References (from memory)

* Dowty, D. (1985). On recent analyses of the semantics of control. *Linguistics and Philosophy* 8.
* Jackendoff, R. and Culicover, P. W. (2003). The semantic basis of control in English. *Language* 79.
* Rosenbaum, P. S. (1967). *The Grammar of English Predicate Complement Constructions.* MIT Press.
* Sag, I. A. and Pollard, C. (1991). An integrated theory of complement control. *Language* 67.
