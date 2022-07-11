package engine.methods

interface MethodId {

    val category: String
    val key get() = "$category.$this"
}

class MethodRegistry {

    data class EntryData(
        val methodId: MethodId,
        val isPublic: Boolean,
        val description: String,
    )

    private var methods = HashMap<MethodId, Pair<EntryData, Method>>()

    fun registerEntry(entryData: EntryData, method: Method) {
        methods[entryData.methodId] = Pair(entryData, method)
    }

    fun registerEntry(
        entryData: EntryData,
        default: ContextSensitiveMethod,
        vararg alternatives: ContextSensitiveMethod
    ) {
        registerEntry(entryData, ContextSensitiveMethodSelector(default, listOf(default) + alternatives.asList()))
    }

    fun getMethodById(methodId: MethodId): Method? {
        return methods[methodId]?.second
    }

    fun getMethodByName(methodIdString: String): Method? {
        return methods.entries.find { (id, _) -> id.key == methodIdString }?.value?.second
    }

    fun getPublicEntries() = methods.values.filter { it.first.isPublic }
}
