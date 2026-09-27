# Montague, events and continuations: what kind of grammar quadruplet has

quadruplet's grammars have been described, at different times, as
"Montague-style", as having "event semantics", and, in conversation, as
using "continuations". Each description is partly right, and none is the
whole story. This essay sets out the ideas behind the MASC grammar
(`src/test/resources/masc/masc.fcfg`, version 0), where each one comes
from, how they fit together, and what the grammar does and does not do.

It assumes an introductory course in formal semantics, at the level of
Heim and Kratzer (1998): types *e* and *t*, the lambda calculus,
generalised quantifiers, predicate logic. Readers from NLP who have met
AMR or the Parallel Meaning Bank, but not Montague, should be able to
follow it, skipping a derivation here and there. The formulas are the
parser's own output, as `-pretty` prints it.

## The short answer

The MASC grammar is

> a **unification grammar** (context-free rules over feature structures)
> whose semantics is an **extensional Montague semantics** extended with
> **quantificational event semantics** in the style of Champollion,
> with gaps handled by **GPSG slash categories**.

Four layers, each borrowed from a different tradition:

| layer | what it does | where it comes from |
|---|---|---|
| syntax | context-free rules over categories with features (agreement, verb form, subcategorisation), combined by unification | PATR-II, GPSG, the NLTK feature grammars |
| composition | each rule says how the meaning of the whole is built from the meanings of the parts, as a lambda term | Montague (PTQ), the "rule-to-rule" hypothesis |
| meanings | noun phrases are generalised quantifiers; verbs introduce events with thematic roles; the event is existentially quantified inside the verb | Montague; Barwise and Cooper; Davidson and Parsons; Champollion |
| unbounded dependencies | a missing noun phrase is recorded in the category (VP/NP, written `VPgap`) and its meaning is a lambda abstraction over the gap | GPSG (Gazdar 1981) |

"Continuations" is not a fifth layer. It is a way of describing what the
third layer does, and it is the best way to see why Montague's
treatment of noun phrases and Champollion's treatment of events belong
together. Section 4 explains.

## 1. What we take from Montague

Montague's "The Proper Treatment of Quantification in Ordinary English"
(PTQ, 1973) gave us three things this grammar uses directly.

**Compositionality, rule by rule.** Every syntactic rule comes paired with
a semantic rule. In quadruplet the pairing is literal: the semantics is a
feature, `sem`, on each category, and a rule's left-hand side says how to
build its `sem` from its daughters':

    S[sem=<\F.?np(\x.?vp(x, F))>] -> NP[sem=<?np>] VP[sem=<?vp>]

`?np` and `?vp` are variables that unification binds to the daughters'
meanings, and the result is simplified by beta reduction. (Ignore the `F`
for now.)

**Noun phrases as generalised quantifiers.** Montague gave every noun
phrase the same type, (*e*→*t*)→*t*: a set of properties. A name denotes
the properties its bearer has, and a quantified NP denotes a
quantifier:

    Dave       \P.P(dave)
    every      \P Q.all x.(P(x) -> Q(x))
    every guy  \Q.all x.(guy(x) -> Q(x))

Uniform types let one subject rule serve names and quantifiers alike. We
also follow Barwise and Cooper (1981) where first-order logic runs out:
"many shops" is a relation between two properties,
`many(λv1.shop(v1), λv1.…)`, not a first-order formula.

**Transitive verbs take quantifiers as objects.** In PTQ a transitive verb
applies to the meaning of its object NP, not to an individual, so "sees a
dog" needs no special rule to handle a quantified object:

    knows      \X y F.X(\x.exists e.(know(e) & Experiencer(e, y) & Theme(e, x) & F(e)))

Here `X` is the object's quantifier. The verb hands it a property of
individuals ("is known by `y`") and lets it do the quantifying. (The
event `e` and the condition `F` are the subject of sections 2 and 3.)

We also keep two of PTQ's lexical analyses. "the" is Russell's: "the
book" says there is exactly one book, so the reading of "the book was
found" is

    ∃x1.(book(x1) ∧ ∀x2.(book(x2) → (x1 = x2)) ∧ ∃x2.∃x3.(Agent(x3, x2) ∧ Theme(x3, x1) ∧ find(x3)))

