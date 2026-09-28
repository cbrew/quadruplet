// Command ambiguity measures where a treebank grammar's ambiguity comes
// from. It parses a sample of the corpus's sentences with variants of the
// grammar tools/masc/treebank.py reads off it (counts.tsv): the grammar as it
// is; with each word restricted to the tag its own tree gives it; with only
// the rules seen at least k times; and both. For each it reports how many
// sentences still parse, how many of their own trees are still among the
// parses, and the number of trees, as log10 per word.
//
// With -samples k, it then draws k trees at random from each sentence's
// forest under the grammar as it is, every tree as likely as any other, and
// places the sentence's own tree among them on some measures of a tree: where
// it stands shows what, if anything, sets the attested tree apart from the
// astronomically many others.
//
// With -exact, it measures every tree of each forest at once (package
// interp's PhraseCounts, DepthCounts, ShortestDependencies) and places the
// sentence's own tree exactly: among the trees by number of phrases, by how
// deep its most centre-embedded word is, and against the shortest total
// dependency length any tree has.
//
//	ambiguity -counts counts.tsv -annotated annotated.jsonl [-n 300] [-min 5] [-max 25] [-samples 200] [-exact]
package main

import (
	"bufio"
	"encoding/json"
	"flag"
	"fmt"
	"math"
	"math/big"
	mrand "math/rand"
	"math/rand/v2"
	"os"
	"slices"
	"strconv"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
	"github.com/cbrew/quadruplet/go/interp"
)

type rule struct {
	count int
	r     cfg.Rule
}

type sentence struct {
	id    string
	words []string
	tags  []string
	tree  *cfg.Tree // with the grammar's labels, as counts.tsv writes them
}

// variant is a grammar and a way of spelling a sentence's words for it.
type variant struct {
	name  string
	g     *cfg.Grammar
	spell func(s sentence) []string
}

