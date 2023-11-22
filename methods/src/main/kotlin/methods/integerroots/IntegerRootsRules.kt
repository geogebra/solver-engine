package methods.integerroots

import engine.expressions.DefaultView
import engine.expressions.Factor
import engine.expressions.IntegerExpression
import engine.expressions.IntegerFactorView
import engine.expressions.Power
import engine.expressions.SumView
import engine.expressions.isSignedInteger
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.rootOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.squareRootOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.BuilderCondition
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.integerCondition
import engine.patterns.integerOrderRootOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.rootOf
import engine.patterns.squareRootOf
import engine.patterns.withOptionalIntegerCoefficient
import engine.sign.Sign
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata
import engine.utility.divides
import engine.utility.greatestSquareFactor
import engine.utility.hasFactorOfDegree
import engine.utility.isEven
import engine.utility.isFactorizableUnderRationalExponent
import engine.utility.isSquare
import engine.utility.primeFactorDecomposition
import java.math.BigInteger

enum class IntegerRootsRules(override val runner: Rule) : RunnerMethod {
    SimplifyRootOfOne(
        rule {
            val one = FixedPattern(xp(1))
            val rootOfOne = rootOf(one)

            onPattern(rootOfOne) {
                ruleResult(
                    toExpr = move(one),
                    explanation = metadata(Explanation.SimplifyRootOfOne, move(one)),
                )
            }
        },
    ),

    SimplifyRootOfZero(
        rule {
            val zero = FixedPattern(xp(0))
            val rootOfZero = rootOf(zero)

            onPattern(rootOfZero) {
                ruleResult(
                    toExpr = move(zero),
                    explanation = metadata(Explanation.SimplifyRootOfZero, move(zero)),
                )
            }
        },
    ),

    WriteRootAsRootProduct(
        rule {
            val radicand = UnsignedIntegerPattern()
            val root = integerOrderRootOf(radicand)

            onPattern(root) {
                val asProduct = getValue(radicand).asProductForRoot(getValue(root.order))
                asProduct?.let {
                    ruleResult(
                        toExpr = rootOf(
                            transform(radicand, productOf(asProduct.map { introduce(xp(it)) })),
                            move(root.order),
                        ),
                        explanation = metadata(Explanation.WriteRootAsRootProduct, move(radicand)),
                    )
                }
            }
        },
    ),

    WriteRootAsRootPower(
        rule {
            val radicand = UnsignedIntegerPattern()
            val root = integerOrderRootOf(radicand)

            onPattern(root) {
                val asPower = getValue(radicand).asPowerForRoot(getValue(root.order))
                asPower?.let {
                    ruleResult(
                        toExpr = rootOf(
                            transform(
                                radicand,
                                powerOf(introduce(xp(asPower.first)), introduce(xp(asPower.second))),
                            ),
                            move(root.order),
                        ),
                        explanation = metadata(Explanation.WriteRootAsRootPower, move(radicand)),
                    )
                }
            }
        },
    ),

    FactorizeIntegerUnderRoot(
        rule {
            val integer = UnsignedIntegerPattern()
            val root = integerOrderRootOf(integer)

            onPattern(root) {
                val integerValue = getValue(integer)
                val rootOrderValue = getValue(root.order)
                if (integerValue.isFactorizableUnderRationalExponent(BigInteger.ONE, rootOrderValue)) {
                    val factorized = getValue(integer)
                        .primeFactorDecomposition()
                        .map { (f, n) -> introduce(if (n == BigInteger.ONE) xp(f) else powerOf(xp(f), xp(n))) }

                    ruleResult(
                        toExpr = rootOf(transform(integer, productOf(factorized)), get(root.order)),
                        explanation = metadata(Explanation.FactorizeIntegerUnderRoot, move(integer)),
                        skills = listOf(metadata(Skill.FactorInteger, move(integer))),
                    )
                } else {
                    null
                }
            }
        },
    ),

