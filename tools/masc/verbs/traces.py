"""The traces *T* and * at MASC's lexical verbs: extraction, passive, raising.

    python3 traces.py MASC_DATA_DIR OUT_DIR
    python3 traces.py --report OUT_DIR VERBS_JSONL

writes OUT_DIR/traces.jsonl, one JSON object per empty *T* or * argument of a
lexical verb (subject, complement or modifier, in the sense of verbframes.py,
whose definitions of lexical verb, clause, subject, complement and modifier
it reuses), with the verb, its frames, the trace, its coindexation chain and
how the chain ends, and OUT_DIR/traces_extra.json (some counts over
prepositional phrases and clauses); with --report, prints the tables of
docs/verbs/02-traces.md from those and verbframes.py's verbs.jsonl.

A chain is followed from an empty element through the index on its word
(*T*-1) to the node labelled with that index (WHNP-1, NP-SBJ-1; never NP=1,
which is gapping), and on while that node is itself empty. It ends:
overt (a node with words), null-op (WHNP-1 (-NONE- 0)), unindexed (an
empty element with no index: *, *PRO*), dangling (no node carries the
index in the tree), multiple (more than one does, and exactly one of them
is of the right sort, a WH phrase or topic for *T* and neither for *, and
c-commands the trace, is not the case; where exactly one is, the chain goes
on through it and the clash is recorded). An overt WH antecedent
or a null operator in a relative clause is taken one step further, to the
head noun phrase the relative clause modifies.
"""
import collections, json, os, re, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))
from masctrees import *
import verbframes as vf


def coindex(lbl):
    """The coindex of a phrase label (NP-SBJ-1 -> '1'), not a gapping one."""
    m = re.search(r'-(\d+)(?:=\d+)?$', lbl)
    return m.group(1) if m else None


def empty_word(n):
    """The word of the (first) empty element of an all-empty node."""
    for t, w in leaves(n.raw):
        if t == '-NONE-':
            return w
    return None


def word_kind_index(w):
    m = re.match(r'^(.*?)-(\d+)$', w)
    return (m.group(1), m.group(2)) if m else (w, None)


def index_table(root):
    tab = collections.defaultdict(list)
    for x in vf.nodes(root):
        if x.word is None:
            i = coindex(x.label)
            if i:
                tab[i].append(x)
    return tab


def relative_head(a):
    """For a WH antecedent a (or null operator) heading a relative clause
    (SBAR daughter of NP after an NP), that NP; else None."""
    s = a.parent
    if s is None or s.cat != 'SBAR' or s.parent is None or s.parent.cat != 'NP':
        return None
    np = s.parent
    before = [k for k in np.kids[:np.kids.index(s)] if k.cat in ('NP', 'NML', 'NX')]
    return before[-1] if before else None


def environment(a):
    """Where a WH antecedent (or a null operator) sits."""
    s = a.parent
    if s is None:
        return 'root'
    p = s.parent
    if s.cat == 'SBARQ':
        return 'direct question'
    if s.cat != 'SBAR':
        return 'in ' + s.cat
    if 'NOM' in s.tags:
        return 'free relative (SBAR-NOM)'
    if set(s.tags) & vf.MODIFIER_TAGS:
        return 'adverbial clause (SBAR-TMP, -LOC, -ADV ...)'
    if p is None:
        return 'SBAR at root'
    if p.cat == 'NP':
        if relative_head(a) is not None:
            return 'relative clause'
        return 'free relative (NP over SBAR)'
    if p.cat == 'VP' and vf.empty_kind(a.raw) is None:
        return 'embedded question or free relative under VP'
    if p.cat == 'VP':
        return '0 operator under VP (cleft, purpose, other)'
    if p.cat == 'ADJP':
        return 'under ADJP (tough, too/enough, comparative)'
    return 'SBAR under ' + p.cat


