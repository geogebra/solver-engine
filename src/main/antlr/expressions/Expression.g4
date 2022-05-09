grammar Expression;

expr: sum;

sum: first=firstTerm (rest+=otherTerm)*;

firstTerm: sign=('+'|'-')? product;

otherTerm: sign=('+'|'-') product;

product: implicitProduct | explicitProduct;

explicitProduct: products+=implicitProduct ('*' products+=implicitProduct)+;

implicitProduct: first=factor (others+=nonNumericFactor)*;

nonNumericFactor: nonNumericAtom | power;

fraction: '[' num=expr '/' den=expr ']';

factor: atom | power;

power: base=atom '^' exp=atom;

bracket: '(' expr ')';

atom: nonNumericAtom | naturalNumber;

nonNumericAtom: bracket | fraction | variable;

naturalNumber: NATNUM;

variable: VARIABLE;

fragment DIGIT: [0-9];
NATNUM: DIGIT+;

VARIABLE: [a-z];
