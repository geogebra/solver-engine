package methods.equations

import engine.conditions.signOf
import engine.context.emptyContext
import engine.expressions.Constants
import engine.expressions.Contradiction
import engine.expressions.Expression
import engine.expressions.ExpressionComparator
import engine.expressions.FiniteSet
import engine.expressions.Identity
import engine.expressions.Interval
import engine.expressions.Root
import engine.expressions.SetSolution
import engine.expressions.VariableList
import engine.expressions.negOf
import engine.expressions.setSolutionOf
import engine.expressions.solutionSetOf
import engine.expressions.sumOf
import engine.sign.Sign
import methods.constantexpressions.ConstantExpressionsPlans

fun computeOverallSolution(solutions: List<Expression>): Expression? {
    // If one of the equations results in an identity, then the overall solution is also an identity
    val identity = solutions.firstOrNull { it is Identity }
    if (identity != null) {
        return identity
    }

    val (singleSolutions, intervals) = separateSingleSolutionsAndIntervals(solutions) ?: return null

    return if (intervals.size > 1) {
        // we cannot handle multiple intervals yet
        null
    } else if (intervals.size == 1) {
        // try to merge the single solutions into the interval
        mergeSolutionsIntoInterval(singleSolutions, intervals[0])?.let {
            setSolutionOf(
                solutions[0].firstChild as VariableList,
                it,
            )
        }
    } else {
        setSolutionOf(
            solutions[0].firstChild as VariableList,
            solutionSetOf(singleSolutions.sortedBy { it.doubleValue }),
        )
    }
}

fun mergeSolutionsIntoInterval(singleSolutions: Set<Expression>, interval: Interval): Interval? {
    var currentInterval = interval

    for (solution in singleSolutions) {
        if (currentInterval.leftBound == solution) {
            currentInterval = currentInterval.leftClosed()
        } else if (currentInterval.rightBound == solution) {
            currentInterval = currentInterval.rightClosed()
        } else if (currentInterval.contains(solution, expressionComparator) != true) {
            return null
        }
    }

    return currentInterval
}

private fun separateSingleSolutionsAndIntervals(solutions: List<Expression>): Pair<Set<Expression>, List<Interval>>? {
    val singleSolutions = mutableSetOf<Expression>()
    val intervals = mutableListOf<Interval>()

    for (solution in solutions) {
        when (solution) {
            is SetSolution -> when (val solutionSet = solution.solutionSet) {
                is FiniteSet -> singleSolutions.addAll(solutionSet.elements)
                is Interval -> intervals.add(solutionSet)
                else -> return null
            }
            !is Contradiction -> return null
        }
    }

    return Pair(singleSolutions, intervals)
}

val expressionComparator = ExpressionComparator { e1: Expression, e2: Expression ->
    when {
        e1 == Constants.Infinity -> Sign.POSITIVE
        e1 == Constants.NegativeInfinity -> Sign.NEGATIVE
        e2 == Constants.NegativeInfinity -> Sign.POSITIVE
        e2 == Constants.Infinity -> Sign.NEGATIVE
        else -> {
            val diff = sumOf(e1, negOf(e2)).withOrigin(Root())
            val result = ConstantExpressionsPlans.SimplifyConstantExpression.tryExecute(emptyContext, diff)
                ?: return@ExpressionComparator Sign.UNKNOWN
            val simplifiedDiff = result.toExpr
            val signOfDiff = simplifiedDiff.signOf()
            if (signOfDiff != Sign.UNKNOWN) {
                signOfDiff
            } else {
                val d = simplifiedDiff.doubleValue
                when {
                    d > 0 -> Sign.POSITIVE
                    d < 0 -> Sign.NEGATIVE
                    d.isNaN() -> Sign.NONE
                    else -> Sign.UNKNOWN
                }
            }
        }
    }
}
