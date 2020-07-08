grammar Linearization;
/*
 For now it is easiest to do this as a separate nearly
 trivial sub-grammar that recognizes lists of lists of integers.
*/

LINE_COMMENT
  :  '#' ~( '\r' | '\n' )*
  -> skip;

WS
    : [ \t\r\n]+
    -> skip;
Number: [0-9]+;
Lparen: '(';
Rparen: ')';
Comma: ',';

numbers: Number (Comma Number)*;
numseq: Lparen numbers Rparen;
numseqs: numseq+;