package engine.steps.metadata

import kotlin.reflect.KClass

interface ExplanationBase : MetadataKey {
    val category: String
    override val key get() = "$category.$this"
    // val defaultTranslation: String
    // val parameters: List<String>
}

fun getExplanations(): List<KClass<out ExplanationBase>> {
    return ExplanationBase::class.sealedSubclasses
}
