package methods.integerroots

import engine.expressionmakers.FixedExpressionMaker
import engine.expressionmakers.makeNumericOp
import engine.expressionmakers.makePowerOf
import engine.expressionmakers.makeProductOf
import engine.expressionmakers.makeRootOf
import engine.expressionmakers.makeSimplifiedPowerOf
import engine.expressionmakers.makeSimplifiedProductOf
import engine.expressionmakers.makeSumOf
import engine.expressionmakers.move
import engine.expressionmakers.substituteIn
import engine.expressionmakers.transform
import engine.expressions.BinaryOperator
import engine.expressions.MappedExpression
import engine.expressions.NaryOperator
import engine.expressions.UnaryOperator
import engine.expressions.mappedExpression
import engine.expressions.powerOf
import engine.expressions.xp
import engine.methods.Rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.bracketOf
import engine.patterns.condition
import engine.patterns.custom
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
import engine.steps.metadata.makeMetadata
import engine.utility.divides
import engine.utility.hasFactorOfDegree
import engine.utility.primeFactorDecomposition
import java.math.BigInteger

val simplifyRootOfOne = run {
    val one = FixedPattern(xp(1))

    Rule(
        pattern = oneOf(squareRootOf(one), rootOf(one, AnyPattern())),
        resultMaker = move(one),
        explanationMaker = makeMetadata(Explanation.SimplifyRootOfOne, move(one))
    )
}

val simplifyRootOfZero = run {
    val zero = FixedPattern(xp(0))

    Rule(
        pattern = oneOf(squareRootOf(zero), rootOf(zero, AnyPattern())),
        resultMaker = move(zero),
        explanationMaker = makeMetadata(Explanation.SimplifyRootOfZero, move(zero))
    )
}

val factorizeIntegerUnderSquareRoot = run {
    val integer = UnsignedIntegerPattern()
    val root = integerOrderRootOf(integer)

    Rule(
        pattern = ConditionPattern(
            root,
            numericCondition(root.order, integer) { p, n -> n.hasFactorOfDegree(p.toInt()) }
        ),
        resultMaker = custom {
            val factorized = getValue(integer)
                .primeFactorDecomposition()
                .map { (f, n) -> FixedExpressionMaker(if (n == BigInteger.ONE) xp(f) else powerOf(xp(f), xp(n))) }
            makeRootOf(transform(integer, makeProductOf(factorized)), move(root.order))
        },
        explanationMaker = makeMetadata(Explanation.FactorizeNumberUnderSquareRoot, move(integer)),
        skillMakers = listOf(makeMetadata(Skill.FactorInteger, move(integer)))
    )
}

/**
 * sqrt[a] * sqrt[a] -> a
 */
val simplifyMultiplicationOfSquareRoots = run {
    val radicand = UnsignedIntegerPattern()
    val radical = squareRootOf(radicand)
    val pattern = productContaining(radical, radical)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(
            pattern,
            move(radicand)
        ),
        explanationMaker = makeMetadata(Explanation.SimplifyMultiplicationOfSquareRoots, move(radical), move(radical))
    )
}

/**
 * sqrt[[2^5]] -> sqrt[[2^4] * 2]
 */
val splitPowerUnderRoot = run {
    val base = AnyPattern()
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(base, exponent)
    val root = integerOrderRootOf(power)

    Rule(
        pattern = ConditionPattern(root, numericCondition(root.order, exponent) { p, n -> p < n && !p.divides(n) }),
        resultMaker = makeRootOf(
            makeProductOf(
                makePowerOf(move(base), makeNumericOp(root.order, exponent) { n1, n2 -> n2 - n2 % n1 }),
                makeSimplifiedPowerOf(move(base), makeNumericOp(root.order, exponent) { p, n -> n % p })
            ),
            move(root.order)
        ),
        explanationMaker = makeMetadata(Explanation.SeparateSquaresUnderSquareRoot, move(base), move(exponent))
    )
}

/**
 * root[a, p] * root[b, p] -> root[a * b, p]
 */
val multiplyNthRoots = run {
    val radicand1 = UnsignedIntegerPattern()
    val radicand2 = UnsignedIntegerPattern()
    val root1 = integerOrderRootOf(radicand1)
    val root2 = integerOrderRootOf(radicand2)
    val product = productContaining(root1, root2)

    Rule(
        pattern = ConditionPattern(product, numericCondition(root1.order, root2.order) { n1, n2 -> n1 == n2 }),
        resultMaker = substituteIn(
            product,
            makeRootOf(makeProductOf(move(radicand1), move(radicand2)), move(root1.order))
        ),
        explanationMaker = makeMetadata(Explanation.MultiplyNthRoots)
    )
}

