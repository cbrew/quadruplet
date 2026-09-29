// Command countbank relabels the treebank by counts of basic types (package
// counts), reads a grammar off it, and measures that grammar's ambiguity on
// the held-out sentences cmd/verbentropy and cmd/framelex use, beside the
// treebank grammar's.
//
// A word's tag becomes its tag and its lexical type, so the lexicon now
// chooses types. Three lexicons: each word its own type (an oracle, as gold
// supertags would be); the types the word was seen with, with its tag, in
// the training documents (else the tag's); and every type seen with the
// tag. Each is measured by the number of trees (log10 per word), whether
// the sentence's own tree is among them, and the entropy of the trees
// weighted by P(rule | parent) and, for a word, P(word | tag and type).
//
//	countbank -annotated annotated.jsonl [-n 300] [-min 5] [-max 25] [-o counts.tsv]
package main

import (
	"bufio"
	"encoding/json"
	"flag"
	"fmt"
	"math"
	"os"
	"slices"
	"sort"
	"strconv"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
	"github.com/cbrew/quadruplet/go/explore/counts"
	"github.com/cbrew/quadruplet/go/explore/entropy"
	fr "github.com/cbrew/quadruplet/go/explore/frames"
	"github.com/cbrew/quadruplet/go/explore/quotient"
	"github.com/cbrew/quadruplet/go/interp"
)

type sentence struct {
	id    string
	words []string
	tags  []string // tag_type
	tree  *cfg.Tree
}

