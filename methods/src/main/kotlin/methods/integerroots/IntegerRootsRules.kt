package methods.integerroots

import engine.expressions.Constants
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.rootOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.TransformationResult
import engine.methods.rule
import engine.operators.BinaryExpressionOperator
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.integerCondition
import engine.patterns.integerOrderRootOf
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.squareRootOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalIntegerCoefficient
import engine.patterns.withOptionalRationalCoefficient
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata
import engine.utility.asPowerForRoot
import engine.utility.asProductForRoot
import engine.utility.divides
import engine.utility.hasFactorOfDegree
import engine.utility.primeFactorDecomposition
import java.math.BigInteger

val simplifyRootOfOne = rule {
    val one = FixedPattern(xp(1))
    val rootOfOne = integerOrderRootOf(one)

    onPattern(rootOfOne) {
        TransformationResult(
            toExpr = move(one),
            explanation = metadata(Explanation.SimplifyRootOfOne, move(one))
        )
    }
}

val simplifyRootOfZero = rule {
    val zero = FixedPattern(xp(0))
    val rootOfZero = integerOrderRootOf(zero)

    onPattern(rootOfZero) {
        TransformationResult(
            toExpr = move(zero),
            explanation = metadata(Explanation.SimplifyRootOfZero, move(zero))
        )
    }
}

val writeRootAsRootProduct = rule {
    val radicand = UnsignedIntegerPattern()
    val root = integerOrderRootOf(radicand)

    onPattern(root) {
        val asProduct = getValue(radicand).asProductForRoot(getValue(root.order))
        asProduct?.let {
            TransformationResult(
                toExpr = rootOf(transform(radicand, productOf(asProduct.map { introduce(xp(it)) })), move(root.order)),
                explanation = metadata(Explanation.WriteRootAsRootProduct, move(radicand)),
            )
        }
    }
}

val writeRootAsRootPower = rule {
    val radicand = UnsignedIntegerPattern()
    val root = integerOrderRootOf(radicand)

    onPattern(root) {
        val asPower = getValue(radicand).asPowerForRoot(getValue(root.order))
        asPower?.let {
            TransformationResult(
                toExpr = rootOf(
                    transform(
                        radicand,
                        powerOf(introduce(xp(asPower.first)), introduce(xp(asPower.second)))
                    ),
                    move(root.order)
                ),
                explanation = metadata(Explanation.WriteRootAsRootPower, move(radicand)),
            )
        }
    }
}

val factorizeIntegerUnderRoot = rule {
    val integer = UnsignedIntegerPattern()
    val root = integerOrderRootOf(integer)

    onPattern(
        ConditionPattern(
            root,
            integerCondition(root.order, integer) { p, n -> n.hasFactorOfDegree(p.toInt()) }
        )
    ) {
        val factorized = getValue(integer)
            .primeFactorDecomposition()
            .map { (f, n) -> introduce(if (n == BigInteger.ONE) xp(f) else powerOf(xp(f), xp(n))) }

        TransformationResult(
            toExpr = rootOf(transform(integer, productOf(factorized)), move(root.order)),
            explanation = metadata(Explanation.FactorizeIntegerUnderRoot, move(integer)),
            skills = listOf(metadata(Skill.FactorInteger, move(integer)))
        )
    }
}

/**
 * sqrt[a] * sqrt[a] -> a
 */
val simplifyMultiplicationOfSquareRoots = rule {
    val radicand = UnsignedIntegerPattern()
    val radical = squareRootOf(radicand)
    val product = productContaining(radical, radical)

    onPattern(product) {
        TransformationResult(
            toExpr = product.substitute(move(radicand)),
            explanation = metadata(Explanation.SimplifyMultiplicationOfSquareRoots, move(radical), move(radical))
        )
    }
}

/**
 * sqrt[[2^5]] -> sqrt[[2^4] * 2]
 */
val splitPowerUnderRoot = rule {
    val base = AnyPattern()
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(base, exponent)
    val root = integerOrderRootOf(power)

    onPattern(ConditionPattern(root, integerCondition(root.order, exponent) { p, n -> p < n && !p.divides(n) })) {
        TransformationResult(
            toExpr = rootOf(
                productOf(
                    powerOf(move(base), integerOp(root.order, exponent) { n1, n2 -> n2 - n2 % n1 }),
                    simplifiedPowerOf(move(base), integerOp(root.order, exponent) { p, n -> n % p })
                ),
                move(root.order)
            ),
            explanation = metadata(Explanation.SeparateSquaresUnderSquareRoot, move(base), move(exponent))
        )
    }
}

/**
 * root[a, p] * root[b, p] -> root[a * b, p]
 */
val multiplyNthRoots = rule {
    val radicand1 = AnyPattern()
    val radicand2 = AnyPattern()
    val root1 = integerOrderRootOf(radicand1)
    val root2 = integerOrderRootOf(radicand2)
    val product = productContaining(root1, root2)

    onPattern(ConditionPattern(product, integerCondition(root1.order, root2.order) { n1, n2 -> n1 == n2 })) {
        TransformationResult(
            toExpr = product.substitute(
                rootOf(productOf(move(radicand1), move(radicand2)), move(root1.order))
            ),
            explanation = metadata(Explanation.MultiplyNthRoots)
        )
    }
}

