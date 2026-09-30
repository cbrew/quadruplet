complement(A) :-
   next(A).
complement(A) :-
   prep(A,into).
complement(A) :-
   prep(A,of), obj_before(A).
complement(A) :-
   prep(A,on), lemma(A,give).
complement(A) :-
   verb_sense(A,'verb.motion'), obj_cat(A,none).
complement(A) :-
   verb_sense(A,'verb.change'), obj_sense(A,'noun.artifact'), vtag(A,vb).
complement(A) :-
   prep(A,to).
complement(A) :-
   prep(A,from), obj_before(A).
complement(A) :-
   verb_sense(A,'verb.competition'), vtag(A,vbd), obj_before(A).
complement(A) :-
   prep(A,on), obj_sense(A,'noun.state'), vtag(A,vbd).
complement(A) :-
   obj_before(A), lemma(A,put).
complement(A) :-
   prep(A,about), verb_sense(A,'verb.communication').
complement(A) :-
   prep(A,on), verb_sense(A,'verb.contact'), obj_before(A).
complement(A) :-
   prep(A,with), verb_sense(A,'verb.contact'), obj_before(A).
complement(A) :-
   obj_sense(A,trace), obj_cat(A,np), obj_before(A).
complement(A) :-
   prep(A,for), other_pp(A), lemma(A,go).
complement(A) :-
   prep(A,about), verb_sense(A,'verb.cognition').
complement(A) :-
   prep(A,at), obj_before(A), lemma(A,take).
complement(A) :-
   prep(A,for), verb_sense(A,'verb.possession'), obj_before(A).
complement(A) :-
   prep(A,with), lemma(A,go).
complement(A) :-
   prep(A,from), verb_sense(A,'verb.motion').
complement(A) :-
   obj_head(A,wife).
complement(A) :-
   prep(A,with), lemma(A,provide).
complement(A) :-
   prep(A,with), verb_sense(A,'verb.change'), obj_before(A).
complement(A) :-
   obj_cat(A,np), lemma(A,throw).
complement(A) :-
   prep(A,at), lemma(A,find).
complement(A) :-
   obj_cat(A,np), lemma(A,take).
complement(A) :-
   verb_sense(A,'verb.contact'), obj_sense(A,none), obj_before(A).
complement(A) :-
   prep(A,for), lemma(A,make).
complement(A) :-
   prep(A,at), lemma(A,get).
complement(A) :-
   prep(A,with), lemma(A,share).
complement(A) :-
   prep(A,with), lemma(A,combine).
complement(A) :-
   prep(A,as), lemma(A,view).
complement(A) :-
   obj_cat(A,np), lemma(A,care).
complement(A) :-
   prep(A,by), obj_before(A), passive(A).
complement(A) :-
   prep(A,in), lemma(A,say).
complement(A) :-
   prep(A,for), lemma(A,thank).
complement(A) :-
   prep(A,in), vtag(A,vbn), lemma(A,leave).
complement(A) :-
   prep(A,in), lemma(A,show).
complement(A) :-
   prep(A,in), lemma(A,serve).
complement(A) :-
   prep(A,about), verb_sense(A,'verb.stative'), obj_cat(A,np).
complement(A) :-
   prep(A,in), lemma(A,live).
complement(A) :-
   prep(A,on), lemma(A,focus).
complement(A) :-
   prep(A,on), lemma(A,spend).
complement(A) :-
   prep(A,on), lemma(A,work).
complement(A) :-
   prep(A,as), verb_sense(A,'verb.perception'), obj_cat(A,np).
complement(A) :-
   prep(A,out), vtag(A,vb), obj_before(A).
complement(A) :-
   prep(A,in), lemma(A,lead).
complement(A) :-
   prep(A,for), lemma(A,set).
complement(A) :-
   prep(A,by), obj_sense(A,unknown), vtag(A,vbn).
complement(A) :-
   prep(A,about), obj_before(A), lemma(A,do).
complement(A) :-
   obj_cat(A,np), vtag(A,vbg), lemma(A,build).
complement(A) :-
   obj_cat(A,np), vtag(A,vb), lemma(A,work).
complement(A) :-
   prep(A,at), obj_sense(A,pronoun).
complement(A) :-
   prep(A,in), lemma(A,go).
complement(A) :-
   prep(A,on), obj_sense(A,'noun.communication'), vtag(A,vbp).
complement(A) :-
   prep(A,in), lemma(A,look).
complement(A) :-
   obj_cat(A,np), vtag(A,vb), lemma(A,sign).
complement(A) :-
   verb_sense(A,'verb.motion'), obj_sense(A,trace).
complement(A) :-
   prep(A,toward).
complement(A) :-
   prep(A,for), lemma(A,use).
complement(A) :-
   prep(A,there), vtag(A,vbd), copula(A).
complement(A) :-
   prep(A,on), lemma(A,get).
complement(A) :-
   prep(A,with), verb_sense(A,'verb.cognition'), vtag(A,vbg).
complement(A) :-
   prep(A,in), lemma(A,work).
complement(A) :-
   prep(A,in), obj_cat(A,np), lemma(A,include).
complement(A) :-
   obj_cat(A,np), vtag(A,vb), lemma(A,call).
complement(A) :-
   prep(A,on), verb_sense(A,'verb.creation'), vtag(A,vb).
complement(A) :-
   prep(A,with), lemma(A,leave).
complement(A) :-
   prep(A,for), obj_before(A), lemma(A,ask).
complement(A) :-
   verb_sense(A,'verb.cognition'), obj_sense(A,'noun.person'), obj_before(A).
complement(A) :-
   prep(A,through), verb_sense(A,'verb.motion'), vtag(A,vbg).
complement(A) :-
   prep(A,in), obj_before(A), lemma(A,hold).
complement(A) :-
   prep(A,as), obj_sense(A,'noun.group'), obj_before(A).
complement(A) :-
   prep(A,with), verb_sense(A,'verb.competition').
complement(A) :-
   prep(A,as), lemma(A,identify).
complement(A) :-
   prep(A,as), obj_before(A), lemma(A,describe).
complement(A) :-
   prep(A,as), other_pp(A), lemma(A,think).
complement(A) :-
   obj_cat(A,np), lemma(A,represent).
complement(A) :-
   prep(A,in), lemma(A,join).
complement(A) :-
   prep(A,up), verb_sense(A,'verb.motion').
complement(A) :-
   prep(A,down), verb_sense(A,'verb.motion'), obj_before(A).
complement(A) :-
   prep(A,across), verb_sense(A,'verb.motion'), obj_cat(A,np).
complement(A) :-
   prep(A,at), lemma(A,stare).
complement(A) :-
   vtag(A,vbd), obj_head(A,of).
complement(A) :-
   prep(A,off), verb_sense(A,'verb.contact').
complement(A) :-
   prep(A,at), vtag(A,vbg), lemma(A,look).
complement(A) :-
   prep(A,onto), verb_sense(A,'verb.contact'), obj_sense(A,'noun.artifact').
complement(A) :-
   prep(A,with), obj_sense(A,'noun.possession'), obj_before(A).
complement(A) :-
   verb_sense(A,'verb.motion'), obj_sense(A,'noun.body'), obj_before(A).
complement(A) :-
   prep(A,at), obj_before(A), lemma(A,point).
complement(A) :-
   prep(A,in), lemma(A,increase).
complement(A) :-
   prep(A,in), lemma(A,carry).
complement(A) :-
   prep(A,for), verb_sense(A,'verb.creation'), vtag(A,vbg).
complement(A) :-
   prep(A,for), obj_cat(A,s), vtag(A,vbd).
