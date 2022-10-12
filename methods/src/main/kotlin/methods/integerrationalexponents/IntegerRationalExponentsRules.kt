package methods.integerrationalexponents

import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.xp
import engine.methods.TransformationResult
import engine.methods.rule
import engine.operators.BinaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata
import engine.utility.primeFactorDecomposition
import method.integerrationalexponents.Explanation
import java.math.BigInteger

val factorizeIntegerUnderRationalExponent = rule {
    val integer = UnsignedIntegerPattern()
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val exp = fractionOf(numerator, denominator)
    val power = powerOf(integer, exp)

    onPattern(power) {
        val numeratorValue = getValue(numerator)
        val denominatorValue = getValue(denominator)

        val primeFactorization = getValue(integer).primeFactorDecomposition()

        // when at-least one of the prime factor has degree
        // greater than or equal to denominator of rational exponent
        // or when the gcd of multiplicity of prime factors with
        // denominator of rational exponent is not equal to 1
        val canBeSimplified = primeFactorization.any { (_, p) -> p * numeratorValue >= denominatorValue } ||
            primeFactorization.fold(denominatorValue) { acc, f -> acc.gcd(f.second) } != BigInteger.ONE

        if (canBeSimplified) {
            val factorized = primeFactorization
                .map { (f, n) -> introduce(if (n == BigInteger.ONE) xp(f) else powerOf(xp(f), xp(n))) }

            TransformationResult(
                toExpr = powerOf(productOf(factorized), move(exp)),
                explanation = metadata(Explanation.FactorizeIntegerUnderRationalExponent),
                skills = listOf(metadata(Skill.FactorInteger, move(integer)))
            )
        } else {
            null
        }
    }
}

/**
 * brings the "integers" or exponents with integral powers to the front
 * e.g. [2 ^ [2 / 5]] * [3 ^ 2] * 5 --> [3 ^ 2] * 5 * [2 ^ [2 / 5]]
 */
val normaliseProductWithRationalExponents = rule {
    val notRationalExponent =
        condition(AnyPattern()) {
            it.operator != BinaryExpressionOperator.Power ||
                it.operands[1].operator != BinaryExpressionOperator.Fraction
        }
    val product = productContaining(
        powerOf(UnsignedIntegerPattern(), fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())),
        notRationalExponent
    )
    onPattern(product) {
        val (rationalExponents, nonRationalExponents) = get(product)!!.children()
            .partition {
                it.expr.operator == BinaryExpressionOperator.Power &&
                    it.expr.operands[1].operator == BinaryExpressionOperator.Fraction
            }
        TransformationResult(
            toExpr = productOf(
                productOf(nonRationalExponents.map { move(it) }),
                productOf(rationalExponents.map { move(it) })
            ),
            explanation = metadata(Explanation.NormaliseProductWithRationalExponents)
        )
    }
}
