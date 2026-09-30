"""The PPs of verbs in UD English, in CGEL's sense, and CGELBank's gold labels
for them.

A PP in CGEL is headed by a preposition, and CGEL's prepositions include what
UD calls subordinators of adverbial clauses (*before they closed*) and many
of what UD calls adverbs (*there*, *out*, *away*). So a PP of a verb V is,
in a UD tree:

    obl     a dependent of V, relation obl or obl:agent, with a case child;
            the preposition is the case word, with its fixed children
    advcl   a dependent of V, relation advcl, with a mark child that is a
            preposition in CGEL (not that, whether, to or for)
    advmod  a dependent of V, relation advmod, that is an intransitive
            preposition in CGEL (INTRANSITIVE_P)
    cop     a predicate with a copula be and a case child (*been to quite a
            few shops*), or a predicate that is an intransitive preposition
            (*has been around*); the verb is be, and in CGEL the PP is its
            complement

V is a word tagged VERB, or the copula of the last kind. CGELBank's gold
trees give each PP of a VP the function Comp or Mod; gold() reads them, and
align() matches them to UD PPs by sentence, verb lemma and preposition.
"""
import collections
import re

INTRANSITIVE_P = {
    'there', 'here', 'where', 'away', 'back', 'out', 'off', 'home', 'abroad', 'inside',
    'outside', 'upstairs', 'downstairs', 'down', 'up', 'over', 'around', 'forward', 'ahead',
    'apart', 'aside', 'in', 'on', 'through', 'along', 'across', 'behind', 'below', 'above',
    'underneath', 'overseas', 'nearby', 'about', 'before', 'since', 'afterwards', 'together',
    'now', 'then', 'so', 'next',       # prepositions in CGELBank's trees
}
AUXILIARIES = {'be', 'have', 'do', 'will', 'would', 'shall', 'should', 'can', 'could', 'may',
               'might', 'must'}
NOT_P = {'that', 'whether', 'to', 'for'}


def conllu(path):
    """(sent_id, text, tokens) for each sentence; a token is a dict. Sentences
    without an id are numbered s1, s2 ... in order."""
    sid, text, toks, n = None, '', [], 0
    for line in open(path, encoding='utf-8'):
        line = line.rstrip('\n')
        if line.startswith('# sent_id'):
            sid = line.split('=', 1)[1].strip()
        elif line.startswith('# text'):
            text = line.split('=', 1)[1].strip()
        elif line and not line.startswith('#'):
            c = line.split('\t')
            if '-' in c[0] or '.' in c[0]:
                continue
            toks.append(dict(id=int(c[0]), form=c[1], lemma=c[2].lower(), upos=c[3], xpos=c[4],
                             head=int(c[6]), rel=c[7]))
        elif not line and toks:
            n += 1
            yield sid or 's%d' % n, text or ' '.join(t['form'] for t in toks), toks
            sid, text, toks = None, '', []
    if toks:
        n += 1
        yield sid or 's%d' % n, text or ' '.join(t['form'] for t in toks), toks


def children(toks, i):
    return [t for t in toks if t['head'] == i]


def subtree(toks, i):
    out, todo = [], [i]
    while todo:
        j = todo.pop()
        out.append(j)
        todo += [t['id'] for t in toks if t['head'] == j]
    return sorted(out)


def preposition(toks, t, rel):
    """The preposition of a PP-like dependent, or None."""
    kids = children(toks, t['id'])
    if rel in ('obl', 'obl:agent', 'cop'):
        case = [k for k in kids if k['rel'] == 'case']
        if not case:
            return t['lemma'] if rel == 'cop' and t['lemma'] in INTRANSITIVE_P else None
        c = case[0]
        fixed = [k['form'].lower() for k in children(toks, c['id']) if k['rel'] == 'fixed']
        return ' '.join([c['form'].lower()] + fixed)
    if rel == 'advcl':
        mark = [k for k in kids if k['rel'] == 'mark' and k['lemma'] not in NOT_P]
        return mark[0]['form'].lower() if mark else None
    if rel == 'advmod':
        return t['lemma'] if t['lemma'] in INTRANSITIVE_P and t['upos'] in ('ADV', 'ADP') else None
    return None


