package chart

import (
	"iter"
	"strings"

	"github.com/cbrew/quadruplet/go/term"
)

// Tree is a parse tree: a Node with children, or a Leaf over words.
type Tree interface {
	Category() term.Term
	// Format prints the tree one node per line, indented by depth plus
	// indent spaces, as the Kotlin treestring does.
	Format(indent int) string
}

type Node struct {
	Cat      term.Term
	Children []Tree
}

type Leaf struct {
	Cat   term.Term
	Words []string
}

func (n *Node) Category() term.Term { return n.Cat }
func (l *Leaf) Category() term.Term { return l.Cat }

func (n *Node) Format(indent int) string {
	var b strings.Builder
	b.WriteString(strings.Repeat(" ", indent))
	b.WriteString(term.Label(n.Cat))
	b.WriteByte('\n')
	for _, c := range n.Children {
		b.WriteString(c.Format(indent + 1))
	}
	return b.String()
}

// Format prints the words run together, as the Kotlin version does.
func (l *Leaf) Format(indent int) string {
	return strings.Repeat(" ", indent) + term.Label(l.Cat) + ":" + strings.Join(l.Words, "") + "\n"
}

// Trees enumerates the trees under e lazily, so the first few of a huge
// forest come quickly.
func (c *Chart) Trees(e *Edge) iter.Seq[Tree] {
	return func(yield func(Tree) bool) {
		c.trees(e, yield)
	}
}

func (c *Chart) trees(e *Edge, yield func(Tree) bool) bool {
	pairs := c.preds[e]
	if len(pairs) == 0 && e.Start == e.End {
		return yield(&Node{Cat: e.Cat})
	}
	if len(pairs) == 0 || c.lexical[e] {
		if !yield(&Leaf{Cat: e.Cat, Words: c.words[e.Start:e.End]}) {
			return false
		}
	}
	for _, pr := range pairs {
		ok := c.trees(pr.Partial, func(t1 Tree) bool {
			return c.trees(pr.Complete, func(t2 Tree) bool {
				n := t1.(*Node)
				children := append(n.Children[:len(n.Children):len(n.Children)], t2)
				return yield(&Node{Cat: n.Cat, Children: children})
			})
		})
		if !ok {
			return false
		}
	}
	return true
}