And predicative "be" is identity: "Dublin is a young city" says Dublin
is identical to some young city (inside a state, as section 2 explains).

### Scope, and scope ambiguity

The *scope* of a quantifier, a negation or a modal is the part of the
formula it governs. In logic the order of quantifiers matters:
∀x∃y.loves(x, y) says everyone loves someone or other, while
∃y∀x.loves(x, y) says there is one person everyone loves. The second
entails the first, but not the other way round.

A sentence is *scope ambiguous* when one syntactic structure allows more
than one order of this kind. "every architect found a house" has two
readings:

    ∀x.(architect(x) → ∃y.(house(y) ∧ find(x, y)))     each architect found some house or other
    ∃y.(house(y) ∧ ∀x.(architect(x) → find(x, y)))     there is one house every architect found

The first is the *surface scope* reading, since the quantifiers come in
the order of the words; the second is the *inverse scope* reading.
Negation does the same. "Dave didn't find a book" can mean that he found
no book (¬∃) or that there is a book he didn't find (∃¬).

This is not the kind of ambiguity a parser usually finds. In "a guy walks
into a bar with a small dog" the two readings come from two trees, with
the PP attached in different places. In "every architect found a house"
there is only one tree, and the ambiguity is in how the meanings of its
parts are put together. So a theory has to add something to produce the
inverse reading. Montague added *quantifying-in*: a rule that builds the
sentence around a placeholder pronoun and brings the quantifier in at the
end, outside everything else. Cooper (1983) put quantifiers in a store
and let them out later, in any order, when a clause is complete.
Transformational grammar moves them, at an abstract level of syntax
called Logical Form, by "quantifier raising" (May 1977; Heim and Kratzer
1998). Continuations do it by letting any expression take
scope over a larger stretch of the sentence (section 4). And much
grammar-based NLP, such as the English Resource Grammar's Minimal
Recursion Semantics (Copestake et al. 2005), does not choose at all: it
outputs one underspecified representation that leaves the order open.

With *n* quantifiers there can be up to *n*! orders, and some of them are
logically equivalent, so producing every scoping multiplies the readings
quickly. A benchmark that enumerated them would be a very different
benchmark.

quadruplet's grammars have none of these devices. Each tree gets exactly
one meaning, and that meaning has surface scope:

    every architect found a house
    ∀x1.(architect(x1) → ∃x2.(house(x2) ∧ ∃x3.(Agent(x3, x1) ∧ Theme(x3, x2) ∧ find(x3))))

    Dave did n't find a book
    ¬∃x1.(book(x1) ∧ ∃x2.(Agent(x2, dave) ∧ Theme(x2, x1) ∧ find(x2)))

(The `∃x3` and `∃x2` over events are explained in sections 2 and 3.)

### What we leave out of PTQ

Enough that "Montague-style" alone oversells the grammar:

* **No intensionality.** PTQ is an intensional logic with possible worlds
  and times, needed for "seek a unicorn" or "the temperature is rising".
  Our logic is extensional. "think", "want" and "can" take propositions as
  arguments or apply operators to them, but nothing interprets those
  operators.
* **No tense.** Past and present forms mean the same. The perfect and the
  progressive are uninterpreted operators, `perf(…)` and `prog(…)`.
* **No quantifying-in, so no scope ambiguity.** As the previous section
  says, every reading has surface scope. Section 4 says why, and every
  ambiguity the parser finds is structural or lexical.
* **No bound pronouns or anaphora.** Pronouns are constants: "every Japan
  veteran has his list" gives `of(x2, he)`, not the veteran's list.

## 2. What we take from event semantics

Davidson (1967) noticed that adverbial modifiers behave like conjuncts.
"Dave found a book quickly in the library" entails "Dave found a book
quickly", which entails "Dave found a book", and Montague-style meanings
for adverbs, as functions from properties to properties, don't deliver
those entailments without extra postulates. Davidson's fix was to give
verbs an extra argument, an event, and make modifiers predicates of it.
The neo-Davidsonian version (Parsons 1990) goes further. The verb is a
one-place predicate of events, and each participant is linked to the
event by a thematic role:

    Dave found a book quickly
    ∃x1.(book(x1) ∧ ∃x2.(Agent(x2, dave) ∧ Theme(x2, x1) ∧ find(x2) ∧ quickly(x2)))

