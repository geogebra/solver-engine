package methods.solvable

import engine.context.BooleanSetting
import engine.context.Context
import engine.context.Setting
import engine.expressions.AbsoluteValue
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.expressions.Fraction
import engine.expressions.Power
import engine.expressions.Product
import engine.expressions.Sum
import engine.expressions.Variable
import engine.methods.Method
import engine.methods.plan
import engine.methods.stepsproducers.StepsBuilder
import engine.methods.stepsproducers.branchOn
import engine.methods.stepsproducers.steps
import engine.patterns.BinaryIntegerCondition
import engine.patterns.ConditionPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.SolvablePattern
import engine.patterns.VariableExpressionPattern
import engine.patterns.condition
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficientInSolutionVariables
import engine.patterns.withOptionalIntegerCoefficient
import engine.steps.Transformation
import engine.steps.metadata.Metadata
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.metadata
import methods.approximation.ApproximationPlans
import methods.polynomials.PolynomialsPlans

class SolvablePlans(private val simplificationPlan: Method, private val constraintSimplificationPlan: Method? = null) {

    private fun getExplanationKey(solvableKey: SolvableKey, ctx: Context, expr: Expression): MetadataKey {
        val keyGetter = if (expr is Equation) {
            EquationsExplanation
        } else {
            InequalitiesExplanation
        }
        return keyGetter.getKey(
            solvableKey,
            explicitVariables = ctx.solutionVariables.size < expr.variables.size,
            simplify = true,
        )
    }

    inner class ApplyRuleAndSimplify(private val key: SolvableKey) : Method {

        override fun tryExecute(ctx: Context, sub: Expression): Transformation? {
            val expression = if (sub is ExpressionWithConstraint) sub.expression else sub

            val initialStep = key.rule.tryExecute(ctx, expression) ?: return null

            val builder = StepsBuilder(ctx, expression)
            builder.addStep(initialStep)

            simplificationPlan.tryExecute(ctx, builder.simpleExpression)?.let { builder.addStep(it) }

            val constraint = builder.constraint
            if (constraintSimplificationPlan != null && constraint != null) {
                constraintSimplificationPlan.tryExecute(ctx, constraint)?.let { builder.addStep(it) }
            }

            val explanationKey = getExplanationKey(key, ctx, builder.simpleExpression)

            return Transformation(
                type = Transformation.Type.Plan,
                fromExpr = expression,
                toExpr = builder.expression,
                steps = builder.getFinalSteps(),
                explanation = Metadata(explanationKey, initialStep.explanation!!.mappedParams),
            )
        }
    }

    private fun applyRuleAndSimplify(key: SolvableKey) = branchOn(Setting.MoveTermsOneByOne) {
        case(BooleanSetting.True) { whilePossible(ApplyRuleAndSimplify(key)) }
        case(BooleanSetting.False) { apply(ApplyRuleAndSimplify(key)) }
    }

    val moveConstantsToTheLeftAndSimplify = applyRuleAndSimplify(SolvableKey.MoveConstantsToTheLeft)

    val moveConstantsToTheRightAndSimplify = applyRuleAndSimplify(SolvableKey.MoveConstantsToTheRight)

    val moveVariablesToTheLeftAndSimplify = applyRuleAndSimplify(SolvableKey.MoveVariablesToTheLeft)

    val moveVariablesToTheRightAndSimplify = applyRuleAndSimplify(SolvableKey.MoveVariablesToTheRight)

    val moveEverythingToTheLeftAndSimplify = applyRuleAndSimplify(SolvableKey.MoveEverythingToTheLeft)

    val multiplyByInverseCoefficientOfVariableAndSimplify = ApplyRuleAndSimplify(
        SolvableKey.MultiplyByInverseCoefficientOfVariable,
    )

    val multiplyByDenominatorOfVariableLHSAndSimplify = ApplyRuleAndSimplify(
        SolvableKey.MultiplyByDenominatorOfVariableLHS,
    )

    val divideByCoefficientOfVariableAndSimplify = ApplyRuleAndSimplify(
        SolvableKey.DivideByCoefficientOfVariable,
    )

    val multiplyByLCDAndSimplify = plan {
        explanation {
            metadata(getExplanationKey(SolvableKey.MultiplyBothSidesByLCD, context, expression))
        }

        steps {
            apply(SolvableRules.MultiplySolvableByLCD)
            whilePossible(PolynomialsPlans.ExpandPolynomialExpressionWithoutNormalization)
        }
    }

    val takeRootOfBothSidesAndSimplify = ApplyRuleAndSimplify(SolvableKey.TakeRootOfBothSides)

    /**
     * Only rearrange when the variable subexpression is "atomic". For example: don't reorganize
     * (3x + 1)(3x + 2) + 3 = 0 into (3x + 1)(3x + 2) = -3.
     */
    private val variableTerm = oneOf(
        SolutionVariablePattern(),
        condition { (it is Power || it is AbsoluteValue) && !it.isConstantIn(solutionVariables) },
    )

