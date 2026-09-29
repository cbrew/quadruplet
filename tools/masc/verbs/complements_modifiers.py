"""Complements versus modifiers in MASC: how promiscuous is each?

    python3 complements_modifiers.py MASC_DATA_DIR            # all tables
    python3 complements_modifiers.py MASC_DATA_DIR --show ID POS   # one verb
    python3 complements_modifiers.py MASC_DATA_DIR --lemma LEMMA   # one lemma

Re-reads the raw trees with verbframes.py's own functions (so the records
are exactly those of verbs.jsonl) and keeps each verb's verb phrase node,
for what verbs.jsonl does not record: the preposition of a PP, the inside of
an S complement, the daughters verbframes.py calls neither complement nor
modifier. Standard library only.

Definitions used here (see docs/verbs/06-complements-and-modifiers.md):

* lemma: lemma(word, tag) below; a crude lemmatizer (irregular table, then
  -s/-ed/-ing stripping, a candidate chosen by its frequency as a base form,
  VB or VBP, in MASC).
* complement frame: the complements part of frame_full (or frame_backbone),
  i.e. the frame without the subject; '0' if none.
* modifier type: the modifier's first semantic function tag (TMP, LOC, MNR,
  PRP, DIR, ADV, EXT, BNF, VOC), else 'u' + its category (uPP, uADVP, uRB...).
* modifier set: the sorted multiset of a verb's modifier types; '0' if none.
  Full: overt and empty; backbone: overt only. verbs.jsonl, and so these
  sets, hold only the lexical verb phrase's own daughters; 'mx' adds the
  modifiers of the auxiliary verb phrases above it and of its clause.
* The information table gives H(Y), H(Y|verb), I(verb;Y), the mean I over
  five random shufflings of Y (the estimate's bias under independence), and
  (I - shuffled I)/H(Y), a bias-corrected uncertainty coefficient.
"""
import collections, math, os, random, re, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))
from masctrees import *
import verbframes as vf

SPOKEN = {'court-transcript', 'debate-transcript', 'face-to-face', 'telephone'}
SEM = ['TMP', 'LOC', 'MNR', 'PRP', 'DIR', 'ADV', 'EXT', 'BNF', 'VOC']

# ---------------------------------------------------------------- lemmas
IRREG = {}
for line in """
be: be is are am was were been being 's 're 'm ai art s rsquos rsquore rsquom
have: have has had having 've 'd hath
do: do does did done doing
go: go goes went gone going gon
say: says said
make: made
take: took taken
get: got gotten
come: came
know: knew known
think: thought
tell: told
find: found
give: gave given
see: saw seen
feel: felt
leave: left
mean: meant
keep: kept
bring: brought
begin: began begun
run: ran
hold: held
stand: stood
understand: understood
write: wrote written
buy: bought
pay: paid
lay: laid
sell: sold
send: sent
spend: spent
build: built
lose: lost
meet: met
lead: led
read: read
hear: heard
win: won
fall: fell fallen
grow: grew grown
show: shown
speak: spoke spoken
break: broke broken
choose: chose chosen
drive: drove driven
eat: ate eaten
forget: forgot forgotten
rise: rose risen
teach: taught
catch: caught
fight: fought
seek: sought
throw: threw thrown
wear: wore worn
draw: drew drawn
fly: flew flown
sit: sat
lie: lay lain lying
die: dying
tie: tying
become: became
blow: blew blown
hang: hung
shoot: shot
sleep: slept
steal: stole stolen
swim: swam swum
sing: sang sung
ring: rang rung
drink: drank drunk
bear: bore born borne
beat: beaten
bite: bit bitten
hide: hid hidden
ride: rode ridden
shake: shook shaken
wake: woke woken
forgive: forgave forgiven
freeze: froze frozen
feed: fed
flee: fled
bleed: bled
deal: dealt
dig: dug
light: lit
slide: slid
stick: stuck
strike: struck
swing: swung
tear: tore torn
undertake: undertook undertaken
overcome: overcame
withdraw: withdrew withdrawn
arise: arose arisen
awake: awoke
bind: bound
creep: crept
dream: dreamt
feel: felt
lend: lent
mislead: misled
oversee: oversaw overseen
seek: sought
shine: shone
sink: sank sunk
spin: spun
spit: spat
spring: sprang sprung
sting: stung
stink: stank
strive: strove striven
swear: swore sworn
sweep: swept
weep: wept
wind: wound
foresee: foresaw foreseen
mistake: mistook mistaken
misunderstand: misunderstood
withhold: withheld
uphold: upheld
forbid: forbade forbidden
want: wan wanna
""".strip().splitlines():
    lem, forms = line.split(':')
    for f in forms.split():
        IRREG[f] = lem

BASE = collections.Counter()   # base forms (VB, VBP) and their frequency
VBZ_STEMS = collections.Counter()  # third-singular forms minus -s, as extra evidence


