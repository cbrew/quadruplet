parser grammar FeatParser;

options { tokenVocab=FeatLexer; }



cfg: (lexentry | cfgrule | mcfgrule)*;
mcfgrule: featureMap Arrow2 mcfgrhs;
cfgrule: featureMap Arrow cfgrhs;
cfgrhs: rhspart (Pipe rhspart)*;
mcfgrhs: mcfgrhspart (Pipe mcfgrhspart)*;
rhspart: (featureMap|word) +;
mcfgrhspart: featureMap+ Colon linseq (Comma linseq)*;
linseq:  Open  numseq+  Close;
numseq: (SemLparen Number+ SemRparen);
lexentry: word Colon featureMap (Pipe featureMap)*;
word: Word ;
featureMap: Category (Lsq mapping Rsq);
mapping: fpair? (Comma fpair)* ;
fpair: Fname Equals fvalue;
fvalue: Fname |FstructVariable| flist| semantics;
flist: Lsq fvalue (Comma fvalue)* Rsq;
semantics: Open expression Close;
expression:
        FstructVariable2             # Variable
        | expression applicationTail  # Application
        | expression And expression   # And
        | expression Or expression    # Or
        | parenthesizedExpression     # Parenthesized
        | negation                    # Negated
        | expression relationTail     # Relational
        | existsExpression            # Exists
        | forallExpression            # Forall
        | lambdaExpression            # Lambda
        | Box                         # Box
        | Constant                    # Constant
        ;






 boolOp:
 	Iff
 	| SemEquals
 	| Implies
 	| NotEquals
 	;

relationTail:
        boolOp expression
	;

parenthesizedExpression: SemLparen expression SemRparen;
existsExpression: Exists (Constant +) Dot expression;
forallExpression: Forall (Constant +) Dot expression;
lambdaExpression: Lambda (larg +) Dot expression;




applicationTail: SemLparen expression (SemComma expression)* SemRparen;

negation: Not expression;


larg : Constant ;