package notation

import (
	"bufio"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/cbrew/quadruplet/go/grammar"
	"github.com/cbrew/quadruplet/go/term"
)

// The golden files are written by the Kotlin implementation
// (src/test/kotlin/com/cbrew/golden/GoldenDumpTest.kt); each line is an
// input and the Kotlin output, tab-separated.

func goldenLines(t *testing.T, name string) [][]string {
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
	if err := sc.Err(); err != nil {
		t.Fatal(err)
	}
	if len(out) < 10 {
		t.Fatalf("%s: only %d records", name, len(out))
	}
	return out
}

func TestLogicGolden(t *testing.T) {
	for _, rec := range goldenLines(t, "logic.golden") {
		input := rec[0]
		l, err := ParseLogic(input)
		if rec[1] == "ERROR" {
			if err == nil {
				t.Errorf("%q: want a syntax error, got %v", input, l)
			}
			continue
		}
		if err != nil {
			t.Errorf("%q: %v", input, err)
			continue
		}
		if got := l.String(); got != rec[1] {
			t.Errorf("%q:\n got  %s\n want %s", input, got, rec[1])
		}
		if got := term.Normalized(l).String(); got != rec[2] {
			t.Errorf("%q normalized:\n got  %s\n want %s", input, got, rec[2])
		}
	}
}

func TestFSGolden(t *testing.T) {
	for _, rec := range goldenLines(t, "fs.golden") {
		m, err := ParseFS(rec[0])
		if rec[1] == "ERROR" {
			if err == nil {
				t.Errorf("%q: want a syntax error, got %v", rec[0], m)
			}
			continue
		}
		if err != nil {
			t.Errorf("%q: %v", rec[0], err)
		} else if got := m.String(); got != rec[1] {
			t.Errorf("%q:\n got  %s\n want %s", rec[0], got, rec[1])
		}
	}
}

// grammarFiles are the Kotlin test grammars and their notations.
var grammarFiles = []struct{ file, notation string }{
	{"demo.fcfg", "features"},
	{"patio.fcfg", "integrated"},
	{"sem2.fcfg", "integrated"},
	{"tiny.cfg", "integrated"},
	{"tiny2.cfg", "integrated"},
	{"alternatives.fcfg", "integrated"},
}

func TestGrammarGolden(t *testing.T) {
	var got []string
	for _, gf := range grammarFiles {
		g := loadTestGrammar(t, gf.file, gf.notation)
		for _, r := range g.Rules {
			got = append(got, strings.Join([]string{gf.file, "rule", r.String(), r.Normalized().String()}, "\t"))
		}
		for _, w := range g.Lexicon.Words() {
			for _, c := range g.Lexicon.Lookup(w) {
				got = append(got, strings.Join([]string{gf.file, "lex", w, c.String(), term.Normalized(c).String()}, "\t"))
			}
		}
	}
	want := goldenLines(t, "grammar.golden")
	for i := 0; i < max(len(got), len(want)); i++ {
		var g, w string
		if i < len(got) {
			g = got[i]
		}
		if i < len(want) {
			w = strings.Join(want[i], "\t")
		}
		if g != w {
			t.Errorf("line %d:\n got  %s\n want %s", i+1, g, w)
		}
	}
}

func loadTestGrammar(t *testing.T, file, notation string) *grammar.Grammar {
	t.Helper()
	text, err := os.ReadFile(filepath.Join("..", "..", "src", "test", "resources", file))
	if err != nil {
		t.Fatal(err)
	}
	var g *grammar.Grammar
	if notation == "features" {
		g, err = ParseFeatureGrammar(string(text))
	} else {
		g, err = ParseIntegratedGrammar(string(text))
	}
	if err != nil {
		t.Fatalf("%s: %v", file, err)
	}
	return g
}
