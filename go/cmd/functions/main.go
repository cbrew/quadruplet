// Command functions learns a table of function tags (package interp's
// FunctionTable) from the annotated trees tools/masc/treebank.py writes, and
// tests it on documents it did not learn from: a tenth of them, chosen by a
// hash of the document's name. It scores the tags the table gives, and the
// relations of the flat meanings (interp.Flat) built on them, against those
// built on the treebank's own tags, beside the relations built on no tags.
//
//	functions -annotated annotated.jsonl [-min 3] [-o table.json]
package main

import (
	"bufio"
	"cmp"
	"encoding/json"
	"flag"
	"fmt"
	"hash/fnv"
	"maps"
	"os"
	"slices"
	"strings"

	"github.com/cbrew/quadruplet/go/interp"
)

func main() {
	file := flag.String("annotated", "", "annotated trees, as JSON lines: {\"id\": ..., \"tree\": ...}")
	minCount := flag.Int("min", 3, "how often a configuration must have been seen to be used")
	outFile := flag.String("o", "", "write the table, learned from every document, here")
	flag.Parse()
	if *file == "" {
		flag.Usage()
		os.Exit(2)
	}
	fail := func(err error) {
		fmt.Fprintln(os.Stderr, "functions:", err)
		os.Exit(1)
	}
	type sentence struct {
		id   string
		tree *interp.Node
		raw  json.RawMessage
	}
	var train, test, all []sentence
	f, err := os.Open(*file)
	if err != nil {
		fail(err)
	}
	sc := bufio.NewScanner(f)
	sc.Buffer(make([]byte, 1<<20), 1<<26)
	for sc.Scan() {
		var rec struct {
			ID   string          `json:"id"`
			Tree json.RawMessage `json:"tree"`
		}
		if err := json.Unmarshal(sc.Bytes(), &rec); err != nil {
			fail(err)
		}
		n, err := interp.FromJSON(rec.Tree)
		if err != nil {
			fail(fmt.Errorf("%s: %v", rec.ID, err))
		}
		s := sentence{rec.ID, n, rec.Tree}
		all = append(all, s)
		doc, _, _ := strings.Cut(rec.ID, "#")
		h := fnv.New32a()
		h.Write([]byte(doc))
		if h.Sum32()%10 == 0 {
			test = append(test, s)
		} else {
			train = append(train, s)
		}
	}
	if err := sc.Err(); err != nil {
		fail(err)
	}
	f.Close()

	table := interp.NewFunctionTable(*minCount)
	for _, s := range train {
		table.Learn(s.tree)
	}
	// score each phrase of the test documents: its predicted tags against its own
	var phrases, exact, none, predicted, gold, correct int
	perTag := map[string]*[3]int{} // tag -> predicted, gold, correct
	count := func(tag string, i int) {
		if perTag[tag] == nil {
			perTag[tag] = &[3]int{}
		}
		perTag[tag][i]++
	}
	for _, s := range test {
		interp.Fold(s.tree, func(*interp.Node) int { return 0 }, func(n *interp.Node, _ []int) int {
			for i, k := range n.Kids {
				if k.IsWord() {
					continue
				}
				want, got := k.Fn, table.Predict(n, i)
				phrases++
				if slices.Equal(want, got) {
					exact++
				}
				if len(want) == 0 {
					none++
				}
				for _, t := range got {
					predicted++
					count(t, 0)
					if slices.Contains(want, t) {
						correct++
						count(t, 2)
					}
				}
				for _, t := range want {
					gold++
					count(t, 1)
				}
			}
			return 0
		})
	}
	fmt.Printf("learned from %d sentences, tested on %d (%d phrases)\n", len(train), len(test), phrases)
	fmt.Printf("tags exactly right on %.1f%% of phrases (%.1f%% have none)\n", pct(exact, phrases), pct(none, phrases))
	p, r := pct(correct, predicted), pct(correct, gold)
	fmt.Printf("tags: precision %.1f%%, recall %.1f%%, F1 %.1f\n", p, r, f1(p, r))
	tags := slices.Collect(maps.Keys(perTag))
	slices.SortFunc(tags, func(a, b string) int { return cmp.Or(perTag[b][1]-perTag[a][1], cmp.Compare(a, b)) })
	fmt.Println("tag\tgold\tprecision\trecall\tF1")
	for _, t := range tags[:min(15, len(tags))] {
		c := perTag[t]
		p, r := pct(c[2], c[0]), pct(c[2], c[1])
		fmt.Printf("%s\t%d\t%.1f\t%.1f\t%.1f\n", t, c[1], p, r, f1(p, r))
	}
	// the relations of the flat meanings: with the table's tags, and with none
	var goldRels, tableRels, bareRels, tableRight, bareRight int
	for _, s := range test {
		gold := relations(interp.Flat(s.tree))
		tagged, _ := interp.FromJSON(s.raw)
		table.Assign(tagged)
		bare, _ := interp.FromJSON(s.raw)
		interp.Fold(bare, func(*interp.Node) int { return 0 }, func(n *interp.Node, _ []int) int {
			for _, k := range n.Kids {
				k.Fn = nil
			}
			return 0
		})
		got, none := relations(interp.Flat(tagged)), relations(interp.Flat(bare))
		goldRels += len(gold)
		tableRels += len(got)
		bareRels += len(none)
		for r := range got {
			if gold[r] {
				tableRight++
			}
		}
		for r := range none {
			if gold[r] {
				bareRight++
			}
		}
	}
	p, r = pct(tableRight, tableRels), pct(tableRight, goldRels)
	fmt.Printf("relations of the flat meanings, against those on the treebank's tags (%d):\n", goldRels)
	fmt.Printf("  on the table's tags: precision %.1f%%, recall %.1f%%, F1 %.1f\n", p, r, f1(p, r))
	p, r = pct(bareRight, bareRels), pct(bareRight, goldRels)
	fmt.Printf("  on no tags:          precision %.1f%%, recall %.1f%%, F1 %.1f\n", p, r, f1(p, r))

	if *outFile != "" {
		table = interp.NewFunctionTable(*minCount)
		for _, s := range all {
			table.Learn(s.tree)
		}
		out, err := os.Create(*outFile)
		if err != nil {
			fail(err)
		}
		if err := table.Write(out); err != nil {
			fail(err)
		}
		if err := out.Close(); err != nil {
			fail(err)
		}
	}
}

// relations are a flat meaning's two-place atoms, as strings.
func relations(atoms []interp.Atom) map[string]bool {
	out := map[string]bool{}
	for _, a := range atoms {
		if len(a.Args) == 2 {
			out[a.String()] = true
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

func f1(p, r float64) float64 {
	if p+r == 0 {
		return 0
	}
	return 2 * p * r / (p + r)
}
