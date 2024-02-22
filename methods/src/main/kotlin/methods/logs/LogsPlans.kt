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

import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan

enum class LogsPlans(override val runner: CompositeMethod) : RunnerMethod {
    SimplifyLogOfKnownPower(simplifyLogOfKnownPower),
}

private val simplifyLogOfKnownPower = plan {
    explanation = Explanation.SimplifyLogOfKnownPower

    steps {
        apply(LogsRules.RewriteLogOfKnownPower)
        apply(LogsRules.TakePowerOutOfLog)
    }
}
