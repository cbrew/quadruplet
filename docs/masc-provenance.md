# When and how MASC's Penn Treebank trees were made

**The question:** when was MASC's Penn Treebank (PTB) annotation done?
Everything else in `tools/masc` and `tools/frames` reads these trees as
earlier human judgment, so it matters which judgment, and from when.

**Short answer:** in three stages.

1. **The original WSJ trees.** 34 files are the Penn Treebank's own WSJ
   trees, bracketed in the early 1990s and copied in unchanged.
2. **MASC's first release: 2007 to September 2010.** About 74K words were
   annotated for the first release, which the LDC credits with 82K words
   of PTB syntax.
3. **The bulk: finished June to August 2013.** About 473K words, most of
   the corpus, were done under guidelines that date from about 2012.

The dates are the files' modification times, so each one is an upper
bound: when that file was last saved, not when work on it began.

## The evidence

### File modification times

The `.mrg` files kept their modification times through the zip and Dropbox
copy used here. `tools/masc/provenance.py` groups the files by month
modified:

| modified | files | words | `*PRO*` /1k | `NML` /1k | `HYPH` /1k | `ADD`/`NFP`/`GW` | `SU` | `{TEXT:` |
|---|---|---|---|---|---|---|---|---|
| 2000-10 | 34 | 7,267 | 0.0 | 0.0 | 0.0 | 0 | 0 | 0 |
| 2007-10 | 6 | 8,945 | 18.9 | 0.6 | 0.0 | 0 | 808 | 0 |
| 2007-11 | 2 | 2,364 | 13.1 | 6.8 | 2.1 | 0 | 0 | 0 |
| 2009-05 | 1 | 17,596 | 19.4 | 6.0 | 0.9 | 19 | 0 | 0 |
| 2009-08 | 1 | 882 | 13.6 | 13.6 | 4.5 | 0 | 0 | 0 |
| 2010-09 | 52 | 37,086 | 22.7 | 12.5 | 2.8 | 0 | 270 | 0 |
| 2012-03 | 4 | 30,784 | 18.4 | 5.8 | 1.7 | 0 | 0 | 0 |
| 2013-06 | 15 | 81,308 | 20.6 | 9.2 | 7.6 | 19 | 0 | 70 |
| 2013-07 | 241 | 383,088 | 24.4 | 11.5 | 4.6 | 1,359 | 0 | 362 |
| 2013-08 | 35 | 8,397 | 38.5 | 15.7 | 1.5 | 56 | 0 | 9 |

There are 577,717 words in all, counting overt tokens and punctuation.
The files modified up to September 2010 hold 74,140 words.

What each group contains:

| modified | genres |
|---|---|
| 2000-10 | `wsj_0006` to `wsj_0189`: WSJ sections 00–01 |
| 2007-10 | Switchboard telephone calls (`sw2014-UTF16-ms98-a-trans`, Mississippi State 1998 transcripts), Charlotte face-to-face narratives |
| 2007-11 | `sw2025-…-NEW`, `wsj_1640.mrg-NEW` |
| 2009 | one journal file, `wsj_2465` |
| 2010-09 | 30 philanthropic-fundraising letters, journal, face-to-face, enron, govt-docs, travel-guides, technical, non-fiction; five `-NEW` newswire files (`20000410_nyt-NEW`, `20000415_apw_eng-NEW` …) and two `-NEW` files `A1.E1`, `A1.E2` |
| 2012-03 | one file each: court-transcript, fiction, non-fiction, philanthropic-fundraising |
| 2013-06 to 2013-08 | every other genre: blog, spam, w3c and enron email, essays, ficlets, fiction, jokes, movie-script, nyt, solicitation-brochures, technical, travel-guides, twitter, court and debate transcripts, face-to-face |

### The release dates

The waves line up with MASC's releases.

