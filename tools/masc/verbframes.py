"""Every lexical verb of MASC with its frame, as the full annotation gives it
and as a context-free backbone sees it.

    python3 verbframes.py MASC_DATA_DIR OUT_DIR

writes OUT_DIR/verbs.jsonl, a JSON object per verb occurrence, and prints a
summary. A lexical verb is a word tagged VB* heading the lowest verb phrase
above it: a verb phrase with a verb phrase daughter is an auxiliary's and is
passed over. For each verb:

* the clause: the nearest S-like node above its verb phrase, through any
  auxiliary and coordinated verb phrases; its label with function tags;
* the subject: that clause's -SBJ daughter, overt or empty (and which empty
  element: *PRO*, *T*, * ...), or none;
* each other daughter of the verb phrase, as a complement or a modifier, by
  the Penn Treebank's function tags (Bies et al. 1995): untagged NP, S, SBAR
  and any -PRD, -CLR, -DTV, -PUT are complements, as is a particle; any
  -ADV, -TMP, -LOC, -MNR, -PRP, -DIR, -EXT, -BNF, and untagged ADVP, PP and
  the like are modifiers; each overt or empty;
* flags for what lies around it: a coordination of verb phrases on the way
  up to the clause; right-node raising, gapping, extraposition (*ICH*,
  *EXP*); the clause being imperative, a question, inverted, a fragment;
* the frame twice: in full (subject and complements, empty ones marked), and
  as the backbone sees it, with the empty elements gone;
* why the backbone's frame lacks a subject, where it does: an empty subject
  of a named kind, a subject shared through coordination, an imperative, a
  verb phrase with no clause above it (a reduced relative or an absolute, a
  fragment), or none of these.
"""
import collections, json, os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from masctrees import *

COMPLEMENT_TAGS = {'PRD', 'CLR', 'DTV', 'PUT'}
MODIFIER_TAGS = {'ADV', 'TMP', 'LOC', 'MNR', 'PRP', 'DIR', 'EXT', 'BNF', 'VOC'}
CLAUSES = {'S', 'SQ', 'SINV', 'SBARQ'}
VERB_TAGS = {'VB', 'VBD', 'VBG', 'VBN', 'VBP', 'VBZ'}


def tags(l):
    return [p for p in re.split(r'[-=]', l)[1:] if p and not p.isdigit()]


def empty_kind(n):
    """The kind of empty element a constituent is, if it has nothing else:
    *PRO*, *T*, *, *?*, *EXP*, *ICH*, *RNR*, *U*, 0; else None."""
    ls = leaves(n)
    if ls and all(t == '-NONE-' for t, _ in ls):
        return re.sub(r'-\d+$', '', ls[0][1])
    return None


def words(n):
    return [w for t, w in leaves(n) if t != '-NONE-']


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


def role(d):
    """A verb phrase daughter's role: complement, modifier, or other."""
    if d.word is not None and d.cat != '-NONE-':
        if d.cat == 'RP':
            return 'complement'
        if d.cat in ('RB', 'RBR', 'RBS'):
            return 'modifier'
        return 'other'
    ts = set(d.tags)
    if ts & COMPLEMENT_TAGS:
        return 'complement'
    if ts & MODIFIER_TAGS:
        return 'modifier'
    if d.cat in ('NP', 'S', 'SBAR', 'SQ', 'SBARQ', 'SINV', 'PRT', 'UCP'):
        return 'complement'
    if d.cat in ('ADVP', 'PP', 'ADJP', 'WHADVP'):
        return 'modifier'
    return 'other'


def describe(d):
    kind = empty_kind(d.raw)
    return {'label': d.label, 'cat': d.cat, 'tags': d.tags,
            'realization': 'empty' if kind else 'overt', 'empty': kind,
            'words': ' '.join(words(d.raw))[:80]}


