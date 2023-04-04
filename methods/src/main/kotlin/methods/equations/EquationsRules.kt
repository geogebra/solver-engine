package methods.equations

import engine.conditions.Sign
import engine.conditions.isDefinitelyNotUndefined
import engine.conditions.signOf
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.contradictionOf
import engine.expressions.denominator
import engine.expressions.equationOf
import engine.expressions.equationUnionOf
import engine.expressions.fractionOf
import engine.expressions.hasSingleValue
import engine.expressions.identityOf
import engine.expressions.inverse
import engine.expressions.isNeg
import engine.expressions.isSignedFraction
import engine.expressions.negOf
import engine.expressions.plusMinusOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.rootOf
import engine.expressions.simplifiedNegOf
import engine.expressions.solutionOf
import engine.expressions.solutionSetOf
import engine.expressions.splitPlusMinus
import engine.expressions.squareRootOf
import engine.expressions.sumOf
import engine.expressions.variableListOf
import engine.expressions.withoutNegOrPlus
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.ConditionPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.ConstantPattern
import engine.patterns.EquationPattern
import engine.patterns.FixedPattern
import engine.patterns.KeyedPattern
import engine.patterns.Match
import engine.patterns.Pattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeProductOf
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.inSolutionVariables
import engine.patterns.integerCondition
import engine.patterns.monomialPattern
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.rationalMonomialPattern
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.Transformation
import engine.steps.metadata.metadata
import engine.utility.isEven
import engine.utility.isOdd
import engine.utility.withMaxDP
import methods.solvable.simplifiedNegOfSum
import java.math.BigInteger
import engine.steps.metadata.DragTargetPosition as Position
import engine.steps.metadata.GmPathModifier as PM

enum class EquationsRules(override val runner: Rule) : RunnerMethod {

    CollectLikeTermsToTheLeft(
        rule {
            val variable = ArbitraryVariablePattern()
            val lhsTerm = withOptionalConstantCoefficient(variable)
            val rhsTerm = withOptionalConstantCoefficient(variable)
            val lhs = oneOf(lhsTerm, sumContaining(lhsTerm))
            val rhs = oneOf(rhsTerm, sumContaining(rhsTerm))

            onEquation(lhs, rhs) {
                val negatedRhsTerm = simplifiedNegOf(get(rhsTerm))
                ruleResult(
                    toExpr = equationOf(
                        sumOf(get(lhs), negatedRhsTerm),
                        sumOf(get(rhs), negatedRhsTerm),
                    ),
                    explanation = metadata(Explanation.CollectLikeTermsToTheLeft, get(variable)),
                )
            }
        },
    ),

