package methods.equations

import engine.context.Context
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.Minus
import engine.expressions.StatementUnion
import engine.expressions.Sum
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.BinaryIntegerCondition
import engine.patterns.ConditionPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.VariableExpressionPattern
import engine.patterns.condition
import engine.patterns.contradictionOf
import engine.patterns.equationOf
import engine.patterns.fractionOf
import engine.patterns.identityOf
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.setSolutionOf
import engine.patterns.solutionSetOf
import engine.patterns.sumContaining
import engine.patterns.variableListOf
import engine.patterns.withOptionalConstantCoefficient
import engine.patterns.withOptionalIntegerCoefficient
import engine.steps.Transformation
import engine.steps.metadata.metadata
import methods.constantexpressions.simpleTidyUpSteps
import methods.factor.FactorPlans
import methods.factor.FactorRules
import methods.general.NormalizationPlans
import methods.polynomials.PolynomialsPlans
import methods.polynomials.algebraicSimplificationSteps
import methods.solvable.ApplySolvableRuleAndSimplify
import methods.solvable.DenominatorExtractor.extractDenominator
import methods.solvable.SolvableKey
import methods.solvable.SolvableRules
import methods.solvable.computeOverallUnionSolution
import methods.solvable.extractSumTermsFromSolvable
import methods.solvable.fractionRequiringMultiplication

enum class EquationsPlans(override val runner: CompositeMethod) : RunnerMethod {

    MoveConstantsToTheLeftAndSimplify(
        applySolvableRuleAndSimplify(SolvableKey.MoveConstantsToTheLeft, SolvableRules.MoveConstantsToTheLeft),
    ),

    MoveConstantsToTheRightAndSimplify(
        applySolvableRuleAndSimplify(SolvableKey.MoveConstantsToTheRight, SolvableRules.MoveConstantsToTheRight),
    ),

    MoveVariablesToTheLeftAndSimplify(
        applySolvableRuleAndSimplify(SolvableKey.MoveVariablesToTheLeft, SolvableRules.MoveVariablesToTheLeft),
    ),

    MoveVariablesToTheRightAndSimplify(
        applySolvableRuleAndSimplify(SolvableKey.MoveVariablesToTheRight, SolvableRules.MoveVariablesToTheRight),
    ),

    CollectLikeTermsToTheLeftAndSimplify(
        plan {
            explanation = Explanation.CollectLikeTermsToTheLeftAndSimplify

            steps {
                apply(EquationsRules.CollectLikeTermsToTheLeft)
                optionally(simplifyEquation)
            }
        },
    ),

    MoveEverythingToTheLeftAndSimplify(
        plan {
            explanation = Explanation.MoveEverythingToTheLeftAndSimplify

            steps {
                apply(EquationsRules.MoveEverythingToTheLeft)
                optionally(simplifyEquation)
            }
        },
    ),

    SimplifyByFactoringNegativeSignOfLeadingCoefficient(
        plan {
            explanation = Explanation.SimplifyByFactoringNegativeSignOfLeadingCoefficient

            steps {
                check { it.secondChild == Constants.Zero }
                applyTo(FactorRules.FactorNegativeSignOfLeadingCoefficient) { it.firstChild }
                apply(EquationsRules.NegateBothSides)
            }
        },
    ),

    SimplifyByDividingByGcfOfCoefficients(
        plan {
            explanation = Explanation.SimplifyByDividingByGcfOfCoefficients

            steps {
                applyTo(FactorPlans.FactorGreatestCommonIntegerFactor) { it.firstChild }
                apply(EquationsRules.EliminateConstantFactorOfLhsWithZeroRhs)
            }
        },
    ),

    MultiplyByInverseCoefficientOfVariableAndSimplify(
        plan {
            explanation = Explanation.MultiplyByInverseCoefficientOfVariableAndSimplify

            steps {
                apply(EquationsRules.MultiplyByInverseCoefficientOfVariable)
                optionally(simplifyEquation)
            }
        },
    ),

    /**
     * rewrite a simplified quadratic equation to [(x + a)^2] = b form
     */
    RewriteToXPLusASquareEqualsBForm(
        plan {
            explanation = Explanation.RewriteToXPLusASquareEqualsBForm

            steps {
                apply(CompleteTheSquareAndSimplify)
                applyTo(FactorPlans.FactorSquareOfBinomial) { it.firstChild }
            }
        },
    ),

