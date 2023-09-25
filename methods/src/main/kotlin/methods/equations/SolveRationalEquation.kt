
import engine.context.Context
import engine.context.Curriculum
import engine.expressions.Constants
import engine.expressions.Contradiction
import engine.expressions.DivideBy
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.Fraction
import engine.expressions.Sum
import engine.expressions.equationOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.condition
import engine.steps.metadata.metadata
import methods.algebra.AlgebraExplanation
import methods.algebra.AlgebraPlans
import methods.algebra.algebraicSimplificationSteps
import methods.algebra.findDenominatorsAndDivisors
import methods.equations.EquationsPlans
import methods.equations.EquationsRules
import methods.equations.Explanation
import methods.equations.checkSolutionsAgainstConstraint
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.fractionarithmetic.FractionArithmeticRules
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.rationalexpressions.RationalExpressionsPlans
import methods.rationalexpressions.computeLcdAndMultipliers
import methods.rationalexpressions.factorFractionDenominatorTask

private val multiplyAndSimplifyRationalFractions = plan {
    explanation = Explanation.MultiplyAndSimplifyRationalFractions

    steps {
        optionally(FractionArithmeticRules.TurnProductOfFractionAndNonFractionFactorIntoFraction)
        optionally {
            deeply(FractionArithmeticPlans.SimplifyFraction)
        }
        whilePossible {
            deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct)
        }
    }
}

/**
 * An equation of the form: [p(x) / r(x)] = [q(x) / r(x)]
 * is called like rational equation (custom term)
 */
private val simplifyLikeRationalEquationToPolynomialEquation = taskSet {
    explanation = Explanation.SimplifyToPolynomialEquation

    tasks {
        val multiplyBothSidesByDenominatorOfLikeRationalEquation = task(
            startExpr = expression,
            explanation = metadata(Explanation.MultiplyBothSidesByDenominator),
            stepsProducer = EquationsRules.MultiplyBothSidesOfLikeRationalEquation,
        ) ?: return@tasks null

        task(
            startExpr = multiplyBothSidesByDenominatorOfLikeRationalEquation.result,
            explanation = metadata(Explanation.SimplifyRationalExpression),
            stepsProducer = steps {
                whilePossible {
                    deeply(multiplyAndSimplifyRationalFractions)
                }
            },
        ) ?: return@tasks null

        allTasks()
    }
}

private fun multiplyBothSidesAndSimplifyEquation(multiplier: Expression) = taskSet {
    explanation = Explanation.MultiplyBothSidesAndSimplifyEquation

    tasks {
        fun multiplyEachSumTermWithMultiplier(expr: Expression, multiplier: Expression): List<Expression> {
            return if (expr is Sum) {
                expr.children.map { productOf(it, multiplier) }
            } else {
                listOf(productOf(expr, multiplier))
            }
        }

        val lhsTermsProductWithMultiplier = multiplyEachSumTermWithMultiplier(expression.firstChild, multiplier)
        val rhsTermsProductWithMultiplier = multiplyEachSumTermWithMultiplier(expression.secondChild, multiplier)

        val computeProductOfRationalTermsWithMultiplierLhs = lhsTermsProductWithMultiplier.map { productTerm ->
            task(
                startExpr = productTerm,
                explanation = metadata(Explanation.SimplifyRationalExpression),
                stepsProducer = steps {
                    deeply(multiplyAndSimplifyRationalFractions)
                },
            )?.result ?: productTerm
        }

        val computeProductOfRationalTermsWithMultiplierRhs = rhsTermsProductWithMultiplier.map { productTerm ->
            task(
                startExpr = productTerm,
                explanation = metadata(Explanation.SimplifyRationalExpression),
                stepsProducer = steps {
                    deeply(multiplyAndSimplifyRationalFractions)
                },
            )?.result ?: productTerm
        }

        task(
            startExpr = equationOf(
                sumOf(computeProductOfRationalTermsWithMultiplierLhs),
                sumOf(computeProductOfRationalTermsWithMultiplierRhs),
            ),
            explanation = metadata(Explanation.SimplifiedPolynomialEquation),
        )

        allTasks()
    }
}

