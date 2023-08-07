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

class CancellableView(override val original: Expression) : View {
    private var cancelled = false

    fun cancel() {
        cancelled = true
    }

    override fun recombine() = if (cancelled) null else original.withOrigin(Move(original))
}

class TermView<T : View>(override val original: Expression, factorViewCreator: (Expression) -> T) : View {

    val negated: Boolean
    val factors: List<T>
    val denominatorFactors: List<T>

    init {
        val positiveTerm = if (original is Minus) {
            negated = true
            original.argument
        } else {
            negated = false
            original
        }

        if (positiveTerm is Fraction) {
            factors = positiveTerm.numerator.factors().map { factorViewCreator(it) }
            denominatorFactors = positiveTerm.denominator.factors().map { factorViewCreator(it) }
        } else {
            factors = positiveTerm.factors().map { factorViewCreator(it) }
            denominatorFactors = emptyList()
        }
    }

    inline fun <reified Q : View> findSingleFactor() = factors.singleOrNull { it is Q } as Q?

    inline fun findSingleFactor(condition: (T) -> Boolean) = factors.singleOrNull(condition)

    override fun recombine(): Expression {
        val numerator = productOf(factors.mapNotNull { it.recombine() })
        val denominator = productOf(denominatorFactors.mapNotNull { it.recombine() })
        val fraction = simplifiedFractionOf(numerator, denominator)
        return if (negated) negOf(fraction) else fraction
    }
}

class SumView<T : View>(override val original: Sum, factorViewCreator: (Expression) -> T) : View {

    val termViews = original.terms.map { TermView(it) { factor -> factorViewCreator(factor) } }

    override fun recombine() = sumOf(termViews.map { it.recombine() })
}
