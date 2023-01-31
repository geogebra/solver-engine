package methods.equations

import engine.conditions.Sign
import engine.conditions.signOf
import engine.expressions.Constants
import engine.expressions.denominator
import engine.expressions.equationOf
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.numerator
import engine.expressions.plusMinusOf
import engine.expressions.productOf
import engine.expressions.simplifiedNegOf
import engine.expressions.solutionOf
import engine.expressions.solutionSetOf
import engine.expressions.squareRootOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.methods.ruleResult
import engine.operators.BinaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.FixedPattern
import engine.patterns.MonomialPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.condition
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.plusMinusOf
import engine.patterns.powerOf
import engine.steps.metadata.metadata

enum class EquationsRules(override val runner: Rule) : RunnerMethod {

    NegateBothSides(
        rule {
            val variable = oneOf(SolutionVariablePattern(), powerOf(SolutionVariablePattern(), AnyPattern()))
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
            val lhs = MonomialPattern(SolutionVariablePattern())
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
            val lhs = MonomialPattern(SolutionVariablePattern())
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

    TakeSquareRootOfBothSides(
        rule {
            val solutionVariable = SolutionVariablePattern()
            val lhs = powerOf(solutionVariable, FixedPattern(Constants.Two))
            val rhs = condition(ConstantInSolutionVariablePattern()) { it.signOf() == Sign.POSITIVE }

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = equationOf(move(solutionVariable), plusMinusOf(squareRootOf(move(rhs)))),
                    explanation = metadata(Explanation.TakeSquareRootOfBothSides)
                )
            }
        }
    ),

    TakeSquareRootOfBothSidesRHSIsZero(
        rule {
            val solutionVariable = SolutionVariablePattern()
            val lhs = powerOf(solutionVariable, FixedPattern(Constants.Two))
            val rhs = FixedPattern(Constants.Zero)

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = equationOf(move(solutionVariable), move(rhs)),
                    explanation = metadata(Explanation.TakeSquareRootOfBothSidesRHSIsZero)
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
                        toExpr = solutionOf(xp(context.solutionVariable!!), Constants.EmptySet),
                        explanation = metadata(Explanation.ExtractSolutionFromContradiction)
                    )
                } else {
                    null
                }
            }
        }
    ),

    ExtractSolutionFromSquareEqualsNegative(
        rule {
            val solutionVariable = SolutionVariablePattern()
            val lhs = powerOf(solutionVariable, FixedPattern(Constants.Two))
            val rhs = condition(ConstantInSolutionVariablePattern()) { it.signOf() == Sign.NEGATIVE }

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = solutionOf(move(solutionVariable), Constants.EmptySet),
                    explanation = metadata(Explanation.ExtractSolutionFromSquareEqualsNegative)
                )
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
    ),

    ExtractSolutionFromEquationInPlusMinusForm(
        rule {
            val lhs = SolutionVariablePattern()
            val absoluteValue = ConstantInSolutionVariablePattern()
            val rhs = plusMinusOf(absoluteValue)

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = solutionOf(move(lhs), solutionSetOf(negOf(move(absoluteValue)), move(absoluteValue))),
                    explanation = metadata(Explanation.ExtractSolutionFromEquationInPlusMinusForm)
                )
            }
        }
    )
}
