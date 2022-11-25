package engine.methods

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
            buildPublicEntries(moreSpecificMethods)
        )
    }

    /**
     * The following build publicEntries in such a way that more specific entries (according to the
     * [moreSpecificMethods] map) come first
     */
    private fun buildPublicEntries(moreSpecificMethods: Map<MethodId, List<MethodId>>): List<MethodRegistry.EntryData> {
        val remaining = registeredEntries.filter { it.isPublic }.toMutableSet()
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
        return registeredEntries.filter { it.isPublic }
            .associateBy({ it.methodId }, { calculateMoreSpecificMethods(it) })
    }

    private fun calculateMoreSpecificMethods(entry: MethodRegistry.EntryData): List<MethodId> {
        val method = entry.implementation
        if (method is RunnerMethod) {
            val runner = method.runner
            if (runner is Plan) {
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
    val publicEntries: List<EntryData>
) {

    data class EntryData(
        val methodId: MethodId,
        val isPublic: Boolean,
        val description: String,
        val implementation: Method
    )

    /**
     * Get list of method IDs of methods more specific than [methodId]
     */
    fun getMoreSpecificMethods(methodId: MethodId) = moreSpecificMethods[methodId] ?: emptyList()

    /**
     * Find the implementation of the method whose method ID has string representation [name]
     */
    fun getMethodByName(name: String): Method? {
        val methodId = methodIdFromName(name) ?: return null
        return entries[methodId]?.implementation
    }
}

data class MethodId(val category: String, val name: String) {

    override fun toString() = "$category.$name"
}

private fun methodIdFromName(name: String): MethodId? {
    val parts = name.split(".", limit = 2)
    return if (parts.size == 2) MethodId(parts[0], parts[1]) else null
}
