package engine.expressions

import engine.conditions.sumTermsAreIncommensurable
import engine.operators.SumOperator
import engine.sign.Sign

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
