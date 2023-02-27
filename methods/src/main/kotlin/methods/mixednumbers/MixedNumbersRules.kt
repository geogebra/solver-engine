package methods.mixednumbers

import engine.expressions.Constants
import engine.expressions.fractionOf
import engine.expressions.mixedNumberOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.ConditionPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeSumOf
import engine.patterns.mixedNumberOf
import engine.patterns.numericCondition
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata
import methods.general.GeneralExplanation
import java.math.BigInteger

enum class MixedNumbersRules(override val runner: Rule) : RunnerMethod {
    SplitMixedNumber(
        rule {
            val integer = UnsignedIntegerPattern()
            val numerator = UnsignedIntegerPattern()
            val denominator = UnsignedIntegerPattern()
            val mixedNumber = mixedNumberOf(integer, numerator, denominator)

            onPattern(mixedNumber) {
                when {
                    getValue(denominator) == BigInteger.ZERO -> ruleResult(
                        toExpr = transformTo(mixedNumber, Constants.Undefined),
                        explanation = metadata(
                            GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined,
                            fractionOf(move(numerator), move(denominator)),
                        ),
                    )

                    getValue(numerator) == BigInteger.ZERO -> ruleResult(
                        toExpr = move(integer),
                        explanation = metadata(GeneralExplanation.SimplifyZeroNumeratorFractionToZero),
                    )

                    else -> ruleResult(
                        toExpr = sumOf(
                            move(integer),
                            fractionOf(move(numerator), move(denominator)),
                        ),
                        explanation = metadata(Explanation.ConvertMixedNumberToSum, move(mixedNumber)),
                    )
                }
            }
        },
    ),

    ConvertSumOfIntegerAndProperFractionToMixedNumber(
        rule {
            val integer = UnsignedIntegerPattern()
            val fraction = IntegerFractionPattern()
            val sum = commutativeSumOf(integer, fraction)

            onPattern(sum) {
                val numeratorValue = getValue(fraction.numerator)
                val denominatorValue = getValue(fraction.denominator)

                when {
                    numeratorValue < denominatorValue -> ruleResult(
                        toExpr = mixedNumberOf(
                            move(integer),
                            move(fraction.numerator),
                            move(fraction.denominator),
                        ),
                        explanation = metadata(Explanation.ConvertSumOfIntegerAndProperFractionToMixedNumber),
                    )

                    else -> null
                }
            }
        },
    ),

    FractionToMixedNumber(
        rule {
            val fraction = IntegerFractionPattern()
            val improperFractionCondition = numericCondition(
                fraction.numerator,
                fraction.denominator,
            ) { n1, n2 -> n1 > n2 }
            val improperFraction = ConditionPattern(fraction, improperFractionCondition)

            onPattern(improperFraction) {
                val quotient = integerOp(fraction.numerator, fraction.denominator) { n, d -> n / d }
                val remainder = integerOp(fraction.numerator, fraction.denominator) { n, d -> n % d }

                ruleResult(
                    toExpr = mixedNumberOf(quotient, remainder, move(fraction.denominator)),
                    explanation = metadata(Explanation.ConvertFractionToMixedNumber),
                    skills = listOf(
                        metadata(Skill.DivisionWithRemainder, move(fraction.numerator), move(fraction.denominator)),
                    ),
                )
            }
        },
    ),
}
