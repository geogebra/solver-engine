package engine.patterns

import engine.context.Context
import engine.expressions.ConstantChecker
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.defaultConstantChecker

class QuadraticPolynomialPattern(
    val variable: Pattern,
    constantChecker: ConstantChecker = defaultConstantChecker,
) : KeyedPattern, ConstantChecker by constantChecker {

    private fun NaryPattern.restIsConstant(context: Context, match: Match) =
        getRestSubexpressions(match).all { rest -> isConstant(context, rest) }

    private val quadraticTerm = withOptionalConstantCoefficient(
        powerOf(variable, FixedPattern(Constants.Two)),
        constantChecker,
    )

    private val linearTerm = withOptionalConstantCoefficient(variable, constantChecker)

    private val completeQuadraticPolynomial = commutativeSumContaining(quadraticTerm, linearTerm)

    private val incompleteQuadraticPolynomial = commutativeSumContaining(quadraticTerm)

    private val quadraticPolynomial = oneOf(
        ConditionPattern(completeQuadraticPolynomial) { context, match, _ ->
            completeQuadraticPolynomial.restIsConstant(context, match)
        },
        ConditionPattern(incompleteQuadraticPolynomial) { context, match, _ ->
            incompleteQuadraticPolynomial.restIsConstant(context, match)
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
