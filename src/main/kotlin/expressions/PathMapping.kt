package expressions

data class PathMapping(
    val fromPaths: List<Path>,
    val type: PathMappingType,
    val toPath: Path,
)

enum class PathMappingType {
    Move,
    Relate,
    Combine,
    Factor,
    Distribute,
    Cancel,
    Introduce;
}
