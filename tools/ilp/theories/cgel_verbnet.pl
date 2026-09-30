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
   obj_sense(A,none), next(A), vn_class(A,'act-114').
complement(A) :-
   prep(A,with), next(A), vn_class(A,'act-114').
complement(A) :-
   prep(A,to), verb_sense(A,'verb.possession').
complement(A) :-
   prep(A,from), obj_cat(A,np), vn_spatial(A).
complement(A) :-
   obj_before(A), vn_class(A,'invest-13.5.4'), vn_spatial(A).
complement(A) :-
   prep(A,of), vn_group(A,'26'), vn_group(A,'51').
complement(A) :-
   prep(A,into), obj_before(A), vn_spatial(A).
complement(A) :-
   obj_cat(A,pp), obj_before(A), vn_spatial(A).
complement(A) :-
   prep(A,for), vn_class(A,'inquire-37.1.2').
complement(A) :-
   prep(A,to), vn_spatial(A).
complement(A) :-
   prep(A,in), vn_class(A,'admire-31.2'), vn_class(A,'conjecture-29.5').
complement(A) :-
   prep(A,into), vn_class(A,'other_cos-45.4').
complement(A) :-
   prep(A,to), verb_sense(A,'verb.cognition'), vn_class(A,'amalgamate-22.2').
complement(A) :-
   next(A), vn_group(A,'29'), vn_group(A,'87').
complement(A) :-
   prep(A,toward), vn_spatial(A).
complement(A) :-
   obj_cat(A,np), vn_class(A,'pit-10.7'), vn_group(A,'111').
complement(A) :-
   prep(A,at), verb_sense(A,'verb.perception'), vn_class(A,'peer-30.3').
complement(A) :-
   prep(A,at), obj_before(A), vn_group(A,'40').