    MoveEverythingToTheLeft(
        rule {
            val lhs = AnyPattern()
            val rhs = condition(AnyPattern()) { it != Constants.Zero }

            onEquation(lhs, rhs) {
                val negatedRhs = simplifiedNegOfSum(get(rhs))

                ruleResult(
                    toExpr = equationOf(
                        sumOf(get(lhs), negatedRhs),
                        sumOf(get(rhs), negatedRhs),
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
            val rhs = optionalNegOf(AnyPattern())

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = equationOf(get(variable), simplifiedNegOf(move(rhs))),
                    gmAction = when {
                        rhs.isNeg() -> drag(lhs, PM.Operator, rhs, PM.Operator)
                        else -> drag(lhs, PM.Operator, rhs, null, Position.LeftOf)
                    },
                    explanation = metadata(Explanation.NegateBothSides),
                )
            }
        },
    ),

    MultiplyByInverseOfLeadingCoefficient(
        rule {
            val lhs = inSolutionVariables(sumContaining())
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                val lhsExpr = get(lhs)
                val leadingCoefficient = leadingCoefficientOfPolynomial(lhsExpr)
                if (leadingCoefficient == null || leadingCoefficient == Constants.One) {
                    return@onEquation null
                }
                val inverse = introduce(leadingCoefficient, leadingCoefficient.inverse())

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

                if (coefficient.isSignedFraction()) {
                    val inverse = coefficient.inverse()
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
                        val positiveCoefficient = coefficient.withoutNegOrPlus()
                        if (positiveCoefficient.parent !== null) {
                            positiveCoefficient
                        } else {
                            // TODO What should we do in situations like `[2x/5]`? In
                            // that, this logic would only provide a drag target of `5` so
                            // the `2` would still be left behind. It's not ideal that GM
                            // would require two steps to move the `[2/3]`, while Solver
                            // would take only one step.
                            positiveCoefficient.denominator()
                        }
                    }
                    ruleResult(
                        toExpr = equationOf(
                            productOf(get(lhs), inverse),
                            productOf(get(rhs), inverse),
                        ),
                        gmAction = drag(dragTarget, rhs, Position.LeftOf),
                        explanation = metadata(Explanation.MultiplyByInverseCoefficientOfVariable),
                    )
                } else {
                    null
                }
            }
        },
    ),

    FactorNegativeSignOfLeadingCoefficient(
        rule {
            val lhs = inSolutionVariables(sumContaining())
            val rhs = FixedPattern(Constants.Zero)

            onEquation(lhs, rhs) {
                val lhsExpr = get(lhs)
                val leadingCoefficient = leadingCoefficientOfPolynomial(lhsExpr)
                if ((leadingCoefficient == null) || (leadingCoefficient.signOf() == Sign.POSITIVE)) {
                    return@onEquation null
                }

                val additiveInverseLhsExpr = simplifiedNegOfSum(lhsExpr)

                ruleResult(
                    toExpr = equationOf(
                        productOf(negOf(Constants.One), additiveInverseLhsExpr),
                        get(rhs),
                    ),
                    explanation = metadata(Explanation.FactorNegativeSignOfLeadingCoefficient),
                )
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
                val coefficientValue = get(lhs::coefficient)!!

                when (val coefficient = introduce(coefficientValue, coefficientValue)) {
                    Constants.One -> null
                    else -> ruleResult(
                        toExpr = equationOf(
                            fractionOf(get(lhs), coefficient),
                            fractionOf(get(rhs), coefficient),
                        ),
                        gmAction = drag(coefficient, rhs, Position.Below),
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
            val eq = EquationPattern(lhs, rhs)

            onPattern(eq) {
                ruleResult(
                    toExpr = equationOf(move(rhs), move(lhs)),
                    gmAction = drag(lhs, null, eq, PM.Operator, Position.Above),
                    explanation = metadata(Explanation.FlipEquation),
                )
            }
        },
    ),

    TakeRootOfBothSides(
        rule {
            val variableTerm = condition(AnyPattern()) { !it.isConstantIn(solutionVariables) }
            val exponent = UnsignedIntegerPattern()
            val lhs = powerOf(variableTerm, integerCondition(exponent) { it >= BigInteger.TWO })
            val rhs = ConstantInSolutionVariablePattern()

            onEquation(lhs, rhs) {
                val rhsValue = get(rhs)
                val signOfRHS = rhsValue.signOf()
                val exponentValue = getValue(exponent)
                val newRHS = when {
                    exponentValue.isEven() && (signOfRHS == Sign.POSITIVE || rhsValue.doubleValue > 0) ->
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
                    gmAction = drag(exponent, rhs),
                    explanation = metadata(Explanation.TakeRootOfBothSides),
                )
            }
        },
    ),

    TakeRootOfBothSidesRHSIsZero(
        rule {
            val variableTerm = condition(AnyPattern()) { !it.isConstantIn(solutionVariables) }
            val exponent = UnsignedIntegerPattern()
            val lhs = powerOf(variableTerm, integerCondition(exponent) { it >= BigInteger.TWO })
            val rhs = FixedPattern(Constants.Zero)

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = equationOf(move(variableTerm), move(rhs)),
                    gmAction = drag(exponent, rhs),
                    explanation = metadata(Explanation.TakeRootOfBothSidesRHSIsZero),
                )
            }
        },
    ),

    ExtractSolutionFromIdentity(
        rule {
            val value = condition(ConstantInSolutionVariablePattern()) { it.isDefinitelyNotUndefined() }
            val eqn = equationOf(value, value)
            onPattern(eqn) {
                ruleResult(
                    toExpr = identityOf(variableListOf(context.solutionVariables), get(eqn)),
                    explanation = metadata(
                        Explanation.ExtractSolutionFromIdentity,
                        variableListOf(context.solutionVariables),
                    ),
                )
            }
        },
    ),

    ExtractSolutionFromContradiction(
        rule {
            val lhs = ConstantInSolutionVariablePattern()
            val rhs = ConstantInSolutionVariablePattern()
            val eqn = equationOf(lhs, rhs)
            onPattern(eqn) {
                if (get(lhs) != get(rhs)) {
                    ruleResult(
                        toExpr = contradictionOf(variableListOf(context.solutionVariables), get(eqn)),
                        explanation = metadata(
                            Explanation.ExtractSolutionFromContradiction,
                            variableListOf(context.solutionVariables),
                        ),
                    )
                } else {
                    null
                }
            }
        },
    ),

    ExtractSolutionFromNegativeUnderSquareRootInRealDomain(
        rule {
            val lhs = SolutionVariablePattern()
            val rhs = condition(ConstantPattern()) {
                // if any of the '+' or '-' term has non-real term in it
                // for e.g. if the expression contains `sqrt[-3]`
                it.splitPlusMinus().any { child -> child.signOf() == Sign.NONE }
            }

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = solutionOf(get(lhs), Constants.EmptySet),
                    explanation = metadata(Explanation.ExtractSolutionFromNegativeUnderSquareRootInRealDomain),
                )
            }
        },
    ),

    ExtractSolutionFromEvenPowerEqualsNegative(
        rule {
            val variableTerm = condition(AnyPattern()) { !it.isConstantIn(solutionVariables) }
            val exponent = UnsignedIntegerPattern()
            val lhs = powerOf(variableTerm, integerCondition(exponent) { it.isEven() })
            val rhs = condition(ConstantInSolutionVariablePattern()) { it.doubleValue < 0 }

            onEquation(lhs, rhs) {
                val rhsVal = get(rhs)
                val explanationArgument = if (rhsVal.signOf() == engine.conditions.Sign.NEGATIVE) {
                    rhsVal
                } else {
                    @Suppress("MagicNumber")
                    equationOf(rhsVal, xp(rhsVal.doubleValue.toBigDecimal().withMaxDP(3)))
                }
                ruleResult(
                    toExpr = solutionOf(xp(context.solutionVariables.first()), Constants.EmptySet),
                    explanation = metadata(Explanation.ExtractSolutionFromEvenPowerEqualsNegative, explanationArgument),
                )
            }
        },
    ),

    ExtractSolutionFromEquationInSolvedForm(
        rule {
            val lhs = SolutionVariablePattern()
            val rhs = condition(ConstantInSolutionVariablePattern()) {
                // excluding values containing +/-
                it.hasSingleValue()
            }

            onEquation(lhs, rhs) {
                if (context.gmFriendly) return@onEquation null
                ruleResult(
                    tags = listOf(Transformation.Tag.Pedantic),
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
                if (splitRhs.size < 2) {
                    return@onEquation null
                }
                ruleResult(
                    toExpr = solutionOf(move(lhs), solutionSetOf(splitRhs)),
                    explanation = metadata(Explanation.ExtractSolutionFromEquationInPlusMinusForm),
                )
            }
        },
    ),

    ExtractSolutionFromEquationInUnionForm(
        rule {
            val lhs = SolutionVariablePattern()
            val rhs1 = ConstantPattern()
            val rhs2 = ConstantPattern()
            val eq1 = equationOf(lhs, rhs1)
            val eq2 = equationOf(lhs, rhs2)
            val pattern = engine.patterns.equationUnionOf(eq1, eq2)

            onPattern(pattern) {
                ruleResult(
                    toExpr = solutionOf(move(lhs), solutionSetOf(get(rhs1), get(rhs2))),
                    explanation = metadata(Explanation.ExtractSolutionFromEquationInUnionForm),
                )
            }
        },
    ),

    ApplyQuadraticFormula(applyQuadraticFormula),

    SeparateEquationInPlusMinusForm(separateEquationInPlusMinusForm),

    SeparateFactoredEquation(separateFactoredEquation),

    EliminateConstantFactorOfLhsWithZeroRhs(eliminateConstantFactorOfLhsWithZeroRhs),
}

