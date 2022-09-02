package methods.general

import engine.expressions.BracketOperator
import engine.expressions.Constants
import engine.expressions.MappedExpression
import engine.expressions.UnaryOperator
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.TransformationResult
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.bracketOf
import engine.patterns.commutativeProductOf
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.divideBy
import engine.patterns.fractionOf
import engine.patterns.negOf
import engine.patterns.optionalBracketOf
import engine.patterns.optionalDivideBy
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.steps.metadata.metadata

val eliminateOneInProduct = rule {
    val one = FixedPattern(Constants.One)
    val pattern = productContaining(one)

    onPattern(pattern) {
        TransformationResult(
            toExpr = cancel(one, restOf(pattern)),
            explanation = metadata(Explanation.EliminateOneInProduct, move(one))
        )
    }
}

val eliminateLoneOneInExponent = rule {
    val one = FixedPattern(xp(1))
    val base = AnyPattern()
    val pattern = powerOf(base, one)

    onPattern(pattern) {
        TransformationResult(

            toExpr = move(base),
            explanation = metadata(Explanation.EliminateLoneOneInExponent)
        )
    }
}

val eliminateZeroInSum = rule {
    val zero = FixedPattern(Constants.Zero)
    val pattern = sumContaining(zero)

    onPattern(pattern) {
        TransformationResult(
            toExpr = cancel(zero, restOf(pattern)),
            explanation = metadata(Explanation.EliminateZeroInSum, move(zero)),
        )
    }
}

val evaluateProductContainingZero = rule {
    val zero = FixedPattern(Constants.Zero)
    val pattern = productContaining(zero)

    onPattern(pattern) {
        TransformationResult(
            toExpr = transform(zero),
            explanation = metadata(Explanation.EvaluateProductContainingZero, move(zero))
        )
    }
}

val simplifyDoubleMinus = rule {
    val value = AnyPattern()
    val pattern = negOf(
        bracketOf(
            negOf(optionalBracketOf(value))
        )
    )

    onPattern(pattern) {
        TransformationResult(
            toExpr = move(value),
            explanation = metadata(Explanation.SimplifyDoubleMinus, move(value))
        )
    }
}

val simplifyProductWithTwoNegativeFactors = rule {
    val f1 = AnyPattern()
    val f2 = AnyPattern()
    val fd1 = optionalDivideBy(bracketOf(negOf(f1)))
    val fd2 = optionalDivideBy(bracketOf(negOf(f2)))
    val product = productContaining(fd1, fd2)

    onPattern(product) {
        TransformationResult(
            toExpr = product.substitute(
                optionalDivideBy(fd1, move(f1)),
                optionalDivideBy(fd2, move(f2)),
            ),
            explanation = metadata(Explanation.SimplifyProductWithTwoNegativeFactors)
        )
    }
}

val moveSignOfNegativeFactorOutOfProduct = rule {
    val f = AnyPattern()
    val fd = optionalDivideBy(bracketOf(negOf(f)))
    val product = productContaining(fd)

    onPattern(product) {
        TransformationResult(
            toExpr = negOf(product.substitute(optionalDivideBy(fd, move(f)))),
            explanation = metadata(Explanation.MoveSignOfNegativeFactorOutOfProduct)
        )
    }
}

val simplifyUnitFractionToOne = rule {
    val common = AnyPattern()
    val pattern = fractionOf(common, common)

    onPattern(pattern) {
        TransformationResult(
            toExpr = cancel(common, introduce(Constants.One)),
            explanation = metadata(Explanation.SimplifyUnitFractionToOne)
        )
    }
}

val simplifyFractionWithOneDenominator = rule {
    val numerator = AnyPattern()
    val denominator = FixedPattern(Constants.One)
    val pattern = fractionOf(numerator, denominator)

    onPattern(pattern) {
        TransformationResult(
            toExpr = cancel(denominator, move(numerator)),
            explanation = metadata(Explanation.SimplifyFractionWithOneDenominator)
        )
    }
}

val cancelDenominator = rule {
    val common = AnyPattern()
    val numerator = productContaining(common, minSize = 2)
    val pattern = fractionOf(numerator, common)

    onPattern(pattern) {
        TransformationResult(
            toExpr = cancel(common, restOf(numerator)),
            explanation = metadata(Explanation.CancelDenominator)
        )
    }
}

