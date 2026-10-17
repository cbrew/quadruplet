complement(A) :-
   next(A), vn_group(A,'13'), vn_prep(A).
complement(A) :-
   vn_group(A,'88'), vn_prep(A).
complement(A) :-
   prep(A,to), vn_class(A,'spend_time-104').
complement(A) :-
   next(A), vn_class(A,'peer-30.3'), vn_class(A,'rummage-35.5').
complement(A) :-
   prep(A,about).
complement(A) :-
   next(A), vn_class(A,'correspond-36.1.1').
complement(A) :-
   prep(A,into), vn_class(A,'convert-26.6.2'), vn_group(A,'109').
complement(A) :-
   prep(A,to), verb_sense(A,'verb.change'), vn_group(A,'48').
complement(A) :-
   next(A), vn_class(A,'admire-31.2').
complement(A) :-
   verb_sense(A,'verb.stative'), obj_sense(A,trace).
complement(A) :-
   prep(A,of).
complement(A) :-
   prep(A,on), vtag(A,vbn), vn_prep(A).
complement(A) :-
   prep(A,from), vtag(A,vbz), vn_class(A,'appear-48.1.1').
complement(A) :-
   prep(A,to), obj_sense(A,'noun.group'), vn_prep(A).
complement(A) :-
   verb_sense(A,'verb.cognition'), passive(A), vn_prep(A).
complement(A) :-
   prep(A,to), vn_group(A,'37'), vn_prep(A).
complement(A) :-
   prep(A,to), obj_sense(A,'noun.state'), next(A).
complement(A) :-
   prep(A,with), next(A), vn_class(A,'act-114').
complement(A) :-
   prep(A,on), vn_class(A,'rely-70').
complement(A) :-
   vn_class(A,'invest-13.5.4'), vn_spatial(A).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.possession'), obj_before(A).
complement(A) :-
   verb_sense(A,'verb.stative'), next(A), vn_group(A,'48').
complement(A) :-
   obj_cat(A,np), next(A), vn_class(A,'pay-68').
complement(A) :-
   vn_class(A,'contiguous_location-47.8'), vn_class(A,'other_cos-45.4'), vn_prep(A).
complement(A) :-
   verb_sense(A,'verb.stative'), obj_sense(A,pronoun), next(A).
complement(A) :-
   prep(A,from), vn_class(A,'discover-84'), vn_prep(A).
complement(A) :-
   prep(A,with), verb_sense(A,'verb.possession'), vn_class(A,'fulfilling-13.4.1').
complement(A) :-
   vn_class(A,'mix-22.1'), vn_prep(A).
complement(A) :-
   prep(A,for), vn_class(A,'inquire-37.1.2').
complement(A) :-
   prep(A,on), next(A), vn_group(A,'95').
complement(A) :-
   verb_sense(A,'verb.stative'), vtag(A,vbp), vn_group(A,'47').
complement(A) :-
   prep(A,from), vn_group(A,'64').
complement(A) :-
   prep(A,on), next(A), vn_class(A,'assuming_position-50').
complement(A) :-
   prep(A,to), verb_sense(A,'verb.cognition'), vn_class(A,'amalgamate-22.2').
complement(A) :-
   prep(A,upon), next(A).
complement(A) :-
   verb_sense(A,'verb.stative'), vn_group(A,'86'), vn_prep(A).
complement(A) :-
   next(A), vn_class(A,'earn-54.6'), vn_class(A,'establish-55.5').
complement(A) :-
   prep(A,to), vn_class(A,'confine-92'), vn_class(A,'send-11.1').
complement(A) :-
   prep(A,in), next(A), vn_class(A,'assuming_position-50').
complement(A) :-
   vn_group(A,'30'), vn_group(A,'47'), vn_prep(A).
complement(A) :-
   obj_cat(A,np), vn_class(A,'separate-23.1').
complement(A) :-
   prep(A,for), verb_sense(A,'verb.stative'), vn_none(A).
complement(A) :-
   prep(A,to), obj_sense(A,pronoun), obj_before(A).
complement(A) :-
   prep(A,at), vtag(A,vbg), vn_class(A,'peer-30.3').
