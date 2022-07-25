package methods.fractionarithmetic

import engine.expressionmakers.move
import engine.methods.plan
import engine.patterns.AnyPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.fractionOf
import engine.patterns.optionalNegOf
import engine.patterns.sumContaining
import engine.steps.metadata.Skill
import methods.general.cancelCommonTerms
import methods.general.normalizeNegativeSigns
import methods.integerarithmetic.evaluateIntegerProductAndDivision
import methods.integerarithmetic.evaluateSignedIntegerAddition
import methods.integerarithmetic.evaluateSignedIntegerPower
import methods.integerarithmetic.simplifyEvenPowerOfNegative
import methods.integerarithmetic.simplifyIntegersInProduct
import methods.integerarithmetic.simplifyOddPowerOfNegative

val normalizeSignsInFraction = plan {
    explanation(Explanation.NormalizeSignsInFraction)

    whilePossible {
        firstOf {
            option { deeply(simplifyNegativeNumeratorAndDenominator) }
            option { deeply(simplifyNegativeInNumerator) }
            option { deeply(simplifyNegativeInDenominator) }
            option { deeply(normalizeNegativeSigns) }
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

val simplifyFraction = plan {
    val f = fractionOf(AnyPattern(), AnyPattern())
    pattern = f

    explanation(Explanation.SimplifyFraction, move(f))

    skill(Skill.SimplifyNumericFraction, move(f))

    whilePossible {
        firstOf {
            option(simplifyFractionToInteger)
            option(cancelCommonTerms)
            option(findCommonFactorInFraction)
        }
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
        optionalStep {
            explanation(Explanation.EvaluateProductsInNumeratorAndDenominator)
            whilePossible {
                deeply(evaluateIntegerProductAndDivision)
            }
        }
        step(addLikeFractions)
        step {
            explanation(Explanation.EvaluateSumInNumerator)
            deeply(evaluateSignedIntegerAddition)
        }
        optionalStep(normalizeSignsInFraction)
        optionalStep {
            deeply(simplifyFraction)
        }
    }
}

val evaluateSumOfFractionAndInteger = plan {
    pipeline {
        step(turnSumOfFractionAndIntegerToFractionSum)
        step {
            deeply(simplifyIntegersInProduct)
        }
        step(evaluateFractionSum)
    }
}

val multiplyAndSimplifyFractions = plan {
    pipeline {
        optionalStep {
            whilePossible(turnProductOfFractionByIntegerToFractionProduct)
        }
        step {
            whilePossible(multiplyFractions)
        }
        optionalStep(simplifyFraction)
        optionalStep {
            whilePossible {
                deeply(simplifyIntegersInProduct)
            }
        }
    }
}

val evaluateIntegerToNegativePower = plan {
    firstOf {
        option(turnIntegerToMinusOneToFraction)
        option {
            pipeline {
                step(turnNegativePowerOfIntegerToFraction)
                step {
                    whilePossible {
                        deeply(evaluateSignedIntegerPower)
                    }
                }
            }
        }
    }
}

val evaluateFractionPower = plan {
    pipeline {
        optionalStep(simplifyEvenPowerOfNegative)
        optionalStep(simplifyOddPowerOfNegative)
        optionalStep(simplifyFractionNegativePower)
        step {
            // there might be a minus before the fraction, needs deeply
            deeply(distributeFractionPositivePower)
        }
        step {
            whilePossible {
                deeply(evaluateSignedIntegerPower)
            }
        }
    }
}
