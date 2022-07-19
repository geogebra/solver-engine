package methods.general

import engine.expressionmakers.cancel
import engine.expressionmakers.makeFractionOf
import engine.expressionmakers.makeNegOf
import engine.expressionmakers.makeOptionalDivideBy
import engine.expressionmakers.move
import engine.expressionmakers.restOf
import engine.expressionmakers.substituteIn
import engine.expressionmakers.transform
import engine.expressions.xp
import engine.methods.Rule
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.OptionalDivideBy
import engine.patterns.bracketOf
import engine.patterns.fractionOf
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.metadata.makeMetadata

val eliminateOneInProduct = run {
    val one = FixedPattern(xp(1))
    val pattern = productContaining(one)

    Rule(
        pattern = pattern,
        resultMaker = cancel(one, restOf(pattern)),
        explanationMaker = makeMetadata(Explanation.EliminateOneInProduct, move(one))
    )
}

val eliminateZeroInSum = run {
    val zero = FixedPattern(xp(0))
    val pattern = sumContaining(zero)

    Rule(
        pattern = pattern,
        resultMaker = cancel(zero, restOf(pattern)),
        explanationMaker = makeMetadata(Explanation.EliminateZeroInSum, move(zero)),
    )
}

val evaluateProductContainingZero = run {
    val zero = FixedPattern(xp(0))
    val pattern = productContaining(zero)

    Rule(
        pattern = pattern,
        resultMaker = transform(zero),
        explanationMaker = makeMetadata(Explanation.EvaluateProductContainingZero, move(zero))
    )
}

val simplifyDoubleMinus = run {
    val value = AnyPattern()
    val pattern = negOf(
        bracketOf(
            negOf(
                oneOf(
                    value,
                    bracketOf(value)
                )
            )
        )
    )

    Rule(
        pattern = pattern,
        resultMaker = move(value),
        explanationMaker = makeMetadata(Explanation.SimplifyDoubleMinus, move(value))
    )
}

val simplifyProductWithTwoNegativeFactors = run {
    val f1 = AnyPattern()
    val f2 = AnyPattern()
    val fd1 = OptionalDivideBy(bracketOf(negOf(f1)))
    val fd2 = OptionalDivideBy(bracketOf(negOf(f2)))
    val product = productContaining(fd1, fd2)

    Rule(
        pattern = product,
        resultMaker = substituteIn(
            product,
            makeOptionalDivideBy(fd1, move(f1)),
            makeOptionalDivideBy(fd2, move(f2)),
        ),
        explanationMaker = makeMetadata(Explanation.SimplifyProductWithTwoNegativeFactors)
    )
}

val moveSignOfNegativeFactorOutOfProduct = run {
    val f = AnyPattern()
    val fd = OptionalDivideBy(bracketOf(negOf(f)))
    val product = productContaining(fd)

    Rule(
        pattern = product,
        resultMaker = makeNegOf(substituteIn(product, makeOptionalDivideBy(fd, move(f)))),
        explanationMaker = makeMetadata(Explanation.MoveSignOfNegativeFactorOutOfProduct)
    )
}

val cancelCommonTerms = run {
    val common = AnyPattern()
    val numerator = productContaining(common, minSize = 2)
    val denominator = productContaining(common, minSize = 2)
    val pattern = fractionOf(numerator, denominator)

    Rule(
        pattern = pattern,
        resultMaker = cancel(common, makeFractionOf(restOf(numerator), restOf(denominator))),
        explanationMaker = makeMetadata(
            Explanation.CancelCommonTerms,
            move(pattern),
            move(common)
        ),
    )
}
