# A MASC tree as a Prolog program

A tree of MASC's Penn Treebank annotation is written as a small Prolog
program, loaded together with the rules in `ptb.pl`. This follows
odd_one_out's TIGER programs (`dep2tiger/prolog` there). `docs/masc-prolog.md`
says what carries over and what differs.

## Files

| file | what it is |
|---|---|
| `../ptb2pl.py` | writes the program for a tree, or for every tree of the corpus |
| `ptb.pl` | the rules every program loads: the `--->` notation, the scan step `daughters/3`, and the transformations `analysis/1`, `tree/1` and `reattached/1`; two hooks, `displacement/1` and `extra_fact/1`, let the CGELBank, UD and spaCy programs of `tools/prolog` share it |
| `check.pl` | every program's analyses counted, before and after reattachment, in one process |
| `examples/` | eight MASC trees, chosen for their empty elements: a relative clause (`relative`), a wh-question (`whq`), extraposition (`ich`), an expletive's clause (`exp`), a topicalised quotation (`tpc`), control (`pro`), right-node raising (`rnr`), gapping (`gap`) |
| `test_ptb2pl.py` | the tests: `python3 -m unittest tools/masc/prolog/test_ptb2pl.py` |

## Writing the programs

```bash
python3 tools/masc/ptb2pl.py $MASC/data 'court-transcript/Day3PMSession#385'    # one, to stdout
python3 tools/masc/ptb2pl.py $MASC/data --all $OUT                              # all, 12 seconds
```

A tree is named as `verbframes.py` names it (`genre/file#index`), and its
words are numbered as there. `--all` writes `$OUT/genre/file/index.pl`: 34,555
programs, 139 MB.

Some trees are left out:
* 27 are a single symbol with no constituent (`----`, `=-=-=`).
* One is the `(3225)` of an annotation-tool log line in enron 52555.

The converter mends three slips in the source files:
* **A word split at a no-break space.** A double-encoded *vis-à-vis* in the
  blog Effing-Idiot contains a no-break space. `masctrees.tokenize` keeps a
  no-break space inside a word as part of it.
* **Empty brackets**, `( )`, are dropped.
* **A tag over several strings** is taken as one word.

22 empty elements have an index that no constituent bears. They are kept
without an antecedent, and the program says so in a comment.

## What a program says

`court-transcript/Day3PMSession#385`, *What grade is she in ?*:

```prolog
sentence([
  w(wdt,'What'), w(nn,grade), w(vbz,is,be), w(prp,she), w(in,in), w('.','?')
]).

guidelines(revised).
root(sbarq1).
sbarq(sbarq1) ---> ['--':whnp(whnp1), ^'--':sq(sq1), '--':t('.')].
whnp(whnp1) ---> [^'--':t(wdt), '--':t(nn)].
sq(sq1) ---> [^'--':t(vbz), sbj:np(np1), prd:pp(pp1)].
np(np1) ---> [^'--':t(prp)].
pp(pp1) ---> [^'--':t(in), '--':np(np2)].
np(np2) ---> [^'--':e('*T*', whnp1)].
```

* **Words.** A word is `w(Tag, Form)`, or `w(Tag, Form, Lemma)` for a verb
  whose lemma differs from its form. Every leaf but an empty element is a
  word, punctuation and MASC's `CODE` and `SU` leaves included.
* **Constituents** are named by category and their number in a top-down
  scan.
* **Labels.**
  * A daughter is `Label:Daughter`. The label is the node's function tags
    that say what it is to its parent: grammatical role and adverbial
    function (SBJ LGS PRD CLR DTV PUT BNF DIR EXT LOC MNR PRP TMP VOC ADV).
  * It is a list where there are several, `[loc,prd]`, and `'--'` where
    there are none.
  * The other function tags (NOM HLN TTL TPC …) are a fact,
    `tags(s2, [nom])`.
* **Heads.** `^` marks the head daughter, by Collins's head table (1999,
  appendix A). NML is read as NP, and empty elements are passed over.
* **Empty elements.**
  * An empty element is `e(Kind)`, or `e(Kind, Antecedent)` where it is
    co-indexed. The index is resolved to the name of the constituent that
    bears it.
  * A constituent marked for gapping, `NP=1`, gives `gap(np1, Ref)`. `Ref`
    is the constituent labelled `-1`; where there is none, as for most of
    MASC's gapping marks, it is the number, which the parallel constituents
    share.
* **Guidelines.** `guidelines(ii)` marks the 34 old WSJ files (see
  `docs/masc-provenance.md`); all other programs say `guidelines(revised)`.

## The transformations

```
swipl tools/masc/prolog/ptb.pl tools/masc/prolog/examples/whq.pl
?- analysis(Facts).        % the tree as ground facts
?- tree(T).                % the tree as a term
?- reattached(P), write_program(P).
```

**`analysis/1`** gives these facts:
* `root/1`;
* `constituent(Name, Cat, Ranges)`, whose ranges are empty (`[7-6]`) for a
  constituent over an empty element only;
* `edge(Parent, Label, D)` and `head(Parent, D)`, where `D` is a
  constituent, a word position, or an empty element `e(Parent, J)`, the
  J-th daughter of its parent;
* `empty(Id, Kind, P)`, `P` being the position of the word the empty
  element stands before, and `antecedent(Id, Name)`;
* `tags/2`, `gap/2`, and `word/2`, `tag/2`, `lemma/2`.

**`reattached/1`** moves each displaced constituent to its trace:
* It applies to `*T*`, `*ICH*` and `*EXP*` traces whose antecedent has
  words and does not contain the trace.
* The constituent goes under the trace's label, or under its own where the
  trace's is `'--'`.
* Each constituent's runs are then read off its words, and its rule is
  written over them.
* The result is a program of the same form whose rules may have several
  runs, a linear context-free rewriting system like TIGER's. It is
  Evang and Kallmeyer's (2011) conversion, as a transformation over the
  stored tree.
* `moved(Name, Kind)` records each move.
* Empty elements that did not move stay at the place they had.

*What grade is she in ?* reattached:

```prolog
sbarq(sbarq1)--->[^ -- : sq(sq1),-- : t('.')].
sq(sq1)--->[prd:pp(pp1)@1,^ -- : t(vbz),sbj:np(np1),prd:pp(pp1)@2].
pp(pp1)--->[[-- : np(np2)],[^ -- : t(in)]].
np(np2)--->[^ -- : whnp(whnp1)].
```

## Over the corpus

```bash
find $OUT -name '*.pl' | awk -v B=$OUTB '{o=$0; gsub("/","_",o); print $0 "\t" B "/" o}' \
  | swipl -q -g check -t 'halt(1)' tools/masc/prolog/ptb.pl tools/masc/prolog/check.pl
```

This takes 2.5 minutes. The results:
* **One analysis each.** Every one of the 34,555 programs has exactly one
  analysis, and so does every reattached program.
* **Moves.** Reattachment moves 9,941 constituents in 7,824 trees.
  * 9,328 constituents in 5,180 trees become discontinuous.
  * 5,156 of those trees have constituents of at most two runs, and 24
    have one of three.
  * The other trees with a move have their antecedent next to the trace,
    as in a subject question, and stay continuous.
* **Traces not moved.** The rest of the 12,149 `*T*`, `*ICH*` and `*EXP*`
  traces point at an antecedent with no words (`WHNP-1` over `0` in a
  relative clause), or have no antecedent.
