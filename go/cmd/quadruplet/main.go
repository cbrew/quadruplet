// Command quadruplet parses sentences with a feature grammar and prints the
// readings.
//
//	quadruplet -grammar sem2.fcfg "John sees a dog with Fido"
//	quadruplet -grammar demo.fcfg -notation features -trees 3 < sentences.txt
//
// Sentences are given as arguments, or read one per line from standard
// input. Words are separated by spaces. Parsing uses all CPUs (see
// -workers); setting the GOGC environment variable above its default of 100,
// for example GOGC=400, trades memory for less garbage collection and helps
// long sentences.
package main

import (
	"bufio"
	"flag"
	"fmt"
	"io"
	"math/big"
	"os"
	"slices"
	"strings"
	"time"

	"github.com/cbrew/quadruplet/go/cfg"
	"github.com/cbrew/quadruplet/go/chart"
	"github.com/cbrew/quadruplet/go/grammar"
	"github.com/cbrew/quadruplet/go/notation"
	"github.com/cbrew/quadruplet/go/term"
)

func main() {
	grammarFile := flag.String("grammar", "", "grammar file (required)")
	style := flag.String("notation", "integrated", "grammar notation: integrated (patio.fcfg, sem2.fcfg) or features (demo.fcfg)")
	trees := flag.Int("trees", 0, "print up to this many trees per reading")
	workers := flag.Int("workers", 0, "goroutines for parsing: 0 for one per CPU, 1 for the sequential agenda parser")
	startCat := flag.String("start", "", "count only readings of this category, such as Top")
	quiet := flag.Bool("quiet", false, "print only the summary line for each sentence")
	pretty := flag.Bool("pretty", false, "print readings' semantics with named variables and sorted conjuncts")
	count := flag.Bool("count", true, "with -fast, count the trees, exactly (memory in proportion to the forest)")
	fast := flag.Bool("fast", false, "parse with the fast context-free parser (package cfg); needs -start, and a grammar whose categories are plain")
	flag.Usage = func() {
		fmt.Fprintf(flag.CommandLine.Output(), "usage: quadruplet -grammar FILE [flags] [sentence ...]\n")
		flag.PrintDefaults()
	}
	flag.Parse()
	if *grammarFile == "" {
		flag.Usage()
		os.Exit(2)
	}
	g, err := load(*grammarFile, *style)
	if err != nil {
		fmt.Fprintf(os.Stderr, "quadruplet: %s: %v\n", *grammarFile, err)
		os.Exit(1)
	}
	if *fast {
		if *startCat == "" {
			fmt.Fprintln(os.Stderr, "quadruplet: -fast needs -start")
			os.Exit(2)
		}
		start := time.Now()
		cg, err := cfg.FromGrammar(g, *startCat)
		if err != nil {
			fmt.Fprintf(os.Stderr, "quadruplet: %s: %v\n", *grammarFile, err)
			os.Exit(1)
		}
		symbols, rules, steps := cg.Size()
		fmt.Fprintf(os.Stderr, "%d symbols, %d rules, %d binary steps, compiled in %v\n",
			symbols, rules, steps, time.Since(start).Round(time.Millisecond))
		each(func(words []string) {
			start := time.Now()
			f := cg.Parse(words)
			elapsed := time.Since(start)
			items, own, edges := f.Stats()
			number := "uncounted"
			if *count {
				number = f.Count().String()
			}
			fmt.Printf("%s\n  %s trees, %d items (%d of the grammar's own symbols), %d hyperedges, %d derivable, %v (recognise %v, build %v)\n",
				strings.Join(words, " "), number, items, own, edges, f.Derivable, elapsed.Round(time.Microsecond),
				f.Recognise.Round(time.Microsecond), f.Build.Round(time.Microsecond))
			n := 0
			for t := range f.Trees() {
				if n == *trees {
					break
				}
				fmt.Printf("    %s\n", t)
				n++
			}
		})
		return
	}
	fg := chart.NewFeatureGrammar(g)
	parse := func(sentence string) {
		words := strings.Fields(sentence)
		if len(words) == 0 {
			return
		}
		start := time.Now()
		c := chart.New(words)
		if *workers == 1 {
			c.Parse(fg)
		} else {
			c.ParseParallel(fg, *workers)
		}
		elapsed := time.Since(start)
		s := c.Stats()
		solutions := c.Solutions()
		if *startCat != "" {
			solutions = solutions[:0:0]
			s.Trees = new(big.Int)
			for _, e := range c.Solutions() {
				if term.Key(e.Cat) == *startCat {
					solutions = append(solutions, e)
					s.Trees.Add(s.Trees, c.CountTreesUnder(e))
				}
			}
			s.Solutions = len(solutions)
		}
		fmt.Printf("%s\n  %d readings, %s trees, %d complete and %d partial edges, %v\n",
			strings.Join(words, " "), s.Solutions, s.Trees, s.Completes, s.Partials, elapsed.Round(time.Microsecond))
		if *quiet {
			return
		}
		var lines []string
		for _, e := range solutions {
			if sem, ok := e.Cat.(*term.Map).Get("sem"); ok && *pretty {
				if s, ok := sem.(*term.Sem); ok {
					lines = append(lines, term.Key(e.Cat)+": "+term.Pretty(s.Value()))
					continue
				}
			}
			lines = append(lines, e.Cat.String())
		}
		if *pretty {
			slices.Sort(lines)
		}
		for i, e := range solutions {
			fmt.Printf("  %s\n", lines[i])
			n := 0
			for t := range c.Trees(e) {
				if n == *trees {
					break
				}
				printTree(t, "    ")
				n++
			}
		}
	}
	each(func(words []string) { parse(strings.Join(words, " ")) })
}

// each calls f with the words of each sentence: the arguments, or else the
// lines of standard input.
func each(f func(words []string)) {
	if flag.NArg() > 0 {
		for _, s := range flag.Args() {
			if words := strings.Fields(s); len(words) > 0 {
				f(words)
			}
		}
		return
	}
	in := bufio.NewScanner(os.Stdin)
	in.Buffer(make([]byte, 1<<20), 1<<20)
	for in.Scan() {
		if words := strings.Fields(in.Text()); len(words) > 0 {
			f(words)
		}
	}
	if err := in.Err(); err != nil && err != io.EOF {
		fmt.Fprintf(os.Stderr, "quadruplet: %v\n", err)
		os.Exit(1)
	}
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

// printTree prints one node per line, labelled by category.
func printTree(t chart.Tree, prefix string) {
	switch n := t.(type) {
	case *chart.Leaf:
		fmt.Printf("%s%s %s\n", prefix, term.Key(n.Cat), strings.Join(n.Words, " "))
	case *chart.Node:
		fmt.Printf("%s%s\n", prefix, term.Key(n.Cat))
		for _, c := range n.Children {
			printTree(c, prefix+"  ")
		}
	}
}
