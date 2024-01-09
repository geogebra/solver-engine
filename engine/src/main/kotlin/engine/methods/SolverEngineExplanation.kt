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

package engine.methods

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class SolverEngineExplanation : CategorisedMetadataKey {
    /**
     * Internal step for extracting neighbouring terms or factors into a partial sum or product
     * for further simplifications focusing on them.
     */
    ExtractPartialExpression,

    /**
     * Internal step for merging nested constraints into a single constraint. Ideally it would be hidden from the user.
     *
     * E.g. x = y + 1 GIVEN y >= 0 GIVEN y != 0 --> x = y + 1 GIVEN y > 0
     */
    MergeConstraints,

    /**
     * Rearrange a product so that certain factors (on which the system is going to focus) are next to each other.
     * E.g. 3^(1/2) * 2 * 3^(3/2) -> 3^(1/2) * 3^(3/2) * 2
     */
    RearrangeProduct,

    /**
     * Rearrange a sum so that certain terms (on which the system is going to focus) are next to each other.
     * E.g. 1/4 + 2 + 3/2 -> 1/4 + 3/2 + 2
     */
    RearrangeSum,

    /**
     * After executing a series of tasks on some terms of a sum or some factors of a product, substitute the result
     * into the original expression
     * E.g. for 1/(x + 1) + 1/(x + 2) + 1/(x + 3)
     *      first compute 1/(x + 1) + 1/(x + 2) -> (2x + 3)/(x + 1)(x + 2)
     *      then substitute back 1/(x + 1) + 1/(x + 2) + 1/(x + 3) -> (2x + 3)/(x + 1)(x + 2) + 1/(x + 3)
     */
    SubstituteResultOfTaskSet,
    InlinePartialSum,
    InlinePartialProduct,
    ;

    override val category = "SolverEngine"
}

/**
 * Words used in Math expressions. The SDK uses these words to render math.  It is convenient to define the keys here
 * as the export process will then automatically add them to the poker.
 */
@TranslationKeys
enum class MathWord : CategorisedMetadataKey {
    /**
     * Used when an expression has no value
     *
     * E.g. 1 / 0 = undefined
     */
    Undefined,

    /**
     * Used e.g. to introduce a constraint for an equation
     *
     * E.g. y = 1/x given x != 0
     */
    Given,

    /**
     * Used to say a statement is true.
     *
     * E.g. 1 + 1 = 2 --> true
     */
    True,

    /**
     * Used to say a statement is false
     *
     * E.g. 1 + 1 = 3 --> false
     */
    False,

    /**
     * Used to express the conjunction of two conditions
     *
     * E.g. x > 2 and x != 3
     */
    And,

    //
    // The remaining words are used in the SDK, not in the engine.  They are defined here so that they get
    // automatically uploaded to ggbtrans.
    //

    /**
     * Used to express the disjunction of two conditions
     *
     * E.g. x < -2 or x > 2
     */
    Or,

    /**
     * Used to express that an equation has no solution
     */
    NoSolution,

    /**
     * Used to express that an equation has infinitely many solutions - this is a vague notion,
     * typically used to talk about an identity (e.g. "x = x has infinitely many solutions")
     */
    InfinitelyManySolutions,

    ;

    override val category = "MathWord"
}
