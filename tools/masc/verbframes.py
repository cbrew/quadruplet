"""Every verb of MASC with its frame, as the full annotation gives it and as
a context-free backbone sees it.

    python3 verbframes.py MASC_DATA_DIR OUT_DIR

writes OUT_DIR/verbs.jsonl, a JSON object per verb occurrence, and
OUT_DIR/lemmas.tsv (form, tag, lemma and count of every word tagged VB*), and
prints a summary. A verb is:

* a word tagged VB* heading the lowest verb phrase above it (a verb phrase
  with a verb phrase daughter is an auxiliary's, and is passed over); every
  verb of a verb phrase whose verbs are coordinated, (VP (VB say) (CC or)
  (VB imply) SBAR), each with the phrase's dependents;
* a word tagged VB* that is a daughter of an SQ or SINV with no verb phrase
  daughter, (SQ (VBD were) (NP-SBJ they) (PP-LOC-PRD at the meeting)): a
  copula or possessive *have*, with the clause's daughters as dependents;
* the head of a verb phrase with no verb in it whose first daughter is a
  word tagged JJ or NN (a mistagged verb), or a verb tagged BES or HVS.

For each verb:

* its lemma (tools/masc/verblemmas.py), the auxiliaries above it, and
  whether it is negated (*not*, *n't*, in its phrase, an auxiliary's or
  the clause);
* the clause: the nearest S-like node above its verb phrase, through any
  auxiliary and coordinated verb phrases and coordinations of unlike
  categories (UCP); its label with function tags;
* the subject: that clause's -SBJ daughter, overt or empty (and which empty
  element: *PRO*, *T*, * ...); failing that, an NP with no function tag
  before the verb phrase (flag untagged-subject); or none;
* each other daughter of the verb phrase, by the Penn Treebank's function
  tags (Bies et al. 1995), as
  - a complement: untagged NP, S, SBAR, SQ, SBARQ, SINV, UCP, a particle,
    and anything tagged -PRD, -CLR, -DTV or -PUT;
  - a modifier: anything tagged -ADV, -TMP, -LOC, -MNR, -PRP, -DIR, -EXT
    or -BNF, untagged ADVP, PP, ADJP, WHADVP, and adverbs other than *not*;
  - the agent: a *by* phrase whose NP is -LGS;
  - extraposed: the antecedent of an *EXP* or *ICH* trace elsewhere (It is
    important [that ...]), which belongs to the trace's host, not the verb;
  - neither: vocatives, *or whatever* (-ETC), punctuation, conjunctions;
  each overt, empty, or a preposition whose object is empty (stranded);
* the modifiers above the verb phrase: those of the auxiliaries' verb
  phrases and of the clause;
* flags for what lies around it: a coordination of verbs, verb phrases,
  unlike categories or clauses on the way up; the verb's own dependents
  including a right-node raised, gapped or extraposed element; the clause
  being imperative, a question, inverted, a fragment; a passive (VBN with an
  empty NP object);
* the frame three times: in full (subject and complements, with function
  tags, empty ones marked); without the empty elements (frame_overt); and as
  the backbone's categories (frame_backbone: no function tags, and a
  clause's category as tools/masc/treebank.py collapses it, SxVP for a
  clause whose subject is empty);
* for each dependent, besides its label, realization and words: its words'
  positions (span, [first, last + 1]); its marker (a prepositional
  phrase's preposition, a subordinate clause's complementizer or wh-word);
  a rough lexical head; for a clausal one, its type (S finite, I nonfinite
  or verbless, W a question or wh/if/whether clause), verb form,
  complementizer and subject; for an empty one with an index, its
  antecedent, and its type if it is a clause;
* why the verb has no overt subject, where it has none: an empty subject of
  a named kind, an imperative, a verb phrase with no clause above it (a
  reduced relative or an absolute, a fragment), a right-node raised verb
  phrase, a question or inversion, or none of these.

The first version of this script, with which the reports in docs/verbs/ were
made, is tools/masc/verbs/verbframes_v1.py; docs/verbs/README.md lists the
changes.
"""
import collections, json, os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from masctrees import *
from verblemmas import collect_bases, lemma

