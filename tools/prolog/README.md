# Treebank trees as Prolog programs: CGELBank, UD, spaCy

Three converters write a sentence's analysis as a small Prolog program in the
`--->` notation of `tools/masc/prolog/ptb.pl`. That notation comes from
odd_one_out's TIGER programs (`dep2tiger/prolog` there): the tree is a grammar
with one rule per constituent and exactly one derivation. A program is loaded
after a rules file, which shares `ptb.pl`'s scan and its transformations:
* `analysis/1`, the tree as ground facts;
* `tree/1`, the tree as a term;
* `reattached/1`, displaced constituents moved to their gaps, giving a
  linear context-free rewriting system.

| converter | reads | rules file | program for |
|---|---|---|---|
| `cgel2pl.py` | CGELBank's `.cgel` trees ([nert-nlp/cgel](https://github.com/nert-nlp/cgel)) | `cgel.pl` | a CGEL tree: categories, functions, gaps, fused functions |
| `ud2pl.py` | CoNLL-U (Universal Dependencies) | `dep.pl` | a dependency tree, plus the enhanced graph as facts |
| `clear2pl.py` | text, parsed by spaCy (`en_core_web_trf` by default) | `dep.pl` | spaCy's dependency tree (ClearNLP-style labels) |

`deptree.py` is the part `ud2pl.py` and `clear2pl.py` share. `test_prolog.py`
holds the tests, and `examples/` six programs.

## Writing programs

```bash
python3 tools/prolog/cgel2pl.py $CGEL/datasets/ewt.cgel reviews-020992-0003      # one tree, to stdout
python3 tools/prolog/cgel2pl.py $CGEL/datasets/ewt.cgel --all $OUT                # every tree
python3 tools/prolog/ud2pl.py en_ewt-ud-dev.conllu --all $OUT                     # every sentence
tools/frames/.venv/bin/python tools/prolog/clear2pl.py text.txt --all $OUT        # spaCy's environment
```

`$CGEL` is a clone of nert-nlp/cgel. CGELBank also has a `.conllu` file beside
each `.cgel` file, so `ud2pl.py` gives every CGEL tree its UD counterpart, for
the same sentence and under the same `sent_id`. Output files are named by
sentence id, with characters other than letters, digits, `.`, `#`, `-` and
`_` replaced by `_`.

## Loading and querying

```bash
swipl tools/prolog/cgel.pl tools/prolog/examples/cgel_is_that_all.pl
?- analysis(F).          % the tree as facts: constituent/3, edge/3, head/2, empty/3, antecedent/2, ...
?- functions(F).         % CGEL's graph: function(Node, Parent, Function), fused edges restored
?- reattached(P).        % each gap's antecedent moved to the gap

swipl tools/prolog/dep.pl tools/prolog/examples/ud_nonprojective.pl
?- arcs(A).              % the dependency tree back: arc(Dependent, Head, Relation)
```

The rules files load `../masc/prolog/ptb.pl` themselves. One query over many
files in one process, as with the MASC programs:

```bash
ls $OUT/*.pl | LANG=C.UTF-8 swipl -q -g 'scan(analysis)' -t 'halt(1)' tools/prolog/cgel.pl
```

Use a UTF-8 locale: a few CGELBank sentence ids, and so file names, are not ASCII.

## CGELBank programs (`cgel2pl.py`, `cgel.pl`)

```prolog
sentence([w(v_aux,is,be), w(d,that), w(d,all), w(n_pro,you), w(v,got,get), w(n,winter)]).
guidelines(cgelbank).
root(clause1).
clause(clause1) ---> [prenucleus:t(v_aux), ^head:clause(clause2), vocative:np(np4)].
clause(clause2) ---> [subj:np(np1), ^head:vp(vp1)].
nom(nom1) ---> [^det_head:dp(dp1)].
vp(vp1) ---> [^head:e(gap, 1), predcomp:np(np2)].
vp(vp2) ---> [^head:t(v), obj:e(gap, dp2)].
fused(dp1, np1, det).
```

* **Nodes and words.** A phrasal node is named by its category and its number
  in a top-down scan. Categories are CGEL's, lower-cased, with `+` and `_` as
  `_`: `clause_rel`, `n_pro`, `v_aux`, `pp_strand`, `np_pp` (the nonce NP+PP).
  A lexical node is a preterminal `t(Cat)`, and its word is `w(Cat, Form)`, or
  `w(Cat, Form, Lemma)` where the tree gives `:l`.
* **Functions are the labels:** CGEL's, lower-cased, with `-` as `_`
  (`det_head`, `head_prenucleus`, `obj_ind`). A nonce function keeps its `+`
  and `/`, quoted (`'obj+comp'`). The root has none. `^` marks the head: the
  daughter whose function is `Head`, or a fused function ending in `Head`.
* **Gaps.** A gap is `Label:e(gap, Antecedent)`. The antecedent is the overt
  node with the gap's variable, by name, or by position where it is a word
  (subject–auxiliary inversion, `x / V_aux`). `cgel.pl` makes `gap` a kind of
  displacement, so `reattached/1` moves a prenucleus or postnucleus constituent
  to its gap, as `ptb.pl` moves a Penn constituent to its `*T*`. An antecedent
  that is a word stays where it is.
* **Words the writer left out** (`:correct` with no `:t`) read no words:
  `Label:e(omitted(Cat, Form))`.
