"""Coordination and the other non-local annotations of MASC, and what they
do to verb frames.

    python3 coordination.py MASC_DATA_DIR OUT_DIR
    python3 coordination.py --tables OUT_DIR

reads the raw MASC trees, rebuilds each lexical verb's record exactly as
../verbframes.py does (by importing it), and adds what that record leaves
implicit: whether the verb is in the head conjunct of a coordinated verb
phrase (the one a head-driven reading of the backbone attaches the shared
subject to), which daughters of a coordination are shared by its conjuncts,
the antecedents of *RNR*, *ICH* and *EXP* and where they sit, the gapped
conjuncts (=N), and the coordinations of unlike categories (UCP). Writes
OUT_DIR/coordination.jsonl (one object per verb: id, pos, verb, and the
fields below) and OUT_DIR/coordination-trees.jsonl (per tree: gapped
conjuncts, multi-verb VPs, UCPs, clause coordinations, non-local traces); with --tables, prints the tables used in
docs/verbs/03-coordination.md from those two files.

A verb's *local* frame is the frame a reading of the backbone by heads
gives it: the overt subject only if every verb phrase between the verb's own
and the clause passes the verb up as its head (Collins's VP rule; a verb
phrase whose head word is followed by a verb phrase is transparent, as in
verbframes.is_aux_vp and go/interp's Flat reading);
and the overt complements of its own verb phrase, as in frame_backbone.
"""
import collections, json, os, re, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))
from masctrees import *
import verbframes_v1 as vf

PUNCT = {',', '.', ':', '``', "''", '"', "'", '-LRB-', '-RRB-', '-LSB-', '-RSB-', 'HYPH', 'NFP'}
VP_HEADS = ['TO', 'VBD', 'VBN', 'MD', 'VBZ', 'VB', 'VBG', 'VBP', 'VP', 'ADJP', 'NN', 'NNS', 'NP']
AUX = {'be', 'is', 'are', 'was', 'were', 'am', "'s", "'re", "'m", 'been', 'being',
       'have', 'has', 'had', "'ve", "'d", 'having', 'do', 'does', 'did', 'to',
       'get', 'got', 'gets', 'getting', 'gotten', 'ai', 'wo', 'ca'}


def coindex(label):
    """The reference index of a label, NP-SBJ-1 -> '1', NP=2 -> None."""
    m = re.search(r'-(\d+)(?:=\d+)?$', label) or re.search(r'-(\d+)=', label)
    return m.group(1) if m else None


def gapindex(label):
    m = re.search(r'=(\d+)', label)
    return m.group(1) if m else None


def is_empty(n):
    return vf.empty_kind(n.raw) is not None


def overt_kids(n):
    return [k for k in n.kids if not is_empty(k) and not (k.word is not None and k.cat in PUNCT)]


def is_coordination(n):
    return sum(1 for k in n.kids if k.cat == 'VP') > 1 or any(k.cat in ('CC', 'CONJP') for k in n.kids)


def vp_head(n):
    """The daughter a head-driven reading of the backbone takes as the head
    of verb phrase n: Collins's rule over its overt daughters, except that
    a verb, modal or to followed by a verb phrase passes to that phrase."""
    ks = overt_kids(n)
    if not ks:
        return None
    h = None
    for c in VP_HEADS:
        h = next((k for k in ks if k.cat == c), None)
        if h is not None:
            break
    if h is None:
        h = ks[0]
    if h.word is not None and (h.cat in ('MD', 'TO') or h.cat in vf.VERB_TAGS):
        # as in verbframes.is_aux_vp, a verb with a verb phrase after it is
        # an auxiliary, and the reading passes to that phrase
        after = ks[ks.index(h) + 1:]
        v = next((k for k in after if k.cat == 'VP'), None)
        if v is not None:
            return v
    return h


def conjunct_kids(n):
    """The conjuncts of a coordination: its VP daughters, or where it
    coordinates a verb with a verb phrase, the verb too."""
    return [k for k in n.kids if k.cat == 'VP' or (k.word is not None and k.cat in vf.VERB_TAGS)]


