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
* The spaCy analyzer is next: `en_core_web_trf` dependencies, n-best parses
  through odd_one_out's `spacy_beam_docbin`, evaluated against the gold
  frames.

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

## Known conflations

* **`s-2`** holds both reported clauses without *that* (*I think it works*)
  and quotations.
* **`i` with `a`** does not tell raising to object (*want him to go*) from
  object control (*persuade him to go*). As in odd_one_out, the accusative
  is `a` either way.
* **Gerund objects** (*enjoy swimming*) are `i`. Free relatives (*get what
  they want*) are `a`.
