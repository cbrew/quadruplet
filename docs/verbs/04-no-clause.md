# Verbs with no clause above them

Of MASC's 70,101 lexical verbs, 2,708 (3.9%) head a verb phrase with no S,
SQ, SINV or SBARQ above it. About 72% of them are post-nominal participial
modifiers ("reduced relatives"), a genuine construction of written,
informational prose; about 17% are non-sentential units (list items,
headings, captions, citation formulas, stage directions, answers), 3% are
disfluent reparanda under EDITED, and an estimated 8% (roughly 6–12%) are
annotation errors: an S node missing or closed too early, or a participial
adjunct attached to the wrong noun phrase. The understood subject of these
verbs is almost never recorded by an index: in a reduced relative the
modified noun is related to the verb only by its position, and the empty
object of a passive participle is left unbound.

## Question

What are the verbs that `verbframes.py` reports as having "no clause above"
(1,983 in an NP, 234 FRAG, 195 root, 113 UCP, 70 EDITED, and smaller
groups), how are they spread over MASC's spoken and written genres, what is
the understood subject of each construction and does the annotation record
it, and how many are genuine constructions, how many annotation errors, and
how many artefacts of spoken language?

## Data and method

```
S=/tmp/claude-0/-home-user/32748c59-1ad9-5140-855d-e9a1edeaf50a/scratchpad
cd tools/masc/verbs
python3 noclause.py $S/masc/data $S/noclause/nc.jsonl    # classes, genre table
python3 noclause_sample.py $S/noclause/nc.jsonl [--show] # the two samples, judged
python3 noclause.py $S/masc/data --show ID ...           # raw trees by sentence id
```

