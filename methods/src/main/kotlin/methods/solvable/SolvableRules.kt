package methods.solvable

import engine.context.emptyContext
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.AbsoluteValue
import engine.expressions.Constants
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.Fraction
import engine.expressions.Introduce
import engine.expressions.Sum
import engine.expressions.fractionOf
import engine.expressions.inverse
import engine.expressions.isSignedFraction
import engine.expressions.productOf
import engine.expressions.simplifiedNegOf
import engine.expressions.sumOf
import engine.expressions.withoutNegOrPlus
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.SolvablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.VariableExpressionPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import engine.sign.Sign
import engine.steps.metadata.GmPathModifier
import engine.steps.metadata.Metadata
import engine.utility.lcm
import methods.solvable.DenominatorExtractor.extractDenominator
import java.math.BigInteger
import engine.steps.metadata.DragTargetPosition as Position

enum class SolvableRules(override val runner: Rule) : RunnerMethod {

    CancelCommonTermsOnBothSides(
        rule {
            val common = condition { it != Constants.Zero && it != Constants.Undefined }

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
                    toExpr = cancel(common, solvable.deriveSolvable(restLeft, restRight)),
                    gmAction = drag(common.within(lhs), common.within(rhs)),
                    explanation = solvableExplanation(SolvableKey.CancelCommonTermsOnBothSides),
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
                val constants = extractConstants(get(rhs), context.solutionVariables)
                if (constants == Constants.Zero || constants == null) return@onPattern null

                val negatedConstants = simplifiedNegOfSum(constants)

                ruleResult(
                    toExpr = solvable.deriveSolvable(
                        sumOf(get(lhs), negatedConstants),
                        sumOf(get(rhs), negatedConstants),
                    ),
                    gmAction = drag(constants, lhs, Position.RightOf),
                    explanation = solvableExplanation(
                        SolvableKey.MoveConstantsToTheLeft,
                        parameters = listOf(constants),
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
                val constants = extractConstants(get(lhs), context.solutionVariables)
                if (constants == Constants.Zero || constants == null) return@onPattern null

                val negatedConstants = simplifiedNegOfSum(constants)

                ruleResult(
                    toExpr = solvable.deriveSolvable(
                        sumOf(get(lhs), negatedConstants),
                        sumOf(get(rhs), negatedConstants),
                    ),
                    gmAction = drag(constants, rhs, Position.RightOf),
                    explanation = solvableExplanation(
                        SolvableKey.MoveConstantsToTheRight,
                        parameters = listOf(constants),
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
                val variables = extractVariableTerms(get(rhs), context.solutionVariables) ?: return@onPattern null

                val negatedVariable = simplifiedNegOfSum(variables)

                ruleResult(
                    toExpr = solvable.deriveSolvable(
                        sumOf(get(lhs), negatedVariable),
                        sumOf(get(rhs), negatedVariable),
                    ),
                    gmAction = drag(variables, lhs, Position.RightOf),
                    explanation = solvableExplanation(
                        SolvableKey.MoveVariablesToTheLeft,
                        parameters = listOf(variables),
                    ),
                )
            }
        },
    ),

    MoveVariablesToTheRight(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                val variables = extractVariableTerms(get(lhs), context.solutionVariables) ?: return@onPattern null

                val negatedVariable = simplifiedNegOfSum(variables)

                ruleResult(
                    toExpr = solvable.deriveSolvable(
                        sumOf(get(lhs), negatedVariable),
                        sumOf(get(rhs), negatedVariable),
                    ),
                    gmAction = drag(variables, rhs, Position.RightOf),
                    explanation = solvableExplanation(
                        SolvableKey.MoveVariablesToTheRight,
                        parameters = listOf(variables),
                    ),
                )
            }
        },
    ),

    MoveEverythingToTheLeft(
        rule {
            val lhs = condition(AnyPattern()) { it != Constants.Undefined }
            val rhs = condition { it != Constants.Zero && it != Constants.Undefined }

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                val negatedRhs = simplifiedNegOfSum(get(rhs))

                ruleResult(
                    toExpr = solvable.deriveSolvable(
                        sumOf(get(lhs), negatedRhs),
                        sumOf(get(rhs), negatedRhs),
                    ),
                    explanation = solvableExplanation(SolvableKey.MoveEverythingToTheLeft),
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
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                val terms = extractSumTermsFromSolvable(get(solvable))
                val denominators = terms.mapNotNull { extractDenominator(it) }

                if (denominators.isEmpty()) {
                    return@onPattern null
                }

                when (val lcm = denominators.lcm()) {
                    BigInteger.ONE -> null
                    else -> ruleResult(
                        toExpr = solvable.deriveSolvable(
                            productOf(get(lhs), xp(lcm)),
                            productOf(get(rhs), xp(lcm)),
                        ),
                        gmAction = editOp(solvable),
                        explanation = solvableExplanation(SolvableKey.MultiplyBothSidesByLCD),
                    )
                }
            }
        },
    ),

    MultiplyByInverseCoefficientOfVariable(
        rule {
            val lhs = withOptionalConstantCoefficient(VariableExpressionPattern())
            val rhs = ConstantInSolutionVariablePattern()

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                val coefficient = get(lhs::coefficient)!!
                val useDual = when (coefficient.signOf()) {
                    Sign.POSITIVE -> false
                    Sign.NEGATIVE -> true
                    Sign.NOT_ZERO -> if (solvable.isSelfDual()) false else return@onPattern null
                    else -> return@onPattern null
                }

                if (coefficient.isSignedFraction()) {
                    val inverse = introduce(coefficient, coefficient.inverse())

                    val dragTarget = if (coefficient.parent !== null) {
                        coefficient
                    } else {
                        // We can't use `coefficient` as the drag target because it
                        // doesn't have a parent chain. We need a parent chain in order to
                        // create a path mapping to the drag target. This might happen in
                        // a situation like `[-x/3]` where `coefficient` would be the
                        // artificially created expression [-1/3]. The `3` in that
                        // example, however, does have a parent chain to the root of the
                        // "from expression" so we can use that.
                        val positiveCoefficient = coefficient.withoutNegOrPlus() as Fraction
                        if (positiveCoefficient.parent !== null) {
                            positiveCoefficient
                        } else {
                            // TODO What should we do in situations like `[2x/5]`? In
                            // that, this logic would only provide a drag target of `5` so
                            // the `2` would still be left behind. It's not ideal that GM
                            // would require two steps to move the `[2/3]`, while Solver
                            // would take only one step.
                            positiveCoefficient.denominator
                        }
                    }

                    val newLhs = productOf(get(lhs), inverse)
                    val newRhs = productOf(get(rhs), inverse)

                    ruleResult(
                        toExpr = solvable.deriveSolvable(newLhs, newRhs, useDual),
                        gmAction = drag(dragTarget, rhs, Position.LeftOf),
                        explanation = solvableExplanation(
                            SolvableKey.MultiplyByInverseCoefficientOfVariable,
                            flipSign = useDual && !solvable.isSelfDual(),
                            parameters = listOf(coefficient),
                        ),
                    )
                } else {
                    null
                }
            }
        },
    ),