    FactorizeIntegerPowerUnderRoot(
        rule {
            val integer = UnsignedIntegerPattern()
            val exponent = UnsignedIntegerPattern()
            val pow = powerOf(integer, exponent)
            val root = integerOrderRootOf(pow)

            onPattern(
                ConditionPattern(
                    root,
                    integerCondition(root.order, integer, exponent) { p, n, q ->
                        n.pow(q.toInt()).hasFactorOfDegree(p.toInt())
                    },
                ),
            ) {
                val factorized = productOfPrimeFactors(integer)

                ruleResult(
                    toExpr = rootOf(
                        powerOf(transform(integer, productOf(factorized)), move(exponent)),
                        move(root.order),
                    ),
                    explanation = metadata(Explanation.FactorizeIntegerPowerUnderRoot, move(integer)),
                    skills = listOf(metadata(Skill.FactorInteger, move(integer))),
                )
            }
        },
    ),

    /**
     * sqrt[a] * sqrt[a] -> a
     */
    SimplifyMultiplicationOfSquareRoots(
        rule {
            val radicand = UnsignedIntegerPattern()
            val radical = squareRootOf(radicand)
            val product = productContaining(radical, radical)

            onPattern(product) {
                ruleResult(
                    toExpr = product.substitute(move(radicand)),
                    explanation = metadata(Explanation.SimplifyMultiplicationOfSquareRoots),
                )
            }
        },
    ),

    /**
     * sqrt[[2^5]] -> sqrt[[2^4] * 2]
     */
    SplitPowerUnderRoot(
        rule {
            val base = AnyPattern()
            val exponent = UnsignedIntegerPattern()
            val power = powerOf(base, exponent)
            val root = integerOrderRootOf(power)
            val pattern = ConditionPattern(
                root,
                integerCondition(root.order, exponent) { p, n -> p < n && !p.divides(n) },
            )

            onPattern(pattern) {
                ruleResult(
                    toExpr = rootOf(
                        productOf(
                            powerOf(move(base), integerOp(root.order, exponent) { n1, n2 -> n2 - n2 % n1 }),
                            simplifiedPowerOf(move(base), integerOp(root.order, exponent) { p, n -> n % p }),
                        ),
                        move(root.order),
                    ),
                    explanation = metadata(Explanation.SeparateSquaresUnderSquareRoot),
                )
            }
        },
    ),

    /**
     * root[a, p] * root[b, p] -> root[a * b, p]
     */
    MultiplyNthRoots(
        rule {
            val order = UnsignedIntegerPattern()
            val radicand1 = AnyPattern()
            val radicand2 = AnyPattern()
            val root1 = rootOf(radicand1, order)
            val root2 = rootOf(radicand2, order)
            val product = productContaining(root1, root2)

            onPattern(product) {
                ruleResult(
                    toExpr = product.substitute(
                        rootOf(productOf(move(radicand1), move(radicand2)), factor(order)),
                    ),
                    explanation = metadata(Explanation.MultiplyNthRoots),
                )
            }
        },
    ),

    /**
     * root[x1 * ... * xn, p] -> root[x1, p] * ... * root[xn, p]
     */
    SplitRootOfProduct(
        rule {
            val product = productContaining()
            val root = rootOf(product)

            onPattern(root) {
                ruleResult(
                    toExpr = productOf(
                        get(product).children.map { rootOf(move(it), move(root.order)) },
                    ),
                    explanation = metadata(Explanation.SplitRootOfProduct),
                )
            }
        },
    ),

    /**
     * sqrt[25xy] -> sqrt[25]*sqrt[xy]
     */
    MoveSquareFactorOutOfRoot(moveSquareFactorOutOfSquareRoot),

    /**
     * [root[a, n] ^ n] -> a
     */
    SimplifyNthRootToThePowerOfN(
        rule {
            val exponent = UnsignedIntegerPattern()
            val radicand = AnyPattern()
            val root = rootOf(radicand, exponent)
            val power = powerOf(root, exponent)

            onPattern(power) {
                ruleResult(
                    toExpr = cancel(exponent, get(radicand)),
                    explanation = metadata(Explanation.SimplifyNthRootToThePowerOfN),
                )
            }
        },
    ),

