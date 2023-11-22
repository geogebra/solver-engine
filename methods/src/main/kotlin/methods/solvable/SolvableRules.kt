package methods.solvable

import engine.conditions.isDefinitelyPositive
import engine.context.Context
import engine.context.Setting
import engine.context.emptyContext
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.AbsoluteValue
import engine.expressions.Constants
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.expressions.Fraction
import engine.expressions.Sum
import engine.expressions.asInteger
import engine.expressions.equationOf
import engine.expressions.fractionOf
import engine.expressions.greaterThanEqualOf
import engine.expressions.inequationOf
import engine.expressions.inverse
import engine.expressions.isSignedFraction
import engine.expressions.plusMinusOf
import engine.expressions.productOf
import engine.expressions.rootOf
import engine.expressions.simplifiedNegOf
import engine.expressions.simplifiedNegOfSum
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.expressions.withoutNegOrPlus
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.SolvablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.VariableExpressionPattern
import engine.patterns.condition
import engine.patterns.expressionWithFactor
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optionalIntegerPowerOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import engine.patterns.withOptionalConstantCoefficientInSolutionVariables
import engine.sign.Sign
import engine.steps.metadata.Metadata
import engine.utility.isEven
import engine.utility.isOdd
import engine.utility.lcm
import methods.solvable.DenominatorExtractor.extractDenominator
import java.math.BigInteger
import engine.steps.metadata.DragTargetPosition as Position
import engine.steps.metadata.GmPathModifier as PM

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
                    toExpr = cancel(common, solvable.deriveSolvable(restLeft, restRight)),
                    gmAction = drag(
                        common.within(lhs),
                        if (isBound(leftSum)) PM.Group else null,
                        common.within(rhs),
                        if (isBound(rightSum)) PM.Group else null,
                    ),
                    explanation = solvableExplanation(SolvableKey.CancelCommonTermsOnBothSides),
                )
            }
        },
    ),

    FindCommonIntegerFactorOnBothSides(
        rule {
            val factorLHS = SignedIntegerPattern()
            val factorRHS = SignedIntegerPattern()

            val productLHS = productContaining(factorLHS)
            val productRHS = productContaining(factorRHS)

            val lhs = oneOf(factorLHS, productLHS)
            val rhs = oneOf(factorRHS, productRHS)

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(
                ConditionPattern(
                    solvable,
                    integerCondition(factorLHS, factorRHS) { n, d ->
                        d != n && n.gcd(d) != BigInteger.ONE
                    },
                ),
            ) {
                val gcd = integerOp(factorLHS, factorRHS) { n, d -> n.gcd(d) }
                val lhsOverGCD = integerOp(factorLHS, factorRHS) { n, d -> n / n.gcd(d) }
                val rhsOverGCD = integerOp(factorLHS, factorRHS) { n, d -> d / n.gcd(d) }

                ruleResult(
                    toExpr = solvable.deriveSolvable(
                        if (isBound(productLHS)) {
                            productLHS.substitute(
                                simplifiedProductOf(gcd, lhsOverGCD),
                            )
                        } else {
                            productOf(gcd, lhsOverGCD)
                        },
                        if (isBound(productRHS)) {
                            productRHS.substitute(
                                simplifiedProductOf(gcd, rhsOverGCD),
                            )
                        } else {
                            productOf(gcd, rhsOverGCD)
                        },
                    ),
                    explanation = solvableExplanation(SolvableKey.FindCommonIntegerFactorOnBothSides),
                )
            }
        },
    ),

    CancelCommonFactorOnBothSides(
        rule {
            val commonFactor = AnyPattern()

            val lhsFactor = optionalIntegerPowerOf(commonFactor)
            val lhs = expressionWithFactor(lhsFactor)

            val rhsFactor = optionalIntegerPowerOf(commonFactor)
            val rhs = expressionWithFactor(rhsFactor)

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                val factor = get(commonFactor)
                // factor.isDefinitelyPositive() is too strong but it'll do for now.
                if (!factor.isDefinitelyPositive()) {
                    return@onPattern null
                }

                val newLHS: Expression
                val newRHS: Expression

                when ((getValue(lhsFactor.exponent) - getValue(rhsFactor.exponent)).signum()) {
                    1 -> {
                        val simplifiedPower = simplifiedPowerOf(
                            get(commonFactor),
                            integerOp(lhsFactor.exponent, rhsFactor.exponent) { n, m -> n - m },
                        )
                        newLHS = lhs.substitute(simplifiedPower)
                        newRHS = restOf(rhs)
                    }
                    0 -> {
                        newLHS = restOf(lhs)
                        newRHS = restOf(rhs)
                    }
                    else -> {
                        val simplifiedPower = simplifiedPowerOf(
                            get(commonFactor),
                            integerOp(lhsFactor.exponent, rhsFactor.exponent) { n, m -> m - n },
                        )
                        newLHS = restOf(lhs)
                        newRHS = rhs.substitute(simplifiedPower)
                    }
                }

                ruleResult(
                    toExpr = cancel(commonFactor, solvable.deriveSolvable(newLHS, newRHS)),
                    explanation = solvableExplanation(SolvableKey.CancelCommonFactorOnBothSides),
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
                val result = Mover.ConstantTerms.move(context, get(rhs), get(lhs)) ?: return@onPattern null
                ruleResult(
                    toExpr = solvable.deriveSolvable(result.toSide, result.fromSide),
                    gmAction = drag(result.movedTerms, PM.Group, lhs),
                    explanation = solvableExplanation(
                        SolvableKey.MoveConstantsToTheLeft,
                        parameters = listOf(result.movedTerms),
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
                val result = Mover.ConstantTerms.move(context, get(lhs), get(rhs)) ?: return@onPattern null
                ruleResult(
                    toExpr = solvable.deriveSolvable(result.fromSide, result.toSide),
                    gmAction = drag(result.movedTerms, PM.Group, rhs),
                    explanation = solvableExplanation(
                        SolvableKey.MoveConstantsToTheRight,
                        parameters = listOf(result.movedTerms),
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
                val result = Mover.VariableTerms.move(context, get(rhs), get(lhs)) ?: return@onPattern null
                ruleResult(
                    toExpr = solvable.deriveSolvable(result.toSide, result.fromSide),
                    gmAction = drag(result.movedTerms, if (get(rhs) is Sum) PM.Group else null, lhs),
                    explanation = solvableExplanation(
                        SolvableKey.MoveVariablesToTheLeft,
                        parameters = listOf(result.movedTerms),
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
                val result = Mover.VariableTerms.move(context, get(lhs), get(rhs)) ?: return@onPattern null
                ruleResult(
                    toExpr = solvable.deriveSolvable(result.fromSide, result.toSide),
                    gmAction = drag(result.movedTerms, if (get(lhs) is Sum) PM.Group else null, rhs),
                    explanation = solvableExplanation(
                        SolvableKey.MoveVariablesToTheRight,
                        parameters = listOf(result.movedTerms),
                    ),
                )
            }
        },
    ),

    MoveEverythingToTheLeft(
        rule {
            val lhs = AnyPattern()
            val rhs = condition { it != Constants.Zero }

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                val result = Mover.Everything.move(context, get(rhs), get(lhs)) ?: return@onPattern null
                ruleResult(
                    toExpr = solvable.deriveSolvable(result.toSide, result.fromSide),
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
                    .map { it.asInteger() ?: return@onPattern null }

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
            val variable = SolutionVariablePattern()
            val lhs = withOptionalConstantCoefficient(variable)
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
                    val newLhs = if (context.isSet(Setting.AdvancedBalancing)) {
                        get(variable)
                    } else {
                        productOf(inverse, get(lhs))
                    }
                    val newRhs = productOf(inverse, get(rhs))

                    ruleResult(
                        toExpr = solvable.deriveSolvable(newLhs, newRhs, useDual),
                        gmAction = drag(dragTarget, PM.Group, rhs),
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

    MultiplyByDenominatorOfVariable(multiplyByDenominatorOfVariable),

    DivideByCoefficientOfVariable(
        rule {
            val variable = VariableExpressionPattern()
            val lhs = withOptionalConstantCoefficientInSolutionVariables(variable)
            val rhs = ConstantInSolutionVariablePattern()

            val solvable = SolvablePattern(lhs, rhs)

            onPattern(solvable) {
                val coefficientValue = get(lhs::coefficient)!!
                if (coefficientValue == Constants.One || coefficientValue == Constants.MinusOne) return@onPattern null

                val coefficient = introduce(coefficientValue, coefficientValue)

                val newLhs = if (context.isSet(Setting.AdvancedBalancing)) {
                    get(variable)
                } else {
                    fractionOf(get(lhs), coefficient)
                }
                val newRhs = fractionOf(get(rhs), coefficient)

                if (solvable.isSelfDual()) {
                    val derivedSolvable = solvable.deriveSolvable(newLhs, newRhs, useDual = false)

                    val toExpr = if (coefficientValue.isConstant()) {
                        derivedSolvable
                    } else {
                        ExpressionWithConstraint(
                            derivedSolvable,
                            inequationOf(coefficientValue, Constants.Zero),
                        )
                    }

                    ruleResult(
                        toExpr = toExpr,
                        gmAction = drag(coefficient, PM.Group, rhs),
                        explanation = solvableExplanation(
                            SolvableKey.DivideByCoefficientOfVariable,
                            flipSign = false,
                            parameters = listOf(coefficient),
                        ),
                    )
                } else {
                    val useDual = when (coefficientValue.signOf()) {
                        Sign.POSITIVE -> false
                        Sign.NEGATIVE -> true
                        else -> return@onPattern null
                    }

                    ruleResult(
                        toExpr = solvable.deriveSolvable(newLhs, newRhs, useDual),
                        gmAction = drag(coefficient, PM.Group, rhs),
                        explanation = solvableExplanation(
                            SolvableKey.DivideByCoefficientOfVariable,
                            flipSign = useDual,
                            parameters = listOf(coefficient),
                        ),
                    )
                }
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
                        rhs.isNeg() -> drag(lhs, PM.Operator, rhs, PM.Operator)
                        else -> drag(lhs, PM.Operator, rhs, null, Position.LeftOf)
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
                    gmAction = drag(lhs, null, expression, PM.Operator, Position.Above),
                    explanation = solvableExplanation(SolvableKey.FlipSolvable),
                )
            }
        },
    ),

    MoveTermsNotContainingModulusToTheRight(moveTermsNotContainingModulusToTheRight),

    MoveTermsNotContainingModulusToTheLeft(moveTermsNotContainingModulusToTheLeft),

    TakeRootOfBothSides(
        rule {
            val variableTerm = VariableExpressionPattern()
            val exponent = UnsignedIntegerPattern()
            val lhs = powerOf(variableTerm, integerCondition(exponent) { it >= BigInteger.TWO })
            val rhs = ConstantInSolutionVariablePattern()

            onEquation(lhs, rhs) {
                val rhsValue = get(rhs)
                val signOfRHS = rhsValue.signOf()
                val exponentValue = getValue(exponent)
                val toExpr = when {
                    // This case is actually handled in another rule... but it could be here?
                    signOfRHS == Sign.ZERO ->
                        equationOf(move(variableTerm), move(rhs))
                    exponentValue.isEven() -> {
                        val toEq = equationOf(move(variableTerm), plusMinusOf(rootOf(move(rhs), move(exponent))))
                        when {
                            signOfRHS.implies(Sign.NON_NEGATIVE) || rhsValue.doubleValue > 0 -> toEq
                            !rhsValue.isConstant() -> ExpressionWithConstraint(
                                toEq,
                                greaterThanEqualOf(rhsValue, Constants.Zero),
                            )
                            else -> return@onEquation null
                        }
                    }
                    exponentValue.isOdd() ->
                        equationOf(move(variableTerm), rootOf(move(rhs), move(exponent)))

                    // In other cases (e.g. the RHS is negative and the power is even, the rule cannot apply
                    else -> return@onEquation null
                }
                ruleResult(
                    toExpr = toExpr,
                    gmAction = drag(exponent, rhs),
                    explanation = solvableExplanation(SolvableKey.TakeRootOfBothSides),
                )
            }
        },
    ),
}

private val moveTermsNotContainingModulusToTheRight = rule {
    val lhs = condition(sumContaining()) { it.countAbsoluteValues(solutionVariables) > 0 }
    val rhs = condition { it.countAbsoluteValues(solutionVariables) == 0 }
    val solvable = SolvablePattern(lhs, rhs)

    onPattern(solvable) {
        val result = Mover.TermsNotContainingModulus.move(context, get(lhs), get(rhs)) ?: return@onPattern null

        ruleResult(
            toExpr = solvable.deriveSolvable(result.fromSide, result.toSide),
            explanation = solvableExplanation(SolvableKey.MoveTermsNotContainingModulusToTheRight),
        )
    }
}

private val moveTermsNotContainingModulusToTheLeft = rule {
    val lhs = condition { it.countAbsoluteValues(solutionVariables) == 0 }
    val rhs = condition(sumContaining()) { it.countAbsoluteValues(solutionVariables) > 0 }
    val solvable = SolvablePattern(lhs, rhs)

    onPattern(solvable) {
        val result = Mover.TermsNotContainingModulus.move(context, get(rhs), get(lhs)) ?: return@onPattern null

        ruleResult(
            toExpr = solvable.deriveSolvable(result.toSide, result.fromSide),
            explanation = solvableExplanation(SolvableKey.MoveTermsNotContainingModulusToTheLeft),
        )
    }
}

private enum class Mover {
    ConstantTerms {
        override fun extractMove(c: Context, e: Expression) = extractConstants(e, c.solutionVariables)
        override fun extractRemain(c: Context, e: Expression) =
            extractVariableTerms(e, c.solutionVariables) ?: Constants.Zero
    },
    VariableTerms {
        override fun extractMove(c: Context, e: Expression) = extractVariableTerms(e, c.solutionVariables)
        override fun extractRemain(c: Context, e: Expression) =
            extractConstants(e, c.solutionVariables) ?: Constants.Zero
    },
    TermsNotContainingModulus {
        override fun extractMove(c: Context, e: Expression) = extractTermsNotContainingModulus(e, c.solutionVariables)

        override fun extractRemain(c: Context, e: Expression) =
            extractTermsContainingModulus(e, c.solutionVariables) ?: Constants.Zero
    },
    Everything {
        override fun extractMove(c: Context, e: Expression) = e
        override fun extractRemain(c: Context, e: Expression) = Constants.Zero
    },
    ;

    data class MoveResult(val movedTerms: Expression, val fromSide: Expression, val toSide: Expression)
    abstract fun extractMove(c: Context, e: Expression): Expression?
    abstract fun extractRemain(c: Context, e: Expression): Expression

    fun move(context: Context, fromSide: Expression, toSide: Expression): MoveResult? {
        val termsToMove = extractMove(context, fromSide)
        if (termsToMove == null || termsToMove == Constants.Zero) {
            return null
        }
        val negatedTerms = simplifiedNegOfSum(termsToMove)
        val fromSideAfter = if (context.isSet(Setting.AdvancedBalancing)) {
            extractRemain(context, fromSide)
        } else {
            sumOf(fromSide, negatedTerms)
        }
        val toSideAfter = if (toSide == Constants.Zero) {
            negatedTerms
        } else {
            sumOf(toSide, negatedTerms)
        }
        return MoveResult(termsToMove, fromSideAfter, toSideAfter)
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

private fun extractTermsContainingModulus(expression: Expression, variables: List<String>): Expression? {
    return when {
        expression is Sum -> {
            val termsWithModulus = expression.children.filter { it.countAbsoluteValues(variables) > 0 }
            if (termsWithModulus.isEmpty()) null else sumOf(termsWithModulus)
        }
        expression.countAbsoluteValues(variables) > 0 -> expression
        else -> null
    }
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
    private val denominator = AnyPattern()
    private val fraction = fractionOf(AnyPattern(), denominator)
    private val denominatorDetectingPattern = expressionWithFactor(fraction)

    fun extractDenominator(expression: Expression): Expression? {
        val match = denominatorDetectingPattern.findMatches(emptyContext, subexpression = expression).firstOrNull()
        return match?.let { denominator.getBoundExpr(it) }
    }

    fun extractFraction(expression: Expression): Fraction? {
        val match = denominatorDetectingPattern.findMatches(emptyContext, subexpression = expression).firstOrNull()
        return match?.let { fraction.getBoundExpr(it) as Fraction }
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

private val multiplyByDenominatorOfVariable = rule {
    val variable = VariableExpressionPattern()
    val lhs = withOptionalConstantCoefficientInSolutionVariables(variable)
    val rhs = ConstantInSolutionVariablePattern()

    val solvable = SolvablePattern(lhs, rhs)

    onPattern(solvable) {
        val denominator = extractDenominator(get(lhs::coefficient)!!) ?: return@onPattern null
        val useDual = if (solvable.isSelfDual()) {
            false
        } else {
            when (denominator.signOf()) {
                Sign.POSITIVE -> false
                Sign.NEGATIVE -> true
                Sign.NOT_ZERO -> if (solvable.isSelfDual()) false else return@onPattern null
                else -> return@onPattern null
            }
        }

        val newLhs = productOf(denominator, get(lhs))
        val newRhs = productOf(denominator, get(rhs))

        ruleResult(
            toExpr = solvable.deriveSolvable(newLhs, newRhs, useDual),
            explanation = solvableExplanation(
                SolvableKey.MultiplyByDenominatorOfVariable,
                flipSign = useDual && !solvable.isSelfDual(),
                parameters = listOf(denominator),
            ),
        )
    }
}
