package chart

import (
	"bufio"
	"math/big"
	"os"
	"path/filepath"
	"slices"
	"strconv"
	"strings"
	"testing"
	"time"

	"github.com/cbrew/quadruplet/go/term"
)

// The MASC benchmark: sentences from the MASC Penn Treebank and a grammar
// with Montague-style semantics written for them, in
// src/test/resources/masc (see the README there).

type mascSentence struct {
	id, genre string
	words     []string
}

func mascFile(t testing.TB, name string) []string {
	t.Helper()
	f, err := os.Open(filepath.Join("..", "..", "src", "test", "resources", "masc", name))
	if err != nil {
		t.Fatal(err)
	}
	defer f.Close()
	var lines []string
	sc := bufio.NewScanner(f)
	for sc.Scan() {
		lines = append(lines, sc.Text())
	}
	return lines
}

func mascSample(t testing.TB) []mascSentence {
	var out []mascSentence
	for _, line := range mascFile(t, "sample.txt") {
		f := strings.Split(line, "\t")
		out = append(out, mascSentence{f[0], f[1], strings.Split(f[2], " ")})
	}
	return out
}

func mascGrammar(t testing.TB) Grammar {
	return NewFeatureGrammar(loadGrammar(t, filepath.Join("masc", "masc.fcfg")))
}

// tops returns the solutions whose category is Top, and the trees under them.
func tops(c *Chart) ([]*Edge, *big.Int) {
	var out []*Edge
	trees := new(big.Int)
	memo := map[*Edge]*big.Int{}
	for _, e := range c.Solutions() {
		if term.Key(e.Cat) == "Top" {
			out = append(out, e)
			trees.Add(trees, c.countTrees(e, memo))
		}
	}
	return out, trees
}

// mascRecord renders a parse the way masc.golden records it.
func mascRecord(c *Chart, id string) []string {
	s := c.Stats()
	top, trees := tops(c)
	out := []string{strings.Join([]string{id, "stats", strconv.Itoa(s.Completes), strconv.Itoa(s.Partials),
		strconv.Itoa(len(top)), trees.String()}, "\t")}
	var readings []string
	for _, e := range top {
		readings = append(readings, id+"\treading\t"+e.Cat.String())
	}
	slices.Sort(readings)
	return append(out, readings...)
}

// TestMascGolden checks the agenda parser's charts and readings against the
// Kotlin implementation's, and the wave parsers' against the agenda
// parser's. Readings are compared as terms there, since a conjunction is a
// set and the chart keeps the first of equal edges to arrive, which prints
// its conjuncts in the order they were built.
func TestMascGolden(t *testing.T) {
	want := map[string][]string{}
	for _, rec := range readGolden(t, "masc.golden") {
		want[rec[0]] = append(want[rec[0]], strings.Join(rec, "\t"))
	}
	g := mascGrammar(t)
	sample := mascSample(t)
	if len(sample) != len(want) {
		t.Fatalf("%d sentences, %d in masc.golden", len(sample), len(want))
	}
	for _, s := range sample {
		c := New(s.words)
		c.Parse(g)
		if got := mascRecord(c, s.id); !slices.Equal(got, want[s.id]) {
			t.Errorf("agenda %s:\n got  %s\n want %s", s.id,
				strings.Join(got, "\n      "), strings.Join(want[s.id], "\n      "))
		}
		readings, _ := tops(c)
		for _, workers := range []int{1, 4} {
			w := New(s.words)
			w.ParseParallel(g, workers)
			if got := mascRecord(w, s.id); got[0] != want[s.id][0] {
				t.Errorf("wave %d %s: got %s, want %s", workers, s.id, got[0], want[s.id][0])
			}
			wr, _ := tops(w)
			for _, e := range wr {
				if !slices.ContainsFunc(readings, func(r *Edge) bool { return term.Equal(r.Cat, e.Cat) }) {
					t.Errorf("wave %d %s: extra reading %s", workers, s.id, e.Cat)
				}
			}
		}
	}
}

// goldSpans returns the spans of the phrases of more than one word in a
// normalised treebank tree, written (S (NP (PRP I)) ...).
func goldSpans(tree string) (spans [][2]int, words int) {
	toks := strings.Fields(strings.NewReplacer("(", " ( ", ")", " ) ").Replace(tree))
	i := 0
	var node func()
	node = func() {
		i += 2 // "(" and the label
		start := words
		for toks[i] != ")" {
			if toks[i] == "(" {
				node()
			} else {
				words++
				i++
			}
		}
		i++
		if words-start > 1 {
			spans = append(spans, [2]int{start, words})
		}
	}
	node()
	return spans, words
}

// TestMascReport reports coverage, how many of the treebank's phrases the
// parses contain, and timing. Run with -v to see it.
func TestMascReport(t *testing.T) {
	g := mascGrammar(t)
	gold := map[string]string{}
	for _, line := range mascFile(t, "gold.txt") {
		id, tree, _ := strings.Cut(line, "\t")
		gold[id] = tree
	}
	var parsed, found, total int
	var edges int
	trees := new(big.Int)
	start := time.Now()
	for _, s := range mascSample(t) {
		c := New(s.words)
		c.Parse(g)
		top, n := tops(c)
		edges += len(c.Completes()) + len(c.Partials())
		trees.Add(trees, n)
		if len(top) == 0 {
			continue
		}
		parsed++
		spans := map[[2]int]bool{}
		seen := map[*Edge]bool{}
		var walk func(e *Edge)
		walk = func(e *Edge) {
			if seen[e] {
				return
			}
			seen[e] = true
			if e.Complete() {
				spans[[2]int{e.Start, e.End}] = true
			}
			for _, p := range c.Predecessors(e) {
				walk(p.Partial)
				walk(p.Complete)
			}
		}
		for _, e := range top {
			walk(e)
		}
		gs, _ := goldSpans(gold[s.id])
		for _, sp := range gs {
			total++
			if spans[sp] {
				found++
			}
		}
	}
	n := len(mascSample(t))
	t.Logf("parsed %d of %d sentences (%.1f%%)", parsed, n, 100*float64(parsed)/float64(n))
	t.Logf("of those sentences' treebank phrases, %d of %d (%.1f%%) are spanned by an edge in some parse",
		found, total, 100*float64(found)/float64(total))
	t.Logf("%d edges, %s trees, %v with the agenda parser", edges, trees, time.Since(start).Round(time.Millisecond))
}

func benchmarkMasc(b *testing.B, parse func(*Chart, Grammar)) {
	g := mascGrammar(b)
	sample := mascSample(b)
	b.ResetTimer()
	for range b.N {
		for _, s := range sample {
			parse(New(s.words), g)
		}
	}
}

// BenchmarkMasc parses the whole MASC sample.
func BenchmarkMasc(b *testing.B) { benchmarkMasc(b, func(c *Chart, g Grammar) { c.Parse(g) }) }

func BenchmarkMascParallel(b *testing.B) {
	benchmarkMasc(b, func(c *Chart, g Grammar) { c.ParseParallel(g, 0) })
}
