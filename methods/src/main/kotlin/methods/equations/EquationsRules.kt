package methods.equations

import engine.context.emptyContext
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.denominator
import engine.expressions.equationOf
import engine.expressions.fractionOf
import engine.expressions.numerator
import engine.expressions.productOf
import engine.expressions.simplifiedNegOf
import engine.expressions.solutionOf
import engine.expressions.solutionSetOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.methods.ruleResult
import engine.operators.BinaryExpressionOperator
import engine.operators.NaryOperator
import engine.patterns.AnyPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.metadata.metadata
import engine.utility.lcm
import java.math.BigInteger

enum class EquationsRules(override val runner: Rule) : RunnerMethod {

    CancelCommonTermsOnBothSides(
        rule {
            val common = condition(AnyPattern()) { it != Constants.Zero }

            // intentionally matching members of the sum first, to avoid cancelling an
            // entire sum at once
            val leftSum = sumContaining(common)
            val lhs = oneOf(leftSum, common)
            val rightSum = sumContaining(common)
            val rhs = oneOf(rightSum, common)

            onEquation(lhs, rhs) {
                val restLeft = if (isBound(leftSum)) restOf(leftSum) else Constants.Zero
                val restRight = if (isBound(rightSum)) restOf(rightSum) else Constants.Zero

                ruleResult(
                    toExpr = equationOf(restLeft, restRight),
                    explanation = metadata(Explanation.CancelCommonTermsOnBothSides)
                )
            }
        }
    ),

    MoveConstantsToTheLeft(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                val constants = extractConstants(get(rhs)!!, context.solutionVariable)
                if (constants == Constants.Zero) return@onEquation null

                val negatedConstants = simplifiedNegOf(constants)

                ruleResult(
                    toExpr = equationOf(
                        sumOf(get(lhs), negatedConstants),
                        sumOf(get(rhs), negatedConstants)
                    ),
                    explanation = metadata(Explanation.MoveConstantsToTheLeft)
                )
            }
        }
    ),

    MoveConstantsToTheRight(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                val constants = extractConstants(get(lhs)!!, context.solutionVariable)
                if (constants == Constants.Zero) return@onEquation null

                val negatedConstants = simplifiedNegOf(constants)

                ruleResult(
                    toExpr = equationOf(
                        sumOf(get(lhs), negatedConstants),
                        sumOf(get(rhs), negatedConstants)
                    ),
                    explanation = metadata(Explanation.MoveConstantsToTheRight)
                )
            }
        }
    ),

    MoveVariablesToTheLeft(
        rule {
            val lhs = AnyPattern()
            val solutionVariable = withOptionalConstantCoefficient(SolutionVariablePattern())
            val rhs = oneOf(solutionVariable, sumContaining(solutionVariable))

            onEquation(lhs, rhs) {
                val negatedVariable = simplifiedNegOf(move(solutionVariable))

                ruleResult(
                    toExpr = equationOf(
                        sumOf(get(lhs), negatedVariable),
                        sumOf(get(rhs), negatedVariable)
                    ),
                    explanation = metadata(Explanation.MoveVariablesToTheLeft)
                )
            }
        }
    ),

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
    ),

    MultiplyEquationByLCD(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                var lcm = BigInteger.ONE

                val lhsVal = get(lhs)
                lcm = if (lhsVal.operator == NaryOperator.Sum) {
                    lhsVal.children().map { extractDenominator(it) }.fold(lcm) { curr, new -> curr.lcm(new) }
                } else {
                    lcm.lcm(extractDenominator(lhsVal))
                }

                val rhsVal = get(rhs)
                lcm = if (rhsVal.operator == NaryOperator.Sum) {
                    rhsVal.children().map { extractDenominator(it) }.fold(lcm) { curr, new -> curr.lcm(new) }
                } else {
                    lcm.lcm(extractDenominator(rhsVal))
                }

                ruleResult(
                    toExpr = equationOf(
                        productOf(get(lhs), xp(lcm)),
                        productOf(get(rhs), xp(lcm))
                    ),
                    explanation = metadata(Explanation.MultiplyEquationByLCD)
                )
            }
        }
    )
}

private fun extractConstants(expression: Expression, variable: String?): Expression {
    return when {
        expression.operator == NaryOperator.Sum -> {
            val constantTerms = expression.children().filter { it.isConstantIn(variable) }
            if (constantTerms.isEmpty()) Constants.Zero else sumOf(constantTerms)
        }
        expression.isConstantIn(variable) -> expression
        else -> Constants.Zero
    }
}

private val integerDenominator = UnsignedIntegerPattern()
private val integerDenominatorFraction = fractionOf(AnyPattern(), integerDenominator)
private val denominatorDetectingPattern = optionalNegOf(
    oneOf(
        productContaining(integerDenominatorFraction),
        integerDenominatorFraction
    )
)

private fun extractDenominator(expression: Expression): BigInteger {
    val match = denominatorDetectingPattern.findMatches(emptyContext, subexpression = expression).firstOrNull()
    return if (match != null) {
        integerDenominator.getBoundInt(match)
    } else {
        return BigInteger.ONE
    }
}
