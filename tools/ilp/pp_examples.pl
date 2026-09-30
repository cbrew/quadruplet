% The prepositional phrases of MASC's verbs, as examples for learning which are
% complements: a transformation over the sentence programs of tools/masc/ptb2pl.py, loaded
% after tools/masc/prolog/ptb.pl.
%
%     swipl -q -g 'scan(pp_examples)' -t 'halt(1)' ptb.pl pp_examples.pl < files
%
% pp_examples(Rows): one row for each PP that is a daughter of a lexical verb's VP, a VP
% headed by a word tagged VB* with no VP daughter (an auxiliary's VP has one):
%
%     pp(Class, PP, Lemma, VerbTag, Prep, ObjCat, ObjHead, ObjTag, Next, ObjBefore, Siblings,
%        Passive, Labels)
%
% Class is the treebank's: complement where the PP's labels include CLR, PUT or DTV,
% adjunct where they include none of these, nor PRD; a predicative PP, and a PP whose NP
% is the passive's agent (LGS), are left out.
%   Prep      the PP's head word, lower case: its preposition
%   ObjCat    the category of the PP's other daughter: np, s, sbar, ...; none if it has none
%   ObjHead   that daughter's head word, followed down the heads, lower case; trace where
%             it is an empty element (a stranded preposition)
%   ObjTag    that word's tag
%   Next      next if the PP comes straight after the verb, else later
%   ObjBefore np if an NP daughter of the VP stands between the verb and the PP, else none
%   Siblings  how many other PPs the VP has
%   Passive   passive if the verb is a participle with an empty NP object, else active
%   Labels    the PP's labels, for inspection

pp_examples(Rows) :-
    analysis(F),
    findall(Row, pp_row(F, Row), Rows).

pp_row(F, pp(Class, P, Lemma, VTag, Prep, ObjCat, ObjHead, ObjTag, Next, ObjBefore, NSib, Voice, Ls)) :-
    member(constituent(V, vp, _), F),
    member(head(V, I), F), integer(I),
    member(tag(I, VTag), F), sub_atom(VTag, 0, _, _, vb),
    \+ ( member(edge(V, _, D), F), member(constituent(D, vp, _), F) ),
    member(edge(V, L, P), F),
    member(constituent(P, pp, [From-_|_]), F),
    labels(L, Ls),
    class(Ls, Class),
    \+ agent(F, P),
    member(head(P, H), F), integer(H),
    member(word(H, W), F), downcase_atom(W, Prep),
    member(lemma(I, Lemma0), F), downcase_atom(Lemma0, Lemma),
    object(F, P, H, ObjCat, ObjHead, ObjTag),
    ( From =:= I + 1 -> Next = next ; Next = later ),
    ( member(edge(V, _, N), F), member(constituent(N, np, [NF-_|_]), F), NF > I, NF < From,
      N \== P -> ObjBefore = np ; ObjBefore = none ),
    aggregate_all(count, ( member(edge(V, _, Q), F), Q \== P, member(constituent(Q, pp, _), F) ), NSib),
    ( passive(F, V, VTag) -> Voice = passive ; Voice = active ).

labels('--', []) :- !.
labels(L, L) :- is_list(L), !.
labels(L, [L]).

class(Ls, _) :- memberchk(prd, Ls), !, fail.
class(Ls, complement) :- ( memberchk(clr, Ls) ; memberchk(put, Ls) ; memberchk(dtv, Ls) ), !.
class(_, adjunct).

agent(F, P) :- member(edge(P, lgs, _), F).

% object(F, P, H, Cat, Head, Tag): the PP's daughter other than its head
object(F, P, H, Cat, Head, Tag) :-
    (   member(edge(P, _, O), F), O \== H, \+ integer(O)
    ->  (   member(constituent(O, Cat, _), F)
        ->  head_word(F, O, Head, Tag)
        ;   Cat = empty, Head = trace, Tag = none
        )
    ;   Cat = none, Head = none, Tag = none
    ).

% head_word(F, C, Word, Tag): the word at the bottom of C's heads, or trace
head_word(F, C, Word, Tag) :-
    (   member(head(C, D), F)
    ->  (   integer(D)
        ->  member(word(D, W), F), downcase_atom(W, Word), member(tag(D, Tag), F)
        ;   D = e(_, _)
        ->  Word = trace, Tag = none
        ;   head_word(F, D, Word, Tag)
        )
    ;   Word = none, Tag = none
    ).

passive(F, V, vbn) :-
    member(edge(V, _, N), F), member(constituent(N, np, _), F),
    member(edge(N, _, e(N, 1)), F), member(empty(e(N, 1), '*', _), F), !.

% pp_tsv: the rows over many sentence files, as tab-separated lines, each led by its file
%
%     swipl -q -g pp_tsv -t 'halt(1)' ptb.pl pp_examples.pl < files > pp_examples.tsv
pp_tsv :-
    read_string(user_input, _, S),
    split_string(S, "\n", "\n \t\r", Files),
    forall(( member(File, Files), File \== "" ), pp_tsv_file(File)),
    halt.

pp_tsv_file(File) :-
    load_files(File, [silent(true)]),
    (   catch(pp_examples(Rows), _, fail)
    ->  forall(member(Row, Rows),
               ( Row =.. [pp|Args] ->
                 format("~w", [File]), forall(member(A, Args), format("\t~w", [A])), nl
               ; true ))
    ;   format(user_error, "failed: ~w~n", [File])
    ),
    unload_file(File).
