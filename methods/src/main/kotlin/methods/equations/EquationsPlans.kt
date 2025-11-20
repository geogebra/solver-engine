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

package methods.equations

import engine.context.BooleanSetting
import engine.context.Context
import engine.context.Setting
import engine.expressions.Comparison
import engine.expressions.Constants
import engine.expressions.Contradiction
import engine.expressions.DecimalExpression
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.expressions.RecurringDecimalExpression
import engine.expressions.StatementSystem
import engine.expressions.StatementUnion
import engine.expressions.bracketOf
import engine.expressions.containsTrigExpression
import engine.expressions.equationOf
import engine.expressions.expressionWithConstraintOf
import engine.expressions.negOf
import engine.expressions.statementUnionOf
import engine.expressions.sumOf
import engine.methods.CompositeMethod
import engine.methods.Method
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.SolverEngineExplanation
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.AnyPattern
import engine.patterns.FindPattern
import engine.patterns.FixedPattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.SolvablePattern
import engine.patterns.condition
import engine.patterns.contradictionOf
import engine.patterns.equationOf
import engine.patterns.identityOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.setSolutionOf
import engine.patterns.solutionSetOf
import engine.patterns.variableListOf
import engine.steps.Transformation
import engine.steps.metadata.metadata
import methods.algebra.AlgebraExplanation
import methods.algebra.AlgebraPlans
import methods.algebra.findDenominatorsAndDivisors
import methods.constantexpressions.constantSimplificationSteps
import methods.constantexpressions.simpleTidyUpSteps
import methods.factor.FactorPlans
import methods.factor.FactorRules
import methods.general.NormalizationPlans
import methods.inequalities.InequalitiesPlans
import methods.inequations.InequationsPlans
import methods.polynomials.PolynomialsPlans
import methods.simplify.SimplifyPlans
import methods.simplify.algebraicSimplificationStepsForEquations
import methods.solvable.SolvablePlans
import methods.solvable.SolvableRules
import methods.solvable.computeOverallIntersectionSolution
import methods.solvable.computeOverallUnionSolution
import methods.solvable.evaluateBothSidesNumerically

enum class EquationsPlans(override val runner: CompositeMethod) : RunnerMethod {
    SimplifyByEliminatingConstantFactorOfLhsWithZeroRhs(
        plan {
            explanation = Explanation.SimplifyByEliminatingConstantFactorOfLhsWithZeroRhs
            pattern = SolvablePattern(AnyPattern(), FixedPattern(Constants.Zero))

            steps {
                branchOn(Setting.EliminateNonZeroFactorByDividing) {
                    case(BooleanSetting.False) {
                        apply(EquationsRules.EliminateConstantFactorOfLhsWithZeroRhsDirectly)
                    }
                    case(BooleanSetting.True) {
                        check { it is Comparison && it.rhs == Constants.Zero }
                        apply(solvablePlansForEquations.coefficientRemovalSteps)
                    }
                }
            }
        },
    ),

