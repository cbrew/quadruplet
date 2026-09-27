"""Writes the MASC correctness suite, src/test/resources/masc/readings.txt,
and the examples of sentences outside the sample, examples.txt.

    python3 tools/masc/suite.py        (from the top of the repository)

For each sentence in SUITE and EXAMPLES it records every reading the grammar gives, as the
Go command-line parser prints them with -pretty, marks the intended one,
and adds the note. The readings were checked by hand: after changing the
grammar, rerun this and check every change in readings.txt before
committing it. The script stops if a sentence has no reading, or if the
text chosen to pick out the intended reading does not pick out exactly one.
"""
import subprocess
import sys

# (sentence, text found only in the intended reading or None if there is
# only one reading, note)
SUITE = [
 # copula, determiners, names
 ("this is an economic statute", None, None),
 ("we are a big industrial state", None, None),
 ("Dublin is a young city", None, None),
 ("my name is Mrs Linda Yace", None, "A proper name of three words is one lexical entry."),
 ("the guy 's guilty", None, "\"the\" is Russellian: there is exactly one guy."),
 ("is faux confidence enough", None, None),
 ("I 'm now a buffalo in Montaaaaaaaana", None, "A time adverb holds of the state; the PP after the copula is inside its NP complement."),
 # verbs and their roles
 ("an additional patient required a liver transplant", None, "Noun compounds relate the head to the modifier by nn."),
 ("my husband works for TI", None, None),
 ("I can not give you a specific date", None, "Two objects: the first is the Recipient."),
 ("get Flash Player from Adobe", None, "An imperative: the subject is the hearer."),
 ("signing up on Twitter", None, "A subjectless tweet: the subject is the speaker."),
 ("explore Penobscot Bay by canoe", None, None),
 ("join my crew and postpone the judgment", None, None),
 ("quickly she holds up a cross", None, "A manner adverb and a particle are predicates of the event."),
 ("the back came down and an elephant walked out", None, None),
 # quantifiers
 ("however the attacks of 9/11 changed everything", None, "Quantified pronouns restrict to things or persons."),
 ("Nathan Road has many electronics shops", None, "many is a relation between properties (Barwise and Cooper)."),
 ("these stores sell excess stock or factory overruns", None, "Disjoined NPs give a disjunction of quantifications."),
 ("the young adults who leave Pleasant Run have those same feelings", None, None),
 # auxiliaries, negation, ellipsis
 ("we did n't finalize that plan", None, "There is no quantifier storage: the object NP scopes under negation."),
 ("I will not cut the benefits", None, "Modals and negation scope over the event quantifier."),
 ("I do n't remember", None, None),
 ("state legislatures always have", None, "VP ellipsis: the elided VP is an event of the subject's."),
 ("I have n't either", None, None),
 ("this change would have doubled the channel gradient", None, None),
 ("this wolf will be running for its life", None, None),
 # passives
 ("the next destination was discussed", None, "An agentless passive: the agent is existentially quantified."),
 ("these rules were not used extensively", None, None),
 ("isopropyl alcohol was removed by evaporation under nitrogen", "under(x4, x5)",
  "\"by evaporation\" is an agent in two readings and a means in two; \"under nitrogen\" modifies the evaporation or the removal."),
 ("the alliance became known as the Delian League", None, "became is an auxiliary taking a passive VP."),
 ("the financial markets have become increasingly globalized", None, None),
 # complements
 ("I think it 's a triumph", None, "A clausal complement is the Topic of the thinking."),
 ("she said reading is the new civil right", None, None),
 ("caps ensure that environmental goals are met", None, None),
 ("then the region began to subside", None, "Subject control: the infinitive's subject is the region."),
 ("the advertisement attempts to make several points and reach several different constituencies", None, None),
 ("it may help a family stay together", "stay(x3) ∧ together(x3)", "Object control; \"together\" modifies the staying or the helping."),
 ("one can finally grasp how the world is filled with conflictuous passions", "with(x4, x5)",
  "An embedded question is a property of manners."),
 ("he pushed off the dark glasses because he could n't see to fly", None, None),
 ("whereas Greenberg seamlessly resolves this tension Jacobsen allows it to surface", None, None),
 # questions
 ("did you hear about the cheapskate vampire hunter", None, "A yes/no question."),
 ("am I missing something", None, None),
 ("what 're you doing", None, "A wh-question is a property of its answers."),
 ("what do you think", None, None),
 ("what do you think the time frame is on this", "on(x3, this)", "The gap is in the embedded clause; \"on this\" modifies its state or the thinking."),
 ("where is it overextended", None, "Wh-adverbs relate the event to a place, time, manner or reason."),
 ("how are you doing today", "do(x1) ∧ today(x1)", "\"doing\" is also transitive (from \"what 're you doing\"), with \"today\" as its object."),
 # relative clauses
 ("the only thing that Moore compressed is the timeframe", None, None),
 ("this is the big lie the wholesalers tell", None, "A relative clause without a relative pronoun."),
 ("it 's the best story you 've ever written", None, None),
 ("this is what Java Bayes takes", None, "A free relative."),
 ("I do it as somebody who has a conscience and is caring", None, "A quantified pronoun with a relative clause."),
 # existentials, coordination, adjuncts
 ("there is also a market and a porcelain shop", None, "Existential there: a state of each thing the NP quantifies over."),
 ("then there is the furniture", None, None),
 ("and Christmas would come and Christmas would go", None, None),
 ("of course we notice", "of_course", "\"of course\" is also a PP modifying the sentence."),
 # PP attachment
 ("a guy walks into a bar with a small dog", "with(x2, x3)", "The PP modifies the walking or the bar."),
 ("I looked at the sheet in my hand", "in(x2, x3)", "The PP modifies the sheet or the looking."),
 ("I rapped my fingers against my desk nervously", "against(x2, x3)", "The PP modifies the rapping or the fingers."),
 ("both sides are in talks to settle the dispute", "Agent(x5, x3)", "An infinitival relative (the talks settle it) or a purpose clause (the sides do)."),
]

