% UD ewt-trial.conllu reviews-074896-0008, written by tools/prolog/ud2pl.py
% load tools/prolog/dep.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(adj,'Huge',huge),
  w(noun,selection),
  w(cconj,and),
  w(punct,','),
  w(adj,great),
  w(noun,suggestions,suggestion),
  w(adp,from),
  w(det,the),
  w(noun,staff),
  w(cconj,and),
  w(pron,they),
  w(verb,refer),
  w(pron,you),
  w(adp,to),
  w(adj,reliable),
  w(noun,places,place),
  w(sconj,if),
  w(pron,they),
  w(aux,do),
  w(part,'n''t',not),
  w(verb,have),
  w(pron,what),
  w(pron,you),
  w(verb,need),
  w(punct,'.')
]).

guidelines(ud).
root(w2).
noun(w2) ---> [amod:t(adj), ^'--':t(noun), conj:noun(w6), conj:verb(w12), punct:t(punct)].
noun(w6) ---> [cc:t(cconj), punct:t(punct), amod:t(adj), ^'--':t(noun), nmod:noun(w9)].
noun(w9) ---> [case:t(adp), det:t(det), ^'--':t(noun)].
verb(w12) ---> [cc:t(cconj), nsubj:t(pron), ^'--':t(verb), obj:t(pron), obl:noun(w16), advcl:verb(w21)].
noun(w16) ---> [case:t(adp), amod:t(adj), ^'--':t(noun)].
verb(w21) ---> [mark:t(sconj), nsubj:t(pron), aux:t(aux), advmod:t(part), ^'--':t(verb), obj:pron(w22)].
pron(w22) ---> [^'--':t(pron), 'acl:relcl':verb(w24)].
verb(w24) ---> [nsubj:t(pron), ^'--':t(verb)].
sent_id('reviews-074896-0008').
text('Huge selection and, great suggestions from the staff and they refer you to reliable places if they don''t have what you need.').
xpos(1, 'JJ').
feats(1, ['Degree=Pos']).
edep(1, 2, amod).
xpos(2, 'NN').
feats(2, ['Number=Sing']).
edep(2, 0, root).
xpos(3, 'CC').
edep(3, 6, cc).
xpos(4, ',').
edep(4, 6, punct).
xpos(5, 'JJ').
feats(5, ['Degree=Pos']).
edep(5, 6, amod).
xpos(6, 'NNS').
feats(6, ['Number=Plur']).
edep(6, 2, 'conj:and').
xpos(7, 'IN').
edep(7, 9, case).
xpos(8, 'DT').
feats(8, ['Definite=Def','PronType=Art']).
edep(8, 9, det).
xpos(9, 'NN').
feats(9, ['Number=Sing']).
edep(9, 6, 'nmod:from').
xpos(10, 'CC').
edep(10, 12, cc).
xpos(11, 'PRP').
feats(11, ['Case=Nom','Number=Plur','Person=3','PronType=Prs']).
edep(11, 12, nsubj).
xpos(12, 'VBP').
feats(12, ['Mood=Ind','Number=Plur','Person=3','Tense=Pres','VerbForm=Fin']).
edep(12, 2, 'conj:and').
xpos(13, 'PRP').
feats(13, ['Case=Nom','Person=2','PronType=Prs']).
edep(13, 12, obj).
xpos(14, 'IN').
edep(14, 16, case).
xpos(15, 'JJ').
feats(15, ['Degree=Pos']).
edep(15, 16, amod).
xpos(16, 'NNS').
feats(16, ['Number=Plur']).
edep(16, 12, 'obl:to').
xpos(17, 'IN').
edep(17, 21, mark).
xpos(18, 'PRP').
feats(18, ['Case=Nom','Number=Plur','Person=3','PronType=Prs']).
edep(18, 21, nsubj).
mwt(19, 20, 'don''t').
xpos(19, 'VBP').
feats(19, ['Mood=Ind','Number=Plur','Person=3','Tense=Pres','VerbForm=Fin']).
edep(19, 21, aux).
xpos(20, 'RB').
edep(20, 21, advmod).
xpos(21, 'VB').
feats(21, ['VerbForm=Inf']).
edep(21, 12, 'advcl:if').
xpos(22, 'WP').
feats(22, ['PronType=Rel']).
edep(22, 21, obj).
edep(22, 24, obj).
xpos(23, 'PRP').
feats(23, ['Case=Nom','Person=2','PronType=Prs']).
edep(23, 24, nsubj).
xpos(24, 'VBP').
feats(24, ['Mood=Ind','Number=Sing','Person=2','Tense=Pres','VerbForm=Fin']).
edep(24, 22, 'acl:relcl').
xpos(25, '.').
edep(25, 2, punct).
