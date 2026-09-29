// Command verbentropy locates the ambiguity of a treebank grammar relative to
// the verbs: how much of it lies in the verbs' own choices (the kind of verb
// phrase rule, the complement frame, the rest of the rule), how much in the
// choices inside their dependents, and how much outside any verb.
//
// Every tree of a sentence's forest is taken as likely as any other, so the
// entropy of the trees is log10 of their number, and cfg.Forest.Entropy
// splits it exactly into the expected entropies of the local choices a tree
// makes. Here a choice is classed by where it is and what it decides.
//
// Where: by depth, the number of lexical verb phrases (package frames) above
// the choice. At depth 0 are the choices outside any verb and those of the
// top-layer verbs' phrases (their auxiliaries, coordinations, adjunctions and
// own rules); at depth 1 those inside the top-layer verbs' dependents, and of
// the phrases of the verbs they contain; and so on. A choice is a verb
// phrase's if it is made at an item whose symbol is, or ends in, a VP.
//
// What: a verb phrase's choice of rule is split by the chain rule into its
// kind (lexical, auxiliary, coordination, adjunction, other), then the
// complement frame of a lexical rule (the core grain of package frames), then
// the rest of the rule given those (the modifiers and PPs inside the verb
// phrase, their order); any other phrase's choice is its rule. Last come the
// choices of the daughters' spans given the rule, which binarization spreads
// over auxiliary items; they are charged to the rule's phrase.
//
// With -lexicon GRAIN, the grammar is the one package frames renames at that
// grain, with the frame lexicon cmd/framelex learns (frames seen once or more
// with lemmas seen -lemma times or more), so the decomposition is of the
// trees the lexicon allows.
//
//	verbentropy -counts counts.tsv -annotated annotated.jsonl -lemmas lemmas.tsv [-n 300] [-min 5] [-max 25] [-lexicon core]
package main

import (
	"flag"
	"fmt"
	"math"
	"os"
	"slices"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
	fr "github.com/cbrew/quadruplet/go/frames"
)

// ruleInfo is what a verb context needs to know of a rule.
type ruleInfo struct {
	vp      bool   // its parent is, or ends in, a VP
	lexical bool   // a lexical verb phrase's
	kind    string // for a verb phrase's: lexical, auxiliary, coordination, adjunction, other
	frame   string // for a lexical verb phrase's: its core complement frame
}

func info(r cfg.Rule) ruleInfo {
	if !fr.IsVP(r.LHS) {
		return ruleInfo{}
	}
	if fr.LexicalVP(r.LHS, r.RHS) {
		return ruleInfo{true, true, "lexical", fr.Frame(r.RHS, "core")}
	}
	vps, verbs, conj := 0, 0, false
	for _, d := range r.RHS {
		switch t := fr.Tag(d); {
		case fr.Chain(d)[0] == "VP":
			vps++
		case fr.IsVerbTag(d) || t == "MD" || t == "TO":
			verbs++
		case t == "CC" || t == "CONJP":
			conj = true
		}
	}
	kind := "other"
	switch {
	case vps >= 2 || vps == 1 && conj:
		kind = "coordination"
	case vps == 1 && verbs > 0:
		kind = "auxiliary"
	case vps == 1:
		kind = "adjunction"
	}
	return ruleInfo{true, false, kind, ""}
}

// the owners of auxiliary items: whose rule their choice of spans is
const (
	ownNone = iota
	ownLexical
	ownVP
	ownOther
)

const maxDepth = 3

