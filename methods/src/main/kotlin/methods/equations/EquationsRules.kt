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
import engine.expressions.Constants.Pi
import engine.expressions.Constants.Two
import engine.expressions.Contradiction
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.Fraction
import engine.expressions.Minus
import engine.expressions.Product
import engine.expressions.SimpleComparator
import engine.expressions.StatementUnion
import engine.expressions.Sum
import engine.expressions.Variable
import engine.expressions.VariableList
import engine.expressions.VoidExpression
import engine.expressions.asInteger
import engine.expressions.bracketOf
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
import engine.operators.TrigonometricFunctionType
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.ConditionPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.FixedPattern
import engine.patterns.OptionalWrappingPattern
import engine.patterns.QuadraticPolynomialPattern
import engine.patterns.RationalPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.SolvablePattern
import engine.patterns.TrigonometricExpressionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.VariableExpressionPattern
import engine.patterns.absoluteValueOf
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.expressionWithFactor
import engine.patterns.fractionOf
import engine.patterns.inSolutionVariables
import engine.patterns.integerCondition
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.rationalMonomialPattern
import engine.patterns.squareRootOf
import engine.patterns.statementUnionOf
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.patterns.withOptionalConstantCoefficient
import engine.patterns.withOptionalRationalCoefficient
import engine.sign.Sign
import engine.steps.Transformation
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.metadata
import engine.utility.gcd
import engine.utility.isEven
import engine.utility.withMaxDP
import methods.angles.AnglesExplanation
import methods.rationalexpressions.computeLcdAndMultipliers
import methods.solvable.DenominatorExtractor.extractDenominator
import methods.solvable.DenominatorExtractor.extractFraction
import methods.solvable.extractSumTermsFromSolvable
import methods.solvable.findUnusedVariableLetter
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

    ApplyInverseSineFunctionToBothSides(applyInverseFunctionToBothSides),
    ExtractSolutionFromImpossibleSineLikeEquation(extractSolutionFromImpossibleSineLikeEquation),
    ExtractSolutionFromImpossibleQuadraticEquationWithTrigonometricExpressions(
        extractSolutionFromImpossibleQuadraticEquationWithTrigonometricExpressions,
    ),

    /**
     * After an expression with sine has been simplified:
     * x = c --> x = c + 2k * /pi/
     */
    AddPeriodicityOfSine(
        createAddPeriodicityRule(Explanation.AddPeriodicityOfSine, ::sinePeriodCreator),
    ),

    /**
     * After an expression with cosine has been simplified:
     * x = c --> x = c + 2k * /pi/
     */
    AddPeriodicityOfCosine(
        createAddPeriodicityRule(Explanation.AddPeriodicityOfCosine, ::sinePeriodCreator),
    ),

    /**
     * After an expression with tangent has been simplified:
     * x = c --> x = c + k * /pi/
     */
    AddPeriodicityOfTanLike(
        createAddPeriodicityRule(Explanation.AddPeriodicityOfTan) {
            productOf(it, Constants.Pi)
        },
    ),
    ExtractPeriodFromFraction(extractPeriodFromFraction),
    FlipSignOfPeriod(flipSignOfPeriod),
    ExtractSolutionFromEquationWithInverseSineOfZero(extractSolutionFromEquationWithInverseSineOfZero),
    ExtractSolutionFromEquationWithInverseCosineOfZero(extractSolutionFromEquationWithInverseCosineOfZero),
    ExtractSolutionWithoutPeriod(extractSolutionWithoutPeriod),
    ReorderSumWithPeriod(reorderSumWithPeriod),
    BalanceEquationWithTrigonometricExpressions(balanceEquationWithTrigonometricExpressions),
    SubstituteTrigFunctionInQuadraticEquation(substituteTrigFunctionInQuadraticEquation),
    ReorderQuadraticEquationWithTrigonometricFunctions(reorderQuadraticEquationWithTrigonometricFunctions),
    MergeTrigonometricEquationSolutions(mergeTrigonometricEquationSolutions),
    DivideByCos(divideByCos),
    ExtractSineOverCosine(extractSineOverCosine),
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
        val factors = get(product).children.filter {
            !it.isConstantIn(context.solutionVariables)
        }
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

/**
 * sin(x) = c --> arcsin(sin(x)) = arcsin(c)
 * sin(x) = sin(y) --> arcsin(sin(x)) = arcsin(sin(y))
 */