COMPLEMENT_TAGS = {'PRD', 'CLR', 'DTV', 'PUT'}
MODIFIER_TAGS = {'ADV', 'TMP', 'LOC', 'MNR', 'PRP', 'DIR', 'EXT', 'BNF'}
NEITHER_TAGS = {'VOC', 'ETC'}
COMPLEMENT_CATS = {'NP', 'S', 'SBAR', 'SQ', 'SBARQ', 'SINV', 'PRT', 'UCP'}
MODIFIER_CATS = {'ADVP', 'PP', 'ADJP', 'WHADVP'}
CLAUSES = {'S', 'SQ', 'SINV', 'SBARQ'}
VERB_TAGS = {'VB', 'VBD', 'VBG', 'VBN', 'VBP', 'VBZ'}
ODD_VERB_TAGS = {'BES', 'HVS'}
NEGATION = {'not', "n't"}


def tags(l):
    return [p for p in re.split(r'[-=]', l)[1:] if p and not p.isdigit()]


def index(l):
    """A label's index, NP-SBJ-1 -> '1'; not a gapping index, NP=1."""
    m = re.search(r'-(\d+)(?:=\d+)?$', l)
    return m.group(1) if m else None


def trace_kind(w):
    """The kind of an empty element, its index gone and typos mended:
    *PRO*-1 and *PRO-1 -> *PRO*, *-2 -> *, 0 -> 0."""
    w = re.sub(r'-\d+$', '', w)
    m = re.fullmatch(r'\*([A-Z?]+)\*?', w)
    return '*%s*' % m.group(1) if m else w


def literal(parent, w):
    """A literal asterisk tagged -NONE-, (SYM (-NONE- *)), (LS (-NONE- **))."""
    return base(parent) in ('SYM', 'LS') or re.fullmatch(r'\*{2,}', w) is not None


def empty_kind(n):
    """The kind of empty element a constituent is, if it has nothing else:
    *PRO*, *T*, *, *?*, *EXP*, *ICH*, *RNR*, *U*, 0; else None. An SBAR of a
    null complementizer over an empty clause is the clause's kind."""
    if base(label(n)) in ('SYM', 'LS'):
        return None
    ls = leaves(n)
    if not ls or any(t != '-NONE-' for t, _ in ls) or any(literal(label(n), w) for _, w in ls):
        return None
    kinds = [trace_kind(w) for _, w in ls]
    return next((k for k in kinds if k != '0'), '0')


def words(n):
    return [w for t, w in leaves(n) if t != '-NONE-']


def backbone(n):
    """The constituent as tools/masc/treebank.py makes it: empty elements gone,
    function tags dropped, a chain of single-child phrases one symbol."""
    if is_leaf(n):
        return None if n[0] == '-NONE-' else [base(n[0]), n[1]]
    kids = [k for k in (backbone(c) for c in n[1:] if not isinstance(c, str)) if k]
    if not kids:
        return None
    l = base(label(n)) or 'ROOT'
    if len(kids) == 1 and not is_leaf(kids[0]):
        k = kids[0]
        return [l if l == k[0] else l + 'x' + k[0]] + k[1:]
    return [l] + kids


class Node:
    """A tree node with a parent link, for walking up."""
    __slots__ = ('raw', 'label', 'cat', 'tags', 'kids', 'parent', 'word', 'pos')

    def __init__(self, raw, parent=None):
        self.raw, self.parent = raw, parent
        self.label = label(raw) if not is_leaf(raw) else raw[0]
        self.cat = base(self.label) if not is_leaf(raw) else raw[0]
        self.tags = tags(self.label) if not is_leaf(raw) else []
        self.kids, self.word, self.pos = [], None, None
        if is_leaf(raw):
            self.word = raw[1]
        else:
            self.kids = [Node(c, self) for c in raw[1:] if not isinstance(c, str)]


def number(root):
    """Number the overt words from 0, as the backbone does."""
    i = 0
    def walk(n):
        nonlocal i
        if n.word is not None:
            if n.cat != '-NONE-':
                n.pos = i
                i += 1
            return
        for k in n.kids:
            walk(k)
    walk(root)


def nodes(n):
    yield n
    for k in n.kids:
        yield from nodes(k)


def is_aux_vp(vp):
    return any(k.cat == 'VP' for k in vp.kids)


def is_verb(k):
    return k.word is not None and k.cat in VERB_TAGS


