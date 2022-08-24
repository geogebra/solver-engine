package methods.fractionroots

import engine.expressionmakers.FixedExpressionMaker
import engine.expressionmakers.copyFlippedSign
import engine.expressionmakers.copySign
import engine.expressionmakers.makeBracketOf
import engine.expressionmakers.makeFractionOf
import engine.expressionmakers.makeNegOf
import engine.expressionmakers.makeNumericOp
import engine.expressionmakers.makePowerOf
import engine.expressionmakers.makeProductOf
import engine.expressionmakers.makeRootOf
import engine.expressionmakers.makeSimplifiedPowerOf
import engine.expressionmakers.makeSquareRootOf
import engine.expressionmakers.makeSumOf
import engine.expressionmakers.move
import engine.expressionmakers.substituteIn
import engine.expressions.Constants.Three
import engine.expressions.xp
import engine.methods.Rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.bracketOf
import engine.patterns.commutativeSumOf
import engine.patterns.custom
import engine.patterns.fractionOf
import engine.patterns.integerOrderRootOf
import engine.patterns.invisibleBracketOf
import engine.patterns.negOf
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.oppositeSignPattern
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.rootOf
import engine.patterns.squareRootOf
import engine.patterns.sumOf
import engine.steps.metadata.makeMetadata
import engine.utility.divides

/**
 * E.g: sqrt[[2 / 3]] -> [sqrt[2] / sqrt[3]]
 */
val distributeRadicalOverFraction = run {
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
        explanationMaker = makeMetadata(Explanation.DistributeRadicalOverFraction)
    )
}

/**
 * E.g: [4 / sqrt[3]] -> [4 * sqrt[3] / sqrt[3] * sqrt[3]]
 */
val rationalizeSimpleDenominator = run {
    val numerator = AnyPattern()
    val radical = squareRootOf(UnsignedIntegerPattern())
    val pattern = fractionOf(numerator, radical)

    Rule(
        pattern = pattern,
        resultMaker = makeFractionOf(
            makeProductOf(move(numerator), move(radical)),
            makeProductOf(move(radical), move(radical))
        ),
        explanationMaker = makeMetadata(Explanation.RationalizeSimpleDenominator)
    )
}

/**
 * E.g: [4 / 2 * sqrt[3]] -> [4 * sqrt[3] / 2 * sqrt[3] * sqrt[3]]
 */
val rationalizeSimpleDenominatorWithCoefficient = run {
    val numerator = AnyPattern()
    val radical = squareRootOf(UnsignedIntegerPattern())
    val denominator = productContaining(radical)
    val pattern = fractionOf(numerator, denominator)

    Rule(
        pattern = pattern,
        resultMaker = makeFractionOf(
            makeProductOf(move(numerator), move(radical)),
            makeProductOf(move(denominator), move(radical))
        ),
        explanationMaker = makeMetadata(Explanation.RationalizeSimpleDenominatorWithCoefficient)
    )
}

val simplifyFractionOfRootsWithSameOrder = run {
    val radicand1 = SignedIntegerPattern()
    val radicand2 = SignedIntegerPattern()

    val numerator = integerOrderRootOf(radicand1)
    val denominator = integerOrderRootOf(radicand2)

    val fraction = ConditionPattern(
        fractionOf(numerator, denominator),
        numericCondition(radicand1, radicand2) { n1, n2 -> n2.divides(n1) }
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
 * [root[a, p] / root[b, q]] -> [root[a ^ m / p, m] / root[b ^ m / q, m]]
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
            )
        ),
        explanationMaker = makeMetadata(Explanation.BringRootsToSameIndexInFraction)
    )
}

