package engine.context

import engine.logger.createNewLogger
import java.util.function.Supplier
import java.util.logging.Level

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

private const val DEFAULT_PRECISION = 3 // 3 decimal places

data class Context(
    val curriculum: Curriculum? = null,
    /** GM stands for Graspable Math. `gmFriendly` set to `true` will yield math steps that
     * are like what you would do if you were doing your work on graspablemath.com. */
    val gmFriendly: Boolean = false,
    val precision: Int? = null, // decimal places
    val preferDecimals: Boolean? = null,
    val solutionVariable: String? = null,
) {
    val effectivePrecision = precision ?: DEFAULT_PRECISION

    private val logger = createNewLogger(this)

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

    fun log(level: Level, string: String) {
        logger.log(level, string)
    }

    fun log(level: Level, stringSupplier: Supplier<String>) {
        logger.log(level, stringSupplier)
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
}

val emptyContext = Context()
