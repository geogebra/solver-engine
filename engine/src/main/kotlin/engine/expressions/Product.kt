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

package engine.expressions

import engine.operators.BinaryExpressionOperator
import engine.operators.ProductOperator
import engine.operators.RenderContext
import engine.operators.UnaryExpressionOperator
import engine.sign.Sign

class Product(
    factors: List<Expression>,
    val forcedSigns: List<Int> = emptyList(),
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = ProductOperator(forcedSigns),
        operands = factors,
        meta,
    ) {
    init {
        require(operands.size > 1)
    }

    override fun signOf() = operands.map { it.signOf() }.reduce(Sign::times)

    private fun productSignRequiredForOperand(i: Int, op: Expression) =
        when {
            op.operator == UnaryExpressionOperator.DivideBy -> false
            forcedSigns.contains(i) -> true
            i > 0 && productSignRequired(operands[i - 1], op) -> true
            else -> false
        }

    override fun toString(): String {
        val str = operands.mapIndexed { i, op ->
            val sign = if (productSignRequiredForOperand(i, op)) "* " else ""
            "$sign$op"
        }.joinToString(separator = " ")
        return decorators.fold(str) { acc, dec -> dec.decorateString(acc) }
    }

    override fun toLatexString(ctx: RenderContext): String {
        val str = operands.mapIndexed { i, op ->
            val sign = if (productSignRequiredForOperand(i, op)) "\\times " else ""
            "$sign${op.toLatexString(ctx)}"
        }.joinToString(separator = " ")
        return decorators.fold(str) { acc, dec -> dec.decorateLatexString(acc) }
    }

    override fun fillJson(s: MutableMap<String, Any>) {
        s["type"] = "SmartProduct"
        if (operands.isNotEmpty()) {
            s["operands"] = operands.map { it.toJson() }
            s["signs"] = operands.withIndex().map { (i, op) -> productSignRequiredForOperand(i, op) }
        }
    }
}

class DivideBy(divisor: Expression, meta: NodeMeta = BasicMeta()) : ValueExpression(
    operator = UnaryExpressionOperator.DivideBy,
    operands = listOf(divisor),
    meta = meta,
) {
    val divisor get() = firstChild

    override fun signOf() = divisor.signOf().inverse()
}

private fun getBaseOfPower(expr: Expression): Expression =
    when (expr) {
        is Power -> getBaseOfPower(expr.base)
        else -> expr
    }

private fun Expression.isNumbery(): Boolean =
    when {
        this is Power -> base.isNumbery()
        this is Fraction -> true
        this is IntegerExpression -> true
        this is DecimalExpression -> true
        this is RecurringDecimalExpression -> true
        operator == UnaryExpressionOperator.Minus || operator == UnaryExpressionOperator.Plus ||
            operator == UnaryExpressionOperator.PlusMinus -> {
            val op = operands[0]
            op is IntegerExpression || op is DecimalExpression || op is RecurringDecimalExpression
        }
        else -> false
    }

// couldn't come up with a good way of splitting or simplifying this method
@Suppress("CyclomaticComplexMethod")
fun productSignRequired(left: Expression, right: Expression): Boolean =
    when {
        right is DivideBy -> false
        left.isPartialProduct() -> productSignRequired(left.children.last(), right)
        right.isPartialProduct() -> productSignRequired(left, right.children.first())
        left.operator == UnaryExpressionOperator.DivideBy || right.operator == UnaryExpressionOperator.DivideBy -> true
        right.isNumbery() -> true
        left.hasVisibleBracket() || right.hasVisibleBracket() -> false
        else -> {
            val rightOp = getBaseOfPower(right)
            val leftOp = getBaseOfPower(left)

            val leftIsVariable = leftOp is Variable
            val rightIsRoot = rightOp.operator == UnaryExpressionOperator.SquareRoot ||
                rightOp.operator == BinaryExpressionOperator.Root
            val rightIsRootOrVariable = rightIsRoot || rightOp is Variable
            val differentVariables = leftOp is Variable && rightOp is Variable &&
                leftOp.variableName != rightOp.variableName

            !(left.isNumbery() && rightIsRootOrVariable || leftIsVariable && rightIsRoot || differentVariables)
        }
    }
