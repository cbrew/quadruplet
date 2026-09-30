# MASC trees as Prolog programs

odd_one_out writes each sentence of the TIGER treebank as a small Prolog
program (`dep2tiger/prolog` there: `README.md`, `ALGORITHM.md`, `tiger.pl`,
`tiger2pl.py`, `analyzer.pl`). This note works out what changes in doing
the same for MASC's Penn Treebank (PTB) trees. It is now built:
`tools/masc/ptb2pl.py` and `tools/masc/prolog/` (see its README), with the
decision of point 4 taken as proposed.

**Short answer:** the core idea carries over unchanged. Each sentence
becomes a grammar whose only derivation is its own tree, and every analysis
is a transformation that scans it. What changes follows from two
differences between the treebanks:

* **PTB trees are continuous.** Every rule has fan-out one, and the linear
  context-free rewriting system (LCFRS) machinery goes unused.
* **Empty elements do the work of TIGER's crossing branches and secondary
  edges.** PTB records displacement and sharing with empty elements and
  co-indexation. How to encode them is the one real design decision
  (point 4 below).

## What stays the same

* **A nonterminal is a node, not a category.** `np(np1)` has exactly one
  `--->` rule, so the program has exactly one derivation. The argument for
  this (`ALGORITHM.md`, Proposition 3) goes through unchanged, and more
  simply.
* **One scan, many transformations.** The scan step `daughters/3` stays.
  `analysis/1`, and every other transformation, is a recursion through it
  that emits something at each node.
* **The term view (`ALGORITHM.md` §7) applies, and meets this repository.**
  With the node names dropped, a program's rules are the treebank's
  context-free productions, the grammar `go/cfg` reads. The Prolog corpus
  and the BitPar grammar are the same object at the level of types. §7's
  intersection with a partial annotation is parsing under constraints,
  which the forests of `go/cfg` already provide.
* **The scale is similar.** MASC has 34,586 trees and about 578K tokens;
  TIGER has 50K sentences and 768K tokens. It is one file per tree.

## What changes

### 1. Fan-out one: the rules are plain DCG

No PTB constituent is split into several runs. So none of these is needed:
* the `@K` notation;
* the extra difference-list pairs per run;
* the `when/2` delay for a word read before it is bound (part 6 of
  Proposition 3).

A `--->` rule is a DCG rule in other clothes, which `tiger.pl` already
allows. The exception is the reattachment option in point 4.

### 2. Labels, split between node and edge

TIGER labels every edge. PTB puts function tags on some nodes (Bies et al.
1995), and they are of two kinds:

* **Edge labels.** Grammatical role and adverbial function: SBJ, LGS, PRD,
  CLR, DTV, PUT, TMP, LOC, DIR, MNR, PRP, EXT, BNF, VOC. They become
  `Label:Daughter`, as a list where a node has several (`[loc,prd]:pp(pp1)`).
* **Category tags.** Form rather than function: NOM, HLN, TTL, TPC, CLF,
  SEZ, IMP, UNF. They stay with the node, as a fact `tags(s2, [nom])` or
  folded into the category.

Most PTB daughters have no function tag. TIGER's own `--` label serves for
them.

Some PTB tags need quoted atoms: `t(',')`, `t('prp$')`, `t('-lrb-')`,
``t('``')``.

### 3. Heads, computed

PTB marks no heads. The `^` mark comes from a head table at compile time,
as `heads.py` supplies it for the TIGER constituents with no HD. The rules
this project uses are already written: the lexical-verb walk of
`tools/masc/verbframes.py`, and the head rules of `go/explore/counts`.

### 4. Empty elements and co-indexation

14,335 of MASC's trees, about 40%, have at least one co-indexed empty
element. They fall into three kinds:

| kind | in MASC | TIGER's counterpart | encoding |
|---|---|---|---|
| unlinked: `*PRO*`, `*`, `0`, `*U*`, `*?*` with no index | 8,474, 1,780, 4,206, 440, 481 | none | a daughter that consumes no words: `sbj:e('*PRO*')` |
| sharing: `*PRO*-n`, `*-n` (control, raising, passive), `*RNR*-n`, gapping `=n` | 4,815, 6,635, 214, 631 | secondary edges | the empty daughter names its antecedent: `sbj:e('*PRO*', np1)`; outside the derivation, as secondary edges are |
| displacement: `*T*-n`, `*ICH*-n`, `*EXP*-n` | 10,826, 709, 614 | crossing branches (discontinuity) | see below |

PTB's numeric indices are only names for nodes. The converter resolves them
at compile time, so an empty element points straight at the constituent
it is co-indexed with (`e('*T*', whnp1)`), as a secondary edge names its
stepparent.

Displacement can be encoded two ways:

* **(a) Keep the trace.** It is an empty daughter pointing at its
  antecedent. The program stays context-free and faithful to the treebank,
  and the displacement is an edge beside the tree.
* **(b) Reattach the antecedent at the trace.** The tree becomes
  discontinuous and the program an LCFRS, as for TIGER, with `@K` in full
  use. This is Evang and Kallmeyer's (2011) conversion of the PTB for LCFRS
  parsing.

**Decided:** store (a), and make (b) a transformation, `reattached/1` in
`tools/masc/prolog/ptb.pl`. It moves each displaced antecedent to its trace
and rewrites every rule over the runs that result: a TIGER-style
discontinuous derivation, for comparison with the German.

### 5. Thinner words

PTB has no lemmas and no morphology, so nearly every word is
`w(Tag, Form)`:
* **Lemmas** come from `tools/masc/verblemmas.py`, or from MASC's separate
  lemma layer, as `w(Tag, Form, Lemma)`.
* **Morphology goes.** The feature facts (`case/2`, `number/2` …) do not
  arise, and neither does §8's point about selecting morphology rather
  than storing it.

### 6. Punctuation, roots and MASC's odd nodes

PTB keeps punctuation inside the tree, so there is one root per tree,
punctuation as daughters, no VROOT and no roots side by side.

MASC's extra nodes (see `docs/masc-provenance.md`):
* **Slash units:** `(SU /)` is a daughter.
* **Turns and original spellings.** Trees that are only `(CODE <TURN>)`
  are left out. A `CODE` leaf inside a tree, `(CODE {TEXT:gonna})`, is kept
  as a word: the stored form stays faithful, and words are numbered as
  `verbframes.py` numbers them. Reading such leaves as facts about the
  sentence is for a transformation.
* **Repairs:** `EDITED` is a daughter like any other.
* **Debris** (tokens like `RSQUOs`, `<disfluency>`) is kept as it is. A
  repair table, as `patches.py` supplies for TIGER, is still to write.
* **Slips the converter mends:** a word split at a no-break space, empty
  brackets `( )`, and a tag over several strings.

### 7. Two layers of conventions

The 34 old WSJ files write a controlled subject as `*-n`, not `*PRO*-n`.
The converter either normalises them, or records which convention the file
follows as a fact.

### 8. The transformations

* **`dependencies/1` and `conll/1`.** For TIGER these are Seeker and Kuhn's
  conversion to dependencies. For MASC they become a PTB-to-dependency
  conversion, most usefully to Universal Dependencies (UD) and CoNLL-U. The
  enhanced UD representation's controlled subjects and relative-clause gaps
  can be read straight off the trace links.
* **`analyzer.pl`.** This becomes the English frame analyzer: the rules of
  `tools/frames/src/frames/gold.py` in Prolog, emitting `frame(V, "nap")`,
  `unsaid(V, controlled)` and so on. It is easier than the German one:
  control, passive and relative gaps are in the traces, where the TIGER
  analyzer has to infer them.
* **`analysis/1`.** This needs only new kinds of fact: `trace/3`,
  `antecedent/2`, `tags/2`.

## An example

A MASC telephone sentence, *that 's the only thing I found out tonight*:

```
( (S (NP-SBJ (DT that))
     (VP (VBZ 's)
         (NP-PRD (NP (DT the) (JJ only) (NN thing))
                 (SBAR (WHNP-1 (-NONE- 0))
                       (S (NP-SBJ (PRP I))
                          (VP (VBD found)
                              (NP (-NONE- *T*-1))
                              (PRT (RP out))
                              (NP-TMP (NN tonight)))))))
     (SU /)))
```

The program `ptb2pl.py` writes, encoding (a). The empty element sits under
its own NP node, as in the tree:

```prolog
sentence([
  w(dt,that), w(vbz,'''s',be), w(dt,the), w(jj,only), w(nn,thing),
  w(prp,'I'), w(vbd,found,find), w(rp,out), w(nn,tonight), w(su,'/')
]).

guidelines(revised).
root(s1).
s(s1) ---> [sbj:np(np1), ^'--':vp(vp1), '--':t(su)].
np(np1) ---> [^'--':t(dt)].
vp(vp1) ---> [^'--':t(vbz), prd:np(np2)].
np(np2) ---> [^'--':np(np3), '--':sbar(sbar1)].
np(np3) ---> ['--':t(dt), '--':t(jj), ^'--':t(nn)].
sbar(sbar1) ---> ['--':whnp(whnp1), ^'--':s(s2)].
whnp(whnp1) ---> [^'--':e('0')].
s(s2) ---> [sbj:np(np4), ^'--':vp(vp2)].
np(np4) ---> [^'--':t(prp)].
vp(vp2) ---> [^'--':t(vbd), '--':np(np5), '--':prt(prt1), tmp:np(np6)].
np(np5) ---> [^'--':e('*T*', whnp1)].
prt(prt1) ---> [^'--':t(rp)].
np(np6) ---> [^'--':t(nn)].
```

Here the antecedent of `*T*` is the empty `WHNP` of the relative clause,
so there is nothing to move. Encoding (b) matters when the displaced
constituent has words. Take `court-transcript/Day3PMSession#385`, *What
grade is she in ?*:

```prolog
% (a) as stored
sbarq(sbarq1) ---> ['--':whnp(whnp1), ^'--':sq(sq1), '--':t('.')].
whnp(whnp1) ---> [^'--':t(wdt), '--':t(nn)].
sq(sq1) ---> [^'--':t(vbz), sbj:np(np1), prd:pp(pp1)].
np(np1) ---> [^'--':t(prp)].
pp(pp1) ---> [^'--':t(in), '--':np(np2)].
np(np2) ---> [^'--':e('*T*', whnp1)].

% (b) reattached/1: the PP is discontinuous, What grade ... in
sbarq(sbarq1)--->[^ -- : sq(sq1),-- : t('.')].
sq(sq1)--->[prd:pp(pp1)@1,^ -- : t(vbz),sbj:np(np1),prd:pp(pp1)@2].
np(np1)--->[^ -- : t(prp)].
pp(pp1)--->[[-- : np(np2)],[^ -- : t(in)]].
np(np2)--->[^ -- : whnp(whnp1)].
whnp(whnp1)--->[^ -- : t(wdt),-- : t(nn)].
moved(whnp1,'*T*').
```

The (b) rules have the shape of TIGER's s47200: a two-run constituent whose
runs the clause places around the finite verb and the subject.

## As built

* **`tools/masc/ptb2pl.py`** writes the programs, 34,555 of them, in 12
  seconds.
* **`tools/masc/prolog/ptb.pl`** holds the notation, the scan, and three
  transformations:
  * `analysis/1`, the tree as ground facts;
  * `tree/1`, the tree as a term;
  * `reattached/1`, point 4(b).
* **`check.pl`** counts analyses over the corpus in one process.
* **Tests:** `test_ptb2pl.py`, with eight example trees in `examples/`.
* **Corpus check:**
  * Every program has exactly one analysis, and so does every reattached
    program.
  * Reattachment moves 9,941 constituents in 7,824 trees.
  * 9,328 constituents in 5,180 trees become discontinuous, of fan-out 2,
    or 3 in 24 trees.

