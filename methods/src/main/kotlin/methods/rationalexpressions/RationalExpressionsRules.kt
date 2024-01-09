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

package methods.rationalexpressions

import engine.expressions.Fraction
import engine.expressions.divideBy
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.condition
import engine.patterns.divideBy
import engine.patterns.productOf
import engine.patterns.sumContaining
import engine.steps.metadata.metadata

enum class RationalExpressionsRules(override val runner: Rule) : RunnerMethod {
    DistributeDivisionOverSum(
        rule {
            val dividend = sumContaining()
            val divisor = condition { it !is Fraction }

            onPattern(productOf(dividend, divideBy(divisor))) {
                ruleResult(
                    toExpr = sumOf(get(dividend).children.map { productOf(move(it), divideBy(get(divisor))) }),
                    explanation = metadata(Explanation.DistributeDivisionOverSum),
                )
            }
        },
    ),
}