// verbContext classes a forest's choices by depth below lexical verb phrases
// and by what they decide, as the package comment says. A state is depth*4
// plus, for an auxiliary item, the kind of rule it belongs to.
func verbContext(g *cfg.Grammar) cfg.Context {
	infos := make([]ruleInfo, len(g.Rules))
	for i, r := range g.Rules {
		infos[i] = info(r)
	}
	isVP := make([]bool, len(g.Names))
	for s, name := range g.Names {
		isVP[s] = !g.Aux[s] && fr.IsVP(name)
	}
	owner := func(s int, item cfg.Item, e cfg.Hyperedge) int {
		if g.Aux[item.Sym] {
			return s % 4
		}
		in := infos[g.Steps[e.Step].Rule]
		switch {
		case in.lexical:
			return ownLexical
		case in.vp:
			return ownVP
		}
		return ownOther
	}
	return cfg.Context{
		Start: 0,
		Next: func(s int, item cfg.Item, e cfg.Hyperedge, child cfg.Item) int {
			d, own := s/4, owner(s, item, e)
			if g.Aux[child.Sym] {
				return d*4 + own
			}
			if own == ownLexical {
				d = min(d+1, maxDepth)
			}
			return d * 4
		},
		Groups: func(item cfg.Item, e cfg.Hyperedge) []string {
			if g.Aux[item.Sym] {
				return nil
			}
			var in ruleInfo
			rule := "words"
			if e.Step >= 0 {
				r := g.Steps[e.Step].Rule
				in, rule = infos[r], g.Rules[r].String()
			}
			if isVP[item.Sym] {
				return []string{in.kind, in.frame, rule}
			}
			return []string{rule}
		},
		Class: func(s int, item cfg.Item, level int) string {
			d := s / 4
			verb := isVP[item.Sym]
			what := "spans"
			if g.Aux[item.Sym] {
				verb = s%4 == ownLexical || s%4 == ownVP
			} else if verb {
				what = []string{"kind", "frame", "rest", "spans"}[level]
			} else {
				what = []string{"rule", "spans"}[level]
			}
			return fmt.Sprintf("%d\t%t\t%s", d, verb, what)
		},
	}
}

