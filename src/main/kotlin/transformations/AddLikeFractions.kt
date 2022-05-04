package transformations

import expressions.Expression
import expressions.fractionOf
import expressions.sumOf
import patterns.IntegerPattern
import patterns.Match
import patterns.fractionOf
import patterns.sumOf

object AddLikeFractions : Rule {
    val num1 = IntegerPattern()
    val num2 = IntegerPattern()
    val denom = IntegerPattern()
    val f1 = fractionOf(num1, denom)
    val f2 = fractionOf(num2, denom)
    val sum = sumOf(f1, f2)

    override val pattern = sum

    override fun apply(match: Match): Expression {
        val num1Val = match.getBinding(num1)!!.expr
        val num2Val = match.getBinding(num2)!!.expr
        val denomVal = match.getBinding(denom)!!.expr

        return fractionOf(sumOf(num1Val, num2Val), denomVal)
    }
}
