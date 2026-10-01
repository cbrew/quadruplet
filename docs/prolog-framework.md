# A common Prolog framework for treebank traditions

Status: a design, October 2026. Nothing in this document beyond the converters
of `tools/prolog` and `tools/masc/prolog` is built yet.

## The aim

To hold, in one Prolog framework, what different notations and traditions say
about the same sentences, and to relate them:
* constituency trees: MASC's Penn Treebank, TIGER in odd_one_out, CGELBank;
* dependencies: Universal Dependencies, spaCy's ClearNLP-style parses;
* predicate–argument and lexical resources: PropBank, VerbNet, MASC's FrameNet
  layer.

Each tradition keeps its own notation, and the framework says how they
correspond. It isn't one interlingua into which everything is translated. The
differences between traditions (gaps or none, headed coordination or not,
complement against adjunct) are part of the information, and a translation
would erase them.

## What exists

* **The program form.** odd_one_out's `dep2tiger/prolog/tiger.pl`:
  * a sentence is a small program in the `--->` notation, a grammar with one
    rule per constituent and exactly one derivation;
  * everything one wants from it is a transformation, a scan of that
    derivation;
  * `ALGORITHM.md` there, §5, proves there is one derivation.
* **Penn Treebank.** `tools/masc/prolog/ptb.pl` and `ptb2pl.py` do the same
  for MASC, all 34,555 trees. `reattached/1` moves displaced constituents to
  their traces, turning a tree into a linear context-free rewriting system.
* **CGELBank, UD and spaCy.** `tools/prolog` adds `cgel2pl.py`, `ud2pl.py` and
  `clear2pl.py`. They share `ptb.pl`'s scan and transformations through two
  hooks, `displacement/1` and `extra_fact/1`. They are checked on all 257
  CGELBank trees and 2,101 UD trees; `tools/prolog/README.md` has the details.

Why the programs are deterministic: a nonterminal names one node, not a
category. `np(np1)` and `np(np2)` are different nonterminals with one rule
each, so the program is the tree's derivation written as a grammar, and
nothing is left to choose.

## What is missing

### 1. A common anchor

Each program numbers its own tokens, and the traditions tokenise differently:
* CGELBank makes *no one* and *so long as* single lexemes and leaves
  punctuation out of the tree;
* UD splits *don't* into *do* + *n't*;
* spaCy and the Penn Treebank have rules of their own.

Position 3 in one program isn't position 3 in another. Every word should carry
character offsets into the sentence's text, `offsets(I, From, To)`, and
statements across traditions should be about spans of text.

CGELBank is the test bed. Its 257 sentences come as both `.cgel` and
`.conllu`, with exactly these tokenisation differences, and spaCy can parse the
same texts.

### 2. Coexistence

A program is loaded one at a time, into `user`, and `ptb.pl`'s expansion
defines `d/…` and a predicate per category. Several analyses of one sentence
can't be loaded together. Each tradition's program should load into a module
(`cgel:`, `ud:`, `ptb:`, `tiger:`, `clear:`), with the shared transformations
taking the module as an argument.

### 3. Bridge rules

Correspondences between traditions should be declarative clauses over spans:

```prolog
bridge(cgel_comp_pp, Sent) :-
    cgel:function(Sent, PP, VP, comp), cgel:category(Sent, PP, pp),
    ud:arc(Sent, P, V, obl), same_span(Sent, cgel:PP, ud:P), ...
```

nert-nlp/cgel's UD-to-CGEL converter (`convertor/ud-to-cgel.ini`,
`conll2cgel.py`) and our relabelling (`tools/ilp/cgel/relabel.py`) already
encode such correspondences procedurally. As clauses they become:
* something to check over parallel data: where each holds, where it
  conflicts, what it leaves undecided;
* background knowledge Aleph can use and extend.

## Shared grammars and controls

The second step, once anchoring and bridges exist, is to factor each
tradition's programs into a shared, nondeterministic grammar (the rules over
categories) and, for each sentence, a control that selects its analysis. Three
kinds of control, from most explicit to least:
1. **The derivation as a term.** Rule identifiers as first arguments; first-
   argument indexing makes the parse deterministic. This is the present
   programs without their node names, so it saves repetition and nothing else.
2. **The choices search can't settle.** Close to the information-theoretic
   minimum, but brittle: a change to the grammar or the search invalidates
   every control. For scale, `docs/ambiguity.md` measures the MASC grammar read
   off the treebank at about 10^1.67 trees per word. That's about 5.5 bits per
   word to pick one tree uniformly, some 110 bits for a 20-word sentence.
