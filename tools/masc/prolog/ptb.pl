% Rules over what tools/masc/ptb2pl.py writes for a MASC Penn Treebank tree.
%
% This is odd_one_out's dep2tiger/prolog/tiger.pl for the Penn Treebank; the notation, the
% expansion of ---> rules into clauses and the scan step are taken from there, and its
% comments say why they are as they are. docs/masc-prolog.md says what differs and why.
%
% A sentence file holds sentence(Words), the words in order, w(Tag,Form) or
% w(Tag,Form,Lemma); guidelines(G); root(R); and a grammar whose nonterminals are the
% tree's nodes, one ---> rule each, so that the grammar has one derivation, the tree:
%
%     s(s2)   ---> [sbj:np(np4), ^'--':vp(vp2)].
%     np(np5) ---> [^'--':e('*T*', whnp1)].
%
% A daughter is Label:Daughter: a preterminal t(Tag), a constituent, or an empty element
% e(Kind) or, co-indexed with a constituent, e(Kind, Antecedent). An empty element reads no
% words. ^ on a label marks the head daughter. Facts tags(Name, Tags) and gap(Name, Name)
% say what the rules do not.
%
% Everything one wants to know about the tree is a TRANSFORMATION: a scan of the grammar
% over the sentence from the root down. This file holds the scan step, daughters/3, and
% these transformations:
%
%     analysis(Facts)        the tree as ground facts
%     tree(T)                the tree as a term
%     reattached(Program)    each displaced constituent (*T*, *ICH*, *EXP*) moved to its
%                            trace: a program of the same form whose rules may have several
%                            runs, a linear context-free rewriting system, like TIGER's
%
% Penn Treebank trees are continuous, so every rule the converter writes has one run. The
% expansion takes rules of several runs, as tiger.pl does, because the reattached programs
% have them, and they are loaded and scanned like any other.

:- encoding(utf8).

:- op(1200, xfx, --->).
:- op(200, xfx, @).                % Daughter@K: the K-th run of a daughter
:- op(300, fy, ^).                 % ^Label: the daughter the constituent is headed by
:- multifile user:term_expansion/2.
% a sentence file gives them; tags/2, gap/2 and moved/2 only where there are any
:- multifile sentence/1, root/1, rule/2, guidelines/1, tags/2, gap/2, moved/2.
:- dynamic tags/2, gap/2, moved/2.
% hooks for other schemes' programs (tools/prolog: CGELBank, UD, spaCy): kinds of empty
% element that mark a displaced constituent, and facts that analysis/1 and reattached/1
% pass on beside tags/2 and gap/2. A MASC program defines neither.
:- multifile displacement/1, extra_fact/1.
:- dynamic extra_fact/1.


% ---> RULES (as tiger.pl)

% Head ---> Rhs becomes rule(Head, Runs) and a clause for Head with a difference-list pair
% per run, whose body reads the daughters in order.
user:term_expansion((Head ---> Rhs),
                    [(:- discontiguous rule/2), (:- discontiguous F/N), rule(Head, Runs), (Head1 :- Body)]) :-
    is_list(Rhs),
    rule_runs(Rhs, Runs),
    Head =.. [F|Args],
    length(Runs, K), length(Args, N0), N is N0 + 2*K,
    run_pairs(Runs, Pairs, PairArgs),
    append(Args, PairArgs, Args1),
    Head1 =.. [F|Args1],
    laid_out(Runs, Pairs, Daughters),
    maplist(goal, Daughters, Goals),
    conj(Goals, Body).

run_pairs([], [], []).
run_pairs([_|Rs], [S0-S|Ps], [S0,S|As]) :- run_pairs(Rs, Ps, As).

runs_args([], []).
runs_args([S0-S|Rs], [S0,S|As]) :- runs_args(Rs, As).

conj([], true).
conj([G], G) :- !.
conj([G|Gs], (G, Rest)) :- conj(Gs, Rest).

