package chart

import (
	"bufio"
	"os"
	"path/filepath"
	"slices"
	"strconv"
	"strings"
	"testing"

	"github.com/cbrew/quadruplet/go/grammar"
	"github.com/cbrew/quadruplet/go/notation"
)

// parse.golden and treeas.golden are written by the Kotlin implementation
// (src/test/kotlin/com/cbrew/golden/GoldenDumpTest.kt).

func readGolden(t *testing.T, name string) [][]string {
	t.Helper()
	f, err := os.Open(filepath.Join("..", "testdata", "golden", name))
	if err != nil {
		t.Fatal(err)
	}
	defer f.Close()
	var out [][]string
	sc := bufio.NewScanner(f)
	sc.Buffer(make([]byte, 1<<20), 1<<20)
	for sc.Scan() {
		out = append(out, strings.Split(sc.Text(), "\t"))
	}
	if len(out) < 10 {
		t.Fatalf("%s: only %d records", name, len(out))
	}
	return out
}

func loadGrammar(t testing.TB, file string) *grammar.Grammar {
	t.Helper()
	text, err := os.ReadFile(filepath.Join("..", "..", "src", "test", "resources", file))
	if err != nil {
		t.Fatal(err)
	}
	var g *grammar.Grammar
	if file == "demo.fcfg" {
		g, err = notation.ParseFeatureGrammar(string(text))
	} else {
		g, err = notation.ParseIntegratedGrammar(string(text))
	}
	if err != nil {
		t.Fatalf("%s: %v", file, err)
	}
	return g
}

// parsers are the ways a chart can be filled; each must match the goldens.
var parsers = map[string]func(c *Chart, g Grammar){
	"agenda":     func(c *Chart, g Grammar) { c.Parse(g) },
	"wave":       func(c *Chart, g Grammar) { c.ParseParallel(g, 1) },
	"wave-par-4": func(c *Chart, g Grammar) { c.ParseParallel(g, 4) },
}

// parseRecord renders a parse the way parse.golden records it.
func parseRecord(g Grammar, parse func(*Chart, Grammar), file, sentence string) []string {
	c := New(strings.Split(sentence, " "))
	parse(c, g)
	s := c.Stats()
	out := []string{strings.Join([]string{file, sentence, "stats", strconv.Itoa(s.Completes),
		strconv.Itoa(s.Partials), strconv.Itoa(s.Solutions), s.Trees.String()}, "\t")}
	var sols, trees []string
	for _, e := range c.Solutions() {
		sols = append(sols, e.Cat.String())
		n := 0
		for tr := range c.Trees(e) {
			if n == 20 {
				break
			}
			trees = append(trees, strings.ReplaceAll(tr.Format(0), "\n", "|"))
			n++
		}
	}
	slices.Sort(sols)
	slices.Sort(trees)
	for _, x := range sols {
		out = append(out, file+"\t"+sentence+"\tsolution\t"+x)
	}
	for _, x := range trees {
		out = append(out, file+"\t"+sentence+"\ttree\t"+x)
	}
	return out
}

func TestParseGolden(t *testing.T) {
	want := map[string][]string{}
	var order []string
	for _, rec := range readGolden(t, "parse.golden") {
		key := rec[0] + "\t" + rec[1]
		if _, seen := want[key]; !seen {
			order = append(order, key)
		}
		want[key] = append(want[key], strings.Join(rec, "\t"))
	}
	grammars := map[string]Grammar{}
	for _, key := range order {
		file, sentence, _ := strings.Cut(key, "\t")
		if grammars[file] == nil {
			grammars[file] = NewFeatureGrammar(loadGrammar(t, file))
		}
		for name, parse := range parsers {
			got := parseRecord(grammars[file], parse, file, sentence)
			if !slices.Equal(got, want[key]) {
				t.Errorf("%s %s:\n got  %s\n want %s", name, key,
					strings.Join(got, "\n      "), strings.Join(want[key], "\n      "))
			}
		}
	}
}

func TestTreeGrammarGolden(t *testing.T) {
	for _, rec := range readGolden(t, "treeas.golden") {
		n, _ := strconv.Atoi(rec[0])
		for name, parse := range parsers {
			c := New(slices.Repeat([]string{"a"}, n))
			parse(c, TreeGrammar{})
			s := c.Stats()
			got := []string{rec[0], strconv.Itoa(s.Completes), strconv.Itoa(s.Partials), s.Trees.String()}
			if !slices.Equal(got, rec) {
				t.Errorf("%s n=%d: got %v, want %v", name, n, got, rec)
			}
		}
	}
}
