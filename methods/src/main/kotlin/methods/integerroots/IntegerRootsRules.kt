package methods.integerroots

import engine.expressionmakers.FixedExpressionMaker
import engine.expressionmakers.FlattenedNaryExpressionMaker
import engine.expressionmakers.makeBracketOf
import engine.expressionmakers.makeDivideBy
import engine.expressionmakers.makeNumericOp
import engine.expressionmakers.makePowerOf
import engine.expressionmakers.makeProductOf
import engine.expressionmakers.makeSquareRootOf
import engine.expressionmakers.move
import engine.expressionmakers.restOf
import engine.expressionmakers.substituteIn
import engine.expressionmakers.transform
import engine.expressions.MappedExpression
import engine.expressions.NaryOperator
import engine.expressions.mappedExpression
import engine.expressions.powerOf
import engine.expressions.xp
import engine.methods.Rule
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.bracketOf
import engine.patterns.condition
import engine.patterns.custom
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.rootOf
import engine.patterns.squareRootOf
import engine.steps.metadata.Skill
import engine.steps.metadata.makeMetadata
import engine.utility.hasFactorOfDegree
import engine.utility.isEven
import engine.utility.isOdd
import engine.utility.primeFactorDecomposition
import java.math.BigInteger

val simplifyRootOfOne = run {
    val one = FixedPattern(xp(1))

    Rule(
        pattern = oneOf(squareRootOf(one), rootOf(one, AnyPattern())),
        resultMaker = move(one),
        explanationMaker = makeMetadata(Explanation.SimplifyRootOfOne, move(one)),
    )
}

val simplifyRootOfZero = run {
    val zero = FixedPattern(xp(0))

    Rule(
        pattern = oneOf(squareRootOf(zero), rootOf(zero, AnyPattern())),
        resultMaker = move(zero),
        explanationMaker = makeMetadata(Explanation.SimplifyRootOfZero, move(zero)),
    )
}

val factorizeIntegerUnderSquareRoot = run {
    val integer = numericCondition(UnsignedIntegerPattern()) { it.hasFactorOfDegree(2) }
    val root = squareRootOf(integer)

    Rule(
        pattern = root,
        resultMaker = custom {
            val factorized = getValue(integer)
                .primeFactorDecomposition()
                .map { (f, n) -> FixedExpressionMaker(if (n == BigInteger.ONE) xp(f) else powerOf(xp(f), xp(n))) }
            makeSquareRootOf(transform(integer, FlattenedNaryExpressionMaker(NaryOperator.Product, factorized)))
        },
        explanationMaker = makeMetadata(Explanation.FactorizeNumberUnderSquareRoot, move(integer)),
        skillMakers = listOf(makeMetadata(Skill.FactorInteger, move(integer)))
    )
}

val separateOddPowersUnderSquareRoot = run {
    val base = UnsignedIntegerPattern()
    val exponent = numericCondition(UnsignedIntegerPattern()) { it.isOdd() && it != BigInteger.ONE }
    val power = powerOf(base, exponent)
    val product = productContaining(power)
    val root = squareRootOf(product)

    Rule(
        pattern = root,
        resultMaker = makeSquareRootOf(
            substituteIn(
                product,
                makeProductOf(
                    makePowerOf(move(base), makeNumericOp(exponent) { it - BigInteger.ONE }),
                    move(base),
                )
            )
        ),
        explanationMaker = makeMetadata(Explanation.SeparateIntegerPowersUnderSquareRoot, move(base), move(exponent)),
    )
}

val splitEvenPowersUnderSeparateRoot = run {
    val base = UnsignedIntegerPattern()
    val exponent = numericCondition(UnsignedIntegerPattern()) { it.isEven() }
    val square = powerOf(base, exponent)
    val product = productContaining(square, minSize = 2)
    val root = squareRootOf(product)

    Rule(
        pattern = root,
        resultMaker = makeProductOf(
            makeSquareRootOf(move(square)),
            makeSquareRootOf(restOf(product)),
        ),
        explanationMaker = makeMetadata(Explanation.SeparateSquaresUnderSquareRoot, move(base), move(exponent)),
    )
}

val simplifySquareRootOfSquare = run {
    val base = UnsignedIntegerPattern()
    val square = powerOf(base, FixedPattern(xp(2)))
    val root = squareRootOf(square)

    Rule(
        pattern = root,
        resultMaker = move(base),
        explanationMaker = makeMetadata(Explanation.SimplifySquareRootOfSquare),
    )
}

