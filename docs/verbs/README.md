# Verbs in MASC: what the non-context-free annotation does to them

This series asks what the parts of the Penn Treebank annotation that a
context-free backbone throws away (empty elements, their indices, function
tags) do to MASC's verbs. The aim is groundwork for the theory of verbs
(Dowty, Parsons, Krifka, Levin), and first of all for its largest
distinction: complement versus modifier. Complements are chosen by the verb
and are few; modifiers go with nearly any verb and carry much of what a
sentence is for.

The question came from TIGER. There, odd verb frames (no subject, say)
turned out to mark one of three things: coordination, the secondary edges
the backbone ignored, or annotation error. Is MASC the same?

Six reports, each written independently from the same data, answer parts
of the question. This page puts them together.

| report | question |
|---|---|
| [01 *PRO* subjects](01-pro-subjects.md) | what the empty *PRO* subject is, what controls it, what the backbone makes of it |
| [02 traces](02-traces.md) | *T* and * (extraction, passive, raising): what is lost, and how far indices restore it |
| [03 coordination](03-coordination.md) | VP coordination, right-node raising, gapping, extraposition: the analogue of TIGER's secondary edges |
| [04 no clause](04-no-clause.md) | verbs with no clause above them: reduced relatives, fragments, disfluencies, errors |
| [05 residue and errors](05-residue-and-errors.md) | the frames nothing explains, and the annotation error rate |
| [06 complements and modifiers](06-complements-and-modifiers.md) | how promiscuous complements and modifiers are, by entropy and mutual information |
| [07 where the ambiguity is](07-frame-lexicon.md) | how much a lexicon of verb uses cuts a forest, and the exact split of a forest's entropy among the verbs' choices and the rest |

Every number comes from the scripts in `tools/masc/verbs/` or
`tools/masc/verbframes.py`, run on MASC's Penn Treebank files. Every example
is given by sentence id and can be shown with the scripts' `--show`
options. Claims about the literature are from memory and are marked where
they matter.

## The answer in brief

* **In MASC, unlike TIGER, a wacky frame means annotation error.** Only 146
  of the 70,101 lexical verbs lack a subject for no nameable reason. 136 of
  those are errors, and only 9 are genuinely subjectless (05). Coordination
  never leaves a verb subjectless, because the Penn Treebank puts a subject
  shared by conjoined predicates above a coordinated VP. The sharing is left
  to the tree structure, where TIGER spells it out as a secondary edge (03).
* **Empty elements change 38.4% of verb frames** (26,947 verbs). About half
  are traces of passive, extraction and raising; the rest are nearly all
  *PRO* subjects. Almost everything else is marginal (table below).
* **What is lost is almost all recoverable, and locally.** Indices within
  the sentence tree resolve 89.5% of traces to a filler. Two structural
  steps bring about 94% of them to overt words (02). Controllers are almost
  categorically predictable from the verb (01). Six kinds of sharing link
  restore nearly every coordinated frame, three of them from the backbone's
  shape alone (03).
* **Complements are much less promiscuous than modifiers in the sense that
  matters.** Knowing the verb removes 38% of the uncertainty about its
  complement frame and 10% about its modifiers (06). But promiscuity
  belongs to individual types, not to the two classes: the NP object is
  as widely spread as a locative, and DIR and EXT modifiers are as
  verb-selective as complements.
* **The complement/modifier line is carried by function tags, and the
  backbone drops them.** Without tags, a control complement and a purpose
  clause have the same backbone shape (01), and an NP-TMP looks like an
  object (06). About one verb-phrase daughter in six has a doubtful role
  even with the tags.

## What the empty elements do: the ledger

All 70,101 lexical verbs, by what makes the frame in full differ from the
frame without empty elements:

| cause | verbs | share of all verbs | report |
|---|---|---|---|
| a *T* or * trace (passive, extraction, raising) | 14,081 | 20.1% | 02 |
| a *PRO* subject (control, imperative, gerund, dropped subject) | 12,597 | 18.0% | 01 |
| ellipsis `*?*`, a null complementizer `0`, *RNR*, *ICH*, *EXP* | 269 | 0.4% | 02, 03 |
| **frame changed** | **26,947** | **38.4%** | |

