package methods.inequalities

import engine.conditions.Sign
import engine.conditions.signOf
import engine.expressions.Constants
import engine.expressions.Fraction
import engine.expressions.Variable
import engine.expressions.contradictionOf
import engine.expressions.fractionOf
import engine.expressions.identityOf
import engine.expressions.inverse
import engine.expressions.isNeg
import engine.expressions.productOf
import engine.expressions.setSolutionOf
import engine.expressions.simplifiedNegOf
import engine.expressions.variableListOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.inequalityOf
import engine.patterns.negOf
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.metadata.metadata

enum class InequalitiesRules(override val runner: Rule) : RunnerMethod {

    ExtractSolutionFromConstantInequality(
        rule {
            val lhs = UnsignedNumberPattern()
            val rhs = UnsignedNumberPattern()

            val inequality = inequalityOf(lhs, rhs)

            onPattern(inequality) {
                if (inequality.holdsFor(getValue(lhs), getValue(rhs))) {
                    ruleResult(
                        toExpr = identityOf(variableListOf(context.solutionVariables), get(inequality)),
                        explanation = metadata(Explanation.ExtractSolutionFromTrueInequality),
                    )
                } else {
                    ruleResult(
                        toExpr = contradictionOf(variableListOf(context.solutionVariables), get(inequality)),
                        explanation = metadata(Explanation.ExtractSolutionFromFalseInequality),
                    )
                }
            }
        },
    ),

    ExtractSolutionFromConstantInequalityBasedOnSign(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            val inequality = inequalityOf(lhs, rhs)

            onPattern(inequality) {
                val lhsSign = get(lhs).signOf()
                val rhsSign = get(rhs).signOf()

                if (lhsSign.isKnown() && rhsSign.isKnown()) {
                    if (inequality.holdsFor(lhsSign.signum.toBigDecimal(), rhsSign.signum.toBigDecimal())) {
                        ruleResult(
                            toExpr = identityOf(variableListOf(context.solutionVariables), get(inequality)),
                            explanation = metadata(Explanation.ExtractSolutionFromTrueInequality),
                        )
                    } else {
                        ruleResult(
                            toExpr = contradictionOf(variableListOf(context.solutionVariables), get(inequality)),
                            explanation = metadata(Explanation.ExtractSolutionFromFalseInequality),
                        )
                    }
                } else {
                    null
                }
            }
        },
    ),

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
            val lhs = negOf(variable)
            val rhs = AnyPattern()

            val inequality = inequalityOf(lhs, rhs)

            onPattern(inequality) {
                ruleResult(
                    toExpr = inequality.dualInequality(
                        move(variable),
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
