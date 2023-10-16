package engine.patterns

import engine.context.Context
import engine.expressions.ConstantChecker
import engine.expressions.Expression
import engine.expressions.defaultConstantChecker
import engine.expressions.solutionVariableConstantChecker

/**
 * This encapsulate the data needed to check whether an expression is a polynomial.  The [variable] pattern
 * can detect instances of the variable and the [constantChecker] can verify that other parts are constant.
 */
class PolynomialSpecification(
    val variable: Pattern,
    val constantChecker: ConstantChecker,
) : ConstantChecker by constantChecker

/**
 * Returns the most appropriate [PolynomialSpecification] for the given [expression] in the given [context].
 */
fun defaultPolynomialSpecification(context: Context, expression: Expression): PolynomialSpecification? {
    return when {
        context.solutionVariables.size == 1 -> PolynomialSpecification(
            variable = SolutionVariablePattern(),
            constantChecker = solutionVariableConstantChecker,
        )
        expression.variables.size == 1 -> PolynomialSpecification(
            variable = ArbitraryVariablePattern(),
            constantChecker = defaultConstantChecker,
        )
        else -> null
    }
}
