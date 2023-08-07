package methods.rationalexpressions

import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.Fraction
import engine.expressions.IntegerExpression
import engine.expressions.Minus
import engine.expressions.Power
import engine.expressions.areEquivalentSums
import engine.expressions.asInteger
import engine.expressions.explicitProductOf
import engine.expressions.fractionOf
import engine.expressions.productOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.TasksBuilder
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.AnyPattern
import engine.patterns.FractionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.commutativeSumContaining
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.metadata.metadata
import engine.utility.lcm
import methods.factor.FactorPlans
import methods.factor.factorizationSteps
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.fractionarithmetic.FractionArithmeticRules
import methods.general.GeneralRules
import methods.polynomials.PolynomialsPlans
import java.math.BigInteger

enum class RationalExpressionsPlans(override val runner: CompositeMethod) : RunnerMethod {

    FactorNumeratorOfFraction(
        plan {
            explanation = Explanation.FactorNumeratorOfFraction
            pattern = FractionPattern()
            resultPattern = fractionOf(factoredExpressionPattern, AnyPattern())

            steps {
                applyToKind<Fraction>(factorizationSteps) { it.numerator }
            }
        },
    ),

    FactorDenominatorOfFraction(
        plan {
            explanation = Explanation.FactorDenominatorOfFraction
            pattern = FractionPattern()
            resultPattern = fractionOf(AnyPattern(), factoredExpressionPattern)

            steps {
                applyToKind<Fraction>(factorizationSteps) { it.denominator }
            }
        },
    ),

    AddLikeRationalExpressions(
        plan {
            explanation = Explanation.AddLikeRationalExpressions

            val denominator = condition { !it.isConstant() }
            val fraction1 = fractionOf(AnyPattern(), denominator)
            val fraction2 = fractionOf(AnyPattern(), denominator)
            pattern = sumContaining(optionalNegOf(fraction1), optionalNegOf(fraction2))

            partialExpressionSteps {
                apply(FractionArithmeticRules.AddLikeFractions)
                optionally(rationalExpressionSimplificationSteps)
            }
        },
    ),

    AddRationalExpressions(addRationalExpressions),

    AddTermAndRationalExpression(
        plan {
            explanation = Explanation.AddTermAndRationalExpression
            pattern = commutativeSumContaining(
                condition { it !is Fraction && !(it is Minus && it.argument is Fraction) },
                optionalNegOf(FractionPattern()),
            )

            partialExpressionSteps {
                apply(FractionArithmeticRules.BringToCommonDenominatorWithNonFractionalTerm)
                apply(FractionArithmeticRules.AddLikeFractions)
                optionally {
                    applyToKind<Fraction>(
                        PolynomialsPlans.ExpandPolynomialExpressionWithoutNormalization,
                    ) { it.numerator }
                }
                optionally(rationalExpressionSimplificationSteps)
            }
        },
    ),

    SimplifyRationalExpression(
        plan {
            explanation = Explanation.SimplifyRationalExpression

            steps {
                apply(rationalExpressionSimplificationSteps)
            }
        },
    ),

    MultiplyRationalExpressions(
        plan {
            explanation = Explanation.MultiplyRationalExpressions

            steps {
                apply(FractionArithmeticRules.MultiplyFractions)
                optionally(rationalExpressionSimplificationSteps)
            }
        },
    ),

    SimplifyPowerOfRationalExpression(
        plan {
            explanation = Explanation.SimplifyPowerOfRationalExpression
            pattern = powerOf(condition { it is Fraction && !it.isConstant() }, SignedIntegerPattern())

            steps {
                optionally(GeneralRules.FlipFractionUnderNegativePower)
                apply(FractionArithmeticRules.DistributePositiveIntegerPowerOverFraction)
                applyToChildren(PolynomialsPlans.SimplifyPolynomialExpression)
            }
        },
    ),
}

private val factoredExpressionPattern = optionalNegOf(oneOf(productContaining(), powerOf(AnyPattern(), AnyPattern())))

private val rationalExpressionSimplificationSteps = steps {
    optionally(RationalExpressionsPlans.FactorNumeratorOfFraction)
    optionally(RationalExpressionsPlans.FactorDenominatorOfFraction)
    apply(FractionArithmeticPlans.SimplifyFraction)
}

private val addRationalExpressions = taskSet {
    explanation = Explanation.AddRationalExpressions

    val f1 = FractionPattern()
    val f2 = FractionPattern()

    val nf1 = optionalNegOf(f1)
    val nf2 = optionalNegOf(f2)

    pattern = sumContaining(nf1, nf2)

    partialExpressionTasks {
        val fraction1 = get(f1) as Fraction
        val fraction2 = get(f2) as Fraction

        // We only use this plan if at least one of the terms is a rational expression
        // (the other one might be a constant fraction)
        if (fraction1.denominator.isConstant() && fraction2.denominator.isConstant()) {
            return@partialExpressionTasks null
        }

        // 1, 2. Factor the denominators of both fractions
        val factoredFraction1 = factorFractionDenominatorTask(fraction1)
        val factoredFraction2 = factorFractionDenominatorTask(fraction2)

        val (lcm, multiplier1, multiplier2) = computeLcdAndMultipliers(factoredFraction1, factoredFraction2)

        // 3. Display the LCD (no computations shown)
        task(
            startExpr = lcm,
            explanation = metadata(
                Explanation.ComputeLeastCommonDenominatorOfFractions,
                factoredFraction1,
                factoredFraction2,
            ),
        )

        // 4, 5. Multiply both fractions to bring them to the LCD
        val simplifiedFraction1 = bringFractionToCommonDenominatorTask(factoredFraction1, multiplier1)
        val simplifiedFraction2 = bringFractionToCommonDenominatorTask(factoredFraction2, multiplier2)

        // 6. Add the two like fractions and simplify
        taskWithOptionalSteps(
            startExpr = fractionOf(
                sumOf(copySign(nf1, simplifiedFraction1.numerator), copySign(nf2, simplifiedFraction2.numerator)),
                simplifiedFraction1.denominator,
            ),
            explanation = metadata(Explanation.AddLikeRationalExpressions),
        ) {
            optionally(PolynomialsPlans.SimplifyPolynomialExpression)
            optionally(rationalExpressionSimplificationSteps)
        }

        allTasks()
    }
}