/**
 * root[x1 * ... * xn, p] -> root[x1, p] * ... * root[xn, p]
 */
val splitRootOfProduct = run {
    val product = productContaining()
    val root = integerOrderRootOf(product)

    Rule(
        pattern = root,
        resultMaker = custom {
            makeProductOf(get(product)!!.children().map { makeRootOf(it, move(root.order)) })
        },
        explanationMaker = makeMetadata(Explanation.SplitRootOfProduct)
    )
}

val normaliseProductWithRoots = run {
    val notRoot =
        condition(AnyPattern()) { it.operator != UnaryOperator.SquareRoot && it.operator != BinaryOperator.Root }
    val product = productContaining(integerOrderRootOf(UnsignedIntegerPattern()), notRoot)

    Rule(
        pattern = product,
        resultMaker = custom {
            val (roots, nonRoots) = get(product)!!.children()
                .partition { it.expr.operator == UnaryOperator.SquareRoot || it.expr.operator == BinaryOperator.Root }
            makeProductOf(makeProductOf(nonRoots), makeProductOf(roots))
        },
        explanationMaker = makeMetadata(Explanation.NormaliseProductWithRoots)
    )
}

/**
 * [root[a, n] ^ n] -> a
 */
val simplifyNthRootToThePowerOfN = run {
    val radicand = AnyPattern()
    val root = integerOrderRootOf(radicand)
    val exponent = UnsignedIntegerPattern()
    val power = ConditionPattern(
        powerOf(bracketOf(root), exponent),
        numericCondition(root.order, exponent) { n1, n2 -> n1 == n2 }
    )

    Rule(
        pattern = power,
        resultMaker = move(radicand),
        explanationMaker = makeMetadata(Explanation.SimplifyNthRootToThePowerOfN)
    )
}

/**
 * [root[a, n] ^ m] -> [[[root[a, n] ^ n] ^ m/n] when n divides m
 */
val prepareCancellingPowerOfARoot = run {
    val radicand = AnyPattern()
    val root = integerOrderRootOf(radicand)
    val exponent = UnsignedIntegerPattern()
    val power = ConditionPattern(
        powerOf(bracketOf(root), exponent),
        numericCondition(root.order, exponent) { n1, n2 -> n1.divides(n2) && n1 != n2 }
    )

    Rule(
        pattern = power,
        resultMaker = makePowerOf(
            makePowerOf(
                makeRootOf(move(radicand), move(root.order)),
                move(root.order)
            ),
            makeNumericOp(root.order, exponent) { n1, n2 -> n2 / n1 }
        ),
        explanationMaker = makeMetadata(Explanation.PrepareCancellingPowerOfARoot)
    )
}

/**
 * root[[a ^ n], n] -> a
 */
val simplifyNthRootOfNthPower = run {
    val base = AnyPattern()
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(oneOf(bracketOf(base), base), exponent)
    val radical = integerOrderRootOf(power)

    Rule(
        pattern = ConditionPattern(radical, numericCondition(radical.order, exponent) { n1, n2 -> n1 == n2 }),
        resultMaker = move(base),
        explanationMaker = makeMetadata(Explanation.SimplifyNthRootOfNthPower)
    )
}

/**
 * root[[a ^ n], m] -> root[[[a ^ n/m] ^ m], m] when m divides n
 */
val prepareCancellingRootOfAPower = run {
    val base = AnyPattern()
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(base, exponent)
    val radical = integerOrderRootOf(power)

    Rule(
        pattern = ConditionPattern(
            radical,
            numericCondition(radical.order, exponent) { n1, n2 -> n1.divides(n2) && n1 != n2 }
        ),
        resultMaker = makeRootOf(
            makePowerOf(
                makePowerOf(move(base), makeNumericOp(radical.order, exponent) { n1, n2 -> n2 / n1 }),
                move(radical.order)
            ),
            move(radical.order)
        ),
        explanationMaker = makeMetadata(Explanation.PrepareCancellingRootOfAPower)
    )
}

/**
 * [root[a, p] ^ n] -> root[[a ^ n], p]]
 */
val turnPowerOfRootToRootOfPower = run {
    val radicand = UnsignedIntegerPattern()
    val root = integerOrderRootOf(radicand)
    val exponent = UnsignedIntegerPattern()
    val pattern = powerOf(bracketOf(root), exponent)

    Rule(
        pattern = pattern,
        resultMaker = makeRootOf(makePowerOf(move(radicand), move(exponent)), move(root.order)),
        explanationMaker = makeMetadata(Explanation.TurnPowerOfRootToRootOfPower, move(pattern))
    )
}

/**
 * root[root[a, p], q] -> root[a, p * q]
 */
