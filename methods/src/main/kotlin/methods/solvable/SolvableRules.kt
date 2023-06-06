package methods.solvable

import engine.context.Context
import engine.context.emptyContext
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.Introduce
import engine.expressions.productOf
import engine.expressions.simplifiedNegOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.CompositeMethod
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.methods.stepsproducers.StepsBuilder
import engine.methods.stepsproducers.StepsProducer
import engine.operators.EquationOperator
import engine.operators.SumOperator
import engine.patterns.AnyPattern
import engine.patterns.SolvablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.Transformation
import engine.steps.metadata.Metadata
import engine.utility.lcm
import methods.solvable.DenominatorExtractor.extractDenominator
import java.math.BigInteger
import engine.steps.metadata.DragTargetPosition as Position

enum class SolvableRules(override val runner: Rule) : RunnerMethod {

    CancelCommonTermsOnBothSides(
        rule {
            val common = condition { it != Constants.Zero }

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
                    toExpr = solvable.sameSolvable(
                        sumOf(get(lhs), negatedConstants),
                        sumOf(get(rhs), negatedConstants),
                    ),
                    gmAction = drag(constants, lhs, Position.RightOf),
                    explanation = solvableExplanation(SolvableKey.MoveConstantsToTheLeft, constants),
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
                    toExpr = solvable.sameSolvable(
                        sumOf(get(lhs), negatedConstants),
                        sumOf(get(rhs), negatedConstants),
                    ),
                    gmAction = drag(constants, rhs, Position.RightOf),
                    explanation = solvableExplanation(SolvableKey.MoveConstantsToTheRight, constants),
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
                    toExpr = solvable.sameSolvable(
                        sumOf(get(lhs), negatedVariable),
                        sumOf(get(rhs), negatedVariable),
                    ),
                    gmAction = drag(variables, lhs, Position.RightOf),
                    explanation = solvableExplanation(SolvableKey.MoveVariablesToTheLeft, variables),
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
                    toExpr = solvable.sameSolvable(
                        sumOf(get(lhs), negatedVariable),
                        sumOf(get(rhs), negatedVariable),
                    ),
                    gmAction = drag(variables, rhs, Position.RightOf),
                    explanation = solvableExplanation(SolvableKey.MoveVariablesToTheRight, variables),
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
                        toExpr = solvable.sameSolvable(
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
}

private fun extractVariableTerms(expression: Expression, variables: List<String>): Expression? {
    return when {
        expression.operator == SumOperator -> {
            val variableTerms = expression.children.filter { !it.isConstantIn(variables) }
            if (variableTerms.isEmpty()) null else sumOf(variableTerms)
        }
        !expression.isConstantIn(variables) -> expression
        else -> null
    }
}

private fun extractConstants(expression: Expression, variables: List<String>): Expression? {
    return when {
        expression.operator == SumOperator -> {
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
fun simplifiedNegOfSum(expr: Expression) = when (expr.operator) {
    SumOperator -> sumOf(expr.children.map { simplifiedNegOf(it).withOrigin(Introduce(listOf(it))) })
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
        when (it.operator) {
            SumOperator -> it.children
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

class ApplySolvableRuleAndSimplify(private val simplifySteps: StepsProducer) {
    fun getPlan(solvableKey: SolvableKey, rule: RunnerMethod) = object : CompositeMethod() {

        override fun run(ctx: Context, sub: Expression): Transformation? {
            val initialStep = rule.tryExecute(ctx, sub) ?: return null

            val builder = StepsBuilder(ctx, sub)
            builder.addStep(initialStep)
            simplifySteps.produceSteps(ctx, builder.lastSub)?.let { builder.addSteps(it) }

            val explicitVariables = ctx.solutionVariables.size < sub.variables.size
            val key = if (sub.operator == EquationOperator) {
                EquationsExplanation.getKey(solvableKey, explicitVariables, true)
            } else {
                InequalitiesExplanation.getKey(solvableKey, explicitVariables, true)
            }

            return Transformation(
                type = Transformation.Type.Plan,
                fromExpr = sub,
                toExpr = builder.lastSub,
                steps = builder.getFinalSteps(),
                explanation = Metadata(key, initialStep.explanation!!.mappedParams),
            )
        }
    }
}

private fun MappedExpressionBuilder.solvableExplanation(
    solvableKey: SolvableKey,
    vararg parameters: Expression,
): Metadata {
    val explicitVariables = context.solutionVariables.size < expression.variables.size
    val key = if (expression.operator == EquationOperator) {
        EquationsExplanation.getKey(solvableKey, explicitVariables, false)
    } else {
        InequalitiesExplanation.getKey(solvableKey, explicitVariables, false)
    }
    return if (explicitVariables) {
        Metadata(key, listOf(listOfsolutionVariables()) + parameters)
    } else {
        Metadata(key, parameters.asList())
    }
}
