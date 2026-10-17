// Command idforest builds the ID forests of verbs' projections (package
// idlp) and measures them against grammars that keep the orders the
// treebank attests. Four grammars, read off the counts treebank (package
// counts):
//
//   - counts: the counts grammar as it is (verb projections in layers);
//   - flat, attested orders: verb projections flat, each rule a
//     projection's daughters in an order seen in the treebank (option 1:
//     linear precedence as attested);
//   - ID, modifier multiset attested: a projection licensed by a multiset
//     of daughters seen together, in any order (no LP);
//   - ID, modifiers free: a frame (the verb and its complements) seen, in
//     any order, with any modifiers anywhere.
//
// Each is measured on the held-out sample of the other commands: the
// number of trees, with each word given its own type (an oracle), or the
// types seen with its tag; and whether the sentence's own tree is among
// them.
//
//	idforest -annotated annotated.jsonl [-n 300] [-min 5] [-max 25]
package main

import (
	"bufio"
	"encoding/json"
	"flag"
	"fmt"
	"os"
	"sort"
	"strconv"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
	"github.com/cbrew/quadruplet/go/explore/counts"
	fr "github.com/cbrew/quadruplet/go/explore/frames"
	"github.com/cbrew/quadruplet/go/explore/idlp"
	"github.com/cbrew/quadruplet/go/interp"
)

type sentence struct {
	id    string
	words []string
	tags  []string
	tree  *cfg.Tree
}

func main() {
	annotatedFile := flag.String("annotated", "", "the corpus's trees, as tools/masc/treebank.py writes them")
	n := flag.Int("n", 300, "how many test sentences to sample")
	minWords := flag.Int("min", 5, "the fewest words a sampled sentence has")
	maxWords := flag.Int("max", 25, "the most words a sampled sentence has")
	seed := flag.Uint64("seed", 1, "the sample's random seed")
	trainOnly := flag.Bool("train", false, "read every grammar off the training documents only, so that the held-out trees test its coverage")
	flag.Parse()
	if *annotatedFile == "" {
		flag.Usage()
		os.Exit(2)
	}
	fail := func(err error) {
		fmt.Fprintln(os.Stderr, "idforest:", err)
		os.Exit(1)
	}
	plain, err := convertAll(*annotatedFile, counts.Options{})
	if err != nil {
		fail(err)
	}
	flat, err := convertAll(*annotatedFile, counts.Options{FlatVerbs: true})
	if err != nil {
		fail(err)
	}
	orig, err := fr.ReadSentences(*annotatedFile)
	if err != nil {
		fail(err)
	}
	train, test := fr.Split(orig)
	var ids []string
	for _, s := range fr.Sample(test, *n, *minWords, *maxWords, *seed) {
		ids = append(ids, s.ID)
	}
	byTag := map[string]map[string]bool{} // tag -> tag_types, from training
	for _, s := range train {
		if x, ok := plain[s.ID]; ok {
			for _, t := range x.tags {
				pos, _, _ := strings.Cut(t, "_")
				if byTag[pos] == nil {
					byTag[pos] = map[string]bool{}
				}
				byTag[pos][t] = true
			}
		}
	}

	// the documents the grammars are read off: all, or the training ones
	inTrain := map[string]bool{}
	for _, s := range train {
		inTrain[s.ID] = true
	}
	source := func(all map[string]sentence) map[string]sentence {
		if !*trainOnly {
			return all
		}
		out := map[string]sentence{}
		for id, s := range all {
			if inTrain[id] {
				out[id] = s
			}
		}
		return out
	}
	var flatTrees []*cfg.Tree
	for _, s := range source(flat) {
		flatTrees = append(flatTrees, s.tree)
	}
	attested := idlp.Compile(flatTrees, false)
	free := idlp.Compile(flatTrees, true)
	type grammar struct {
		name  string
		sents map[string]sentence
		rules []cfg.Rule
		own   func(t *cfg.Tree) *cfg.Tree
	}
	identity := func(t *cfg.Tree) *cfg.Tree { return t }
	grammars := []grammar{
		{"counts, projections in layers", plain, rulesOf(source(plain)), identity},
		{"flat projections, attested orders (LP as attested)", flat, rulesOf(source(flat)), identity},
		{"ID, modifier multiset attested (no LP)", flat, attested.Rules, attested.Derivation},
		{"ID, modifiers free (no LP)", flat, free.Rules, free.Derivation},
	}
	fmt.Printf("ID rules: %d with the modifier multiset, compiled over %d states; %d frames with free modifiers, over %d states, %d modifier labels\n\n",
		len(attested.IDRules), attested.States(), len(free.IDRules), free.States(), len(free.Modifiers))
	from := "all the documents, the held-out ones included"
	if *trainOnly {
		from = "the training documents only"
	}
	fmt.Printf("%d held-out sentences of %d to %d words; grammars read off %s\n", len(ids), *minWords, *maxWords, from)
	fmt.Println("grammar\trules\tlexicon\tparsed\town tree among the parses\tlog10 trees per word, median\tlog10 trees, mean")
	for _, gr := range grammars {
		for _, lexName := range []string{"oracle types", "types seen with the tag"} {
			lex := map[string][]string{}
			spelled := map[string][]string{}
			for _, id := range ids {
				s := gr.sents[id]
				words := make([]string, len(s.words))
				for i := range s.words {
					if lexName == "oracle types" {
						words[i] = s.words[i] + "|" + s.tags[i] + "|" + strconv.Itoa(i)
						lex[words[i]] = []string{s.tags[i]}
					} else {
						pos, _, _ := strings.Cut(s.tags[i], "_")
						words[i] = pos + "|" + pos
						lex[words[i]] = keysOf(byTag[pos])
					}
				}
				spelled[id] = words
			}
			g, err := cfg.New(gr.rules, lex, []string{"Top"})
			if err != nil {
				fail(fmt.Errorf("%s: %v", gr.name, err))
			}
			var parsed, own int
			var perWord, logT []float64
			for _, id := range ids {
				words := spelled[id]
				f := g.Parse(words)
				if len(f.Goals) == 0 {
					continue
				}
				parsed++
				if f.Contains(respell(gr.own(gr.sents[id].tree), words)) {
					own++
				}
				lc := fr.Log10(f.Count())
				perWord = append(perWord, lc/float64(len(words)))
				logT = append(logT, lc)
			}
			fmt.Printf("%s\t%d\t%s\t%.1f%%\t%.1f%%\t%.2f\t%.2f\n", gr.name, len(gr.rules), lexName,
				fr.Pct(parsed, len(ids)), fr.Pct(own, len(ids)), fr.Median(perWord), fr.Mean(logT))
		}
	}
}

