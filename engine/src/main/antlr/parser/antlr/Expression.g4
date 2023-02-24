grammar Expression;

@header {
    package parser.antlr;
}

wholeInput: equationSystem | equationUnion | equation | inequality | solution | exprOrUndefined EOF;

equationSystem: equations += equation (',' equations += equation)+;

equationUnion: equations += equation ('OR' equations += equation)+;

equation: lhs=expr '=' rhs=expr;

inequality: lhs=expr comparator=('<' | '<=' | '>' | '>=') rhs=expr;

solution: 'Solution' '[' var=variable ',' solutionSet=set ']';

set: emptySet | finiteSet | reals | interval;

emptySet: '{' '}';

finiteSet: '{' first=expr (rest+=restElement)* '}';

restElement: ',' expr;

reals: REALS;

interval: leftBracket=('(' | '[') left=exprOrInfinity ',' right=exprOrInfinity rightBracket=(')' | ']');

exprOrInfinity: expr | infinity | minusInfinity;

exprOrUndefined: expr | undefined;

expr: sum;

sum: first=firstTerm (rest+=otherTerm)*;

firstTerm
    : sign=('+' | '-' | PLUSMINUS)? explicitProduct       #realFirstTerm
    | OPEN_PARTIALSUM sum CLOSE_PARTIALSUM            #firstPartialSum
    ;

otherTerm
    : sign=('+' | '-' | PLUSMINUS) explicitProduct                       #realOtherTerm
    | OPEN_PARTIALSUM (terms+=otherTerm)+ CLOSE_PARTIALSUM           #otherPartialSum
    ;

explicitProduct: first=implicitProduct (rest+=otherExplicitFactor)*;

otherExplicitFactor: op=('*'|':') implicitProduct;

implicitProduct: first=firstFactor (others+=otherFactor)*;

firstFactor
    : sign=('+' | '-' | PLUSMINUS) factor=firstFactor           #firstFactorWithSign
    | (mixedNumber | fraction | power | atom)       #firstFactorWithoutSign
    ;

otherFactor: power | nonNumericAtom;

fraction: '[' num=expr '/' den=expr ']';

power: '[' base=atom '^' exp=expr ']';

sqrt: 'sqrt[' radicand=expr ']';

root: 'root[' radicand=expr ',' order=expr ']';

bracket
    :'(' expr ')' #roundBracket
    | OPEN_SQUARE expr CLOSE_SQUARE #squareBracket
    | OPEN_CURLY expr CLOSE_CURLY #curlyBracket
    ;

atom: nonNumericAtom | naturalNumber | decimalNumber | recurringDecimalNumber;

nonNumericAtom: sqrt | root | bracket | variable;

mixedNumber: '[' integer=NATNUM num=NATNUM '/' den=NATNUM ']';

naturalNumber: NATNUM;

infinity: INFINITY;
minusInfinity: '-' INFINITY;

undefined: UNDEFINED;

decimalNumber: DECNUM;

recurringDecimalNumber: RECURRING_DECNUM;

variable: VARIABLE;

fragment DIGIT: [0-9];
NATNUM: DIGIT+;
DECNUM: NATNUM '.' NATNUM;
RECURRING_DECNUM: NATNUM '.' NATNUM? '[' NATNUM ']';

OPEN_SQUARE: '[.';
CLOSE_SQUARE: '.]';

OPEN_CURLY: '{.';
CLOSE_CURLY: '.}';

OPEN_PARTIALSUM: '<.';
CLOSE_PARTIALSUM: '.>';

PLUSMINUS: '+/-';

REALS: 'REALS';
INFINITY: 'INFINITY';
UNDEFINED: 'UNDEFINED';

VARIABLE: [a-z];
WHITESPACE: [ \t] -> skip;

UNKNOWN: .;