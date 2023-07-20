package engine.expressions

import engine.operators.VoidOperator

class VoidExpression(
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = VoidOperator,
    operands = emptyList(),
    meta = meta,
)
