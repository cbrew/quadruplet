% An active chart parser in pure Prolog, with the architecture of the Kotlin
% Chart (src/main/kotlin/com/cbrew/chart/Chart.kt). Edges come off an agenda;
% an edge new to the chart is added to it, and then a complete edge spawns the
% rules it is the left corner of and is paired with the partial edges that
% need it (pairwithpartials), and a partial edge is paired with the complete
% edges it needs (pairwithcompletes). The fundamental rule makes the new edges,
% and each records the pair it was made from, so the chart is a packed forest.
%
% No assert and no cut, if-then-else or negation in this file: the chart and
% the agenda are terms passed along, and choices are made by clause selection.
% Library predicates are used as they are, cuts and all.
%
% Categories are Prolog terms, unified with the occurs check. Edges are kept
% ground, their variables numbered, so that variants are the same edge and two
% edges are renamed apart (varnumbers/2) before they are combined:
%
%     complete(Cat, Start, End)
%     partial(Cat, Start, End, Needed)
%
% A grammar is a list of rule(Cat, [Cat|Cats]) and lex(Word, Cat) terms.
%
%     ?- G = [rule(s(S), [np(X), vp(X^S)]), rule(vp(X^S), [tv(Y^X^S), np(Y)]),
%             lex(john, np(john)), lex(mary, np(mary)),
%             lex(sees, tv(Y^X^see(X, Y)))],
%        parse(G, [john, sees, mary], s(Sem), _).
%     Sem = see(john, mary).

:- module(agenda,
          [ parse/4,            % +Grammar, +Words, ?Cat, -Tree
            chart/3,            % +Grammar, +Words, -Chart
            solutions/3,        % +Chart, +N, -Completes
            gettrees/3,         % +Chart, +Edge, -Tree
            count_trees/3       % +Chart, +Edge, -Count
          ]).

:- use_module(library(apply)).
:- use_module(library(assoc)).
:- use_module(library(lists)).
:- use_module(library(varnumbers)).

%!  parse(+Grammar, +Words, ?Cat, -Tree) is nondet.
%
%   Cat unifies with the category of an edge over all of Words, and Tree is a
%   tree of that edge: each pair, on backtracking.

parse(Grammar, Words, Cat, Tree) :-
    chart(Grammar, Words, Chart),
    length(Words, N),
    solutions(Chart, N, Solutions),
    member(Edge, Solutions),
    Edge = complete(Category, _, _),
    varnumbers(Category, Cat),
    gettrees(Chart, Edge, Tree).

%!  chart(+Grammar, +Words, -Chart) is det.
%
%   Chart is chart(Completes, Partials, Predecessors, Seen): completes by
%   start, partials by end, each edge's predecessors (newest first), and the
%   edges in the chart.

chart(Grammar, Words, Chart) :-
    empty_assoc(E),
    foldl(lookup(Grammar), Words, 0-chart(E, E, E, E)-[], _-Chart0-Agenda),
    agenda(Agenda, Grammar, Chart0, Chart).

% A word's categories, as complete edges.
lookup(Grammar, Word, Start-Chart0-Agenda0, End-Chart-Agenda) :-
    succ(Start, End),
    convlist(lexical(Word, Start, End), Grammar, Lexical),
    foldl(record, Lexical, Chart0, Chart),
    append(Lexical, Agenda0, Agenda).

lexical(Word, Start, End, lex(Word, Cat), complete(Category, Start, End)-word(Word)) :-
    canonical(Cat, Category).

agenda([], _, Chart, Chart).
agenda([Edge-_|Agenda0], Grammar, Chart0, Chart) :-
    add(Chart0, Edge, New, Chart1),
    step(New, Edge, Grammar, Chart1, Agenda0, Chart2, Agenda),
    agenda(Agenda, Grammar, Chart2, Chart).

% An agendum is Edge-Predecessor; the predecessor is recorded when the edge is
% made, so an edge made again only gains one.

