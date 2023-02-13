package methods.fractionroots

import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.rootOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.methods.ruleResult
import engine.operators.BinaryExpressionOperator
import engine.operators.NaryOperator
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.integerOrderRootOf
import engine.patterns.negOf
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
import java.math.BigInteger

enum class FractionRootsRules(override val runner: Rule) : RunnerMethod {

    /**
     * E.g: root[[2 / 3], 4] -> [root[2, 4] / root[3, 4]]
     */
    DistributeRadicalOverFraction(
        rule {
            val numerator = UnsignedIntegerPattern()
            val denominator = UnsignedIntegerPattern()
            val fraction = fractionOf(numerator, denominator)

            val pattern = rootOf(fraction)

            onPattern(pattern) {
                ruleResult(
                    toExpr = fractionOf(
                        rootOf(move(numerator), move(pattern.order)),
                        rootOf(move(denominator), move(pattern.order)),
                    ),
                    explanation = metadata(Explanation.DistributeRadicalOverFraction),
                )
            }
        },
    ),

    /**
     * [4 / sqrt[3]] -> [4 / sqrt[3]] * [sqrt[3] / sqrt[3]]
     * [5 / 3 * sqrt[2]] -> [5 / 3 * sqrt[2]] * [sqrt[2] / sqrt[2]]
     */
    RationalizeSimpleDenominator(
        rule {
            val numerator = AnyPattern()
            val radical = squareRootOf(UnsignedIntegerPattern())
            val denominator = withOptionalIntegerCoefficient(radical)
            val fraction = fractionOf(numerator, denominator)

            onPattern(fraction) {
                ruleResult(
                    toExpr = productOf(
                        get(fraction),
                        fractionOf(move(radical), move(radical)),
                    ),
                    explanation = metadata(Explanation.RationalizeSimpleDenominator),
                )
            }
        },
    ),

    TurnFractionOfRootsIntoRootOfFractions(
        rule {
            val radicand1 = SignedIntegerPattern()
            val radicand2 = SignedIntegerPattern()

            val index = AnyPattern()
            val numerator = rootOf(radicand1, index)
            val denominator = rootOf(radicand2, index)

            val fraction = ConditionPattern(
                fractionOf(numerator, denominator),
                integerCondition(radicand1, radicand2) { n1, n2 -> n2.divides(n1) },
            )

            onPattern(fraction) {
                ruleResult(
                    toExpr = rootOf(
                        fractionOf(move(radicand1), move(radicand2)),
                        move(index),
                    ),
                    explanation = metadata(Explanation.TurnFractionOfRootsIntoRootOfFractions),
                )
            }
        },
    ),

