// Command quotient measures, by enumeration, how much of a forest's
// ambiguity is the order in which dependents attach within a head's
// projection (package quotient): for short sentences whose forests can be
// enumerated, each tree is mapped to its class, and the classes are counted
// and their entropy taken, every tree as likely as any other and weighted by
// the treebank's rule probabilities. Beside them, the flat meanings of
// interp.Flat, as in docs/verbs/09-dont-care.md: H(meaning | class) says
// whether a class ever holds trees of different meanings (0 if never), and
// the share of the "don't care" entropy, H(tree) - H(meaning), that the
// quotient accounts for is (H(tree) - H(class)) / (H(tree) - H(meaning)).
//
//	quotient -counts counts.tsv -annotated annotated.jsonl [-n 200] [-min 3] [-max 7] [-limit 300000]
package main

import (
	"flag"
	"fmt"
	"math"
	"math/big"
	mrand "math/rand"
	"os"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
	fr "github.com/cbrew/quadruplet/go/frames"
	"github.com/cbrew/quadruplet/go/interp"
	"github.com/cbrew/quadruplet/go/quotient"
)

type variant struct {
	name string
	o    quotient.Options
}

var variants = []variant{
	{"verbs", quotient.Options{Verbs: true}},
	{"verbs, no scope exemption", quotient.Options{Verbs: true, NoScope: true}},
	{"verbs and auxiliaries", quotient.Options{Verbs: true, Aux: true}},
	{"nouns", quotient.Options{Nouns: true}},
	{"verbs, auxiliaries and nouns", quotient.Options{Verbs: true, Aux: true, Nouns: true}},
	{"phrase labels", quotient.Options{Unlabelled: true}},
	{"phrase labels as counts of S, NP, PP", quotient.Options{Counts: true}},
	{"counts, unary chains collapsed", quotient.Options{Counts: true, Unary: true}},
}

// dist is a distribution over keys, by weight.
type dist map[string]float64

func entropy(d dist) float64 {
	z := 0.0
	for _, w := range d {
		z += w
	}
	h := 0.0
	for _, w := range d {
		if w > 0 {
			p := w / z
			h -= p * math.Log10(p)
		}
	}
	return h
}

// sums are the per-sentence figures summed over sentences.
type sums struct {
	classes, h, hW, hMgivenC, hMgivenCW float64
}

