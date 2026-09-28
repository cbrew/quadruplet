"""Draw the scaling comparison as a static SVG: parse time and peak memory
against sentence length, log-scaled, one point per sentence and a line
through the medians."""
import json, math, statistics, sys
# python3 plot.py results.jsonl out.svg
recs = [json.loads(l) for l in open(sys.argv[1])]
SERIES = [('cfg', 'go/cfg', '#2a78d6'), ('bitpar', 'BitPar', '#eb6834')]
def t(r): return r['recognise'] + r['build'] if 'build' in r else None
def m(r): return r['rss'] / 2**20 if 'build' in r else None
W, H = 800, 350
PW, PH, TOP, LEFT, GAP = 270, 220, 50, 62, 130
out = ['<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 %d %d" font-family="system-ui, -apple-system, Segoe UI, sans-serif" font-size="12">' % (W, H),
       '<rect width="100%" height="100%" fill="#fcfcfb"/>']
def panel(x0, title, get, ylo, yhi, ticks, unit):
    xs = sorted({r['n'] for r in recs}); xlo, xhi = 4, 90
    X = lambda n: x0 + (math.log(n) - math.log(xlo)) / (math.log(xhi) - math.log(xlo)) * PW
    Y = lambda v: TOP + PH - (math.log10(v) - math.log10(ylo)) / (math.log10(yhi) - math.log10(ylo)) * PH
    out.append('<text x="%d" y="%d" fill="#0b0b0b" font-weight="600">%s</text>' % (x0, TOP - 22, title))
    for v in ticks:
        out.append('<line x1="%d" x2="%d" y1="%.1f" y2="%.1f" stroke="#e4e3df"/>' % (x0, x0 + PW, Y(v), Y(v)))
        out.append('<text x="%d" y="%.1f" fill="#52514e" text-anchor="end" dy="4">%s</text>' % (x0 - 6, Y(v), unit(v)))
    for n in (5, 10, 20, 40, 80):
        out.append('<text x="%.1f" y="%d" fill="#52514e" text-anchor="middle">%d</text>' % (X(n), TOP + PH + 16, n))
    out.append('<line x1="%d" x2="%d" y1="%d" y2="%d" stroke="#8a8984"/>' % (x0, x0 + PW, TOP + PH, TOP + PH))
    out.append('<text x="%d" y="%d" fill="#52514e" text-anchor="middle">words (log scale)</text>' % (x0 + PW / 2, TOP + PH + 34))
    for key, name, col in SERIES:
        pts = [(r['n'], get(r[key])) for r in recs if get(r[key])]
        med = []
        for n in xs:
            vs = [v for k, v in pts if k == n]
            if vs: med.append((n, statistics.median(vs)))
        out.append('<polyline fill="none" stroke="%s" stroke-width="2" points="%s"/>' % (col, ' '.join('%.1f,%.1f' % (X(n), Y(v)) for n, v in med)))
        for n, v in pts:
            out.append('<circle cx="%.1f" cy="%.1f" r="3.5" fill="%s" stroke="#fcfcfb" stroke-width="1.5"><title>%s, %d words: %s</title></circle>' % (X(n), Y(v), col, name, n, unit(v)))
        n, v = med[-1]
        out.append('<text x="%.1f" y="%.1f" fill="#0b0b0b" dx="6" dy="4">%s</text>' % (X(n), Y(v), name))
def secs(v): return ('%g s' % v) if v >= 1 else ('%g ms' % (v * 1000))
def mb(v): return ('%g GB' % (v / 1024)) if v >= 1024 else ('%g MB' % v)
panel(LEFT, 'Parse time (recognise + build), one core', t, 1e-3, 1e3, [1e-3, 1e-2, 1e-1, 1, 10, 100, 1000], secs)
panel(LEFT + PW + GAP, 'Peak memory of the process', m, 16, 16384, [16, 64, 256, 1024, 4096, 16384], mb)
lx = LEFT
for key, name, col in SERIES:
    out.append('<line x1="%d" x2="%d" y1="%d" y2="%d" stroke="%s" stroke-width="2"/><circle cx="%d" cy="%d" r="3.5" fill="%s"/>' % (lx, lx + 18, H - 12, H - 12, col, lx + 9, H - 12, col))
    out.append('<text x="%d" y="%d" fill="#0b0b0b" dy="4">%s</text>' % (lx + 24, H - 12, name))
    lx += 110
out.append('<text x="%d" y="%d" fill="#52514e" dy="4">dots: sentences; lines: medians</text>' % (lx + 10, H - 12))
out.append('</svg>')
open(sys.argv[2], 'w').write('\n'.join(out))
