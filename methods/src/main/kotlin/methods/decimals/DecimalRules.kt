package methods.decimals

import engine.expressions.equationOf
import engine.expressions.equationSystemOf
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.TransformationResult
import engine.methods.rule
import engine.patterns.ConditionPattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.UnsignedDecimalPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.VariablePattern
import engine.patterns.divideBy
import engine.patterns.equationOf
import engine.patterns.equationSystemOf
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalIntegerCoefficient
import engine.steps.Transformation
import engine.steps.metadata.metadata
import engine.utility.Factorizer
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.max

val convertTerminatingDecimalToFraction = rule {
    val decimal = UnsignedDecimalPattern()

    onPattern(decimal) {
        val decimalValue = getValue(decimal)

        val scale = decimalValue.scale()
        when {
            scale > 0 -> {
                // E.g. if decimalValue = 6.75 then entireNumber = 675
                val entireNumber = decimalValue.scaleByPowerOfTen(scale).toBigInteger()
                val denominator = BigInteger.TEN.pow(scale)

                TransformationResult(
                    toExpr = fractionOf(
                        introduce(xp(entireNumber)),
                        introduce(xp(denominator))
                    ),
                    explanation = metadata(Explanation.ConvertTerminatingDecimalToFraction)
                )
            }

            else -> null
        }
    }
}

val convertRecurringDecimalToFractionDirectly = rule {
    val decimal = RecurringDecimalPattern()

    onPattern(decimal) {
        val recurringDecimalValue = getValue(decimal)

        val scale1 = recurringDecimalValue.decimalDigits
        val scale2 = recurringDecimalValue.decimalDigits - recurringDecimalValue.repeatingDigits

        // E.g. recurringDecimalValue = 3.14[15]
        // Then entireNumber = 31415
        // And nonRecurringPart = 314
        val entireNumber = recurringDecimalValue.nonRepeatingValue.scaleByPowerOfTen(scale1).toBigInteger()
        val nonRecurringPart = recurringDecimalValue.nonRepeatingValue.scaleByPowerOfTen(scale2).toBigInteger()

        val denominator = BigDecimal.ONE.scaleByPowerOfTen(scale1) - BigDecimal.ONE.scaleByPowerOfTen(scale2)

        TransformationResult(
            toExpr = fractionOf(
                sumOf(introduce(xp(entireNumber)), negOf(introduce(xp(nonRecurringPart)))),
                introduce(xp(denominator))
            ),
            explanation = metadata(Explanation.ConvertRecurringDecimalToFractionDirectly)
        )
    }
}

val convertRecurringDecimalToEquation = rule {
    val decimal = RecurringDecimalPattern()

    onPattern(decimal) {
        TransformationResult(
            toExpr = equationOf(introduce(xp("x")), move(decimal)),
            explanation = metadata(Explanation.ConvertRecurringDecimalToEquation)
        )
    }
}

val makeEquationSystemForRecurringDecimal = rule {
    val variable = VariablePattern()
    val decimal = RecurringDecimalPattern()

    val equation = equationOf(variable, decimal)

    onPattern(equation) {
        val recurringDecimalValue = getValue(decimal)

        val scale1 = recurringDecimalValue.decimalDigits - recurringDecimalValue.repeatingDigits
        val scale2 = recurringDecimalValue.decimalDigits

        val scaledDecimal1 = recurringDecimalValue.movePointRight(scale1)
        val scaledDecimal2 = recurringDecimalValue.movePointRight(scale2)

        val scaledEquation1 = if (scale1 == 0) move(equation) else equationOf(
            productOf(introduce(xp(BigInteger.TEN.pow(scale1))), move(variable)),
            introduce(xp(scaledDecimal1))
        )

        val scaledEquation2 = equationOf(
            productOf(introduce(xp(BigInteger.TEN.pow(scale2))), move(variable)),
            introduce(xp(scaledDecimal2))
        )

        val steps = mutableListOf<Transformation>()

        if (scale1 != 0) {
            steps.add(
                Transformation(
                    fromExpr = get(equation)!!,
                    toExpr = scaledEquation1,
                    explanation = metadata(Explanation.MultiplyRecurringDecimal, move(variable), introduce(xp(scale1)))
                )
            )
        }
        steps.add(
            Transformation(
                fromExpr = get(equation)!!,
                toExpr = scaledEquation2,
                explanation = metadata(Explanation.MultiplyRecurringDecimal, move(variable), introduce(xp(scale2)))
            )
        )

        TransformationResult(
            toExpr = equationSystemOf(scaledEquation1, scaledEquation2),
            explanation = metadata(Explanation.MakeEquationSystemForRecurringDecimal),
            steps = steps
        )
    }
}

