# English verb frames

The frame analyzer of odd_one_out (`dep2tiger/frames`), for English: each
verb occurrence gets a frame, which is its complements as slots in a
canonical order, and a multiset of modifiers. The analysis of the clause
is CGEL's (Huddleston and Pullum, *The Cambridge Grammar of the English
Language*, chapter 4, "The clause: complements"). A departure from CGEL
has to be argued for; those that stand are listed below, with their
arguments. The goal is corpus evidence
for verb classes (Dowty-style properties such as Volitional), to be combined
with sparse LLM judgments (triads). Treebanks supply earlier human judgment,
and parsers extend it to unlabeled text.

* `src/frames/inventory.py`: the inventory, meaning the symbols, the
  canonical order, and the `Frame`, `Argument` and `Modifier` records.
* `src/frames/gold.py`: gold frames from MASC's Penn trees, by way of
  `tools/masc/verbframes.py`.
* `src/frames/analyze.py`: the same frames from `en_core_web_trf`
  dependencies (ClearNLP labels). A PP counts as a complement by
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
| `n` | subject; after existential *there*, the displaced subject (every frame has `n` or `x`) |
| `x` | dummy subject: existential *there*, or *it* for an extraposed subject |
| `k` | subjective predicative complement (*is happy*, *became president*) |
| `a` | direct object; a passive's surface subject; the object of a catenative verb with an infinitive or small clause (*want him to go*, *persuade him to go*, *make it better*) |
| `d` | indirect object: the first of two NP objects |
| `o` | objective predicative complement (*consider him foolish*) |
| `p` | PP complement, with its preposition: a specified preposition (PP-CLR, -PUT, stranded), a dative *to*/*for* PP, or a particle |
| `i` | nonfinite clause: *to*, bare, *-ing*, *-en* |
| `r` | reflexive object |
| `s-that`, `s-2`, `s-if`, `s-w`, `s-X` | declarative content clause with *that* or with no subordinator; closed interrogative (yes/no, *if*, *whether*); open interrogative (wh); a clause with another subordinator |

From CGEL:
* **The subject is a complement and is in the string.** A copular clause is
  `nk` (complex-intransitive) and a complex-transitive one `nao`.
* **The indirect object is an NP.** *gave Mary books* is `nad`; *gave books
  to Mary* is `nap` (`nap.to`).
* **A particle is a preposition with no object, functioning as a
  complement.** *pick up the book* is `nap.up`, and the lemma is *pick*.
  Verb and particle are an idiom of the lexicon, not a unit of the syntax.
* **Existential *there* is a dummy subject and the NP after the verb the
  displaced subject.** *There is a problem* and *there remain problems*
  are `nx`. An extraposed subject clause stands beside its dummy *it*:
  *it appears that …* is `xs-that`, and *it is clear that …* `xks-that`.
* **Catenatives.** In *want him to go* and *persuade him to go*, *him* is
  the matrix verb's object either way (a raised or an ordinary object).
  Both are `nai`.

The notation is odd_one_out's:
* **Numbered repeats.** A second argument of one type is numbered (`a2`,
  `p2`), and the string spells the symbol again (`naa`).
* **Clause slots last.** The `s-` slots come last in the string and are
  named once (`nas-that`).

`Frame.refined()` adds the prepositions and particles of the `p`s, sorted
(`np.on`, `nap.up`).

## Departures from CGEL

With their arguments, or marked open where there is none yet.

* **Frames describe the verb, not its clause.** CGEL's passive clause has
  its own subject, and the *by* phrase is not its subject. Here a passive's
  surface subject is `a` and its agent, named or not, is `n`.
  * *Argument:* the frames are evidence about the verb's argument
    structure, so they should agree across voice. CGEL relates passive and
    active as alternative packagings of the same content (chapter 16). This
    is odd_one_out's treatment too.
  * Supplying a controlled infinitive's subject and an imperative's
    addressee is not a departure: CGEL gives both an understood subject.
    `Argument.source` records where each filler comes from: overt, trace,
    controlled, agent, addressee or unsaid.
* **Auxiliaries and modals are modifiers**, not verbs heading the clause
  with catenative complements as in CGEL.
  * *Argument:* the event view, under which they modify the event the
    lexical verb describes. The frame is the lexical verb's.
* **`r`, the reflexive object, is open.** CGEL has no reflexive
  complement: *availed themselves of* has an object. `r` comes from the
  German inventory, where reflexives are distinct.
  * *For keeping it:* inherent reflexives (*pride oneself on*, *avail
    oneself of*) are lexically marked.
  * *Against:* nothing else in English syntax singles them out.
* **`s-2` beside `s-that` is open.** In CGEL (chapter 11) *that* is an
  optional subordinator of one declarative content clause, which would make
  these a single slot. `s-2` also holds quotations.
* **Complements the treebank calls adjuncts are open.** CGEL counts goal
  and locative PPs licensed by the verb (*went to London*, *lives in
  Paris*) as complements. MASC mostly tags them DIR or LOC, so `p` is
  undercounted by CGEL's standard. This needs per-verb licensing evidence.

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

There are 71,218 verbs and 119 frames (385 with prepositions). The
commonest:

| frame | share |
|---|---|
| `na` | 36.4% |
| `nk` | 16.5% |
| `n` | 15.2% |
| `np` | 6.1% |
| `ni` | 5.0% |
| `nap` | 4.9% |
| `ns-2` | 3.5% |
| `nai` | 2.7% |
| `ns-that` | 2.0% |
| `nao` | 1.3% |
| `nx` | 1.2% |
| `ns-w` | 1.0% |
| `nad` | 0.9% |

## spaCy against the gold, greedy parses

```bash
uv run python -m frames.parse $S/v3/verbs.jsonl $S/v3/masc-greedy.spacy     # 14 minutes, 4 CPUs
uv run python -m frames.evaluate $S/v3/gold.jsonl $S/v3/masc-greedy.spacy
```

The test set is the held-out fifth of the documents: 19,712 gold verbs.
The PP prior is read off the other four fifths.

* **Verbs found:** P 96.0, R 98.8.
* **Exact frame:** 80.7% of the verbs both sides find. The lemma is right
  for 99.0%.
* **By symbol (F):**

  | symbol | F |
  |---|---|
  | n | 99.6 |
  | a | 93.2 |
  | k | 92.9 |
  | i | 90.0 |
  | r | 90.0 |
  | s-that | 84.4 |
  | d | 84.3 |
  | x | 82.6 |
  | s-if | 79.3 |
  | p | 75.8 (P 81.4, R 70.9) |
  | s-w | 66.8 |
  | s-2 | 65.8 |
  | o | 60.9 |

* **Modifier multiset:** P 77.6, R 83.8.
* **Per lemma:** for 144 lemmas with at least 20 held-out occurrences, the
  mean total variation distance between the gold's and spaCy's frame
  distributions is 0.139. Between two halves of the gold it is 0.158, but
  each half has half the occurrences. At the full count, sampling noise
  would be about 0.11, so spaCy adds a little on top of noise.

The commonest errors (gold → spaCy):
* **Missed objects, `na → n` (406).** Gaps spaCy has no token for:
  relative clauses through an infinitive (*the pain I was beginning to
  experience*), and *get X covered*.
* **PP complements taken for modifiers, `np → n` (326) and `nap → na`
  (206), and the reverse, `na → nap` (124).** The prior's threshold is a
  trade-off here; the score is kept for adjudication.
* **A neighbouring clause taken for a complement, `na → nas-2` (135) and
  `nk → nks-2` (122).** spaCy attaches a run-on or adjacent clause as a
  `ccomp` (*The measurement is not : Are we safer ?*).

Before the CGEL changes, exact frames were 82.6%. The drop is mostly
errors the old bare `k` string hid, since any predicative frame then
matched any other. Also, a particle now counts in the frame, not the
lemma.

## Known conflations

* **`i` with `a`** does not tell raising to object (*want him to go*) from
  object control (*persuade him to go*). As in CGEL, and odd_one_out, the
  object is `a` either way. The distinction can still be recovered from
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