    /**
     * [root[a, n] ^ m] -> [[[root[a, n] ^ n] ^ m/n] when n divides m
     */
    PrepareCancellingPowerOfARoot(
        rule {
            val radicand = AnyPattern()
            val root = integerOrderRootOf(radicand)
            val exponent = UnsignedIntegerPattern()
            val power = ConditionPattern(
                powerOf(root, exponent),
                integerCondition(root.order, exponent) { n1, n2 -> n1.divides(n2) && n1 != n2 },
            )

            onPattern(power) {
                ruleResult(
                    toExpr = powerOf(
                        powerOf(
                            rootOf(get(radicand), move(root.order)),
                            move(root.order),
                        ),
                        integerOp(root.order, exponent) { n1, n2 -> n2 / n1 },
                    ),
                    explanation = metadata(Explanation.PrepareCancellingPowerOfARoot),
                )
            }
        },
    ),

    /**
     * root[[a ^ n], n] -> a
     */
    SimplifyNthRootOfNthPower(
        rule {
            val exponent = UnsignedIntegerPattern()
            val base = AnyPattern()
            val power = powerOf(base, exponent)
            val radical = rootOf(power, exponent)

            onPattern(radical) {
                // e.g. root[ [a^4], 4 ] = a, only when a > 0
                if (getValue(exponent).isEven() && get(base).signOf() != Sign.POSITIVE) {
                    return@onPattern null
                }

                ruleResult(
                    toExpr = cancel(exponent, get(base)),
                    explanation = metadata(Explanation.SimplifyNthRootOfNthPower),
                )
            }
        },
    ),

    /**
     * [root[a, p] ^ n] -> root[[a ^ n], p]]
     */
    TurnPowerOfRootToRootOfPower(
        rule {
            val radicand = UnsignedIntegerPattern()
            val root = integerOrderRootOf(radicand)
            val exponent = UnsignedIntegerPattern()
            val power = powerOf(root, exponent)

            onPattern(power) {
                ruleResult(
                    toExpr = rootOf(powerOf(get(radicand), move(exponent)), move(root.order)),
                    explanation = metadata(Explanation.TurnPowerOfRootToRootOfPower, move(power)),
                )
            }
        },
    ),

    /**
     * root[root[a, p], q] -> root[a, p * q]
     */
    SimplifyRootOfRoot(
        rule {
            val radicand = UnsignedIntegerPattern()
            val innerRoot = rootOf(radicand)
            val outerRoot = rootOf(innerRoot)

            onPattern(outerRoot) {
                ruleResult(
                    toExpr = rootOf(
                        get(radicand),
                        productOf(move(outerRoot.order), move(innerRoot.order)),
                    ),
                    explanation = metadata(Explanation.SimplifyRootOfRoot),
                )
            }
        },
    ),

    /**
     * k * root[a, p] -> root[[k ^ p] * a, p]
     */
    PutRootCoefficientUnderRoot(
        rule {
            val coefficient = UnsignedIntegerPattern()
            val radicand = UnsignedIntegerPattern()
            val root = integerOrderRootOf(radicand)
            val product = productOf(coefficient, root)

            onPattern(product) {
                ruleResult(
                    toExpr = rootOf(
                        productOf(
                            powerOf(move(coefficient), move(root.order)),
                            move(radicand),
                        ),
                        get(root.order),
                    ),
                    explanation = metadata(Explanation.PutRootCoefficientUnderRoot),
                )
            }
        },
    ),

