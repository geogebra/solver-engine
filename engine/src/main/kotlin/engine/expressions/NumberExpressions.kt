package engine.expressions

import engine.operators.DecimalOperator
import engine.operators.IntegerOperator
import engine.operators.MixedNumberOperator
import engine.operators.RecurringDecimalOperator
import engine.sign.Sign
import engine.utility.RecurringDecimal
import java.math.BigDecimal
import java.math.BigInteger

class IntegerExpression(
    val value: BigInteger,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
    operator = IntegerOperator(value),
    operands = emptyList(),
    meta,
) {
    override fun signOf() = Sign.fromInt(value.signum())

    override fun fillJson2(s: MutableMap<String, Any>) {
        s["type"] = "Integer"
        s["value"] = value.toString()
    }
}

class DecimalExpression(
    val value: BigDecimal,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
    operator = DecimalOperator(value),
    operands = emptyList(),
    meta,
) {
    override fun signOf() = Sign.fromInt(value.signum())

    override fun fillJson2(s: MutableMap<String, Any>) {
        s["type"] = "Decimal"
        s["value"] = value.toString()
    }
}

class RecurringDecimalExpression(
    val value: RecurringDecimal,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
    operator = RecurringDecimalOperator(value),
    operands = emptyList(),
    meta,
) {
    override fun signOf() = Sign.POSITIVE // If it was 0, it would not be recurring

    override fun fillJson2(s: MutableMap<String, Any>) {
        s["type"] = "RecurringDecimal"
        s["value"] = value.toString()
    }
}

class MixedNumberExpression(
    val integerPart: IntegerExpression,
    val numerator: IntegerExpression,
    val denominator: IntegerExpression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
    operator = MixedNumberOperator,
    operands = listOf(integerPart, numerator, denominator),
    meta,
) {
    override fun signOf() = Sign.POSITIVE // If it was 0, it would not be a mixed number
}