# Sentences that are not in the sample, made from the lexicon's words.
EXAMPLES = [
 ("every architect who knows Dave found a house", None, None),
 ("the company expected every customer to find a house", "Patient(x3, x2)",
  "The intended reading has the customers find houses; the others read \"to find a house\" as an infinitival relative on \"customer\" or as a purpose clause of the company's."),
 ("a guy with a dog looked at the house in Dublin", "in(x3, dublin)", "\"in Dublin\" modifies the house or the looking."),
 ("the book was found by a guy in the house", "in(x2, x3)",
  "\"by a guy\" is the agent or an adjunct; \"in the house\" modifies the guy or the finding."),
 ("the elephant was not seen by the family", "family(x2)", "\"by the family\" is the agent, or an adjunct."),
 ("Dave thought that no customer noticed the change", None, None),
 ("the guy that the company ignored knew the answer", None, None),
 ("there is a dog in the house", None, None),
 ("Dave and Bruce heard a holler", None, "Conjoined names distribute: each heard a holler."),
 ("who knows Dave", None, None),
 ("what did the architect say", None, None),
 ("where did Dave find the book", None, None),
]

HEADERS = {
    'readings.txt': """# The MASC correctness suite: sentences from the sample whose readings
# have been checked by hand.
#
# Each sentence ("> ...") is followed by every reading of Top that the
# grammar gives it, as Lambda.pretty() and term.Pretty print them, in sorted
# order. The intended reading is marked "*"; where there are others, a note
# ("# ...") says where they come from. MascTest and masc_test.go check that
# the parsers give exactly these readings.
#
# Variables are named by depth: x1, x2, ... are quantified, v1, v2, ...
# λ-bound. speaker and hearer are the deictic constants; ynq marks a yes/no
# question.""",
    'examples.txt': """# Sentences that are not in the MASC sample, made from words in its
# lexicon, with every reading the grammar gives them, checked by hand. The
# format is that of readings.txt: the intended reading is marked "*", and
# MascTest and masc_test.go check that the parsers give exactly these
# readings.""",
}


def write(name, entries):
    sents = [s for s, _, _ in entries]
    assert len(set(sents)) == len(sents)
    out = subprocess.run(['go', 'run', './cmd/quadruplet', '-grammar', '../src/test/resources/masc/masc.fcfg',
                          '-start', 'Top', '-pretty', '-workers', '1'],
                         cwd='go', input='\n'.join(sents) + '\n', capture_output=True, text=True,
                         check=True).stdout
    readings, cur = {}, None
    for line in out.split('\n'):
        if line and not line.startswith(' '):
            cur = line
            readings[cur] = []
        elif line.startswith('  Top: '):
            readings[cur].append(line[len('  Top: '):])
    lines = []
    for s, sel, note in entries:
        rs = readings[s]
        assert rs, s
        if sel is None:
            assert len(rs) == 1, (s, rs)
            marked = [True]
        else:
            marked = [sel in r for r in rs]
            assert sum(marked) == 1, (s, sel, rs)
        lines.append('')
        lines.append('> ' + s)
        if note:
            lines.append('# ' + note)
        for m, r in zip(marked, rs):
            lines.append(('* ' if m else '  ') + r)
    with open('src/test/resources/masc/' + name, 'w') as fh:
        fh.write(HEADERS[name] + '\n' + '\n'.join(lines) + '\n')
    print('%s: %d sentences, %d readings' % (name, len(entries), sum(len(readings[s]) for s in sents)),
          file=sys.stderr)


write('readings.txt', SUITE)
write('examples.txt', EXAMPLES)
