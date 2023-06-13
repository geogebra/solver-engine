package engine.expressions

import engine.conditions.sumTermsAreIncommensurable
import engine.context.emptyContext
import engine.operators.SumOperator
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.RootMatch
import engine.patterns.monomialPattern
import engine.sign.Sign
import java.math.BigInteger

class Sum(
    terms: List<Expression>,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = SumOperator,
    operands = terms,
    meta,
) {
    val terms get() = children

    override fun signOf(): Sign {
        val signBasedOnOperandSigns = operands.map { it.signOf() }.reduce(Sign::plus)
        return if (signBasedOnOperandSigns == Sign.UNKNOWN && sumTermsAreIncommensurable(operands)) {
            Sign.NOT_ZERO
        } else {
            signBasedOnOperandSigns
        }
    }
}

fun areEquivalentSums(expr1: Expression, expr2: Expression): Boolean {
    if (expr1 == expr2) {
        return true
    }
    if (expr1 !is Sum || expr2 !is Sum || expr1.childCount != expr2.childCount) {
        return false
    }

    val remainingChildren = expr2.children.toMutableList()
    return expr1.children.all { remainingChildren.remove(it) }
}

@Suppress("ReturnCount")
fun leadingCoefficientOfPolynomial(polynomialExpr: Sum): Expression? {
    val variables = polynomialExpr.variables
    if (variables.size != 1) return null

    val monomial = monomialPattern(ArbitraryVariablePattern())
    var degree = BigInteger.ZERO
    var leadingCoefficient: Expression? = null
    for (term in polynomialExpr.terms) {
        if (!term.isConstant()) {
            // If it isn't a monomial, `polynomialExpr` isn't a polynomial or a polynomial not expanded
            val monomialMatch = monomial.findMatches(emptyContext, RootMatch, term).firstOrNull() ?: return null
            val monomialDegree = monomial.exponent.getBoundInt(monomialMatch)
            when {
                monomialDegree > degree -> {
                    leadingCoefficient = monomial.coefficient(monomialMatch)
                    degree = monomialDegree
                }
                monomialDegree == degree -> {
                    // The polynomial is not normalised
                    return null
                }
            }
        }
    }
    return leadingCoefficient
}
