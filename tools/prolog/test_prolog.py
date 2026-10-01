"""cgel2pl.py, ud2pl.py, clear2pl.py and their rules, on hand-made trees and on
the example programs.

    python3 -m unittest tools/prolog/test_prolog.py

The Prolog tests need SWI-Prolog (swipl) and are skipped without it. None
needs spaCy, its model, CGELBank or UD: clear2pl.py is tried on stand-ins for
spaCy's tokens.
"""
import io
import os
import re
import shutil
import subprocess
import sys
import tempfile
import unittest
from types import SimpleNamespace

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import cgel2pl  # noqa: E402
import clear2pl  # noqa: E402
import deptree  # noqa: E402
import ud2pl  # noqa: E402

SWIPL = shutil.which('swipl')
EXAMPLES = os.path.join(HERE, 'examples')

# the tree of Reynolds, Arora and Schneider (2023), Figure 4, for "which Liz bought",
# with a determiner fused with the head and a gap
WHICH = '''# sent_id = which-liz-bought
# text = which Liz bought.
# sent = which Liz bought --
(Clause
    :Prenucleus (x / NP
        :Head (Nom
            :Det-Head (DP
                :Head (D :t "which"))))
    :Head (Clause
        :Subj (NP
            :Head (Nom
                :Head (N :t "Liz")))
        :Head (VP
            :Head (V :t "bought" :l "buy" :p ".")
            :Obj (x / GAP))))
'''

CONLLU = '''# sent_id = t1
# text = Which book did Kim say she bought?
1\tWhich\twhich\tDET\tWDT\tPronType=Int\t2\tdet\t2:det\t_
2\tbook\tbook\tNOUN\tNN\tNumber=Sing\t7\tobj\t7:obj\t_
3\tdid\tdo\tAUX\tVBD\tMood=Ind|Tense=Past\t5\taux\t5:aux\t_
4\tKim\tKim\tPROPN\tNNP\tNumber=Sing\t5\tnsubj\t5:nsubj\t_
5\tsay\tsay\tVERB\tVB\tVerbForm=Inf\t0\troot\t0:root\t_
6\tshe\tshe\tPRON\tPRP\tCase=Nom\t7\tnsubj\t7:nsubj\t_
7\tbought\tbuy\tVERB\tVBD\tTense=Past\t5\tccomp\t5:ccomp\tSpaceAfter=No
8\t?\t?\tPUNCT\t.\t_\t5\tpunct\t5:punct\t_

# sent_id = t2
# text = I don't know.
1\tI\tI\tPRON\tPRP\t_\t4\tnsubj\t_\t_
2-3\tdon't\t_\t_\t_\t_\t_\t_\t_\t_
2\tdo\tdo\tAUX\tVBP\t_\t4\taux\t_\t_
3\tn't\tnot\tPART\tRB\t_\t4\tadvmod\t_\t_
4\tknow\tknow\tVERB\tVB\t_\t0\troot\t_\t_
5\t.\t.\tPUNCT\t.\t_\t4\tpunct\t_\t_
'''


def swipl(rules, files, goal):
    # the goal halts; if it fails or raises, the toplevel halts with 1
    r = subprocess.run([SWIPL, '-q', '-g', '(%s), halt' % goal, '-t', 'halt(1)', os.path.join(HERE, rules)] + files,
                       capture_output=True, text=True, timeout=60,
                       env=dict(os.environ, LANG='C.UTF-8', LC_ALL='C.UTF-8'))
    if r.returncode != 0:
        raise AssertionError(r.stderr)
    return r.stdout


def cgel_program(text):
    meta, tree = next(cgel2pl.blocks_text(text))
    out = io.StringIO()
    cgel2pl.write(meta, tree, out, 'test.cgel')
    return out.getvalue()


def ud_programs(text):
    path = tempfile.mktemp(suffix='.conllu')
    with open(path, 'w', encoding='utf-8') as f:
        f.write(text)
    outs = []
    for comments, rows in ud2pl.sentences(path):
        out = io.StringIO()
        ud2pl.write_one(path, comments, rows, out)
        outs.append(out.getvalue())
    os.remove(path)
    return outs


