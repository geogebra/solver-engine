package methods.equationsystems

import engine.context.emptyContext
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.cartesianProductOf
import engine.expressions.contradictionOf
import engine.expressions.equationOf
import engine.expressions.equationSystemOf
import engine.expressions.identityOf
import engine.expressions.implicitSolutionOf
import engine.expressions.negOf
import engine.expressions.setSolutionOf
import engine.expressions.solutionSetOf
import engine.expressions.tupleOf
import engine.expressions.variableListOf
import engine.expressions.xp
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.TasksBuilder
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.operators.MultiVariateSolutionOperator
import engine.operators.SetOperators
import engine.operators.SolutionOperator
import engine.patterns.AnyPattern
import engine.patterns.FindPattern
import engine.patterns.FixedPattern
import engine.patterns.RootMatch
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.equationSystemOf
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.Task
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.metadata
import methods.equations.EquationsPlans
import methods.equations.equationSimplificationSteps
import methods.equations.rearrangeLinearEquationSteps
import methods.polynomials.PolynomialsPlans

enum class EquationSystemsPlans(override val runner: CompositeMethod) : RunnerMethod {

    /**
     * 1. Select which equation (1) will be rearranged to x = ...
     * 2. Rearrange (1) --> (1')
     * 3. Substitute x = ... into the other equation (2)
     * 4. Solve equation (2) in y
     * 5. Substitute y = ... back into equation (1')
     */
    @PublicMethod
    SolveEquationSystemBySubstitution(SystemSolverBySubstitution.taskSet()),
}

/**
 * This is meant to be a parent class for both solving equation systems by substitution and elimination.
 */
private abstract class SystemSolver {

    /**
     * The explanation key for the produced task set.
     */
    abstract val explanation: MetadataKey

    /**
     * Rearrange the equations to prepare them for solving, i.e. collect like terms, gather like terms on either
     * side of the equation, etc...  The steps might be slightly different for substitution and elimination because
     * in the case of elimination we want variables one the left and constants on the right, whereas we are more
     * flexible for substitution.
     *
     * This is used as a first step on both equations by the taskSet method.
     */
    abstract val prearrangeLinearEquationSteps: StepsProducer

    /**
     * Use [firstEq] and [secondEq], which are guaranteed to be equations in both [variables], to produce
     * - an equation relating both variables (it may be [firstEq] or [secondEq])
     * - a solved equation in either variable (could be an identity or a contradiction)
     * If that is not possible then null is returned.
     */
    abstract fun TasksBuilder.solveIndependentEquations(
        firstEq: Expression,
        secondEq: Expression,
        variables: List<String>,
    ): Pair<Expression, Expression>?

    /**
     * Returns a taskSet that can solve a system of 2 equations, using the values for [explanation],
     * [prearrangeLinearEquationSteps] and [solveIndependentEquations].
     */
    fun taskSet() = taskSet {
        explanation = this@SystemSolver.explanation
        val systemPtn = equationSystemOf(
            equationOf(AnyPattern(), AnyPattern()),
            equationOf(AnyPattern(), AnyPattern()),
        )
        pattern = condition(systemPtn) { it.variables.size == 2 }

        tasks {
            val system = get(systemPtn)
            val variables = system.variables.sorted()

            // Simplify both equations and sort them to reduce the number of cases to consider.
            val (firstEq, secondEq) = prearrangeEquations(system, variables)

            // Now deal with them according to the outcomes
            when {
                firstEq.isContradiction() -> {
                    task(
                        startExpr = contradictionOf(variableListOf(variables), firstEq.secondChild),
                        explanation = metadata(Explanation.BuildSolutionContradiction),
                    )
                }
                firstEq.isIdentity() -> solveSystemContainingIdentity(firstEq, secondEq, variables)
                firstEq.isSolved() -> when {
                    secondEq.isSolved() -> solveSystemWithBothEquationsSolved(firstEq, secondEq, variables)
                    else -> solveSystemWithOneEquationSolved(firstEq, secondEq)
                }
                else -> solveSystemWithTwoEquationsInBothVariables(firstEq, secondEq, variables)
            } ?: return@tasks null
            allTasks()
        }
    }

