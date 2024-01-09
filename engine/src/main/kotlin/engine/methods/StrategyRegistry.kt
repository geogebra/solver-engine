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

import kotlin.reflect.KClass

class StrategyRegistry {
    private val entries: MutableMap<String, List<EntryData>> = mutableMapOf()

    data class EntryData(
        val category: KClass<out Strategy>,
        val strategy: Strategy,
        val description: String,
    )

    data class StrategyData(
        val strategy: String,
        val description: String,
    )

    fun addEntry(categoryName: String, entry: EntryData) {
        entries.merge(categoryName, listOf(entry)) { a, b -> a + b }
    }

    fun getStrategyChoice(category: String, strategyName: String): Pair<KClass<out Strategy>, Strategy>? {
        val entry = entries[category]?.firstOrNull { it.strategy.name == strategyName } ?: return null
        return entry.category to entry.strategy
    }

    fun listStrategies(): Map<String, List<StrategyData>> {
        return entries.mapValues { (_, entries) -> entries.map { StrategyData(it.strategy.name, it.description) } }
    }
}
