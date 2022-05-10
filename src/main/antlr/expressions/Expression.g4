grammar Expression;

expr: sum;

sum: first=firstTerm (rest+=otherTerm)*;

firstTerm: sign=('+'|'-')? explicitProduct;

otherTerm: sign=('+'|'-') explicitProduct;

explicitProduct: products+=implicitProduct ('*' products+=implicitProduct)*;

implicitProduct: first=firstFactor (others+=otherFactor)*;

firstFactor: fraction | power | atom;

otherFactor: power | atom;

fraction: '[' num=expr '/' den=expr ']';

power: '[' base=atom '^' exp=expr ']';

bracket: '(' expr ')';

atom: nonNumericAtom | naturalNumber;

nonNumericAtom: bracket | variable;

naturalNumber: NATNUM;

variable: VARIABLE;

fragment DIGIT: [0-9];
NATNUM: DIGIT+;

VARIABLE: [a-z];
