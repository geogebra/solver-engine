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

package engine.patterns

import engine.expressions.Expression

/**
 * An interface for matching with a pattern. Can be
 * thought of as a linked-list of (pattern, pattern's value)
 */
interface Match {
    /** This is redundant. It does the same as [getBoundExpr] */
    fun getLastBinding(p: Pattern): Expression?

    fun isBound(p: Pattern) = getLastBinding(p) != null

    /**
     * Returns the [Expression] value of the given match
     * with the provided [Pattern] [p].
     */
    fun getBoundExpr(p: Pattern): Expression?

    /**
     * Appends to the list `acc` the [Expression]s from
     * root to the start of the pattern [p] in
     * a provided match
     *
     * @see getBoundExprs
     */
    fun accumulateExprs(p: Pattern, acc: MutableList<Expression>)

    /**
     * Returns a list of [Expression]s from the root of
     * the tree to the where the pattern [p] is present
     * in a provided match object
     */
    fun getBoundExprs(p: Pattern): List<Expression>

    fun newChild(key: Pattern, value: Expression): Match = ChildMatch(key, value, this)
}

/**
 * Object to refer to the matching with the root of
 * expression tree.
 */
object RootMatch : Match {
    override fun getLastBinding(p: Pattern): Expression? = null

    override fun getBoundExpr(p: Pattern): Expression? = null

    override fun accumulateExprs(p: Pattern, acc: MutableList<Expression>) {
        // do nothing
    }

    override fun getBoundExprs(p: Pattern): List<Expression> = emptyList()
}

/**
 * Used to create a non-root `Match` object. Created
 * with `Pattern` pointing to a `Subexpression` value.
 *
 * @param key the pattern to be searched for
 * @param value the expression in which to search for the pattern
 * @param parent the parent Match object of the ChildMatch object
 */
data class ChildMatch(
    private val key: Pattern,
    private val value: Expression,
    private val parent: Match,
) : Match {
    override fun getLastBinding(p: Pattern): Expression? {
        return when {
            key === p.key -> value
            else -> parent.getLastBinding(p)
        }
    }

    override fun getBoundExpr(p: Pattern): Expression? {
        return if (key === p.key) value else parent.getBoundExpr(p)
    }

    override fun accumulateExprs(p: Pattern, acc: MutableList<Expression>) {
        parent.accumulateExprs(p, acc)
        if (key === p.key) {
            acc.add(value)
        }
    }

    override fun getBoundExprs(p: Pattern): List<Expression> {
        val acc = mutableListOf<Expression>()
        accumulateExprs(p, acc)
        return acc
    }
}