def resolve(tab, node, ambiguous):
    """Follows the chain from an all-empty node. Returns (end, steps, first
    antecedent, final antecedent)."""
    w = empty_word(node)
    steps, first, a = [], None, None
    seen = set()
    while True:
        kind, idx = word_kind_index(w)
        steps.append(kind)
        if idx is None:
            return 'unindexed', steps, first, a
        if idx in seen:
            return 'cycle', steps, first, a
        seen.add(idx)
        ants = [x for x in tab.get(idx, []) if x is not node]
        if not ants:
            return 'dangling', steps, first, a
        if len(ants) > 1:
            # an index used twice in one tree: keep the c-commanding ones
            # of the right sort (*T*: a WH phrase or a topic; *: neither)
            # and c-commanding the trace
            def fits(x):
                wh = x.cat.startswith('WH') or 'TPC' in x.tags
                return wh if kind == '*T*' else not wh
            cc = [x for x in ants if x.parent is not None and fits(x) and
                  is_under(node, x.parent) and not is_under(node, x)]
            if len(cc) != 1:
                return 'multiple', steps, first, ants[0]
            ambiguous.append(len(ants))
            ants = cc
        a = ants[0]
        if first is None:
            first = a
        k = vf.empty_kind(a.raw)
        if k is None:
            return 'overt', steps, first, a
        if k == '0':
            return 'null-op', steps, first, a
        node, w = a, empty_word(a)


def slots(vp):
    """The subject, complement and modifier nodes of a lexical verb's phrase,
    as verbframes.verb_record finds them."""
    n = vp.parent
    while n is not None and n.cat == 'VP':
        n = n.parent
    clause = n if n is not None and n.cat in vf.CLAUSES else None
    subj = None
    if clause is not None:
        subj = next((k for k in clause.kids if 'SBJ' in k.tags), None)
    head = next((k for k in vp.kids if k.word is not None and k.cat in vf.VERB_TAGS), None)
    comps, mods = [], []
    for d in vp.kids:
        if d is head or d.cat == '-NONE-' and d.word:
            continue
        r = vf.role(d)
        if r == 'complement':
            comps.append(d)
        elif r == 'modifier':
            mods.append(d)
    return clause, subj, comps, mods, head


def is_under(x, anc):
    while x is not None:
        if x is anc:
            return True
        x = x.parent
    return False


def by_phrase(vp):
    for d in vp.kids:
        if 'LGS' in d.tags:
            return True
        if d.cat == 'PP' and any('LGS' in k.tags for k in d.kids):
            return True
    return False


def crossed(x, a):
    """The clauses between a trace x and its antecedent a: the S-like
    nodes above x and below a's parent; None if a does not c-command x."""
    if a is None or a.parent is None or not is_under(x, a.parent):
        return None
    n, k = x.parent, 0
    while n is not a.parent:
        if n.cat in vf.CLAUSES:
            k += 1
        n = n.parent
    return k


def filler(end, first, final):
    """What, in words, fills the argument, and how it is found."""
    if end in ('unindexed', 'dangling', 'multiple', 'cycle'):
        return end
    if final.cat.startswith('WH'):
        rh = relative_head(final)
        if rh is not None and vf.words(rh.raw):
            return 'relative head (' + ('null operator' if end == 'null-op' else 'overt WH') + ')'
        if end == 'null-op':
            return 'null operator, no relative head'
        return 'WH phrase itself (question, free relative)'
    if end == 'null-op':
        return 'null operator (non-WH label)'
    return 'overt antecedent, non-WH'


def describe(x):
    if x is None:
        return None
    return {'label': x.label, 'words': ' '.join(vf.words(x.raw))[:60]}


