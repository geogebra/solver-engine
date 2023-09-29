export const ruleSkipList = [
  // Purposely rounding decimal arithmetic (beyond standard machine precision)
  // is not and will not be supported by GM, because it is generally not a good
  // idea to do in the middle of a calculation.
  'Approximation.ApproximateDecimalProduct',

  // This will always fail because GM goes directly from 1/7+2/7 to 3/7 instead
  // of {1+2}/7 like solver does. TODO: adjust in GM via a setting or adjust in
  // solver.
  'FractionArithmetic.AddLikeFractions',
  'FractionArithmetic.SubtractLikeFractions',

  // No GM gesture for this. TODO: ask students to do this via the keypad.
  'Approximation.RoundTerminatingDecimal',

  // TODO: Erik, please look at this and the one below it. David got stuck on them.
  'General.RewriteFractionOfPowersWithSameExponent',
  'General.RewriteProductOfPowersWithSameExponent',

  // TODO: add support for the following gesture / action to GM (e.g. x+1=3+1 ==> x=3 directly)
  'Equations.CancelCommonTermsOnBothSides',

  // TODO: adjust the solver to do x*x => x^2 instead of x^(1+1); GM does that
  // when multiplying with an implied ^1 power (e.g. x^6*x or x*x but not x^(1+2)*x)
  'General.RewriteProductOfPowersWithSameBase',

  /* TODO: Failing: 41 of 41
  Problems:
  - [OK] use 'left-of' instead of 'onto' or 'inside-left'
  - [OK] need to call finish_interaction or we'll be left with extra parentheses
  - [XX] need to pick up all of the remaining product, not just the first factor
  */
  'Expand.DistributeMultiplicationOverSum',

  /*
  TODO: Look into this more
  In (x*(x+1))/(x+1) instead of moving x+1:2 onto x+1 we should move /(x+1) onto *(x+1)
  We are currently using PM.Group but that does not seem to select the right targets
  */
  'General.CancelDenominator',

  /* In gm we can't bring two fraction to the same denominator using a single
  action. Example: in 3+1/2+1/3 solver expects that by moving 2 around 1/3 we
  would get 3+1*3/2*3+1*2/3*2, while we can only transform to 3 + 1/2 + 2/2*3 
  */
  'FractionArithmetic.BringToCommonDenominator',
  /* I adjusted this one so that the action would at least happen, but GM does
  not simplify the expression as much as solver: In 5*x+(-4)*x^2 when moving the
  - in front of the product we get 5*x+-(4)*x^2 instead of 5*x-4*x^2*/
  'General.MoveSignOfNegativeFactorOutOfProduct',
  /* TODO: Look into this more
    It seems that we need the parent of the actors/targets we are getting here, where should this adjustment be made?
    Check createPathMap, rearrangeNegativeProducts for errors as the selectors from solver are mismatched
  */
  'General.SimplifyProductWithTwoNegativeFactors',

  //  ===========================================================================  //
  // The tests below were made after the adjustments to the GMActions in the Rules
  //  ===========================================================================  //

  /* TODO: Look into this more
    It seems that we need the parent of the actors/targets we are getting here, where should this adjustment be made?
    Check createPathMap, rearrangeNegativeProducts for errors as the selectors from solver are mismatched
  */
  'General.FactorMinusFromSum',
  'General.RemoveRedundantPlusSign',

  /* The expressions are not simplified the same way in Solver and GM:
    - In case of sums: GM leaves the + resulting in expressions with '+-' which should be simplified to only '-'
    - In case of powers: GM removes the parentheses around the negative element
  */
  'IntegerArithmetic.SimplifyOddPowerOfNegative',
  /* Skip this for now, can't really figure out what's happening
   */
  'Expand.ExpandProductOfSumAndDifference',
  /* The expressions are not simplified the same way in Solver and GM:
    - Gm simplifies x*x to x^2
    - Gm simplifies products e.g. 2*2*x*x to 4*x^2
  */
  'Expand.ApplyFoilMethod',
  /* The expressions are not simplified the same way in Solver and GM:
    - Gm simplifies powers automatically e.g. (2*x)^2 - 4*x^2; 3^2 vs 9; 2*x*2*x - 4*x^2
  */
  'Expand.ExpandBinomialSquaredUsingIdentity',
  'Expand.ExpandTrinomialSquaredUsingIdentity',
  'Expand.ExpandDoubleBrackets',
  'Expand.ExpandBinomialCubedUsingIdentity',

  /*
    - Gm does not simplify x^1 to x
    - Gm does not distribute all the components from the exponents if there are
      multiple, only the selected one, e.g 2^(a+b+c) - moving c to the right
      will yield 2^(a+b)*2^c instead of 2^a*2^b*2^c as solver would expect
  */
  'General.DistributeSumOfPowers',
  /*
  The behavior of GM seems to be inconsistent in case of simplifying double
  negatives:
  - Sometimes the second minus should be dragged around the first, but the
    parentheses won't be simplified (this seems to be the case with expressions
    like -(-x) where there are no other components)
  - Sometimes the first minus should be tapped
  TODO: Ask Erik about this
  */
  'General.SimplifyDoubleMinus',
  /* In this case the GM action seems to be wrong, as it tries to drag the
  constant from the right side right-of some expression on the left side, but
  manually executing the action indicates the action should be dragging left-of
  the inequality sign/ right-inside the target expression
  */
  'Inequalities.MoveConstantsToTheLeft',
  'Equations.MoveConstantsToTheLeft',
  /* In these cases it seems that we need to use right-inside There doesn't seem
    to be a Position.RightInside, and I couldn't figure out how to use it in GM
    even if I added the type in solver-engine.
    These are closely related to the ones above
  */
  'Equations.MoveConstantsToTheRight',
  'Equations.MoveVariablesToTheLeft',
  'Equations.MoveVariablesToTheRight',
  'Equations.MoveConstantsInVariablesToTheRight',
  'Equations.MoveConstantsInVariablesToTheLeft',
  /* Similar to the previous one, although in this case the problem seems to be
  that the actor should be dragged 'right-inside'? */
  'Inequalities.CancelCommonTermsOnBothSides',
  'Inequalities.MoveVariablesToTheRight',
  'Inequalities.MoveConstantsToTheRight',
  'Inequalities.MoveVariablesToTheLeft',
  /* In this case the entire group should be selected, but even if the selection
  were right, we would have different result from solver, I couldn't find a way
  to drag only the denominator to the opposite side */
  'Inequalities.MultiplyByInverseCoefficientOfVariableAndFlipTheSign',
  /* This throws an error: TypeError: Cannot read properties of undefined (reading 'is_group'),
    but the action itself seems to be right, even so it's possible that gm would simplify this
    expression more then solver would*/
  'Inequalities.DivideByCoefficientOfVariable',
  /* I haven't been able to fix this but the target should use PM.Group as
  otherwise the multiplication seems to be executed on expressions with
  parentheses */
  'General.ReorderProduct',
  /* Gm does not automatically simplify sqrt[0] to 0 */
  'Equations.TakeRootOfBothSidesRHSIsZero',
  /* Parentheses are not automatically simplified in GM */
  'Equations.TakeRootOfBothSides',
  'Equations.NegateBothSides',
  'General.DistributePowerOfProduct',
  /* Parentheses are automatically simplified in GM */
  'General.SimplifyOddPowerOfNegative',
  /*
  TODO: Look into this more
  In (x*(x+1))/(x+1) instead of moving x+1:2 onto x+1 we should move /(x+1) onto *(x+1)
  We are currently using PM.Group but that does not seem to select the right targets
  */
  'Inequalities.MultiplyByInverseCoefficientOfVariable',
  'General.RewriteFractionOfPowersWithSameBase',
  'Equations.MultiplyByInverseCoefficientOfVariable',
  /* GM does not automatically simplify fractions when dividing both sides*/
  'Decimals.SolveLinearEquation',
];

