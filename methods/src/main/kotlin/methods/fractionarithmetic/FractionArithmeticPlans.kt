package methods.fractionarithmetic

import engine.expressions.Fraction
import engine.expressions.Power
import engine.expressions.Product
import engine.expressions.asRational
import engine.expressions.isPolynomial
import engine.methods.CompositeMethod
import engine.methods.Method
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeSumContaining
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.integerOrderRootOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalIntegerCoefficient
import engine.steps.metadata.Skill
import engine.utility.gcd
import methods.general.GeneralRules
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.integerarithmetic.IntegerArithmeticRules
import java.math.BigInteger

enum class FractionArithmeticPlans(override val runner: CompositeMethod) : RunnerMethod {

    RewriteDivisionsAsFractions(
        plan {
            explanation = Explanation.RewriteDivisionsAsFractionInExpression

            steps {
                whilePossible {
                    deeply {
                        firstOf {
                            option(FractionArithmeticRules.RewriteDivisionAsMultiplicationByReciprocal)
                            option(FractionArithmeticRules.RewriteDivisionAsFraction)
                        }
                    }
                }
            }
        },
    ),

    SimplifyFraction(
        plan {
            val f = fractionOf(AnyPattern(), AnyPattern())
            pattern = f

            explanation = Explanation.SimplifyFraction
            explanationParameters(f)

            skill(Skill.SimplifyNumericFraction, f)

            steps {
                whilePossible {
                    firstOf {
                        option(GeneralRules.SimplifyUnitFractionToOne)
                        option(GeneralRules.SimplifyFractionWithOneDenominator)
                        option(FractionArithmeticRules.SimplifyFractionToInteger)
                        option(GeneralRules.CancelDenominator)
                        option(FractionArithmeticRules.CancelCommonFactorInFraction)
                        option(FractionArithmeticRules.ReorganizeCommonSumFactorInFraction)
                        option(FractionArithmeticRules.FindCommonIntegerFactorInFraction)
                    }
                }
            }
        },
    ),

    MultiplyAndSimplifyFractions(
        plan {
            pattern = condition { it is Product && it.isPolynomial() }
            explanation = Explanation.MultiplyAndSimplifyFractions

            steps {
                whilePossible(FractionArithmeticRules.TurnFactorIntoFractionInProduct)
                apply {
                    whilePossible(FractionArithmeticRules.MultiplyFractions)
                }
                optionally(SimplifyFraction)
                whilePossible {
                    deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct)
                }
            }
        },
    ),

    /**
     * [2 ^ [11/3]] --> [2 ^ [3 2/3]] --> [2 ^ 3 + [2 / 3]]
     * --> [2 ^ 3] * [2 ^ [2 / 3]]
     */
    SplitRationalExponent(
        plan {
            pattern = powerOf(AnyPattern(), IntegerFractionPattern())
            explanation = Explanation.SplitRationalExponent

            steps {
                applyToKind<Power>(FractionArithmeticRules.ConvertImproperFractionToSumOfIntegerAndFraction) {
                    it.exponent
                }
                apply(GeneralRules.DistributeSumOfPowers)
            }
        },
    ),
}

private fun createAddFractionsSteps(numeratorSimplificationSteps: StepsProducer) = steps {
    optionally {
        plan {
            explanation = Explanation.EvaluateProductsInNumeratorAndDenominator
            steps {
                whilePossible {
                    deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision)
                }
            }
        }
    }
    optionally(FractionArithmeticRules.BringToCommonDenominator)
    optionally {
        plan {
            explanation = Explanation.EvaluateProductsInNumeratorAndDenominator

            steps {
                whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision) }
            }
        }
        check {
            val f11 = it.firstChild.asRational()
            val f22 = it.secondChild.asRational()
            f11 == null || f22 == null || gcd(
                f11.numerator,
                f11.denominator,
                f22.numerator,
                f22.denominator,
            ) == BigInteger.ONE
        }
    }
    apply(FractionArithmeticRules.AddLikeFractions)
    optionally {
        plan {
            explanation = Explanation.SimplifyNumerator

            steps {
                applyToKind<Fraction>(numeratorSimplificationSteps) { it.numerator }
            }
        }
    }
    optionally(normalizeNegativeSignsInFraction)
    optionally(FractionArithmeticPlans.SimplifyFraction)
}