Not yet written: the dependency and CoNLL-U transformations, and the frame
analyzer in Prolog (point 8).

## What the Prolog form offers, and what it does not

**Compared with FSTs and tree transformations, the stored form adds
nothing.** `ALGORITHM.md` §7–8 in odd_one_out says as much:
* Each program is a derivation term, with no sharing inside a sentence, and
  a corpus of them is a finite regular tree language.
* To hold a large corpus, its §8 architecture is the right one: a
  finite-state lexicon and an LCFRS over tags.
* Automata bring minimisation, determinisation and composition, and make
  some questions decidable, such as whether a conversion loses information
  (whether its transducer is injective). Prolog gives none of this: a
  transformation can fail to terminate, or be many-to-one, without anyone
  knowing.

**Most of the transformations are standard too.** `analysis/1`, dependencies
and CoNLL output are homomorphisms or attribute-grammar computations.
Reattachment moves subtrees along co-indexation and recomputes runs from
positions. That is beyond a plain top-down tree transducer, but within
reach of a macro tree transducer with attributes, since a tree has boundedly
many indices. In Prolog it is about 100 lines over the shared scan, easy to
change and to check.

**The difference is a general query and transformation language that sits
alongside the data.** That opens up six things:

1. **Partial annotations and constrained parsing.** Leave a label, a daughter
   or a word as a variable, and the program asks which completions fit.
   This is §7's intersection with a partial annotation, run directly
   rather than built as an automaton.
2. **Generation.** The same DCG parses and generates. A tree's program can
   check a candidate sentence against that tree, for instance the sentence
   with its verb replaced. That gives controlled minimal pairs for triads,
   with the structure held fixed.
3. **Queries beyond a fixed query language.** TGrep2, Tregex and
   TIGERSearch fix their query languages. Here a query can use recursion,
   negation, aggregation and relations defined as needed. The search for
   raising and object control in `tools/frames/README.md` was a Python
   script; here it is a short query over `empty/3` and `antecedent/2`.
4. **One store for evidence and judgments.** Treebank facts, frame rules,
   verb-class lexicons and LLM triad answers can all be facts, with rules
   that combine them. A class assignment consistent with a verb's corpus
   frames and with the triads it passed becomes an inference rather than
   glue code between pipelines. FSTs have no place for this.
5. **Explanations.** A proof records which rule and which trace produced a
   frame. That is a checkable reason to set beside an LLM's stated reason
   on a triad.
6. **One notation for German and English.** TIGER and reattached MASC now
   share one notation and one scan. A frame analyzer written once in Prolog
   could run over both.

**The division of labour this suggests:** Prolog as the specification and
reference layer, automata as the compiled and scalable one. Transformations
and inferences are developed in Prolog. Where one falls in a decidable
class, it is compiled to a transducer and tested against the Prolog
original.

## Reproducing the counts

```bash
F=$(find $MASC/data -name '*.mrg' ! -name '._*')
for p in '\*T\*-[0-9]' '\*ICH\*-[0-9]' '\*EXP\*-[0-9]' '\*RNR\*-[0-9]' \
         '\*PRO\*-[0-9]' '(-NONE- \*PRO\*)' '(-NONE- \*-[0-9]' '(-NONE- \*)' \
         '(-NONE- 0)' '\*U\*' '\*?\*' '[A-Z]=[0-9]'; do
  printf '%-22s %6s\n' "$p" "$(cat $F | grep -o "$p" | wc -l)"
done
```

## References

* Bies, A., Ferguson, M., Katz, K. and MacIntyre, R. (1995). *Bracketing
  Guidelines for Treebank II Style.* Penn Treebank Project.
* Evang, K. and Kallmeyer, L. (2011). PLCFRS parsing of English
  discontinuous constituents. *Proceedings of IWPT 2011*.
* Seeker, W. and Kuhn, J. (2012). Making ellipses explicit in dependency
  conversion for a German treebank. *Proceedings of LREC 2012*.
