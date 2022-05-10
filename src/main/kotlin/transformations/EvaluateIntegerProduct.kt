package transformations

import expressionmakers.cancel
import expressionmakers.makeNumericOp
import expressionmakers.restOf
import expressionmakers.substituteIn
import expressions.IntegerExpr
import patterns.FixedPattern
import patterns.IntegerPattern
import patterns.productContaining

object EvaluateIntegerProduct : Rule {
    private val factor1 = IntegerPattern()
    private val factor2 = IntegerPattern()

    override val pattern = productContaining(factor1, factor2)

    override val resultMaker = substituteIn(pattern, makeNumericOp(factor1, factor2) { n1, n2 -> n1 * n2 })
}

object EliminateOneInProduct : Rule {
    private val one = FixedPattern(IntegerExpr(1))

    override val pattern = productContaining(one)

    override val resultMaker = cancel(one, restOf(pattern))
}