fun createAddFractionsPlan(numeratorSimplificationSteps: StepsProducer): Method {
    val addFractionSteps = createAddFractionsSteps(numeratorSimplificationSteps)

    return plan {
        val f1 = optionalNegOf(fractionOf(AnyPattern(), AnyPattern()))
        val f2 = optionalNegOf(fractionOf(AnyPattern(), AnyPattern()))

        pattern = sumContaining(f1, f2)

        explanation = Explanation.AddFractions
        explanationParameters(f1, f2)

        skill(Skill.AddFractions, f1, f2)

        partialExpressionSteps {
            apply(addFractionSteps)
        }
    }
}

/**
 * Only add an integer to a fraction if that fraction already contains an integer
 * in its numerator (i.e. don't add 3 and [sqrt[2] / 2] but do add 3 and [1 + sqrt[2] / 2])
 */
fun createAddIntegerAndFractionPlan(numeratorSimplificationSteps: StepsProducer): CompositeMethod {
    val addFractionSteps = createAddFractionsSteps(numeratorSimplificationSteps)

    return plan {
        val integer = SignedIntegerPattern()
        val numerator = oneOf(SignedIntegerPattern(), sumContaining(SignedIntegerPattern()))
        val fraction = optionalNegOf(fractionOf(numerator, UnsignedIntegerPattern()))
        pattern = commutativeSumContaining(integer, fraction)

        explanation = Explanation.AddIntegerAndFraction
        explanationParameters(integer, fraction)

        partialExpressionSteps {
            apply(FractionArithmeticRules.BringToCommonDenominatorWithNonFractionalTerm)
            deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision)
            apply(addFractionSteps)
        }
    }
}

/**
 * Only add a root to a fraction if that fraction already contains the same root in
 * its numerator (i.e. don't add sqrt[2] and [1 / 2] but do add sqrt[2] and [1 + sqrt[2] / 2])
 */
fun createAddRootAndFractionPlan(numeratorSimplificationSteps: StepsProducer): CompositeMethod {
    val addFractionSteps = createAddFractionsSteps(numeratorSimplificationSteps)

    return plan {
        explanation = Explanation.AddRootAndFraction

        val root = integerOrderRootOf(AnyPattern())
        val rootWithCoefficient = withOptionalIntegerCoefficient(root)
        val numerator = oneOf(
            withOptionalIntegerCoefficient(root),
            sumContaining(withOptionalIntegerCoefficient(root)),
        )
        val fraction = optionalNegOf(fractionOf(numerator, UnsignedIntegerPattern()))
        pattern = commutativeSumContaining(rootWithCoefficient, fraction)

        partialExpressionSteps {
            apply(FractionArithmeticRules.BringToCommonDenominatorWithNonFractionalTerm)
            apply(addFractionSteps)
        }
    }
}

val addIntegerFractions = createAddFractionsPlan(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
val addIntegerAndFraction = createAddIntegerAndFractionPlan(IntegerArithmeticRules.EvaluateSignedIntegerAddition)

val normalizeFractionsWithinFractions = steps {
    firstOf {
        option(FractionArithmeticRules.SimplifyFractionWithFractionDenominator)
        option(FractionArithmeticRules.SimplifyFractionWithFractionNumerator)
    }
}

val normalizeNegativeSignsInFraction = steps {
    check { it is Fraction }

    optionally {
        applyToKind<Fraction>(GeneralRules.FactorMinusFromSum) { it.numerator }
    }
    optionally {
        applyToKind<Fraction>(GeneralRules.FactorMinusFromSum) { it.denominator }
    }

    firstOf {
        option(FractionArithmeticRules.SimplifyNegativeNumeratorAndDenominator)
        option(FractionArithmeticRules.SimplifyNegativeInNumerator)
        option(FractionArithmeticRules.SimplifyNegativeInDenominator)
    }
}

val simplifyIntegerToNegativePower = steps {
    firstOf {
        option(FractionArithmeticRules.TurnIntegerToMinusOneToFraction)

        option {
            plan {
                explanation = Explanation.EvaluateIntegerToNegativePower

                steps {
                    apply(FractionArithmeticRules.TurnNegativePowerOfIntegerToFraction)
                    applyToKind<Fraction>(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) { it.denominator }
                }
            }
        }

        option {
            plan {
                explanation = Explanation.EvaluateIntegerToNegativePower

                steps {
                    // [0 ^ -n] -> [[1 / 0] ^ n]
                    apply(FractionArithmeticRules.TurnNegativePowerOfZeroToPowerOfFraction)
                    applyToKind<Power>(GeneralRules.SimplifyZeroDenominatorFractionToUndefined) { it.base }
                }
            }
        }
    }
}
