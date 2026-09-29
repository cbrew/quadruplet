package interp

// A head rule searches a phrase's daughters for the first category of a
// list, in priority order, from the left or from the right; if none is
// found, the head is the first daughter from that side. These are Collins's
// (1999, appendix A), with NP's special rule below, NML and NX treated as NP
// is, and punctuation never chosen where anything else will do.
type headRule struct {
	fromRight bool
	cats      []string
}

var headRules = map[string][]headRule{
	"ADJP":   {{false, []string{"NNS", "QP", "NN", "$", "ADVP", "JJ", "VBN", "VBG", "ADJP", "JJR", "NP", "JJS", "DT", "FW", "RBR", "RBS", "SBAR", "RB"}}},
	"ADVP":   {{true, []string{"RB", "RBR", "RBS", "FW", "ADVP", "TO", "CD", "JJR", "JJ", "IN", "NP", "JJS", "NN"}}},
	"CONJP":  {{true, []string{"CC", "RB", "IN"}}},
	"FRAG":   {{true, nil}},
	"INTJ":   {{false, nil}},
	"LST":    {{true, []string{"LS", ":"}}},
	"NAC":    {{false, []string{"NN", "NNS", "NNP", "NNPS", "NP", "NAC", "EX", "$", "CD", "QP", "PRP", "VBG", "JJ", "JJS", "JJR", "ADJP", "FW"}}},
	"PP":     {{true, []string{"IN", "TO", "VBG", "VBN", "RP", "FW"}}},
	"PRN":    {{false, nil}},
	"PRT":    {{true, []string{"RP"}}},
	"QP":     {{false, []string{"$", "IN", "NNS", "NN", "JJ", "RB", "DT", "CD", "NCD", "QP", "JJR", "JJS"}}},
	"RRC":    {{true, []string{"VP", "NP", "ADVP", "ADJP", "PP"}}},
	"S":      {{false, []string{"TO", "IN", "VP", "S", "SBAR", "ADJP", "UCP", "NP"}}},
	"SBAR":   {{false, []string{"WHNP", "WHPP", "WHADVP", "WHADJP", "IN", "DT", "S", "SQ", "SINV", "SBAR", "FRAG"}}},
	"SBARQ":  {{false, []string{"SQ", "S", "SINV", "SBARQ", "FRAG"}}},
	"SINV":   {{false, []string{"VBZ", "VBD", "VBP", "VB", "MD", "VP", "S", "SINV", "ADJP", "NP"}}},
	"SQ":     {{false, []string{"VBZ", "VBD", "VBP", "VB", "MD", "VP", "SQ"}}},
	"UCP":    {{true, nil}},
	"VP":     {{false, []string{"TO", "VBD", "VBN", "MD", "VBZ", "VB", "VBG", "VBP", "VP", "ADJP", "NN", "NNS", "NP"}}},
	"WHADJP": {{false, []string{"CC", "WRB", "JJ", "ADJP"}}},
	"WHADVP": {{true, []string{"CC", "WRB"}}},
	"WHNP":   {{false, []string{"WDT", "WP", "WP$", "WHADJP", "WHPP", "WHNP"}}},
	"WHPP":   {{true, []string{"IN", "TO", "FW"}}},
	"X":      {{true, nil}},
}

// npRules is Collins's rule for NP: the last word if it is a possessive
// marker; else, from the right, a noun; else, from the left, an NP; else,
// from the right, $, ADJP or PRN; then CD; then JJ, JJS, RB or QP; else the
// last daughter.
var npRules = []headRule{
	{true, []string{"NN", "NNP", "NNPS", "NNS", "NML", "NX", "POS", "JJR"}},
	{false, []string{"NP"}},
	{true, []string{"$", "ADJP", "PRN"}},
	{true, []string{"CD"}},
	{true, []string{"JJ", "JJS", "RB", "QP"}},
}

// punctuation are the tags a head is never chosen from while there is
// anything else.
var punctuation = map[string]bool{
	",": true, ".": true, ":": true, "``": true, "''": true, `"`: true, "'": true,
	"-LRB-": true, "-RRB-": true, "-LSB-": true, "-RSB-": true, "HYPH": true, "NFP": true,
}

// IsPunctuation says whether a node is a punctuation mark.
func IsPunctuation(n *Node) bool { return n.IsWord() && punctuation[n.Cat] }

// FindHeads sets the head daughter of every phrase in the tree.
func FindHeads(n *Node) {
	if n.IsWord() {
		return
	}
	for _, k := range n.Kids {
		FindHeads(k)
	}
	n.Head, n.ByDefault = head(n)
}

func head(n *Node) (int, bool) {
	kids := n.Kids
	if len(kids) == 1 {
		return 0, false
	}
	// the daughters that may head it: all but punctuation, unless there is only punctuation
	var cands []int
	for i, k := range kids {
		if !IsPunctuation(k) {
			cands = append(cands, i)
		}
	}
	if len(cands) == 0 {
		for i := range kids {
			cands = append(cands, i)
		}
	}
	cat := n.Bottom()
	var rules []headRule
	switch cat {
	case "NP", "NML", "NX":
		if last := kids[cands[len(cands)-1]]; last.Cat == "POS" {
			return cands[len(cands)-1], false
		}
		rules = npRules
	default:
		rules = headRules[cat]
	}
	for _, r := range rules {
		for _, c := range r.cats {
			for j := range cands {
				i := cands[j]
				if r.fromRight {
					i = cands[len(cands)-1-j]
				}
				if kids[i].Cat == c {
					return i, false
				}
			}
		}
	}
	// nothing matched: the first daughter from the side of the phrase's first rule
	if len(rules) > 0 && rules[0].fromRight || cat == "NP" || cat == "NML" || cat == "NX" {
		return cands[len(cands)-1], true
	}
	return cands[0], true
}