func main() {
	countsFile := flag.String("counts", "", "the grammar with counts, as tools/masc/treebank.py writes it")
	annotatedFile := flag.String("annotated", "", "the corpus's trees, as tools/masc/treebank.py writes them")
	lemmasFile := flag.String("lemmas", "", "verb forms' lemmas, as tools/masc/verbframes.py writes them")
	n := flag.Int("n", 300, "how many test sentences to sample")
	minWords := flag.Int("min", 5, "the fewest words a sampled sentence has")
	maxWords := flag.Int("max", 25, "the most words a sampled sentence has")
	lexicon := flag.String("lexicon", "", "filter the trees by a frame lexicon at this grain: core, pp or rule")
	minLemma := flag.Int("lemma", 5, "restrict a lemma's frames only if it was seen this often in training")
	seed := flag.Uint64("seed", 1, "the sample's random seed")
	flag.Parse()
	if *countsFile == "" || *annotatedFile == "" || *lemmasFile == "" {
		flag.Usage()
		os.Exit(2)
	}
	fail := func(err error) {
		fmt.Fprintln(os.Stderr, "verbentropy:", err)
		os.Exit(1)
	}
	rules, err := fr.ReadRules(*countsFile)
	if err != nil {
		fail(err)
	}
	lemmas, err := fr.ReadLemmas(*lemmasFile)
	if err != nil {
		fail(err)
	}
	sents, err := fr.ReadSentences(*annotatedFile)
	if err != nil {
		fail(err)
	}
	train, test := fr.Split(sents)
	sample := fr.Sample(test, *n, *minWords, *maxWords, *seed)

	grain := *lexicon
	if grain == "" {
		grain = "core" // renamed but unfiltered: the same trees as the grammar
	}
	rn := fr.Rename(rules, grain)
	allow := func(string) map[string]bool { return nil }
	if *lexicon != "" {
		lx := fr.Learn(train, lemmas, grain)
		allow = func(l string) map[string]bool { return lx.Allowed(l, *minLemma, 1) }
	}
	g, err := rn.Grammar(sample, lemmas, allow)
	if err != nil {
		fail(err)
	}
	ctx := verbContext(g)

	// where the words other than verbs are: how many lexical verb phrases
	// lie above them, in the trees on average and in the sentence's own tree
	tags := map[string]bool{}
	for _, s := range sample {
		for _, t := range s.Tags {
			tags[t] = true
		}
	}
	isWord := make([]bool, len(g.Names))
	for sym, name := range g.Names {
		isWord[sym] = !g.Aux[sym] && tags[fr.Tag(name)] && !fr.IsVerbTag(name)
	}
	wordDepth := func(s int, it cfg.Item) string {
		if it.R != it.L+1 || !isWord[it.Sym] {
			return ""
		}
		return fmt.Sprint(s / 4)
	}
	uniformWords, goldWords := map[string]float64{}, map[string]float64{}
	goldDepths := func(t *cfg.Tree) {
		var walk func(t *cfg.Tree, d int)
		walk = func(t *cfg.Tree, d int) {
			if t.Words != nil {
				if !fr.IsVerbTag(t.Label) {
					goldWords[fmt.Sprint(d)]++
				}
				return
			}
			rhs := make([]string, len(t.Children))
			for i, c := range t.Children {
				rhs[i] = c.Label
			}
			if fr.LexicalVP(t.Label, rhs) {
				d = min(d+1, maxDepth)
			}
			for _, c := range t.Children {
				walk(c, d)
			}
		}
		walk(t, 0)
	}

	totals := map[string]float64{} // class -> summed over sentences
	var sumLog, worst float64
	var perWord []float64
	parsed := 0
	for _, s := range sample {
		f := g.Parse(s.Spell())
		if len(f.Goals) == 0 {
			continue
		}
		parsed++
		lc := fr.Log10(f.Count())
		for k, v := range f.Occupancy(ctx, wordDepth) {
			if k != "" {
				uniformWords[k] += v
			}
		}
		goldDepths(s.Tree)
		parts := f.Entropy(ctx)
		sum := 0.0
		for k, v := range parts {
			totals[k] += v
			sum += v
		}
		worst = max(worst, math.Abs(sum-lc))
		sumLog += lc
		perWord = append(perWord, lc/float64(len(s.Words)))
	}
	if *lexicon != "" {
		fmt.Printf("the trees a frame lexicon at the %s grain allows (lemmas seen %d+ times in training)\n", grain, *minLemma)
	}
	fmt.Printf("%d of %d test sentences of %d to %d words parsed, with gold tags; log10 trees: mean %.2f, per word median %.2f\n",
		parsed, len(sample), *minWords, *maxWords, sumLog/float64(parsed), fr.Median(perWord))
	fmt.Printf("the parts sum to log10 of the count to within %.1g\n\n", worst)
	fmt.Println("Words other than verbs, by how many lexical verb phrases lie above them: in the trees, each as likely as any other, and in the sentences' own trees")
	fmt.Println("lexical verb phrases above\tall trees\town trees")
	sumU, sumG := 0.0, 0.0
	for d := 0; d <= maxDepth; d++ {
		sumU += uniformWords[fmt.Sprint(d)]
		sumG += goldWords[fmt.Sprint(d)]
	}
	for d := 0; d <= maxDepth; d++ {
		k := fmt.Sprint(d)
		fmt.Printf("%d\t%.1f%%\t%.1f%%\n", d, 100*uniformWords[k]/sumU, 100*goldWords[k]/sumG)
	}
	fmt.Println()
	fmt.Println("Each part as a mean over the sentences, in decimal digits (log10 trees), and as a share of all the sentences' log10 trees:")

	type row struct {
		depth int
		verb  bool
	}
	rows := map[row]map[string]float64{}
	for k, v := range totals {
		var r row
		var what string
		fmt.Sscanf(strings.ReplaceAll(k, "\t", " "), "%d %t %s", &r.depth, &r.verb, &what)
		if rows[r] == nil {
			rows[r] = map[string]float64{}
		}
		rows[r][what] += v
	}
	keys := make([]row, 0, len(rows))
	for r := range rows {
		keys = append(keys, r)
	}
	slices.SortFunc(keys, func(a, b row) int {
		if a.depth != b.depth {
			return a.depth - b.depth
		}
		if a.verb == b.verb {
			return 0
		}
		if a.verb {
			return -1
		}
		return 1
	})
	cols := []string{"kind", "frame", "rest", "rule", "spans"}
	fmt.Println("where\tkind of VP rule\tcomplement frame\trest of VP rule\tother phrases' rules\tdaughters' spans\ttotal")
	cell := func(v float64) string {
		return fmt.Sprintf("%.2f (%.0f%%)", v/float64(parsed), 100*v/sumLog)
	}
	colTotals := map[string]float64{}
	for _, r := range keys {
		name := fmt.Sprintf("inside the dependents of verbs at depth %d", r.depth)
		switch {
		case r.depth == 0 && r.verb:
			name = "top-layer verbs' phrases"
		case r.depth == 0:
			name = "outside any verb"
		case r.verb:
			name = fmt.Sprintf("verbs' phrases at depth %d", r.depth)
		}
		if r.depth == maxDepth {
			name += "+"
		}
		fmt.Print(name)
		t := 0.0
		for _, c := range cols {
			v := rows[r][c]
			colTotals[c] += v
			t += v
			if v == 0 {
				fmt.Print("\t-")
			} else {
				fmt.Print("\t" + cell(v))
			}
		}
		fmt.Println("\t" + cell(t))
	}
	fmt.Print("all")
	for _, c := range cols {
		fmt.Print("\t" + cell(colTotals[c]))
	}
	fmt.Println("\t" + cell(sumLog))
}
