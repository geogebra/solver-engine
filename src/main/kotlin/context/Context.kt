package context

data class ResourceData(val curriculum: String?)

interface Resource {
    val resourceData: ResourceData
}

data class Context(val curriculum: String? = null) {

    fun rateResourceData(resourceData: ResourceData): Double {
        return when (curriculum) {
            resourceData.curriculum -> 1.0
            null -> 0.5
            else -> 0.0
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