goal(daughter(L, _, D, Pairs), Goal) :- runs_args(Pairs, Args), Goal =.. [d, L, D|Args].

% laid_out(Runs, Pairs, Daughters): each daughter(Label, Mark, D, DPairs), its pairs
% threaded through the rule's; the items D@K of one daughter are its runs, in order.
laid_out(Runs, Pairs, Daughters) :-
    pieces(Runs, Pairs, Pieces),
    grouped(Pieces, Daughters).

pieces([], [], []).
pieces([Run|Runs], [S0-S|Pairs], Pieces) :-
    run_pieces(Run, S0, S, Here),
    pieces(Runs, Pairs, More),
    append(Here, More, Pieces).

rule_runs(Rhs, Runs) :- Rhs = [Item|_], \+ is_list(Item), !, Runs = [Rhs].
rule_runs(Rhs, Rhs).

run_pieces([], S, S, []).
run_pieces([Item], S0, S, [piece(Item, S0-S)]) :- !.
run_pieces([Item|Items], S0, S, [piece(Item, S0-S1)|Pieces]) :-
    run_pieces(Items, S1, S, Pieces).

grouped([], []).
grouped([piece(Item, Pair)|Pieces], [daughter(L, Mark, D, [Pair|Pairs])|Daughters]) :-
    item(Item, L, D, K, Mark),
    later_runs(K, L, D, Pieces, Pairs, Rest),
    grouped(Rest, Daughters).

later_runs(K, _, _, Pieces, Pairs, Rest) :- K == 0, !, Pairs = [], Rest = Pieces.
later_runs(_, L, D, Pieces, Pairs, Rest) :-
    partition(run_of(L, D), Pieces, Later, Rest),
    maplist(piece_pair, Later, Pairs).

run_of(L, D, piece(Item, _)) :- item(Item, L1, D1, K, _), K > 0, L1 == L, D1 == D.

piece_pair(piece(_, Pair), Pair).

% item(Item, Label, D, K, Mark): Label:D or Label:D@K; K is 0 for a daughter of one run
item(L0:X, L, D, K, Mark) :- !, marked(L0, L, Mark), daughter(X, D, K).
item(X, '--', D, K, dep) :- daughter(X, D, K).

marked(^L0, L, Mark) :- !, L = L0, Mark = head.
marked(L, L, dep).

daughter(D@K, D, K) :- !.
daughter(D, D, 0).


% DAUGHTERS AND WORDS

d(_,D) --> D.
:- forall(between(2, 6, K),
          ( N is 2*K, length(Ls, N),
            Head =.. [d, _, C|Ls], Body =.. [call, C|Ls],
            assertz((Head :- Body)) )).
t(T) --> [W], { when(nonvar(W), tag(W,T)) }.
% an empty element reads no words
e(_) --> [].
e(_,_) --> [].

tag(w(T,_),T).
tag(w(T,_,_),T).
form(w(_,F),F).
form(w(_,F,_),F).
lemma(w(_,F),F).
lemma(w(_,_,L),L).

word_at(I, W) :- sentence(S), nth1(I, S, W).

% position(S, S0, I): S0 is the sentence from its I-th word on.
position(S, S0, I) :- length(S, N), length(S0, N0), I is N - N0 + 1.

range(S, S0-S1, I0-I1) :- position(S, S0, I0), position(S, S1, I), I1 is I - 1.


% THE SCAN

% daughters(C, Pairs, Daughters): C parsed over its pairs, its daughters handed back with
% their pairs bound. The scan starts at top, whose one daughter is the root.
daughters(C, Pairs, Daughters) :-
    runs_of(C, Runs),
    laid_out(Runs, Pairs, Daughters),
    maplist(read_daughter, Daughters).

read_daughter(Daughter) :- goal(Daughter, Goal), call(Goal).

runs_of(top, [['--':C]]) :- !, root(N), rule(C, _), C =.. [_, N], !.
runs_of(C, Runs) :- rule(C, Runs).

