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
mapping: (fpair|fabbrev)? (Comma (fpair|fabbrev))* ;
fpair: Fname Equals fvalue;
fvalue: Fname |FstructVariable| flist| ftuple | featureMap | semantics;
flist: Lsq (fvalues|fexpr) Rsq;
ftuple:  Lparen (fvalues|fexpr) Rparen;
fvalues: fvalue? (Comma fvalue)*;
// tuple and list expressions, for now just X + Y
// where X and Y are either collections or ? variables
fexpr: fvalue Plus fvalue (Plus fvalue)*;
fabbrev: (Plus|Minus) Fname;

semantics: Open expression Close;
expression:
        constantExpression          # Constant
        | FstructVariable2            # Variable
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
        | individualExpression        # Individual
        | predicateExpression         # Predicate
        ;


predicateExpression:
    Predicate
    ;

individualExpression:
    Individual
    ;

constantExpression:
    Constant
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
existsExpression: Exists (argument +) Dot expression;
forallExpression: Forall (argument +) Dot expression;
lambdaExpression: Lambda (larg +) Dot expression;





applicationTail: SemLparen expression (SemComma expression)* SemRparen;

negation: Not expression;

argument : Individual | Predicate | FstructVariable;
larg: Individual | Predicate;