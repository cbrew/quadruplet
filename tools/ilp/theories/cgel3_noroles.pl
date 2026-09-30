complement(A) :-
   next(A).
complement(A) :-
   obj_before(A), vn_prep(A).
complement(A) :-
   prep(A,to).
complement(A) :-
   cgel_lex(A).
complement(A) :-
   prep(A,of).
complement(A) :-
   prep(A,by), vtag(A,vbn), obj_before(A).