3. **Constraints that hold of the selected tree** (discriminants):
   ```prolog
   control(S) :- has(S, np, 3-9), has(S, pp-clr, 10-12).
   ```
   A control is right when exactly one parse satisfies it, the same check made
   of the present programs. It survives grammar changes, and its failures show
   where a change matters.

The third kind has a long precedent:
* the Redwoods treebank (Oepen et al. 2002) and DeepBank (Flickinger et al.
  2012) store an HPSG grammar plus each sentence's discriminant decisions, and
  re-apply the decisions when the grammar changes, after Carter's TreeBanker
  (1997);
* nert-nlp's Active DOP fork does the same in miniature: its "dectree" asks
  about labelled spans until one parse remains.

Changes the `--->` machinery needs for shared grammars:
* **Search.** With categories as nonterminals, Prolog's top-down search loops
  on left recursion (NP → NP PP). Kinds 2 and 3 need SWI-Prolog tabling or a
  chart; quadruplet's Go and Kotlin parsers count and sample forests. Unary
  cycles and empty elements make the number of trees infinite unless excluded.
* **Discontinuous pieces.** The expansion ties a daughter's pieces `D@1`,
  `D@2` together by its node name. Shared rules need daughter indices, as
  LCFRS rules have.
* **Co-indexation and fusion.** Gap antecedents, Penn indices and CGEL's
  `fused/3` refer to node names. In a shared grammar they become category
  information: slash categories (`vp/np`), as in GPSG, which is also how
  Pullum and Rogers (2008) expect unbounded dependencies to stay context-free.
  Alternatively, the control supplies the bindings.
* **Lexical ambiguity.** If the lexicon is shared, tag choice is part of the
  ambiguity: about a quarter of it in logs, by `docs/ambiguity.md`.

What this gives the integration:
* **Conversion becomes constrained parsing.** Converting from one tradition to
  another is parsing with the target's shared grammar under constraints from
  the source analysis, carried by the bridge rules.
* **What remains open is the residue.** What the source leaves undecided is
  exactly the remaining discriminants. UD's tree doesn't settle whether a PP
  is CGEL's Comp or Mod, so that is a residual discriminant, decided by a
  person, PropBank evidence, or a learned rule.
* **Conflicts are candidate errors.** Where bridges and a gold analysis can't
  both hold, there's a candidate annotation error.
* **The complement/adjunct problem in its most direct form.** That's the ILP
  work of `tools/ilp`: learning to predict the control decisions an annotator
  makes, and flagging disagreements.

## Plan

1. **Character offsets** in `ptb2pl.py`, `cgel2pl.py`, `ud2pl.py`,
   `clear2pl.py` and odd_one_out's `tiger2pl.py`, and an alignment predicate
   over spans. Test on CGELBank's parallel `.cgel` and `.conllu` and on spaCy's
   parses of the same texts.
2. **Module-qualified loading.** Several analyses of one sentence at once,
   with `analysis/1` and the other transformations per module.
3. **CGEL↔UD bridge rules**, scored over the 257 sentences: coverage,
   conflicts, residue.
4. **A shared grammar and controls for CGELBank**, with tabling: bits of
   control per sentence, and what breaks when the grammar is coarsened. Then
   UD as a constraint source.

Steps 1 and 2 are the foundation and commit to nothing in the later steps.

## References

* Carter, D. 1997. The TreeBanker: a tool for supervised training of parsed
  corpora. *ACL Workshop on Computational Environments for Grammar
  Development and Linguistic Engineering*.
* Flickinger, D., Y. Zhang and V. Kordoni. 2012. DeepBank: a dynamically
  annotated treebank of the Wall Street Journal. *TLT 11*.
* Oepen, S., K. Toutanova, S. Shieber, C. Manning, D. Flickinger and T. Brants.
  2002. The LinGO Redwoods treebank: motivation and preliminary applications.
  *COLING*.
* Pullum, G. K. and J. Rogers. 2008. Expressive power of the syntactic theory
  implicit in *The Cambridge Grammar of the English Language*. LAGB, Essex
  (https://pullum.ppls.ed.ac.uk/EssexLAGB.pdf).
* Reynolds, B., A. Arora and N. Schneider. 2023. Unified syntactic annotation
  of English in the CGEL framework. *LAW-XVII*.