def collect_bases(trees):
    for _, _, _, t in trees:
        for tag, w in leaves(unwrap(t)):
            if tag in ('VB', 'VBP'):
                BASE[w.lower()] += 1
            elif tag == 'VBZ' and w.lower().endswith('s'):
                VBZ_STEMS[w.lower()[:-1]] += 1   # glances -> glance
                if re.search(r'(ss|ch|sh|x|z)es$', w.lower()):
                    VBZ_STEMS[w.lower()[:-2]] += 1   # attaches -> attach


def lemma(word, tag):
    w = word.lower()
    if tag in ('VB', 'VBP') and IRREG.get(w) not in ('be', 'have', 'do', 'want'):
        return w                                # lay, saw, found, wound as base forms
    if w in IRREG:
        return IRREG[w]
    cands = []
    if tag == 'VBZ' and w.endswith('s'):
        if w.endswith('ies'):
            cands.append(w[:-3] + 'y')
        if w.endswith('es'):
            cands.append(w[:-2])
        cands.append(w[:-1])
    elif tag in ('VBD', 'VBN') and w.endswith('ed'):
        if w.endswith('ied'):
            cands.append(w[:-3] + 'y')
        cands += [w[:-2], w[:-1]]
        if len(w) > 4 and w[-3] == w[-4]:
            cands.append(w[:-3])
    elif tag == 'VBG' and w.endswith('ing'):
        s = w[:-3]
        cands += [s, s + 'e']
        if len(s) > 2 and s[-1] == s[-2]:
            cands.append(s[:-1])
    if not cands:
        return w
    seen = [c for c in cands if BASE[c] > 0]
    if seen:
        return max(seen, key=lambda c: BASE[c])
    seen = [c for c in cands if VBZ_STEMS[c] > 0]
    if seen:
        return max(seen, key=lambda c: VBZ_STEMS[c])
    if tag == 'VBZ':
        return w[:-2] if re.search(r'(ss|ch|sh|x|z|o)es$', w) else w[:-1]
    s = cands[0]
    if not w.endswith('ied'):
        s = w[:-2] if tag != 'VBG' else w[:-3]
        if len(s) > 3 and s[-1] == s[-2] and s[-1] not in 'lsfz':
            return s[:-1]                       # trapped -> trap
        if re.search(r'([cguvz]|[^aeiou]l|..at|[^aeiou][iu]t|[aeiou]s)$', s):
            return s + 'e'                      # glanc, struggl, situat, collaps
    return s


def lemma_verified(word, tag):
    w = word.lower()
    l = lemma(word, tag)
    return w in IRREG or BASE[l] > 0 or VBZ_STEMS[l] > 0


# ---------------------------------------------------------------- tokens
def mtype(m):
    for t in SEM:
        if t in m['tags']:
            return t
    return 'u' + m['cat']


def coarse_comp(c):
    """NP, NP-empty, PRD, CLR, DTV/PUT, S, SBAR, PRT, S-empty, other."""
    if 'PRD' in c:
        return 'PRD'
    if 'CLR' in c:
        return 'CLR'
    if 'DTV' in c or 'PUT' in c:
        return 'DTV/PUT'
    cat = re.sub(r'[-(].*', '', c)
    if cat in ('NP', 'S', 'SBAR', 'PRT'):
        return cat + ('-empty' if '(' in c else '')
    return 'other'


def fine_mod(m):
    return m['cat'] + ''.join('-' + t for t in m['tags'] if t in SEM) + ('(e)' if m['realization'] == 'empty' else '')


def comps_of(frame):
    parts = [p for p in frame.split() if not p.startswith('SBJ')]
    return ' '.join(parts) or '0'


def preposition(node):
    for k in node.kids:
        if k.word is not None and k.cat in ('IN', 'TO', 'RP'):
            return k.word.lower()
    return None


def s_inside(node):
    """Subject kind and predicate kind of an S complement."""
    sbj = next((k for k in node.kids if 'SBJ' in k.tags), None)
    if sbj is None:
        sk = 'none'
    else:
        e = vf.empty_kind(sbj.raw)
        sk = 'overt' if e is None else e
    pred = 'other'
    for k in node.kids:
        if 'PRD' in k.tags:
            pred = 'small-clause:' + k.cat
            break
        if k.cat == 'VP':
            first = next((x for x in k.kids if x.word is not None), None)
            pred = 'VP:' + (first.cat if first is not None else '?')
            break
    return sk, pred


