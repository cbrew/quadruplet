complement(A) :-
   vn_prep(A).
complement(A) :-
   prep(A,by), obj_cat(A,np), vtag(A,vbn).
complement(A) :-
   prep(A,to).
complement(A) :-
   prep(A,from).
complement(A) :-
   obj_sense(A,pronoun), next(A).
complement(A) :-
   verb_sense(A,'verb.stative'), next(A).
complement(A) :-
   next(A), vn_group(A,'51').
complement(A) :-
   verb_sense(A,'verb.motion'), obj_cat(A,none).
complement(A) :-
   prep(A,of), next(A).
complement(A) :-
   verb_sense(A,'verb.contact'), obj_cat(A,none).
complement(A) :-
   obj_sense(A,none), next(A), lemma(A,do).
complement(A) :-
   cgel_lex(A).
complement(A) :-
   lemma(A,put), vn_spatial(A).
complement(A) :-
   verb_sense(A,'verb.perception'), next(A), vn_class(A,'peer-30.3').
complement(A) :-
   prep(A,of), vn_group(A,'26'), vn_group(A,'51').
complement(A) :-
   prep(A,into).
complement(A) :-
   lemma(A,throw), vn_spatial(A).
complement(A) :-
   prep(A,out), vn_spatial(A).
complement(A) :-
   next(A), vn_class(A,'put_spatial-9.2'), vn_group(A,'50').
complement(A) :-
   prep(A,off).
complement(A) :-
   prep(A,toward), vn_spatial(A).
complement(A) :-
   prep(A,with), next(A), lemma(A,do).
complement(A) :-
   prep(A,at), vtag(A,vbz), vn_group(A,'40').
