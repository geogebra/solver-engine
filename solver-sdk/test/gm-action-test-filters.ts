// DEPRECATED - will be removed soon
// need to move all valuable information here somewhere else

export const ruleSkipList = [
  // [TRACKED] Adding like fractions works differently in GM: GM goes directly
  // from 1/7+2/7 to 3/7 instead.
  'FractionArithmetic.AddLikeFractions',
  'FractionArithmetic.SubtractLikeFractions',

  // [TRACKED] adjust the solver to do x*x => x^2 instead of x^(1+1); GM does that
  // when multiplying with an implied ^1 power (e.g. x^6*x or x*x but not x^(1+2)*x)
  'General.RewriteProductOfPowersWithSameBase',

  // [TRACKED] GM simplifies more than Solver: 5*(x+1) ==> 5x+5 vs. 5x+5*1
  // [TRACKED] In GM, it is easier to distribute one term at a time: 2x(1+x) ==> 2(x+x^2) vs. 2x*1+2x*x^2
  'Expand.DistributeMultiplicationOverSum',

  // [TRACKED] There is no special way to automatically compute the least common denominator
  // in GM. Also, should it take more than one action? How automatic should it be?
  'FractionArithmetic.BringToCommonDenominator',

  // [TRACKED] The expressions are not simplified the same way in Solver and GM:
  // - Gm simplifies x*x to x^2
  // - Gm simplifies products e.g. 2*2*x*x to 4*x^2
  'Expand.ApplyFoilMethod',

  /* [TRACKED] The expressions are not simplified the same way in Solver and GM:
    - Gm simplifies powers automatically e.g. (2*x)^2 - 4*x^2; 3^2 vs 9; 2*x*2*x - 4*x^2
  */
  'Expand.ExpandBinomialSquaredUsingIdentity',
  'Expand.ExpandTrinomialSquaredUsingIdentity',
  'Expand.ExpandDoubleBrackets',
  'Expand.ExpandBinomialCubedUsingIdentity',

  /* [TRACKED]
    - Gm does not simplify x^1 to x
    - Gm does not distribute all the components from the exponents if there are
      multiple, only the selected one, e.g 2^(a+b+c) - moving c to the right
      will yield 2^(a+b)*2^c instead of 2^a*2^b*2^c as solver would expect
  */
  'General.DistributeSumOfPowers',

  /* [TRACKED] Resolve what the intended behavior should be, e.g.:
   * x+(-3)^3 ==> x-3^3 or x+(-3^3)
   * 2*(-x)^3 ==> 2*(-x^3) (this one is clear)
   */
  'General.SimplifyOddPowerOfNegative',

  ///////////////// SIGNS ///////////////////////////////////

  // TODO: GM does not combine the signs as the solver does:
  // x+(-4)*x ==> x+-(4)*x instead of x-4x
  // TODO: GM does not remove brackets as the solver does:
  // (-7)*3 ==> -(7)*3 instead of -7*3
  'General.MoveSignOfNegativeFactorOutOfProduct',

  // TODO: Remove brackets after simplifying signs in GM: -3*(-a) ==> 3a not 3(a)
  // TODO: Allow dragging a negative sign onto a minus sign in GM: 1-3*(-x) ==> 1+3x
  'General.SimplifyProductWithTwoNegativeFactors',

  // TODO: Find a way to factor out a "-" from a sum without brackets in GM: -3-x ==> -(3+x)
  'General.FactorMinusFromSumWithAllNegativeTerms',

  /* TODO: improve GM behavior for signs. Currently, GM does this:
   - for nested signs: -(-x) need to drag signs & brackets stay
   - for minus and sign: 1-(-x) need to click the minus sign & brackets go
  */
  'General.SimplifyDoubleMinus',

  ///////////////// FORMULAS ///////////////////////////////////

  // TODO: 1. Extend GM so it also covers (a-b)(a+b) and not just (a+b)(a-b)
  // TODO: 2. Adjust ActionInfo in solver so that the target is correctly defined
  //          when the formula is applied to part of a product, e.g. 2*(a-b)(a+b)
  'Expand.ExpandProductOfSumAndDifference',

  ///////////////// OTHER ///////////////////////////////////

  /* TODO: Cleanup old, weird Solver plan */
  'Decimals.SolveLinearEquation',

  // [TRACKED] APA doesn't support automatic rounding in intermediate steps.
  'Approximation.ApproximateDecimalProduct',

  // skipping it currently as now the inverse is multiplied on the left-side of both
  // sides of the solvable
  'Equations.MultiplyByInverseCoefficientOfVariable',
];

