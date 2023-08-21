package methods.decimals

import engine.expressions.equationOf
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.statementSystemOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.ConditionPattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.commutativeEquationOf
import engine.patterns.divideBy
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.statementSystemOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalIntegerCoefficient
import engine.steps.Transformation
import engine.steps.metadata.DragTargetPosition
import engine.steps.metadata.metadata
import engine.utility.Factorizer
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.max
import engine.steps.metadata.GmPathModifier as PM

private val MAX_POWER = 64.toBigInteger()

@Suppress("MagicNumber")
private val five = BigInteger.valueOf(5)

enum class DecimalRules(override val runner: Rule) : RunnerMethod {
    ConvertTerminatingDecimalToFraction(
        rule {
            val decimal = UnsignedNumberPattern()

            onPattern(decimal) {
                val decimalValue = getValue(decimal)

                val scale = decimalValue.scale()
                when {
                    scale > 0 -> {
                        // E.g. if decimalValue = 6.75 then entireNumber = 675
                        val entireNumber = decimalValue.scaleByPowerOfTen(scale).toBigInteger()
                        val denominator = BigInteger.TEN.pow(scale)

                        ruleResult(
                            toExpr = fractionOf(
                                introduce(xp(entireNumber)),
                                introduce(xp(denominator)),
                            ),
                            gmAction = edit(decimal),
                            explanation = metadata(Explanation.ConvertTerminatingDecimalToFraction),
                        )
                    }

                    else -> null
                }
            }
        },
    ),

