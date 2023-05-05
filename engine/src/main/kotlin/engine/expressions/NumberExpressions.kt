package engine.expressions

import engine.operators.DecimalOperator
import engine.operators.IntegerOperator
import engine.operators.MixedNumberOperator
import engine.operators.RecurringDecimalOperator
import engine.utility.RecurringDecimal
import java.math.BigDecimal
import java.math.BigInteger

class IntegerExpression(
    val value: BigInteger,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = IntegerOperator(value),
    operands = emptyList(),
    meta,
)

class DecimalExpression(
    val value: BigDecimal,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = DecimalOperator(value),
    operands = emptyList(),
    meta,
)

class RecurringDecimalExpression(
    val value: RecurringDecimal,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = RecurringDecimalOperator(value),
    operands = emptyList(),
    meta,
)

class MixedNumberExpression(
    val integerPart: IntegerExpression,
    val numerator: IntegerExpression,
    val denominator: IntegerExpression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = MixedNumberOperator,
    operands = listOf(integerPart, numerator, denominator),
    meta,
)
