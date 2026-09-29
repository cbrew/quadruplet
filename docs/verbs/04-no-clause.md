# Verbs with no clause above them

Of MASC's 70,101 lexical verbs, 2,708 (3.9%) head a verb phrase with no S,
SQ, SINV or SBARQ above it. About 72% are post-nominal participial
modifiers (reduced relatives), a construction of written, informational
prose; about 17% are non-sentential units (list items, headings, captions,
citations, stage directions, answers), 3% disfluent reparanda, and an
estimated 8% (6–12%) annotation errors: a missing or misplaced S, or a
participial adjunct attached to the wrong noun. The understood subject is
almost never indexed: a reduced relative's noun is related to the verb
only by position, and a passive participle's empty object is unbound.

## Question

Which constructions are the verbs `verbframes.py` finds with "no clause
above" (1,983 in NP, 234 FRAG, 195 root, 113 UCP, 70 EDITED, ...), how do
they spread over genres, what is their understood subject and is it
annotated, and how many are genuine, errors, or artefacts of speech?

## Data and method

```
S=/tmp/claude-0/-home-user/32748c59-1ad9-5140-855d-e9a1edeaf50a/scratchpad
cd tools/masc/verbs
python3 noclause.py $S/masc/data $S/noclause/nc.jsonl    # classes, genre table
python3 noclause_sample.py $S/noclause/nc.jsonl [--show] # the two samples, judged
python3 noclause.py $S/masc/data --show ID ...           # raw trees by sentence id
```