    ConvertRecurringDecimalToFractionDirectly(
        rule {
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

                ruleResult(
                    toExpr = fractionOf(
                        sumOf(introduce(xp(entireNumber)), negOf(introduce(xp(nonRecurringPart)))),
                        introduce(xp(denominator)),
                    ),
                    gmAction = edit(decimal),
                    explanation = metadata(Explanation.ConvertRecurringDecimalToFractionDirectly),
                )
            }
        },
    ),

    ConvertRecurringDecimalToEquation(
        rule {
            val decimal = RecurringDecimalPattern()

            onPattern(decimal) {
                ruleResult(
                    toExpr = equationOf(introduce(xp("x")), move(decimal)),
                    gmAction = edit(decimal),
                    explanation = metadata(Explanation.ConvertRecurringDecimalToEquation),
                )
            }
        },
    ),

    MakeEquationSystemForRecurringDecimal(
        rule {
            val variable = ArbitraryVariablePattern()
            val decimal = RecurringDecimalPattern()

            val equation = commutativeEquationOf(variable, decimal)

            onPattern(equation) {
                val recurringDecimalValue = getValue(decimal)

                val scale1 = recurringDecimalValue.decimalDigits - recurringDecimalValue.repeatingDigits
                val scale2 = recurringDecimalValue.decimalDigits

                val scaledDecimal1 = recurringDecimalValue.movePointRight(scale1)
                val scaledDecimal2 = recurringDecimalValue.movePointRight(scale2)

                val scaledEquation1 = if (scale1 == 0) {
                    move(equation)
                } else {
                    equationOf(
                        productOf(introduce(xp(BigInteger.TEN.pow(scale1))), move(variable)),
                        introduce(xp(scaledDecimal1)),
                    )
                }

                val scaledEquation2 = equationOf(
                    productOf(introduce(xp(BigInteger.TEN.pow(scale2))), move(variable)),
                    introduce(xp(scaledDecimal2)),
                )

                val steps = mutableListOf<Transformation>()

                if (scale1 != 0) {
                    steps.add(
                        Transformation(
                            type = Transformation.Type.Rule,
                            fromExpr = get(equation),
                            toExpr = scaledEquation1,
                            explanation = metadata(
                                Explanation.MultiplyRecurringDecimal,
                                move(variable),
                                introduce(xp(scale1)),
                            ),
                        ),
                    )
                }
                steps.add(
                    Transformation(
                        type = Transformation.Type.Rule,
                        fromExpr = get(equation),
                        toExpr = scaledEquation2,
                        explanation = metadata(
                            Explanation.MultiplyRecurringDecimal,
                            move(variable),
                            introduce(xp(scale2)),
                        ),
                    ),
                )

                ruleResult(
                    toExpr = statementSystemOf(scaledEquation1, scaledEquation2),
                    explanation = metadata(Explanation.MakeEquationSystemForRecurringDecimal),
                    steps = steps,
                )
            }
        },
    ),

    SimplifyEquationSystemForRecurringDecimal(
        rule {
            val variable = ArbitraryVariablePattern()
            val decimal1 = RecurringDecimalPattern()
            val decimal2 = RecurringDecimalPattern()
            val lhs1 = withOptionalIntegerCoefficient(variable, true)
            val lhs2 = withOptionalIntegerCoefficient(variable, true)

            val equation1 = commutativeEquationOf(lhs1, decimal1)
            val equation2 = commutativeEquationOf(lhs2, decimal2)

            val equationSystem = statementSystemOf(equation1, equation2)

            onPattern(equationSystem) {
                val d1 = getValue(decimal1)
                val d2 = getValue(decimal2)

                when {
                    d1.repetend == d2.repetend -> ruleResult(
                        toExpr = equationOf(
                            productOf(
                                integerOp(lhs1.integerCoefficient, lhs2.integerCoefficient) { n1, n2 -> n2 - n1 },
                                move(variable),
                            ),
                            combineTo(
                                decimal1,
                                decimal2,
                                xp((d2.nonRepeatingValue - d1.nonRepeatingValue).toBigInteger()),
                            ),
                        ),
                        explanation = metadata(Explanation.SimplifyEquationSystemForRecurringDecimal),
                    )

                    else -> null
                }
            }
        },
    ),

    SolveLinearEquation(
        rule {
            val variable = ArbitraryVariablePattern()
            val coefficient = SignedIntegerPattern()
            val rhs = SignedIntegerPattern()

            val equation = commutativeEquationOf(productOf(coefficient, variable), rhs)

            onPattern(equation) {
                ruleResult(
                    toExpr = fractionOf(move(rhs), move(coefficient)),
                    gmAction = drag(coefficient, rhs),
                    explanation = metadata(Explanation.SolveLinearEquation),
                )
            }
        },
    ),

    /**
     * If a fraction [x / y] has decimal numerator and denominator, multiply them by a power of 10 f
     * so that x*f and y*f are integers, unless x == y (in that case the fraction would be equal to 1).
     */
    MultiplyFractionOfDecimalsByPowerOfTen(
        rule {
            val numerator = UnsignedNumberPattern()
            val denominator = UnsignedNumberPattern()

            val fraction = fractionOf(numerator, denominator)

            onPattern(fraction) {
                val numeratorValue = getValue(numerator)
                val denominatorValue = getValue(denominator)
                val maxDecimalPlaces = max(numeratorValue.scale(), denominatorValue.scale())
                val multiplier = introduce(xp(BigInteger.TEN.pow(maxDecimalPlaces)))
                when {
                    maxDecimalPlaces > 0 && numeratorValue != denominatorValue -> ruleResult(
                        toExpr = fractionOf(
                            productOf(get(numerator), multiplier),
                            productOf(get(denominator), multiplier),
                        ),
                        gmAction = edit(fraction),
                        explanation = metadata(
                            Explanation.MultiplyFractionOfDecimalsByPowerOfTen,
                            move(fraction),
                            multiplier,
                        ),
                    )
                    else -> null
                }
            }
        },
    ),

    TurnDivisionOfDecimalsIntoFraction(
        rule {
            val numerator = SignedNumberPattern()
            val denominator = SignedNumberPattern()
            val product = productContaining(numerator, divideBy(denominator))

            onPattern(product) {
                ruleResult(
                    toExpr = product.substitute(fractionOf(move(numerator), move(denominator))),
                    gmAction = drag(denominator, numerator, dragToPosition = DragTargetPosition.Below),
                    explanation = metadata(
                        Explanation.TurnDivisionOfDecimalsIntoFraction,
                        move(numerator),
                        move(denominator),
                    ),
                )
            }
        },
    ),

    /**
     * Multiply out decimals in product and also divide if the dividend and divisor are integers.
     */
    EvaluateDecimalProductAndDivision(
        rule {
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
                            divisor,
                        ) { n1, n2 -> n2.stripTrailingZeros().scale() <= 0 && n1 % n2 == BigDecimal.ZERO },
                    ),
                ),
            )

            onPattern(product) {
                when {
                    isBound(multiplier) -> ruleResult(
                        toExpr = product.substitute(
                            numericOp(base, multiplier) { n1, n2 ->
                                // stripTrailingZeros is required because otherwise integer result remain decimals
                                // with a non-zero scale, which means the system does not recognize them as integers
                                // We should think about how to deal with this better.
                                (n1 * n2).stripTrailingZeros()
                            },
                        ),
                        gmAction = drag(multiplier, base),
                        explanation = metadata(Explanation.EvaluateDecimalProduct, move(base), move(multiplier)),
                    )

                    else -> ruleResult(
                        toExpr = product.substitute(
                            numericOp(base, divisor) { n1, n2 ->
                                // See comment in previous case
                                (n1 / n2).stripTrailingZeros()
                            },
                        ),
                        gmAction = drag(divisor, base),
                        explanation = metadata(Explanation.EvaluateDecimalDivision, move(base), move(divisor)),
                    )
                }
            }
        },
    ),

    StripTrailingZerosAfterDecimal(
        rule {
            val num = SignedNumberPattern()

            onPattern(num) {
                val n = numericOp(num) { n -> n.stripTrailingZeros() }
                if (get(num) == n) return@onPattern null

                ruleResult(
                    toExpr = transform(num, n),
                    explanation = metadata(Explanation.StripTrailingZerosAfterDecimal),
                )
            }
        },
    ),

    EvaluateSignedDecimalAddition(
        rule {
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

                ruleResult(
                    toExpr = sum.substitute(numericOp(term1, term2) { n1, n2 -> (n1 + n2).stripTrailingZeros() }),
                    gmAction = drag(term2, term1),
                    explanation = explanation,
                )
            }
        },
    ),

    ExpandFractionToPowerOfTenDenominator(
        rule {
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
                val expandWith = xp(
                    BigInteger.TWO.pow(maxPower - powerOfTwo) *
                        five.pow(maxPower - powerOfFive),
                )

                ruleResult(
                    toExpr = fractionOf(
                        productOf(get(numerator), introduce(expandWith)),
                        productOf(get(denominator), introduce(expandWith)),
                    ),
                    gmAction = edit(fraction),
                    explanation = metadata(Explanation.ExpandFractionToPowerOfTenDenominator),
                )
            }
        },
    ),

    ConvertFractionWithPowerOfTenDenominatorToDecimal(
        rule {
            val numerator = UnsignedIntegerPattern()
            val denominator = UnsignedIntegerPattern()

            val fraction = fractionOf(numerator, denominator)

            onPattern(fraction) {
                val factorizer = Factorizer(getValue(denominator))
                val powerOfTen = factorizer.extractMultiplicity(BigInteger.TEN)

                if (!factorizer.fullyFactorized() || powerOfTen == 0) {
                    return@onPattern null
                }

                ruleResult(
                    toExpr = numericOp(numerator) { it.movePointLeft(powerOfTen) },
                    gmAction = doubleTap(fraction, PM.FractionBar),
                    explanation = metadata(Explanation.ConvertFractionWithPowerOfTenDenominatorToDecimal),
                )
            }
        },
    ),

    EvaluateDecimalPowerDirectly(
        rule {
            val base = SignedNumberPattern()
            val exponent = integerCondition(UnsignedIntegerPattern()) { it <= MAX_POWER }
            val power = powerOf(base, exponent)

            onPattern(power) {
                ruleResult(
                    toExpr = numericOp(base, exponent) { n1, n2 -> n1.pow(n2.toInt()) },
                    gmAction = tap(exponent),
                    explanation = metadata(Explanation.EvaluateDecimalPowerDirectly, move(base), move(exponent)),
                )
            }
        },
    ),
}
