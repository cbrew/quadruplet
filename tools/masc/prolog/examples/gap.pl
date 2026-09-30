% MASC face-to-face/Bed012#20, written by tools/masc/ptb2pl.py
% load tools/masc/prolog/ptb.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(cc,'Or'),
  w(vbz,is,be),
  w(prp,it),
  w(in,on),
  w(nn,fire),
  w(cc,or),
  w(nn,something),
  w(vbg,happening,happen),
  w(in,to),
  w(prp,it),
  w('.','?')
]).

guidelines(revised).
root(sq1).
sq(sq1) ---> ['--':t(cc), ^'--':sq(sq2), '--':t(cc), '--':sq(sq3), '--':t('.')].
sq(sq2) ---> [^'--':t(vbz), sbj:np(np1), prd:pp(pp1)].
np(np1) ---> [^'--':t(prp)].
pp(pp1) ---> [^'--':t(in), '--':np(np2)].
np(np2) ---> [^'--':t(nn)].
sq(sq3) ---> [sbj:np(np3), ^'--':vp(vp1)].
np(np3) ---> [^'--':t(nn)].
vp(vp1) ---> [^'--':t(vbg), clr:pp(pp2)].
pp(pp2) ---> [^'--':t(in), '--':np(np4)].
np(np4) ---> [^'--':t(prp)].
gap(np1, 1).
gap(np3, 1).
