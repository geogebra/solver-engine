package methods.inequalities

import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.patterns.AnyPattern
import engine.patterns.FindPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.inequalityOf
import engine.patterns.oneOf
import engine.patterns.productContaining
import engine.patterns.solutionOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import methods.polynomials.PolynomialPlans
import methods.solvable.SolvableRules

enum class InequalitiesPlans(override val runner: CompositeMethod) : RunnerMethod {

    MoveConstantsToTheLeftAndSimplify(
        plan {
            explanation = Explanation.MoveConstantsToTheLeftAndSimplify

            steps {
                apply(SolvableRules.MoveConstantsToTheLeft)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        }
    ),

    MoveConstantsToTheRightAndSimplify(
        plan {
            explanation = Explanation.MoveConstantsToTheRightAndSimplify

            steps {
                apply(SolvableRules.MoveConstantsToTheRight)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        }
    ),

    MoveVariablesToTheLeftAndSimplify(
        plan {
            explanation = Explanation.MoveVariablesToTheLeftAndSimplify

            steps {
                apply(SolvableRules.MoveVariablesToTheLeft)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        }
    ),

    MultiplyByInverseCoefficientOfVariableAndSimplify(
        plan {
            explanation = Explanation.MultiplyByInverseCoefficientOfVariableAndSimplify

            steps {
                apply(InequalitiesRules.MultiplyByInverseCoefficientOfVariable)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        }
    ),

    MultiplyByLCDAndSimplify(
        plan {
            explanation = Explanation.MultiplyByLCDAndSimplify

            steps {
                apply(SolvableRules.MultiplySolvableByLCD)
                whilePossible(PolynomialPlans.ExpandPolynomialExpressionInOneVariable)
            }
        }
    ),

    DivideByCoefficientOfVariableAndSimplify(
        plan {
            explanation = Explanation.DivideByCoefficientOfVariableAndSimplify

            steps {
                apply(InequalitiesRules.DivideByCoefficientOfVariable)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        }
    ),

    @PublicMethod
    SolveLinearInequality(
        plan {
            explanation = Explanation.SolveLinearInequality
            pattern = inequalityOf(AnyPattern(), AnyPattern())
            resultPattern = solutionOf(SolutionVariablePattern(), AnyPattern())

            steps {
                whilePossible {
                    firstOf {
                        // check if the inequality is in one of the possible solved forms
                        option(InequalitiesRules.ExtractSolutionFromConstantInequality)
                        option(InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign)
                        option(InequalitiesRules.ExtractSolutionFromInequalityInSolvedForm)

                        // normalize the inequality
                        option(SolvableRules.CancelCommonTermsOnBothSides)
                        option(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)

                        option {
                            // multiply through with the LCD if the equation contains one fraction with a sum numerator
                            // or a fraction multiplied by a sum
                            checkForm {
                                val nonConstantSum = condition(sumContaining()) { !it.isConstant() }
                                oneOf(
                                    FindPattern(fractionOf(nonConstantSum, UnsignedIntegerPattern())),
                                    FindPattern(
                                        productContaining(
                                            fractionOf(AnyPattern(), UnsignedIntegerPattern()),
                                            nonConstantSum
                                        )
                                    )
                                )
                            }
                            apply(MultiplyByLCDAndSimplify)
                        }

                        option(PolynomialPlans.ExpandPolynomialExpressionInOneVariable)

                        // two ways to reorganize the equation into ax = b form
                        option {
                            // if the equation is in the form `a = bx + c` with `b` non-negative, then
                            // we move `c` to the left hand side and flip the equation
                            checkForm {
                                val lhs = condition(AnyPattern()) { it.isConstant() }
                                val variableWithCoefficient = withOptionalConstantCoefficient(
                                    SolutionVariablePattern(),
                                    positiveOnly = true
                                )
                                val rhs = oneOf(variableWithCoefficient, sumContaining(variableWithCoefficient))
                                inequalityOf(lhs, rhs)
                            }
                            optionally(MoveConstantsToTheLeftAndSimplify)
                            apply(InequalitiesRules.FlipInequality)
                        }
                        option {
                            // otherwise we first move variables to the left and then constants
                            // to the right
                            optionally(MoveVariablesToTheLeftAndSimplify)
                            optionally(MoveConstantsToTheRightAndSimplify)
                        }

                        // get rid of the coefficient of the variable
                        option(InequalitiesRules.NegateBothSides)
                        option(MultiplyByInverseCoefficientOfVariableAndSimplify)
                        option(DivideByCoefficientOfVariableAndSimplify)
                    }
                }
            }
        }
    )
}
