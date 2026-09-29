# Exploratory work

Measurements of the ambiguity of treebank grammars, and of verbs and their
frames, built on the packed forests of the context-free parser in
[`../cfg`](../cfg). They are kept here, apart from the parser, so that the
parser stays a stable, tested implementation of BitPar's approach, and so
that experiments can change, or be dropped, without touching it. The code
here uses only `cfg`'s exported API. The write-ups are in
[`../../docs/ambiguity.md`](../../docs/ambiguity.md) and
[`../../docs/verbs/`](../../docs/verbs/README.md).

| package | contents |
|---|---|
| `entropy` | the entropy of a forest's trees, split exactly by kind of choice, uniform or weighted (reports 07–09) |
| `frames` | verbs' frames and uses read off grammar rules, lexicons of them, renamed grammars that filter forests exactly; `VerbLayers` (reports 07, 08) |
| `quotient` | trees up to the order of attachment within a head's projection, and other quotients (report 10) |
| `counts` | the treebank relabelled by counts of the basic types S, NP, PP and AP (report 11) |
| `cmd/ambiguity` | where a treebank grammar's ambiguity comes from; sampling and exact measures ([`docs/ambiguity.md`](../../docs/ambiguity.md)) |
| `cmd/framelex` | a verb-use lexicon as an exact filter (07) |
| `cmd/verbentropy` | the entropy of forests by the verbs' choices (07) |
| `cmd/layers` | the entropy by layers of verbs (08) |
| `cmd/dontcare` | what the flat meaning cannot see (09) |
| `cmd/quotient` | the order of attachment, by enumeration and estimate (10) |
| `cmd/countbank` | the counts grammar and its ambiguity (11) |

```bash
cd go
go test ./explore/...
go run ./explore/cmd/verbentropy -h
```