private fun TasksBuilder.factorFractionDenominatorTask(fraction: Fraction): Fraction {
    val factoringTask = task(
        startExpr = fraction,
        explanation = metadata(Explanation.FactorDenominatorOfFraction),
    ) {
        applyToKind<Fraction>(FactorPlans.FactorPolynomialInOneVariable) { it.denominator }
    }

    return factoringTask?.result as? Fraction ?: fraction
}

private fun TasksBuilder.bringFractionToCommonDenominatorTask(fraction: Fraction, multiplier: Expression): Fraction {
    if (multiplier == Constants.One) return fraction

    val expandedFraction = fractionOf(
        explicitProductOf(fraction.numerator, multiplier),
        explicitProductOf(fraction.denominator, multiplier),
    )

    val simplificationTask = taskWithOptionalSteps(
        startExpr = expandedFraction,
        explanation = metadata(Explanation.BringFractionToLeastCommonDenominator, fraction),
    ) {
        optionally {
            applyToKind<Fraction>(
                PolynomialsPlans.ExpandPolynomialExpressionWithoutNormalization,
            ) { it.numerator }
        }
        optionally {
            applyToKind<Fraction>(
                PolynomialsPlans.SimplifyPolynomialExpression,
            ) { it.denominator }
        }
    }

    return simplificationTask.result as Fraction
}

private fun computeLcdAndMultipliers(factoredFraction1: Fraction, factoredFraction2: Fraction):
    Triple<Expression, Expression, Expression> {
    // Compute the LCM of the denominators
    val factors1 = factoredFraction1.denominator.factors()
    val factors2 = factoredFraction2.denominator.factors()

    // Compute the LCM of the non-integer factors
    val multiplicities1 = factors1.mapNotNull { extractMultiplicity(it) }
    val multiplicities2 = factors2.mapNotNull { extractMultiplicity(it) }

    val lcmMultiplicities = multiplicities1.toMutableList()
    for (factor in multiplicities2) {
        val index = lcmMultiplicities.indexOfFirst { areEquivalentSums(it.base, factor.base) }
        if (index == -1) {
            lcmMultiplicities.add(factor)
        } else if (lcmMultiplicities[index].exponent < factor.exponent) {
            lcmMultiplicities[index] = factor
        }
    }

    // Compute the LCM of the integer factors
    val integer1 = factors1.singleOrNull { it is IntegerExpression }?.asInteger() ?: BigInteger.ONE
    val integer2 = factors2.singleOrNull { it is IntegerExpression }?.asInteger() ?: BigInteger.ONE
    val integerLcm = integer1.lcm(integer2)

    val lcm = simplifiedProductOf(
        xp(integerLcm),
        productOf(lcmMultiplicities.map { (base, exponent) -> simplifiedPowerOf(base, xp(exponent)) }),
    )

    // Compute the multiplier as a product of the integer multiplier and the non-integer multiplier
    val integerMultiplier1 = xp(integerLcm / integer1)
    val integerMultiplier2 = xp(integerLcm / integer2)

    val multiplier1 = simplifiedProductOf(
        integerMultiplier1,
        computeMultiplierToLCM(multiplicities1, lcmMultiplicities),
    )

    val multiplier2 = simplifiedProductOf(
        integerMultiplier2,
        computeMultiplierToLCM(multiplicities2, lcmMultiplicities),
    )

    return Triple(lcm, multiplier1, multiplier2)
}

private data class FactorWithMultiplicity(val base: Expression, val exponent: BigInteger)

private fun extractMultiplicity(exp: Expression) = when {
    exp is IntegerExpression -> null
    exp is Power && exp.exponent is IntegerExpression ->
        FactorWithMultiplicity(exp.base, (exp.exponent as IntegerExpression).value)
    else -> FactorWithMultiplicity(exp, BigInteger.ONE)
}

/**
 * Compute the value x such that x * productOf([factors]) = productOf([lcmFactors])
 */
private fun computeMultiplierToLCM(
    factors: List<FactorWithMultiplicity>,
    lcmFactors: List<FactorWithMultiplicity>,
): Expression {
    return productOf(
        lcmFactors.mapNotNull { lcmFactor ->
            val exponent = factors.find { areEquivalentSums(it.base, lcmFactor.base) }?.exponent ?: BigInteger.ZERO
            if (lcmFactor.exponent > exponent) {
                simplifiedPowerOf(lcmFactor.base, xp(lcmFactor.exponent - exponent))
            } else {
                null
            }
        },
    )
}
