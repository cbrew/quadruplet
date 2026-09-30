% MASC face-to-face/Bed012#507, written by tools/masc/ptb2pl.py
% load tools/masc/prolog/ptb.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(dt,'That'),
  w(vbz,'''s',be),
  w(jj,true),
  w(',',','),
  w(prp,'I'),
  w(vbp,guess),
  w('.','.')
]).

guidelines(revised).
root(s1).
s(s1) ---> ['--':s(s2), '--':t(','), sbj:np(np2), ^'--':vp(vp2), '--':t('.')].
s(s2) ---> [sbj:np(np1), ^'--':vp(vp1)].
np(np1) ---> [^'--':t(dt)].
vp(vp1) ---> [^'--':t(vbz), prd:adjp(adjp1)].
adjp(adjp1) ---> [^'--':t(jj)].
np(np2) ---> [^'--':t(prp)].
vp(vp2) ---> [^'--':t(vbp), '--':sbar(sbar1)].
sbar(sbar1) ---> ['--':e('0'), ^'--':s(s3)].
s(s3) ---> [^'--':e('*T*', s2)].
tags(s2, [tpc]).