% words(C, Words), runs(C, Runs): a constituent's words, all together or by run
words(C, Words) :- runs(C, Runs), append(Runs, Words).
runs(C, Runs) :- pairs(C, Pairs), maplist(run_words, Pairs, Runs).
run_words(S0-S, Run) :- append(Run, S, S0).
pairs(C, Pairs) :- sentence(S), within(top, [S-[]], C, Pairs).
within(C, Pairs, C, Pairs).
within(C0, Pairs0, C, Pairs) :-
    daughters(C0, Pairs0, Daughters),
    member(daughter(_, _, D, DPairs), Daughters),
    constituent(D),
    within(D, DPairs, C, Pairs).

constituent(D) :- D \= t(_), D \= e(_), D \= e(_,_).


% THE ANALYSIS AS GROUND FACTS

% analysis(Facts): the tree as ground facts. Words are numbered from 1, constituents go by
% name, and an empty element by its parent and its place among the parent's daughters,
% e(vp2, 2).
%
%   guidelines(revised)
%   root(s1)
%   constituent(np2, np, [3-9])           name, category, runs as position ranges; a
%                                         constituent over an empty element has [I-J], J < I
%   tags(s2, [nom])                       the function tags that are not edge labels
%   edge(vp2, tmp, np6)                   parent, label, daughter: a constituent, a word
%   edge(np5, '--', e(np5, 1))            position, or an empty element
%   head(vp2, 8)                          the daughter vp2 is headed by
%   empty(e(np5, 1), '*T*', 9)            an empty element, its kind, and the position of
%                                         the word it stands before (the length + 1 at the end)
%   antecedent(e(np5, 1), whnp1)          the constituent it is co-indexed with
%   gap(np3, 1)                           a constituent marked for gapping, and the
%                                         constituent it refers to, or the number it
%                                         shares with its parallels
%   word(8, found)  tag(8, vbd)  lemma(8, find)
analysis(Facts) :-
    sentence(S),
    guidelines(G),
    daughters(top, [S-[]], [daughter(_, _, C, Pairs)]),
    C =.. [_, Root],
    phrase(constituent_facts(C, Pairs, S), Facts0, Rest),
    findall(F, other_fact(F), Others),
    findall(F, word_fact(S, F), WordFacts),
    append(Others, WordFacts, Rest),
    Facts = [guidelines(G), root(Root)|Facts0].

constituent_facts(C, Pairs, S) -->
    { C =.. [Cat, Name],
      daughters(C, Pairs, Daughters),
      maplist(range(S), Pairs, Ranges) },
    [constituent(Name, Cat, Ranges)],
    daughters_facts(Daughters, 1, Name, S).

daughters_facts([], _, _, _) --> [].
daughters_facts([Daughter|Daughters], J, Name, S) -->
    { Daughter = daughter(Label, Mark, D, DPairs), daughter_id(Daughter, S, Name, J, Id) },
    [edge(Name, Label, Id)],
    head_fact(Mark, Name, Id),
    own_facts(D, DPairs, S, Id),
    { J1 is J + 1 },
    daughters_facts(Daughters, J1, Name, S).

daughter_id(daughter(_, _, t(_), [S0-_]), S, _, _, I) :- !, position(S, S0, I).
daughter_id(daughter(_, _, E, _), _, Parent, J, e(Parent, J)) :- \+ constituent(E), !.
daughter_id(daughter(_, _, C, _), _, _, _, Name) :- C =.. [_, Name].

head_fact(Mark, Name, Id) --> { Mark == head }, !, [head(Name, Id)].
head_fact(_, _, _) --> [].

own_facts(t(_), _, _, _) --> !.
own_facts(e(Kind), [S0-_], S, Id) --> !, { position(S, S0, P) }, [empty(Id, Kind, P)].
own_facts(e(Kind, A), [S0-_], S, Id) --> !,
    { position(S, S0, P) }, [empty(Id, Kind, P), antecedent(Id, A)].
