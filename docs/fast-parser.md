# The fast context-free parser, and what a hyperedge is

Package `go/cfg` parses context-free grammars: grammars whose categories
are plain symbols, with no features to unify. It builds the same packed
forest as the feature parser in `go/chart` would, but faster: 0.13 s
against 49 s for a 15-token sentence under a treebank grammar read off
MASC. This note explains how, starting with the idea everything else rests
on: the forest as a hypergraph.

## 1. Items and hyperedges

### Two words for "edge"

Chart parsing and hypergraphs both use the word "edge", for different
things, and that is the source of most confusion:

| | chart parsing (Kay, and `go/chart`) | a parse forest as a hypergraph |
|---|---|---|
| a constituent, e.g. an NP over words 2 to 4 | an **edge** (a complete edge; a partial edge is a dotted rule) | a **node**, or **item** |
| one way of building it: a rule and the constituents it combines | a **predecessor pair** of the edge | a **hyperedge** |

So an *item* is a thing that was found, a symbol over a span, and a
*hyperedge* is a reason for believing in it: one application of one rule
to particular children. `go/cfg` uses the hypergraph words.

### A worked example

Take the textbook grammar

    S -> NP VP     VP -> V NP     VP -> VP PP
    NP -> NP PP    NP -> Det N    PP -> P NP

with the words "I" (NP), "saw" (V or N), "the" (Det), "man" and
"telescope" (N) and "with" (P). The sentence

    0 I 1 saw 2 the 3 man 4 with 5 the 6 telescope 7

has two trees: I used the telescope to see the man, or the man had it.
Here is the whole forest the parser builds, 14 items with their hyperedges
(each in brackets), children before parents:

    NP(0,1)   [word "I"]
    V(1,2)    [word "saw"]
    Det(2,3)  [word "the"]
    N(3,4)    [word "man"]
    P(4,5)    [word "with"]
    Det(5,6)  [word "the"]
    N(6,7)    [word "telescope"]
    NP(2,4)   [Det(2,3) N(3,4)]
    NP(5,7)   [Det(5,6) N(6,7)]
    VP(1,4)   [V(1,2) NP(2,4)]
    PP(4,7)   [P(4,5) NP(5,7)]
    NP(2,7)   [NP(2,4) PP(4,7)]
    VP(1,7)   [VP(1,4) PP(4,7)]   [V(1,2) NP(2,7)]
    S(0,7)    [NP(0,1) VP(1,7)]

Every item but one has a single hyperedge. VP(1,7) has two: it can be
built by VP → VP PP from VP(1,4) and PP(4,7), or by VP → V NP from V(1,2)
and NP(2,7). That one item with two hyperedges *is* the ambiguity. The two
trees share everything else: there is one PP(4,7) and one NP(2,4), used by
both.

### Why "hyper"

An edge in an ordinary directed graph goes from one node to one node. A
hyperedge goes from a *tuple* of nodes, the children, to one node, the
parent:

                      VP(1,7)
                    /        \
         hyperedge 1          hyperedge 2
          /       \            /       \
      VP(1,4)   PP(4,7)     V(1,2)   NP(2,7)

Hyperedge 1 joins VP(1,4) and PP(4,7) into VP(1,7); it is not two edges,
because neither child alone builds anything. A lexical entry is a hyperedge
with no children at all: `[word "the"]`. A forest is a directed acyclic
hypergraph, and a tree is what you get by choosing, from the root down,
one hyperedge at each item you reach.

Hyperedges are not something extra that a parser can do without. Any
packed forest needs them, context-free or not, the moment an item can be
built in more than one way; without them it could store the items but not
the trees. `go/chart` has them too, as each edge's list of predecessor
pairs. Its hypergraph also has nodes for partial, dotted edges, so a
three-daughter rule becomes a chain of pairs.

### Counting trees

The number of trees under an item is a sum over its hyperedges of a
product over their children:

    trees(item) = Σ over hyperedges h of item  Π over children c of h  trees(c)

