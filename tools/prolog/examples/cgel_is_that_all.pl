% CGELBank twitter.cgel Tree IsThatAllYouGot-0, written by tools/prolog/cgel2pl.py
% load tools/prolog/cgel.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(v_aux,is,be),
  w(d,that),
  w(d,all),
  w(n_pro,you),
  w(v,got,get),
  w(n,winter)
]).

guidelines(cgelbank).
root(clause1).
clause(clause1) ---> [prenucleus:t(v_aux), ^head:clause(clause2), vocative:np(np4)].
clause(clause2) ---> [subj:np(np1), ^head:vp(vp1)].
np(np1) ---> [^head:nom(nom1)].
nom(nom1) ---> [^det_head:dp(dp1)].
dp(dp1) ---> [^head:t(d)].
vp(vp1) ---> [^head:e(gap, 1), predcomp:np(np2)].
np(np2) ---> [^head:nom(nom2)].
nom(nom2) ---> [^det_head:dp(dp2), mod:clause_rel(clause_rel1)].
dp(dp2) ---> [^head:t(d)].
clause_rel(clause_rel1) ---> [subj:np(np3), ^head:vp(vp2)].
np(np3) ---> [^head:nom(nom3)].
nom(nom3) ---> [^head:t(n_pro)].
vp(vp2) ---> [^head:t(v), obj:e(gap, dp2)].
np(np4) ---> [^head:nom(nom4)].
nom(nom4) ---> [^head:t(n)].
sent_id('Tree IsThatAllYouGot-0').
text('Is that all you got winter?').
sent('is that -- all you got -- winter').
xpos(1, 'VBZ').
fused(dp1, np1, det).
fused(dp2, np2, det).
xpos(5, 'VBD').
punct(6, after, ['?']).