/**
 * root[x1 * ... * xn, p] -> root[x1, p] * ... * root[xn, p]
 */
val splitRootOfProduct = rule {
    val product = productContaining()
    val root = integerOrderRootOf(product)

    onPattern(root) {
        TransformationResult(
            toExpr = productOf(get(product)!!.children().map { rootOf(move(it), move(root.order)) }),
            explanation = metadata(Explanation.SplitRootOfProduct)
        )
    }
}

val normaliseProductWithRoots = rule {
    val notRoot =
        condition(AnyPattern()) {
            it.operator != UnaryExpressionOperator.SquareRoot && it.operator != BinaryExpressionOperator.Root
        }
    val product = productContaining(integerOrderRootOf(UnsignedIntegerPattern()), notRoot)
    onPattern(product) {
        val (roots, nonRoots) = get(product)!!.children()
            .partition {
                it.expr.operator == UnaryExpressionOperator.SquareRoot ||
                    it.expr.operator == BinaryExpressionOperator.Root
            }

        TransformationResult(
            toExpr = productOf(productOf(nonRoots.map { move(it) }), productOf(roots.map { move(it) })),
            explanation = metadata(Explanation.NormaliseProductWithRoots)
        )
    }
}

/**
 * [root[a, n] ^ n] -> a
 */
val simplifyNthRootToThePowerOfN = rule {
    val radicand = AnyPattern()
    val root = integerOrderRootOf(radicand)
    val exponent = UnsignedIntegerPattern()
    val power = ConditionPattern(
        powerOf(root, exponent),
        integerCondition(root.order, exponent) { n1, n2 -> n1 == n2 }
    )

    onPattern(power) {
        TransformationResult(
            toExpr = move(radicand),
            explanation = metadata(Explanation.SimplifyNthRootToThePowerOfN)
        )
    }
}

/**
 * [root[a, n] ^ m] -> [[[root[a, n] ^ n] ^ m/n] when n divides m
 */
val prepareCancellingPowerOfARoot = rule {
    val radicand = AnyPattern()
    val root = integerOrderRootOf(radicand)
    val exponent = UnsignedIntegerPattern()
    val power = ConditionPattern(
        powerOf(root, exponent),
        integerCondition(root.order, exponent) { n1, n2 -> n1.divides(n2) && n1 != n2 }
    )

    onPattern(power) {
        TransformationResult(
            toExpr = powerOf(
                powerOf(
                    rootOf(move(radicand), move(root.order)),
                    move(root.order)
                ),
                integerOp(root.order, exponent) { n1, n2 -> n2 / n1 }
            ),
            explanation = metadata(Explanation.PrepareCancellingPowerOfARoot)
        )
    }
}

/**
 * root[[a ^ n], n] -> a
 */
val simplifyNthRootOfNthPower = rule {
    val base = AnyPattern()
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(base, exponent)
    val radical = integerOrderRootOf(power)

    onPattern(ConditionPattern(radical, integerCondition(radical.order, exponent) { n1, n2 -> n1 == n2 })) {
        TransformationResult(
            toExpr = move(base),
            explanation = metadata(Explanation.SimplifyNthRootOfNthPower)
        )
    }
}

/**
 * root[[a ^ n], m] -> root[[[a ^ n/m] ^ m], m] when m divides n
 */
val prepareCancellingRootOfAPower = rule {
    val base = AnyPattern()
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(base, exponent)
    val radical = integerOrderRootOf(power)

    onPattern(
        ConditionPattern(
            radical,
            integerCondition(radical.order, exponent) { n1, n2 -> n1.divides(n2) && n1 != n2 }
        )
    ) {
        TransformationResult(
            toExpr = rootOf(
                powerOf(
                    powerOf(move(base), integerOp(radical.order, exponent) { n1, n2 -> n2 / n1 }),
                    move(radical.order)
                ),
                move(radical.order)
            ),
            explanation = metadata(Explanation.PrepareCancellingRootOfAPower)
        )
    }
}

/**
 * [root[a, p] ^ n] -> root[[a ^ n], p]]
 */
val turnPowerOfRootToRootOfPower = rule {
    val radicand = UnsignedIntegerPattern()
    val root = integerOrderRootOf(radicand)
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(root, exponent)

    onPattern(power) {
        TransformationResult(
            toExpr = rootOf(powerOf(move(radicand), move(exponent)), move(root.order)),
            explanation = metadata(Explanation.TurnPowerOfRootToRootOfPower, move(power))
        )
    }
}

/**
 * root[root[a, p], q] -> root[a, p * q]
 */
val simplifyRootOfRoot = rule {
    val radicand = UnsignedIntegerPattern()
    val innerRoot = integerOrderRootOf(radicand)
    val outerRoot = integerOrderRootOf(innerRoot)

    onPattern(outerRoot) {
        TransformationResult(
            toExpr = rootOf(
                move(radicand),
                productOf(move(outerRoot.order), move(innerRoot.order))
            ),
            explanation = metadata(Explanation.SimplifyRootOfRoot)
        )
    }
}

