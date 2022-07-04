package methods.plans

import engine.expressionmakers.move
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.bracketOf
import engine.patterns.fractionOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.plans.PlanId
import engine.plans.plan
import engine.steps.metadata.PlanExplanation
import engine.steps.metadata.Skill
import methods.rules.addLikeFractions
import methods.rules.cancelInAFraction
import methods.rules.commonDenominator
import methods.rules.distributeFractionPositivePower
import methods.rules.findCommonFactorInFraction
import methods.rules.moveSignOfNegativeFactorOutOfProduct
import methods.rules.multiplyPositiveFractions
import methods.rules.negativeDenominator
import methods.rules.negativeNumerator
import methods.rules.negativeNumeratorAndDenominator
import methods.rules.simplifyDividingByAFraction
import methods.rules.simplifyDividingByANumber
import methods.rules.simplifyDoubleNeg
import methods.rules.simplifyDoubleNegBracket
import methods.rules.simplifyEvenPowerOfNegative
import methods.rules.simplifyFractionNegativePower
import methods.rules.simplifyFractionToInteger
import methods.rules.simplifyFractionToMinusOne
import methods.rules.simplifyFractionWithFractionDenominator
import methods.rules.simplifyFractionWithFractionNumerator
import methods.rules.simplifyOddPowerOfNegative
import methods.rules.simplifyProductWithTwoNegativeFactors
import methods.rules.turnIntegerToMinusOneToFraction
import methods.rules.turnNegativePowerOfIntegerToFraction

val normalizeFractionSigns = plan {
    firstOf {
        option(negativeNumeratorAndDenominator)
        option(negativeNumerator)
        option(negativeDenominator)
    }
}

val normalizeSignsInProduct = plan {
    firstOf {
        option(simplifyDoubleNegBracket)
        option(simplifyDoubleNeg)
        option(simplifyProductWithTwoNegativeFactors)
        option(moveSignOfNegativeFactorOutOfProduct)
    }
}

val simplifyNumericFraction = plan {
    planId = PlanId.SimplifyNumericFraction

    val f = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())

    pattern = f

    explanation(PlanExplanation.SimplifyNumericFraction, move(f))

    skill(Skill.SimplifyNumericFraction, move(f))

    firstOf {
        option(simplifyFractionToInteger)
        option {
            pipeline {
                step(findCommonFactorInFraction)
                step(cancelInAFraction)
            }
        }
    }
}

val simplifyFractionsInExpression = plan {
    whilePossible {
        deeply(simplifyNumericFraction)
    }
}

val evaluatePositiveFractionSum = plan {
    planId = PlanId.EvaluatePositiveFractionSum

    val f1 = optionalNegOf(fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern()))
    val f2 = optionalNegOf(fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern()))

    pattern = sumContaining(f1, f2)

    explanation(PlanExplanation.AddFractions, move(f1), move(f2))

    skill(Skill.AddFractions, move(f1), move(f2))

    pipeline {
        optionalStep(commonDenominator)
        optionalStep(simplifyArithmeticExpression)
        step(addLikeFractions)
        step(simplifyArithmeticExpression)
        optionalStep(normalizeFractionSigns)
        optionalStep(simplifyFractionsInExpression)
    }
}

val evaluatePositiveFractionProduct = plan {
    planId = PlanId.EvaluatePositiveFractionProduct

    val f1 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
    val f2 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())

    pattern = productContaining(f1, f2)

    explanation(PlanExplanation.MultiplyFractions, move(f1), move(f2))

    skill(Skill.MultiplyFractions, move(f1), move(f2))

    pipeline {
        step(multiplyPositiveFractions)
        step(simplifyArithmeticExpression)
        optionalStep(simplifyFractionsInExpression)
    }
}

val evaluatePositiveFractionPower = plan {
    planId = PlanId.EvaluatePositiveFractionPower

    pipeline {
        optionalStep(simplifyFractionNegativePower)
        step(distributeFractionPositivePower)
        step(simplifyArithmeticExpression)
    }
}

val evaluateNegativePowerOfInteger = plan {
    pipeline {
        step(turnNegativePowerOfIntegerToFraction)
        step(simplifyArithmeticExpression)
    }
}

val combineFractionsInExpression = plan {
    planId = PlanId.CombineFractionsInExpression

    whilePossible {
        firstOf {
            option {
                whilePossible {
                    deeply {
                        firstOf {
                            option(simplifyEvenPowerOfNegative)
                            option(simplifyOddPowerOfNegative)
                            option(normalizeFractionSigns)
                            option(normalizeSignsInProduct)
                        }
                    }
                }
            }
            option {
                whilePossible {
                    deeply {
                        firstOf {
                            option(simplifyDividingByANumber)
                            option(simplifyDividingByAFraction)
                            option(simplifyFractionWithFractionDenominator)
                            option(simplifyFractionWithFractionNumerator)
                        }
                    }
                }
            }
            option {
                deeply(deepFirst = true) {
                    firstOf {
                        option(turnIntegerToMinusOneToFraction)
                        option(simplifyFractionToMinusOne)
                        option(evaluateNegativePowerOfInteger)
                        option(evaluatePositiveFractionPower)
                        option(evaluatePositiveFractionProduct)
                        option(evaluatePositiveFractionSum)
                    }
                }
            }
        }
    }
}

val evaluatePowerOfFraction = plan {
    planId = PlanId.EvaluatePowerOfFraction

    val f = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
    val exponent = UnsignedIntegerPattern()
    pattern = powerOf(bracketOf(f), exponent)

    explanation(PlanExplanation.EvaluatePowerOfFraction, move(f), move(exponent))

    pipeline {
        step(distributeFractionPositivePower)
        step(simplifyArithmeticExpression)
    }
}