`noclause.py` walks the raw trees with `verbframes.py`'s own `Node`,
lexical-verb test and upward walk through verb phrases, so it selects
exactly the same 2,708 verbs (every one matches a record in `verbs.jsonl`
by id and word position). For each it records the *attachment node* (the
first non-VP ancestor), the sisters of the topmost VP, the empty elements
in the VP, and assigns a class by rules applied in this order: (1) an
EDITED ancestor; (2) an -SBJ sister of the VP (a subject with no S);
(3) a left sister that is an S containing only an -SBJ (an S closed before
its VP); (4) a movie-script VP after a bracketed character name, or
*CONT'D*; (5) REF; (6) a TTL or HLN tag above; (7) no attachment node
(root), by tag; (8) a VP in NP, NML, NX, QP, WHNP, PP, ADJP, NAC or RRC
after nominal material: reduced relative, by tag (VBZ/VBP/VB/MD: "finite
or base VP inside a phrase"); (9) a VP first in NP/NML; (10) UCP, split by
whether a clause is reached going up through UCP and VP; (11) FRAG, split
by whether other material precedes the VP; (12) SBAR; (13) other.

To estimate what the rules cannot see, I read 150 verbs drawn at random
from the 2,708 (seed 20260928) and judged each from its tree and sentence
as **G** (a genuine construction annotated as the guidelines intend),
**F** (a non-sentential unit), **D** (a disfluency), **E** (annotation
error) or **e** (arguable error). Because the first sample showed that
errors among reduced relatives concentrate in *NP , V-ing* sequences, I
also judged a random 50 of the 111 VBG reduced relatives with a comma
before the VP (seed 7). The judgements are in `noclause_sample.py`.
Intervals are Wilson 95% intervals. Smaller counts quoted below (root
conventions, `you know` brackets, coindexation, backbone rules) come from
short one-off Python loops over the same files; each is described where
it is used.

## Counts

**Table 1.** Rule classes, by mode (spoken = face-to-face, telephone,
court and debate transcripts).

| class | spoken | written | all |
|---|---|---|---|
| reduced relative: VBN | 58 | 1175 | 1233 |
| reduced relative: VBG | 58 | 651 | 709 |
| reduced relative: VBD (tag error) | 0 | 5 | 5 |
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
| finite or base VP inside a phrase | 0 | 11 | 11 |
| SBAR without S | 0 | 3 | 3 |
| other | 1 | 9 | 10 |
| **all** | 256 | 2452 | 2708 |

**Table 2.** Groups of classes by genre, and clauseless verbs per 1000
lexical verbs of the genre. RR reduced relative; UCP under a clause; NOM
compound, title, citation; ROOT; FRAG with stage directions and UCP lists;
EDIT; ERR the four error-like classes of Table 1; OTH.

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
| reduced relative, VBN | 1233 | the modified noun, as logical object (as subject for unaccusatives: *stars gone supernova*) | no index: 1097 have an unindexed `(NP (-NONE- *))` object; 70 an indexed one that only controls a small-clause `*PRO*` below (*called X*); 33 a `*` in a PP or S; 36 no `*` at all |
| reduced relative, VBG | 709 | the modified noun, as subject | no: nothing in the VP stands for it |
| UCP under a clause | 72 | the clause's subject | 23 VBN have `*-n` bound to the NP-SBJ; the rest only by structure |
| root gerund or participle | 172 | generic, or the writer (*Singing in the shower.*), or the poster (*Posted by ...*) | no |
| root bare VP, other | 21 | mostly the addressee (imperatives) | no |
| FRAG, UCP lists | 162 | from context: speaker, writer, the person answered | no |
| stage direction | 50 | the character named just before | only by adjacency |
| title, headline, compound, citation | 71 | none, generic, or the author | no |
| EDITED | 75 | the subject of the repair | only by structure |
| error classes | 128 | an NP-SBJ that is there, but outside any S | yes, but invisible to `verbframes.py` |

Of the 1,947 reduced relatives, the modified noun phrase carries an index
in exactly one case (`w3c/lists-046-12122969#36`, an NP-TPC-3 whose index
happens to be used inside the VP); in the other 1,946 no index relates the
noun to the verb.

**Table 4.** How the clauseless verbs divide. Left: the random sample.
Right: an estimate from the rule classes (all 2,708 classified) with the
within-class rates the samples give.

| | sample (n=150) | 95% interval | estimate from classes |
|---|---|---|---|
| G genuine construction | 108 (72.0%) | 64.3–78.6 | ≈1,970 (73%) |
| F non-sentential unit | 24 (16.0%) | 11.0–22.7 | ≈450 (17%) |
| D disfluency | 5 (3.3%) | 1.4–7.6 | 75 (2.8%) |
| E error (+ e arguable) | 11+2 (8.7%) | 5.1–14.3 | ≈210 (7.8%); ≈250 with arguable |

The rule classes agreed with the judgements in every non-RR item (all 6
"error" items were errors, all 24 root/FRAG/title/citation/stage items
were F, all 5 EDITED were D). Inside the reduced relatives 5 of 109 were
errors (2.0–10.3%) and 2 more arguable. The class-based estimate is:
errors = 117 rule-detected (92 + 22 + 3, 25 of the 92 checked, all errors,
interval for the rate 87–100%) + about 5 of the 11 finite VPs in NPs +
1,947 × 5/109 ≈ 89 misattached participles (interval 39–200) ≈ 210. In the
comma sample, 15 of 50 VBG reduced relatives were misattached (30%,
19–44%) and 4 more arguable, so about 33 of the 111 comma cases alone.

## Examples

Trees are trimmed to the attachment node; *full* and *bb* are `frame_full`
and `frame_backbone` from `verbs.jsonl`.

1. `debate-transcript/2nd_Gore-Bush#639` *guns sold* — `(NP (NP (NNS guns)) (VP (VBN sold) (NP (-NONE- *))))`; full `NP(*)`, bb empty. Passive reduced relative in speech; the object gap is there but bound to nothing.
2. `debate-transcript/2nd_Gore-Bush#1070` *too many issues left unresolved* — `(NP (NP ... (NNS issues)) (VP (VBN left) (NP-1 (-NONE- *)) (S (NP-SBJ (-NONE- *PRO*-1)) (ADJP-PRD (JJ unresolved)))))`; full `NP(*) S`, bb `S`. The index chain runs from the small-clause subject to the gap and stops.
3. `debate-transcript/3rd_Bush-Kerry#613` *People listening out there know ...* — `(NP-SBJ (NP (NNS People)) (VP (VBG listening) (ADVP-LOC (RP out) (RB there))))`; full and bb empty. Active reduced relative: nothing represents the subject.
4. `debate-transcript/2nd_Gore-Bush#1083` *promises kept* — `(NP-PRD (NP (NNS promises)) (VP (VBN kept)))`; full and bb empty. Passive with its `(NP *)` missing, unlike 1,097 others.
5. `wsj/wsj_0127#0` *the six months ended Oct. 1* — `(NP (NP (DT the) (CD six) (NNS months)) (VP (VBD ended) (NP-TMP (NNP Oct.) (CD 1))))`; full and bb empty. Reduced relative with a VBD tag for a participle (also `wsj_0136#0`, `wsj_0189#3`, `wsj_0176#4`).
6. `ficlets/1403#545` *Emilie plopped down on the couch, pulling a down pillow onto her lap* — `(NP (NP (DT the) (NN couch)) (VP (, ,) (VBG pulling) (NP ...)))`; full and bb `NP`. A free adjunct whose subject is *Emilie*, attached as if the couch pulled the pillow; the usual analysis is `S-ADV (NP-SBJ *PRO*)`.
7. `non-fiction/rybczynski-ch3#24` *The library board had conducted a national search for an architect, visited new libraries ..., and solicited proposals* — `(NP (NP an architect) (VP (VP (, ,) (VBN visited) (NP ...)) (, ,) (CC and) (VP (VBN solicited) ...)))`; full and bb `NP`. Coordinated finite VPs of *the library board* attached as a participle modifying *an architect*.
8. `blog/detroit#33` *Houses and businesses are boarded up, painted up, bombed out and falling down* — `(VP (VBP are) (UCP (VP (VBN boarded) (NP (-NONE- *-1)) (PRT up)) , (VP (VBN painted) (NP (-NONE- *-1)) (PRT up)) , (ADJP-PRD bombed out) (CC and) (VP (VBG falling) (PRT down))))`; *painted* full `NP(*) PRT`, bb `PRT`; *falling* `PRT`. Genuine unlike coordination; the passives' `*-1` is bound to NP-SBJ-1, but `verbframes.py` stops at UCP.
9. `ficlets/1402#720` *Building a snow-man.* — `(VP (VBG Building) (NP (DT a) (NN snow-man)) (. .))`; full and bb `NP`. A list item ("things I love") as a bare VP at the root; elsewhere 311 such sentences are `S` with an empty subject.
10. `solicitation-brochures/appalachian1#36` *Build an igloo?* — `(VP (VB Build) (NP (DT an) (NN igloo)) (. ?))`; `NP` / `NP`. Imperative as a bare VP, against 663 imperatives annotated `S-IMP` with `*PRO*`.
11. `movie-script/JurassicParkIV-Scene_3#18` *IAN MALCOLM (smirking) Well, I don't ...* — `(REF (FRAG (NP (NNP IAN) (NNP MALCOLM)) (CODE -LRB-) (VP (VBG smirking))))`; empty / empty. Stage direction; its subject is the sister NP.
12. `non-fiction/CUP1#219` *(modified after Stevens, 1974)* — `(REF (VP (VBN modified) (NP (-NONE- *)) (PP (IN after) (NP (NNP Stevens) , (NN 1974)))))`; `NP(*)` / empty. Citation formula; the object is the figure, the agent the author.
13. `journal/VOL15_3#12` *Paradise Lost* — `(NP-TTL (NP (NN Paradise)) (VP (VBN Lost) (NP (-NONE- *))))`; `NP(*)` / empty. A reduced relative inside a title.
14. `solicitation-brochures/defenders5#36` *get-out-the-vote efforts* — `(NML (VP (VB get) (HYPH -) (PRT (RP out)) (HYPH -) (NP (DT the) (HYPH -) (NN vote))))`; `PRT NP` / `PRT NP`. A lexicalized VP as a prenominal compound modifier; no subject is understood.
15. `face-to-face/Bed012#812` *you can probably count - count the ways* — `(EDITED (VP (VB count)))`; empty / empty. Reparandum; the repair *count the ways* has the subject.
16. `face-to-face/Bed012#766` *And not meet tomorrow?* — `(FRAG (CC And) (RB not) (VP (VB meet) (NP-TMP (NN tomorrow))) (. ?))`; empty / empty. A spoken fragment; the subject (*we*) is understood from the conversation.
17. `jokes/jokes3#36` *Good Advice: The Japanese eat very little fat and suffer fewer heart attacks ...* — `(FRAG (NP Good Advice) (: :) (NP-SBJ (DT The) (NNP Japanese)) (VP (VP (VBP eat) ...) (CC and) (VP (VBP suffer) ...)))`; *eat* full `NP`, bb `NP`. The subject is annotated but the S is missing, so the frame has no SBJ.
18. `telephone/sw2071-UTF16-ms98-a-trans#34` *you know* — `(PRN (S (NP-SBJ (PRP you))) (VP (VBP know)))`; empty / empty. The S closes before its VP (91 other *you know* parentheticals are bracketed correctly); the backbone learns `PRN -> SxNP VP` (7 times in MASC).
19. `jokes/jokes1#82` *"You're in incredible shape," the doctor said.* — `(S-TPC-1 (NP-SBJ (PRP You) (VP (VBP 're) (PP-PRD ...))))`; `PP-PRD` / `PP-PRD`. A finite clause bracketed as a noun phrase.

## Findings

1. **Most clauseless verbs are reduced relatives.** 1,947 of 2,708 (72%)
   are participial VPs after a noun (Table 1); 1,233 VBN and 709 VBG. The
   random sample confirms 102 of 109 of them as genuine.
2. **They are a written-register construction.** Reduced relatives occur
   at 33.3 per 1000 lexical verbs in written genres and 7.7 in spoken ones;
   technical articles reach 105, government documents 62, travel guides
   60, while telephone, court and debate transcripts have 6–10 (Table 2).
   This agrees with what I recall (from memory) of Biber et al. (1999) on
   post-nominal participle clauses being frequent in academic prose and
   news and rare in conversation.
3. **The understood subject is not recorded.** In 1,946 of 1,947 reduced
   relatives the modified noun has no index. For VBG nothing in the VP
   stands for the subject; for VBN the passive object `(NP (-NONE- *))` is
   present in 1,097 cases but unbound, as the Penn Treebank guidelines
   prescribe for reduced relatives (Bies et al. 1995, from memory). The
   relation between noun and verb is purely configurational: NP → NP VP.
4. **Some passive participles lack their empty object.** 36 VBN reduced
   relatives have no `*` anywhere in the VP. Some are unaccusative or
   adjectival and need none (*gone wrong*, *fallen*, *headed to China*,
   *perched*, *crouched*, *15 seconds left*); others are passives with the
   trace missing (Example 4; see the error table).
5. **Participial adjuncts are often attached to the wrong noun.** Of 50
   VBG reduced relatives of the form *NP , V-ing ...*, 15 (30%, 19–44%)
   are free adjuncts whose subject is the clause's subject or the whole
   event (Examples 6, 7): *Somebody was sitting in the room, beating his
   hands against a book* (`ficlets/1402#520`) attaches *beating* to *the
   room*. The correct analysis, `S-ADV` with an empty `*PRO*` subject, is
   used 1,154 times for VBG elsewhere in MASC, so these are inconsistencies,
   not a convention. Without the comma the rate is much lower (1 clear case
   in about 90 in the random sample: `twitter/tweets1#87` *At work
   conducting meetings*). Coordinated gerunds (*goals of increasing energy
   supplies, accelerating ..., and increasing ...*,
   `govt-docs/Env_Prot_Agency-nov1#138`) and a coordinated finite VP
   (Example 7) are misattached the same way.
6. **A missing or misplaced S accounts for 117–122 verbs.** In 92 an
   -SBJ phrase is a sister of the VP under FRAG (32), SBAR (26), UCP (23)
   or another phrase: typically *Label: Subject VP* in headings and tweets
   (Example 17), `(SBAR (IN as) (NP-SBJ (-NONE- *PRO*)) (VP ...))` for
   *as planned*, *based on*. In 22 an S contains only its subject and the
   VP follows it (Example 18). Five more put a finite VP inside an NP
   (Example 19). These are sporadic slips, not a convention: of 93 *you
   know* parentheticals, 2 have the misplaced bracket; of 16 *I mean*, 1.
   79 of the 92 are in written genres, most in twitter, govt-docs, movie
   scripts and jokes.
7. **Bare VPs at the root mix two conventions.** 193 verbs (171 backbone
   trees `Top -> VP`) sit in a VP with nothing above it. For stand-alone
   gerunds, 141 trees are bare VPs and 311 are `S` with an empty subject;
   82 of the bare-VP trees come from two ficlets list files (1402, 1403),
   which use this bracketing almost uniformly (82 bare VP, 1 S). For
   imperatives, 19 bare VPs stand against 663 `S-IMP` trees. The bare VP
   looks like a per-file or per-annotator habit rather than a principled
   distinction.
8. **Unlike coordination is genuine, and its subject is available.** 72
   verbs sit in a UCP that is itself a predicate under a clause (48 under a
   copular or auxiliary VP, 23 directly under S): *fresh, made from
   scratch, sassy and scrumptious* (`blog/Tupelo-Honey-Cafe#9`), Example 8.
   Their subject is the clause's NP-SBJ; for 23 VBN it is even coindexed
   through `*-n`. `verbframes.py` stops at the UCP and so misses it.
9. **Non-sentential text is genre-specific.** Stage directions (50, all in
   movie scripts), ficlets and jokes list items (root, 105 and 28),
   twitter fragments (38), captions and headers (*Posted by ...*, *Sent:*,
   in blog and enron), titles (33) and citation formulas (9: *modified
   after X*, *qtd. in X*). These are well-formed units of their text types.
10. **Spoken language contributes little and mostly disfluency.** The
    spoken genres have 256 clauseless verbs, 16.9 per 1000 against 44.6 in
    writing. Of these 116 are reduced relatives, 67 are reparanda under
    EDITED, 32 fragments or root VPs and 20 errors. Spoken fragments are
    rare (28 FRAG verbs in 15,151), and face-to-face conversation has most
    of them (22). The 8 EDITED verbs in "written" genres are scripted
    dialogue (6 movie script) or quoted speech.
11. **The frame changes here are almost all passive reduced relatives.**
    1,262 of the 2,708 have `frame_full` ≠ `frame_backbone`; 1,170 of these
    are VBN reduced relatives losing `NP(*)` (e.g. `NP(*) PP-CLR` →
    `PP-CLR`: 210). They are 4.3% of the 26,947 changed frames in MASC.
    For VBG reduced relatives the two frames agree (708 of 709), but both
    lack the subject.
12. **Overall division** (Table 4): about 73% genuine constructions, 17%
    non-sentential units, 3% disfluencies and 8% (6–12%) annotation errors.
    Artefacts of spoken language proper (EDITED plus spoken fragments) are
    about 100 verbs, 4% of the clauseless verbs, but 39% of those in the
    spoken genres.

## What it means

**(a) The context-free backbone and the fast parser.** The reduced
relatives give the backbone `NP -> NP VP` (1,427 uses) and
`NP -> NP Comma VP` (193), plus 120 rarer NP rules containing a VP (1,916
uses in all). Every VP after a noun phrase can then attach low or high,
and the gold standard itself takes the wrong option for about 30% of
*NP , V-ing* sequences, so a parser trained on these trees will learn the
misattachment too. The bare root VP adds `Top -> VP` (171), and EDITED
over a VP becomes a separate symbol `EDITEDxVP` (55) beside `EDITEDxS`
(154). The error trees add rules found nowhere else, such as
`PRN -> SxNP VP` (7) and FRAG and SBAR rules with a bare subject before a
VP. Inserting the missing S in the 117 detected trees before reading off
the grammar would be cheap and would remove this noise.

**(b) Verb frames and the complement/modifier distinction.** The reduced
relative as a whole is a modifier of its noun, like a relative clause, but
the noun fills an argument slot of the verb: the subject of a VBG, the
logical object of a passive VBN. Neither frame records it, and the
backbone frame of a passive participle (`PP-CLR` for *associated with X*)
looks like an intransitive verb's. For frame extraction it would be better
to add the modified noun as a configurational argument (SBJ for VBG, the
object for VBN except unaccusatives) than to count 1,947 verbs as
subjectless. The misattached free adjuncts then give wrong arguments (the
couch pulls a pillow) at roughly the rates in finding 5. Root VPs,
fragments, titles and stage directions are subjectless for reasons of
text type, not valency, and should be kept apart from the frame counts, as
imperatives are. The 128 error-class verbs do have subjects in the
annotation; a frame extractor that reads them from the VP's -SBJ sister
would recover most.

**(c) The flat neo-Davidsonian semantics.** In `go/interp/semantics.go`,
a VP daughter of an NP with no function tag gets the relation `mod`, so
*guns sold* yields `guns(x) ∧ sold(e) ∧ mod(x, e)`: no `sbj` or `obj`.
Keeping the gold empty elements, as `docs/flat-semantics.md` proposes for
traces, would not help here, since the reduced relative's `*` is unbound
and the VBG has none. What would help is a construction rule on
`NP -> NP VP`: relate the head noun's referent to the event as `sbj` when
the VP is headed by VBG and as `obj` when by a passive VBN. For bare root
VPs and fragments the reading correctly has an event with no subject; any
agent would have to come from discourse.

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
| `essays/Ohio_Steel#36`, `ficlets/1403#545`, `ficlets/1402#520`, `ficlets/1401#495`, `fiction/Nathans_Bylichka#83`, `blog/detroit#22`, `blog/detroit#23`, `essays/anth_essay_4#89`, `essays/Ant_Robot#116`, `non-fiction/CUP1#199`, `non-fiction/CUP2#164`, `twitter/tweets1#87`, `twitter/tweets1#390`, `twitter/tweets1#653` | participial free adjunct attached as a reduced relative to the preceding NP; should be `S-ADV` with `*PRO*` in the VP or clause |
| `govt-docs/Env_Prot_Agency-nov1#138` | coordinated gerunds attached as a reduced relative to the first conjunct |
| `non-fiction/rybczynski-ch3#24` | VPs coordinated with *conducted* attached as a reduced relative to *an architect* |
| `debate-transcript/2nd_Gore-Bush#1083`, `spam/Re_JobOffer#19`, `spam/111369#4`, `movie-script/pirates#43`, `wsj/wsj_0184#14`, `technical/1468-6708-3-1#161`, `twitter/tweets2#163` | passive participle in a reduced relative without its `(NP (-NONE- *))` |
| `wsj/wsj_0127#0`, `wsj/wsj_0136#0`, `wsj/wsj_0189#3`, `wsj/wsj_0176#4`, `w3c/lists-046-12122969#4` | participle tagged VBD |
| `ficlets/1403#153` | `(VP (VBG watching) (NP-SBJ them) (VP melt))`: small clause without S |

**On `verbframes.py`.** (i) For the 92 + 22 verbs whose subject is a
sister of the VP or sits in an S closed too early, it reports "no clause
above" and a frame without SBJ although the subject is annotated
(`jokes/jokes3#36`: `frame_full` = `NP`). (ii) Its upward walk stops at
UCP, so 72 verbs whose UCP is a predicate of a clause lose that clause's
subject (`blog/detroit#33`). (iii) `is_aux_vp` treats any VP with a VP
daughter as auxiliary, so in `ficlets/1403#153` the lexical verb
*watching* is skipped and *melt* becomes a root clauseless verb; only 8 VPs
in MASC have an -SBJ daughter, so the effect is negligible. (iv) Its cause
"no clause above (EDITED)" (70) counts only verbs whose first non-VP
ancestor is EDITED; 5 more are inside EDITED higher up.

## Open questions

- Why do participial adjuncts get attached low so often? If MASC's trees
  were produced by correcting an automatic parse (I have not checked), the
  low attachment may be the parser's, left uncorrected; this is
  speculative.
- Of the 36 VBN reduced relatives without `*`, which are unaccusative or
  adjectival and which are missing traces? I judged about 8 clearly
  passive; the rest need a decision rule.
- The spoken genres contributed only 16 items to the random sample, so no
  separate error rate for speech can be given; a stratified sample would.
- Should *called X*, *named X*, *known as X* reduced relatives (70 with an
  indexed object controlling a small clause) count as complement frames of
  their own (object plus predicative)?
- Is the bare-VP root convention confined to particular files or
  annotators? The ficlets list files suggest so; a file-by-file count would
  settle it, and normalising to `S` with an empty subject would unify the
  frames with the 311 sentences already so annotated.
