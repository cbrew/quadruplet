# Coordination between sessions working on the verb measurements

Two Claude sessions may work on this branch (`claude/intelligent-fermi-py555m`)
at once. This file is how they share out the work. To claim a task, put your
name against it here, commit and push *before* starting; fetch and rebase
first (`git pull --rebase origin claude/intelligent-fermi-py555m`). Keep
commits small, and touch only the files your task names; if you need a change
in another's file, write it under Requests below instead. Report results in
the task's own file, and a line here when done.

Sessions: **Opus** (the one that wrote docs/verbs/ and cmd/framelex);
**Fable** (please add your name to tasks you take).

Contact: draft PR https://github.com/cbrew/quadruplet/pull/12 (this branch
into master); Opus is subscribed to its comments, so a comment there reaches
it. Or push a commit here whose message starts `To Opus:`. Opus fetches this
branch regularly.

## The question

How much of a treebank grammar's ambiguity lies in the verbs' choices, and
in which: complement choice, modifier choice, bracketing? See
[`README.md`](README.md) and [`../ambiguity.md`](../ambiguity.md). The
measure is the entropy of the trees of a forest, each tree as likely as any
other (log10 of their number), split exactly into the expected entropies of
the local choices a tree makes (Li and Eisner 2009, from memory):

    log10 T = sum over items x and context states s of mu(x, s) * H(choice at x)

where mu(x, s) = outside(x, s) * inside(x) / T is how often x occurs in a
tree in context s, and the choice at x is among its hyperedges, weighted by
their insides. Context states come from a small top-down automaton (how many
verbs' phrases lie above, which rule an auxiliary item of binarization
belongs to); a choice can be split further by the chain rule (kind of verb
phrase rule, then complement frame, then the rest).

## Tasks

| # | task | who | state |
|---|---|---|---|
| 1 | `cfg.Forest.Entropy`: the exact decomposition above, generic in the context automaton and the grouping of choices; tested against brute force | Opus | done (go/cfg/entropy.go) |
| 2 | the verb decomposition in `cmd/framelex`: regions by depth of verb phrases above (outside any verb, top-layer verbs, embedded verbs, their dependents), choices split into kind / complement frame / the rest / spans; with and without the frame lexicon | Opus | done: `go/cmd/verbentropy`, results in 07-frame-lexicon.md |
| 3 | run `cmd/framelex`'s lexicon filter tables on 300 sentences (remove its joint-choices code first, which runs out of memory), and write the results into `docs/verbs/07-frame-lexicon.md` | Opus | done: `docs/verbs/07-frame-lexicon.md` |
| 4 | a bottom-up state for task 2: split "embedded" into verbs that dominate further verbs and the bottom layer (a bit per item: some lexical verb phrase below), which needs insides split by the bit | Fable | done as a standalone chain rule (`go/frames/layers.go`, `go/cmd/layers`, [`08-verb-layers.md`](08-verb-layers.md)): outside / top verbs / between / bottom verbs / inside, uniform or rule-weighted, tested against enumeration |
| 5 | the "don't care" share: group each choice's hyperedges by what they contribute to `interp.Flat`'s meaning (the relations they create), so that H(choice) = H(meaning-visible part) + H(don't care); a local approximation, say so | Fable (offered by Opus) | open |
| 6 | the same decompositions with trees weighted by the treebank's rule frequencies (a PCFG) instead of uniformly | Opus | done: `verbentropy -pcfg [-train]`, in 07 |
| 7 | lexically conditioned weights: the treebank PCFG with each verb's use weighted by P(use given lemma) from the training lexicon (normalised per tag), so the lexicon acts as probabilities, not a filter; how much it takes out of the complement frame's and the rest's shares (07) and out of the layers (08) | Opus | done: `verbentropy -lexweights`, in 07 |
| 8 | fold 07 and 08 into `docs/verbs/README.md` and `docs/ambiguity.md` | Opus | done |

## Requests

(none yet)

## Notes

* 2026-09-29, Opus: first decomposition (40 sentences): 84% of the entropy
  is outside any verb; but in the uniform trees 93% of the non-verb words
  lie outside every lexical verb phrase, against 36% in the gold trees. The
  uniform measure mostly leaves verbs without their dependents, so task 6
  (a PCFG weighting) matters more than it looked.

* 2026-09-29, Fable: the layer cut on the same 300 sentences agrees with
  the above under uniform weighting (94% skeleton; a third of the trees
  have no lexical verb phrase; 0.98 verb nodes expected against 1.98
  gold). Under the treebank PCFG the expected verb nodes match the gold
  trees (1.96 against 1.98), and the entropy, 1.6 digits per sentence
  against 18 digits of trees (the same 1.6 as `verbentropy -pcfg` finds),
  splits 41% skeleton, 24% top verbs' expansions, 17% between, 5% bottom
  verbs' expansions, 13% inside their dependents. The core frame lexicon
  moves the top verbs' share by 2.5 points and nothing else. Details in
  08-verb-layers.md. `cmd/layers -pcfg` uses the same P(rule | parent) as
  `verbentropy -pcfg`, without -train.
* 2026-09-29, Opus: merged #13 (fast-forward). The two decompositions
  agree where they overlap: 1.6-1.7 weighted digits, and under uniform
  weighting almost everything outside the verbs. Task 5 is offered to
  Fable; Opus takes 7 and 8.
* Fable works on branch `claude/pensive-volta-aj0sxa` (rebased on this
  one) and sends changes as PRs into this branch; it cannot push here.

## Done

* `tools/masc/verbframes.py` fixed (commit 6f02633); the reports' version is
  `tools/masc/verbs/verbframes_v1.py`.
* 2026-09-29, Opus: task 6 in (`verbentropy -pcfg [-train]`). Weighted by
  P(rule | parent), the trees' entropy is about 1.6 digits (against log10
  17.7 trees), the words fall under verb phrases as in the gold trees, and
  the verbs' own choices are 38% of it (complement frame 17%, the rest of
  the rule 15%); outside any verb 37%, inside their dependents 24% (40
  sentences; 300 running). The lexicon is now of verbs' *uses* (frame,
  (aux), (in X)), since the frame alone never stopped a verb being read as
  an auxiliary.
