% MASC telephone/sw2078-UTF16-ms98-a-trans#6, written by tools/masc/ptb2pl.py
% load tools/masc/prolog/ptb.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(dt,that),
  w(vbz,'''s',be),
  w(dt,the),
  w(jj,only),
  w(nn,thing),
  w(prp,'I'),
  w(vbd,found,find),
  w(rp,out),
  w(nn,tonight),
  w(su,'/')
]).

guidelines(revised).
root(s1).
s(s1) ---> [sbj:np(np1), ^'--':vp(vp1), '--':t(su)].
np(np1) ---> [^'--':t(dt)].
vp(vp1) ---> [^'--':t(vbz), prd:np(np2)].
np(np2) ---> [^'--':np(np3), '--':sbar(sbar1)].
np(np3) ---> ['--':t(dt), '--':t(jj), ^'--':t(nn)].
sbar(sbar1) ---> ['--':whnp(whnp1), ^'--':s(s2)].
whnp(whnp1) ---> [^'--':e('0')].
s(s2) ---> [sbj:np(np4), ^'--':vp(vp2)].
np(np4) ---> [^'--':t(prp)].
vp(vp2) ---> [^'--':t(vbd), '--':np(np5), '--':prt(prt1), tmp:np(np6)].
np(np5) ---> [^'--':e('*T*', whnp1)].
prt(prt1) ---> [^'--':t(rp)].
np(np6) ---> [^'--':t(nn)].
