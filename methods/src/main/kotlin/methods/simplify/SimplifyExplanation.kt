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

package methods.simplify

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.LegacyKeyName
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class SimplifyExplanation : CategorisedMetadataKey {
    /**
     * Simplify an expression in brackets
     */
    @LegacyKeyName("Polynomials.SimplifyExpressionInBrackets")
    SimplifyExpressionInBrackets,

    /**
     * Simplify a polynomial expression in one variable
     */
    @LegacyKeyName("Polynomials.SimplifyPolynomialExpressionInOneVariable")
    SimplifyPolynomialExpression,

    /**
     * Simplify an algebraic expression in one variable
     */
    @LegacyKeyName("Algebra.SimplifyAlgebraicExpression")
    SimplifyAlgebraicExpression,

    ;

    override val category = "Simplify"
}

typealias Explanation = SimplifyExplanation
