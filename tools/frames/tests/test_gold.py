"""Gold frames end to end: hand-made Penn trees through tools/masc's
verbframes.py and frames.gold."""
import os
import sys

import pytest

MASC = os.path.join(os.path.dirname(__file__), "..", "..", "masc")
sys.path.insert(0, os.path.abspath(MASC))
import verbframes  # noqa: E402
from masctrees import read_trees, unwrap  # noqa: E402

from frames import gold  # noqa: E402
from frames.inventory import Frame  # noqa: E402


def frames_of(ptb: str) -> dict[str, Frame]:
    tree = verbframes.Tree("t#0", unwrap(read_trees(ptb)[0]))
    out = {}
    for container, head, heads in verbframes.verbs_of(tree):
        rec = verbframes.verb_record(tree, container, head, heads)
        out[rec["verb"]] = gold.frame(rec)
    return out


@pytest.mark.parametrize("ptb, verb, frame", [
    # transitive, with a particle joining the lemma
    ("((S (NP-SBJ (PRP She)) (VP (VBD picked) (PRT (RP up)) (NP (DT the) (NN book))) (. .)))",
     "picked", "na"),
    # double object: d then a
    ("((S (NP-SBJ (PRP She)) (VP (VBD gave) (NP (NNP Mary)) (NP (NNS books)))))", "gave", "nad"),
    # dative PP
    ("((S (NP-SBJ (PRP She)) (VP (VBD gave) (NP (NNS books)) (PP-DTV (TO to) (NP (NNP Mary))))))",
     "gave", "nad"),
    # copula
    ("((S (NP-SBJ (PRP She)) (VP (VBZ is) (ADJP-PRD (JJ happy)))))", "is", "k"),
    # passive with no agent: the surface subject is the object; n is the unnamed agent
    ("((S (NP-SBJ-1 (DT The) (NN book)) (VP (VBD was) (VP (VBN read) (NP (-NONE- *-1))))))",
     "read", "na"),
    # controlled infinitive
    ("((S (NP-SBJ-1 (PRP She)) (VP (VBD tried) (S (NP-SBJ (-NONE- *PRO*-1)) (VP (TO to) (VP (VB leave)))))))",
     "tried", "ni"),
    # an infinitive with its own subject: a and i
    ("((S (NP-SBJ (PRP I)) (VP (VBP want) (S (NP-SBJ (PRP him)) (VP (TO to) (VP (VB go)))))))",
     "want", "nai"),
    # a small clause: a and o
    ("((S (NP-SBJ (PRP It)) (VP (VBD made) (S (NP-SBJ (PRP it)) (ADJP-PRD (JJR better))))))",
     "made", "nao"),
    # finite clauses by complementizer
    ("((S (NP-SBJ (PRP I)) (VP (VBP think) (SBAR (IN that) (S (NP-SBJ (PRP it)) (VP (VBZ works)))))))",
     "think", "ns-that"),
    ("((S (NP-SBJ (PRP I)) (VP (VBP think) (SBAR (-NONE- 0) (S (NP-SBJ (PRP it)) (VP (VBZ works)))))))",
     "think", "ns-2"),
    ("((S (NP-SBJ (PRP I)) (VP (VBP wonder) (SBAR (IN whether) (S (NP-SBJ (PRP it)) (VP (VBZ works)))))))",
     "wonder", "ns-if"),
    ("((S (NP-SBJ (PRP I)) (VP (VBP know) (SBAR (WHNP-1 (WP what)) (S (NP-SBJ (PRP she)) (VP (VBD said) (NP (-NONE- *T*-1))))))))",
     "know", "ns-w"),
    # prepositional object and a reflexive
    ("((S (NP-SBJ (PRP They)) (VP (VBD availed) (NP (PRP themselves)) (PP-CLR (IN of) (NP (PRP it))))))",
     "availed", "npr"),
])
def test_frames(ptb, verb, frame):
    assert frames_of(ptb)[verb].symbols() == frame


def test_passive_agent_is_n():
    f = frames_of("((S (NP-SBJ-1 (DT The) (NN book)) (VP (VBD was) (VP (VBN read) (NP (-NONE- *-1)) "
                  "(PP (IN by) (NP-LGS (NNS children)))))))")["read"]
    n = next(a for a in f.arguments if a.symbol == "n")
    assert (f.symbols(), f.passive, n.marker, n.head) == ("na", True, "by", "children")


def test_refined_and_modifiers():
    f = frames_of("((S (NP-SBJ (PRP She)) (VP (MD will) (RB not) (VP (VB rely) (PP-CLR (IN on) (NP (PRP him))) "
                  "(ADVP-MNR (RB deliberately)) (PP-TMP (IN on) (NP (NNP Monday)))))))")["rely"]
    assert f.refined() == "np.on"
    assert f.modifier_keys() == ["adv:deliberately", "aux:will", "neg", "pp:on"]


def test_json_round_trip():
    f = frames_of("((S (NP-SBJ (PRP She)) (VP (VBD gave) (NP (NNP Mary)) (NP (NNS books)))))")["gave"]
    assert Frame.from_json(f.to_json()) == f


def test_extraposed_clause_beside_expletive():
    f = frames_of("((S (NP-SBJ (NP (PRP It)) (S (-NONE- *EXP*-1))) (VP (VBZ appears) "
                  "(SBAR-1 (IN that) (S (NP-SBJ (PRP it)) (VP (VBZ works)))))))")["appears"]
    assert f.symbols() == "xs-that"