export const expressionTypeSkipList = [
  // TODO: support mixed numbers in GM
  'MixedNumber',
  // TODO: support horizontal division "÷" in GM
  'DivideBy',
  // TODO: support inequations "!=" in GM
  'Inequation',
  // TODO: support recurring decimals in GM (?)
  'RecurringDecimal',
  // Constraints are not yet supported
  'ExpressionWithConstraint',
];

export const expressionSkipList = [
  // TODO: adjust Solver behavior (either the default or gm-friendly one)
  // currently, it rewrites 3x=1/2 to x=1/2/3, not x=1/(2*3)
  '3*(x+1)^2=1/3',

  // TODO: adjust GM to handle signs and brackets better:
  // addition: a+(3-5)b => a+(-2)b ; -(2-4)+1 ==> -(-2)+1
  // multiplication: a-2*(-3) => a-(-6) ; -(-1*x)+1 ==> -(-x)+1
  // leading addend: (-1)+1 => -1+1
  // drag factors on top of each other: (-2)(-3) => 6
  // exponents: (x^2)^-2 => x^(2*(-2)) not x^(2*-2)
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
  '(-1)+1',
  '(-0.2)*(-0.2)',
  '-(-1*sqrt[3]3)-(-sqrt[3]3-3*sqrt[3]3+5*sqrt[3]3)',
  '-(3-4)*sqrt[3]3-(-sqrt[3]3-3*sqrt[3]3+5*sqrt[3]3)',
  '(5^2)^(-{1/2})',

  // TODO: adjust GM to set entire product to 0 when multiplying by 0
  'x=(-7±sqrt(7^2-4*1*0))/(2*1)',

  // TODO: extend GM gesture to cover these: 0^x, 1^x, x^1, x^0, radical cases
  '1^(sqrt 2+1)',
  '0^(3/2)',

  // TODO: GM turns (2*a^2)^3 into 2^3*a^(2*3), while the solver stacks the
  // exponents first. Adjust either the Solver or GM.
  '(3*a*b^2*c*x^4)^3',
  '(2*x^2)^2+x^2+(-3)^2+2*2*x^2*x+2*x*(-3)+2*(-3)*2*x^2',
  '(2^3*3)^(2/3)',
  '(5+2*x^2)*(25-10*x^2+(2*x^2)^2)',
  '24*sqrt[3]((2^3*3)^2)',
  'sqrt[6]((2^3*3)^5)',
  '2*(2^2*3)^(1/2)',

  // TODO: this should work in GM, find out why it doesn't
  '24+2*2*sqrt 6*3*sqrt 2+(3*sqrt 2)^2',

  // TODO: usually the Solver and GM don't keep a 0 with inverse addends,
  // but the Solver does so here - adjust?
  '(6-6+1)*x^2+30-x^6',

  // TODO: fix GM bug with dragging factors across equal sign
  '-3*x=-1',
  '3.2*x=-1',
];
