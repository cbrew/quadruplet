// Command framelex measures how much a verb frame lexicon, read off the
// treebank, cuts the ambiguity of a treebank grammar: exactly, over every tree
// of each forest, not by sampling.
//
// A verb's frame is read off the rule of its verb phrase: the phrase's
// daughters other than the verb, at one of three grains:
//
//	core   the daughters that are mostly complements in the treebank (NP, the
//	       clauses S, SBAR, SQ, SBARQ, SINV and their collapsed chains, SxVP;
//	       ADJP, PRT, UCP), in order;
//	pp     those and PP, which is a complement (-CLR, -DTV, -PUT) a third of
//	       the time;
//	rule   every daughter: the verb phrase's rule itself.
//
// The lexicon is learned from nine tenths of the documents (as cmd/functions
// splits them, by a hash of the document's name) and tested on sentences of
// the other tenth. For each lemma seen at least -lemma times, it allows the
// frames seen with the lemma at least -frame times; other verbs are
// unrestricted.
//
// The filter is exact. Each verb of a lexical verb phrase rule (a rule whose
// parent is a VP, with a daughter tagged VB* and no VP daughter) is renamed by
// the rule's frame, VBD -> VBD~F7, and the lexicon gives a verb the renamed
// tags of its allowed frames besides its plain tag (for its uses as an
// auxiliary). A tree of the grammar corresponds to at most one tree of the
// renamed grammar, and to one exactly when every verb's frame is allowed; so
// the renamed grammar's count is the number of trees the lexicon allows, and
// the renamed tags over a verb in the forest are the frames it can have in
// some tree.
//
//	framelex -counts counts.tsv -annotated annotated.jsonl -lemmas lemmas.tsv [-n 300] [-min 5] [-max 25] [-lemma 5]
package main

import (
	"bufio"
	"encoding/json"
	"flag"
	"fmt"
	"hash/fnv"
	"math"
	"math/big"
	"math/rand/v2"
	"os"
	"slices"
	"strconv"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
)

type sentence struct {
	id    string
	words []string
	tags  []string
	tree  *cfg.Tree
}

// grains are the frames' grains, from coarsest to finest.
var grains = []string{"core", "pp", "rule"}

var core = map[string]bool{"NP": true, "S": true, "SBAR": true, "SQ": true, "SBARQ": true, "SINV": true,
	"ADJP": true, "PRT": true, "UCP": true}

// chain is a grammar symbol's categories, top first: SxVP -> [S VP], NPph -> [NP].
func chain(sym string) []string {
	parts := strings.Split(sym, "x")
	for i, p := range parts {
		parts[i] = strings.TrimSuffix(p, "ph")
	}
	return parts
}

func isVerbTag(sym string) bool {
	switch sym {
	case "VB", "VBD", "VBG", "VBN", "VBP", "VBZ":
		return true
	}
	return false
}

// lexicalVP says whether a rule is a lexical verb phrase's: a VP (at the
// bottom of its chain) with a verb daughter and no verb phrase daughter.
func lexicalVP(lhs string, rhs []string) bool {
	c := chain(lhs)
	if c[len(c)-1] != "VP" {
		return false
	}
	verb := false
	for _, d := range rhs {
		if chain(d)[0] == "VP" {
			return false
		}
		verb = verb || isVerbTag(d)
	}
	return verb
}

// frame is the rule's frame at a grain.
func frame(rhs []string, grain string) string {
	var parts []string
	for _, d := range rhs {
		if isVerbTag(d) {
			if grain == "rule" {
				parts = append(parts, "V")
			}
			continue
		}
		c := chain(d)
		if grain == "rule" || core[c[0]] || grain == "pp" && c[0] == "PP" {
			parts = append(parts, strings.Join(c, "x"))
		}
	}
	if len(parts) == 0 {
		return "0"
	}
	return strings.Join(parts, " ")
}

// lexicon is what the training documents say of verbs' frames at one grain.
type lexicon struct {
	tokens map[string]int            // lemma -> lexical verb tokens
	frames map[string]map[string]int // lemma -> frame -> tokens
}

// frameIDs numbers a grain's frames, for symbols: frame -> F0, F1, ...
type frameIDs map[string]string

func (ids frameIDs) id(f string) string {
	if s, ok := ids[f]; ok {
		return s
	}
	s := "F" + strconv.Itoa(len(ids))
	ids[f] = s
	return s
}

