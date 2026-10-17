% CGELBank ewt-trial.cgel reviews-074896-0008, written by tools/prolog/cgel2pl.py
% load tools/prolog/cgel.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(adj,huge),
  w(n,selection),
  w(coordinator,and),
  w(adj,great),
  w(n,suggestions,suggestion),
  w(p,from),
  w(d,the),
  w(n,staff),
  w(coordinator,and),
  w(n_pro,they),
  w(v,refer),
  w(n_pro,you),
  w(p,to),
  w(adj,reliable),
  w(n,places,place),
  w(p,if),
  w(n_pro,they),
  w(v_aux,'don''t',do),
  w(v,have),
  w(n_pro,what),
  w(n_pro,you),
  w(v,need)
]).

guidelines(cgelbank).
root(coordination1).
coordination(coordination1) ---> [coordinate:np(np1), coordinate:np(np2), coordinate:clause(clause1)].
np(np1) ---> [^head:nom(nom1)].
nom(nom1) ---> [mod:adjp(adjp1), ^head:t(n)].
adjp(adjp1) ---> [^head:t(adj)].
np(np2) ---> [marker:t(coordinator), ^head:np(np3)].
np(np3) ---> [^head:nom(nom2)].
nom(nom2) ---> [mod:adjp(adjp2), ^head:nom(nom3)].
adjp(adjp2) ---> [^head:t(adj)].
nom(nom3) ---> [^head:t(n), mod:pp(pp1)].
pp(pp1) ---> [^head:t(p), obj:np(np4)].
np(np4) ---> [det:dp(dp1), ^head:nom(nom4)].
dp(dp1) ---> [^head:t(d)].
nom(nom4) ---> [^head:t(n)].
clause(clause1) ---> [marker:t(coordinator), ^head:clause(clause2)].
clause(clause2) ---> [subj:np(np5), ^head:vp(vp1)].
np(np5) ---> [^head:nom(nom5)].
nom(nom5) ---> [^head:t(n_pro)].
vp(vp1) ---> [^head:vp(vp2), mod:pp(pp3)].
vp(vp2) ---> [^head:t(v), obj:np(np6), comp:pp(pp2)].
np(np6) ---> [^head:nom(nom6)].
nom(nom6) ---> [^head:t(n_pro)].
pp(pp2) ---> [^head:t(p), obj:np(np7)].
np(np7) ---> [^head:nom(nom7)].
nom(nom7) ---> [mod:adjp(adjp3), ^head:t(n)].
adjp(adjp3) ---> [^head:t(adj)].
pp(pp3) ---> [^head:t(p), comp:clause(clause3)].
clause(clause3) ---> [subj:np(np8), ^head:vp(vp3)].
np(np8) ---> [^head:nom(nom8)].
nom(nom8) ---> [^head:t(n_pro)].
vp(vp3) ---> [^head:t(v_aux), comp:clause(clause4)].
clause(clause4) ---> [^head:vp(vp4)].
vp(vp4) ---> [^head:t(v), obj:np(np9)].
np(np9) ---> [^head:nom(nom9)].
nom(nom9) ---> [mod:clause_rel(clause_rel1)].
clause_rel(clause_rel1) ---> [head_prenucleus:np(np10), ^head:clause_rel(clause_rel2)].
np(np10) ---> [^head:nom(nom10)].
nom(nom10) ---> [^head:t(n_pro)].
clause_rel(clause_rel2) ---> [subj:np(np11), ^head:vp(vp5)].
np(np11) ---> [^head:nom(nom11)].
nom(nom11) ---> [^head:t(n_pro)].
vp(vp5) ---> [^head:t(v), obj:e(gap, np10)].
sent_id('reviews-074896-0008').
text('Huge selection and, great suggestions from the staff and they refer you to reliable places if they don''t have what you need.').
sent('huge selection and great suggestions from the staff and they refer you to reliable places if they don''t have what you need --').
punct(3, after, [',']).
xpos(11, 'VBP').
xpos(18, 'VBP').
subtokens(18, [do,'n''t']).
xpos(19, 'VB').
fused(np10, nom9, head).
xpos(22, 'VBP').
punct(22, after, ['.']).
