% Tests for agenda.pl:  swipl -g run_tests -t halt tools/agenda/test_agenda.pl

:- use_module(agenda).
:- use_module(library(plunit)).
:- use_module(library(lists)).
:- use_module(library(readutil)).

% Kotlin's TreeAsFeatureGrammar (src/main/kotlin/com/cbrew/chart).
treeas([ rule(odd, [odd, odd, odd]), rule(odd, [odd, even, even]),
         rule(even, [odd, even, odd]), rule(even, [odd, odd, even]),
         rule(odd, [odd, even]), rule(even, [odd, odd]),
         rule(even, [even, even]), rule(odd, [even, odd]),
         rule(odd, [even, odd, even]), rule(odd, [even, even, odd]),
         rule(even, [even, even, even]), rule(even, [even, odd, odd]),
         lex(a, odd) ]).

% The textbook grammar of docs/fast-parser.md §1.
telescope([ rule(s, [np, vp]), rule(vp, [v, np]), rule(vp, [vp, pp]),
            rule(np, [np, pp]), rule(np, [det, n]), rule(pp, [p, np]),
            lex(i, np), lex(saw, v), lex(saw, n), lex(the, det),
            lex(man, n), lex(with, p), lex(telescope, n) ]).

% Agreement as a feature, and semantics built by unification.
features([ rule(s(S), [np(N, X), vp(N, X^S)]),
           rule(vp(N, X^S), [tv(N, Y^X^S), np(_, Y)]),
           rule(np(N, Q), [det(N, X^R^Q), n(N, X^R)]),
           lex(john, np(sg, john)), lex(nyc, np(sg, nyc)),
           lex(they, np(pl, they)),
           lex(sees, tv(sg, Y^X^see(X, Y))), lex(see, tv(pl, Y^X^see(X, Y))),
           lex(a, det(sg, X^R^exists(X, R))), lex(dog, n(sg, X^dog(X))) ]).

golden(Rows) :-
    read_file_to_string('../../go/testdata/golden/treeas.golden', Text, []),
    split_string(Text, "\n", "", Lines0),
    exclude(==(""), Lines0, Lines),
    maplist([Line, row(N, C, P, T)]>>(
                split_string(Line, "\t", "", Fields),
                maplist(number_string, [N, C, P, T], Fields)),
            Lines, Rows).

:- begin_tests(agenda).

% The Kotlin chart's tree counts for 1 to 20 a's.
test(treeas_golden, [forall((golden(Rows), member(row(N, _, _, T), Rows))),
                     true(Trees == T)]) :-
    treeas(G),
    length(Words, N), maplist(=(a), Words),
    chart(G, Words, Chart),
    solutions(Chart, N, Solutions),
    foldl([E, S0, S]>>(count_trees(Chart, E, C), S is S0 + C),
          Solutions, 0, Trees).

test(telescope_trees, Trees == 2) :-
    telescope(G),
    aggregate_all(count,
                  parse(G, [i, saw, the, man, with, the, telescope], s, _),
                  Trees).

test(telescope_tree, [nondet,
     true(T == node(s, [leaf(np, i),
                   node(vp, [leaf(v, saw),
                             node(np, [leaf(det, the), leaf(n, man)])])]))]) :-
    telescope(G),
    parse(G, [i, saw, the, man], s, T).

test(semantics, [nondet, true(Sem == see(john, nyc))]) :-
    features(G),
    parse(G, [john, sees, nyc], s(Sem), _).

test(quantifier, [nondet, true(Sem =@= see(john, exists(X, dog(X))))]) :-
    features(G),
    parse(G, [john, sees, a, dog], s(Sem), _).

test(agreement, fail) :-
    features(G),
    parse(G, [they, sees, john], s(_), _).

test(agreement_plural, [nondet, true(Sem == see(they, john))]) :-
    features(G),
    parse(G, [they, see, john], s(Sem), _).

test(left_recursion, Count == 1) :-
    G = [rule(l, [l, a]), rule(l, [a]), lex(a, a)],
    length(W, 20), maplist(=(a), W),
    chart(G, W, Chart),
    count_trees(Chart, complete(l, 0, 20), Count).

test(lexical_and_built, Trees == 2) :-
    G = [rule(np, [n]), lex(fish, np), lex(fish, n)],
    aggregate_all(count, parse(G, [fish], np, _), Trees).

test(unknown_word, fail) :-
    telescope(G),
    parse(G, [i, saw, the, zebra], s, _).

test(deterministic) :-
    telescope(G),
    chart(G, [i, saw, the, man, with, the, telescope], _).

:- end_tests(agenda).