Report 02 counts 14,139 verbs with a trace, 58 more than the table: those
whose clausal complement is `SBAR(0)` over a *T* clause. Beyond these
changed frames, some events are missing from `verbs.jsonl` altogether:

* 438 second verbs of `(VP V CC V NP)`;
* 204 gapped conjuncts;
* 677 verbs, mostly copulas, directly under SQ or SINV;
* 11 verbs tagged JJ or NN as the head of a VP (03, 05, 06).

Some frames are wrong in both the full and the backbone version: 831
extraposition antecedents count as complements (03), and 974 passive
by-phrases count as modifiers (06).

## Report by report

**01 *PRO*.**
* Only about 40% of *PRO* is control proper: verb complements 18%,
  adverbials 22%. Imperatives are 18%, gerunds 14%, and dropped subjects
  8% (mostly Twitter).
* Only a third of *PRO* are indexed, and whether they are depends on the
  construction.
* Where there is an index, the verb decides the controller almost without
  exception: *want* is subject control 401 times in 402, *ask* object
  control 68 in 71. Nearest object, else subject, gets 92% of verb
  complements right.
* The backbone fuses a subjectless S with its VP into SxVP in 81% of
  cases. SxVP means "subject missing", not "controlled"; it covers *T* and
  * subjects too. So it works as an accidental, unlabelled slash category.

**02 Traces.**
* A quarter of the verbs with an NP object look intransitive to the
  backbone (7,629 of 31,423), mostly passives (5,555).
* Object relatives are half invisible: 69.5% are contact relatives (*the
  problem they were solving*).
* * and *PRO* do not mean the same thing throughout MASC. The WSJ files
  use the older convention, where * covers control too, and the split
  leaks elsewhere (*like*: 102 *PRO* against 8 *). Reading raising versus
  control off the labels mixes annotation convention with grammar.

**03 Coordination.**
* 9.8% of verbs are under VP coordination.
* A head-driven reading such as `interp.Flat` gives the subject to the
  first conjunct only. For 2,145 verbs (3.1%) that is the frame's only
  defect.
* The annotation writes the complement/modifier distinction into
  coordination. A shared complement is traced into each conjunct (55 *RNR*
  against 19 untraced). A shared modifier is attached once (83 against 6).
* Six sharing relations, the counterpart of TIGER's secondary edges, would
  restore these frames.

**04 No clause.**
* 3.9% of verbs have no clause above them; 72% of those are reduced
  relatives, a written-register construction.
* The understood subject of a reduced relative is never indexed. Recovering
  it takes a construction rule on NP → NP VP, not the empty elements.
* 30% of *NP , V-ing* "reduced relatives" are free adjuncts attached to the
  wrong noun. The backbone learns the error.

**05 Residue.**
* The residue is annotation error, as above.
* A random hand-checked sample puts wrong frames at 1.3% (0.4–3.4%).
* A frame seen only once is about eight times as likely to be an error, so
  a frame inventory needs a frequency threshold or a hand check.
* Two MASC blog files are the same document.

**06 Complements and modifiers.** Beyond the headline:
* Within one verb, complement frames are about as varied as modifier sets
  (2.12 against 1.98 bits), and the two are uncorrelated. What differs is
  that the verb decides *which* frames vary. Modifiers vary the same way
  whatever the verb.
* Genre moves the modifiers, not the complements. Spoken language has
  fewer modifiers inside the VP (35 against 47 per 100 verbs) but as many
  above it. The commonest modifier type follows the text type: DIR in
  movie scripts, PRP in brochures.

## The theories, against the data

These links are drawn by the reports or by this summary. Citations are
from memory.

* **Parsons and Davidson: modifiers as independent conjuncts.** The verb
  tells little about its modifiers (U ≈ 0.10), which supports treating them
  as event predicates outside the verb's entry, as the flat semantics does.
  But they are the part that moves with genre and text purpose. That fits
  the idea that they carry the communicative load, which a verb lexicon
  will not capture.
