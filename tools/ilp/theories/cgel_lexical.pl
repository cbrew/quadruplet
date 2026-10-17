complement(A) :-
   next(A).
complement(A) :-
   prep(A,into).
complement(A) :-
   prep(A,of), obj_before(A).
complement(A) :-
   verb_sense(A,'verb.motion'), obj_cat(A,none).
complement(A) :-
   prep(A,to).
complement(A) :-
   prep(A,from), obj_before(A).
complement(A) :-
   obj_before(A), lemma(A,put).
complement(A) :-
   prep(A,about), verb_sense(A,'verb.communication').
complement(A) :-
   prep(A,on), verb_sense(A,'verb.contact'), obj_before(A).
complement(A) :-
   prep(A,with), verb_sense(A,'verb.contact'), obj_before(A).
complement(A) :-
   obj_cat(A,np), lemma(A,take).
complement(A) :-
   prep(A,for), verb_sense(A,'verb.possession'), obj_before(A).
complement(A) :-
   prep(A,from), verb_sense(A,'verb.motion').
complement(A) :-
   prep(A,with), lemma(A,provide).
complement(A) :-
   prep(A,with), verb_sense(A,'verb.change'), obj_before(A).
complement(A) :-
   verb_sense(A,'verb.contact'), obj_sense(A,none), obj_before(A).
complement(A) :-
   prep(A,for), lemma(A,make).
complement(A) :-
   prep(A,by), obj_before(A), passive(A).
complement(A) :-
   prep(A,for), lemma(A,thank).
complement(A) :-
   prep(A,as), verb_sense(A,'verb.perception'), obj_cat(A,np).
complement(A) :-
   prep(A,toward).
complement(A) :-
   prep(A,for), lemma(A,use).
complement(A) :-
   obj_cat(A,np), vtag(A,vb), lemma(A,call).
complement(A) :-
   prep(A,out), vtag(A,vbd).
complement(A) :-
   prep(A,at), obj_cat(A,np), lemma(A,look).