def tokens(root_dir):
    trees = list(masc_trees(root_dir))
    collect_bases(trees)
    out = []
    for g, fid, i, t in trees:
        raw = unwrap(t)
        if is_leaf(raw):
            continue
        root = vf.Node(raw)
        vf.number(root)
        tid = '%s/%s#%d' % (g, fid, i)
        for vp in vf.nodes(root):
            if vp.cat != 'VP' or vf.is_aux_vp(vp):
                continue
            rec = vf.verb_record(tid, root, vp)
            if rec is None:
                continue
            rec['genre'] = g
            rec['spoken'] = g in SPOKEN
            rec['lemma'] = lemma(rec['verb'], rec['tag'])
            rec['cf'] = comps_of(rec['frame_full'])
            rec['cb'] = comps_of(rec['frame_backbone'])
            mods = rec['modifiers']
            rec['mf'] = ' '.join(sorted(mtype(m) for m in mods)) or '0'
            rec['mb'] = ' '.join(sorted(mtype(m) for m in mods if m['realization'] == 'overt')) or '0'
            # daughters with their roles, prepositions, S insides
            head = next(k for k in vp.kids if k.word is not None and k.cat in vf.VERB_TAGS)
            ds = []
            for d in vp.kids:
                if d is head or d.cat == '-NONE-' and d.word:
                    continue
                ds.append({'cat': d.cat, 'tags': d.tags, 'role': vf.role(d),
                           'empty': vf.empty_kind(d.raw) if d.word is None else None,
                           'prep': preposition(d) if d.cat == 'PP' else None,
                           'lgs': d.cat == 'PP' and any('LGS' in k.tags for k in d.kids),
                           'word': d.word.lower() if d.word else None,
                           'sin': s_inside(d) if d.cat == 'S' and d.word is None and not vf.empty_kind(d.raw) else None})
            rec['daughters'] = ds
            # modifiers above the lexical verb phrase: in auxiliary or
            # coordinating verb phrases, and daughters of the clause
            up, n = [], vp.parent
            while n is not None and n.cat == 'VP':
                up += [('aux', d) for d in n.kids if d.cat != 'VP' and vf.role(d) == 'modifier']
                n = n.parent
            if n is not None and n.cat in vf.CLAUSES:
                up += [('clause', d) for d in n.kids if 'SBJ' not in d.tags and d.cat != 'VP' and vf.role(d) == 'modifier']
            rec['upmods'] = [(w, vf.describe(d)) for w, d in up]
            rec['mx'] = ' '.join(sorted([mtype(m) for m in mods] + [mtype(m) for _, m in rec['upmods']])) or '0'
            rec['vp'] = vp
            out.append(rec)
    return out


# ---------------------------------------------------------------- measures
def H(counter):
    n = sum(counter.values())
    return -sum(c / n * math.log2(c / n) for c in counter.values() if c) if n else 0.0


def mi(pairs):
    """H(Y), H(Y|X), I(X;Y) for a list of (x, y)."""
    y = collections.Counter(b for _, b in pairs)
    xy = collections.Counter(pairs)
    x = collections.Counter(a for a, _ in pairs)
    hy, hx, hxy = H(y), H(x), H(xy)
    i = hx + hy - hxy
    return hy, hy - i, i


def mi_table(pairs, seed=1, runs=5):
    hy, hyx, i = mi(pairs)
    rng = random.Random(seed)
    xs = [a for a, _ in pairs]
    ys = [b for _, b in pairs]
    base = 0.0
    for _ in range(runs):
        rng.shuffle(ys)
        base += mi(list(zip(xs, ys)))[2] / runs
    nvals = len(set(b for _, b in pairs))
    return hy, hyx, i, base, (i - base) / hy if hy else 0.0, nvals


def rarefied_H(values, n, rng, draws=50):
    if len(values) <= n:
        return H(collections.Counter(values))
    return sum(H(collections.Counter(rng.sample(values, n))) for _ in range(draws)) / draws


def cover80(counter):
    tot, acc, k = sum(counter.values()), 0, 0
    for _, c in counter.most_common():
        acc += c
        k += 1
        if acc >= 0.8 * tot:
            return k
    return k


def type_spread(obs, pv):
    """obs: Counter lemma -> count for one type; pv: background P(lemma).
    Returns n, lemmas, expected lemmas under independence, 80% cover,
    KL(P(v|type) || P(v)) in bits."""
    n = sum(obs.values())
    exp = sum(1 - (1 - p) ** n for p in pv.values())
    kl = sum(c / n * math.log2((c / n) / pv[v]) for v, c in obs.items())
    return n, len(obs), exp, cover80(obs), kl


# ---------------------------------------------------------------- report
def pct(a, b):
    return '%.1f' % (100.0 * a / b) if b else '-'


def row(*xs):
    print('| ' + ' | '.join(str(x) for x in xs) + ' |')


