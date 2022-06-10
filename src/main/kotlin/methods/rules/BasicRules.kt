package methods.rules

import engine.expressionmakers.cancel
import engine.expressionmakers.makeNegOf
import engine.expressionmakers.move
import engine.expressionmakers.restOf
import engine.expressionmakers.substituteIn
import engine.expressionmakers.transform
import engine.expressions.xp
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.bracketOf
import engine.patterns.negOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.rules.Rule
import engine.steps.metadata.Explanation
import engine.steps.metadata.makeMetadata

val eliminateZeroInSum = run {
    val zero = FixedPattern(xp(0))
    val pattern = sumContaining(zero)

    Rule(
        pattern = pattern,
        resultMaker = cancel(zero, restOf(pattern)),
        explanationMaker = makeMetadata(Explanation.EliminateZeroInSum, move(zero)),
    )
}

val zeroInProduct = run {
    val zero = FixedPattern(xp(0))
    val pattern = productContaining(zero)

    Rule(
        pattern = pattern,
        resultMaker = transform(zero),
        explanationMaker = makeMetadata(Explanation.ProductContainingZero, move(zero))
    )
}

val simplifyDoubleNegBracket = run {
    val value = AnyPattern()
    val pattern = negOf(bracketOf(negOf(bracketOf(value))))

    Rule(
        pattern = pattern,
        resultMaker = move(value),
        explanationMaker = makeMetadata(Explanation.SimplifyDoubleMinus, move(value))
    )
}

val simplifyDoubleNeg = run {
    val value = AnyPattern()
    val pattern = negOf(bracketOf(negOf(value)))

    Rule(
        pattern = pattern,
        resultMaker = move(value),
        explanationMaker = makeMetadata(Explanation.SimplifyDoubleMinus, move(value))
    )
}

val simplifyProductWithTwoNegativeFactors = run {
    val f1 = AnyPattern()
    val f2 = AnyPattern()
    val product = productContaining(bracketOf(negOf(f1)), bracketOf(negOf(f2)))

    Rule(
        pattern = product,
        resultMaker = substituteIn(product, move(f1), move(f2)),
        explanationMaker = makeMetadata(Explanation.SimplifyTwoNegativeFactorsInProduct)
    )
}

val moveSignOfNegativeFactorOutOfProduct = run {
    val f = AnyPattern()
    val product = productContaining(bracketOf(negOf(f)))

    Rule(
        pattern = product,
        resultMaker = makeNegOf(substituteIn(product, move(f))),
        explanationMaker = makeMetadata(Explanation.MoveSignOfNegativeFactorOutOfProduct)
    )
}
