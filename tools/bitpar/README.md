# Timing go/cfg against BitPar

These scripts compare `go/cfg` with Helmut Schmid's BitPar on the same
treebank grammar and sentences. The results are in `results.jsonl` and in
[`docs/fast-parser.md`](../../docs/fast-parser.md) §6.

| file | what it does |
|---|---|
| `treebank.py` | reads the context-free grammar `tb.fcfg` off the MASC trees, and lists the sentences in `sents.txt` |
| `convert.py` | writes that grammar in BitPar's formats, `bp.gram` and `bp.lex` |
| `input.py` | writes sentences as BitPar input, each word with its lexicon tags |
| `forest-time.patch` | makes `bitpar -i` report the time of its forest pass and the forest's size |
| `bench.py` | runs both parsers, one process per sentence, and writes a JSON line per sentence |
| `count.py` | counts the trees in BitPar's printed forests (`bitpar -o`) |
| `plot.py` | draws `docs/bitpar-scaling.svg` from the results |

## Reproducing

BitPar is free for research and education, but not ours to redistribute,
so it is fetched and patched (`$QUADRUPLET` is this repository):

```bash
curl -LO https://www.cis.uni-muenchen.de/~schmid/tools/BitPar/data/BitPar.tar.gz
tar xzf BitPar.tar.gz
(cd BitPar/src && patch < $QUADRUPLET/tools/bitpar/forest-time.patch && make)
```

Then, with the MASC data unpacked as `tools/masc` expects:

```bash
python3 treebank.py MASC_DATA_DIR work     # tb.fcfg, sents.txt
python3 convert.py work                    # bp.gram, bp.lex
(cd ../../go && go build -o /tmp/qp ./cmd/quadruplet)
python3 bench.py /tmp/qp BitPar/src/bitpar work 5,10,15,20,25,30,35,40,45,50,55,60,70,80 3 > results.jsonl
python3 plot.py results.jsonl ../../docs/bitpar-scaling.svg
```

`bench.py` runs `go/cfg` with `GOMAXPROCS=1`, so both parsers use one core,
and with `-count=false`, since BitPar does not count trees either.
It reads each parser's own timings, which leave out loading the grammar,
and each process's peak resident memory, which includes it: about 120 MB
for `quadruplet`, which reads the grammar as a feature grammar, and 35 MB
for BitPar.

## Making BitPar parse the same grammar

Four things had to be set for BitPar to parse exactly the same grammar as
`go/cfg`:

* **Counts.** BitPar requires a count on every rule and lexical entry. Each
  gets 1.
* **`-W`.** Without it, BitPar smooths the lexicon and a word takes tags it
  was never seen with. `-W` reads the counts as weights and does no
  smoothing.
* **Sentence-initial words.** Until it has seen a lowercase word, BitPar
  gives a capitalized word the tags of its lowercase form as well ("I" gets
  those of "i"). `input.py` gives each word its tags on the input line,
  which BitPar then keeps to.
* **Tag names.** BitPar matches tags given in the input as prefixes, so `NN`
  would also admit `NNS`, `NNP` and `NNPS`. `convert.py` ends every symbol
  with `~`.

With these, the two parsers' tree counts agree on 300 random sentences of 2
to 7 words, and on the nine benchmark sentences of 10 to 20 words (`bitpar
-o | python3 count.py`). Beyond 20 words BitPar's printed forest is too big
for `count.py`.
