#!/usr/bin/env bash
# Parse every sentence of MASC with the treebank grammar read off its trees,
# using the fast context-free parser (go/cfg): a line per sentence in
# OUT_DIR/forests.tsv, progress in OUT_DIR/forests.log.
#
#   tools/masc/forests.sh MASC_DATA_DIR OUT_DIR [flags for go/cmd/forests]
#
# MASC_DATA_DIR is the directory holding MASC's spoken/ and written/ trees.
# The whole of MASC should take two to three hours on one core, much of it
# counting the trees of the longest sentences exactly. Its longest sentence
# (174 words) should need 22-25 GB, so it wants a 32 GB machine; -count=false
# roughly halves that. -maxwords 60 is a quick first run. Run it again after
# an interruption and it carries on where it stopped.
set -euo pipefail
if [ $# -lt 2 ]; then sed -n '2,13p' "$0"; exit 2; fi
masc=$1 out=$2
shift 2
repo=$(cd "$(dirname "$0")/../.." && pwd)
mkdir -p "$out"
out=$(cd "$out" && pwd)
if [ ! -s "$out/tb.fcfg" ]; then
  python3 "$repo/tools/masc/treebank.py" "$masc" "$out"
fi
(cd "$repo/go" && go build -o "$out/forests" ./cmd/forests)
tsv=$out/forests.tsv
resume=()
if [ -s "$tsv" ]; then
  # drop a last line an interrupted run left unfinished, and skip what is done
  python3 -c 'import sys; p = sys.argv[1]; b = open(p, "rb").read(); b.endswith(b"\n") or open(p, "wb").write(b[:b.rfind(b"\n") + 1])' "$tsv"
  resume=(-done "$tsv")
fi
# GOMEMLIMIT makes the collector work harder before the heap nears 28 GB.
GOMEMLIMIT=${GOMEMLIMIT:-28GiB} "$out/forests" -grammar "$out/tb.fcfg" -sents "$out/sents.txt" \
  -gold "$out/trees.jsonl" ${resume[@]+"${resume[@]}"} "$@" >> "$tsv" 2> >(tee -a "$out/forests.log" >&2)
