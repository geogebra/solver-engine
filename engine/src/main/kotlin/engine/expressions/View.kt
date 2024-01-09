/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

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
    private enum class TermSign(val of: (Expression) -> Expression) {
        PLUS({ it }),
        MINUS({ negOf(it) }),
        PLUSMINUS({ plusMinusOf(it) }),
    }

    private val termSign: TermSign
    val factors: List<T>
    val denominatorFactors: List<T>

    init {
        val positiveTerm = when (original) {
            is Minus -> {
                termSign = TermSign.MINUS
                original.argument
            }
            is PlusMinus -> {
                termSign = TermSign.PLUSMINUS
                original.argument
            }
            else -> {
                termSign = TermSign.PLUS
                original
            }
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
        return termSign.of(fraction)
    }
}

class SumView<T : View>(override val original: Expression, factorViewCreator: (Expression) -> T) : View {
    private fun originalTerms() = if (original is Sum) original.terms else listOf(original)

    val termViews = originalTerms().map { TermView(it) { factor -> factorViewCreator(factor) } }

    override fun recombine() = sumOf(termViews.map { it.recombine() })
}

class IntegerFactorView(override val original: IntegerExpression) : IntegerView {
    private var newValue: BigInteger? = null

    override val value get() = newValue ?: original.value

    override fun changeValue(newValue: BigInteger) {
        this.newValue = newValue
    }

    override fun recombine(): Expression? {
        return when {
            newValue == null -> original
            newValue == BigInteger.ONE -> null
            else -> xp(newValue!!).withOrigin(Combine(listOf(original)))
        }
    }
}
