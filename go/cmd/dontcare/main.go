// Command dontcare splits the entropy of each sentence's forest into the
// part the flat semantics can see and the part it cannot: the "don't care"
// share of the ambiguity (task 5 of docs/verbs/COORDINATION.md).
//
// A tree's meaning, interp.Flat, is a set of atoms: each content word says
// itself of its referent, and each daughter of a phrase stands in a relation
// to the phrase's referent named by its marker (a preposition or
// complementizer), its function tags, or its configuration (obj, comp, nn,
// num, poss, mod). At each item of the forest, the hyperedges that build it
// are grouped by what the phrase they build would contribute: the head
// daughter's position and category, each other daughter's relation class,
// and the spans the top step of the binarization gives the daughters. By
// the chain rule, cfg.Forest.Entropy then splits the choice at the item
// into the choice of a group (meaning-visible) and the choice of a rule
// within the group (don't care). The choices of the remaining daughters'
// spans, made at the auxiliary items of binarization, are reported apart
// as spans: they change which words fall under which relation, so they are
// taken to be visible.
//
// This is a local approximation, in three ways. The relation a daughter
// contributes is read off its category, not off the word heading it
// (a preposition names its relation, and two prepositional phrases over the
// same words have the same one, but a phrase's referent is its head word,
// which the item does not fix). Function tags, which the parser's trees get
// from a learned table, are not consulted. And two trees may differ at
// several items and yet mean the same, as NP -> DT JJ NN and NP -> DT
// (NP JJ NN) do: their choices are visible here, though the meaning is not.
// So the local don't-care share is a lower bound on the true one. With
// -exact N, the true share is also computed, by enumerating the trees of
// every forest with at most N of them and taking the entropy of the
// distribution of meanings; the two are reported side by side on those
// sentences.
//
//	dontcare -counts counts.tsv -annotated annotated.jsonl [-n 300] [-min 5] [-max 25] [-pcfg] [-exact 20000]
package main

import (
	"flag"
	"fmt"
	"math"
	"math/big"
	"os"
	"slices"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
	fr "github.com/cbrew/quadruplet/go/frames"
	"github.com/cbrew/quadruplet/go/interp"
)

// contentTags are interp's: the tags of words that introduce a referent.
var contentTags = map[string]bool{
	"NN": true, "NNS": true, "NNP": true, "NNPS": true, "PRP": true, "PRP$": true, "WP": true, "WP$": true,
	"CD": true, "FW": true, "UH": true, "ADD": true, "GW": true, "AFX": true,
	"VB": true, "VBD": true, "VBG": true, "VBN": true, "VBP": true, "VBZ": true, "MD": true,
	"JJ": true, "JJR": true, "JJS": true, "RB": true, "RBR": true, "RBS": true, "WRB": true,
}

var punctuation = map[string]bool{
	",": true, ".": true, ":": true, "``": true, "''": true, `"`: true, "'": true,
	"-LRB-": true, "-RRB-": true, "-LSB-": true, "-RSB-": true, "HYPH": true, "NFP": true,
	"Comma": true, "Period": true, "Colon": true, "LQuote": true, "RQuote": true, "Quote": true, "Apos": true,
	"LRB": true, "RRB": true, "LSB": true, "RSB": true,
}

// markerBottoms are the categories of phrases headed by a function word
// that names their relation to the phrase above.
var markerBottoms = map[string]bool{"PP": true, "WHPP": true, "SBAR": true}

var clauses = map[string]bool{"S": true, "SBAR": true, "VP": true, "SQ": true, "SINV": true, "SBARQ": true}