val simplifyEquationSystemForRecurringDecimal = rule {
    val variable = VariablePattern()
    val decimal1 = RecurringDecimalPattern()
    val decimal2 = RecurringDecimalPattern()
    val lhs1 = withOptionalIntegerCoefficient(variable)
    val lhs2 = withOptionalIntegerCoefficient(variable)

    val equation1 = equationOf(lhs1, decimal1)
    val equation2 = equationOf(lhs2, decimal2)

    val equationSystem = equationSystemOf(equation1, equation2)

    onPattern(equationSystem) {
        val d1 = getValue(decimal1)
        val d2 = getValue(decimal2)

        when {
            d1.repetend == d2.repetend -> TransformationResult(
                toExpr = equationOf(
                    productOf(integerOp(lhs1.coefficient, lhs2.coefficient) { n1, n2 -> n2 - n1 }, move(variable)),
                    combineTo(decimal1, decimal2, xp((d2.nonRepeatingValue - d1.nonRepeatingValue).toBigInteger()))
                ),
                explanation = metadata(Explanation.SimplifyEquationSystemForRecurringDecimal)
            )

            else -> null
        }
    }
}

val solveLinearEquation = rule {
    val variable = VariablePattern()
    val coefficient = SignedIntegerPattern()
    val rhs = SignedIntegerPattern()

    val equation = equationOf(productOf(coefficient, variable), rhs)

    onPattern(equation) {
        TransformationResult(
            toExpr = fractionOf(move(rhs), move(coefficient)),
            explanation = metadata(Explanation.SolveLinearEquation)
        )
    }
}

/**
 * If a fraction [x / y] has decimal numerator and denominator, multiply them by a power of 10 f
 * so that x*f and y*f are integers, unless x == y (in that case the fraction would be equal to 1).
 */
val multiplyFractionOfDecimalsByPowerOfTen = rule {
    val numerator = UnsignedDecimalPattern()
    val denominator = UnsignedDecimalPattern()

    val fraction = fractionOf(numerator, denominator)

    onPattern(fraction) {
        val numeratorValue = getValue(numerator)
        val denominatorValue = getValue(denominator)
        val maxDecimalPlaces = max(numeratorValue.scale(), denominatorValue.scale())
        val multiplier = introduce(xp(BigInteger.TEN.pow(maxDecimalPlaces)))
        when {
            maxDecimalPlaces > 0 && numeratorValue != denominatorValue -> TransformationResult(
                toExpr = fractionOf(productOf(move(numerator), multiplier), productOf(move(denominator), multiplier)),
                explanation = metadata(Explanation.MultiplyFractionOfDecimalsByPowerOfTen, move(fraction), multiplier)
            )
            else -> null
        }
    }
}

val turnDivisionOfDecimalsIntoFraction = rule {
    val numerator = SignedNumberPattern()
    val denominator = SignedNumberPattern()
    val product = productContaining(numerator, divideBy(denominator))

    onPattern(product) {
        TransformationResult(
            toExpr = product.substitute(fractionOf(move(numerator), move(denominator))),
            explanation = metadata(Explanation.TurnDivisionOfDecimalsIntoFraction, move(numerator), move(denominator))
        )
    }
}

/**
 * Multiply out decimals in product and also divide if the dividend and divisor are integers.
 */
