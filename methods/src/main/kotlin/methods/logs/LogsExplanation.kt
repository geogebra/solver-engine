/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
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

package methods.logs

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class LogsExplanation : CategorisedMetadataKey {
    /**
     * Move the power to the outside of a log expression.
     *
     * E.g. log(x ^ 5) --> 5 log x
     */
    TakePowerOutOfLog,

    /**
     * Evaluate logarithm of base to 1.
     *
     * E.g. ln e    --> 1
     *      log 10  --> 1
     *      log_2 2 --> 1
     */
    EvaluateLogOfBase,

    /**
     * Evaluate logarithm of 1 to 0.
     */
    EvaluateLogOfOne,

    /**
     * Evaluate logarithm of a non-positive number as undefined.
     *
     * E.g.  ln -10 --> UNDEFINED
     *       log 0  --> UNDEFINED
     */
    EvaluateLogOfNonPositiveAsUndefined,

    /**
     * Simplify an expression in the form log(1/n)
     *
     * E.g.  log[1 / 10] --> -log 10
     *       ln[1/x] --> = ln x
     */
    SimplifyLogOfReciprocal,

    /**
     * Rewrite the logarithm of a "known" power so it can be further simplified.
     *
     * E.g.  ln 8 --> ln(2^3)
     *       log 10000 --> log(10^4)
     *       log_2 400 --> log_2(20^2)
     *
     * Known powers include squares of small numbers, cubes of very small numbers, both possibly multiplied by a
     * square / cube of a power of 10.
     */
    RewriteLogOfKnownPower,

    /**
     * Simplify the logarithm of a known simple power as in the examples.
     *
     * E.g.  ln 8 --> ln(2^3) --> 3 ln 2
     *       log 10000 --> log(10^4) --> 4 log 10
     */
    SimplifyLogOfKnownPower,

    SplitLogOfProduct,
    SplitLogOfFraction,

    /**
     * Evaluate logarithm with non-positive base as an undefined expression
     */
    EvaluateLogWithNonPositiveBaseAsUndefined,

    /**
     * Evaluate log with base one as an undefined expression
     */
    EvaluateLogWithBaseOne,

    /**
     * Convert logarithm to base %1
     *
     * %1 - new base
     */
    SwitchBaseOfLogarithm,

    /**
     * Bring logarithms to common base
     *
     */
    BringLogsToCommonBase,

    /**
     * Switch the base of a logarithm to %1 and simplify
     *
     * %1 - new base
     */
    SwitchBaseOfLogarithmAndSimplify,

    /**
     * Rewrite the base and argument as powers with same exponent
     */
    RewriteLogUsingMatchingPowers,

    /**
     * Simplify the common exponents in base and argument
     */
    SimplifyLogWithCommonExponents,

    /**
     * Expand a logarithm which has an argument that is not a power of the base
     *
     * e.g. log_[2] 45 -> log_[2] [3^2] + log_[2] 5
     */
    ExpandLogNotMatchingBase,

    /**
     * Simplify logarithm where the base and argument can be rewritten as powers with same exponent
     */
    SimplifyLogWithMatchingPowers,

    ;

    override val category = "Logs"
}

typealias Explanation = LogsExplanation