// relationClass is the relation interp.Flat would name between a phrase of
// category parent (the bottom of its chain) and its daughter d, as far as
// the daughter's symbol tells: a word's tag or a phrase's chain. conj says
// a conjunction came before it among the daughters.
func relationClass(parent, d string, word, conj bool) string {
	if word {
		tag := fr.Tag(d)
		if tag == "PRPS" {
			tag = "PRP$"
		}
		if tag == "WPS" {
			tag = "WP$"
		}
		switch {
		case punctuation[tag]:
			return "."
		case tag == "CC":
			return "cc"
		case !contentTags[tag]:
			return "w:" + tag // a determiner or particle: an atom on the phrase's referent
		case conj:
			return "conj"
		case tag == "PRP$" || tag == "WP$":
			return "poss"
		case tag == "CD":
			return "num"
		case (parent == "NP" || parent == "NML" || parent == "NX") && strings.HasPrefix(tag, "NN"):
			return "nn"
		}
		return "mod"
	}
	chain := fr.Chain(d)
	top, bottom := chain[0], chain[len(chain)-1]
	switch {
	case markerBottoms[bottom]:
		return "M" // named by its preposition or complementizer
	case conj:
		return "conj"
	case top == "QP":
		return "num"
	case parent == "VP" && top == "NP":
		return "obj"
	case (parent == "NP" || parent == "NML" || parent == "NX") && (top == "NML" || top == "NX"):
		return "nn"
	case clauses[top]:
		return "comp"
	}
	return "mod"
}

// meaningKey is what a rule contributes to the flat meaning of the phrase it
// builds, as far as the rule tells: the head daughter's position and
// category, and each other daughter's relation class; an auxiliary head is
// marked, since the phrase is then about its verb phrase's event.
func meaningKey(r cfg.Rule, head int, isWord func(string) bool) string {
	chain := fr.Chain(r.LHS)
	parent := chain[len(chain)-1]
	parts := make([]string, len(r.RHS))
	conj := false
	hasVP := slices.ContainsFunc(r.RHS, func(d string) bool { return !isWord(d) && fr.Chain(d)[0] == "VP" })
	for i, d := range r.RHS {
		word := isWord(d)
		if i == head {
			cat := d
			if !word {
				cat = strings.Join(fr.Chain(d), "x")
			}
			parts[i] = "H:" + cat
			if word && (fr.Tag(d) == "MD" || fr.IsVerbTag(d)) && hasVP {
				parts[i] = "aux:" + cat
			}
			continue
		}
		parts[i] = relationClass(parent, d, word, conj)
		if parts[i] == "cc" {
			conj = true
		}
	}
	return strings.Join(parts, " ")
}

// owners of an auxiliary item's choice: the kind of rule it belongs to
const (
	ownNone = iota
	ownVP
	ownOther
)

// classifier classes each choice by region (outside any verb; a verb
// phrase's own rule; inside verbs' dependents) and by what the meaning sees
// of it, for every forest of a grammar.
type classifier struct {
	g       *cfg.Grammar
	keys    []string  // rule -> its meaning key
	lexical []bool    // rule -> it is a lexical verb phrase's
	isVP    []bool    // symbol -> a verb phrase's, not auxiliary
	weight  []float64 // rule -> its weight, or nil
}

func newClassifier(g *cfg.Grammar, weight []float64) *classifier {
	parents := map[string]bool{}
	for _, r := range g.Rules {
		parents[r.LHS] = true
	}
	isWord := func(s string) bool { return !parents[s] }
	keys := make([]string, len(g.Rules))
	lexical := make([]bool, len(g.Rules))
	for i, r := range g.Rules {
		keys[i] = meaningKey(r, interp.RuleHead(r, isWord), isWord)
		lexical[i] = fr.LexicalVP(r.LHS, r.RHS)
	}
	isVP := make([]bool, len(g.Names))
	for s, name := range g.Names {
		isVP[s] = !g.Aux[s] && fr.IsVP(name)
	}
	return &classifier{g, keys, lexical, isVP, weight}
}

