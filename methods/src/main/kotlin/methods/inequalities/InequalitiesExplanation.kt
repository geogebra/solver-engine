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

package methods.inequalities

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class InequalitiesExplanation : CategorisedMetadataKey {
    /**
     * Simplify an inequality so that
     * - opposite terms on the same side are cancelled first
     * - equal terms on both sides are cancelled next
     * - each side is simplified last
     *
     * E.g. x + 3 - 3 < 5x - 3 --> x < 5x - 3
     *      x - 4 >= 2x - 4 --> x >= 2x
     *      x + 2x <= 5 + 1 --> 3x <= 6
     */
    SimplifyInequality,

    /**
     * Extract a truth from a true inequality that has no variables
     *
     * E.g. 1 < 2 -> Truth
     */
    ExtractTruthFromTrueInequality,

    /**
     * Extract the solution from a true inequality.
     *
     * E.g. -3 < 7 -> x \in R
     */
    ExtractSolutionFromTrueInequality,

    /**
     * Extract a falsehood from a false inequality that has no variables
     *
     * E.g. 3 < 5 -> Falsehood
     */
    ExtractFalsehoodFromFalseInequality,

    /**
     * Extract the solution from a false inequality.
     *
     * E.g. -3 >= 7 -> x \in \emptyset
     */
    ExtractSolutionFromFalseInequality,

    /**
     * Extract the solution of an inequality from an inequality which is in
     * a solved form, i.e. x (<, <=, >, >=) a.
     *
     * E.g. x <= 2 sqrt[2] -> x \in ( -infinity, 2 sqrt[2] ]
     */
    ExtractSolutionFromInequalityInSolvedForm,

    /**
     * Extract the solution from an inequality of type "less than", whose
     * LHS is an absolute value and RHS is non-positive, i.e.
     * `k * abs(x) < a` where a <= 0, k > 0 and `x` is any expression
     *
     * E.g. 2 * abs[x + 1] < -3 -> x doesn't have a solution
     */
    ExtractSolutionFromModulusLessThanNonPositiveConstant,

    /**
     * Reduce an inequality of type "less than equal", whose
     * LHS is an absolute value and RHS equals 0 to an equation,
     * i.e. `k * abs(x) <= 0` where k > 0 and `x` is any expression
     *
     * E.g. 2 * abs[x + 1] <= 0 --> 2(x + 1) = 0
     */
    ReduceModulusLessThanEqualToZeroInequalityToEquation,

    /**
     * Extract the solution from an inequality of type "greater than equal to",
     * whose LHS is an absolute value and RHS is non-positive,
     * i.e. `k * abs(x) > a` where a <= 0, k > 0 and `x` is any expression
     *
     * E.g. 2*abs[x + 1] >= -3 -> true for all x
     */
    ExtractSolutionFromModulusGreaterThanEqualToNonPositiveConstant,

    /**
     * Extract the solution from an inequality of type "greater than",
     * whose LHS is an absolute value and RHS is a negative constant,
     * i.e. `k * abs(x) > a` where a < 0, k > 0 and `x` is any expression
     *
     * E.g. 2 * abs[x + 1] > -3 -> true for all x
     */
    ExtractSolutionFromModulusGreaterThanNegativeConstant,

    /**
     * Separate greater than inequality of the form p*|f(x)| > K, where
     * p > 0 and K > 0, into two inequalities, f(x) > K and f(x) < -K
     * so that they can then be solved.
     *
     * E.g. 2*abs[x - 1] > 3 --> 2(x - 1) > 3 OR 2(x - 1) < -3
     */
    SeparateModulusGreaterThanPositiveConstant,

    /**
     * Separate greater than equal to inequality of the form
     * p*|f(x)| >= K, where p > 0 and K > 0, into two inequalities,
     * f(x) >= K and f(x) <= -K so that they can then be solved.
     *
     * E.g. 2*abs[x - 1] >= 3 --> 2(x - 1) >= 3 OR 2(x - 1) <= -3
     */
    SeparateModulusGreaterThanEqualToPositiveConstant,

    /**
     * Solve a single inequality in an inequality union.
     *
     * E.g. solving 2(x - 1) >= 3 in inequality union
     * 2(x - 1) >= 3 OR 2(x - 1) <= -3
     */
    SolveInequalityInInequalityUnion,

    /**
     * solves the left inequality of the double inequality
     * k1 < f(x) < k2 or k1 < f(x) <= k2 or k1 <= f(x) < k2 or k1 <= f(x) <= k2
     * k1 > f(x) > k2 or k1 > f(x) >= k2 or k1 >= f(x) > k2 or k1 >= f(x) >= k2
     *
     * i.e. solves k1 < f(x) (or k1 <= f(x) or k1 > f(x) or k1 >= f(x))
     *
     * for e.g. for the double inequality -2 < 3x + 1 < 2
     * solves the inequality: -2 < 3x + 1 --> x > -1
     */
    SolveLeftInequalityInDoubleInequality,

    /**
     * solves the right inequality of the double inequality
     * k1 < f(x) < k2 or k1 < f(x) <= k2 or k1 <= f(x) < k2 or k1 <= f(x) <= k2
     * k1 > f(x) > k2 or k1 > f(x) >= k2 or k1 >= f(x) > k2 or k1 >= f(x) >= k2
     *
     * i.e. solves f(x) < k2 (or f(x) <= k2 or f(x) > k2 or f(x) >= k2)
     *
     * for e.g. for the double inequality -2 < 3x + 1 < 2
     * solves the inequality: 3x + 1 < 2 --> x < [1 / 3]
     */
    SolveRightInequalityInDoubleInequality,

    /**
     * Given an inequality union: e.g. 2(x + 1) > 3 OR 2(x + 1) < -3,
     * solve each inequality and gather the solutions together.
     *
     * E.g. 2(x + 1) > 3 OR 2(x + 1) < -3
     *      --> task # 1:
     *          2(x + 1) > 3
     *              x + 1 > [3/2]
     *              x > [1/2]
     *
     *      --> task # 2:
     *          2(x + 1) < -3
     *              x + 1 < -[3/2]
     *              x < -[5/2]
     *
     *      --> task # 3:
     *          x \in SetUnion[ (-\infty, -[5/2]), ([1/2], \infty) ]
     */
    SolveInequalityUnion,

    /**
     * Solves a simple double inequality of the form
     * k1 < f(x) < k2   OR
     * k1 <= f(x) < k2  OR
     * k1 < f(x) <= k2  OR
     * k1 <= f(x) <= k2 OR
     * k1 > f(x) > k2   OR
     * k1 > f(x) >= k2  OR
     * k1 >= f(x) > k2  OR
     * k1 >= f(x) >= k2
     *
     * and collects the solution
     */
    SolveDoubleInequality,

    /**
     * Collect solutions from several inequalities (e.g. when solving
     * an absolute value linear inequality, it combines the solution
     * of individual linear inequalities) and combine them by intersection.
     *
     * This will be the last task in a task set.
     *
     * E.g. when solving 2*abs[x + 1] < 3 the system solves
     *      2(x + 1) < 3 -> x \in Interval(-\infty, [1/2]) and
     *      2(x + 1) > -3 -> x \in Interval(-[5/2], \infty) and
     *      collects the solutions as x \in Interval(-[5/2], [1/2])
     */
    CollectIntersectionSolutions,

    /**
     * Collect solutions from several inequalities (e.g. when solving
     * an absolute value linear inequality, it combines the solution
     * of individual linear inequalities) and combine them by union.
     *
     * This will be the last task in a task set.
     *
     * E.g. when solving 2*abs[x + 1] > 3 the system solves
     *      2(x + 1) < -3 -> x \in Interval(-\infty, -[5/2]) and
     *      2(x + 1) > 3 -> x \in Interval([1/2], \infty) and
     *      collects the solutions as x \in Union[ Interval(-\infty, -[5/2]), Interval([1/2], \infty)]
     */
    CollectUnionSolutions,

    /**
     * In an inequality with an absolute value, ensure the absolute value is on its own
     * on one side of the equation.
     *
     * E.g. 3x + 2*abs[x-1] - 2 > 1 - x --> 2*abs[x - 1] > 3 - 4x
     */
    IsolateAbsoluteValue,

    /**
     * Converts greater than inequality of the form |f(x)| > 0 to negation
     * i.e. f(x) != 0
     *
     * for e.g. |3x - 1| > 0 --> 3x - 1 != 0
     */
    ConvertModulusGreaterThanZero,

    /**
     * Converts an inequality of the form |f(x)| < positiveConstant to a double inequality
     * i.e. -positiveConstant < f(x) < positiveConstant
     *
     * for e.g. |3x - 1| < 2 --> -2 < 3x - 1 < 2
     */
    ConvertModulusLessThanPositiveConstant,

    /**
     * Converts an inequality of the form `|f(x)| <= positiveConstant` to a double inequality
     * i.e. `-positiveConstant <= f(x) <= positiveConstant`
     *
     * for e.g. |3x - 1| <= 2 --> -2 <= 3x - 1 <= 2
     */
    ConvertModulusLessThanEqualToPositiveConstant,

    /**
     * Solve a linear inequality in a given variable by collecting variables
     * on the LHS, constants on the RHS then dividing by the coefficient
     * of the variable.
     *
     * E.g. 4x + 3 < 2x + 7
     *      -> 2x + 3 < 7
     *      -> 2x < 4
     *      -> x < 2
     */
    SolveLinearInequality,

    /**
     * Determine whether an inequality between constants is true or false.
     */
    SolveConstantInequality,

    /**
     * Solve an inequality reducible to the form
     * |f(x)| > k        OR
     * |f(x)| >= k       OR
     * |f(x)| < k        OR
     * |f(x)| <= k       OR
     */
    SolveInequalityWithVariablesInOneAbsoluteValue,

    ;

    override val category = "Inequalities"
}

typealias Explanation = InequalitiesExplanation
