package methods.fractionarithmetic

import engine.expressionmakers.move
import engine.methods.plan
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.bracketOf
import engine.patterns.fractionOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.metadata.Skill
import methods.general.cancelCommonTerms
import methods.general.normalizeNegativeSigns
import methods.integerarithmetic.simplifyArithmeticExpression
import methods.integerarithmetic.simplifyEvenPowerOfNegative
import methods.integerarithmetic.simplifyOddPowerOfNegative

val normalizeSignsInFraction = plan {
    explanation(Explanation.NormalizeSignsInFraction)

    whilePossible {
        deeply {
            firstOf {
                option(normalizeNegativeSigns)
                option(simplifyNegativeNumeratorAndDenominator)
                option(simplifyNegativeInNumerator)
                option(simplifyNegativeInDenominator)
            }
        }
    }
}

val normalizeFractionsAndDivisions = plan {
    // Normalize division / fractions (e.g. fractions within fractions)
    explanation(Explanation.NormalizeFractionsAndDivisions)

    whilePossible {
        deeply {
            firstOf {
                option(simplifyDividingByAFraction)
                option(simplifyFractionWithFractionDenominator)
                option(simplifyFractionWithFractionNumerator)
            }
        }
    }
}

val simplifyNumericFraction = plan {
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

val evaluateFractionSum = plan {
    val f1 = optionalNegOf(fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern()))
    val f2 = optionalNegOf(fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern()))

    pattern = sumContaining(f1, f2)

    explanation(Explanation.EvaluateFractionSum, move(f1), move(f2))

    skill(Skill.AddFractions, move(f1), move(f2))

    pipeline {
        optionalStep(bringToCommonDenominator)
        optionalStep(simplifyArithmeticExpression)
        step(addLikeFractions)
        step(simplifyArithmeticExpression)
        optionalStep(normalizeSignsInFraction)
        optionalStep(simplifyFractionsInExpression)
    }
}

val evaluatePositiveFractionProduct = plan {
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

val evaluatePositiveFractionProductByAnInteger = plan {
    pipeline {
        step(turnProductOfFractionByIntegerToFractionProduct)
        step(evaluatePositiveFractionProduct)
    }
}

val evaluateSumOfFractionAndInteger = plan {
    pipeline {
        step(turnSumOfFractionAndIntegerToFractionSum)
        step(simplifyArithmeticExpression)
        step(evaluateFractionSum)
    }
}

val evaluatePositiveFractionPower = plan {
    pipeline {
        optionalStep(simplifyEvenPowerOfNegative)
        optionalStep(simplifyOddPowerOfNegative)
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
    whilePossible {
        firstOf {
            option(normalizeSignsInFraction)
            option(normalizeFractionsAndDivisions)
            option {
                // Evaluate a fraction operation
                deeply(deepFirst = true) {
                    firstOf {
                        option(turnIntegerToMinusOneToFraction)
                        option(simplifyFractionToMinusOne)
                        option(evaluateNegativePowerOfInteger)
                        option(evaluatePositiveFractionPower)
                        option(evaluatePositiveFractionProduct)
                        option(evaluatePositiveFractionProductByAnInteger)
                        option(evaluateFractionSum)
                        option(evaluateSumOfFractionAndInteger)
                        option(turnDivisionToFraction)
                    }
                }
            }
        }
    }
}

val evaluatePowerOfFraction = plan {
    val f = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
    val exponent = UnsignedIntegerPattern()
    pattern = powerOf(bracketOf(f), exponent)

    explanation(Explanation.EvaluatePowerOfFraction, move(f), move(exponent))

    pipeline {
        step(distributeFractionPositivePower)
        step(simplifyArithmeticExpression)
    }
}