step(false, _, _, Chart, Agenda, Chart, Agenda).
step(true, Edge, Grammar, Chart0, Agenda0, Chart, Agenda) :-
    made(Edge, Grammar, Chart0, Made),
    foldl(record, Made, Chart0, Chart),
    append(Made, Agenda0, Agenda).

made(complete(Cat, Start, End), Grammar, Chart, Made) :-
    spawn(Grammar, complete(Cat, Start, End), Spawned),
    pairwithpartials(Chart, complete(Cat, Start, End), Paired),
    append(Spawned, Paired, Made).
made(partial(Cat, Start, End, Needed), _, Chart, Made) :-
    pairwithcompletes(Chart, partial(Cat, Start, End, Needed), Made).

add(Chart0, Edge, New, Chart) :-
    Chart0 = chart(_, _, _, Seen),
    get(Seen, Edge, false, Present),
    add_(Present, Edge, New, Chart0, Chart).

add_(true, _, false, Chart, Chart).
add_(false, Edge, true, chart(Completes0, Partials0, Predecessors, Seen0),
     chart(Completes, Partials, Predecessors, Seen)) :-
    put_assoc(Edge, Seen0, true, Seen),
    index(Edge, Completes0-Partials0, Completes-Partials).

index(complete(Cat, Start, End), Completes0-Partials, Completes-Partials) :-
    push(Completes0, Start, complete(Cat, Start, End), Completes).
index(partial(Cat, Start, End, Needed), Completes-Partials0, Completes-Partials) :-
    push(Partials0, End, partial(Cat, Start, End, Needed), Partials).

% A spawned edge has no predecessor: it is the empty start of a rule.
record(Edge-Predecessor, Chart0, Chart) :-
    record_(Predecessor, Edge, Chart0, Chart).

record_(spawned, _, Chart, Chart).
record_(word(Word), Edge, Chart0, Chart) :-
    predecessor(Edge, word(Word), Chart0, Chart).
record_(Partial-Complete, Edge, Chart0, Chart) :-
    predecessor(Edge, Partial-Complete, Chart0, Chart).

predecessor(Edge, Predecessor, chart(Completes, Partials, Predecessors0, Seen),
            chart(Completes, Partials, Predecessors, Seen)) :-
    push(Predecessors0, Edge, Predecessor, Predecessors).

%!  spawn(+Grammar, +Complete, -Agenda) is det.
%
%   The empty partial edges, at Complete's start, of the rules whose first
%   daughter unifies with Complete's category.

spawn(Grammar, complete(Category, Start, _), Agenda) :-
    convlist(spawn_rule(Category, Start), Grammar, Agenda).

spawn_rule(Category, Start, rule(Cat, [First|Rest]),
           Partial-spawned) :-
    varnumbers(Category, Found),
    copy_term(First, Next),
    unify_with_occurs_check(Next, Found),
    canonical(partial(Cat, Start, Start, [First|Rest]), Partial).

%!  pairwithpartials(+Chart, +Complete, -Agenda) is det.
%!  pairwithcompletes(+Chart, +Partial, -Agenda) is det.
%
%   The edges the fundamental rule makes from the edge and those of the chart
%   it meets. A pair is made once, when the later of its edges is added.

pairwithpartials(chart(_, Partials, _, _), Complete, Agenda) :-
    Complete = complete(_, Start, _),
    get(Partials, Start, [], Meeting),
    convlist([Partial, Agendum]>>fundamental(Partial, Complete, Agendum),
             Meeting, Agenda).

pairwithcompletes(chart(Completes, _, _, _), Partial, Agenda) :-
    Partial = partial(_, _, End, _),
    get(Completes, End, [], Meeting),
    convlist([Complete, Agendum]>>fundamental(Partial, Complete, Agendum),
             Meeting, Agenda).

%!  fundamental(+Partial, +Complete, -Agendum) is semidet.

fundamental(Partial, Complete, Edge-(Partial-Complete)) :-
    varnumbers(Partial, partial(Cat, Start, _, [Next|Needed])),
    varnumbers(Complete, complete(Found, _, End)),
    unify_with_occurs_check(Next, Found),
    make_edge(Needed, Cat, Start, End, Made),
    canonical(Made, Edge).