    private fun TasksBuilder.prearrangeEquations(
        system: Expression,
        variables: List<String>,
    ): Pair<Expression, Expression> {
        val firstEq = task(
            startExpr = system.firstChild,
            explanation = metadata(Explanation.PrepareEquation),
            stepsProducer = this@SystemSolver.prearrangeLinearEquationSteps,
            context = context.copy(solutionVariables = variables),
        )?.result ?: system.firstChild

        val secondEq = task(
            startExpr = system.secondChild,
            explanation = metadata(Explanation.PrepareEquation),
            stepsProducer = this@SystemSolver.prearrangeLinearEquationSteps,
            context = context.copy(solutionVariables = variables),
        )?.result ?: system.secondChild

        return when {
            firstEq.isContradiction() -> Pair(firstEq, secondEq)
            secondEq.isContradiction() -> Pair(secondEq, firstEq)
            firstEq.isIdentity() -> Pair(firstEq, secondEq)
            secondEq.isIdentity() -> Pair(secondEq, firstEq)
            firstEq.isSolved() -> Pair(firstEq, secondEq)
            secondEq.isSolved() -> Pair(secondEq, firstEq)
            else -> Pair(firstEq, secondEq)
        }
    }

    private fun TasksBuilder.solveSystemContainingIdentity(
        firstEq: Expression,
        secondEq: Expression,
        variables: List<String>,
    ): Task {
        return when {
            secondEq.isIdentity() -> {
                task(
                    startExpr = identityOf(
                        variableListOf(variables),
                        equationSystemOf(firstEq.secondChild, secondEq.secondChild),
                    ),
                    explanation = metadata(
                        Explanation.BuildSolutionIdentities,
                        xp(variables[0]),
                        xp(variables[1]),
                    ),
                )
            }
            secondEq.isSolved() -> {
                val secondEqVar = secondEq.firstChild
                val otherVar = variables.first { it != secondEqVar.operator.name }

                task(
                    startExpr = combineSolutions(
                        secondEqVar,
                        secondEq.secondChild,
                        xp(otherVar),
                        Constants.Reals,
                    ),
                    explanation = metadata(Explanation.BuildSolutionOneVariableFixed, secondEqVar),
                )
            }
            else -> {
                task(
                    startExpr = implicitSolutionOf(variableListOf(variables), secondEq),
                    explanation = metadata(Explanation.BuildSolutionIdentityAndEquation),
                )
            }
        }
    }

    private fun TasksBuilder.solveSystemWithBothEquationsSolved(
        firstEq: Expression,
        secondEq: Expression,
        variables: List<String>,
    ) {
        val firstEqVar = firstEq.firstChild
        val secondEqVar = secondEq.firstChild
        if (firstEqVar != secondEqVar) {
            val solution = combineSolutions(
                firstEqVar,
                firstEq.secondChild,
                secondEqVar,
                secondEq.secondChild,
            )
            task(
                startExpr = solution,
                explanation = metadata(
                    Explanation.BuildSolutionCombineUniqueSolutions,
                    solution.firstChild.children,
                ),
            )
        } else {
            val firstEqValue = firstEq.secondChild.firstChild
            val secondEqValue = secondEq.secondChild.firstChild
            if (firstEqValue == secondEqValue) {
                val otherVar = variables.first { it != firstEqVar.operator.name }
                task(
                    startExpr = combineSolutions(
                        firstEq.firstChild,
                        firstEq.secondChild,
                        xp(otherVar),
                        Constants.Reals,
                    ),
                    explanation = metadata(
                        Explanation.BuildSolutionSameSolutionInOneVariable,
                        firstEq.firstChild,
                        firstEqValue,
                    ),
                )
            } else {
                task(
                    startExpr = contradictionOf(
                        variableListOf(variables),
                        equationOf(firstEqValue, secondEqValue),
                    ),
                    explanation = metadata(
                        Explanation.BuildSolutionDifferentSolutionsInOneVariable,
                        firstEq.firstChild,
                        firstEqValue,
                        secondEqValue,
                    ),
                )
            }
        }
    }

