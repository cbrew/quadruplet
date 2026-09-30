% MASC court-transcript/Day3PMSession#538, written by tools/masc/ptb2pl.py
% load tools/masc/prolog/ptb.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(ex,'There'),
  w(vbz,'''s',be),
  w(dt,a),
  w(jj,great),
  w(nn,temptation),
  w(in,in),
  w(nn,'cross-examination'),
  w(to,to),
  w(vb,talk),
  w(in,over),
  w('.','.')
]).

guidelines(revised).
root(s1).
s(s1) ---> [sbj:np(np1), ^'--':vp(vp1), '--':t('.')].
np(np1) ---> [^'--':t(ex)].
vp(vp1) ---> [^'--':t(vbz), prd:np(np2), loc:pp(pp1), '--':s(s3)].
np(np2) ---> ['--':t(dt), '--':t(jj), ^'--':t(nn), '--':s(s2)].
s(s2) ---> [^'--':e('*ICH*', s3)].
pp(pp1) ---> [^'--':t(in), '--':np(np3)].
np(np3) ---> [^'--':t(nn)].
s(s3) ---> [sbj:np(np4), ^'--':vp(vp2)].
np(np4) ---> [^'--':e('*PRO*')].
vp(vp2) ---> [^'--':t(to), '--':vp(vp3)].
vp(vp3) ---> [^'--':t(vb), mnr:pp(pp2)].
pp(pp2) ---> [^'--':t(in), '--':np(np5)].
np(np5) ---> [^'--':e('*?*')].
