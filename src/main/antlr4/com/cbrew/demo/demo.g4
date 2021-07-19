grammar demo;


LINE_COMMENT
  :  '#' ~( '\r' | '\n' )*
  -> skip;

WS
    : [ \t\r\n]+
    -> skip;

Arrow: '->';

Pipe: '|';
Quoted: '\'' [A-Za-z]+([ ][A-Za-z]+)*'\'';

Equals
 : '='
 ;

Category
    : [A-Z][A-Za-z0-9]*
    ;

cfg: cfgrule +;
cfgrule: Category Arrow cfgrhs;
cfgrhs: rhspart (Pipe rhspart)*;
rhspart: (Category | Quoted)*;