def pps(sid, text, toks):
    """Each PP of a verb: a dict of what the learners and rules use."""
    by = {t['id']: t for t in toks}
    out = []
    for t in toks:
        rel = t['rel'].split(':')[0] if t['rel'] != 'obl:agent' else 'obl:agent'
        v = by.get(t['head'])
        if rel in ('obl', 'obl:agent', 'advcl', 'advmod') and v is not None and v['upos'] == 'VERB':
            kind = rel
        elif any(k['rel'] == 'cop' and k['lemma'] == 'be' for k in children(toks, t['id'])) and \
                (any(k['rel'] == 'case' for k in children(toks, t['id'])) or t['lemma'] in INTRANSITIVE_P):
            kind, v = 'cop', [k for k in children(toks, t['id']) if k['rel'] == 'cop'][0]
        else:
            continue
        prep = preposition(toks, t, kind)
        if prep is None:
            continue
        span = subtree(toks, t['id'])
        if kind == 'cop':
            span = [i for i in span if i != v['id']]
        vkids = children(toks, v['id']) if kind != 'cop' else children(toks, t['id'])
        start = span[0]
        between = [i for i in range(min(v['id'], start) + 1, max(v['id'], start))
                   if by[i]['rel'] not in ('compound:prt', 'punct')]
        out.append(dict(
            sid=sid, text=text, verb_id=v['id'], pp_id=t['id'], lemma=v['lemma'],
            vtag=v['xpos'].lower(), prep=prep, rel=kind,
            obj_lemma=t['lemma'] if kind in ('obl', 'obl:agent', 'cop') else
                      (t['lemma'] if kind == 'advcl' else 'none'),
            obj_upos=t['upos'] if kind != 'advmod' else 'none',
            obj_cat={'obl': 'np', 'obl:agent': 'np', 'cop': 'np', 'advcl': 'clause',
                     'advmod': 'none'}[kind],
            next=start == v['id'] + 1 or (start > v['id'] and not between),
            obj_before=any(k['rel'] in ('obj', 'iobj') and v['id'] < k['id'] < start for k in vkids),
            other_pp=False,
            passive=any(k['rel'] in ('aux:pass', 'nsubj:pass', 'csubj:pass') for k in vkids),
            pp_words=' '.join(by[i]['form'] for i in span),
        ))
    counts = collections.Counter((p['sid'], p['verb_id']) for p in out)
    for p in out:
        p['other_pp'] = counts[p['sid'], p['verb_id']] > 1
    return out


# CGELBank

def parse_cgel(text):
    toks = re.findall(r'\(|\)|:[\w-]+|"(?:[^"\\]|\\.)*"|[^\s()]+', text)
    pos = 0

    def node():
        nonlocal pos
        pos += 1
        n = {'cat': toks[pos], 'attrs': {}, 'kids': []}
        pos += 1
        while toks[pos] != ')':
            t = toks[pos]
            if t.startswith(':') and toks[pos + 1] == '(':
                pos += 1
                k = node()
                k['func'] = t[1:]
                n['kids'].append(k)
            elif t.startswith(':'):
                n['attrs'][t[1:]] = toks[pos + 1].strip('"')
                pos += 2
            else:
                pos += 1
        pos += 1
        return n
    out = []
    while pos < len(toks):
        if toks[pos] == '(':
            out.append(node())
        else:
            pos += 1
    return out


def lexical_head(n, cats):
    while True:
        if n['cat'] in cats and 't' in n['attrs']:
            return n
        h = [k for k in n['kids'] if k.get('func') == 'Head']
        if not h:
            return None
        n = h[0]


def gold(path):
    """(sent index, sent_id, verb lemma, preposition, Comp or Mod) for each PP
    of a VP in a CGELBank file."""
    blocks = [b for b in re.split(r'\n(?=# sent_id)', open(path, encoding='utf-8').read())
              if '# sent_id' in b]
    out = []
    for n, b in enumerate(blocks, 1):
        sid = re.search(r'# sent_id = (.*)', b).group(1).strip()
        body = '\n'.join(l for l in b.split('\n') if not l.startswith('#'))

        def walk(node):
            for k in node['kids']:
                if node['cat'] == 'VP' and k['cat'] == 'PP' and k.get('func') in ('Comp', 'Mod'):
                    v = lexical_head(node, {'V', 'V_aux'})
                    p = lexical_head(k, {'P'})
                    if v is not None and p is not None:
                        out.append((n, sid, v['attrs'].get('l', v['attrs']['t']).lower(),
                                    p['attrs']['t'].lower(), k['func']))
                walk(k)
        for t in parse_cgel(body):
            walk(t)
    return out


def align(gold_items, ud_pps_by_sent):
    """Each gold item matched to a UD PP of the same sentence, verb lemma and
    preposition (the first unmatched one); returns (matched pairs, unmatched).
    Where CGEL's verb is an auxiliary (CGEL makes auxiliaries heads, and a PP
    can hang on one), any verb of the sentence will do."""
    used, pairs, missed = set(), [], []
    for n, sid, lemma, prep, func in gold_items:
        cands = [p for p in ud_pps_by_sent.get(sid, []) + ud_pps_by_sent.get('#%d' % n, [])
                 if id(p) not in used and p['prep'].split()[0] == prep.split()[0] and
                 (p['lemma'] == lemma or p['lemma'].startswith(lemma[:4]) or lemma in AUXILIARIES)]
        if cands:
            used.add(id(cands[0]))
            pairs.append((func, cands[0]))
        else:
            missed.append((sid, lemma, prep, func))
    return pairs, missed
