% MASC court-transcript/Day3PMSession#210, written by tools/masc/ptb2pl.py
% load tools/masc/prolog/ptb.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(cc,'And'),
  w(prp,'I'),
  w(vbp,understand),
  w(cc,and),
  w(prp,'I'),
  w(vbp,respect),
  w(dt,that),
  w(nn,argument),
  w('.','.')
]).

guidelines(revised).
root(s1).
s(s1) ---> ['--':t(cc), ^'--':s(s2), '--':t(cc), '--':s(s3), '--':np(np5), '--':t('.')].
s(s2) ---> [sbj:np(np1), ^'--':vp(vp1)].
np(np1) ---> [^'--':t(prp)].
vp(vp1) ---> [^'--':t(vbp), '--':np(np2)].
np(np2) ---> [^'--':e('*RNR*', np5)].
s(s3) ---> [sbj:np(np3), ^'--':vp(vp2)].
np(np3) ---> [^'--':t(prp)].
vp(vp2) ---> [^'--':t(vbp), '--':np(np4)].
np(np4) ---> [^'--':e('*RNR*', np5)].
np(np5) ---> ['--':t(dt), ^'--':t(nn)].
