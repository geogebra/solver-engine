package methods.constantexpressions

import engine.expressions.Expression
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.operators.VariableOperator
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FindPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.condition
import engine.patterns.integerCondition
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.utility.divides
import methods.decimals.convertRecurringDecimalToFractionAndSimplify
import methods.decimals.convertTerminatingDecimalToFractionAndSimplify
import methods.decimals.normalizeFractionOfDecimals
import methods.fractionarithmetic.FractionArithmeticRules
import methods.fractionarithmetic.evaluateFractionSum
import methods.fractionarithmetic.evaluateSumOfFractionAndInteger
import methods.fractionarithmetic.multiplyAndSimplifyFractions
import methods.fractionarithmetic.normalizeFractions
import methods.fractionarithmetic.normalizeSignsInFraction
import methods.fractionarithmetic.simplifyFraction
import methods.fractionarithmetic.simplifyFractionsInExpression
import methods.fractionarithmetic.simplifyIntegerToNegativePower
import methods.fractionroots.FractionRootsRules
import methods.fractionroots.rationalizeDenominators
import methods.fractionroots.simplifyFractionOfRoots
import methods.general.GeneralRules
import methods.general.normalizeExpression
import methods.general.removeRedundantBrackets
import methods.general.simplifyProductOfPowersWithSameBase
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerarithmetic.evaluateSignedIntegerPower
import methods.integerarithmetic.simplifyIntegersInProduct
import methods.integerarithmetic.simplifyIntegersInSum
import methods.integerrationalexponents.IntegerRationalExponentsRules
import methods.integerrationalexponents.collectLikeRationalPowersAndSimplify
import methods.integerrationalexponents.simplifyRationalExponentsInProduct
import methods.integerroots.IntegerRootsRules
import methods.integerroots.cancelPowerOfARoot
import methods.integerroots.collectLikeRootsAndSimplify
import methods.integerroots.simplifyIntegerRoot
import methods.integerroots.simplifyIntegerRootToInteger
import methods.integerroots.simplifyPowerOfIntegerUnderRoot
import methods.integerroots.simplifyProductWithRoots
import methods.integerroots.simplifyRootOfRootWithCoefficient
import methods.mixednumbers.MixedNumbersRules

val simplifyPowers = plan {
    pattern = powerOf(AnyPattern(), AnyPattern())
    explanation(Explanation.SimplifyPowers)

    steps {
        whilePossible {
            firstOf {
                // one as base or as exponent
                option { deeply(GeneralRules.EvaluateOneToAnyPower) }
                option { deeply(GeneralRules.SimplifyExpressionToThePowerOfOne) }

                // zero as base or as exponent
                option { deeply(GeneralRules.EvaluateZeroToThePowerOfZero) }
                option { deeply(GeneralRules.EvaluateZeroToAPositivePower) }
                option { deeply(GeneralRules.EvaluateExpressionToThePowerOfZero) }

                // minus one and other negative integer powers
                option { deeply(simplifyIntegerToNegativePower) }
                option { deeply(FractionArithmeticRules.SimplifyFractionToMinusOne) }
                option { deeply(FractionArithmeticRules.SimplifyFractionNegativePower) }

                option { deeply(FractionArithmeticRules.DistributeFractionPositivePower) }
                option { deeply(IntegerArithmeticRules.SimplifyEvenPowerOfNegative) }
                option { deeply(IntegerArithmeticRules.SimplifyOddPowerOfNegative) }
                option { deeply(GeneralRules.DistributePowerOfProduct) }
                option { deeply(GeneralRules.ExpandBinomialSquared) }
                option { deeply(evaluateSignedIntegerPower) }
            }
        }
    }
}

val simplifyRootsInExpression = plan {
    explanation(Explanation.SimplifyRootsInExpression)

    steps {
        whilePossible {
            firstOf {
                option { deeply(IntegerRootsRules.SimplifyRootOfZero, deepFirst = true) }
                option { deeply(IntegerRootsRules.SimplifyRootOfOne, deepFirst = true) }
                option { deeply(simplifyIntegerRootToInteger, deepFirst = true) }
                option { deeply(cancelPowerOfARoot, deepFirst = true) }
                option { deeply(simplifyRootOfRootWithCoefficient, deepFirst = true) }
                option { deeply(simplifyIntegerRoot, deepFirst = true) }
                option { deeply(IntegerRootsRules.TurnPowerOfRootToRootOfPower, deepFirst = true) }
                option { deeply(simplifyFractionOfRoots, deepFirst = true) }
                option { deeply(FractionRootsRules.DistributeRadicalOverFraction, deepFirst = true) }
            }
        }
    }
}