with the empty product, 1, for a lexical hyperedge. Here every item has
one tree except VP(1,7), which has 1 × 1 + 1 × 1 = 2, so S(0,7) has
1 × 2 = 2. Visiting items children first makes this one pass over the
forest, however many trees it holds. The 15-token sentence above has
3.6 × 10²³ trees in 36,349 items and 427,236 hyperedges.

Writing the lexical entries as explicit hyperedges caught a bug. `go/chart`
and the Kotlin chart took "no predecessors" to mean "a word", so an item
that was a word *and* built by a rule (a B over "x", when "x" is a B and
B → A with "x" an A) lost its word reading. The fast parser counted it,
the two parsers disagreed on a random grammar, and both charts are now
fixed.

## 2. Binarization with shared prefixes

The parser only ever joins two items, so a rule of more than two daughters
is broken into steps. The rules

    NP -> Det Adj N       NP -> Det Adj N PP      NP -> Det N

become

    |Det Adj    -> Det Adj
    |Det Adj N  -> |Det Adj N
    NP          -> |Det Adj N          (completes NP -> Det Adj N)
    NP          -> |Det Adj N PP       (completes NP -> Det Adj N PP)
    NP          -> Det N

A symbol like `|Det Adj N` is a *prefix*: "the first three daughters of
some rule have been found". Prefixes are shared between rules, and between
left-hand sides, so the two NP rules build `|Det Adj N` once. Each original
rule still corresponds to exactly one path of steps, so derivations are in
one-to-one correspondence and tree counts are unchanged. For "the old man
with the dog":

    |Det Adj(0,2)    [Det(0,1) Adj(1,2)]
    |Det Adj N(0,3)  [|Det Adj(0,2) N(2,3)]
    PP(3,6)          [P(3,4) NP(4,6)]
    NP(0,6)          [|Det Adj N(0,3) PP(3,6)]

Trees are reported in the grammar's own rules: a prefix item's children are
spliced into its parent, giving `(NP (Det the) (Adj old) (N man) (PP ...))`.

Sharing matters for treebank grammars. The MASC grammar has 21,273 rules,
up to 2,616 of which begin with the same symbol. The shared-prefix grammar
has 45,029 binary steps, but at most 283 begin with any one symbol, so each
item found starts far fewer steps.

## 3. Two passes

### Bottom-up: what is derivable where

The first pass is CKY. For every span, shortest first, it computes the set
of symbols derivable over it: the words' lexical symbols; every parent of a
binary step whose left child is derivable over (i, k) and right child over
(k, j), for each split k; then everything reachable from those by unary
steps. Each set is a bitset (one bit per symbol) and also a list of the
symbols found, so "is C derivable over (k, j)?" is one bit test and "what
is derivable over (i, k)?" is a list. For each left symbol the pass either
walks that symbol's steps and tests their right children, or walks the
right cell's symbols and looks them up, whichever list is shorter.

This pass stores no hyperedges and no items, only bits. It finds every
symbol over every span that the words support, which includes a great deal
that no parse of the whole sentence uses. In the example, "saw" is also a
noun, so N(1,2) is derivable, and "I saw the man" is an S, so S(0,4) is
derivable. Neither is on a parse of the whole sentence: 16 derivable
items, 14 in the forest.

### Top-down: only what a parse uses

The second pass starts from the start symbol over the whole input and
works down. For an item it has reached, it looks at every step that could
build it: a lexical entry, a unary step whose child is derivable over the
same span, or a binary step whose children are derivable over (i, k) and
(k, j) for some k. Each one found becomes a hyperedge, and each child not
yet reached becomes an item and is visited in turn.

An item is reached exactly when it lies on some derivation of the whole
input: it must be derivable (the first pass) and connected to the root (the
second). So dead items are never stored at all. On treebank grammars they
are most of what a bottom-up parser builds:

| tokens | derivable (pass 1) | items in the forest | hyperedges |
|---|---|---|---|
| 15 | 109,330 | 36,349 | 427,236 |
| 30 | 927,533 | 416,486 | 6,599,726 |
| 60 | 3,632,467 | 1,497,846 | 56,957,292 |

