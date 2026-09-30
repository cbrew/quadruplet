"""Which of a verb's PPs are complements: the sources against CGELBank's gold.

    uv run python cgel/evaluate_cgel.py CGELBANK_DATASETS EXAMPLES_TSV NLTK_DATA_DIR [OUT_TSV] [--trial]
        [--relabelled=CGEL_EXAMPLES_TSV --theories=DIR] [--propbank=PROPBANK_EWT_DIR]
        [--frames=PROPBANK_FRAMES_DIR]

CGELBANK_DATASETS is nert-nlp/cgel's datasets/ directory; EXAMPLES_TSV the
MASC PP examples pp_examples.pl writes (for the rate table); NLTK_DATA_DIR
holds WordNet and VerbNet 3.3, as for build.py.

The gold is every PP of a VP in CGELBank's gold trees (ewt, twitter,
ewt-test_iaa50, ewt-test_pilot5), Comp or Mod, aligned with its UD PP. The
sources, each deciding complement or not for a UD PP:

  masc_table   the MASC rate table, P(complement | lemma, prep) >= 0.5
  verbnet      VerbNet licenses the preposition for the verb (vn_prep)
  masc_rules   the theory Aleph learned on MASC with VerbNet (theories/verbnet.pl)
  cgel_tests   a decision list of CGEL's criteria, fixed before looking at the gold:
                 a passive's by-phrase (obl:agent)              complement
                 be with a PP                                   complement
                 an object of time (WordNet noun.time)          modifier
                 a clause-taking preposition of time, cause or
                 condition (before, after, because, if ...)     modifier
                 now, then, so                                  modifier
                 an intransitive preposition with a verb of
                 motion or position                             complement
               and where none applies, masc_table or verbnet
  cgel_tests3  cgel_tests2, with the prepositional verbs of CGEL ch. 4 §6.1.2
               (prepositional_verbs.tsv) as a test, after now/then/so: Comp
  cgel_tests2  the same, with the passive test as EWT needs it: EWT labels a
               passive's by-phrase plain obl, so a by-PP of a past participle
               is the agent. (This was found by looking at the gold, where
               cgel_tests never fired; --trial tests it on CGELBank's trial
               trees, which nothing here was fitted to.)

With --relabelled and --theories, the sources learned from MASC relabelled
by CGEL's tests (relabel.py) are scored too:

  relabel_table      the rate table of the relabelled data
  learned_B          each theory Aleph learned on it, DIR/B/theory.pl
  cgel_tests2+X      cgel_tests2 with X as the fallback in place of masc_table or verbnet

With --propbank (propbank-release's data/google/ewt), each PP is given the
PropBank label of the argument of its verb that covers it (propbank_ewt.py):
a numbered argument, whose role the verb's roleset gives, is Comp; a
modifier (ARGM-) is Mod. PPs of sentences PropBank lacks (Twitter), or
that no argument covers, have none; they are scored apart. With --frames too
(propbank-frames), each covered PP gets its roles in CGEL's terms
(propbank_roles.py), which the learned theories with role/2 can use.

With --trial, the gold is CGELBank's trial trees (datasets/trial/: ewt-trial,
twitter-etc-trial), annotated but not adjudicated by both annotators.
"""
import collections
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
from build import SPATIAL, Senses, VerbNet, examples, prior, scores  # noqa: E402
from lexicon import prepositional_verbs  # noqa: E402
from ud_pps import align, conllu, gold, pps  # noqa: E402

LEXICON = prepositional_verbs()

FILES = ['ewt', 'twitter', 'ewt-test_iaa50', 'ewt-test_pilot5']
TRIAL = ['trial/ewt-trial', 'trial/twitter-etc-trial']
ADVERBIAL_P = {'before', 'after', 'while', 'until', 'till', 'since', 'because', 'if', 'unless',
               'although', 'though', 'once', 'when', 'whenever', 'as', 'whereas', 'whilst'}
TIME_P = {'now', 'then', 'so'}
MOTION = {'verb.motion', 'verb.contact'}