def gapped(n):
    """A conjunct whose verb is gapped: it has =N daughters and no verb."""
    return n.cat in ('S', 'VP', 'SINV', 'SQ') and any('=' in k.label for k in n.kids) and \
        not any(k.cat == 'VP' or (k.word is not None and (k.cat in VERB_TAGS or k.cat in ('MD', 'TO')))
                for k in n.kids)


class Tree:
    """A sentence tree with what the verb records need from all of it."""

    def __init__(self, tree_id, raw):
        self.id = tree_id
        self.root = Node(raw)
        number(self.root)
        self.traces = collections.defaultdict(set)   # index -> kinds of trace
        for x in nodes(self.root):
            if x.cat == '-NONE-' and x.word and index('X' + x.word):
                self.traces[index('X' + x.word)].add(trace_kind(x.word))
        self.disfluent = any(x.cat == 'EDITED' for x in nodes(self.root))
        self.antecedents = {}                          # index -> the overt node bearing it
        for x in nodes(self.root):
            if x.word is None and index(x.label) and not empty_kind(x.raw):
                self.antecedents.setdefault(index(x.label), x)

    def antecedent(self, n):
        """The overt constituent an empty one stands for, *T*-1 -> the node
        labelled -1; or None."""
        for x in nodes(n):
            if x.cat == '-NONE-' and x.word:
                m = re.search(r'-(\d+)$', x.word)
                if m:
                    return self.antecedents.get(m.group(1))
        return None

    def antecedent_of(self, n, kinds):
        i = index(n.label)
        return i is not None and bool(self.traces.get(i, set()) & kinds)


def contains_trace(n, kinds, depth=2):
    """Whether a trace of one of the kinds is n, or within depth levels of it."""
    if n.cat == '-NONE-':
        return n.word is not None and trace_kind(n.word) in kinds
    return depth > 0 and any(contains_trace(k, kinds, depth - 1) for k in n.kids)


def role(d, tree):
    """A dependent's role: complement, modifier, agent, extraposed, negation,
    or other."""
    if d.word is not None:
        if d.cat == 'RP':
            return 'complement'
        if d.cat in ('RB', 'RBR', 'RBS'):
            return 'negation' if d.word.lower() in NEGATION else 'modifier'
        return 'other'
    ts = set(d.tags)
    if tree.antecedent_of(d, {'*EXP*', '*ICH*'}):
        return 'extraposed'
    if ts & NEITHER_TAGS:
        return 'other'
    if d.cat == 'PP' and any('LGS' in k.tags for k in d.kids):
        return 'agent'
    if ts & COMPLEMENT_TAGS:
        return 'complement'
    if ts & MODIFIER_TAGS:
        return 'modifier'
    if d.cat in COMPLEMENT_CATS:
        return 'complement'
    if d.cat in MODIFIER_CATS:
        return 'modifier'
    return 'other'


def span(n):
    """The overt words' positions, [first, last + 1], or None."""
    ps = [x.pos for x in nodes(n) if x.pos is not None]
    return [min(ps), max(ps) + 1] if ps else None


def overt(n):
    return n.word is not None and n.cat != '-NONE-' or n.word is None and not empty_kind(n.raw)


NOMINAL_TAGS = ('NN', 'PRP', 'CD', 'EX', 'WP')


def head_word(n):
    """A rough lexical head: a noun phrase's last noun (its first NP
    daughter's, if it has one), a clause's lexical verb, a prepositional
    phrase's object's head, an adverb or adjective phrase's last adverb or
    adjective; the word itself for a word; else the last overt word."""
    if n.word is not None:
        return n.word.lower() if n.cat != '-NONE-' else None
    kids = [k for k in n.kids if overt(k)]
    if not kids:
        return None
    if n.cat in ('S', 'SQ', 'SINV', 'SBAR', 'SBARQ', 'VP'):
        v = lexical_verb(n)
        if v is not None:
            return v.word.lower()
        prd = next((k for k in kids if 'PRD' in k.tags), None)   # a small clause's predicate
        return head_word(prd) if prd is not None else None
    if n.cat in ('NP', 'NML', 'NX', 'WHNP'):
        np = next((k for k in kids if k.cat in ('NP', 'NML')), None)
        if np is not None and np is kids[0]:
            return head_word(np)
        nouns = [k for k in kids if k.word is not None and k.cat.startswith(NOMINAL_TAGS)]
        return (nouns[-1] if nouns else kids[-1]).word.lower() if (nouns or kids[-1].word) else head_word(kids[-1])
    if n.cat in ('PP', 'WHPP'):
        obj = next((k for k in kids if k.word is None), None)
        return head_word(obj) if obj is not None else None
    if n.cat in ('ADVP', 'ADJP', 'WHADVP', 'WHADJP', 'PRT'):
        ws = [k for k in kids if k.word is not None and k.cat[:2] in ('RB', 'JJ', 'RP', 'WR')]
        return ws[-1].word.lower() if ws else head_word(kids[-1])
    return head_word(kids[-1])


