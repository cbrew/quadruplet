// Command readings reads the annotated trees tools/masc/treebank.py writes,
// finds their heads, and gives each its dependency reading: CoNLL-style
// lines with -conll, and otherwise a summary of the corpus, checking that
// every tree's dependencies form a tree over its words.
//
//	readings -annotated annotated.jsonl [-conll]
package main

import (
	"bufio"
	"cmp"
	"encoding/json"
	"flag"
	"fmt"
	"maps"
	"os"
	"slices"

	"github.com/cbrew/quadruplet/go/interp"
)

func main() {
	file := flag.String("annotated", "", "annotated trees, as JSON lines: {\"id\": ..., \"tree\": ...}")
	conll := flag.Bool("conll", false, "write each sentence's dependencies, a line per word")
	flag.Parse()
	if *file == "" {
		flag.Usage()
		os.Exit(2)
	}
	fail := func(err error) {
		fmt.Fprintln(os.Stderr, "readings:", err)
		os.Exit(1)
	}
	f, err := os.Open(*file)
	if err != nil {
		fail(err)
	}
	defer f.Close()
	out := bufio.NewWriter(os.Stdout)
	defer out.Flush()

	var sentences, words, arcs, tagged, bad int
	labels := map[string]int{}
	defaults := map[string]int{} // phrases headed by default, by category
	phrases := map[string]int{}
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
		deps := interp.Dependencies(n)
		if err := isTree(deps); err != nil {
			bad++
			fmt.Fprintf(os.Stderr, "%s: %v\n", rec.ID, err)
		}
		sentences++
		words += len(deps)
		var forms, tags []string
		interp.Fold(n, func(w *interp.Node) int {
			forms, tags = append(forms, w.Word), append(tags, w.Cat)
			return 0
		}, func(p *interp.Node, _ []int) int {
			if p.Cat != "TOP" {
				phrases[p.Bottom()]++
				if p.ByDefault {
					defaults[p.Bottom()]++
				}
			}
			for _, k := range p.Kids {
				if len(k.Fn) > 0 {
					tagged++
				}
			}
			return 0
		})
		for _, d := range deps {
			if d.Head >= 0 {
				arcs++
				labels[d.Label]++
			}
		}
		if *conll {
			fmt.Fprintf(out, "# %s\n", rec.ID)
			for _, d := range deps {
				fmt.Fprintf(out, "%d\t%s\t%s\t%d\t%s\n", d.Dependent+1, forms[d.Dependent], tags[d.Dependent], d.Head+1, d.Label)
			}
			fmt.Fprintln(out)
		}
	}
	if err := sc.Err(); err != nil {
		fail(err)
	}
	fmt.Fprintf(os.Stderr, "%d sentences, %d words, %d arcs; %d not a tree\n", sentences, words, arcs, bad)
	fmt.Fprintf(os.Stderr, "%d arcs to a daughter with function tags\n", tagged)
	fmt.Fprintf(os.Stderr, "most frequent arc labels:%s\n", top(labels, 25))
	fmt.Fprintf(os.Stderr, "phrases headed by default, by category (of all of that category):")
	for _, c := range sortedKeys(defaults) {
		fmt.Fprintf(os.Stderr, " %s %d/%d", c, defaults[c], phrases[c])
	}
	fmt.Fprintln(os.Stderr)
}

// isTree checks that every word has one head, that there is one root, and
// that following heads from any word reaches it.
func isTree(deps []interp.Dep) error {
	roots := 0
	for i, d := range deps {
		if d.Dependent != i {
			return fmt.Errorf("word %d has no arc", i+1)
		}
		if d.Head < 0 {
			roots++
		}
	}
	if roots != 1 {
		return fmt.Errorf("%d roots", roots)
	}
	for i := range deps {
		for j, steps := i, 0; deps[j].Head >= 0; j, steps = deps[j].Head, steps+1 {
			if steps > len(deps) {
				return fmt.Errorf("a cycle through word %d", i+1)
			}
		}
	}
	return nil
}

func top(counts map[string]int, n int) string {
	keys := slices.Collect(maps.Keys(counts))
	slices.SortFunc(keys, func(a, b string) int { return cmp.Or(counts[b]-counts[a], cmp.Compare(a, b)) })
	s := ""
	for _, k := range keys[:min(n, len(keys))] {
		s += fmt.Sprintf(" %s %d", k, counts[k])
	}
	return s
}

func sortedKeys(m map[string]int) []string {
	keys := slices.Collect(maps.Keys(m))
	slices.SortFunc(keys, func(a, b string) int { return cmp.Or(m[b]-m[a], cmp.Compare(a, b)) })
	return keys
}
