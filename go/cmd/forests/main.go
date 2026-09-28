// Command forests parses every sentence of a corpus with a context-free
// grammar, using package cfg, and writes a line per sentence: the size of
// its forest, how long it took, whether the corpus's own tree for it is in
// the forest, and how many trees there are.
//
//	forests -grammar tb.fcfg -sents sents.txt -gold trees.jsonl > forests.tsv
//
// The sentences file has a line per sentence: id, number of words, and the
// words, separated by tabs. The gold file, if given, has a JSON object per
// line: {"id": ..., "tree": [label, child, ...]}, a leaf being [tag, word],
// with labels as the grammar names its symbols. tools/masc/treebank.py writes
// all three for MASC.
//
// Sentences are parsed one at a time, shortest first: the longest need the
// most memory, and if the run fails there, everything shorter is done.
// With -done, sentences already in an earlier output are skipped, so a run
// can be resumed by appending to its output.
package main

import (
	"bufio"
	"encoding/json"
	"errors"
	"flag"
	"fmt"
	"os"
	"runtime"
	"runtime/debug"
	"slices"
	"strconv"
	"strings"
	"time"

	"github.com/cbrew/quadruplet/go/cfg"
	"github.com/cbrew/quadruplet/go/grammar"
	"github.com/cbrew/quadruplet/go/notation"
)

type sentence struct {
	id    string
	words []string
}

func main() {
	grammarFile := flag.String("grammar", "", "the grammar file")
	style := flag.String("notation", "integrated", "grammar notation: integrated or features")
	start := flag.String("start", "Top", "the start symbol")
	sentsFile := flag.String("sents", "", "the sentences: id, number of words, words, separated by tabs")
	goldFile := flag.String("gold", "", "optional: the corpus's trees, as JSON lines, to check each is in its forest")
	count := flag.Bool("count", true, "count each forest's trees exactly (memory in proportion to the forest)")
	maxWords := flag.Int("maxwords", 0, "parse only sentences of at most this many words (0: all)")
	doneFile := flag.String("done", "", "optional: an earlier output; skip the sentences in it, and print no header")
	flag.Parse()
	if *grammarFile == "" || *sentsFile == "" {
		flag.Usage()
		os.Exit(2)
	}
	fail := func(err error) {
		fmt.Fprintln(os.Stderr, "forests:", err)
		os.Exit(1)
	}

	began := time.Now()
	g, err := load(*grammarFile, *style)
	if err != nil {
		fail(err)
	}
	cg, err := cfg.FromGrammar(g, *start)
	if err != nil {
		fail(err)
	}
	g = nil
	symbols, rules, steps := cg.Size()
	fmt.Fprintf(os.Stderr, "%d symbols, %d rules, %d steps, loaded in %v\n",
		symbols, rules, steps, time.Since(began).Round(time.Millisecond))

	sents, err := readSentences(*sentsFile)
	if err != nil {
		fail(err)
	}
	var gold map[string][]byte
	if *goldFile != "" {
		if gold, err = readGold(*goldFile); err != nil {
			fail(err)
		}
	}
	done := map[string]bool{}
	if *doneFile != "" {
		if done, err = readDone(*doneFile); err != nil {
			fail(err)
		}
	}
	var todo []sentence
	for _, s := range sents {
		if !done[s.id] && (*maxWords == 0 || len(s.words) <= *maxWords) {
			todo = append(todo, s)
		}
	}
	slices.SortStableFunc(todo, func(a, b sentence) int { return len(a.words) - len(b.words) })
	fmt.Fprintf(os.Stderr, "%d sentences, %d already done, %d to parse\n", len(sents), len(done), len(todo))

	out := bufio.NewWriter(os.Stdout)
	defer out.Flush()
	if *doneFile == "" {
		fmt.Fprintln(out, "id\twords\titems\thyperedges\tderivable\trecognise_s\tbuild_s\theap_mb\tgold\ttrees")
	}
	var parsed, inGold, notInGold int
	var slowest time.Duration
	var slowestID string
	for k, s := range todo {
		f := cg.Parse(s.words)
		var m runtime.MemStats
		runtime.ReadMemStats(&m)
		items, _, edges := f.Stats()
		if len(f.Goals) > 0 {
			parsed++
		}
		inForest := "-"
		if raw, ok := gold[s.id]; ok {
			t, err := goldTree(raw)
			if err != nil {
				fail(fmt.Errorf("%s: %v", s.id, err))
			}
			if f.Contains(t) {
				inForest, inGold = "yes", inGold+1
			} else {
				inForest, notInGold = "no", notInGold+1
			}
		}
		trees := "-"
		if *count {
			trees = f.Count().String()
		}
		fmt.Fprintf(out, "%s\t%d\t%d\t%d\t%d\t%.6f\t%.6f\t%d\t%s\t%s\n", s.id, len(s.words), items, edges, f.Derivable,
			f.Recognise.Seconds(), f.Build.Seconds(), m.HeapInuse>>20, inForest, trees)
		if took := f.Recognise + f.Build; took > slowest {
			slowest, slowestID = took, s.id
		}
		n := len(s.words)
		if n >= 40 || (k+1)%1000 == 0 || k+1 == len(todo) {
			out.Flush()
			fmt.Fprintf(os.Stderr, "%d/%d: %d words, %d hyperedges, %.1f s; %v so far\n", k+1, len(todo), n, edges,
				(f.Recognise + f.Build).Seconds(), time.Since(began).Round(time.Second))
		}
		if edges > 10_000_000 {
			debug.FreeOSMemory() // give a big forest's memory back before the next
		}
	}
	out.Flush()
	fmt.Fprintf(os.Stderr, "parsed %d of %d; gold tree in its forest: %d, not: %d; slowest %s, %.1f s; %v in all%s\n",
		parsed, len(todo), inGold, notInGold, slowestID, slowest.Seconds(), time.Since(began).Round(time.Second), peak())
}