class DepTreeTest(unittest.TestCase):
    def test_projective(self):
        t = deptree.DependencyTree(['w(pron,i)', 'w(verb,left,leave)'], ['pron', 'verb'], [2, 0], ['nsubj', 'root'])
        self.assertEqual(t.rules(), ["verb(w2) ---> [nsubj:t(pron), ^'--':t(verb)]."])
        self.assertEqual(t.discontinuous(), [])

    def test_nonprojective_runs(self):
        t = deptree.DependencyTree(['w(det,a)'] * 8, ['det', 'noun', 'aux', 'propn', 'verb', 'pron', 'verb', 'punct'],
                                   [2, 7, 5, 5, 0, 7, 5, 5], ['det', 'obj', 'aux', 'nsubj', 'root', 'nsubj', 'ccomp', 'punct'])
        rules = t.rules()
        self.assertIn("verb(w5) ---> [ccomp:verb(w7)@1, aux:t(aux), nsubj:t(propn), ^'--':t(verb), "
                      "ccomp:verb(w7)@2, punct:t(punct)].", rules)
        self.assertIn("verb(w7) ---> [[obj:noun(w2)], [nsubj:t(pron), ^'--':t(verb)]].", rules)
        self.assertEqual(t.discontinuous(), [7])

    def test_malformed(self):
        with self.assertRaises(ValueError):
            deptree.DependencyTree(['w(x,a)', 'w(x,b)'], ['x', 'x'], [0, 0], ['root', 'root'])
        with self.assertRaises(ValueError):
            deptree.DependencyTree(['w(x,a)', 'w(x,b)', 'w(x,c)'], ['x'] * 3, [2, 1, 0], ['a', 'b', 'root'])

    def test_filename(self):
        self.assertEqual(deptree.filename('Tree IsThatAllYouGot-0'), 'Tree_IsThatAllYouGot-0')


class Cgel2plTest(unittest.TestCase):
    def test_program(self):
        p = cgel_program(WHICH)
        self.assertIn("w(v,bought,buy)", p)
        self.assertIn('clause(clause1) ---> [prenucleus:np(np1), ^head:clause(clause2)].', p)
        self.assertIn('nom(nom1) ---> [^det_head:dp(dp1)].', p)
        self.assertIn('vp(vp1) ---> [^head:t(v), obj:e(gap, np1)].', p)
        self.assertIn('fused(dp1, np1, det).', p)
        self.assertIn("punct(3, after, ['.']).", p)

    def test_words_in_order(self):
        # a phrase before a word in one rule: the words go in sentence order, not in the
        # order the rules are written (Italian is written after food's rule)
        p = cgel_program("""# sent_id = t
(NP
    :Head (Nom
        :Mod (AdjP
            :Head (Adj :t "Italian"))
        :Head (N :t "food")))
""")
        self.assertIn("sentence([\n  w(adj,'Italian'),\n  w(n,food)\n]).", p)

    def test_reader_keeps_order_and_variables(self):
        meta, tree = next(cgel2pl.blocks_text(WHICH))
        self.assertEqual(meta['sent_id'], 'which-liz-bought')
        self.assertEqual(tree['kids'][0]['var'], 'x')
        self.assertEqual(tree['kids'][0]['cat'], 'NP')
        self.assertEqual([k for k, _ in tree['kids'][1]['kids'][1]['kids'][0]['attrs']], ['t', 'l', 'p'])

    @unittest.skipUnless(SWIPL, 'needs swipl')
    def test_prolog(self):
        with tempfile.NamedTemporaryFile('w', suffix='.pl', delete=False, encoding='utf-8') as f:
            f.write(cgel_program(WHICH))
        out = swipl('cgel.pl', [f.name],
                    'aggregate_all(count, analysis(_), N), writeln(N), functions(Fs), '
                    'forall(member(function(dp1, P, F), Fs), writeln(P-F)), '
                    'reattached(R), forall(member(moved(A, K), R), writeln(moved(A, K)))')
        os.remove(f.name)
        self.assertEqual(out.split('\n')[0], '1')
        self.assertIn('nom1-head', out)            # the stored edge
        self.assertIn('np1-det', out)              # the edge fused/3 restores
        self.assertIn('moved(np1,gap)', out)


