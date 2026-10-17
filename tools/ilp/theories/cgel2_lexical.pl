complement(A) :-
   next(A).
complement(A) :-
   obj_cat(A,np), cgel_lex(A).
complement(A) :-
   prep(A,into), obj_before(A).
complement(A) :-
   prep(A,of), obj_before(A).
complement(A) :-
   verb_sense(A,'verb.motion'), obj_cat(A,none).
complement(A) :-
   prep(A,to), obj_before(A).
complement(A) :-
   prep(A,from), obj_before(A).
complement(A) :-
   obj_before(A), lemma(A,put).
complement(A) :-
   prep(A,about), verb_sense(A,'verb.communication').
complement(A) :-
   prep(A,with), verb_sense(A,'verb.contact'), obj_before(A).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.motion').
complement(A) :-
   obj_sense(A,none), cgel_lex(A).
complement(A) :-
   obj_cat(A,np), obj_before(A), lemma(A,take).
complement(A) :-
   prep(A,for), verb_sense(A,'verb.possession'), obj_before(A).
complement(A) :-
   prep(A,from), verb_sense(A,'verb.motion').
complement(A) :-
   prep(A,into), verb_sense(A,'verb.motion').
complement(A) :-
   prep(A,with), lemma(A,provide).
complement(A) :-
   prep(A,with), verb_sense(A,'verb.change'), obj_before(A).
complement(A) :-
   lemma(A,throw).
complement(A) :-
   obj_before(A), lemma(A,pull).
complement(A) :-
   prep(A,by), obj_before(A), passive(A).
complement(A) :-
   prep(A,for), lemma(A,thank).
complement(A) :-
   prep(A,out), obj_before(A), obj_head(A,of).
complement(A) :-
   verb_sense(A,'verb.contact'), obj_cat(A,none).
complement(A) :-
   obj_cat(A,np), obj_before(A), lemma(A,set).
complement(A) :-
   prep(A,toward).
complement(A) :-
   prep(A,for), lemma(A,use).
