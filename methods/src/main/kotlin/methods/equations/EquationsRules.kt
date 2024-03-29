/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package methods.equations

import engine.conditions.isDefinitelyNegative
import engine.conditions.isNotZeroBySign
import engine.expressions.Comparison
import engine.expressions.Constants
import engine.expressions.Contradiction
import engine.expressions.Equation
import engine.expressions.Product
import engine.expressions.SimpleComparator
import engine.expressions.Sum
import engine.expressions.Variable
import engine.expressions.VariableList
import engine.expressions.VoidExpression
import engine.expressions.contradictionOf
import engine.expressions.equationOf
import engine.expressions.expressionWithConstraintOf
import engine.expressions.finiteSetOf
import engine.expressions.fractionOf
import engine.expressions.greaterThanEqualOf
import engine.expressions.hasSingleValue
import engine.expressions.identityOf
import engine.expressions.inequationOf
import engine.expressions.inverse
import engine.expressions.leadingCoefficientOfPolynomial
import engine.expressions.lessThanOf
import engine.expressions.negOf
import engine.expressions.plusMinusOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.setSolutionOf
import engine.expressions.simplifiedNegOf
import engine.expressions.simplifiedProductOf
import engine.expressions.solutionVariableConstantChecker
import engine.expressions.splitPlusMinus
import engine.expressions.squareRootOf
import engine.expressions.statementSystemOf
import engine.expressions.statementSystemOfNotNullOrNull
import engine.expressions.statementUnionOf
import engine.expressions.sumOf
import engine.expressions.termwiseProductOf
import engine.expressions.variableListOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RuleResultBuilder
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.ConditionPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.FixedPattern
import engine.patterns.QuadraticPolynomialPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.SolvablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.VariableExpressionPattern
import engine.patterns.absoluteValueOf
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.expressionWithFactor
import engine.patterns.inSolutionVariables
import engine.patterns.integerCondition
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.rationalMonomialPattern
import engine.patterns.squareRootOf
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.patterns.withOptionalConstantCoefficient
import engine.sign.Sign
import engine.steps.Transformation
import engine.steps.metadata.metadata
import engine.utility.isEven
import engine.utility.withMaxDP
import methods.rationalexpressions.computeLcdAndMultipliers
import methods.solvable.DenominatorExtractor.extractDenominator
import methods.solvable.DenominatorExtractor.extractFraction
import methods.solvable.extractSumTermsFromSolvable
import java.math.BigInteger

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

    MultiplyByInverseOfLeadingCoefficient(
        rule {
            val lhs = inSolutionVariables(sumContaining())
            val rhs = AnyPattern()

            onEquation(lhs, rhs) {
                val lhsExpr = get(lhs) as Sum
                val leadingCoefficient = leadingCoefficientOfPolynomial(context, lhsExpr)
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

    CompleteTheSquare(
        rule {
            val variable = VariableExpressionPattern()

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

    TakeRootOfBothSidesRHSIsZero(
        rule {
            val variableTerm = VariableExpressionPattern()
            val exponent = integerCondition(UnsignedIntegerPattern()) { it >= BigInteger.TWO }
            val lhs = powerOf(variableTerm, exponent)
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

    ExtractSolutionFromConstantEquation(
        rule {
            val lhs = ConstantInSolutionVariablePattern()
            val rhs = ConstantInSolutionVariablePattern()

            val equation = equationOf(lhs, rhs)

            onPattern(equation) {
                val isSatisfied = (get(equation) as Comparison).holds(SimpleComparator) ?: return@onPattern null
                trueOrFalseRuleResult(isSatisfied)
            }
        },
    ),

    UndefinedConstantEquationIsFalse(
        rule {
            onPattern(FixedPattern(Constants.Undefined)) {
                ruleResult(
                    toExpr = Contradiction(VariableList(emptyList()), expression),
                    explanation = metadata(Explanation.UndefinedConstantEquationIsFalse),
                )
            }
        },
    ),

    UndefinedEquationCannotBeSolved(
        rule {
            onPattern(FixedPattern(Constants.Undefined)) {
                ruleResult(
                    toExpr = VoidExpression(),
                    explanation = metadata(Explanation.UndefinedEquationCannotBeSolved),
                )
            }
        },
    ),

    SolveEquationWithIncompatibleSigns(solveEquationWithIncompatibleSigns),

    ExtractSolutionFromNegativeUnderSquareRootInRealDomain(
        rule {
            val lhs = SolutionVariablePattern()
            val rhs = condition {
                // if any of the '+' or '-' term has non-real term in it
                // for e.g. if the expression contains `sqrt[-3]`
                it.isConstant() && it.splitPlusMinus().any { it.signOf() == Sign.NONE }
            }

            onEquation(lhs, rhs) {
                ruleResult(
                    toExpr = contradictionOf(variableListOf(get(lhs) as Variable), expression),
                    explanation = metadata(Explanation.ExtractSolutionFromNegativeUnderSquareRootInRealDomain),
                )
            }
        },
    ),

    ExtractSolutionFromEvenPowerEqualsNegative(
        rule {
            val variableTerm = VariableExpressionPattern()
            val exponent = UnsignedIntegerPattern()
            val lhs = powerOf(variableTerm, integerCondition(exponent) { it.isEven() })
            val rhs = condition { it.isDefinitelyNegative() }

            onEquation(lhs, rhs) {
                val rhsVal = get(rhs)
                val explanationArgument = if (rhsVal.signOf() == Sign.NEGATIVE) {
                    rhsVal
                } else {
                    @Suppress("MagicNumber")
                    equationOf(rhsVal, xp(rhsVal.doubleValue.toBigDecimal().withMaxDP(3)))
                }
                ruleResult(
                    toExpr = contradictionOf(variableListOf(context.solutionVariables), expression),
                    explanation = metadata(Explanation.ExtractSolutionFromEvenPowerEqualsNegative, explanationArgument),
                )
            }
        },
    ),

    ExtractSolutionFromEquationInSolvedForm(
        rule {
            val lhs = SolutionVariablePattern()
            val rhs = condition {
                // excluding values containing +/-
                it.isConstantIn(solutionVariables) && it.hasSingleValue()
            }

            onEquation(lhs, rhs) {
                ruleResult(
                    tags = listOf(Transformation.Tag.Pedantic),
                    toExpr = setSolutionOf(variableListOf(move(lhs) as Variable), finiteSetOf(move(rhs))),
                    explanation = metadata(Explanation.ExtractSolutionFromEquationInSolvedForm),
                )
            }
        },
    ),

    ExtractSolutionFromEquationInPlusMinusForm(extractSolutionFromEquationInPlusMinusForm),

    ApplyQuadraticFormula(applyQuadraticFormula),

    SeparateEquationInPlusMinusForm(separateEquationInPlusMinusForm),

    SeparateFactoredEquation(separateFactoredEquation),

    EliminateConstantFactorOfLhsWithZeroRhsDirectly(eliminateConstantFactorOfLhsWithZeroRhsDirectly),

    SeparateModulusEqualsPositiveConstant(separateModulusEqualsPositiveConstant),
    ResolveModulusEqualsZero(resolveModulusEqualsZero),
    ExtractSolutionFromModulusEqualsNegativeConstant(extractSolutionFromModulusEqualsNegativeConstant),

    MoveSecondModulusToRhs(moveSecondModulusToRhs),
    MoveSecondModulusToLhs(moveSecondModulusToLhs),
    SeparateModulusEqualsModulus(separateModulusEqualsModulus),
    ResolveModulusEqualsNegativeModulus(resolveModulusEqualsNegativeModulus),

    SeparateModulusEqualsExpression(separateModulusEqualsExpression),
    SeparateModulusEqualsExpressionWithoutConstraint(separateModulusEqualsExpressionWithoutConstraint),

    MultiplyBothSidesOfRationalEquationWithTrivialLCD(multiplyBothSidesOfRationalEquationWithTrivialLCD),
    MultiplyBothSidesOfRationalEquation(multiplyBothSidesOfRationalEquation),

    SplitEquationWithRationalVariables(splitEquationWithRationalVariables),
}

private val extractSolutionFromEquationInPlusMinusForm = rule {
    val lhs = SolutionVariablePattern()
    val rhs = ConstantInSolutionVariablePattern()

    onEquation(lhs, rhs) {
        val splitRhs = get(rhs).splitPlusMinus()
        if (splitRhs.size < 2) {
            return@onEquation null
        }
        ruleResult(
            toExpr = setSolutionOf(variableListOf(move(lhs) as Variable), finiteSetOf(splitRhs)),
            explanation = metadata(Explanation.ExtractSolutionFromEquationInPlusMinusForm),
        )
    }
}

private val applyQuadraticFormula = rule {
    val quadraticPolynomial = QuadraticPolynomialPattern(
        variable = SolutionVariablePattern(),
        constantChecker = solutionVariableConstantChecker,
    )
    val rhs = FixedPattern(Constants.Zero)

    onEquation(quadraticPolynomial, rhs) {
        val a = get(quadraticPolynomial::quadraticCoefficient)!!
        val b = get(quadraticPolynomial::linearCoefficient)!!
        val c = get(quadraticPolynomial::constantTerm)!!

        val discriminant = sumOf(
            powerOf(b, Constants.Two),
            negOf(
                productOf(Constants.Four, a, c),
            ),
        )
        val denominator = productOf(
            Constants.Two,
            a,
        )
        val sol = fractionOf(
            sumOf(
                negOf(b),
                plusMinusOf(
                    squareRootOf(discriminant),
                ),
            ),
            denominator,
        )

        val constraint = statementSystemOfNotNullOrNull(
            if (discriminant.isConstant()) null else greaterThanEqualOf(discriminant, Constants.Zero),
            if (denominator.isConstant()) null else inequationOf(denominator, Constants.Zero),
        )

        ruleResult(
            toExpr = expressionWithConstraintOf(equationOf(get(quadraticPolynomial.variable), sol), constraint),
            explanation = metadata(Explanation.ApplyQuadraticFormula),
        )
    }
}

private val separateFactoredEquation = rule {
    val product = condition {
        it is Product && it.children.count { factor -> !factor.isConstantIn(solutionVariables) } > 1
    }

    onEquation(product, FixedPattern(Constants.Zero)) {
        val factors = get(product).children
        ruleResult(
            toExpr = statementUnionOf(factors.map { equationOf(it, Constants.Zero) }),
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
            toExpr = statementUnionOf(splitEquations),
            explanation = metadata(Explanation.SeparatePlusMinusQuadraticSolutions),
        )
    }
}

private val eliminateConstantFactorOfLhsWithZeroRhsDirectly = rule {
    val factor = condition { it.isConstant() && it.isNotZeroBySign() }
    val unsignedLHS = productContaining(factor)
    val lhs = optionalNegOf(unsignedLHS)
    val rhs = FixedPattern(Constants.Zero)

    val solvable = SolvablePattern(condition(lhs) { !it.isConstant() }, rhs)

    onPattern(solvable) {
        val canceledFactor = copySign(lhs, get(factor))
        val newLHS = cancel(canceledFactor, restOf(unsignedLHS))

        ruleResult(
            toExpr = solvable.deriveSolvable(newLHS, get(rhs)),
            explanation = metadata(Explanation.EliminateConstantFactorOfLhsWithZeroRhs, canceledFactor),
        )
    }
}

private val separateModulusEqualsPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.POSITIVE }

    onEquation(lhs, rhs) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = statementUnionOf(
                equationOf(newLHS, get(rhs)),
                equationOf(newLHS, negOf(get(rhs))),
            ),
            explanation = metadata(Explanation.SeparateModulusEqualsPositiveConstant),
        )
    }
}

private val resolveModulusEqualsZero = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = FixedPattern(Constants.Zero)

    onEquation(lhs, rhs) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = equationOf(newLHS, get(rhs)),
            explanation = metadata(Explanation.ResolveModulusEqualsZero),
        )
    }
}