own_facts(C, DPairs, S, _) --> constituent_facts(C, DPairs, S).

other_fact(tags(N, T)) :- tags(N, T).
other_fact(gap(N, M)) :- gap(N, M).
other_fact(F) :- extra_fact(F).

word_fact(S, Fact) :-
    nth1(I, S, W),
    (   form(W, F), Fact = word(I, F)
    ;   tag(W, T), Fact = tag(I, T)
    ;   lemma(W, L), Fact = lemma(I, L)
    ).


% THE TREE AS A TERM

% tree(T): node(Cat, Name, Daughters), each daughter d(Label, Mark, X) with X a word
% w(I, Tag), an empty element e(Kind, Antecedent, P), Antecedent none where it has none
% and P the position of the word it stands before, or a node.
tree(T) :-
    sentence(S),
    daughters(top, [S-[]], [daughter(_, _, C, Pairs)]),
    tree(C, Pairs, S, T).

tree(C, Pairs, S, node(Cat, Name, Ds)) :-
    C =.. [Cat, Name],
    daughters(C, Pairs, Daughters),
    maplist(tree_daughter(S), Daughters, Ds).

tree_daughter(S, daughter(L, M, t(Tag), [S0-_]), d(L, M, w(I, Tag))) :- !, position(S, S0, I).
tree_daughter(S, daughter(L, M, e(K), [S0-_]), d(L, M, e(K, none, P))) :- !, position(S, S0, P).
tree_daughter(S, daughter(L, M, e(K, A), [S0-_]), d(L, M, e(K, A, P))) :- !, position(S, S0, P).
tree_daughter(S, daughter(L, M, C, Pairs), d(L, M, T)) :- tree(C, Pairs, S, T).

% tree_words(T, Ws): the positions of a tree's words, in order
tree_words(w(I, _), [I]) :- !.
tree_words(e(_, _, _), []) :- !.
tree_words(node(_, _, Ds), Ws) :-
    foldl(add_words, Ds, [], Ws0),
    msort(Ws0, Ws).

add_words(d(_, _, X), W0, W) :- tree_words(X, W1), append(W0, W1, W).


% REATTACHMENT: THE TRACES UNDONE

% reattached(Program): the program with each displaced constituent where its trace is.
% The Penn Treebank writes displacement with an empty element and co-indexation: a
% constituent stands where it is heard, and *T* (wh-movement, topicalisation), *ICH*
% ("interpret constituent here", extraposition) or *EXP* (the clause an expletive stands
% for) marks where it is interpreted. Moving the constituent to its trace makes the
% trees discontinuous, as TIGER's are: the conversion of the Penn Treebank for LCFRS
% parsing of Evang and Kallmeyer (2011).
%
% Each such trace in document order, whose antecedent has words and does not contain the
% trace, has the antecedent taken from its parent, which must keep a word, and put in the
% trace's place, under the trace's label, or its own where the trace's is '--'. Then every
% constituent's runs are read off its words, maximal stretches of consecutive positions,
% and each rule is written over them: a daughter's run stands in the parent's run that
% holds it, D@K where the daughter has several; an empty element stands before the word
% it stood before. The other empty elements, and the facts, stay as they were.
%
% Program is a list of clauses: sentence/1, guidelines/1, root/1, the rules, the tags/2
% and gap/2 facts, the extra_fact/1 facts of other schemes, and moved(Antecedent, Kind) for
% each constituent moved.
reattached(Program) :-
    sentence(S), guidelines(G),
    tree(T0),
    displaced(T0, Traces),
    foldl(move, Traces, T0-[], T-Moved0),
    reverse(Moved0, Moved),
    T = node(_, Root, _),
    rules(T, Rules),
    findall(tags(N, Ts), tags(N, Ts), Tags),
    findall(gap(N, M), gap(N, M), Gaps),
    findall(F, extra_fact(F), Extra),
    findall(moved(A, K), member(K-A, Moved), MovedFacts),
    append([[sentence(S), guidelines(G), root(Root)], Rules, Tags, Gaps, Extra, MovedFacts], Program).

