% UD en_ewt-ud-dev.conllu reviews-249889-0002, written by tools/prolog/ud2pl.py
% load tools/prolog/dep.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(adj,great),
  w(noun,knowledge),
  w(cconj,and),
  w(noun,prices,price),
  w(verb,compared,compare),
  w(adp,to),
  w(pron,anyone),
  w(adp,in),
  w(det,the),
  w(noun,industry),
  w(punct,'.')
]).

guidelines(ud).
root(w2).
noun(w2) ---> [amod:adj(w1)@1, ^'--':t(noun), conj:noun(w4), amod:adj(w1)@2, punct:t(punct)].
adj(w1) ---> [[^'--':t(adj)], [advcl:verb(w5)]].
verb(w5) ---> [^'--':t(verb), obl:pron(w7)].
pron(w7) ---> [case:t(adp), ^'--':t(pron), nmod:noun(w10)].
noun(w10) ---> [case:t(adp), det:t(det), ^'--':t(noun)].
noun(w4) ---> [cc:t(cconj), ^'--':t(noun)].
sent_id('reviews-249889-0002').
text('great knowledge and prices compared to anyone in the industry.').
xpos(1, 'JJ').
feats(1, ['Degree=Pos']).
edep(1, 2, amod).
xpos(2, 'NN').
feats(2, ['Number=Sing']).
edep(2, 0, root).
misc(2, ['Supersense=n.COGNITION']).
xpos(3, 'CC').
edep(3, 4, cc).
xpos(4, 'NNS').
feats(4, ['Number=Plur']).
edep(4, 2, 'conj:and').
misc(4, ['Supersense=n.POSSESSION']).
xpos(5, 'VBN').
feats(5, ['Tense=Past','VerbForm=Part','Voice=Pass']).
edep(5, 1, advcl).
misc(5, ['MWELemma[weak]=compare to','MWELen[weak]=2','MWEString[weak]=compared to','Supersense=v.stative']).
xpos(6, 'IN').
edep(6, 7, case).
misc(6, ['PRel[config]=default','PRel[gov]=5:compare','PRel[obj]=7:anyone','Supersense[coding]=p.Goal','Supersense[scene]=p.ComparisonRef']).
xpos(7, 'NN').
feats(7, ['Number=Sing','PronType=Ind']).
edep(7, 5, 'obl:to').
xpos(8, 'IN').
edep(8, 10, case).
misc(8, ['PRel[config]=default','PRel[gov]=7:anyone','PRel[obj]=10:industry','Supersense[coding]=p.Locus','Supersense[scene]=p.Org']).
xpos(9, 'DT').
feats(9, ['Definite=Def','PronType=Art']).
edep(9, 10, det).
xpos(10, 'NN').
feats(10, ['Number=Sing']).
edep(10, 7, 'nmod:in').
misc(10, ['Supersense=n.GROUP']).
xpos(11, '.').
edep(11, 2, punct).
