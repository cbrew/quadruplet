complement(A) :-
   vn_prep(A).
complement(A) :-
   next(A), vn_spatial(A).
complement(A) :-
   prep(A,to), vn_group(A,'105').
complement(A) :-
   verb_sense(A,'verb.perception'), next(A), vn_class(A,'marvel-31.3').
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
   obj_cat(A,s), next(A), vn_class(A,'intend-61.2').
complement(A) :-
   obj_before(A), vn_class(A,'invest-13.5.4'), vn_spatial(A).
complement(A) :-
   prep(A,of), vn_group(A,'26'), vn_group(A,'51').
complement(A) :-
   obj_cat(A,none), vn_spatial(A).
complement(A) :-
   prep(A,into), obj_before(A), vn_spatial(A).
complement(A) :-
   obj_cat(A,np), vn_class(A,'throw-17.1'), vn_group(A,'111').
complement(A) :-
   obj_sense(A,'noun.state'), vn_group(A,'9'), vn_spatial(A).
complement(A) :-
   obj_cat(A,pp), obj_before(A), vn_spatial(A).
complement(A) :-
   prep(A,for), vn_class(A,'inquire-37.1.2').
complement(A) :-
   prep(A,to), obj_cat(A,np), vn_spatial(A).
complement(A) :-
   prep(A,in), vn_class(A,'admire-31.2'), vn_class(A,'conjecture-29.5').
complement(A) :-
   prep(A,to), verb_sense(A,'verb.motion'), vtag(A,vbn).
complement(A) :-
   prep(A,into), vn_class(A,'other_cos-45.4').
complement(A) :-
   prep(A,with), obj_sense(A,'noun.communication'), vn_class(A,'become-109.1').
complement(A) :-
   prep(A,about), verb_sense(A,'verb.stative'), obj_cat(A,np).
complement(A) :-
   prep(A,like), verb_sense(A,'verb.perception'), next(A).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.cognition'), vn_class(A,'amalgamate-22.2').
complement(A) :-
   prep(A,upon), vn_class(A,'base-97.1').
complement(A) :-
   prep(A,against), vn_group(A,'29').
complement(A) :-
   prep(A,about), obj_sense(A,none), obj_before(A).
complement(A) :-
   prep(A,to), verb_sense(A,'verb.social'), obj_sense(A,pronoun).
complement(A) :-
   verb_sense(A,'verb.cognition'), obj_cat(A,np), vn_class(A,'marvel-31.3').
complement(A) :-
   prep(A,with), next(A), vn_class(A,'put_spatial-9.2').
complement(A) :-
   prep(A,to), next(A), vn_class(A,'correspond-36.1.1').
complement(A) :-
   obj_sense(A,pronoun), vn_class(A,'seem-109'), vn_spatial(A).
complement(A) :-
   next(A), vn_group(A,'29'), vn_group(A,'87').
complement(A) :-
   prep(A,off), obj_cat(A,np), vn_spatial(A).
complement(A) :-
   prep(A,after), next(A), vn_class(A,'meander-47.7').
complement(A) :-
   obj_sense(A,trace), vn_group(A,'51').
complement(A) :-
   prep(A,toward), vn_spatial(A).
complement(A) :-
   prep(A,there), vtag(A,vbd), vn_class(A,'seem-109').
complement(A) :-
   prep(A,to), obj_sense(A,'noun.person').
complement(A) :-
   prep(A,to), verb_sense(A,'verb.contact'), obj_cat(A,np).
complement(A) :-
   prep(A,from), vn_class(A,'defend-72.3').
complement(A) :-
   obj_cat(A,np), vn_class(A,'pit-10.7'), vn_group(A,'111').
complement(A) :-
   prep(A,as), obj_sense(A,'noun.person'), vn_class(A,'masquerade-29.6').
complement(A) :-
   prep(A,through), vtag(A,vbg), vn_group(A,'11').
complement(A) :-
   prep(A,down), obj_before(A), vn_spatial(A).
complement(A) :-
   vtag(A,vbg), vn_class(A,'forbid-64.4'), vn_class(A,'keep-15.2').
complement(A) :-
   prep(A,as), other_pp(A), vn_class(A,'wish-62').
complement(A) :-
   prep(A,in), obj_before(A), vn_class(A,'trifle-105.3').
complement(A) :-
   prep(A,to), obj_sense(A,'noun.group'), next(A).
complement(A) :-
   prep(A,up), verb_sense(A,'verb.motion').
complement(A) :-
   prep(A,across), verb_sense(A,'verb.motion'), obj_cat(A,np).
complement(A) :-
   prep(A,at), verb_sense(A,'verb.perception'), vn_class(A,'peer-30.3').
complement(A) :-
   vn_class(A,'wink-40.3.1'), vn_spatial(A).
complement(A) :-
   prep(A,out), verb_sense(A,'verb.motion'), vtag(A,vbd).
complement(A) :-
   obj_sense(A,'noun.body'), obj_before(A), vn_group(A,'11').
complement(A) :-
   prep(A,onto), vn_spatial(A).
complement(A) :-
   prep(A,from), vn_class(A,'free-10.6.3').
complement(A) :-
   prep(A,past), vn_spatial(A).
complement(A) :-
   prep(A,into), obj_sense(A,'noun.group').
complement(A) :-
   prep(A,out), vtag(A,vbd), obj_before(A).
complement(A) :-
   prep(A,for), verb_sense(A,'verb.cognition'), obj_sense(A,'noun.act').
complement(A) :-
   obj_cat(A,none), vn_class(A,'exist-47.1'), vn_class(A,'seem-109').
complement(A) :-
   obj_sense(A,none), obj_cat(A,np), vn_group(A,'55').
complement(A) :-
   prep(A,for), next(A), vn_class(A,'adjust-26.9').