* **Fusion.** A node with fused functions has two parents, and CGELBank stores
  only the edge from the lower one. That's Pullum and Rogers's spanning tree,
  which they show is unique. `fused(Node, Upper, Function)` restores the other
  edge by the rule of nert-nlp/cgel's `tree2tex.py`:
  * for `Det-Head`, the NP above the Nom layers;
  * for `Mod-Head`, `Marker-Head` and `Head-Prenucleus`, the grandparent;
  * the restored edge's function is the first part of the label: `det`, `mod`,
    `marker`, `head`.

  `functions/1` gives the whole graph.
* **Facts beside the tree:**
  * `sent_id/1`, `text/1` (`# text`) and `sent/1` (`# sent`);
  * for word *I*: `xpos(I, X)`, `correct(I, Form)`, `subtokens(I, Parts)`
    (`:subt`, `:subp`), `punct(I, before, Marks)` and `punct(I, after, Marks)`;
  * `note(Node, Note)`.

**Checked on all 257 gold and trial trees** (ewt, twitter, ewt-test_iaa50,
ewt-test_pilot5, trial):
* The reader agrees with nert-nlp/cgel's own `cgel.py`, node for node
  (category, function, word, variable).
* Every program has exactly one analysis.
* `reattached/1` moves 111 constituents to their gaps; the reattached programs
  also have one analysis each. 55 of them have a discontinuous constituent,
  with fan-out at most 3.
* All 92 fused functions get their `fused/3` fact. Every Det-Head goes to an
  NP, every Mod-Head to a Nom, the Marker-Head to an NP.
* Of the 8 Head-Prenucleus (fused relatives), 7 go to a Nom and one to a PP,
  in `ewt-trial` `weblog-blogspot.com_rigorousintuition_20060511134300_ENG_20060511_134300-0289`.
  That's what the tree says, and `tree2tex.py` would draw the same.

## Dependency programs (`ud2pl.py`, `clear2pl.py`, `dep.pl`)

```prolog
vb(w5) ---> [ccomp:vbd(w7)@1, aux:t(vbd), nsubj:t(nnp), ^'--':t(vb), ccomp:vbd(w7)@2, punct:t('.')].
vbd(w7) ---> [[dobj:nn(w2)], [nsubj:t(prp), ^'--':t(vbd), npadvmod:t(nn)]].
nn(w2) ---> [det:t(wdt), ^'--':t(nn)].
```

* **Constituents.** Each word that has dependents, and the root, heads a
  constituent named `w` and its position, under the functor of its tag. Its
  rule has the word itself, `^'--':t(Tag)`, and its dependents in order, each
  under its relation.
* **Non-projective arcs.** A non-projective arc makes a constituent
  discontinuous, as with *Which book did Kim say she bought?*. Its rule then
  has a list of daughters per run, and a split daughter appears as `D@K` in
  each run that holds its *K*-th run. That's the LCFRS form of the TIGER and
  reattached Penn programs, read with the same rule expansion.
* **`ud2pl.py`:**
  * a word is `w(Upos, Form)` or `w(Upos, Form, Lemma)`, with the UPOS
    lower-cased;
  * facts: `sent_id/1`, `text/1`, `xpos/2`, `feats/2`, `mwt(First, Last, Form)`,
    `edep(I, Head, Rel)` (the enhanced graph; `Head` may be an empty node,
    `'8.1'`), `empty_node(Id, Form, Lemma, Upos)`, and `misc/2` (MISC other
    than SpaceAfter);
  * `guidelines(ud)`.
* **`clear2pl.py`:**
  * a word is `w(Tag, Form)` or `w(Tag, Form, Lemma)`, `Tag` being spaCy's
    fine-grained (Penn Treebank) tag;
  * facts: `sent_id/1` (*NAME*#*N*), `text/1`, `upos/2`, `feats/2` (from
    `token.morph`), and `entity(First, Last, Label)`;
  * `guidelines(clear)`.

  Each non-blank line of the input is a document, and each sentence spaCy finds
  in it a program.
* **`arcs/1`** gives the tree back as `arc(Dependent, Head, Relation)`.

**Checked:** all 100 trees of CGELBank's `ewt.conllu` and all 2,001 of EWT's
dev set give back their trees exactly through `arcs/1`. 36 of them have a
discontinuous constituent.

## The examples

| file | what it shows |
|---|---|
| `cgel_is_that_all.pl` | *Is that all you got winter?* (CGELBank twitter): subject–auxiliary inversion, a word as a gap's antecedent; two Det-Head fusions; a relative clause's gap |
| `cgel_remarkable_claim.pl` | *What a remarkable claim to make!* (twitter): a hollow clause, its object gap's antecedent the noun; discontinuous when reattached |
| `cgel_fused_relative.pl` | a fused relative, *what you need*, with Head-Prenucleus (ewt-trial `reviews-074896-0008`) |
| `ud_fused_relative.pl` | the same sentence's UD tree (CGELBank's `ewt-trial.conllu`) |
| `ud_nonprojective.pl` | an EWT dev sentence with a non-projective arc (`reviews-249889-0002`) |
| `clear_which_book.pl` | `en_core_web_trf`'s parse of *Which book did Kim say she bought yesterday?*, whose object attaches across *did Kim say* |

The CGELBank examples are from CGELBank (Reynolds, Arora and Schneider 2023;
CC BY 4.0), and the UD ones from the English Web Treebank's UD version
(CC BY-SA 4.0).

## Tests

```bash
python3 -m unittest tools/prolog/test_prolog.py
```

The tests use hand-made trees, and stand-ins for spaCy's tokens. They need
SWI-Prolog for the Prolog half, but neither spaCy nor the corpora.