Dropping a conjunct gives an entailment, which is what Davidson wanted.
The same move handles prepositional phrases:

    a guy walks into a bar
    ∃x1.(guy(x1) ∧ ∃x2.(Agent(x2, x1) ∧ walk(x2) ∧ ∃x3.(bar(x3) ∧ into(x2, x3))))

The walking, not the guy, is into the bar. For comparison, `sem2.fcfg`,
an NLTK grammar among quadruplet's tests, predicates the PP of the
subject: "Mary walks in Noosa" is `in(mary, noosa) ∧ walk(mary)`. That is
fine for "in Noosa" and wrong for "into a bar". The first version of the
MASC grammar applied the PP to the whole proposition instead,
`in(walk(mary), noosa)`, which at least locates the right thing but has no
logic behind it. Events are the principled version of both.

Two notes on our choices:

* **The roles are a small, crude set.** Subjects are Agent, or Experiencer
  for about thirty verbs of perception and attitude ("know", "think",
  "see"). Objects are Theme, second objects Recipient, clausal complements
  Topic, and the object of "help" or "expect" Patient. They are assigned
  by subcategorisation frame, not verb by verb as VerbNet does.
* **States are events too.** The copula introduces a state:
  "Dublin is a young city" is
  `∃x1.(Theme(x1, dublin) ∧ be(x1) ∧ ∃x2.((dublin = x2) ∧ city(x2) ∧ young(x2)))`,
  so "is now a buffalo" can say `now(x1)` of the state.

## 3. Where does the event quantifier go?

If verbs have an event argument, something has to bind it. The textbook
answer is *existential closure*: the VP denotes a property of events, and
the sentence is closed off with ∃*e* at the end. That goes wrong as soon
as there is another quantifier or a negation.

Take "no customer noticed the change", simplified to "no customer
noticed". Let the VP be a relation between individuals and events,
`λx λe. notice(e) ∧ Experiencer(e, x)`, compose it with the subject, and
close at the top:

    ∃e.¬∃x.(customer(x) ∧ notice(e) ∧ Experiencer(e, x))

This says there is an event that is not a noticing by a customer. That is
true in almost any situation, including ones where a customer did notice.
The event quantifier has taken wide scope over "no customer", and it
should have taken narrow scope.

Champollion (2015) showed that the problem goes away if you stop treating
the event as a free variable waiting for closure. Instead, **the verb
itself contains the existential quantifier over events**. Everything the
verb combines with afterwards (its object and subject, negation, a modal)
then takes scope over the event quantifier. To let modifiers still reach
the event, the verb takes one more argument, a condition *F* on the
event:

    walks   \x F.exists e.(walk(e) & Agent(e, x) & F(e))

A modifier doesn't apply to the verb's meaning. It adds itself to the
condition before passing it inward:

    VP[sem=<\x F.?vp(x, \e.(?pp(e) & F(e)))>] -> VP[sem=<?vp>] PP[sem=<?pp>]

And at the very top, where there is nothing left to say about the event,
the sentence is applied to the trivial condition:

    Top[sem=<?s(\e.true)>] -> S[sem=<?s>]

(`true` is the unit of conjunction, and the logic drops it.) The
parser's reading of the full sentence has the scopes right:

    no customer noticed the change
    ¬∃x1.(customer(x1) ∧ ∃x2.(change(x2) ∧ ∀x3.(change(x3) → (x2 = x3)) ∧
          ∃x3.(Experiencer(x3, x1) ∧ Theme(x3, x2) ∧ notice(x3))))

