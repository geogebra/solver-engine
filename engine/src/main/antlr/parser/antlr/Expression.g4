grammar Expression;

@header {
    package parser.antlr;
}

wholeInput: (inputList | singleInput | void) EOF;

inputList: elements += singleInput (',' elements += singleInput)+;

singleInput: statementUnion | set | expr | undefined | name;

// statements are in disjunctive normal form (constraints and systems both represent conjunctions)
statementUnion: statements += statementWithConstraints ('OR' statements += statementWithConstraints)*;

statementWithConstraints: stmt=statementSystem ('GIVEN' constraints+=statementSystem)*;

statementSystem: statements += simpleStatement ('AND' statements += simpleStatement)*;

simpleStatement: equation | equationAddition | equationSubtraction | inequality | doubleInequality | inequation | solution;

doubleInequality: first=expr left=('<' | '<=') second=expr right=('<' | '<=') third=expr
    | first=expr left=('>' | '>=') second=expr right=('>' | '>=') third=expr;

equationAddition: eq1=equation '/+/' eq2=equation;
equationSubtraction: eq1=equation '/-/' eq2=equation;

equation: lhs=expr '=' rhs=expr;
inequation: lhs=expr '!=' rhs=expr;
inequality: lhs=expr comparator=('<' | '<=' | '>' | '>=') rhs=expr;

solution
    : 'Identity' '[' (vars=variables ':')? stmt=statementUnion ']'                      #identity
    | 'Contradiction' '[' (vars=variables ':')? (stmt=statementUnion | undefined) ']'   #contradiction
    | 'ImplicitSolution' '[' vars=variables ':' stmt=statementUnion ']'                 #implicitSolution
    | 'SetSolution' '[' vars=variables ':' solutionSet=set ']'                          #setSolution
    ;

set: cartesianProduct | setUnion | setDifference;

cartesianProduct: first=simpleSet ('*' rest+=simpleSet)*;

setUnion: 'SetUnion' '[' first=simpleSet (',' rest+=simpleSet)+ ']';

setDifference: left=simpleSet '\\' right=simpleSet;

simpleSet: emptySet | finiteSet | reals | interval;

emptySet: '{' '}';

finiteSet: '{' first=element (',' rest+=element)* '}';

reals: REALS;

interval: leftBracket=('(' | '[') left=expr ',' right=expr rightBracket=(')' | ']');

element: tuple | expr;

tuple: '(' first=element (',' rest+=element)+ ')';

expr: sum | infinity | minusInfinity | expressionWithConstraint;

expressionWithConstraint: sum 'GIVEN' simpleStatement;

sum: first=firstTerm (rest+=otherTerm)*;

firstTerm
    : sign=('+' | '-' | PLUSMINUS)? term             #realFirstTerm
    | OPEN_PARTIAL sum CLOSE_PARTIAL                 #firstPartialSum
    ;

otherTerm
    : sign=('+' | '-' | PLUSMINUS) term              #realOtherTerm
    | '+' OPEN_PARTIAL sum CLOSE_PARTIAL             #otherPartialSum
    ;

term: product | percentage | percentageOf;

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

percentage: atom '%';
percentageOf: part=atom '%of' base=atom;

power: '[' base=atom '^' exp=expr ']';

sqrt: 'sqrt[' radicand=expr ']';

root: 'root[' radicand=expr ',' order=expr ']';

absoluteValue: 'abs[' argument=expr ']';

logarithm
    : 'ln' argument=implicitProduct                     #naturalLog
    | 'log' argument=implicitProduct                    #logBase10
    | 'log_[' base=expr ']' argument=implicitProduct    #log
    ;

trigFunction
    : function=TRIG_FUNCTION argument=implicitProduct                       # simpleTrigFunction
    | '[' base=TRIG_FUNCTION '^' exp=expr ']' argument=implicitProduct      # powerTrigFunction
    ;

bracket
    : '(' expr ')'                  #roundBracket
    | OPEN_SQUARE expr CLOSE_SQUARE #squareBracket
    | OPEN_CURLY expr CLOSE_CURLY   #curlyBracket
    ;

atom: bracket
    | sqrt | root | absoluteValue | logarithm | trigFunction
    | derivative | indefiniteIntegral | definiteIntegral
    | vector | matrix
    | variable | constant | naturalNumber | decimalNumber | recurringDecimalNumber
    ;

variablePower
    : variable                                      #justVariable
    | '[' base=variable '^' exp=naturalNumber ']'   #realVariablePower
    ;

derivative
    : 'diff' '[' product '/' variablePower ']'                              #firstDerivative
    | '[' 'diff' '^' order=expr ']' '[' product '/' (vars+=variablePower)+ ']'  #nthDerivative
    ;

indefiniteIntegral: 'prim' '[' function=expr ',' variable ']';

definiteIntegral: 'int' '[' lowerBound=expr ',' upperBound=expr ',' function=expr ',' variable ']';

vector: 'vec' '[' expr (',' expr)* ']';

matrix: 'mat' '[' matrixRow (';' matrixRow)* ']';

matrixRow: expr (',' expr)*;

constant
    : PI                            #pi
    | E                             #e
    | I                             #imaginaryUnit
    ;

mixedNumber: '[' integer=NATNUM num=NATNUM '/' den=NATNUM ']';

naturalNumber: NATNUM;

infinity: INFINITY;
minusInfinity: '-' INFINITY;

undefined: UNDEFINED;

decimalNumber: DECNUM;

recurringDecimalNumber: RECURRING_DECNUM;

variables: first=variable (',' rest+=variable)*;

variable: LETTER | LETTER_WITH_SUBSCRIPT;

name: NAME;

void: VOID;

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

PI: '/pi/';
E: '/e/';
I: '/i/';
REALS: '/reals/';
INFINITY: '/infinity/';
UNDEFINED: '/undefined/';

fragment ROMAN_LETTER: [a-zA-Z];
fragment GREEK_LETTER: '\\' ('alpha' | 'beta' | 'gamma' | 'delta' | 'epsilon' | 'zeta' | 'eta' | 'theta' | 'iota' | 'kappa' | 'lambda' | 'mu' | 'nu' | 'xi' | 'omicron' | 'pi' | 'rho' | 'sigma' | 'tau' | 'upsilon' | 'phi' | 'chi' | 'psi' | 'omega'
          | 'Alpha' | 'Beta' | 'Gamma' | 'Delta' | 'Epsilon' | 'Zeta' | 'Eta' | 'Theta' | 'Iota' | 'Kappa' | 'Lambda' | 'Mu' | 'Nu' | 'Xi' | 'Omicron' | 'Pi' | 'Rho' | 'Sigma' | 'Tau' | 'Upsilon' | 'Phi' | 'Chi' | 'Psi' | 'Omega');
LETTER: ROMAN_LETTER | GREEK_LETTER;
LETTER_WITH_SUBSCRIPT: LETTER '_' (LETTER | DIGIT+);

TRIG_FUNCTION: 'sin' | 'cos' | 'tan' | 'arcsin' | 'arccos' | 'arctan'
             | 'sec' | 'csc' | 'cot' | 'arcsec' | 'arccsc' | 'arccot'
             | 'sinh' | 'cosh' | 'tanh' | 'arsinh' | 'arcosh' | 'artanh'
             | 'sech' | 'csch' | 'coth' | 'arsech' | 'arcsch' | 'arcoth'
             ;

NAME: '"' (~ '"')+ '"';

VOID: '/void/';

WHITESPACE: [ \t] -> skip;
UNKNOWN: .;