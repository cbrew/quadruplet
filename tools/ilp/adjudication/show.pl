% Each sampled PP in its sentence, for adjudication. Loaded after tools/masc/prolog/ptb.pl.
%
%     swipl -q -g show -t 'halt(1)' ptb.pl show.pl < items
%
% reads lines "Key<TAB>File<TAB>PP" and writes "Key<TAB>Verb<TAB>PP words<TAB>Sentence",
% the sentence with the verb in *stars* and the PP in [brackets].

show :-
    read_string(user_input, _, S),
    split_string(S, "\n", "\n\r", Lines),
    forall(( member(L, Lines), L \== "" ), show_line(L)),
    halt.

show_line(Line) :-
    split_string(Line, "\t", "", [Key, File, PPs]),
    atom_string(PP, PPs),
    load_files(File, [silent(true)]),
    analysis(F),
    member(edge(V, _, PP), F), member(head(V, I), F), integer(I), !,
    member(word(I, Verb), F),
    member(constituent(PP, pp, Ranges), F),
    findall(W, ( member(A-B, Ranges), between(A, B, K), member(word(K, W), F) ), PPWords),
    atomic_list_concat(PPWords, ' ', PPText),
    findall(T, ( member(word(K, W), F), marked(K, W, I, Ranges, T) ), Ts),
    atomic_list_concat(Ts, ' ', Sentence0),
    atomic_list_concat(Parts, '] [', Sentence0), atomic_list_concat(Parts, ' ', Sentence),
    format("~w\t~w\t~w\t~w~n", [Key, Verb, PPText, Sentence]),
    unload_file(File).

% a word with its marks: the verb in stars, a PP run in brackets
marked(K, W, I, Ranges, T) :-
    ( K =:= I -> atomic_list_concat(['*', W, '*'], W1) ; W1 = W ),
    ( member(A-_, Ranges), K =:= A -> atom_concat('[', W1, W2) ; W2 = W1 ),
    ( member(_-B, Ranges), K =:= B -> atom_concat(W2, ']', T) ; T = W2 ).
