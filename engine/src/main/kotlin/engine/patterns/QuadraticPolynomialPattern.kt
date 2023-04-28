package engine.patterns

import engine.expressions.Constants
import engine.expressions.Expression

class QuadraticPolynomialPattern(val variable: Pattern) : KeyedPattern {

    private fun NaryPattern.restIsConstant(match: Match) =
        getRestSubexpressions(match).all { rest -> rest.isConstant() }

    private val quadraticTerm = withOptionalConstantCoefficient(powerOf(variable, FixedPattern(Constants.Two)))

    private val linearTerm = withOptionalConstantCoefficient(variable)

    private val completeQuadraticPolynomial = commutativeSumContaining(quadraticTerm, linearTerm)

    private val incompleteQuadraticPolynomial = commutativeSumContaining(quadraticTerm)

    private val quadraticPolynomial = oneOf(
        ConditionPattern(completeQuadraticPolynomial) { _, match ->
            completeQuadraticPolynomial.restIsConstant(match)
        },
        ConditionPattern(incompleteQuadraticPolynomial) { _, match ->
            incompleteQuadraticPolynomial.restIsConstant(match)
        },
    )

    override val key = quadraticPolynomial

    fun quadraticCoefficient(match: Match) = quadraticTerm.coefficient(match)

    fun linearCoefficient(match: Match): Expression {
        return when {
            linearTerm.getBoundExpr(match) != null -> linearTerm.coefficient(match)
            else -> Constants.Zero
        }
    }

    fun constantTerm(match: Match): Expression {
        return when {
            completeQuadraticPolynomial.getBoundExpr(match) != null ->
                completeQuadraticPolynomial.substitute(match, arrayOf())
            else -> incompleteQuadraticPolynomial.substitute(match, arrayOf())
        }
    }
}
