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

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.RootOrigin
import engine.steps.Transformation
import java.util.logging.Level

/**
 * Builder class for creating a [MethodRegistry] instance.
 */
class MethodRegistryBuilder {
    private var registeredEntries = mutableListOf<MethodRegistry.EntryData>()

    /**
     * Register an individual registry entry.
     */
    fun registerEntry(entryData: MethodRegistry.EntryData) {
        registeredEntries.add(entryData)
    }

    /**
     * Call this to get a [MethodRegistry] once all the entries have been registered via [registerEntry]
     */
    fun buildRegistry(): MethodRegistry {
        val moreSpecificMethods = buildMoreSpecificMethodsMap()

        return MethodRegistry(
            registeredEntries.associateBy({ it.methodId }, { it }),
            moreSpecificMethods,
            buildPublicEntries(moreSpecificMethods),
        )
    }

    /**
     * The following build publicEntries in such a way that more specific entries (according to the
     * [moreSpecificMethods] map) come first
     */
    private fun buildPublicEntries(moreSpecificMethods: Map<MethodId, List<MethodId>>): List<MethodRegistry.EntryData> {
        val remaining = registeredEntries.toMutableSet()
        val addedIds = mutableSetOf<MethodId>()
        val publicEntries = mutableListOf<MethodRegistry.EntryData>()
        repeat(remaining.size) {
            for (entry in remaining) {
                if (moreSpecificMethods[entry.methodId]!!.all { it in addedIds }) {
                    publicEntries.add(entry)
                    addedIds.add(entry.methodId)
                    remaining.remove(entry)
                    break
                }
            }
        }
        require(remaining.isEmpty())
        return publicEntries
    }

    private fun buildMoreSpecificMethodsMap(): Map<MethodId, List<MethodId>> {
        return registeredEntries.associateBy({ it.methodId }, { calculateMoreSpecificMethods(it) })
    }

    private fun calculateMoreSpecificMethods(entry: MethodRegistry.EntryData): List<MethodId> {
        val method = entry.implementation
        if (method is RunnerMethod) {
            val runner = method.runner
            if (runner is CompositeMethod) {
                return runner.specificPlans.mapNotNull { findMethodId(it)?.methodId }
            }
        }
        return emptyList()
    }

    private fun findMethodId(method: Method): MethodRegistry.EntryData? {
        return registeredEntries.firstOrNull { it.implementation === method }
    }
}

/**
 * Contains all publicly available methods and provides ways to access them.  It is immutable thus safe for concurrent
 * use.
 */
class MethodRegistry internal constructor(
    private val entries: Map<MethodId, EntryData>,
    private val moreSpecificMethods: Map<MethodId, List<MethodId>>,
    /**
     * List of public method entries, guaranteed to be ordered such that more specific methods come first.
     */
    private val sortedEntries: List<EntryData>,
) {
    data class EntryData(
        val methodId: MethodId,
        val hiddenFromList: Boolean,
        val description: String,
        val implementation: Method,
    )

    val listedEntries = sortedEntries.filter { !it.hiddenFromList }

    /**
     * Get list of method IDs of methods more specific than [methodId]
     */
    private fun getMoreSpecificMethods(methodId: MethodId) = moreSpecificMethods[methodId] ?: emptyList()

    /**
     * Find the implementation of the method whose method ID has string representation [name]
     */
    fun getMethodByName(name: String): Method? {
        val methodId = methodIdFromName(name) ?: return null
        return entries[methodId]?.implementation
    }

    fun methodIsNotListed(methodId: MethodId): Boolean {
        return entries[methodId]?.hiddenFromList ?: false
    }

    @Suppress("TooGenericExceptionCaught")
    fun selectSuccessfulPlansMethodIdAndTransformation(
        expr: Expression,
        context: Context,
    ): List<Pair<MethodId, Transformation>> {
        val successfulPlansIds = mutableSetOf<MethodId>()
        val selections = mutableListOf<Pair<MethodId, Transformation>>()

        for (entryData in sortedEntries) {
            if (this.getMoreSpecificMethods(entryData.methodId).any { it in successfulPlansIds }) {
                successfulPlansIds.add(entryData.methodId)
                context.log(Level.FINE, "Skipping plan ID: ${entryData.methodId}")
                continue
            }
            val transformation = try {
                entryData.implementation.tryExecute(context, expr.withOrigin(RootOrigin()))
            } catch (e: Exception) {
                context.log(Level.SEVERE, "Exception caught: ${e.stackTraceToString()}")
                null
            }
            transformation?.let {
                context.log(Level.FINE, "Success for plan ID: ${entryData.methodId}")
                successfulPlansIds.add(entryData.methodId)
                selections.add(entryData.methodId to transformation)
            } ?: run {
                context.log(Level.FINE, "Failure for plan ID: ${entryData.methodId}")
            }
        }

        return selections
    }
}

data class MethodId(val category: String, val name: String) {
    override fun toString() = "$category.$name"
}

private fun methodIdFromName(name: String): MethodId? {
    val parts = name.split(".", limit = 2)
    return if (parts.size == 2) MethodId(parts[0], parts[1]) else null
}