* **MASC First Release (LDC2010T22)** came out on 20 December 2010
  ([LDC catalogue](https://catalog.ldc.upenn.edu/LDC2010T22)). All its words
  had PTB syntax; the third release's page puts that at 82K words. This is
  the files modified up to September 2010 (74K words by our count, which
  includes punctuation and so differs a little from the LDC's).
* **MASC Third Release (LDC2013T12)** came out on 17 July 2013. It still
  lists only "Penn Treebank syntax (82k)"
  ([LDC catalogue](https://catalog.ldc.upenn.edu/LDC2013T12)). There was no
  second release.
* **The MASC site** now gives PTB syntax "for the entire 500K words of
  MASC" ([anc.org](https://anc.org/data/masc/)). The 2013 files were
  finished in the weeks around the third release, so they must have
  appeared in a later MASC distribution from the site.

### The annotation conventions

The conventions in the trees agree with the dates. They also show that
the 2013 dates mark real annotation, not a later conversion that happened
to re-save old files.

* **The 2000 files use the original Penn Treebank II conventions.** A
  controlled subject is `*-1`, like any NP trace. There is no `*PRO*`, no
  `NML` and no `HYPH`, and the files keep the original formatting (spaces,
  closing brackets written `) )`).
* **Every later file uses the revised LDC conventions.** These include
  `*PRO*` told apart from `*`, `NML` inside noun phrases and hyphens
  tokenized as `HYPH`.
* **Only the 2013 files use the English Web Treebank's tags in earnest.**
  `ADD` (URLs and email addresses), `NFP` and `GW` arrived with that
  treebank, published by the LDC in 2012. The 2013 files have 1,434 of
  them; the one 2009 file has 19; no other file has any. So the 2013 trees
  were bracketed under guidelines that existed only from about 2012.
* **Only the 2013 files keep the original spelling of re-tokenized words**,
  in `CODE` nodes: `(CODE {TEXT:gonna})`, `(CODE {TEXT:book,don't})`.
* **The 2007 and 2010 spoken files mark Switchboard-style slash units**,
  `(SU /)`, and turns, `(CODE <TURN>)`. The 2013 transcripts do not.

## Who made them, and how

* **Who.** The MASC site says the syntax was "produced by the Penn Treebank
  project" in its own bracketed format, then converted to GrAF for MASC.
  The tokenization and part-of-speech tags were "manually validated by that
  project" ([MASC corpus structure](https://anc.org/data/masc/corpus/masc-structure/)).
  MASC describes all its annotations as "either manually produced or
  hand-validated".
* **Parser first or hand-bracketed?** Nothing found says whether the
  annotators corrected parser output or bracketed from scratch. Correcting
  automatic parses was the LDC's usual practice. Traces and function tags
  are at similar rates in every genre after 2000, which a parser of the
  period would not supply, so they at least were added by hand.
* **The `-NEW` files are probably from the Language Understanding
  Annotation Corpus.** The LDC names it as MASC's second source of texts,
  besides the Open American National Corpus. This is an inference from the
  file names and dates, not something the documentation says.

## Debris from preparing the texts

The texts were altered before annotation, and the changes are in the
tokens:
* **Curly quotes became the letters `RSQUO`**: `(POS RSQUOs)` 553 times,
  `(VBZ RSQUOs)` 213, `(MD RSQUOll)` 94.
* **`/` in URLs became `$$`.**
* **Transcript markup survives as words**: `<bracket>` 795 times,
  `<disfluency>` and `</disfluency>` 184 each, plus `<curly>`, `<seg>` and
  `<p>`.

## Consequences for this project

* **`*PRO*` in the old WSJ files.** In the 34 files of 2000, a controlled
  subject is `*-n`. So `tools/frames/gold.py` gives it the source `trace`,
  not `controlled`, and a search for object control through `*PRO*` misses
  them. That is 310 of the 34,586 trees. Analyses that depend on `*PRO*`
  should drop these files or map `*-n` subjects of nonfinite clauses.
* **MASC is not one uniform treebank.** It is one set of guidelines after
  2007, applied mostly in 2013, plus an older WSJ layer. Comparisons across
  genres should keep the old WSJ files apart.
* **spaCy sees the debris.** Tokens such as `RSQUOs` and `<disfluency>` go
  to it unchanged. Mapping them back to characters in
  `tools/frames/src/frames/parse.py` would help it a little.
* **The copy has junk files.** It includes macOS `._` files and an `Icon`
  file. `masctrees.masc_files` already skips them.

## Reproducing

```bash
python3 tools/masc/provenance.py $MASC/data
```

The modification times are only as good as the copy. They survive zip and
Dropbox, but not every other way of copying files.
