"""The lexical verbs of MASC whose subject is *PRO*: what construction the
clause comes from, what controls the empty subject, and what the context-free
backbone makes of the clause.

    python3 pro_subjects.py MASC_DATA_DIR ANNOTATED_JSONL OUT_JSONL

Walks the raw trees with the same definitions as ../verbframes.py (lexical
verb, clause, subject), keeps the verbs whose clause has an -SBJ daughter
that is *PRO* (with or without an index), and for each writes a JSON object
to OUT_JSONL and adds it to the tables printed at the end:

* construction: what the clause is, from its function tags and its parent
  (imperative, tagged or not; gerund S-NOM by position; adverbial S-ADV,
  S-PRP ...; complement of a verb, of an adjective, of a noun; wh-infinitive;
  predicative; ...);
* control: the node the *PRO*'s index points to, and its relation to the
  nearest clause above (subject of it, object in the verb phrase between,
  subject of a clause further up, ...); 'arbitrary' where there is no index;
* backbone: the label of the node spanning the clause in the backbone tree
  (annotated.jsonl, written by ../treebank.py), e.g. SxVP where the S over
  its subjectless VP was collapsed, S where it survived.
"""
import collections, json, os, re, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))
from masctrees import *
from verbframes import Node, number, nodes, is_aux_vp, empty_kind, words, CLAUSES, VERB_TAGS

ADVERBIAL = {'ADV', 'PRP', 'MNR', 'TMP', 'LOC', 'DIR', 'EXT', 'BNF', 'CND'}


def index_of(label):
    m = re.search(r'-(\d+)(?:=\d+)?$', label)
    return m.group(1) if m else None


def pro_index(subj):
    ls = leaves(subj.raw)
    m = re.match(r'\*PRO\*?-(\d+)$', ls[0][1])
    return m.group(1) if m else None


def lexical_head(vp):
    return next((k for k in vp.kids if k.word is not None and k.cat in VERB_TAGS), None)


def clause_of(vp):
    n = vp.parent
    while n is not None and n.cat == 'VP':
        n = n.parent
    return n if n is not None and n.cat in CLAUSES else None


def first_verb(c):
    """The first verb or TO of a clause's verb phrase chain."""
    vp = next((k for k in c.kids if k.cat == 'VP'), None)
    while vp is not None:
        for k in vp.kids:
            if k.word is not None and (k.cat in VERB_TAGS or k.cat in ('TO', 'MD')):
                return k
        vp = next((k for k in vp.kids if k.cat == 'VP'), None)
    return None


ROOTISH = {'ROOT', 'FRAG', 'PRN', 'INTJ', 'X', 'UCP', 'SINV', 'SQ', 'SBARQ', 'S', 'LST'}
BE = {'is', "'s", 'was', 'be', 'are', 'were', 'been', 'being', "'re", 'am', "'m"}


