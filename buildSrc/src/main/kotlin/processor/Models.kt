package processor

data class TranslationData(
    val defaultTranslation: String?,
    val parameters: List<String>?,
)

data class MethodData(
    val visibility: String,
    val description: String,
    val implementationName: String,
)

data class CategoryMetadata(
    val name: String,
    val title: String,
)

data class Category(
    val metadata: CategoryMetadata,
    val explanations: Map<String, TranslationData>,
    val methods: Map<String, MethodData>,
)
