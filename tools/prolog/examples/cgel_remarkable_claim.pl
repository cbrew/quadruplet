% CGELBank twitter.cgel Tree WhatARemarkableClaim-0, written by tools/prolog/cgel2pl.py
% load tools/prolog/cgel.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(adj,what),
  w(d,a),
  w(adj,remarkable),
  w(n,claim),
  w(sdr,to),
  w(v,make)
]).

guidelines(cgelbank).
root(np1).
np(np1) ---> [mod:adjp(adjp1), ^head:np(np2)].
adjp(adjp1) ---> [^head:t(adj)].
np(np2) ---> [det:dp(dp1), ^head:nom(nom1)].
dp(dp1) ---> [^head:t(d)].
nom(nom1) ---> [^head:nom(nom2), comp_ind:clause(clause1)].
nom(nom2) ---> [mod:adjp(adjp2), ^head:t(n)].
adjp(adjp2) ---> [^head:t(adj)].
clause(clause1) ---> [^head:vp(vp1)].
vp(vp1) ---> [marker:t(sdr), ^head:vp(vp2)].
vp(vp2) ---> [^head:t(v), obj:e(gap, nom2)].
sent_id('Tree WhatARemarkableClaim-0').
text('What a remarkable claim to make!').
sent('what a remarkable claim to make --').
xpos(6, 'VB').
punct(6, after, ['!']).
