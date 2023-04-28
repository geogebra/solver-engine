package engine.expressions

class IncompatiblePathMappingsException : Exception()

data class PathMapping(
    val fromPaths: List<Pair<Path, PathScope>>,
    val type: PathMappingType,
    val toPaths: List<Pair<Path, PathScope>>,
) {
    fun relativeTo(fromRoot: Path = RootPath(), toRoot: Path = RootPath()): PathMapping {
        return PathMapping(
            fromPaths.map { Pair(it.first.relativeTo(fromRoot), it.second) },
            type,
            toPaths.map { Pair(it.first.relativeTo(toRoot), it.second) },
        )
    }

    private fun mergeableWith(other: PathMapping): Boolean {
        val isMergeableType = type == PathMappingType.Distribute ||
            (type == PathMappingType.Introduce && fromPaths.isNotEmpty())

        return isMergeableType && type == other.type && fromPaths == other.fromPaths
    }

    fun mergeWith(other: PathMapping): PathMapping? {
        if (this == other) {
            return this
        }
        if (mergeableWith(other)) {
            return copy(toPaths = toPaths.union(other.toPaths).toList())
        }
        return null
    }
}

enum class PathMappingType {
    /**
     * Used when a subexpression of a sum or product shifts
     * to the left or right because some terms to its left
     * have been combined or expanded, e.g. the `x` in
     * `1 + 2 + x -> 3 + x`.
     */
    Shift,

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

/**
 * Path scope helps in further specifying the node(s) or decorators
 * of the path being referred to by the path mapping.
 *
 * Specifying the path scope of each of toPaths in a path mapping,
 * the default being [Expression], meaning we are referring to the whole
 * expression being referred to by "toPaths" (without scope),
 *
 * [Operator] path scope refers to specifying of only the current node
 * (operator node), being pointed to by the path mapping. For e.g. in the expression
 * `1 + 2 + 3 + 4` (n-ary operator) , a path and path-scope `.:op` would refer to
 * the root "+" node (i.e. all the three "+" signs in LaTeX representation).
 * While in case of unary operator, for e.g. in the expression 1 + (-2) + 3,
 * a path and path-scope of `./1:op` would only refer to the unary negative sign
 * next to "2" (and not the whole expression `-2` unlike the path and path-scope`./1`).
 *
 * [Decorator] path scope refers to the decorator (if any) of the current
 * path mapping specified
 *
 * [OuterOperator] path scope refers to the parent operator of the current
 * path mapping specified. This is useful in case of an n-ary operator having
 * more than two children, and we need to refer to the operator not associated
 * with all operands in the tree. For e.g. in expression: `1 + 2 + x + 0 + y`
 * to refer to only the left "+" in front of "x", can be done using [OuterOperator]
 * path-scope. When we refer to [OuterOperator] of "1" in the expression,
 * it would be nothing.
 * Also in case of unary operator, when [OuterOperator] refers to the
 * one and only parent operator, for e.g. in the expression: `1 : 6`
 * a path mapping with path and path-scope `./1/0:outerOp` would refer to the division
 * unary operator (though the same can be done by the path and path-scope `./1:op`).
 *
 * Please also see [engine.steps.metadata.GmPathModifier] for something similar
 */
enum class PathScope {
    Expression {
        override fun toString() = ""
    },
    Operator {
        override fun toString() = "op"
    },
    Decorator {
        override fun toString() = "decorator"
    },
    OuterOperator {
        override fun toString() = "outerOp"
    },

    ;

    companion object {
        val default = Expression

        fun fromString(stringValue: String): PathScope {
            return values().find { it.toString() == stringValue } ?: default
        }
    }
}

fun mergePathMappings(pathMappings: Sequence<PathMapping>): List<PathMapping> {
    val mergedMappings = mutableListOf<PathMapping>()
    loop@ for (newMapping in pathMappings) {
        for ((i, existingMapping) in mergedMappings.withIndex()) {
            val mergedMapping = existingMapping.mergeWith(newMapping)
            if (mergedMapping != null) {
                mergedMappings[i] = mergedMapping
                continue@loop
            }
        }
        mergedMappings.add(newMapping)
    }
    return mergedMappings
}
