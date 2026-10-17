lexer grammar FeatLexer;

// Set by a `%semantics drt` declaration. In DRT mode `<` enters
// DRT_ISLAND rather than ISLAND, so that '[', ']' and '+' inside <...>
// are DRS punctuation rather than parts of constants.
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


Open: '<' { pushMode(drt ? DRT_ISLAND : ISLAND); };
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
Constant: ('['|']'|'\''|[a-zA-Z/0-9_%;+:@`*#]|'\u0080'..'\ufffe')+ ;

// The semantics of a `%semantics drt` grammar, and IntegratedParser.toDrs.
// The rules of ISLAND, giving the same token types, except that constants
// may not contain '[', ']' or '+', which are DRS punctuation instead.
mode DRT_ISLAND;
DrtClose: '>' -> popMode, type(Close);
DrtNumber: [0-9]+ -> type(Number);
DrtWS
    : [ \t\r\n]+
    -> skip;
DrtOr: ('|' | '\u2228') -> type(Or);
DrtAnd: ('&' | '\u2227') -> type(And);
DrtImplies: ('->' | '\u2192') -> type(Implies);
DrtIff: ('<->' | '\u2194') -> type(Iff);
DrtNot: ('-' | '~') -> type(Not);
DrtNotEquals: ('!=' | '<>' | '\u2260') -> type(NotEquals);
DrtSemEquals: ('==' | '=') -> type(SemEquals);
DrtSemComma: ',' -> type(SemComma);
DrtSemLparen: '(' -> type(SemLparen);
DrtSemRparen: ')' -> type(SemRparen);
DrtLambda: ('\\' | '\u03BB') -> type(Lambda);
DrtForall: ('forall' | 'all' | '\u2200') -> type(Forall);
DrtExists: ('exists' | '\u2203') -> type(Exists);
DrtDot: '.' -> type(Dot);
DrtBox: '\u2610' -> type(Box);
DrtIndividual: [a-z][0-9]* -> type(Individual);
DrtPredicate: [A-Z][0-9]* -> type(Predicate);
DrtFstructVariable2: [?@][a-z0-9]+ -> type(FstructVariable2);
DrtConstant: ('\''|[a-zA-Z/0-9_%;:@`*#]|'\u0080'..'\ufffe')+ -> type(Constant);
SemLsq: '[';
SemRsq: ']';
SemPlus: '+';
