package methods.equations

import engine.methods.Plan
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.operators.SolutionOperator
import engine.patterns.AnyPattern
import engine.patterns.FindPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.fractionOf
import engine.patterns.oneOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import methods.polynomials.PolynomialPlans

enum class EquationsPlans(override val runner: Plan) : RunnerMethod {

    MoveConstantsToTheLeftAndSimplify(
        plan {
            explanation = Explanation.MoveConstantsToTheLeftAndSimplify

            steps {
                apply(EquationsRules.MoveConstantsToTheLeft)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        }
    ),

    MoveConstantsToTheRightAndSimplify(
        plan {
            explanation = Explanation.MoveConstantsToTheRightAndSimplify

            steps {
                apply(EquationsRules.MoveConstantsToTheRight)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        }
    ),

    MoveVariablesToTheLeftAndSimplify(
        plan {
            explanation = Explanation.MoveVariablesToTheLeftAndSimplify

            steps {
                apply(EquationsRules.MoveVariablesToTheLeft)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        }
    ),

    MultiplyByInverseCoefficientOfVariableAndSimplify(
        plan {
            explanation = Explanation.MultiplyByInverseCoefficientOfVariableAndSimplify

            steps {
                apply(EquationsRules.MultiplyByInverseCoefficientOfVariable)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        }
    ),

    MultiplyByLCDAndSimplify(
        plan {
            explanation = Explanation.MultiplyByLCDAndSimplify

            steps {
                apply(EquationsRules.MultiplyEquationByLCD)
                whilePossible(PolynomialPlans.ExpandPolynomialExpressionInOneVariable)
            }
        }
    ),

    DivideByCoefficientOfVariableAndSimplify(
        plan {
            explanation = Explanation.DivideByCoefficientOfVariableAndSimplify

            steps {
                apply(EquationsRules.DivideByCoefficientOfVariable)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        }
    ),

    @PublicMethod
    SolveLinearEquation(
        plan {
            explanation = Explanation.SolveLinearEquation
            resultPattern = condition(AnyPattern()) { it.operator == SolutionOperator }

            steps {
                whilePossible {
                    firstOf {
                        // check if the equation is in one of the possible solved forms
                        option(EquationsRules.ExtractSolutionFromIdentity)
                        option(EquationsRules.ExtractSolutionFromEquationInSolvedForm)

                        // normalize the equation
                        option(EquationsRules.CancelCommonTermsOnBothSides)
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

                        // we can only deduce that the sides are unequal if we have normalized the equation already
                        option(EquationsRules.ExtractSolutionFromContradiction)

                        // two ways to reorganize the equation into ax = b form
                        option {
                            // if the equation is in the form `a = bx + c` with `b` non-negative, then
                            // we move `c` to the left hand side and flip the equation
                            checkForm {
                                val lhs = condition(AnyPattern()) { it.isConstant() }
                                val variableWithCoefficient = withOptionalConstantCoefficient(
                                    SolutionVariablePattern(), positiveOnly = true
                                )
                                val rhs = oneOf(variableWithCoefficient, sumContaining(variableWithCoefficient))
                                equationOf(lhs, rhs)
                            }
                            optionally(MoveConstantsToTheLeftAndSimplify)
                            apply(EquationsRules.FlipEquation)
                        }
                        option {
                            // otherwise we first move variables to the left and then constants
                            // to the right
                            optionally(MoveVariablesToTheLeftAndSimplify)
                            optionally(MoveConstantsToTheRightAndSimplify)
                        }

                        // get rid of the coefficient of the variable
                        option(EquationsRules.NegateBothSides)
                        option(MultiplyByInverseCoefficientOfVariableAndSimplify)
                        option(DivideByCoefficientOfVariableAndSimplify)
                    }
                }
            }
        }
    )
}