def main(data, out_dir):
    out = open(os.path.join(out_dir, 'traces.jsonl'), 'w')
    T = collections.defaultdict(collections.Counter)  # table name -> counter
    for g, fid, i, t in masc_trees(data):
        raw = unwrap(t)
        if is_leaf(raw):
            continue
        root = vf.Node(raw)
        vf.number(root)
        tid = '%s/%s#%d' % (g, fid, i)
        tab = index_table(root)
        for vp in vf.nodes(root):
            if vp.cat != 'VP' or vf.is_aux_vp(vp):
                continue
            rec = vf.verb_record(tid, root, vp)
            if rec is None:
                continue
            clause, subj, comps, mods, head = slots(vp)
            T['verbs']['all'] += 1
            ctypes = [c.cat for c in comps]
            # prepositional passives: NP * inside a PP daughter, coindexed
            for d in comps + mods:
                if d.cat == 'PP' and vf.empty_kind(d.raw) is None:
                    for k in d.kids:
                        if k.cat == 'NP' and vf.empty_kind(k.raw) == '*':
                            T['prep passive']['PP with NP * (' + vf.role(d) + ')'] += 1
                        if k.cat == 'NP' and vf.empty_kind(k.raw) == '*T*':
                            T['stranded preposition']['PP with NP *T* (' + vf.role(d) + ')'] += 1
            # small-clause / ECM passives: an S complement whose subject is *
            for d in comps:
                if d.cat == 'S' and vf.empty_kind(d.raw) is None:
                    s = next((k for k in d.kids if 'SBJ' in k.tags), None)
                    if s is not None and vf.empty_kind(s.raw) == '*':
                        haspred = any(k.cat == 'VP' for k in d.kids)
                        T['S complement with * subject'][
                            ('infinitive/VP' if haspred else 'small clause, no VP')] += 1
            items = []
            if subj is not None:
                items.append(('subject', subj))
            items += [('complement', c) for c in comps] + [('modifier', m) for m in mods]
            for slot, x in items:
                k = vf.empty_kind(x.raw)
                # an SBAR of (-NONE- 0) and (S (-NONE- *T*-n)): count as *T*
                if k == '0' and x.cat == 'SBAR':
                    ws = [w for tg, w in leaves(x.raw)]
                    if len(ws) == 2 and ws[1].startswith('*T*'):
                        T['0+*T* SBAR'][slot] += 1
                        k = '*T*'
                        x = next(c for c in x.kids if c.cat != '-NONE-')
                if k not in ('*T*', '*'):
                    continue
                clash = []
                end, steps, first, final = resolve(tab, x, clash)
                r = {'id': tid, 'verb': head.word, 'tag': head.cat, 'slot': slot,
                     'label': x.label, 'kind': k, 'end': end, 'chain': steps,
                     'first': describe(first), 'final': describe(final),
                     'clash_settled': bool(clash),
                     'frame_full': rec['frame_full'], 'frame_backbone': rec['frame_backbone']}
                r['clauses_crossed'] = crossed(x, first)
                r['filler'] = filler(end, first, final)
                if k == '*' and slot == 'complement' and x.cat == 'NP':
                    r['to_own_subject'] = first is not None and first is subj
                    r['subject'] = rec['subject']['empty'] or 'overt' if rec['subject'] else None
                    r['by'] = by_phrase(vp)
                    r['vp_parent'] = vp.parent.cat if vp.parent else None
                    r['other_np'] = sum(1 for c in comps if c.cat == 'NP' and vf.empty_kind(c.raw) is None)
                if k == '*' and slot == 'subject':
                    # the clause's place: complement of which head?
                    p = clause.parent if clause is not None else None
                    ph = None
                    if p is not None:
                        ph = next((kk for kk in p.kids if kk.word is not None and kk.cat not in ('-NONE-',)), None)
                    r['matrix'] = (p.cat if p is not None else None,
                                   ph.word.lower() if ph is not None else None,
                                   ph.cat if ph is not None else None)
                    r['to_matrix_subject'] = False
                    if first is not None and p is not None:
                        q = p
                        while q is not None and q.cat == 'VP':
                            q = q.parent
                        if q is not None and first.parent is q and 'SBJ' in first.tags:
                            r['to_matrix_subject'] = True
                if final is not None and end in ('overt', 'null-op') and final.cat.startswith('WH'):
                    r['env'] = environment(final)
                    rh = relative_head(final)
                    r['relative_head'] = describe(rh)
                    r['wh'] = final.cat + ('(0)' if end == 'null-op' else '')
                    r['wh_words'] = ' '.join(vf.words(final.raw)).lower()
                elif final is not None and end == 'overt':
                    r['env'] = 'non-WH antecedent ' + final.cat + ''.join('-' + t for t in final.tags)
                out.write(json.dumps(r) + '\n')
    out.close()
    with open(os.path.join(out_dir, 'traces_extra.json'), 'w') as f:
        json.dump({k: dict(v) for k, v in T.items()}, f, indent=1)


