package engine.methods

import kotlin.reflect.KClass

class StrategyRegistry {
    private val entries: MutableMap<String, List<EntryData>> = mutableMapOf()

    data class EntryData(
        val category: KClass<out Strategy>,
        val strategy: Strategy,
        val description: String,
    )

    data class StrategyData(
        val strategy: String,
        val description: String,
    )

    fun addEntry(categoryName: String, entry: EntryData) {
        entries.merge(categoryName, listOf(entry)) { a, b -> a + b }
    }

    fun getStrategyChoice(category: String, strategyName: String): Pair<KClass<out Strategy>, Strategy>? {
        val entry = entries[category]?.firstOrNull { it.strategy.name == strategyName } ?: return null
        return entry.category to entry.strategy
    }

    fun listStrategies(): Map<String, List<StrategyData>> {
        return entries.mapValues { (_, entries) -> entries.map { StrategyData(it.strategy.name, it.description) } }
    }
}
