# Handover: quadruplet, October 2026

For an LLM assistant picking up this work. Read this, then the documents it
points to. Branch: `claude/intelligent-fermi-py555m`. A companion private
repository, `cbrew/cgel_examples`, has its own `HANDOVER.md`.

## The person and how they work

The user is an academic computer scientist (NLP, neural networks,
knowledge-based systems), at home in Python, Go and OCaml.
* **Plain, accurate prose.** No model identifiers in commits or files, except
  where they ask for attribution by name.
* **Read sources before describing them.** An earlier session named the
  CGELBank manual "v1.0" from a citation, when the document is v1.2, and it
  cost trust. Check versions, counts and claims in the primary source.
* **Report what was measured and what wasn't.** Distinguish both from
  conjecture.
* **Never ask for secrets in chat.** Use `OPENAI_API_KEY` or other LLM keys
  only with explicit permission for the task at hand.

## What this repository is

A chart parser for feature grammars, in Kotlin and Go (`README.md`), and,
growing beside it, tools over the MASC corpus and CGELBank for learning the
complement/adjunct distinction.

### ILP: complements against adjuncts (`tools/ilp`)

Aleph learns which of a verb's PPs are complements. It learns from MASC, and
from MASC relabelled by CGEL's tests (`tools/ilp/cgel/relabel.py`, versions
1–3). CGELBank's gold trees are kept for evaluation only. Read:
* `tools/ilp/README.md`, the ledger of Aleph runs 1–5;
* `tools/ilp/cgel/README.md`: the CGEL tests, the cited prepositional-verb
  lexicon of CGEL ch. 4 §6.1.2 (`prepositional_verbs.tsv`, 187 pairs), the
  PropBank comparison, semantic roles (`role/2`), and the scores of every
  theory on CGELBank.

The learned theories are in `tools/ilp/theories/`.

### Trees as Prolog programs (`tools/masc/prolog`, `tools/prolog`)

* **The form.** A sentence is a small program in the `--->` notation, after
  odd_one_out's TIGER programs: a grammar with one rule per constituent and
  one derivation.
* **Penn trees.** `tools/masc/prolog` does MASC's Penn trees.
* **New this session, `tools/prolog`:**
  * `cgel2pl.py` (CGELBank), `ud2pl.py` (CoNLL-U) and `clear2pl.py` (spaCy
    `en_core_web_trf`, ClearNLP-style labels), with rules files `cgel.pl` and
    `dep.pl`;
  * all three load `ptb.pl` through two hooks added to it, `displacement/1`
    and `extra_fact/1`;
  * checked: the CGEL reader agrees node for node with nert-nlp/cgel's
    `cgel.py` on all 257 trees, and every program has one analysis before and
    after reattachment; 2,101 UD trees round-trip exactly through `arcs/1`;
  * `tools/prolog/README.md` has the details;
  * tests: `python3 -m unittest tools/prolog/test_prolog.py tools/masc/prolog/test_ptb2pl.py`.

## The direction: a common framework

The user wants one Prolog framework that integrates information from all the
notations and traditions: Penn/MASC with PropBank and FrameNet, CGELBank, UD,
spaCy, TIGER, VerbNet. The design is in `docs/prolog-framework.md`:
* keep each notation native, and anchor everything to character offsets;
* load each tradition into its own module;
* relate traditions by declarative bridge rules over spans;
* later, factor each tradition into a shared nondeterministic grammar plus a
  per-sentence control: discriminants, as in Redwoods.

**Next steps, agreed in outline and not started:**
1. character offsets in all converters, and a span-alignment predicate;
2. module-qualified loading;
3. CGEL↔UD bridge rules, scored on CGELBank's 257 parallel sentences;
4. a shared CGELBank grammar with controls.

Start with 1 and 2 on CGELBank (`.cgel` + `.conllu` + spaCy on the same
texts).

## Findings about CGELBank (October 2026)

* **The manual (v1.2, arXiv 2305.17347).**
  * §2.5.2 "Modifier vs. Complement" covers only pre-head dependents of nouns,
    which CGELBank, unlike CGEL, makes modifiers. There is no operational
    guidance for PPs in clause structure.
  * §2.5.1 gives a Modifier vs. Supplement default.
  * Function and VP layering are one decision: a modifier is binary with a
    head, and internal complements share the lowest VP level. So any feature
    read off a CGEL tree's structure leaks the label.
* **Consistency** (`tools/ilp/cgel/pp_consistency.py`):
  * the trees obey the manual's branching rules;
  * too few verb+preposition pairs repeat to test lexical consistency;
  * in the agreement study, 10 of 28 VP PPs found in all three versions were
    Comp vs Mod disagreements between the annotators;
  * the adjudications mostly follow CGEL's semantic criteria;
  * two look inconsistent: *join up* (Mod, against *coming in* as Comp and
    *cleaned up* as Particle), and *differ … in the fact that* (Mod).
* **Their annotation pipeline** (nschneid/activedop, a fork of Active DOP on
  disco-dop):
  * the parser fuses category and function into one label (`PP-Mod`), learned
    from 331 trees, with no Comp/Mod heuristics;
  * the "dectree" asks annotators about labelled spans;
  * their UD-to-CGEL converter maps `obl`+PP to Mod and `nmod`+PP to Comp,
    with a `TODO: obl` comment.
* **The book's examples.** nert-nlp/cgel's `all-examples` holds the book's
  examples for ch. 1–17, as hand-extracted files aligned with the printed PDF,
  with page numbers. See the cgel_examples handover.
* **Pullum and Rogers (2008, Essex LAGB):** a conditional result. If CGEL
  structures satisfy three properties and its statements are MSO-expressible,
  the theory interprets faithfully into MSO on trees, and the string yield is
  context-free. CGELBank's format (one stored edge for fused nodes) is their
  reachability-preserving spanning tree.

**ILP denoising ideas, not yet run:**
* check learned theories against the 10 agreement-study disputes, where the
  adjudicated label is known;
* use Aleph's noise settings to list exceptions as candidate errors;
* better pre-annotation for their converter;
* reranking or answering dectree discriminants;
* negative evidence from the book's starred examples.

Features must come from outside the tree (UD, PropBank, lexicon), not from
CGEL structure.

## Environment notes

* **Containers are ephemeral; re-clone as needed:**
  * `git clone https://github.com/nert-nlp/cgel ~/nert-nlp/cgel` (CGELBank;
    its scripts need `uv run --with-requirements requirements.txt`);
  * `git clone https://github.com/nschneid/activedop ~/nschneid/activedop`;
  * UD English EWT for the round-trip checks.
* **SWI-Prolog 9** is used. Run it under `LANG=C.UTF-8`: some CGELBank
  sentence ids aren't ASCII.
* **spaCy and `en_core_web_trf`** are in `tools/frames/.venv`. Run
  `clear2pl.py` with that Python.
* **Licences:** CGELBank is CC BY 4.0 and UD EWT is CC BY-SA 4.0. The example
  programs in `tools/prolog/examples` say so.