private val simplifyToPolynomialEquation = taskSet {
    explanation = Explanation.SimplifyToPolynomialEquation

    tasks {
        val simplifiedRationalEquation = task(
            startExpr = expression,
            explanation = metadata(Explanation.SimplifyEquation),
            stepsProducer = algebraicSimplificationSteps(addRationalExpressions = false),
        )?.result ?: expression

        val denominatorsAndDivisors = findDenominatorsAndDivisors(simplifiedRationalEquation)
            .map { it.first }.distinct().toList()

        if (denominatorsAndDivisors.isEmpty()) {
            return@tasks null
        } else if (denominatorsAndDivisors.size == 1) {
            val uniqueDenominator = denominatorsAndDivisors.single()
            task(
                startExpr = simplifiedRationalEquation,
                explanation = metadata(Explanation.MultiplyBothSidesAndSimplifyEquation, uniqueDenominator),
                stepsProducer = multiplyBothSidesAndSimplifyEquation(uniqueDenominator),
            )
        } else {
            // factorize the denominators
            val factoredDenominatorOfFractions = task(
                startExpr = simplifiedRationalEquation,
                explanation = metadata(Explanation.FactorDenominatorOfFraction),
            ) {
                whilePossible {
                    deeply(RationalExpressionsPlans.FactorDenominatorOfFraction)
                }
            }?.result ?: simplifiedRationalEquation

            // we shouldn't filter terms without solutionVariable in them,
            // for e.g. [12 / [x^2] - 9] + [1/3] = 8x --> LCD([x^2] - 9, 3)  (i.e. "3" should be included)
            val denominatorsAndDivisorsOfFactoredDenominators = findDenominatorsAndDivisors(
                factoredDenominatorOfFractions,
            ).toList()

            val factoredFractions = denominatorsAndDivisorsOfFactoredDenominators.map {
                factorFractionDenominatorTask(it.second as Fraction)
            }

            val (lcd, _) = computeLcdAndMultipliers(factoredFractions)
            task(
                startExpr = lcd,
                explanation = metadata(Explanation.ComputeLeastCommonDenominatorOfFractions),
            )

            task(
                startExpr = factoredDenominatorOfFractions,
                explanation = metadata(Explanation.MultiplyBothSidesAndSimplifyEquation, lcd),
                stepsProducer = multiplyBothSidesAndSimplifyEquation(lcd),
            )
        }

        allTasks()
    }
}

private fun Expression.containsVariableDenominator(context: Context): Boolean {
    val variableDenominator = when (this) {
        is Fraction -> !denominator.isConstantIn(context.solutionVariables)
        is DivideBy -> !divisor.isConstantIn(context.solutionVariables)
        else -> false
    }
    return variableDenominator || children.any { it.containsVariableDenominator(context) }
}

internal val solveRationalEquation = taskSet {
    explanation = Explanation.SolveEquation
    pattern = condition { it is Equation && it.containsVariableDenominator(this) }

    tasks {
        val constraint = when (context.curriculum) {
            Curriculum.US -> expression
            else -> task(
                startExpr = expression,
                explanation = metadata(AlgebraExplanation.ComputeDomainOfAlgebraicExpression),
                stepsProducer = AlgebraPlans.ComputeDomainOfAlgebraicExpression,
            )?.result ?: return@tasks null
        }

        val simplifyToPolynomialExpression = task(
            startExpr = expression,
            explanation = metadata(Explanation.SimplifyToPolynomialEquation),
            stepsProducer = steps {
                firstOf {
                    // [p(x) / r(x)] = [q(x) / r(x)] -> p(x) = r(x)
                    option(simplifyLikeRationalEquationToPolynomialEquation)
                    option(simplifyToPolynomialEquation)
                }
            },
        )!!.result

        val solvePolynomialEquation = task(
            startExpr = simplifyToPolynomialExpression,
            explanation = metadata(Explanation.SolveEquation),
            stepsProducer = EquationsPlans.SolveEquation,
        ) ?: return@tasks null

        val solution = solvePolynomialEquation.result

        // no need to check if the constraint(s) is/are satisfied if solution
        // is an empty set
        if (solution !is Contradiction || solution == Constants.EmptySet) {
            checkSolutionsAgainstConstraint(solution, constraint) ?: return@tasks null
        }

        allTasks()
    }
}