def lexical_verb(n):
    """The lexical verb of a clause or verb phrase: down through the verb
    phrases, auxiliaries' and the first conjunct's, to the first verb word
    of the lowest."""
    while n is not None and n.word is None:
        if n.cat == 'SBAR':
            n = next((k for k in n.kids if k.cat in CLAUSES), None)
        elif n.cat in CLAUSES:
            vp = next((k for k in n.kids if k.cat == 'VP'), None)
            if vp is None:
                return next((k for k in n.kids if is_verb(k)), None)
            n = vp
        elif n.cat == 'VP':
            vp = next((k for k in n.kids if k.cat == 'VP'), None)
            if vp is None:
                return next((k for k in n.kids if is_verb(k)), None)
            n = vp
        else:
            return None
    return n


def marker(n):
    """The word that marks a phrase: a prepositional phrase's preposition, a
    subordinate clause's complementizer or wh-word (the first word of its
    WH phrase); else None."""
    if n.cat in ('PP', 'WHPP'):
        p = next((k for k in n.kids if k.word is not None and k.cat in ('IN', 'TO', 'RP')), None)
        return p.word.lower() if p is not None else None
    if n.cat in ('SBAR', 'SBARQ'):
        for k in n.kids:
            if k.cat.startswith('WH'):
                ws = [w for t, w in leaves(k.raw) if t != '-NONE-']
                return ws[0].lower() if ws else None
            if k.word is not None and k.cat in ('IN', 'DT', 'TO'):
                return k.word.lower()
            if k.cat == '-NONE-':
                continue
            if k.cat in CLAUSES:
                return None
    return None


FINITE_TAGS = {'MD', 'VBD', 'VBZ', 'VBP'}
FORMS = {'TO': 'to', 'VB': 'bare', 'VBG': 'ing', 'VBN': 'en'}
WH_MARKERS = {'if', 'whether'}


def clause_of(n):
    """What a clausal constituent is: its type, S (a finite clause), I (a
    nonfinite or verbless one) or W (a question, or a clause opened by a
    wh-word, *if* or *whether*); its verb's form (finite, to, bare, ing, en,
    or verbless); its complementizer or wh-word; and its subject's kind
    (overt, or the empty element). None if it is not clausal."""
    if n.cat not in CLAUSES | {'SBAR', 'UCP'}:
        return None
    if n.cat == 'UCP':
        first = next((k for k in n.kids if overt(k) and k.cat not in ('CC', 'CONJP', ',', ':')), None)
        return clause_of(first) if first is not None else None
    m = marker(n)
    if n.cat == 'SBAR':
        s = next((k for k in n.kids if k.cat in CLAUSES), None)
        if s is None:                            # a coordination of subordinate clauses
            sub = next((k for k in n.kids if k.cat == 'SBAR' and overt(k)), None)
            if sub is not None:
                return clause_of(sub)
        inner = clause_of(s) if s is not None else None
        out = dict(inner) if inner else {'type': None, 'form': None, 'subject': None}
        out['marker'] = m
        if any(k.cat.startswith('WH') for k in n.kids) or m in WH_MARKERS:
            out['type'] = 'W'
        return out
    if n.cat in ('SBARQ', 'SQ'):
        v = lexical_verb(n)
        return {'type': 'W', 'form': 'finite', 'marker': m, 'subject': None,
                'verb': v.word.lower() if v is not None else None}
    subj = next((k for k in n.kids if 'SBJ' in k.tags), None)
    subject = None if subj is None else (empty_kind(subj.raw) or 'overt')
    who = {'subject': subject}
    if subject == 'overt':
        who.update(subject_span=span(subj), subject_head=head_word(subj))
    vp = next((k for k in n.kids if k.cat == 'VP'), None)
    if vp is None:
        inner = next((k for k in n.kids if k.cat in CLAUSES), None)
        if inner is not None and 'SBJ' not in inner.tags:
            return clause_of(inner)             # a coordination of clauses
        if n.cat == 'SINV':
            v = next((k for k in n.kids if k.word is not None and k.cat in FINITE_TAGS | VERB_TAGS), None)
            form = 'finite' if v is not None and v.cat in FINITE_TAGS else 'verbless'
            return {'type': 'S' if form == 'finite' else 'I', 'form': form, 'marker': None, **who}
        return {'type': 'I', 'form': 'verbless', 'marker': None, **who}
    x = vp
    while True:
        first = next((k for k in x.kids if k.word is not None and
                      k.cat in FINITE_TAGS | VERB_TAGS | ODD_VERB_TAGS | {'TO'}), None)
        if first is not None or not any(k.cat == 'VP' for k in x.kids):
            break
        x = next(k for k in x.kids if k.cat == 'VP')
    if first is None:
        form = 'verbless'
    elif first.cat in FINITE_TAGS or 'IMP' in n.tags:
        form = 'finite'
    else:
        form = FORMS.get(first.cat, 'finite' if first.cat in ODD_VERB_TAGS else 'verbless')
    return {'type': 'S' if form == 'finite' else 'I', 'form': form, 'marker': None, **who}