def features(p, senses, vn):
    """The MASC learners' features, for a UD PP."""
    if p['rel'] == 'advcl':
        osense = 'clause'
    elif p['rel'] == 'advmod':
        osense = 'none'
    elif p['obj_upos'] == 'PRON':
        osense = 'pronoun'
    elif p['obj_upos'] in ('NOUN', 'PROPN', 'NUM'):
        osense = senses.of(p['obj_lemma'], 'n')
    else:
        osense = 'none'
    tops, groups, preps, spatial = vn.of(p['lemma'])
    return {
        'lemma': p['lemma'], 'prep': p['prep'], 'verb_sense': senses.of(p['lemma'], 'v'),
        'obj_sense': osense, 'obj_cat': {'np': 'np', 'clause': 's', 'none': 'none'}[p['obj_cat']],
        'vtag': p['vtag'], 'next': p['next'], 'obj_before': p['obj_before'],
        'other_pp': p['other_pp'], 'passive': p['passive'], 'copula': p['lemma'] == 'be',
        'obj_head': p['obj_lemma'], 'cgel_lex': (p['lemma'], p['prep']) in LEXICON,
        'vn_class': tops, 'vn_group': groups, 'vn_prep': p['prep'] in preps,
        'vn_spatial': spatial and p['prep'] in SPATIAL, 'vn_none': not tops,
    }


def theory(path):
    clauses = []
    for m in re.finditer(r'complement\(A\)\s*:-(.*?)\.\s*$', open(path).read(), re.S | re.M):
        clauses.append([(l.group(1), l.group(2).strip("'") if l.group(2) else None)
                        for l in re.finditer(r"(\w+)\(A(?:,('[^']*'|[^)]*))?\)", m.group(1))])
    return clauses


def covers(clauses, f):
    def holds(p, arg):
        v = f.get(p)
        if arg is None:
            return v is True
        return arg in v if isinstance(v, list) else str(v) == arg
    return any(all(holds(p, a) for p, a in body) for body in clauses)


def cgel_tests(p, f, fallback, by_agent=False, lexicon=False):
    """(decision, the test that decided)"""
    if p['rel'] == 'obl:agent' or by_agent and p['prep'] == 'by' and p['vtag'] == 'vbn':
        return True, 'agent'
    if p['rel'] == 'cop':
        return True, 'be+PP'
    if f['obj_sense'] == 'noun.time':
        return False, 'time object'
    if p['rel'] == 'advcl' and p['prep'] in ADVERBIAL_P:
        return False, 'adverbial clause'
    if p['prep'] in TIME_P:
        return False, 'now/then/so'
    if lexicon and f['cgel_lex']:
        return True, 'CGEL prepositional verb'
    if p['rel'] == 'advmod' and (f['verb_sense'] in MOTION or f['vn_spatial']):
        return True, 'locative with motion verb'
    return fallback, 'evidence'


