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

import engine.expressions.Constants.Pi
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.patterns.ConstantPattern
import engine.patterns.FixedPattern
import engine.patterns.degreeOf
import engine.patterns.withOptionalRationalCoefficient
import methods.constantexpressions.ConstantExpressionsPlans

enum class AnglesPlans(override val runner: CompositeMethod) : RunnerMethod {
    @PublicMethod
    ConvertDegreesToRadians(
        plan {
            pattern = degreeOf(ConstantPattern())

            explanation = Explanation.ConvertDegreesToRadians

            steps {
                apply(AnglesRules.UseDegreeConversionFormula)
                apply(ConstantExpressionsPlans.SimplifyConstantExpression)
            }
        },
    ),

    @PublicMethod
    ConvertRadiansToDegrees(
        plan {
            pattern = withOptionalRationalCoefficient(FixedPattern(Pi), false)

            explanation = Explanation.ConvertRadiansToDegrees

            steps {
                apply(AnglesRules.UseRadianConversionFormula)
                apply(ConstantExpressionsPlans.SimplifyConstantExpression)
            }
        },
    ),
}
