% A learned theory of complement/1 on the held-out examples build.py writes to test.pl.
%
%     swipl -q -g 'evaluate(Theory, Test, Train)' -t 'halt(1)' evaluate.pl
%
% Theory is the file Aleph's write_rules/1 wrote. A PP is predicted a complement where a
% clause of the theory covers it; ground clauses, which Aleph adds for examples it found no
% rule for, cover nothing held out and are dropped. Prints the scores, overall and on the
% examples whose (lemma, prep) pair no training example had (Train names the training
% background, complement.b, for that), and each clause's precision and coverage on the
% held-out examples.

:- dynamic complement/1, lemma/2, obj_head/2, prep/2, verb_sense/2, obj_sense/2, obj_cat/2,
           vtag/2, next/1, obj_before/1, other_pp/1, passive/1, example/4, trained/2.

evaluate(Theory, Test, Train) :-
    load_files(Test, [silent(true)]),
    read_terms(Theory, Clauses0),
    include(rule, Clauses0, Clauses),
    length(Clauses, NC),
    forall(member(C, Clauses), assertz(C)),
    read_terms(Train, TrainTerms),
    forall(( member(lemma(E, L), TrainTerms), member(prep(E, P), TrainTerms) ),
           ( trained(L, P) -> true ; assertz(trained(L, P)) )),
    format("~d clauses~n", [NC]),
    findall(G-S, ( example(E, Cls, _, _), gold(Cls, G), predicted(E, S) ), All),
    report('all held-out', All),
    findall(G-S, ( example(E, Cls, _, _), lemma(E, L), prep(E, P), \+ trained(L, P),
                   gold(Cls, G), predicted(E, S) ), Unseen),
    report('(lemma, prep) unseen in training', Unseen),
    format("~nclauses, with precision and coverage on the held-out examples:~n"),
    forall(nth1(I, Clauses, (complement(X) :- Body)),
           ( aggregate_all(count, ( example(X, _, _, _), call(Body) ), N),
             aggregate_all(count, ( example(X, complement, _, _), call(Body) ), K),
             ( N > 0 -> Pr is 100 * K / N ; Pr = 0 ),
             copy_term((complement(X) :- Body), C), numbervars(C, 0, _),
             format("~t~d~4|  ~1f% of ~d  ~p~n", [I, Pr, N, C]) )),
    halt.

rule((complement(_) :- _)).

gold(complement, true).
gold(adjunct, false).

predicted(E, S) :- ( complement(E) -> S = true ; S = false ).

read_terms(File, Terms) :-
    setup_call_cleanup(open(File, read, In), read_all(In, Terms), close(In)).

read_all(In, Terms) :-
    read_term(In, T, []),
    (   T == end_of_file
    ->  Terms = []
    ;   T = (:- _)
    ->  read_all(In, Terms)
    ;   Terms = [T|Rest], read_all(In, Rest)
    ).

report(Name, Pairs) :-
    length(Pairs, N),
    aggregate_all(count, member(true-true, Pairs), TP),
    aggregate_all(count, member(false-true, Pairs), FP),
    aggregate_all(count, member(true-false, Pairs), FN),
    aggregate_all(count, ( member(G-S, Pairs), G == S ), Right),
    Acc is 100 * Right / max(1, N),
    P is 100 * TP / max(1, TP + FP),
    R is 100 * TP / max(1, TP + FN),
    F is 2 * P * R / max(1.0e-9, P + R),
    format("~w (~d): accuracy ~1f  complement P ~1f R ~1f F ~1f~n", [Name, N, Acc, P, R, F]).
