package engine.steps.metadata

import engine.expressions.Expression

/**
 * This class represents a Graspable Math (GM) action that can be performed by
 * the user to mimic a solver transformation. It describes the type of the
 * interaction, and a list of expressions that the user should interact with
 * (usually one, except for DragCollect (>1) or NotSupported (0)). If the type of
 * the action is Drag or DragCollect, then the action also contains information
 * about where to drag the expressions. If the type of the action is Formula, a
 * GM formulaId is provided.
 *
 * PathModifiers can be provided for the expressions and target expression to
 * refer to parts of the expressions that can't be expressed with solver path
 * notation.
 */
data class GmAction(
    val type: GmActionType,
    val expressions: List<Expression> = listOf(),
    val pathModifier: GmPathModifier? = null,
    val dragTo: GmDragToInfo? = null,
    val formulaId: String? = null,
) {
    fun expressionsAsPathStrings(): List<String> {
        return expressions.map { serializeExpression(it, pathModifier) }
    }

    fun dragToExpressionAsPathString(): String? {
        if (dragTo == null) return null
        return serializeExpression(dragTo.expression, dragTo.pathModifier)
    }

    companion object {
        fun serializeExpression(expr: Expression, modifier: GmPathModifier? = null): String {
            val pathMapping = expr.mergedPathMappings().firstOrNull()
            if (pathMapping === null) {
                // This should never happen when used correctly. If it does, it means that
                // the gmAction was created with a pattern that didn't get bound to an
                // expression, such as the minus in an opional negative pattern.
                throw NoPathException("Trying to turn an unbound pattern into an expression path.")
            }
            val path = pathMapping.fromPaths.firstOrNull()
            if (path === null) {
                // This should never happen when used correctly. If it does, it means that
                // a gmAction was created with a pattern that didn't exist in the
                // fromExpr, such as the 0 in x-x → 0. Since gmActions describe the user's
                // actions on the fromExpr, this is not allowed.
                throw NoPathException("Trying to turn an expression that doesn't exist on the fromExpr into an expression path.")
            }
            return path.toString() +
                if (modifier != null) {
                    ":${modifier.value}"
                } else {
                    ""
                }
        }
    }
}

class NoPathException(message: String) : Exception(message)

/** Graspable Math (GM) */
data class GmDragToInfo(
    val expression: Expression,
    val position: DragTargetPosition = DragTargetPosition.Onto,
    val pathModifier: GmPathModifier? = null,
)

/**
 * Where to drag the expression relative to dragTo.expression in Graspable Math
 * (GM) to trigger the transformation. One way to figure out which relative
 * position is used by GM, preform the action on a GM canvas and look at the
 * serialized JSON of the canvas.
 */
enum class DragTargetPosition {
    Onto, Above, Below, LeftOf, RightOf,

    /**
     Drag an expression far enough away from the dragTo expression. Only used in very few
     actions, such as factoring out exponents of powers with matching bases.
     */
    OutsideOf,
}

/** Path modifiers for expressions and `dragTo.expression` allow referring to parts
 * of the expressions that can't be expressed with solver path notation. E.g.,
 * the `+` in `1+2`, or the opening brackets in `2-(-3)`. */
enum class GmPathModifier(val value: String) {
    /** select '(' in *(-2) */
    OpenParens("("),

    /** select ')' in *(-2) */
    CloseParens(")"),

    /** select '(-2)' in *(-2) */
    Parens("()"),

    /** select '-' in *(-2) */
    Operator("op"),

    /** select '*' in *(-2) */
    OuterOperator("op()"),

    /** select the invisible '2' in ```sqrt[x]```
     */
    RootIndex("idx"),
}

/** Describes the type of interaction to trigger a specific transformation in
 Graspable Math. */
enum class GmActionType {
    /** Tap/click on the expression. */
    Tap,

    /** Double tap/double tap on the expression. */
    DoubleTap,

    /** Tap/click and hold on the expression. */
    TapHold,

    /** Drag the expression(s) onto the dragTo.expression */
    Drag,

    /** Drag the first term in expression(s) over the other terms in
     * expression(s) to collect them, then drag them onto dragTo.expression. */
    DragCollect,

    /** Apply a GM formula to the expression(s). */
    Formula,

    /** Edit the expression(s) via keyboard entry in GM. This can be a
     * fallback action when there is no gesture or formula available in GM. */
    Edit,

    /** GM does not support the kind of math used in this transformation. */
    NotSupported,
}