private val applyInverseFunctionToBothSides = rule {
    val leftValue = AnyPattern()
    val lhs = TrigonometricExpressionPattern(leftValue)
    val rightValue = AnyPattern()
    val rightTrig = TrigonometricExpressionPattern(rightValue)
    val rightRational = RationalPattern()
    val rightConstant = condition { it.isConstant() }
    val rhs = oneOf(rightTrig, rightRational, rightConstant)

    onEquation(lhs, rhs) {
        val functionType = getFunctionType(lhs)

        if (isBound(rightTrig)) {
            if (getFunctionType(rightTrig) != functionType) {
                return@onEquation null
            }
        } else if (isBound(rightRational)) {
            // Check if inverse function can be applied to constant (-1 <= c <= 1)
            val value = getValue(rightRational)
            if (
                value == null ||
                value.numerator.abs() > value.denominator.abs()
            ) {
                return@onEquation null
            }
        }

        val invFunctionType = functionType.getInv()

        ruleResult(
            toExpr = equationOf(
                wrapWithTrigonometricFunction(lhs, move(lhs), invFunctionType),
                wrapWithTrigonometricFunction(lhs, move(rhs), invFunctionType),
            ),
            explanation = metadata(methods.angles.Explanation.ApplyInverseSineFunctionToBothSides),
        )
    }
}

/**
 * sin(x) = c, c not in [-1, 1] --> /undefined/
 */
private val extractSolutionFromImpossibleSineLikeEquation = rule {
    val leftValue = AnyPattern()
    val lhs = TrigonometricExpressionPattern(
        leftValue,
        listOf(TrigonometricFunctionType.Sin, TrigonometricFunctionType.Cos),
    )
    val rightConstant = RationalPattern()

    onEquation(lhs, rightConstant) {
        val value = getValue(rightConstant)

        if (value == null || value.numerator.abs() <= value.denominator.abs()) {
            return@onEquation null
        }

        ruleResult(
            toExpr = contradictionOf(variableListOf(context.solutionVariables), expression),
            explanation = metadata(methods.angles.Explanation.ExtractSolutionFromImpossibleSineEquation),
        )
    }
}

/**
 * Check if the substituted equation solution in the equation system is a contradiction.
 */
private val extractSolutionFromImpossibleQuadraticEquationWithTrigonometricExpressions = rule {
    val contradiction = engine.patterns.contradictionOf(AnyPattern())
    val originalExpressionEquation = equationOf(
        ArbitraryVariablePattern(),
        TrigonometricExpressionPattern(AnyPattern()),
    )

    val equationUnion = engine.patterns.statementSystemOf(
        contradiction,
        originalExpressionEquation,
    )

    onPattern(equationUnion) {
        ruleResult(
            move(contradiction),
            metadata(Explanation.ExtractSolutionFromImpossibleQuadraticEquationWithTrigonometricExpressions),
        )
    }
}

private fun sinePeriodCreator(variable: Variable): Expression =
    productOf(
        Two,
        variable,
        Pi,
    )

/**
 * After an expression with trigonometric expressions has been simplified:
 * x = c --> x = c + <period of trigonometric expression>
 */
private fun createAddPeriodicityRule(
    explanation: MetadataKey,
    periodCreator: (variable: Variable) -> Expression,
): Rule =
    rule {
        val equation = equationOf(AnyPattern(), AnyPattern())
        val equationUnion = condition { it is StatementUnion }

        val pattern = oneOf(
            equation,
            equationUnion,
        )

        onPattern(pattern) {
            val variable = Variable(findUnusedVariableLetter(get(pattern)))

            val periodicAddend = periodCreator(variable)

            val equations = if (isBound(equation)) {
                val content = get(equation)
                equationOf(content.firstChild, sumOf(content.secondChild, periodicAddend))
            } else {
                val content = get(equationUnion)
                bracketOf(
                    statementUnionOf(
                        content.children.map {
                            if (it is Equation) {
                                equationOf(
                                    it.firstChild,
                                    sumOf(it.secondChild, periodicAddend),
                                )
                            } else {
                                return@onPattern null
                            }
                        },
                    ),
                )
            }

            val constraint = setSolutionOf(VariableList(listOf(variable)), Constants.Integers)

            ruleResult(
                toExpr = expressionWithConstraintOf(equations, introduce(constraint)),
                explanation = metadata(explanation),
            )
        }
    }