    SimplifyEquation(
        plan {
            explanation = Explanation.SimplifyEquation

            steps {
                whilePossible { deeply(simpleTidyUpSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible(SimplifyByEliminatingConstantFactorOfLhsWithZeroRhs)
                whilePossible(SolvableRules.CancelCommonTermsOnBothSides)
                optionally {
                    check { !isSet(Setting.DontCancelCommonFactorsWhenSimplifyingEquation) }
                    whilePossible(SolvableRules.CancelCommonFactorOnBothSides)
                }
                optionally(solvablePlansForEquations.rewriteBothSidesWithSameBaseAndSimplify)
                optionally(SolvableRules.CancelCommonBase)
                optionally(algebraicSimplificationStepsForEquations)
            }
        },
    ),

    // After balancing an equation we don't want to "undo" the balancing operation, e.g. if we multiply both sides by a
    // number we don't want to undo that in a simplification step.  Instead we always want to simplify both sides of the
    // equation.
    SimplifyEquationAfterBalancing(
        plan {
            explanation = Explanation.SimplifyEquation

            steps {
                whilePossible { deeply(simpleTidyUpSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                optionally(algebraicSimplificationStepsForEquations)
            }
        },
    ),
    CollectLikeTermsToTheLeftAndSimplify(
        plan {
            explanation = Explanation.CollectLikeTermsToTheLeftAndSimplify

            steps {
                apply(EquationsRules.CollectLikeTermsToTheLeft)
                optionally(SimplifyEquation)
            }
        },
    ),

    SimplifyByFactoringNegativeSignOfLeadingCoefficient(
        plan {
            explanation = Explanation.SimplifyByFactoringNegativeSignOfLeadingCoefficient

            steps {
                check { it.secondChild == Constants.Zero }
                applyTo(FactorRules.FactorNegativeSignOfLeadingCoefficient) { it.firstChild }
                apply(SolvableRules.NegateBothSides)
            }
        },
    ),

    SimplifyByDividingByGcfOfCoefficients(
        plan {
            explanation = Explanation.SimplifyByDividingByGcfOfCoefficients

            steps {
                applyTo(FactorRules.FactorGreatestCommonIntegerFactor) { it.firstChild }
                apply(SimplifyByEliminatingConstantFactorOfLhsWithZeroRhs)
            }
        },
    ),

    /**
     * rewrite a simplified quadratic equation to [(x + a)^2] = b form
     */
    RewriteToXPLusASquareEqualsBForm(
        plan {
            explanation = Explanation.RewriteToXPLusASquareEqualsBForm

            steps {
                apply(CompleteTheSquareAndSimplify)
                applyTo(FactorPlans.FactorSquareOfBinomial) { it.firstChild }
            }
        },
    ),

    CompleteTheSquareAndSimplify(
        plan {
            explanation = Explanation.CompleteTheSquareAndSimplify

            steps {
                apply(EquationsRules.CompleteTheSquare)
                applyToChildren(SimplifyPlans.SimplifyAlgebraicExpression)
            }
        },
    ),

    MultiplyByInverseOfLeadingCoefficientAndSimplify(
        plan {
            explanation = Explanation.MultiplyByInverseOfLeadingCoefficientAndSimplify

            steps {
                apply(EquationsRules.MultiplyByInverseOfLeadingCoefficient)
                apply(PolynomialsPlans.ExpandPolynomialExpressionWithoutNormalization)
            }
        },
    ),

    IsolateAbsoluteValue(
        plan {
            explanation = Explanation.IsolateAbsoluteValue

            val innerSteps = engine.methods.stepsproducers.steps {
                firstOf {
                    option(SolvableRules.MoveTermsNotContainingModulusToTheRight)
                    option(SolvableRules.MoveTermsNotContainingModulusToTheLeft)
                }
                apply(SimplifyEquation)
            }

            steps {
                branchOn(Setting.MoveTermsOneByOne) {
                    case(BooleanSetting.True) { whilePossible(innerSteps) }
                    case(BooleanSetting.False) { apply(innerSteps) }
                }
            }
        },
    ),

    MoveOneModulusToOtherSideAndSimplify(
        plan {
            explanation = Explanation.MoveOneModulusToOtherSideAndSimplify
            steps {
                firstOf {
                    option(EquationsRules.MoveSecondModulusToRhs)
                    option(EquationsRules.MoveSecondModulusToLhs)
                }
                optionally(SimplifyEquation)
            }
        },
    ),

    SolveEquationUnion(
        taskSet {
            val equationUnion = condition { it is StatementUnion }
            pattern = equationUnion
            explanation = Explanation.SolveEquationUnion

            tasks {
                // Create a task for each to simplify it
                val splitTasks = get(equationUnion).children.map {
                    task(
                        startExpr = it,
                        explanation = metadata(Explanation.SolveEquationInEquationUnion),
                        stepsProducer = optimalEquationSolvingSteps,
                    ) ?: return@tasks null
                }

                // Else combine the solutions together
                val overallSolution = computeOverallUnionSolution(splitTasks.map { it.result }) ?: return@tasks null
                task(
                    startExpr = overallSolution,
                    explanation = metadata(Explanation.CollectSolutions),
                )
                allTasks()
            }
        },
    ),

    SolveEquationWithOneAbsoluteValueBySubstitution(solveEquationWithOneAbsoluteValueBySubstitution),

    @PublicMethod
    SolveDecimalLinearEquation(
        plan {
            explanation = Explanation.SolveDecimalLinearEquation
            pattern = FindPattern(condition { it is DecimalExpression || it is RecurringDecimalExpression })

            val acceptedSolutions = oneOf(
                SignedNumberPattern(),
                optionalNegOf(RecurringDecimalPattern()),
            )

            resultPattern = oneOf(
                setSolutionOf(variableListOf(SolutionVariablePattern()), solutionSetOf(acceptedSolutions)),
                contradictionOf(variableListOf(SolutionVariablePattern())),
                identityOf(variableListOf(SolutionVariablePattern())),
            )

            steps {
                inContext(contextFactory = {
                    copy(settings = settings + Pair(Setting.PreferDecimals, BooleanSetting.True))
                }) {
                    apply(rearrangeLinearEquationSteps)
                    optionally {
                        branchOn(Setting.DontExtractSetSolution) {
                            case(BooleanSetting.False) {
                                apply(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
                            }
                        }
                    }
                }
            }
        },
    ),

    @PublicMethod
    SolveEquation(solveEquationPlan),

    @PublicMethod
    SolveEquationWithInequalityConstraint(solveEquationWithInequalityConstraint),

    SolveRationalEquation(solveEquationPlan),

    SineEquationSolutionExtractionTask(sineEquationSolutionExtractionTask),

    @PublicMethod
    SolveConstantEquation(
        plan {
            explanation = Explanation.SolveConstantEquation

            steps {
                apply(solveConstantEquationSteps)
            }
        },
    ),
}

val equationSimplificationSteps = steps {
    whilePossible {
        firstOf {
            option(NormalizationPlans.NormalizeExpression)
            // check for contradiction or identity
            option(EquationsRules.ExtractSolutionFromConstantEquation)
            // normalize the equation
            option(EquationsPlans.SimplifyEquation)
        }
    }
}

val optimalEquationSolvingSteps = steps {
    firstOf {
        option {
            optionally(EquationsPlans.SimplifyEquation)
            branchOn(Setting.DontExtractSetSolution) {
                case(BooleanSetting.False) {
                    firstOf {
                        option(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
                        option(EquationsRules.ExtractSolutionFromConstantEquation)
                    }
                }
            }
        }
        option(EquationsPlans.SolveEquation)
        option(EquationsPlans.SolveEquationWithInequalityConstraint)
    }
}

internal val constraintSimplificationPlan = plan {
    explanation = EquationsExplanation.SimplifyConstraint

    val simplifySingleConstraint = engine.methods.stepsproducers.steps {
        inContext(contextFactory = { copy(solutionVariables = it.variables.toList()) }) {
            firstOf {
                option(InequationsPlans.SolveInequation)
                option(InequalitiesPlans.SolveLinearInequality)
                option(InequalitiesPlans.SimplifyInequality)
            }
        }
    }

    steps {
        firstOf {
            option {
                check { it is StatementSystem }
                applyToChildren(simplifySingleConstraint)
            }
            option(simplifySingleConstraint)
        }
    }
}

val solvablePlansForEquations = SolvablePlans(
    EquationsPlans.SimplifyEquationAfterBalancing,
    constraintSimplificationPlan,
)

val rearrangeLinearEquationSteps = steps {
    whilePossible {
        firstOf {
            option(NormalizationPlans.NormalizeExpression)
            // check for contradiction or identity
            option(EquationsRules.ExtractSolutionFromConstantEquation)
            // normalize the equation
            option(EquationsPlans.SimplifyEquation)

            option(solvablePlansForEquations.removeConstantDenominatorsSteps)
            option(PolynomialsPlans.ExpandPolynomialExpressionWithoutNormalization)
        }
    }

    optionally(solvablePlansForEquations.solvableRearrangementSteps)
    optionally(solvablePlansForEquations.coefficientRemovalSteps)
}

val solveConstantEquationSteps = steps {
    check { it is Equation && it.isConstant() }

    optionally {
        plan {
            explanation = Explanation.SimplifyEquation

            steps {
                whilePossible(constantSimplificationSteps)
            }
        }
    }
    shortcut(EquationsRules.UndefinedConstantEquationIsFalse)
    shortcut(EquationsRules.ExtractSolutionFromConstantEquation)

    optionally(EquationsPlans.SimplifyEquation)
    shortcut(EquationsRules.ExtractSolutionFromConstantEquation)

    optionally(solvablePlansForEquations.moveEverythingToTheLeftAndSimplify)
    shortcut(EquationsRules.ExtractSolutionFromConstantEquation)

    inContext(contextFactory = { copy(precision = 10) }) {
        apply(evaluateBothSidesNumerically)
    }
    apply(EquationsRules.ExtractSolutionFromConstantEquation)
}

/**
 * f(x) = arcsin(g(x)) -->
 *      - solve: f(x) = arcsin(g(x))
 *      - solve: f(x) = /pi/ - arcsin(g(x))
 *      - collect the solutions into a statement union
 */
val sineEquationSolutionExtractionTask = taskSet {
    val lhs = condition { !it.isConstantIn(solutionVariables) }
    val rhs = condition { it.containsTrigExpression() }

    val equation = equationOf(
        lhs,
        rhs,
    )

    pattern = equation

    explanation = Explanation.DeriveGeneralSolutionOfEquationWithSine

    tasks {
        val params = listOf(
            get(equation) to metadata(Explanation.FindPrincipalSolution),
            equationOf(
                get(lhs),
                sumOf(Constants.Pi, negOf(get(rhs))),
            ) to metadata(Explanation.FindSupplementarySolution),
        )

        val tasks = params.map { (expression, explanation) ->
            task(
                startExpr = expression,
                explanation = explanation,
                stepsProducer = extractedTrigonometricEquationSolvingSteps,
            ) ?: return@tasks null
        }

        val (results, constraints) = tasks.map { t ->
            t.result.let {
                it.firstChild to it.secondChild
            }
        }.unzip()

        val overallSolution = if (results[0] != results[1]) {
            statementUnionOf(
                results,
            )
        } else {
            results[0]
        }

        val constraint = if (constraints.all { it == constraints[0] }) {
            constraints[0]
        } else {
            // This branch should never be reached, as in this taskset the constraint should always be the same
            computeOverallIntersectionSolution(constraints)
        }

        task(
            startExpr = expressionWithConstraintOf(
                bracketOf(overallSolution),
                constraint,
            ),
            explanation = metadata(Explanation.CollectSolutions),
        )

        allTasks()
    }
}

val solveEquationPlan = object : CompositeMethod() {
    // Can't be a rule since rules have been changed to apply only on the expression not the constraint
    private val mergeConstraintsRule = object : Method {
        override fun tryExecute(ctx: Context, sub: Expression): Transformation? {
            if (sub !is ExpressionWithConstraint) return null
            val innerExpression = sub.expression
            val constraint1 = sub.constraint

            if (innerExpression !is ExpressionWithConstraint) return null
            val expression = innerExpression.expression
            val constraint2 = innerExpression.constraint

            val constraintList = mutableListOf<Expression>()
            if (constraint1 is StatementSystem) {
                constraintList.addAll(constraint1.equations)
            } else {
                constraintList.add(constraint1)
            }
            if (constraint2 is StatementSystem) {
                constraintList.addAll(constraint2.equations)
            } else {
                constraintList.add(constraint2)
            }

            val mergedConstraints = computeOverallIntersectionSolution(constraintList)

            return Transformation(
                type = Transformation.Type.Rule,
                fromExpr = sub,
                toExpr = ExpressionWithConstraint(expression, mergedConstraints),
                explanation = metadata(SolverEngineExplanation.MergeConstraints),
            )
        }
    }

    private val solveEquationWithDomainRestrictions = taskSet {
        explanation = Explanation.SolveEquation

        tasks {
            val constraint = when {
                context.isSet(Setting.SolveEquationsWithoutComputingTheDomain) -> expression
                else -> task(
                    startExpr = expression,
                    explanation = metadata(AlgebraExplanation.ComputeDomainOfAlgebraicExpression),
                    stepsProducer = AlgebraPlans.ComputeDomainOfAlgebraicExpression,
                )?.result ?: return@tasks null
            }

            val solvePolynomialEquation = task(
                startExpr = expression,
                context = context.copy(constraintMerger = mergeConstraintsRule),
                explanation = metadata(Explanation.SolveEquation),
                stepsProducer = solveEquation.value,
            ) ?: return@tasks null

            val solution = solvePolynomialEquation.result

            // no need to check if the constraint(s) is/are satisfied if solution
            // is an empty set
            if (solution !is Contradiction || solution == Constants.EmptySet) {
                taskWithOptionalSteps(
                    startExpr = expressionWithConstraintOf(solution, constraint),
                    explanation = metadata(Explanation.AddDomainConstraintToSolution),
                    stepsProducer = addDomainConstraintToSolution,
                )
            }

            allTasks()
        }
    }

    private val addDomainConstraintToSolution = steps {
        optionally(mergeConstraintsRule)
        optionally(simplifySolutionWithConstraint)
    }

    override fun run(ctx: Context, sub: Expression): Transformation? {
        if (sub is Equation) {
            val solutionVariable = ctx.solutionVariables.singleOrNull() ?: return null

            if (sub.variables.contains(solutionVariable)) {
                val equationContext = ctx.copy(constraintMerger = mergeConstraintsRule)

                return if (findDenominatorsAndDivisors(sub).any { (expr, _) -> !expr.isConstant() }) {
                    solveEquationWithDomainRestrictions.run(equationContext, sub)
                } else {
                    solveEquation.value.run(equationContext, sub)
                }
            }
        }

        return null
    }
}
