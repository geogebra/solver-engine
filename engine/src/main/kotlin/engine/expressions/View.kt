package engine.expressions

import java.math.BigInteger

// Views are an experimental construct used to provide a simple interface for accessing
// and rewriting expression. They are generally _mutable_ classes.

interface View {
    val original: Expression
    fun recombine(): Expression?
}

interface IntegerView : View {
    val value: BigInteger
    fun changeValue(newValue: BigInteger)
}

class DefaultView(override val original: Expression) : View {
    override fun recombine() = original
}

class TermView<T : View>(override val original: Expression, factorViewCreator: (Expression) -> T) : View {

    val negated: Boolean
    val factors: List<T>

    init {
        if (original is Minus) {
            negated = true
            factors = original.argument.factors().map { factorViewCreator(it) }
        } else {
            negated = false
            factors = original.factors().map { factorViewCreator(it) }
        }
    }

    inline fun <reified Q : View> findSingleFactor() = factors.singleOrNull { it is Q } as Q?

    inline fun findSingleFactor(condition: (T) -> Boolean) = factors.singleOrNull(condition)

    override fun recombine(): Expression {
        val product = productOf(factors.mapNotNull { it.recombine() })
        return if (negated) negOf(product) else product
    }
}

class SumView<T : View>(override val original: Sum, factorViewCreator: (Expression) -> T) : View {

    val termViews = original.terms.map { TermView(it) { factor -> factorViewCreator(factor) } }

    override fun recombine() = sumOf(termViews.map { it.recombine() })
}
