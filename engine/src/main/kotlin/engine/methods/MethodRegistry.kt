package engine.methods

interface MethodId {
    val category: String
    val key get() = "$category.$this"
}

data class SimpleMethodId(override val category: String, val name: String) : MethodId {
    override val key: String
        get() = "$category.$name"
}

class MethodRegistry {

    data class EntryData(
        val methodId: MethodId,
        val isPublic: Boolean,
        val description: String,
        val implementation: Method
    )

    private var methods = HashMap<MethodId, EntryData>()

    fun registerEntry(entryData: EntryData) {
        methods[entryData.methodId] = entryData
    }

    fun getMethodById(methodId: MethodId): Method? {
        return methods[methodId]?.implementation
    }

    fun getMethodByName(methodIdString: String): Method? {
        return methods.entries.find { (id, _) -> id.key == methodIdString }?.value?.implementation
    }

    fun getPublicEntries() = methods.values.filter { it.isPublic }
}