    private fun TasksBuilder.solveSystemWithOneEquationSolved(
        firstEq: Expression,
        secondEq: Expression,
    ): Task? {
        val solveSecondEq = substituteAndSolveIn(secondEq, firstEq) ?: return null
        val solution = combineSolutions(
            firstEq.firstChild,
            firstEq.secondChild,
            solveSecondEq.result.firstChild,
            solveSecondEq.result.secondChild,
        )
        return task(
            startExpr = solution,
            explanation = metadata(
                Explanation.BuildSolutionCombineUniqueSolutions,
                solution.firstChild.children,
            ),
        )
    }

    private fun TasksBuilder.solveSystemWithTwoEquationsInBothVariables(
        firstEq: Expression,
        secondEq: Expression,
        variables: List<String>,
    ): Task? {
        val (eq1, eq2Solution) = solveIndependentEquations(firstEq, secondEq, variables)
            ?: return null

        return when (eq2Solution.operator) {
            MultiVariateSolutionOperator.Contradiction -> {
                task(
                    startExpr = contradictionOf(variableListOf(variables), eq2Solution.secondChild),
                    explanation = metadata(Explanation.BuildSolutionContradiction),
                )
            }
            MultiVariateSolutionOperator.Identity -> {
                task(
                    startExpr = implicitSolutionOf(variableListOf(variables), eq1),
                    explanation = metadata(Explanation.BuildSolutionIdentityAndEquation),
                )
            }
            else -> {
                val solveEq1 = substituteAndSolveIn(eq1, eq2Solution) ?: return null

                val solution = combineSolutions(
                    solveEq1.result.firstChild,
                    solveEq1.result.secondChild,
                    eq2Solution.firstChild,
                    eq2Solution.secondChild,
                )
                task(
                    startExpr = solution,
                    explanation = metadata(
                        Explanation.BuildSolutionCombineUniqueSolutions,
                        solution.firstChild.children,
                    ),
                )
            }
        }
    }

    private fun TasksBuilder.substituteAndSolveIn(equation: Expression, solution: Expression): Task? {
        val solutionAsEquation = solution.asEquation() ?: return null
        val substitutedBackSolution = equation.substituteAllOccurrences(
            solutionAsEquation.firstChild,
            solutionAsEquation.secondChild,
        )
        val var1 = substitutedBackSolution.variables.first()
        return task(
            startExpr = substitutedBackSolution,
            explanation = metadata(
                Explanation.SubstituteAndSolveIn,
                solutionAsEquation,
                xp(var1),
            ),
            stepsProducer = EquationsPlans.SolveLinearEquation,
            context = context.copy(solutionVariables = listOf(var1)),
        )
    }
}

/**
 * This implements [SystemSolver] to solve a system of linear equations by substitution.
 */
private object SystemSolverBySubstitution : SystemSolver() {
    override val explanation = Explanation.SolveEquationSystemBySubstitution

