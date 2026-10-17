"""ptb2pl.py and ptb.pl on hand-made Penn trees, and on the example programs.

    python3 -m unittest tools/masc/prolog/test_ptb2pl.py

The Prolog tests need SWI-Prolog (swipl) and are skipped without it.
"""
import io
import os
import shutil
import subprocess
import sys
import tempfile
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))
from masctrees import read_trees, tokenize, unwrap  # noqa: E402
import ptb2pl  # noqa: E402

PTB_PL = os.path.join(HERE, 'ptb.pl')
SWIPL = shutil.which('swipl')

WHQ = ("( (SBARQ (WHNP-1 (WDT What) (NN grade)) (SQ (VBZ is) (NP-SBJ (PRP she)) "
       "(PP-PRD (IN in) (NP (-NONE- *T*-1)))) (. ?)))")
CONTROL = ("( (S (NP-SBJ-1 (PRP She)) (VP (VBD tried) (S (NP-SBJ (-NONE- *PRO*-1)) "
           "(VP (TO to) (VP (VB leave))))) (. .)))")


def program(ptb, guidelines='revised'):
    out = io.StringIO()
    tree = ptb2pl.mended(unwrap(read_trees(ptb)[0]))
    ptb2pl.write('test#0', tree, guidelines, out)
    return out.getvalue()


def swipl(files, goal):
    r = subprocess.run([SWIPL, '-q', '-g', goal, '-t', 'halt(1)', PTB_PL] + files,
                       capture_output=True, text=True, timeout=60)
    if r.returncode != 0:
        raise AssertionError(r.stderr)
    return r.stdout


class Converter(unittest.TestCase):

    def test_words_labels_heads_and_indices(self):
        p = program(CONTROL)
        self.assertIn("w(prp,'She'),\n  w(vbd,tried,try),\n  w(to,to),\n  w(vb,leave),\n  w('.','.')", p)
        self.assertIn("s(s1) ---> [sbj:np(np1), ^'--':vp(vp1), '--':t('.')].", p)
        self.assertIn("np(np2) ---> [^'--':e('*PRO*', np1)].", p)

    def test_edge_labels_and_category_tags(self):
        p = program("( (S (NP-SBJ (PRP I)) (VP (VBD was) (PP-LOC-PRD (IN in) (NP (NNP Rome)))) "
                    "(S-NOM-ADV (VP (VBG working)))))")
        self.assertIn("[loc,prd]:pp(pp1)", p)
        self.assertIn("adv:s(s2)", p)
        self.assertIn("tags(s2, [nom]).", p)

    def test_gapping(self):
        p = program("( (S (S (NP-SBJ=1 (PRP I)) (VP (VBD left))) (CC and) "
                    "(S (NP-SBJ=1 (PRP she)) (VP (VBD stayed)))))")
        self.assertIn("gap(np1, 1).", p)
        self.assertIn("gap(np2, 1).", p)

    def test_mending(self):
        # a no-break space inside a word is part of it; an empty bracket is dropped
        self.assertEqual(tokenize('(IN vis-Ã -vis)'), ['(', 'IN', 'vis-Ã -vis', ')'])
        p = program("( (S (NP-SBJ (PRP I)) (VP (VBD left) ( ))))")
        self.assertIn("vp(vp1) ---> [^'--':t(vbd)].", p)


@unittest.skipUnless(SWIPL, 'needs swipl')
class Prolog(unittest.TestCase):

    def setUp(self):
        self.dir = tempfile.mkdtemp()

    def tearDown(self):
        shutil.rmtree(self.dir)

    def write(self, name, text):
        path = os.path.join(self.dir, name)
        with open(path, 'w', encoding='utf-8') as f:
            f.write(text)
        return path

    def test_one_analysis(self):
        f = self.write('whq.pl', program(WHQ))
        out = swipl([f], 'aggregate_all(count, analysis(_), N), writeln(N), halt')
        self.assertEqual(out.strip(), '1')
        out = swipl([f], "analysis(F), forall(member(X, F), (writeq(X), nl)), halt")
        for fact in ["constituent(pp1,pp,[5-5])", "empty(e(np2,1),'*T*',6)",
                     "antecedent(e(np2,1),whnp1)", "head(sq1,3)", "lemma(3,be)"]:
            self.assertIn(fact, out)

    def test_reattached(self):
        f = self.write('whq.pl', program(WHQ))
        b = self.write('whq-b.pl', swipl([f], 'reattached(P), write_program(P), halt'))
        with open(b) as fh:
            text = fh.read()
        self.assertIn("moved(whnp1,'*T*')", text)
        out = swipl([b], "aggregate_all(count, analysis(_), N), writeln(N), analysis(F), "
                         "forall(member(X, F), (writeq(X), nl)), halt")
        self.assertEqual(out.splitlines()[0], '1')
        # What grade ... in: the PP is discontinuous
        self.assertIn("constituent(pp1,pp,[1-2,5-5])", out)
        self.assertIn("constituent(sq1,sq,[1-5])", out)

    def test_examples(self):
        # every example program has one analysis, and so has its reattached program
        ex = os.path.join(HERE, 'examples')
        for name in sorted(os.listdir(ex)):
            f = os.path.join(ex, name)
            with self.subTest(name):
                out = swipl([f], 'aggregate_all(count, analysis(_), N), writeln(N), halt')
                self.assertEqual(out.strip(), '1')
                b = self.write(name, swipl([f], 'reattached(P), write_program(P), halt'))
                out = swipl([b], 'aggregate_all(count, analysis(_), N), writeln(N), halt')
                self.assertEqual(out.strip(), '1')


if __name__ == '__main__':
    unittest.main()