    /**
     * root[a, p] * root[b, q] -> root[a ^ m / p, m] * root[b ^ m / q, m]
     * where m = lcm(p, q)
     */
    BringRootsToSameIndexInProduct(
        rule {
            val leftRadicand = UnsignedIntegerPattern()
            val leftRoot = integerOrderRootOf(leftRadicand)
            val rightRadicand = UnsignedIntegerPattern()
            val rightRoot = integerOrderRootOf(rightRadicand)
            val product = productContaining(leftRoot, rightRoot)

            onPattern(
                ConditionPattern(
                    product,
                    integerCondition(leftRoot.order, rightRoot.order) { n1, n2 -> n1 != n2 },
                ),
            ) {
                ruleResult(
                    toExpr = product.substitute(
                        rootOf(
                            simplifiedPowerOf(
                                move(leftRadicand),
                                integerOp(leftRoot.order, rightRoot.order) { n1, n2 -> n2 / n1.gcd(n2) },
                            ),
                            integerOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 * n2 / n1.gcd(n2) },
                        ),
                        rootOf(
                            simplifiedPowerOf(
                                move(rightRadicand),
                                integerOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 / n1.gcd(n2) },
                            ),
                            integerOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 * n2 / n1.gcd(n2) },
                        ),
                    ),
                    explanation = metadata(Explanation.BringRootsToSameIndexInProduct),
                )
            }
        },
    ),

    /**
     * root[ [a^p] * [b^p] * [c^p] * ..., p] --> root[[(a * b * c * ... )^p], p]
     */
    CombineProductOfSamePowerUnderHigherRoot(
        rule {
            val prod = productContaining()
            val root = rootOf(prod)
            val cond = ConditionPattern(
                root,
                BuilderCondition {
                    val order = get(root.order)
                    val product = get(prod)

                    product.children.all { it is Power && it.exponent == order }
                },
            )

            val pattern = withOptionalIntegerCoefficient(cond, true)

            onPattern(pattern) {
                val product = get(prod)
                val order = product.firstChild.secondChild

                ruleResult(
                    toExpr = simplifiedProductOf(
                        move(pattern.integerCoefficient),
                        rootOf(
                            powerOf(
                                productOf(product.children.map { move(it.firstChild) }),
                                move(order),
                            ),
                            move(root.order),
                        ),
                    ),
                    explanation = metadata(Explanation.CombineProductOfSamePowerUnderHigherRoot),
                )
            }
        },
    ),

    FactorGreatestCommonSquareIntegerFactor(factorGreatestCommonSquareIntegerFactor),
}

private val moveSquareFactorOutOfSquareRoot = rule {
    val square = integerCondition(UnsignedIntegerPattern()) { it != BigInteger.ONE && it.isSquare() }
    val product = productContaining(square)

    onPattern(squareRootOf(product)) {
        ruleResult(
            toExpr = productOf(
                squareRootOf(move(square)),
                squareRootOf(restOf(product)),
            ),
            explanation = metadata(Explanation.SplitRootOfProduct),
        )
    }
}

private val factorGreatestCommonSquareIntegerFactor = rule {
    onPattern(AnyPattern()) {
        if (expression.isSignedInteger()) {
            // If it's just an integer we defer to more basic methods
            return@onPattern null
        }

        val sumView = SumView(expression) {
            if (it is IntegerExpression) {
                IntegerFactorView(it)
            } else {
                DefaultView(it)
            }
        }

        val integerFactors = sumView.termViews.map {
            it.findSingleFactor<IntegerFactorView>() ?: return@onPattern null
        }

        val gcd = integerFactors.fold(BigInteger.ZERO) { acc, n -> acc.gcd(n.value) }
        val squareFactor = gcd.greatestSquareFactor()
        if (squareFactor == BigInteger.ONE || squareFactor == gcd && integerFactors.size == 1) {
            return@onPattern null
        }

        val commonIntegerFactor = xp(squareFactor).withOrigin(Factor(integerFactors.map { it.original }))

        for (integerFactor in integerFactors) {
            integerFactor.changeValue(integerFactor.value / squareFactor)
        }

        ruleResult(
            toExpr = productOf(commonIntegerFactor, sumView.recombine()),
            explanation = metadata(Explanation.FactorGreatestCommonSquareIntegerFactor),
        )
    }
}
