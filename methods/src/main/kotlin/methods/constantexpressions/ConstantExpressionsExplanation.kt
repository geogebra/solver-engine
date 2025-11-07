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

package methods.constantexpressions

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class ConstantExpressionsExplanation : CategorisedMetadataKey {
    /**
     * Simplify an expression in brackets
     */
    SimplifyExpressionInBrackets,

    /**
     * Simplify roots in the expression, even if they are deep inside it
     *
     * E.g. 5 + 7 * root[12]] -> 5 + 7 * (2 * root[2])
     *
     * Note: it can be roots of any order, not just square roots.
     */
    SimplifyRootsInExpression,

    /**
     * Simplify the power of an integer
     * E.g. 3^4 -> 81
     *      2^(-3) -> 1/2^3 -> 1/8
     */
    SimplifyPowerOfInteger,

    /**
     * Simplify the power of a product
     * E.g. (3 sqrt(2))^2 -> 3^2 (sqrt(2))^2 -> 9 * 2 -> 18
     */
    SimplifyPowerOfProduct,

    /**
     * Simplify the power of a fraction, in multiple sub-steps
     * E.g. (1 + sqrt(2) / 2)^(-2) -> 12 - 8 sqrt(2)
     */
    SimplifyPowerOfFraction,

    /**
     * Simplify the power of an absolute value
     * E.g.; |y|^2 -> y^2
     */
    SimplifyPowerOfAbsoluteValue,

    /**
     * Simplify power of trigonometric function
     * E.g [-sin x ^ 2] -> sin x^2
     */
    SimplifyPowerOfTrigonometricFunction,

    /**
     * Rewrite integer order roots as powers in an expression that also contains rational exponents.
     * This is so that further simplifications of rational exponents can be performed
     *
     * E.g. 5 + sqrt[3] * [4 ^ [1/2]] ->  5 + [3 ^ [1/2]] * [4 ^ [1/2]]
     */
    RewriteIntegerOrderRootsAsPowers,

    /**
     * Simplify an expression containing only constant terms (no variable)
     *
     * This is a public top-level transformation.
     */
    SimplifyConstantExpression,

    ;

    override val category = "ConstantExpressions"
}

typealias Explanation = ConstantExpressionsExplanation