    DivideByCoefficientOfVariable(
        rule {
            val lhs = withOptionalConstantCoefficient(VariableExpressionPattern())
            val rhs = ConstantInSolutionVariablePattern()

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                val coefficientValue = get(lhs::coefficient)!!

                val useDual = when (coefficientValue.signOf()) {
                    Sign.POSITIVE -> false
                    Sign.NEGATIVE -> true
                    Sign.NOT_ZERO -> if (solvable.isSelfDual()) false else return@onPattern null
                    else -> return@onPattern null
                }

                if (coefficientValue == Constants.One || coefficientValue == Constants.MinusOne) return@onPattern null

                val coefficient = introduce(coefficientValue, coefficientValue)

                val newLhs = fractionOf(get(lhs), coefficient)
                val newRhs = fractionOf(get(rhs), coefficient)

                ruleResult(
                    toExpr = solvable.deriveSolvable(newLhs, newRhs, useDual),
                    gmAction = drag(coefficient, rhs, engine.steps.metadata.DragTargetPosition.Below),
                    explanation = solvableExplanation(
                        SolvableKey.DivideByCoefficientOfVariable,
                        flipSign = useDual && !solvable.isSelfDual(),
                        parameters = listOf(coefficient),
                    ),
                )
            }
        },
    ),

    NegateBothSides(
        rule {
            val unsignedLhs = AnyPattern()
            val lhs = negOf(unsignedLhs)
            val rhs = optionalNegOf(AnyPattern())

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                ruleResult(
                    toExpr = solvable.deriveSolvable(get(unsignedLhs), simplifiedNegOf(move(rhs)), useDual = true),
                    gmAction = when {
                        rhs.isNeg() -> drag(lhs, GmPathModifier.Operator, rhs, GmPathModifier.Operator)
                        else -> drag(lhs, GmPathModifier.Operator, rhs, null, Position.LeftOf)
                    },
                    explanation = solvableExplanation(SolvableKey.NegateBothSides),
                )
            }
        },
    ),

    FlipSolvable(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                ruleResult(
                    toExpr = solvable.deriveSolvable(move(rhs), move(lhs), useDual = true),
                    gmAction = drag(lhs, null, expression, GmPathModifier.Operator, Position.Above),
                    explanation = solvableExplanation(SolvableKey.FlipSolvable),
                )
            }
        },
    ),

    MoveTermsNotContainingModulusToTheRight(moveTermsNotContainingModulusToTheRight),

    MoveTermsNotContainingModulusToTheLeft(moveTermsNotContainingModulusToTheLeft),
}

private val moveTermsNotContainingModulusToTheRight = rule {
    val lhs = condition(sumContaining()) { it.countAbsoluteValues(solutionVariables) > 0 }
    val rhs = condition { it.countAbsoluteValues(solutionVariables) == 0 }
    val solvable = SolvablePattern(lhs, rhs)

    onPattern(solvable) {
        val termsNotContainingModulus = extractTermsNotContainingModulus(get(lhs), context.solutionVariables)
            ?: return@onPattern null

        val negatedTerms = simplifiedNegOfSum(termsNotContainingModulus)

        ruleResult(
            toExpr = solvable.deriveSolvable(sumOf(get(lhs), negatedTerms), sumOf(get(rhs), negatedTerms)),
            explanation = solvableExplanation(SolvableKey.MoveTermsNotContainingModulusToTheRight),
        )
    }
}

