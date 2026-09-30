complement(A) :-
   next(A).
complement(A) :-
   role(A,theme).
complement(A) :-
   role(A,attribute).
complement(A) :-
   role(A,source).
complement(A) :-
   obj_cat(A,np), obj_before(A), role(A,predicative).
complement(A) :-
   role(A,goal).
complement(A) :-
   role(A,path).
complement(A) :-
   cgel_lex(A).
complement(A) :-
   role(A,patient).
complement(A) :-
   role(A,causer).
