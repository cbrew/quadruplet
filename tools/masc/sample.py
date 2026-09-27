"""Selects the MASC benchmark sample.

    python3 tools/masc/sample.py MASC_DATA_DIR src/test/resources/masc

MASC_DATA_DIR is the data directory of the MASC Penn Treebank release (with
spoken/ and written/ below it). The sample is sentences of 4 to 15 words,
leaving out punctuation, whose normalised trees use only the constructions
listed in OK below, 13 from each genre, chosen at random with a fixed seed.
It writes

    sample.mrg   the chosen trees as MASC has them, each preceded by its id
    sample.txt   id, genre and the words to parse, tab-separated
    gold.txt     id and the normalised tree, tab-separated

Words to parse are the tree's words without punctuation, lower-cased
unless they are proper nouns (NNP) or "I".
"""
import collections
import random
import re
import sys

from masctrees import (base, leaves, masc_trees, normalise, pretty, productions,
                       unwrap)

PER_GENRE = 13
SEED = 20260927

BAN_TAGS = {'UH', 'FW', 'SYM', 'LS', '$', '#', 'ADD', 'SU', 'XX', 'GW', 'AFX',
            'WP$', 'PDT', 'CD', 'NNPS', 'CODE'}
BAN_PHRASES = {'FRAG', 'INTJ', 'X', 'PRN', 'UCP', 'NX', 'NAC', 'CONJP', 'RRC',
               'LST', 'QP', 'SINV', 'EDITED', 'META', 'EMBED', 'NML', 'WHPP',
               'WHADJP', 'CODE', 'REF'}
BAN_WORDS = {'gon', 'wan', 'na'}  # gonna, wanna

# The normalised productions allowed, as regular expressions over
# "LHS -> RHS". A VP may be any verbal head followed by any complements.
V = '(VB|VBD|VBZ|VBP|VBN|VBG|MD|TO)'
OK = [re.compile(p) for p in [
    # noun phrases
    r'NP -> (PRP|EX|DT|NNP( NNP)*)',
    r'NP -> ((DT|PRP\$|NP) )?((JJ|JJR|JJS|ADJP) )*((NN|NNS|NNP) )*(NN|NNS)',
    r'NP -> (DT )?NNP( NNP)*',
    r'NP -> NP (PP|SBAR)',
    r'NP -> NP CC NP',
    r'NP -> (NN|NNS) CC (NN|NNS)',
    r'NP -> (NP|NNP|PRP|NN) POS',
    # verb phrases
    r'VP -> ' + V + r'( (NP|PP|ADJP|SBAR|S|VP|PRT|ADVP|RB))*',
    r'VP -> VP CC VP',
    # clauses
    r'S -> (CC |ADVP |PP |SBAR )?(NP (ADVP )?)?VP',
    r'S -> S CC S',
    r'SBAR -> (IN |WHNP |WHADVP )?S',
    r'SQ -> (VBP|VBZ|VBD|MD) NP (VP|ADJP|NP|PP)',
    r'SQ -> VP',
    r'SBARQ -> (WHNP|WHADVP) SQ',
    # modifiers
    r'ADJP -> (RB |RBR )?(JJ|JJR|JJS)',
    r'ADJP -> JJ CC JJ',
    r'ADJP -> ADJP (PP|S)',
    r'ADVP -> (RB|RBR|RBS)( RB)?',
    r'PP -> (IN|TO) NP',
    r'WHNP -> (WP|WDT)',
    r'WHADVP -> WRB',
    r'PRT -> RP',
]]


def accept(tree):
    """Whether a normalised tree is in the sample's scope."""
    words = leaves(tree)
    if not 4 <= len(words) <= 15 or tree[0] not in ('S', 'SQ', 'SBARQ'):
        return False
    if any(tag in BAN_TAGS or base(tag) != tag or w.lower() in BAN_WORDS
           for tag, w in words):
        return False
    ps = []
    productions(tree, ps)
    for lhs, rhs in ps:
        if lhs in BAN_PHRASES or any(k in BAN_PHRASES for k in rhs):
            return False
        if not any(r.fullmatch(lhs + ' -> ' + ' '.join(rhs)) for r in OK):
            return False
    return True


def parse_words(tree):
    return [w if tag == 'NNP' or w == 'I' else w.lower() for tag, w in leaves(tree)]


def main(masc_dir, out_dir):
    by_genre = collections.defaultdict(list)
    for genre, fid, i, raw in masc_trees(masc_dir):
        tree = normalise(unwrap(raw))
        if tree and accept(tree):
            by_genre[genre].append(('%s/%s#%d' % (genre, fid, i), genre, raw, tree))
    rng = random.Random(SEED)
    chosen = []
    for genre in sorted(by_genre):
        pool = by_genre[genre]
        chosen += sorted(rng.sample(pool, min(PER_GENRE, len(pool))), key=lambda c: c[0])
    with open(out_dir + '/sample.mrg', 'w') as mrg, \
            open(out_dir + '/sample.txt', 'w') as txt, \
            open(out_dir + '/gold.txt', 'w') as gold:
        for sid, genre, raw, tree in chosen:
            mrg.write('# %s\n%s\n\n' % (sid, pretty_raw(raw)))
            txt.write('%s\t%s\t%s\n' % (sid, genre, ' '.join(parse_words(tree))))
            gold.write('%s\t%s\n' % (sid, pretty(tree)))
    print('%d sentences from %d genres' % (len(chosen), len(by_genre)))


def pretty_raw(n):
    if isinstance(n, str):
        return n
    if len(n) == 2 and isinstance(n[0], str) and isinstance(n[1], str):
        return '(%s %s)' % (n[0], n[1])
    head = [n[0]] if n and isinstance(n[0], str) else ['']
    kids = n[1:] if n and isinstance(n[0], str) else n
    return '(%s)' % ' '.join(head + [pretty_raw(k) for k in kids]).strip() \
        if head != [''] else '( %s)' % ' '.join(pretty_raw(k) for k in kids)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