func main() {
	countsFile := flag.String("counts", "", "the grammar with counts, as tools/masc/treebank.py writes it")
	annotatedFile := flag.String("annotated", "", "the corpus's trees, as tools/masc/treebank.py writes them")
	lemmasFile := flag.String("lemmas", "", "verb forms' lemmas, as tools/masc/verbframes.py writes them")
	n := flag.Int("n", 300, "how many test sentences to sample")
	minWords := flag.Int("min", 5, "the fewest words a sampled sentence has")
	maxWords := flag.Int("max", 25, "the most words a sampled sentence has")
	minLemma := flag.Int("lemma", 5, "restrict a lemma's frames only if it was seen this often in training")
	seed := flag.Uint64("seed", 1, "the sample's random seed")
	flag.Parse()
	if *countsFile == "" || *annotatedFile == "" || *lemmasFile == "" {
		flag.Usage()
		os.Exit(2)
	}
	fail := func(err error) {
		fmt.Fprintln(os.Stderr, "framelex:", err)
		os.Exit(1)
	}
	rules, err := readRules(*countsFile)
	if err != nil {
		fail(err)
	}
	lemmas, err := readLemmas(*lemmasFile)
	if err != nil {
		fail(err)
	}
	lemmaOf := func(word, tag string) string {
		if l, ok := lemmas[word+"\t"+tag]; ok {
			return l
		}
		return strings.ToLower(word)
	}
	sents, err := readSentences(*annotatedFile)
	if err != nil {
		fail(err)
	}
	var train, test []sentence
	for _, s := range sents {
		doc, _, _ := strings.Cut(s.id, "#")
		h := fnv.New32a()
		h.Write([]byte(doc))
		if h.Sum32()%10 == 0 {
			test = append(test, s)
		} else {
			train = append(train, s)
		}
	}

	// each lexical verb in a tree, with its lemma and its phrase's rule
	type verbUse struct {
		leaf  *cfg.Tree
		lemma string
		rhs   []string
	}
	uses := func(t *cfg.Tree) []verbUse {
		var out []verbUse
		var walk func(t *cfg.Tree)
		walk = func(t *cfg.Tree) {
			if t.Words != nil {
				return
			}
			rhs := make([]string, len(t.Children))
			for i, c := range t.Children {
				rhs[i] = c.Label
			}
			if lexicalVP(t.Label, rhs) {
				for _, c := range t.Children {
					if c.Words != nil && isVerbTag(c.Label) {
						out = append(out, verbUse{c, lemmaOf(c.Words[0], c.Label), rhs})
					}
				}
			}
			for _, c := range t.Children {
				walk(c)
			}
		}
		walk(t)
		return out
	}
	lexicons := map[string]lexicon{}
	for _, g := range grains {
		lexicons[g] = lexicon{map[string]int{}, map[string]map[string]int{}}
	}
	for _, s := range train {
		for _, u := range uses(s.tree) {
			for _, g := range grains {
				lx := lexicons[g]
				lx.tokens[u.lemma]++
				if lx.frames[u.lemma] == nil {
					lx.frames[u.lemma] = map[string]int{}
				}
				lx.frames[u.lemma][frame(u.rhs, g)]++
			}
		}
	}

	// the sample: test sentences of the right length, with a verb
	rng := rand.New(rand.NewPCG(*seed, 0))
	var pool []sentence
	for _, s := range test {
		if len(s.words) >= *minWords && len(s.words) <= *maxWords && slices.ContainsFunc(s.tags, isVerbTag) {
			pool = append(pool, s)
		}
	}
	rng.Shuffle(len(pool), func(i, j int) { pool[i], pool[j] = pool[j], pool[i] })
	sample := pool[:min(*n, len(pool))]
	slices.SortFunc(sample, func(a, b sentence) int { return len(a.words) - len(b.words) })
	fmt.Printf("learned from %d sentences; tested on %d sentences of %d to %d words from the other documents, with gold tags\n",
		len(train), len(sample), *minWords, *maxWords)
	fmt.Printf("a lemma's frames are restricted if it was seen %d times or more in training\n\n", *minLemma)

	// how well each lexicon covers the verbs of every test sentence
	fmt.Println("grain\tframes in the grammar\tfewest tokens for a frame\ttest verbs restricted\tof which own frame allowed\tframes allowed a restricted verb, median")
	type setting struct {
		grain string
		least int // a frame is allowed if seen this often with the lemma
	}
	settings := []setting{{"core", 1}, {"core", 2}, {"pp", 1}, {"pp", 2}, {"rule", 1}, {"rule", 2}}
	allowed := func(st setting, lemma string) map[string]bool {
		lx := lexicons[st.grain]
		if lx.tokens[lemma] < *minLemma {
			return nil // unrestricted
		}
		out := map[string]bool{}
		for f, k := range lx.frames[lemma] {
			if k >= st.least {
				out[f] = true
			}
		}
		return out
	}
	grammarFrames := map[string]map[string]bool{} // grain -> frames of the grammar's lexical VP rules
	for _, g := range grains {
		grammarFrames[g] = map[string]bool{}
		for _, r := range rules {
			if lexicalVP(r.LHS, r.RHS) {
				grammarFrames[g][frame(r.RHS, g)] = true
			}
		}
	}
	for _, st := range settings {
		var restricted, own int
		var sizes []float64
		for _, s := range test {
			for _, u := range uses(s.tree) {
				a := allowed(st, u.lemma)
				if a == nil {
					continue
				}
				restricted++
				sizes = append(sizes, float64(len(a)))
				if a[frame(u.rhs, st.grain)] {
					own++
				}
			}
		}
		total := 0
		for _, s := range test {
			total += len(uses(s.tree))
		}
		fmt.Printf("%s\t%d\t%d\t%.1f%% of %d\t%.1f%%\t%.0f\n", st.grain, len(grammarFrames[st.grain]), st.least,
			pct(restricted, total), total, pct(own, restricted), median(sizes))
	}

	// the grammars: renamed for each grain; the lexicon for the sample's words
	type renamed struct {
		ids   frameIDs
		rules []cfg.Rule
		byTag map[string]map[string]bool // tag -> frames it heads in some rule
	}
	renamedFor := map[string]*renamed{}
	for _, g := range grains {
		rn := &renamed{frameIDs{}, nil, map[string]map[string]bool{}}
		for _, r := range rules {
			if !lexicalVP(r.LHS, r.RHS) {
				rn.rules = append(rn.rules, r)
				continue
			}
			f := frame(r.RHS, g)
			rhs := slices.Clone(r.RHS)
			for i, d := range rhs {
				if isVerbTag(d) {
					rhs[i] = d + "~" + rn.ids.id(f)
					if rn.byTag[d] == nil {
						rn.byTag[d] = map[string]bool{}
					}
					rn.byTag[d][f] = true
				}
			}
			rn.rules = append(rn.rules, cfg.Rule{LHS: r.LHS, RHS: rhs})
		}
		renamedFor[g] = rn
	}
	build := func(g string, allow func(lemma string) map[string]bool) *cfg.Grammar {
		rn := renamedFor[g]
		lex := map[string][]string{}
		for _, s := range sample {
			for i, w := range s.words {
				key, tag := w+"|"+s.tags[i], s.tags[i]
				if _, done := lex[key]; done {
					continue
				}
				entry := []string{tag}
				if isVerbTag(tag) {
					a := allow(lemmaOf(w, tag))
					for f := range rn.byTag[tag] {
						if a == nil || a[f] {
							entry = append(entry, tag+"~"+rn.ids[f])
						}
					}
				}
				lex[key] = entry
			}
		}
		gr, err := cfg.New(rn.rules, lex, []string{"Top"})
		if err != nil {
			fail(err)
		}
		return gr
	}
	// the sentence's own tree, its verbs renamed by their frames at a grain
	own := func(s sentence, g string, indexed bool) *cfg.Tree {
		rn := renamedFor[g]
		var copy func(t *cfg.Tree, rhs []string, lexical bool) *cfg.Tree
		i := 0
		copy = func(t *cfg.Tree, parentRHS []string, lexical bool) *cfg.Tree {
			out := &cfg.Tree{Label: t.Label}
			if t.Words != nil {
				if lexical && isVerbTag(t.Label) {
					out.Label = t.Label + "~" + rn.ids[frame(parentRHS, g)]
				}
				out.Words = []string{s.words[i] + "|" + s.tags[i]}
				if indexed {
					out.Words[0] += "|" + strconv.Itoa(i)
				}
				i++
				return out
			}
			rhs := make([]string, len(t.Children))
			for k, c := range t.Children {
				rhs[k] = c.Label
			}
			lex := lexicalVP(t.Label, rhs)
			for _, c := range t.Children {
				out.Children = append(out.Children, copy(c, rhs, lex))
			}
			return out
		}
		return copy(s.tree, nil, false)
	}

	// the grammar as it is, with gold tags: its counts must be the renamed
	// grammar's without a lexicon
	plain := func() *cfg.Grammar {
		lex := map[string][]string{}
		for _, s := range sample {
			for i, w := range s.words {
				lex[w+"|"+s.tags[i]] = []string{s.tags[i]}
			}
		}
		gr, err := cfg.New(rules, lex, []string{"Top"})
		if err != nil {
			fail(err)
		}
		return gr
	}()
	plainCount := map[string]*big.Int{}
	for _, s := range sample {
		f := plain.Parse(spell(s))
		plainCount[s.id] = f.Count()
	}

	// the oracle: each verb of the sentence allowed only the frame its own
	// tree gives it, or only its plain tag if it heads no lexical verb phrase;
	// its words are spelled with their positions, so that each is its own entry
	oracleGrammar := func(g string, s sentence) *cfg.Grammar {
		lex := map[string][]string{}
		var walk func(t *cfg.Tree)
		walk = func(t *cfg.Tree) {
			if t.Words != nil {
				lex[t.Words[0]] = []string{t.Label}
			}
			for _, c := range t.Children {
				walk(c)
			}
		}
		walk(own(s, g, true))
		gr, err := cfg.New(renamedFor[g].rules, lex, []string{"Top"})
		if err != nil {
			fail(err)
		}
		return gr
	}

	fmt.Println()
	fmt.Println("grain\tfewest tokens for a frame\tparsed\town tree among the parses\tlog10 trees per word, median\tlog10 trees cut, median (mean)\tframes a verb can have, median (mean)")
	var decomposition, perVerb strings.Builder
	for _, g := range grains {
		base := build(g, func(string) map[string]bool { return nil })
		baseline := map[string]float64{}
		report := func(name string, least int, gr *cfg.Grammar, oracle bool) {
			var parsed, ownFound int
			var perWord, cut, frames []float64
			var logT, hF, share, correlation, marginal, chained []float64
			var marginalAll, pairMI, sumAll, logTAll []float64
			gaveUp := 0
			for _, s := range sample {
				words := spell(s)
				if oracle {
					gr = oracleGrammar(g, s)
					for i := range words {
						words[i] += "|" + strconv.Itoa(i)
					}
				}
				f := gr.Parse(words)
				if len(f.Goals) == 0 {
					continue
				}
				parsed++
				if f.Contains(own(s, g, oracle)) {
					ownFound++
				}
				count := f.Count()
				lc := log10(count)
				if least == 0 {
					if count.Cmp(plainCount[s.id]) != 0 {
						fail(fmt.Errorf("%s: the renamed grammar counts %v trees, the grammar %v", s.id, count, plainCount[s.id]))
					}
					baseline[s.id] = lc
				} else {
					cut = append(cut, baseline[s.id]-lc)
				}
				perWord = append(perWord, lc/float64(len(words)))
				// the frames each verb has in some tree of the forest
				per := make([]map[int32]bool, len(words))
				for _, it := range f.Items {
					if it.R == it.L+1 && strings.Contains(gr.Names[it.Sym], "~") {
						if per[it.L] == nil {
							per[it.L] = map[int32]bool{}
						}
						per[it.L][it.Sym] = true
					}
				}
				for _, p := range per {
					if p != nil {
						frames = append(frames, float64(len(p)))
					}
				}
				if oracle {
					continue
				}
				// the verbs' choices, each tree as likely as any other
				var verbs []int
				for i, t := range s.tags {
					if isVerbTag(t) {
						verbs = append(verbs, i)
					}
				}
				logTAll = append(logTAll, lc)
				var sumH float64
				for k, v := range verbs {
					h := entropy(choices(f, []int{v}, math.MaxInt))
					marginalAll = append(marginalAll, h)
					sumH += h
					if k > 0 {
						pair := choices(f, verbs[k-1:k+1], math.MaxInt)
						pairMI = append(pairMI, marginalAll[len(marginalAll)-2]+h-entropy(pair))
					}
				}
				sumAll = append(sumAll, sumH)
				joint := choices(f, verbs, 1<<22)
				if joint == nil {
					gaveUp++
					continue
				}
				d := decompose(joint, len(verbs))
				logT = append(logT, lc)
				hF = append(hF, d.joint)
				if lc > 0 {
					share = append(share, d.joint/lc)
				}
				correlation = append(correlation, d.sumMarginal-d.joint)
				marginal = append(marginal, d.marginal...)
				chained = append(chained, d.chained...)
			}
			if !oracle {
				fmt.Fprintf(&perVerb, "%s\t%s\t%.1f\t%.2f (%.1f)\t%.2f\t%.0f%%\t%.3f (%.3f)\n", g, name,
					median(logTAll), median(marginalAll), math.Pow(10, median(marginalAll)), median(sumAll),
					100*median(ratio(sumAll, logTAll)), median(pairMI), mean(pairMI))
				fmt.Fprintf(&decomposition, "%s\t%s\t%.1f\t%.2f\t%.0f%%\t%.2f (%.1f)\t%.2f (%.1f)\t%.2f\t%d\n", g, name,
					median(logT), median(hF), 100*median(share), median(marginal), math.Pow(10, median(marginal)),
					median(chained), math.Pow(10, median(chained)), median(correlation), gaveUp)
			}
			cuts := "-"
			if least != 0 {
				cuts = fmt.Sprintf("%.2f (%.2f)", median(cut), mean(cut))
			}
			fmt.Printf("%s\t%s\t%.1f%%\t%.1f%%\t%.2f\t%s\t%.0f (%.1f)\n", g, name, pct(parsed, len(sample)),
				pct(ownFound, len(sample)), median(perWord), cuts, median(frames), mean(frames))
		}
		report("no lexicon", 0, base, false)
		for _, least := range []int{1, 2} {
			st := setting{g, least}
			report(strconv.Itoa(least), least, build(g, func(l string) map[string]bool { return allowed(st, l) }), false)
		}
		report("each verb its own frame", -1, nil, true)
	}
	fmt.Println()
	fmt.Println("Each verb's choice (its frame, or its plain tag as an auxiliary or outside a verb phrase), every tree as likely as any other; entropies in decimal digits (log10), medians over verbs or sentences; all sentences:")
	fmt.Println("grain\tfewest tokens for a frame\tlog10 trees\tH(a verb's choice) (effective choices)\tsum over a sentence's verbs\tshare of log10 trees\tmutual information of neighbouring verbs' choices, median (mean)")
	fmt.Print(perVerb.String())
	fmt.Println()
	fmt.Println("Jointly, for the sentences where the joint distribution is small enough to compute exactly:")
	fmt.Println("The verbs' choices (each verb's frame, or its plain tag as an auxiliary or outside a verb phrase), every tree as likely as any other; entropies in decimal digits (log10), medians:")
	fmt.Println("grain\tfewest tokens for a frame\tlog10 trees\tH(verbs' choices)\tshare of log10 trees\tH(one verb's choice) (effective choices)\tH(a verb's choice | the verbs before it) (effective)\tsum of verbs' H minus joint H\tsentences given up")
	fmt.Print(decomposition.String())
}