def analyse(tree_id, root, vp, rec, index):
    """The coordination facts of the verb heading vp."""
    out = {'id': tree_id, 'pos': rec['pos'], 'verb': rec['verb']}
    # the chain up to the clause: coordinations and head-ness
    coords, local, n, child = [], True, vp.parent, vp
    while n is not None and n.cat == 'VP':
        if vp_head(n) is not child:
            local = False
        if is_coordination(n):
            conj = conjunct_kids(n)
            shared = []
            ci = [j for j, k in enumerate(n.kids) if k in conj]
            for j, k in enumerate(n.kids):
                # before the first conjunct, between two, or after the last
                where = 'before' if ci and j < ci[0] else 'after' if ci and j > ci[-1] else 'between'
                if k in conj or k.cat in ('CC', 'CONJP') or (k.word is not None and k.cat in PUNCT):
                    continue
                if k.word is not None and k.cat in ('MD', 'TO') or k.word is not None and k.word.lower() in AUX:
                    shared.append({'what': 'auxiliary', 'label': k.cat, 'words': k.word, 'where': where})
                    continue
                ix = coindex(k.label)
                link = index.get(ix, {}).get('traces', []) if ix else []
                kinds = sorted({t for t in link})
                shared.append({'what': 'dependent', 'label': k.label, 'cat': k.cat, 'where': where,
                               'role': vf.role(k), 'empty': vf.empty_kind(k.raw),
                               'antecedent_of': kinds,
                               'words': ' '.join(vf.words(k.raw))[:60]})
            coords.append({'n_conjuncts': len(conj), 'position': conj.index(child) if child in conj else -1,
                           'head_conjunct': vp_head(n) is child,
                           'pattern': ' '.join(k.cat for k in n.kids),
                           'shared': shared})
        child, n = n, n.parent
    out['coordinations'] = coords
    # where the walk up stopped; if at a UCP, is there a subject further up?
    out['stop'] = n.cat if n is not None else None
    out['subject_above_ucp'] = None
    if n is not None and n.cat == 'UCP':
        up = n.parent
        while up is not None and up.cat not in vf.CLAUSES:
            up = up.parent
        sb = next((k for k in up.kids if 'SBJ' in k.tags), None) if up is not None else None
        out['subject_above_ucp'] = None if sb is None else (vf.empty_kind(sb.raw) or 'overt')
        out['ucp_parent'] = n.parent.cat if n.parent is not None else None
    out['subject_local'] = local
    subj = rec['subject']
    out['subject'] = None if subj is None else (subj['empty'] or 'overt')
    # the local frame
    parts = []
    if subj and subj['realization'] == 'overt' and local:
        parts.append('SBJ')
    back = rec['frame_backbone'].split()
    parts += [p for p in back if p != 'SBJ']
    out['frame_local'] = ' '.join(parts)
    out['frame_full'] = rec['frame_full']
    out['frame_backbone'] = rec['frame_backbone']
    # the verb phrase's own daughters that are antecedents of non-local traces
    ants = []
    for d in vp.kids:
        ix = coindex(d.label)
        if ix and index.get(ix, {}).get('traces'):
            for kind, where in index[ix]['where']:
                if kind in ('*RNR*', '*ICH*', '*EXP*'):
                    ants.append({'label': d.label, 'role': vf.role(d), 'kind': kind, 'trace_in': where})
    out['antecedents_in_vp'] = ants
    # the verb phrase's daughters that are traces of non-local kinds, and their antecedents
    tr = []
    for d in vp.kids:
        k = vf.empty_kind(d.raw)
        if k in ('*RNR*', '*ICH*', '*EXP*'):
            m = re.search(r'-(\d+)$', vf.leaves(d.raw)[0][1])
            ant = index.get(m.group(1), {}).get('antecedent') if m else None
            tr.append({'label': d.label, 'role': vf.role(d), 'kind': k,
                       'antecedent_parent': ant.parent.label if ant is not None and ant.parent else None,
                       'antecedent_parent_is_coordination': bool(ant is not None and ant.parent is not None
                                                                 and is_coordination(ant.parent))})
    out['nonlocal_traces_in_vp'] = tr
    # a gapping correlate (=N) among the verb phrase's own daughters
    out['gap_correlate_in_vp'] = any(gapindex(d.label) for d in vp.kids if d.word is None)
    # reasons the local frame differs from the full frame
    reasons = []
    if subj and subj['realization'] == 'empty':
        reasons.append('empty subject ' + subj['empty'])
    elif subj and not local:
        reasons.append('shared subject, non-head conjunct' if any(not c['head_conjunct'] for c in coords)
                       else 'subject non-local, other')
    for c in rec['complements']:
        if c['realization'] == 'empty':
            reasons.append('empty complement ' + c['empty'])
    out['reasons'] = reasons
    out['flags'] = rec['flags']
    out['cause'] = rec['backbone_subjectless']
    return out


