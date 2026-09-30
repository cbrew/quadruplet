complement(A) :-
   prep(A,for), vtag(A,vbg), next(A).
complement(A) :-
   obj_sense(A,trace), obj_cat(A,np).
complement(A) :-
   prep(A,with), verb_sense(A,'verb.communication'), next(A).
complement(A) :-
   prep(A,about).
complement(A) :-
   prep(A,from), verb_sense(A,'verb.perception'), next(A).
complement(A) :-
   prep(A,of).
complement(A) :-
   prep(A,to), obj_sense(A,'noun.group'), obj_before(A).
complement(A) :-
   verb_sense(A,'verb.cognition'), vtag(A,vbn), next(A).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.communication'), next(A).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.possession').
complement(A) :-
   prep(A,at), verb_sense(A,'verb.perception'), next(A).
complement(A) :-
   verb_sense(A,'verb.stative'), obj_sense(A,clause), next(A).
complement(A) :-
   prep(A,on), verb_sense(A,'verb.stative'), next(A).
complement(A) :-
   prep(A,to), obj_sense(A,pronoun), obj_before(A).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.perception'), next(A).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.change'), passive(A).
complement(A) :-
   prep(A,for), verb_sense(A,'verb.perception'), next(A).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.contact'), passive(A).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.motion'), obj_sense(A,'noun.act').
complement(A) :-
   verb_sense(A,'verb.contact'), obj_sense(A,none), obj_cat(A,np).
complement(A) :-
   prep(A,into), verb_sense(A,'verb.social'), next(A).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.cognition'), obj_before(A).
complement(A) :-
   prep(A,as), verb_sense(A,'verb.stative'), next(A).
complement(A) :-
   verb_sense(A,'verb.stative'), obj_sense(A,'noun.event'), next(A).
complement(A) :-
   prep(A,from), verb_sense(A,'verb.stative'), next(A).
complement(A) :-
   prep(A,like), verb_sense(A,'verb.perception'), next(A).
