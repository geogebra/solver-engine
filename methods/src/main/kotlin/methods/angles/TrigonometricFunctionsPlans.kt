/*
 * Copyright (c) 2025 GeoGebra GmbH, office@geogebra.org
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

package methods.angles

import engine.expressions.Constants.Pi
import engine.expressions.Constants.Two
import engine.expressions.Expression
import engine.expressions.Label
import engine.expressions.Minus
import engine.expressions.TrigonometricExpression
import engine.expressions.Variable
import engine.expressions.fractionOf
import engine.expressions.inequationOf
import engine.expressions.productOf
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.operators.TrigonometricFunctionType
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.FixedPattern
import engine.patterns.TrigonometricExpressionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeSumContaining
import engine.patterns.degreeOf
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optional
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumOf
import engine.steps.metadata.metadata
import methods.algebra.AlgebraExplanation
import methods.factor.FactorRules
import methods.general.GeneralRules
import methods.inequations.InequationsPlans
import methods.solvable.computeOverallIntersectionSolution
import methods.solvable.findUnusedVariableLetter

enum class TrigonometricFunctionsPlans(override val runner: CompositeMethod) : RunnerMethod {
    ComputeDomainOfTrigonometricExpression(
        taskSet {
            explanation = Explanation.ComputeDomainOfTrigonometricExpression

            val solveInequationInOneVariableSteps = steps {
                inContext({ copy(solutionVariables = it.firstChild.variables.toList()) }) {
                    apply(InequationsPlans.SolveInequation)
                }
            }

            tasks {
                val variable = Variable(findUnusedVariableLetter(expression))

                val rhs = engine.expressions.sumOf(
                    fractionOf(Pi, Two),
                    productOf(variable, Pi),
                )

                val functions = findFunctionsRequiringDomainCheck(expression).filter {
                    !it.isConstant() && it.firstChild !is Variable
                }.distinct().toList()

                if (functions.isEmpty()) {
                    return@tasks null
                }

                val constraints = functions.map {
                    taskWithOptionalSteps(
                        startExpr = inequationOf(
                            it.firstChild,
                            rhs,
                        ),
                        explanation = metadata(Explanation.ExpressionMustNotBeUndefined),
                        stepsProducer = solveInequationInOneVariableSteps,
                    )
                }

                val overallSolution = computeOverallIntersectionSolution(
                    constraints.map { it.result },
                )

                task(
                    startExpr = overallSolution,
                    explanation = metadata(AlgebraExplanation.CollectDomainRestrictions),
                )

                allTasks()
            }
        },
    ),

    ReduceDoubleAngleInSum(
        plan {
            val constant1 = optional(UnsignedIntegerPattern(), ::degreeOf)
            val constant2 = optional(UnsignedIntegerPattern(), ::degreeOf)

            val variable1 = ArbitraryVariablePattern()
            val variable2 = ArbitraryVariablePattern()

            val variableArgument1 = engine.patterns.productOf(constant1, variable1)
            val variableArgument2 = engine.patterns.productOf(constant2, variable2)

            val trigFunction1 =
                optionalPatternContaining(
                    TrigonometricExpressionPattern(
                        oneOf(
                            constant1,
                            variableArgument1,
                            variable1,
                        ),
                    ),
                )

            // We don't include the variable pattern as it is not possible for both trigonometric functions to have
            // only a variable as an argument and one to be double the other.
            val trigFunction2 = optionalPatternContaining(
                TrigonometricExpressionPattern(
                    oneOf(
                        constant2,
                        variableArgument2,
                    ),
                ),
            )

            pattern = commutativeSumContaining(trigFunction1, trigFunction2)

            explanation = Explanation.SimplifySumContainingDoubleAngles

            partialExpressionSteps {
                withNewLabels {
                    apply(TrigonometricFunctionsRules.AddLabelToSumContainingDoubleAngle)
                    applyTo(Label.A) {
                        applyTo(extractor = {
                            if (it is Minus) {
                                it.firstChild
                            } else {
                                it
                            }
                        }) {
                            firstOf {
                                option {
                                    apply(doubleAngleSolvingSteps)
                                }
                                option {
                                    apply(AnglesRules.DeriveTrigonometricFunctionFromPrimitiveFunctions)
                                    applyToChildren(
                                        stepsProducer = doubleAngleSolvingSteps,
                                        atLeastOne = true,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    ),
}

private val doubleAngleSolvingSteps = steps {
    optionally {
        applyTo(TrigonometricFunctionsRules.ExpressAs2xArgument) {
            it.firstChild
        }
    }
    apply(TrigonometricFunctionsRules.ApplyDoubleAngleIdentity)
}

/**
 * Try to apply the pythagorean identity to an expressions, in this case we should try factoring as well in case
 * both terms are multiplied by the same coefficient.
 * - we try to apply the pythagorean identity
 * ┌ IF it can be applied, we return the result
 * │    - [sin^2]\[x\] + [cos^2]\[x\] --> 1
 * └ ELSE we try to factor the expression and apply the identity to the result
 *      - k [sin ^ 2]\[x\] + k [cos ^ 2]\[x\] --> k (sin^2)\[x\] + k (cos^2)\[x\] --> k (1) --> k
 *      OR
 *      - -[sin^2][ x ] - [cos^2] [ x ] --> - ( [sin ^ 2][ x ] + [cos ^ 2] [ x ]) --> -( 1 ) --> -1
 */