private val applyQuadraticFormula = rule {
    val solutionVariable = SolutionVariablePattern()
    val quadraticPolynomial = QuadraticPolynomialPattern(solutionVariable)
    val rhs = FixedPattern(Constants.Zero)

    onEquation(quadraticPolynomial, rhs) {
        val a = get(quadraticPolynomial::quadraticCoefficient)!!
        val b = get(quadraticPolynomial::linearCoefficient)!!
        val c = get(quadraticPolynomial::constantCoefficient)!!

        val discriminant = sumOf(
            powerOf(b, Constants.Two),
            negOf(
                productOf(Constants.Four, a, c),
            ),
        )
        val sol = fractionOf(
            sumOf(
                negOf(b),
                plusMinusOf(
                    squareRootOf(discriminant),
                ),
            ),
            productOf(
                Constants.Two,
                a,
            ),
        )

        ruleResult(
            toExpr = equationOf(get(solutionVariable), sol),
            explanation = metadata(Explanation.ApplyQuadraticFormula),
        )
    }
}

private val separateFactoredEquation = rule {
    val product = productContaining()

    onEquation(product, FixedPattern(Constants.Zero)) {
        val factors = get(product).children
        ruleResult(
            toExpr = equationUnionOf(factors.map { equationOf(it, Constants.Zero) }),
            explanation = metadata(Explanation.SeparateFactoredEquation),
        )
    }
}

