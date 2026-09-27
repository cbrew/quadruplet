package main

import (
	"strings"
	"testing"
)

func as(n int) []string { return strings.Fields(strings.Repeat("a ", n)) }

// Expected values are from the Kotlin Chart (numCompletes, numPartials,
// countTrees) on TreeAsFeatureGrammar.
var kotlin = []struct {
	n                   int
	completes, partials int
	trees               string
}{
	{16, 136, 1002, "717061938"},
	{30, 465, 3144, "4954217073368227192"},
	{40, 820, 5394, "67640307007394294146092847"},
}

func TestStrategiesMatchKotlin(t *testing.T) {
	g := treeGrammar()
	for _, k := range kotlin {
		words := as(k.n)
		got := map[string]Stats{
			"agenda":     ParseAgenda(g, words, false).Stats(k.n),
			"agenda-par": ParseAgenda(g, words, true).Stats(k.n),
			"wave":       ParseWave(g, words, false).Stats(),
			"wave-par":   ParseWave(g, words, true).Stats(),
		}
		for name, s := range got {
			if s.completes != k.completes || s.partials != k.partials || s.trees.String() != k.trees {
				t.Errorf("%s n=%d: got completes=%d partials=%d trees=%s, want %d %d %s",
					name, k.n, s.completes, s.partials, s.trees, k.completes, k.partials, k.trees)
			}
		}
	}
}

func benchmark(b *testing.B, n int, parse func(*Grammar, []string)) {
	g, words := treeGrammar(), as(n)
	for i := 0; i < b.N; i++ {
		parse(g, words)
	}
}

func BenchmarkAgenda60(b *testing.B) {
	benchmark(b, 60, func(g *Grammar, w []string) { ParseAgenda(g, w, false) })
}
func BenchmarkAgendaPar60(b *testing.B) {
	benchmark(b, 60, func(g *Grammar, w []string) { ParseAgenda(g, w, true) })
}
func BenchmarkWave60(b *testing.B) {
	benchmark(b, 60, func(g *Grammar, w []string) { ParseWave(g, w, false) })
}
func BenchmarkWavePar60(b *testing.B) {
	benchmark(b, 60, func(g *Grammar, w []string) { ParseWave(g, w, true) })
}
