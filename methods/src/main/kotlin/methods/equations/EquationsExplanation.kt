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

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.LegacyKeyName
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
     * Reduce the equation to an equivalent equation which should be simpler to solve.
     * If possible this puts the equation in the form f(x) = 0
     */
    ReduceEquation,

    /**
     * Multiply both sides of an equation by the inverse of the leading coefficient
     * on the LHS.  When simplified this will lead to a monic polynomial on the left.
     *
     * E.g. 2[x ^ 2] + 1 = 7 --> (2[x ^ 2] + 1)*[1 / 2] = 7 * [1 / 2]
     */
    MultiplyByInverseOfLeadingCoefficient,

    /**
     * Transform the equation x^n = 0 into x = 0 (with n > 0).
     */
    TakeRootOfBothSidesRHSIsZero,

    /**
     * Extract the solution of an equation from an identity.
     * %1: solution variable
     *
     * E.g. 3x + 1 = 3x + 1 -> x \in R
     */
    ExtractSolutionFromIdentity,

    /**
     * Extract the solution of an equation from a contradiction.
     * %1: solution variable
     *
     * E.g. 3 = 4 -> x \in \emptyset
     */
    ExtractSolutionFromContradiction,

    /**
     * Extract a true statement from an equation with no variable which is obviously true
     *
     * E.g. 1 = 1 --> True
     */
    ExtractTruthFromTrueEquality,

    /**
     * Extract a false statement from an equation with no variable which is obviously false
     *
     * E.g. 1 = 3 --> False
     */
    ExtractFalsehoodFromFalseEquality,

    /**
     * A constant equation with at least one side undefined is false.
     *
     * E.g. 2 = 1/(1 - 1) -> undefined -> false
     */
    UndefinedConstantEquationIsFalse,

    /**
     * An equation with at least one side undefined cannot be solved.
     *
     * E.g. x = 1/(1 - 1) -> undefined -> cannot be solved
     */
    UndefinedEquationCannotBeSolved,

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
     * Rewrite a quadratic equation to the form [(x + a)^2] = b
     *
     * E.g. [x^2] + 3x = [15 / 8] -> [(x + [3/2])^2] = [33 / 8]
     */
    RewriteToXPLusASquareEqualsBForm,

    /**
     * Multiply both sides of an equation by the inverse of the leading coefficient
     * on the LHS, the resulting polynomial on the left is monic.
     *
     * E.g. 2[x ^ 2] + 1 = 7 --> [x ^ 2] + [1 / 2] = [7 / 2]
     */
    MultiplyByInverseOfLeadingCoefficientAndSimplify,

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
     * Solve a linear equation in a given variable, working with decimal
     * numbers instead of fractions, if possible.
     *
     * E.g. 4x + 3/2 = 2x + 7.3
     *      -> 4x + 1.5 = 2x + 7.3
     *      -> 2x + 1.5 = 7.3
     *      -> 2x = 5.8
     *      -> x = 2.9
     */
    SolveDecimalLinearEquation,

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
     * %1: the factor to eliminate
     *
     * E.g.  [ 2([x^2] - 2x + 1) / 2] = [0 / 2]
     *      --> [x^2] - 2x + 1 = 0
     */
    EliminateConstantFactorOfLhsWithZeroRhs,

    /**
     * Simplify an equation or inequation with zero RHS by eliminating a
     * non-zero factor on the LHS.
     *
     * E.g 4xy = 0 --> xy = 0
     */
    SimplifyByEliminatingConstantFactorOfLhsWithZeroRhs,

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
     * Solve equation in a given variable, trying to apply the most relevant method.
     */
    @LegacyKeyName("Equations.SolveEquationInOneVariable")
    SolveEquation,

    /**
     * Determine whether an equation between constants is true or false.
     */
    SolveConstantEquation,

    /**
     * Solve a rational equation
     */
    SolveRationalEquation,

    /**
     * Separate an equation of the form |f(x)| = K (positive constant) into two equations,
     * f(x) = K and f(x) = -K so that they can then be solved.
     *
     * E.g. 2abs[x - 1] = 3 --> 2(x - 1) = 3 OR 2(x - 1) = -3
     *      abs[x - 3] = 5  --> x - 3 = 5 OR x - 3 = -5
     */
    SeparateModulusEqualsPositiveConstant,

    /**
     * Remove the modulus sign from an equation of the form K|f(x)| = 0
     *
     * E.g. abs[2x + 1] = 0 --> 2x + 1
     *      2abs[x - 3] = 0 --> 2(x - 3) = 0
     */
    ResolveModulusEqualsZero,

    /**
     * Deduce a contradiction from an equation of the form K_1|f(x)| = K_2 where K_1 > 0 and K_2 < 0.
     *
     * E.g. abs[x + 1] = -2        --> Contradiction
     *      5abs[x + 2] = -3       --> Contradiction
     *      [abs[x + 4] / 2] = -10 --> Contradiction
     */
    ExtractSolutionFromModulusEqualsNegativeConstant,

    /**
     * Solve an equation with one absolute value containing the variable
     *
     * It solves e.g. abs[2x + 3] = 10
     *                5 = 3 - 2abs[[x^2] - x]
     */
    SolveEquationWithOneAbsoluteValue,

    /**
     * Subtract a modulus from both sides of an equation so there is a modulus on each side,
     * in an equation with two moduli on the LHS.
     *
     * E.g. abs[x + 1] - abs[x - 2] = 0 --> abs[x + 1] - abs[x - 2] + abs[x - 2] = abs[x - 2]
     *
     */
    MoveSecondModulusToRhs,

    /**
     * Subtract a modulus from both sides of an equation so there is a modulus on each side,
     * in an equation with two moduli on the RHS.
     *
     * E.g. 0 = abs[x + 1] - abs[x - 2] --> abs[x - 2] = abs[x + 1] - abs[x - 2] + abs[x - 2]
     *
     */
    MoveSecondModulusToLhs,

    /**
     * Rewrite an equation with two moduli on the same side to an equation with one on each side.
     *
     * E.g. abs[ x ] + abs[x - 1] = 0 --> abs[ x ] = abs[x - 1]
     */
    MoveOneModulusToOtherSideAndSimplify,

    /**
     * Turn an equation of the form |f(x)| = -|g(x)| into a system of two equations
     *      f(x) = 0, g(x) = 0
     *
     * E.g. abs[[x^2] - x] = -abs[x - 1] --> [x^2] - x = 0, x - 1 = 0
     */
    ResolveModulusEqualsNegativeModulus,

    /**
     * Solve an equation containing two moduli and nothing else
     *
     * E.g. abs[[x^3]] - abs[ x ] = 0
     *      abs[2x + 1] + 5abs[x] = 0
     */
    SolveEquationWithTwoAbsoluteValues,

    /**
     * In an equation with an absolute value, ensure the absolute value is on its own
     * on one side of the equation.
     *
     * E.g. 3x + 2abs[x-1] - 2 = 1 - x --> 2abs[x - 1] = 3 - 4x
     */
    IsolateAbsoluteValue,

    /**
     * Simplify a constraint as much as possible
     *
     * E.g. 2x > 4 -> x \in (2, \Infty)
     *
     * Sometimes the constraint cannot be solved so we end up with an inequality!
     */
    SimplifyConstraint,

    /**
     * In the context of solving an equation with a constraint, solve the equation
     * ignoring the constraint.  At a later stage the solutions will be checked against
     * the constraint.
     */
    SolveEquationWithoutConstraint,

    /**
     * Write the final solution of an equation with a constraint, in the case that all
     * solutions satisfy the constraint.
     */
    AllSolutionsSatisfyConstraint,

    /**
     * Write the final solution of an equation with a constraint, in the case that none
     * of the solutions satisfy the constraint.
     */
    NoSolutionSatisfiesConstraint,

    /**
     * Write the final solution of an equation with a constraint, in the case that some
     * solutions do not satisfy the constraint.
     */
    SomeSolutionsDoNotSatisfyConstraint,

    /**
     * Write the final solution of an equation with a constraint, in the case that the constraint applies to the
     * parameters of the solution, not the solution variable.
     *
     * E.g. the solution is of the form x = 2y GIVEN y != 0
     */
    AddConstraintToSolution,

    /**
     * Write the final solution of an equation with a constraint, in the case that the result
     * is obvious because e.g. the equation has no solution or the constraint is a contradiction.
     */
    GatherSolutionsAndConstraint,

    /**
     * In the context of solving an equation with a constraint, check if an individual
     * solution satisfies the constraint
     *
     * %1: the solution to check
     */
    CheckIfSolutionSatisfiesConstraint,

    /**
     * Solve an equation of the form |f(x)| = g(x) checking solutions are correct by
     * substituting them back into the original equation (method used in the US).
     */
    SolveEquationWithOneAbsoluteValueBySubstitution,

    /**
     * Split an equation containing an absolute value without carrying the constraints into the individual equations
     * and solve all the resulting equations, gathering the solutions at the end.  This is used as a step in the US
     * method for solving an equation of the form |f(x)| = g(x)
     *
     * E.g.
     *      abs[x - 1] = 2x + 5
     *      --> x - 1 = 2x + 5 OR x - 1 = -(2x + 5)
     *      --> x = -6 OR x = -[4/3]
     *
     *  Note that in the above -6 is not a solution to the original equation.
     */
    SplitEquationWithAbsoluteValueAndSolve,

    /**
     * Split an equation containing an absolute value without carrying the constraints into the individual equations.
     * This is used as a step in the US method for solving an equation of the form |f(x)| = g(x)
     *
     * E.g.
     *      abs[x - 1] = 2x + 5
     *      --> x - 1 = 2x + 5 OR x - 1 = -(2x + 5)
     */
    SeparateModulusEqualsExpressionWithoutConstraint,

    /**
     * Split an equation containing an absolute value into two equations with constraints
     *
     * E.g.
     *      abs[x - 1] = 2x + 5
     *      --> x - 1 = 2x + 5 GIVEN x - 1 >= 0 OR -(x - 1) = 2x + 5 GIVEN x - 1 < 0
     */
    SeparateModulusEqualsExpression,

    /**
     * Factors denominator of a fraction(s) in an expression/equation
     *
     * E.g. [12 / [x^2] - 9] = [8x / [x^2] - 16] --> [12 / (x-3)(x+3)] = [8x / (x-4)(x+4)]
     */
    FactorDenominatorOfFraction,

    /**
     * Multiply both sides of a rational equation with denominator (unique) present in the equation
     *
     * E.g. [12 / [x^2] - 9] + [1/[x^2] - 9] = 8
     *      --> [12 / [x^2] - 9] ([x^2] - 9) + [1/[x^2] - 9] ([x^2] - 9) = 8 ([x^2] - 9)
     *
     * Another E.g. [12 / [x^2] - 9] + 8 sqrt[3] = [8 / [x^2] - 9]
     *              --> [12 / [x^2] - 9] ([x^2] - 9) + 8 sqrt[3] ([x^2] - 9) = [8 / [x^2] - 9] ([x^2] - 9)
     */
    MultiplyBothSidesByDenominator,

    /**
     * Split an equation with rational variables and irrational coefficients
     *
     * E.g x + sqrt[2] = y + x sqrt[2] + 1 --> x = y + 1 AND sqrt[2] = x sqrt[2]
     */
    SplitEquationWithRationalVariables,
    ;

    override val category = "Equations"
}

typealias Explanation = EquationsExplanation
