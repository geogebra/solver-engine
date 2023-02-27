package methods.solvable

import engine.context.emptyContext
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.productOf
import engine.expressions.simplifiedNegOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.operators.BinaryExpressionOperator
import engine.operators.NaryOperator
import engine.patterns.AnyPattern
import engine.patterns.SolvablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.metadata.metadata
import engine.utility.lcm
import methods.solvable.DenominatorExtractor.extractDenominator
import java.math.BigInteger

enum class SolvableRules(override val runner: Rule) : RunnerMethod {

    CancelCommonTermsOnBothSides(
        rule {
            val common = condition(AnyPattern()) { it != Constants.Zero }

            // intentionally matching members of the sum first, to avoid cancelling an
            // entire sum at once
            val leftSum = sumContaining(common)
            val lhs = oneOf(leftSum, common)
            val rightSum = sumContaining(common)
            val rhs = oneOf(rightSum, common)

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                val restLeft = if (isBound(leftSum)) restOf(leftSum) else Constants.Zero
                val restRight = if (isBound(rightSum)) restOf(rightSum) else Constants.Zero

                ruleResult(
                    toExpr = cancel(common, solvable.sameSolvable(restLeft, restRight)),
                    explanation = metadata(
                        if (solvable.isEquation()) {
                            EquationsExplanation.CancelCommonTermsOnBothSides
                        } else {
                            InequalitiesExplanation.CancelCommonTermsOnBothSides
                        },
                    ),
                )
            }
        },
    ),

    MoveConstantsToTheLeft(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                val constants = extractConstants(get(rhs), context.solutionVariable)
                if (constants == Constants.Zero || constants == null) return@onPattern null

                val negatedConstants = introduce(constants, simplifiedNegOf(constants))

                ruleResult(
                    toExpr = solvable.sameSolvable(
                        sumOf(get(lhs), negatedConstants),
                        sumOf(get(rhs), negatedConstants),
                    ),
                    explanation = metadata(
                        if (solvable.isEquation()) {
                            EquationsExplanation.MoveConstantsToTheLeft
                        } else {
                            InequalitiesExplanation.MoveConstantsToTheLeft
                        },
                    ),
                )
            }
        },
    ),

    MoveConstantsToTheRight(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                val constants = extractConstants(get(lhs), context.solutionVariable)
                if (constants == Constants.Zero || constants == null) return@onPattern null

                val negatedConstants = introduce(constants, simplifiedNegOf(constants))

                ruleResult(
                    toExpr = solvable.sameSolvable(
                        sumOf(get(lhs), negatedConstants),
                        sumOf(get(rhs), negatedConstants),
                    ),
                    explanation = metadata(
                        if (solvable.isEquation()) {
                            EquationsExplanation.MoveConstantsToTheRight
                        } else {
                            InequalitiesExplanation.MoveConstantsToTheRight
                        },
                    ),
                )
            }
        },
    ),

    MoveVariablesToTheLeft(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                val variables = extractVariableTerms(get(rhs), context.solutionVariable) ?: return@onPattern null

                val negatedVariable = introduce(variables, simplifiedNegOf(variables))

                ruleResult(
                    toExpr = solvable.sameSolvable(
                        sumOf(get(lhs), negatedVariable),
                        sumOf(get(rhs), negatedVariable),
                    ),
                    explanation = metadata(
                        if (solvable.isEquation()) {
                            EquationsExplanation.MoveVariablesToTheLeft
                        } else {
                            InequalitiesExplanation.MoveVariablesToTheLeft
                        },
                    ),
                )
            }
        },
    ),

    /**
     * Multiply through with the LCD if the equation contains one fraction with a sum numerator
     * OR a fraction multiplied by a sum OR at least two fractions
     */
    MultiplySolvableByLCD(
        rule {
            val nonConstantSum = condition(sumContaining()) { !it.isConstantIn(solutionVariable) }
            val fractionRequiringMultiplication = oneOf(
                fractionOf(nonConstantSum, UnsignedIntegerPattern()),
                productContaining(
                    fractionOf(AnyPattern(), UnsignedIntegerPattern()),
                    nonConstantSum,
                ),
            )

            val lhs = AnyPattern()
            val rhs = AnyPattern()

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                fun extractSumTerms(expr: Expression) = when (expr.operator) {
                    NaryOperator.Sum -> expr.children
                    else -> listOf(expr)
                }

                val terms = extractSumTerms(get(lhs)) + extractSumTerms(get(rhs))

                if (terms.count { it.operator == BinaryExpressionOperator.Fraction } < 2 &&
                    terms.none { fractionRequiringMultiplication.matches(context, it) }
                ) {
                    return@onPattern null
                }

                val lcm = terms.map { extractDenominator(it) }.fold(BigInteger.ONE) { curr, new -> curr.lcm(new) }
                when (lcm) {
                    BigInteger.ONE -> null
                    else -> ruleResult(
                        toExpr = solvable.sameSolvable(
                            productOf(get(lhs), xp(lcm)),
                            productOf(get(rhs), xp(lcm)),
                        ),
                        explanation = metadata(
                            if (solvable.isEquation()) {
                                EquationsExplanation.MultiplyEquationByLCD
                            } else {
                                InequalitiesExplanation.MultiplyInequalityByLCD
                            },
                        ),
                    )
                }
            }
        },
    ),
}

private fun extractVariableTerms(expression: Expression, variable: String?): Expression? {
    return when {
        expression.operator == NaryOperator.Sum -> {
            val constantTerms = expression.children.filter { !it.isConstantIn(variable) }
            if (constantTerms.isEmpty()) null else sumOf(constantTerms)
        }
        !expression.isConstantIn(variable) -> expression
        else -> null
    }
}

private fun extractConstants(expression: Expression, variable: String?): Expression? {
    return when {
        expression.operator == NaryOperator.Sum -> {
            val constantTerms = expression.children.filter { it.isConstantIn(variable) }
            if (constantTerms.isEmpty()) null else sumOf(constantTerms)
        }
        expression.isConstantIn(variable) -> expression
        else -> null
    }
}

private object DenominatorExtractor {
    private val integerDenominator = UnsignedIntegerPattern()
    private val integerDenominatorFraction = fractionOf(AnyPattern(), integerDenominator)
    private val denominatorDetectingPattern = optionalNegOf(
        oneOf(
            productContaining(integerDenominatorFraction),
            integerDenominatorFraction,
        ),
    )

    fun extractDenominator(expression: Expression): BigInteger {
        val match = denominatorDetectingPattern.findMatches(emptyContext, subexpression = expression).firstOrNull()
        return if (match != null) {
            integerDenominator.getBoundInt(match)
        } else {
            return BigInteger.ONE
        }
    }
}
