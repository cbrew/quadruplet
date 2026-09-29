"""A crude lemmatizer for MASC's verbs: a table of irregular forms, then
suffix stripping, choosing among candidates by how often each is attested as
a base form (VB, VBP) in MASC. Call collect_bases(masc_trees(...)) first.
Written for docs/verbs/06-complements-and-modifiers.md, which checks it."""
import collections, re
from masctrees import leaves, unwrap

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