def main(root_dir):
    toks = tokens(root_dir)
    print('tokens', len(toks))
    # lemmatizer
    lem = collections.Counter(t['lemma'] for t in toks)
    unver = collections.Counter((t['verb'].lower(), t['tag'], t['lemma']) for t in toks
                                if not lemma_verified(t['verb'], t['tag']))
    print('\n## lemmatizer: %d lemmas; %d tokens (%s%%) with a lemma not attested as VB/VBP; top:' %
          (len(lem), sum(unver.values()), pct(sum(unver.values()), len(toks))))
    print(unver.most_common(40))
    freq = {l for l, c in lem.items() if c >= 20}
    F = [t for t in toks if t['lemma'] in freq]
    print('lemmas >= 20: %d, covering %d tokens (%s%%)' % (len(freq), len(F), pct(len(F), len(toks))))
    print('lemmas >= 100: %d' % sum(1 for c in lem.values() if c >= 100))
    print('top lemmas:', lem.most_common(40))

    def global_block(sub, name):
        print('\n## information, %s (%d tokens, %d lemmas)' % (name, len(sub), len({t['lemma'] for t in sub})))
        row('variable Y', 'values', 'H(Y)', 'H(Y|verb)', 'I(verb;Y)', 'shuffled I', '(I-shuf)/H(Y)')
        row(*['---'] * 7)
        for key, desc in [('cf', 'complement frame, full'), ('cb', 'complement frame, backbone'),
                          ('cbcat', 'complement frame, backbone, categories only'),
                          ('mf', 'modifier set, full'), ('mb', 'modifier set, backbone'),
                          ('mfpres', 'modifier types present (set), full'),
                          ('mx', 'modifier set incl. aux-VP and clause level')]:
            pairs = [(t['lemma'], t[key]) for t in sub]
            hy, hyx, i, b, u, nv = mi_table(pairs)
            row(desc, nv, '%.3f' % hy, '%.3f' % hyx, '%.3f' % i, '%.3f' % b, '%.3f' % u)
        # per daughter token
        cp = [(t['lemma'], c) for t in sub for c in t['cf'].split() if c != '0']
        mp = [(t['lemma'], m) for t in sub for m in t['mf'].split() if m != '0']
        cpc = [(v, coarse_comp(c)) for v, c in cp]
        mpf = [(t['lemma'], fine_mod(m)) for t in sub for m in t['modifiers']]
        for pairs, desc in [(cp, 'one complement (type), full'), (cpc, 'one complement (coarse type), full'),
                            (mp, 'one modifier (type), full'), (mpf, 'one modifier (tag+category), full')]:
            hy, hyx, i, b, u, nv = mi_table(pairs)
            row(desc + ' [per daughter, n=%d]' % len(pairs), nv, '%.3f' % hy, '%.3f' % hyx, '%.3f' % i, '%.3f' % b, '%.3f' % u)
    for t in toks:
        t['cbcat'] = ' '.join(re.sub(r'-.*|\(.*', '', p) for p in t['cb'].split())
        t['mfpres'] = ' '.join(sorted(set(t['mf'].split())))
    global_block(F, 'lemmas >= 20, all genres')
    global_block([t for t in F if t['spoken']], 'lemmas >= 20, spoken')
    global_block([t for t in F if not t['spoken']], 'lemmas >= 20, written')
    # same lemmas and same size in each: equalize by sampling written down to spoken size
    rng = random.Random(7)
    sp = [t for t in F if t['spoken']]
    wr = rng.sample([t for t in F if not t['spoken']], len(sp))
    global_block(wr, 'lemmas >= 20, written, random sample of spoken size')

    # (1) spread of each type across lemmas
    pv = collections.Counter(t['lemma'] for t in F)
    N = sum(pv.values())
    pv = {v: c / N for v, c in pv.items()}
    by = collections.defaultdict(collections.Counter)
    kind = {}
    for t in F:
        for c in t['cf'].split():
            if c != '0':
                by['C:' + c][t['lemma']] += 1
        for m in t['mf'].split():
            if m != '0':
                by['M:' + m][t['lemma']] += 1
    print('\n## spread of each complement and modifier type over %d lemmas (>=20); types with >=100 tokens' % len(pv))
    row('type', 'tokens', 'lemmas', 'expected lemmas if indep.', 'obs/exp', 'lemmas for 80%', 'KL from P(verb) bits', 'top lemmas')
    row(*['---'] * 8)
    res = []
    for k, obs in by.items():
        if sum(obs.values()) < 100:
            continue
        n, nl, ex, c80, kl = type_spread(obs, pv)
        res.append((kl, k, n, nl, ex, c80, obs))
    for kl, k, n, nl, ex, c80, obs in sorted(res):
        row(k, n, nl, '%.0f' % ex, '%.2f' % (nl / ex), c80, '%.2f' % kl,
            ', '.join('%s %d' % x for x in obs.most_common(4)))

    # (2) per lemma
    rng = random.Random(3)
    bylem = collections.defaultdict(list)
    for t in F:
        bylem[t['lemma']].append(t)
    print('\n## per lemma (>=100 tokens): entropies (rarefied to 100 tokens) of complement frames and modifier sets')
    stats = []
    for l, ts in bylem.items():
        if len(ts) < 100:
            continue
        cf = [t['cf'] for t in ts]
        cb = [t['cb'] for t in ts]
        mf = [t['mf'] for t in ts]
        stats.append((l, len(ts), rarefied_H(cf, 100, rng), rarefied_H(cb, 100, rng), rarefied_H(mf, 100, rng),
                      H(collections.Counter(cf)), cover80(collections.Counter(cf)),
                      sum(1 for t in ts if t['cf'] != t['cb']) / len(ts),
                      sum(1 for t in ts if t['mf'] == '0') / len(ts),
                      collections.Counter(cf).most_common(5)))
    print('lemmas with >= 100 tokens:', len(stats))
    corr = lambda xs, ys: (sum((a - sum(xs) / len(xs)) * (b - sum(ys) / len(ys)) for a, b in zip(xs, ys)) /
                           math.sqrt(sum((a - sum(xs) / len(xs)) ** 2 for a in xs) * sum((b - sum(ys) / len(ys)) ** 2 for b in ys)))
    print('mean rarefied H: comp full %.3f, comp backbone %.3f, mod set %.3f' %
          tuple(sum(s[i] for s in stats) / len(stats) for i in (2, 3, 4)))
    print('lemmas where rarefied H(mod set) > H(comp full): %d of %d' % (sum(1 for s in stats if s[4] > s[2]), len(stats)))
    print('pearson r(H comp full, H mod set) = %.3f' % corr([s[2] for s in stats], [s[4] for s in stats]))
    print('pearson r(H comp full, log freq) = %.3f' % corr([s[2] for s in stats], [math.log(s[1]) for s in stats]))
    hdr = ('lemma', 'n', 'H comp full', 'H comp bb', 'H mod set', 'H comp full (all)', 'frames for 80%', 'full!=bb', 'no modifier', 'top complement frames (full)')
    for title, key, rev in [('most frame-promiscuous', 2, True), ('most frame-faithful', 2, False)]:
        print('\n### ' + title)
        row(*hdr)
        row(*['---'] * len(hdr))
        for s in sorted(stats, key=lambda s: s[key], reverse=rev)[:12]:
            row(s[0], s[1], '%.2f' % s[2], '%.2f' % s[3], '%.2f' % s[4], '%.2f' % s[5], s[6], pct(s[7], 1), pct(s[8], 1),
                '; '.join('%s %d' % x for x in s[9]))
    print('\n### most modifier-promiscuous and -faithful (rarefied H mod set)')
    for s in sorted(stats, key=lambda s: s[4], reverse=True)[:8] + [None] + sorted(stats, key=lambda s: s[4])[:8]:
        if s is None:
            print('...')
            continue
        mods = collections.Counter(m for t in bylem[s[0]] for m in t['mf'].split() if m != '0').most_common(5)
        print(s[0], s[1], '%.2f' % s[4], 'no mod %.1f%%' % (100 * s[8]), mods)
    print('\n### lemmas most changed by empty elements (share of tokens with full != backbone complements)')
    for s in sorted(stats, key=lambda s: -s[7])[:12]:
        print(s[0], s[1], pct(s[7], 1))

    # full vs backbone: which empty complements
    print('\n## empty complements and modifiers (all tokens)')
    ec = collections.Counter()
    em = collections.Counter()
    for t in toks:
        for c in t['complements']:
            if c['realization'] == 'empty':
                ec[(c['cat'], c['empty'])] += 1
        for m in t['modifiers']:
            if m['realization'] == 'empty':
                em[(mtype(m), m['empty'])] += 1
    nc = sum(len(t['complements']) for t in toks)
    nm = sum(len(t['modifiers']) for t in toks)
    print('complements %d, empty %d (%s%%); modifiers %d, empty %d (%s%%)' %
          (nc, sum(ec.values()), pct(sum(ec.values()), nc), nm, sum(em.values()), pct(sum(em.values()), nm)))
    print('empty complements', ec.most_common(15))
    print('empty modifiers', em.most_common(15))
    mtot = collections.Counter(mtype(m) for t in toks for m in t['modifiers'])
    print('empty share by modifier type:', [(k, v, pct(sum(c for (a, _), c in em.items() if a == k), v)) for k, v in mtot.most_common(14)])
    up = collections.Counter((w, mtype(m)) for t in toks for w, m in t['upmods'])
    print('modifiers above the lexical VP: aux-VP %d, clause %d; by type:' % (
        sum(v for (w, _), v in up.items() if w == 'aux'), sum(v for (w, _), v in up.items() if w == 'clause')), up.most_common(24))
    print('tokens with cf != cb: %s%%; with mf != mb: %s%%' %
          (pct(sum(t['cf'] != t['cb'] for t in toks), len(toks)), pct(sum(t['mf'] != t['mb'] for t in toks), len(toks))))

    # doubtful assignment
    print('\n## VP daughters by category: role distribution (what the backbone, with no tags, cannot tell apart)')
    cr = collections.defaultdict(collections.Counter)
    tagdist = collections.defaultdict(collections.Counter)
    for t in toks:
        for d in t['daughters']:
            cr[d['cat']][d['role']] += 1
            if d['empty'] is None:
                tagdist[d['cat']]['-'.join(d['tags']) or '(none)'] += 1
    row('category', 'n', 'complement %', 'modifier %', 'other %', 'overt tags (top)')
    row(*['---'] * 6)
    for c, cnt in sorted(cr.items(), key=lambda x: -sum(x[1].values()))[:16]:
        n = sum(cnt.values())
        row(c, n, pct(cnt['complement'], n), pct(cnt['modifier'], n), pct(cnt['other'], n),
            ', '.join('%s %d' % x for x in tagdist[c].most_common(6)))
    oth = collections.Counter((d['cat'], '-'.join(d['tags'])) for t in toks for d in t['daughters'] if d['role'] == 'other')
    print('role other:', oth.most_common(20))

    # PPs: preposition x tag consistency
    print('\n## overt PP daughters of VP: tags')
    pps = [(t['lemma'], d['prep'], '-'.join(d['tags']) or '(none)', t['id']) for t in toks for d in t['daughters']
           if d['cat'] == 'PP' and d['empty'] is None]
    ptag = collections.Counter(p[2] for p in pps)
    print(len(pps), ptag.most_common(20))
    un = collections.Counter(p[1] for p in pps if p[2] == '(none)')
    clr = collections.Counter(p[1] for p in pps if 'CLR' in p[2])
    print('untagged PP prepositions', un.most_common(15))
    print('CLR PP prepositions', clr.most_common(15))
    vp_pairs = collections.defaultdict(collections.Counter)
    for l, p, tg, _ in pps:
        coarse = 'CLR' if 'CLR' in tg else 'PRD' if 'PRD' in tg else 'DTV' if 'DTV' in tg else 'PUT' if 'PUT' in tg else \
            'none' if tg == '(none)' else 'SEM'
        vp_pairs[(l, p)][coarse] += 1
    big = {k: v for k, v in vp_pairs.items() if sum(v.values()) >= 10}
    mixed = {k: v for k, v in big.items() if v['CLR'] >= 0.2 * sum(v.values()) and v['none'] >= 0.2 * sum(v.values())}
    tokbig = sum(sum(v.values()) for v in big.values())
    tokmixed = sum(sum(v.values()) for v in mixed.values())
    print('(lemma, prep) pairs with >=10 PPs: %d (%d PPs); with CLR and untagged each >=20%%: %d (%d PPs, %s%%)' %
          (len(big), tokbig, len(mixed), tokmixed, pct(tokmixed, tokbig)))
    comp_share = lambda v: (v['CLR'] + v['PRD'] + v['DTV'] + v['PUT']) / sum(v.values())
    mid = {k: v for k, v in big.items() if 0.2 <= comp_share(v) <= 0.8}
    print('pairs whose complement share is between 20%% and 80%%: %d (%d PPs, %s%%)' %
          (len(mid), sum(sum(v.values()) for v in mid.values()), pct(sum(sum(v.values()) for v in mid.values()), tokbig)))
    for k, v in sorted(mixed.items(), key=lambda x: -sum(x[1].values()))[:25]:
        print('  ', k, dict(v))
    # majority-tag agreement: how predictable is the coarse tag from (lemma, prep)?
    agree = sum(v.most_common(1)[0][1] for v in big.values())
    print('majority coarse tag of (lemma,prep) covers %s%% of those PPs' % pct(agree, tokbig))

    # summary of doubtful assignments
    print('\n## doubtful complement/modifier assignments (all tokens)')
    alld = [(t, d) for t in toks for d in t['daughters'] if d['role'] in ('complement', 'modifier')]
    print('complement+modifier daughters:', len(alld))
    def cnt(f):
        return sum(1 for t, d in alld if f(t, d))
    items = [
        ('untagged overt PP (modifier by default)', lambda t, d: d['cat'] == 'PP' and not d['tags'] and d['empty'] is None),
        ('  of which by-PP with NP-LGS (passive agent)', lambda t, d: d['cat'] == 'PP' and not d['tags'] and d.get('lgs')),
        ('any -CLR daughter (complement)', lambda t, d: 'CLR' in d['tags']),
        ('  of which PP-CLR', lambda t, d: 'CLR' in d['tags'] and d['cat'] == 'PP'),
        ('S complement with overt subject (small clause / ECM)', lambda t, d: d['sin'] and d['role'] == 'complement' and d['sin'][0] == 'overt'),
        ('RB not/n\'t as modifier', lambda t, d: d['cat'] == 'RB' and d.get('word') in ('not', "n't", 'n?t', 'nt')),
        ('-DTV or -PUT PP', lambda t, d: set(d['tags']) & {'DTV', 'PUT'}),
        ('-PRD with a semantic tag (LOC-PRD, MNR-PRD...)', lambda t, d: 'PRD' in d['tags'] and set(d['tags']) & set(SEM)),
        ('-DIR modifier', lambda t, d: 'DIR' in d['tags'] and d['role'] == 'modifier'),
    ]
    for name, f in items:
        k = cnt(f)
        ks = sum(1 for t, d in alld if t['spoken'] and f(t, d))
        print('  %-58s %6d  %5s%% of daughters; spoken %s%%, written %s%% of verbs' % (
            name, k, pct(k, len(alld)), pct(ks, sum(1 for t in toks if t['spoken'])), pct(k - ks, sum(1 for t in toks if not t['spoken']))))
    oth = [(t, d) for t in toks for d in t['daughters'] if d['role'] == 'other' and d['cat'] in vf.VERB_TAGS]
    print('  verb (VB*) daughters of a VP, flat verb coordination, no record of their own: %d' % len(oth))

    # S complements of perception, causative, ECM verbs
    print('\n## S complements (overt, untagged or -CLR): subject and predicate inside, by lemma')
    sc = collections.defaultdict(collections.Counter)
    allS = collections.Counter()
    for t in toks:
        for d in t['daughters']:
            if d['sin'] and d['role'] == 'complement':
                sc[t['lemma']][d['sin']] += 1
                allS[d['sin']] += 1
    print('all S complements:', sum(allS.values()), allS.most_common(15))
    subj = collections.Counter(k[0] for k in allS.elements())
    print('by subject:', subj.most_common())
    for l in ['see', 'hear', 'watch', 'feel', 'notice', 'make', 'let', 'have', 'help', 'get', 'want', 'need', 'like',
              'believe', 'consider', 'find', 'expect', 'keep', 'seem', 'try', 'go', 'start', 'begin', 'use', 'call']:
        if sc[l]:
            n = sum(sc[l].values())
            ov = sum(c for (s, _), c in sc[l].items() if s == 'overt')
            print('  %-9s S=%4d overt-subject %s%%  %s' % (l, n, pct(ov, n), sc[l].most_common(4)))

    # Levin: causative/inchoative, dative
    print('\n## Levin: causative/inchoative verbs; complement frames (full), active intransitive vs passive vs transitive')
    CI = ['break', 'open', 'close', 'change', 'increase', 'grow', 'move', 'turn', 'start', 'begin', 'stop', 'end',
          'improve', 'drop', 'burn', 'develop', 'fill', 'roll', 'shut', 'melt', 'decrease', 'split']
    CTRL = ['arrive', 'die', 'happen', 'come', 'make', 'take', 'buy', 'need', 'like']
    row('verb', 'n', 'no NP: intrans. (overt subj)', 'NP(*) passive-type', 'overt NP object', 'backbone: no NP at all', 'passives among backbone NP-less')
    row(*['---'] * 7)
    for l in CI + ['--'] + CTRL:
        if l == '--':
            row(*['..'] * 7)
            continue
        ts = bylem.get(l, [])
        if not ts:
            continue
        n = len(ts)
        pas = sum(1 for t in ts if 'NP(*)' in t['cf'].split())
        ov = sum(1 for t in ts if 'NP' in t['cf'].split())
        intr = sum(1 for t in ts if not any(p.startswith('NP') for p in t['cf'].split()))
        bbno = sum(1 for t in ts if not any(p == 'NP' for p in t['cb'].split()))
        row(l, n, pct(intr, n), pct(pas, n), pct(ov, n), pct(bbno, n), pct(pas, bbno))
    print('\n## Levin: dative verbs; NP NP vs NP PP(to) and its tag')
    for l in ['give', 'send', 'tell', 'show', 'offer', 'bring', 'sell', 'pay', 'teach', 'hand', 'ask', 'write', 'provide', 'lend', 'owe']:
        ts = bylem.get(l, [])
        if not ts:
            continue
        nn = sum(1 for t in ts if re.search(r'\bNP\S* NP\b', t['cf']))
        topp = collections.Counter('-'.join(d['tags']) or '(none)' for t in ts for d in t['daughters']
                                   if d['cat'] == 'PP' and d['prep'] in ('to', 'for') and any(x['cat'] == 'NP' for x in t['daughters']))
        print('  %-8s n=%4d  NP NP: %3d   NP PP(to/for) by tag: %s' % (l, len(ts), nn, dict(topp)))

    # Dowty / agentivity: PRP, MNR, BNF rates by lemma
    print('\n## rates of PRP, MNR, BNF, TMP, LOC modifiers per 100 tokens, by lemma (>=100 tokens)')
    rates = []
    for l, ts in bylem.items():
        if len(ts) < 100:
            continue
        c = collections.Counter(m for t in ts for m in t['mf'].split())
        rates.append((l, len(ts), *(100.0 * c[k] / len(ts) for k in ('PRP', 'MNR', 'BNF', 'TMP', 'LOC', 'uPP', 'ADV'))))
    print('lowest PRP+MNR:', [(r[0], r[1], '%.1f' % (r[2] + r[3])) for r in sorted(rates, key=lambda r: r[2] + r[3])[:15]])
    print('highest PRP+MNR:', [(r[0], r[1], '%.1f' % (r[2] + r[3])) for r in sorted(rates, key=lambda r: -(r[2] + r[3]))[:15]])
    for l in ['be', 'know', 'seem', 'have', 'want', 'like', 'need', 'think', 'believe', 'mean', 'use', 'work', 'make', 'do', 'go', 'get', 'take', 'come']:
        r = next((r for r in rates if r[0] == l), None)
        if r:
            print('  %-8s n=%5d PRP %.1f MNR %.1f BNF %.1f TMP %.1f LOC %.1f uPP %.1f ADV %.1f' % r)

    # genres: modifier types per 100 verbs
    print('\n## modifier types per 100 verb tokens, by genre (all tokens; full, overt+empty)')
    TYPES = ['TMP', 'LOC', 'DIR', 'MNR', 'PRP', 'ADV', 'EXT', 'uPP', 'uADVP', 'uRB']
    gc = collections.defaultdict(collections.Counter)
    gn = collections.Counter()
    for t in toks:
        g = t['genre']
        gn[g] += 1
        gc[g].update(t['mf'].split())
        for grp in ('SPOKEN' if t['spoken'] else 'WRITTEN', 'ALL'):
            gn[grp] += 1
            gc[grp].update(t['mf'].split())
    gx = collections.Counter()
    for t in toks:
        k = len(t['upmods'])
        gx[t['genre']] += k
        gx['SPOKEN' if t['spoken'] else 'WRITTEN'] += k
        gx['ALL'] += k
    row('genre', 'verbs', 'no modifier %', *TYPES, 'all modifiers', 'top type', 'above VP')
    row(*['---'] * (len(TYPES) + 6))
    order = sorted([g for g in gn if g not in ('SPOKEN', 'WRITTEN', 'ALL')], key=lambda g: (g not in SPOKEN, g)) + ['SPOKEN', 'WRITTEN', 'ALL']
    for g in order:
        n = gn[g]
        allm = sum(v for k, v in gc[g].items() if k != '0')
        top = max(TYPES, key=lambda k: gc[g][k])
        row(g, n, pct(gc[g]['0'], n), *('%.1f' % (100.0 * gc[g][k] / n) for k in TYPES), '%.1f' % (100.0 * allm / n), top,
            '%.1f' % (100.0 * gx[g] / n))
    print('\n## modifier types incl. aux-VP and clause level, per 100 verbs, spoken vs written')
    for grp in (True, False):
        c = collections.Counter(m for t in toks if t['spoken'] == grp for m in t['mx'].split() if m != '0')
        n = sum(1 for t in toks if t['spoken'] == grp)
        print('spoken' if grp else 'written', [(k, '%.1f' % (100.0 * v / n)) for k, v in c.most_common(12)])
    # complement frame distribution by spoken/written
    print('\n## top complement frames (full), spoken vs written, % of tokens')
    for grp in (True, False):
        c = collections.Counter(t['cf'] for t in toks if t['spoken'] == grp)
        n = sum(c.values())
        print('spoken' if grp else 'written', [(k, pct(v, n)) for k, v in c.most_common(12)])


