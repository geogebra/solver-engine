package methods.inequalities

import engine.expressions.Constants
import engine.expressions.Fraction
import engine.expressions.Variable
import engine.expressions.closedRangeOf
import engine.expressions.contradictionOf
import engine.expressions.equationOf
import engine.expressions.fractionOf
import engine.expressions.greaterThanEqualOf
import engine.expressions.greaterThanOf
import engine.expressions.identityOf
import engine.expressions.inverse
import engine.expressions.isNeg
import engine.expressions.lessThanEqualOf
import engine.expressions.lessThanOf
import engine.expressions.negOf
import engine.expressions.notEqualOf
import engine.expressions.openRangeOf
import engine.expressions.productOf
import engine.expressions.setSolutionOf
import engine.expressions.simplifiedNegOf
import engine.expressions.simplifiedProductOf
import engine.expressions.statementUnionOf
import engine.expressions.variableListOf
import engine.methods.Rule
import engine.methods.RuleResultBuilder
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.FixedPattern
import engine.patterns.Pattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.absoluteValueOf
import engine.patterns.condition
import engine.patterns.greaterThanEqualOf
import engine.patterns.greaterThanOf
import engine.patterns.inequalityOf
import engine.patterns.lessThanEqualOf
import engine.patterns.lessThanOf
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.withOptionalConstantCoefficient
import engine.sign.Sign
import engine.steps.Transformation
import engine.steps.metadata.metadata
import methods.solvable.countAbsoluteValues

enum class InequalitiesRules(override val runner: Rule) : RunnerMethod {

    ExtractSolutionFromConstantInequality(
        rule {
            val lhs = UnsignedNumberPattern()
            val rhs = UnsignedNumberPattern()

            val inequality = inequalityOf(lhs, rhs)

            onPattern(inequality) {
                trueOrFalseRuleResult(inequality, inequality.holdsFor(getValue(lhs), getValue(rhs)))
            }
        },
    ),