def verb_record(tree_id, root, vp):
    head = next((k for k in vp.kids if k.word is not None and k.cat in VERB_TAGS), None)
    if head is None:
        return None
    flags = set()
    # up through auxiliary and coordinated verb phrases to the clause
    n, child = vp.parent, vp
    while n is not None and n.cat == 'VP':
        if sum(1 for k in n.kids if k.cat == 'VP') > 1 or any(k.cat in ('CC', 'CONJP') for k in n.kids):
            flags.add('vp-coordination')
        child, n = n, n.parent
    clause = n if n is not None and n.cat in CLAUSES else None
    if n is not None and n.cat not in CLAUSES:
        flags.add('no-clause:' + n.cat)
    subject = None
    if clause is not None:
        # a clause coordinated with others can share a subject above: S -> S CC S
        subject = next((k for k in clause.kids if 'SBJ' in k.tags), None)
        if 'IMP' in clause.tags:
            flags.add('imperative')
        if clause.cat in ('SQ', 'SINV', 'SBARQ'):
            flags.add('clause:' + clause.cat)
        up = clause.parent
        if up is not None and up.cat in CLAUSES and any(k.cat in ('CC', 'CONJP') for k in up.kids):
            flags.add('clause-coordination')
        a = clause
        while a is not None:
            if a.cat in ('FRAG',) or 'HLN' in a.tags or 'TTL' in a.tags:
                flags.add('fragment-or-headline')
            a = a.parent
    for x in nodes(vp):
        if x.cat == '-NONE-' and x.word:
            k = re.sub(r'-\d+$', '', x.word)
            if k in ('*RNR*', '*ICH*', '*EXP*'):
                flags.add(k.strip('*').lower())
        if '=' in x.label:
            flags.add('gapping')
    for x in nodes(root):
        if x.parent is not None and x.parent.cat in ('EDITED',):
            flags.add('disfluency-in-sentence')
            break
    comps, mods = [], []
    for d in vp.kids:
        if d is head or d.cat == '-NONE-' and d.word:
            continue
        r = role(d)
        if r == 'complement':
            comps.append(describe(d))
        elif r == 'modifier':
            mods.append(describe(d))
    subj = None
    if subject is not None:
        subj = describe(subject)
    # the frame in full, and as the backbone sees it
    def frame(include_empty):
        parts = []
        if subj and (include_empty or subj['realization'] == 'overt'):
            parts.append('SBJ' + ('' if subj['realization'] == 'overt' else '(' + subj['empty'] + ')'))
        for c in comps:
            if include_empty or c['realization'] == 'overt':
                name = c['cat'] + ''.join('-' + t for t in c['tags'] if t in COMPLEMENT_TAGS)
                parts.append(name + ('' if c['realization'] == 'overt' else '(' + c['empty'] + ')'))
        return ' '.join(parts)
    cause = None
    if subj is None or subj['realization'] == 'empty':
        if subj is not None:
            cause = 'empty subject ' + subj['empty']
        elif 'imperative' in flags:
            cause = 'imperative'
        elif clause is None:
            cause = 'no clause above (' + (n.cat if n is not None else 'root') + ')'
        elif 'clause-coordination' in flags:
            cause = 'clause coordination'
        elif 'fragment-or-headline' in flags:
            cause = 'fragment or headline'
        elif clause.cat in ('SQ', 'SINV', 'SBARQ'):
            cause = 'question or inversion, no SBJ'
        else:
            cause = 'unexplained'
    return {
        'id': tree_id, 'pos': head.pos, 'verb': head.word, 'tag': head.cat,
        'clause': clause.label if clause is not None else None,
        'subject': subj, 'complements': comps, 'modifiers': mods,
        'flags': sorted(flags),
        'frame_full': frame(True), 'frame_backbone': frame(False),
        'backbone_subjectless': cause,
        'sentence': ' '.join(words(root.raw)),
    }


def main(root_dir, out_dir):
    out = open(os.path.join(out_dir, 'verbs.jsonl'), 'w')
    total = 0
    causes = collections.Counter()
    changed = 0
    for g, fid, i, t in masc_trees(root_dir):
        raw = unwrap(t)
        if is_leaf(raw):
            continue
        root = Node(raw)
        number(root)
        tree_id = '%s/%s#%d' % (g, fid, i)
        for vp in nodes(root):
            if vp.cat != 'VP' or is_aux_vp(vp):
                continue
            rec = verb_record(tree_id, root, vp)
            if rec is None:
                continue
            total += 1
            causes[rec['backbone_subjectless'] or 'has an overt subject'] += 1
            if rec['frame_full'] != rec['frame_backbone']:
                changed += 1
            out.write(json.dumps(rec) + '\n')
    out.close()
    print('%d lexical verbs; frame changed by removing empty elements: %d (%.1f%%)' % (total, changed, 100 * changed / total))
    print('subject, as the backbone sees it:')
    for c, k in causes.most_common(25):
        print('  %6d  %5.1f%%  %s' % (k, 100 * k / total, c))


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