val simplifySquareRootOfPower = run {
    val base = UnsignedIntegerPattern()
    val exponent = UnsignedIntegerPattern()
    val square = powerOf(base, exponent)
    val root = squareRootOf(square)

    Rule(
        pattern = root,
        resultMaker = custom {
            if (getValue(exponent) == BigInteger.TWO) {
                move(base)
            } else if (getValue(exponent).isEven()) {
                makePowerOf(
                    move(base),
                    makeProductOf(move(exponent), makeDivideBy(FixedExpressionMaker(xp(2))))
                )
            } else {
                makeProductOf(
                    makePowerOf(
                        move(base),
                        makeProductOf(
                            makeNumericOp(exponent) { it - BigInteger.ONE },
                            makeDivideBy(FixedExpressionMaker(xp(2)))
                        )
                    ),
                    makeSquareRootOf(move(base))
                )
            }
        },
        explanationMaker = makeMetadata(Explanation.SimplifySquareRootOfPower, move(exponent))
    )
}

/*
sqrt[a] * sqrt[a] --> a
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
 * separates all factors under the square root as
 * product of square root of factors
 *
 * ex:
 * sqrt[[2^3] * 5 * [7^2]] --> sqrt[[2^3]] * sqrt[5] * sqrt[[7^2]]
 */
val separateFactorizedPowersUnderSquareRootAsSquareRoots = run {
    val prod = productContaining()
    val pattern = squareRootOf(prod)

    Rule(
        pattern = pattern,
        resultMaker = custom {
            FlattenedNaryExpressionMaker(
                NaryOperator.Product,
                get(prod)!!.children().map { makeSquareRootOf(it) }
            )
        },
        explanationMaker = makeMetadata(Explanation.SeparateFactorizedPowersUnderSquareRootAsSquareRoots)
    )
}

/**
 * Splits multiplication of square root of exponents with
 * odd exponent values
 * ex:
 * sqrt[[2^5]] * sqrt[[3^4]] * sqrt[7] ... -> sqrt[[2^4] * 2] * sqrt[[3^4]] * sqrt[7]
 */
val splitPowerUnderSquareRootOfProduct = run {
    val exponent = numericCondition(UnsignedIntegerPattern()) { it.isOdd() && it != BigInteger.ONE }
    val base = UnsignedIntegerPattern()
    val oddPower = powerOf(base, exponent)
    val squareRootOfOddPower = squareRootOf(oddPower)
    val prod = condition(productContaining()) { expression ->
        expression.operands.any { squareRootOfOddPower.matches(it) }
    }

    Rule(
        pattern = prod,
        resultMaker = custom {
            val result = mutableListOf<MappedExpression>()
            val terms = get(prod)!!.children()
            for (term in terms) {
                val match = squareRootOfOddPower.findMatches(term).firstOrNull()
                if (match != null) {
                    result.add(
                        makeSquareRootOf(
                            makeProductOf(
                                makePowerOf(move(base), makeNumericOp(exponent) { it - BigInteger.ONE }),
                                move(base),
                            )
                        ).make(match)
                    )
                } else {
                    result.add(term.toMappedExpr())
                }
            }

            mappedExpression(NaryOperator.Product, result)
        },
        explanationMaker = makeMetadata(Explanation.SplitPowerUnderSquareRootOfProduct)
    )
}

/**
 * splits product of exponent term with an integer as product of squareRoot
 * of exponent term and squareRoot of integer term to (possible) multiple
 * such terms in a product
 * ex:
 * sqrt[[2^2] * 2] * sqrt[[3^4] * 3] * sqrt[7] --> sqrt[[2^2]] * sqrt[2] * sqrt[[3^4]] * sqrt[3] * sqrt[7]
 */