val simpleTidyUpSteps = steps {
    firstOf {
        option { deeply(MixedNumbersRules.SplitMixedNumber) }
        option { deeply(GeneralRules.SimplifyZeroDenominatorFractionToUndefined) }
        option { deeply(GeneralRules.SimplifyZeroNumeratorFractionToZero) }
        option { deeply(GeneralRules.SimplifyFractionWithOneDenominator) }
        option { deeply(IntegerRationalExponentsRules.EvaluateNegativeToRationalExponentAsUndefined) }
        option { deeply(GeneralRules.EvaluateProductContainingZero) }
        option { deeply(GeneralRules.CancelAdditiveInverseElements) }
        option { deeply(IntegerRootsRules.SimplifyRootOfOne) }
    }
}

val simplifyAfterCollectingLikeTerms = steps {
    apply(simplifyFractionsInExpression)
    optionally { deeply(GeneralRules.MoveSignOfNegativeFactorOutOfProduct) }
    optionally { deeply(removeRedundantBrackets) }
    optionally { deeply(multiplyAndSimplifyFractions) }
}

val rewriteIntegerOrderRootsAsPowers = plan {
    val exponent = IntegerFractionPattern()
    pattern = FindPattern(
        powerOf(
            AnyPattern(),
            ConditionPattern(
                optionalNegOf(exponent),
                integerCondition(exponent.numerator, exponent.denominator) { n, d -> !d.divides(n) }
            )
        )
    )
    explanation(Explanation.RewriteIntegerOrderRootsAsPowers)
    steps {
        whilePossible {
            deeply(GeneralRules.RewriteIntegerOrderRootAsPower)
        }
    }
}

val simplificationSteps = steps {
    firstOf {
        option(simpleTidyUpSteps)
        option(rewriteIntegerOrderRootsAsPowers)

        option { deeply(removeRedundantBrackets, deepFirst = true) }

        option { deeply(simplifyPowerOfIntegerUnderRoot, deepFirst = true) }
        option { deeply(simplifyProductOfPowersWithSameBase) }
        option { deeply(simplifyPowers, deepFirst = true) }

        option(normalizeFractions)
        option(normalizeSignsInFraction)

        option { deeply(FractionArithmeticRules.AddLikeFractions, deepFirst = true) }
        option { deeply(simplifyFraction, deepFirst = true) }
        option { deeply(normalizeFractionOfDecimals, deepFirst = true) }
        option { deeply(convertTerminatingDecimalToFractionAndSimplify, deepFirst = true) }
        option { deeply(convertRecurringDecimalToFractionAndSimplify, deepFirst = true) }

        option { deeply(collectLikeRootsAndSimplify, deepFirst = true) }
        option { deeply(collectLikeRationalPowersAndSimplify, deepFirst = true) }

        option(simplifyRootsInExpression)
        option(simplifyRationalExponentsInProduct)

        option { deeply(multiplyAndSimplifyFractions, deepFirst = true) }
        option { deeply(simplifyProductWithRoots, deepFirst = true) }

        option { deeply(simplifyIntegersInProduct, deepFirst = true) }

        option { deeply(simplifyIntegersInSum, deepFirst = true) }
        option { deeply(evaluateFractionSum, deepFirst = true) }
        option { deeply(evaluateSumOfFractionAndInteger, deepFirst = true) }

        option { deeply(rationalizeDenominators, deepFirst = true) }
        option { deeply(GeneralRules.DistributeMultiplicationOverSum, deepFirst = true) }
    }
}

val simplifyConstantSubexpression = plan {
    explanation(Explanation.SimplifyExpressionInBrackets)
    pattern = condition(AnyPattern()) { it.hasBracket() }

    steps {
        whilePossible(simplificationSteps)
    }
}

private fun Expression.isConstantExpression(): Boolean {
    for (operand in operands) {
        if (!operand.isConstantExpression()) return false
    }

    return operator !is VariableOperator
}

val simplifyConstantExpression = plan {
    pattern = condition(AnyPattern()) { it.isConstantExpression() }
    explanation(Explanation.SimplifyConstantExpression)

    steps {
        whilePossible { deeply(simpleTidyUpSteps) }
        optionally(normalizeExpression)
        whilePossible { deeply(simplifyConstantSubexpression, deepFirst = true) }
        whilePossible(simplificationSteps)
    }
}
