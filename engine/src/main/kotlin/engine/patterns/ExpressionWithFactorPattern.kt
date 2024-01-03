package engine.patterns

import engine.expressions.Expression
import engine.expressions.negOf
import engine.expressions.productOf

class ExpressionWithFactorPattern(val factor: Pattern, val positiveOnly: Boolean = false) :
    KeyedPattern, SubstitutablePattern {
    private val productWithFactor = productContaining(factor)
    private val options = oneOf(factor, productWithFactor)
    private val optionalNegPattern = optionalNegOf(options)

    override val key = if (positiveOnly) options.key else optionalNegPattern.key

    override fun substitute(match: Match, newVals: Array<out Expression>): Expression {
        val substituted = if (match.isBound(productWithFactor)) {
            productWithFactor.substitute(match, newVals)
        } else {
            productOf(newVals.toList())
        }

        return if (positiveOnly || !optionalNegPattern.isNeg(match)) substituted else negOf(substituted)
    }
}

fun expressionWithFactor(factor: Pattern, positiveOnly: Boolean = false) =
    ExpressionWithFactorPattern(
        factor,
        positiveOnly,
    )
