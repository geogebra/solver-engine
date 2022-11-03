package methods.fractionarithmetic

import engine.expressionmakers.move
import engine.expressions.base
import engine.expressions.denominator
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.operators.BinaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.fractionOf
import engine.patterns.optionalNegOf
import engine.patterns.sumContaining
import engine.steps.metadata.Skill
import methods.general.GeneralRules
import methods.general.normalizeNegativeSigns
import methods.general.rewriteDivisionsAsFractions
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerarithmetic.evaluateSignedIntegerPower
import methods.integerarithmetic.simplifyIntegersInExpression
import methods.integerarithmetic.simplifyIntegersInProduct

val normalizeSignsInFraction = plan {
    explanation(Explanation.NormalizeSignsInFraction)

    steps {
        whilePossible {
            firstOf {
                option {
                    deeply {
                        applyTo(GeneralRules.FactorMinusFromSum) {
                            if (it.parent?.expr?.operator == BinaryExpressionOperator.Fraction) it else null
                        }
                    }
                }
                option { deeply(FractionArithmeticRules.SimplifyNegativeNumeratorAndDenominator) }
                option { deeply(FractionArithmeticRules.SimplifyNegativeInNumerator) }
                option { deeply(FractionArithmeticRules.SimplifyNegativeInDenominator) }
                option { deeply(normalizeNegativeSigns) }
            }
        }
    }
}

val normalizeFractions = plan {
    // Normalize fractions within fractions
    explanation(Explanation.NormalizeFractionsAndDivisions)

    steps {
        whilePossible {
            deeply {
                firstOf {
                    option(rewriteDivisionsAsFractions)
                    option(FractionArithmeticRules.SimplifyFractionWithFractionDenominator)
                    option(FractionArithmeticRules.SimplifyFractionWithFractionNumerator)
                }
            }
        }
    }
}

val simplifyFraction = plan {
    val f = fractionOf(AnyPattern(), AnyPattern())
    pattern = f

    explanation(Explanation.SimplifyFraction, move(f))

    skill(Skill.SimplifyNumericFraction, move(f))

    steps {
        whilePossible {
            firstOf {
                option(GeneralRules.SimplifyUnitFractionToOne)
                option(GeneralRules.SimplifyFractionWithOneDenominator)
                option(FractionArithmeticRules.SimplifyFractionToInteger)
                option(GeneralRules.CancelCommonTerms)
                option(GeneralRules.CancelDenominator)
                option(FractionArithmeticRules.FindCommonFactorInFraction)
            }
        }
    }
}

val evaluateFractionSum = plan {
    val f1 = optionalNegOf(fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern()))
    val f2 = optionalNegOf(fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern()))

    pattern = sumContaining(f1, f2)

    explanation(Explanation.EvaluateFractionSum, move(f1), move(f2))

    skill(Skill.AddFractions, move(f1), move(f2))

    steps {
        optionally(FractionArithmeticRules.BringToCommonDenominator)
        optionally {
            plan {
                explanation(Explanation.EvaluateProductsInNumeratorAndDenominator)

                steps {
                    whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision) }
                }
            }
        }
        apply(FractionArithmeticRules.AddLikeFractions)
        plan {
            explanation(Explanation.EvaluateSumInNumerator)

            steps {
                deeply(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
            }
        }
        optionally(normalizeSignsInFraction)
        optionally {
            deeply(simplifyFraction)
        }
    }
}

val evaluateSumOfFractionAndInteger = plan {
    explanation(Explanation.EvaluateSumOfFractionAndInteger)

    steps {
        apply(FractionArithmeticRules.TurnSumOfFractionAndIntegerToFractionSum)
        deeply(simplifyIntegersInProduct)
        apply(evaluateFractionSum)
    }
}

val multiplyAndSimplifyFractions = plan {
    explanation(Explanation.MultiplyAndSimplifyFractions)

    steps {
        whilePossible(FractionArithmeticRules.TurnFactorIntoFractionInProduct)
        whilePossible(FractionArithmeticRules.MultiplyFractions)
        optionally(simplifyFraction)
        whilePossible {
            deeply(simplifyIntegersInProduct)
        }
    }
}

val simplifyIntegerToNegativePower = steps {
    firstOf {
        option(FractionArithmeticRules.TurnIntegerToMinusOneToFraction)

        option {
            plan {
                explanation(Explanation.EvaluateIntegerToNegativePower)

                steps {
                    apply(FractionArithmeticRules.TurnNegativePowerOfIntegerToFraction)
                    applyTo(evaluateSignedIntegerPower) { it.denominator() }
                }
            }
        }

        option {
            plan {
                explanation(Explanation.EvaluateIntegerToNegativePower)

                steps {
                    // [0 ^ -n] -> [[1 / 0] ^ n]
                    apply(FractionArithmeticRules.TurnNegativePowerOfZeroToPowerOfFraction)
                    applyTo(GeneralRules.SimplifyZeroDenominatorFractionToUndefined) { it.base() }
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
