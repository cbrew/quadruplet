% MASC court-transcript/Day3PMSession#385, written by tools/masc/ptb2pl.py
% load tools/masc/prolog/ptb.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(wdt,'What'),
  w(nn,grade),
  w(vbz,is,be),
  w(prp,she),
  w(in,in),
  w('.','?')
]).

guidelines(revised).
root(sbarq1).
sbarq(sbarq1) ---> ['--':whnp(whnp1), ^'--':sq(sq1), '--':t('.')].
whnp(whnp1) ---> [^'--':t(wdt), '--':t(nn)].
sq(sq1) ---> [^'--':t(vbz), sbj:np(np1), prd:pp(pp1)].
np(np1) ---> [^'--':t(prp)].
pp(pp1) ---> [^'--':t(in), '--':np(np2)].
np(np2) ---> [^'--':e('*T*', whnp1)].