val rationalizeCubeRootDenominator = run {
    val numerator = AnyPattern()
    val cubePattern = FixedPattern(Three)
    val a = UnsignedIntegerPattern()
    val xRoot = rootOf(UnsignedIntegerPattern(), cubePattern)
    val b = UnsignedIntegerPattern()
    val yRoot = rootOf(UnsignedIntegerPattern(), cubePattern)
    // a * root[x, 3] + b * root[y, 3]
    val term1 = oneOf(xRoot, productOf(a, xRoot))
    val term2 = oneOf(yRoot, productOf(b, yRoot))
    val negatedTerm2 = optionalNegOf(term2)
    val denominator = sumOf(term1, negatedTerm2)

    val pattern = fractionOf(numerator, denominator)

    Rule(
        pattern = pattern,
        resultMaker = custom {
            val rationalizationTerm = makeSumOf(
                makePowerOf(move(term1), FixedExpressionMaker(xp(2))),
                copyFlippedSign(
                    negatedTerm2,
                    makeProductOf(makeBracketOf(move(term1)), makeBracketOf(move(term2)))
                ),
                makePowerOf(move(term2), FixedExpressionMaker(xp(2)))
            )

            makeProductOf(
                makeFractionOf(
                    move(numerator),
                    move(denominator)
                ),
                makeFractionOf(
                    rationalizationTerm,
                    rationalizationTerm
                )
            )
        },
        explanationMaker = makeMetadata(Explanation.RationalizeCubeRootDenominator)
    )
}

/**
 * (a + b)*(a^2 - ab + b^2) -> a^3 + b^3
 * or
 * (a - b)*(a^2 + ab + b^2) -> a^3 - b^3
 */
val identityCubeSumDifference = run {
    val a = rootOf(UnsignedIntegerPattern(), FixedPattern(Three))
    val b = rootOf(UnsignedIntegerPattern(), FixedPattern(Three))
    val x = UnsignedIntegerPattern()
    val y = UnsignedIntegerPattern()
    val term1 = oneOf(a, productOf(x, a))
    val term2 = oneOf(b, productOf(y, b))

    val bTerm1 = oneOf(invisibleBracketOf(term1), bracketOf(term1), term1)
    val bTerm2 = oneOf(invisibleBracketOf(term2), bracketOf(term2), term2)
    val opbTerm2 = optionalNegOf(bTerm2)

    val pattern = productOf(
        bracketOf(sumOf(bTerm1, opbTerm2)),
        bracketOf(
            sumOf(
                powerOf(
                    bTerm1,
                    FixedPattern(xp(2))
                ),
                oppositeSignPattern(opbTerm2, productOf(bTerm1, bTerm2)),
                powerOf(
                    bTerm2,
                    FixedPattern(xp(2))
                )
            )
        )
    )

    Rule(
        pattern = pattern,
        resultMaker = makeSumOf(
            makePowerOf(move(term1), FixedExpressionMaker(Three)),
            copySign(opbTerm2, makePowerOf(move(term2), FixedExpressionMaker(Three)))
        ),
        explanationMaker = makeMetadata(Explanation.IdentityCubeSum)
    )
}

/**
 * [ numerator / -root[x, 3] + root[y, 3] ]  ->  [ numerator / root[y, 3] - root[x, 3] ]
 */
val rewriteCubeRootDenominator = run {
    val numerator = AnyPattern()

    val a = rootOf(UnsignedIntegerPattern(), FixedPattern(Three))
    val b = rootOf(UnsignedIntegerPattern(), FixedPattern(Three))

    val denominator = sumOf(negOf(a), b)

    val pattern = fractionOf(numerator, denominator)

    Rule(
        pattern = pattern,
        resultMaker = makeFractionOf(
            move(numerator),
            makeSumOf(move(b), makeNegOf(move(a)))
        ),
        explanationMaker = makeMetadata(Explanation.RewriteCubeRootDenominator)
    )
}

/**
 *
 */
val rationalizeSumOfIntegerAndRadical = run {
    val numerator = AnyPattern()
    val integer = SignedIntegerPattern()
    val radical = optionalNegOf(squareRootOf(UnsignedIntegerPattern()))
    val denominator = commutativeSumOf(integer, radical)

    val fraction = fractionOf(numerator, denominator)

    Rule(
        pattern = fraction,
        resultMaker = makeFractionOf(
            makeProductOf(
                move(numerator),
                substituteIn(denominator, move(integer), copyFlippedSign(radical, move(radical.unsignedPattern)))
            ),
            makeProductOf(
                move(denominator),
                substituteIn(denominator, move(integer), copyFlippedSign(radical, move(radical.unsignedPattern)))
            )
        ),
        explanationMaker = makeMetadata(Explanation.RationalizeSumOfIntegerAndRadical)
    )
}