/**
 * [c1 + c2 * k * /pi/ / c3] -> [c1 / c3] + [c2 * k * /pi/ / c3]
 */
private val extractPeriodFromFraction = rule {
    val variable = condition(ArbitraryVariablePattern()) {
        it.isConstantIn(solutionVariables)
    }
    val coefficient = ConstantInSolutionVariablePattern()
    val period = oneOf(
        productOf(variable, FixedPattern(Pi)),
        productOf(coefficient, variable, FixedPattern(Pi)),
    )

    val optionalNegPeriod = optionalNegOf(period)

    val numerator = sumContaining(optionalNegPeriod)
    val denominator = AnyPattern()

    val fraction = fractionOf(numerator, denominator)

    onPattern(fraction) {
        ruleResult(
            toExpr = sumOf(
                fractionOf(
                    restOf(numerator),
                    distribute(denominator),
                ),
                fractionOf(
                    move(period),
                    distribute(denominator),
                ),
            ),
            explanation = metadata(Explanation.ExtractPeriodicityFromFraction),
        )
    }
}

/**
 * The sign of the period does not matter in case of equations with trigonometric functions, so we want to keep
 * it always positive
 */
private val flipSignOfPeriod = rule {
    val variable = condition(ArbitraryVariablePattern()) {
        it.isConstantIn(solutionVariables)
    }
    val coefficient = ConstantInSolutionVariablePattern()
    val period = oneOf(
        productOf(variable, FixedPattern(Pi)),
        productOf(coefficient, variable, FixedPattern(Pi)),
    )

    val negPeriod = engine.patterns.negOf(period)

    val periodFraction = fractionOf(period, AnyPattern())

    val negFraction = engine.patterns.negOf(periodFraction)

    val pattern = oneOf(
        negPeriod,
        negFraction,
    )

    onPattern(pattern) {
        val toExpr = when {
            isBound(negPeriod) -> period
            isBound(negFraction) -> periodFraction
            else -> return@onPattern null
        }

        ruleResult(
            toExpr = move(toExpr),
            explanation = metadata(Explanation.FlipSignOfPeriodicity),
        )
    }
}

/**
 * x = arcsin(0) --> x = k * /pi/
 */
private val extractSolutionFromEquationWithInverseSineOfZero = rule {
    val lhs = AnyPattern()
    val rhs = TrigonometricExpressionPattern(
        FixedPattern(Constants.Zero),
        listOf(TrigonometricFunctionType.Arcsin),
    )

    val equation = equationOf(lhs, rhs)

    onPattern(equation) {
        val variable = Variable(findUnusedVariableLetter(get(equation)))

        ruleResult(
            toExpr = expressionWithConstraintOf(
                equationOf(
                    get(lhs),
                    transform(rhs, productOf(variable, Pi)),
                ),
                setSolutionOf(VariableList(listOf(variable)), Constants.Integers),
            ),
            explanation = metadata(Explanation.ExtractSolutionFromEquationWithInverseSineOfZero),
        )
    }
}

/**
 * x = arccos(0) --> x = [/pi/ / 2] + k * /pi/
 */
private val extractSolutionFromEquationWithInverseCosineOfZero = rule {
    val lhs = AnyPattern()
    val rhs = TrigonometricExpressionPattern(
        FixedPattern(Constants.Zero),
        listOf(TrigonometricFunctionType.Arccos),
    )

    val equation = equationOf(lhs, rhs)

    onPattern(equation) {
        val variable = Variable(findUnusedVariableLetter(get(equation)))

        ruleResult(
            toExpr = expressionWithConstraintOf(
                equationOf(
                    get(lhs),
                    sumOf(fractionOf(Pi, Two), productOf(variable, Pi)),
                ),
                setSolutionOf(VariableList(listOf(variable)), Constants.Integers),
            ),
            explanation = metadata(Explanation.ExtractSolutionFromEquationWithInverseCosineOfZero),
        )
    }
}

/**
 * Used for checking if the solution of a trigonometric equation is a contradiction / identity (In these cases the
 * periodicity can be ignored)
 * e.g lhs = rhs + 2k * /pi/ --> lhs = rhs
 */