displacement('*T*').
displacement('*ICH*').
displacement('*EXP*').

displaced(T, Traces) :- phrase(displaced(T), Traces).
displaced(node(_, _, Ds)) --> displaced_ds(Ds).
displaced_ds([]) --> [].
displaced_ds([d(_, _, X)|Ds]) --> displaced_x(X), displaced_ds(Ds).
displaced_x(w(_, _)) --> [].
displaced_x(e(K, A, _)) --> ( { A \== none, displacement(K) } -> [K-A] ; [] ).
displaced_x(N) --> { N = node(_, _, _) }, displaced(N).

% move(K-A, T0-M0, T-M): A moved to its trace of kind K, where it can be; else nothing
move(K-A, T0-M0, T-M) :-
    (   subtree(T0, A, Sub),
        tree_words(Sub, [_|_]),
        \+ holds_trace(Sub, K, A),
        detach(T0, A, T1, LA),
        attach(T1, K, A, LA, Sub, T)
    ->  M = [K-A|M0]
    ;   T = T0, M = M0
    ).

subtree(N, A, N) :- N = node(_, A, _), !.
subtree(node(_, _, Ds), A, Sub) :- member(d(_, _, X), Ds), X = node(_, _, _), subtree(X, A, Sub), !.

holds_trace(node(_, _, Ds), K, A) :-
    member(d(_, _, X), Ds),
    (   X = e(K1, A1, _), K1 == K, A1 == A
    ;   X = node(_, _, _), holds_trace(X, K, A)
    ), !.

% detach(T0, A, T, LA): T0 with A taken from its parent, which keeps a word; LA its label
detach(node(C, N, Ds0), A, node(C, N, Ds), LA) :-
    (   select(d(LA, _, node(_, A, _)), Ds0, Ds)
    ->  tree_words(node(C, N, Ds), [_|_])
    ;   append(Before, [d(L, M, X0)|After], Ds0),
        X0 = node(_, _, _),
        detach(X0, A, X, LA)
    ->  append(Before, [d(L, M, X)|After], Ds)
    ).

% attach(T0, K, A, LA, Sub, T): the first trace of kind K co-indexed with A replaced by Sub
attach(node(C, N, Ds0), K, A, LA, Sub, node(C, N, Ds)) :-
    append(Before, [D0|After], Ds0),
    attached(D0, K, A, LA, Sub, D), !,
    append(Before, [D|After], Ds).

attached(d(L, M, e(K1, A1, _)), K, A, LA, Sub, d(L1, M, Sub)) :-
    K1 == K, A1 == A, !,
    ( L == '--' -> L1 = LA ; L1 = L ).
attached(d(L, M, X0), K, A, LA, Sub, d(L, M, X)) :-
    X0 = node(_, _, _),
    attach(X0, K, A, LA, Sub, X).

% rules(T, Rules): a rule for each node of T, in document order
rules(T, Rules) :- phrase(rules(T), Rules).
rules(node(C, N, Ds)) -->
    { node_runs(node(C, N, Ds), Runs),
      foldl(daughter_pieces, Ds, 1-[], _-Pieces0),
      reverse(Pieces0, Pieces),
      maplist(run_items(Runs, Pieces), Runs, ItemRuns),
      Head =.. [C, N],
      ( ItemRuns = [Items] -> Rhs = Items ; Rhs = ItemRuns ) },
    [(Head ---> Rhs)],
    daughter_rules(Ds).

daughter_rules([]) --> [].
daughter_rules([d(_, _, X)|Ds]) -->
    ( { X = node(_, _, _) } -> rules(X) ; [] ),
    daughter_rules(Ds).

