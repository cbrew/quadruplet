
grammar FeatureTerms;
/*
 * Parse a feature structure expression.
 */


LINE_COMMENT
  :  '#' ~( '\r' | '\n' )*
  -> skip;

WS
    : [ \t\r\n]+
    -> skip;

Equals
 : '='
 ;

FstructVariable
    : [?@][a-z0-9]+
    ;


Category
    : [A-Z][a-z0-9]*
    ;

Fname
    : [a-z0-9]+
    ;

Lparen
    : '('
    ;

Rparen
    : ')'
    ;

Lsq
    : '['
    ;

Rsq
    : ']'
    ;

Comma
    : ','
    ;


// anything in angle brackets is tokenized as a single
// token.
Sem: '<' ('->'|.)*? '>';
Arrow: '->';
Arrow2: '=>';
Colon: ':' ;
Pipe: '|' ;
Word: '"' ~'"'+ '"';

Number: [0-9]+;


/*
 This grammar is intentionally restrictive. Feature names may only be lower-case, syntactic categories
 must start with upper case.
*/

cfg: (lexentry | cfgrule | mcfgrule)*;
mcfgrule: expression Arrow2 mcfgrhs;
cfgrule: expression Arrow cfgrhs;
cfgrhs: rhspart (Pipe rhspart)*;
mcfgrhs: mcfgrhspart (Pipe mcfgrhspart)*;
rhspart: expression +;
mcfgrhspart: expression+ Colon linseq;
linseq:  Sem (Comma Sem)*;   // linearization sequences are handled handled using a separate mini-grammar. See
                             // Linearization.g4
lexentry: word Colon expression (Pipe expression)*;
word: Word ;


expression: categoryExpression (Lsq mapping Rsq) | categoryExpression;
categoryExpression: Category ;
mapping: fpair? (Comma fpair)* ;
fpair: Fname Equals fvalue;
fvalue: Fname |FstructVariable| flist | semantics;
flist: Lsq fvalue (Comma fvalue)* Rsq;
semantics: Sem; // semantics are handled using a separate mini-grammar. see LogicTerms.g4






