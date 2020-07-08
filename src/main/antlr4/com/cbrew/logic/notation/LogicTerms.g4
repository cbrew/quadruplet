
grammar LogicTerms;
/*
 * Parse a logic expression to the data structure like  that
 * which nltk 3.0 would give.
 */


LINE_COMMENT
  :  '#' ~( '\r' | '\n' )*
  -> skip;

WS
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

Equals
    : '=='
    | '='
    ;

Comma
    : ','
    ;
Lparen
    : '('
    ;

Rparen
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
FstructVariable
    : [?@][a-z0-9]+
    ;


// constants are any mix of ',letters numbers, %, ; / and _
Constant: ('['|']'|'\''|[a-zA-Z/0-9_%;+:@`*#]|'\u0080'..'\ufffe')+ ;





semantics:
    (LINE_COMMENT | expression)+
    EOF
      ;

// The # annotations affect the names of the visitor methods
// and listener methods that Antlr generates.



expression:
        constantExpression           # ConstantExpr
        | FstructVariable             # Variable
        | Individual                  # Individual
        | Predicate                   # Predicate
        | expression applicationTail  # Application
        | expression And expression   # And
        | expression Or expression    # Or
        | parenthesizedExpression     # Parenthesized
        | negation                    # Negated
        | expression relationTail     # Relational
        | existsExpression            # Exists
        | forallExpression            # Forall
        | lambdaExpression            # Lambda
        | injectableExpression        # Injectable
        | Box                         # Box
        ;




constantExpression:
    Constant
    ;

 boolOp:
 	Iff
 	| Equals
 	| Implies
 	| NotEquals
 	;

relationTail:
        boolOp expression
	;

parenthesizedExpression: Lparen expression Rparen;
existsExpression: Exists (argument +) Dot expression;
forallExpression: Forall (argument +) Dot expression;
lambdaExpression: Lambda (larg +) Dot expression;

injectableExpression:
    '@Injectable' Lparen FstructVariable (Comma expression)* Rparen;


applicationTail: Lparen expression (Comma expression)* Rparen;

negation: Not expression;

argument : Individual | Predicate | FstructVariable;

larg : Individual | Predicate;



