package methods.fractionarithmetic

import engine.context.BooleanSetting
import engine.context.Setting
import engine.expressions.Fraction
import engine.expressions.Minus
import engine.expressions.Power
import engine.expressions.Product
import engine.expressions.asRational
import engine.expressions.isPolynomial
import engine.expressions.isSigned
import engine.methods.CompositeMethod
import engine.methods.Method
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.firstOf
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.ConstantPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.VariableExpressionPattern
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
import methods.general.GeneralPlans
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
                // Cancel obvious common factors, and if the numerator and denominator have an integer factor,
                // simplify that.
                whilePossible {
                    check { it is Fraction }
                    firstOf {
                        option(trivialFractionCancellationSteps)
                        option(FractionArithmeticRules.SimplifyFractionToInteger)
                        // simplification of something like: [(1 - sqrt[2])x + 1 / (-1 + sqrt[2])x - 1]
                        // doesn't happen because "simplify" doesn't expand (1 - sqrt[2])x
                        // and extracting minus from it is tricky, hence "expand" is where
                        // the above should get simplified
                        option(extractNegativeFromNumeratorOrDenominatorAndCancelFactor)
                        option(FractionArithmeticRules.ReorganizeCommonSumFactorInFraction)
                        option(FractionArithmeticRules.FindCommonIntegerFactorInFraction)
                    }
                }
            }
        },
    ),

    SimplifyCommonIntegerFactorInFraction(
        plan {
            val f = fractionOf(AnyPattern(), AnyPattern())
            pattern = f

            explanation = Explanation.SimplifyFraction
            explanationParameters(f)

            steps {
                apply(FractionArithmeticRules.FactorGreatestCommonIntegerFactorInFraction)
                apply(trivialFractionCancellationSteps)
            }
        },
    ),

    MultiplyAndSimplifyFractions(
        plan {
            pattern = condition { it is Product && it.isPolynomial() }
            explanation = Explanation.MultiplyAndSimplifyFractions

            steps {
                branchOn(Setting.MultiplyFractionsAndNotFractionsDirectly) {
                    case(BooleanSetting.False) {
                        // Doesn't turn all factors into fractions, e.g. it won't turn variable expressions into
                        // fractions
                        whilePossible(FractionArithmeticRules.TurnFactorIntoFractionInProduct)
                        apply {
                            whilePossible(FractionArithmeticRules.MultiplyFractionAndFractionable)
                        }
                    }
                    case(BooleanSetting.True) {
                        whilePossible(FractionArithmeticRules.MultiplyFractionAndFractionable)
                    }
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
    firstOf {
        option {
            check { isSet(Setting.QuickAddLikeFraction) }
            apply(FractionArithmeticRules.AddAndSimplifyLikeFractions)
        }

        option {
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
        }
    }
    optionally(FractionArithmeticPlans.SimplifyFraction)
}

fun createAddFractionsPlan(numeratorSimplificationSteps: StepsProducer): Method {
    val addFractionSteps = createAddFractionsSteps(numeratorSimplificationSteps)

    return plan {
        val f1 = optionalNegOf(fractionOf(AnyPattern(), ConstantPattern()))
        val f2 = optionalNegOf(fractionOf(AnyPattern(), ConstantPattern()))

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

/**
 * Only add a term to a fraction if that fraction already contains a similar term in
 * its numerator (i.e. don't add x and [1 / 2] but do add x and [1 + x / 2])
 */
fun createAddTermAndFractionPlan(numeratorSimplificationSteps: StepsProducer): CompositeMethod {
    val addFractionSteps = createAddFractionsSteps(numeratorSimplificationSteps)

    return plan {
        explanation = Explanation.AddTermAndFraction

        val variable = VariableExpressionPattern()
        val rootWithCoefficient = withOptionalIntegerCoefficient(variable)
        val numerator = oneOf(
            withOptionalIntegerCoefficient(variable),
            sumContaining(withOptionalIntegerCoefficient(variable)),
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
    check { it.isSigned<Fraction>() }

    optionally {
        applyTo(GeneralRules.FactorMinusFromSumWithAllNegativeTerms) {
            when {
                it is Minus && it.argument is Fraction -> (it.argument as Fraction).numerator
                it is Fraction -> it.numerator
                else -> null
            }
        }
    }
    optionally {
        applyTo(GeneralRules.FactorMinusFromSumWithAllNegativeTerms) {
            when {
                it is Minus && it.argument is Fraction -> (it.argument as Fraction).denominator
                it is Fraction -> it.denominator
                else -> null
            }
        }
    }

    firstOf {
        option(FractionArithmeticRules.SimplifyNegativeNumeratorAndDenominator)
        option(FractionArithmeticRules.SimplifyNegativeInNumerator)
        option(FractionArithmeticRules.SimplifyNegativeInDenominator)
    }
}

/**
 * Fraction cancellation steps that doesn't involve any calculation
 * - cancel common factor which is already factorized
 * - cancel 1
 */
private val trivialFractionCancellationSteps = firstOf {
    option(GeneralRules.SimplifyUnitFractionToOne)
    option(GeneralRules.SimplifyFractionWithOneDenominator)
    option(GeneralRules.CancelDenominator)
    option(FractionArithmeticRules.CancelCommonFactorInFraction)
}

private val extractMinusFromPower = steps {
    check { it is Power }
    applyToKind<Power>(GeneralRules.FactorMinusFromSum) { it.base }
    firstOf {
        option(GeneralRules.SimplifyEvenPowerOfNegative)
        option(GeneralRules.SimplifyOddPowerOfNegative)
    }
}

private val extractMinusFromProduct = steps {
    check { it is Product }
    applyToChildren {
        firstOf {
            option(GeneralRules.FactorMinusFromSum)
            option(extractMinusFromPower)
        }
    }
    optionally(GeneralPlans.NormalizeNegativeSignsInProduct)
}

/**
 * extract -ve sign from a product or sum or power expression, whether
 * to extract the -ve sign or not is done on the basis of "pseudo-degree"
 * assigned to term with highest "pseudo-degree" in Sum; the term with
 * highest "pseudo-degree" is preferred to have no -ve sign next to it
 *
 * e.g.; 1 - x + [x^[5/3]] - [x^3] -> -(-1 + x + [x^3])
 * since the term (i.e. [x^3]) with highest "pseudo-degree" has -ve
 * next to it
 */
val extractMinusFromProductOrSumOrPower = steps {
    firstOf {
        option(GeneralRules.FactorMinusFromSum)
        option(extractMinusFromProduct)
        option(extractMinusFromPower)
    }
}

/**
 * extract -ve from numerator (or one of its multiples) or denominator (or one of its multiples)
 * and cancel the common factor in the fraction
 */
private val extractNegativeFromNumeratorOrDenominatorAndCancelFactor = steps {
    firstOf {
        option {
            applyToKind<Fraction>(extractMinusFromProductOrSumOrPower) { it.numerator }
            optionally(FractionArithmeticRules.ReorganizeCommonSumFactorInFraction)
            apply(FractionArithmeticRules.CancelCommonFactorInFraction)
        }
        option {
            applyToKind<Fraction>(extractMinusFromProductOrSumOrPower) { it.denominator }
            optionally(FractionArithmeticRules.ReorganizeCommonSumFactorInFraction)
            apply(FractionArithmeticRules.CancelCommonFactorInFraction)
        }
    }
}
