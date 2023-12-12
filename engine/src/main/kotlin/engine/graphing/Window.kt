package engine.graphing

import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.expressions.FiniteSet
import engine.expressions.SetSolution
import kotlin.math.max
import kotlin.math.min

/**
 * A window is a view into the 2D xy-plane bound by x0 <= x <= x1 and y0 <= y <= y1
 */
data class Window(val x0: Double, val x1: Double, val y0: Double, val y1: Double) {
    val height = y1 - y0
    val width = x1 - x0

    fun isVisibleX(x: Double) = x0 <= x && x <= x1
    fun isVisibleY(y: Double) = y0 <= y && y <= y1

    fun containingPoints(points: List<Point>, padding: Double = 0.5): Window {
        var x0 = x0
        var x1 = x1
        var y0 = y0
        var y1 = y1
        for ((x, y) in points) {
            x0 = min(x0, x - padding)
            x1 = max(x1, x + padding)
            y0 = min(y0, y - padding)
            y1 = max(y1, y + padding)
        }
        return Window(x0, x1, y0, y1)
    }

    fun join(w: Window): Window {
        return Window(min(x0, w.x0), max(x1, w.x1), min(y0, w.y0), max(y1, w.y1))
    }

    fun bestSquareFit(): Window {
        return when {
            width > height -> {
                val padding = (width - height) / 2
                Window(x0, x1, y0 - padding, y1 + padding)
            }
            height > width -> {
                val padding = (height - width) / 2
                Window(x0 - padding, x1 + padding, y0, y1)
            }
            else -> this
        }
    }

    /**
     * Swap the x and y axes
     */
    fun transpose(): Window {
        return Window(y0, y1, x0, x1)
    }

    /**
     * Pad the window, keeping the center of the window unchanged
     */
    fun withPadding(paddingFactor: Double): Window {
        val xPadding = width * paddingFactor
        val yPadding = height * paddingFactor
        return Window(x0 - xPadding, x1 + xPadding, y0 - yPadding, y1 + yPadding)
    }
}

private const val DEFAULT_WINDOW_AMPLITUDE = 5.0
private val defaultWindow = Window(
    -DEFAULT_WINDOW_AMPLITUDE,
    DEFAULT_WINDOW_AMPLITUDE,
    -DEFAULT_WINDOW_AMPLITUDE,
    DEFAULT_WINDOW_AMPLITUDE,
)

/**
 * Try to find a suitable window for graphing the expressions [exprs].  The returned window will contain [window]
 * The function may attempt to rearrange a formula using the [solveForVariable] function.
 */
fun bestWindowForExprs(
    exprs: List<Expression>,
    axisVariables: AxisVariables,
    solveForVariable: (expr: Expression, variable: String) -> Expression?,
    window: Window = defaultWindow,
): Window {
    return exprs.fold(window) { w, expr -> w.join(bestWindowForExpr(expr, axisVariables, solveForVariable, window)) }
}

/**
 * Try to find a suitable window for graphing the expression [expr].  The returned window will contain [window]
 * The function may attempt to rearrange a formula using the [solveForVariable] function.
 */
@Suppress("ReturnCount")
fun bestWindowForExpr(
    expr: Expression,
    axisVariables: AxisVariables,
    solveForVariable: (expr: Expression, variable: String) -> Expression?,
    window: Window = defaultWindow,
): Window {
    // Try expr as y = f(x)
    val fx = getFunction(expr, axisVariables.horizontal)
    if (fx != null) {
        return fx.bestWindow(window)
    }
    // Try expr as x = f(y)
    val fy = getFunction(expr, axisVariables.vertical)
    if (fy != null) {
        return fy.bestWindow(defaultWindow.transpose()).transpose()
    }
    // Try to rearrange expr as y = f(x)
    val solutionY = solveForVariable(expr, axisVariables.vertical)
    if (solutionY != null) {
        val solutions = extractSolutionsFromResult(solutionY)
        if (solutions != null) {
            return bestWindowForExprs(solutions, axisVariables, solveForVariable, window)
        }
    }
    // Try to rearrange expr as x = f(y)
    val solutionX = solveForVariable(expr, axisVariables.horizontal)
    if (solutionX != null) {
        val solutions = extractSolutionsFromResult(solutionX)
        if (solutions != null) {
            return bestWindowForExprs(solutions, axisVariables, solveForVariable, window)
        }
    }
    // Nothing works; return the default window.
    return window
}

private fun extractSolutionsFromResult(expr: Expression): List<Expression>? {
    val simpleExpr = if (expr is ExpressionWithConstraint) expr.expression else expr
    if (simpleExpr !is SetSolution) {
        return null
    }
    val set = simpleExpr.solutionSet
    return if (set is FiniteSet) {
        set.elements
    } else {
        null
    }
}