The event quantifier, ∃x3, is under the negation, the subject and the
object. The same holds for negation in the auxiliary ("Dave did n't find
the book" is ¬∃…∃x2.(find(x2) …)) and for modals. Quantifiers inside modifiers go the other way. A PP adds itself
to the condition *F*, so in "a guy walks into a bar" the quantifier over
bars, ∃x3, is inside the event's scope: each walking is into some bar.

### A derivation

Here is "every guy knows Dave", rule by rule, as the parser builds it.
Beta reduction happens as each rule is applied.

1. The verb phrase, from `VP -> V[tr] NP` with `sem=<?v(?np)>`:

       (\X y F.X(\x.exists e.(know(e) & Experiencer(e, y) & Theme(e, x) & F(e))))(\P.P(dave))
       = \y F.exists e.(know(e) & Experiencer(e, y) & Theme(e, dave) & F(e))

2. The subject, from `NP -> Det Nom` with `sem=<?d(?n)>`:

       (\P Q.all x.(P(x) -> Q(x)))(\x.guy(x)) = \Q.all x.(guy(x) -> Q(x))

3. The sentence, from `S -> NP VP` with `sem=<\F.?np(\x.?vp(x, F))>`:

       \F.all x.(guy(x) -> exists e.(know(e) & Experiencer(e, x) & Theme(e, dave) & F(e)))

   A sentence is still waiting for a condition on its event, so a
   sentence-level modifier ("in Dublin, every guy knows Dave") can add one.

4. The top, from `Top -> S` with `sem=<?s(\e.true)>`:

       ∀x1.(guy(x1) → ∃x2.(Experiencer(x2, x1) ∧ Theme(x2, dave) ∧ know(x2)))

## 4. Continuations

A **continuation** is a programming-language idea. Instead of a
computation returning its value, you pass it a function that says what
to do with the value (the rest of the computation), and it calls that
function. A function that would return an *a* becomes one that takes an
(*a*→*r*) and returns an *r*. Chris Barker (2002) pointed out that
Montague had already done this to noun phrases:

* An individual, type *e*, "continuized" with answer type *t*, has type
  (*e*→*t*)→*t*. That is exactly Montague's type for NPs:
  `\P.P(dave)` is Dave waiting to be told what to do with him.
* An event, type *v*, continuized the same way, has type (*v*→*t*)→*t*.
  That is exactly Champollion's type for verbs (after they have taken
  their arguments): `\F.exists e.(walk(e) & … & F(e))` is an event
  waiting to be told what to do with it.

So the grammar continuizes two kinds of thing, individuals (following
Montague) and events (following Champollion). The payoff is the same in
both cases: a continuized thing controls where its own quantifier goes.
An NP puts its quantifier outside whatever property it is handed. A verb
puts its event quantifier outside whatever condition it is handed, and
inside its arguments, negation and modals, because they are handed the
verb. Closing a sentence with `\e.true` is what programmers would call
running the computation with the empty continuation.

That is the extent of it, and it is worth being exact, because
"continuation semantics" in the literature (Barker and Shan 2014) means
more. There, continuations are the mechanism for scope-taking: any
expression can take scope over any larger stretch of the sentence it is
in, which gives inverse scope, scope islands and more. Our grammar does
not do that. Each rule fixes the order in which the pieces apply, as the
S rule above does (subject outside, VP inside), so the subject outscopes
the auxiliary and negation, which outscope the object, which outscopes
the event, which outscopes any quantifier inside a modifier. Adding
Barker and Shan's machinery, or Cooper storage, is the obvious route to
scope ambiguity in a later version.

## 5. Gaps

"the guy that the company ignored" has a gap: "ignored" has no object in
the string. PTQ handles relative clauses with numbered pronouns
("the company ignored him₃") and a rule that abstracts over the variable.
Transformational grammar moves the relative pronoun and leaves a trace.
We follow GPSG instead (Gazdar 1981; Gazdar, Klein, Pullum and Sag
1985): a constituent with a missing NP gets its own category, VP/NP or
S/NP, written `VPgap` and `Sgap`, and a few rules pass the gap up the
tree. Semantically, a VP/NP means what the VP would mean, abstracted over
the missing object:

    VPgap[sem=<\z.?v(\P.P(z))>] -> V[subcat=tr, sem=<?v>]
    Sgap[sem=<\z F.?np(\x.?vp(z, x, F))>] -> NP[sem=<?np>] VPgap[sem=<?vp>]

