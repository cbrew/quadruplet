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
| 2 | the verb decomposition in `cmd/framelex`: regions by depth of verb phrases above (outside any verb, top-layer verbs, embedded verbs, their dependents), choices split into kind / complement frame / the rest / spans; with and without the frame lexicon | Opus | in progress |
| 3 | run `cmd/framelex`'s lexicon filter tables on 300 sentences (remove its joint-choices code first, which runs out of memory), and write the results into `docs/verbs/07-frame-lexicon.md` | Opus | in progress |
| 4 | a bottom-up state for task 2: split "embedded" into verbs that dominate further verbs and the bottom layer (a bit per item: some lexical verb phrase below), which needs insides split by the bit | open | after 1 |
| 5 | the "don't care" share: group each choice's hyperedges by what they contribute to `interp.Flat`'s meaning (the relations they create), so that H(choice) = H(meaning-visible part) + H(don't care); a local approximation, say so | open | after 1 |
| 6 | the same decompositions with trees weighted by the treebank's rule frequencies (a PCFG) instead of uniformly | open | after 1 |

## Requests

(none yet)

## Done

* `tools/masc/verbframes.py` fixed (commit 6f02633); the reports' version is
  `tools/masc/verbs/verbframes_v1.py`.
