package methods.equationsystems

import engine.context.Context
import engine.context.emptyContext
import engine.expressions.Constants
import engine.expressions.Contradiction
import engine.expressions.Expression
import engine.expressions.Identity
import engine.expressions.SetSolution
import engine.expressions.addEquationsOf
import engine.expressions.asInteger
import engine.expressions.cartesianProductOf
import engine.expressions.contradictionOf
import engine.expressions.equationOf
import engine.expressions.equationSystemOf
import engine.expressions.identityOf
import engine.expressions.implicitSolutionOf
import engine.expressions.isEquationSystem
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.setSolutionOf
import engine.expressions.simplifiedNegOf
import engine.expressions.solutionSetOf
import engine.expressions.subtractEquationsOf
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
import engine.patterns.AnyPattern
import engine.patterns.FindPattern
import engine.patterns.FixedPattern
import engine.patterns.RootMatch
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.equationSystemOf
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.Task
import engine.steps.Transformation
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.metadata
import methods.equations.EquationsPlans
import methods.equations.EquationsRules
import methods.equations.equationSimplificationSteps
import methods.equations.rearrangeLinearEquationSteps
import methods.equations.simplifyEquation
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
    SolveEquationSystemBySubstitution(SystemSolverBySubstitution),

    @PublicMethod
    SolveEquationSystemByElimination(SystemSolverByElimination),

    /**
     * Solve a system of two equations in one variable by solving each equation individually and keeping
     * only the common solutions.
     */
    @PublicMethod
    SolveEquationSystemInOneVariable(solveEquationSystemInOneVariable),
}

/**
 * This is meant to be a parent class for both solving equation systems by substitution and elimination.
 */
private abstract class SystemSolver : CompositeMethod() {

    override fun run(ctx: Context, sub: Expression): Transformation? {
        if (sub.isEquationSystem()) {
            val namedSystem = equationSystemOf(
                sub.children.mapIndexed { i, eq -> eq.withName(label(i + 1)) },
            ).withOrigin(sub.origin)
            return taskSet().run(ctx, namedSystem)
        }
        return null
    }

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
            when (firstEq) {
                is Contradiction -> {
                    task(
                        startExpr = contradictionOf(variableListOf(variables), firstEq.secondChild),
                        explanation = metadata(Explanation.BuildSolutionContradiction),
                    )
                }
                is Identity -> solveSystemContainingIdentity(firstEq, secondEq, variables)
                is SetSolution -> when {
                    secondEq is SetSolution -> solveSystemWithBothEquationsSolved(firstEq, secondEq, variables)
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
            startExpr = system.firstChild.withoutName(),
            explanation = metadata(Explanation.PrepareEquation),
            stepsProducer = this@SystemSolver.prearrangeLinearEquationSteps,
            resultLabel = system.firstChild.name,
            context = context.copy(solutionVariables = variables),
        )?.result ?: system.firstChild

        val secondEq = task(
            startExpr = system.secondChild.withoutName(),
            explanation = metadata(Explanation.PrepareEquation),
            stepsProducer = this@SystemSolver.prearrangeLinearEquationSteps,
            resultLabel = system.secondChild.name,
            context = context.copy(solutionVariables = variables),
        )?.result ?: system.secondChild

        return when {
            firstEq is Contradiction -> Pair(firstEq, secondEq)
            secondEq is Contradiction -> Pair(secondEq, firstEq)
            firstEq is Identity -> Pair(firstEq, secondEq)
            secondEq is Identity -> Pair(secondEq, firstEq)
            firstEq is SetSolution -> Pair(firstEq, secondEq)
            secondEq is SetSolution -> Pair(secondEq, firstEq)
            else -> Pair(firstEq, secondEq)
        }
    }