// context is the classifier as a cfg.Context for one forest: the meaning
// key of a hyperedge needs the span its top step gives the first daughters,
// which is the forest's to know.
func (cl *classifier) context(f *cfg.Forest) cfg.Context {
	g, keys, lexical, isVP, weight := cl.g, cl.keys, cl.lexical, cl.isVP, cl.weight
	c := cfg.Context{
		Start: 0,
		// state: depth (0: no lexical verb phrase above; 1: one or more)
		// times 4, plus, for an auxiliary item, the owner of its rule
		Next: func(s int, item cfg.Item, e cfg.Hyperedge, child cfg.Item) int {
			depth, own := s/4, s%4
			isLexical := false
			if !g.Aux[item.Sym] {
				r := g.Steps[e.Step].Rule
				isLexical = lexical[r]
				own = ownOther
				if isVP[item.Sym] {
					own = ownVP
				}
			}
			if g.Aux[child.Sym] {
				return depth*4 + own
			}
			if isLexical {
				depth = 1
			}
			return depth * 4
		},
		Groups: func(item cfg.Item, e cfg.Hyperedge) []string {
			if g.Aux[item.Sym] {
				return nil
			}
			if e.Step < 0 {
				return []string{"words", "words"}
			}
			r := g.Steps[e.Step].Rule
			left := f.Items[e.Left]
			return []string{fmt.Sprintf("%s %d-%d", keys[r], left.L, left.R), g.Rules[r].String()}
		},
		Class: func(s int, item cfg.Item, level int) string {
			depth, own := s/4, s%4
			var region string
			switch {
			case !g.Aux[item.Sym] && isVP[item.Sym], g.Aux[item.Sym] && own == ownVP:
				region = "verb phrases' own rules"
			case depth == 0:
				region = "outside any verb"
			default:
				region = "inside verbs' dependents"
			}
			what := "spans"
			if !g.Aux[item.Sym] {
				what = []string{"meaning", "don't care", "spans"}[level]
			}
			return region + "\t" + what
		},
	}
	if weight != nil {
		c.Weight = func(_ cfg.Item, e cfg.Hyperedge) float64 {
			if e.Step < 0 || g.Steps[e.Step].Rule < 0 {
				return 1
			}
			return weight[g.Steps[e.Step].Rule]
		}
	}
	return c
}

// exact is the true don't-care share of a forest small enough to
// enumerate: the entropy of its trees less the entropy of their meanings,
// each tree as likely as any other or as its rules' weights make it.
func exact(f *cfg.Forest, weight map[string]float64) (trees, meanings, hTrees, hMeanings float64) {
	byMeaning := map[string]float64{}
	var z, zlogz float64
	for t := range f.Trees() {
		w := 1.0
		if weight != nil {
			w = treeWeight(t, weight)
		}
		trees++
		z += w
		zlogz += w * math.Log10(w)
		atoms := interp.Flat(interp.FromTree(plain(t)))
		var b strings.Builder
		for _, a := range atoms {
			b.WriteString(a.String())
			b.WriteByte(' ')
		}
		byMeaning[b.String()] += w
	}
	meanings = float64(len(byMeaning))
	hTrees = math.Log10(z) - zlogz/z
	for _, m := range byMeaning {
		p := m / z
		hMeanings -= p * math.Log10(p)
	}
	return
}

