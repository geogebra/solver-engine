/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package methods.fallback

import engine.expressions.Constants
import engine.expressions.Identity
import engine.expressions.VoidExpression
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.taskSet
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.QuadraticPolynomialPattern
import engine.steps.metadata.metadata
import methods.approximation.ApproximationPlans
import methods.constantexpressions.ConstantExpressionsPlans
import methods.equations.EquationsPlans
import methods.factor.FactorPlans
import methods.inequalities.InequalitiesPlans
import methods.inequalities.solveConstantInequalitySteps
import methods.inequations.InequationsPlans
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.simplify.SimplifyPlans

enum class FallbackPlans(override val runner: CompositeMethod) : RunnerMethod {
    // Users shouldn't call this method
    @PublicMethod(hiddenFromList = true)
    QuadraticIsIrreducible(
        taskSet {
            specificPlans(
                SimplifyPlans.SimplifyAlgebraicExpression,
                FactorPlans.FactorPolynomialInOneVariable,
            )

            explanation = Explanation.QuadraticIsIrreducible
            val quadratic = QuadraticPolynomialPattern(ArbitraryVariablePattern())
            pattern = quadratic

            tasks {
                val a = get(quadratic::quadraticCoefficient)!!
                val b = get(quadratic::linearCoefficient)!!
                val c = get(quadratic::constantTerm)!!

                val discriminant = sumOf(
                    powerOf(b, Constants.Two),
                    negOf(
                        productOf(Constants.Four, a, c),
                    ),
                )

                val checkDiscriminantIsNegative = task(
                    context = context.copy(solutionVariables = emptyList()),
                    startExpr = engine.expressions.lessThanOf(discriminant, Constants.Zero),
                    explanation = metadata(Explanation.CheckDiscriminantIsNegative),
                    stepsProducer = solveConstantInequalitySteps,
                ) ?: return@tasks null

                if (checkDiscriminantIsNegative.result !is Identity) {
                    return@tasks null
                }

                task(
                    startExpr = VoidExpression(),
                    explanation = metadata(Explanation.QuadraticIsIrreducibleBecauseDiscriminantIsNegative),
                )

                allTasks()
            }
        },
    ),

    // Users shouldn't call this method
    @PublicMethod(hiddenFromList = true)
    ExpressionIsFullySimplified(
        plan {
            specificPlans(
                ConstantExpressionsPlans.SimplifyConstantExpression,
                IntegerArithmeticPlans.EvaluateArithmeticExpression,
                ApproximationPlans.EvaluateExpressionNumerically,
                SimplifyPlans.SimplifyAlgebraicExpression,
                FactorPlans.FactorPolynomialInOneVariable,
                EquationsPlans.SolveConstantEquation,
                EquationsPlans.SolveEquation,
                InequalitiesPlans.SolveConstantInequality,
                InequalitiesPlans.SolveLinearInequality,
                InequationsPlans.SolveConstantInequation,
                InequationsPlans.SolveInequation,
                QuadraticIsIrreducible,
            )

            explanation = Explanation.ExpressionIsFullySimplified

            steps {
                apply(FallbackRules.ExpressionIsFullySimplified)
            }
        },
    ),
}