func main() {
	annotatedFile := flag.String("annotated", "", "the corpus's trees, as tools/masc/treebank.py writes them")
	n := flag.Int("n", 300, "how many test sentences to sample")
	minWords := flag.Int("min", 5, "the fewest words a sampled sentence has")
	maxWords := flag.Int("max", 25, "the most words a sampled sentence has")
	seed := flag.Uint64("seed", 1, "the sample's random seed")
	out := flag.String("o", "", "write the counts grammar here, as counts.tsv")
	limit := flag.Int64("enumerate", 0, "also enumerate the oracle forests of at most this many trees and measure the order of attachment")
	flag.Parse()
	if *annotatedFile == "" {
		flag.Usage()
		os.Exit(2)
	}
	fail := func(err error) {
		fmt.Fprintln(os.Stderr, "countbank:", err)
		os.Exit(1)
	}

	// convert every tree
	f, err := os.Open(*annotatedFile)
	if err != nil {
		fail(err)
	}
	var all []sentence
	byID := map[string]int{}
	sc := bufio.NewScanner(f)
	sc.Buffer(make([]byte, 1<<20), 1<<26)
	bad := 0
	for sc.Scan() {
		var rec struct {
			ID   string          `json:"id"`
			Tree json.RawMessage `json:"tree"`
		}
		if err := json.Unmarshal(sc.Bytes(), &rec); err != nil {
			fail(err)
		}
		node, err := interp.FromJSON(rec.Tree)
		if err != nil {
			fail(fmt.Errorf("%s: %v", rec.ID, err))
		}
		t, err := counts.Convert(node)
		if err != nil {
			bad++
			if bad <= 5 {
				fmt.Fprintf(os.Stderr, "%s: %v\n", rec.ID, err)
			}
			continue
		}
		s := sentence{id: rec.ID, tree: t}
		var walk func(t *cfg.Tree)
		walk = func(t *cfg.Tree) {
			if t.Words != nil {
				s.words = append(s.words, t.Words[0])
				s.tags = append(s.tags, t.Label)
				return
			}
			for _, k := range t.Children {
				walk(k)
			}
		}
		walk(t)
		byID[s.id] = len(all)
		all = append(all, s)
	}
	f.Close()
	if err := sc.Err(); err != nil {
		fail(err)
	}

	// the grammar: rules and words, over every document
	ruleCount := map[string]int{}
	ruleOf := map[string]cfg.Rule{}
	tagWord := map[[2]string]int{} // (tag_type, word)
	var walk func(t *cfg.Tree)
	walk = func(t *cfg.Tree) {
		if t.Words != nil {
			return
		}
		r := cfg.Rule{LHS: t.Label}
		for _, k := range t.Children {
			r.RHS = append(r.RHS, k.Label)
			if k.Words != nil {
				tagWord[[2]string{k.Label, k.Words[0]}]++
			}
			walk(k)
		}
		ruleCount[r.String()]++
		ruleOf[r.String()] = r
	}
	phrases, types, posTypes := map[string]bool{}, map[string]bool{}, map[string]map[string]bool{}
	for _, s := range all {
		walk(s.tree)
		for _, t := range s.tags {
			types[t] = true
			pos, _, _ := strings.Cut(t, "_")
			if posTypes[pos] == nil {
				posTypes[pos] = map[string]bool{}
			}
			posTypes[pos][t] = true
		}
	}
	var rules []cfg.Rule
	var keys []string
	for k := range ruleOf {
		keys = append(keys, k)
	}
	sort.Strings(keys)
	for _, k := range keys {
		rules = append(rules, ruleOf[k])
		phrases[ruleOf[k].LHS] = true
	}
	fmt.Printf("%d trees converted, %d refused; additivity holds at every node of every converted tree\n", len(all), bad)
	fmt.Printf("counts grammar: %d rules, %d phrase symbols, %d lexical types (tag and counts) over %d tags\n",
		len(rules), len(phrases), len(types), len(posTypes))
	var perPOS []float64
	for _, ts := range posTypes {
		perPOS = append(perPOS, float64(len(ts)))
	}
	fmt.Printf("types per tag: median %.0f, max %.0f\n", fr.Median(perPOS), slices.Max(perPOS))
	if *out != "" {
		w, err := os.Create(*out)
		if err != nil {
			fail(err)
		}
		for _, k := range keys {
			r := ruleOf[k]
			fmt.Fprintf(w, "rule\t%d\t%s\t%s\n", ruleCount[k], r.LHS, strings.Join(r.RHS, "\t"))
		}
		for tw, c := range tagWord {
			fmt.Fprintf(w, "word\t%d\t%s\t%s\n", c, tw[0], tw[1])
		}
		w.Close()
	}

	// the same held-out sample as the other commands
	orig, err := fr.ReadSentences(*annotatedFile)
	if err != nil {
		fail(err)
	}
	train, test := fr.Split(orig)
	var sample []sentence
	for _, s := range fr.Sample(test, *n, *minWords, *maxWords, *seed) {
		if i, ok := byID[s.ID]; ok {
			sample = append(sample, all[i])
		}
	}
	// types seen in training, by word and tag, and by tag
	seen := map[string]map[string]bool{} // word|pos -> tag_types
	byPOS := map[string]map[string]bool{}
	for _, o := range train {
		i, ok := byID[o.ID]
		if !ok {
			continue
		}
		s := all[i]
		for j, t := range s.tags {
			pos, _, _ := strings.Cut(t, "_")
			k := s.words[j] + "|" + pos
			if seen[k] == nil {
				seen[k] = map[string]bool{}
			}
			seen[k][t] = true
			if byPOS[pos] == nil {
				byPOS[pos] = map[string]bool{}
			}
			byPOS[pos][t] = true
		}
	}
	// probabilities: rules given their parent, words given their tag and type
	lhsCount := map[string]int{}
	for k, c := range ruleCount {
		lhsCount[ruleOf[k].LHS] += c
	}
	tagCount := map[string]int{}
	for tw, c := range tagWord {
		tagCount[tw[0]] += c
	}

	type lexicon struct {
		name  string
		entry func(s sentence, i int) (key string, tags []string)
	}
	lexicons := []lexicon{
		{"each word its own type (oracle)", func(s sentence, i int) (string, []string) {
			return s.words[i] + "|" + s.tags[i] + "|" + strconv.Itoa(i), []string{s.tags[i]}
		}},
		{"types seen with the word and its tag", func(s sentence, i int) (string, []string) {
			pos, _, _ := strings.Cut(s.tags[i], "_")
			k := s.words[i] + "|" + pos
			ts := seen[k]
			if len(ts) == 0 {
				ts = byPOS[pos]
			}
			return k, keysOf(ts)
		}},
		{"types seen with the tag", func(s sentence, i int) (string, []string) {
			pos, _, _ := strings.Cut(s.tags[i], "_")
			return pos + "|" + pos, keysOf(byPOS[pos])
		}},
	}
	fmt.Printf("\n%d held-out sentences of %d to %d words (the sample of cmd/verbentropy)\n", len(sample), *minWords, *maxWords)
	fmt.Println("For comparison, the treebank grammar with gold tags: log10 trees per word, median 1.29; mean log10 trees 18.26; weighted entropy 1.60 digits.")
	fmt.Println("lexicon\tparsed\town tree among the parses\tlog10 trees per word, median\tlog10 trees, mean\tweighted entropy, mean digits")
	for _, lx := range lexicons {
		lex := map[string][]string{}
		spell := func(s sentence) []string {
			out := make([]string, len(s.words))
			for i := range s.words {
				k, ts := lx.entry(s, i)
				lex[k] = ts
				out[i] = k
			}
			return out
		}
		spelled := make([][]string, len(sample))
		for i, s := range sample {
			spelled[i] = spell(s)
		}
		g, err := cfg.New(rules, lex, []string{"Top"})
		if err != nil {
			fail(err)
		}
		weight := make([]float64, len(g.Steps))
		for i, st := range g.Steps {
			weight[i] = 1
			if st.Rule >= 0 {
				r := g.Rules[st.Rule]
				weight[i] = float64(ruleCount[r.String()]) / float64(lhsCount[r.LHS])
				ruleWeight[r.String()] = weight[i]
			}
		}
		var parsed, own int
		var perWord, logT, ent []float64
		for i, s := range sample {
			words := spelled[i]
			f := g.Parse(words)
			if len(f.Goals) == 0 {
				continue
			}
			parsed++
			if f.Contains(respell(s.tree, words)) {
				own++
			}
			lc := fr.Log10(f.Count())
			perWord = append(perWord, lc/float64(len(words)))
			logT = append(logT, lc)
			ctx := entropy.Context{
				Start: 0,
				Next:  func(int, cfg.Item, cfg.Hyperedge, cfg.Item) int { return 0 },
				Class: func(int, cfg.Item, int) string { return "all" },
				Weight: func(it cfg.Item, e cfg.Hyperedge) float64 {
					if e.Step < 0 {
						word, _, _ := strings.Cut(words[it.L], "|")
						tag := g.Names[it.Sym]
						return (float64(tagWord[[2]string{tag, word}]) + 0.5) / (float64(tagCount[tag]) + 1)
					}
					return weight[e.Step]
				},
			}
			h := 0.0
			for _, v := range entropy.Of(f, ctx) {
				h += v
			}
			ent = append(ent, h)
		}
		if *limit > 0 && lx.name == lexicons[0].name {
			attachment(g, sample, spelled, weight, tagWord, tagCount, *limit)
		}
		fmt.Printf("%s\t%.1f%%\t%.1f%%\t%.2f\t%.2f\t%.2f\n", lx.name, fr.Pct(parsed, len(sample)), fr.Pct(own, len(sample)),
			fr.Median(perWord), fr.Mean(logT), fr.Mean(ent))
	}
}