private val extractSolutionFromModulusEqualsNegativeConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.NEGATIVE }

    onEquation(lhs, rhs) {
        ruleResult(
            toExpr = contradictionOf(variableListOf(context.solutionVariables), expression),
            explanation = metadata(Explanation.ExtractSolutionFromModulusEqualsNegativeConstant),
        )
    }
}

private val moveSecondModulusToRhs = rule {
    val firstModulus = withOptionalConstantCoefficient(absoluteValueOf(AnyPattern()))
    val secondModulus = withOptionalConstantCoefficient(absoluteValueOf(AnyPattern()))
    val lhs = sumOf(firstModulus, secondModulus)
    val rhs = FixedPattern(Constants.Zero)

    onEquation(lhs, rhs) {
        val firstModulusSign = firstModulus.getCoefficient().signOf()
        val secondModulusSign = secondModulus.getCoefficient().signOf()
        val termToSubtract = simplifiedNegOf(
            if (firstModulusSign == Sign.POSITIVE || secondModulusSign != Sign.POSITIVE) {
                get(secondModulus)
            } else {
                get(firstModulus)
            },
        )
        ruleResult(
            toExpr = equationOf(sumOf(get(lhs), termToSubtract), termToSubtract),
            explanation = metadata(Explanation.MoveSecondModulusToRhs),
        )
    }
}