% node_runs(T, Runs): maximal stretches of consecutive positions, From-To; a node with no
% words has one run, at the place of its first empty element
node_runs(T, Runs) :-
    tree_words(T, Ws),
    (   Ws = []
    ->  first_point(T, P), P0 is P - 1, Runs = [P-P0]
    ;   stretches(Ws, Runs)
    ).

stretches([W|Ws], Runs) :- stretches(Ws, W, W, Runs).
stretches([], F, T, [F-T]).
stretches([W|Ws], F, T, Runs) :-
    (   W =:= T + 1
    ->  stretches(Ws, F, W, Runs)
    ;   Runs = [F-T|Rest], stretches(Ws, W, W, Rest)
    ).

first_point(e(_, _, P), P) :- !.
first_point(w(I, _), I) :- !.
first_point(node(_, _, [d(_, _, X)|_]), P) :- first_point(X, P).

% daughter_pieces(D, J0-P0, J-P): the pieces of a daughter, piece(Pos, Order, J, Item),
% sorted by position, an empty element (Order 0) before the word it stands before
daughter_pieces(d(L, M, X), J0-P0, J-P) :-
    J is J0 + 1,
    label_term(L, M, Lab),
    x_pieces(X, Lab, J0, Ps),
    reverse(Ps, RPs),
    append(RPs, P0, P).

label_term(L, head, ^L) :- !.
label_term(L, _, L).

x_pieces(w(I, Tag), Lab, J, [piece(I, 1, J, Lab:t(Tag))]).
x_pieces(e(K, A, P), Lab, J, [piece(P, 0, J, Lab:E)]) :- ( A == none -> E = e(K) ; E = e(K, A) ).
x_pieces(node(C, N, Ds), Lab, J, Ps) :-
    Term =.. [C, N],
    node_runs(node(C, N, Ds), Runs),
    tree_words(node(C, N, Ds), Ws),
    (   Ws = []
    ->  Runs = [P-_], Ps = [piece(P, 0, J, Lab:Term)]
    ;   Runs = [F-_]
    ->  Ps = [piece(F, 1, J, Lab:Term)]
    ;   findall(piece(F, 1, J, Lab:Term@K), nth1(K, Runs, F-_), Ps)
    ).

% run_items(Runs, Pieces, Run, Items): the items of the pieces that stand in Run, in order.
% A piece stands in the run that holds its position, an empty element also in the run it
% ends; one in a gap between runs, in the run before it, or the first.
run_items(Runs, Pieces, Run, Items) :-
    include(in_run(Runs, Run), Pieces, Mine),
    msort(Mine, Sorted),
    findall(I, member(piece(_, _, _, I), Sorted), Items).

in_run(Runs, Run, piece(Pos, _, _, _)) :- home(Runs, Pos, Home), Home == Run.

home(Runs, Pos, Run) :- member(Run, Runs), Run = F-T, Pos >= F, Pos =< T + 1, !.
home(Runs, Pos, Run) :- findall(F-T, (member(F-T, Runs), T < Pos), Before), last(Before, Run), !.
home([Run|_], _, Run).


% WRITING A PROGRAM

% write_program(Clauses): the clauses as a sentence file, readable after this one
write_program(Clauses) :-
    format(":- encoding(utf8).~n"),
    forall(member(C, Clauses), format("~q.~n", [C])).


% ONE QUERY OVER MANY FILES (as odd_one_out's scan.pl)

% swipl -q -g 'scan(analysis)' -t 'halt(1)' ptb.pl < files
scan(Query) :-
    read_string(user_input, _, S),
    split_string(S, "\n", "\n \t\r", Files),
    forall((member(F, Files), F \== ""), scanned(Query, F)),
    halt.

scanned(Query, File) :-
    load_files(File, [silent(true)]),
    format("# ~w~n", [File]),
    Goal =.. [Query, Answers],
    (   catch(call(Goal), E, (print_message(error, E), fail))
    ->  forall(member(A, Answers), (print(A), nl))
    ;   format("% failed~n")
    ),
    unload_file(File).