func main() {
	countsFile := flag.String("counts", "", "the grammar with counts, as tools/masc/treebank.py writes it")
	annotatedFile := flag.String("annotated", "", "the corpus's trees, as tools/masc/treebank.py writes them")
	n := flag.Int("n", 300, "how many sentences to sample")
	minWords := flag.Int("min", 5, "the fewest words a sampled sentence has")
	maxWords := flag.Int("max", 25, "the most words a sampled sentence has")
	seed := flag.Uint64("seed", 1, "the sample's random seed")
	samples := flag.Int("samples", 0, "draw this many trees from each forest, and place the sentence's own among them")
	exactly := flag.Bool("exact", false, "place the sentence's own tree among all of its forest's, by phrases, depth and dependency length")
	flag.Parse()
	if *countsFile == "" || *annotatedFile == "" {
		flag.Usage()
		os.Exit(2)
	}
	fail := func(err error) {
		fmt.Fprintln(os.Stderr, "ambiguity:", err)
		os.Exit(1)
	}
	rules, tagged, err := readCounts(*countsFile)
	if err != nil {
		fail(err)
	}
	sents, err := readSentences(*annotatedFile, *minWords, *maxWords)
	if err != nil {
		fail(err)
	}
	rng := rand.New(rand.NewPCG(*seed, 0))
	rng.Shuffle(len(sents), func(i, j int) { sents[i], sents[j] = sents[j], sents[i] })
	sents = sents[:min(*n, len(sents))]
	slices.SortFunc(sents, func(a, b sentence) int { return len(a.words) - len(b.words) })

	// the lexicons: every tag a word has anywhere, or one entry per word and tag
	anyTag, oneTag := map[string][]string{}, map[string][]string{}
	for wt := range tagged {
		anyTag[wt[1]] = append(anyTag[wt[1]], wt[0])
		oneTag[wt[1]+"|"+wt[0]] = []string{wt[0]}
	}
	asIs := func(s sentence) []string { return s.words }
	goldTagged := func(s sentence) []string {
		out := make([]string, len(s.words))
		for i := range s.words {
			out[i] = s.words[i] + "|" + s.tags[i]
		}
		return out
	}
	build := func(name string, k int, lexicon map[string][]string, spell func(sentence) []string) variant {
		var rs []cfg.Rule
		for _, r := range rules {
			if r.count >= k {
				rs = append(rs, r.r)
			}
		}
		g, err := cfg.New(rs, lexicon, []string{"Top"})
		if err != nil {
			fail(err)
		}
		return variant{name, g, spell}
	}
	variants := []variant{
		build("the grammar as it is", 1, anyTag, asIs),
		build("gold tags", 1, oneTag, goldTagged),
		build("rules seen twice or more", 2, anyTag, asIs),
		build("rules seen 5 times or more", 5, anyTag, asIs),
		build("gold tags, rules seen twice or more", 2, oneTag, goldTagged),
		build("gold tags, rules seen 5 times or more", 5, oneTag, goldTagged),
	}
	fmt.Printf("%d sentences of %d to %d words\n\n", len(sents), *minWords, *maxWords)
	fmt.Println("grammar\tparsed\town tree among the parses\tlog10 trees per word: median\t5-9 words\t10-14\t15-19\t20-25")
	for _, v := range variants {
		var parsed, own int
		var perWord []float64
		byLength := map[int][]float64{}
		for _, s := range sents {
			words := v.spell(s)
			f := v.g.Parse(words)
			if len(f.Goals) == 0 {
				continue
			}
			parsed++
			if f.Contains(respelled(s.tree, words)) {
				own++
			}
			pw := log10(f.Count()) / float64(len(words))
			perWord = append(perWord, pw)
			bin := min(len(words), 25) / 5
			byLength[bin] = append(byLength[bin], pw)
		}
		fmt.Printf("%s\t%.1f%%\t%.1f%%\t%.2f", v.name, pct(parsed, len(sents)), pct(own, len(sents)), median(perWord))
		for bin := 1; bin <= 4; bin++ {
			fmt.Printf("\t%.2f", median(byLength[bin]))
		}
		fmt.Println()
	}
	if *exactly {
		exact(variants[0].g, sents)
	}
	if *samples > 0 {
		counts := map[string]int{}
		for _, r := range rules {
			counts[r.r.String()] = r.count
		}
		typical(variants[0].g, sents, *samples, *seed, counts)
	}
}

// exact places each sentence's own tree among all the trees of its forest.
func exact(g *cfg.Grammar, sents []sentence) {
	const most = 4
	var used int
	var phrasePct, phraseTied, fewer []float64
	ownDepth := make([]int, most+2)
	forestDepth := make([][]float64, most+1) // log10 trees per word within each depth
	var withinOwn []float64                  // share of the forest no deeper than the own tree
	var excess []int
	var shortestWays []float64
	for _, s := range sents {
		f := g.Parse(s.words)
		if len(f.Goals) == 0 || !f.Contains(s.tree) {
			continue
		}
		used++
		words := float64(len(s.words))
		own := interp.FromTree(s.tree)

		counts := interp.PhraseCounts(f)
		p := interp.Phrases(own)
		var below, all float64
		for q, c := range counts {
			all += c
			if q < p {
				below += c
			}
		}
		phrasePct = append(phrasePct, 100*(below+counts[p]/2)/all)
		phraseTied = append(phraseTied, math.Log10(counts[p])/words)
		fewer = append(fewer, math.Log10(max(below, 1))/words)

		d := interp.CentreDepth(own)
		ownDepth[min(d, most+1)]++
		within := interp.DepthCounts(f, most)
		for b := range within {
			forestDepth[b] = append(forestDepth[b], math.Log10(max(within[b], 1))/words)
		}
		if d <= most {
			withinOwn = append(withinOwn, 100*within[d]/all)
		} else {
			withinOwn = append(withinOwn, 100)
		}

		least, ways := interp.ShortestDependencies(f)
		excess = append(excess, interp.DependencyLength(own)-least)
		shortestWays = append(shortestWays, math.Log10(ways)/words)
	}
	fmt.Printf("\nthe sentence's own tree among all the trees of its forest, over %d sentences:\n", used)
	fmt.Printf("phrases: own tree's percentile, median %.2f; trees with as many phrases as it, median %.2f log10 per word; trees with fewer, %.2f\n",
		median(phrasePct), median(phraseTied), median(fewer))
	fmt.Print("centre-embedding: own trees at depth")
	for d := 0; d <= most+1; d++ {
		fmt.Printf(" %d: %d", d, ownDepth[d])
	}
	fmt.Println()
	fmt.Print("  trees of the forest within depth, median log10 per word:")
	for b := range forestDepth {
		fmt.Printf(" %d: %.2f", b, median(forestDepth[b]))
	}
	fmt.Printf("\n  share of the forest no deeper than the own tree, median %.2f%%\n", median(withinOwn))
	var shortest, near int
	var ex []float64
	for _, e := range excess {
		if e == 0 {
			shortest++
		}
		if e <= 2 {
			near++
		}
		ex = append(ex, float64(e))
	}
	fmt.Printf("dependency length: own tree the shortest in %.1f%% of sentences, within 2 words of it in %.1f%%; its excess, median %.0f words\n",
		pct(shortest, len(excess)), pct(near, len(excess)), median(ex))
	fmt.Printf("  trees with the shortest, median %.2f log10 per word\n", median(shortestWays))
}