def construction(c, displaced):
    """(group, construction, verb form) of a clause with a *PRO* subject.
    displaced: the indices that an *EXP*, *ICH* or *RNR* points to."""
    ts, p = set(c.tags), c.parent
    pc = p.cat if p is not None else 'ROOT'
    fv = first_verb(c)
    form = 'to-inf' if fv is not None and fv.cat == 'TO' else (fv.cat if fv is not None else '?')
    ix = index_of(c.label)
    if 'IMP' in ts:
        return 'imperative', 'S-IMP', form
    if ix and ix in displaced:
        return 'displaced clause', displaced[ix] + ' (' + ('S-' + '-'.join(sorted(ts)) if ts else 'S') + ')', form
    if 'NOM' in ts:
        if 'SBJ' in ts:
            return 'gerund (S-NOM)', 'subject', form
        if pc == 'PP':
            return 'gerund (S-NOM)', 'object of preposition', form
        if pc == 'VP':
            return 'complement of verb', 'gerund S-NOM', form
        return 'gerund (S-NOM)', 'other (under ' + pc + ')', form
    adv = ts & ADVERBIAL
    if adv:
        return 'adverbial', 'S-' + sorted(adv)[0], form
    if 'PRD' in ts:
        return 'predicative / subject', 'S-PRD', form
    if 'SBJ' in ts:
        return 'predicative / subject', 'S-SBJ', form
    if 'TPC' in ts:
        return 'other', 'S-TPC', form
    if pc == 'VP':
        h = next((k for k in p.kids if k.word is not None and k.cat in VERB_TAGS), None)
        if h is not None and h.word.lower() in BE and not ts and not any('PRD' in k.tags for k in p.kids):
            return 'predicative / subject', 'untagged S after be', form
        return 'complement of verb', 'S-CLR' if 'CLR' in ts else 'S', form
    if pc == 'SBAR':
        gp = p.parent.cat if p.parent is not None else 'ROOT'
        wh = any(k.cat.startswith('WH') for k in p.kids)
        comp = next((k.word for k in p.kids if k.word is not None and k.cat == 'IN'), None)
        if wh:
            return 'wh- or relative infinitive', 'SBAR under ' + gp, form
        if comp:
            if 'ADV' in p.tags or 'TMP' in p.tags or 'PRP' in p.tags or gp in ('VP', 'S', 'ROOT') and comp.lower() != 'whether':
                return 'adverbial', 'SBAR with ' + comp.lower(), form
            return 'other', 'SBAR with ' + comp.lower() + ' under ' + gp, form
        return 'other', 'SBAR without complementizer under ' + gp, form
    if pc == 'ADJP':
        return 'complement of adjective', 'S', form
    if pc in ('NP', 'NML'):
        return 'complement of noun', 'S', form
    if pc == 'PP':
        return 'other', 'S under PP (not NOM)', form
    if pc in ROOTISH:
        where = 'root' if pc == 'ROOT' else 'under ' + pc
        if pro_index_node(c) is None and form == 'VB':
            return 'imperative', 'untagged, ' + where, form
        if form in ('VBP', 'VBD', 'VBZ', 'MD'):
            return 'subject drop (main clause)', 'finite, ' + where, form
        if form in ('VBG', 'VBN'):
            return 'subject drop (main clause)', form + ', ' + where, form
        return 'other', 'clause ' + where, form
    return 'other', 'under ' + pc, form


def pro_index_node(c):
    s = next(k for k in c.kids if 'SBJ' in k.tags)
    return pro_index(s)


def control(c, root, byindex):
    """(relation, antecedent node, overt end of its chain, duplicate index?)
    for the clause's *PRO*. The antecedent is a node whose label carries the
    *PRO*'s index; outside the clause and c-commanding it where there is a
    choice."""
    s = next(k for k in c.kids if 'SBJ' in k.tags)
    ix = pro_index(s)
    if ix is None:
        return 'arbitrary (no index)', None, None, False
    inside = lambda n, anc: any(x is n for x in nodes(anc))
    cands = [n for n in byindex.get(ix, []) if not inside(n, s)]
    dup = len(cands) > 1
    anc = set()
    p = c.parent
    while p is not None:
        anc.add(id(p))
        p = p.parent
    cands.sort(key=lambda n: (inside(n, c), id(n.parent) not in anc, empty_kind(n.raw) is not None))
    if not cands:
        return 'index with no antecedent', None, None, dup
    a = cands[0]
    path, p = [], c.parent
    while p is not None and p.cat not in CLAUSES:
        path.append(p)
        p = p.parent
    m = p
    if inside(a, c):
        rel = 'antecedent only inside the clause'
    elif m is not None and a.parent is m and 'SBJ' in a.tags:
        passive = any(k.cat == 'NP' and empty_kind(k.raw) == '*' and
                      leaves(k.raw)[0][1].endswith('-' + ix)
                      for v in path if v.cat == 'VP' for k in v.kids)
        rel = 'subject of nearest clause' + (' (passive: via object trace)' if passive else '')
    elif any(a.parent is v and v.cat == 'VP' for v in path):
        rel = 'object in the governing VP' if a.cat == 'NP' else 'other daughter of the governing VP'
        if a.cat == 'NP' and empty_kind(a.raw) == '*':
            rel += ' (passive trace)'
    elif any(inside(a, v) for v in path if v.cat == 'VP'):
        rel = 'object of a preposition in the governing VP' if a.parent.cat == 'PP' else 'elsewhere in the governing VP'
    elif any(inside(a, v) for v in path):
        rel = 'inside the governing phrase (' + path[0].cat + ')'
    elif m is not None and inside(a, m):
        rel = 'elsewhere in nearest clause'
    elif 'SBJ' in a.tags:
        rel = 'subject of a higher clause'
    else:
        rel = 'other'
    b = a
    for _ in range(5):
        if empty_kind(b.raw) is None:
            break
        m2 = re.search(r'-(\d+)$', leaves(b.raw)[0][1])
        nxt = [n for n in byindex.get(m2.group(1), []) if n is not b] if m2 else []
        if not nxt:
            break
        nxt.sort(key=lambda n: empty_kind(n.raw) is not None)
        b = nxt[0]
    return rel, a, b, dup


