package methods.fractionarithmetic

import engine.expressionmakers.move
import engine.methods.plan
import engine.methods.steps
import engine.operators.BinaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.fractionOf
import engine.patterns.optionalNegOf
import engine.patterns.sumContaining
import engine.steps.metadata.Skill
import methods.general.cancelCommonTerms
import methods.general.cancelDenominator
import methods.general.factorMinusFromSum
import methods.general.normalizeNegativeSigns
import methods.general.rewriteDivisionsAsFractions
import methods.general.simplifyFractionWithOneDenominator
import methods.general.simplifyUnitFractionToOne
import methods.integerarithmetic.evaluateIntegerProductAndDivision
import methods.integerarithmetic.evaluateSignedIntegerAddition
import methods.integerarithmetic.evaluateSignedIntegerPower
import methods.integerarithmetic.simplifyIntegersInExpression
import methods.integerarithmetic.simplifyIntegersInProduct

val normalizeSignsInFraction = plan {
    explanation(Explanation.NormalizeSignsInFraction)

    whilePossible {
        firstOf {
            option {
                deeply {
                    applyTo(factorMinusFromSum) {
                        if (it.parent?.expr?.operator == BinaryExpressionOperator.Fraction) it else null
                    }
                }
            }
            option { deeply(simplifyNegativeNumeratorAndDenominator) }
            option { deeply(simplifyNegativeInNumerator) }
            option { deeply(simplifyNegativeInDenominator) }
            option { deeply(normalizeNegativeSigns) }
        }
    }
}

val normalizeFractions = plan {
    // Normalize fractions within fractions
    explanation(Explanation.NormalizeFractionsAndDivisions)

    whilePossible {
        deeply {
            firstOf {
                option(rewriteDivisionsAsFractions)
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
            option(simplifyUnitFractionToOne)
            option(simplifyFractionWithOneDenominator)
            option(simplifyFractionToInteger)
            option(cancelCommonTerms)
            option(cancelDenominator)
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
        optionalSteps(bringToCommonDenominator)
        optionalSteps {
            plan {
                explanation(Explanation.EvaluateProductsInNumeratorAndDenominator)
                whilePossible {
                    deeply(evaluateIntegerProductAndDivision)
                }
            }
        }
        steps(addLikeFractions)
        steps {
            plan {
                explanation(Explanation.EvaluateSumInNumerator)
                deeply(evaluateSignedIntegerAddition)
            }
        }
        optionalSteps(normalizeSignsInFraction)
        optionalSteps {
            deeply(simplifyFraction)
        }
    }
}

val evaluateSumOfFractionAndInteger = plan {
    explanation(Explanation.EvaluateSumOfFractionAndInteger)

    pipeline {
        steps(turnSumOfFractionAndIntegerToFractionSum)
        steps {
            deeply(simplifyIntegersInProduct)
        }
        steps(evaluateFractionSum)
    }
}

val multiplyAndSimplifyFractions = plan {
    explanation(Explanation.MultiplyAndSimplifyFractions)

    pipeline {
        optionalSteps {
            whilePossible(turnFactorIntoFractionInProduct)
        }
        steps {
            whilePossible(multiplyFractions)
        }
        optionalSteps(simplifyFraction)
        optionalSteps {
            whilePossible {
                deeply(simplifyIntegersInProduct)
            }
        }
    }
}

val evaluateIntegerToNegativePower = plan {
    explanation(Explanation.EvaluateIntegerToNegativePower)

    firstOf {
        option(turnIntegerToMinusOneToFraction)
        option {
            pipeline {
                steps(turnNegativePowerOfIntegerToFraction)
                steps {
                    whilePossible {
                        deeply(evaluateSignedIntegerPower)
                    }
                }
            }
        }
    }
}

// Auxiliary steps used in several plans
val simplifyFractionsInExpression = steps {
    whilePossible {
        firstOf {
            option { deeply(simplifyIntegersInExpression) }
            option { deeply(evaluateFractionSum) }
            option { deeply(evaluateSumOfFractionAndInteger) }
        }
    }
}
