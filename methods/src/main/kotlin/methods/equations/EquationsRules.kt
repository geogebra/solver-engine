package methods.equations

import engine.expressions.Constants
import engine.expressions.denominator
import engine.expressions.equationOf
import engine.expressions.fractionOf
import engine.expressions.numerator
import engine.expressions.productOf
import engine.expressions.simplifiedNegOf
import engine.expressions.solutionOf
import engine.expressions.solutionSetOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.methods.ruleResult
import engine.operators.BinaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.negOf
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.metadata.metadata

enum class EquationsRules(override val runner: Rule) : RunnerMethod {

    NegateBothSides(
        rule {
            val variable = SolutionVariablePattern()
            val lhs = negOf(variable)
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = equationOf(get(variable), simplifiedNegOf(move(rhs))),
                    explanation = metadata(Explanation.NegateBothSides)
                )
            }
        }
    ),

    MultiplyByInverseCoefficientOfVariable(
        rule {
            val lhs = withOptionalConstantCoefficient(SolutionVariablePattern())
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                val coefficient = get(lhs::coefficient)!!

                if (coefficient.operator == BinaryExpressionOperator.Fraction) {
                    val inverse = if (coefficient.numerator() == Constants.One) coefficient.denominator()
                    else fractionOf(coefficient.denominator(), coefficient.numerator())

                    ruleResult(
                        toExpr = equationOf(
                            productOf(get(lhs), inverse),
                            productOf(get(rhs), inverse)
                        ),
                        explanation = metadata(Explanation.MultiplyByInverseCoefficientOfVariable)
                    )
                } else {
                    null
                }
            }
        }
    ),

    DivideByCoefficientOfVariable(
        rule {
            val lhs = withOptionalConstantCoefficient(SolutionVariablePattern())
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                when (val coefficient = get(lhs::coefficient)!!) {
                    Constants.One -> null
                    else -> ruleResult(
                        toExpr = equationOf(
                            fractionOf(get(lhs), coefficient),
                            fractionOf(get(rhs), coefficient)
                        ),
                        explanation = metadata(Explanation.DivideByCoefficientOfVariable)
                    )
                }
            }
        }
    ),

    FlipEquation(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = equationOf(move(rhs), move(lhs)),
                    explanation = metadata(Explanation.FlipEquation)
                )
            }
        }
    ),

    ExtractSolutionFromIdentity(
        rule {
            val value = ConstantInSolutionVariablePattern()

            onEquation(value, value) {
                ruleResult(
                    toExpr = solutionOf(xp(context.solutionVariable!!), Constants.Reals),
                    explanation = metadata(Explanation.ExtractSolutionFromIdentity)
                )
            }
        }
    ),

    ExtractSolutionFromContradiction(
        rule {
            val lhs = ConstantInSolutionVariablePattern()
            val rhs = ConstantInSolutionVariablePattern()

            onEquation(lhs, rhs) {
                if (get(lhs) != get(rhs)) {
                    ruleResult(
                        toExpr = solutionOf(xp(context.solutionVariable!!), solutionSetOf()),
                        explanation = metadata(Explanation.ExtractSolutionFromContradiction)
                    )
                } else {
                    null
                }
            }
        }
    ),

    ExtractSolutionFromEquationInSolvedForm(
        rule {
            val lhs = SolutionVariablePattern()
            val rhs = ConstantInSolutionVariablePattern()

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = solutionOf(move(lhs), solutionSetOf(move(rhs))),
                    explanation = metadata(Explanation.ExtractSolutionFromEquationInSolvedForm)
                )
            }
        }
    )
}