def build_index(root):
    """For each index N in a tree: its antecedent node, and the kinds of
    trace pointing to it with the label of the trace's parent."""
    index = collections.defaultdict(lambda: {'antecedent': None, 'traces': [], 'where': [], 'gapped': []})
    for x in vf.nodes(root):
        if x.word is not None:
            if x.cat == '-NONE-':
                m = re.match(r'(\*[A-Z?]*\*|\*|0)-(\d+)$', x.word)
                if m:
                    index[m.group(2)]['traces'].append(m.group(1))
                    p = x.parent
                    # the constituent the trace stands for, and where that sits
                    while p.parent is not None and vf.empty_kind(p.parent.raw) is not None:
                        p = p.parent
                    index[m.group(2)]['where'].append((m.group(1), (p.parent.cat if p.parent else None)))
            continue
        ix = coindex(x.label)
        if ix and vf.empty_kind(x.raw) is None:
            index[ix]['antecedent'] = x
        g = gapindex(x.label)
        if g:
            index['=' + g]['gapped'].append(x)
    return index


def has_verb(n):
    return any(k.word is not None and k.cat in vf.VERB_TAGS or k.cat == 'VP' and has_verb(k) for k in n.kids)


def main(data, out_dir):
    fv = open(os.path.join(out_dir, 'coordination.jsonl'), 'w')
    ft = open(os.path.join(out_dir, 'coordination-trees.jsonl'), 'w')
    for g, fid, i, t in masc_trees(data):
        raw = unwrap(t)
        if is_leaf(raw):
            continue
        root = vf.Node(raw)
        vf.number(root)
        tree_id = '%s/%s#%d' % (g, fid, i)
        index = build_index(root)
        for vp in vf.nodes(root):
            if vp.cat != 'VP' or vf.is_aux_vp(vp):
                continue
            rec = vf.verb_record(tree_id, root, vp)
            if rec is None:
                continue
            fv.write(json.dumps(analyse(tree_id, root, vp, rec, index)) + '\n')
        # per tree: gapped conjuncts, verb coordination inside one VP,
        # verbs lost by the auxiliary test, UCPs, clause coordinations
        facts = collections.defaultdict(list)
        for x in vf.nodes(root):
            if x.word is not None:
                continue
            if any(gapindex(k.label) for k in x.kids) and x.cat in ('S', 'VP', 'SINV', 'SQ') and not any(
                    k.cat == 'VP' or k.word is not None and (k.cat in vf.VERB_TAGS or k.cat in ('MD', 'TO'))
                    for k in x.kids):
                # a gapped conjunct: remnants and no verb or verb phrase of its own,
                # so no event and no verb record; where are its remnants' correlates?
                rem = [k for k in x.kids if gapindex(k.label)]
                corr = []
                for k in rem:
                    g = gapindex(k.label)
                    dash = [y for y in vf.nodes(root) if y.word is None and coindex(y.label) == g
                            and vf.empty_kind(y.raw) is None]
                    eq = [y for y in index['=' + g]['gapped'] if y is not k and not any(a is y for a in vf.nodes(x))]
                    corr.append('-N' if dash else '=N only' if eq else 'none')
                # the verb of the full conjunct: the verb heading the phrase that
                # holds the first correlate
                fullverb = None
                g = gapindex(rem[0].label)
                cand = [y for y in vf.nodes(root) if y.word is None and (coindex(y.label) == g or gapindex(y.label) == g)
                        and not any(a is y for a in vf.nodes(x))]
                if cand:
                    p = cand[0].parent
                    while p is not None and p.cat != 'VP' and p.cat not in vf.CLAUSES:
                        p = p.parent
                    while p is not None and p.cat in vf.CLAUSES:
                        p = next((k for k in p.kids if k.cat == 'VP'), None)
                    while p is not None and vf.is_aux_vp(p):
                        p = next((k for k in p.kids if k.cat == 'VP'), None)
                    if p is not None:
                        h = next((k for k in p.kids if k.word is not None and k.cat in vf.VERB_TAGS), None)
                        fullverb = h.word if h is not None else None
                facts['gapped'].append({'cat': x.label, 'parent': x.parent.label if x.parent else None,
                                        'remnants': [k.label for k in rem], 'correlates': corr,
                                        'verb': fullverb,
                                        'words': ' '.join(vf.words(x.raw))[:80]})
            vbs = [k for k in x.kids if k.word is not None and k.cat in vf.VERB_TAGS]
            if x.cat == 'VP' and len(vbs) > 1:
                facts['multi_verb_vp'].append({'verbs': [k.word for k in vbs], 'pattern': ' '.join(k.cat for k in x.kids)})
            if x.cat == 'VP' and vf.is_aux_vp(x) and vbs:
                w = vbs[0].word.lower()
                if w not in AUX and not re.match(r"^(be|is|are|was|were|am|been|being|have|has|had|do|does|did|'s|'re|'m|'ve|'d)$", w):
                    facts['lexical_verb_over_vp'].append({'verb': vbs[0].word, 'pattern': ' '.join(k.cat for k in x.kids),
                                                          'coordination': is_coordination(x)})
            if x.cat == 'UCP':
                facts['ucp'].append({'label': x.label, 'parent': x.parent.label if x.parent else None,
                                     'pattern': ' '.join(k.cat for k in x.kids)})
            if x.cat in vf.CLAUSES and any(k.cat in ('CC', 'CONJP') for k in x.kids) and \
                    sum(1 for k in x.kids if k.cat in vf.CLAUSES) > 1:
                sh = [k.label for k in x.kids if k.cat not in vf.CLAUSES and k.cat not in ('CC', 'CONJP')
                      and not (k.word is not None and k.cat in PUNCT)]
                facts['clause_coordination'].append({'label': x.label, 'shared': sh,
                                                     'conjuncts': sum(1 for k in x.kids if k.cat in vf.CLAUSES)})
        # every *RNR*, *ICH*, *EXP* trace: what it stands for, where, and where its antecedent is
        ants = collections.defaultdict(list)
        for x in vf.nodes(root):
            if x.word is None and coindex(x.label) and vf.empty_kind(x.raw) is None:
                ants[coindex(x.label)].append(x)
        for x in vf.nodes(root):
            if x.cat != '-NONE-' or not x.word:
                continue
            m = re.match(r'(\*RNR\*|\*ICH\*|\*EXP\*)(?:-(\d+))?$', x.word)
            if not m:
                continue
            p = x.parent
            while p.parent is not None and vf.empty_kind(p.parent.raw) is not None:
                p = p.parent
            a = ants.get(m.group(2), []) if m.group(2) else []
            ap = a[0].parent if len(a) == 1 else None
            dom = False
            q = x
            while q is not None and ap is not None:
                dom = dom or q is ap
                q = q.parent
            facts['traces'].append({'kind': m.group(1), 'trace': p.cat if p.cat != '-NONE-' else None,
                                    'host': p.parent.label if p.parent else None,
                                    'antecedents': len(a), 'antecedent': a[0].cat if len(a) == 1 else None,
                                    'antecedent_parent': ap.cat if ap is not None else None,
                                    'antecedent_parent_coordinates': bool(ap is not None and any(k.cat in ('CC', 'CONJP') for k in ap.kids)),
                                    'antecedent_parent_dominates_trace': dom})
        if facts:
            ft.write(json.dumps({'id': tree_id, **facts}) + '\n')
    fv.close()
    ft.close()


