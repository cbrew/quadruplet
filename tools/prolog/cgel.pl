% Rules for the CGELBank programs of cgel2pl.py: tools/masc/prolog/ptb.pl, whose notation,
% scan and transformations they share, and what CGEL adds.
%
%     clause(clause1) ---> [prenucleus:np(np1), ^head:clause(clause2)].
%     vp(vp1) ---> [^head:t(v), obj:e(gap, np1)].
%     fused(dp1, np1, det).
%
% * A gap is an empty element of kind gap, co-indexed with its antecedent, so reattached/1
%   moves a prenucleus (or postnucleus) constituent to its gap, as it moves a Penn
%   constituent to its *T* trace: the CGEL tree as a linear context-free rewriting system.
%   An antecedent that is a word (x / V_aux, subject-auxiliary inversion) is named by its
%   position and stays where it is.
% * fused(Node, Upper, Function) is the second incoming edge of a node with fused
%   functions, which the tree leaves out (Pullum and Rogers's spanning tree; CGELBank's
%   format stores only the lower edge).
% * The other facts are what the .cgel file says beside the tree; cgel2pl.py lists them.

:- prolog_load_context(directory, Dir),
   atomic_list_concat([Dir, '/../masc/prolog/ptb.pl'], PTB),
   ensure_loaded(PTB).

:- multifile sent_id/1, text/1, sent/1, xpos/2, correct/2, subtokens/2, punct/3, note/2,
   fused/3.
:- dynamic sent_id/1, text/1, sent/1, xpos/2, correct/2, subtokens/2, punct/3, note/2,
   fused/3.

displacement(gap).

extra_fact(sent_id(S)) :- sent_id(S).
extra_fact(text(T)) :- text(T).
extra_fact(sent(T)) :- sent(T).
extra_fact(xpos(I, X)) :- xpos(I, X).
extra_fact(correct(I, F)) :- correct(I, F).
extra_fact(subtokens(I, S)) :- subtokens(I, S).
extra_fact(punct(I, W, M)) :- punct(I, W, M).
extra_fact(note(N, T)) :- note(N, T).
extra_fact(fused(N, U, F)) :- fused(N, U, F).


% functions(Facts): each node's function(s), function(Node, Parent, Function), with the
% fused nodes' second edge from fused/3: the CGEL structure as Pullum and Rogers's graph.
functions(Facts) :-
    analysis(A),
    findall(function(D, P, F), ( member(edge(P, F0, D), A), F0 \== '--', lower_part(F0, F) ), Fs0),
    findall(function(N, U, F), fused(N, U, F), Fs1),
    append(Fs0, Fs1, Fs),
    msort(Fs, Facts).

% the function a fused label gives the lower parent: det_head -> head, head_prenucleus ->
% prenucleus
lower_part(F0, F) :-
    atomic_list_concat([_, F], '_', F0),
    memberchk(F0, [det_head, mod_head, marker_head, head_prenucleus]), !.
lower_part(F, F).
