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

package methods.integerrationalexponents

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class IntegerRationalExponentsExplanation : CategorisedMetadataKey {
    /**
     * factorize integer under rational exponent
     * e.g. [ 18^[2/3] ] --> [ ([2 * [3^2]]) ^ [2/3] ]
     */
    FactorizeIntegerUnderRationalExponent,

    /**
     * do two things one after the other to one exponent term:
     * 1. apply power rule of exponent, i.e. [ [a ^ b] ^ c] --> [ a ^ b*c ]
     * 2. simplify the power `b * c`
     */
    PowerRuleOfExponents,

    /**
     * brings the "integers" or exponents with integral powers to the front
     * e.g. [2 ^ [2 / 5]] * [3 ^ 2] * 5 --> [3 ^ 2] * 5 * [2 ^ [2 / 5]]
     */
    NormaliseProductWithRationalExponents,

    /**
     * splits product of exponents with rational powers,
     * e.g. [2 ^ [2 / 3]] * [3 ^ [10 / 3]] * [5 ^ [4 / 3]] -->
     * [2 ^ [2 / 3]] * [3 ^ 3] *  [3 ^ [1 / 3]] * 5 * [5 ^ [4 / 3]]
     */
    SplitProductOfExponentsWithImproperFractionPowers,

    /**
     * evaluates product of integers, integer exponents
     * and simplifies the product to bring integer to the beginning
     * of the expression
     * for e.g. 2 * [2 ^ [1/5]] * 5 * [5 ^ [2/3]] * [7^2] -->
     * 2 * 5 * [7^2] * [2 ^ [1/5]] * [5 ^ [2/3]] -->
     * 490 * [2^[1/5]] * [5^[2/3]]
     */
    NormalizeRationalExponentsAndIntegers,

    /**
     * simplifies rational exponent of an integer
     */
    SimplifyRationalExponentOfInteger,

    /**
     * Transforms two powers with rational exponents in a product or fraction
     * such that the exponents have the same denominator (without evaluating the
     * products in the exponents)
     * E.g. [3 ^ [1 / 2]] * [5 ^ [2 / 3]] -> [3 ^ [3 * 1 / 3 * 2]] * [5 ^ [2 * 2 / 2 * 3]]
     */
    FindCommonDenominatorOfRationalExponents,

    /**
     * Transforms two powers with rational exponents in a product or fraction
     * such that the exponents have the same denominator (and also evaluates the
     * products in the new exponents)
     * E.g. [3 ^ [1 / 2]] * [5 ^ [2 / 3]] -> [3 ^ [3 / 6]] * [5 ^ [4 / 6]]
     */
    BringRationalExponentsToSameDenominator,

    /**
     * Transforms two powers with rational exponents with the
     * same denominator in a product by extracting the denominator
     * E.g. [3 ^ [3 / 6]] * [5 ^ [4 / 6]] -> [[3 ^ 3] * [5 ^ 4] ^ [1 / 6]]
     */
    FactorDenominatorOfRationalExponents,

    /**
     * Write the product of two powers with the same exponent
     * as the power of the products and simplify the result
     * E.g. [3 ^ [2 / 3]] * [5 ^ [2 / 3]] -> [15 ^ [2 / 3]]
     */
    SimplifyProductOfPowersWithSameExponent,

    /**
     * Do a series of transformations to convert a product of
     * two powers with different bases and different rational
     * exponents to a single power.
     * E.g. [3 ^ [1 / 2]] * [5 ^ [2 / 3]] -> [16875 ^ [1 / 6]]
     */
    SimplifyProductOfPowersWithRationalExponents,

    /**
     * If in a product there are two powers whose exponents are the negations
     * of each other, then inverts the base of the negated to get equal exponents
     * and simplifies the result
     *
     * E.g. [2 ^ [1 / 2]] * [([4 / 3]) ^ -[1 / 2]] -> [([3 / 2]) ^ [1 / 2]]
     * or [2 ^ [1 / 2]] * [3 ^ -[1 / 2]] -> [([2 / 3]) ^ [1 / 2]]
     */
    SimplifyProductOfPowersWithNegatedExponent,

    /**
     * Write the product of two powers with the same base as the
     * power to the sum of the exponents and simplify the result
     * E.g. [3 ^ [1 / 2]] * [3 ^ [2 / 3]] -> [3 ^ [7 / 6]]
     */
    SimplifyProductOfPowersWithSameBase,

    /**
     * If in a product there are two powers such that one of them is the reciprocal
     * of the other, then it inverts that to get equal bases and then simplifies the
     * result
     *
     * E.g. [2 ^ [1 / 2]] * [([1 / 2]) ^ [2 / 5]] -> [2 ^ [1 / 10]]
     */
    SimplifyProductOfPowersWithInverseBase,

    /**
     * If in a product there are two powers of fractions, such that one of the
     * fractions is the inverse of the other, then it inverts one of them to get
     * equal bases and simplifies the result
     *
     * E.g. [([2 / 3]) ^ [1 / 2]] * [([3 / 2]) ^ [2 / 5]] -> [([2 / 3]) ^ [1 / 10]]
     */
    SimplifyProductOfPowersWithInverseFractionBase,

    /**
     * Write the fraction of two powers with the same base as the
     * power to the difference of the exponents and simplify the result
     * E.g. [[3 ^ [1 / 2]] / [3 ^ [2 / 3]]] -> [3 ^ -[1 / 6]]
     */
    SimplifyFractionOfPowersWithSameBase,

    /**
     * Write the fraction of two powers with the same exponent
     * as the power of the fraction and simplify the result
     * E.g. [[3 ^ [2 / 3]] / [6 ^ [2 / 3]]] -> [([1 / 2]) ^ [2 / 3]]
     */
    SimplifyFractionOfPowersWithSameExponent,

    /**
     * A negative value cannot be raised to a rational power.
     *
     * E.g. [-1 ^ [1/3]] -> /undefined/
     */
    EvaluateNegativeToRationalExponentAsUndefined,

    /**
     * apply the power rule [1/a^n] = a^-n
     *
     * E.g. [1 / [3^-[1/4]]] -> [3^[1/4]]
     */
    ApplyReciprocalPowerRule,

    /**
     * Simplifies product of integer and rational exponent of integer
     *
     * E.g. 27 * [6^-[1/3]] ->
     *          27          -> [3^3]
     *          [6^-[1/3]]  -> [2^-[1/3]] * [3^-[1/3]]
     *          [3^3] * [2^-[1/3]] * [3^-[1/3]]
     */
    SimplifyProductOfIntegerAndRationalExponentOfInteger,
    ;

    override val category = "IntegerRationalExponents"
}

typealias Explanation = IntegerRationalExponentsExplanation
