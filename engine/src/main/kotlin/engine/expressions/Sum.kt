package engine.expressions

import engine.conditions.sumTermsAreIncommensurable
import engine.context.Context
import engine.operators.SumOperator
import engine.patterns.RootMatch
import engine.patterns.defaultPolynomialSpecification
import engine.patterns.monomialPattern
import engine.sign.Sign
import java.math.BigInteger

class Sum(
    terms: List<Expression>,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
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
fun leadingCoefficientOfPolynomial(context: Context, polynomialExpr: Sum): Expression? {
    val spec = defaultPolynomialSpecification(context, polynomialExpr) ?: return null
    val monomialPattern = monomialPattern(spec)

    var degree = BigInteger.ZERO
    var leadingCoefficient: Expression? = null
    for (term in polynomialExpr.terms) {
        if (!spec.isConstant(context, term)) {
            // If it isn't a monomial, `polynomialExpr` isn't a polynomial or a polynomial not expanded
            val monomialMatch = monomialPattern.findMatches(context, RootMatch, term).firstOrNull() ?: return null
            val monomialDegree = monomialPattern.exponent.getBoundInt(monomialMatch)
            when {
                monomialDegree > degree -> {
                    leadingCoefficient = monomialPattern.coefficient(monomialMatch)
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
