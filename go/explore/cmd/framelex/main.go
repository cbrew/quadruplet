// Command framelex measures how much a verb frame lexicon, read off the
// treebank, cuts the ambiguity of a treebank grammar: exactly, over every tree
// of each forest, not by sampling.
//
// A verb's frame is read off the rule of its verb phrase, at three grains
// (package frames): core complements, those and PP, and the whole rule. The
// lexicon is learned from nine tenths of the documents (as cmd/functions
// splits them, by a hash of the document's name) and tested on sentences of
// the other tenth, with gold tags. A verb's use is its frame, where it heads a
// lexical verb phrase, or that it is an auxiliary, or the kind of phrase it is
// in otherwise (frames.Label). For each lemma seen at least -lemma times, the
// lexicon allows the uses seen with the lemma at least once, or twice; other
// verbs are unrestricted. As an upper bound on what any such lexicon could
// do, the oracle allows each verb only the use its own tree gives it.
//
// The filter is exact: the grammar is renamed so that a verb's tag carries its
// use (frames.Renamed), and its count is the number of trees the
// lexicon allows. Without a lexicon it counts as many trees as the grammar,
// which the command checks.
//
//	framelex -counts counts.tsv -annotated annotated.jsonl -lemmas lemmas.tsv [-n 300] [-min 5] [-max 25] [-lemma 5]
package main

import (
	"flag"
	"fmt"
	"math/big"
	"os"
	"slices"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
	fr "github.com/cbrew/quadruplet/go/explore/frames"
)

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
	fmt.Printf("learned from %d sentences; tested on %d sentences of %d to %d words from the other documents, with gold tags\n",
		len(train), len(sample), *minWords, *maxWords)
	fmt.Printf("a lemma's frames are restricted if it was seen %d times or more in training\n\n", *minLemma)

	lexicons := map[string]*fr.Lexicon{}
	for _, g := range fr.Grains {
		lexicons[g] = fr.Learn(train, lemmas, g)
	}
	grammarFrames := map[string]map[string]bool{}
	for _, g := range fr.Grains {
		grammarFrames[g] = map[string]bool{}
		for _, r := range rules {
			if slices.ContainsFunc(r.RHS, fr.IsVerbTag) {
				grammarFrames[g][fr.Label(r.LHS, r.RHS, g)] = true
			}
		}
	}

	// how well each lexicon covers the verbs of every test sentence
	fmt.Println("grain\tverb uses in the grammar\tfewest tokens for a use\ttest verbs restricted\tof which own use allowed\tuses allowed a restricted verb, median")
	total := 0
	for _, s := range test {
		total += len(fr.Uses(s.Tree, lemmas))
	}
	for _, g := range fr.Grains {
		for _, least := range []int{1, 2} {
			var restricted, own int
			var sizes []float64
			for _, s := range test {
				for _, u := range fr.Uses(s.Tree, lemmas) {
					a := lexicons[g].Allowed(u.Lemma, *minLemma, least)
					if a == nil {
						continue
					}
					restricted++
					sizes = append(sizes, float64(len(a)))
					if a[u.Label(g)] {
						own++
					}
				}
			}
			fmt.Printf("%s\t%d\t%d\t%.1f%% of %d\t%.1f%%\t%.0f\n", g, len(grammarFrames[g]), least,
				fr.Pct(restricted, total), total, fr.Pct(own, restricted), fr.Median(sizes))
		}
	}

	// the grammar as it is, with gold tags: the renamed grammar without a
	// lexicon must count as many trees
	lex := map[string][]string{}
	for _, s := range sample {
		for i, w := range s.Words {
			lex[w+"|"+s.Tags[i]] = []string{s.Tags[i]}
		}
	}
	plain, err := cfg.New(rules, lex, []string{"Top"})
	if err != nil {
		fail(err)
	}
	plainCount := map[string]*big.Int{}
	for _, s := range sample {
		plainCount[s.ID] = plain.Parse(s.Spell()).Count()
	}

	fmt.Println()
	fmt.Println("grain\tlexicon\tparsed\town tree among the parses\tlog10 trees per word, median\tlog10 trees cut, median (mean)\tuses a verb can have, median (mean)")
	for _, g := range fr.Grains {
		rn := fr.Rename(rules, g)
		baseline := map[string]float64{}
		report := func(name string, grammarFor func(s fr.Sentence) (*cfg.Grammar, []string, bool)) {
			var parsed, ownFound int
			var perWord, cut, frames []float64
			for _, s := range sample {
				gr, words, indexed := grammarFor(s)
				f := gr.Parse(words)
				if len(f.Goals) == 0 {
					continue
				}
				parsed++
				if f.Contains(rn.Own(s, indexed)) {
					ownFound++
				}
				count := f.Count()
				lc := fr.Log10(count)
				if name == "none" {
					if count.Cmp(plainCount[s.ID]) != 0 {
						fail(fmt.Errorf("%s: the renamed grammar counts %v trees, the grammar %v", s.ID, count, plainCount[s.ID]))
					}
					baseline[s.ID] = lc
				} else {
					cut = append(cut, baseline[s.ID]-lc)
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
			}
			cuts := "-"
			if name != "none" {
				cuts = fmt.Sprintf("%.2f (%.2f)", fr.Median(cut), fr.Mean(cut))
			}
			fmt.Printf("%s\t%s\t%.1f%%\t%.1f%%\t%.2f\t%s\t%.0f (%.1f)\n", g, name, fr.Pct(parsed, len(sample)),
				fr.Pct(ownFound, len(sample)), fr.Median(perWord), cuts, fr.Median(frames), fr.Mean(frames))
		}
		fixed := func(gr *cfg.Grammar) func(fr.Sentence) (*cfg.Grammar, []string, bool) {
			return func(s fr.Sentence) (*cfg.Grammar, []string, bool) { return gr, s.Spell(), false }
		}
		gr, err := rn.Grammar(sample, lemmas, func(string) map[string]bool { return nil })
		if err != nil {
			fail(err)
		}
		report("none", fixed(gr))
		for _, least := range []int{1, 2} {
			gr, err := rn.Grammar(sample, lemmas, func(l string) map[string]bool {
				return lexicons[g].Allowed(l, *minLemma, least)
			})
			if err != nil {
				fail(err)
			}
			report(fmt.Sprintf("uses seen %d+ times", least), fixed(gr))
		}
		report("oracle: each verb its own use", func(s fr.Sentence) (*cfg.Grammar, []string, bool) {
			gr, err := rn.Oracle(s)
			if err != nil {
				fail(err)
			}
			return gr, s.Indexed(), true
		})
	}
}
