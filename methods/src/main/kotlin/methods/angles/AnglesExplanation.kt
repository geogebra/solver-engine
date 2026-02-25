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
     * Determine the main angle principal value of inverse trigonometric function
     */
    DetermineMainAnglePrincipalValueOfInverseFunction,

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

    /**
     * Apply the odd symmetry of sine
     */
    ApplyOddSymmetryOfSine,

    /**
     * Apply the even symmetry of cosine
     */
    ApplyEvenSymmetryOfCosine,

    /**
     * Apply the odd symmetry of secant
     */
    ApplyEvenSymmetryOfSecant,

    /**
     * Apply the odd symmetry of cosecant
     */
    ApplyOddSymmetryOfCosecant,

    /**
     * Apply the odd symmetry of tangent
     */
    ApplyOddSymmetryOfTangent,

    /**
     * Apply the odd symmetry of cotangent
     */
    ApplyOddSymmetryOfCotangent,

    /**
     * Apply the negative identity of cosine
     */
    ApplyNegativeIdentityOfCosine,

    /**
     * Apply the Pythagorean identity
     */
    ApplyPythagoreanIdentity,

    /**
     * Apply the Pythagorean identity and simplify
     */
    ApplyPythagoreanIdentityAndSimplify,

    /**
     * Apply the cosine difference identity
     */
    ApplyCosineDifferenceIdentity,

    /**
     * Apply the cosine sum identity
     */
    ApplyCosineSumIdentity,

    /**
     * Apply the sine difference identity
     */
    ApplySineDifferenceIdentity,

    /**
     * Apply the sine sum identity
     */
    ApplySineSumIdentity,

    /**
     * Apply the tangent difference identity
     */
    ApplyTangentDifferenceIdentity,

    /**
     * Apply the tangent sum identity
     */
    ApplyTangentSumIdentity,

    /**
     * Apply the sine double-angle identity
     */
    ApplySineDoubleAngleIdentity,

    /**
     * Apply the cosine double-angle identity
     */
    ApplyCosineDoubleAngleIdentity,

    /**
     * Apply the tangent double-angle identity
     */
    ApplyTangentDoubleAngleIdentity,

    /**
     * Apply trigonometric identity and simplify
     */
    ApplyTrigonometricIdentityAndSimplify,

    /**
     * Rearrange addends in argument of trigonometric function
     */
    RearrangeAddendsInArgument,

    /**
     * Apply the inverse sine function to both sides
     */
    ApplyInverseSineFunctionToBothSides,

    /**
     * The sine of an angle is always a real number between -1 and 1
     */
    ExtractSolutionFromImpossibleSineEquation,

    /**
     * Apply the identity of the inverse trigonometric function
     */
    ApplyIdentityOfInverseTrigonometricFunction,

    /**
     * Balance the trigonometric functions containing
     */
    BalanceEquationWithTrigonometricExpressions,

    /**
     * The trigonometric function containing a variable must not be undefined
     */
    ExpressionMustNotBeUndefined,

    /**
     * Compute domain of expression containing trigonometric functions
     */
    ComputeDomainOfTrigonometricExpression,

    /**
     * Simplify sum of trigonometric functions, where the argument of one is double the argument of the other
     */
    SimplifySumContainingDoubleAngles,

    /**
     * Reorder addends in sum containing double angle
     */
    AddLabelToSumContainingDoubleAngle,

    /**
     * Extract 2 from Trigonometric Function argument
     */
    ExtractTwoFromArgument,

    /**
     * Derive trigonometric function
     */
    DeriveTrigonometricFunction,

    ;

    override val category = "Angles"
}

typealias Explanation = AnglesExplanation