func main() {
	countsFile := flag.String("counts", "", "the grammar with counts, as tools/masc/treebank.py writes it")
	annotatedFile := flag.String("annotated", "", "the corpus's trees, as tools/masc/treebank.py writes them")
	n := flag.Int("n", 200, "how many test sentences to sample")
	minWords := flag.Int("min", 3, "the fewest words a sampled sentence has")
	maxWords := flag.Int("max", 7, "the most words a sampled sentence has")
	limit := flag.Int64("limit", 300000, "enumerate forests of at most this many trees")
	seed := flag.Uint64("seed", 1, "the sample's random seed")
	show := flag.Int("show", 0, "for this many sentences, print some trees of the meaning with the most trees")
	estimate := flag.Int("estimate", 0, "instead, estimate the verb quotients' part of the uniform entropy by drawing this many trees from each forest")
	flag.Parse()
	if *countsFile == "" || *annotatedFile == "" {
		flag.Usage()
		os.Exit(2)
	}
	fail := func(err error) {
		fmt.Fprintln(os.Stderr, "quotient:", err)
		os.Exit(1)
	}
	rules, counts, err := fr.ReadRuleCounts(*countsFile)
	if err != nil {
		fail(err)
	}
	sents, err := fr.ReadSentences(*annotatedFile)
	if err != nil {
		fail(err)
	}
	_, test := fr.Split(sents)
	sample := fr.Sample(test, *n, *minWords, *maxWords, *seed)
	lexicon := map[string][]string{}
	for _, s := range sample {
		for i, w := range s.Words {
			lexicon[w+"|"+s.Tags[i]] = []string{s.Tags[i]}
		}
	}
	g, err := cfg.New(rules, lexicon, []string{"Top"})
	if err != nil {
		fail(err)
	}
	prob := fr.Probabilities(rules, counts)
	if *estimate > 0 {
		estimateAll(g, rules, sample, *estimate, *limit, *seed)
		return
	}

	var done int
	var logT, hT, hTW, hM, hMW float64
	per := make([]sums, len(variants))
	for _, s := range sample {
		f := g.Parse(s.Spell())
		c := f.Count()
		if c.Sign() == 0 || c.Cmp(big.NewInt(*limit)) > 0 {
			continue
		}
		done++
		trees, treesW := dist{}, dist{}
		meanings, meaningsW := dist{}, dist{}
		classes := make([]dist, len(variants))
		classesW := make([]dist, len(variants))
		joint := make([]dist, len(variants)) // class and meaning
		jointW := make([]dist, len(variants))
		for i := range variants {
			classes[i], classesW[i], joint[i], jointW[i] = dist{}, dist{}, dist{}, dist{}
		}
		k := 0
		for t := range f.Trees() {
			w := weight(t, prob)
			key := fmt.Sprint(k)
			k++
			trees[key], treesW[key] = 1, w
			m := meaning(t)
			meanings[m]++
			meaningsW[m] += w
			for i, v := range variants {
				q := quotient.Key(t, v.o)
				classes[i][q]++
				classesW[i][q] += w
				joint[i][q+"\x00"+m]++
				jointW[i][q+"\x00"+m] += w
			}
		}
		if *show > 0 {
			*show--
			printBiggest(f, s)
		}
		logT += math.Log10(float64(k))
		hT += entropy(trees)
		hTW += entropy(treesW)
		hM += entropy(meanings)
		hMW += entropy(meaningsW)
		for i := range variants {
			per[i].classes += math.Log10(float64(len(classes[i])))
			per[i].h += entropy(classes[i])
			per[i].hW += entropy(classesW[i])
			per[i].hMgivenC += entropy(joint[i]) - entropy(classes[i])
			per[i].hMgivenCW += entropy(jointW[i]) - entropy(classesW[i])
		}
	}
	d := float64(done)
	fmt.Printf("%d test sentences of %d to %d words with forests of at most %d trees, of %d sampled; gold tags; means per sentence, decimal digits\n\n",
		done, *minWords, *maxWords, *limit, len(sample))
	fmt.Printf("trees: log10 %.2f; meanings (interp.Flat): entropy %.2f uniform, %.2f weighted by rule probabilities\n",
		logT/d, hM/d, hMW/d)
	fmt.Printf("entropy of the trees: %.2f uniform, %.2f weighted; so the don't-care entropy is %.2f and %.2f\n\n",
		hT/d, hTW/d, (hT-hM)/d, (hTW-hMW)/d)
	fmt.Println("quotient\tlog10 classes\tH(class) uniform\tH(class) weighted\tdon't care it accounts for, uniform\tweighted\tH(meaning | class) uniform\tweighted")
	for i, v := range variants {
		p := per[i]
		fmt.Printf("%s\t%.2f\t%.2f\t%.2f\t%.0f%%\t%.0f%%\t%.3f\t%.3f\n", v.name, p.classes/d, p.h/d, p.hW/d,
			100*(hT-p.h)/(hT-hM), 100*(hTW-p.hW)/(hTW-hMW), p.hMgivenC/d, p.hMgivenCW/d)
	}
}

// weight is a tree's weight: the product of its rules' probabilities.
func weight(t *cfg.Tree, prob map[string]float64) float64 {
	if t.Children == nil {
		return 1
	}
	r := cfg.Rule{LHS: t.Label}
	w := 1.0
	for _, k := range t.Children {
		r.RHS = append(r.RHS, k.Label)
		w *= weight(k, prob)
	}
	return w * prob[r.String()]
}

