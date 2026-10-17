% spaCy en_core_web_trf which_book#1, written by tools/prolog/clear2pl.py
% load tools/prolog/dep.pl first: it defines the operators this reads with.
:- encoding(utf8).

sentence([
  w(wdt,'Which',which),
  w(nn,book),
  w(vbd,did,do),
  w(nnp,'Kim'),
  w(vb,say),
  w(prp,she),
  w(vbd,bought,buy),
  w(nn,yesterday),
  w('.','?')
]).

guidelines(clear).
root(w5).
vb(w5) ---> [ccomp:vbd(w7)@1, aux:t(vbd), nsubj:t(nnp), ^'--':t(vb), ccomp:vbd(w7)@2, punct:t('.')].
vbd(w7) ---> [[dobj:nn(w2)], [nsubj:t(prp), ^'--':t(vbd), npadvmod:t(nn)]].
nn(w2) ---> [det:t(wdt), ^'--':t(nn)].
sent_id('which_book#1').
text('Which book did Kim say she bought yesterday?').
upos(1, det).
upos(2, noun).
feats(2, ['Number=Sing']).
upos(3, aux).
feats(3, ['Tense=Past','VerbForm=Fin']).
upos(4, propn).
feats(4, ['Number=Sing']).
upos(5, verb).
feats(5, ['VerbForm=Inf']).
upos(6, pron).
feats(6, ['Case=Nom','Gender=Fem','Number=Sing','Person=3','PronType=Prs']).
upos(7, verb).
feats(7, ['Tense=Past','VerbForm=Fin']).
upos(8, noun).
feats(8, ['Number=Sing']).
upos(9, punct).
feats(9, ['PunctType=Peri']).
entity(4, 4, 'PERSON').
entity(8, 8, 'DATE').