def main(datasets, masc_tsv, nltk_dir, out=None, trial=False, relabelled=None, theories=None,
         propbank=None, frames_dir=None):
    senses = Senses(nltk_dir)
    vn = VerbNet(nltk_dir)
    table = prior(list(examples(masc_tsv, senses, vn)))
    rules = theory(os.path.join(HERE, '..', 'theories', 'verbnet.pl'))
    extra = []
    if relabelled:
        from relabel import read, relabel
        rows = [(doc, path, pp, 'complement' if relabel(kind, ls, f)[0] else 'adjunct', f)
                for doc, path, pp, kind, ls, f in read(relabelled, senses, vn)]
        rtable = prior(rows)
        extra.append(('relabel_table', lambda f: rtable(f['lemma'], f['prep']) >= 0.5))
    for b in sorted(os.listdir(theories)) if theories else []:
        if os.path.exists(os.path.join(theories, b, 'theory.pl')):
            extra.append(('learned_' + b, lambda f, t=theory(os.path.join(theories, b, 'theory.pl')):
                          covers(t, f)))
    pairs, missed = [], 0
    for name in (TRIAL if trial else FILES):
        by = {}
        for n, (sid, text, toks) in enumerate(conllu(os.path.join(datasets, name + '.conllu')), 1):
            by[sid if name != 'twitter' else '#%d' % n] = pps(sid, text, toks)
        g = gold(os.path.join(datasets, name + '.cgel'))
        ps, ms = align(g, by)
        pairs += ps
        missed += len(ms)
    print('gold PPs of VPs: %d aligned to UD, %d not; %d Comp, %d Mod' % (
        len(pairs), missed, sum(f == 'Comp' for f, _ in pairs), sum(f == 'Mod' for f, _ in pairs)))
    rows = []
    pbdata = fr = None
    if propbank:
        from propbank_ewt import read
        pbdata = read(propbank)
        if frames_dir:
            from propbank_roles import frames
            fr = frames(frames_dir)
    for func, p in pairs:
        f = features(p, senses, vn)
        f['role'] = []
        if pbdata is not None:
            hit = propbank_label(pbdata, p)
            if hit and fr is not None:
                from propbank_roles import roles
                f['role'] = roles(hit[0], hit[1], fr)
        r = dict(gold=func == 'Comp', p=p, f=f)
        r['masc_table'] = table(p['lemma'], p['prep']) >= 0.5
        r['verbnet'] = f['vn_prep']
        r['masc_rules'] = covers(rules, f)
        r['cgel_tests'], r['test'] = cgel_tests(p, f, r['masc_table'] or r['verbnet'])
        r['cgel_tests2'], r['test2'] = cgel_tests(p, f, r['masc_table'] or r['verbnet'], by_agent=True)
        r['cgel_tests3'], r['test3'] = cgel_tests(p, f, r['masc_table'] or r['verbnet'], by_agent=True,
                                                  lexicon=True)
        for name, source in extra:
            r[name] = source(f)
            r['cgel_tests2+' + name] = cgel_tests(p, f, r[name], by_agent=True)[0]
            r['cgel_tests3+' + name] = cgel_tests(p, f, r[name], by_agent=True, lexicon=True)[0]
        rows.append(r)
    print('\n%-30s %s' % ('source', 'agreement with CGELBank'))
    print('%-30s %s' % ('all Mod', scores([(r['gold'], False) for r in rows])))
    for s in ('masc_table', 'verbnet', 'masc_rules'):
        print('%-30s %s' % (s, scores([(r['gold'], r[s]) for r in rows])))
    print('%-30s %s' % ('masc_table or verbnet', scores([(r['gold'], r['masc_table'] or r['verbnet']) for r in rows])))
    print('%-30s %s' % ('masc_table or masc_rules', scores([(r['gold'], r['masc_table'] or r['masc_rules']) for r in rows])))
    print('%-30s %s' % ('cgel_tests', scores([(r['gold'], r['cgel_tests']) for r in rows])))
    print('%-30s %s' % ('cgel_tests2', scores([(r['gold'], r['cgel_tests2']) for r in rows])))
    print('%-30s %s' % ('cgel_tests3', scores([(r['gold'], r['cgel_tests3']) for r in rows])))
    lex = [r for r in rows if r['test3'] == 'CGEL prepositional verb']
    print('  the prepositional verb test decides %d, agrees with the gold on %d: %s' % (
        len(lex), sum(r['gold'] for r in lex), ', '.join('%s %s' % (r['p']['lemma'], r['p']['prep']) for r in lex)))
    for name, _ in extra:
        print('%-30s %s' % (name, scores([(r['gold'], r[name]) for r in rows])))
    for name, _ in extra:
        print('%-30s %s' % ('cgel_tests2+' + name, scores([(r['gold'], r['cgel_tests2+' + name]) for r in rows])))
    for name, _ in extra:
        print('%-30s %s' % ('cgel_tests3+' + name, scores([(r['gold'], r['cgel_tests3+' + name]) for r in rows])))
    for name, _ in extra:
        ev = [r for r in rows if r['test2'] == 'evidence']
        print('  on the %d the tests leave to evidence, %-22s %s' % (
            len(ev), name, scores([(r['gold'], r[name]) for r in ev])))
    if extra:
        ev = [r for r in rows if r['test2'] == 'evidence']
        print('  on the %d the tests leave to evidence, %-22s %s' % (
            len(ev), 'masc_table or verbnet', scores([(r['gold'], r['cgel_tests2']) for r in ev])))
    print('\nthe CGEL tests of cgel_tests2, one by one:')
    for t in ('agent', 'be+PP', 'time object', 'adverbial clause', 'now/then/so',
              'locative with motion verb', 'evidence'):
        rs = [r for r in rows if r['test2'] == t]
        right = sum(r['cgel_tests2'] == r['gold'] for r in rs)
        print('  %-26s decides %3d, agrees with the gold on %3d' % (t, len(rs), right))
    print('\nby UD relation (cgel_tests2):')
    for rel in ('obl', 'obl:agent', 'advcl', 'advmod', 'cop'):
        rs = [r for r in rows if r['p']['rel'] == rel]
        if rs:
            print('  %-10s %3d  Comp %3d  %s' % (rel, len(rs), sum(r['gold'] for r in rs),
                                                scores([(r['gold'], r['cgel_tests2']) for r in rs])))
    if propbank:
        report_propbank(rows, pbdata, [name for name, _ in extra])
    if out:
        with open(out, 'w') as o:
            o.write('gold\tlemma\tprep\trel\tmasc_table\tverbnet\tmasc_rules\tcgel_tests2\ttest\tpp\ttext\n')
            for r in rows:
                p = r['p']
                o.write('\t'.join(str(x) for x in (
                    'Comp' if r['gold'] else 'Mod', p['lemma'], p['prep'], p['rel'], int(r['masc_table']),
                    int(r['verbnet']), int(r['masc_rules']), int(r['cgel_tests2']), r['test2'],
                    p['pp_words'], p['text'])) + '\n')


