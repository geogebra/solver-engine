package methods.integerrationalexponents

import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.xp
import engine.methods.TransformationResult
import engine.methods.rule
import engine.operators.BinaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata
import engine.utility.gcd
import engine.utility.hasFactorOfDegree
import engine.utility.primeFactorDecomposition
import method.integerrationalexponents.Explanation
import java.math.BigInteger

val factorizeIntegerUnderRationalExponent = rule {
    val integer = UnsignedIntegerPattern()
    val expNum = UnsignedIntegerPattern()
    val expDen = UnsignedIntegerPattern()
    val exp = fractionOf(expNum, expDen)
    val pattern = powerOf(integer, exp)

    onPattern(
        ConditionPattern(
            pattern,
            integerCondition(expNum, expDen, integer) { l, m, n ->
                // here ideally we shouldn't use "primeFactorDecomposition"
                // but rather "smartFactorDecomposition" (i.e. factorization of for e.g.
                // 250000 as [500^2] instead of [ ([2^4] * [5^6]) ^ 2] as,
                // which is to be handled in another (already in backlog) ticket
                val primeFactorization = n.primeFactorDecomposition()
                // when at-least one of the prime factor has degree
                // greater than or equal to denominator of rational exponent
                // or when the gcd of multiplicity of prime factors with
                // denominator of rational exponent is not equal to 1
                n.pow(l.toInt()).hasFactorOfDegree(m.toInt()) ||
                    primeFactorization.fold(m) { acc, f -> acc.gcd(f.second) } != BigInteger.ONE
            }
        )
    ) {
        val factorized = getValue(integer)
            .primeFactorDecomposition()
            .map { (f, n) -> introduce(if (n == BigInteger.ONE) xp(f) else powerOf(xp(f), xp(n))) }

        TransformationResult(
            toExpr = powerOf(productOf(factorized), move(exp)),
            explanation = metadata(Explanation.FactorizeIntegerUnderRationalExponent),
            skills = listOf(metadata(Skill.FactorInteger, move(integer)))
        )
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
