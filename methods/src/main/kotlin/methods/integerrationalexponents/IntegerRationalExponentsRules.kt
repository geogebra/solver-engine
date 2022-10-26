package methods.integerrationalexponents

import engine.conditions.Sign
import engine.conditions.signOf
import engine.expressions.Constants
import engine.expressions.fractionOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.xp
import engine.methods.TransformationResult
import engine.methods.rule
import engine.operators.BinaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata
import engine.utility.divides
import engine.utility.isPrime
import engine.utility.isZero
import engine.utility.primeFactorDecomposition
import method.integerrationalexponents.Explanation
import java.math.BigInteger

val evaluateNegativeToRationalExponentAsUndefined = rule {
    val base = AnyPattern()

    val exponent = IntegerFractionPattern()
    val pattern = powerOf(
        condition(base) { it.signOf() == Sign.NEGATIVE },
        ConditionPattern(
            optionalNegOf(exponent),
            integerCondition(exponent.numerator, exponent.denominator) { n, d -> !d.divides(n) }
        )
    )

    onPattern(pattern) {
        TransformationResult(
            toExpr = transformTo(pattern, Constants.Undefined),
            explanation = metadata(Explanation.EvaluateNegativeToRationalExponentAsUndefined),
        )
    }
}

val factorizeIntegerUnderRationalExponent = rule {
    val integer = integerCondition(UnsignedIntegerPattern()) { !it.isPrime() }
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
        val canBeSimplified = primeFactorization.any { (_, p) -> (p * numeratorValue % denominatorValue).isZero() } ||
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

val bringRationalExponentsToSameDenominator = rule {
    val base1 = AnyPattern()
    val base2 = AnyPattern()

    val exponent1 = IntegerFractionPattern()
    val exponent2 = IntegerFractionPattern()

    val power1 = powerOf(base1, exponent1)
    val power2 = powerOf(base2, exponent2)

    val product = productContaining(power1, power2)

    onPattern(product) {
        if (getValue(exponent1.denominator) == getValue(exponent2.denominator)) {
            null
        } else {
            val expandingTerm1 = integerOp(exponent1.denominator, exponent2.denominator) { n1, n2 -> n2 / n1.gcd(n2) }
            val expandingTerm2 = integerOp(exponent1.denominator, exponent2.denominator) { n1, n2 -> n1 / n1.gcd(n2) }

            val fraction1 = when (expandingTerm1.expr) {
                Constants.One -> move(exponent1)
                else -> fractionOf(
                    productOf(move(exponent1.numerator), expandingTerm1),
                    productOf(move(exponent1.denominator), expandingTerm1)
                )
            }

            val fraction2 = when (expandingTerm2.expr) {
                Constants.One -> move(exponent2)
                else -> fractionOf(
                    productOf(move(exponent2.numerator), expandingTerm2),
                    productOf(move(exponent2.denominator), expandingTerm2)
                )
            }

            TransformationResult(
                toExpr = product.substitute(powerOf(move(base1), fraction1), powerOf(move(base2), fraction2)),
                explanation = metadata(Explanation.BringRationalExponentsToSameDenominator)
            )
        }
    }
}

val factorDenominatorOfRationalExponents = rule {
    val base1 = AnyPattern()
    val base2 = AnyPattern()

    val numerator1 = UnsignedIntegerPattern()
    val numerator2 = UnsignedIntegerPattern()

    val denominator = UnsignedIntegerPattern()

    val exponent1 = fractionOf(numerator1, denominator)
    val exponent2 = fractionOf(numerator2, denominator)

    val power1 = powerOf(base1, exponent1)
    val power2 = powerOf(base2, exponent2)

    val product = productContaining(power1, power2)

    onPattern(product) {
        TransformationResult(
            toExpr = product.substitute(
                powerOf(
                    productOf(
                        simplifiedPowerOf(move(base1), move(numerator1)),
                        simplifiedPowerOf(move(base2), move(numerator2))
                    ),
                    fractionOf(introduce(Constants.One), move(denominator))
                )
            ),
            explanation = metadata(Explanation.FactorDenominatorOfRationalExponents)
        )
    }
}