private val separateEquationInPlusMinusForm = rule {
    val eq = inSolutionVariables(equationOf(AnyPattern(), AnyPattern()))

    onPattern(eq) {
        val splitEquations = get(eq).splitPlusMinus()
        if (splitEquations.size <= 1) {
            return@onPattern null
        }
        ruleResult(
            toExpr = equationUnionOf(splitEquations),
            explanation = metadata(Explanation.SeparatePlusMinusQuadraticSolutions),
        )
    }
}

private val eliminateConstantFactorOfLhsWithZeroRhs = rule {
    val factor = ConstantPattern()
    val polynomial = inSolutionVariables(sumContaining())
    val lhs = commutativeProductOf(factor, polynomial)
    val rhs = FixedPattern(Constants.Zero)

    onEquation(lhs, rhs) {
        val newLhs = cancel(factor, get(polynomial))

        ruleResult(
            toExpr = equationOf(newLhs, get(rhs)),
            explanation = metadata(Explanation.EliminateConstantFactorOfLhsWithZeroRhs),
        )
    }
}

class QuadraticPolynomialPattern(val variable: Pattern) : KeyedPattern {

    private val quadraticTerm = withOptionalConstantCoefficient(powerOf(variable, FixedPattern(Constants.Two)))

    private val linearTerm = withOptionalConstantCoefficient(variable)

    private val constantTerm = ConstantPattern()

    private val pureQuadraticPolynomial = commutativeSumOf(quadraticTerm, constantTerm)

    private val incompleteQuadraticPolynomial = commutativeSumOf(quadraticTerm, linearTerm)

    private val completeQuadraticPolynomial = commutativeSumOf(quadraticTerm, linearTerm, constantTerm)

    private val ptn = oneOf(
        pureQuadraticPolynomial,
        incompleteQuadraticPolynomial,
        completeQuadraticPolynomial,
    )

    override val key = ptn.key

    fun quadraticCoefficient(match: Match) = quadraticTerm.coefficient(match)

    fun linearCoefficient(match: Match): Expression {
        return when (pureQuadraticPolynomial.getBoundExpr(match)) {
            null -> linearTerm.coefficient(match)
            else -> Constants.Zero
        }
    }

    fun constantCoefficient(match: Match): Expression {
        return when (incompleteQuadraticPolynomial.getBoundExpr(match)) {
            null -> constantTerm.getBoundExpr(match)!!
            else -> Constants.Zero
        }
    }
}