val evaluateDecimalProductAndDivision = rule {
    val base = SignedNumberPattern()
    val multiplier = SignedNumberPattern()
    val divisor = SignedNumberPattern()
    val product = productContaining(
        base,
        oneOf(
            multiplier,
            ConditionPattern(
                divideBy(divisor),
                numericCondition(
                    base,
                    divisor
                ) { n1, n2 -> n2.stripTrailingZeros().scale() <= 0 && n1 % n2 == BigDecimal.ZERO }
            )
        )
    )

    onPattern(product) {
        when {
            isBound(multiplier) -> TransformationResult(
                toExpr = product.substitute(
                    numericOp(base, multiplier) { n1, n2 ->
                        // stripTrailingZeros is required because otherwise integer result remain decimals with
                        // a non-zero scale, which means the system does not recognize them as integers
                        // We should think about how to deal with this better.
                        (n1 * n2).stripTrailingZeros()
                    }
                ),
                explanation = metadata(Explanation.EvaluateDecimalProduct, move(base), move(multiplier))
            )

            else -> TransformationResult(
                toExpr = product.substitute(
                    numericOp(base, divisor) { n1, n2 ->
                        // See comment in previous case
                        (n1 / n2).stripTrailingZeros()
                    }
                ),
                explanation = metadata(Explanation.EvaluateDecimalDivision, move(base), move(divisor))
            )
        }
    }
}

val evaluateSignedDecimalAddition = rule {
    val term1 = SignedNumberPattern()
    val term2 = SignedNumberPattern()
    val sum = sumContaining(term1, term2)

    onPattern(sum) {
        val explanation = when {
            getValue(term1) > BigDecimal.ZERO && getValue(term2) < BigDecimal.ZERO ->
                metadata(Explanation.EvaluateDecimalSubtraction, move(term1), move(term2.unsignedPattern))

            else ->
                metadata(Explanation.EvaluateDecimalAddition, move(term1), move(term2))
        }

        TransformationResult(
            toExpr = sum.substitute(numericOp(term1, term2) { n1, n2 -> n1 + n2 }),
            explanation = explanation
        )
    }
}

@Suppress("MagicNumber")
private val five = BigInteger.valueOf(5)

val expandFractionToPowerOfTenDenominator = rule {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val fraction = fractionOf(numerator, denominator)

    onPattern(fraction) {
        val factorizer = Factorizer(getValue(denominator))

        val powerOfTwo = factorizer.extractMultiplicity(BigInteger.TWO)
        val powerOfFive = factorizer.extractMultiplicity(five)

        if (!factorizer.fullyFactorized() || powerOfFive == powerOfTwo) {
            return@onPattern null
        }
        val maxPower = max(powerOfTwo, powerOfFive)
        val expandWith = xp(BigInteger.TWO.pow(maxPower - powerOfTwo) * five.pow(maxPower - powerOfFive))

        TransformationResult(
            toExpr = fractionOf(
                productOf(move(numerator), introduce(expandWith)),
                productOf(move(denominator), introduce(expandWith))
            ),
            explanation = metadata(Explanation.ExpandFractionToPowerOfTenDenominator)
        )
    }
}

val convertFractionWithPowerOfTenDenominatorToDecimal = rule {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val fraction = fractionOf(numerator, denominator)

    onPattern(fraction) {
        val factorizer = Factorizer(getValue(denominator))
        val powerOfTen = factorizer.extractMultiplicity(BigInteger.TEN)

        if (!factorizer.fullyFactorized() || powerOfTen == 0) {
            return@onPattern null
        }

        TransformationResult(
            toExpr = numericOp(numerator) { it.movePointLeft(powerOfTen) },
            explanation = metadata(Explanation.ConvertFractionWithPowerOfTenDenominatorToDecimal)
        )
    }
}

private val MAX_POWER = 64.toBigInteger()

val evaluateDecimalPowerDirectly = rule {
    val base = SignedNumberPattern()
    val exponent = integerCondition(UnsignedIntegerPattern()) { it <= MAX_POWER }
    val power = powerOf(base, exponent)

    onPattern(power) {
        TransformationResult(
            toExpr = numericOp(base, exponent) { n1, n2 -> n1.pow(n2.toInt()) },
            explanation = metadata(Explanation.EvaluateDecimalPowerDirectly, move(base), move(exponent))
        )
    }
}