def span(n):
    ps = [x.pos for x in nodes(n) if x.word is not None and x.pos is not None]
    return (min(ps), max(ps) + 1) if ps else None


KIDS = {}  # span -> the daughters of the outermost backbone node with it


def backbone_spans(tree):
    out = {}
    KIDS.clear()
    def walk(n, i):
        if 'w' in n:
            return i + 1
        j = i
        for k in n['k']:
            j = walk(k, j)
        out.setdefault((i, j), []).append(n['c'].replace('[]', ''))
        KIDS[(i, j)] = tuple(re.sub(r'(ph)?\[\]', '', k['c']) for k in n['k'])
        return j
    walk(tree, 0)
    return out  # innermost first


# verbs whose S complement is either raising (*-n) or control (*PRO*-n)
GOVERNORS = ['seem', 'seems', 'seemed', 'appear', 'appears', 'tend', 'begin', 'began',
             'start', 'started', 'continue', 'continued', 'going', 'have', 'has', 'had',
             'need', 'needs', 'happen', 'got', 'want', 'wanted', 'try', 'tried',
             'decided', 'asked', 'told', 'promised', 'persuade', 'expected', 'allowed']


def main(data, ann, outp):
    bb = {}
    for l in open(ann):
        r = json.loads(l)
        bb[r['id']] = r['tree']['k'][0]
    out = open(outp, 'w')
    T = collections.defaultdict(collections.Counter)
    clauses_seen = set()
    total = 0
    for g, fid, i, t in masc_trees(data):
        raw = unwrap(t)
        if is_leaf(raw):
            continue
        root = Node(raw)
        number(root)
        tid = '%s/%s#%d' % (g, fid, i)
        byindex = collections.defaultdict(list)
        displaced = {}
        for n in nodes(root):
            if n.word is None:
                ix = index_of(n.label)
                if ix:
                    byindex[ix].append(n)
            elif n.cat == '-NONE-':
                m = re.match(r'(\*(?:EXP|ICH|RNR)\*)-(\d+)$', n.word)
                if m:
                    displaced[m.group(2)] = m.group(1)
        spans = backbone_spans(bb[tid]) if tid in bb else {}
        # every S-like clause: its subject and what the backbone makes of it
        for x in nodes(root):
            if x.word is None and x.cat in ('S', 'SQ', 'SINV'):
                sb = next((k for k in x.kids if 'SBJ' in k.tags), None)
                kind = 'no SBJ' if sb is None else (empty_kind(sb.raw) or 'overt')
                kind = re.sub(r'^\*PRO$', '*PRO*', kind)
                sp = span(x)
                labs = spans.get(sp, []) if sp else []
                bl = re.sub(r'ph', '', labs[-1]) if labs else '(no overt words)'
                T['all clauses: subject x backbone'][(x.cat, kind, bl)] += 1
        for x in nodes(root):
            # imperatives: what their subject is
            if x.word is None and x.cat == 'S' and 'IMP' in x.tags:
                sb = next((k for k in x.kids if 'SBJ' in k.tags), None)
                T['S-IMP clauses: subject'][('none' if sb is None else empty_kind(sb.raw) or 'overt')] += 1
            # raising and control verbs: the subject of their S complement
            if x.word is None and x.cat == 'VP':
                h = lexical_head(x)
                if h is None or h.word.lower() not in GOVERNORS:
                    continue
                for k in x.kids:
                    if k.cat == 'S' and not set(k.tags) & (ADVERBIAL | {'PRD', 'NOM'}):
                        sb = next((y for y in k.kids if 'SBJ' in y.tags), None)
                        kind = 'none' if sb is None else empty_kind(sb.raw) or 'overt'
                        if sb is not None and leaves(sb.raw) and re.search(r'-\d+$', leaves(sb.raw)[-1][1]):
                            kind += '-n'
                        T['S complements of raising/control verbs: subject'][(h.word.lower(), kind)] += 1
            # adjectives with an S complement
            if x.word is None and x.cat == 'S' and not x.tags and x.parent is not None and x.parent.cat == 'ADJP':
                sb = next((y for y in x.kids if 'SBJ' in y.tags), None)
                if sb is not None and empty_kind(sb.raw):
                    js = [w.lower() for y in x.parent.kids if y is not x for t_, w in leaves(y.raw) if t_.startswith('JJ')]
                    kind = empty_kind(sb.raw) + ('-n' if re.search(r'-\d+$', leaves(sb.raw)[-1][1]) else '')
                    T['S complements of adjectives: subject'][(js[-1] if js else '?', kind)] += 1
        for vp in nodes(root):
            if vp.cat != 'VP' or is_aux_vp(vp):
                continue
            head = lexical_head(vp)
            if head is None:
                continue
            c = clause_of(vp)
            if c is None:
                continue
            s = next((k for k in c.kids if 'SBJ' in k.tags), None)
            if s is None or not (empty_kind(s.raw) or '').startswith('*PRO'):
                continue
            total += 1
            group, cons, form = construction(c, displaced)
            rel, a, b, dup = control(c, root, byindex)
            sp = span(c)
            labs = spans.get(sp, [])
            bl = re.sub(r'ph', '', labs[-1]) if labs else '(none)'
            gov = None
            if c.parent is not None and c.parent.cat == 'VP':
                h = lexical_head(c.parent)
                gov = h.word.lower() if h else None
            rec = {'id': tid, 'verb': head.word, 'tag': head.cat, 'clause': c.label,
                   'parent': c.parent.label if c.parent is not None else 'ROOT',
                   'group': group, 'construction': cons, 'form': form, 'control': rel,
                   'duplicate_index': dup,
                   'controller': a.label if a is not None else None,
                   'controller_words': ' '.join(words(b.raw))[:60] if b is not None else None,
                   'governor': gov, 'backbone': bl, 'clause_words': ' '.join(words(c.raw))[:100]}
            out.write(json.dumps(rec) + '\n')
            key = (tid, id(c))
            if key not in clauses_seen:
                T['clauses by group'][group] += 1
            clauses_seen.add(key)
            T['group'][group] += 1
            T['group / construction'][(group, cons)] += 1
            T['group / form'][(group, form)] += 1
            T['group / control'][(group, rel)] += 1
            T['control'][rel] += 1
            T['group / backbone'][(group, bl)] += 1
            T['backbone'][bl] += 1
            if bl == 'S':
                T['backbone S kept: its daughters'][(group == 'imperative', KIDS.get(sp))] += 1
            T['genre / group'][(g, group)] += 1
            # the Minimal Distance Principle: the nearest NP in the governing
            # verb phrase before the clause (a passive trace included), else
            # the subject; tested where the treebank gives an index
            if a is not None and c.parent is not None and c.parent.cat == 'VP':
                before = c.parent.kids[:[id(k) for k in c.parent.kids].index(id(c))]
                obj = any(k.cat == 'NP' and not set(k.tags) & ADVERBIAL for k in before)
                actual = 'subject' if rel.startswith('subject of nearest') else \
                    'object' if rel.startswith('object in') else 'other'
                T['minimal distance: group, predicted, actual'][(group, 'object' if obj else 'subject', actual)] += 1
            T['genre / group / indexed'][(g, group, a is not None)] += 1
            if dup:
                T['duplicate index'][tid] += 1
            if gov and group == 'complement of verb':
                T['governor (complement of verb)'][(gov, rel)] += 1
    out.close()
    print('verbs with *PRO* subject:', total, ' distinct clauses:', len(clauses_seen))
    for name, c in T.items():
        print('\n==', name)
        for k, v in c.most_common(400):
            print('%6d  %5.1f%%  %s' % (v, 100 * v / total, k))


if __name__ == '__main__':
    main(*sys.argv[1:4])