    private fun TasksBuilder.solveSystemContainingIdentity(
        firstEq: Identity,
        secondEq: Expression,
        variables: List<String>,
    ): Task {
        return when (secondEq) {
            is Identity -> {
                task(
                    startExpr = identityOf(
                        variableListOf(variables),
                        equationSystemOf(firstEq.identityExpression, secondEq.identityExpression),
                    ),
                    explanation = metadata(
                        Explanation.BuildSolutionIdentities,
                        xp(variables[0]),
                        xp(variables[1]),
                    ),
                )
            }
            is SetSolution -> {
                val secondEqVar = secondEq.solutionVariable
                val otherVar = variables.first { it != secondEqVar }

                task(
                    startExpr = combineSolutions(
                        secondEqVar,
                        secondEq.secondChild,
                        otherVar,
                        Constants.Reals,
                    ),
                    explanation = metadata(Explanation.BuildSolutionOneVariableFixed, xp(secondEqVar)),
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
        firstEq: SetSolution,
        secondEq: SetSolution,
        variables: List<String>,
    ) {
        val firstEqVar = firstEq.solutionVariable
        val secondEqVar = secondEq.solutionVariable
        if (firstEqVar != secondEqVar) {
            val solution = combineSolutions(
                firstEqVar,
                firstEq.solutionSet,
                secondEqVar,
                secondEq.solutionSet,
            )
            task(
                startExpr = solution,
                explanation = metadata(
                    Explanation.BuildSolutionCombineUniqueSolutions,
                    solution.firstChild.children,
                ),
            )
        } else {
            val firstEqValue = firstEq.solution
            val secondEqValue = secondEq.solution
            if (firstEqValue == secondEqValue) {
                val otherVar = variables.first { it != firstEqVar }
                task(
                    startExpr = combineSolutions(
                        firstEqVar,
                        firstEq.solutionSet,
                        otherVar,
                        Constants.Reals,
                    ),
                    explanation = metadata(
                        Explanation.BuildSolutionSameSolutionInOneVariable,
                        firstEq.solutionVariables.firstChild,
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
                        firstEq.solutionVariables.firstChild,
                        firstEqValue,
                        secondEqValue,
                    ),
                )
            }
        }
    }

    private fun TasksBuilder.solveSystemWithOneEquationSolved(
        firstEq: SetSolution,
        secondEq: Expression,
    ): Task? {
        val solveSecondEq = substituteAndSolveIn(secondEq, firstEq)?.result as? SetSolution ?: return null
        val solution = combineSolutions(
            firstEq.solutionVariable,
            firstEq.solutionSet,
            solveSecondEq.solutionVariable,
            solveSecondEq.solutionSet,
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

        return when (eq2Solution) {
            is Contradiction -> {
                task(
                    startExpr = contradictionOf(variableListOf(variables), eq2Solution.secondChild),
                    explanation = metadata(Explanation.BuildSolutionContradiction),
                )
            }
            is Identity -> {
                task(
                    startExpr = implicitSolutionOf(variableListOf(variables), eq1),
                    explanation = metadata(Explanation.BuildSolutionIdentityAndEquation),
                )
            }
            is SetSolution -> {
                val solveEq1 = substituteAndSolveIn(eq1, eq2Solution)?.result as? SetSolution ?: return null

                val solution = combineSolutions(
                    solveEq1.solutionVariable,
                    solveEq1.solutionSet,
                    eq2Solution.solutionVariable,
                    eq2Solution.solutionSet,
                )
                task(
                    startExpr = solution,
                    explanation = metadata(
                        Explanation.BuildSolutionCombineUniqueSolutions,
                        solution.firstChild.children,
                    ),
                )
            }
            else -> null
        }
    }

    private fun TasksBuilder.substituteAndSolveIn(equation: Expression, solution: SetSolution): Task? {
        val solutionAsEquation = solution.asEquation() ?: return null
        val substitutedBackSolution = equation.substituteAllOccurrences(
            solutionAsEquation.firstChild,
            solutionAsEquation.secondChild,
        )
        val var1 = substitutedBackSolution.variables.first()
        return task(
            startExpr = substitutedBackSolution.withoutName(),
            explanation = metadata(
                Explanation.SubstituteAndSolveIn,
                solutionAsEquation,
                xp(var1),
                equation.byName(),
            ),
            stepsProducer = EquationsPlans.SolveEquationInOneVariable,
            context = context.copy(solutionVariables = listOf(var1)),
        )
    }

    protected fun label(i: Int) = "($i)"
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
                apply(EquationsPlans.SolveEquationInOneVariable)
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
        @Suppress("MagicNumber")
        val expressVar1InTermsOfVar2 = task(
            startExpr = eq1.withoutName(),
            explanation = metadata(Explanation.ExpressInTermsOf, xp(var1), xp(var2), eq1.byName()),
            resultLabel = label(3),
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
                startExpr = eq2InTermsOfVar2.withoutName(),
                explanation = metadata(
                    Explanation.SubstituteAndSolveIn,
                    var1InTermsOfVar2.withoutName(),
                    xp(var2),
                    eq2InTermsOfVar2.byName(),
                ),
                stepsProducer = EquationsPlans.SolveEquationInOneVariable,
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

private object SystemSolverByElimination : SystemSolver() {
    override val explanation = Explanation.SolveEquationSystemByElimination

    override val prearrangeLinearEquationSteps = steps {
        whilePossible {
            firstOf {
                option(equationSimplificationSteps)
                option(EquationsPlans.MultiplyByLCDAndSimplify)
                option(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
                option(EquationsPlans.MoveConstantsToTheRightAndSimplify)
                option(EquationsPlans.MoveVariablesToTheLeftAndSimplify)
            }
        }
        optionally {
            check { it.variables.size == 1 }
            inContext(contextFactory = { copy(solutionVariables = it.variables.toList()) }) {
                apply(EquationsPlans.SolveEquationInOneVariable)
            }
        }
    }

    private fun TasksBuilder.multiplyEquation(eq: Expression, factor: Expression): Expression {
        return if (factor != Constants.One) {
            task(
                startExpr = equationOf(productOf(factor, eq.firstChild), productOf(factor, eq.secondChild)),
                explanation = metadata(Explanation.MultiplyEquation, factor, eq.byName()),
                resultLabel = eq.name,
                stepsProducer = PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization,
            )!!.result
        } else {
            eq
        }
    }

    override fun TasksBuilder.solveIndependentEquations(
        firstEq: Expression,
        secondEq: Expression,
        variables: List<String>,
    ): Pair<Expression, Expression>? {
        val (f1, f2, eliminatedVariable, remainingVariable) =
            getEquationFactors(firstEq, secondEq, variables) ?: return null

        val scaledEq1 = multiplyEquation(firstEq, f1)
        val scaledEq2 = multiplyEquation(secondEq, f2)

        val univariateEquation = eliminateVariable(scaledEq1, scaledEq2, eliminatedVariable) ?: return null

        return task(
            startExpr = univariateEquation,
            explanation = metadata(Explanation.SolveEliminatedEquation, univariateEquation.byName()),
            context = context.copy(solutionVariables = listOf(remainingVariable)),
        ) {
            firstOf {
                option(EquationsRules.ExtractSolutionFromConstantEquation)
                option(EquationsPlans.SolveEquationInOneVariable)
            }
        }?.let { solvedUnivariateEquation ->
            val reorganizedFirstEq = if (solvedUnivariateEquation.result is Identity) {
                task(
                    startExpr = firstEq,
                    stepsProducer = rearrangeLinearEquationSteps,
                    explanation = metadata(
                        Explanation.ExpressInTermsOf,
                        xp(variables[0]),
                        xp(variables[1]),
                        firstEq.byName(),
                    ),
                    context = context.copy(solutionVariables = listOf(variables[0])),
                )!!.result
            } else {
                firstEq
            }

            Pair(reorganizedFirstEq, solvedUnivariateEquation.result)
        }
    }

    /**
     * Add or subtract [eq1] and [eq2] to eliminate the variable [variable]
     */
    private fun TasksBuilder.eliminateVariable(eq1: Expression, eq2: Expression, variable: String): Expression? {
        val coeff1 = eq1.coefficientOf(variable)!!
        val coeff2 = eq2.coefficientOf(variable)!!

        return when {
            coeff1 == simplifiedNegOf(coeff2) -> {
                val eqSum = addEquationsOf(eq1, eq2)

                @Suppress("MagicNumber")
                task(
                    startExpr = eqSum,
                    explanation = metadata(Explanation.AddEquations),
                    resultLabel = label(3),
                ) {
                    apply(EquationSystemsRules.RewriteEquationAddition)
                    whilePossible(simplifyEquation)
                }?.result
            }

            coeff1 == coeff2 -> {
                val eqSum = subtractEquationsOf(eq1, eq2)

                @Suppress("MagicNumber")
                task(
                    startExpr = eqSum,
                    explanation = metadata(Explanation.SubtractEquations),
                    resultLabel = label(3),
                ) {
                    apply(EquationSystemsRules.RewriteEquationSubtraction)
                    whilePossible(simplifyEquation)
                }?.result
            }

            else -> null
        }
    }

    private data class EquationFactors(
        val firstFactor: Expression,
        val secondFactor: Expression,
        val eliminatedVariable: String,
        val remainingVariable: String,
    )

    @Suppress("ReturnCount")
    private fun getEquationFactors(
        eq1: Expression,
        eq2: Expression,
        variables: List<String>,
    ): EquationFactors? {
        val x = variables[0]
        val y = variables[1]

        val xCoeffInEq1 = eq1.coefficientOf(x) ?: return null
        val yCoeffInEq1 = eq1.coefficientOf(y) ?: return null
        val xCoeffInEq2 = eq2.coefficientOf(x) ?: return null
        val yCoeffInEq2 = eq2.coefficientOf(y) ?: return null

        // First check if the coefficients are equal or opposite
        when {
            xCoeffInEq1 == xCoeffInEq2 || xCoeffInEq1 == simplifiedNegOf(xCoeffInEq2) ->
                return EquationFactors(Constants.One, Constants.One, x, y)
            yCoeffInEq1 == yCoeffInEq2 || yCoeffInEq1 == simplifiedNegOf(yCoeffInEq2) ->
                return EquationFactors(Constants.One, Constants.One, y, x)
        }

        val coeffVal1 = xCoeffInEq1.asInteger()
        val coeffVal2 = xCoeffInEq2.asInteger()

        if (coeffVal1 != null && coeffVal2 != null) {
            val gcd = coeffVal1.gcd(coeffVal2)
            val factor1 = xp((coeffVal2 / gcd).abs())
            val factor2 = xp((coeffVal1 / gcd).abs())

            return EquationFactors(factor1, factor2, x, y)
        }

        // We're out of ideas, so do something that will work
        return EquationFactors(xCoeffInEq2, xCoeffInEq1, x, y)
    }
}

/**
 * Combines two solutions in one variable each into one solution in two variables.
 */
private fun combineSolutions(var1: String, sol1: Expression, var2: String, sol2: Expression): Expression {
    if (var1 > var2) {
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

private val solveEquationSystemInOneVariable = taskSet {
    val systemPtn = equationSystemOf(
        equationOf(AnyPattern(), AnyPattern()),
        equationOf(AnyPattern(), AnyPattern()),
    )
    explanation = Explanation.SolveEquationSystemInOneVariable

    pattern = condition(systemPtn) { it.variables.size == 1 }

    tasks {

        val system = get(systemPtn)

        val solveFirstEq = task(
            startExpr = get(system.firstChild),
            stepsProducer = EquationsPlans.SolveEquationInOneVariable,
            context = context.copy(solutionVariables = system.variables.toList()),
            explanation = metadata(Explanation.SolveEquationInSystem),
        ) ?: return@tasks null

        if (solveFirstEq.result !is Contradiction) {
            val solveSecondEq = task(
                startExpr = get(system.secondChild),
                stepsProducer = EquationsPlans.SolveEquationInOneVariable,
                context = context.copy(solutionVariables = system.variables.toList()),
                explanation = metadata(Explanation.SolveEquationInSystem),
            ) ?: return@tasks null

            val firstSolution = solveFirstEq.result
            val secondSolution = solveSecondEq.result

            when {
                secondSolution is Contradiction -> task(
                    startExpr = secondSolution,
                    explanation = metadata(Explanation.ComputeOverallSolution),
                )
                secondSolution is Identity -> task(
                    startExpr = solveFirstEq.result,
                    explanation = metadata(Explanation.ComputeOverallSolution),
                )
                firstSolution is Identity -> task(
                    startExpr = secondSolution,
                    explanation = metadata(Explanation.ComputeOverallSolution),
                )
                firstSolution is SetSolution && secondSolution is SetSolution -> {
                    val firstEqSolutions = firstSolution.solutionSet.children
                    val secondEqSolutions = secondSolution.solutionSet.children
                    val commonSolutions = firstEqSolutions.filter { secondEqSolutions.contains(it) }
                    val overallSolution = if (commonSolutions.isEmpty()) {
                        Contradiction(firstSolution.solutionVariables, system)
                    } else {
                        SetSolution(firstSolution.solutionVariables, solutionSetOf(commonSolutions))
                    }
                    task(
                        startExpr = overallSolution,
                        explanation = metadata(Explanation.ComputeOverallSolution),
                    )
                }
                else -> return@tasks null
            }
        }
        allTasks()
    }
}