val cancelCommonTerms = rule {
    val common = condition(AnyPattern()) { it != Constants.One }
    val numerator = productContaining(common, minSize = 2)
    val denominator = productContaining(common, minSize = 2)
    val fraction = fractionOf(numerator, denominator)

    onPattern(fraction) {
        TransformationResult(
            toExpr = cancel(common, fractionOf(restOf(numerator), restOf(denominator))),
            explanation = metadata(
                Explanation.CancelCommonTerms,
                move(fraction),
                move(common)
            )
        )
    }
}

val factorMinusFromSum = rule {
    val sum = condition(sumContaining()) { expression ->
        expression.operands.all { it.operator == UnaryOperator.Minus }
    }

    onPattern(sum) {
        TransformationResult(
            toExpr = negOf(sumOf(get(sum)!!.children().map { move(it.children()[0]) })),
            explanation = metadata(Explanation.FactorMinusFromSum)
        )
    }
}

val simplifyProductOfConjugates = rule {
    val a = AnyPattern()
    val b = AnyPattern()
    val sum1 = commutativeSumOf(a, b)
    val sum2 = commutativeSumOf(a, negOf(b))
    val product = commutativeProductOf(bracketOf(sum1), bracketOf(sum2))

    onPattern(product) {
        TransformationResult(
            toExpr = sum2.substitute(
                powerOf(move(a), introduce(Constants.Two)),
                negOf(powerOf(move(b), introduce(Constants.Two))),
            ),
            explanation = metadata(Explanation.SimplifyProductOfConjugates),
        )
    }
}

val distributePowerOfProduct = rule {
    val exponent = AnyPattern()
    val product = productContaining()
    val pattern = powerOf(bracketOf(product), exponent)

    onPattern(pattern) {
        TransformationResult(
            toExpr = productOf(get(product)!!.children().map { powerOf(move(it), move(exponent)) }),
            explanation = metadata(Explanation.DistributePowerOfProduct)
        )
    }
}

val expandBinomialSquared = rule {
    val a = AnyPattern()
    val b = AnyPattern()
    val pattern = powerOf(bracketOf(sumOf(a, b)), FixedPattern(xp(2)))

    onPattern(pattern) {
        TransformationResult(
            toExpr = sumOf(
                powerOf(move(a), introduce(Constants.Two)),
                productOf(introduce(Constants.Two), move(a), move(b)),
                powerOf(move(b), introduce(Constants.Two))
            ),
            explanation = metadata(Explanation.ExpandBinomialSquared)
        )
    }
}

val rewriteDivisionAsFraction = rule {
    val product = productContaining(divideBy(AnyPattern()))

    onPattern(product) {
        val factors = get(product)!!.children()
        val division = factors.indexOfFirst { it.expr.operator == UnaryOperator.DivideBy }

        val result = mutableListOf<MappedExpression>()
        result.addAll(factors.subList(0, division - 1).map { move(it) })

        // We take the opportunity to remove unneeded brackets
        val rawDenominator = factors[division].nthChild(0)
        val denominator = when (rawDenominator.expr.operator) {
            is BracketOperator -> rawDenominator.nthChild(0)
            else -> rawDenominator
        }
        result.add(
            fractionOf(
                move(factors[division - 1]),
                move(denominator)
            )
        )
        result.addAll(factors.subList(division + 1, factors.size).map { move(it) })

        TransformationResult(
            toExpr = productOf(result),
            explanation = metadata(Explanation.RewriteDivisionAsFraction),
        )
    }
}

/**
 * a * (b + c) -> a * b + a * c
 * (b + c + d) * a -> b * a + c * a + d * a
 */
val distributeMultiplicationOverSum = rule {
    val singleTerm = AnyPattern()
    val sum = sumContaining()
    val product = commutativeProductOf(singleTerm, bracketOf(sum))

    onPattern(product) {
        val terms = get(sum)!!.children()

        TransformationResult(
            toExpr = sumOf(
                terms.map {
                    when (it.expr.operator) {
                        UnaryOperator.Minus -> negOf(
                            product.substitute(
                                distribute(singleTerm),
                                move(it.nthChild(0))
                            )
                        )
                        else -> product.substitute(distribute(singleTerm), move(it))
                    }
                }
            ),
            explanation = metadata(Explanation.DistributeMultiplicationOverSum)
        )
    }
}
