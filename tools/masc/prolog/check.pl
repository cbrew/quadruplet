% Checking the programs over the corpus, in one process: for each sentence file, how many
% analyses it has, and the same of the program reattached/1 makes of it.
%
%     swipl -q -g check -t 'halt(1)' ptb.pl check.pl < pairs
%
% reads lines "File<TAB>Out", writes the reattached program to Out, and prints a line
%
%     File  Analyses  Moved  AnalysesAfter  Discontinuous  MaxFanOut
%
% Analyses counts analysis/1's solutions up to two; each should be one, before and after.
% Discontinuous is how many constituents the reattached tree has with more than one run,
% and MaxFanOut the most runs any has.

check :-
    read_string(user_input, _, S),
    split_string(S, "\n", "\n \t\r", Lines),
    forall(( member(L, Lines), L \== "" ), check_line(L)),
    halt.

check_line(Line) :-
    split_string(Line, "\t", "", [File, Out]),
    (   catch(checked(File, Out, Row), E, ( print_message(error, E), fail ))
    ->  true
    ;   Row = failed, catch(unload_file(File), _, true)
    ),
    format("~w\t~w~n", [File, Row]).

checked(File, Out, Row) :-
    load_files(File, [silent(true)]),
    aggregate_all(count, limit(2, analysis(_)), N),
    (   reattached(P)
    ->  aggregate_all(count, member(moved(_, _), P), Moved),
        unload_file(File),
        setup_call_cleanup(open(Out, write, St, [encoding(utf8)]), with_output_to(St, write_program(P)), close(St)),
        load_files(Out, [silent(true)]),
        aggregate_all(count, limit(2, analysis(_)), NB),
        (   analysis(F)
        ->  findall(K, ( member(constituent(_, _, R), F), length(R, K), K > 1 ), Ks),
            length(Ks, D), max_list([1|Ks], MaxK)
        ;   D = -1, MaxK = -1
        ),
        unload_file(Out)
    ;   unload_file(File), Moved = -1, NB = -1, D = -1, MaxK = -1
    ),
    format(atom(Row), "~w\t~w\t~w\t~w\t~w", [N, Moved, NB, D, MaxK]).
