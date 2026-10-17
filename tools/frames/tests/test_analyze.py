"""The analyzer's rules on hand-made dependency parses."""
import spacy
from spacy.tokens import Doc

from frames.analyze import frames_of

nlp = spacy.blank("en")


def parse(spec: str):
    """word/TAG/dep/head per token, head a 0-based index (itself for ROOT)."""
    words, tags, deps, heads, lemmas = [], [], [], [], []
    for tok in spec.split():
        w, tag, dep, head = tok.split("/")
        words.append(w)
        tags.append(tag)
        deps.append(dep)
        heads.append(int(head))
        lemmas.append({"is": "be", "are": "be", "was": "be"}.get(w.lower(), w.lower()))
    doc = Doc(nlp.vocab, words=words, tags=tags, deps=deps, heads=heads, lemmas=lemmas)
    doc.user_data["id"] = "t#0"
    return {f.verb: f for f in frames_of(doc, prior=lambda lemma, prep: 0.9 if prep == "on" else 0.1)}


def test_double_object():
    f = parse("She/PRP/nsubj/1 gave/VBD/ROOT/1 Mary/NNP/dative/1 books/NNS/dobj/1")["gave"]
    assert f.symbols() == "nad"


def test_passive_with_agent():
    f = parse("It/PRP/nsubjpass/2 was/VBD/auxpass/2 read/VBN/ROOT/2 by/IN/agent/2 kids/NNS/pobj/3")["read"]
    assert (f.symbols(), f.passive, f.modifier_keys()) == ("na", True, ["aux:be"])


def test_control_and_prior():
    fs = parse("She/PRP/nsubj/1 tried/VBD/ROOT/1 to/TO/aux/3 rely/VB/xcomp/1 on/IN/prep/3 "
               "him/PRP/pobj/4 at/IN/prep/3 noon/NN/pobj/6")
    assert fs["tried"].symbols() == "ni"
    rely = fs["rely"]
    assert (rely.refined(), rely.modifier_keys()) == ("np.on", ["pp:at"])
    assert next(a for a in rely.arguments if a.symbol == "n").source == "controlled"


def test_clauses():
    fs = parse("I/PRP/nsubj/1 think/VBP/ROOT/1 that/IN/mark/4 it/PRP/nsubj/4 works/VBZ/ccomp/1")
    assert fs["think"].symbols() == "ns-that"
    fs = parse("I/PRP/nsubj/1 wonder/VBP/ROOT/1 what/WP/dobj/4 she/PRP/nsubj/4 said/VBD/ccomp/1")
    assert fs["wonder"].symbols() == "ns-w"
    fs = parse("Let/VB/ROOT/0 me/PRP/nsubj/2 go/VB/ccomp/0")
    assert fs["Let"].symbols() == "nai"


def test_copula_and_particle():
    assert parse("She/PRP/nsubj/1 is/VBZ/ROOT/1 happy/JJ/acomp/1")["is"].symbols() == "nk"
    assert parse("We/PRP/nsubj/1 are/VBP/ROOT/1 in/IN/prep/1 chambers/NNS/pobj/2")["are"].symbols() == "nk"
    f = parse("She/PRP/nsubj/1 picked/VBD/ROOT/1 up/RP/prt/1 books/NNS/dobj/1")["picked"]
    assert (f.lemma, f.refined()) == ("picked", "nap.up")


def test_dative_pp_and_existential():
    f = parse("She/PRP/nsubj/1 gave/VBD/ROOT/1 books/NNS/dobj/1 to/IN/dative/1 Mary/NNP/pobj/3")["gave"]
    assert f.refined() == "nap.to"
    f = parse("There/EX/expl/1 is/VBZ/ROOT/1 a/DT/det/3 problem/NN/attr/1")["is"]
    assert f.symbols() == "nx"
