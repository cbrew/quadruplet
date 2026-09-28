"""Time go/cfg and BitPar on the same MASC sentences, one process per parse,
recording each parser's own timings, forest size, and peak memory."""
import json, os, random, re, subprocess, sys, threading, time
# python3 bench.py QP BITPAR DIR LENGTHS PER > results.jsonl
# QP is the quadruplet command, BITPAR the patched bitpar, DIR holds tb.fcfg,
# sents.txt, bp.gram and bp.lex; LENGTHS is e.g. 5,10,20 and PER the number
# of sentences of each length.
QP, BP, DIR = sys.argv[1:4]
lengths = [int(x) for x in sys.argv[4].split(',')]
per = int(sys.argv[5])
LIMIT = 12 << 30          # kill a run whose resident memory passes 12 GB
TIMEOUT = 1200
rows = [l.rstrip('\n').split('\t') for l in open(os.path.join(DIR, 'sents.txt'))]
bad = set('(){}#')
lex = {}
for line in open(os.path.join(DIR, 'bp.lex')):
    w, *ts = line.rstrip('\n').split('\t')
    lex[w] = [t.split(' ')[0] for t in ts]

def run(cmd, stdin, env=None):
    """Run cmd; return (stdout+stderr, wall seconds, cpu seconds, peak RSS bytes, status)."""
    p = subprocess.Popen(cmd, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, env=env)
    killed = []
    def watch():
        while p.poll() is None:
            try:
                for l in open('/proc/%d/status' % p.pid):
                    if l.startswith('VmRSS:') and int(l.split()[1]) * 1024 > LIMIT:
                        killed.append('memory'); p.kill()
            except OSError:
                return
            time.sleep(0.05)
    t0 = time.time()
    threading.Thread(target=watch, daemon=True).start()
    out = []
    def feed():
        p.stdin.write(stdin.encode()); p.stdin.close()
    threading.Thread(target=feed, daemon=True).start()
    timer = threading.Timer(TIMEOUT, lambda: (killed.append('time'), p.kill())); timer.start()
    data = p.stdout.read().decode(errors='replace')
    _, status, ru = os.wait4(p.pid, 0)
    timer.cancel()
    return data, time.time() - t0, ru.ru_utime + ru.ru_stime, ru.ru_maxrss * 1024, (killed[0] if killed else status)

def godur(s):
    m = re.fullmatch(r'([\d.]+)(ns|µs|ms|s|m)', s)
    if not m:  # e.g. 1m2.5s
        m2 = re.fullmatch(r'(\d+)m([\d.]+)s', s); return int(m2.group(1)) * 60 + float(m2.group(2))
    return float(m.group(1)) * {'ns': 1e-9, 'µs': 1e-6, 'ms': 1e-3, 's': 1, 'm': 60}[m.group(2)]

def ours(sent):
    env = dict(os.environ, GOMAXPROCS='1')
    out, wall, cpu, rss, st = run([QP, '-grammar', os.path.join(DIR, 'tb.fcfg'), '-start', 'Top', '-fast', '-quiet', '-count=false'], sent + '\n', env)
    m = re.search(r'(\S+) trees, (\d+) items \((\d+) of the grammar's own symbols\), (\d+) hyperedges, (\d+) derivable, \S+ \(recognise (\S+), build (\S+)\)', out)
    if not m:
        return {'status': st, 'wall': wall, 'rss': rss}
    return {'status': st, 'trees': m.group(1), 'items': int(m.group(2)), 'hyperedges': int(m.group(4)), 'derivable': int(m.group(5)),
            'recognise': godur(m.group(6)), 'build': godur(m.group(7)), 'wall': wall, 'cpu': cpu, 'rss': rss}

def bitpar(sent):
    inp = ''.join(w + '\t' + ' '.join(lex[w]) + '\n' for w in sent.split()) + '\n'
    out, wall, cpu, rss, st = run([BP, '-W', '-i', '-s', 'Top~', os.path.join(DIR, 'bp.gram'), os.path.join(DIR, 'bp.lex')], inp)
    c = re.search(r'computing chart\.*finished\ntime ([\d.]+)', out)
    f = re.search(r'forest time ([\d.]+) nodes (\d+) analyses (\d+)', out)
    if not (c and f):
        return {'status': st, 'wall': wall, 'rss': rss}
    return {'status': st, 'recognise': float(c.group(1)), 'build': float(f.group(1)), 'items': int(f.group(2)), 'hyperedges': int(f.group(3)),
            'wall': wall, 'cpu': cpu, 'rss': rss}

random.seed(7)
for L in lengths:
    pool = [r for r in rows if int(r[1]) == L and not any(c in bad for c in r[2])]
    for r in random.sample(pool, min(per, len(pool))):
        rec = {'id': r[0], 'n': L, 'sentence': r[2], 'cfg': ours(r[2]), 'bitpar': bitpar(r[2])}
        print(json.dumps(rec), flush=True)