val splitProductOfPowerUnderSquareRootAsProductMultipleRemoveBrackets = run {
    val exponentTerm = powerOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
    val integerTerm = UnsignedIntegerPattern()
    val factorPattern = productOf(exponentTerm, integerTerm)
    val squareRootPattern = squareRootOf(factorPattern)
    val prod = condition(productContaining()) { expression ->
        expression.operands.any { squareRootPattern.matches(it) }
    }

    Rule(
        pattern = prod,
        resultMaker = custom {
            val result = mutableListOf<MappedExpression>()
            val terms = get(prod)!!.children()
            for (term in terms) {
                val match = squareRootPattern.findMatches(term).firstOrNull()
                if (match != null) {
                    result.add(makeSquareRootOf(move(exponentTerm)).make(match))
                    result.add(makeSquareRootOf(move(integerTerm)).make(match))
                } else {
                    result.add(term.toMappedExpr())
                }
            }

            mappedExpression(NaryOperator.Product, result)
        },
        explanationMaker = makeMetadata(Explanation.SplitProductOfPowerUnderSquareRootAsProductMultipleRemoveBrackets)
    )
}

/**
 * For ex:
 * sqrt[[2^2]] * sqrt[2] * sqrt[[3^4]] --> 2 * sqrt[2] * [3^2]
 */
val simplifyEvenIntegerPowerUnderRootProduct = run {
    val exponent = numericCondition(UnsignedIntegerPattern()) { it.isEven() }
    val base = UnsignedIntegerPattern()
    val evenPower = powerOf(base, exponent)
    val squareRootOfEvenPower = squareRootOf(evenPower)
    val prod = condition(productContaining()) { expression ->
        expression.operands.any { squareRootOfEvenPower.matches(it) }
    }

    Rule(
        pattern = prod,
        resultMaker = custom {
            val result = mutableListOf<MappedExpression>()
            val terms = get(prod)!!.children()
            for (term in terms) {
                val match = squareRootOfEvenPower.findMatches(term).firstOrNull()
                if (match != null) {
                    if (exponent.getBoundInt(match) == BigInteger.TWO) {
                        result.add(move(base).make(match))
                    } else {
                        result.add(
                            makePowerOf(move(base), makeNumericOp(exponent) { it.divide(BigInteger.TWO) }).make(match)
                        )
                    }
                } else {
                    result.add(term.toMappedExpr())
                }
            }

            mappedExpression(NaryOperator.Product, result)
        },
        explanationMaker = makeMetadata(Explanation.SimplifyEvenIntegerPowerUnderRootProduct)
    )
}

/**
 * moves the integer or power factors to the begging of the product
 * For ex:
 * [2^2] * sqrt[2] * [3^2] * sqrt[3] --> ([2^2] * [3^2]) * (sqrt[2] * sqrt[3])
 */
val rewriteWithIntegerFactorsAtFront = run {
    val nonSquareRootFactor = oneOf(
        UnsignedIntegerPattern(),
        powerOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
    )
    val squareRootFactor = squareRootOf(UnsignedIntegerPattern())
    val prod = condition(productContaining()) { expression ->
        expression.operands.all { nonSquareRootFactor.matches(it) || squareRootFactor.matches(it) }
    }

    Rule(
        pattern = prod,
        resultMaker = custom {
            val children = get(prod)!!.children()
            val (nonSquareRootFactors, squareRootFactors) = children.partition { nonSquareRootFactor.matches(it.expr) }

            makeProductOf(
                makeBracketOf(
                    makeProductOf(nonSquareRootFactors)
                ),
                makeBracketOf(
                    makeProductOf(squareRootFactors)
                )
            )
        },
        explanationMaker = makeMetadata(Explanation.RewriteWithIntegerFactorsAtFront)
    )
}

/**
 * Simplifies the multiplication of product containing squareRoot
 * of integers. For ex:
 * 10 * (sqrt[2] * sqrt[3] * sqrt[7]) --> 10 * sqrt[42]
 */
val multiplySquareRootFactors = run {
    val factor = UnsignedIntegerPattern()
    val nonSquareRootFactor = UnsignedIntegerPattern()
    val squareRootFactor = squareRootOf(factor)
    val prod = condition(productContaining()) { expression ->
        expression.operands.all { squareRootFactor.matches(it) }
    }

    Rule(
        pattern = productOf(nonSquareRootFactor, bracketOf(prod)),
        resultMaker = custom {
            var result = BigInteger.ONE
            val terms = get(prod)!!.children()
            for (term in terms) {
                val match = squareRootFactor.findMatches(term).firstOrNull()
                if (match != null) {
                    result *= factor.getBoundInt(match)
                }
            }

            makeProductOf(
                move(nonSquareRootFactor),
                makeSquareRootOf(FixedExpressionMaker(xp(result)))
            )
        },
        explanationMaker = makeMetadata(Explanation.MultiplySquareRootFactors)
    )
}
