% MASC court-transcript/Day3PMSession#122, written by tools/masc/ptb2pl.py
% load tools/masc/prolog/ptb.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(prp,'I'),
  w(vbp,do),
  w(rb,'n''t'),
  w(vb,think),
  w(prp,it),
  w(vbz,'''s',be),
  w(jj,necessary),
  w(to,to),
  w(vb,do),
  w(dt,that),
  w('.','.')
]).

guidelines(revised).
root(s1).
s(s1) ---> [sbj:np(np1), ^'--':vp(vp1), '--':t('.')].
np(np1) ---> [^'--':t(prp)].
vp(vp1) ---> [^'--':t(vbp), '--':t(rb), '--':vp(vp2)].
vp(vp2) ---> [^'--':t(vb), '--':sbar(sbar1)].
sbar(sbar1) ---> ['--':e('0'), ^'--':s(s2)].
s(s2) ---> [sbj:np(np2), ^'--':vp(vp3)].
np(np2) ---> [^'--':np(np3), '--':s(s3)].
np(np3) ---> [^'--':t(prp)].
s(s3) ---> [^'--':e('*EXP*', s4)].
vp(vp3) ---> [^'--':t(vbz), prd:adjp(adjp1), '--':s(s4)].
adjp(adjp1) ---> [^'--':t(jj)].
s(s4) ---> [sbj:np(np4), ^'--':vp(vp4)].
np(np4) ---> [^'--':e('*PRO*')].
vp(vp4) ---> [^'--':t(to), '--':vp(vp5)].
vp(vp5) ---> [^'--':t(vb), '--':np(np5)].
np(np5) ---> [^'--':t(dt)].