// convertAll reads annotated.jsonl and converts every tree.
func convertAll(file string, o counts.Options) (map[string]sentence, error) {
	f, err := os.Open(file)
	if err != nil {
		return nil, err
	}
	defer f.Close()
	out := map[string]sentence{}
	sc := bufio.NewScanner(f)
	sc.Buffer(make([]byte, 1<<20), 1<<26)
	for sc.Scan() {
		var rec struct {
			ID   string          `json:"id"`
			Tree json.RawMessage `json:"tree"`
		}
		if err := json.Unmarshal(sc.Bytes(), &rec); err != nil {
			return nil, err
		}
		node, err := interp.FromJSON(rec.Tree)
		if err != nil {
			return nil, fmt.Errorf("%s: %v", rec.ID, err)
		}
		t, err := counts.ConvertWith(node, o)
		if err != nil {
			return nil, fmt.Errorf("%s: %v", rec.ID, err)
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
		out[rec.ID] = s
	}
	return out, sc.Err()
}

// rulesOf is the rules of every tree.
func rulesOf(sents map[string]sentence) []cfg.Rule {
	seen := map[string]cfg.Rule{}
	var walk func(t *cfg.Tree)
	walk = func(t *cfg.Tree) {
		if t.Words != nil {
			return
		}
		r := cfg.Rule{LHS: t.Label}
		for _, k := range t.Children {
			r.RHS = append(r.RHS, k.Label)
			walk(k)
		}
		seen[r.String()] = r
	}
	for _, s := range sents {
		walk(s.tree)
	}
	var keys []string
	for k := range seen {
		keys = append(keys, k)
	}
	sort.Strings(keys)
	out := make([]cfg.Rule, len(keys))
	for i, k := range keys {
		out[i] = seen[k]
	}
	return out
}

func keysOf(m map[string]bool) []string {
	var out []string
	for k := range m {
		out = append(out, k)
	}
	sort.Strings(out)
	return out
}

// respell is a tree with its words spelled as the lexicon has them.
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
