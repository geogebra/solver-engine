package rules

import expressionmakers.*
import patterns.UnsignedIntegerPattern
import patterns.fractionOf
import patterns.sumContaining
import steps.makeMetadata

val addLikeFractions = run {
    val num1 = UnsignedIntegerPattern()
    val num2 = UnsignedIntegerPattern()
    val denom = UnsignedIntegerPattern()
    val f1 = fractionOf(num1, denom)
    val f2 = fractionOf(num2, denom)

    val pattern = sumContaining(f1, f2)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, makeFractionOf(makeSumOf(move(num1), move(num2)), factor(denom))),
        explanationMaker = makeMetadata("add like fractions", move(f1), move(f2)),
    )
}