def describe(d, where=None, tree=None):
    kind = empty_kind(d.raw)
    realization = 'empty' if kind else 'overt'
    if not kind and d.cat == 'PP':
        phrases = [k for k in d.kids if k.word is None]
        kinds = [empty_kind(k.raw) for k in phrases]
        if phrases and all(kinds):
            realization, kind = 'stranded', kinds[0]
    bb = backbone(d.raw)
    out = {'label': d.label, 'cat': d.cat, 'tags': d.tags,
           'realization': realization, 'empty': kind,
           'backbone': bb[0] if bb else None,
           'words': ' '.join(words(d.raw))[:80],
           'span': span(d), 'marker': marker(d), 'head': head_word(d)}
    if d.cat == 'PP' and realization == 'stranded':
        out['head'] = None
    c = None
    if kind and tree is not None:
        a = tree.antecedent(d)                 # an empty constituent: its antecedent's
        if a is not None:
            c = clause_of(a)
            out['antecedent'] = {'label': a.label, 'span': span(a), 'head': head_word(a)}
    elif not kind:
        c = clause_of(d)
    if c is not None:
        out['clause'] = c
    if where:
        out['where'] = where
    return out


def frame(subj, comps, include_empty, tagged):
    parts = []
    if subj and (include_empty or subj['realization'] == 'overt'):
        parts.append('SBJ' + ('' if subj['realization'] == 'overt' or not include_empty else '(' + subj['empty'] + ')'))
    for c in comps:
        if c['realization'] == 'empty' and not include_empty:
            continue
        if tagged:
            name = c['cat'] + ''.join('-' + t for t in c['tags'] if t in COMPLEMENT_TAGS)
        else:
            name = c['backbone'] or c['cat']
        if include_empty and c['realization'] == 'empty':
            name += '(' + c['empty'] + ')'
        elif include_empty and c['realization'] == 'stranded':
            name += '[' + c['empty'] + ']'
        parts.append(name)
    return ' '.join(parts)


