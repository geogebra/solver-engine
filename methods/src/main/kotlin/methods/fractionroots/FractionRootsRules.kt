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
import engine.expressionmakers.makeSimplifiedProductOf
import engine.expressionmakers.makeSquareRootOf
import engine.expressionmakers.makeSumOf
import engine.expressionmakers.move
import engine.expressionmakers.transform
import engine.expressions.BinaryOperator
import engine.expressions.Constants.Three
import engine.expressions.MappedExpression
import engine.expressions.NaryOperator
import engine.expressions.mappedExpression
import engine.expressions.powerOf
import engine.expressions.xp
import engine.methods.Rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.bracketOf
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
import engine.patterns.withOptionalIntegerCoefficient
import engine.steps.metadata.makeMetadata
import engine.utility.divides
import engine.utility.isPrime
import engine.utility.primeFactorDecomposition
import java.math.BigInteger

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
 * [4 / sqrt[3]] -> [4 / sqrt[3]] * [sqrt[3] / sqrt[3]]
 * [5 / 3 * sqrt[2]] -> [5 / 3 * sqrt[2]] * [sqrt[2] / sqrt[2]]
 */
val rationalizeSimpleDenominator = run {
    val numerator = AnyPattern()
    val radical = squareRootOf(UnsignedIntegerPattern())
    val denominator = withOptionalIntegerCoefficient(radical)
    val fraction = fractionOf(numerator, denominator)

    Rule(
        pattern = fraction,
        resultMaker = makeProductOf(
            move(fraction),
            makeFractionOf(move(radical), move(radical))
        ),
        explanationMaker = makeMetadata(Explanation.RationalizeSimpleDenominator)
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
    val xRoot = rootOf(UnsignedIntegerPattern(), cubePattern)
    val yRoot = rootOf(UnsignedIntegerPattern(), cubePattern)
    // a * root[x, 3] + b * root[y, 3]
    val term1 = withOptionalIntegerCoefficient(xRoot)
    val term2 = withOptionalIntegerCoefficient(yRoot)
    val negatedTerm2 = optionalNegOf(term2)
    val denominator = sumOf(term1, negatedTerm2)

    val fraction = fractionOf(numerator, denominator)

    Rule(
        pattern = fraction,
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
                move(fraction),
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
 * (a + b) * (a^2 - ab + b^2) -> a^3 + b^3
 * or
 * (a - b) * (a^2 + ab + b^2) -> a^3 - b^3
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
        explanationMaker = makeMetadata(Explanation.IdentityCubeSumDifference)
    )
}

/**
 * If a fractions denominator consists of two roots, optionally
 * with integer coefficients, with the first one having a negative
 * sign in front and the second one not, then it flips them.
 */
val flipRootsInDenominator = run {
    val numerator = AnyPattern()

    val integer1 = UnsignedIntegerPattern()
    val radical1 = withOptionalIntegerCoefficient(integerOrderRootOf(UnsignedIntegerPattern()))
    val term1 = negOf(oneOf(integer1, radical1))

    val integer2 = UnsignedIntegerPattern()
    val radical2 = withOptionalIntegerCoefficient(integerOrderRootOf(UnsignedIntegerPattern()))
    val term2 = oneOf(integer2, radical2)

    val denominator = ConditionPattern(sumOf(term1, term2)) { it.isBound(radical1) || it.isBound(radical2) }

    val fraction = fractionOf(numerator, denominator)

    Rule(
        pattern = fraction,
        resultMaker = makeFractionOf(
            move(numerator),
            makeSumOf(move(term2), move(term1))
        ),
        explanationMaker = makeMetadata(Explanation.FlipRootsInDenominator)
    )
}

/**
 * Handles denominators in the form
 *      integer +- square root
 *      square root +- integer
 *      square root +- square root
 * with each root potentially having an integer coefficient.
 */
val rationalizeSumOfIntegerAndRadical = run {
    val numerator = AnyPattern()

    val integer1 = UnsignedIntegerPattern()
    val radical1 = withOptionalIntegerCoefficient(squareRootOf(UnsignedIntegerPattern()))
    val term1 = oneOf(integer1, radical1)

    val integer2 = UnsignedIntegerPattern()
    val radical2 = withOptionalIntegerCoefficient(squareRootOf(UnsignedIntegerPattern()))
    val term2 = oneOf(integer2, radical2)

    val signedTerm2 = optionalNegOf(term2)
    val denominator = ConditionPattern(sumOf(term1, signedTerm2)) { it.isBound(radical1) || it.isBound(radical2) }

    val fraction = fractionOf(numerator, denominator)

    Rule(
        pattern = fraction,
        resultMaker = makeProductOf(
            move(fraction),
            makeFractionOf(
                makeSumOf(move(term1), copyFlippedSign(signedTerm2, move(term2))),
                makeSumOf(move(term1), copyFlippedSign(signedTerm2, move(term2)))
            )
        ),
        explanationMaker = makeMetadata(Explanation.RationalizeSumOfIntegerAndRadical)
    )
}

/**
 * finds and multiplies the rationalizing term of a higher order root
 * input: [a / root[x, n]]
*/
val higherOrderRationalizingTerm = run {
    val numerator = AnyPattern()
    val index = UnsignedIntegerPattern()
    val base = UnsignedIntegerPattern()
    val exponent = UnsignedIntegerPattern()
    val exponentFactorPtn = powerOf(base, exponent)
    val integerFactorPtn = UnsignedIntegerPattern()
    val radicand = oneOf(
        productContaining(),
        exponentFactorPtn,
        integerFactorPtn
    )
    val radical = rootOf(radicand, index)
    val denominator = withOptionalIntegerCoefficient(radical)

    val pattern = fractionOf(numerator, denominator)

    Rule(
        pattern = pattern,
        resultMaker = custom {
            val rationalizationFactors = mutableListOf<MappedExpression>()
            val matchPatternRadical = radical.findMatches(get(radical)!!).firstOrNull()

            val indexValue = matchPatternRadical?.let { index.getBoundExpr(it) }

            if (indexValue != null) {
                val primeFactorizedFormExpr = get(radical)!!.children()[0]

                if (primeFactorizedFormExpr.expr.operator == NaryOperator.Product) {
                    for (term in primeFactorizedFormExpr.children()) {
                        val exponentFactorMatch = exponentFactorPtn.findMatches(term).firstOrNull()
                        val integerFactorMatch = integerFactorPtn.findMatches(term).firstOrNull()
                        if (exponentFactorMatch != null) {
                            rationalizationFactors.add(
                                makePowerOf(
                                    move(base),
                                    makeSumOf(FixedExpressionMaker(indexValue), makeNegOf(move(exponent)))
                                ).make(exponentFactorMatch)
                            )
                        } else if (integerFactorMatch != null) {
                            rationalizationFactors.add(
                                makePowerOf(
                                    FixedExpressionMaker(term.expr),
                                    makeSumOf(FixedExpressionMaker(indexValue), FixedExpressionMaker(xp(-1)))
                                ).make(integerFactorMatch)
                            )
                        }
                    }
                } else if (primeFactorizedFormExpr.expr.operator == BinaryOperator.Power) {
                    val exponentFactorMatch = exponentFactorPtn.findMatches(primeFactorizedFormExpr).firstOrNull()
                    if (exponentFactorMatch != null) {
                        rationalizationFactors.add(
                            makePowerOf(
                                move(base),
                                makeSumOf(FixedExpressionMaker(indexValue), makeNegOf(move(exponent)))
                            ).make(exponentFactorMatch)
                        )
                    }
                } else {
                    val integerFactorMatch = integerFactorPtn.findMatches(primeFactorizedFormExpr).firstOrNull()
                    if (integerFactorMatch != null) {
                        rationalizationFactors.add(
                            makePowerOf(
                                FixedExpressionMaker(primeFactorizedFormExpr.expr),
                                makeSumOf(FixedExpressionMaker(indexValue), FixedExpressionMaker(xp(-1)))
                            ).make(integerFactorMatch)
                        )
                    }
                }
            }

            val rationalizationTerm = makeRootOf(
                if (rationalizationFactors.size == 1) rationalizationFactors[0]
                else mappedExpression(NaryOperator.Product, rationalizationFactors),
                move(index)
            )

            makeProductOf(
                move(pattern),
                makeFractionOf(rationalizationTerm, rationalizationTerm)
            )
        },
        explanationMaker = makeMetadata(Explanation.HigherOrderRationalizingTerm)
    )
}

/**
 * factorize radicand of nth root in the denominator, possibly with a coefficient
 * [9 / root[18, 4]] --> [9 / root[ 2 * 3^2, 4] ]
 */
val factorizeHigherOrderRadicand = run {
    val numerator = AnyPattern()
    val radicand = numericCondition(UnsignedIntegerPattern()) { !it.isPrime() }
    val rootOrder = numericCondition(UnsignedIntegerPattern()) { it > BigInteger.TWO }
    val root = rootOf(radicand, rootOrder)
    val denominator = withOptionalIntegerCoefficient(root)
    val pattern = fractionOf(numerator, denominator)

    Rule(
        pattern = pattern,
        resultMaker = custom {
            val factorized = getValue(radicand)
                .primeFactorDecomposition()
                .map { (f, n) -> FixedExpressionMaker(if (n == BigInteger.ONE) xp(f) else powerOf(xp(f), xp(n))) }

            val match = matchPattern(pattern, get(pattern)!!)
            makeFractionOf(
                move(numerator),
                makeSimplifiedProductOf(
                    denominator.coefficient(match!!),
                    makeRootOf(transform(radicand, makeProductOf(factorized)), move(rootOrder))
                )
            )
        },
        explanationMaker = makeMetadata(Explanation.FactorizeHigherOrderRadicand)
    )
}
