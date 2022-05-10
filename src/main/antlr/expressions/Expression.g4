grammar Expression;

expr: sum;

sum: first=explicitProduct (rest+=otherTerm)*;

otherTerm: sign=('+'|'-') explicitProduct;

explicitProduct: products+=implicitProduct ('*' products+=implicitProduct)*;

implicitProduct: first=firstFactor (others+=otherFactor)*;

firstFactor
    : sign=('+' | '-') factor=firstFactor           #firstFactorWithSign
    | (mixedNumber | fraction | power | atom)       #firstFactorWithoutSign
;

otherFactor: power | nonNumericAtom;

fraction: '[' num=expr '/' den=expr ']';

power: '[' base=atom '^' exp=expr ']';

bracket: '(' expr ')';

atom: nonNumericAtom | naturalNumber;

nonNumericAtom: bracket | variable;

mixedNumber: '[' integer=naturalNumber num=naturalNumber '/' den=naturalNumber ']';

naturalNumber: NATNUM;

variable: VARIABLE;

fragment DIGIT: [0-9];
NATNUM: DIGIT+;

VARIABLE: [a-z];
WHITESPACE: [ \t] -> skip;