class Ud2plTest(unittest.TestCase):
    def test_programs(self):
        p1, p2 = ud_programs(CONLLU)
        self.assertIn("verb(w7) ---> [[obj:noun(w2)], [nsubj:t(pron), ^'--':t(verb)]].", p1)
        self.assertIn("feats(1, ['PronType=Int']).", p1)
        self.assertIn('edep(2, 7, obj).', p1)
        self.assertIn("mwt(2, 3, 'don''t').", p2)
        self.assertIn("w(part,'n''t',not)", p2)

    @unittest.skipUnless(SWIPL, 'needs swipl')
    def test_round_trip(self):
        paths = []
        for p in ud_programs(CONLLU):
            with tempfile.NamedTemporaryFile('w', suffix='.pl', delete=False, encoding='utf-8') as f:
                f.write(p)
            paths.append(f.name)
        gold = [[(r[0], r[6], 'root' if r[6] == '0' else r[7]) for r in rows if r[0].isdigit()]
                for _, rows in ud2pl.sentences(self._write(CONLLU))]
        for path, g in zip(paths, gold):
            out = swipl('dep.pl', [path], 'aggregate_all(count, analysis(_), N), writeln(N), arcs(A), print(A), nl')
            n, arcs = out.split('\n')[:2]
            self.assertEqual(n, '1')
            got = sorted(re.findall(r"arc\((\d+),(\d+),'?([a-z:]+)'?\)", arcs))
            self.assertEqual(got, sorted(g))
            os.remove(path)

    def _write(self, text):
        path = tempfile.mktemp(suffix='.conllu')
        with open(path, 'w', encoding='utf-8') as f:
            f.write(text)
        self.addCleanup(os.remove, path)
        return path


class Clear2plTest(unittest.TestCase):
    """clear2pl.convert on stand-ins for spaCy's tokens and sentence."""

    class Span(list):
        """A sentence: its tokens, start (the first token's index in the document),
        text and ents."""

    def sentence(self):
        rows = [('It', 'PRP', 'PRON', 'it', 'nsubj', 1, 'Case=Nom'),
                ('rained', 'VBD', 'VERB', 'rain', 'ROOT', 1, 'Tense=Past'),
                ('in', 'IN', 'ADP', 'in', 'prep', 1, ''),
                ('Paris', 'NNP', 'PROPN', 'Paris', 'pobj', 2, 'Number=Sing')]
        toks = [SimpleNamespace(i=10 + k, text=f, tag_=t, pos_=p, lemma_=l, dep_=d, morph=m)
                for k, (f, t, p, l, d, _, m) in enumerate(rows)]
        for tok, row in zip(toks, rows):
            tok.head = toks[row[5]]
        span = self.Span(toks)
        span.start, span.text = 10, 'It rained in Paris'
        span.ents = [SimpleNamespace(start=13, end=14, label_='GPE')]
        return span

    def test_convert(self):
        tree, facts = clear2pl.convert(self.sentence(), 'test#1')
        self.assertEqual(tree.root, 2)
        self.assertEqual(tree.rules(), ["vbd(w2) ---> [nsubj:t(prp), ^'--':t(vbd), prep:in(w3)].",
                                        "in(w3) ---> [^'--':t(in), pobj:t(nnp)]."])
        self.assertIn("entity(4, 4, 'GPE').", facts)
        self.assertIn('upos(2, verb).', facts)
        self.assertIn("w(vbd,rained,rain)", tree.words)


@unittest.skipUnless(SWIPL, 'needs swipl')
class ExamplesTest(unittest.TestCase):
    def test_examples(self):
        for name in sorted(os.listdir(EXAMPLES)):
            rules = 'cgel.pl' if name.startswith('cgel_') else 'dep.pl'
            out = swipl(rules, [os.path.join(EXAMPLES, name)], 'aggregate_all(count, analysis(_), N), writeln(N)')
            self.assertEqual(out.strip(), '1', name)


if __name__ == '__main__':
    unittest.main()
