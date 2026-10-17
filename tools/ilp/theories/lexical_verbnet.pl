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
   next(A), lemma(A,live), vn_spatial(A).
complement(A) :-
   prep(A,of).
complement(A) :-
   prep(A,on), vtag(A,vbn), vn_prep(A).
complement(A) :-
   prep(A,for), lemma(A,wait).
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
   prep(A,with), next(A), lemma(A,do).
complement(A) :-
   lemma(A,depend).
complement(A) :-
   lemma(A,put), vn_spatial(A).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.possession'), obj_before(A).
complement(A) :-
   verb_sense(A,'verb.stative'), next(A), vn_group(A,'48').
complement(A) :-
   obj_cat(A,np), next(A), vn_class(A,'pay-68').
complement(A) :-
   lemma(A,fill), vn_prep(A).
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
   prep(A,from), vn_group(A,'64').
complement(A) :-
   next(A), lemma(A,participate).
complement(A) :-
   obj_cat(A,np), next(A), lemma(A,sit).
complement(A) :-
   lemma(A,compare).
complement(A) :-
   verb_sense(A,'verb.cognition'), obj_cat(A,np), vn_group(A,'97').
complement(A) :-
   verb_sense(A,'verb.stative'), vn_group(A,'86'), vn_prep(A).
complement(A) :-
   next(A), lemma(A,bring).
complement(A) :-
   lemma(A,send), vn_prep(A).
complement(A) :-
   next(A), lemma(A,lie).
complement(A) :-
   vn_group(A,'30'), vn_group(A,'47'), vn_prep(A).
complement(A) :-
   obj_cat(A,np), vn_class(A,'separate-23.1').
complement(A) :-
   lemma(A,describe), vn_prep(A).
complement(A) :-
   prep(A,for), lemma(A,account).
complement(A) :-
   prep(A,to), obj_sense(A,pronoun), obj_before(A).
complement(A) :-
   prep(A,at), lemma(A,stare).