// measure is something to say of a tree, bigger or smaller.
type measure struct {
	name string
	of   func(t *interp.Node, rules map[string]int) float64
}

var measures = []measure{
	{"mean dependency length", meanDependencyLength},
	{"most centre-embedded word", centreEmbedding},
	{"phrases", func(t *interp.Node, _ map[string]int) float64 {
		return interp.Fold(t, func(*interp.Node) float64 { return 0 }, func(_ *interp.Node, kids []float64) float64 {
			s := 1.0
			for _, k := range kids {
				s += k
			}
			return s
		})
	}},
	{"depth", func(t *interp.Node, _ map[string]int) float64 {
		return interp.Fold(t, func(*interp.Node) float64 { return 0 }, func(_ *interp.Node, kids []float64) float64 {
			return 1 + slices.Max(kids)
		})
	}},
	{"mean log10 count of its rules", meanRuleCount},
}

// typical draws trees from each sentence's forest and reports, for each
// measure, where the sentence's own tree falls among them: its mean
// percentile, and how often it is below all but 5% of them or above all
// but 5%.
func typical(g *cfg.Grammar, sents []sentence, k int, seed uint64, rules map[string]int) {
	rng := mrand.New(mrand.NewSource(int64(seed)))
	pcts := make([][]float64, len(measures))
	var fewest, ownFewest int
	var logAll, logFewest []float64
	excess := map[int]int{} // own tree's phrases beyond the fewest -> sentences
	for _, s := range sents {
		f := g.Parse(s.words)
		if len(f.Goals) == 0 || !f.Contains(s.tree) {
			continue
		}
		least, ways := fewestPhrases(f)
		fewest++
		own := phrases(s.tree)
		if own == least {
			ownFewest++
		}
		excess[min(own-least, 5)]++
		logAll = append(logAll, log10(f.Count())/float64(len(s.words)))
		logFewest = append(logFewest, log10(ways)/float64(len(s.words)))
		ownTree := interp.FromTree(s.tree)
		drawn := make([]*interp.Node, k)
		sampler := f.Sampler(rng)
		for i := range drawn {
			drawn[i] = interp.FromTree(sampler.Tree())
		}
		for m, ms := range measures {
			x := ms.of(ownTree, rules)
			below, same := 0, 0
			for _, d := range drawn {
				switch y := ms.of(d, rules); {
				case y < x:
					below++
				case y == x:
					same++
				}
			}
			pcts[m] = append(pcts[m], 100*(float64(below)+float64(same)/2)/float64(k))
		}
	}
	fmt.Printf("\nthe sentence's own tree among %d drawn from its forest, over %d sentences:\n", k, len(pcts[0]))
	fmt.Println("measure\tmean percentile\town tree in the lowest 5%\tin the highest 5%")
	for m, ms := range measures {
		var sum float64
		lo, hi := 0, 0
		for _, p := range pcts[m] {
			sum += p
			if p <= 5 {
				lo++
			}
			if p >= 95 {
				hi++
			}
		}
		n := len(pcts[m])
		fmt.Printf("%s\t%.1f\t%.1f%%\t%.1f%%\n", ms.name, sum/float64(n), pct(lo, n), pct(hi, n))
	}
	fmt.Printf("\ntrees with the fewest phrases, over %d sentences:\n", fewest)
	fmt.Printf("log10 trees per word, median: all %.2f, with the fewest phrases %.2f\n", median(logAll), median(logFewest))
	fmt.Printf("own tree has the fewest phrases: %.1f%%\n", pct(ownFewest, fewest))
	fmt.Print("own tree's phrases beyond the fewest (5 or more as 5):")
	for d := 0; d <= 5; d++ {
		fmt.Printf(" %d: %d", d, excess[d])
	}
	fmt.Println()
}