private val extractSolutionWithoutPeriod = rule {
    val variable = condition(ArbitraryVariablePattern()) {
        it.isConstantIn(solutionVariables)
    }
    val coefficient = ConstantInSolutionVariablePattern()
    val period = optionalNegOf(
        oneOf(
            productOf(variable, FixedPattern(Pi)),
            productOf(coefficient, variable, FixedPattern(Pi)),
        ),
    )

    val periodFraction = OptionalWrappingPattern(period) {
        optionalNegOf(
            fractionOf(it, AnyPattern()),
        )
    }

    val lhs = ConstantInSolutionVariablePattern()
    val rhsSum = sumContaining(periodFraction)

    onEquation(lhs, oneOf(periodFraction, rhsSum)) {
        val toExpr = when {
            isBound(rhsSum) -> restOf(rhsSum)
            else -> transform(periodFraction, Constants.Zero)
        }

        ruleResult(
            equationOf(get(lhs), toExpr),
            explanation = metadata(Explanation.ExtractSolutionWithoutPeriod),
            tags = listOf(Transformation.Tag.Pedantic),
        )
    }
}

/**
 * Used for getting a nice order in the result, with the
 * e.g c1 + 2k * /pi/ + c2 --> c1 + c2 + 2k * /pi/
 */
private val reorderSumWithPeriod = rule {
    val variable = condition(ArbitraryVariablePattern()) {
        it.isConstantIn(solutionVariables)
    }
    val coefficient = ConstantInSolutionVariablePattern()
    val period = optionalNegOf(
        oneOf(
            productOf(variable, FixedPattern(Pi)),
            productOf(coefficient, variable, FixedPattern(Pi)),
        ),
    )

    val periodFraction = OptionalWrappingPattern(period) {
        optionalNegOf(
            fractionOf(it, AnyPattern()),
        )
    }

    val sum = sumContaining(periodFraction)

    onPattern(sum) {
        val sum = get(sum)
        val period = move(periodFraction)

        if (sum.children.last() == period) {
            return@onPattern null
        }

        val operands = sum.children.filter { it != period } + period
        val toExpr = sumOf(operands)

        ruleResult(
            toExpr,
            explanation = metadata(Explanation.ReorderSumWithPeriod),
            tags = listOf(Transformation.Tag.Rearrangement),
        )
    }
}

/**
 * sin(a) ± sin(b) = 0 --> sin(a) = ± sin(b)
 */
private val balanceEquationWithTrigonometricExpressions = rule {
    val argument = AnyPattern()
    val argument2 = AnyPattern()
    val trigFunction1 = TrigonometricExpressionPattern(argument)
    val trigFunction2 = TrigonometricExpressionPattern(argument2)
    val term1 = optionalNegOf(trigFunction1)
    val term2 = optionalNegOf(trigFunction2)
    val lhs = sumOf(term1, term2)
    val rhs = FixedPattern(Constants.Zero)

    val solvableLeft = SolvablePattern(lhs, rhs)
    val solvableRight = SolvablePattern(rhs, lhs)

    onPattern(oneOf(solvableLeft, solvableRight)) {
        if (getFunctionType(trigFunction1) != getFunctionType(trigFunction2)) {
            return@onPattern null
        }

        val (lhs, rhs) = when {
            term1.isNeg() -> Pair(move(term2), copyFlippedSign(term1, move(trigFunction1)))
            else -> Pair(move(term1), copyFlippedSign(term2, move(trigFunction2)))
        }

        val toExpr = when {
            isBound(solvableLeft) -> solvableLeft.deriveSolvable(
                lhs,
                rhs,
            )
            isBound(solvableRight) -> solvableRight.deriveSolvable(
                rhs,
                lhs,
            )
            else -> null
        } ?: return@onPattern null

        ruleResult(
            toExpr = toExpr,
            explanation = metadata(AnglesExplanation.BalanceEquationWithTrigonometricExpressions),
        )
    }
}

/**
 * Substitute the trigonometric function with a variable (t if possible) and return it along with the substitution in
 * a statement union
 *
 * a * f^2(x) + b * f(x) + c = 0
 * -->
 * ┌ at + bt + c = 0
 * │
 * └ t = f(x)
 */
