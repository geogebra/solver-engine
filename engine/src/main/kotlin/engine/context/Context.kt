package engine.context

data class ResourceData(val curriculum: String?)

val emptyResourceData = ResourceData(null)

interface Resource {
    val resourceData: ResourceData
}

private const val BEST_MATCH = 1.0
private const val DEFAULT_MATCH = 0.5
private const val WORST_MATCH = 0.0

data class Context(val curriculum: String? = null) {

    fun rateResourceData(resourceData: ResourceData): Double {
        return when (curriculum) {
            resourceData.curriculum -> BEST_MATCH
            null -> DEFAULT_MATCH
            else -> WORST_MATCH
        }
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