fun createUsePythagoreanIdentityAndSimplifyPlan(simplificationSteps: StepsProducer): StepsProducer {
    return plan {
        val argument = AnyPattern()
        val exponent = FixedPattern(Two)
        val sine =
            powerOf(TrigonometricExpressionPattern(argument, listOf(TrigonometricFunctionType.Sin)), exponent)
        val cosine =
            powerOf(TrigonometricExpressionPattern(argument, listOf(TrigonometricFunctionType.Cos)), exponent)

        pattern = oneOf(
            commutativeSumContaining(sine, cosine),
            commutativeSumContaining(productContaining(sine), productContaining(cosine)),
            commutativeSumContaining(negOf(sine), negOf(cosine)),
        )

        explanation = Explanation.ApplyPythagoreanIdentityAndSimplify

        steps {
            shortcut(TrigonometricFunctionsRules.ApplyPythagoreanIdentity)

            firstOf {
                option(FactorRules.FactorCommonFactor)
                option(GeneralRules.FactorMinusFromSumWithAllNegativeTerms)
            }

            applyTo(TrigonometricFunctionsRules.ApplyPythagoreanIdentity) {
                if (it is Minus) {
                    it.firstChild
                } else {
                    it.secondChild
                }
            }

            whilePossible(simplificationSteps)
        }
    }
}

/**
 * - Apply trigonometric identity
 * - Simplify resulting expression using the provided simplification rules
 */
fun createUseTrigonometricIdentityAndSimplifyPlan(simplificationSteps: StepsProducer): StepsProducer {
    return plan {
        pattern = TrigonometricExpressionPattern(
            sumOf(AnyPattern(), AnyPattern()),
            listOf(
                TrigonometricFunctionType.Sin,
                TrigonometricFunctionType.Cos,
                TrigonometricFunctionType.Tan,
            ),
        )

        explanation = Explanation.ApplyTrigonometricIdentityAndSimplify

        steps {
            optionally(TrigonometricFunctionsRules.RearrangeAddendsInArgument)
            firstOf {
                option(TrigonometricFunctionsRules.ApplyCosineSumIdentity)
                option(TrigonometricFunctionsRules.ApplySineSumIdentity)
                option(TrigonometricFunctionsRules.ApplyTangentSumIdentity)
            }
            whilePossible(simplificationSteps)
        }
    }
}

/**
 * - Evaluate inverse trigonometric function exactly
 * - OPTIONALLY: Simplify the result (in case of negative values, the result may be in the form /pi/ - x)
 */
fun createEvaluateInverseTrigonometricFunctionExactlyPlan(simplificationSteps: StepsProducer): StepsProducer {
    return plan {
        pattern = TrigonometricExpressionPattern(
            AnyPattern(),
            listOf(
                TrigonometricFunctionType.Arcsin,
                TrigonometricFunctionType.Arccos,
                TrigonometricFunctionType.Arctan,
                TrigonometricFunctionType.Arccot,
            ),
        )

        explanation = Explanation.DetermineMainAnglePrincipalValueOfInverseFunction

        steps {
            apply(AnglesRules.EvaluateInverseFunctionOfMainAngle)
            optionally(simplificationSteps)
        }
    }
}

fun findFunctionsRequiringDomainCheck(expr: Expression): Sequence<Expression> =
    sequence {
        for (child in expr.children) {
            yieldAll(findFunctionsRequiringDomainCheck(child))
        }

        when (expr) {
            is TrigonometricExpression ->
                if (expr.functionType == TrigonometricFunctionType.Tan &&
                    !expr.argument.isConstant()
                ) {
                    yield(expr)
                }
        }
    }
