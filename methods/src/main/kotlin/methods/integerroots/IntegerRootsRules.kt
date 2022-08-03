package methods.integerroots

import engine.expressionmakers.FixedExpressionMaker
import engine.expressionmakers.FlattenedNaryExpressionMaker
import engine.expressionmakers.makeDivideBy
import engine.expressionmakers.makeNumericOp
import engine.expressionmakers.makePowerOf
import engine.expressionmakers.makeProductOf
import engine.expressionmakers.makeSquareRootOf
import engine.expressionmakers.move
import engine.expressionmakers.restOf
import engine.expressionmakers.substituteIn
import engine.expressionmakers.transform
import engine.expressions.NaryOperator
import engine.expressions.powerOf
import engine.expressions.xp
import engine.methods.Rule
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.custom
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
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
