package methods.fractionroots

import engine.expressionmakers.makeFractionOf
import engine.expressionmakers.makeNumericOp
import engine.expressionmakers.makeProductOf
import engine.expressionmakers.makeRootOf
import engine.expressionmakers.makeSimplifiedPowerOf
import engine.expressionmakers.makeSquareRootOf
import engine.expressionmakers.move
import engine.methods.Rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.fractionOf
import engine.patterns.integerOrderRootOf
import engine.patterns.numericCondition
import engine.patterns.productContaining
import engine.patterns.squareRootOf
import engine.steps.metadata.makeMetadata
import engine.utility.divides

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

val simplifyFractionOfRootsWithSameOrder = run {
    val radicand1 = SignedIntegerPattern()
    val radicand2 = SignedIntegerPattern()

    val numerator = integerOrderRootOf(radicand1)
    val denominator = integerOrderRootOf(radicand2)

    val fraction = ConditionPattern(
        fractionOf(numerator, denominator),
        numericCondition(radicand1, radicand2) { n1, n2 -> n2.divides(n1) },
    )

    Rule(
        pattern = ConditionPattern(
            fraction,
            numericCondition(numerator.order, denominator.order) { n1, n2 -> n1 == n2 }
        ),
        resultMaker = makeRootOf(
            makeFractionOf(move(radicand1), move(radicand2)),
            move(numerator.order)
        ),
        explanationMaker = makeMetadata(Explanation.SimplifyFractionOfRoots)
    )
}

/**
 * []root[a, p] / root[b, q]] -> [root[a ^ m / p, m] / root[b ^ m / q, m]]
 * where m = lcm(p, q)
 */
val bringRootsToSameIndexInFraction = run {
    val leftRadicand = UnsignedIntegerPattern()
    val leftRoot = integerOrderRootOf(leftRadicand)
    val rightRadicand = UnsignedIntegerPattern()
    val rightRoot = integerOrderRootOf(rightRadicand)
    val product = fractionOf(leftRoot, rightRoot)

    Rule(
        pattern = ConditionPattern(
            product,
            numericCondition(leftRoot.order, rightRoot.order) { n1, n2 -> n1 != n2 }
        ),
        resultMaker = makeFractionOf(
            makeRootOf(
                makeSimplifiedPowerOf(
                    move(leftRadicand),
                    makeNumericOp(leftRoot.order, rightRoot.order) { n1, n2 -> n2 / n1.gcd(n2) }
                ),
                makeNumericOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 * n2 / n1.gcd(n2) }
            ),
            makeRootOf(
                makeSimplifiedPowerOf(
                    move(rightRadicand),
                    makeNumericOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 / n1.gcd(n2) }
                ),
                makeNumericOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 * n2 / n1.gcd(n2) }
            ),
        ),
        explanationMaker = makeMetadata(Explanation.BringRootsToSameIndexInFraction),
    )
}
