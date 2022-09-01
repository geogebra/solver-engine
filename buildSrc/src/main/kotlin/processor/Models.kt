package processor

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
    val methods: Map<String, MethodData>,
)
