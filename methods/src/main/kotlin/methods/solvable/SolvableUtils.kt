package methods.solvable

import engine.context.emptyContext
import engine.expressions.Constants
import engine.expressions.Contradiction
import engine.expressions.Expression
import engine.expressions.ExpressionComparator
import engine.expressions.Identity
import engine.expressions.RootOrigin
import engine.expressions.SetExpression
import engine.expressions.SetSolution
import engine.expressions.VariableList
import engine.expressions.negOf
import engine.expressions.setSolutionOf
import engine.expressions.statementSystemOf
import engine.expressions.sumOf
import engine.sign.Sign
import methods.constantexpressions.ConstantExpressionsPlans

fun computeOverallUnionSolution(solutions: List<Expression>): Expression? {
    val solutionSets = solutions.mapNotNull { solution ->
        when (solution) {
            // If one of solutions is an identity, then the overall solution is also an identity
            is Identity -> return solution
            // Contradictions are irrelevant in a union
            is Contradiction -> null
            // Select SetSolutions
            is SetSolution -> solution.solutionSet
            // Otherwise we cannot merge the solutions
            else -> return null
        }
    }

    return computeUnionOfSets(solutionSets)?.let {
        setSolutionOf(solutions[0].firstChild as VariableList, it)
    }
}

fun computeOverallIntersectionSolution(solutions: List<Expression>): Expression {
    val processedSolutions = mutableListOf(solutions[0])

    for (solution in solutions.drop(1)) {
        var intersected = false
        for ((index, processedSolution) in processedSolutions.withIndex()) {
            val intersectedSolution = intersect(processedSolution, solution)
            if (intersectedSolution != null) {
                processedSolutions[index] = intersectedSolution
                intersected = true
                break
            }
        }

        if (!intersected) {
            processedSolutions.add(solution)
        }
    }

    return if (processedSolutions.size == 1) {
        processedSolutions[0]
    } else {
        statementSystemOf(processedSolutions)
    }
}

fun intersect(solution1: Expression, solution2: Expression): Expression? {
    return when {
        solution1 is Contradiction -> solution1
        solution2 is Contradiction -> solution2
        solution1 is Identity -> solution2
        solution2 is Identity -> solution1
        solution1 is SetSolution && solution2 is SetSolution &&
            solution1.solutionVariables == solution2.solutionVariables ->
            solution1.solutionSet.intersect(solution2.solutionSet, expressionComparator)?.let {
                setSolutionOf(solution1.solutionVariables, it)
            }
        else -> null
    }
}

fun computeUnionOfSets(sets: List<SetExpression>): Expression? {
    return when (sets.size) {
        0 -> Constants.EmptySet
        else -> sets.reduce { acc, set ->
            acc.union(set, expressionComparator) ?: return null
        }
    }
}

val expressionComparator = ExpressionComparator { e1: Expression, e2: Expression ->
    when {
        e1 == Constants.Infinity -> Sign.POSITIVE
        e1 == Constants.NegativeInfinity -> Sign.NEGATIVE
        e2 == Constants.NegativeInfinity -> Sign.POSITIVE
        e2 == Constants.Infinity -> Sign.NEGATIVE
        else -> {
            val diff = sumOf(e1, negOf(e2)).withOrigin(RootOrigin())
            val result = ConstantExpressionsPlans.SimplifyConstantExpression.tryExecute(emptyContext, diff)
            val simplifiedDiff = result?.toExpr ?: diff
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
