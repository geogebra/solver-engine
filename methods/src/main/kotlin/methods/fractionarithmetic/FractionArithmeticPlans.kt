package methods.fractionarithmetic

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
import engine.steps.metadata.Skill
import methods.general.cancelCommonTerms
import methods.general.moveSignOfNegativeFactorOutOfProduct
import methods.general.simplifyDoubleMinus
import methods.general.simplifyEvenPowerOfNegative
import methods.general.simplifyOddPowerOfNegative
import methods.general.simplifyProductWithTwoNegativeFactors
import methods.integerarithmetic.simplifyArithmeticExpression

val normalizeFractionSigns = plan {
    firstOf {
        option(simplifyNegativeNumeratorAndDenominator)
        option(simplifyNegativeInNumerator)
        option(simplifyNegativeInDenominator)
    }
}

val normalizeSignsInProduct = plan {
    firstOf {
        option(simplifyDoubleMinus)
        option(simplifyProductWithTwoNegativeFactors)
        option(moveSignOfNegativeFactorOutOfProduct)
    }
}

val simplifyNumericFraction = plan {
    planId = PlanId.SimplifyNumericFraction

    val f = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())

    pattern = f

    explanation(Explanation.SimplifyNumericFraction, move(f))

    skill(Skill.SimplifyNumericFraction, move(f))

    firstOf {
        option(simplifyFractionToInteger)
        option {
            pipeline {
                step(findCommonFactorInFraction)
                step(cancelCommonTerms)
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

    explanation(Explanation.AddFractions, move(f1), move(f2))

    skill(Skill.AddFractions, move(f1), move(f2))

    pipeline {
        optionalStep(bringToCommonDenominator)
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

    explanation(Explanation.EvaluatePositiveFractionProduct, move(f1), move(f2))

    skill(Skill.MultiplyFractions, move(f1), move(f2))

    pipeline {
        step(multiplyFractions)
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
                            option(turnDivisionToFraction)
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

    explanation(Explanation.EvaluatePowerOfFraction, move(f), move(exponent))

    pipeline {
        step(distributeFractionPositivePower)
        step(simplifyArithmeticExpression)
    }
}