/**
 * k * root[a, p] -> root[[k ^ p] * a, p]
 */
val putRootCoefficientUnderRoot = rule {
    val coefficient = UnsignedIntegerPattern()
    val radicand = UnsignedIntegerPattern()
    val root = integerOrderRootOf(radicand)
    val product = productOf(coefficient, root)

    onPattern(product) {
        TransformationResult(
            toExpr = rootOf(
                productOf(
                    powerOf(move(coefficient), move(root.order)),
                    move(radicand)
                ),
                move(root.order)
            ),
            explanation = metadata(Explanation.PutRootCoefficientUnderRoot)
        )
    }
}

/**
 * root[a, p] * root[b, q] -> root[a ^ m / p, m] * root[b ^ m / q, m]
 * where m = lcm(p, q)
 */
val bringRootsToSameIndexInProduct = rule {
    val leftRadicand = UnsignedIntegerPattern()
    val leftRoot = integerOrderRootOf(leftRadicand)
    val rightRadicand = UnsignedIntegerPattern()
    val rightRoot = integerOrderRootOf(rightRadicand)
    val product = productContaining(leftRoot, rightRoot)

    onPattern(
        ConditionPattern(
            product,
            integerCondition(leftRoot.order, rightRoot.order) { n1, n2 -> n1 != n2 }
        )
    ) {
        TransformationResult(
            toExpr = product.substitute(
                rootOf(
                    simplifiedPowerOf(
                        move(leftRadicand),
                        integerOp(leftRoot.order, rightRoot.order) { n1, n2 -> n2 / n1.gcd(n2) }
                    ),
                    integerOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 * n2 / n1.gcd(n2) }
                ),
                rootOf(
                    simplifiedPowerOf(
                        move(rightRadicand),
                        integerOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 / n1.gcd(n2) }
                    ),
                    integerOp(leftRoot.order, rightRoot.order) { n1, n2 -> n1 * n2 / n1.gcd(n2) }
                )
            ),
            explanation = metadata(Explanation.BringRootsToSameIndexInProduct)
        )
    }
}

/**
 * root[ [a^p] * [b^p] * [c^p] * ..., p] --> root[[(a * b * c * ... )^p], p]
 */
val combineProductOfSamePowerUnderHigherRoot = rule {
    val prod = productContaining()
    val root = integerOrderRootOf(prod)
    val cond = ConditionPattern(root) { match ->
        val order = root.order.getBoundExpr(match)!!
        val product = prod.getBoundExpr(match)!!

        product.operands.all { it.operator == BinaryExpressionOperator.Power && it.operands[1] == order }
    }

    val pattern = withOptionalIntegerCoefficient(cond)

    onPattern(pattern) {
        val product = get(prod)!!
        val order = product.nthChild(0).nthChild(1)

        TransformationResult(
            toExpr = simplifiedProductOf(
                move(pattern.coefficient),
                rootOf(
                    powerOf(
                        productOf(product.children().map { move(it.nthChild(0)) }),
                        move(order)
                    ),
                    move(root.order)
                )
            ),
            explanation = metadata(Explanation.CombineProductOfSamePowerUnderHigherRoot)
        )
    }
}

/**
 * collect the powers of two exponents with the same base
 * 3^3 * 3^2 -> 3^(3 + 2)
 */
val collectPowersOfExponentsWithSameBase = rule {
    val base = UnsignedIntegerPattern()
    val exponent1 = UnsignedIntegerPattern()
    val exponent2 = UnsignedIntegerPattern()
    val term1ExpFactor = powerOf(base, exponent1)
    val term2ExpFactor = powerOf(base, exponent2)
    val term1 = oneOf(term1ExpFactor, base)
    val term2 = oneOf(term2ExpFactor, base)

    val prod = productContaining(term1, term2)

    onPattern(prod) {
        val exponentValue1 = when {
            isBound(term1ExpFactor) -> move(exponent1)
            else -> introduce(Constants.One)
        }

        val exponentValue2 = when {
            isBound(term2ExpFactor) -> move(exponent2)
            else -> introduce(Constants.One)
        }

        TransformationResult(
            toExpr = prod.substitute(
                powerOf(move(base), sumOf(exponentValue1, exponentValue2))
            ),
            explanation = metadata(Explanation.CollectPowersOfExponentsWithSameBase)
        )
    }
}

val collectLikeRoots = rule {
    val common = integerOrderRootOf(UnsignedIntegerPattern())

    val commonTerm1 = withOptionalRationalCoefficient(common)
    val commonTerm2 = withOptionalRationalCoefficient(common)
    val sum = sumContaining(commonTerm1, commonTerm2)

    onPattern(sum) {
        TransformationResult(
            toExpr = collectLikeTermsInSum(get(sum)!!, withOptionalRationalCoefficient(common)),
            explanation = metadata(Explanation.CollectLikeRoots)
        )
    }
}