    override val prearrangeLinearEquationSteps = steps {
        whilePossible {
            firstOf {
                option(equationSimplificationSteps)
                option(EquationsPlans.MultiplyByLCDAndSimplify)
                option(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
                option(EquationsPlans.CollectLikeTermsToTheLeftAndSimplify)
            }
        }
        optionally {
            check { it.variables.size == 1 }
            inContext(contextFactory = { copy(solutionVariables = it.variables.toList()) }) {
                apply(EquationsPlans.SolveLinearEquation)
            }
        }
    }

    override fun TasksBuilder.solveIndependentEquations(
        firstEq: Expression,
        secondEq: Expression,
        variables: List<String>,
    ): Pair<Expression, Expression>? {
        val (eq1, var1, eq2, var2) = chooseSolutionOrder(firstEq, secondEq, variables) ?: return null

        // Try to rearrange eq1 to express x in terms of y
        val expressVar1InTermsOfVar2 = task(
            startExpr = eq1,
            explanation = metadata(Explanation.ExpressInTermsOf, xp(var1), xp(var2)),
            context = context.copy(solutionVariables = listOf(var1)),
        ) {
            apply(rearrangeLinearEquationSteps)
            optionally {
                applyTo(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization) {
                    it.secondChild
                }
            }
        }

        // If it was successful, or if x was already expressed in terms of y, substitute all occurrences of x in
        // eq2 and solve it to find the value of y.
        return when {
            expressVar1InTermsOfVar2 != null -> expressVar1InTermsOfVar2.result
            eq1.firstChild.variables == setOf(var1) && eq1.secondChild.variables == setOf(var2) -> eq1
            else -> null
        }?.let { var1InTermsOfVar2 ->
            val eq2InTermsOfVar2 = eq2.substituteAllOccurrences(xp(var1), var1InTermsOfVar2.secondChild)

            task(
                startExpr = eq2InTermsOfVar2,
                explanation = metadata(Explanation.SubstituteAndSolveIn, var1InTermsOfVar2, xp(var2)),
                stepsProducer = EquationsPlans.SolveLinearEquation,
                context = context.copy(solutionVariables = listOf(var2)),
            )?.let { solveEq2 ->
                Pair(var1InTermsOfVar2, solveEq2.result)
            }
        }
    }

    private data class SolutionOrder(val eq1: Expression, val var1: String, val eq2: Expression, val var2: String)

    @Suppress("ReturnCount")
    private fun chooseSolutionOrder(eq1: Expression, eq2: Expression, allVariables: List<String>): SolutionOrder? {
        if (allVariables.size != 2) {
            return null
        }
        val x = allVariables[0]
        val y = allVariables[1]

        val xCoeffInEq1 = eq1.coefficientOf(x) ?: return SolutionOrder(eq1, y, eq2, x)
        val yCoeffInEq1 = eq1.coefficientOf(y) ?: return SolutionOrder(eq1, x, eq2, y)
        val xCoeffInEq2 = eq2.coefficientOf(x) ?: return SolutionOrder(eq2, y, eq1, x)
        val yCoeffInEq2 = eq2.coefficientOf(y) ?: return SolutionOrder(eq2, x, eq1, y)

        for (goodCoeff in listOf(Constants.One, negOf(Constants.One))) {
            when {
                xCoeffInEq1 == goodCoeff -> return SolutionOrder(eq1, x, eq2, y)
                yCoeffInEq1 == goodCoeff -> return SolutionOrder(eq1, y, eq2, x)
                xCoeffInEq2 == goodCoeff -> return SolutionOrder(eq2, x, eq1, y)
                yCoeffInEq2 == goodCoeff -> return SolutionOrder(eq2, y, eq1, x)
            }
        }
        // No obvious order, so just return the default one
        return SolutionOrder(eq1, x, eq2, y)
    }
}

/**
 * Combines two solutions in one variable each into one solution in two variables.
 */
private fun combineSolutions(var1: Expression, sol1: Expression, var2: Expression, sol2: Expression): Expression {
    if (var1.operator.name > var2.operator.name) {
        return combineSolutions(var2, sol2, var1, sol1)
    }
    return setSolutionOf(
        variableListOf(var1, var2),
        when {
            sol1 == Constants.Reals || sol2 == Constants.Reals -> cartesianProductOf(sol1, sol2)
            else -> solutionSetOf(tupleOf(sol1.firstChild, sol2.firstChild))
        },
    )
}

/**
 * Returns the coefficient of [x] if [x] appears exactly once in the expression, else returns null
 */
private fun Expression.coefficientOf(x: String): Expression? {
    val ptn = withOptionalConstantCoefficient(FixedPattern(xp(x)))
    val matches = FindPattern(ptn, stopWhenFound = true).findMatches(emptyContext, RootMatch, this).toList()
    return if (matches.size == 1) ptn.coefficient(matches[0]) else null
}

/**
 * Turn a unique solution into an equation x = k
 */
private fun Expression.asEquation(): Expression? {
    return when (operator) {
        SolutionOperator -> {
            when (secondChild.operator) {
                SetOperators.Reals -> equationOf(firstChild, firstChild)
                SetOperators.FiniteSet -> {
                    if (secondChild.childCount == 1) equationOf(firstChild, secondChild.firstChild) else null
                }
                else -> null
            }
        }
        else -> null
    }
}

private fun Expression.isIdentity() = operator == MultiVariateSolutionOperator.Identity
private fun Expression.isContradiction() = operator == MultiVariateSolutionOperator.Contradiction
private fun Expression.isSolved() = operator == SolutionOperator
