complement(A) :-
   prep(A,for), vtag(A,vbg), next(A).
complement(A) :-
   prep(A,to), lemma(A,lead).
complement(A) :-
   obj_cat(A,np), next(A), lemma(A,look).
complement(A) :-
   prep(A,about).
complement(A) :-
   prep(A,with), verb_sense(A,'verb.communication'), next(A).
complement(A) :-
   prep(A,into), lemma(A,turn).
complement(A) :-
   prep(A,from), verb_sense(A,'verb.perception'), next(A).
complement(A) :-
   obj_sense(A,trace), vtag(A,vbn), next(A).
complement(A) :-
   prep(A,of).
complement(A) :-
   lemma(A,give).
complement(A) :-
   verb_sense(A,'verb.cognition'), vtag(A,vbn), next(A).
complement(A) :-
   prep(A,to), lemma(A,add).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.communication'), next(A).
complement(A) :-
   prep(A,with), next(A), lemma(A,do).
complement(A) :-
   lemma(A,depend).
complement(A) :-
   lemma(A,put).
complement(A) :-
   next(A), lemma(A,result).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.possession').
complement(A) :-
   prep(A,for), lemma(A,pay).
complement(A) :-
   next(A), lemma(A,engage).
complement(A) :-
   prep(A,with), lemma(A,fill).
complement(A) :-
   prep(A,with), obj_cat(A,np), lemma(A,provide).
complement(A) :-
   prep(A,for), lemma(A,ask).
complement(A) :-
   prep(A,on), next(A), lemma(A,work).
complement(A) :-
   obj_cat(A,np), vtag(A,vbp), lemma(A,live).
complement(A) :-
   prep(A,to), obj_sense(A,pronoun), obj_before(A).
complement(A) :-
   prep(A,to), lemma(A,listen).
complement(A) :-
   lemma(A,believe).
complement(A) :-
   next(A), lemma(A,participate).
complement(A) :-
   obj_cat(A,np), next(A), lemma(A,sit).
complement(A) :-
   next(A), lemma(A,stand).
complement(A) :-
   prep(A,to), obj_sense(A,'noun.person'), other_pp(A).
complement(A) :-
   prep(A,into), verb_sense(A,'verb.social'), next(A).
complement(A) :-
   prep(A,in), lemma(A,involve).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.cognition'), obj_before(A).
complement(A) :-
   next(A), lemma(A,bring).
complement(A) :-
   prep(A,as), next(A), lemma(A,serve).
complement(A) :-
   next(A), lemma(A,call).
complement(A) :-
   next(A), lemma(A,lie).
complement(A) :-
   prep(A,to), lemma(A,correspond).
complement(A) :-
   prep(A,as), lemma(A,describe).
complement(A) :-
   prep(A,from), verb_sense(A,'verb.stative'), next(A).
complement(A) :-
   prep(A,for), lemma(A,account).
complement(A) :-
   prep(A,at), lemma(A,stare).
