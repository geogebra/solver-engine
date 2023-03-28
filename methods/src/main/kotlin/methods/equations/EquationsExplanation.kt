package methods.equations

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class EquationsExplanation : CategorisedMetadataKey {

    /**
     * Simplify an equation so that
     * - opposite terms on the same side are cancelled first
     * - equal terms on both sides are cancelled next
     * - each side is simplified last
     *
     * E.g. x + 3 - 3 = 5x - 3 --> x = 5x - 3
     *      x - 4 = 2x - 4 --> x = 2x
     *      x + 2x = 5 + 1 --> 3x = 6
     */
    SimplifyEquation,

    /**
     * Negate both sides of the equation, i.e. turn an equation of
     * the form -x = a to x = -a.
     *
     * E.g. -x = -2 sqrt[3] -> x = 2 sqrt[3]
     */
    NegateBothSides,

    /**
     * Multiply both sides of the equation by the inverse of the coefficient
     * of the variable.
     *
     * E.g. [x / 9] = 3 -> [x / 9] * 9 = 3 * 9
     * [2x / 5] = 3 -> [2x / 5] * [5 / 2] = 3 * [5 / 2]
     */
    MultiplyByInverseCoefficientOfVariable,

    /**
     * Divide both sides of the equation by the coefficient of the
     * variable.
     *
     * E.g. 2 sqrt[2] x = 3 -> [2 sqrt[2] x / 2 sqrt[2]] = [3 / 2 sqrt[2]]
     */
    DivideByCoefficientOfVariable,

    /**
     * Multiply both sides of an equation by the inverse of the leading coefficient
     * on the LHS.  When simplified this will lead to a monic polynomial on the left.
     *
     * E.g. 2[x ^ 2] + 1 = 7 --> (2[x ^ 2] + 1)*[1 / 2] = 7 * [1 / 2]
     */
    MultiplyByInverseOfLeadingCoefficient,

    /**
     * Flip the equation.
     *
     * E.g. 7 = 3x -> 3x = 7
     */
    FlipEquation,

    /**
     * Take the square root of both sides of an equation of the form
     * x^n = non-zero constant.
     *
     * E.g. x^2 = 9 -> x = +/-sqrt[9]
     *      x^3 = 5 -> x = root[5, 3]
     *      x^5 = -2 -> x = root[-2, 5]
     */
    TakeRootOfBothSides,

    /**
     * Transform the equation x^n = 0 into x = 0 (with n > 0).
     */
    TakeRootOfBothSidesRHSIsZero,

    /**
     * Extract the solution of an equation from an identity.
     *
     * E.g. 3x + 1 = 3x + 1 -> x \in R
     */
    ExtractSolutionFromIdentity,

    /**
     * Extract the solution of an equation from a contradiction.
     *
     * E.g. 3 = 4 -> x \in \emptyset
     */
    ExtractSolutionFromContradiction,

    /**
     * Extract the solution from an equation of the form
     * x^n = negative constant, when n is even.
     *
     * E.g. x^2 = -9 -> x \in \emptyset
     *      x^6 = -1 -> x \in \emptyset
     */
    ExtractSolutionFromEvenPowerEqualsNegative,

    /**
     * Extract the solution of an equation from an equation which is in
     * a solved form, i.e. x = a.
     *
     * E.g. x = 2 sqrt[2] -> x \in { 2 sqrt[2] }
     */
    ExtractSolutionFromEquationInSolvedForm,

    /**
     * Extract the solution set from an equation of the form
     * x = +/-constant
     *
     * E.g. x = +/-3 -> x \in { 3, -3 }
     */
    ExtractSolutionFromEquationInPlusMinusForm,

    /**
     * Solve a single equation in an equation union.
     */
    SolveEquationInEquationUnion,

    /**
     * Add the opposite of everything on the LHS to both sides
     * of the equation.
     *
     * E.g. 4x - 3 = 2x + 1 -> 4x - 3 - (2x + 1) = 2x + 1 - (2x + 1)
     */
    MoveEverythingToTheLeft,

    /**
     * Collect like terms to the left
     *
     * %1: "like" variable
     */
    CollectLikeTermsToTheLeft,

    /**
     * Collect like terms to the left and simplify
     */
    CollectLikeTermsToTheLeftAndSimplify,

    /**
     * Add the opposite of everything on the LHS to both sides
     * of the equation and simplify.
     *
     * E.g. 4x - 3 = 2x + 1
     *      -> 4x - 3 - (2x + 1) = 2x + 1 - (2x + 1)
     *      -> 2x - 4 = 0
     */
    MoveEverythingToTheLeftAndSimplify,

    /**
     * Multiply both sides of the equation by the inverse of the coefficient
     * of the variable and simplify.
     *
     * E.g. [2x / 5] = 3
     *      -> [2x / 5] * [5 / 2] = 3 * [5 / 2]
     *      -> x = [15 / 2]
     */
    MultiplyByInverseCoefficientOfVariableAndSimplify,

    /**
     * Complete a binomial so that the LHS can be factorised to a square
     *
     * E.g. [x ^ 2] + 6x = 3 -> [x ^ 2] + 6x + [([6 / 2] ^ 2] = 3 + [([6 / 2] ^ 2]
     */
    CompleteTheSquare,

    /**
     * Complete the square on the LHS then simplify the equation
     *
     * E.g. [x ^ 2] + 6x = 3 -> [x ^ 2] + 6x + 9 = 3 + 9
     */
    CompleteTheSquareAndSimplify,

    /**
     * Multiply both sides of an equation by the inverse of the leading coefficient
     * on the LHS, the resulting polynomial on the left is monic.
     *
     * E.g. 2[x ^ 2] + 1 = 7 --> [x ^ 2] + [1 / 2] = [7 / 2]
     */
    MultiplyByInverseOfLeadingCoefficientAndSimplify,

    /**
     * Divide both sides of the equation by the coefficient of the variable
     * and simplify.
     *
     * E.g. 2 sqrt[2] x = 3
     *      -> [2 sqrt[2] x / 2 sqrt[2]] = [3 / 2 sqrt[2]]
     *      -> x = [3 sqrt[2] / 4]
     */
    DivideByCoefficientOfVariableAndSimplify,

    /**
     * Solve a linear equation in a given variable by collecting variables
     * on the LHS, constants on the RHS then dividing by the coefficient
     * of the variable.
     *
     * E.g. 4x + 3 = 2x + 7
     *      -> 2x + 3 = 7
     *      -> 2x = 4
     *      -> x = 2
     */
    SolveLinearEquation,

    /**
     * Solve an equation with a single monomial and  without a linear term by
     * moving the monomial to the left hand side, the constants to the right
     * hand side and taking the square root.
     *
     * E.g. 3x^4 + 2 = x^4 + 4
     *      -> 2x^4 + 2 = 4
     *      -> 2x^4 = 2
     *      -> x^4 = 1
     *      -> x = +/-1
     */
    SolveEquationUsingRootsMethod,

    /**
     * Solve an equation by completing the square
     *
     * E.g.  [x ^ 2] + 4x = 10
     *       --> [x ^ 2] + 4x + 4 = 10 + 4
     *       --> [(x + 2) ^ 2] = 14
     *       ...
     *  Note that the equation does not have to be quadratic, e.g. it could be
     *
     *       [x ^ 4] + 2[x ^2] = 6
     */
    SolveByCompletingTheSquare,

    /**
     * Collect solutions from several equations (e.g. when solving a quadratic
     * equation by factorisation, collect the solutions after solving two linear
     * equations).
     * This will be the last task in a task set.
     *
     * E.g. when solving (x − 1)(x + 1) = 0 the system solves
     *      x - 1 = 0 -> x \in { 1 } and
     *      x + 1 = 0 -> x \in { -1 } and
     *      collects the solutions as x \in { 1, -1}
     */
    CollectSolutions,

    /**
     * Solve a polynomial equation (of arbitrary degree) by moving everything
     * to the left hand side, factoring it, then setting each of the terms equal
     * to zero.
     *
     * E.g. x^6 = x^2
     *      -> x^6 - x^2 = 0
     *      -> x^2(x − 1)(x + 1)(x^2 + 1) = 0
     *      solve x^2 = 0, x - 1 = 0, x + 1 = 0 and x^2 + 1 = 0
     *      -> x \in {0, 1, -1}
     */
    SolveEquationByFactoring,

    /**
     * factor out the negative sign from the leading coefficient term
     * of the polynomial equation
     *
     * E.g. -[x^2] + 2x - 1 = 0
     *      --> (-1) ([x^2] - 2x + 1) = 0
     */
    FactorNegativeSignOfLeadingCoefficient,

    /**
     * Applies the quadratic formula i.e. x = [ -b +/- sqrt[b^2 - 4ac] / 2a ]
     * to a quadratic equation in standard form: a[x^2] + bx + c = 0
     *
     * E.g.  [x^2] + 4x + 3 = 0
     *      --> x = [-4 +/- sqrt[[4 ^ 2] - 4 * 1 * 3] / 2 * 1]
     */
    ApplyQuadraticFormula,

    /**
     * Separates the two distinct quadratic equation solutions with +/- sign
     * to an `Equation Union` of two separate equations, i.e.
     * one containing the '+' sign and the other containing
     * the '-' sign
     *
     * E.g.  x = [-4 +/- 2 sqrt[2] / 2 * 1]
     *      --> x = [-4 + 2 sqrt[2] / 2 * 1]
     */
    SeparatePlusMinusQuadraticSolutions,

    /**
     * Split a factored equation = 0 to several smaller equations
     *
     * E.g. (x + 1)(2x - 2) = 0 --> x + 1 = 0 OR 2x - 2 = 0
     */
    SeparateFactoredEquation,

    /**
     * Cancel the common numerator and denominator terms on both the sides
     * of an equation, to get the equation into a standard form, i.e.
     *  a[x^2] + bx + c = 0, where a > 0 and gcd(a, b, c) = 1
     *
     * E.g.  [ 2([x^2] - 2x + 1) / 2] = [0 / 2]
     *      --> [x^2] - 2x + 1 = 0
     */
    EliminateConstantFactorOfLhsWithZeroRhs,

    /**
     * Simplify a quadratic equation by standardizing the quadratic equation to
     * the form, a[x^2] + bx + c = 0, where a > 0
     *
     * E.g. -2[x^2] + 4x - 2 = 0
     *      --> (-1)(2[x^2] - 4x + 2) = 0
     *      --> 2[x^2] - 4x + 2 = 0
     */
    SimplifyByFactoringNegativeSignOfLeadingCoefficient,

    /**
     * Simplify a quadratic equation by standardizing the quadratic equation to
     * the form, a[x^2] + bx + c = 0, where gcd(a, b, c) = 1
     *
     * E.g.  2[x^2] - 4x + 2 = 0
     *      --> 2([x^2] - 2x + 1) = 0
     *      --> [x^2] - 2x + 1 = 0
     */
    SimplifyByDividingByGcfOfCoefficients,

    /**
     * Extract solution for an equation, which has a negative value
     * under square root
     *
     * E.g. x = [-4 +/- sqrt[-5] / 2] --> x \in emptySet if x \in R
     */
    ExtractSolutionFromNegativeUnderSquareRootInRealDomain,

    /**
     * Extract the solution set from an equation union of the form:
     *
     * x = constantExpr1, x = real conjugate of constantExpr1
     * E.g. x = [-1 - sqrt[2] / 2], x = [-1 + sqrt[2] / 2]
     *      --> x \in { [-1 - sqrt[2] / 2], [-1 + sqrt[2] / 2] }
     */
    ExtractSolutionFromEquationInUnionForm,

    /**
     * Given an equation union: e.g. x + 2 = 3 OR 2x = 5, solve each equation and
     * gather the solutions together.
     *
     * E.g. x = [-4 - 2 / 2 * 1], x = [-4 + 2 / 2 * 1]
     *      --> task # 1:
     *          x = -3
     *
     *      --> task # 2:
     *          x = -1
     *
     *      --> task # 3:
     *          x \in {-3, -1}
     */
    SolveEquationUnion,

    /**
     * Solve any quadratic equation (complete or incomplete) by the method of quadratic formula
     *
     * E.g. [x^2] + 4x = -3
     *      --> [x^2] + 4x + 3 = 0
     *      --> x = [-4 +/- sqrt[[4 ^ 2] - 4 * 1 * 3] / 2 * 1]
     *      --> x = [-4 +/- 2 / 2 * 1]
     *      --> x = [-4 - 2 / 2 * 1], x = [-4 + 2 / 2 * 1]
     *      --> x \in {-3, -1}
     */
    SolveQuadraticEquationUsingQuadraticFormula,

    /**
     * Solve equation in one variable, trying to apply the most relevant method.
     */
    SolveEquationInOneVariable,
    ;

    override val category = "Equations"
}

typealias Explanation = EquationsExplanation