// phrases is the number of phrases in a tree: its nodes above the words.
func phrases(t *cfg.Tree) int {
	if t.Words != nil {
		return 0
	}
	n := 1
	for _, c := range t.Children {
		n += phrases(c)
	}
	return n
}

// fewestPhrases is the fewest phrases any tree of the forest has, and how
// many trees have that few: a pass over the forest in the semiring of
// (least, how many), since a tree's phrases are the sum of its parts'.
func fewestPhrases(f *cfg.Forest) (int, *big.Int) {
	least := make([]int, len(f.Items))
	ways := make([]*big.Int, len(f.Items))
	for _, item := range f.Order() {
		best, count := math.MaxInt, new(big.Int)
		for e, end := f.EdgeRange(item); e < end; e++ {
			h := f.Edge(e)
			cost, w := 0, big.NewInt(1)
			if h.Step >= 0 && !f.G.Aux[f.Items[item].Sym] {
				cost = 1 // a phrase, not a word nor a binarization's auxiliary
			}
			for _, c := range []int32{h.Left, h.Right} {
				if c >= 0 {
					cost += least[c]
					w.Mul(w, ways[c])
				}
			}
			switch {
			case cost < best:
				best, count = cost, w
			case cost == best:
				count.Add(count, w)
			}
		}
		least[item], ways[item] = best, count
	}
	best, count := math.MaxInt, new(big.Int)
	for _, g := range f.Goals {
		switch {
		case least[g] < best:
			best, count = least[g], new(big.Int).Set(ways[g])
		case least[g] == best:
			count.Add(count, ways[g])
		}
	}
	return best, count
}

// meanDependencyLength is the mean distance in words between a word and its
// head, over the words that are not punctuation.
func meanDependencyLength(t *interp.Node, _ map[string]int) float64 {
	var punct []bool
	interp.Fold(t, func(w *interp.Node) int {
		punct = append(punct, interp.IsPunctuation(w))
		return 0
	}, func(*interp.Node, []int) int { return 0 })
	total, n := 0, 0
	for _, d := range interp.Dependencies(t) {
		if d.Head >= 0 && !punct[d.Dependent] {
			total += max(d.Dependent-d.Head, d.Head-d.Dependent)
			n++
		}
	}
	if n == 0 {
		return 0
	}
	return float64(total) / float64(n)
}

// centreEmbedding is the most phrases any word lies inside by way of a
// daughter that is neither the first nor the last of its parent's.
func centreEmbedding(t *interp.Node, _ map[string]int) float64 {
	var deepest func(n *interp.Node, d int) int
	deepest = func(n *interp.Node, d int) int {
		best := d
		for i, k := range n.Kids {
			kd := d
			if i > 0 && i < len(n.Kids)-1 {
				kd++
			}
			best = max(best, deepest(k, kd))
		}
		return best
	}
	return float64(deepest(t, 0))
}