def show(root_dir, tid, pos):
    for g, fid, i, t in masc_trees(root_dir):
        if '%s/%s#%d' % (g, fid, i) != tid:
            continue
        raw = unwrap(t)
        root = vf.Node(raw)
        vf.number(root)
        for vp in vf.nodes(root):
            if vp.cat == 'VP' and not vf.is_aux_vp(vp):
                h = next((k for k in vp.kids if k.word is not None and k.cat in vf.VERB_TAGS), None)
                if h is not None and h.pos == pos:
                    up = vp.parent
                    while up is not None and up.cat == 'VP':
                        up = up.parent
                    print(pretty((up or vp).raw))
                    r = vf.verb_record(tid, root, vp)
                    print('full: %r  backbone: %r  mods: %s' % (r['frame_full'], r['frame_backbone'],
                                                                [m['label'] for m in r['modifiers']]))
                    return


def lemma_profile(root_dir, lem):
    ts = [t for t in tokens(root_dir) if t['lemma'] == lem]
    print('%s: %d tokens' % (lem, len(ts)))
    for key, name in (('cf', 'complement frames, full'), ('cb', 'complement frames, backbone'), ('mf', 'modifier sets, full')):
        c = collections.Counter(t[key] for t in ts)
        print(name, '(H=%.2f):' % H(c), '; '.join('%s %d' % x for x in c.most_common(15)))


if __name__ == '__main__':
    if len(sys.argv) > 2 and sys.argv[2] == '--lemma':
        lemma_profile(sys.argv[1], sys.argv[3])
    elif len(sys.argv) > 2 and sys.argv[2] == '--show':
        show(sys.argv[1], sys.argv[3], int(sys.argv[4]))
    else:
        main(sys.argv[1])