    MultiplyByLCDAndSimplify(
        plan {
            explanation = methods.solvable.EquationsExplanation.MultiplyByLCDAndSimplify

            steps {
                apply(SolvableRules.MultiplySolvableByLCD)
                whilePossible(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    DivideByCoefficientOfVariableAndSimplify(
        plan {
            val lhs = withOptionalConstantCoefficient(VariableExpressionPattern())
            val rhs = ConstantInSolutionVariablePattern()
            pattern = equationOf(lhs, rhs)

            explanation = Explanation.DivideByCoefficientOfVariableAndSimplify
            explanationParameters { listOf(get(lhs::coefficient)!!) }

            steps {
                apply(EquationsRules.DivideByCoefficientOfVariable)
                optionally(simplifyEquation)
            }
        },
    ),

    CompleteTheSquareAndSimplify(
        plan {
            explanation = Explanation.CompleteTheSquareAndSimplify

            steps {
                apply(EquationsRules.CompleteTheSquare)
                optionally(PolynomialsPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        },
    ),

    MultiplyByInverseOfLeadingCoefficientAndSimplify(
        plan {
            explanation = Explanation.MultiplyByInverseOfLeadingCoefficientAndSimplify

            steps {
                apply(EquationsRules.MultiplyByInverseOfLeadingCoefficient)
                apply(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    IsolateAbsoluteValue(
        plan {
            explanation = Explanation.IsolateAbsoluteValue

            steps {
                firstOf {
                    option(SolvableRules.MoveTermsNotContainingModulusToTheRight)
                    option(SolvableRules.MoveTermsNotContainingModulusToTheLeft)
                }
                apply(simplifyEquation)
            }
        },
    ),

    MoveOneModulusToOtherSideAndSimplify(
        plan {
            explanation = Explanation.MoveOneModulusToOtherSideAndSimplify
            steps {
                firstOf {
                    option(EquationsRules.MoveSecondModulusToRhs)
                    option(EquationsRules.MoveSecondModulusToLhs)
                }
                optionally(equationSimplificationSteps)
            }
        },
    ),

    SolveEquationUnion(
        taskSet {
            val equationUnion = condition { it is StatementUnion }
            pattern = equationUnion
            explanation = Explanation.SolveEquationUnion

            tasks {
                // Create a task for each to simplify it
                val splitTasks = get(equationUnion).children.map {
                    task(
                        startExpr = it,
                        explanation = metadata(Explanation.SolveEquationInEquationUnion),
                        stepsProducer = optimalEquationSolvingSteps,
                    ) ?: return@tasks null
                }

                // Else combine the solutions together
                val overallSolution = computeOverallUnionSolution(splitTasks.map { it.result }) ?: return@tasks null
                task(
                    startExpr = overallSolution,
                    explanation = metadata(Explanation.CollectSolutions),
                )
                allTasks()
            }
        },
    ),

    SolveEquationWithOneAbsoluteValueBySubstitution(solveEquationWithOneAbsoluteValueBySubstitution),

    @PublicMethod
    SolveDecimalLinearEquation(
        plan {
            explanation = Explanation.SolveDecimalLinearEquation

            val acceptedSolutions = oneOf(
                SignedNumberPattern(),
                optionalNegOf(RecurringDecimalPattern()),
                optionalNegOf(fractionOf(UnsignedNumberPattern(), UnsignedNumberPattern())),
            )

            resultPattern = oneOf(
                setSolutionOf(variableListOf(SolutionVariablePattern()), solutionSetOf(acceptedSolutions)),
                contradictionOf(variableListOf(SolutionVariablePattern())),
                identityOf(variableListOf(SolutionVariablePattern())),
            )

            steps {
                inContext(contextFactory = { copy(preferDecimals = true) }) {
                    apply(rearrangeLinearEquationSteps)
                    optionally(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
                }
            }
        },
    ),

    @PublicMethod
    SolveEquationInOneVariable(
        object : CompositeMethod() {
            override fun run(ctx: Context, sub: Expression): Transformation? {
                if (sub.variables.count() != 1) return null
                return solveEquationInOneVariable.value.run(ctx, sub)
            }
        },
    ),

    @PublicMethod
    SolveEquationWithConstraint(solveEquationWithConstraint),
}

val simplifyEquation = plan {
    explanation = Explanation.SimplifyEquation

    steps {
        whilePossible { deeply(simpleTidyUpSteps) }
        optionally(NormalizationPlans.NormalizeExpression)
        whilePossible(EquationsRules.EliminateConstantFactorOfLhsWithZeroRhs)
        whilePossible(SolvableRules.CancelCommonTermsOnBothSides)
        whilePossible(algebraicSimplificationSteps)
    }
}

private val applySolvableRuleAndSimplify = ApplySolvableRuleAndSimplify(simplifyEquation)::getPlan

val equationSimplificationSteps = steps {
    whilePossible {
        firstOf {
            option(NormalizationPlans.NormalizeExpression)
            // check for contradiction or identity
            option(EquationsRules.ExtractSolutionFromConstantEquation)
            // normalize the equation
            option(simplifyEquation)
        }
    }
}

val equationRearrangementSteps = steps {
    // three ways to reorganize the equation into aX = b form
    firstOf {
        option {
            // if the equation is in the form `a = bX + c` with `b` non-negative, then
            // we move `c` to the left hand side and flip the equation
            checkForm {
                val lhs = ConstantInSolutionVariablePattern()
                val nonConstantTerm = condition(VariableExpressionPattern()) { it !is Minus && it !is Sum }
                val rhs = oneOf(
                    nonConstantTerm,
                    sumContaining(nonConstantTerm) { rest -> rest.isConstantIn(solutionVariables) },
                )
                equationOf(lhs, rhs)
            }
            optionally(EquationsPlans.MoveConstantsToTheLeftAndSimplify)
            apply(EquationsRules.FlipEquation)
        }
        option {
            // if the equation is in the form `aX + b = cx + d` with an integer and `c` a
            // positive integer such that `c > a`, we move `aX` to the right hand side, `d` to
            // the left hand side and flip the equation
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
                    equationOf(lhs, rhs),
                    BinaryIntegerCondition(
                        lhsVariable.integerCoefficient,
                        rhsVariable.integerCoefficient,
                    ) { n1, n2 -> n2 > n1 },
                )
            }

            apply(EquationsPlans.MoveVariablesToTheRightAndSimplify)
            optionally(EquationsPlans.MoveConstantsToTheLeftAndSimplify)
            apply(EquationsRules.FlipEquation)
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

                equationOf(lhs, rhs)
            }
            optionally(EquationsPlans.MoveVariablesToTheLeftAndSimplify)
            optionally(EquationsPlans.MoveConstantsToTheRightAndSimplify)
        }
    }
}

private val optimalEquationSolvingSteps = steps {
    firstOf {
        option {
            optionally(simplifyEquation)
            firstOf {
                option(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
                option(EquationsRules.ExtractSolutionFromConstantEquation)
            }
        }
        option(EquationsPlans.SolveEquationInOneVariable)
        option(EquationsPlans.SolveEquationWithConstraint)
    }
}

/**
 * multiply by the LCM of the constant denominators if there are at least two fractions
 * or a single fraction with a non-constant numerator (including also 1/2 * (x + 1))
 */
val removeConstantDenominatorsSteps = steps {
    check {
        val sumTerms = extractSumTermsFromSolvable(it)
        val denominators = sumTerms.mapNotNull { term -> extractDenominator(term) }
        denominators.size >= 2 || sumTerms.any { term ->
            fractionRequiringMultiplication.matches(this, term)
        }
    }
    apply(EquationsPlans.MultiplyByLCDAndSimplify)
}

val coefficientRemovalSteps = steps {
    firstOf {
        // get rid of the coefficient of the variable
        option(EquationsPlans.MultiplyByInverseCoefficientOfVariableAndSimplify)
        option(EquationsPlans.DivideByCoefficientOfVariableAndSimplify)
        option {
            checkForm {
                equationOf(negOf(VariableExpressionPattern()), ConstantInSolutionVariablePattern())
            }
            apply(EquationsRules.NegateBothSides)
        }
    }
}

val rearrangeLinearEquationSteps = steps {
    whilePossible {
        firstOf {
            option(equationSimplificationSteps)
            option(removeConstantDenominatorsSteps)
            option(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
        }
    }

    optionally(equationRearrangementSteps)
    optionally(coefficientRemovalSteps)
}