* **Dowty's proto-roles.** Nothing annotates them, and the clearest
  Proto-Agent outside the subject, the by-phrase, is tagged as a modifier.
  The by-phrase is optional (16.5% of passives) but hardly promiscuous: it
  is only ever *by*. It looks like an oblique argument once the passive
  trace is restored (02, 06). Purpose and manner modifiers, the classic
  agentivity tests, are rare with statives and common with activities.
  The fit is rough (06).
* **Krifka and aspect.** The modifiers that behave like complements are
  exactly the ones an aspectual theory would expect:
  * DIR, paths with motion verbs;
  * EXT, the difference argument of degree verbs (*increased Pell Grants
    by a million students*).
  Both measure out or bound the event, as incremental themes and paths do
  (Krifka 1998; Hay, Kennedy and Levin 1999 for degree achievements). They
  are candidates for moving across the line. Nothing here measures telicity
  itself.
* **Levin's alternations.**
  * Without traces, passives merge with inchoatives. 18% of *open*'s and
    85% of *fill*'s object-less backbone uses are passives (06). So the
    causative alternation cannot be read off the backbone.
  * The dative alternation is visible: *give* NP NP 253 against a *to*- or
    *for*-PP about 79; *tell* 55 against 1.
  * *send to* is tagged both goal (DIR 26) and recipient (DTV 30).
  * Spray/load is untestable at MASC's size.
* **Control is lexical.** The controller is near-categorical by verb (01),
  which fits a semantic account of control (Dowty 1985; Sag and Pollard
  1991; Jackendoff and Culicover 2003).

## What it means for this project

**The parser and the backbone.** Nothing here needs more than a
context-free parser; what is missing can be restored after the parse.
Three things would make the backbone better at no cost in formalism:

* keep an explicit subjectless-clause category, instead of an SxVP whose
  presence depends on punctuation (01);
* optionally keep `(NP *)` object gaps or a slash feature. NP movement is
  clause-local 4,350 times in 4,355; unbounded *T* crosses clauses about
  535 times (02);
* repair about 120 trees with a missing or misplaced S before reading off
  the grammar (04, 05).

The misattached free adjuncts and the CLR/untagged variation cap what any
parser trained on MASC can learn about attachment (04, 06).

**The flat semantics.** The reports together give a layered restoration
of each event's participants.

1. **Gold trees:** follow indices, including through passive traces (01,
   02).
2. **Controllers:** a small control lexicon, backed off to nearest object,
   else subject; for S-ADV and S-PRP, the matrix subject (01).
3. **Reduced relatives:** the head noun is subject of a VBG and object of a
   passive VBN (04).
4. **Coordination:** the six sharing relations (03).
5. **Imperatives and dropped subjects:** constants for the addressee and
   the writer (01).

On parser output, which has no indices, rules keyed on SxVP, WHNP sisters
and VBN after *be* recover subject relatives and simple passives exactly
(02). The learned function-tag table has to supply the complement/modifier
line, and its weakest tags (PRP F1 58, CLR 46) are exactly where the line
is doubtful (01, 06).

