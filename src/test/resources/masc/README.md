# MASC grammar v0

A grammar with an extensional Montague semantics and quantificational event
semantics, and 299 real sentences to parse with it, for testing and timing
the parser on something bigger than a toy. This is version 0 of the
grammar: it parses 284 of the sentences (95%), and the [known
failures](#known-failures) are listed below. A second, [held-out
sample](#held-out-sample) of 299 sentences measures how well it
generalises.

| file | contents |
|---|---|
| `masc.fcfg` | the grammar: 113 hand-written rules, then a lexicon of 1,136 words and phrases generated from the sample |
| `sample.txt` | the sentences: id, genre, and the words to parse, tab-separated |
| `sample.mrg` | their Penn Treebank trees as MASC has them |
| `gold.txt` | the same trees normalised: no punctuation, traces or function tags |
| `readings.txt` | the correctness suite: 61 sentences with their readings, checked by hand |
| `examples.txt` | 12 sentences from outside the sample, with their readings, checked by hand |
| `heldout.txt`, `heldout.mrg`, `heldout-gold.txt` | the held-out sample, in the same formats as the development sample |
| `masc-heldout.fcfg` | generated: the rules of `masc.fcfg` with a lexicon from the held-out trees |

## The sentences

The sentences are from the Penn Treebank annotation of MASC, the Manually
Annotated Sub-Corpus of the Open American National Corpus:

> Nancy Ide, Collin Baker, Christiane Fellbaum and Rebecca Passonneau (2010).
> The Manually Annotated Sub-Corpus: A Community Resource For and By the
> People. *Proceedings of ACL 2010*, 68–73. <http://anc.org/data/masc/>

MASC is distributed under a Creative Commons Attribution license; the
sentences and trees here are a small sample of it, otherwise unchanged.

`tools/masc/sample.py` chose them. It takes sentences of 4 to 15 words,
leaving out punctuation, whose trees use only common constructions (listed
in the script): no fragments, interjections, parentheticals, numbers or
foreign words. That leaves 4,328 of MASC's 34,586 sentences, from 23 of
its genres. From these it takes 13 sentences from each genre, at random
with a fixed seed: the development sample, which the grammar was written
by looking at.
Words are lower-cased unless tagged NNP; PTB tokenisation (`do n't`,
`John 's`) is kept.

## The grammar

The rules, at the top of `masc.fcfg`, are written by hand for these
sentences. The notes there give the semantic types and features.

[`docs/semantics.md`](../../../../docs/semantics.md) explains the ideas
behind the grammar, and what it does and doesn't take from each. In
brief, the semantics is Montague's, extensional and without quantifying-in,
with neo-Davidsonian events, following
Champollion's quantificational event semantics (*The interaction of
compositional semantics and event semantics*, Linguistics and Philosophy
38, 2015), and in the spirit of the Parallel Meaning Bank (Johan Bos, *The
Sequence Notation: Catching Complex Meanings in Simple Graphs*, IWCS 2023).
A verb says there is an event, with thematic roles (Agent, Experiencer,
Theme, Recipient, Patient, Topic) for its arguments. A verb phrase takes
its subject and then a condition on its event:

    walks            \x F.exists e.(walk(e) & Agent(e, x) & F(e))

so the event quantifier takes scope below the verb's arguments, negation
and modals. PPs, manner, place and time adverbs, particles and purpose clauses
are predicates of the event; other adverbs, negation, auxiliaries and
subordinating conjunctions are operators on propositions. A sentence is
closed with `\e.true`, which conjunction drops. Predicates are lemmas,
not WordNet senses. "I rapped my fingers against my desk nervously"
has, among its two readings,

    ∃x1.(finger(x1) ∧ of(x1, speaker) ∧ ∃x2.(Agent(x2, speaker) ∧ Theme(x2, x1) ∧
         nervously(x2) ∧ rap(x2) ∧ ∃x3.(against(x2, x3) ∧ desk(x3) ∧ of(x3, speaker))))

The rules cover:

* declaratives, imperatives, yes/no and wh-questions, and subjectless
  diary-style tweets
* determiners and generalised quantifiers, with a Russellian "the";
  possessives; partitives ("some of the analysis"); pronouns and names
* adjectives, noun compounds, and PPs attached to nouns, verb phrases or
  sentences
* auxiliaries, modals, negation, VP ellipsis ("they always have"), the
  copula, existential "there", passives with and without an agent