// choices is the exact joint distribution of some verbs' choices over the
// trees of a forest: for each assignment of a preterminal symbol to each verb
// position (as a key of 4 bytes a verb, in order), the number of trees with
// those preterminals. It is one inside pass whose values are such maps,
// restricted to the verbs within an item's span; the spans of a hyperedge's
// items are disjoint and in order, so their keys concatenate. An item's map is
// dropped once every hyperedge using it has been visited. It gives up,
// returning nil, if the maps held at once have more than budget entries.
func choices(f *cfg.Forest, verbs []int, budget int) map[string]float64 {
	isVerb := map[int32]bool{}
	for _, v := range verbs {
		isVerb[int32(v)] = true
	}
	uses := make([]int32, len(f.Items))
	for item := range f.Items {
		for e, end := f.EdgeRange(int32(item)); e < end; e++ {
			h := f.Edge(e)
			if h.Left >= 0 {
				uses[h.Left]++
			}
			if h.Right >= 0 {
				uses[h.Right]++
			}
		}
	}
	for _, g := range f.Goals {
		uses[g]++ // kept to the end
	}
	inside := make([]map[string]float64, len(f.Items))
	held := 0
	done := func(item int32) {
		if uses[item]--; uses[item] == 0 {
			held -= len(inside[item])
			inside[item] = nil
		}
	}
	for _, item := range f.Order() {
		it := f.Items[item]
		m := map[string]float64{}
		for e, end := f.EdgeRange(item); e < end; e++ {
			h := f.Edge(e)
			switch {
			case h.Left < 0: // the item's words
				key := ""
				if isVerb[it.L] {
					key = string([]byte{byte(it.Sym >> 24), byte(it.Sym >> 16), byte(it.Sym >> 8), byte(it.Sym)})
				}
				m[key]++
			case h.Right < 0:
				for k, v := range inside[h.Left] {
					m[k] += v
				}
			default:
				for kl, vl := range inside[h.Left] {
					for kr, vr := range inside[h.Right] {
						m[kl+kr] += vl * vr
					}
				}
			}
			if held+len(m) > budget {
				return nil
			}
		}
		inside[item] = m
		held += len(m)
		for e, end := f.EdgeRange(item); e < end; e++ {
			h := f.Edge(e)
			if h.Left >= 0 {
				done(h.Left)
			}
			if h.Right >= 0 {
				done(h.Right)
			}
		}
	}
	joint := map[string]float64{}
	for _, g := range f.Goals {
		for k, v := range inside[g] {
			joint[k] += v
		}
	}
	return joint
}