val simplifyRootOfRoot = run {
    val radicand = UnsignedIntegerPattern()
    val innerRoot = integerOrderRootOf(radicand)
    val outerRoot = integerOrderRootOf(innerRoot)

    Rule(
        pattern = outerRoot,
        resultMaker = makeRootOf(move(radicand), makeProductOf(move(outerRoot.order), move(innerRoot.order))),
        explanationMaker = makeMetadata(Explanation.SimplifyRootOfRoot)
    )
}

/**
 * k * root[a, p] -> root[[k ^ p] * a, p]
 */
val putRootCoefficientUnderRoot = run {
    val coefficient = UnsignedIntegerPattern()
    val radicand = UnsignedIntegerPattern()
    val root = integerOrderRootOf(radicand)
    val pattern = productOf(coefficient, root)

    Rule(
        pattern = pattern,
        resultMaker = makeRootOf(
            makeProductOf(
                makePowerOf(move(coefficient), move(root.order)),
                move(radicand)
            ),
            move(root.order)
        ),
        explanationMaker = makeMetadata(Explanation.PutRootCoefficientUnderRoot)
    )
}

/**
 * root[a, p] * root[b, q] -> root[a ^ m / p, m] * root[b ^ m / q, m]
 * where m = lcm(p, q)
 */
val bringRootsToSameIndexInProduct = run {
    val leftRadicand = UnsignedIntegerPattern()
    val leftRoot = integerOrderRootOf(leftRadicand)
    val rightRadicand = UnsignedIntegerPattern()
    val rightRoot = integerOrderRootOf(rightRadicand)
    val product = productContaining(leftRoot, rightRoot)

    Rule(
        pattern = ConditionPattern(
            product,
            numericCondition(leftRoot.order, rightRoot.order) { n1, n2 -> n1 != n2 }
        ),
        resultMaker = substituteIn(
            product,
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
        explanationMaker = makeMetadata(Explanation.BringRootsToSameIndexInProduct)
    )
}

val bringSameIndexSameFactorRootsAsOneRoot = run {
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

    Rule(
        pattern = pattern,
        resultMaker = custom {
            val result = mutableListOf<MappedExpression>()
            val valLeftRadicand = get(leftRadicand)!!

            val terms = if (valLeftRadicand.expr.operator == NaryOperator.Product) valLeftRadicand.children()
            else listOf(valLeftRadicand)

            val indexValue = getValue(orderOfRoot)

            for (term in terms) {
                val powerFactorMatch = powerFactor.findMatches(term).firstOrNull()
                if (powerFactorMatch != null) {
                    result.add(
                        makePowerOf(
                            move(base),
                            makeSumOf(move(exponent), makeNumericOp(exponent) { indexValue - it })
                        ).make(powerFactorMatch)
                    )
                } else {
                    val integerFactorMatch = integerFactor.findMatches(term).firstOrNull()
                    if (integerFactorMatch != null) {
                        result.add(
                            makePowerOf(
                                move(integerFactor),
                                makeSumOf(
                                    FixedExpressionMaker(xp(1)),
                                    FixedExpressionMaker(xp(indexValue - BigInteger.ONE))
                                )
                            ).make(integerFactorMatch)
                        )
                    }
                }
            }

            val resultRadicand = if (result.size == 1) result[0] else mappedExpression(NaryOperator.Product, result)

            substituteIn(
                pattern,
                makeRootOf(
                    resultRadicand,
                    move(orderOfRoot)
                )
            )
        },
        explanationMaker = makeMetadata(Explanation.BringSameIndexSameFactorRootsAsOneRoot)
    )
}

/**
 * root[ [a^p] * [b^p] * [c^p] * ..., p] --> root[[(a * b * c * ... )^p], p]
 */
val combineProductOfSamePowerUnderHigherRoot = run {
    val prod = productContaining()
    val root = integerOrderRootOf(prod)
    val cond = ConditionPattern(root) { match ->
        val order = root.order.getBoundExpr(match)!!
        val product = prod.getBoundExpr(match)!!

        product.operands.all { it.operator == BinaryOperator.Power && it.operands[1] == order }
    }

    val pattern = withOptionalIntegerCoefficient(cond)

    Rule(
        pattern = pattern,
        resultMaker = custom {
            val product = get(prod)!!
            val order = product.nthChild(0).nthChild(1)

            val match = matchPattern(pattern, get(pattern)!!)

            makeSimplifiedProductOf(
                pattern.coefficient(match!!),
                makeRootOf(
                    makePowerOf(
                        makeProductOf(product.children().map { it.nthChild(0) }),
                        order
                    ),
                    move(root.order)
                )
            )
        },
        explanationMaker = makeMetadata(Explanation.CombineProductOfSamePowerUnderHigherRoot)
    )
}
