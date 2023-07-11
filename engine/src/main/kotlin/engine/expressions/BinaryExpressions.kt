package engine.expressions

import engine.operators.BinaryExpressionOperator
import engine.sign.Sign
import engine.utility.isEven

class Fraction(
    numerator: Expression,
    denominator: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = BinaryExpressionOperator.Fraction,
    operands = listOf(numerator, denominator),
    meta,
) {
    val numerator get() = firstChild
    val denominator get() = secondChild

    override fun signOf() = numerator.signOf() / denominator.signOf()
}

class Power(
    base: Expression,
    exponent: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = BinaryExpressionOperator.Power,
    operands = listOf(base, exponent),
    meta,
) {
    val base get() = firstChild
    val exponent get() = secondChild

    override fun signOf() = when (val sign = base.signOf()) {
        Sign.POSITIVE, Sign.NON_NEGATIVE -> sign
        Sign.ZERO -> if (exponent.signOf() == Sign.POSITIVE) Sign.ZERO else Sign.NONE
        Sign.NEGATIVE, Sign.NON_POSITIVE, Sign.UNKNOWN, Sign.NOT_ZERO -> {
            val intExp = exponent.asInteger()
            when {
                intExp == null || sign.canBeZero && intExp.signum() <= 0 -> Sign.NONE
                intExp.isEven() -> Sign.POSITIVE.orMaybeZero(sign.canBeZero)
                else -> sign
            }
        }
        Sign.NONE -> Sign.NONE
    }
}

class Root(
    radicand: Expression,
    index: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = BinaryExpressionOperator.Root,
    operands = listOf(radicand, index),
    meta,
) {
    val radicand get() = firstChild
    val index get() = secondChild

    override fun signOf(): Sign {
        // This is not quite right because we should check the order as well.
        return radicand.signOf().truncateToPositive()
    }
}

class PercentageOf(
    part: Expression,
    base: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = BinaryExpressionOperator.PercentageOf,
    operands = listOf(part, base),
    meta = meta,
) {
    val part get() = firstChild
    val base get() = secondChild

    override fun signOf() = part.signOf() * base.signOf()
}