private val substituteTrigFunctionInQuadraticEquation = rule {
    val trigFunction = TrigonometricExpressionPattern(AnyPattern())

    val quadraticPolynomial = QuadraticPolynomialPattern(
        variable = trigFunction,
        constantChecker = solutionVariableConstantChecker,
    )
    val rhs = FixedPattern(Constants.Zero)

    onEquation(quadraticPolynomial, rhs) {
        val a = get(quadraticPolynomial::quadraticCoefficient)!!
        val b = get(quadraticPolynomial::linearCoefficient)!!
        val c = get(quadraticPolynomial::constantTerm)!!

        val substitutedVariable = Variable(
            findUnusedVariableLetter(
                get(quadraticPolynomial),
                listOf('t'),
            ),
        )

        fun Expression.withCoefficient(coefficient: Expression): Expression =
            when (coefficient) {
                Constants.One -> this
                is Minus -> Minus(
                    this.withCoefficient(coefficient.firstChild),
                )
                else -> productOf(
                    coefficient,
                    this,
                )
            }

        val terms = buildList {
            add(
                powerOf(
                    substitutedVariable,
                    Two,
                ).withCoefficient(a),
            )
            if (b != Constants.Zero) {
                add(
                    substitutedVariable.withCoefficient(b),
                )
            }
            if (c != Constants.Zero) {
                add(c)
            }
        }

        val newEquation = equationOf(
            sumOf(terms),
            get(rhs),
        )

        ruleResult(
            toExpr = statementSystemOf(
                newEquation,
                equationOf(
                    substitutedVariable,
                    get(trigFunction),
                ),
            ),
            explanation = metadata(
                key = Explanation.SubstituteTrigonometricFunctionInQuadraticEquation,
                parameters = listOf(
                    equationOf(
                        get(trigFunction),
                        substitutedVariable,
                    ),
                ),
            ),
        )
    }
}

/**
 * Reorder the quadratic equation with trigonometric base so that the powers are in descending order.
 */
private val reorderQuadraticEquationWithTrigonometricFunctions = rule {
    val trigFunction = TrigonometricExpressionPattern(AnyPattern())

    val quadraticPolynomial = QuadraticPolynomialPattern(
        variable = trigFunction,
        constantChecker = solutionVariableConstantChecker,
    )
    val rhs = FixedPattern(Constants.Zero)

    onEquation(quadraticPolynomial, rhs) {
        val a = get(quadraticPolynomial::quadraticTerm)
        val b = get(quadraticPolynomial::linearTerm)
        val c = get(quadraticPolynomial::constantTerm)

        val terms = listOf(a, b, c).filter { it != null && it != Constants.Zero }.map { it!! }

        val lhs = sumOf(terms)

        // Prevent adding redundant steps
        if (lhs == get(quadraticPolynomial)) {
            return@onEquation null
        }

        val newEquation = equationOf(
            lhs,
            get(rhs),
        )

        ruleResult(
            toExpr = newEquation,
            explanation = metadata(Explanation.ReorganizeQuadraticPolynomialWithTrigonometricFunctions),
        )
    }
}

/**
 * Merge two trigonometric equations solutions, when they are at two opposing points of the unit circle and have the
 * same period.
 */
