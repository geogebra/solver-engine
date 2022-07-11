package processor

data class TranslationData(
    val defaultTranslation: String?,
    val parameters: List<String>?,
)

data class MethodVariant(
    val implementationName: String,
    val level: Int?,
    val region: String?,
)

data class MethodData(
    val visibility: String,
    val description: String,
    val defaultVariant: MethodVariant,
    val variants: List<MethodVariant>,
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
