lexer grammar FeatLexer;

// Set by a `%semantics drt` declaration, or by IntegratedParser.toDrs.
// In DRT mode '[', ']' and '+' inside <...> are DRS punctuation rather
// than parts of constants.
@members { public boolean drt = false; }

LINE_COMMENT
  :  '#' ~( '\r' | '\n' )*
  -> skip;

WS
    : [ \t\r\n]+
    -> skip;

// `%semantics logic` or `%semantics drt`; any other name is rejected by
// IntegratedVisitor.visitCfg.
Declaration
    : '%semantics' [ \t]+ ~[ \t\r\n]+
      { drt = getText().matches("%semantics[ \\t]+drt"); }
    ;

Equals
 : '='
 ;

FstructVariable
    : [?@][a-z0-9]+
    ;


Category
    : [A-Z][A-Za-z0-9]*
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

Plus: '+'
    ;
Minus: '-'
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

// Individual variables must begin with a lowercase letter and continue
// with zero or more digits.
Individual
    : [a-z][0-9]*
    ;

// Predicate names must begin with an uppercase letter and continue
// with zero or more digits.
Predicate
    : [A-Z][0-9]*
    ;

// Feature structure variables can start with ? or @
FstructVariable2
    : [?@][a-z0-9]+
    ;

// constants are any mix of ',letters numbers, %, ; / and _
// Disabled in DRT mode, where DrtConstant takes over.
Constant: {!drt}? ('['|']'|'\''|[a-zA-Z/0-9_%;+:@`*#]|'\u0080'..'\ufffe')+ ;

// In DRT mode, constants are Constant without '[', ']' and '+'.
DrtConstant: {drt}? ('\''|[a-zA-Z/0-9_%;:@`*#]|'\u0080'..'\ufffe')+ -> type(Constant);

// DRS punctuation. In logic mode Constant matches these too and, coming
// first, wins the tie, so they are only ever produced in DRT mode.
SemLsq: '[';
SemRsq: ']';
SemPlus: '+';
