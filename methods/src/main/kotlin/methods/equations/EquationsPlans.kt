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
import engine.expressions.Constants.Pi
import engine.expressions.Constants.Two
import engine.expressions.Contradiction
import engine.expressions.DecimalExpression
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.expressions.RecurringDecimalExpression
import engine.expressions.SetSolution
import engine.expressions.StatementSystem
import engine.expressions.StatementUnion
import engine.expressions.Variable
import engine.expressions.containsTrigExpression
import engine.expressions.equationOf
import engine.expressions.expressionWithConstraintOf
import engine.expressions.finiteSetOf
import engine.expressions.fractionOf
import engine.expressions.inequationOf
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.statementUnionOf
import engine.expressions.sumOf
import engine.methods.CompositeMethod
import engine.methods.Method
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.SolverEngineExplanation
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.operators.TrigonometricFunctionType
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.FindPattern
import engine.patterns.FixedPattern
import engine.patterns.OptionalWrappingPattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.SolvablePattern
import engine.patterns.TrigonometricExpressionPattern
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.contradictionOf
import engine.patterns.equationOf
import engine.patterns.expressionWithConstraintOf
import engine.patterns.identityOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.setSolutionOf
import engine.patterns.solutionSetOf
import engine.patterns.variableListOf
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.Task
import engine.steps.Transformation
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.metadata
import methods.algebra.AlgebraExplanation
import methods.algebra.AlgebraPlans
import methods.algebra.findDenominatorsAndDivisors
import methods.angles.TrigonometricFunctionsRules
import methods.angles.findFunctionsRequiringDomainCheck
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
import methods.solvable.findUnusedVariableLetter

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

    ComputeDomainOfTrigonometricExpression(
        taskSet {
            explanation = methods.angles.Explanation.ComputeDomainOfTrigonometricExpression

            val solveInequationInOneVariableSteps = steps {
                inContext({ copy(solutionVariables = it.firstChild.variables.toList()) }) {
                    apply(InequationsPlans.SolveInequation)
                    optionally(
                        EquationsPlans.NormalizePeriod,
                    )
                }
            }

            tasks {
                val variable = Variable(findUnusedVariableLetter(expression))

                val rhs = sumOf(
                    fractionOf(Pi, Two),
                    productOf(variable, Pi),
                )

                val functions = findFunctionsRequiringDomainCheck(expression).distinct().toList()

                if (functions.isEmpty()) {
                    return@tasks null
                }

                val constraints = functions.map {
                    taskWithOptionalSteps(
                        startExpr = inequationOf(
                            it.firstChild,
                            rhs,
                        ),
                        explanation = metadata(methods.angles.Explanation.ExpressionMustNotBeUndefined),
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

    @PublicMethod
    SolveEquation(solveEquationPlan),

    @PublicMethod
    SolveEquationWithInequalityConstraint(solveEquationWithInequalityConstraint),

    SolveRationalEquation(solveEquationPlan),

    SineEquationSolutionExtractionTask(sineEquationSolutionExtractionTask),

    CosineEquationSolutionExtractionTask(cosineEquationSolutionExtractionTask),

    SubstituteOriginalExpressionIntoQuadraticTrigEquation(substituteOriginalExpressionIntoQuadraticTrigEquation),

    MergeTrigonometricEquationSolutionsTask(mergeTrigonometricEquationSolutionsTask),

    NormalizePeriod(normalizePeriodPlan),

    DivideByCosAndSimplify(divideByCosAndSimplify),

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

fun mergeSolutionsWithConstraints(tasks: List<Task>): Expression? {
    val (results, constraints) = tasks.map { task ->
        task.result.let {
            when (it) {
                is ExpressionWithConstraint -> it.firstChild to it.secondChild
                else -> it to null
            }
        }
    }.unzip()

    val overallUnionSolution = computeOverallUnionSolution(results) ?: return null

    val filteredConstraints = constraints.filterNotNull()

    return if (filteredConstraints.isNotEmpty()) {
        val constraint = if (filteredConstraints.all { it == constraints[0] }) {
            constraints[0]
        } else {
            // This branch should never be reached, as in this taskset the constraint
            // should always be the same
            computeOverallIntersectionSolution(filteredConstraints)
        }

        expressionWithConstraintOf(
            overallUnionSolution,
            constraint,
        )
    } else {
        overallUnionSolution
    }
}

fun trigonometricFunctionExtractionTaskBuilder(
    solvingSteps: StepsProducer,
    explanationKey: MetadataKey,
    supplementarySolutionKey: MetadataKey,
    supplementarySolutionExtractor: (lhs: Expression, rhs: Expression) -> Expression,
): CompositeMethod =
    taskSet {
        val lhs = condition { !it.isConstantIn(solutionVariables) }
        val rhs = condition { it.containsTrigExpression() }

        val equation = equationOf(
            lhs,
            rhs,
        )

        pattern = equation

        explanation = explanationKey

        tasks {
            val params = listOf(
                get(equation) to metadata(Explanation.FindPrincipalSolution),
                supplementarySolutionExtractor(
                    get(lhs),
                    get(rhs),
                ) to metadata(supplementarySolutionKey),
            )

            val tasks = params.map { (expression, explanation) ->
                task(
                    startExpr = expression,
                    explanation = explanation,
                    stepsProducer = solvingSteps,
                ) ?: return@tasks null
            }

            val startExpression = mergeSolutionsWithConstraints(tasks) ?: return@tasks null

            task(
                startExpr = startExpression,
                explanation = metadata(Explanation.CollectSolutions),
            )

            allTasks()
        }
    }

/**
 * f(x) = arcsin(g(x)) -->
 *      - solve: f(x) = arcsin(g(x))
 *      - solve: f(x) = /pi/ - arcsin(g(x))
 *      - collect the solutions into a statement union
 */
val sineEquationSolutionExtractionTask = trigonometricFunctionExtractionTaskBuilder(
    extractedSineEquationSolvingSteps,
    Explanation.DeriveGeneralSolutionOfEquationWithSine,
    Explanation.FindSupplementarySolution,
) { lhs: Expression, rhs: Expression ->
    equationOf(
        lhs,
        sumOf(Constants.Pi, negOf(rhs)),
    )
}

/**
 * f(x) = arccos(g(x)) -->
 *      - solve: f(x) = arccos(g(x))
 *      - solve: f(x) = - arccos(g(x))
 *      - collect the solutions into a statement union
 */
val cosineEquationSolutionExtractionTask =
    trigonometricFunctionExtractionTaskBuilder(
        extractedCosineEquationSolvingSteps,
        Explanation.DeriveGeneralSolutionOfEquationWithCosine,
        Explanation.FindOppositeSolution,
    ) { lhs: Expression, rhs: Expression ->
        equationOf(
            lhs,
            negOf(rhs),
        )
    }

val substituteOriginalExpressionIntoQuadraticTrigEquation = taskSet {
    val solvedEquation = setSolutionOf(AnyPattern(), AnyPattern())

    val originalExpressionEquation = equationOf(
        ArbitraryVariablePattern(),
        TrigonometricExpressionPattern(AnyPattern()),
    )

    val equationUnion = engine.patterns.statementSystemOf(
        solvedEquation,
        originalExpressionEquation,
    )

    pattern = equationUnion
    explanation = Explanation.SubstituteOriginalExpressionIntoQuadraticTrigEquationAndSolve

    explanationParameters(originalExpressionEquation)

    tasks {
        val solutionSet = get(solvedEquation) as SetSolution
        val substitutedVariable = solutionSet.firstChild.let {
            if (it.childCount == 1) {
                it.firstChild
            } else {
                return@tasks null
            }
        }

        val substitutedValues = solutionSet.secondChild.children.map {
            equationOf(substitutedVariable, it)
        }

        val originalEquation = get(originalExpressionEquation)
        val originalExp = originalEquation.secondChild

        val splitTasks = substitutedValues.map {
            task(
                startExpr = equationOf(originalExp, it.secondChild),
                explanation = metadata(
                    Explanation.SolveTrigonometricEquation,
                ),
                stepsProducer = optimalEquationSolvingSteps,
            ) ?: return@tasks null
        }

        if (splitTasks.size > 1) {
            val overallSolution = mergeSolutionsWithConstraints(splitTasks) ?: return@tasks null
            task(
                startExpr = overallSolution,
                explanation = metadata(Explanation.CollectSolutions),
            )
        }

        allTasks()
    }
}

val mergeTrigonometricEquationSolutionsTask = taskSet {
    val solution = setSolutionOf(
        AnyPattern(),
        AnyPattern(),
    )
    val optionalConstraint = OptionalWrappingPattern(solution) {
        expressionWithConstraintOf(
            it,
            AnyPattern(),
        )
    }

    pattern = optionalConstraint
    explanation = Explanation.MergeTrigonometricEquationSolutions

    tasks {
        val (solution, constraint) = get(optionalConstraint).let {
            if (it is ExpressionWithConstraint) {
                it.firstChild to it.secondChild
            } else {
                it to null
            }
        }

        val allSolutions = solution.let {
            if (it is SetSolution) {
                it.asEquationList()
            } else {
                null
            }
        }?.toMutableList() ?: return@tasks null

        var modified = true
        val solutionTasks = mutableListOf<Task>()

        // This may not be the most elegant solution :(
        // We try to apply the rule for merging to all combinations of solutions, if we merge two, we start the loop
        // again. If in a pass we didn't merge any, we stop and return the solution.
        while (modified) {
            modified = false
            for (i in 0..<allSolutions.size) {
                for (j in 0..<allSolutions.size) {
                    if (i != j) {
                        val newTask = task(
                            startExpr = statementUnionOf(
                                allSolutions[i],
                                allSolutions[j],
                            ),
                            explanation = metadata(Explanation.SimplifyTrigonometricEquationSolution),
                            stepsProducer = simplifyTrigonometricEquationSolutionSteps,
                        )

                        newTask?.result?.let { result ->
                            modified = true

                            allSolutions[i] = result.let {
                                if (it is SetSolution) {
                                    it.asEquation() ?: it
                                } else {
                                    it
                                }
                            }

                            allSolutions.removeAt(j)

                            solutionTasks.add(newTask)
                        }
                    }
                    if (modified) break
                }
                if (modified) break
            }
        }

        val mergedSolution = engine.expressions.setSolutionOf(
            engine.expressions.variableListOf(
                allSolutions.first().firstChild.variables.toList(),
            ),
            finiteSetOf(
                allSolutions.map {
                    it.secondChild
                },
            ),
        )

        solutionTasks.add(
            task(
                startExpr = if (constraint != null) {
                    expressionWithConstraintOf(mergedSolution, constraint)
                } else {
                    mergedSolution
                },
                explanation = metadata(Explanation.CollectSolutions),
            ),
        )

        solutionTasks
    }
}

val normalizePeriodPlan = plan {
    explanation = Explanation.NormalizePeriod

    steps {
        optionally {
            applyTo(extractor = { it.secondChild }) {
                deeply(EquationsRules.ExtractPeriodFromFraction)
                optionally(EquationsPlans.SimplifyEquation)
            }
        }

        optionally {
            applyTo(extractor = { it.secondChild }) {
                deeply(EquationsRules.FlipSignOfPeriod)
            }
        }

        optionally {
            applyTo(EquationsRules.ReorderSumWithPeriod) {
                it.secondChild
            }
        }
    }
}

// Divide equation of form sin[x] + b cos [x] = 0 by the cosine term and simplify the equation.
val divideByCosAndSimplify = plan {
    val argument = AnyPattern()
    val sine = withOptionalConstantCoefficient(
        TrigonometricExpressionPattern(
            argument,
            listOf(TrigonometricFunctionType.Sin),
        ),
    )
    val cosine =
        TrigonometricExpressionPattern(
            argument,
            listOf(TrigonometricFunctionType.Cos),
        )

    val cosineTerm = withOptionalConstantCoefficient(cosine)

    pattern = equationOf(commutativeSumOf(sine, cosineTerm), FixedPattern(Constants.Zero))

    explanation = Explanation.DivideByTrigFunctionAndSimplify

    explanationParameters(cosine)

    steps {
        applyTo(EquationsRules.DivideByCos) {
            it.firstChild
        }

        applyTo(extractor = { it.firstChild }) {
            optionally { deeply(EquationsRules.ExtractSineOverCosine) }
            deeply(TrigonometricFunctionsRules.SimplifyToDerivedFunction)
        }

        optionally(algebraicSimplificationStepsForEquations)

        // The expression should only contain tangents. Otherwise, something went wrong with the simplification and
        // we could enter an infinite loop.
        check {
            it.onlyContainsTrigFunctionType(TrigonometricFunctionType.Tan)
        }
    }
}

val simplifyTrigonometricEquationSolutionSteps = steps {
    apply(EquationsRules.MergeTrigonometricEquationSolutions)
    optionally {
        applyTo(algebraicSimplificationStepsForEquations) {
            it.secondChild
        }
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
                    stepsProducer = steps {
                        firstOf {
                            option(AlgebraPlans.ComputeDomainOfAlgebraicExpression)
                            option(EquationsPlans.ComputeDomainOfTrigonometricExpression)
                        }
                    },
                )?.result ?: return@tasks null
            }

            val solvePolynomialEquation = task(
                startExpr = expression,
                context = context.copy(constraintMerger = mergeConstraintsRule),
                explanation = metadata(Explanation.SolveEquation),
                stepsProducer = solveEquation.value,
            ) ?: return@tasks null

            val result = solvePolynomialEquation.result
            val (solution, solutionConstraint) = when (result) {
                is ExpressionWithConstraint -> result.firstChild to result.secondChild
                else -> result to null
            }

            val mergedConstraint = when (solutionConstraint) {
                null -> constraint
                else -> computeOverallIntersectionSolution(listOf(constraint, solutionConstraint))
            }

            // no need to check if the constraint(s) is/are satisfied if solution
            // is an empty set
            if (solution !is Contradiction || solution == Constants.EmptySet) {
                taskWithOptionalSteps(
                    startExpr = expressionWithConstraintOf(solution, mergedConstraint),
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

                return if (
                    findDenominatorsAndDivisors(sub).any { (expr, _) ->
                        !expr.isConstant()
                    } ||
                    findFunctionsRequiringDomainCheck(sub).toList().isNotEmpty()
                ) {
                    solveEquationWithDomainRestrictions.run(equationContext, sub)
                } else {
                    solveEquation.value.run(equationContext, sub)
                }
            }
        }

        return null
    }
}