    ExtractSolutionFromConstantInequalityBasedOnSign(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            val inequality = inequalityOf(lhs, rhs)

            onPattern(condition(inequality) { it.isConstant() }) {
                val lhsSign = get(lhs).signOf()
                val rhsSign = get(rhs).signOf()

                if (lhsSign.isKnown() && rhsSign.isKnown()) {
                    val isSatisfied = inequality.holdsFor(lhsSign.signum.toBigDecimal(), rhsSign.signum.toBigDecimal())
                    trueOrFalseRuleResult(inequality, isSatisfied)
                } else {
                    null
                }
            }
        },
    ),

    SeparateModulusGreaterThanPositiveConstant(separateModulusGreaterThanPositiveConstant),

    SeparateModulusGreaterThanEqualToPositiveConstant(separateModulusGreaterThanEqualToPositiveConstant),

    ExtractSolutionFromModulusLessThanNonPositiveConstant(extractSolutionFromModulusLessThanNonPositiveConstant),

    ExtractSolutionFromModulusGreaterThanEqualToNonPositiveConstant(
        extractSolutionFromModulusGreaterThanEqualToNonPositiveConstant,
    ),

    ExtractSolutionFromModulusGreaterThanNegativeConstant(extractSolutionFromModulusGreaterThanNegativeConstant),

    ReduceModulusLessThanEqualToZeroInequalityToEquation(reduceModulusLessThanEqualToZeroInequalityToEquation),

    ConvertModulusGreaterThanZero(convertModulusGreaterThanZero),

    ConvertModulusLessThanPositiveConstant(convertModulusLessThanPositiveConstant),

    ConvertModulusLessThanEqualToPositiveConstant(convertModulusLessThanEqualToPositiveConstant),

    ExtractSolutionFromInequalityInSolvedForm(
        rule {
            val lhs = SolutionVariablePattern()
            val rhs = ConstantInSolutionVariablePattern()

            val inequality = inequalityOf(lhs, rhs)

            onPattern(inequality) {
                ruleResult(
                    toExpr = setSolutionOf(variableListOf(move(lhs) as Variable), inequality.toInterval(get(rhs))),
                    explanation = metadata(Explanation.ExtractSolutionFromInequalityInSolvedForm),
                )
            }
        },
    ),

    FlipInequality(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            val inequality = inequalityOf(lhs, rhs)

            onPattern(inequality) {
                ruleResult(
                    toExpr = inequality.dualInequality(move(rhs), move(lhs)),
                    explanation = metadata(Explanation.FlipInequality),
                )
            }
        },
    ),

    NegateBothSides(
        rule {
            val variable = SolutionVariablePattern()
            val absoluteValueContainingLHS = condition { it.countAbsoluteValues(solutionVariables) > 0 }
            val unsignedLHS = oneOf(variable, absoluteValueContainingLHS)
            val lhs = negOf(unsignedLHS)
            val rhs = AnyPattern()

            val inequality = inequalityOf(lhs, rhs)

            onPattern(inequality) {
                ruleResult(
                    toExpr = inequality.dualInequality(
                        move(unsignedLHS),
                        simplifiedNegOf(move(rhs)),
                    ),
                    explanation = metadata(Explanation.NegateBothSidesAndFlipTheSign),
                )
            }
        },
    ),

    MultiplyByInverseCoefficientOfVariable(
        rule {
            val lhs = withOptionalConstantCoefficient(SolutionVariablePattern())
            val rhs = AnyPattern()

            val inequality = inequalityOf(lhs, rhs)

            onPattern(inequality) {
                val coefficient = get(lhs::coefficient)!!

                if (coefficient is Fraction || (coefficient.isNeg() && coefficient.firstChild is Fraction)) {
                    val inverse = introduce(coefficient, coefficient.inverse())

                    when (inverse.signOf()) {
                        Sign.POSITIVE -> ruleResult(
                            toExpr = inequality.sameInequality(
                                productOf(get(lhs), inverse),
                                productOf(get(rhs), inverse),
                            ),
                            explanation = metadata(Explanation.MultiplyByInverseCoefficientOfVariable),
                        )
                        Sign.NEGATIVE -> ruleResult(
                            toExpr = inequality.dualInequality(
                                productOf(get(lhs), inverse),
                                productOf(get(rhs), inverse),
                            ),
                            explanation = metadata(Explanation.MultiplyByInverseCoefficientOfVariableAndFlipTheSign),
                        )
                        else -> null
                    }
                } else {
                    null
                }
            }
        },
    ),

    DivideByCoefficientOfVariable(
        rule {
            val lhs = withOptionalConstantCoefficient(SolutionVariablePattern())
            val rhs = AnyPattern()

            val inequality = inequalityOf(lhs, rhs)

            onPattern(inequality) {
                val coefficientValue = get(lhs::coefficient)!!
                if (coefficientValue == Constants.One) return@onPattern null

                val coefficient = introduce(coefficientValue, coefficientValue)

                when (coefficient.signOf()) {
                    Sign.POSITIVE -> ruleResult(
                        toExpr = inequality.sameInequality(
                            fractionOf(get(lhs), coefficient),
                            fractionOf(get(rhs), coefficient),
                        ),
                        explanation = metadata(Explanation.DivideByCoefficientOfVariable),
                    )
                    Sign.NEGATIVE -> ruleResult(
                        toExpr = inequality.dualInequality(
                            fractionOf(get(lhs), coefficient),
                            fractionOf(get(rhs), coefficient),
                        ),
                        explanation = metadata(Explanation.DivideByCoefficientOfVariableAndFlipTheSign),
                    )
                    else -> null
                }
            }
        },
    ),
}

private fun RuleResultBuilder.trueOrFalseRuleResult(inequality: Pattern, isSatisfied: Boolean): Transformation {
    val noVariable = context.solutionVariables.isEmpty()
    val variableList = variableListOf(context.solutionVariables)
    val toExpr = if (isSatisfied) {
        identityOf(variableList, get(inequality))
    } else {
        contradictionOf(variableList, get(inequality))
    }
    return if (noVariable) {
        val key = if (isSatisfied) {
            Explanation.ExtractTruthFromTrueInequality
        } else {
            Explanation.ExtractFalsehoodFromFalseInequality
        }
        ruleResult(toExpr = toExpr, explanation = metadata(key))
    } else {
        val key = if (isSatisfied) {
            Explanation.ExtractSolutionFromTrueInequality
        } else {
            Explanation.ExtractSolutionFromFalseInequality
        }
        ruleResult(toExpr = toExpr, explanation = metadata(key, variableList))
    }
}

/**
 * abs(x) < a, where a <= 0  --> x has no solution in R
 */
private val extractSolutionFromModulusLessThanNonPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = oneOf(
        condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.NEGATIVE },
        FixedPattern(Constants.Zero),
    )

    val lessThanInequality = lessThanOf(lhs, rhs)

    onPattern(lessThanInequality) {
        ruleResult(
            toExpr = contradictionOf(variableListOf(context.solutionVariables), expression),
            explanation = metadata(Explanation.ExtractSolutionFromModulusLessThanNonPositiveConstant),
        )
    }
}

/**
 * abs[x + 1] <= 0 --> abs[x + 1] = 0, for any x in R
 */
