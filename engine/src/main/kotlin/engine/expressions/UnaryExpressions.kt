package engine.expressions

import engine.operators.TrigonometricFunctionOperator
import engine.operators.TrigonometricFunctionType
import engine.operators.UnaryExpressionOperator
import engine.sign.Sign

class Minus(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
    operator = UnaryExpressionOperator.Minus,
    operands = listOf(argument),
    meta,
) {
    val argument get() = firstChild

    override fun signOf() = -argument.signOf()
}

class Plus(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
    operator = UnaryExpressionOperator.Plus,
    operands = listOf(argument),
    meta,
) {
    val argument get() = firstChild

    override fun signOf() = argument.signOf()
}

class PlusMinus(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
    operator = UnaryExpressionOperator.PlusMinus,
    operands = listOf(argument),
    meta,
) {
    val argument get() = firstChild

    override fun signOf() = when (argument.signOf()) {
        Sign.POSITIVE, Sign.NEGATIVE, Sign.NOT_ZERO -> Sign.NOT_ZERO
        Sign.NONE -> Sign.NONE
        else -> Sign.UNKNOWN
    }
}

class AbsoluteValue(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
    operator = UnaryExpressionOperator.AbsoluteValue,
    operands = listOf(argument),
    meta,
) {
    val argument get() = firstChild

    override fun signOf() = when (val sign = argument.signOf()) {
        Sign.NONE, Sign.ZERO -> sign
        else -> Sign.POSITIVE.orMaybeZero(sign.canBeZero)
    }
}

class SquareRoot(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
    operator = UnaryExpressionOperator.SquareRoot,
    operands = listOf(argument),
    meta,
) {
    val argument get() = firstChild

    override fun signOf() = argument.signOf().truncateToPositive()
}

class Percentage(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
    operator = UnaryExpressionOperator.Percentage,
    operands = listOf(argument),
    meta = meta,
) {
    val argument get() = firstChild

    override fun signOf() = argument.signOf()
}

class TrigonometricExpression(
    private val functionType: TrigonometricFunctionType,
    operand: Expression,
    val powerInside: Boolean,
    private val inverseNotation: String,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
    operator = TrigonometricFunctionOperator(functionType, powerInside, inverseNotation),
    operands = listOf(operand),
    meta = meta,
) {
    val argument get() = firstChild

    override fun fillJson(s: MutableMap<String, Any>) {
        s["type"] = functionType.name
        s["operands"] = operands.map { it.toJson() }
        s["inverseNotation"] = inverseNotation
        s["powerNotation"] = if (powerInside) "inside" else "outside"
    }
}
