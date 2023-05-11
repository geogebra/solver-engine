grammar Expression;

@header {
    package parser.antlr;
}

wholeInput: (statement | solution | expr | undefined | name) EOF;

statement: equationSystem | equationUnion | equationAddition | equationSubtraction | equation | inequality;

equationSystem: equations += equation (',' equations += equation)+;

equationUnion: equations += equation ('OR' equations += equation)+;

equationAddition: eq1=equation '/+/' eq2=equation;
equationSubtraction: eq1=equation '/-/' eq2=equation;

equation: lhs=expr '=' rhs=expr;

inequality: lhs=expr comparator=('<' | '<=' | '>' | '>=') rhs=expr;

solution
    : 'Solution' '[' var=variable ',' solutionSet=set ']'       #legacySolution
    | 'Identity' '[' vars=variables ':' statement ']'           #identity
    | 'Contradiction' '[' vars=variables ':' statement ']'      #contradiction
    | 'ImplicitSolution' '[' vars=variables ':' statement ']'   #implicitSolution
    | 'SetSolution' '[' vars=variables ':' solutionSet=set ']'  #setSolution
    ;

set: cartesianProduct;

cartesianProduct: first=simpleSet ('*' rest+=simpleSet)*;

simpleSet: emptySet | finiteSet | reals | interval;

emptySet: '{' '}';

finiteSet: '{' first=element (',' rest+=element)* '}';

reals: REALS;

interval: leftBracket=('(' | '[') left=exprOrInfinity ',' right=exprOrInfinity rightBracket=(')' | ']');

exprOrInfinity: expr | infinity | minusInfinity;

element: tuple | expr;

tuple: '(' first=element (',' rest+=element)+ ')';

expr: sum;

sum: first=firstTerm (rest+=otherTerm)*;

firstTerm
    : sign=('+' | '-' | PLUSMINUS)? product          #realFirstTerm
    | OPEN_PARTIAL sum CLOSE_PARTIAL                 #firstPartialSum
    ;

otherTerm
    : sign=('+' | '-' | PLUSMINUS) product           #realOtherTerm
    | '+' OPEN_PARTIAL sum CLOSE_PARTIAL             #otherPartialSum
    ;

product: first=firstFactor (rest+=factorWithOperator)*;

firstFactor
    : signedFactor                              #realFirstFactor
    | OPEN_PARTIAL product CLOSE_PARTIAL        #partialFirstFactor
    ;

factorWithOperator
    : factor                                                    #factorWithNoOperator
    | '*' signedFactor                                          #factorWithProductOperator
    | ':' implicitProduct                                       #factorWithDivisionOperator
    | OPEN_PARTIAL product CLOSE_PARTIAL                        #partialProductWithNoOperator
    | '*' OPEN_PARTIAL product CLOSE_PARTIAL                    #partialProductWithProductOperator
    ;

implicitProduct: first=signedFactor (others+=factor)*;

signedFactor: (signs+=('+' | '-' | PLUSMINUS))* factor;

factor: mixedNumber | fraction | power | atom;

fraction: '[' num=expr '/' den=expr ']';

power: '[' base=atom '^' exp=expr ']';

sqrt: 'sqrt[' radicand=expr ']';

root: 'root[' radicand=expr ',' order=expr ']';

absoluteValue: 'abs[' argument=expr ']';

bracket
    :'(' expr ')'                   #roundBracket
    | OPEN_SQUARE expr CLOSE_SQUARE #squareBracket
    | OPEN_CURLY expr CLOSE_CURLY   #curlyBracket
    ;

atom: nonNumericAtom | naturalNumber | decimalNumber | recurringDecimalNumber;

nonNumericAtom: sqrt | root | absoluteValue | bracket | variable;

mixedNumber: '[' integer=NATNUM num=NATNUM '/' den=NATNUM ']';

naturalNumber: NATNUM;

infinity: INFINITY;
minusInfinity: '-' INFINITY;

undefined: UNDEFINED;

decimalNumber: DECNUM;

recurringDecimalNumber: RECURRING_DECNUM;

variables: first=variable (',' rest+=variable)*;

variable: VARIABLE;

name: NAME;

fragment DIGIT: [0-9];
NATNUM: DIGIT+;
DECNUM: NATNUM '.' NATNUM;
RECURRING_DECNUM: NATNUM '.' NATNUM? '[' NATNUM ']';

OPEN_SQUARE: '[.';
CLOSE_SQUARE: '.]';

OPEN_CURLY: '{.';
CLOSE_CURLY: '.}';

OPEN_PARTIAL: '<.';
CLOSE_PARTIAL: '.>';

PLUSMINUS: '+/-';

REALS: 'REALS';
INFINITY: 'INFINITY';
UNDEFINED: 'UNDEFINED';

VARIABLE: [a-z];
WHITESPACE: [ \t] -> skip;

NAME: '"' (~ '"')+ '"';

UNKNOWN: .;