// meaning is the tree's flat meaning, as a string.
func meaning(t *cfg.Tree) string {
	var b strings.Builder
	for _, a := range interp.Flat(interp.FromTree(plain(t))) {
		b.WriteString(a.String())
		b.WriteByte(' ')
	}
	return b.String()
}

// plain is the tree with its words as words, not word|tag.
func plain(t *cfg.Tree) *cfg.Tree {
	out := &cfg.Tree{Label: t.Label}
	if t.Children == nil {
		for _, w := range t.Words {
			w, _, _ = strings.Cut(w, "|")
			out.Words = append(out.Words, w)
		}
		return out
	}
	for _, k := range t.Children {
		out.Children = append(out.Children, plain(k))
	}
	return out
}

// printBiggest prints a few trees of the meaning that has the most trees in
// the forest, with their verb classes.
func printBiggest(f *cfg.Forest, s fr.Sentence) {
	by := map[string][]*cfg.Tree{}
	for t := range f.Trees() {
		m := meaning(t)
		by[m] = append(by[m], t)
	}
	best := ""
	for m, ts := range by {
		if len(ts) > len(by[best]) {
			best = m
		}
	}
	fmt.Printf("%s: %s\n  %d meanings; the largest has %d trees: %s\n", s.ID, strings.Join(s.Words, " "), len(by), len(by[best]), best)
	for i, t := range by[best] {
		if i == 6 {
			break
		}
		fmt.Println("   ", plain(t))
	}
}

// bracketings counts the ways the grammar's projection rules bracket a
// projection's dependents under its top label: parses of the dependents'
// labels, as words, by the lexical verb phrase rules and the layer rules
// alone. A tree drawn from a forest is fixed but for the bracketing inside
// its projections, so the number of trees in its class is the product of
// these over its projections.
type bracketings struct {
	g    *cfg.Grammar
	memo map[string]float64
}

func newBracketings(rules []cfg.Rule, o quotient.Options) *bracketings {
	var rs []cfg.Rule
	lex := map[string][]string{}
	var starts []string
	seen := map[string]bool{}
	for _, r := range rules {
		if !fr.IsVP(r.LHS) || !(fr.LexicalVP(r.LHS, r.RHS) || isLayer(r, o)) {
			continue
		}
		rs = append(rs, r)
		if !seen[r.LHS] {
			seen[r.LHS] = true
			starts = append(starts, r.LHS)
		}
		for _, d := range r.RHS {
			lex[d] = []string{d}
		}
	}
	g, err := cfg.New(rs, lex, starts)
	if err != nil {
		panic(err)
	}
	return &bracketings{g, map[string]float64{}}
}

// isLayer is quotient's layer rule: a verb phrase with exactly one verb
// phrase daughter, no conjunction, and unless o.Aux no verb, modal or to.
func isLayer(r cfg.Rule, o quotient.Options) bool {
	vps := 0
	for _, d := range r.RHS {
		switch t := fr.Tag(d); {
		case fr.Chain(d)[0] == "VP":
			vps++
		case t == "CC" || t == "CONJP":
			return false
		case !o.Aux && (fr.IsVerbTag(d) || t == "MD" || t == "TO"):
			return false
		}
	}
	return vps == 1
}

func (b *bracketings) count(p quotient.Projection) float64 {
	key := p.Top + "\x00" + strings.Join(p.Deps, " ")
	if v, ok := b.memo[key]; ok {
		return v
	}
	v := 0.0
	if sym, ok := b.g.Symbol(p.Top); ok {
		c, _ := new(big.Float).SetInt(b.g.Parse(p.Deps).CountFrom(sym)).Float64()
		v = c
	}
	b.memo[key] = v
	return v
}

