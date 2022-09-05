package methods.fractionroots

import engine.expressions.BinaryOperator
import engine.expressions.Constants.Three
import engine.expressions.MappedExpression
import engine.expressions.NaryOperator
import engine.expressions.bracketOf
import engine.expressions.fractionOf
import engine.expressions.mappedExpression
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.rootOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.TransformationResult
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.bracketOf
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
import engine.steps.metadata.metadata
import engine.utility.divides
import engine.utility.isPrime
import engine.utility.primeFactorDecomposition
import java.math.BigInteger

/**
 * E.g: root[[2 / 3], 4] -> [root[2, 4] / root[3, 4]]
 */
val distributeRadicalOverFraction = rule {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val fraction = fractionOf(numerator, denominator)

    val pattern = integerOrderRootOf(fraction)

    onPattern(pattern) {
        TransformationResult(
            toExpr = fractionOf(
                rootOf(move(numerator), move(pattern.order)),
                rootOf(move(denominator), move(pattern.order))
            ),
            explanation = metadata(Explanation.DistributeRadicalOverFraction)
        )
    }
}

/**
 * [4 / sqrt[3]] -> [4 / sqrt[3]] * [sqrt[3] / sqrt[3]]
 * [5 / 3 * sqrt[2]] -> [5 / 3 * sqrt[2]] * [sqrt[2] / sqrt[2]]
 */
val rationalizeSimpleDenominator = rule {
    val numerator = AnyPattern()
    val radical = squareRootOf(UnsignedIntegerPattern())
    val denominator = withOptionalIntegerCoefficient(radical)
    val fraction = fractionOf(numerator, denominator)

    onPattern(fraction) {
        TransformationResult(
            toExpr = productOf(
                move(fraction),
                fractionOf(move(radical), move(radical))
            ),
            explanation = metadata(Explanation.RationalizeSimpleDenominator)
        )
    }
}

val simplifyFractionOfRootsWithSameOrder = rule {
    val radicand1 = SignedIntegerPattern()
    val radicand2 = SignedIntegerPattern()

    val numerator = integerOrderRootOf(radicand1)
    val denominator = integerOrderRootOf(radicand2)

    val fraction = ConditionPattern(
        fractionOf(numerator, denominator),
        numericCondition(radicand1, radicand2) { n1, n2 -> n2.divides(n1) }
    )

    onPattern(
        ConditionPattern(
            fraction,
            numericCondition(numerator.order, denominator.order) { n1, n2 -> n1 == n2 }
        )
    ) {
        TransformationResult(
            toExpr = rootOf(
                fractionOf(move(radicand1), move(radicand2)),
                move(numerator.order)
            ),
            explanation = metadata(Explanation.SimplifyFractionOfRoots)
        )
    }
}

/**
 * [root[a, p] / root[b, q]] -> [root[a ^ m / p, m] / root[b ^ m / q, m]]
 * where m = lcm(p, q)
 */
val bringRootsToSameIndexInFraction = rule {
    val leftRadicand = UnsignedIntegerPattern()
    val leftRoot = integerOrderRootOf(leftRadicand)
    val rightRadicand = UnsignedIntegerPattern()
    val rightRoot = integerOrderRootOf(rightRadicand)
    val product = fractionOf(leftRoot, rightRoot)

    onPattern(
        ConditionPattern(
            product,
            numericCondition(leftRoot.order, rightRoot.order) { n1, n2 -> n1 != n2 }
        )
    ) {
        TransformationResult(
            toExpr = fractionOf(
                rootOf(
                    simplifiedPowerOf(
                        move(leftRadicand),
                        numericOp(leftRoot.order, rightRoot.order) { n1, n2 -> n2 / n1.gcd(n2) }
                    ),
                    numericOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 * n2 / n1.gcd(n2) }
                ),
                rootOf(
                    simplifiedPowerOf(
                        move(rightRadicand),
                        numericOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 / n1.gcd(n2) }
                    ),
                    numericOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 * n2 / n1.gcd(n2) }
                )
            ),
            explanation = metadata(Explanation.BringRootsToSameIndexInFraction)
        )
    }
}

val rationalizeCubeRootDenominator = rule {
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

    onPattern(fraction) {
        val rationalizationTerm = sumOf(
            powerOf(move(term1), introduce(xp(2))),
            copyFlippedSign(
                negatedTerm2,
                productOf(
                    bracketOf(move(term1)),
                    bracketOf(move(term2))
                )
            ),
            powerOf(move(term2), introduce(xp(2)))
        )
        TransformationResult(
            toExpr = productOf(
                move(fraction),
                fractionOf(
                    rationalizationTerm,
                    rationalizationTerm
                )
            ),
            explanation = metadata(Explanation.RationalizeCubeRootDenominator)
        )
    }
}

/**
 * (a + b) * (a^2 - ab + b^2) -> a^3 + b^3
 * or
 * (a - b) * (a^2 + ab + b^2) -> a^3 - b^3
 */
val identityCubeSumDifference = rule {
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

    onPattern(pattern) {
        TransformationResult(
            toExpr = sumOf(
                powerOf(move(term1), introduce(Three)),
                copySign(opbTerm2, powerOf(move(term2), introduce(Three)))
            ),
            explanation = metadata(Explanation.IdentityCubeSumDifference)
        )
    }
}

