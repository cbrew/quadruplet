package main

import (
	"flag"
	"fmt"
	"math/big"
	"os"
	"strings"
	"time"
)

func short(b *big.Int) string {
	s := b.String()
	if len(s) > 60 {
		return fmt.Sprintf("%s…(%d digits)", s[:8], len(s))
	}
	return s
}

func main() {
	n := flag.Int("n", 30, "number of 'a's")
	flag.IntVar(&work, "work", 0, "synthetic unification cost (LCG steps)")
	mode := flag.String("mode", "all", "agenda|agenda-par|wave|wave-par|all")
	flag.Parse()
	g := treeGrammar()
	words := strings.Split(strings.TrimSpace(strings.Repeat("a ", *n)), " ")

	// best of 3 parse times; stats (incl. memoised tree count) timed apart
	run := func(name string, f func() func() Stats) {
		best := time.Duration(1 << 62)
		var stats func() Stats
		for rep := 0; rep < 3; rep++ {
			t0 := time.Now()
			stats = f()
			best = min(best, time.Since(t0))
		}
		t1 := time.Now()
		s := stats()
		fmt.Printf("%-11s n=%-4d work=%-5d parse %9.2f ms  count %7.2f ms  completes=%d partials=%d trees=%s\n",
			name, *n, work, float64(best.Microseconds())/1000,
			float64(time.Since(t1).Microseconds())/1000,
			s.completes, s.partials, short(s.trees))
	}
	modes := map[string]func() func() Stats{
		"agenda":     func() func() Stats { c := ParseAgenda(g, words, false); return func() Stats { return c.Stats(*n) } },
		"agenda-par": func() func() Stats { c := ParseAgenda(g, words, true); return func() Stats { return c.Stats(*n) } },
		"wave":       func() func() Stats { c := ParseWave(g, words, false); return c.Stats },
		"wave-par":   func() func() Stats { c := ParseWave(g, words, true); return c.Stats },
	}
	if *mode == "all" {
		for _, m := range []string{"agenda", "agenda-par", "wave", "wave-par"} {
			run(m, modes[m])
		}
		return
	}
	f, ok := modes[*mode]
	if !ok {
		fmt.Fprintln(os.Stderr, "unknown mode")
		os.Exit(2)
	}
	run(*mode, f)
}
