package methods.fractionroots

import engine.expressionmakers.makeFractionOf
import engine.expressionmakers.makeProductOf
import engine.expressionmakers.makeSquareRootOf
import engine.expressionmakers.move
import engine.methods.Rule
import engine.patterns.AnyPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.fractionOf
import engine.patterns.productContaining
import engine.patterns.squareRootOf
import engine.steps.metadata.makeMetadata

/*
[4 / 2 * sqrt[3]] --> [4 / 2 * sqrt[3]] * [sqrt[3] / sqrt[3]]
 */
val writeAsMultiplicationWithUnitaryRadicalFraction = run {
    val numerator = AnyPattern()
    val radical = squareRootOf(UnsignedIntegerPattern())
    val denominator = productContaining(radical)
    val pattern = fractionOf(numerator, denominator)

    Rule(
        pattern = pattern,
        resultMaker = makeProductOf(
            makeFractionOf(move(numerator), move(denominator)),
            makeFractionOf(move(radical), move(radical))
        ),
        explanationMaker = makeMetadata(Explanation.WriteAsMultiplicationWithUnitaryRadicalFraction)
    )
}

/*
converts sqrt[ [2 / 3] ] --> [ sqrt[2] / sqrt[3] ]
 */
val distributeRadicalRuleOverFractionsToNumeratorAndDenominator = run {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val fraction = fractionOf(numerator, denominator)

    val pattern = squareRootOf(fraction)

    Rule(
        pattern = pattern,
        resultMaker = makeFractionOf(
            makeSquareRootOf(move(numerator)),
            makeSquareRootOf(move(denominator))
        ),
        explanationMaker = makeMetadata(Explanation.DistributeRadicalRuleOverFractionsToNumeratorAndDenominator)
    )
}
