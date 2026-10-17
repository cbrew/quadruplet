// Command layers splits the entropy of each sentence's forest by verb
// layers (frames.VerbLayers): the skeleton outside the top verb nodes, the
// top verb nodes' expansions, what lies between them and the bottom verb
// nodes, the bottom verb nodes' expansions, and the verb-free trees under
// those. It takes the same input and sample as cmd/verbentropy, so the two
// decompositions are of the same forests: the treebank grammar and the
// corpus's trees as tools/masc/treebank.py writes them, the lemmas as
// tools/masc/verbframes.py writes them, and, with -lexicon, the trees a
// frame lexicon allows.
//
// With -pcfg the trees are weighted by the treebank's rule probabilities
// (each rule's count over its left-hand side's) instead of equally, and
// the entropy is that of the PCFG's distribution over the forest's trees.
//
//	layers -counts counts.tsv -annotated annotated.jsonl -lemmas lemmas.tsv [-n 300] [-min 5] [-max 25] [-lexicon core] [-pcfg] [-each]
package main

import (
	"flag"
	"fmt"
	"math"
	"os"

	fr "github.com/cbrew/quadruplet/go/explore/frames"
)

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
	pcfg := flag.Bool("pcfg", false, "weight the trees by the treebank's rule probabilities instead of equally")
	each := flag.Bool("each", false, "print a line per sentence")
	flag.Parse()
	if *countsFile == "" || *annotatedFile == "" || *lemmasFile == "" {
		flag.Usage()
		os.Exit(2)
	}
	fail := func(err error) {
		fmt.Fprintln(os.Stderr, "layers:", err)
		os.Exit(1)
	}
	rules, counts, err := fr.ReadRuleCounts(*countsFile)
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
	verbRule := fr.LexicalRules(g)
	var weights []float64
	if *pcfg {
		// the renamed rules' probabilities are the rules'; rn.Rules keeps
		// the order of rules
		probs := fr.Probabilities(rules, counts)
		renamed := map[string]float64{}
		for i, r := range rn.Rules {
			renamed[r.String()] = probs[rules[i].String()]
		}
		weights = make([]float64, len(g.Rules))
		for i, r := range g.Rules {
			p, ok := renamed[r.String()]
			if !ok {
				fail(fmt.Errorf("no count for the rule %s", r))
			}
			weights[i] = p
		}
	}

	names := []string{"outside", "top", "between", "bottom", "inside"}
	terms := func(l fr.Layers) []float64 {
		return []float64{l.Outside, l.Top, l.Between, l.Bottom, l.Inside}
	}
	var sums [5]float64
	var sumTotal, sumCount float64
	perWord := make([][]float64, 6)
	shares := make([][]float64, 5)
	var verbs, tops, bottoms, ownVerbs, ownTops, ownBottoms, verbFree float64
	parsed, overflowed := 0, 0
	byVerbs := map[int][]float64{} // verb nodes in the own tree -> per-sentence shares of the verbs' own terms
	if *each {
		fmt.Println("id\twords\town verbs\ttop\tbottom\tE verbs\tE top\tE bottom\tlog10 trees\tentropy\toutside\ttop\tbetween\tbottom\tinside")
	}
	for _, s := range sample {
		f := g.Parse(s.Spell())
		if len(f.Goals) == 0 {
			continue
		}
		l := fr.VerbLayers(f, verbRule, weights)
		if math.IsInf(l.Total, 0) {
			overflowed++
			continue
		}
		parsed++
		v, top, bottom := fr.VerbNodes(rn.Own(s, false))
		ts := terms(l)
		w := float64(len(s.Words))
		for i, x := range ts {
			sums[i] += x
			perWord[i] = append(perWord[i], x/w)
			shares[i] = append(shares[i], x/l.Total)
		}
		sumTotal += l.Total
		sumCount += l.Count
		perWord[5] = append(perWord[5], l.Total/w)
		byVerbs[min(v, 4)] = append(byVerbs[min(v, 4)], (ts[1]+ts[3])/l.Total)
		verbs += l.Verbs
		tops += l.TopVerbs
		bottoms += l.BottomVerbs
		verbFree += l.VerbFree
		ownVerbs += float64(v)
		ownTops += float64(top)
		ownBottoms += float64(bottom)
		if *each {
			fmt.Printf("%s\t%d\t%d\t%d\t%d\t%.2f\t%.2f\t%.2f\t%.1f\t%.1f", s.ID, len(s.Words), v, top, bottom, l.Verbs, l.TopVerbs, l.BottomVerbs, l.Count, l.Total)
			for _, x := range ts {
				fmt.Printf("\t%.2f", x)
			}
			fmt.Println()
		}
	}
	if *each {
		fmt.Println()
	}
	if *lexicon != "" {
		fmt.Printf("the trees a frame lexicon at the %s grain allows (lemmas seen %d+ times in training)\n", grain, *minLemma)
	}
	fmt.Printf("%d of %d test sentences of %d to %d words parsed, with gold tags", parsed, len(sample), *minWords, *maxWords)
	if overflowed > 0 {
		fmt.Printf(" (%d skipped, too many trees for floating point)", overflowed)
	}
	if *pcfg {
		fmt.Printf("; trees weighted by the treebank's rule probabilities; log10 trees: mean %.2f; entropy, in decimal digits: mean %.2f, per word median %.2f\n\n", sumCount/float64(parsed), sumTotal/float64(parsed), fr.Median(perWord[5]))
	} else {
		fmt.Printf("; every tree as likely as any other; log10 trees: mean %.2f, per word median %.2f\n\n", sumTotal/float64(parsed), fr.Median(perWord[5]))
	}
	fmt.Println("Each stage's share of all the sentences' entropy, its median share per sentence, and its median decimal digits per word:")
	fmt.Println("stage\tshare of all\tmedian share\tmedian per word")
	for i, name := range names {
		fmt.Printf("%s\t%.1f%%\t%.1f%%\t%.3f\n", name, 100*sums[i]/sumTotal, 100*fr.Median(shares[i]), fr.Median(perWord[i]))
	}
	fmt.Println()
	k := float64(parsed)
	fmt.Println("Verb nodes per sentence: expected over the forest's trees, and in the sentence's own tree")
	fmt.Println("verb nodes\tall trees\town tree")
	fmt.Printf("all\t%.2f\t%.2f\n", verbs/k, ownVerbs/k)
	fmt.Printf("top\t%.2f\t%.2f\n", tops/k, ownTops/k)
	fmt.Printf("bottom\t%.2f\t%.2f\n", bottoms/k, ownBottoms/k)
	fmt.Printf("trees with no verb node\t%.1f%%\n\n", 100*verbFree/k)
	fmt.Println("The verb nodes' own expansions (top + bottom) as a share of the sentence's entropy, by the verb nodes in its own tree:")
	fmt.Println("own verb nodes\tsentences\tmedian share")
	for v := 0; v <= 4; v++ {
		if xs := byVerbs[v]; len(xs) > 0 {
			label := fmt.Sprint(v)
			if v == 4 {
				label = "4+"
			}
			fmt.Printf("%s\t%d\t%.1f%%\n", label, len(xs), 100*fr.Median(xs))
		}
	}
}
