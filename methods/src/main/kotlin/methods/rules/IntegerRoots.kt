package methods.rules

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
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.custom
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.rootOf
import engine.patterns.squareRootOf
import engine.rules.Rule
import engine.steps.metadata.Explanation
import engine.steps.metadata.Skill
import engine.steps.metadata.makeMetadata
import engine.utility.hasFactorOfDegree
import engine.utility.isEven
import engine.utility.isOdd
import engine.utility.primeFactorDecomposition
import java.math.BigInteger

val rootOfOne = run {
    val one = FixedPattern(xp(1))

    Rule(
        pattern = oneOf(squareRootOf(one), rootOf(one, AnyPattern())),
        resultMaker = move(one),
        explanationMaker = makeMetadata(Explanation.RootOfOne, move(one)),
    )
}

val rootOfZero = run {
    val zero = FixedPattern(xp(0))

    Rule(
        pattern = oneOf(squareRootOf(zero), rootOf(zero, AnyPattern())),
        resultMaker = move(zero),
        explanationMaker = makeMetadata(Explanation.RootOfZero, move(zero)),
    )
}

val factorizeIntegerUnderSquareRoot = run {
    val integer = UnsignedIntegerPattern()
    val root = squareRootOf(integer)

    Rule(
        pattern = ConditionPattern(root, numericCondition(integer) { it.hasFactorOfDegree(2) }),
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

val separateIntegerPowersUnderSquareRoot = run {
    val base = UnsignedIntegerPattern()
    val exponent = UnsignedIntegerPattern()
    val exponentCondition = ConditionPattern(
        exponent,
        numericCondition(exponent) { it.isOdd() && it != BigInteger.ONE }
    )
    val power = powerOf(base, exponentCondition)
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

val separateSquaresUnderSquareRoot = run {
    val base = UnsignedIntegerPattern()
    val exponent = UnsignedIntegerPattern()
    val exponentCondition = ConditionPattern(exponent, numericCondition(exponent) { it.isEven() })
    val square = powerOf(base, exponentCondition)
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

val simplifySquareRootOfPower = run {
    val base = UnsignedIntegerPattern()
    val exponent = UnsignedIntegerPattern()
    val exponentCondition = ConditionPattern(exponent, numericCondition(exponent) { it.isEven() })
    val square = powerOf(base, exponentCondition)
    val root = squareRootOf(square)

    Rule(
        pattern = root,
        resultMaker = makePowerOf(
            move(base),
            makeProductOf(move(exponent), makeDivideBy(FixedExpressionMaker(xp(2))))
        ),
        explanationMaker = makeMetadata(Explanation.SimplifySquareRootOfPower, move(exponent)),
    )
}