// meanRuleCount is the mean log10 count, in the treebank, of the rules a
// tree uses. Since the treebank's own trees are counted, it favours them.
func meanRuleCount(t *interp.Node, rules map[string]int) float64 {
	var sum float64
	n := 0
	var walk func(n *interp.Node)
	walk = func(p *interp.Node) {
		if p.IsWord() {
			return
		}
		r := cfg.Rule{LHS: p.Label}
		for _, k := range p.Kids {
			r.RHS = append(r.RHS, k.Label)
			walk(k)
		}
		sum += math.Log10(float64(max(rules[r.String()], 1)))
		n++
	}
	walk(t)
	return sum / float64(max(n, 1))
}

// respelled is a tree with its words replaced, in order, by words.
func respelled(t *cfg.Tree, words []string) *cfg.Tree {
	i := 0
	var copy func(t *cfg.Tree) *cfg.Tree
	copy = func(t *cfg.Tree) *cfg.Tree {
		out := &cfg.Tree{Label: t.Label}
		if t.Words != nil {
			out.Words = []string{words[i]}
			i++
			return out
		}
		for _, c := range t.Children {
			out.Children = append(out.Children, copy(c))
		}
		return out
	}
	return copy(t)
}

func readCounts(file string) ([]rule, map[[2]string]int, error) {
	f, err := os.Open(file)
	if err != nil {
		return nil, nil, err
	}
	defer f.Close()
	var rules []rule
	tagged := map[[2]string]int{}
	sc := bufio.NewScanner(f)
	for sc.Scan() {
		rec := strings.Split(sc.Text(), "\t")
		if len(rec) < 4 {
			return nil, nil, fmt.Errorf("%s: short line %q", file, sc.Text())
		}
		k, err := strconv.Atoi(rec[1])
		if err != nil {
			return nil, nil, err
		}
		switch rec[0] {
		case "rule":
			rules = append(rules, rule{k, cfg.Rule{LHS: rec[2], RHS: rec[3:]}})
		case "word":
			tagged[[2]string{rec[2], rec[3]}] += k
		}
	}
	return rules, tagged, sc.Err()
}

// jnode is a node of annotated.jsonl.
type jnode struct {
	C string  `json:"c"`
	K []jnode `json:"k"`
	W *string `json:"w"`
}

func readSentences(file string, lo, hi int) ([]sentence, error) {
	f, err := os.Open(file)
	if err != nil {
		return nil, err
	}
	defer f.Close()
	var out []sentence
	sc := bufio.NewScanner(f)
	sc.Buffer(make([]byte, 1<<20), 1<<26)
	for sc.Scan() {
		var rec struct {
			ID   string `json:"id"`
			Tree jnode  `json:"tree"`
		}
		if err := json.Unmarshal(sc.Bytes(), &rec); err != nil {
			return nil, err
		}
		s := sentence{id: rec.ID}
		var tree func(j *jnode) *cfg.Tree
		tree = func(j *jnode) *cfg.Tree {
			t := &cfg.Tree{Label: strings.TrimSuffix(j.C, "[]")}
			if j.W != nil {
				t.Words = []string{*j.W}
				s.words, s.tags = append(s.words, *j.W), append(s.tags, t.Label)
				return t
			}
			for i := range j.K {
				t.Children = append(t.Children, tree(&j.K[i]))
			}
			return t
		}
		s.tree = tree(&rec.Tree)
		if len(s.words) >= lo && len(s.words) <= hi {
			out = append(out, s)
		}
	}
	return out, sc.Err()
}

func log10(x *big.Int) float64 {
	if x.Sign() <= 0 {
		return 0
	}
	s := x.String()
	lead, _ := strconv.ParseFloat("0."+s[:min(len(s), 15)], 64)
	return float64(len(s)) + math.Log10(lead)
}

func median(xs []float64) float64 {
	if len(xs) == 0 {
		return math.NaN()
	}
	ys := slices.Clone(xs)
	slices.Sort(ys)
	return ys[len(ys)/2]
}

func pct(a, b int) float64 {
	if b == 0 {
		return 0
	}
	return 100 * float64(a) / float64(b)
}
