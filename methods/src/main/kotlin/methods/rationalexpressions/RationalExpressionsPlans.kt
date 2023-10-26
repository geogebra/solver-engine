package methods.rationalexpressions

import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.Fraction
import engine.expressions.IntegerExpression
import engine.expressions.Power
import engine.expressions.areEquivalentSums
import engine.expressions.asInteger
import engine.expressions.explicitProductOf
import engine.expressions.fractionOf
import engine.expressions.isRationalExpression
import engine.expressions.isSignedFraction
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
import engine.patterns.divideBy
import engine.patterns.fractionOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.metadata.metadata
import engine.utility.lcm
import methods.algebra.algebraicSimplificationSteps
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
                condition { !it.isSignedFraction() },
                condition { it.isRationalExpression() },
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
                optionally(FractionArithmeticRules.RewriteDivisionAsFraction)
                check { it is Fraction && !it.denominator.isConstant() }
                apply(rationalExpressionSimplificationSteps)
            }
        },
    ),

    MultiplyRationalExpressions(
        plan {
            explanation = Explanation.MultiplyRationalExpressions
            pattern = productContaining(
                condition { it is Fraction && !it.denominator.isConstant() },
                condition { it is Fraction && !it.denominator.isConstant() },
            )

            partialExpressionSteps {
                apply(FractionArithmeticRules.MultiplyFractions)
                optionally(rationalExpressionSimplificationSteps)
            }
        },
    ),

    MultiplyRationalExpressionWithNonFractionalFactors(
        plan {
            explanation = Explanation.MultiplyRationalExpressions
            pattern = productContaining(condition { it is Fraction && !it.denominator.isConstant() })

            steps {
                apply(FractionArithmeticRules.TurnProductOfFractionAndNonFractionFactorsIntoFraction)
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

    SimplifyDivisionOfPolynomial(
        plan {
            explanation = Explanation.SimplifyDivisionOfPolynomial
            pattern = productContaining(sumContaining(), divideBy(condition { it !is Fraction }))

            partialExpressionSteps {
                apply(RationalExpressionsRules.DistributeDivisionOverSum)
                applyToChildren(SimplifyRationalExpression)
            }
        },
    ),
}

private val factoredExpressionPattern = optionalNegOf(oneOf(productContaining(), powerOf(AnyPattern(), AnyPattern())))

private val rationalExpressionSimplificationSteps = steps {
    optionally(RationalExpressionsPlans.FactorNumeratorOfFraction)
    optionally(RationalExpressionsPlans.FactorDenominatorOfFraction)
    apply(FractionArithmeticPlans.SimplifyFraction)
    optionally(algebraicSimplificationSteps)
}

private val addRationalExpressions = taskSet {
    explanation = Explanation.AddRationalExpressions

    val f1 = FractionPattern()
    val f2 = FractionPattern()

    val nf1 = optionalNegOf(f1)
    val nf2 = optionalNegOf(f2)

    pattern = sumContaining(nf1, nf2)

    val addLikeFractionsAndSimplifySteps = steps {
        optionally { applyToKind<Fraction>(PolynomialsPlans.SimplifyPolynomialExpression) { it.numerator } }
        optionally(rationalExpressionSimplificationSteps)
    }

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

        val (lcm, multipliers) = computeLcdAndMultipliers(listOf(factoredFraction1, factoredFraction2))
        val (multiplier1, multiplier2) = multipliers

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
            stepsProducer = addLikeFractionsAndSimplifySteps,
            explanation = metadata(Explanation.AddLikeRationalExpressions),
        )

        allTasks()
    }
}

private val factoringTaskSteps = steps {
    applyToKind<Fraction>(FactorPlans.FactorPolynomialInOneVariable) { it.denominator }
}

private fun TasksBuilder.factorFractionDenominatorTask(fraction: Fraction): Fraction {
    val factoringTask = task(
        startExpr = fraction,
        stepsProducer = factoringTaskSteps,
        explanation = metadata(Explanation.FactorDenominatorOfFraction),
    )

    return factoringTask?.result as? Fraction ?: fraction
}

private val simplifyFractionSteps = steps {
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

private fun TasksBuilder.bringFractionToCommonDenominatorTask(fraction: Fraction, multiplier: Expression): Fraction {
    if (multiplier == Constants.One) return fraction

    val expandedFraction = fractionOf(
        explicitProductOf(fraction.numerator, multiplier),
        explicitProductOf(fraction.denominator, multiplier),
    )

    val simplificationTask = taskWithOptionalSteps(
        startExpr = expandedFraction,
        stepsProducer = simplifyFractionSteps,
        explanation = metadata(Explanation.BringFractionToLeastCommonDenominator, fraction),
    )

    return simplificationTask.result as Fraction
}

fun computeLcdAndMultipliers(factoredFractions: List<Fraction>): Pair<Expression, List<Expression>> {
    // Compute the LCM of the denominators
    val factorsList = factoredFractions.map { it.denominator.factors() }

    val multiplicitiesList = factorsList.map { factors -> factors.mapNotNull { expr -> extractMultiplicity(expr) } }

    val lcmMultiplicities = multiplicitiesList[0].toMutableList()

    for (i in 1 until multiplicitiesList.size) {
        val multiplicities = multiplicitiesList[i]
        for (factor in multiplicities) {
            val index = lcmMultiplicities.indexOfFirst { areEquivalentSums(it.base, factor.base) }
            if (index == -1) {
                lcmMultiplicities.add(factor)
            } else if (lcmMultiplicities[index].exponent < factor.exponent) {
                lcmMultiplicities[index] = factor
            }
        }
    }

    val integerList = factorsList.map {
            factors ->
        factors.singleOrNull { it is IntegerExpression }?.asInteger() ?: BigInteger.ONE
    }

    val integerLcm = integerList.lcm()

    val lcm = simplifiedProductOf(
        xp(integerLcm),
        productOf(lcmMultiplicities.map { (base, exponent) -> simplifiedPowerOf(base, xp(exponent)) }),
    )

    // Compute the multiplier as a product of the integer multiplier and the non-integer multiplier
    val integerMultipliers = integerList.map { xp(integerLcm / it) }
    val multipliers = multiplicitiesList.indices.map { i ->
        simplifiedProductOf(
            integerMultipliers[i],
            computeMultiplierToLCM(multiplicitiesList[i], lcmMultiplicities),
        )
    }

    return Pair(lcm, multipliers)
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
