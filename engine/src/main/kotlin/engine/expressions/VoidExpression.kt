package engine.expressions

import engine.operators.VoidOperator

/**
 * A VoidExpression means "no expression". It only makes sense as the output of a rule in the specific case when
 * we want to state something about the input expression but it cannot be transformed. E.g.
 *
 * - the expression is fully simplified
 * - the polynomial is irreducible
 */
class VoidExpression(
    meta: NodeMeta = BasicMeta(),
) : Expression(
        operator = VoidOperator,
        operands = emptyList(),
        meta = meta,
    )