SEMIMODAL = {'have', 'has', 'had', 'having', 'need', 'needs', 'needed', 'ought',
             'going', 'gon', 'used', 'got', 'gotta'}
ASPECTUAL = {'begin', 'began', 'begins', 'beginning', 'begun', 'start', 'started',
             'starts', 'starting', 'continue', 'continued', 'continues',
             'continuing', 'stop', 'stopped', 'stops', 'keep', 'kept', 'keeps',
             'keeping', 'finish', 'finished', 'cease', 'ceased', 'quit', 'resume'}
RAISING = {'seem', 'seems', 'seemed', 'appear', 'appears', 'appeared', 'tend',
           'tends', 'tended', 'happen', 'happens', 'happened', 'prove', 'proved',
           'turn', 'turned', 'fail', 'failed', 'fails', 'come', 'came'}


def matrix_class(r):
    cat, w, tag = r['matrix']
    if r['end'] == 'unindexed' and not r['first']:
        return 'unindexed * (no antecedent)'
    if not r['to_matrix_subject']:
        if cat == 'ADJP':
            return 'raising adjective (likely, about, sure)'
        return 'antecedent not the matrix subject (help/allow NP, other)'
    if cat != 'VP':
        return 'matrix ' + str(cat)
    if w in SEMIMODAL:
        return 'semi-modal (have/need/going/ought/used to)'
    if w in ASPECTUAL:
        return 'aspectual (begin/start/continue/stop/keep)'
    if w in RAISING:
        return 'raising verb (seem/appear/tend/happen/fail)'
    if tag == 'VBN':
        return 'passive matrix (be expected/supposed/said to)'
    return 'other matrix verb'


def table(title, counter, total=None):
    print('\n### ' + title)
    total = total or sum(counter.values())
    print('| | n | % |\n|---|---:|---:|')
    for k, v in counter.most_common():
        print('| %s | %d | %.1f |' % (k, v, 100 * v / total))
    print('| total | %d | |' % sum(counter.values()))