// decomposition is the entropy of the verbs' choices, in decimal digits:
// jointly; each verb's own (its marginal); each verb's given the verbs before
// it, which sum to the joint (the chain rule); and the sum of the marginals.
type decomposition struct {
	joint, sumMarginal float64
	marginal, chained  []float64
}

// entropy is a distribution's entropy in decimal digits, from its counts.
func entropy(m map[string]float64) float64 {
	total, h := 0.0, 0.0
	for _, v := range m {
		total += v
	}
	for _, v := range m {
		if v > 0 {
			p := v / total
			h -= p * math.Log10(p)
		}
	}
	return h
}

func decompose(joint map[string]float64, verbs int) decomposition {
	var d decomposition
	d.joint = entropy(joint)
	prev := 0.0
	for k := 1; k <= verbs; k++ {
		prefix, one := map[string]float64{}, map[string]float64{}
		for key, v := range joint {
			prefix[key[:4*k]] += v
			one[key[4*(k-1):4*k]] += v
		}
		hp, h1 := entropy(prefix), entropy(one)
		d.chained = append(d.chained, hp-prev)
		d.marginal = append(d.marginal, h1)
		d.sumMarginal += h1
		prev = hp
	}
	return d
}

// spell is a sentence's words with their gold tags, as the lexicons key them.
func spell(s sentence) []string {
	words := make([]string, len(s.words))
	for i := range s.words {
		words[i] = s.words[i] + "|" + s.tags[i]
	}
	return words
}

