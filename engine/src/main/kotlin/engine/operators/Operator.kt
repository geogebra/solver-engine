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

package engine.operators

const val ARITY_NULL = 0
const val ARITY_ONE = 1
const val ARITY_TWO = 2
const val ARITY_THREE = 3
const val ARITY_FOUR = 4
const val ARITY_VARIABLE = -1
const val ARITY_VARIABLE_FROM_ZERO = -2

const val MAX_CHILD_COUNT = 1000

data class RenderContext(val align: Boolean = false) {
    companion object {
        val Default = RenderContext()
    }
}

interface LatexRenderable {
    fun toLatexString(ctx: RenderContext = RenderContext.Default): String

    /** Generates the LaTeX of this, prepended with a plus sign, if it would need a plus sign in front of it in the
     * situation where it was the second term in a sum/difference. */
    fun toLatexStringAsSecondTermInASum(ctx: RenderContext): String

    /** Generates a string representation of this, prepended with a plus sign, if it would need a plus sign in front
     * of it in the situation where it was the second term in a sum/difference. */
    fun toReadableStringAsSecondTermInASum(): String

    /** If this is true, then when this term is rendered/printed in a product, a ÷ sign should be used instead of a
     * multiplication sign in front of this term */
    fun isInlineDivideByTerm(): Boolean
}

enum class OperatorKind {
    EXPRESSION,
    SET,
    LIST,
    STATEMENT,
    SET_ELEMENT,
    INNER,
    VOID,
}

internal interface Operator {
    val name: String

    val precedence: Int
    val arity: Int
    val kind: OperatorKind

    fun nthChildAllowed(n: Int, op: Operator): Boolean

    fun childrenAllowed(ops: Iterable<Operator>): Boolean {
        return ops.withIndex().all { (i, op) -> nthChildAllowed(i, op) }
    }

    fun minChildCount(): Int =
        when (arity) {
            ARITY_VARIABLE -> 2
            ARITY_VARIABLE_FROM_ZERO -> 0
            else -> arity
        }

    fun maxChildCount(): Int = if (arity <= ARITY_VARIABLE) MAX_CHILD_COUNT else arity

    fun <T> readableString(children: List<T>): String {
        return "${toString()}(${children.joinToString(", ")})"
    }

    fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String
}

internal abstract class NullaryOperator : Operator {
    override val precedence = MAX_PRECEDENCE
    override val arity = ARITY_NULL

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        throw IllegalArgumentException(
            "Nullary operator ${this::class.simpleName} should have no children. " +
                "Child $op is invalid at position $n.",
        )
    }

    override fun <T> readableString(children: List<T>): String {
        require(children.isEmpty())
        return toString()
    }

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        require(children.isEmpty())
        return latexString(ctx)
    }

    abstract fun latexString(ctx: RenderContext): String
}

internal object VoidOperator : NullaryOperator() {
    override val name = "Void"
    override val kind = OperatorKind.VOID

    override fun toString() = "/void/"

    override fun latexString(ctx: RenderContext) = ""
}

internal interface UnaryOperator : Operator {
    override val arity get() = ARITY_ONE

    fun childAllowed(op: Operator) = op.precedence > this.precedence

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(n == 0)
        return childAllowed(op)
    }

    fun <T> readableString(child: T): String {
        return "$this($child)"
    }

    override fun <T> readableString(children: List<T>): String {
        require(children.size == arity)
        return readableString(children[0])
    }

    fun latexString(ctx: RenderContext, child: LatexRenderable): String

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        require(children.size == arity)
        return latexString(ctx, children[0])
    }
}

internal interface BinaryOperator : Operator {
    override val arity get() = ARITY_TWO

    fun leftChildAllowed(op: Operator) = op.precedence > this.precedence

    fun rightChildAllowed(op: Operator) = op.precedence > this.precedence

    override fun nthChildAllowed(n: Int, op: Operator) =
        when (n) {
            0 -> leftChildAllowed(op)
            1 -> rightChildAllowed(op)
            else -> throw IllegalArgumentException(
                "Binary operator ${this::class.simpleName} should have exactly two children. " +
                    "Child $op is invalid at position $n.",
            )
        }

    fun <T> readableString(left: T, right: T): String {
        return "$this($left, $right)"
    }

    override fun <T> readableString(children: List<T>): String {
        require(children.size == arity)
        return readableString(children[0], children[1])
    }

    fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        require(children.size == arity)
        return latexString(ctx, children[0], children[1])
    }
}

internal interface TernaryOperator : Operator {
    override val arity get() = ARITY_THREE

    fun firstChildAllowed(op: Operator) = op.precedence > this.precedence

    fun secondChildAllowed(op: Operator) = op.precedence > this.precedence

    fun thirdChildAllowed(op: Operator) = op.precedence > this.precedence

    override fun nthChildAllowed(n: Int, op: Operator) =
        when (n) {
            0 -> firstChildAllowed(op)
            1 -> secondChildAllowed(op)
            2 -> thirdChildAllowed(op)
            else -> throw IllegalArgumentException(
                "Ternary operator ${this::class.simpleName} should have exactly three children. " +
                    "Child $op is invalid at position $n.",
            )
        }

    fun <T> readableString(first: T, second: T, third: T): String {
        return "$this($first, $second, $third)"
    }

    override fun <T> readableString(children: List<T>): String {
        require(children.size == arity)
        return readableString(children[0], children[1], children[2])
    }

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        require(children.size == arity)
        return latexString(ctx, children[0], children[1], children[2])
    }

    fun latexString(ctx: RenderContext, first: LatexRenderable, second: LatexRenderable, third: LatexRenderable): String
}

internal object ListOperator : Operator {
    override val arity = ARITY_VARIABLE
    override val name = "List"
    override val kind = OperatorKind.LIST
    override val precedence = MAX_PRECEDENCE

    override fun nthChildAllowed(n: Int, op: Operator) = true

    override fun <T> readableString(children: List<T>): String {
        return children.joinToString()
    }

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        return when (children.size) {
            0 -> ""
            1 -> children[0].toLatexString(ctx)
            else -> "${children.subList(0, children.size - 1).joinToString { it.toLatexString(ctx) }} " +
                "and ${children.last().toLatexString(ctx)}"
        }
    }
}
