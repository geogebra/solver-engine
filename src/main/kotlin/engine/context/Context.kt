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

    fun <T : Resource> sortResources(resources: Sequence<T>): Sequence<T> {
        return resources.sortedByDescending { rateResource(it) }.filter { rateResource(it) > 0 }
    }

    fun <T : Resource> selectBestResource(resources: Sequence<T>): T? {
        return resources.filter { rateResource(it) > 0 }.maxByOrNull { rateResource(it) }
    }
}

val emptyContext = Context()
