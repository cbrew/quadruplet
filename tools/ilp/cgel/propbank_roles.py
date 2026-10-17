"""PropBank's arguments as semantic roles in CGEL's terms (ch. 4 §2.2), and
PropBank's annotation of MASC aligned with the sentence programs.

CGEL does not attempt a closed inventory of roles (§2.1); it invokes them at
the level of generality a case needs. So a PP gets a role at each level of a
small hierarchy, and a learner can choose the level:

    agent < causer                 recipient < goal
    goal, source, path, location < place

A numbered argument (ARG0-5) takes its role from the roleset's frame file
(github.com/propbank/propbank-frames): the VerbNet role it links to where
there is one, else PropBank's function tag. A modifier (ARGM-) takes the role
its label names; these are CGEL's adjunct categories rather than §2.2 roles
(time, manner, purpose, reason ...). Roles §2.2 does not name (topic,
attribute ...) are kept under their VerbNet names.
"""
import glob
import os
import re
import xml.etree.ElementTree as ET

VERBNET = {   # VerbNet 3.3 role -> CGEL §2.2 role
    'agent': 'agent', 'causer': 'causer', 'cause': 'causer', 'instrument': 'instrument',
    'patient': 'patient', 'co-patient': 'patient', 'experiencer': 'experiencer',
    'stimulus': 'stimulus', 'theme': 'theme', 'co-theme': 'theme', 'pivot': 'theme',
    'recipient': 'recipient', 'destination': 'goal', 'goal': 'goal', 'source': 'source',
    'initial_location': 'source', 'location': 'location', 'trajectory': 'path',
    'beneficiary': 'beneficiary', 'result': 'goal', 'product': 'factitive', 'material': 'source',
    'asset': 'secondary_theme', 'value': 'secondary_theme', 'co_theme': 'theme',
    'co-agent': 'agent', 'co_agent': 'agent', 'co_patient': 'patient', 'predicate': 'predicative',
}
FUNCTION = {  # PropBank function tag -> role, where VerbNet gives none
    'PAG': 'causer', 'PPT': 'theme', 'GOL': 'goal', 'DIR': 'path', 'LOC': 'location',
    'COM': 'comitative', 'PRD': 'predicative', 'MNR': 'manner', 'EXT': 'extent',
    'CAU': 'reason', 'TMP': 'time', 'PRP': 'purpose', 'ADV': 'adverbial', 'ADJ': 'predicative',
    'VSP': 'verb_specific', 'REC': 'reciprocal',
}
ARGM = {      # modifier label -> role
    'TMP': 'time', 'LOC': 'location', 'DIR': 'path', 'GOL': 'goal', 'MNR': 'manner',
    'EXT': 'extent', 'PRP': 'purpose', 'PNC': 'purpose', 'CAU': 'reason', 'COM': 'comitative',
    'PRD': 'predicative', 'ADV': 'adverbial', 'DIS': 'discourse', 'NEG': 'negation',
    'MOD': 'modal', 'REC': 'reciprocal', 'ADJ': 'predicative', 'LVB': 'light_verb', 'CXN': 'construction',
}
ISA = {'agent': 'causer', 'recipient': 'goal', 'factitive': 'theme', 'secondary_theme': 'theme',
       'goal': 'place', 'source': 'place', 'path': 'place', 'location': 'place'}


def frames(frames_dir):
    """{(roleset, n): role}"""
    out = {}
    for f in glob.glob(os.path.join(frames_dir, 'frames', '*.xml')):
        try:
            root = ET.parse(f).getroot()
        except ET.ParseError:       # a few frame files are not well-formed
            frames.skipped.append(os.path.basename(f))
            continue
        for rs in root.iter('roleset'):
            for role in rs.iter('role'):
                n = role.get('n', '').lower()
                vn = [l.text.strip().lower() for l in role.iter('rolelink')
                      if l.get('resource') == 'VerbNet' and l.text]
                r = next((VERBNET[v] for v in vn if v in VERBNET), None) or \
                    (vn[0] if vn else FUNCTION.get((role.get('f') or '').upper()))
                if r:
                    out[rs.get('id'), n] = r
    return out


frames.skipped = []


def roles(label, roleset, fr):
    """The roles, most specific first, of an argument labelled label (ARG2,
    ARGM-LOC, R-ARG1 ...) of roleset"""
    label = re.sub(r'^[RC]-', '', label)
    m = re.match(r'ARGM-(\w+)', label)
    if m:
        r = ARGM.get(m.group(1), m.group(1).lower())
    else:
        m = re.match(r'ARG(\d)', label)
        r = fr.get((roleset, m.group(1))) if m else None
    out = []
    while r:
        out.append(r)
        r = ISA.get(r)
    return out


def masc(propbank_masc_dir, programs_dir):
    """{(program path, verb word position): [(label, roleset, first, last)]}, word
    positions from 1 as in the programs. Sentence k of a PropBank document is
    program k.pl of the MASC document of the same name; its tokens are the
    program's words (PropBank's MASC files have no empty elements)."""
    from propbank_ewt import read_file
    docs = {}
    for d in glob.glob(os.path.join(programs_dir, '*', '*')):
        docs[os.path.basename(d)] = d
    out = {}
    for f in glob.glob(os.path.join(propbank_masc_dir, '*', '*', '*.gold_skel')):
        name = os.path.basename(f)[:-len('.gold_skel')]
        if name not in docs:
            continue
        for k, (_tags, preds) in enumerate(read_file(f)):
            path = os.path.join(docs[name], '%d.pl' % k)
            for i, _lemma, roleset, args in preds:
                out[path, i + 1] = [(label, roleset, a + 1, b + 1) for label, a, b in args]
    return out


def covering(args, first, last):
    """The argument covering most of the words first..last, or None"""
    best = None
    for label, roleset, a, b in args:
        n = max(0, min(b, last) - max(a, first) + 1)
        if n and (best is None or n > best[0]):
            best = (n, label, roleset)
    return best and best[1:]
