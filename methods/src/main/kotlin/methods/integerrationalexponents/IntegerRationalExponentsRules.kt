package methods.integerrationalexponents

import engine.conditions.Sign
import engine.conditions.signOf
import engine.expressions.Constants
import engine.expressions.fractionOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
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
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.withOptionalRationalCoefficient
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata
import engine.utility.divides
import engine.utility.isFactorizableUnderRationalExponent
import engine.utility.isPrime
import engine.utility.primeFactorDecomposition
import method.integerrationalexponents.Explanation
import java.math.BigInteger

enum class IntegerRationalExponentsRules(override val runner: Rule) : RunnerMethod {
    EvaluateNegativeToRationalExponentAsUndefined(
        rule {
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
    ),

    FactorizeIntegerUnderRationalExponent(
        rule {
            val integer = integerCondition(UnsignedIntegerPattern()) { !it.isPrime() }
            val exp = IntegerFractionPattern()
            val power = powerOf(integer, exp)

            onPattern(power) {
                val integerValue = getValue(integer)
                val expNum = getValue(exp.numerator)
                val expDen = getValue(exp.denominator)
                if (integerValue.isFactorizableUnderRationalExponent(expNum, expDen)) {
                    val primeFactorization = getValue(integer).primeFactorDecomposition()
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
    ),

    /**
     * brings the "integers" or exponents with integral powers to the front
     * e.g. [2 ^ [2 / 5]] * [3 ^ 2] * 5 --> [3 ^ 2] * 5 * [2 ^ [2 / 5]]
     */
    NormaliseProductWithRationalExponents(
        rule {
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
                        it.operator == BinaryExpressionOperator.Power &&
                            it.operands[1].operator == BinaryExpressionOperator.Fraction
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
    ),

    FindCommonDenominatorOfRationalExponents(
        rule {
            val base1 = AnyPattern()
            val base2 = AnyPattern()

            val exponent1 = IntegerFractionPattern()
            val exponent2 = IntegerFractionPattern()

            val power1 = powerOf(base1, exponent1)
            val power2 = powerOf(base2, exponent2)

            val product = productContaining(power1, power2)
            val fraction = fractionOf(power1, power2)

            onPattern(oneOf(product, fraction)) {
                if (getValue(exponent1.denominator) == getValue(exponent2.denominator)) {
                    null
                } else {
                    val expandingTerm1 =
                        integerOp(exponent1.denominator, exponent2.denominator) { n1, n2 -> n2 / n1.gcd(n2) }
                    val expandingTerm2 =
                        integerOp(exponent1.denominator, exponent2.denominator) { n1, n2 -> n1 / n1.gcd(n2) }

                    val fraction1 = when (expandingTerm1) {
                        Constants.One -> move(exponent1)
                        else -> fractionOf(
                            productOf(move(exponent1.numerator), expandingTerm1),
                            productOf(move(exponent1.denominator), expandingTerm1)
                        )
                    }

                    val fraction2 = when (expandingTerm2) {
                        Constants.One -> move(exponent2)
                        else -> fractionOf(
                            productOf(move(exponent2.numerator), expandingTerm2),
                            productOf(move(exponent2.denominator), expandingTerm2)
                        )
                    }

                    val result = when {
                        isBound(product) -> product.substitute(
                            powerOf(move(base1), fraction1),
                            powerOf(move(base2), fraction2)
                        )

                        else -> fractionOf(
                            powerOf(move(base1), fraction1),
                            powerOf(move(base2), fraction2)
                        )
                    }

                    TransformationResult(
                        toExpr = result,
                        explanation = metadata(Explanation.FindCommonDenominatorOfRationalExponents)
                    )
                }
            }
        }
    ),

    FactorDenominatorOfRationalExponents(
        rule {
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
            val fraction = fractionOf(power1, power2)

            onPattern(oneOf(product, fraction)) {
                val newPower1 = simplifiedPowerOf(move(base1), move(numerator1))
                val newPower2 = simplifiedPowerOf(move(base2), move(numerator2))
                val newExponent = fractionOf(introduce(Constants.One), factor(denominator))

                val result = when {
                    isBound(product) -> product.substitute(
                        powerOf(
                            productOf(newPower1, newPower2),
                            newExponent
                        )
                    )
                    else -> powerOf(fractionOf(newPower1, newPower2), newExponent)
                }

                TransformationResult(
                    toExpr = result,
                    explanation = metadata(Explanation.FactorDenominatorOfRationalExponents)
                )
            }
        }
    ),

    CollectLikeRationalPowers(
        rule {
            val common = powerOf(UnsignedIntegerPattern(), IntegerFractionPattern())

            val commonTerm1 = withOptionalRationalCoefficient(common)
            val commonTerm2 = withOptionalRationalCoefficient(common)
            val sum = sumContaining(commonTerm1, commonTerm2)

            onPattern(sum) {
                TransformationResult(
                    toExpr = collectLikeTermsInSum(get(sum)!!, withOptionalRationalCoefficient(common)),
                    explanation = metadata(Explanation.CollectLikeRationalPowers)
                )
            }
        }
    )
}
