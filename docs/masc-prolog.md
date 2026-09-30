# MASC trees as Prolog programs

odd_one_out writes each sentence of the TIGER treebank as a small Prolog
program (`dep2tiger/prolog` there: `README.md`, `ALGORITHM.md`, `tiger.pl`,
`tiger2pl.py`, `analyzer.pl`). This note works out what would change in
doing the same for MASC's Penn Treebank (PTB) trees.

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

The proposal: store (a), and make (b) a transformation. Moving each
displaced antecedent to its trace is a few clauses over the scan. It gives
a TIGER-style discontinuous derivation, for comparison with the German.

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

MASC's extra nodes need rules (see `docs/masc-provenance.md`):
* **Slash units:** `(SU /)` can be a daughter.
* **Turns and original spellings:** `(CODE <TURN>)` and
  `(CODE {TEXT:gonna})` are better as facts about the sentence than as
  daughters.
* **Repairs:** `EDITED` is a labelled daughter.
* **Debris:** tokens like `RSQUOs` and `<disfluency>` need a repair table,
  as `patches.py` supplies for TIGER.

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

The program, encoding (a):

```prolog
sentence([
  w(dt,that), w(vbz,'''s',be), w(dt,the), w(jj,only), w(nn,thing),
  w(prp,'I'), w(vbd,found,find), w(rp,out), w(nn,tonight), w(su,'/')
]).

root(s1).
s(s1)       ---> [sbj:np(np1), ^'--':vp(vp1), '--':t(su)].
np(np1)     ---> [^'--':t(dt)].
vp(vp1)     ---> [^'--':t(vbz), prd:np(np2)].
np(np2)     ---> [^'--':np(np3), '--':sbar(sbar1)].
np(np3)     ---> ['--':t(dt), '--':t(jj), ^'--':t(nn)].
sbar(sbar1) ---> ['--':whnp(whnp1), ^'--':s(s2)].
whnp(whnp1) ---> [^'--':e('0')].
s(s2)       ---> [sbj:np(np4), ^'--':vp(vp2)].
np(np4)     ---> [^'--':t(prp)].
vp(vp2)     ---> [^'--':t(vbd), '--':e('*T*', whnp1), '--':prt(prt1), tmp:np(np5)].
prt(prt1)   ---> [^'--':t(rp)].
np(np5)     ---> [^'--':t(nn)].
```

Encoding (b) matters when the displaced constituent has words. Take *What
did you say ?*, `(SBARQ (WHNP-1 (WP What)) (SQ (VBD did) (NP-SBJ (PRP you))
(VP (VB say) (NP (-NONE- *T*-1)))) (. ?))`:

```prolog
% (a) the trace kept
sbarq(sbarq1) ---> ['--':whnp(whnp1), ^'--':sq(sq1), '--':t('.')].
sq(sq1)       ---> [^'--':t(vbd), sbj:np(np1), '--':vp(vp1)].
vp(vp1)       ---> [^'--':t(vb), '--':e('*T*', whnp1)].

% (b) the antecedent reattached: the VP is discontinuous, What ... say
sbarq(sbarq1) ---> [^'--':sq(sq1), '--':t('.')].
sq(sq1)       ---> ['--':vp(vp1)@1, ^'--':t(vbd), sbj:np(np1), '--':vp(vp1)@2].
vp(vp1)       ---> [['--':whnp(whnp1)], [^'--':t(vb)]].
```

The (b) rules have the shape of TIGER's s47200: a two-run VP whose runs
the clause places around the finite verb and the subject.

## The work

* **`tools/masc/ptb2pl.py`,** about the size of `tiger2pl.py`. It reads
  `.mrg` files through `masctrees`, splits function tags into edge labels
  and category tags, runs the head table, resolves indices to node names,
  and applies the repairs of point 6.
* **`ptb.pl`:** `tiger.pl` without the code for several runs, with the
  empty daughter `e/1`, `e/2` and the trace facts. The reattachment of
  point 4(b) is a transformation in it.
* **Tests:** on a handful of MASC trees chosen for `*T*`, `*PRO*-n`,
  `*RNR*`, gapping and `*ICH*`, with one old WSJ tree for the `*-n`
  convention.

To decide before building: whether the reattachment is a transformation, as
proposed, or the stored form.

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