private val mergeTrigonometricEquationSolutions = rule {
    val variable = condition(ArbitraryVariablePattern()) {
        it.isConstantIn(solutionVariables)
    }
    val coefficient = ConstantInSolutionVariablePattern()
    val piPattern = FixedPattern(Pi)
    val period = optionalNegOf(
        oneOf(
            productOf(variable, piPattern),
            productOf(coefficient, variable, piPattern),
        ),
    )

    val periodFraction = OptionalWrappingPattern(period) {
        fractionOf(it, AnyPattern())
    }

    val expression1 = withOptionalRationalCoefficient(
        piPattern,
    )

    val expression2 = withOptionalRationalCoefficient(
        piPattern,
    )

    val lhs = SolutionVariablePattern()

    val eq1 = equationOf(
        lhs,
        sumOf(expression1, periodFraction),
    )
    val eq2 = equationOf(
        lhs,
        sumOf(expression2, periodFraction),
    )

    val pattern = statementUnionOf(
        eq1,
        eq2,
    )

    onPattern(pattern) {
        val coefficient1 = getCoefficientValue(expression1)
        val coefficient2 = getCoefficientValue(expression2)

        val periodNumerator = if (isBound(coefficient)) {
            get(coefficient).asInteger()
        } else {
            BigInteger.ONE
        }
        val periodDenominator = if (periodFraction.isWrapping()) {
            get(periodFraction).secondChild.asInteger()
        } else {
            BigInteger.ONE
        }

        // Come on, it's just a null check!
        @Suppress("ComplexCondition")
        if (coefficient1 == null || coefficient2 == null || periodNumerator == null || periodDenominator == null) {
            return@onPattern null
        }

        // We "calculate" the point that is opposing to the first solution
        // We work with just the coefficient, as multiplying all expressions by Pi is redundant
        val c1AndHalfPeriodNumerator = periodNumerator * coefficient1.denominator +
            coefficient1.numerator * periodDenominator * BigInteger.TWO
        val c1AndHalfPeriodDenominator = periodDenominator * coefficient1.denominator * BigInteger.TWO

        val gcdC1AndHalfPeriod = gcd(c1AndHalfPeriodDenominator, c1AndHalfPeriodNumerator)

        val numeratorToVerify = c1AndHalfPeriodNumerator / gcdC1AndHalfPeriod
        val denominatorToVerify = c1AndHalfPeriodDenominator / gcdC1AndHalfPeriod

        // We check if the second solution is the same as the opposing point we calulated
        if (numeratorToVerify != coefficient2.numerator || denominatorToVerify != coefficient2.denominator) {
            return@onPattern null
        }

        // We calculate the new period by halving the original period
        val newPeriodNumeratorTerms = buildList {
            if (isBound(coefficient)) add(get(coefficient))
            add(get(variable))
            add(get(piPattern))
        }

        val newPeriodDenominator = if (periodFraction.isWrapping()) {
            productOf(Two, get(periodFraction).secondChild)
        } else {
            Two
        }

        // Take the smaller positive expression as base. If both are negative take the one closer to 0
        val smallerExpr = get(expression1)
        val greaterExpr = get(expression2)

        val newExpression = when (smallerExpr) {
            is Minus -> greaterExpr
            else -> smallerExpr
        }

        ruleResult(
            toExpr = equationOf(
                get(lhs),
                sumOf(
                    newExpression,
                    fractionOf(
                        productOf(newPeriodNumeratorTerms),
                        newPeriodDenominator,
                    ),
                ),
            ),
            explanation = metadata(Explanation.MergeTrigonometricEquationSolutions),
        )
    }
}

// Find the term with cosine in a sum and divide each term by it
private val divideByCos = rule {
    val cosine = TrigonometricExpressionPattern.cos(AnyPattern())

    val pattern = sumContaining(withOptionalConstantCoefficient(cosine))

    onPattern(pattern) {
        val children = get(pattern).children
        val divisor = distribute(cosine)

        val dividedChildren = children.map {
            when (it) {
                is Fraction -> fractionOf(
                    it.firstChild,
                    productOf(divisor, it.secondChild),
                )
                else -> fractionOf(it, divisor)
            }
        }

        ruleResult(
            toExpr = sumOf(dividedChildren),
            explanation = metadata(
                Explanation.DivideByTrigFunction,
                divisor,
            ),
        )
    }
}

// Extract [ sin[ a ] / cos[ a ] ] from a fraction with other terms in numerator and denominator
private val extractSineOverCosine = rule {
    val argument = AnyPattern()

    val sine = TrigonometricExpressionPattern.sin(argument)
    val cosine = TrigonometricExpressionPattern.cos(argument)

    val numeratorProduct = productContaining(sine)
    val denominatorProduct = productContaining(cosine)

    val pattern = fractionOf(
        oneOf(numeratorProduct, sine),
        oneOf(denominatorProduct, cosine),
    )

    onPattern(pattern) {
        if (!isBound(numeratorProduct) && !isBound(denominatorProduct)) {
            return@onPattern null
        }
        val restNumerator = if (isBound(numeratorProduct)) {
            restOf(numeratorProduct)
        } else {
            Constants.One
        }

        val restDenominator = if (isBound(denominatorProduct)) {
            restOf(denominatorProduct)
        } else {
            Constants.One
        }

        ruleResult(
            toExpr = productOf(
                fractionOf(
                    restNumerator,
                    restDenominator,
                ),
                fractionOf(
                    get(sine),
                    get(cosine),
                ),
            ),
            explanation = metadata(Explanation.ExtractSineOverCosine),
        )
    }
}
