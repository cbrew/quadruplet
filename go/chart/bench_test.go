package chart

import (
	"fmt"
	"slices"
	"testing"
)

// sem2Sentence is "John sees a dog" followed by k ambiguous PPs, which has
// 2^k readings.
func sem2Sentence(k int) []string {
	words := []string{"John", "sees", "a", "dog"}
	nouns := []string{"boy", "girl", "dog"}
	for i := 0; i < k; i++ {
		words = append(words, "with", "a", nouns[i%3])
	}
	return words
}

func BenchmarkSem2(b *testing.B) {
	g := NewFeatureGrammar(loadGrammar(b, "sem2.fcfg"))
	for _, k := range []int{8, 10, 12, 14} {
		words := sem2Sentence(k)
		b.Run(fmt.Sprintf("k=%d", k), func(b *testing.B) {
			for i := 0; i < b.N; i++ {
				New(words).Parse(g)
			}
		})
	}
}

func BenchmarkTreeGrammar(b *testing.B) {
	for _, n := range []int{60, 120, 170} {
		words := slices.Repeat([]string{"a"}, n)
		b.Run(fmt.Sprintf("n=%d", n), func(b *testing.B) {
			for i := 0; i < b.N; i++ {
				New(words).Parse(TreeGrammar{})
			}
		})
	}
}

func TestSem2Readings(t *testing.T) {
	g := NewFeatureGrammar(loadGrammar(t, "sem2.fcfg"))
	for k := 0; k <= 10; k++ {
		c := New(sem2Sentence(k))
		c.Parse(g)
		if got := len(c.Solutions()); got != 1<<k {
			t.Errorf("k=%d: %d readings, want %d", k, got, 1<<k)
		}
	}
}

func BenchmarkSem2Parallel(b *testing.B) {
	g := NewFeatureGrammar(loadGrammar(b, "sem2.fcfg"))
	for _, k := range []int{12, 14} {
		words := sem2Sentence(k)
		for _, workers := range []int{1, 2, 4} {
			b.Run(fmt.Sprintf("k=%d/workers=%d", k, workers), func(b *testing.B) {
				for i := 0; i < b.N; i++ {
					New(words).ParseParallel(g, workers)
				}
			})
		}
	}
}

func BenchmarkTreeGrammarParallel(b *testing.B) {
	for _, n := range []int{120, 170} {
		words := slices.Repeat([]string{"a"}, n)
		for _, workers := range []int{1, 2, 4} {
			b.Run(fmt.Sprintf("n=%d/workers=%d", n, workers), func(b *testing.B) {
				for i := 0; i < b.N; i++ {
					New(words).ParseParallel(TreeGrammar{}, workers)
				}
			})
		}
	}
}

// The parallel parse must not depend on scheduling: any number of workers
// gives the same edges, in the same order, with the same predecessors.
func TestParallelIsDeterministic(t *testing.T) {
	g := NewFeatureGrammar(loadGrammar(t, "sem2.fcfg"))
	render := func(c *Chart) []string {
		var out []string
		for _, e := range append(c.Completes(), c.Partials()...) {
			out = append(out, e.String())
			for _, p := range c.Predecessors(e) {
				out = append(out, "  "+p.Partial.String()+" + "+p.Complete.String())
			}
		}
		return out
	}
	words := sem2Sentence(6)
	base := New(words)
	base.ParseParallel(g, 1)
	want := render(base)
	for _, workers := range []int{2, 3, 8} {
		for run := 0; run < 3; run++ {
			c := New(words)
			c.ParseParallel(g, workers)
			if got := render(c); !slices.Equal(got, want) {
				t.Fatalf("workers=%d run %d differs from the sequential wavefront", workers, run)
			}
		}
	}
	if len(base.Solutions()) != 1<<6 {
		t.Errorf("%d readings", len(base.Solutions()))
	}
}

// The parallel parse finds the same edges, readings and trees as the agenda
// parse, beyond the golden sentences too.
func TestParallelMatchesAgenda(t *testing.T) {
	g := NewFeatureGrammar(loadGrammar(t, "sem2.fcfg"))
	check := func(name string, words []string, g Grammar) {
		a, p := New(words), New(words)
		a.Parse(g)
		p.ParseParallel(g, 4)
		as, ps := a.Stats(), p.Stats()
		if as.Completes != ps.Completes || as.Partials != ps.Partials || as.Solutions != ps.Solutions ||
			as.Trees.Cmp(ps.Trees) != 0 {
			t.Errorf("%s: agenda %+v, parallel %+v", name, as, ps)
		}
		sols := func(c *Chart) []string {
			var out []string
			for _, e := range c.Solutions() {
				out = append(out, e.Cat.String())
			}
			slices.Sort(out)
			return out
		}
		if !slices.Equal(sols(a), sols(p)) {
			t.Errorf("%s: readings differ", name)
		}
	}
	for k := 0; k <= 8; k++ {
		check(fmt.Sprintf("sem2 k=%d", k), sem2Sentence(k), g)
	}
	for n := 1; n <= 40; n += 3 {
		check(fmt.Sprintf("TreeGrammar n=%d", n), slices.Repeat([]string{"a"}, n), TreeGrammar{})
	}
}