func keysOf(m map[string]bool) []string {
	var out []string
	for k := range m {
		out = append(out, k)
	}
	sort.Strings(out)
	return out
}

// respell is a converted tree with its words spelled as the lexicon has them.
func respell(t *cfg.Tree, words []string) *cfg.Tree {
	i := 0
	var copy func(t *cfg.Tree) *cfg.Tree
	copy = func(t *cfg.Tree) *cfg.Tree {
		out := &cfg.Tree{Label: t.Label}
		if t.Words != nil {
			out.Words = []string{words[i]}
			i++
			return out
		}
		for _, k := range t.Children {
			out.Children = append(out.Children, copy(k))
		}
		return out
	}
	return copy(t)
}

// flatKey is a converted tree's class when zero-count daughters may attach
// at any layer of a projection: a daughter with its mother's counts, all of
// whose sisters count zero, is spliced into its mother, unless it has a
// scope-taking daughter of its own. With only, only phrases whose counts
// pass it are spliced.
func flatKey(t *cfg.Tree, only func(v counts.Vec) bool) string {
	var b strings.Builder
	var write func(t *cfg.Tree)
	var kids func(t *cfg.Tree) []*cfg.Tree
	kids = func(t *cfg.Tree) []*cfg.Tree {
		var out []*cfg.Tree
		for _, c := range t.Children {
			if c.Words == nil && c.Label == t.Label && zeroSisters(t, c) && !scoped(c) {
				if v, _ := counts.Parse(c.Label); only == nil || only(v) {
					out = append(out, kids(c)...)
					continue
				}
			}
			out = append(out, c)
		}
		return out
	}
	write = func(t *cfg.Tree) {
		if t.Words != nil {
			b.WriteString("(" + t.Label + " " + t.Words[0] + ")")
			return
		}
		b.WriteString("(" + t.Label)
		for _, c := range kids(t) {
			b.WriteByte(' ')
			write(c)
		}
		b.WriteByte(')')
	}
	write(t)
	return b.String()
}

func zero(t *cfg.Tree) bool {
	v, ok := counts.Parse(t.Label)
	return ok && v == counts.Vec{}
}