export const expressionTypeSkipList = [
  // [TRACKED] support mixed numbers in GM
  'MixedNumber',
  // [TRACKED] support inequations "!=" in GM
  'Inequation',
  // [TRACKED] support recurring decimals in GM (?)
  'RecurringDecimal',
  // [TRACKED] Constraints are not yet supported
  'ExpressionWithConstraint',
  // [TRACKED] GM doesn't support extra '+' signs: 3+(+4)
  'Plus',
];

/**
 * This list is composed of the input of tests that we want to skip because
 * the gm-actions are not aligned with the solver steps
 */
export const expressionSkipList = [
  // GM removes the *1 automatically, Solver doesn't
  // [OK for now]
  '3/3*x<7*(-3)',
  '45/45*C=(F-32)*5/9',
  '2/2*x^2+2*x+2*1/2=0',
  '3*x+2/2*x^2=15/8',
  '2/2*x^2+1/2*5*x=7/2',
  '2/2*x^2+1/2*x=1/2',
  '2/2*x^2+1/2*(-x)=45/2',

  // GM now automatically puts the sign in front of the fraction when dividing an equation
  // [OK for now]
  '3.1*x>=-0.99', // GM: (3.1*x)/3.1>=-{0.99/3.1}, Solver: (3.1*x)/3.1>=-0.99/3.1
  '0.2*x<-11.54',
  '3*x=-22',
  '0.2*x=-11.54',
  '3.2*x<=-1',
  '-3*x=-1',
  '3.2*x=-1',

  // GM puts the term next to the opposite one when multiplying both sides of an equation
  // [TODO SOLVER] need to use new gm-friendly rule for this when running tests
  '9/5*C=F-32',

  // Some cases of RewriteFractionOfPowersWithSameExponent are now supported, but GM needs
  // a more predictable target area [TRACKED]
  '5*5^(1/2)*2^(1/2)',
  '2^(1/3)*3^(1/3)*6^(1/2)',

  // [TRACKED] adjust Solver behavior (either the default or gm-friendly one)
  // currently, it rewrites 3x=1/2 to x=1/2/3, not x=1/2*1/3
  '3*(x+1)^2=1/3',

  // TODO: adjust GM to handle signs and brackets better:
  // addition: a+(3-5)b => a+(-2)b ; -(2-4)+1 ==> -(-2)+1
  // multiplication: a-2*(-3) => a-(-6) ; -(-1*x)+1 ==> -(-x)+1
  // leading addend: (-1)+1 => -1+1
  // drag factors on top of each other: (-2)(-3) => 6
  // exponents: (x^2)^-2 => x^(2*(-2)) not x^(2*-2)
  // negative power: 2*(-x)^3 => 2*(-x^3) not 2*-x^3
  // resolving negative: x+(-4)*x ==> x-4x not x+-(4)*x
  // remove brackets: (-7)*3 => -7*3 not -(7)*3
  // allow drag negative sign on top of minus sign: 1-3*(-x) ==> 1+3x
  // remove brackets: -3*(-a) ==> 3a not 3(a)
  // change sign into minus: -x=2 ==> -x+x=2+x not +-x+x=2+x
  // multiply equation: x/3=-7 ==> x/3*3=-7*3 not x/3*3=(-7)*3
  '5*x+(1-5)*x^2',
  '(1-2)*a*x+1/3*a*b*x+3/4*a*b*x',
  '4*x^2+(-6-6)*x+9',
  '4*x^4+(1-12)*x^2+9+4*x^3-6*x',
  '8*x^3+(-12-24)*x^2+(-12*x)*(-3)+18*x-27',
  '(4*x^2+(-6-6)*x+9)*(2*x-3)',
  '3*x^3+10*x^2+(-25-6)*x+10',
  '34+60+6-(4+10-15*(-2))',
  '(15-36)*x-210+18=1',
  '(3-4)*x-42+2=8*(2*x-5)',
  '8+(2-4)*sqrt 2-7=0',
  '-((3-4)*sqrt[3]3)-(-sqrt[3]3-3*sqrt[3]3+5*sqrt[3]3)',
  '-(-1*sqrt[3]3)-(-sqrt[3]3-3*sqrt[3]3+5*sqrt[3]3)',
  '-(3-4)*sqrt[3]3-(-sqrt[3]3-3*sqrt[3]3+5*sqrt[3]3)',
  '(-0.2)*(-0.2)',
  '-(3-4)*sqrt[3]3-(-sqrt[3]3-3*sqrt[3]3+5*sqrt[3]3)',
  '(5^2)^(-{1/2})', // GM: 5^(2*-{1/2}); Solver: 5^(2*(-{1/2}))
  '3*x+3-2*x-2*(-6)=0',
  '3*x-42-4*x-2*(-1)=8*(2*x-5)',
  '(3-4)*x-40=8*(2*x-5)',
  '15*x-210-36*x-18*(-1)=1',
  '(15-36)*x-192=1',
  '8*x^3+(-12-24)*x^2+36*x+18*x-27',
  'x/3=-7', // GM: x/3*3=-7*3; Solver: x/3*3=(-7)*3
  '-{1/3}*x>7', // GM: -{1/3}*-3*x<7*-3, Solver: (-{1/3}*x)*(-3)<7*(-3)

  // [TRACKED] adjust GM to set entire product to 0 when multiplying by 0
  'x=(-7±sqrt(7^2-4*1*0))/(2*1)',

  // [TRACKED] GM turns (2*a^2)^3 into 2^3*a^(2*3), while the solver stacks the
  // exponents first. Adjust either the Solver or GM.
  '(3*a*b^2*c*x^4)^3',
  '(2*x^2)^2+x^2+(-3)^2+2*2*x^2*x+2*x*(-3)+2*(-3)*2*x^2',
  '(2^3*3)^(2/3)',
  '(5+2*x^2)*(25-10*x^2+(2*x^2)^2)',
  '24*sqrt[3]((2^3*3)^2)',
  'sqrt[6]((2^3*3)^5)',
  '2*(2^2*3)^(1/2)',
  'sqrt[5]((2^2*3)^4)',

  // [TRACKED] GM automatically removes a "0" on the other side of the equation if
  // the user adds or subtracts from both side. The Solver doesn't.
  // E.g. x^4+1=0 ==> x^4+1-1=-1 VS ==> x^4+1-1=0-1
  'x^4+1=0',
  'x^3+10=0',
  '8-x^3=0',
  '3*x+22=0',
  'x+15=0',
  '1-3*x=0',
  'x^2-3=0',
  'x+1=0',
  'x^4-3*x^2+2=0',
  'x-5=0',
  'x^6+2-3*x^3=0',
  '(x+1)^2+2*(x+1)+3=0',
  '(x+1)^4+2*(x+1)^2-6=0',
  '2*x^2-45-x=0',
  'x^2+x+10=0',
  'A-b*h=0',
  'a^3*b+c=0',
  'x^6+y^2+1=0',

  // GM automatically removes factors of *1 after cancelling out a fraction,
  // but the Solver doesn't.
  '2/2*h*(B+b)=2*S',
  '45/45*C=5/9*(F-32)',
  '3/3*x<(-3)*7', // GM: x<(-3)*7, Solver: 1*x<(-3)*7

  // [TRACKED] Rewrite equation: insert position of inverted term.
  // GM inserts the inverted term next to the dragged term. It also inserts
  // the term at a different position for fractions: E.g.: x/3=7 ==> (x*3)/3=7*3 vs x/3*3=7*3
  'x+1<2*x+3',
  'x^2=6*x+5',
  'x^2=x+3',
  '6*x-1+2*x^2=11/4',
  '3.6*x+2.2=0.4*x+1.2',
  '3.1*x+2.2=2.9*x-9.34',
  'x/9=-1',
  '1+x=3/5',
  '4=11+x/3',
  '2*x^2-3=3*x^2+4',
  '4*x^2+5=2*x^2+8',
  '3.6*x+2.2<=0.4*x+1.2',
  '3.1*x+2.2<2.9*x-9.34',
  '4>11+x/3',
  'x/3<-7',
  'x+1=2*x+3',
  '3*a+2*b<9',
  '(2*h*x)/3=1',
  'x*y/2=5',

  // [TRACKED] In GM, it is easier to move one term at a time, though the Solver doesn't
  // TODO: figure out how to tell the GM Action that several terms should be moved
  // at the same time (e.g.: MoveVariablesToTheLeft in SolvableRules.kt:139, `variables`
  // should be a list of addends, but is a sum)
  'x^2=1-x-x^2',

  // TODO: GM doesn't remove the brackets automatically here
  '-(2*sqrt 2)^3', // GM: -(2^3*(sqrt 2)^3), Solver: -2^3*(sqrt 2)^3

  // TODO: this should work in GM, find out why it doesn't
  '24+2*2*sqrt 6*3*sqrt 2+(3*sqrt 2)^2',

  // TODO: in the test case, the Solver goes to "(0+1)*x...", but in the Poker, it directly goes to
  // "1*x..." as it should. Why?
  '(6-6+1)*x^2+30-x^6',

  // Factoring: GM can only factor 2 things at a time
  '2*x^2-4*x+2=0',
  '2*x^2+4*x+2=0',

  // Factoring: GM removes brackets around single remaining factor, but Solver doesn't
  '3*((x+1)^3+2*(x+1)^2)', // GM: 3*(x+1)^2*(x+1+2); Solver: 3*(x+1)^2*((x+1)+2)

  // TODO: these test were introduced in PLUT-845 to test advanced balancing of equations
  '1=3/5-x',
  'x-2=36',
  '3*x=1',

  // Currently GM doesn't check if an absolute value is negative or positive
  // for more complicated cases
  // TODO GM: support those cases
  '|-2+sqrt 2|',
  '|2-sqrt 2|',
  '|1+sqrt 2|=0',
  '|-(x+sqrt 2)|-|x+sqrt 2|=0',

  // Turning division into a fraction: the solver puts all other factors into
  // the numerator, while GM puts one of the other factors into the numerator
  '3^2*3^(-{1/3})÷3^(1/2)',
  '5*x^7÷(-{1/5}*x^3)+12*x^3*y^7÷(-{1/5}*x^3)+(-{10/7}*x^5*y)÷(-{1/5}*x^3)',
  '-25*x^4+12*x^3*y^7÷(-{1/5}*x^3)+(-{10/7}*x^5*y)÷(-{1/5}*x^3)',

  // Turning division into a fraction: the solver removes brackets in the new
  // numerator automatically, while GM doesn't
  '-25*x^4-60*y^7+(-{10/7}*x^5*y)÷(-{1/5}*x^3)',

  // Issues introduced in PLUT-797
  '3/3*(x+1)^2=1/3*1/3',
  '1/3*x<-7',
  '3/3*x<3*(-7)',
];