def verb_record(tree, container, head, heads):
    """The record of verb head, whose dependents are container's daughters:
    its verb phrase, or the clause it is a daughter of. heads are all the
    verbs sharing them, coordinated."""
    flags = set()
    if len(heads) > 1:
        flags.add('verb-coordination')
    if head.cat not in VERB_TAGS:
        flags.add('tag:' + head.cat)
    path = []                       # the verb phrases from the verb's up
    if container.cat in CLAUSES:
        flags.add('verb-in-clause')
        n, child = container, None
    else:
        path.append(container)
        n, child = container.parent, container
        while n is not None and n.cat in ('VP', 'UCP'):
            if n.cat == 'UCP':
                flags.add('ucp-coordination')
            elif sum(1 for k in n.kids if k.cat == 'VP') > 1 or any(k.cat in ('CC', 'CONJP') for k in n.kids):
                flags.add('vp-coordination')
            if n.cat == 'VP':
                path.append(n)
            child, n = n, n.parent
    clause = n if n is not None and n.cat in CLAUSES else None
    if n is not None and n.cat not in CLAUSES:
        flags.add('no-clause:' + n.cat)
    if any(tree.antecedent_of(v, {'*RNR*'}) for v in path):
        flags.add('rnr-vp')
    subject = None
    if clause is not None:
        subject = next((k for k in clause.kids if 'SBJ' in k.tags), None)
        if subject is None and child is not None:
            before = clause.kids[:clause.kids.index(child)]
            subject = next((k for k in reversed(before) if k.cat == 'NP' and not k.tags), None)
            if subject is not None:
                flags.add('untagged-subject')
        if 'IMP' in clause.tags:
            flags.add('imperative')
        if clause.cat in ('SQ', 'SINV', 'SBARQ'):
            flags.add('clause:' + clause.cat)
        up = clause.parent
        if up is not None and sum(1 for k in up.kids if k.cat in CLAUSES) > 1 and \
                any(k.cat in ('CC', 'CONJP') for k in up.kids):
            flags.add('clause-coordination')
        a = clause
        while a is not None:
            if a.cat == 'FRAG' or 'HLN' in a.tags or 'TTL' in a.tags:
                flags.add('fragment-or-headline')
            a = a.parent
    # a sibling conjunct with its verb gapped: this verb is the one it lacks
    for x in path + ([clause] if clause is not None else []):
        if x.parent is not None and any(s is not x and gapped(s) for s in x.parent.kids):
            flags.add('gapping')
    if tree.disfluent:
        flags.add('disfluency-in-sentence')

    comps, mods, agent, extraposed, above = [], [], [], [], []
    negated = False
    auxiliaries = []
    for d in container.kids:
        if d in heads or d is subject or d.cat in ('CC', 'CONJP', ',', ':', 'SYM') and len(heads) > 1 or \
                d.cat == '-NONE-' and d.word:
            continue
        r = role(d, tree)
        if r == 'complement':
            comps.append(describe(d, tree=tree))
        elif r == 'modifier':
            mods.append(describe(d, tree=tree))
        elif r == 'agent':
            agent.append(describe(d, tree=tree))
        elif r == 'extraposed':
            extraposed.append(describe(d, tree=tree))
        elif r == 'negation':
            negated = True
    # the auxiliaries' phrases and the clause: their modifiers and negation
    levels = [(v, 'auxiliary') for v in path[1:]]
    if clause is not None and container is not clause:
        levels.append((clause, 'clause'))
    for x, where in levels:
        for d in x.kids:
            # not the conjuncts of a coordination, which have no function tag
            if d is subject or d in path or d.cat in ('VP', 'UCP', 'CC', 'CONJP') or \
                    d.cat in CLAUSES and not d.tags:
                continue
            if d.word is not None and (d.cat in VERB_TAGS or d.cat in ('MD', 'TO')):
                if where == 'auxiliary':
                    auxiliaries.append(d.word)
                continue
            r = role(d, tree)
            if r == 'modifier':
                above.append(describe(d, where, tree))
            elif r == 'negation':
                negated = True
    subj = describe(subject, tree=tree) if subject is not None else None

    deps = comps + mods + agent + ([subj] if subj else [])
    dnodes = [d for d in container.kids if d not in heads] + ([subject] if subject is not None else [])
    if any(c['empty'] == '*RNR*' for c in deps) or 'rnr-vp' in flags:
        flags.add('rnr')
    if any(contains_trace(d, {'*ICH*'}) for d in dnodes):
        flags.add('ich')
    if any(contains_trace(d, {'*EXP*'}) for d in dnodes):
        flags.add('exp')
    if extraposed:
        flags.add('extraposed-dependent')
    if any('=' in d.label for d in dnodes):
        flags.add('gapping')
    if head.cat == 'VBN' and any(c['cat'] == 'NP' and c['empty'] == '*' for c in comps):
        flags.add('passive')

    cause = None
    if subj is None or subj['realization'] != 'overt':
        if subj is not None:
            cause = 'empty subject ' + subj['empty']
        elif 'imperative' in flags:
            cause = 'imperative'
        elif 'rnr-vp' in flags:
            cause = 'right-node raised verb phrase'
        elif clause is None:
            cause = 'no clause above (' + (n.cat if n is not None else 'root') + ')'
        elif 'fragment-or-headline' in flags:
            cause = 'fragment or headline'
        elif clause.cat in ('SQ', 'SINV', 'SBARQ'):
            cause = 'question or inversion, no SBJ'
        else:
            cause = 'unexplained'
    return {
        'id': tree.id, 'pos': head.pos, 'verb': head.word, 'tag': head.cat,
        'lemma': lemma(head.word, head.cat if head.cat in VERB_TAGS else 'VB'),
        'auxiliaries': auxiliaries, 'negated': negated,
        'clause': clause.label if clause is not None else None,
        'subject': subj, 'complements': comps, 'modifiers': mods,
        'agent': agent, 'extraposed': extraposed, 'modifiers_above': above,
        'flags': sorted(flags),
        'frame_full': frame(subj, comps, True, True),
        'frame_overt': frame(subj, comps, False, True),
        'frame_backbone': frame(subj, comps, False, False),
        'backbone_subjectless': cause,
        'sentence': ' '.join(words(tree.root.raw)),
    }


