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

package methods.equationsystems

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class EquationSystemsExplanation : CategorisedMetadataKey {
    /**
     * Simplify a single equation to prepare for solving the equation system
     */
    PrepareEquation,

    /**
     * Rearrange an equation to express a variable in terms of the other
     *
     * %1: variable to make the subject of the equations (on the LHS)
     * %2: variable to put on the RHS.
     * %3: name of the equation to rearrange
     */
    ExpressInTermsOf,

    /**
     * Substitute a variable into the equation and solve in the other variable
     *
     * %1: variable and value to substitute (e.g. y = x + 1)
     * %2: variable to solve in (e.g. x)
     * %3: the name of the equation to substitute into
     */
    SubstituteAndSolveIn,

    /**
     * Build the solution to a system of 2 equations with no solution because of a contradiction.
     */
    BuildSolutionContradiction,

    /**
     * Build the solution to a system of 2 equations where both solutions are identities.
     */
    BuildSolutionIdentities,

    /**
     * Build the solution to a system of 2 equations when only one variable is fixed.
     *
     * %1: the fixed variable
     */
    BuildSolutionOneVariableFixed,

    /**
     * Build the solution to a system of 2 equations when one equation is an identity
     */
    BuildSolutionIdentityAndEquation,

    /**
     * Build the solution to a system of 2 equations when there is a unique solution in each variable
     *
     * %1: the first variable
     * %2: the second variable
     */
    BuildSolutionCombineUniqueSolutions,

    /**
     * Build the solution to a system of 2 equations when both equations reduce to equations in
     * the same variable with the same solution
     *
     * %1: the variable
     * %2: value of the common equation
     */
    BuildSolutionSameSolutionInOneVariable,

    /**
     * Build the solution to a system of 2 equations when both equations reduce to equations in
     * the same variable with different solutions
     *
     * %1: the variable
     * %2: value of solution 1
     * $3: value of solution 2
     */
    BuildSolutionDifferentSolutionsInOneVariable,

    /**
     * Solve a system of 2 equations by substitution.
     */
    SolveEquationSystemBySubstitution,

    /**
     * Rewrite the addition of two equations, e.g.
     * 2x + 3y = 2 +
     * 3x - 3y = 1
     * as single equation e.g. 2x + 3y + (3x - 3y) = 2 + 1
     */
    RewriteEquationAddition,

    /**
     * Rewrite the subtraction of two equations, e.g.
     * 3x + 4y = 2 -
     * 3x +  y = 1
     * as single equation e.g. 3x + 4y - (3x + y) = 2 - 1
     */
    RewriteEquationSubtraction,

    /**
     * Evaluate the addition of two equations, e.g.
     * 2x + 3y = 2 +
     * 3x - 3y = 1
     * would be first transformed to 2x + 3y + (3x - 3y) = 2 + 1
     * then simplified to 5x = 3
     */
    AddEquations,

    /**
     * Evaluate the subtraction of two equations, e.g.
     * 3x + 4y = 2 -
     * 3x +  y = 1
     * would be first transformed to 3x + 4y - (3x + y) = 2 - 1
     * then simplified to 3y = 1
     */
    SubtractEquations,

    /**
     * Multiply both sides of an equation by a value.
     *
     * %1: factor the equation is multiplied by
     * %2: the name of the equation to multiply
     */
    MultiplyEquation,

    /**
     * Solve the univariate equation obtained by adding or
     * subtracting the two equations in a system when solving
     * it using elimination.
     *
     * %1: the name of the equation to solve
     */
    SolveEliminatedEquation,

    /**
     * Solve a system of two equations by elimination.
     */
    SolveEquationSystemByElimination,

    /**
     * Put the solutions of two individual equations together
     *
     * E.g. x = 1, x = 2            --> Contradiction
     *      x = 1 or 2, x = 2, or 3 -->  x = 2
     *      Etc
     */
    ComputeOverallSolution,

    /**
     * Solve an individual equation (with only one variable) in the system
     */
    SolveEquationInSystem,

    /**
     * Solve a system of two equations in only one variable
     *
     * E.g.
     *     2x + 1 = 5, [x ^ 2] = 2x --> x = 2
     */
    SolveEquationSystemInOneVariable,

    /**
     * Guess Integer solutions of a system of 2 equations in 2 variables where
     * one equation is of the form xy = n (n integer)
     *
     * E.g. x - y = 1 AND xy = 6 --> x = 3 AND y = 2
     *
     * Note: only one solution is given even if there may be more integer solutions.
     */
    GuessIntegerSolutionsOfSystemContainingXYEqualsInteger,
    ;

    override val category = "EquationSystems"
}

typealias Explanation = EquationSystemsExplanation