make_edge([], Cat, Start, End, complete(Cat, Start, End)).
make_edge([N|Ns], Cat, Start, End, partial(Cat, Start, End, [N|Ns])).

%!  solutions(+Chart, +N, -Completes) is det.
%
%   The complete edges over all N words.

solutions(chart(Completes, _, _, _), N, Solutions) :-
    get(Completes, 0, [], Edges),
    convlist([complete(C, 0, N), complete(C, 0, N)]>>true, Edges, Solutions).

%!  gettrees(+Chart, +Edge, -Tree) is nondet.
%
%   Tree is a tree of Edge, each once, on backtracking: leaf(Cat, Word) for a
%   word, and node(Cat, Children); a spawned edge, with no predecessors, is
%   node(Cat, []). Categories are those of the edges, ground.

gettrees(Chart, Edge, Tree) :-
    Chart = chart(_, _, Predecessors, _),
    get(Predecessors, Edge, [], Newest),
    category(Edge, Cat),
    trees(Newest, Cat, Chart, Tree).

trees([], Cat, _, node(Cat, [])).
trees([P|Ps], Cat, Chart, Tree) :-
    reverse([P|Ps], Oldest),
    member(Predecessor, Oldest),
    tree(Predecessor, Cat, Chart, Tree).

tree(word(Word), Cat, _, leaf(Cat, Word)).
tree(Partial-Complete, Cat, Chart, node(Cat, Children)) :-
    gettrees(Chart, Partial, node(_, Children0)),
    gettrees(Chart, Complete, Child),
    append(Children0, [Child], Children).

category(complete(Cat, _, _), Cat).
category(partial(Cat, _, _, _), Cat).

%!  count_trees(+Chart, +Edge, -Count) is det.
%
%   The number of trees of Edge, memoized over the forest.

count_trees(Chart, Edge, Count) :-
    empty_assoc(Memo),
    count(Edge, Chart, Memo, _, Count).

count(Edge, Chart, Memo0, Memo, Count) :-
    get(Memo0, Edge, none, Known),
    count_(Known, Edge, Chart, Memo0, Memo, Count).

count_(known(Count), _, _, Memo, Memo, Count).
count_(none, Edge, Chart, Memo0, Memo, Count) :-
    Chart = chart(_, _, Predecessors, _),
    get(Predecessors, Edge, [], Pairs),
    counts(Pairs, Chart, Memo0, Memo1, Count),
    put_assoc(Edge, Memo1, known(Count), Memo).

counts([], _, Memo, Memo, 1).
counts([P|Ps], Chart, Memo0, Memo, Count) :-
    foldl(count_predecessor(Chart), [P|Ps], Memo0-0, Memo-Count).

count_predecessor(Chart, Predecessor, State0, State) :-
    count_predecessor_(Predecessor, Chart, State0, State).

count_predecessor_(word(_), _, Memo-Sum0, Memo-Sum) :- Sum is Sum0 + 1.
count_predecessor_(Partial-Complete, Chart, Memo0-Sum0, Memo-Sum) :-
    count(Partial, Chart, Memo0, Memo1, P),
    count(Complete, Chart, Memo1, Memo, C),
    Sum is Sum0 + P*C.

% A term with its variables numbered: variants have the same canonical form.
canonical(Term, Canonical) :-
    copy_term(Term, Canonical),
    numbervars(Canonical, 0, _).

% Key's value in Assoc, or Default if Key is not there, so that callers choose
% by clause selection rather than by failure.
get(Assoc, Key, Default, Value) :-
    findall(V, get_assoc(Key, Assoc, V), Found),
    found(Found, Default, Value).

found([], Default, Default).
found([Value], _, Value).

push(Assoc0, Key, Value, Assoc) :-
    get(Assoc0, Key, [], Values),
    put_assoc(Key, Assoc0, [Value|Values], Assoc).