func treeWeight(t *cfg.Tree, weight map[string]float64) float64 {
	if t.Children == nil {
		return 1
	}
	r := cfg.Rule{LHS: t.Label}
	w := 1.0
	for _, k := range t.Children {
		r.RHS = append(r.RHS, k.Label)
		w *= treeWeight(k, weight)
	}
	return w * weight[r.String()]
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

func main() {
	countsFile := flag.String("counts", "", "the grammar with counts, as tools/masc/treebank.py writes it")
	annotatedFile := flag.String("annotated", "", "the corpus's trees, as tools/masc/treebank.py writes them")
	n := flag.Int("n", 300, "how many test sentences to sample")
	minWords := flag.Int("min", 5, "the fewest words a sampled sentence has")
	maxWords := flag.Int("max", 25, "the most words a sampled sentence has")
	seed := flag.Uint64("seed", 1, "the sample's random seed")
	pcfg := flag.Bool("pcfg", false, "weigh each tree by its rules' relative frequencies in the treebank, not every tree alike")
	maxExact := flag.Int64("exact", 20000, "also compute the true don't-care share, by enumeration, for forests of at most this many trees; 0 for none")
	flag.Parse()
	if *countsFile == "" || *annotatedFile == "" {
		flag.Usage()
		os.Exit(2)
	}
	fail := func(err error) {
		fmt.Fprintln(os.Stderr, "dontcare:", err)
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

	// the grammar as it is, with each word given only its gold tag
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
	var weight []float64
	var byRule map[string]float64
	if *pcfg {
		byRule = fr.Probabilities(rules, counts)
		weight = make([]float64, len(g.Rules))
		for i, r := range g.Rules {
			weight[i] = byRule[r.String()]
		}
	}
	cl := newClassifier(g, weight)

	totals := map[string]float64{}
	var sumH float64
	parsed := 0
	// the exact check
	var checked int
	var exTrees, exMeanings, exH, exHM, exLocal, exLocalDC float64
	limit := big.NewInt(*maxExact)
	for _, s := range sample {
		f := g.Parse(s.Spell())
		if len(f.Goals) == 0 {
			continue
		}
		parsed++
		parts := f.Entropy(cl.context(f))
		h, dc := 0.0, 0.0
		for k, v := range parts {
			totals[k] += v
			h += v
			if strings.HasSuffix(k, "don't care") {
				dc += v
			}
		}
		sumH += h
		if *maxExact > 0 && f.Count().Cmp(limit) <= 0 {
			trees, meanings, hT, hM := exact(f, byRule)
			checked++
			exTrees += math.Log10(trees)
			exMeanings += math.Log10(meanings)
			exH += hT
			exHM += hM
			exLocal += h
			exLocalDC += dc
		}
	}
	if *pcfg {
		fmt.Printf("%d of %d test sentences of %d to %d words parsed, with gold tags; trees weighted by the treebank's rule probabilities; entropy: mean %.2f decimal digits\n\n",
			parsed, len(sample), *minWords, *maxWords, sumH/float64(parsed))
	} else {
		fmt.Printf("%d of %d test sentences of %d to %d words parsed, with gold tags; every tree as likely as any other; log10 trees: mean %.2f\n\n",
			parsed, len(sample), *minWords, *maxWords, sumH/float64(parsed))
	}
	fmt.Println("Each part as a mean over the sentences, in decimal digits, and as a share of all the sentences' entropy. Meaning: the choice of what the phrase contributes to the flat meaning; don't care: the choice of rule given that; spans: the choice of the daughters' spans within the rule.")
	regions := []string{"outside any verb", "verb phrases' own rules", "inside verbs' dependents"}
	whats := []string{"meaning", "don't care", "spans"}
	cell := func(v float64) string { return fmt.Sprintf("%.2f (%.0f%%)", v/float64(parsed), 100*v/sumH) }
	fmt.Println("where\tmeaning\tdon't care\tspans\ttotal")
	colTotals := map[string]float64{}
	for _, r := range regions {
		fmt.Print(r)
		t := 0.0
		for _, w := range whats {
			v := totals[r+"\t"+w]
			colTotals[w] += v
			t += v
			fmt.Print("\t" + cell(v))
		}
		fmt.Println("\t" + cell(t))
	}
	fmt.Print("all")
	for _, w := range whats {
		fmt.Print("\t" + cell(colTotals[w]))
	}
	fmt.Println("\t" + cell(sumH))
	if *maxExact > 0 {
		fmt.Printf("\nThe true don't-care share, by enumeration, on the %d sentences whose forests have at most %d trees, against the local approximation on the same sentences (means per sentence, decimal digits):\n", checked, *maxExact)
		fmt.Println("\tlog10 trees\tlog10 meanings\tentropy of trees\tentropy of meanings\tdon't care: exact\tdon't care: local")
		k := float64(checked)
		fmt.Printf("mean\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f (%.0f%%)\t%.2f (%.0f%%)\n",
			exTrees/k, exMeanings/k, exH/k, exHM/k, (exH-exHM)/k, 100*(exH-exHM)/exH, exLocalDC/k, 100*exLocalDC/exLocal)
	}
}