**The ambiguity question** ([`../ambiguity.md`](../ambiguity.md)). The
verb predicts its complements (U ≈ 0.4–0.5) and hardly its modifiers
(≈ 0.1). This suggests where a lexicon would cut the per-event ambiguity
and where it would not. A verb-frame lexicon should prune complement
analyses sharply. Modifier attachment, where the verb gives little
purchase, is where the product of local choices would survive. That is a
hypothesis, not yet measured. The next measurement (how much a frame
lexicon read off the treebank cuts each verb's analyses) should use frames
with traces restored and singleton frames removed, for the reasons above.

## Fixes to `verbframes.py`, made

All the fixes the reports call for are in the current
`tools/masc/verbframes.py`. The reports' own numbers come from the first
version, kept unchanged as `tools/masc/verbs/verbframes_v1.py`; the report
scripts import that. The new version records 71,218 verbs (the old one
70,101): the second and later verbs of `V CC V`, verbs directly under
SQ/SINV, and mistagged heads. Its subject walk goes through UCP, and an
untagged NP before the verb phrase can be the subject (70 verbs). The by-phrase
agent and extraposed antecedents have roles of their own, and stranded
prepositions are marked. Negation and vocatives are no longer modifiers.
The flags are scoped to the verb's own dependents, and the data typos are
mended. Modifiers above the verb phrase are collected. It writes three
frames (full, overt, backbone categories), a lemma, and `lemmas.tsv` for
the Go tools. Only 59 verbs remain "unexplained" (was 119).

## Caveats that apply throughout

* MASC mixes conventions. WSJ files use * for control, bare-VP roots and
  S-with-empty-subject roots coexist (04), and CLR is applied
  inconsistently (06).
* `blog/Uprooted_Bike` and `blog/Uprooted_Farming-on-Sand` are the same
  text, and nothing is deduplicated.
* Error rates rest on one reader's hand checks (150 to 300 items per
  report).
* The lemmatizer of 06 is crude, but a sample of 150 tokens had no errors.

## Open questions, collected

* How much of the CLR/untagged variation is disagreement and how much is
  real ambiguity? Double annotation of the 49 mixed verb–preposition pairs
  would tell (06).
* Would counting DIR, EXT and the by-phrase as complements sharpen the
  selectional split (06), and does it line up with telicity?
* Do Levin classes predict a verb's modifier entropy better than frequency
  does (06)?
* Do shared modifiers distribute over coordinated verbs (03)? The
  annotation leaves it open, and so does a flat formula.
* Partial and split control (*we decided to meet*) needs a group referent
  (01). It has not been looked for.
* Can raising versus control be read off * versus *PRO* once the WSJ files
  are normalised, or is a lexical list safer (02)?
* Tough constructions, *too/enough*, clefts and purpose clauses (240 null
  operators) need their own linking rules (02).
* Were MASC's trees made by correcting a parser's output? If so, that may
  explain the low attachment of free adjuncts (04). This is speculative.

## Reproducing

```bash
S=SCRATCH                                # holds masc/data and treebank.py's output in ann/
python3 tools/masc/verbframes.py $S/masc/data $S/verbs                 # verbs.jsonl
python3 tools/masc/verbs/pro_subjects.py $S/masc/data $S/ann/annotated.jsonl $S/pro/pro.jsonl
python3 tools/masc/verbs/traces.py $S/masc/data $S/traces && \
  python3 tools/masc/verbs/traces.py --report $S/traces $S/verbs/verbs.jsonl
python3 tools/masc/verbs/coordination.py $S/masc/data $S/coord && \
  python3 tools/masc/verbs/coordination.py --tables $S/coord
python3 tools/masc/verbs/noclause.py $S/masc/data $S/noclause/nc.jsonl && \
  python3 tools/masc/verbs/noclause_sample.py $S/noclause/nc.jsonl
python3 tools/masc/verbs/residue.py $S/masc/data $S/verbs/verbs.jsonl table
python3 tools/masc/verbs/complements_modifiers.py $S/masc/data
```

Each script runs in under half a minute, and each report gives its exact
commands.

## References (from memory, to be checked)

Dowty, D. (1985). On recent analyses of the semantics of control.
*Linguistics and Philosophy* 8, 291–331.

Dowty, D. (1991). Thematic proto-roles and argument selection. *Language*
67(3), 547–619.

Hay, J., Kennedy, C. and Levin, B. (1999). Scalar structure underlies
telicity in "degree achievements". *Proceedings of SALT 9*.

Jackendoff, R. and Culicover, P. W. (2003). The semantic basis of control
in English. *Language* 79(3), 517–556.

Krifka, M. (1998). The origins of telicity. In Rothstein (ed.), *Events
and Grammar*, Kluwer.

Levin, B. (1993). *English Verb Classes and Alternations*. Chicago.

Parsons, T. (1990). *Events in the Semantics of English*. MIT Press.

Sag, I. A. and Pollard, C. (1991). An integrated theory of complement
control. *Language* 67(1), 63–113.