def report(out_dir, verbs_path):
    R = [json.loads(l) for l in open(os.path.join(out_dir, 'traces.jsonl'))]
    X = json.load(open(os.path.join(out_dir, 'traces_extra.json')))
    V = [json.loads(l) for l in open(verbs_path)]

    def grp(r):
        if r['slot'] == 'subject':
            return 'subject'
        if r['slot'] == 'modifier':
            return 'modifier'
        if r['label'].startswith('NP') and 'PRD' not in r['label']:
            return 'object NP'
        if 'PRD' in r['label']:
            return 'predicative'
        if r['label'].split('-')[0] in ('S', 'SBAR', 'SQ', 'SBARQ', 'UCP'):
            return 'clausal complement'
        return 'other complement'
    table('Empty *T* and * arguments at lexical verbs, by kind and slot',
          collections.Counter('%s %s' % (r['kind'], grp(r)) for r in R))
    T = [r for r in R if r['kind'] == '*T*']
    for g in ('subject', 'object NP', 'predicative', 'clausal complement', 'modifier'):
        xs = [r for r in T if grp(r) == g]
        table('*T* %s: antecedent' % g, collections.Counter(
            r.get('wh') or ('non-WH ' + r['env'].split()[-1] if 'env' in r else r['end']) for r in xs))
        table('*T* %s: environment' % g, collections.Counter(r.get('env', r['end']) for r in xs))
    for g in ('subject', 'object NP'):
        xs = [r for r in T if grp(r) == g and r.get('env') == 'relative clause']
        def rel(r):
            w = r['wh_words']
            if r['wh'].endswith('(0)'):
                return '0'
            if w.startswith('whose'):
                return 'whose N'
            if ' of wh' in w:
                return 'Q of which/whom'
            return w if w in ('that', 'who', 'which', 'whom', 'what') else 'other'
        table('*T* %s in relative clauses: relativizer' % g, collections.Counter(rel(r) for r in xs))
    P = [r for r in R if r['kind'] == '*' and grp(r) == 'object NP']
    def pclass(r):
        if r['end'] in ('multiple', 'dangling', 'cycle') and r['first'] is None:
            return 'index clash or dangling index'
        if r['first'] is None:
            return 'unindexed, VP under %s' % ('NP (reduced relative)' if r['vp_parent'] == 'NP' else 'other')
        if r['to_own_subject']:
            return 'coindexed with own subject: ' + (r['subject'] or '?')
        return 'coindexed with another node'
    table('* objects (passives): antecedent', collections.Counter(pclass(r) for r in P))
    table('* objects (passives): by-phrase (PP with NP-LGS)', collections.Counter(
        ('indexed' if r['first'] else 'unindexed') + (', by-phrase' if r['by'] else ', no by-phrase') for r in P))
    table('* objects (passives): verb tag', collections.Counter(r['tag'] for r in P))
    table('* objects (passives): overt NP left beside the trace', collections.Counter(r['other_np'] for r in P))
    S = [r for r in R if r['kind'] == '*' and r['slot'] == 'subject']
    table('* subjects: where the clause is', collections.Counter(matrix_class(r) for r in S))
    for k in ('*T*', '*'):
        for g in ('subject', 'object NP', 'modifier'):
            xs = [r for r in R if r['kind'] == k and grp(r) == g]
            if xs:
                table('%s %s: what fills the argument' % (k, g), collections.Counter(r['filler'] for r in xs))
                table('%s %s: clauses crossed between trace and antecedent' % (k, g),
                      collections.Counter(str(r['clauses_crossed']) for r in xs))
    table('index clashes settled by sort and c-command', collections.Counter(
        '%s %s' % (r['kind'], r['end']) for r in R if r['clash_settled'] or r['end'] == 'multiple'))
    for k, c in X.items():
        table(k, collections.Counter(c))
    # backbone frames
    def kind(c):
        return '*T*' if c['empty'] == '0' and c['cat'] == 'SBAR' else c['empty']
    def isobj(c):
        return c['cat'] == 'NP' and 'PRD' not in c['tags']
    fr = collections.Counter()
    for r in V:
        cs = r['complements']
        objs = [c for c in cs if isobj(c)]
        ov = [c for c in objs if c['realization'] == 'overt']
        if objs:
            fr['full frame has an NP object'] += 1
        if objs and not ov:
            fr['backbone loses every NP object: ' + '+'.join(sorted(set(kind(c) for c in objs)))] += 1
        if len(objs) >= 2 and len(ov) == 1:
            fr['two NP objects, backbone keeps one: ' + '+'.join(sorted(set(kind(c) for c in objs if c not in ov)))] += 1
        for cls, test in (('predicative', lambda c: 'PRD' in c['tags']),
                          ('clausal', lambda c: c['cat'] in ('S', 'SBAR', 'SQ', 'SBARQ') and 'PRD' not in c['tags'])):
            f = [c for c in cs if test(c)]
            if f and not [c for c in f if c['realization'] == 'overt']:
                fr['backbone loses the %s complement: %s' % (cls, '+'.join(sorted(set(kind(c) for c in f))))] += 1
    table('Backbone frames (all %d verbs)' % len(V), fr, len(V))
    vbn = collections.Counter()
    for r in V:
        if r['tag'] != 'VBN':
            continue
        objs = [c for c in r['complements'] if isobj(c)]
        if any(c['realization'] == 'overt' for c in objs):
            continue
        ks = set(c['empty'] for c in objs)
        vbn['passive, * object' if '*' in ks else '*T* object' if '*T*' in ks else
            'other empty object' if ks else 'no NP object in the full frame'] += 1
    table('VBN verbs with no overt NP object in the backbone', vbn)


if __name__ == '__main__':
    if sys.argv[1] == '--report':
        report(sys.argv[2], sys.argv[3])
    else:
        main(sys.argv[1], sys.argv[2])
