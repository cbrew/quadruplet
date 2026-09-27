// Command quadruplet parses sentences with a feature grammar and prints the
// readings.
//
//	quadruplet -grammar sem2.fcfg "John sees a dog with Fido"
//	quadruplet -grammar demo.fcfg -notation features -trees 3 < sentences.txt
//
// Sentences are given as arguments, or read one per line from standard
// input. Words are separated by spaces.
package main

import (
	"bufio"
	"flag"
	"fmt"
	"io"
	"os"
	"strings"
	"time"

	"github.com/cbrew/quadruplet/go/chart"
	"github.com/cbrew/quadruplet/go/grammar"
	"github.com/cbrew/quadruplet/go/notation"
	"github.com/cbrew/quadruplet/go/term"
)

func main() {
	grammarFile := flag.String("grammar", "", "grammar file (required)")
	style := flag.String("notation", "integrated", "grammar notation: integrated (patio.fcfg, sem2.fcfg) or features (demo.fcfg)")
	trees := flag.Int("trees", 0, "print up to this many trees per reading")
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
	fg := chart.NewFeatureGrammar(g)
	parse := func(sentence string) {
		words := strings.Fields(sentence)
		if len(words) == 0 {
			return
		}
		start := time.Now()
		c := chart.New(words)
		c.Parse(fg)
		elapsed := time.Since(start)
		s := c.Stats()
		fmt.Printf("%s\n  %d readings, %s trees, %d complete and %d partial edges, %v\n",
			strings.Join(words, " "), s.Solutions, s.Trees, s.Completes, s.Partials, elapsed.Round(time.Microsecond))
		for _, e := range c.Solutions() {
			fmt.Printf("  %s\n", e.Cat)
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
	if flag.NArg() > 0 {
		for _, s := range flag.Args() {
			parse(s)
		}
		return
	}
	in := bufio.NewScanner(os.Stdin)
	for in.Scan() {
		parse(in.Text())
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
