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

data class ResourceData(
    val curriculum: Curriculum? = null,
    val gmFriendly: Boolean? = null,
    val preferDecimals: Boolean? = null,
)

val emptyResourceData = ResourceData()

interface Resource {
    val resourceData: ResourceData
}

private const val BEST_MATCH = 1.0
private const val DEFAULT_MATCH = 0.5
private const val WORST_MATCH = 0.1

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
    val curriculum: Curriculum? = null,
    /** GM stands for Graspable Math. `gmFriendly` set to `true` will yield math steps that
     * are like what you would do if you were doing your work on graspablemath.com. */
    val gmFriendly: Boolean = false,
    private val precision: Int? = null, // decimal places
    val preferDecimals: Boolean? = null,
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

    private fun rateResourceData(resourceData: ResourceData): Double {
        // `gmFriendly` can only be `true` or `false`. It can't be `null` like the other
        // settings can.
        val gmFriendlyRating = if (resourceData.gmFriendly == gmFriendly) DEFAULT_MATCH else WORST_MATCH

        val decimalRating = when (resourceData.preferDecimals) {
            null -> DEFAULT_MATCH
            preferDecimals -> BEST_MATCH
            else -> WORST_MATCH
        }

        val curriculumRating = when (resourceData.curriculum) {
            null -> DEFAULT_MATCH
            curriculum -> BEST_MATCH
            else -> WORST_MATCH
        }

        @Suppress("MagicNumber")
        // We multiply the `gmFriendlyRating` by `1000` so that it trumps the other
        // ratings, and so that, when we look at the score returned by this function, we
        // can see what the `gmFriendlyRating` was by looking at the thousands digit of
        // the score.
        return 1000 * gmFriendlyRating + decimalRating * curriculumRating
    }

    private fun rateResource(resource: Resource): Double {
        return rateResourceData(resource.resourceData)
    }

    fun <T : Resource> selectBestResource(default: T, alternatives: List<T>): T {
        var bestResource = default
        var bestScore = rateResource(default)
        for (alt in alternatives) {
            val score = rateResource(alt)
            if (score > bestScore) {
                bestResource = alt
                bestScore = score
            }
        }
        return bestResource
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
