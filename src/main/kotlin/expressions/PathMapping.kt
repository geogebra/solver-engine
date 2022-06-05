package expressions

data class PathMapping(
    val fromPaths: List<Path>,
    val type: PathMappingType,
    val toPaths: List<Path>,
) {
    fun relativeTo(fromRoot: Path = RootPath, toRoot: Path = RootPath): PathMapping {
        return PathMapping(fromPaths.map { it.relativeTo(fromRoot) }, type, toPaths.map { it.relativeTo(toRoot) })
    }
}

enum class PathMappingType {
    /**
     * Used when an expression stays the same, but moves to a
     * different position in the resulting expression, e.g.
     * `-3x + 2 + x^2 -> x^2 - 3x + 2` or the `x` and the `3`
     * in `[x / 4] - [3 / 4] -> [x - 3 / 4]`.
     */
    Move,

    /**
     * Used when an expression that appeared at least twice in the
     * original expression appears once in the result, e.g.
     * `ab - ac -> a(b - c)` or `[x / 4] - [3 / 4] -> [x - 3 / 4]`.
     */
    Factor,

    /**
     * Used when an expression that appeared once in the original
     * expression appears at least twice in the result, e.g.
     * `a(b - c) -> ab - ac` or `[x - 3 / 4] -> [x / 4] - [3 / 4]`.
     */
    Distribute,

    /**
     * Used when a completely new expression is introduced, e.g.
     * the `1` in `3 + [2 / 5] -> [3 / 1] + [2 / 5]`.
     */
    Introduce,

    /**
     * Used when two or more expressions cancel with each other,
     * e.g. `a + b - b -> a` or `[ab / ac] -> [b / c]`.
     */
    Cancel,

    /**
     * Used when a single subexpression changes into another
     * independently of others, e.g. `3 * 4 * 0 * 5 -> 0`
     * or `d/dx sin(x) -> cos(x)`.
     */
    Transform,

    /**
     * Used when a new expression is introduced, which results
     * from the combination of two values in the original, e.g.
     * in `[1 / 4] + [1 / 6] -> [1 * 3 / 4 * 3] + [1 * 2 / 6 * 2]`
     * the `2` and the `3` result from the combination of the `4`
     * and the `6`. It is also used when the combined expression
     * replaces the original, e.g. in `x + 3 - 2 -> x + 1`
     * `3` and `2` are combined into `1`.
     */
    Combine,

    /**
     * Used when the original and the resulting expression are
     * in an indirect and not well defined relationship. Only
     * occurs when combining path mappings. E.g. in the derivation
     * `5(x + y) - 3x -> 5x + 5y - 3x -> (5 - 3)x + 5y -> 2x + 5y`
     * the `5` and `3` in the original expression are related
     * to the `2` in the result.
     */
    Relate,
}
