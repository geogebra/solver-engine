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

package engine.context

import engine.expressions.Child
import engine.expressions.Expression
import engine.expressions.LabelSpace
import engine.logger.DefaultLogger
import engine.logger.Logger
import engine.methods.Method
import engine.methods.Strategy
import engine.operators.Operator
import java.util.function.Supplier
import java.util.logging.Level
import kotlin.reflect.KClass

// precisions in decimal places
private const val MINIMUM_PRECISION = 2
private const val DEFAULT_PRECISION = 3
private const val MAXIMUM_PRECISION = 10

enum class StrategySelectionMode {
    ALL,
    HIGHEST_PRIORITY,
    FIRST,
}

data class Context(
    val settings: Map<Setting, SettingValue> = emptyMap(),
    // decimal places
    private val precision: Int? = null,
    val solutionVariables: List<String> = emptyList(),
    private val logger: Logger = DefaultLogger,
    val preferredStrategies: Map<KClass<out Strategy>, Strategy> = emptyMap(),
    val strategySelectionMode: StrategySelectionMode = StrategySelectionMode.ALL,
    val labelSpace: LabelSpace? = null,
    val constraintMerger: Method? = null,
) {
    val effectivePrecision = (precision ?: DEFAULT_PRECISION).coerceIn(MINIMUM_PRECISION, MAXIMUM_PRECISION)

    private var nestingDepth = 0

    fun nest() {
        nestingDepth++
    }

    fun unnest() {
        nestingDepth--
    }

    /**
     * Checks whether the computation was interrupted from the outside and throws an exception
     * if so. Should be called during more complex computations to allow for termination.
     * Currently, it checks whether the thread was interrupted by a timeout.
     */
    fun requireActive() {
        if (Thread.currentThread().isInterrupted) {
            throw InterruptedException("Computation thread interrupted (probably by timeout)")
        }
    }

    fun addPreset(preset: Preset): Context {
        return copy(settings = settings + preset.settings)
    }

    fun addSettings(settings: Map<Setting, SettingValue>) =
        if (settings.isEmpty()) {
            this
        } else {
            copy(settings = this.settings + settings)
        }

    fun get(flag: Setting): SettingValue {
        return settings.getOrDefault(flag, flag.kind.default)
    }

    fun isSet(flag: Setting): Boolean {
        assert(flag.kind == BooleanSetting)
        return settings.getOrDefault(flag, flag.kind.default) == BooleanSetting.True
    }

    inline fun <reified T : Strategy> preferredStrategy(): Strategy? {
        return preferredStrategies[T::class]
    }

    fun log(level: Level, string: String) {
        logger.log(level, nestingDepth, string)
    }

    fun <T> log(level: Level, supplier: Supplier<T>) {
        logger.log(level, nestingDepth, supplier)
    }

    internal data class CacheKey(
        val expression: Expression,
        val plan: Any,
        // These fields are for StickOptionalNeg pattern to work, and also rules that check the existence of a parent
        val parentOperator: Operator?,
        val parentIndex: Int?,
    )

    private val outcomeCache: MutableMap<CacheKey, Boolean> = mutableMapOf()

    internal inline fun <T : Any, U : Any> unlessPreviouslyFailed(plan: T, sub: Expression, build: () -> U?): U? {
        val parent = sub.parent
        val parentChildOrigin = parent?.origin as? Child

        val cacheKey = CacheKey(sub, plan, parent?.operator, parentChildOrigin?.index)
        val cachedOutcome = outcomeCache[cacheKey]
        if (cachedOutcome == false) {
            log(Level.FINER, "CACHED FAILURE")
            return null
        }
        val result = build()
        if (cachedOutcome == null) {
            outcomeCache[cacheKey] = result != null
        }
        return result
    }
}

inline fun <reified T : Strategy> strategyChoice(choice: T): Pair<KClass<out Strategy>, Strategy> {
    return T::class to choice
}

val emptyContext = Context()

fun emptyContextWithLabels() = Context(labelSpace = LabelSpace())
