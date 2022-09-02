package methods.integerroots

import engine.expressions.BinaryOperator
import engine.expressions.MappedExpression
import engine.expressions.NaryOperator
import engine.expressions.UnaryOperator
import engine.expressions.mappedExpression
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
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.bracketOf
import engine.patterns.condition
import engine.patterns.integerOrderRootOf
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.rootOf
import engine.patterns.squareRootOf
import engine.patterns.withOptionalIntegerCoefficient
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata
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

val factorizeIntegerUnderSquareRoot = rule {
    val integer = UnsignedIntegerPattern()
    val root = integerOrderRootOf(integer)

    onPattern(
        ConditionPattern(
            root,
            numericCondition(root.order, integer) { p, n -> n.hasFactorOfDegree(p.toInt()) }
        )
    ) {
        val factorized = getValue(integer)
            .primeFactorDecomposition()
            .map { (f, n) -> introduce(if (n == BigInteger.ONE) xp(f) else powerOf(xp(f), xp(n))) }

        TransformationResult(
            toExpr = rootOf(transform(integer, productOf(factorized)), move(root.order)),
            explanation = metadata(Explanation.FactorizeNumberUnderSquareRoot, move(integer)),
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

    onPattern(ConditionPattern(root, numericCondition(root.order, exponent) { p, n -> p < n && !p.divides(n) })) {
        TransformationResult(
            toExpr = rootOf(
                productOf(
                    powerOf(move(base), numericOp(root.order, exponent) { n1, n2 -> n2 - n2 % n1 }),
                    simplifiedPowerOf(move(base), numericOp(root.order, exponent) { p, n -> n % p })
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
    val radicand1 = UnsignedIntegerPattern()
    val radicand2 = UnsignedIntegerPattern()
    val root1 = integerOrderRootOf(radicand1)
    val root2 = integerOrderRootOf(radicand2)
    val product = productContaining(root1, root2)

    onPattern(ConditionPattern(product, numericCondition(root1.order, root2.order) { n1, n2 -> n1 == n2 })) {
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
        condition(AnyPattern()) { it.operator != UnaryOperator.SquareRoot && it.operator != BinaryOperator.Root }
    val product = productContaining(integerOrderRootOf(UnsignedIntegerPattern()), notRoot)

    onPattern(product) {
        val (roots, nonRoots) = get(product)!!.children()
            .partition { it.expr.operator == UnaryOperator.SquareRoot || it.expr.operator == BinaryOperator.Root }

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
        powerOf(bracketOf(root), exponent),
        numericCondition(root.order, exponent) { n1, n2 -> n1 == n2 }
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
        powerOf(bracketOf(root), exponent),
        numericCondition(root.order, exponent) { n1, n2 -> n1.divides(n2) && n1 != n2 }
    )

    onPattern(power) {
        TransformationResult(
            toExpr = powerOf(
                powerOf(
                    rootOf(move(radicand), move(root.order)),
                    move(root.order)
                ),
                numericOp(root.order, exponent) { n1, n2 -> n2 / n1 }
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
    val power = powerOf(oneOf(bracketOf(base), base), exponent)
    val radical = integerOrderRootOf(power)

    onPattern(ConditionPattern(radical, numericCondition(radical.order, exponent) { n1, n2 -> n1 == n2 })) {
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
            numericCondition(radical.order, exponent) { n1, n2 -> n1.divides(n2) && n1 != n2 }
        )
    ) {
        TransformationResult(
            toExpr = rootOf(
                powerOf(
                    powerOf(move(base), numericOp(radical.order, exponent) { n1, n2 -> n2 / n1 }),
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
    val power = powerOf(bracketOf(root), exponent)

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
            numericCondition(leftRoot.order, rightRoot.order) { n1, n2 -> n1 != n2 }
        )
    ) {
        TransformationResult(
            toExpr = product.substitute(
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
            explanation = metadata(Explanation.BringRootsToSameIndexInProduct)
        )
    }
}

val bringSameIndexSameFactorRootsAsOneRoot = rule {
    val integerFactor = UnsignedIntegerPattern()
    val base = UnsignedIntegerPattern()
    val exponent = UnsignedIntegerPattern()
    val powerFactor = powerOf(base, exponent)
    val factorPtn = oneOf(integerFactor, powerFactor)
    val leftRadicand = oneOf(
        condition(productContaining()) { expression ->
            expression.operands.all { factorPtn.matches(it) }
        },
        factorPtn
    )

    val rightRadicand = oneOf(
        condition(productContaining()) { expression ->
            expression.operands.all { factorPtn.matches(it) }
        },
        factorPtn
    )

    val orderOfRoot = UnsignedIntegerPattern()
    val leftRoot = rootOf(leftRadicand, orderOfRoot)
    val rightRoot = integerOrderRootOf(rightRadicand)

    val pattern = productContaining(leftRoot, rightRoot)

    onPattern(pattern) {
        val result = mutableListOf<MappedExpression>()
        val valLeftRadicand = get(leftRadicand)!!

        val terms = if (valLeftRadicand.expr.operator == NaryOperator.Product) valLeftRadicand.children()
        else listOf(valLeftRadicand)

        val indexValue = getValue(orderOfRoot)

        for (term in terms) {
            val powerFactorMatch = powerFactor.findMatches(term).firstOrNull()
            if (powerFactorMatch != null) {
                result.add(
                    buildWith(powerFactorMatch) {
                        powerOf(
                            move(base),
                            sumOf(move(exponent), numericOp(exponent) { indexValue - it })
                        )
                    }
                )
            } else {
                val integerFactorMatch = integerFactor.findMatches(term).firstOrNull()
                if (integerFactorMatch != null) {
                    result.add(
                        buildWith(integerFactorMatch) {
                            powerOf(
                                move(integerFactor),
                                sumOf(
                                    introduce(xp(1)),
                                    introduce(xp(indexValue - BigInteger.ONE))
                                )
                            )
                        }
                    )
                }
            }
        }

        val resultRadicand = if (result.size == 1) result[0] else mappedExpression(NaryOperator.Product, result)

        TransformationResult(
            toExpr = pattern.substitute(
                rootOf(
                    resultRadicand,
                    move(orderOfRoot)
                )
            ),
            explanation = metadata(Explanation.BringSameIndexSameFactorRootsAsOneRoot)
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

        product.operands.all { it.operator == BinaryOperator.Power && it.operands[1] == order }
    }

    val pattern = withOptionalIntegerCoefficient(cond)

    onPattern(pattern) {
        val product = get(prod)!!
        val order = product.nthChild(0).nthChild(1)

        val match = matchPattern(pattern, get(pattern)!!)

        TransformationResult(
            toExpr = simplifiedProductOf(
                pattern.coefficient(match!!),
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