/**
 * If a fractions denominator consists of two roots, optionally
 * with integer coefficients, with the first one having a negative
 * sign in front and the second one not, then it flips them.
 */
val flipRootsInDenominator = rule {
    val numerator = AnyPattern()

    val integer1 = UnsignedIntegerPattern()
    val radical1 = withOptionalIntegerCoefficient(integerOrderRootOf(UnsignedIntegerPattern()))
    val term1 = negOf(oneOf(integer1, radical1))

    val integer2 = UnsignedIntegerPattern()
    val radical2 = withOptionalIntegerCoefficient(integerOrderRootOf(UnsignedIntegerPattern()))
    val term2 = oneOf(integer2, radical2)

    val denominator = ConditionPattern(sumOf(term1, term2)) { it.isBound(radical1) || it.isBound(radical2) }

    val fraction = fractionOf(numerator, denominator)

    onPattern(fraction) {
        TransformationResult(
            toExpr = fractionOf(
                move(numerator),
                sumOf(move(term2), move(term1))
            ),
            explanation = metadata(Explanation.FlipRootsInDenominator)
        )
    }
}

/**
 * Handles denominators in the form
 *      integer +- square root
 *      square root +- integer
 *      square root +- square root
 * with each root potentially having an integer coefficient.
 */
val rationalizeSumOfIntegerAndRadical = rule {
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

    onPattern(fraction) {
        TransformationResult(
            toExpr = productOf(
                move(fraction),
                fractionOf(
                    sumOf(move(term1), copyFlippedSign(signedTerm2, move(term2))),
                    sumOf(move(term1), copyFlippedSign(signedTerm2, move(term2)))
                )
            ),
            explanation = metadata(Explanation.RationalizeSumOfIntegerAndRadical)
        )
    }
}

/**
 * finds and multiplies the rationalizing term of a higher order root
 * input: [a / root[x, n]]
 */
val higherOrderRationalizingTerm = rule {
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

    onPattern(pattern) {
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
                            buildWith(exponentFactorMatch) {
                                powerOf(
                                    move(base),
                                    sumOf(introduce(indexValue), negOf(move(exponent)))
                                )
                            }
                        )
                    } else if (integerFactorMatch != null) {
                        rationalizationFactors.add(
                            buildWith(integerFactorMatch) {
                                powerOf(
                                    introduce(term.expr),
                                    sumOf(introduce(indexValue), introduce(xp(-1)))
                                )
                            }
                        )
                    }
                }
            } else if (primeFactorizedFormExpr.expr.operator == BinaryOperator.Power) {
                val exponentFactorMatch = exponentFactorPtn.findMatches(primeFactorizedFormExpr).firstOrNull()
                if (exponentFactorMatch != null) {
                    rationalizationFactors.add(
                        buildWith(exponentFactorMatch) {
                            powerOf(
                                move(base),
                                sumOf(introduce(indexValue), negOf(move(exponent)))
                            )
                        }
                    )
                }
            } else {
                val integerFactorMatch = integerFactorPtn.findMatches(primeFactorizedFormExpr).firstOrNull()
                if (integerFactorMatch != null) {
                    rationalizationFactors.add(
                        buildWith(integerFactorMatch) {
                            powerOf(
                                introduce(primeFactorizedFormExpr.expr),
                                sumOf(introduce(indexValue), introduce(xp(-1)))
                            )
                        }
                    )
                }
            }
        }

        val rationalizationTerm = rootOf(
            if (rationalizationFactors.size == 1) rationalizationFactors[0]
            else mappedExpression(NaryOperator.Product, rationalizationFactors),
            move(index)
        )

        TransformationResult(
            toExpr = productOf(
                move(pattern),
                fractionOf(rationalizationTerm, rationalizationTerm)
            ),
            explanation = metadata(Explanation.HigherOrderRationalizingTerm)
        )
    }
}

/**
 * factorize radicand of nth root in the denominator, possibly with a coefficient
 * [9 / root[18, 4]] --> [9 / root[ 2 * 3^2, 4] ]
 */
val factorizeHigherOrderRadicand = rule {
    val numerator = AnyPattern()
    val radicand = numericCondition(UnsignedIntegerPattern()) { !it.isPrime() }
    val rootOrder = numericCondition(UnsignedIntegerPattern()) { it > BigInteger.TWO }
    val root = rootOf(radicand, rootOrder)
    val denominator = withOptionalIntegerCoefficient(root)
    val pattern = fractionOf(numerator, denominator)

    onPattern(pattern) {
        val factorized = getValue(radicand)
            .primeFactorDecomposition()
            .map { (f, n) -> introduce(if (n == BigInteger.ONE) xp(f) else powerOf(xp(f), xp(n))) }

        val match = matchPattern(pattern, get(pattern)!!)

        TransformationResult(
            toExpr = fractionOf(
                move(numerator),
                simplifiedProductOf(
                    denominator.coefficient(match!!),
                    rootOf(transform(radicand, productOf(factorized)), move(rootOrder))
                )
            ),
            explanation = metadata(Explanation.FactorizeHigherOrderRadicand)
        )
    }
}
