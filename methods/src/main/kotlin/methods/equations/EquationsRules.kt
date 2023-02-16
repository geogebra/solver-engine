package methods.equations

import engine.conditions.Sign
import engine.conditions.signOf
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.denominator
import engine.expressions.equationOf
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.numerator
import engine.expressions.plusMinusOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.rootOf
import engine.expressions.simplifiedFractionOf
import engine.expressions.simplifiedNegOf
import engine.expressions.solutionOf
import engine.expressions.solutionSetOf
import engine.expressions.splitPlusMinus
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.methods.ruleResult
import engine.operators.BinaryExpressionOperator
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.FixedPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.inSolutionVariable
import engine.patterns.integerCondition
import engine.patterns.monomialPattern
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.rationalMonomialPattern
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.steps.metadata.metadata
import engine.utility.isEven
import engine.utility.isOdd
import java.math.BigInteger

enum class EquationsRules(override val runner: Rule) : RunnerMethod {

    MoveEverythingToTheLeft(
        rule {
            val lhs = AnyPattern()
            val rhs = condition(AnyPattern()) { it != Constants.Zero }

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = equationOf(
                        sumOf(get(lhs), negOf(move(rhs))),
                        sumOf(get(rhs), negOf(move(rhs))),
                    ),
                    explanation = metadata(Explanation.MoveEverythingToTheLeft),
                )
            }
        },
    ),

    NegateBothSides(
        rule {
            val variable = oneOf(SolutionVariablePattern(), powerOf(SolutionVariablePattern(), AnyPattern()))
            val lhs = negOf(variable)
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = equationOf(get(variable), simplifiedNegOf(move(rhs))),
                    explanation = metadata(Explanation.NegateBothSides),
                )
            }
        },
    ),

    MultiplyByInverseOfLeadingCoefficient(
        rule {
            val lhs = inSolutionVariable(sumContaining())
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                val lhsExpr = get(lhs)
                val monomial = monomialPattern(SolutionVariablePattern())
                var degree = BigInteger.ZERO
                var leadingCoefficient: Expression? = null
                for (term in lhsExpr.children()) {
                    if (!term.isConstant()) {
                        val monomialMatch = matchPattern(monomial, term)
                        if (monomialMatch != null) {
                            val monomialDegree = monomial.exponent.getBoundInt(monomialMatch)
                            when {
                                monomialDegree > degree -> {
                                    leadingCoefficient = monomial.coefficient(monomialMatch)
                                    degree = monomialDegree
                                }
                                monomialDegree == degree -> {
                                    // The polynomial is not normalised
                                    return@onEquation null
                                }
                            }
                        }
                    }
                }
                if (leadingCoefficient == null || leadingCoefficient == Constants.One) {
                    return@onEquation null
                }
                val inverse = leadingCoefficient!!.inverse()

                ruleResult(
                    toExpr = equationOf(
                        productOf(get(lhs), inverse),
                        productOf(get(rhs), inverse),
                    ),
                    explanation = metadata(Explanation.MultiplyByInverseOfLeadingCoefficient),
                )
            }
        },
    ),

    MultiplyByInverseCoefficientOfVariable(
        rule {
            val lhs = monomialPattern(SolutionVariablePattern())
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                val coefficient = get(lhs::coefficient)!!

                if (coefficient.operator == BinaryExpressionOperator.Fraction) {
                    val inverse = if (coefficient.numerator() == Constants.One) {
                        coefficient.denominator()
                    } else {
                        fractionOf(coefficient.denominator(), coefficient.numerator())
                    }

                    ruleResult(
                        toExpr = equationOf(
                            productOf(get(lhs), inverse),
                            productOf(get(rhs), inverse),
                        ),
                        explanation = metadata(Explanation.MultiplyByInverseCoefficientOfVariable),
                    )
                } else {
                    null
                }
            }
        },
    ),

    CompleteTheSquare(
        rule {
            val variable = SolutionVariablePattern()

            val squaredOrder = UnsignedIntegerPattern()
            val squaredTerm = powerOf(variable, squaredOrder)
            val baseTerm = rationalMonomialPattern(variable)

            val lhs = ConditionPattern(
                sumOf(squaredTerm, baseTerm),
                integerCondition(squaredOrder, baseTerm.exponent) { a, b -> a == BigInteger.TWO * b },
            )

            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                val linearCoefficient = get(baseTerm::coefficient)!!
                val missingTerm = powerOf(
                    fractionOf(linearCoefficient, Constants.Two),
                    Constants.Two,
                )

                ruleResult(
                    toExpr = equationOf(sumOf(get(lhs), missingTerm), sumOf(get(rhs), missingTerm)),
                    explanation = metadata(Explanation.CompleteTheSquare),
                )
            }
        },
    ),

    DivideByCoefficientOfVariable(
        rule {
            val lhs = monomialPattern(SolutionVariablePattern())
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                when (val coefficient = get(lhs::coefficient)!!) {
                    Constants.One -> null
                    else -> ruleResult(
                        toExpr = equationOf(
                            fractionOf(get(lhs), coefficient),
                            fractionOf(get(rhs), coefficient),
                        ),
                        explanation = metadata(Explanation.DivideByCoefficientOfVariable),
                    )
                }
            }
        },
    ),

    FlipEquation(
        rule {
            val lhs = AnyPattern()
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = equationOf(move(rhs), move(lhs)),
                    explanation = metadata(Explanation.FlipEquation),
                )
            }
        },
    ),

    TakeRootOfBothSides(
        rule {
            val variableTerm = condition(AnyPattern()) { !it.isConstantIn(solutionVariable) }
            val exponent = UnsignedIntegerPattern()
            val lhs = powerOf(variableTerm, integerCondition(exponent) { it >= BigInteger.TWO })
            val rhs = ConstantInSolutionVariablePattern()

            onEquation(lhs, rhs) {
                val signOfRHS = get(rhs).signOf()
                val exponentValue = getValue(exponent)
                val newRHS = when {
                    signOfRHS == Sign.POSITIVE && exponentValue.isEven() ->
                        plusMinusOf(rootOf(move(rhs), move(exponent)))
                    exponentValue.isOdd() ->
                        rootOf(move(rhs), move(exponent))
                    // This case is actually handled in another rule... but it could be here?
                    signOfRHS == Sign.ZERO ->
                        move(rhs)
                    // In other cases (e.g. the RHS is negative and the power is even, the rule cannot apply
                    else -> return@onEquation null
                }
                ruleResult(
                    toExpr = equationOf(move(variableTerm), newRHS),
                    explanation = metadata(Explanation.TakeRootOfBothSides),
                )
            }
        },
    ),

    TakeRootOfBothSidesRHSIsZero(
        rule {
            val variableTerm = condition(AnyPattern()) { !it.isConstantIn(solutionVariable) }
            val exponent = UnsignedIntegerPattern()
            val lhs = powerOf(variableTerm, integerCondition(exponent) { it >= BigInteger.TWO })
            val rhs = FixedPattern(Constants.Zero)

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = equationOf(move(variableTerm), move(rhs)),
                    explanation = metadata(Explanation.TakeRootOfBothSidesRHSIsZero),
                )
            }
        },
    ),

    ExtractSolutionFromIdentity(
        rule {
            val value = ConstantInSolutionVariablePattern()

            onEquation(value, value) {
                ruleResult(
                    toExpr = solutionOf(xp(context.solutionVariable!!), Constants.Reals),
                    explanation = metadata(Explanation.ExtractSolutionFromIdentity),
                )
            }
        },
    ),

    ExtractSolutionFromContradiction(
        rule {
            val lhs = ConstantInSolutionVariablePattern()
            val rhs = ConstantInSolutionVariablePattern()

            onEquation(lhs, rhs) {
                if (get(lhs) != get(rhs)) {
                    ruleResult(
                        toExpr = solutionOf(xp(context.solutionVariable!!), Constants.EmptySet),
                        explanation = metadata(Explanation.ExtractSolutionFromContradiction),
                    )
                } else {
                    null
                }
            }
        },
    ),

    ExtractSolutionFromEvenPowerEqualsNegative(
        rule {
            val variableTerm = condition(AnyPattern()) { !it.isConstantIn(solutionVariable) }
            val exponent = UnsignedIntegerPattern()
            val lhs = powerOf(variableTerm, integerCondition(exponent) { it.isEven() })
            val rhs = condition(ConstantInSolutionVariablePattern()) { it.signOf() == Sign.NEGATIVE }

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = solutionOf(xp(context.solutionVariable!!), Constants.EmptySet),
                    explanation = metadata(Explanation.ExtractSolutionFromEvenPowerEqualsNegative),
                )
            }
        },
    ),

    ExtractSolutionFromEquationInSolvedForm(
        rule {
            val lhs = SolutionVariablePattern()
            val rhs = ConstantInSolutionVariablePattern()

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = solutionOf(move(lhs), solutionSetOf(move(rhs))),
                    explanation = metadata(Explanation.ExtractSolutionFromEquationInSolvedForm),
                )
            }
        },
    ),

    ExtractSolutionFromEquationInPlusMinusForm(
        rule {
            val lhs = SolutionVariablePattern()
            val rhs = ConstantInSolutionVariablePattern()

            onEquation(lhs, rhs) {
                val splitRhs = get(rhs).splitPlusMinus()
                ruleResult(
                    toExpr = solutionOf(move(lhs), solutionSetOf(splitRhs)),
                    explanation = metadata(Explanation.ExtractSolutionFromEquationInPlusMinusForm),
                )
            }
        },
    ),
}

/**
 * Perhaps those can be promoted to the engine if we have other use-cases
 */
private fun Expression.isNeg() = operator == UnaryExpressionOperator.Minus
private fun Expression.isFraction() = operator == BinaryExpressionOperator.Fraction

private fun Expression.inverse(): Expression = when {
    this == Constants.One -> this
    isNeg() -> simplifiedNegOf(firstChild.inverse())
    isFraction() -> simplifiedFractionOf(secondChild, firstChild)
    else -> fractionOf(Constants.One, this)
}