private val moveSecondModulusToLhs = rule {
    val firstModulus = withOptionalConstantCoefficient(absoluteValueOf(AnyPattern()))
    val secondModulus = withOptionalConstantCoefficient(absoluteValueOf(AnyPattern()))
    val rhs = sumOf(firstModulus, secondModulus)
    val lhs = FixedPattern(Constants.Zero)

    onEquation(lhs, rhs) {
        val firstModulusSign = firstModulus.getCoefficient().signOf()
        val secondModulusSign = secondModulus.getCoefficient().signOf()
        val termToSubtract = simplifiedNegOf(
            if (firstModulusSign == Sign.POSITIVE || secondModulusSign != Sign.POSITIVE) {
                get(secondModulus)
            } else {
                get(firstModulus)
            },
        )
        ruleResult(
            toExpr = equationOf(termToSubtract, sumOf(get(rhs), termToSubtract)),
            explanation = metadata(Explanation.MoveSecondModulusToLhs),
        )
    }
}

private val separateModulusEqualsModulus = rule {
    val innerLHS = AnyPattern()
    val innerRHS = AnyPattern()
    val lhs = withOptionalConstantCoefficient(absoluteValueOf(innerLHS), positiveOnly = true)
    val rhs = withOptionalConstantCoefficient(absoluteValueOf(innerRHS), positiveOnly = true)

    onEquation(lhs, rhs) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(innerLHS))
        val newRHS = simplifiedProductOf(rhs.getCoefficient(), get(innerRHS))
        ruleResult(
            toExpr = statementUnionOf(
                equationOf(newLHS, newRHS),
                equationOf(newLHS, negOf(newRHS)),
            ),
            explanation = metadata(Explanation.SeparateModulusEqualsPositiveConstant),
        )
    }
}

