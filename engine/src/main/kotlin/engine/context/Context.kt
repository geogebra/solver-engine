package engine.context

import engine.logger.createNewLogger
import java.util.function.Supplier
import java.util.logging.Level

data class ResourceData(val curriculum: Curriculum? = null, val preferDecimals: Boolean? = null)

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
    val precision: Int? = null, // decimal places
    val preferDecimals: Boolean? = null,
    val solutionVariable: String? = null
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

    fun rateResourceData(resourceData: ResourceData): Double {
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

        return decimalRating * curriculumRating
    }

    fun rateResource(resource: Resource): Double {
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