func readRules(file string) ([]cfg.Rule, error) {
	f, err := os.Open(file)
	if err != nil {
		return nil, err
	}
	defer f.Close()
	var rules []cfg.Rule
	sc := bufio.NewScanner(f)
	for sc.Scan() {
		rec := strings.Split(sc.Text(), "\t")
		if len(rec) < 4 {
			return nil, fmt.Errorf("%s: short line %q", file, sc.Text())
		}
		if rec[0] == "rule" {
			rules = append(rules, cfg.Rule{LHS: rec[2], RHS: rec[3:]})
		}
	}
	return rules, sc.Err()
}

// readLemmas reads form, tag, lemma and count: word+"\t"+tag -> lemma.
func readLemmas(file string) (map[string]string, error) {
	f, err := os.Open(file)
	if err != nil {
		return nil, err
	}
	defer f.Close()
	out := map[string]string{}
	sc := bufio.NewScanner(f)
	for sc.Scan() {
		rec := strings.Split(sc.Text(), "\t")
		if len(rec) < 3 {
			return nil, fmt.Errorf("%s: short line %q", file, sc.Text())
		}
		out[rec[0]+"\t"+rec[1]] = rec[2]
	}
	return out, sc.Err()
}

// jnode is a node of annotated.jsonl.
type jnode struct {
	C string  `json:"c"`
	K []jnode `json:"k"`
	W *string `json:"w"`
}

func readSentences(file string) ([]sentence, error) {
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
		out = append(out, s)
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

func mean(xs []float64) float64 {
	if len(xs) == 0 {
		return math.NaN()
	}
	s := 0.0
	for _, x := range xs {
		s += x
	}
	return s / float64(len(xs))
}

// ratio is xs[i]/ys[i], where ys[i] > 0.
func ratio(xs, ys []float64) []float64 {
	var out []float64
	for i := range xs {
		if ys[i] > 0 {
			out = append(out, xs[i]/ys[i])
		}
	}
	return out
}

func pct(a, b int) float64 {
	if b == 0 {
		return 0
	}
	return 100 * float64(a) / float64(b)
}
