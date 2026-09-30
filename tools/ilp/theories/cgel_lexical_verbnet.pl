complement(A) :-
   vn_prep(A).
complement(A) :-
   next(A), vn_spatial(A).
complement(A) :-
   prep(A,to), vn_group(A,'105').
complement(A) :-
   prep(A,about), next(A).
complement(A) :-
   verb_sense(A,'verb.stative'), next(A).
complement(A) :-
   verb_sense(A,'verb.motion'), obj_cat(A,none).
complement(A) :-
   prep(A,by), obj_cat(A,np), vtag(A,vbn).
complement(A) :-
   prep(A,of), next(A).
complement(A) :-
   verb_sense(A,'verb.contact'), obj_cat(A,none).
complement(A) :-
   obj_sense(A,none), next(A), lemma(A,do).
complement(A) :-
   prep(A,with), next(A), vn_class(A,'act-114').
complement(A) :-
   prep(A,to), verb_sense(A,'verb.possession').
complement(A) :-
   prep(A,from), obj_cat(A,np), vn_spatial(A).
complement(A) :-
   obj_before(A), lemma(A,put), vn_spatial(A).
complement(A) :-
   prep(A,of), vn_group(A,'26'), vn_group(A,'51').
complement(A) :-
   prep(A,into), obj_before(A), vn_spatial(A).
complement(A) :-
   obj_cat(A,pp), obj_before(A), vn_spatial(A).
complement(A) :-
   prep(A,for), lemma(A,ask).
complement(A) :-
   prep(A,to), vn_spatial(A).
complement(A) :-
   prep(A,in), lemma(A,believe).
complement(A) :-
   prep(A,into), vn_class(A,'other_cos-45.4').
complement(A) :-
   prep(A,to), lemma(A,compare).
complement(A) :-
   next(A), lemma(A,get), obj_head(A,none).
complement(A) :-
   prep(A,toward), vn_spatial(A).
complement(A) :-
   obj_cat(A,np), lemma(A,head).
complement(A) :-
   prep(A,at), verb_sense(A,'verb.perception'), vn_class(A,'peer-30.3').
complement(A) :-
   prep(A,at), obj_before(A), vn_group(A,'40').