`noclause.py` walks the raw trees with `verbframes.py`'s own `Node`,
lexical-verb test and upward walk, so it selects the same 2,708 verbs (each
matches a `verbs.jsonl` record by id and position). It records the
*attachment node* (first non-VP ancestor), the VP's sisters and its empty
elements, and classifies by rules in this order: EDITED ancestor; an -SBJ
sister of the VP; a left-sister S holding only an -SBJ; movie-script VP
after a bracketed name, or *CONT'D*; REF; TTL/HLN above; root, by tag; VP
in NP, NML, NX, QP, WHNP, PP, ADJP, NAC or RRC after nominal material
(reduced relative by tag; VBZ/VBP/VB/MD as "finite or base VP inside a
phrase"); VP first in NP/NML; UCP, by whether a clause is reached through
UCP and VP; FRAG, by whether material precedes the VP; SBAR; other.

To estimate what rules cannot see, I read 150 verbs drawn at random (seed
20260928) and judged each from tree and sentence as **G** (genuine
construction, annotated as the guidelines intend), **F** (non-sentential
unit), **D** (disfluency), **E** (annotation error) or **e** (arguable
error); and, since errors among reduced relatives clustered in *NP , V-ing*,
a random 50 of the 111 VBG reduced relatives with a comma before the VP
(seed 7). The judgements are in `noclause_sample.py`; intervals are Wilson
95%. The smaller counts below (root conventions, *you know* brackets,
coindexation, backbone rules) come from one-off loops over the same files,
described where used.

## Counts

**Table 1.** Rule classes, by mode (spoken = face-to-face, telephone,
court and debate transcripts).

| class | spoken | written | all |
|---|---|---|---|
| reduced relative: VBN (+5 tagged VBD, written) | 58 | 1175 | 1233 |
| reduced relative: VBG | 58 | 651 | 709 |
| root: gerund or participle | 4 | 168 | 172 |
| root: bare VP, other (mostly imperatives) | 0 | 21 | 21 |
| FRAG: VP with other material | 12 | 73 | 85 |
| FRAG: bare VP | 15 | 48 | 63 |
| UCP: in fragment or list | 1 | 13 | 14 |
| stage direction (movie scripts) | 0 | 50 | 50 |
| title or headline | 1 | 32 | 33 |
| citation formula (REF) | 0 | 9 | 9 |
| VP as nominal or compound modifier | 9 | 20 | 29 |
| UCP: unlike coordination under a clause | 10 | 62 | 72 |
| disfluency (EDITED) | 67 | 8 | 75 |
| error: subject is a sister of the VP (no S) | 13 | 79 | 92 |
| error: S closed before its VP | 7 | 15 | 22 |
| finite VP inside a phrase (11), SBAR without S (3) | 0 | 14 | 14 |
| other | 1 | 9 | 10 |
| **all** | 256 | 2452 | 2708 |

**Table 2.** Groups of classes by genre, and clauseless verbs per 1000
lexical verbs of the genre. RR reduced relative; UCP under a clause; NOM
compound, title, citation; ROOT; FRAG with stage directions and UCP lists;
EDIT; ERR the error rows of Table 1; OTH.

| genre | verbs | RR | UCP | NOM | ROOT | FRAG | EDIT | ERR | OTH | all | per 1000 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| face-to-face | 3689 | 14 | 5 | 6 | 4 | 22 | 33 | 10 | 1 | 95 | 25.8 |
| telephone | 1169 | 10 | 0 | 0 | 0 | 1 | 4 | 4 | 0 | 19 | 16.3 |
| debate-transcript | 5589 | 55 | 3 | 2 | 0 | 3 | 16 | 4 | 0 | 83 | 14.9 |
| court-transcript | 4704 | 37 | 2 | 2 | 0 | 2 | 14 | 2 | 0 | 59 | 12.5 |
| **spoken total** | 15151 | 116 | 10 | 10 | 4 | 28 | 67 | 20 | 1 | 256 | 16.9 |
| technical | 1790 | 188 | 2 | 0 | 1 | 2 | 0 | 0 | 0 | 193 | 107.8 |
| newspaper/unknown | 46 | 4 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 4 | 87.0 |
| govt-docs | 2836 | 177 | 0 | 2 | 0 | 3 | 0 | 10 | 0 | 192 | 67.7 |
| travel-guides | 2513 | 152 | 2 | 3 | 3 | 3 | 0 | 3 | 1 | 167 | 66.5 |
| journal | 2708 | 142 | 5 | 3 | 1 | 0 | 0 | 5 | 2 | 158 | 58.3 |
| non-fiction | 2742 | 132 | 3 | 5 | 5 | 2 | 0 | 0 | 1 | 148 | 54.0 |
| essays | 3653 | 180 | 2 | 6 | 0 | 0 | 0 | 5 | 1 | 194 | 53.1 |
| enron | 1603 | 54 | 3 | 2 | 6 | 9 | 0 | 8 | 0 | 82 | 51.2 |
| wsj | 856 | 41 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 41 | 47.9 |
| ficlets | 4739 | 76 | 4 | 9 | 105 | 19 | 0 | 9 | 1 | 223 | 47.1 |
| nyt | 2442 | 95 | 6 | 1 | 0 | 2 | 1 | 5 | 0 | 110 | 45.0 |
| w3c | 1291 | 48 | 0 | 0 | 1 | 1 | 0 | 5 | 0 | 55 | 42.6 |
| movie-script | 3715 | 57 | 2 | 0 | 5 | 64 | 6 | 10 | 0 | 144 | 38.8 |
| twitter | 3535 | 53 | 14 | 8 | 3 | 38 | 0 | 18 | 0 | 134 | 37.9 |
| blog | 4355 | 115 | 6 | 6 | 8 | 6 | 1 | 6 | 1 | 149 | 34.2 |
| spam | 3000 | 68 | 6 | 5 | 3 | 9 | 0 | 4 | 0 | 95 | 31.7 |
| solicitation-brochures | 1617 | 31 | 2 | 3 | 9 | 3 | 0 | 1 | 1 | 50 | 30.9 |
| fiction | 5518 | 128 | 5 | 1 | 11 | 4 | 0 | 8 | 0 | 157 | 28.5 |
| jokes | 4550 | 53 | 0 | 7 | 28 | 19 | 0 | 11 | 1 | 119 | 26.2 |
| philanthropic-fundraising | 1441 | 37 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 37 | 25.7 |
| **written total** | 54950 | 1831 | 62 | 61 | 189 | 184 | 8 | 108 | 9 | 2452 | 44.6 |
| **all** | 70101 | 1947 | 72 | 71 | 193 | 212 | 75 | 128 | 10 | 2708 | 38.6 |

**Table 3.** The understood subject, and whether the annotation records it.

| construction | n | understood subject (or object) | recorded? |
|---|---|---|---|
| reduced relative, VBN | 1233 | the modified noun, as logical object (as subject for unaccusatives: *stars gone supernova*) | no index: 1097 have an unindexed `(NP (-NONE- *))` object; 70 an indexed one that only controls a small-clause `*PRO*` below (*called X*); 33 a `*` in a PP or S; 9 other empty elements only; 24 none |
| reduced relative, VBG | 709 | the modified noun, as subject | no: nothing in the VP stands for it |
| UCP under a clause | 72 | the clause's subject | 23 VBN have `*-n` bound to the NP-SBJ; the rest only by structure |
| root VP | 193 | generic, the writer (*Singing in the shower.*), the poster (*Posted by*), the addressee (imperatives: most of the 21 non-participles) | no |
| FRAG, UCP lists | 162 | from context: speaker, writer, the person answered | no |
| stage direction | 50 | the character named just before | only by adjacency |
| title, headline, compound, citation | 71 | none, generic, or the author | no |
| EDITED | 75 | the subject of the repair | only by structure |
| error classes | 128 | an NP-SBJ that is there, but outside any S | yes, but invisible to `verbframes.py` |

**Table 4.** How the clauseless verbs divide. Left: the random sample.
Right: an estimate from the rule classes (all 2,708 classified) with the
within-class rates the samples give.

| | sample (n=150) | 95% interval | estimate from classes |
|---|---|---|---|
| G genuine construction | 108 (72.0%) | 64.3–78.6 | ≈1,970 (73%) |
| F non-sentential unit | 24 (16.0%) | 11.0–22.7 | ≈450 (17%) |
| D disfluency | 5 (3.3%) | 1.4–7.6 | 75 (2.8%) |
| E error (+ e arguable) | 11+2 (8.7%) | 5.1–14.3 | ≈210 (7.8%); ≈250 with arguable |

The rule classes matched the judgements for every item outside the
reduced relatives (6 "error" items all E, 24 root/FRAG/title/citation/stage
items all F, 5 EDITED all D); inside them 5 of 109 were errors (2.0–10.3%)
and 2 arguable. Class-based errors: 117 rule-detected (25 of the 92
checked, all errors: rate 87–100%) + about 5 of the 11 finite VPs in NPs +
1,947 × 5/109 ≈ 89 misattached participles (39–200) ≈ 210. In the comma
sample, 15 of 50 were misattached (30%, 19–44%), 4 more arguable.

## Examples

Trees are trimmed to the attachment node; *full* and *bb* are `frame_full`
and `frame_backbone` from `verbs.jsonl`.

1. `debate-transcript/2nd_Gore-Bush#639` *guns sold* — `(NP (NP (NNS guns)) (VP (VBN sold) (NP (-NONE- *))))`; full `NP(*)`, bb empty. Passive reduced relative in speech; the object gap is there but bound to nothing.
2. `debate-transcript/2nd_Gore-Bush#1070` *too many issues left unresolved* — `(NP (NP ... (NNS issues)) (VP (VBN left) (NP-1 (-NONE- *)) (S (NP-SBJ (-NONE- *PRO*-1)) (ADJP-PRD (JJ unresolved)))))`; full `NP(*) S`, bb `S`. The index chain runs from the small-clause subject to the gap and stops.
3. `debate-transcript/3rd_Bush-Kerry#613` *People listening out there know ...* — `(NP-SBJ (NP (NNS People)) (VP (VBG listening) (ADVP-LOC (RP out) (RB there))))`; full and bb empty. Active reduced relative: nothing represents the subject.
4. `debate-transcript/2nd_Gore-Bush#1083` *promises kept* — `(NP-PRD (NP (NNS promises)) (VP (VBN kept)))`; full and bb empty. Passive with its `(NP *)` missing, unlike 1,097 others.
5. `ficlets/1403#545` *Emilie plopped down on the couch, pulling a down pillow onto her lap* — `(NP (NP (DT the) (NN couch)) (VP (, ,) (VBG pulling) (NP ...)))`; full and bb `NP`. A free adjunct whose subject is *Emilie*, attached as if the couch pulled the pillow; the usual analysis is `S-ADV (NP-SBJ *PRO*)`.
6. `non-fiction/rybczynski-ch3#24` *The library board had conducted a national search for an architect, visited new libraries ..., and solicited proposals* — `(NP (NP an architect) (VP (VP (, ,) (VBN visited) (NP ...)) (, ,) (CC and) (VP (VBN solicited) ...)))`; full and bb `NP`. Participle VPs coordinated with *conducted* under *had* (subject *the library board*), attached as a reduced relative on *an architect*.
7. `blog/detroit#33` *Houses and businesses are boarded up, painted up, bombed out and falling down* — `(VP (VBP are) (UCP (VP (VBN boarded) (NP (-NONE- *-1)) (PRT up)) , (VP (VBN painted) (NP (-NONE- *-1)) (PRT up)) , (ADJP-PRD bombed out) (CC and) (VP (VBG falling) (PRT down))))`; *painted* full `NP(*) PRT`, bb `PRT`; *falling* `PRT`. Genuine unlike coordination; the passives' `*-1` is bound to NP-SBJ-1, but `verbframes.py` stops at UCP.
8. `ficlets/1402#720` *Building a snow-man.* — `(VP (VBG Building) (NP (DT a) (NN snow-man)) (. .))`; full and bb `NP`. A list item as a bare VP at the root; elsewhere 311 such sentences are `S` with an empty subject.
9. `movie-script/JurassicParkIV-Scene_3#18` *IAN MALCOLM (smirking) Well, I don't ...* — `(REF (FRAG (NP (NNP IAN) (NNP MALCOLM)) (CODE -LRB-) (VP (VBG smirking))))`; empty / empty. Stage direction; its subject is the sister NP.
10. `solicitation-brochures/defenders5#36` *get-out-the-vote efforts* — `(NML (VP (VB get) (HYPH -) (PRT (RP out)) (HYPH -) (NP (DT the) (HYPH -) (NN vote))))`; `PRT NP` / `PRT NP`. A lexicalized VP as a prenominal compound modifier; no subject is understood.
11. `face-to-face/Bed012#812` *you can probably count - count the ways* — `(EDITED (VP (VB count)))`; empty / empty. Reparandum; the repair *count the ways* has the subject.
12. `face-to-face/Bed012#766` *And not meet tomorrow?* — `(FRAG (CC And) (RB not) (VP (VB meet) (NP-TMP (NN tomorrow))) (. ?))`; empty / empty. A spoken fragment; the subject is understood from the conversation.
13. `jokes/jokes3#36` *Good Advice: The Japanese eat very little fat and suffer fewer heart attacks ...* — `(FRAG (NP Good Advice) (: :) (NP-SBJ (DT The) (NNP Japanese)) (VP (VP (VBP eat) ...) (CC and) (VP (VBP suffer) ...)))`; *eat* full `NP`, bb `NP`. The subject is annotated but the S is missing, so the frame has no SBJ.
14. `telephone/sw2071-UTF16-ms98-a-trans#34` *you know* — `(PRN (S (NP-SBJ (PRP you))) (VP (VBP know)))`; empty / empty. The S closes before its VP (91 other *you know* parentheticals are bracketed correctly); the backbone learns `PRN -> SxNP VP` (7 times in MASC).
15. `jokes/jokes1#82` *"You're in incredible shape," the doctor said.* — `(S-TPC-1 (NP-SBJ (PRP You) (VP (VBP 're) (PP-PRD ...))))`; `PP-PRD` / `PP-PRD`. A finite clause bracketed as a noun phrase.

## Findings

1. **Most clauseless verbs are reduced relatives:** 1,947 of 2,708 (72%),
   1,233 VBN and 709 VBG; the sample confirms 102 of 109 as genuine.
2. **They belong to written registers:** 33.3 per 1000 lexical verbs in
   written genres, 7.7 in spoken; technical 105, govt-docs 62, travel
   guides 60, the transcripts 8–10, face-to-face 4 (Table 2). This agrees
   with Biber et al. (1999), from memory, on post-nominal participle
   clauses being common in academic prose and news, rare in conversation.
3. **The understood subject is not recorded.** In 1,946 of the 1,947 the
   modified noun has no index (the exception, `w3c/lists-046-12122969#36`,
   is an NP-TPC-3). A VBG has nothing standing for its subject; a VBN has an
   unbound `(NP (-NONE- *))` object in 1,097 cases, as the guidelines
   prescribe for reduced relatives (Bies et al. 1995, from memory). The
   relation is purely configurational, NP → NP VP.
4. **Some passive participles lack their empty object.** 24 VBN reduced
   relatives have no empty element. Some need none (*gone wrong*, *fallen*,
   *headed to China*, *perched*, *15 seconds left*); others are passives
   with the trace missing (Example 4, error table).
5. **Participial adjuncts are often attached to the wrong noun.** 15 of 50
   *NP , V-ing* reduced relatives (30%, 19–44%) are free adjuncts whose
   subject is the clause's subject or the event (Example 5):
   *Somebody was sitting in the room, beating his hands against a book*
   (`ficlets/1402#520`) attaches *beating* to *the room*. The analysis
   `S-ADV` with `*PRO*` is used 1,154 times for VBG elsewhere, so this is
   inconsistency, not convention. In the random sample 4 of 6 comma-VBG
   cases were such errors, against 1 of the other 103 reduced relatives
   (`twitter/tweets1#87` *At work conducting meetings*), 2 arguable.
   Coordinated gerunds (`govt-docs/Env_Prot_Agency-nov1#138`) and
   coordinated VPs (Example 6) are misattached the same way.
6. **A missing or misplaced S accounts for 117–122 verbs.** In 92 an -SBJ
   phrase is a sister of the VP under FRAG (32), SBAR (26), UCP (23) or
   another phrase: *Label: Subject VP* in headings and tweets (Example 13),
   `(SBAR (IN as) (NP-SBJ (-NONE- *PRO*)) (VP ...))` for *as planned*
   (`jokes/jokes10#135`). In 22 an S holds only its subject (Example 14);
   about 5 put a finite VP inside an NP or ADJP (Example 15). These are
   sporadic slips: 2 of 93 *you know* and 1 of 16 *I mean* parentheticals
   have the misplaced bracket. 79 of the 92 are written, most in twitter,
   govt-docs, movie scripts and jokes.
7. **Bare VPs at the root mix two conventions.** 193 verbs (171 backbone
   trees `Top -> VP`). Stand-alone gerunds are bare VPs in 141 trees and
   `S` with an empty subject in 311; 82 of the bare ones are in two ficlets
   list files (1402, 1403), which use it almost uniformly (82 to 1).
   Imperatives: 19 bare VPs against 663 `S-IMP`. This looks like a
   per-file or per-annotator habit, not a principled distinction.
8. **Unlike coordination is genuine and its subject is available.** 72
   verbs are in a UCP predicate under a clause (48 under a VP, mostly a
   copula; 23 under S): *fresh, made from scratch, sassy and scrumptious*
   (`blog/Tupelo-Honey-Cafe#9`), Example 7. The subject is the clause's
   NP-SBJ, to which 23 VBN even bind an object `*-n`.
9. **Non-sentential text is genre-specific:** stage directions (50, movie
   scripts), list items at the root (ficlets 105, jokes 28), twitter
   fragments (38), headers (*Posted by*, *Sent:* in blog, enron), titles
   (33), citation formulas (9: *modified after X*, *qtd. in X*).
10. **Speech contributes little, mostly disfluency.** Spoken genres have
    256 clauseless verbs (16.9 per 1000, against 44.6 in writing): 116
    reduced relatives, 67 EDITED reparanda, 32 fragments or root VPs, 20
    errors. Spoken FRAG verbs are rare (28 in 15,151; 22 face-to-face).
    The 8 written EDITED verbs are in movie scripts (6), a blog and nyt.
11. **The frame changes here are passive reduced relatives.** 1,262 of the
    2,708 have `frame_full` ≠ `frame_backbone`; 1,170 are VBN reduced
    relatives losing `NP(*)` (`NP(*) PP-CLR` → `PP-CLR`: 210), 4.3% of the
    26,947 changed frames in MASC. VBG frames agree (708 of 709) but both
    lack the subject.
12. **Overall** (Table 4): about 73% genuine constructions, 17%
    non-sentential units, 3% disfluencies, 8% (6–12%) annotation errors.
    Spoken-language artefacts proper (75 EDITED, 32 spoken fragments and
    root VPs) are about 107 verbs, 4%; within the spoken genres, 99 of 256.

## What it means

**(a) Backbone and fast parser.** Reduced relatives give `NP -> NP VP`
(1,427 uses), `NP -> NP Comma VP` (193) and 120 rarer NP rules with a VP
(1,916 uses in all). Every VP after a noun phrase can attach low or high,
and the gold trees themselves attach about 30% of *NP , V-ing* wrongly, so
a parser trained on them learns the error. Root VPs add `Top -> VP` (171);
EDITED over a VP becomes `EDITEDxVP` (55) beside `EDITEDxS` (154). Error
trees add rules found nowhere else (`PRN -> SxNP VP`, 7; FRAG and SBAR with
a bare subject before a VP); inserting the missing S in the 117 detected
trees before reading off the grammar would be cheap.

**(b) Verb frames and complement/modifier.** A reduced relative is as a
whole a modifier of its noun, like a relative clause, yet the noun fills an
argument slot of the verb: subject of a VBG, logical object of a passive
VBN. Neither frame records it, and the backbone frame of a passive
participle (`PP-CLR` for *associated with X*) looks intransitive. Adding
the modified noun as a configurational argument (SBJ for VBG, object for
VBN except unaccusatives) is better than counting 1,947 verbs as
subjectless; the misattached adjuncts will then contribute wrong arguments
(the couch pulls a pillow) at the rates of finding 5. Root VPs, fragments,
titles and stage directions lack subjects because of text type, not
valency, and belong apart from the frame counts, as imperatives do. The
128 error-class verbs have annotated subjects that a frame extractor could
read from the VP's -SBJ sister.

**(c) Flat neo-Davidsonian semantics.** In `go/interp/semantics.go` a VP
daughter of an NP with no function tag gets `mod`, so *guns sold* yields
`guns(x) ∧ sold(e) ∧ mod(x, e)`, with no `sbj` or `obj`. Keeping the gold
empty elements, as `docs/flat-semantics.md` proposes for traces, would not
help: the reduced relative's `*` is unbound and a VBG has none. A
construction rule on `NP -> NP VP` would: the head noun is `sbj` of a VBG's
event and `obj` of a passive VBN's. For root VPs and fragments an event
without a subject is the right reading; any agent comes from discourse.

## Annotation errors found

All ids can be listed with `grep '"class": "error' nc.jsonl`; this table
gives the ones inspected by hand.

| id | what is wrong |
|---|---|
| `face-to-face/Bmr021#302`, `telephone/sw2015-ms98-a-trans#95`, `telephone/sw2071-UTF16-ms98-a-trans#34`, `telephone/sw2078-UTF16-ms98-a-trans#377`, `ficlets/1401#317`, `jokes/jokes1#97`, `nyt/20000415_apw_eng-NEW#8` | parenthetical *you know / I mean / I guess / you see / Hall said*: S closed after the subject, VP outside it |
| `debate-transcript/3rd_Bush-Kerry#500`, `face-to-face/Bmr021#335`, `face-to-face/Bed012#676`, `govt-docs/chapter-10#19`, `nyt/20020731-nyt#333`, `nyt/NYTnewswire2#25` | S in an SBAR closed after the subject, VP outside it |
| `fiction/hotel-california#80`, `jokes/jokes1#159`, `nyt/NYTnewswire7#3`, `twitter/tweets2#143`, `twitter/tweets2#669`, `twitter/tweets2#697`, `twitter/tweets1#685`, `blog/blog-jet-lag#53`, `journal/VOL15_3#528` | *Label: S* heading or parenthesis with the S closed after its subject |
| `jokes/jokes3#36`, `fiction/hotel-california#148`, `fiction/hotel-california#328`, `spam/ucb12#2`, `govt-docs/LSC-Protocol_Regarding_Access#43`, `govt-docs/Postal_Rate_Comm-ReportToCongress2002WEB#54`, `movie-script/pirates#1558` | NP-SBJ and VP directly under FRAG or UCP: S missing |
| `fiction/hotel-california#127`, `essays/Ant_Robot#279`, `jokes/jokes10#135`, `jokes/jokes10#158`, `govt-docs/chapter-10#271`, `twitter/tweets1#833`, `essays/A_defense_of_Michael_Moore#122`, `essays/Black_and_white#101` | NP-SBJ (often `*PRO*`) and VP directly under SBAR: S missing |
| `jokes/jokes1#82`, `ficlets/1403#415`, `twitter/tweets2#510`, `w3c/lists-003-2137010#6` | a finite clause bracketed as an NP or NML containing a VP |
| `journal/VOL15_3#317` | `(ADJP-PRD (JJ likely) (VP (TO to) ...))`: infinitival S missing |
| `essays/Ohio_Steel#36`, `ficlets/1403#545`, `ficlets/1402#520`, `ficlets/1401#495`, `fiction/Nathans_Bylichka#83`, `blog/detroit#22`, `blog/detroit#23`, `essays/anth_essay_4#89`, `essays/Ant_Robot#116`, `non-fiction/CUP1#199`, `non-fiction/CUP2#164`, `twitter/tweets1#87`, `twitter/tweets1#390`, `twitter/tweets1#653` | participial free adjunct attached as a reduced relative to the preceding NP; should be `S-ADV` with `*PRO*` in the VP or clause |
| `govt-docs/Env_Prot_Agency-nov1#138` | coordinated gerunds attached as a reduced relative to the first conjunct |
| `non-fiction/rybczynski-ch3#24` | VPs coordinated with *conducted* attached as a reduced relative to *an architect* |
| `debate-transcript/2nd_Gore-Bush#1083`, `spam/Re_JobOffer#19`, `spam/111369#4`, `movie-script/pirates#43`, `wsj/wsj_0184#14`, `technical/1468-6708-3-1#161`, `twitter/tweets2#163` | passive participle in a reduced relative without its `(NP (-NONE- *))` |
| `wsj/wsj_0127#0`, `wsj/wsj_0136#0`, `wsj/wsj_0189#3`, `wsj/wsj_0176#4`, `w3c/lists-046-12122969#4` | participle tagged VBD |
| `ficlets/1403#153` | `(VP (VBG watching) (NP-SBJ them) (VP melt))`: small clause without S |

**On `verbframes.py`.** (i) For the 114 verbs whose subject is a sister
of the VP or sits in an S closed too early, it reports "no clause above"
and no SBJ although a subject is annotated (`jokes/jokes3#36`: `NP`).
(ii) Its walk stops at UCP, so 72 verbs in a UCP predicate lose the
clause's subject (`blog/detroit#33`). (iii) `is_aux_vp` takes any VP with a
VP daughter for an auxiliary, so in `ficlets/1403#153` *watching* is
skipped (only 8 VPs in MASC have an -SBJ daughter). (iv) "no clause above
(EDITED)" (70) misses 5 verbs inside EDITED further up.

## Open questions

- Why are participial adjuncts attached low so often? If MASC's trees were
  made by correcting an automatic parse (not checked), the attachment may
  be the parser's, left uncorrected; speculative.
- Which of the 24 VBN reduced relatives without an empty element are
  unaccusative or adjectival, and which lack a trace? About 8 look clearly
  passive. The spoken genres gave only 16 sample items, so no error rate
  for speech alone is estimated.
- Should *called X*, *named X*, *known as X* (70 with an indexed object
  controlling a small clause) be complement frames of their own?
- Is the bare-VP root convention confined to some files or annotators? A
  file-by-file count would settle it; normalising to `S` with an empty
  subject would unify these frames with the 311 sentences so annotated.
