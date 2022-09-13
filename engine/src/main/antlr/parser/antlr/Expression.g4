grammar Expression;

@header {
    package parser.antlr;
}

wholeInput: expr EOF;

expr: sum;

sum: sign=('+'|'-')? first=explicitProduct (rest+=otherTerm)*;

otherTerm: sign=('+'|'-') explicitProduct;

explicitProduct: first=implicitProduct (rest+=otherExplicitFactor)*;

otherExplicitFactor: op=('*'|':') implicitProduct;

implicitProduct: first=firstFactor (others+=otherFactor)*;

firstFactor
    : sign=('+' | '-') factor=firstFactor           #firstFactorWithSign
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

decimalNumber: DECNUM;

recurringDecimalNumber: decimal=DECNUM '[' repetend=NATNUM ']';

variable: VARIABLE;

fragment DIGIT: [0-9];
NATNUM: DIGIT+;
DECNUM: NATNUM '.' NATNUM;

OPEN_SQUARE: '[.';
CLOSE_SQUARE: '.]';

OPEN_CURLY: '{.';
CLOSE_CURLY: '.}';

VARIABLE: [a-z];
WHITESPACE: [ \t] -> skip;

UNKNOWN: .;