def pct(a, b):
    return '%.1f%%' % (100 * a / b) if b else '-'


def tables(out_dir):
    """Prints the tables of docs/verbs/03-coordination.md from the files main wrote."""
    C = collections.Counter
    R = [json.loads(l) for l in open(os.path.join(out_dir, 'coordination.jsonl'))]
    T = [json.loads(l) for l in open(os.path.join(out_dir, 'coordination-trees.jsonl'))]
    N = len(R)
    print('## A. Verbs under verb-phrase coordination')
    vc = [r for r in R if r['coordinations']]
    head = [r for r in vc if r['subject_local']]
    non = [r for r in vc if not r['subject_local']]
    print('verbs', N, '; under VP coordination', len(vc), pct(len(vc), N),
          '; head conjunct', len(head), '; non-head conjunct', len(non))
    print('depth of coordination on the way up', dict(C(len(r['coordinations']) for r in vc)))
    print('conjuncts per coordination (per verb)', dict(C(r['coordinations'][0]['n_conjuncts'] for r in vc)))
    for name, rs in (('head', head), ('non-head', non)):
        print('  subject of %s conjuncts:' % name, C(r['subject'] for r in rs).most_common())
    print('## B. Local backbone frame vs full frame')
    COORD = {'shared subject, non-head conjunct', 'empty complement *RNR*'}
    diff = [r for r in R if r['frame_local'] != r['frame_full']]
    only = [r for r in diff if set(r['reasons']) <= COORD]
    some = [r for r in diff if set(r['reasons']) & COORD]
    print('frame_local != frame_full', len(diff), pct(len(diff), N))
    print('frame_backbone != frame_full', sum(r['frame_backbone'] != r['frame_full'] for r in R))
    print('differs only because of coordination sharing', len(only), pct(len(only), N),
          C(tuple(sorted(set(r['reasons']))) for r in only).most_common())
    print('differs because of coordination sharing and something else', len(some) - len(only))
    print('differs for other reasons only', len(diff) - len(some))
    bonly = [r for r in R if r['frame_backbone'] != r['frame_full'] and
             {x for x in r['reasons'] if x.startswith('empty')} <= {'empty complement *RNR*'}]
    print('frame_backbone != frame_full only because of *RNR*', len(bonly))
    print('reasons (a verb may have several):')
    for k, v in C(x for r in R for x in set(r['reasons'])).most_common():
        print('  %6d %s' % (v, k))
    print('## C. Events with no verb record')
    mv = [x for t in T for x in t.get('multi_verb_vp', [])]
    print('VPs with more than one verb', len(mv), '; with CC or CONJP',
          sum(1 for x in mv if set(x['pattern'].split()) & {'CC', 'CONJP'}),
          '; verbs after the first (never recorded)', sum(len(x['verbs']) - 1 for x in mv))
    print('  patterns', C(x['pattern'] for x in mv).most_common(8))
    gp = [(t['id'], x) for t in T for x in t.get('gapped', [])]
    print('gapped conjuncts (no verb of their own)', len(gp), 'in', len({i for i, _ in gp}), 'trees')
    print('## D. Daughters of a VP coordination other than conjuncts, conjunctions, punctuation')
    seen, sh = set(), C()
    for r in vc:
        for c in r['coordinations']:
            key = (r['id'], c['pattern'], json.dumps(c['shared']))
            if key in seen:
                continue
            seen.add(key)
            for s in c['shared']:
                if s['what'] == 'auxiliary':
                    sh[(s['where'], 'auxiliary', '')] += 1
                else:
                    link = 'antecedent of ' + '/'.join(s['antecedent_of']) if s['antecedent_of'] else ''
                    sh[(s['where'], s['role'], link)] += 1
    print('coordination nodes', len(seen))
    for k, v in sorted(sh.items(), key=lambda kv: -kv[1]):
        print('  %5d %s' % (v, k))
    print('## E. *RNR*, *ICH*, *EXP* traces')
    tr = [x for t in T for x in t.get('traces', [])]
    for kind in ('*RNR*', '*ICH*', '*EXP*'):
        ts = [x for x in tr if x['kind'] == kind]
        print(kind, len(ts), 'traces; antecedents found', C(x['antecedents'] for x in ts).most_common())
        print('  trace stands for', C(x['trace'] for x in ts).most_common(6))
        print('  trace host', C((x['host'] or '').split('-')[0] + ('-SBJ' if 'SBJ' in (x['host'] or '') else '') for x in ts).most_common(8))
        print('  antecedent parent', C((x['antecedent_parent'], x['antecedent_parent_coordinates']) for x in ts).most_common(6))
        print('  antecedent parent dominates trace', C(x['antecedent_parent_dominates_trace'] for x in ts).most_common())
    print('## F. Gapped conjuncts')
    print('conjunct/parent', C((x['cat'].split('-')[0].split('=')[0], (x['parent'] or '').split('-')[0].split('=')[0]) for _, x in gp).most_common(8))
    print('remnants per conjunct', C(len(x['remnants']) for _, x in gp).most_common())
    print('correlates marked', C('all -N' if set(x['correlates']) == {'-N'} else 'all =N only' if set(x['correlates']) == {'=N only'}
                                  else 'mixed or missing' for _, x in gp).most_common())
    print('verb of the full conjunct', C(x['verb'] for _, x in gp).most_common(8))
    print('## G. VP daughters that are antecedents of a trace elsewhere')
    for kind in ('*EXP*', '*ICH*', '*RNR*'):
        rows = [(r, x) for r in R for x in r['antecedents_in_vp'] if x['kind'] == kind]
        print(kind, C((x['role'], x['trace_in']) for _, x in rows).most_common(6))
        comp = {(r['id'], r['pos']) for r, x in rows if x['role'] == 'complement'}
        print('  verbs with such a daughter counted as a complement', len(comp))
    disp = [r for r in R if any(x['kind'] in ('*EXP*', '*ICH*') and x['role'] == 'complement' for x in r['antecedents_in_vp'])]
    print('verbs whose frames (full and backbone) hold a displaced complement', len(disp), pct(len(disp), N))
    print('  frames', C(r['frame_full'] for r in disp).most_common(6))
    print('## H. Verbs whose walk up stops at a UCP')
    u = [r for r in R if r['stop'] == 'UCP']
    print(len(u), C((r.get('ucp_parent'), r['subject_above_ucp']) for r in u).most_common(12))
    print('## I. Clause coordination')
    cc = [x for t in T for x in t.get('clause_coordination', [])]
    print('coordinations of clauses', len(cc), '; with other daughters', sum(1 for x in cc if x['shared']))
    cls = C()
    for x in cc:
        for s in x['shared']:
            b = re.split(r'[-=]', s)[0]
            ts = set(vf.tags(s))
            cls['modifier' if ts & vf.MODIFIER_TAGS - {'VOC'} or b in ('PP', 'ADVP', 'SBAR', 'RB') and not ts else
                'subject' if 'SBJ' in ts else 'vocative' if 'VOC' in ts else
                'verb phrase' if b == 'VP' else b if b in ('CODE', 'INTJ', 'REF', 'EDITED', 'PRN', 'LST', 'FRAG', 'SYM') else 'other ' + b] += 1
    print(cls.most_common())
    ccv = [r for r in R if 'clause-coordination' in r['flags']]
    print('verbs flagged clause-coordination', len(ccv), C(r['subject'] for r in ccv).most_common())
    print('## J. Flags against what they should mark')
    g = [r for r in R if 'gapping' in r['flags']]
    print('gapping flag', len(g), '; with a =N correlate among its own daughters', sum(r['gap_correlate_in_vp'] for r in g))
    for kind, flag in (('*RNR*', 'rnr'), ('*ICH*', 'ich'), ('*EXP*', 'exp')):
        aff = {(r['id'], r['pos']) for r in R if any(x['kind'] == kind for x in r['nonlocal_traces_in_vp'])
               or any(x['kind'] == kind for x in r['antecedents_in_vp'])}
        fl = {(r['id'], r['pos']) for r in R if flag in r['flags']}
        print('%s: verbs with the trace or its antecedent as a daughter %d; flagged %d; both %d' % (flag, len(aff), len(fl), len(aff & fl)))
    nconj = C(r['coordinations'][0]['pattern'] for r in vc if not set(r['coordinations'][0]['pattern'].split()) & {'CC', 'CONJP'})
    print('vp-coordination with no conjunction', sum(nconj.values()), nconj.most_common(6))


if __name__ == '__main__':
    if sys.argv[1] == '--tables':
        tables(sys.argv[2])
    else:
        main(sys.argv[1], sys.argv[2])