private val resolveModulusEqualsNegativeModulus = rule {
    val innerLHS = AnyPattern()
    val innerRHS = AnyPattern()
    val lhs = withOptionalConstantCoefficient(absoluteValueOf(innerLHS), positiveOnly = true)
    val rhs = withOptionalConstantCoefficient(absoluteValueOf(innerRHS))

    onEquation(lhs, rhs) {
        val lhsCoeff = lhs.getCoefficient()
        val rhsCoeff = simplifiedNegOf(rhs.getCoefficient())
        if (rhsCoeff.signOf() != Sign.POSITIVE) {
            return@onEquation null
        }
        val newLHS = simplifiedProductOf(lhsCoeff, get(innerLHS))
        val newRHS = simplifiedProductOf(rhsCoeff, get(innerRHS))
        ruleResult(
            toExpr = statementSystemOf(
                equationOf(newLHS, Constants.Zero),
                equationOf(newRHS, Constants.Zero),
            ),
            explanation = metadata(Explanation.ResolveModulusEqualsNegativeModulus),
        )
    }
}

private val separateModulusEqualsExpression = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = AnyPattern()

    onEquation(lhs, rhs) {
        val signedLHSValue = get(signedLHS)
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), signedLHSValue)
        val rhsValue = get(rhs)
        ruleResult(
            toExpr = statementUnionOf(
                statementSystemOf(
                    equationOf(newLHS, rhsValue),
                    greaterThanEqualOf(signedLHSValue, Constants.Zero),
                ),
                statementSystemOf(
                    equationOf(negOf(newLHS), rhsValue),
                    lessThanOf(signedLHSValue, Constants.Zero),
                ),
            ),
            explanation = metadata(Explanation.SeparateModulusEqualsExpression),
        )
    }
}

private val separateModulusEqualsExpressionWithoutConstraint = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = AnyPattern()

    onEquation(lhs, rhs) {
        val signedLHSValue = get(signedLHS)
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), signedLHSValue)
        val rhsValue = get(rhs)
        ruleResult(
            toExpr = statementUnionOf(
                equationOf(newLHS, rhsValue),
                equationOf(newLHS, negOf(rhsValue)),
            ),
            explanation = metadata(Explanation.SeparateModulusEqualsExpressionWithoutConstraint),
        )
    }
}