// logClass is log10 of the number of trees in the tree's class.
func (b *bracketings) logClass(t *cfg.Tree, o quotient.Options) float64 {
	s := 0.0
	for _, p := range quotient.Projections(t, o) {
		s += math.Log10(b.count(p))
	}
	return s
}

// estimateAll estimates, for each verb quotient without the scope
// exemption, the part of each forest's uniform entropy it removes: the mean
// of log10 of the size of a drawn tree's class. Forests small enough to
// enumerate are measured exactly as well, to check the estimate.
func estimateAll(g *cfg.Grammar, rules []cfg.Rule, sample []fr.Sentence, k int, limit int64, seed uint64) {
	vs := []variant{
		{"verbs, no scope exemption", quotient.Options{Verbs: true, NoScope: true}},
		{"verbs and auxiliaries, no scope exemption", quotient.Options{Verbs: true, Aux: true, NoScope: true}},
	}
	bs := make([]*bracketings, len(vs))
	for i, v := range vs {
		bs[i] = newBracketings(rules, v.o)
	}
	rng := mrand.New(mrand.NewSource(int64(seed)))
	var done int
	var logT float64
	est := make([]float64, len(vs))
	gold := make([]float64, len(vs)) // log10 of the class size of the sentence's own tree
	var checked int
	worst := 0.0
	for _, s := range sample {
		f := g.Parse(s.Spell())
		c := f.Count()
		if c.Sign() == 0 {
			continue
		}
		done++
		lt := log10(c)
		logT += lt
		sampler := f.Sampler(rng)
		sums := make([]float64, len(vs))
		for range k {
			t := sampler.Tree()
			for i, v := range vs {
				sums[i] += bs[i].logClass(t, v.o)
			}
		}
		for i, v := range vs {
			est[i] += sums[i] / float64(k)
			gold[i] += bs[i].logClass(respell(s), v.o)
		}
		if c.Cmp(big.NewInt(limit)) <= 0 {
			// exactly: the mean over all trees of log10 of the class size
			// is log10 T less the entropy of the classes
			checked++
			for i, v := range vs {
				classes := dist{}
				mean, n := 0.0, 0
				for t := range f.Trees() {
					classes[quotient.Key(t, v.o)]++
					mean += bs[i].logClass(t, v.o)
					n++
				}
				worst = max(worst, math.Abs(mean/float64(n)-(lt-entropy(classes))))
			}
		}
	}
	d := float64(done)
	fmt.Printf("%d sentences of %d sampled; gold tags; every tree as likely as any other; %d trees drawn from each forest\n", done, len(sample), k)
	fmt.Printf("check: on the %d forests of at most %d trees, the mean of log10 class size over all trees differs from log10 T - H(class) by at most %.2g\n\n", checked, limit, worst)
	fmt.Printf("log10 trees: mean %.2f\n", logT/d)
	fmt.Println("quotient\tentropy it removes, digits (mean)\tshare of log10 trees\tlog10 size of the own tree's class (mean)")
	for i, v := range vs {
		fmt.Printf("%s\t%.2f\t%.1f%%\t%.2f\n", v.name, est[i]/d, 100*est[i]/logT, gold[i]/d)
	}
}

func log10(x *big.Int) float64 {
	s := x.String()
	lead, _ := new(big.Float).SetString("0." + s[:min(len(s), 15)])
	l, _ := lead.Float64()
	return float64(len(s)) + math.Log10(l)
}

// respell is the sentence's own tree with its words spelled word|tag.
func respell(s fr.Sentence) *cfg.Tree {
	i := 0
	var copy func(t *cfg.Tree) *cfg.Tree
	copy = func(t *cfg.Tree) *cfg.Tree {
		out := &cfg.Tree{Label: t.Label}
		if t.Words != nil {
			out.Words = []string{s.Words[i] + "|" + s.Tags[i]}
			i++
			return out
		}
		for _, c := range t.Children {
			out.Children = append(out.Children, copy(c))
		}
		return out
	}
	return copy(s.Tree)
}
