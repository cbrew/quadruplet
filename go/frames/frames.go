// Package frames reads verbs' frames off the rules of a treebank grammar, as
// tools/masc/treebank.py writes it, learns a lexicon of frames per verb
// lemma, and makes grammars in which a verb's frame is visible in its tag, so
// that a lexicon can filter a forest's trees exactly.
//
// A verb's frame is read off the rule of its verb phrase: a lexical verb
// phrase rule is one whose parent is a VP (at the bottom of its chain, SxVP
// included) with a daughter tagged VB* and no VP daughter. Its frame is the
// daughters other than the verb, at one of three grains:
//
//	core   the daughters that are mostly complements in the treebank (NP, the
//	       clauses S, SBAR, SQ, SBARQ, SINV and their collapsed chains, SxVP;
//	       ADJP, PRT, UCP), in order;
//	pp     those and PP, which is a complement (-CLR, -DTV, -PUT) a third of
//	       the time;
//	rule   every daughter: the verb phrase's rule itself.
package frames

import (
	"bufio"
	"encoding/json"
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

// Grains are the frames' grains, from coarsest to finest.
var Grains = []string{"core", "pp", "rule"}

var core = map[string]bool{"NP": true, "S": true, "SBAR": true, "SQ": true, "SBARQ": true, "SINV": true,
	"ADJP": true, "PRT": true, "UCP": true}

// Sentence is a sentence of annotated.jsonl: its words, their gold tags,
// and its tree, labelled with the grammar's symbols.
type Sentence struct {
	ID    string
	Words []string
	Tags  []string
	Tree  *cfg.Tree
}

// Spell is the sentence's words with their gold tags, word|tag, as the
// grammars here have them in their lexicons.
func (s Sentence) Spell() []string {
	out := make([]string, len(s.Words))
	for i := range s.Words {
		out[i] = s.Words[i] + "|" + s.Tags[i]
	}
	return out
}

// Split puts a tenth of the documents, chosen by a hash of the document's
// name, in test and the rest in train, as cmd/functions does.
func Split(sents []Sentence) (train, test []Sentence) {
	for _, s := range sents {
		doc, _, _ := strings.Cut(s.ID, "#")
		h := fnv.New32a()
		h.Write([]byte(doc))
		if h.Sum32()%10 == 0 {
			test = append(test, s)
		} else {
			train = append(train, s)
		}
	}
	return train, test
}

// Chain is a grammar symbol's categories, top first: SxVP -> [S VP], NPph -> [NP].
func Chain(sym string) []string {
	parts := strings.Split(Tag(sym), "x")
	for i, p := range parts {
		parts[i] = strings.TrimSuffix(p, "ph")
	}
	return parts
}

// Tag is a symbol without the frame a renamed grammar gives a verb: VBD~F7 -> VBD.
func Tag(sym string) string {
	t, _, _ := strings.Cut(sym, "~")
	return t
}

// IsVerbTag says whether a symbol is a verb's tag, VB*, renamed or not.
func IsVerbTag(sym string) bool {
	switch Tag(sym) {
	case "VB", "VBD", "VBG", "VBN", "VBP", "VBZ":
		return true
	}
	return false
}

// IsVP says whether a symbol is, or its chain ends in, a verb phrase.
func IsVP(sym string) bool {
	c := Chain(sym)
	return c[len(c)-1] == "VP"
}

// LexicalVP says whether a rule is a lexical verb phrase's.
func LexicalVP(lhs string, rhs []string) bool {
	if !IsVP(lhs) {
		return false
	}
	verb := false
	for _, d := range rhs {
		if Chain(d)[0] == "VP" {
			return false
		}
		verb = verb || IsVerbTag(d)
	}
	return verb
}

// Frame is a lexical verb phrase rule's frame at a grain: its daughters as
// the grain keeps them, or 0 for none.
func Frame(rhs []string, grain string) string {
	var parts []string
	for _, d := range rhs {
		if IsVerbTag(d) {
			if grain == "rule" {
				parts = append(parts, "V")
			}
			continue
		}
		c := Chain(d)
		if grain == "rule" || core[c[0]] || grain == "pp" && c[0] == "PP" {
			parts = append(parts, strings.Join(c, "x"))
		}
	}
	if len(parts) == 0 {
		return "0"
	}
	return strings.Join(parts, " ")
}

// Lemmas maps a verb's form and tag to its lemma, as tools/masc/verbframes.py
// writes them to lemmas.tsv.
type Lemmas map[string]string

// Of is a form's lemma; a form it does not know is its own, lower-cased.
func (l Lemmas) Of(word, tag string) string {
	if lemma, ok := l[word+"\t"+Tag(tag)]; ok {
		return lemma
	}
	return strings.ToLower(word)
}

// Label is how a verb is used, as the daughter of a rule: in a lexical verb
// phrase rule, the rule's frame at the grain; in another verb phrase rule,
// one with a verb phrase daughter, "(aux)"; in any other rule, "(in X)",
// X the bottom of the parent's chain, as in (in NP) for a participle in a
// noun phrase.
func Label(lhs string, rhs []string, grain string) string {
	switch {
	case LexicalVP(lhs, rhs):
		return Frame(rhs, grain)
	case IsVP(lhs):
		return "(aux)"
	}
	c := Chain(lhs)
	return "(in " + c[len(c)-1] + ")"
}

// Use is a verb in a tree, with the rule it is a daughter of.
type Use struct {
	Leaf    *cfg.Tree
	Lemma   string
	LHS     string
	RHS     []string
	Lexical bool // the rule is a lexical verb phrase's
}

// Label is the use's label at a grain.
func (u Use) Label(grain string) string { return Label(u.LHS, u.RHS, grain) }

// Uses are the verbs of a tree, each with the rule it is a daughter of.
func Uses(t *cfg.Tree, lemmas Lemmas) []Use {
	var out []Use
	var walk func(t *cfg.Tree)
	walk = func(t *cfg.Tree) {
		if t.Words != nil {
			return
		}
		rhs := make([]string, len(t.Children))
		for i, c := range t.Children {
			rhs[i] = c.Label
		}
		lexical := LexicalVP(t.Label, rhs)
		for _, c := range t.Children {
			if c.Words != nil && IsVerbTag(c.Label) {
				out = append(out, Use{c, lemmas.Of(c.Words[0], c.Label), t.Label, rhs, lexical})
			}
		}
		for _, c := range t.Children {
			walk(c)
		}
	}
	walk(t)
	return out
}

// Lexicon is what some sentences say of verbs' uses at one grain.
type Lexicon struct {
	Grain  string
	Tokens map[string]int            // lemma -> verb tokens
	Frames map[string]map[string]int // lemma -> use label -> tokens
}

// Learn reads a lexicon off the sentences' trees: each verb's use label.
func Learn(sents []Sentence, lemmas Lemmas, grain string) *Lexicon {
	lx := &Lexicon{grain, map[string]int{}, map[string]map[string]int{}}
	for _, s := range sents {
		for _, u := range Uses(s.Tree, lemmas) {
			lx.Tokens[u.Lemma]++
			if lx.Frames[u.Lemma] == nil {
				lx.Frames[u.Lemma] = map[string]int{}
			}
			lx.Frames[u.Lemma][u.Label(grain)]++
		}
	}
	return lx
}

// Allowed is the uses the lexicon allows a lemma: those seen with it at
// least least times, if the lemma was seen at least minLemma times; else
// nil, for any frame.
func (lx *Lexicon) Allowed(lemma string, minLemma, least int) map[string]bool {
	if lx.Tokens[lemma] < minLemma {
		return nil
	}
	out := map[string]bool{}
	for f, k := range lx.Frames[lemma] {
		if k >= least {
			out[f] = true
		}
	}
	return out
}

// Renamed is a grammar whose rules have their verb daughters' tags renamed
// by the verb's use (Label) at a grain: VBD -> VBD~F7. A tree of the grammar
// corresponds to at most one tree of the renamed grammar under a lexicon
// that gives each verb the renamed tags of some uses; to one exactly when
// the lexicon allows every verb's use. So the renamed grammar counts the
// trees a lexicon allows, and the renamed tags over a verb in a forest are
// the uses it has in some tree.
type Renamed struct {
	Grain string
	IDs   map[string]string          // use label -> F0, F1, ...
	Rules []cfg.Rule                 // the renamed rules
	ByTag map[string]map[string]bool // tag -> the uses it has in some rule
}

// Rename renames a grammar's rules by their verbs' uses at a grain.
func Rename(rules []cfg.Rule, grain string) *Renamed {
	rn := &Renamed{grain, map[string]string{}, nil, map[string]map[string]bool{}}
	for _, r := range rules {
		label := Label(r.LHS, r.RHS, grain)
		rhs := slices.Clone(r.RHS)
		for i, d := range rhs {
			if IsVerbTag(d) {
				rhs[i] = d + "~" + rn.id(label)
				if rn.ByTag[d] == nil {
					rn.ByTag[d] = map[string]bool{}
				}
				rn.ByTag[d][label] = true
			}
		}
		rn.Rules = append(rn.Rules, cfg.Rule{LHS: r.LHS, RHS: rhs})
	}
	return rn
}

func (rn *Renamed) id(f string) string {
	if s, ok := rn.IDs[f]; ok {
		return s
	}
	s := "F" + strconv.Itoa(len(rn.IDs))
	rn.IDs[f] = s
	return s
}

// FrameOf is the frame a renamed tag stands for, or "" for a plain tag.
func (rn *Renamed) FrameOf(sym string) string {
	_, id, ok := strings.Cut(sym, "~")
	if !ok {
		return ""
	}
	for f, i := range rn.IDs {
		if i == id {
			return f
		}
	}
	return ""
}

// Grammar is the renamed grammar with a lexicon for the sentences' words,
// spelled word|tag with their gold tags: a verb has the renamed tags of the
// uses allow gives its lemma (all of them, where allow gives nil).
func (rn *Renamed) Grammar(sents []Sentence, lemmas Lemmas, allow func(lemma string) map[string]bool) (*cfg.Grammar, error) {
	lex := map[string][]string{}
	for _, s := range sents {
		for i, w := range s.Words {
			key, tag := w+"|"+s.Tags[i], s.Tags[i]
			if _, done := lex[key]; done {
				continue
			}
			entry := []string{tag}
			if IsVerbTag(tag) {
				entry = nil
				a := allow(lemmas.Of(w, tag))
				for f := range rn.ByTag[tag] {
					if a == nil || a[f] {
						entry = append(entry, tag+"~"+rn.IDs[f])
					}
				}
			}
			lex[key] = entry
		}
	}
	return cfg.New(rn.Rules, lex, []string{"Top"})
}

// Own is the sentence's own tree in the renamed grammar, its words spelled
// word|tag, or word|tag|position if indexed.
func (rn *Renamed) Own(s Sentence, indexed bool) *cfg.Tree {
	i := 0
	var copy func(t *cfg.Tree, label string) *cfg.Tree
	copy = func(t *cfg.Tree, label string) *cfg.Tree {
		out := &cfg.Tree{Label: t.Label}
		if t.Words != nil {
			if IsVerbTag(t.Label) {
				out.Label = t.Label + "~" + rn.IDs[label]
			}
			out.Words = []string{s.Words[i] + "|" + s.Tags[i]}
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
		l := Label(t.Label, rhs, rn.Grain)
		for _, c := range t.Children {
			out.Children = append(out.Children, copy(c, l))
		}
		return out
	}
	return copy(s.Tree, "")
}

// Oracle is the renamed grammar with each word of the sentence its own
// lexical entry, spelled word|tag|position, allowed only the tag its own
// tree gives it: a verb only its own use.
func (rn *Renamed) Oracle(s Sentence) (*cfg.Grammar, error) {
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
	walk(rn.Own(s, true))
	return cfg.New(rn.Rules, lex, []string{"Top"})
}

// Indexed is the sentence's words as Oracle's grammar spells them.
func (s Sentence) Indexed() []string {
	out := s.Spell()
	for i := range out {
		out[i] += "|" + strconv.Itoa(i)
	}
	return out
}

// Sample draws n sentences of lo to hi words with a verb, by the seed, and
// puts them shortest first.
func Sample(sents []Sentence, n, lo, hi int, seed uint64) []Sentence {
	rng := rand.New(rand.NewPCG(seed, 0))
	var pool []Sentence
	for _, s := range sents {
		if len(s.Words) >= lo && len(s.Words) <= hi && slices.ContainsFunc(s.Tags, IsVerbTag) {
			pool = append(pool, s)
		}
	}
	rng.Shuffle(len(pool), func(i, j int) { pool[i], pool[j] = pool[j], pool[i] })
	out := pool[:min(n, len(pool))]
	slices.SortFunc(out, func(a, b Sentence) int { return len(a.Words) - len(b.Words) })
	return out
}

// ReadRules reads the rules of counts.tsv.
func ReadRules(file string) ([]cfg.Rule, error) {
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

// ReadLemmas reads lemmas.tsv: form, tag, lemma and count.
func ReadLemmas(file string) (Lemmas, error) {
	f, err := os.Open(file)
	if err != nil {
		return nil, err
	}
	defer f.Close()
	out := Lemmas{}
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

// ReadSentences reads annotated.jsonl.
func ReadSentences(file string) ([]Sentence, error) {
	f, err := os.Open(file)
	if err != nil {
		return nil, err
	}
	defer f.Close()
	var out []Sentence
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
		s := Sentence{ID: rec.ID}
		var tree func(j *jnode) *cfg.Tree
		tree = func(j *jnode) *cfg.Tree {
			t := &cfg.Tree{Label: strings.TrimSuffix(j.C, "[]")}
			if j.W != nil {
				t.Words = []string{*j.W}
				s.Words, s.Tags = append(s.Words, *j.W), append(s.Tags, t.Label)
				return t
			}
			for i := range j.K {
				t.Children = append(t.Children, tree(&j.K[i]))
			}
			return t
		}
		s.Tree = tree(&rec.Tree)
		out = append(out, s)
	}
	return out, sc.Err()
}

// Log10 is log10 of a big number, 0 for none.
func Log10(x *big.Int) float64 {
	if x.Sign() <= 0 {
		return 0
	}
	s := x.String()
	lead, _ := strconv.ParseFloat("0."+s[:min(len(s), 15)], 64)
	return float64(len(s)) + math.Log10(lead)
}

// Median is the middle of some numbers, NaN for none.
func Median(xs []float64) float64 {
	if len(xs) == 0 {
		return math.NaN()
	}
	ys := slices.Clone(xs)
	slices.Sort(ys)
	return ys[len(ys)/2]
}

// Mean is the mean of some numbers, NaN for none.
func Mean(xs []float64) float64 {
	if len(xs) == 0 {
		return math.NaN()
	}
	s := 0.0
	for _, x := range xs {
		s += x
	}
	return s / float64(len(xs))
}

// Pct is a as a percentage of b.
func Pct(a, b int) float64 {
	if b == 0 {
		return 0
	}
	return 100 * float64(a) / float64(b)
}
