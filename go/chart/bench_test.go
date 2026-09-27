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