def propbank_label(pb, p):
    """(label, roleset) of the argument of p's verb covering most of the PP, or None"""
    s = pb.get(p['sid'])
    if s is None:
        return None
    span = {i - 1 for i in p['span']}
    best = None
    for i, _lemma, roleset, args in s[1]:
        if i != p['verb_id'] - 1:
            continue
        for label, first, last in args:
            n = len(span & set(range(first, last + 1)))
            if n and (best is None or n > best[0]):
                best = (n, label, roleset)
    return best and best[1:]


def cgel_kind(label):
    """PropBank's label read as CGEL would: as kind(), except that goals and
    directions of motion (ARGM-DIR, ARGM-GOL) are complements (CGEL ch. 4 §5.2)"""
    from propbank_ewt import kind
    return 'Comp' if re.sub(r'^[RC]-', '', label) in ('ARGM-DIR', 'ARGM-GOL') else kind(label)


def report_propbank(rows, pb, extra):
    from propbank_ewt import kind
    for r in rows:
        r['pb'] = propbank_label(pb, r['p'])
    have = [r for r in rows if r['pb']]
    print('\nPropBank: %d of %d PPs covered by an argument of their verb (%d in sentences PropBank lacks)'
          % (len(have), len(rows), sum(r['p']['sid'] not in pb for r in rows)))
    if not have:
        return
    print('%-34s %s' % ('on those, PropBank (ARGn Comp)', scores([(r['gold'], kind(r['pb'][0]) == 'Comp') for r in have])))
    print('%-34s %s' % ('on those, PropBank, DIR/GOL Comp', scores([(r['gold'], cgel_kind(r['pb'][0]) == 'Comp') for r in have])))
    for name in ['cgel_tests2', 'cgel_tests3'] + extra + ['cgel_tests2+' + e for e in extra]:
        print('%-34s %s' % ('on those, ' + name, scores([(r['gold'], r[name]) for r in have])))
    table = collections.Counter((re.sub(r'^[RC]-', '', r['pb'][0]), 'Comp' if r['gold'] else 'Mod') for r in have)
    print('  PropBank label by CGELBank function:')
    for label in sorted({l for l, _ in table}):
        print('    %-10s Comp %3d  Mod %3d' % (label, table[label, 'Comp'], table[label, 'Mod']))
    print('  disagreements:')
    for r in have:
        if (kind(r['pb'][0]) == 'Comp') != r['gold']:
            print('    CGEL %-4s PropBank %-9s %-16s %s | %s' % (
                'Comp' if r['gold'] else 'Mod', r['pb'][0], r['pb'][1], r['p']['pp_words'], r['p']['text'][:90]))
    ev = [r for r in have if r['test2'] == 'evidence']
    if ev:
        print('  on the %d the CGEL tests leave to evidence and PropBank covers:' % len(ev))
        print('    %-30s %s' % ('PropBank', scores([(r['gold'], kind(r['pb'][0]) == 'Comp') for r in ev])))
        print('    %-30s %s' % ('masc_table or verbnet', scores([(r['gold'], r['cgel_tests2']) for r in ev])))
        for e in extra:
            print('    %-30s %s' % (e, scores([(r['gold'], r[e]) for r in ev])))
    print('%-34s %s' % ('all: cgel_tests2, PropBank fallback', scores([(r['gold'], r['cgel_tests2'] if r['test2'] != 'evidence' or not r['pb'] else kind(r['pb'][0]) == 'Comp') for r in rows])))
    print('%-34s %s' % ('all: cgel_tests2, PropBank DIR/GOL', scores([(r['gold'], r['cgel_tests2'] if r['test2'] != 'evidence' or not r['pb'] else cgel_kind(r['pb'][0]) == 'Comp') for r in rows])))
    if extra:
        e = extra[0]
        print('%-34s %s' % ('all: cgel_tests2, PropBank DIR/GOL, else ' + e, scores([(r['gold'], r['cgel_tests2'] if r['test2'] != 'evidence' else (cgel_kind(r['pb'][0]) == 'Comp' if r['pb'] else r[e])) for r in rows])))


if __name__ == '__main__':
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    opts = dict(a[2:].split('=', 1) for a in sys.argv[1:] if a.startswith('--') and '=' in a)
    main(*args[:4], trial='--trial' in sys.argv, relabelled=opts.get('relabelled'),
         theories=opts.get('theories'), propbank=opts.get('propbank'), frames_dir=opts.get('frames'))
