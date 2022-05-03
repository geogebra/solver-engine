package transformations

import expressions.Expression
import expressions.sumOf
import patterns.FractionPattern
import patterns.IntegerPattern
import patterns.Match
import patterns.acSumPatternOf

object AddLikeFractions : Rule {
    val num1 = IntegerPattern()
    val num2 = IntegerPattern()
    val denom = IntegerPattern()
    val f1 = FractionPattern(num1, denom)
    val f2 = FractionPattern(num2, denom)
    val sum = acSumPatternOf(f1, f2)

    override val pattern = sum

    override fun apply(match: Match): Expression? {
        return sumOf()
    }
}