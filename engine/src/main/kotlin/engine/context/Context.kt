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
    private val precision: Int? = null, // decimal places
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

    fun isSet(flag: Setting, value: SettingValue = BooleanSetting.True): Boolean {
        assert(flag.kind == BooleanSetting)
        return settings.getOrDefault(flag, flag.kind.default) == value
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
