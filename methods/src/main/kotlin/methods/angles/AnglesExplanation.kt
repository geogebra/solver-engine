/*
 * Copyright (c) 2025 GeoGebra GmbH, office@geogebra.org
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

package methods.angles

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class AnglesExplanation : CategorisedMetadataKey {
    /**
     * Convert degrees to radians.
     */
    ConvertDegreesToRadians,

    /**
     * Convert radians to degrees.
     */
    ConvertRadiansToDegrees,

    /**
     * Rewrite angle in degrees as a single fraction.
     */
    RewriteAngleInRadiansAsSingleFraction,

    /**
     * Use the conversion formula to convert degrees to radians.
     */
    UseDegreeConversionFormula,

    /**
     * Use the conversion formula to convert radians to degrees.
     */
    UseRadianConversionFormula,

    /**
     * Evaluate <trigonometric fun>
     */
    EvaluateExactValueOfMainAngle,

    /**
     * Evaluate trigonometric expression.
     */
    EvaluateTrigonometricExpression,

    /**
     * Find reference angle in first quadrant
     */
    FindReferenceAngle,

    /**
     * Convert expression with mixed units to radians
     */
    ConvertExpressionWithMixedUnitsToRadians,

    /**
     * Normalize angles by reducing them to the interval [0°,360°] or [0,2π].
     */
    NormalizeAngles,

    /**
     * Rewrite angle in degrees by extracting multiples of 360.
     */
    RewriteAngleInDegreesByExtractingMultiplesOf360,

    /**
     * Rewrite angle in radians by extracting multiples of 2 Pi
     */
    RewriteAngleInRadiansByExtractingMultiplesOfTwoPi,

    /**
     * Substitute angle with coterminal angle from unit circle.
     */
    SubstituteAngleWithCoterminalAngleFromUnitCircle,

    /**
     * Check if tangent is defined
     * %1: placeholder that will be substituted with 90° or pi/2 depending on the user input (degrees or radians)
     */
    CheckDomainOfTangent,

    /**
     * Check if cotangent is defined
     * %1: placeholder that will be substituted with 180° or pi depending on the user input (degrees or radians)
     */
    CheckDomainOfCotangent,

    /**
     * Check if secant is defined
     * %1: placeholder that will be substituted with 90° or pi/2 depending on the user input (degrees or radians)
     */
    CheckDomainOfSecant,

    /**
     * Check if cosecant is defined
     * %1: placeholder that will be substituted with 180° or pi depending on the user input (degrees or radians)
     */
    CheckDomainOfCosecant,

    /**
     * Derive trigonometric function from primitive functions
     */
    DeriveTrigonometricFunctionFromPrimitiveFunctions,

    ;

    override val category = "Angles"
}

typealias Explanation = AnglesExplanation