private val reduceModulusLessThanEqualToZeroInequalityToEquation = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = FixedPattern(Constants.Zero)

    val lessThanEqualToInequality = lessThanEqualOf(lhs, rhs)
    onPattern(lessThanEqualToInequality) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = transform(equationOf(newLHS, get(rhs))),
            explanation = metadata(Explanation.ReduceModulusLessThanEqualToZeroInequalityToEquation),
        )
    }
}

/**
 * abs(x) >= a --> x \in R, where a \in (-inf, 0]
 */
private val extractSolutionFromModulusGreaterThanEqualToNonPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val zero = FixedPattern(Constants.Zero)
    val negativeConstantRHS = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.NEGATIVE }
    val nonPositiveConstantRHS = oneOf(negativeConstantRHS, zero)

    val pattern = greaterThanEqualOf(lhs, nonPositiveConstantRHS)

    onPattern(pattern) {
        val variableList = variableListOf(context.solutionVariables)
        ruleResult(
            toExpr = transform(identityOf(variableList, get(pattern))),
            explanation = metadata(
                Explanation.ExtractSolutionFromModulusGreaterThanEqualToNonPositiveConstant,
                variableList,
            ),
        )
    }
}

/**
 * abs(x) > a --> x \in R, where a is a negative constant
 */
private val extractSolutionFromModulusGreaterThanNegativeConstant = rule {
    val signedLHS = AnyPattern()
    val lhs = absoluteValueOf(signedLHS)
    val negativeConstantRHS = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.NEGATIVE }

    val pattern = greaterThanOf(lhs, negativeConstantRHS)

    onPattern(pattern) {
        val variableList = variableListOf(context.solutionVariables)
        ruleResult(
            toExpr = transform(identityOf(variableList, get(pattern))),
            explanation = metadata(Explanation.ExtractSolutionFromModulusGreaterThanNegativeConstant, variableList),
        )
    }
}

/**
 * abs(x) > a --> x > a OR x < -a, where a > 0
 */
private val separateModulusGreaterThanPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.POSITIVE }
    val inequality = greaterThanOf(lhs, rhs)

    onPattern(inequality) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = statementUnionOf(
                lessThanOf(newLHS, negOf(get(rhs))),
                greaterThanOf(newLHS, get(rhs)),
            ),
            explanation = metadata(Explanation.SeparateModulusGreaterThanPositiveConstant),
        )
    }
}

/**
 * abs(x) >= a --> x >= a OR x <= -a, where a > 0
 */
private val separateModulusGreaterThanEqualToPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.POSITIVE }
    val inequality = greaterThanEqualOf(lhs, rhs)

    onPattern(inequality) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = transform(
                statementUnionOf(
                    greaterThanEqualOf(newLHS, get(rhs)),
                    lessThanEqualOf(newLHS, negOf(get(rhs))),
                ),
            ),
            explanation = metadata(Explanation.SeparateModulusGreaterThanEqualToPositiveConstant),
        )
    }
}

/**
 * abs(x) > 0 --> abs(x) != 0
 */
private val convertModulusGreaterThanZero = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = FixedPattern(Constants.Zero)

    val inequality = greaterThanOf(lhs, rhs)

    onPattern(inequality) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = transform(notEqualOf(newLHS, get(rhs))),
            explanation = metadata(Explanation.ConvertModulusGreaterThanZero),
        )
    }
}

/**
 * Converts an inequality of the form `|f(x)| < positiveConstant` to a double inequality
 * i.e. `-positiveConstant < f(x) < positiveConstant`
 *
 * for e.g. |3x - 1| < 2 --> -2 < 3x - 1 < 2
 */
private val convertModulusLessThanPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.POSITIVE }

    val inequality = lessThanOf(lhs, rhs)

    onPattern(inequality) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = openRangeOf(negOf(get(rhs)), newLHS, get(rhs)),
            explanation = metadata(Explanation.ConvertModulusLessThanPositiveConstant),
        )
    }
}

/**
 * Converts an inequality of the form `|f(x)| <= positiveConstant` to a double inequality
 * i.e. `-positiveConstant <= f(x) <= positiveConstant`
 *
 * for e.g. |3x - 1| <= 2 --> -2 <= 3x - 1 <= 2
 */
private val convertModulusLessThanEqualToPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.POSITIVE }

    val inequality = lessThanEqualOf(lhs, rhs)

    onPattern(inequality) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = transform(
                closedRangeOf(negOf(get(rhs)), newLHS, get(rhs)),
            ),
            explanation = metadata(Explanation.ConvertModulusLessThanEqualToPositiveConstant),
        )
    }
}