(The feature parser, which has only the bottom-up pass, built 109,330
complete edges and 339,854 partial ones for the 15-token sentence, and kept
them all.)

### Unary steps

Unary steps join one child over the same span, so within a span their
order matters. The grammar is checked at load time for unary cycles (A → B
→ A), which would give infinitely many trees, and refused if it has one;
each symbol gets a rank above everything it can be built from by unary
steps, and items are visited by width, then rank. Treebank grammars do have
unary cycles (MASC has S → VP and VP → S), so the treebank grammar collapses
each chain of single-child phrases into one symbol, `SxVP`, as the
odd_one_out TIGER grammar does.

## 4. What else the forest does

* `Count` is the sum-product above.
* `Trees` enumerates trees lazily, so the first few of 10²³ come at once.
* `Contains` checks whether a given tree is in the forest, node by node
  through the prefixes, without enumerating anything. This is how to check
  that each treebank sentence's own tree is among its parses.
* `Find` looks up an item by symbol and span.

## 5. How it is checked

* On 1,200 random sentences over 300 random grammars (with unary chains,
  lexical phrases and lexical ambiguity), the forest's items of grammar
  symbols are exactly `go/chart`'s complete edges reachable from a parse,
  and the tree counts agree. Where the count is small, every tree is
  enumerated, the trees are all different, and `Contains` finds each one.
* The tree counts for `chart.TreeGrammar` match `treeas.golden`, written by
  the Kotlin implementation.
* On the MASC treebank grammar, the counts match `go/chart`'s for the
  sentences it can finish: 675,831 trees at 5 tokens, 6.8 × 10¹² at 10,
  3.6 × 10²³ at 15.

## 6. Where the time goes, and what next

On one core, with the treebank grammar read off all of MASC:

| tokens | `go/chart` | `go/cfg` | trees |
|---|---|---|---|
| 5 | 0.9 s | 4 ms | 675,831 |
| 10 | 7.6 s | 14 ms | 6.8 × 10¹² |
| 15 | 49 s | 0.13 s | 3.6 × 10²³ |
| 30 | | 2.2 s | 2.2 × 10⁵¹ |
| 40 | | 5.5 s | 9.9 × 10⁷⁴ |
| 60 | | 23 s, 3.4 GB | 3.3 × 10¹¹⁰ |

The bottom-up pass is cheap: 0.24 s at 40 tokens. Nearly all the time is in
the top-down pass, and it is proportional to what it builds: the live
forest, whose hyperedges grow about as the cube of the length. What remains
is the size of the answer, not waste: a flat treebank grammar is enormously
ambiguous, and an exact forest of a 174-token sentence would hold a few
billion hyperedges. The next steps are:

* **Smaller hyperedges.** An item's hyperedges are all found when it is
  visited, so they can be stored side by side, without the `Next` link:
  12 bytes each instead of 16. The item table could be an open-addressed
  table of integers, as in odd_one_out, instead of a Go map.
* **Parallel recognition**, cell by cell, as `go/chart` does.
* **Probabilities.** The treebank's rule counts give a PCFG for free. Klein
  and Manning's A* search, or Charniak, Goldwater and Johnson's best-first
  search, would find the best parse of a long sentence without building
  its forest; coarse-to-fine pruning would keep a smaller, approximate one.

## 7. What it takes from odd_one_out

The LCFRS parser in cbrew/odd_one_out (`internal/lcfrs`) has the same
design: integer symbols, flat memory, items with lists of hyperedges,
collapsed unary chains, and a bitset CKY stage that marks live items top
down. There, the CKY stage runs over a context-free *image* of a
discontinuous grammar, as a whitelist for a second, exact parser, because
an LCFRS item is a tuple of spans and the grammar itself cannot be run as
CKY. For a grammar that is context-free to begin with, the image is the
grammar itself, so the whitelist is exact and the second parser is not
needed: the top-down pass of the CKY stage builds the forest directly.
Everything to do with discontinuity (tuples of spans, yield functions,
gaps, fan-out) drops away.
