package term_test

import (
	"bufio"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/cbrew/quadruplet/go/notation"
	"github.com/cbrew/quadruplet/go/term"
)

// unify.golden holds pairs of feature terms and the Kotlin result of
// unifying them (substituted and canonicalized), or FAIL.
func TestUnifyGolden(t *testing.T) {
	f, err := os.Open(filepath.Join("..", "testdata", "golden", "unify.golden"))
	if err != nil {
		t.Fatal(err)
	}
	defer f.Close()
	n := 0
	for sc := bufio.NewScanner(f); sc.Scan(); n++ {
		rec := strings.Split(sc.Text(), "\t")
		a, err := notation.ParseFS(rec[0])
		if err != nil {
			t.Fatal(err)
		}
		b, err := notation.ParseFS(rec[1])
		if err != nil {
			t.Fatal(err)
		}
		got := "FAIL"
		if r, ok := term.Unify(a, b); ok {
			got = r.String()
		}
		if got != rec[2] {
			t.Errorf("%s ⊔ %s:\n got  %s\n want %s", rec[0], rec[1], got, rec[2])
		}
	}
	if n < 30 {
		t.Fatalf("only %d records", n)
	}
}
