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

package methods.inequations

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class InequationsExplanation : CategorisedMetadataKey {
    /**
     * Simplify both sides of an inequation (a statement with the not equals != operator).
     */
    SimplifyInequation,

    /**
     * Extract the solution of an inequation from an inequation which is in
     * a solved form, i.e. x != a.
     *
     * E.g. x != 2 sqrt[2] -> x \in R \ { 2 sqrt[2] }
     */
    ExtractSolutionFromInequationInSolvedForm,

    /**
     * Extract the solution of an inequation from an identity.
     * %1: solution variable
     *
     * E.g. x + 1 != x + 2 -> x \in R
     */
    ExtractSolutionFromTrueInequation,

    /**
     * Extract the solution of an inequation from a contradiction.
     * %1: solution variable
     *
     * E.g. x + 1 != x + 1 -> x \in \emptyset
     */
    ExtractSolutionFromFalseInequation,

    /**
     * Extract a true statement from an inequation with no variable which is obviously true
     *
     * E.g. 2 != 3 --> True
     */
    ExtractTruthFromTrueInequation,

    /**
     * Extract a false statement from an inequation with no variable which is obviously false
     *
     * E.g. 3 != 3 --> False
     */
    ExtractFalsehoodFromFalseInequation,

    /**
     * Solve an inequation (a statement with the not equals != operator) by first solving the
     * corresponding equation and then taking the complement of the solution.
     *
     * E.g. when solving x^2 + 3x + 2 != 0
     *      first solve x^2 + 3x + 2 = 0 -> x = 1 or x = 2
     *      then the result is x != 1 and x != 2
     */
    SolveInequationInOneVariable,

    /**
     * Solve the equation corresponding to an inequation (a statement with the not equals != operator).
     *
     * E.g. when solving x^2 + 3x + 2 != 0
     *      first solve x^2 + 3x + 2 = 0 to get x = 1 or x = 2
     */
    SolveEquationCorrespondingToInequation,

    /**
     * Determine whether an inequation between constants is true or false.
     */
    SolveConstantInequation,

    /**
     * Take the complement of the solution of the equation corresponding to an inequation (a statement
     * with the not equals != operator) to get the final result.
     *
     * E.g. when solving x^2 + 3x + 2 != 0
     *      knowing that x^2 + 3x + 2 = 0 -> x = 1 or x = 2
     *      take the complement of this result to get x^2 + 3x + 2 != 0 -> x != 1 and x != 2
     */
    TakeComplementOfSolution,

    /**
     * Reduce the inequation to an equivalent equation which should be simpler to solve.
     * If possible this puts the inequation in the form f(x) != 0
     */
    ReduceInequation,
    ;

    override val category = "Inequations"
}

typealias Explanation = InequationsExplanation
