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

  // TODO: adjust GM to set entire product to 0 when multiplying by 0
  'x=(-7±sqrt(7^2-4*1*0))/(2*1)',

  // TODO: extend GM gesture to cover these: 0^x, 1^x, x^1, x^0, radical cases
  '1^(sqrt 2+1)',
  '0^(3/2)',
];

export const ruleWhiteList = [
  'Collecting.CombineTwoSimpleLikeTerms',
  'Decimals.ConvertFractionWithPowerOfTenDenominatorToDecimal',
  'Decimals.EvaluateDecimalAddition',
  'Decimals.EvaluateDecimalPowerDirectly',
  'Decimals.EvaluateDecimalProduct',
  'Decimals.EvaluateDecimalSubtraction',
  'FractionArithmetic.SimplifyFractionToInteger',
  'General.EliminateOneInProduct',
  'General.EliminateZeroInSum',
  'General.EvaluateExpressionToThePowerOfZero',
  'General.EvaluateOneToAnyPower',
  'General.EvaluateProductContainingZero',
  'General.EvaluateZeroToAPositivePower',
  'General.NormalizeNegativeSignOfIntegerInSum',
  'General.RemoveBracketProductInProduct',
  'General.RemoveBracketSumInSum',
  'General.RemoveUnitaryCoefficient',
  'General.RewriteFractionOfPowersWithSameExponent',
  'General.RewriteProductOfPowersWithSameBase',
  'General.RewriteProductOfPowersWithSameExponent',
  'General.SimplifyExpressionToThePowerOfOne',
  'General.SimplifyFractionWithOneDenominator',
  'General.SimplifyUnitFractionToOne',
  'General.SimplifyZeroNumeratorFractionToZero',
  'IntegerArithmetic.EvaluateIntegerAddition',
  'IntegerArithmetic.EvaluateIntegerPowerDirectly',
  'IntegerArithmetic.EvaluateIntegerProduct',
  'IntegerArithmetic.EvaluateIntegerSubtraction',
  'IntegerArithmetic.SimplifyEvenPowerOfNegative',
  'Decimals.ExpandFractionToPowerOfTenDenominator',
  'Decimals.MultiplyFractionOfDecimalsByPowerOfTen',
  // This one is weird as it contains the following test: 9900*x=31101 -> 31101/9900 with the x disappearing into thin air
  // I guess even if we find the reason gm actions are not being executed we will still need to work on this
  // TODO: ask solver team about this
  //'Decimals.SolveLinearEquation',

  // Also, any test case containing equations is prone to this very same behavior

  'Equations.DivideByCoefficientOfVariable',
  // 'Equations.FlipEquation',
  // 'Equations.MoveConstantsToTheLeft',
  // 'Equations.MoveConstantsToTheRight',

  // These cases are pretty stupid, if you move a variable to the other side of an equation,
  // on the original side it should be replaced by zero, not the variable minus itself.
  // We can't expect GM to have this solution.
  // - Abel
  // 'Equations.MoveVariablesToTheLeft',

  // 'Equations.MoveVariablesToTheRight',
  // 'Equations.MultiplyByInverseCoefficientOfVariable',
  // 'Equations.MultiplyEquationByLCD',
  // 'Equations.NegateBothSides',
  // 'Equations.TakeRootOfBothSides',
  // 'Equations.TakeRootOfBothSidesRHSIsZero',

  // In this test for some cases the \cdot is hidden and the double tap is used, except
  // it is not specified where to double tap (the \cdot can't be selected),
  // Workaround: select the parenthesis that follows the operator
  // Related problem: GM simplifies the expression while Solver does not (due to this some tests will fail if uncommented)
  // - Abel
  // 'Expand.ApplyFoilMethod',

  // In these cases the behavior of Solver seems to be too different to align them up,
  // GM will always simplify the distributed expressions (e.g instead of 4*11 we have 44)
  // - Abel
  // 'Expand.DistributeMultiplicationOverSum',

  'Expand.DistributeNegativeOverBracket',

  // GM simplifies the expression further than Solver in some test cases
  // (e.g (2*x)^3 - 8*x^3)
  // 'Expand.ExpandBinomialCubedUsingIdentity',

  // GM simplifies the expression further than Solver in some test cases
  // 'Expand.ExpandBinomialSquaredUsingIdentity',

  // 'Expand.ExpandDoubleBrackets',
  // 'Expand.ExpandProductOfSumAndDifference',
  // 'Expand.ExpandTrinomialSquaredUsingIdentity',
  // 'FractionArithmetic.BringToCommonDenominator',
  // 'FractionArithmetic.ConvertIntegerToFraction',
  // 'General.AddClarifyingBracket',
  // 'General.CancelAdditiveInverseElements',
  // 'General.CancelCommonTerms',

  // Most of these work fine, except some isolated cases where the action is not executed
  // even though the selectors and action type are correct
  // - Abel
  // 'General.CancelDenominator',

  // 'General.CancelRootIndexAndExponent',
  // 'General.DistributePowerOfProduct',
  // 'General.DistributeSumOfPowers',
  'General.EvaluateZeroToThePowerOfZero',
  // 'General.FactorMinusFromSum',
  // 'General.FlipFractionUnderNegativePower',
  // 'General.MoveSignOfNegativeFactorOutOfProduct',
  // 'General.MultiplyExponentsUsingPowerRule',
  // 'General.RemoveRedundantBracket',
  // 'General.RemoveRedundantPlusSign',

  // Same here, even though selectors are correct, the expressions are not moved.
  // I tried to select the parents (as when dragging onto) but it didn't change anything
  // 'General.ReorderProduct',

  // Same here, even though selectors are correct, the expressions are not moved.
  // 'General.RewriteFractionOfPowersWithSameBase',

  // 'General.RewriteIntegerOrderRootAsPower',
  'General.RewritePowerAsProduct',
  'General.RewritePowerUnderRoot',
  'General.RewriteProductOfPowersWithInverseBase',
  // 'General.RewriteProductOfPowersWithInverseFractionBase',
  // 'General.RewriteProductOfPowersWithNegatedExponent',

  // Here GM does not simplify the parentheses by default, but the actions are executed correctly
  // 'General.SimplifyDoubleMinus',

  'General.SimplifyProductOfConjugates',
  // 'General.SimplifyProductWithTwoNegativeFactors',
  // 'General.SimplifyZeroDenominatorFractionToUndefined',
  // 'Inequalities.CancelCommonTermsOnBothSides',
  // 'Inequalities.MoveConstantsToTheLeft',
  // 'Inequalities.MoveConstantsToTheRight',
  // 'Inequalities.MoveVariablesToTheLeft',
  // 'Inequalities.MoveVariablesToTheRight',
];