def verbs_of(tree):
    """Each verb of the tree, with the node whose daughters are its
    dependents, and the verbs sharing them."""
    for x in nodes(tree.root):
        if x.cat == 'VP' and not is_aux_vp(x):
            heads = [k for k in x.kids if is_verb(k)]
            # verbs coordinated, or listed with commas; else a slip, (VP 's been NP)
            if len(heads) > 1 and not any(k.cat in ('CC', 'CONJP', ',', ':', 'SYM') for k in x.kids):
                heads = heads[:1]
            if not heads:
                heads = [k for k in x.kids if k.word is not None and k.cat in ODD_VERB_TAGS][:1]
            if not heads:
                first = next((k for k in x.kids if k.cat != '-NONE-'), None)
                if first is not None and first.word is not None and first.cat in ('JJ', 'NN') and \
                        not any(k.word is not None and k.cat in ('MD', 'TO') for k in x.kids):
                    heads = [first]
            for h in heads:
                yield x, h, heads
        elif x.cat in ('SQ', 'SINV') and not any(k.cat == 'VP' for k in x.kids):
            h = next((k for k in x.kids if is_verb(k)), None)
            if h is not None:
                yield x, h, [h]


def main(root_dir, out_dir):
    collect_bases(masc_trees(root_dir))
    out = open(os.path.join(out_dir, 'verbs.jsonl'), 'w')
    forms = collections.Counter()
    total = changed = 0
    causes, kinds, flagged = collections.Counter(), collections.Counter(), collections.Counter()
    for g, fid, i, t in masc_trees(root_dir):
        raw = unwrap(t)
        if is_leaf(raw):
            continue
        for tag, w in leaves(raw):
            if tag in VERB_TAGS:
                forms[w, tag] += 1
        tree = Tree('%s/%s#%d' % (g, fid, i), raw)
        for container, head, heads in verbs_of(tree):
            rec = verb_record(tree, container, head, heads)
            total += 1
            causes[rec['backbone_subjectless'] or 'has an overt subject'] += 1
            if rec['frame_full'] != rec['frame_overt']:
                changed += 1
            for f in rec['flags']:
                flagged[f.split(':')[0]] += 1
            kinds['agent'] += len(rec['agent'])
            kinds['extraposed'] += len(rec['extraposed'])
            kinds['stranded preposition'] += sum(c['realization'] == 'stranded' for c in rec['complements'] + rec['modifiers'])
            kinds['modifier above the verb phrase'] += len(rec['modifiers_above'])
            kinds['negated verb'] += rec['negated']
            out.write(json.dumps(rec) + '\n')
    out.close()
    with open(os.path.join(out_dir, 'lemmas.tsv'), 'w') as f:
        for (w, tag), k in sorted(forms.items()):
            f.write('%s\t%s\t%s\t%d\n' % (w, tag, lemma(w, tag), k))
    print('%d verbs; frame changed by removing empty elements: %d (%.1f%%)' % (total, changed, 100 * changed / total))
    print('subject, as the backbone sees it:')
    for c, k in causes.most_common(25):
        print('  %6d  %5.1f%%  %s' % (k, 100 * k / total, c))
    print('dependents and flags:')
    for c, k in list(kinds.most_common()) + list(flagged.most_common()):
        print('  %6d  %s' % (k, c))


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
