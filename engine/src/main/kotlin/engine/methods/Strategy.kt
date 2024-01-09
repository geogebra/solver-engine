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

import engine.methods.stepsproducers.StepsProducer
import engine.steps.metadata.MetadataKey

/**
 * Strategies represent different ways a given problem can be solved. Specific strategies can be selected using
 * context settings. When no strategy is specified, or the selected one doesn't apply, the one with the highest
 * priority will be selected.
 */
interface Strategy {
    val family: StrategyFamily
    val priority: Int
    val name: String
    val explanation: MetadataKey
    val steps: StepsProducer

    fun isIncompatibleWith(other: Strategy): Boolean {
        return other.family != family
    }

    companion object {
        const val MAX_PRIORITY = Int.MAX_VALUE
    }
}

interface StrategyFamily

@Target(AnnotationTarget.FIELD)
annotation class PublicStrategy