private fun RuleResultBuilder.trueOrFalseRuleResult(isSatisfied: Boolean): Transformation {
    val noVariable = context.solutionVariables.isEmpty()
    val variableList = variableListOf(context.solutionVariables)
    val toExpr = if (isSatisfied) {
        identityOf(variableList, expression)
    } else {
        contradictionOf(variableList, expression)
    }
    return if (noVariable) {
        val key = if (isSatisfied) {
            Explanation.ExtractTruthFromTrueEquality
        } else {
            Explanation.ExtractFalsehoodFromFalseEquality
        }
        ruleResult(toExpr = toExpr, explanation = metadata(key))
    } else {
        val key = if (isSatisfied) {
            Explanation.ExtractSolutionFromIdentity
        } else {
            Explanation.ExtractSolutionFromContradiction
        }
        ruleResult(toExpr = toExpr, explanation = metadata(key, variableList))
    }
}

private val multiplyBothSidesOfRationalEquationWithTrivialLCD = rule {
    val lhs = AnyPattern()
    val rhs = AnyPattern()

    onEquation(lhs, rhs) {
        val commonDenominator = extractSumTermsFromSolvable(expression)
            .mapNotNull { extractDenominator(it) }.distinct().singleOrNull()
            ?: return@onEquation null

        val newLhs = termwiseProductOf(get(lhs), introduce(commonDenominator))
        val newRhs = termwiseProductOf(get(rhs), introduce(commonDenominator))
        val newEq = equationOf(newLhs, newRhs)

        ruleResult(
            toExpr = newEq,
            explanation = metadata(Explanation.MultiplyBothSidesByDenominator, commonDenominator),
        )
    }
}

private val multiplyBothSidesOfRationalEquation = rule {
    val lhs = AnyPattern()
    val rhs = AnyPattern()

    onEquation(lhs, rhs) {
        val denominators = extractSumTermsFromSolvable(expression).mapNotNull { extractFraction(it) }
        if (denominators.isEmpty()) return@onEquation null
        val (lcd, _) = computeLcdAndMultipliers(denominators)

        val newLhs = termwiseProductOf(get(lhs), introduce(lcd))
        val newRhs = termwiseProductOf(get(rhs), introduce(lcd))
        val newEq = equationOf(newLhs, newRhs)

        ruleResult(
            toExpr = newEq,
            explanation = metadata(Explanation.MultiplyBothSidesByDenominator, lcd),
        )
    }
}

val splitEquationWithRationalVariables = rule {
    // This is incomplete but enough for what we want to use it for for now, which is to separate
    // an equation of the form X + Ysqrt[N] = A + Bsqrt[N] into X = A AND Ysqrt[N] = Bsqrt[N]
    val root = squareRootOf(UnsignedIntegerPattern())
    val lhsTermWithRoot = expressionWithFactor(root)
    val lhs = sumContaining(lhsTermWithRoot)
    val rhsTermWithRoot = expressionWithFactor(root)
    val rhs = sumContaining(rhsTermWithRoot)

    onEquation(lhs, rhs) {
        ruleResult(
            toExpr = statementSystemOf(
                equationOf(restOf(lhs), restOf(rhs)),
                equationOf(get(lhsTermWithRoot), get(rhsTermWithRoot)),
            ),
            explanation = metadata(Explanation.SplitEquationWithRationalVariables),
        )
    }
}

val solveEquationWithIncompatibleSigns = rule {
    onPattern(equationOf(AnyPattern(), AnyPattern())) {
        val equation = expression as Equation
        val lhsSign = equation.lhs.signOf()
        val rhsSign = equation.rhs.signOf()
        if (lhsSign != Sign.NONE && rhsSign != Sign.NONE && lhsSign.implies(rhsSign.complement())) {
            val key = when {
                lhsSign == Sign.ZERO || rhsSign == Sign.ZERO -> Explanation.EquationHasOneZeroAndOneNonZeroSide
                else -> Explanation.EquationSidesHaveIncompatibleSigns
            }
            ruleResult(
                toExpr = contradictionOf(variableListOf(context.solutionVariables), expression),
                explanation = metadata(key),
            )
        } else {
            null
        }
    }
}