func load(file, style string) (*grammar.Grammar, error) {
	text, err := os.ReadFile(file)
	if err != nil {
		return nil, err
	}
	switch style {
	case "integrated":
		return notation.ParseIntegratedGrammar(string(text))
	case "features":
		return notation.ParseFeatureGrammar(string(text))
	}
	return nil, fmt.Errorf("unknown notation %q", style)
}

func lines(file string, each func(line string) error) error {
	f, err := os.Open(file)
	if err != nil {
		return err
	}
	defer f.Close()
	sc := bufio.NewScanner(f)
	sc.Buffer(make([]byte, 1<<20), 1<<26)
	for n := 1; sc.Scan(); n++ {
		if err := each(sc.Text()); err != nil {
			return fmt.Errorf("%s:%d: %v", file, n, err)
		}
	}
	return sc.Err()
}

func readSentences(file string) ([]sentence, error) {
	var out []sentence
	err := lines(file, func(line string) error {
		rec := strings.Split(line, "\t")
		if len(rec) != 3 {
			return errors.New("want id, number of words and words, separated by tabs")
		}
		s := sentence{rec[0], strings.Fields(rec[2])}
		if n, err := strconv.Atoi(rec[1]); err != nil || n != len(s.words) {
			return fmt.Errorf("%q words, but there are %d", rec[1], len(s.words))
		}
		out = append(out, s)
		return nil
	})
	return out, err
}

// readGold keeps each tree as its JSON, to be decoded when its sentence is
// parsed.
func readGold(file string) (map[string][]byte, error) {
	out := map[string][]byte{}
	err := lines(file, func(line string) error {
		var rec struct {
			ID   string          `json:"id"`
			Tree json.RawMessage `json:"tree"`
		}
		if err := json.Unmarshal([]byte(line), &rec); err != nil {
			return err
		}
		out[rec.ID] = rec.Tree
		return nil
	})
	return out, err
}

func goldTree(raw []byte) (*cfg.Tree, error) {
	var v any
	if err := json.Unmarshal(raw, &v); err != nil {
		return nil, err
	}
	var tree func(v any) (*cfg.Tree, error)
	tree = func(v any) (*cfg.Tree, error) {
		node, ok := v.([]any)
		if !ok || len(node) < 2 {
			return nil, fmt.Errorf("not a tree: %v", v)
		}
		label, ok := node[0].(string)
		if !ok {
			return nil, fmt.Errorf("no label: %v", v)
		}
		if w, ok := node[1].(string); ok && len(node) == 2 {
			return &cfg.Tree{Label: label, Words: []string{w}}, nil
		}
		t := &cfg.Tree{Label: label}
		for _, c := range node[1:] {
			ct, err := tree(c)
			if err != nil {
				return nil, err
			}
			t.Children = append(t.Children, ct)
		}
		return t, nil
	}
	return tree(v)
}

func readDone(file string) (map[string]bool, error) {
	out := map[string]bool{}
	err := lines(file, func(line string) error {
		// a line cut short by an interrupted run is not done
		if rec := strings.Split(line, "\t"); len(rec) == 10 && rec[0] != "id" {
			out[rec[0]] = true
		}
		return nil
	})
	return out, err
}

// peak is the process's peak resident memory, where the system reports it.
func peak() string {
	b, err := os.ReadFile("/proc/self/status")
	if err != nil {
		return ""
	}
	for _, l := range strings.Split(string(b), "\n") {
		if f := strings.Fields(l); len(f) == 3 && f[0] == "VmHWM:" {
			if kb, err := strconv.Atoi(f[1]); err == nil {
				return fmt.Sprintf("; peak memory %.1f GB", float64(kb)/(1<<20))
			}
		}
	}
	return ""
}
