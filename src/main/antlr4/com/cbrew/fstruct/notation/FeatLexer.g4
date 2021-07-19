lexer grammar FeatLexer;
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
    : [a-z0-9_]+
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



Open: '<' -> pushMode(ISLAND);
Arrow: '->';
Arrow2: '=>';
Colon: ':' ;
Pipe: '|' ;
Word: '"' ~'"'+ '"';

mode ISLAND;
Close: '>' -> popMode;
Number: [0-9]+;
WS2
    : [ \t\r\n]+
    -> skip;


Or
 : '|'
 | '\u2228'
 ;

And
    : '&'
    | '\u2227'
    ;

Implies
    : '->'
    | '\u2192'
    ;

Iff
    : '<->'
    | '\u2194'
    ;

Not
    : '-'
    | '~'
    ;

NotEquals
    : '!='
    | '<>'
    | '\u2260'
    ;

SemEquals
    : '=='
    | '='
    ;

SemComma
    : ','
    ;
SemLparen
    : '('
    ;

SemRparen
    : ')'
    ;

Lambda: '\\' | '\u03BB';
Forall:
    'forall'
    | 'all'
    | '\u2200'
    ;
Exists: 'exists'
        | '\u2203';
Dot: '.';


Box: '\u2610';



// Feature structure variables can start with ? or @
FstructVariable2
    : [?@][a-z0-9]+
    ;
Constant: ('['|']'|'\''|[a-zA-Z/0-9_%;+:@`*#]|'\u0080'..'\ufffe')+ ;
//  Constant: ([a-z]|'_')+;