private val moveTermsNotContainingModulusToTheLeft = rule {
    val lhs = condition { it.countAbsoluteValues(solutionVariables) == 0 }
    val rhs = condition(sumContaining()) { it.countAbsoluteValues(solutionVariables) > 0 }
    val solvable = SolvablePattern(lhs, rhs)

    onPattern(solvable) {
        val termsNotContainingModulus = extractTermsNotContainingModulus(get(rhs), context.solutionVariables)
            ?: return@onPattern null

        val negatedTerms = simplifiedNegOfSum(termsNotContainingModulus)

        ruleResult(
            toExpr = solvable.deriveSolvable(sumOf(get(lhs), negatedTerms), sumOf(get(rhs), negatedTerms)),
            explanation = solvableExplanation(SolvableKey.MoveTermsNotContainingModulusToTheLeft),
        )
    }
}

private fun extractTermsNotContainingModulus(expression: Expression, variables: List<String>): Expression? {
    return when {
        expression is Sum -> {
            val termsWithoutModulus = expression.children.filter { it.countAbsoluteValues(variables) == 0 }
            if (termsWithoutModulus.isEmpty()) null else sumOf(termsWithoutModulus)
        }
        expression.countAbsoluteValues(variables) == 0 -> expression
        else -> null
    }
}

private fun extractVariableTerms(expression: Expression, variables: List<String>): Expression? {
    return when {
        expression is Sum -> {
            val variableTerms = expression.children.filter { !it.isConstantIn(variables) }
            if (variableTerms.isEmpty()) null else sumOf(variableTerms)
        }
        !expression.isConstantIn(variables) -> expression
        else -> null
    }
}

private fun extractConstants(expression: Expression, variables: List<String>): Expression? {
    return when {
        expression is Sum -> {
            val constantTerms = expression.children.filter { it.isConstantIn(variables) }
            if (constantTerms.isEmpty()) null else sumOf(constantTerms)
        }
        expression.isConstantIn(variables) -> expression
        else -> null
    }
}

/**
 * The simplified negation of an expression, but if [expr] is a sum then each of its terms is negated.  So
 *
 *     x - 2 --> -x + 2
 *
 * Note: it remains to be determined what a good origin would be for the terms in the result.
 */
fun simplifiedNegOfSum(expr: Expression) = when (expr) {
    is Sum -> sumOf(expr.children.map { simplifiedNegOf(it).withOrigin(Introduce(listOf(it))) })
    else -> simplifiedNegOf(expr).withOrigin(Introduce(listOf(expr)))
}

private val nonConstantSum = condition(sumContaining()) { !it.isConstantIn(solutionVariables) }
val fractionRequiringMultiplication = optionalNegOf(
    oneOf(
        fractionOf(nonConstantSum, UnsignedIntegerPattern()),
        productContaining(
            fractionOf(AnyPattern(), UnsignedIntegerPattern()),
            nonConstantSum,
        ),
    ),
)

fun extractSumTermsFromSolvable(equation: Expression): List<Expression> {
    return equation.children.flatMap {
        when (it) {
            is Sum -> it.children
            else -> listOf(it)
        }
    }
}

object DenominatorExtractor {
    private val integerDenominator = UnsignedIntegerPattern()
    private val integerDenominatorFraction = fractionOf(AnyPattern(), integerDenominator)
    private val denominatorDetectingPattern = optionalNegOf(
        oneOf(
            productContaining(integerDenominatorFraction),
            integerDenominatorFraction,
        ),
    )

    fun extractDenominator(expression: Expression): BigInteger? {
        val match = denominatorDetectingPattern.findMatches(emptyContext, subexpression = expression).firstOrNull()
        return match?.let { integerDenominator.getBoundInt(it) }
    }
}

private fun MappedExpressionBuilder.solvableExplanation(
    solvableKey: SolvableKey,
    flipSign: Boolean = false,
    parameters: List<Expression> = emptyList(),
): Metadata {
    val explicitVariables = context.solutionVariables.size < expression.variables.size
    val keyGetter = if (expression is Equation) {
        EquationsExplanation
    } else {
        InequalitiesExplanation
    }
    val key = keyGetter.getKey(solvableKey, explicitVariables = explicitVariables, flipSign = flipSign)

    return if (explicitVariables) {
        Metadata(key, listOf(listOfsolutionVariables()) + parameters)
    } else {
        Metadata(key, parameters)
    }
}

internal fun Expression.countAbsoluteValues(solutionVariables: List<String>): Int = when {
    childCount == 0 -> 0
    this is AbsoluteValue && !isConstantIn(solutionVariables) -> 1
    else -> children.sumOf { it.countAbsoluteValues(solutionVariables) }
}
