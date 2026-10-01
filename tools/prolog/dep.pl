% Rules for the dependency programs of ud2pl.py (Universal Dependencies) and clear2pl.py
% (spaCy's ClearNLP-style parses): tools/masc/prolog/ptb.pl, whose notation, scan and
% transformations they share, and the facts they add.
%
% A word with dependents heads a constituent named w and its position, under the functor
% of its tag; the word is its ^ daughter, and its dependents are daughters labelled with
% their relations. A non-projective arc makes a constituent discontinuous, and its rule has
% several runs, as in the reattached Penn programs and odd_one_out's TIGER programs.
%
%     verb(w5) ---> [verb(w7)@1, aux:t(aux), nsubj:t(propn), ^'--':t(verb), ccomp:verb(w7)@2].
%
% analysis/1 then gives the arcs as edge(Head, Relation, Dependent) with the constituent's
% name for a word that has dependents, and head(Name, Position) for the word it is.

:- prolog_load_context(directory, Dir),
   atomic_list_concat([Dir, '/../masc/prolog/ptb.pl'], PTB),
   ensure_loaded(PTB).

% what a program gives beside the tree (ud2pl.py, clear2pl.py say which)
:- multifile sent_id/1, text/1, xpos/2, upos/2, feats/2, mwt/3, edep/3, empty_node/4,
   misc/2, entity/3.
:- dynamic sent_id/1, text/1, xpos/2, upos/2, feats/2, mwt/3, edep/3, empty_node/4,
   misc/2, entity/3.

extra_fact(sent_id(S)) :- sent_id(S).
extra_fact(text(T)) :- text(T).
extra_fact(xpos(I, X)) :- xpos(I, X).
extra_fact(upos(I, U)) :- upos(I, U).
extra_fact(feats(I, F)) :- feats(I, F).
extra_fact(mwt(A, B, F)) :- mwt(A, B, F).
extra_fact(edep(I, H, R)) :- edep(I, H, R).
extra_fact(empty_node(I, F, L, U)) :- empty_node(I, F, L, U).
extra_fact(misc(I, M)) :- misc(I, M).
extra_fact(entity(A, B, L)) :- entity(A, B, L).


% arcs(Arcs): the tree back as arc(Dependent, Head, Relation) over word positions, the root
% with head 0 and relation root: the inverse of the conversion, for checking it.
arcs(Arcs) :-
    analysis(Facts),
    findall(N-I, member(head(N, I), Facts), Heads),
    memberchk(root(R), Facts), memberchk(R-RI, Heads),
    findall(arc(D, H, L),
            ( member(edge(N, L, X), Facts), L \== '--',
              memberchk(N-H, Heads),
              ( integer(X) -> D = X ; memberchk(X-D, Heads) ) ),
            Arcs0),
    msort([arc(RI, 0, root)|Arcs0], Arcs).
