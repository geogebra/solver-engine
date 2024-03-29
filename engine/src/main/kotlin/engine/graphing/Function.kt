/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package engine.graphing

import engine.expressions.Comparison
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.Inequality
import engine.expressions.StatementSystem
import engine.expressions.ValueExpression
import engine.expressions.Variable
import engine.expressions.equationOf
import engine.expressions.xp
import kotlin.math.absoluteValue

/**
 * This allows considering an expression in one variable as a numeric function.
 */
class Function(val f: ValueExpression, val xVar: String) {
    companion object {
        private const val RANGE_DIVISIONS = 20
        private const val EXPLORE_AMPLITUDE = 20.0
        private const val EXPLORE_DIVISIONS = 100
        private const val MAX_INTERESTING_POINTS = 6
    }

    /**
     * Return the numeric value of the function at [x].
     */
    fun evaluateAt(x: Double): Double = f.evaluate(xVar, x)

    fun evaluateAtRange(x0: Double, x1: Double, n: Int = RANGE_DIVISIONS): DoubleArray {
        val ys = DoubleArray(n)
        val xRange = x1 - x0
        for (i in 0..n) {
            ys[i] = evaluateAt(x0 + i * xRange / n)
        }
        return ys
    }

    /**
     * Looks in the range [[x0], [x1] and return a sequence of interesting points (e.g. turning points, x/y intercepts).
     */
    @Suppress("CyclomaticComplexMethod")
    fun estimateInterestingPoints(x0: Double, x1: Double, n: Int) =
        sequence {
            // The y-intercept is interesting
            if (x0 <= 0 && x1 >= 0) {
                val y0 = evaluateAt(0.0)
                if (y0.isFinite()) {
                    yield(Point(0.0, y0))
                }
            }
            var gradient = Double.NaN
            val xRange = x1 - x0
            var x = x0
            var y = evaluateAt(x)
            val dx = xRange / n
            for (i in 1..n) {
                val newX = x0 + i * xRange / n
                val newY = evaluateAt(newX)
                val newGradient = (newY - y) / dx
                when {
                    newY.isNaN() -> {
                        if (y.isFinite()) {
                            // We are leaving the domain
                            yield(Point(x, y))
                        }
                    }
                    newY.isInfinite() -> {
                        // We found an asymptote probably, so let's make sure the x-coordinate will be in the graph
                        yield(Point(newX, 0.0))
                    }
                    newY == 0.0 -> {
                        // How lucky, an x-intercept
                        yield(Point(newX, newY))
                    }
                    y.isNaN() -> {
                        // As newY is finite, we are entering the domain
                        yield(Point(newX, newY))
                    }
                    y * newY < 0 -> {
                        // There is an x-intercept or a discontinuity between x and newX.  Either way it's interesting
                        // Should work out x by interpolation...
                        yield(Point((x + newX) / 2, 0.0))
                    }
                    newGradient == 0.0 -> {
                        // This is probably a turning point, else it's an inflexion point which is interesting
                        yield(Point(newX, newY))
                    }
                    gradient * newGradient < 0.0 -> {
                        // There is a turning or a discontinuity point near x
                        if (gradient > 1.0 || newGradient > 1.0) {
                            // The gradient is big, so it is probably an asymptote
                            yield(Point(newX, 0.0))
                        } else {
                            yield(Point(x, y))
                        }
                    }
                }
                gradient = newGradient
                x = newX
                y = newY
            }
        }

    /**
     * Find a window containing all interesting points that could be found in the range [[x0], [x1]].  It will contain
     * [startWindow]
     */
    fun windowContainingInterestingPoints(
        startWindow: Window,
        x0: Double = -EXPLORE_AMPLITUDE,
        x1: Double = EXPLORE_AMPLITUDE,
        n: Int = EXPLORE_DIVISIONS,
    ): Window {
        // Find a window containing enough interesting points
        var points = estimateInterestingPoints(x0, x1, n).toList()
        if (points.size > MAX_INTERESTING_POINTS) {
            // Choose points closest to 0
            points = points.sortedBy { it.x.absoluteValue }.subList(0, MAX_INTERESTING_POINTS)
        }
        return startWindow.containingPoints(points)
    }

    /**
     * Returns the best guess of a suitable window to graph the function (containing [startWindow])
     */
    fun bestWindow(startWindow: Window) = windowContainingInterestingPoints(startWindow)
}

/**
 * Create a [Function] with variable [xVar] out of the expression [e] if it makes sense.
 * If [e] is a value expression it will be taken as the definition if the variable is compatible.  If [e] is an
 * equation / inequation / inequality, a function can be created if it is in the form y=f(x) or f(x) = y.
 */
fun getFunction(e: Expression, xVar: String): Function? {
    return when {
        e is ValueExpression && setOf(xVar).containsAll(e.variables) -> Function(e, xVar)
        e is Comparison -> {
            val lhs = e.lhs
            val rhs = e.rhs
            when {
                lhs is Variable && lhs.variableName != xVar -> getFunction(rhs, xVar)
                rhs is Variable && rhs.variableName != xVar -> getFunction(lhs, xVar)
                else -> null
            }
        }
        else -> null
    }
}

/**
 * Finds all the graphable expressions out of one expression.  E.g. an equation system is divided into individual
 * equations.
 */
fun extractGraphableExpressions(expr: Expression): Pair<List<Expression>, List<Intersection>>? {
    return when {
        expr.variables.isEmpty() -> null
        expr.variables.size > 2 -> null
        expr is StatementSystem -> if (expr.equations.all { it is Comparison && it.variables.isNotEmpty() }) {
            Pair(
                expr.equations,
                listOf(
                    Intersection(
                        objectIndexes = listOf(0, 1),
                        showLabelWithCoordinates = true,
                    ),
                ),
            )
        } else {
            null
        }
        expr is ValueExpression -> if (expr.variables.size == 1) Pair(listOf(expr), emptyList()) else null
        (expr is Equation || expr is Inequality) && expr.variables.size == 1 -> extractEquationOrInequality(expr)
        expr is Comparison -> Pair(listOf(expr), emptyList())
        else -> null
    }
}

fun extractEquationOrInequality(expr: Expression): Pair<List<Expression>, List<Intersection>>? {
    // If it's an equation with just one variable, change it so e.g. "x^2 = 2 - x" becomes two equations
    // "y = x^2" and "y = 2 - x" and say we want to show the intersection.
    val axisVariables = selectAxisVariables(expr.variables, emptyList(), listOf(expr))
    return if (axisVariables == null || axisVariables.horizontal !in expr.variables) {
        Pair(listOf(expr), emptyList())
    } else {
        val lhs = xp(axisVariables.vertical)
        val intersection = Intersection(
            objectIndexes = listOf(0, 1),
            projectOntoHorizontalAxis = true,
        )
        // This is needed because the common interface between Equation and Inequality does not
        // guarantee that lhs and rhs are present
        when {
            (expr is Equation) -> Pair(
                listOf(equationOf(lhs, expr.lhs), equationOf(lhs, expr.rhs)),
                listOf(intersection),
            )
            (expr is Inequality) -> Pair(
                listOf(equationOf(lhs, expr.lhs), equationOf(lhs, expr.rhs)),
                listOf(intersection),
            )
            else -> null
        }
    }
}

data class Intersection(
    val objectIndexes: List<Int>,
    val projectOntoHorizontalAxis: Boolean = false,
    val projectOntoVerticalAxis: Boolean = false,
    val showLabelWithCoordinates: Boolean = false,
)
