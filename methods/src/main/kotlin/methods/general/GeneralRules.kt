package methods.general

import engine.expressionmakers.ExpressionMaker
import engine.expressionmakers.FixedExpressionMaker
import engine.expressionmakers.cancel
import engine.expressionmakers.makeFractionOf
import engine.expressionmakers.makeNegOf
import engine.expressionmakers.makeOptionalDivideBy
import engine.expressionmakers.makePowerOf
import engine.expressionmakers.makeProductOf
import engine.expressionmakers.makeSumOf
import engine.expressionmakers.move
import engine.expressionmakers.restOf
import engine.expressionmakers.substituteIn
import engine.expressionmakers.transform
import engine.expressions.BracketOperator
import engine.expressions.Constants
import engine.expressions.UnaryOperator
import engine.expressions.xp
import engine.methods.Rule
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.OptionalDivideBy
import engine.patterns.bracketOf
import engine.patterns.commutativeProductOf
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.custom
import engine.patterns.divideBy
import engine.patterns.fractionOf
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.sumOf
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

val simplifyUnitFractionToOne = run {
    val common = AnyPattern()
    val pattern = fractionOf(common, common)

    Rule(
        pattern = pattern,
        resultMaker = cancel(common, FixedExpressionMaker(Constants.One)),
        explanationMaker = makeMetadata(Explanation.SimplifyUnitFractionToOne)
    )
}

val simplifyFractionWithOneDenominator = run {
    val numerator = AnyPattern()
    val denominator = FixedPattern(Constants.One)
    val pattern = fractionOf(numerator, denominator)

    Rule(
        pattern = pattern,
        resultMaker = cancel(denominator, move(numerator)),
        explanationMaker = makeMetadata(Explanation.SimplifyFractionWithOneDenominator)
    )
}

val cancelDenominator = run {
    val common = AnyPattern()
    val numerator = productContaining(common, minSize = 2)
    val pattern = fractionOf(numerator, common)

    Rule(
        pattern = pattern,
        resultMaker = cancel(common, restOf(numerator)),
        explanationMaker = makeMetadata(Explanation.CancelDenominator)
    )
}

val cancelCommonTerms = run {
    val common = condition(AnyPattern()) { it != Constants.One }
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

val factorMinusFromSum = run {
    val sum = condition(sumContaining()) { expression ->
        expression.operands.all { it.operator == UnaryOperator.Minus }
    }

    Rule(
        pattern = sum,
        resultMaker = makeNegOf(custom { makeSumOf(get(sum)!!.children().map { it.children()[0] }) }),
        explanationMaker = makeMetadata(Explanation.FactorMinusFromSum),
    )
}

val simplifyProductOfConjugates = run {
    val a = AnyPattern()
    val b = AnyPattern()
    val sum1 = commutativeSumOf(a, b)
    val sum2 = commutativeSumOf(a, negOf(b))
    val product = commutativeProductOf(bracketOf(sum1), bracketOf(sum2))

    Rule(
        pattern = product,
        resultMaker = substituteIn(
            sum2,
            makePowerOf(move(a), FixedExpressionMaker(xp(2))),
            makeNegOf(makePowerOf(move(b), FixedExpressionMaker(xp(2)))),
        ),
        explanationMaker = makeMetadata(Explanation.SimplifyProductOfConjugates),
    )
}

val distributePowerOfProduct = run {
    val exponent = AnyPattern()
    val product = productContaining()
    val pattern = powerOf(bracketOf(product), exponent)

    Rule(
        pattern = pattern,
        resultMaker = custom {
            makeProductOf(get(product)!!.children().map { makePowerOf(it, move(exponent)) })
        },
        explanationMaker = makeMetadata(Explanation.DistributePowerOfProduct)
    )
}

val expandBinomialSquared = run {
    val a = AnyPattern()
    val b = AnyPattern()
    val pattern = powerOf(bracketOf(sumOf(a, b)), FixedPattern(xp(2)))

    Rule(
        pattern = pattern,
        resultMaker = makeSumOf(
            makePowerOf(move(a), FixedExpressionMaker(xp(2))),
            makeProductOf(FixedExpressionMaker(xp(2)), move(a), move(b)),
            makePowerOf(move(b), FixedExpressionMaker(xp(2)))
        ),
        explanationMaker = makeMetadata(Explanation.ExpandBinomialSquared)
    )
}

val rewriteDivisionAsFraction = run {
    val product = productContaining(divideBy(AnyPattern()))

    Rule(
        pattern = product,
        resultMaker = custom {
            val factors = get(product)!!.children()
            val division = factors.indexOfFirst { it.expr.operator == UnaryOperator.DivideBy }

            val result = mutableListOf<ExpressionMaker>()
            result.addAll(factors.subList(0, division - 1))

            // We take the opportunity to remove unneeded brackets
            val rawDenominator = factors[division].nthChild(0)
            val denominator = when (rawDenominator.expr.operator) {
                is BracketOperator -> rawDenominator.nthChild(0)
                else -> rawDenominator
            }
            result.add(makeFractionOf(factors[division - 1], denominator))
            result.addAll(factors.subList(division + 1, factors.size))

            makeProductOf(result)
        },
        explanationMaker = makeMetadata(Explanation.RewriteDivisionAsFraction),
    )
}
