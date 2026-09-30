% MASC court-transcript/Day3PMSession#35, written by tools/masc/ptb2pl.py
% load tools/masc/prolog/ptb.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(prp,'I'),
  w(vbp,'''m',be),
  w(rb,not),
  w(rb,here),
  w(to,to),
  w(vb,discuss),
  w(nn,law),
  w('.','.')
]).

guidelines(revised).
root(s1).
s(s1) ---> [sbj:np(np1), ^'--':vp(vp1), '--':t('.')].
np(np1) ---> [^'--':t(prp)].
vp(vp1) ---> [^'--':t(vbp), '--':t(rb), [loc,prd]:advp(advp1), prp:s(s2)].
advp(advp1) ---> [^'--':t(rb)].
s(s2) ---> [sbj:np(np2), ^'--':vp(vp2)].
np(np2) ---> [^'--':e('*PRO*', np1)].
vp(vp2) ---> [^'--':t(to), '--':vp(vp3)].
vp(vp3) ---> [^'--':t(vb), '--':np(np3)].
np(np3) ---> [^'--':t(nn)].