func zeroSisters(t, c *cfg.Tree) bool {
	for _, k := range t.Children {
		if k != c && !zero(k) {
			return false
		}
	}
	return true
}

// scoped says whether a phrase has a daughter that is a scope-taking
// adverb: an RB, or a zero-count phrase of one or two words, with a word
// in quotient.Scope.
func scoped(t *cfg.Tree) bool {
	for _, k := range t.Children {
		var ws []string
		var walk func(t *cfg.Tree)
		walk = func(t *cfg.Tree) {
			if t.Words != nil {
				w, _, _ := strings.Cut(t.Words[0], "|")
				ws = append(ws, strings.ToLower(w))
				return
			}
			for _, c := range t.Children {
				walk(c)
			}
		}
		walk(k)
		if !zero(k) || len(ws) > 2 {
			continue
		}
		for _, w := range ws {
			if quotient.Scope[w] {
				return true
			}
		}
	}
	return false
}

// attachment enumerates the oracle forests small enough and measures how
// much of their entropy is the order of attachment within projections.
func attachment(g *cfg.Grammar, sample []sentence, spelled [][]string, weight []float64,
	tagWord map[[2]string]int, tagCount map[string]int, limit int64) {
	type variant struct {
		name string
		only func(v counts.Vec) bool
	}
	vs := []variant{
		{"every projection", nil},
		{"verbs' projections (counts with S)", func(v counts.Vec) bool { return v[0] > 0 }},
		{"nouns' projections (counts NP)", func(v counts.Vec) bool { return v == counts.Vec{0, 1, 0, 0} }},
	}
	var done int
	var hT, hTW float64
	hC, hCW := make([]float64, len(vs)), make([]float64, len(vs))
	for i, s := range sample {
		f := g.Parse(spelled[i])
		c := f.Count()
		if c.Sign() == 0 || !c.IsInt64() || c.Int64() > limit {
			continue
		}
		done++
		trees, treesW := map[string]float64{}, map[string]float64{}
		classes := make([]map[string]float64, len(vs))
		classesW := make([]map[string]float64, len(vs))
		for j := range vs {
			classes[j], classesW[j] = map[string]float64{}, map[string]float64{}
		}
		k := 0
		for t := range f.Trees() {
			w := treeWeight(t, g, weight, tagWord, tagCount)
			key := strconv.Itoa(k)
			k++
			trees[key], treesW[key] = 1, w
			for j, v := range vs {
				q := flatKey(t, v.only)
				classes[j][q]++
				classesW[j][q] += w
			}
		}
		hT += entropyOf(trees)
		hTW += entropyOf(treesW)
		for j := range vs {
			hC[j] += entropyOf(classes[j])
			hCW[j] += entropyOf(classesW[j])
		}
		_ = s
	}
	d := float64(done)
	fmt.Printf("\nOrder of attachment, auxiliaries and modifiers counting zero: the oracle forests of at most %d trees (%d sentences), enumerated\n", limit, done)
	fmt.Printf("entropy of the trees: %.2f digits uniform, %.2f weighted\n", hT/d, hTW/d)
	fmt.Println("splicing same-count daughters with zero-count sisters in\tH(class) uniform\tshare removed\tH(class) weighted\tshare removed")
	for j, v := range vs {
		fmt.Printf("%s\t%.2f\t%.0f%%\t%.2f\t%.0f%%\n", v.name, hC[j]/d, 100*(hT-hC[j])/hT, hCW[j]/d, 100*(hTW-hCW[j])/hTW)
	}
	fmt.Println()
}

func entropyOf(m map[string]float64) float64 {
	z, h := 0.0, 0.0
	for _, v := range m {
		z += v
	}
	for _, v := range m {
		if v > 0 {
			p := v / z
			h -= p * math.Log10(p)
		}
	}
	return h
}

// treeWeight is a tree's weight as the entropy measure weighs it.
func treeWeight(t *cfg.Tree, g *cfg.Grammar, weight []float64, tagWord map[[2]string]int, tagCount map[string]int) float64 {
	if t.Words != nil {
		word, _, _ := strings.Cut(t.Words[0], "|")
		return (float64(tagWord[[2]string{t.Label, word}]) + 0.5) / (float64(tagCount[t.Label]) + 1)
	}
	r := cfg.Rule{LHS: t.Label}
	w := 1.0
	for _, k := range t.Children {
		r.RHS = append(r.RHS, k.Label)
		w *= treeWeight(k, g, weight, tagWord, tagCount)
	}
	return w * ruleWeight[r.String()]
}

var ruleWeight = map[string]float64{}
