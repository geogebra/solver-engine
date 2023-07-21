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

fun computeOverallIntersectionSolution(solutions: List<Expression>): Expression? {
    val solutionSets = solutions.mapNotNull { solution ->
        when (solution) {
            // If one of solutions is a contradiction, then the overall solution is also a contradiction
            is Contradiction -> return solution
            // Identities are irrelevant in an intersection
            is Identity -> null
            // Select SetSolutions
            is SetSolution -> solution.solutionSet
            // Otherwise we cannot merge the solutions
            else -> return null
        }
    }

    return computeIntersectionOfSets(solutionSets)?.let {
        setSolutionOf(solutions[0].firstChild as VariableList, it)
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

fun computeIntersectionOfSets(sets: List<SetExpression>): Expression? {
    return when (sets.size) {
        0 -> Constants.Reals
        else -> sets.reduce { acc, set ->
            acc.intersect(set, expressionComparator) ?: return null
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