    val solvableRearrangementSteps = steps {
        // three ways to reorganize the solvable into aX = b form
        firstOf {
            option {
                // if the solvable is in the form `a = bX + c` with `b` non-negative, then
                // we move `c` to the left hand side and flip the solvable
                checkForm {
                    val lhs = ConstantInSolutionVariablePattern()
                    val nonConstantTerm = withOptionalConstantCoefficientInSolutionVariables(
                        variableTerm,
                        positiveOnly = true,
                    )
                    val rhs = oneOf(
                        nonConstantTerm,
                        sumContaining(nonConstantTerm) { rest -> rest.isConstantIn(solutionVariables) },
                    )
                    SolvablePattern(lhs, rhs)
                }
                optionally(moveConstantsToTheLeftAndSimplify)
                apply(SolvableRules.FlipSolvable)
            }
            option {
                // if the solvable is in the form `aX + b = cx + d` with an integer and `c` a
                // positive integer such that `c > a`, we move `aX` to the right hand side, `d` to
                // the left hand side and flip the solvable
                checkForm {
                    val lhsVariable = withOptionalIntegerCoefficient(variableTerm, false)
                    val rhsVariable = withOptionalIntegerCoefficient(variableTerm, true)

                    val lhs = oneOf(
                        lhsVariable,
                        sumContaining(lhsVariable) { rest -> rest.isConstantIn(solutionVariables) },
                    )
                    val rhs = oneOf(
                        rhsVariable,
                        sumContaining(rhsVariable) { rest -> rest.isConstantIn(solutionVariables) },
                    )

                    ConditionPattern(
                        SolvablePattern(lhs, rhs),
                        BinaryIntegerCondition(
                            lhsVariable.integerCoefficient,
                            rhsVariable.integerCoefficient,
                        ) { n1, n2 -> n2 > n1 },
                    )
                }

                apply(moveVariablesToTheRightAndSimplify)
                optionally(moveConstantsToTheLeftAndSimplify)
                apply(SolvableRules.FlipSolvable)
            }
            option {
                // otherwise we first move variables to the left and then constants
                // to the right
                checkForm {
                    val lhsVariable = withOptionalConstantCoefficientInSolutionVariables(variableTerm)
                    val rhsVariable = withOptionalConstantCoefficientInSolutionVariables(variableTerm)

                    val lhs = oneOf(
                        ConstantInSolutionVariablePattern(),
                        lhsVariable,
                        sumContaining(lhsVariable) { rest -> rest.isConstantIn(solutionVariables) },
                    )
                    val rhs = oneOf(
                        ConstantInSolutionVariablePattern(),
                        rhsVariable,
                        sumContaining(rhsVariable) { rest -> rest.isConstantIn(solutionVariables) },
                    )

                    SolvablePattern(lhs, rhs)
                }
                optionally(moveVariablesToTheLeftAndSimplify)
                optionally(moveConstantsToTheRightAndSimplify)
            }
        }
    }

    val removeConstantDenominatorsSteps = steps {
        check { requiresMultiplicationByTheLCD(it, this) }
        apply(multiplyByLCDAndSimplify)
    }

    val linearCoefficientRemovalSteps = steps {
        check { it.isLinearIn(solutionVariables) }
        apply { coefficientRemovalSteps }
    }

    // The explanations use advanced balancing for conciseness.
    val coefficientRemovalSteps = steps {
        whilePossible {
            firstOf {
                // First deal with coefficients not containing fractions e.g. -5x = 2 -> x = [2 / -5]
                option(divideByCoefficientOfVariableAndSimplify)

                // Then if the LHS is a negation, negate both side e.g. -[2x / 3] = 7 -> [2x / 3] = -7
                option {
                    checkForm {
                        SolvablePattern(negOf(VariableExpressionPattern()), ConstantInSolutionVariablePattern())
                    }
                    apply(SolvableRules.NegateBothSides)
                }

                // Next if the coefficient contains a constant fraction, multiply both sides by the reciprocal
                // E.g. [5 / 2]x = 3 -> x = [2 / 5] * 3
                option(multiplyByInverseCoefficientOfVariableAndSimplify)

                // Last if the coefficient has a fraction with a constant denominator, multiply by this denominator
                // E.g. [5x / 3] = 8 -> 5x = 3 * 8
                option(multiplyByDenominatorOfVariableLHSAndSimplify)
            }
        }
    }
}

val evaluateBothSidesNumerically = steps {
    optionally {
        applyTo(ApproximationPlans.EvaluateExpressionNumerically) { it.firstChild }
    }
    optionally {
        applyTo(ApproximationPlans.EvaluateExpressionNumerically) { it.secondChild }
    }
}

private fun Expression.isLinearIn(variables: List<String>) = linearDegreeIn(variables) == 1

/**
 * This returns 0 if the expression is constant in the given variables, 1 if it is linear, > 1 if it is neither.
 */
private fun Expression.linearDegreeIn(variables: List<String>): Int {
    return when {
        isConstantIn(variables) -> 0
        this is Variable -> 1
        this is Sum -> terms.maxOf { it.linearDegreeIn(variables) }
        this is Product -> factors().sumOf { it.linearDegreeIn(variables) }
        this is Fraction -> if (denominator.isConstantIn(variables)) numerator.linearDegreeIn(variables) else 2
        else -> 2
    }
}