    /**
     * [root[a, p] / root[b, q]] -> [root[a ^ m / p, m] / root[b ^ m / q, m]]
     * where m = lcm(p, q)
     */
    BringRootsToSameIndexInFraction(
        rule {
            val leftRadicand = UnsignedIntegerPattern()
            val leftRoot = integerOrderRootOf(leftRadicand)
            val rightRadicand = UnsignedIntegerPattern()
            val rightRoot = integerOrderRootOf(rightRadicand)
            val product = fractionOf(leftRoot, rightRoot)

            onPattern(
                ConditionPattern(
                    product,
                    integerCondition(leftRoot.order, rightRoot.order) { n1, n2 -> n1 != n2 },
                ),
            ) {
                ruleResult(
                    toExpr = fractionOf(
                        rootOf(
                            simplifiedPowerOf(
                                get(leftRadicand),
                                integerOp(leftRoot.order, rightRoot.order) { n1, n2 -> n2 / n1.gcd(n2) },
                            ),
                            integerOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 * n2 / n1.gcd(n2) },
                        ),
                        rootOf(
                            simplifiedPowerOf(
                                get(rightRadicand),
                                integerOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 / n1.gcd(n2) },
                            ),
                            integerOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 * n2 / n1.gcd(n2) },
                        ),
                    ),
                    explanation = metadata(Explanation.BringRootsToSameIndexInFraction),
                )
            }
        },
    ),

    /**
     * Handles denominators in the form
     *      integer +- cube root
     *      cube root +- integer
     *      cube root +- cube root
     * with each root potentially having an integer coefficient.
     */
    RationalizeSumOfIntegerAndCubeRoot(
        rule {
            val numerator = AnyPattern()
            val cubePattern = FixedPattern(Constants.Three)
            val integer1 = UnsignedIntegerPattern()
            val radical1 = withOptionalIntegerCoefficient(rootOf(UnsignedIntegerPattern(), cubePattern))
            val term1 = oneOf(integer1, radical1)

            val integer2 = UnsignedIntegerPattern()
            val radical2 = withOptionalIntegerCoefficient(rootOf(UnsignedIntegerPattern(), cubePattern))
            val term2 = oneOf(integer2, radical2)
            // a * root[x, 3] + b * root[y, 3]
            val signedTerm2 = optionalNegOf(term2)
            val denominator = ConditionPattern(sumOf(term1, signedTerm2)) { _, match ->
                match.isBound(radical1) || match.isBound(radical2)
            }

            val fraction = fractionOf(numerator, denominator)

            onPattern(fraction) {
                val rationalizationTerm = sumOf(
                    powerOf(move(term1), introduce(Constants.Two)),
                    copyFlippedSign(
                        signedTerm2,
                        productOf(move(term1), move(term2)),
                    ),
                    powerOf(move(term2), introduce(Constants.Two)),
                )
                ruleResult(
                    toExpr = productOf(
                        get(fraction),
                        fractionOf(
                            rationalizationTerm,
                            rationalizationTerm,
                        ),
                    ),
                    explanation = metadata(Explanation.RationalizeSumOfIntegerAndCubeRoot),
                )
            }
        },
    ),

    /**
     * (a + b) * (a^2 - ab + b^2) -> a^3 + b^3
     * or
     * (a - b) * (a^2 + ab + b^2) -> a^3 - b^3
     */
    IdentifyCubeSumDifference(
        rule {
            val integer1 = UnsignedIntegerPattern()
            val radical1 = withOptionalIntegerCoefficient(
                rootOf(UnsignedIntegerPattern(), FixedPattern(Constants.Three)),
            )
            val term1 = oneOf(integer1, radical1)

            val integer2 = UnsignedIntegerPattern()
            val radical2 = withOptionalIntegerCoefficient(
                rootOf(UnsignedIntegerPattern(), FixedPattern(Constants.Three)),
            )
            val term2 = oneOf(integer2, radical2)

            val opNegTerm2 = optionalNegOf(term2)

            val middleTerm = AnyPattern()

            val pattern = productOf(
                sumOf(term1, opNegTerm2),
                sumOf(
                    powerOf(term1, FixedPattern(Constants.Two)),
                    oppositeSignPattern(opNegTerm2, middleTerm),
                    powerOf(term2, FixedPattern(Constants.Two)),
                ),
            )

            onPattern(pattern) {
                val list1 = get(term1).flattenedProductChildren() + get(term2).flattenedProductChildren()
                val list2 = get(middleTerm).flattenedProductChildren()

                val middleTermMatches = list1.size == list2.size && list1.zip(list2).all { (e1, e2) -> e1.equiv(e2) }

                when {
                    middleTermMatches -> ruleResult(
                        toExpr = sumOf(
                            powerOf(move(term1), introduce(Constants.Three)),
                            copySign(opNegTerm2, powerOf(move(term2), introduce(Constants.Three))),
                        ),
                        explanation = metadata(Explanation.IdentityCubeSumDifference),
                    )

                    else -> null
                }
            }
        },
    ),

    /**
     * If a fractions denominator consists of two roots, optionally
     * with integer coefficients, with the first one having a negative
     * sign in front and the second one not, then it flips them.
     */
    FlipRootsInDenominator(
        rule {
            val numerator = AnyPattern()

            val integer1 = UnsignedIntegerPattern()
            val radical1 = withOptionalIntegerCoefficient(integerOrderRootOf(UnsignedIntegerPattern()))
            val term1 = negOf(oneOf(integer1, radical1))

            val integer2 = UnsignedIntegerPattern()
            val radical2 = withOptionalIntegerCoefficient(integerOrderRootOf(UnsignedIntegerPattern()))
            val term2 = oneOf(integer2, radical2)

            val denominator = ConditionPattern(sumOf(term1, term2)) { _, match ->
                match.isBound(radical1) || match.isBound(radical2)
            }

            val fraction = fractionOf(numerator, denominator)

            onPattern(fraction) {
                ruleResult(
                    toExpr = fractionOf(
                        get(numerator),
                        sumOf(move(term2), move(term1)),
                    ),
                    explanation = metadata(Explanation.FlipRootsInDenominator),
                )
            }
        },
    ),

    /**
     * Handles denominators in the form
     *      integer +- square root
     *      square root +- integer
     *      square root +- square root
     * with each root potentially having an integer coefficient.
     */
    RationalizeSumOfIntegerAndSquareRoot(
        rule {
            val numerator = AnyPattern()

            val integer1 = UnsignedIntegerPattern()
            val radical1 = withOptionalIntegerCoefficient(squareRootOf(UnsignedIntegerPattern()))
            val term1 = oneOf(integer1, radical1)

            val integer2 = UnsignedIntegerPattern()
            val radical2 = withOptionalIntegerCoefficient(squareRootOf(UnsignedIntegerPattern()))
            val term2 = oneOf(integer2, radical2)

            val signedTerm2 = optionalNegOf(term2)
            val denominator = ConditionPattern(sumOf(term1, signedTerm2)) { _, match ->
                match.isBound(radical1) || match.isBound(radical2)
            }

            val fraction = fractionOf(numerator, denominator)

            onPattern(fraction) {
                ruleResult(
                    toExpr = productOf(
                        get(fraction),
                        fractionOf(
                            sumOf(move(term1), copyFlippedSign(signedTerm2, move(term2))),
                            sumOf(move(term1), copyFlippedSign(signedTerm2, move(term2))),
                        ),
                    ),
                    explanation = metadata(Explanation.RationalizeSumOfIntegerAndSquareRoot),
                )
            }
        },
    ),

    /**
     * finds and multiplies the rationalizing term of a higher order root
     * input: [a / root[x, n]]
     */
    HigherOrderRationalizingTerm(
        rule {
            val numerator = AnyPattern()
            val index = UnsignedIntegerPattern()
            val base = UnsignedIntegerPattern()
            val exponent = UnsignedIntegerPattern()
            val exponentFactorPtn = powerOf(base, exponent)
            val integerFactorPtn = UnsignedIntegerPattern()
            val radicand = oneOf(
                productContaining(),
                exponentFactorPtn,
                integerFactorPtn,
            )
            val radical = rootOf(radicand, index)
            val denominator = withOptionalIntegerCoefficient(radical)

            val pattern = fractionOf(numerator, denominator)

            onPattern(pattern) {
                val rationalizationFactors = mutableListOf<Expression>()
                val matchPatternRadical =
                    radical.findMatches(context = context, subexpression = get(radical)).firstOrNull()

                val indexValue = matchPatternRadical?.let { index.getBoundExpr(it) }

                if (indexValue != null) {
                    val primeFactorizedFormExpr = get(radical).children()[0]

                    if (primeFactorizedFormExpr.operator == NaryOperator.Product) {
                        for (term in primeFactorizedFormExpr.children()) {
                            val exponentFactorMatch = matchPattern(exponentFactorPtn, term)
                            val integerFactorMatch = matchPattern(integerFactorPtn, term)
                            if (exponentFactorMatch != null) {
                                rationalizationFactors.add(
                                    buildWith(exponentFactorMatch) {
                                        powerOf(
                                            move(base),
                                            sumOf(introduce(indexValue), negOf(move(exponent))),
                                        )
                                    },
                                )
                            } else if (integerFactorMatch != null) {
                                rationalizationFactors.add(
                                    buildWith(integerFactorMatch) {
                                        powerOf(
                                            introduce(term),
                                            sumOf(introduce(indexValue), introduce(xp(-1))),
                                        )
                                    },
                                )
                            }
                        }
                    } else if (primeFactorizedFormExpr.operator == BinaryExpressionOperator.Power) {
                        val exponentFactorMatch = exponentFactorPtn.findMatches(
                            context = context,
                            subexpression = primeFactorizedFormExpr,
                        ).firstOrNull()
                        if (exponentFactorMatch != null) {
                            rationalizationFactors.add(
                                buildWith(exponentFactorMatch) {
                                    powerOf(
                                        move(base),
                                        sumOf(introduce(indexValue), negOf(move(exponent))),
                                    )
                                },
                            )
                        }
                    } else {
                        val integerFactorMatch = integerFactorPtn.findMatches(
                            context = context,
                            subexpression = primeFactorizedFormExpr,
                        ).firstOrNull()
                        if (integerFactorMatch != null) {
                            rationalizationFactors.add(
                                buildWith(integerFactorMatch) {
                                    powerOf(
                                        introduce(primeFactorizedFormExpr),
                                        sumOf(introduce(indexValue), introduce(xp(-1))),
                                    )
                                },
                            )
                        }
                    }
                }

                val rationalizationTerm = rootOf(
                    if (rationalizationFactors.size == 1) {
                        rationalizationFactors[0]
                    } else {
                        productOf(rationalizationFactors)
                    },
                    move(index),
                )

                ruleResult(
                    toExpr = productOf(
                        get(pattern),
                        fractionOf(rationalizationTerm, rationalizationTerm),
                    ),
                    explanation = metadata(Explanation.HigherOrderRationalizingTerm),
                )
            }
        },
    ),

    /**
     * factorize radicand of nth root in the denominator, possibly with a coefficient
     * [9 / root[18, 4]] --> [9 / root[ 2 * 3^2, 4] ]
     */
    FactorizeHigherOrderRadicand(
        rule {
            val numerator = AnyPattern()
            val radicand = integerCondition(UnsignedIntegerPattern()) { !it.isPrime() }
            val rootOrder = integerCondition(UnsignedIntegerPattern()) { it > BigInteger.TWO }
            val root = rootOf(radicand, rootOrder)
            val denominator = withOptionalIntegerCoefficient(root)
            val pattern = fractionOf(numerator, denominator)

            onPattern(pattern) {
                val factorized = productOfPrimeFactors(radicand)

                ruleResult(
                    toExpr = fractionOf(
                        get(numerator),
                        simplifiedProductOf(
                            move(denominator.integerCoefficient),
                            rootOf(transform(radicand, productOf(factorized)), move(rootOrder)),
                        ),
                    ),
                    explanation = metadata(Explanation.FactorizeHigherOrderRadicand),
                )
            }
        },
    ),
}
