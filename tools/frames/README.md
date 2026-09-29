# English verb frames

The frame analyzer of odd_one_out (`dep2tiger/frames`), for English: each
verb occurrence gets a frame, which is its complements as slots in a
canonical order, and a multiset of modifiers. The goal is corpus evidence
for verb classes (Dowty-style properties such as Volitional), to be combined
with sparse LLM judgments (triads). Treebanks supply earlier human judgment,
and parsers extend it to unlabeled text.

* `src/frames/inventory.py`: the inventory, meaning the symbols, the
  canonical order, and the `Frame`, `Argument` and `Modifier` records.
* `src/frames/gold.py`: gold frames from MASC's Penn trees, by way of
  `tools/masc/verbframes.py`.
* `src/frames/analyze.py`: the same frames from `en_core_web_trf`
  dependencies (ClearNLP labels). A PP counts as a prepositional object by
  the treebank's rate for its verb and preposition, which is a prior, and
  the score is kept.
* `src/frames/parse.py`: parses MASC on MASC's own tokens, so that verbs
  align by word position.
* `src/frames/evaluate.py`: the analyzer against the gold frames, on
  held-out documents.
* **Not done yet:** n-best parses through odd_one_out's
  `spacy_beam_docbin`.

## The inventory

| symbol | argument |
|---|---|
| `n` | subject (every frame has `n`, `x` or `k`) |
| `x` | expletive subject: *there*, or *it* for an extraposed clause |
| `k` | predicative of a copula-like verb; the frame's string is then `k` |
| `a` | direct object; a passive's surface subject; the subject of an infinitive or small clause complement (*want him to go*, *make it better*) |
| `d` | indirect object: the first of two NPs, or a dative *to*/*for* PP |
| `o` | object predicative, or a verbless small clause (*consider him foolish*) |
| `p` | prepositional object (PP-CLR, -PUT, stranded), with its preposition |
| `i` | nonfinite clause: *to*, bare, *-ing*, *-en* |
| `r` | reflexive object |
| `s-that`, `s-2`, `s-if`, `s-w`, `s-X` | finite clause with *that*, with no complementizer, a yes/no question or *if*/*whether* clause, a wh-clause, another complementizer |

The conventions are odd_one_out's:
* **Numbered repeats.** A second argument of one type is numbered (`a2`,
  `p2`), and the string spells the symbol again (`naa`).
* **Clause slots last.** The `s-` slots come last in the string and are
  named once (`nas-that`).
* **Frames describe the verb, not its clause.** A passive's surface subject
  is `a`, and its agent, named or not, is `n`. An infinitive's controlled
  subject, and an imperative's addressee, are `n` too. `Argument.source`
  records where each filler comes from: overt, trace, controlled, agent,
  addressee or unsaid.
* **Particles join the lemma** (*pick_up*).

`Frame.refined()` adds the prepositions of `p` and `d` (`np.on`).

A modifier is recorded with its kind and, where it has them, its
preposition, complementizer or auxiliary and its head. The kinds are aux,
neg, pp, adv, clause, np and adj. The treebank's function tag is kept as
well, but a parser does not supply one. The multiset key (`Modifier.key`)
is the kind plus its marker. An adverb gets a key of its own name only if
it is on the list of diagnostics (`inventory.DIAGNOSTIC`: *deliberately*,
*carefully*, *again* and so on). Scope-bearing modifiers are not told apart
from the others.

## Gold frames on MASC

```bash
python3 tools/masc/verbframes.py $MASC/data $S/v3          # verbs.jsonl
cd tools/frames && uv run python -m frames.gold $S/v3/verbs.jsonl $S/v3/gold.jsonl
uv run --extra test pytest
```

There are 71,218 verbs and 72 frames (216 with prepositions). The
commonest:

| frame | share |
|---|---|
| `na` | 38.1% |
| `k` | 18.6% |
| `n` | 16.9% |
| `ni` | 5.0% |
| `np` | 4.5% |
| `ns-2` | 3.5% |
| `nap` | 2.9% |
| `nai` | 2.7% |
| `ns-that` | 2.0% |
| `nad` | 1.3% |
| `nao` | 1.3% |
| `ns-w` | 1.1% |

## spaCy against the gold, greedy parses

```bash
uv run python -m frames.parse $S/v3/verbs.jsonl $S/v3/masc-greedy.spacy     # 14 minutes, 4 CPUs
uv run python -m frames.evaluate $S/v3/gold.jsonl $S/v3/masc-greedy.spacy
```

The test set is the held-out fifth of the documents: 19,712 gold verbs.
The PP prior is read off the other four fifths.

* **Verbs found:** P 96.0, R 98.8.
* **Exact frame:** 82.6% of the verbs both sides find. The lemma, with its
  particle, is right for 98.0%.
* **By symbol (F):**

  | symbol | F |
  |---|---|
  | n | 99.6 |
  | a | 93.2 |
  | k | 93.3 |
  | i | 90.0 |
  | r | 90.0 |
  | s-that | 84.4 |
  | x | 82.6 |
  | s-if | 79.3 |
  | d | 77.4 |
  | p | 67.7 (P 75.5, R 61.3) |
  | s-w | 66.8 |
  | s-2 | 65.8 |
  | o | 60.9 |

* **Modifier multiset:** P 76.9, R 83.9.
* **Per lemma:** for 141 lemmas with at least 20 held-out occurrences, the
  mean total variation distance between the gold's and spaCy's frame
  distributions is 0.139. Between two halves of the gold it is 0.145, but
  each half has half the occurrences. At the full count, sampling noise
  would be about 0.10, so spaCy adds a little on top of noise.

The commonest errors (gold → spaCy):
* **Missed objects, `na → n` (436).** Gaps spaCy has no token for: relative
  clauses through an infinitive (*the pain I was beginning to
  experience*), and *get X covered*.
* **PP objects taken for modifiers, `np → n` (314), and the reverse,
  `na → nap` (110).** The prior's threshold is a trade-off here; the score
  is kept for adjudication.
* **Finite clauses where the gold has none, `na → nas-2` (147).**

## Known conflations

* **`s-2`** holds both reported clauses without *that* (*I think it works*)
  and quotations.
* **`i` with `a`** does not tell raising to object (*want him to go*) from
  object control (*persuade him to go*). As in odd_one_out, the accusative
  is `a` either way. The distinction can still be recovered from
  `Argument.label`:

  | | raising (MASC: S with an overt subject) | object control (MASC: NP + S with a co-indexed `*PRO*`) |
  |---|---|---|
  | gold `label` | `S:subject` | the NP's own label (`NP`, `NP-1`) |
  | spaCy `label` | `ccomp:subject` | `dobj` |
  | spaCy structure | `ccomp` with its own `nsubj`: 246 of 276 | `dobj` + `xcomp`: 281 of 354 |

  MASC annotates *like*, *allow*, *help* and *enable* both ways. spaCy
  parses *like* and *allow* uniformly as raising.
* **Gerund objects** (*enjoy swimming*) are `i`. Free relatives (*get what
  they want*) are `a`.