A relative clause is then a property of the gap, and it modifies the noun
by intersection:

    Dave knows every guy that the company ignored
    ∀x1.((guy(x1) ∧ ∃x2.(company(x2) ∧ ∀x3.(company(x3) → (x2 = x3)) ∧
                         ∃x3.(Agent(x3, x2) ∧ Theme(x3, x1) ∧ ignore(x3))))
         → ∃x2.(Experiencer(x2, dave) ∧ Theme(x2, x1) ∧ know(x2)))

(A subject relative, "every architect who knows Dave", needs no gap: the
relative clause is just the VP's meaning, closed.)

Wh-questions use the same machinery. A question means the property its
answers supply, what is sometimes called the categorial approach to
questions: "what did Dave find" is
`λv1.(thing(v1) ∧ ∃x1.(Agent(x1, dave) ∧ Theme(x1, v1) ∧ find(x1)))`.
A yes/no question is a proposition marked `ynq(…)`.

There is an implementation reason for slash categories as well. The
variables `?np`, `?vp` in the rules are placeholders filled in by
unification, and filling one in does not rename the bound variables of
what goes in. So a rule cannot say "this gap's meaning is the variable
bound by that λ over there". The abstraction has to be written into the
rules that carry the gap, which is what GPSG does anyway.

## 6. The machinery, briefly

For readers who want to know what the parser actually manipulates:

* **Syntax** is a context-free grammar over feature structures, parsed
  bottom-up with a chart. Features such as `agr`, `vform` and `subcat`
  constrain which rules apply; they are matched by unification (Shieber
  1986 is the classic introduction). Nested feature structures are not
  supported; the grammar uses flat features and category names instead.
* **Meanings** are terms of an untyped lambda calculus with the
  connectives and quantifiers of predicate logic. The types in this
  essay are a discipline the grammar follows, not something the parser
  checks. Bound variables are stored as de Bruijn indices (numbers
  counting binders), so terms that differ only in variable names are
  identical. That is the same idea as the relative indices of Bos's
  (2023) sequence notation for the Parallel Meaning Bank.
* **Simplification** happens as terms are built. Beta reduction is done,
  nested conjunctions and disjunctions are flattened and deduplicated,
  `true` is dropped from conjunctions, and vacuous quantifiers are
  removed. So two parse trees whose meanings simplify to the same term
  give the same edge in the chart.
* **Readings and trees.** The chart is a packed forest. A *tree* is one
  way of building a complete sentence; a *reading* is a distinct meaning
  for it. When two trees give equal meanings (a spurious ambiguity) they
  share one edge, so there are never more readings than trees. The 299
  MASC sentences have 1,798 trees between them, but only 935 readings.
* **Printing.** The parser's internal print shows de Bruijn indices.
  `Lambda.pretty()` (Kotlin) and `term.Pretty` (Go) name variables by
  depth, x1, x2, … for quantified ones and v1, v2, … for λ-bound ones,
  and sort conjuncts, so that equal meanings print the same. All the
  formulas here are printed that way.

## 7. For NLP readers

In NLP terms, this is a grammar-based semantic parser. It maps a
tokenised sentence to all of its logical forms under a hand-written
grammar, with no statistics and no ranking. What comes out is close in
spirit to other meaning representations you may know:

* **AMR** (Banarescu et al. 2013) also uses neo-Davidsonian roles, but it
  has no quantifier scope and no negation scope beyond a polarity flag.
  Our formulas have both. They are mostly first-order, and could go to a
  theorem prover once the exceptions were dealt with: propositions as
  arguments (`Topic(e, …)`, the modals) and a few generalised
  quantifiers.
* **The Parallel Meaning Bank** (Abzianidze et al. 2017; Bos 2023) is the
  closest relative. It is neo-Davidsonian, with VerbNet roles and real
  scope, and its annotations were bootstrapped compositionally (by Boxer,
  from CCG derivations) and then corrected by hand.
  It differs from us in using Discourse Representation Theory, which
  handles anaphora and presupposition (where we have constants and a
  Russellian "the"), WordNet senses (we use lemmas), and tense (we have
  none).

What a grammar like this gives you that a trained parser doesn't is the
complete set of readings, each traceable to the rules that produced it.
That is why it makes a good benchmark for the parser: `readings.txt` lists
every reading of 61 sentences, checked by hand, and both implementations
must produce exactly those. What it does not offer is robustness or
ranking. 15 of the 299 sample sentences get no reading at all, and when
there are several readings the grammar has no opinion about which is
meant.

## 8. Summary of what the grammar does not do

Collected from above, for anyone deciding what version 1 should be:

* no intensionality, tense or aspect beyond uninterpreted operators;
* surface scope only: no quantifier storage, raising or scope-taking
  continuations;
* no anaphora or discourse: pronouns are constants, and "the" is
  Russellian rather than presuppositional;
* a small, frame-based set of thematic roles, and predicates that are
  lemmas, not senses;
* intersective adjectives only ("an alleged thief" is a thief); plurals
  as ordinary individuals; bare plurals as existentials;
* a lexicon limited to the word forms in the MASC sample.

## Names, used consistently

| term | what it means in quadruplet |
|---|---|
| Montague semantics | compositional, rule-by-rule translation into a lambda-calculus logic, with NPs as generalised quantifiers. Here it is extensional, with no tense and no quantifying-in |
| event semantics, neo-Davidsonian | verbs are predicates of events; participants are linked by thematic roles; modifiers are predicates of the event |
| quantificational event semantics | Champollion's version: the verb existentially quantifies over its event and takes a condition *F* on it, so the event quantifier scopes below the verb's arguments, negation and modals |
| continuation, continuized | a meaning that takes "the rest of the sentence" as an argument. Our NPs (Montague) and verbs (Champollion) are continuized individuals and events. We do not use continuations for scope-taking |
| closure | applying a sentence to `\e.true` at the top, or in embedded clauses and relative clauses |
| slash category (`VPgap`, `Sgap`) | a constituent missing an NP, whose meaning abstracts over it (GPSG) |
| scope, scope ambiguity | the part of a formula a quantifier, negation or modal governs; a sentence is scope ambiguous when one structure allows several orders. We give only the surface order |
| reading | a distinct meaning of a whole sentence: a complete `Top` edge |
| tree | one derivation of a whole sentence; several trees may share a reading |

## References

* Abzianidze, L., et al. (2017). The Parallel Meaning Bank. *EACL 2017*.
* Banarescu, L., et al. (2013). Abstract Meaning Representation for sembanking. *Linguistic Annotation Workshop*.
* Barker, C. (2002). Continuations and the nature of quantification. *Natural Language Semantics* 10, 211–242.
* Barker, C. and Shan, C. (2014). *Continuations and Natural Language*. Oxford University Press.
* Barwise, J. and Cooper, R. (1981). Generalized quantifiers and natural language. *Linguistics and Philosophy* 4, 159–219.
* Bos, J. (2023). The sequence notation: catching complex meanings in simple graphs. *IWCS 2023*, 195–208.
* Champollion, L. (2015). The interaction of compositional semantics and event semantics. *Linguistics and Philosophy* 38, 31–66.
* Cooper, R. (1983). *Quantification and Syntactic Theory*. Reidel.
* Copestake, A., Flickinger, D., Pollard, C. and Sag, I. (2005). Minimal Recursion Semantics: an introduction. *Research on Language and Computation* 3, 281–332.
* Davidson, D. (1967). The logical form of action sentences. In N. Rescher (ed.), *The Logic of Decision and Action*.
* Gazdar, G. (1981). Unbounded dependencies and coordinate structure. *Linguistic Inquiry* 12, 155–184.
* Gazdar, G., Klein, E., Pullum, G. and Sag, I. (1985). *Generalized Phrase Structure Grammar*. Blackwell.
* Heim, I. and Kratzer, A. (1998). *Semantics in Generative Grammar*. Blackwell.
* May, R. (1977). *The Grammar of Quantification*. PhD thesis, MIT.
* Montague, R. (1973). The proper treatment of quantification in ordinary English. In J. Hintikka et al. (eds.), *Approaches to Natural Language*.
* Parsons, T. (1990). *Events in the Semantics of English*. MIT Press.
* Russell, B. (1905). On denoting. *Mind* 14, 479–493.
* Shieber, S. (1986). *An Introduction to Unification-Based Approaches to Grammar*. CSLI.
