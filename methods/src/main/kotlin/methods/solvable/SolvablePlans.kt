package methods.solvable

import engine.context.Context
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.Minus
import engine.expressions.Sum
import engine.methods.Runner
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsBuilder
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.patterns.BinaryIntegerCondition
import engine.patterns.ConditionPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.SolvablePattern
import engine.patterns.VariableExpressionPattern
import engine.patterns.condition
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import engine.patterns.withOptionalIntegerCoefficient
import engine.steps.Transformation
import engine.steps.metadata.Metadata
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.metadata
import methods.polynomials.PolynomialsPlans

class SolvablePlans(private val simplificationSteps: StepsProducer) {

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

    inner class ApplyRuleAndSimplify(private val key: SolvableKey) : RunnerMethod {

        override val name = key.name
        override val runner = Runner { ctx: Context, sub: Expression ->
            val initialStep = key.rule.tryExecute(ctx, sub) ?: return@Runner null

            val builder = StepsBuilder(ctx, sub)
            builder.addStep(initialStep)
            simplificationSteps.produceSteps(ctx, builder.lastSub)?.let { builder.addSteps(it) }

            val key = getExplanationKey(key, ctx, sub)

            Transformation(
                type = Transformation.Type.Plan,
                fromExpr = sub,
                toExpr = builder.lastSub,
                steps = builder.getFinalSteps(),
                explanation = Metadata(key, initialStep.explanation!!.mappedParams),
            )
        }
    }

    val moveConstantsToTheLeftAndSimplify = ApplyRuleAndSimplify(SolvableKey.MoveConstantsToTheLeft)

    val moveConstantsToTheRightAndSimplify = ApplyRuleAndSimplify(SolvableKey.MoveConstantsToTheRight)

    val moveVariablesToTheLeftAndSimplify = ApplyRuleAndSimplify(SolvableKey.MoveVariablesToTheLeft)

    val moveVariablesToTheRightAndSimplify = ApplyRuleAndSimplify(SolvableKey.MoveVariablesToTheRight)

    val multiplyByInverseCoefficientOfVariableAndSimplify = ApplyRuleAndSimplify(
        SolvableKey.MultiplyByInverseCoefficientOfVariable,
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
            whilePossible(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
        }
    }

    val solvableRearrangementSteps = steps {
        // three ways to reorganize the solvable into aX = b form
        firstOf {
            option {
                // if the solvable is in the form `a = bX + c` with `b` non-negative, then
                // we move `c` to the left hand side and flip the solvable
                checkForm {
                    val lhs = ConstantInSolutionVariablePattern()
                    val nonConstantTerm = condition(VariableExpressionPattern()) {
                        it !is Minus && it !is Sum
                    }
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
                    val variable = VariableExpressionPattern()
                    val lhsVariable = withOptionalIntegerCoefficient(variable, false)
                    val rhsVariable = withOptionalIntegerCoefficient(variable, true)

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
                    val variable = condition { it !is Sum && !it.isConstantIn(solutionVariables) }
                    val lhsVariable = withOptionalConstantCoefficient(variable)
                    val rhsVariable = withOptionalConstantCoefficient(variable)

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

    /**
     * multiply by the LCM of the constant denominators if there are at least two fractions
     * or a single fraction with a non-constant numerator (including also 1/2 * (x + 1))
     */
    val removeConstantDenominatorsSteps = steps {
        check {
            val sumTerms = extractSumTermsFromSolvable(it)
            val denominators = sumTerms.mapNotNull { term -> DenominatorExtractor.extractDenominator(term) }
            denominators.size >= 2 || sumTerms.any { term ->
                fractionRequiringMultiplication.matches(this, term)
            }
        }
        apply(multiplyByLCDAndSimplify)
    }

    val coefficientRemovalSteps = steps {
        firstOf {
            // get rid of the coefficient of the variable
            option(multiplyByInverseCoefficientOfVariableAndSimplify)
            option(divideByCoefficientOfVariableAndSimplify)
            option {
                checkForm {
                    SolvablePattern(negOf(VariableExpressionPattern()), ConstantInSolutionVariablePattern())
                }
                apply(SolvableRules.NegateBothSides)
            }
        }
    }
}