* verbs taking objects, two objects, clauses, embedded questions, an
  object and a clause or question ("asked him what he thought"), and
  infinitival or participial VPs, including control and ECM verbs
* relative clauses (subject, object and contact), free relatives,
  infinitival and reduced relatives
* coordination of sentences, VPs, NPs, nominals and adjectives, and
  subordinate, purpose and participial adjuncts
* headlines ("Mac OS X updating")

Gaps use slash categories (`VPgap`, `Sgap`, ...), whose meanings abstract over
the gap. There is no quantifier storage, so an object quantifier always
takes narrow scope, and all ambiguity is structural or lexical.

The lexicon is generated by `tools/masc/lexicon.py` from `sample.mrg`.
Closed-class words (pronouns, determiners, auxiliaries, wh-words and so on)
come from tables in the script. Open-class words get a semantic template
for their part of speech. Each verb gets every subcategorisation frame its
lemma has in the sample, read from the complements beside it in the tree
(traces included, so gapped objects and passives count), and a thematic
role for its subject: Experiencer for verbs of perception and attitude,
Agent otherwise. Two tagging slips are repaired: a lone adverb of place
between a verb and its object is also a particle ("push back the tide"),
and a past tense in the complement of "be" is also a participle ("was n't
identified"). Only the word forms the sample has are in the lexicon, as
the parts of speech it has them as: "notice", for one, is only a noun and a
present tense.
The script rewrites the part of `masc.fcfg` after the "generated lexicon"
line:

```bash
pip install lemminflect
cd tools/masc && python3 lexicon.py ../../src/test/resources/masc
```

## Results

284 of the 299 sentences (95%) get at least one reading. The grammar was
written by looking at the sample, so this is not a measure of coverage on
unseen text. Of the multi-word treebank phrases in those 284 sentences,
96.5% are spanned by an edge in some parse.

The 299 charts have 161,954 edges and 1,798 trees. The most ambiguous
sentence, "never mind that the Taliban continued selling opium in spite of
the deal", has 46 readings.

On 4 cores:

| | Kotlin | Go, agenda | Go, 4 workers | Go, 4 workers, `GOGC=400` |
|---|---|---|---|---|
| whole sample | 0.90 s | 1.44 s | 0.83 s | 0.58 s |

```bash
mvn test -Dtest=MascTest -Dmasc.bench=5            # Kotlin timing
cd go && go test ./chart -run MascReport -v        # coverage report
cd go && go test ./chart -run X -bench Masc        # Go timing
```

## Held-out sample

`heldout.txt` is 299 more sentences, drawn by `tools/masc/sample.py` with a
second seed from the 4,029 that pass the same filter and are not in the
development sample. It takes 13 from each genre that has that many left and
the rest at random from the whole remaining pool, so it covers 21 genres:
telephone and wsj had too few sentences left. Nobody has looked at these
sentences while writing the grammar, and nobody should: they are for
measuring, not tuning.

The lexicon is generated from the development sample, so on the held-out
sentences the grammar mostly measures vocabulary. The same generator run on
the held-out trees gives a lexicon with the held-out words, using the
treebank's parts of speech and verb frames, and so measures whether the
hand-written rules generalise:

| | sentences parsed | treebank phrases spanned |
|---|---|---|
| development sample, grammar v0 | 284 of 299 (95.0%) | 96.5% |
| held-out sample, grammar v0 | 10 of 299 (3.3%) | 97.5% |
| held-out sample, held-out lexicon (`masc-heldout.fcfg`) | 278 of 299 (93.0%) | 95.8% |

Only 21 held-out sentences have every word in the v0 lexicon, and 884 of
their 2,511 words (35%) are not in it. With a lexicon for them, the rules
parse 93% of the held-out sentences, two points fewer than the development
sentences they were written for. Both samples come through the same filter,
though, so this says the rules generalise to sentences of the kind the
filter lets through, not to English at large, and the held-out lexicon
uses gold parts of speech.

`masc-heldout.fcfg` is generated, and a test checks that its rules are the
same as `masc.fcfg`'s. After changing the grammar, regenerate it:

```bash
(cd tools/masc && python3 lexicon.py ../../src/test/resources/masc heldout.mrg masc-heldout.fcfg)
(cd go && go test ./chart -run MascHeldoutReport -v)     # the figures above
```

## Known failures

These 15 sentences get no reading. Most need a construction v0 lacks; the
sample's filter lets a verb phrase have any sequence of complements, so
these slipped through it.

| sentence | why |
|---|---|
| we will deliver to your office tomorrow *detailed legislative language* | object after a PP and an adverb (heavy NP shift) |
| it stabilizes and controls forms because it brings to light *the contours of the space* | the same |
| we do need to get *something done* in October | small clause after "get" |
| they were never declared *legal currency* | passive with a predicate NP left over |
| I had no idea *what was in store for me* | a question as the complement of a noun |
| … *although designed by* an architect with a classical sensibility | adverbial clause without a subject |
| *when pickled* bitter melon makes a savory condiment | the same |
| several palaces were built and a water system *installed* | gapping: the second "were" is missing |
| what would any of you be willing to do | the gap is inside "willing to do": no gap rules for adjective phrases or the copula |
| the newsboy just ignored him and went *on* calling out read all about it | a particle before a VP complement, and a quoted imperative as a complement |
| it 's tough growing up today | extraposed subject |
| give me my grammatical games *any day* to a crossword puzzle | a bare NP as an adverb |
| you look like hell *Tar* | a vocative |
| *faculty protest* against apartheid at Cornell | "faculty" is tagged singular and "protest" plural, and the grammar checks agreement |
| this is the reason I make contact with you to help me *received* the money | ungrammatical (spam): a past tense after "help" |

## Examples

`examples.txt` has sentences outside the sample, made from the lexicon's
words, with every reading the grammar gives them. The intended reading is
there, sometimes among others:

    every architect who knows Dave found a house

    ∀x1.((architect(x1) ∧ ∃x2.(Experiencer(x2, x1) ∧ Theme(x2, dave) ∧ know(x2)))
         → ∃x2.(house(x2) ∧ ∃x3.(Agent(x3, x1) ∧ Theme(x3, x2) ∧ find(x3))))

    Dave thought that no customer noticed the change

    ∃x1.(Experiencer(x1, dave) ∧
         Topic(x1, ¬∃x2.(customer(x2) ∧ ∃x3.(change(x3) ∧ ∀x4.(change(x4) → (x3 = x4)) ∧
                         ∃x4.(Experiencer(x4, x2) ∧ Theme(x4, x3) ∧ notice(x4))))) ∧
         think(x1))

    where did Dave find the book

    λv1.∃x1.(book(x1) ∧ ∀x2.(book(x2) → (x1 = x2)) ∧
             ∃x2.(Agent(x2, dave) ∧ Location(x2, v1) ∧ Theme(x2, x1) ∧ find(x2)))

"the company expected every customer to find a house" has three readings.
The intended one is

    ∃x1.(company(x1) ∧ ∀x2.(company(x2) → (x1 = x2)) ∧
         ∀x2.(customer(x2) → ∃x3.(Experiencer(x3, x1) ∧ Patient(x3, x2) ∧
              Theme(x3, ∃x4.(house(x4) ∧ ∃x5.(Agent(x5, x2) ∧ Theme(x5, x4) ∧ find(x5)))) ∧
              expect(x3))))

and the others read "to find a house" as an infinitival relative on
"customer" (customers for finding houses) or as the company's purpose.
"the book was found by a guy in the house" has four: "by a guy" is the
agent or an adjunct, and "in the house" modifies the guy or the finding.

The examples are limited by the lexicon: "did every customer notice the
change" gets no reading, because the sample has "notice" only as a
present tense.

## Testing

`go/testdata/golden/masc.golden` records each sentence's chart size, tree
count and readings as the Kotlin implementation produces them, and the Go
tests check all three Go parsers against it.

`readings.txt` is the correctness suite: 61 sentences, chosen to cover the
constructions above, with every reading the grammar gives them (73 in all),
each checked by hand. The intended reading is marked `*`, and a note says
where any others come from (PP attachment, a lexical ambiguity and so on).
Both implementations check that they give exactly these readings, and
likewise for `examples.txt`. Readings
are printed by `Lambda.pretty()` (Kotlin) and `term.Pretty` (Go), which
name variables by depth and sort conjuncts, so that equal readings print
the same whatever order the parser built them in. `tools/masc/suite.py`
writes both files from its lists of sentences, intended readings and notes;
after a change to the grammar, rerun it and check each changed reading by
hand.
