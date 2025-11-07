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

package methods.general

import engine.expressions.Decorator
import engine.expressions.DivideBy
import engine.expressions.Expression
import engine.expressions.Fraction
import engine.expressions.Logarithm
import engine.expressions.PathScope
import engine.expressions.Power
import engine.expressions.Product
import engine.expressions.Root
import engine.expressions.SquareRoot
import engine.expressions.Sum
import engine.expressions.TrigonometricExpression
import engine.expressions.Variable
import engine.expressions.containsLogs
import engine.expressions.hasRedundantBrackets
import engine.expressions.isSigned
import engine.expressions.productOf
import engine.expressions.variablePowerBase
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.TrigonometricExpressionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.negOf
import engine.patterns.plusOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.Transformation
import engine.steps.metadata.DragTargetPosition
import engine.steps.metadata.metadata
import engine.steps.metadata.GmPathModifier as PM

enum class NormalizationRules(override val runner: Rule) : RunnerMethod {
    AddClarifyingBracket(
        rule {
            val missingBracket = condition { it.outerBracket() == Decorator.MissingBracket }

            onPattern(missingBracket) {
                ruleResult(
                    tags = listOf(Transformation.Tag.Cosmetic),
                    toExpr = transformTo(missingBracket) { it.removeBrackets().decorate(Decorator.RoundBracket) },
                    gmAction = edit(missingBracket),
                    explanation = metadata(Explanation.AddClarifyingBracket),
                )
            }
        },
    ),

    /**
     * Flatten a sum that has terms which are also sums.
     */
    RemoveBracketSumInSum(
        rule {
            val innerSum = sumContaining()
            val pattern = sumContaining(innerSum)

            onPattern(pattern) {
                val idxOfFirstChildWithBracket = get(pattern).children.indexOfFirst { child ->
                    child is Sum && child.hasBracket()
                }
                val sumChildren = get(pattern).children.toMutableList()
                // Remove brackets from the first sum having brackets
                sumChildren[idxOfFirstChildWithBracket] = sumChildren[idxOfFirstChildWithBracket].removeBrackets()
                ruleResult(
                    tags = listOf(Transformation.Tag.Cosmetic),
                    toExpr = cancel(
                        mapOf(innerSum to listOf(PathScope.Decorator)),
                        engine.expressions.sumOf(sumChildren),
                    ),
                    gmAction = tap(innerSum, PM.OpenParens),
                    explanation = metadata(Explanation.RemoveBracketSumInSum),
                )
            }
        },
    ),

    // NOTE: there should just be one rule "RemoveRedundantBracket"
    // that should handle removal of brackets
    RemoveBracketProductInProduct(
        rule {
            val innerProduct = productContaining()
            val pattern = productContaining(condition(innerProduct) { it.hasBracket() })

            onPattern(pattern) {
                val idxOfFirstProdChildWithBracket = get(pattern).children.indexOfFirst { child ->
                    child is Product && child.hasBracket()
                }
                val prodList = get(pattern).children.toMutableList()
                // Remove brackets from the first product having brackets
                prodList[idxOfFirstProdChildWithBracket] = prodList[idxOfFirstProdChildWithBracket].removeBrackets()
                val flattenedProduct = prodList.flatMap {
                    if (isNonPartialProductWithoutLabelAndBracket(it)) it.children else listOf(it)
                }

                ruleResult(
                    tags = listOf(Transformation.Tag.Cosmetic),
                    toExpr = cancel(
                        mapOf(innerProduct to listOf(PathScope.Decorator)),
                        Product(flattenedProduct),
                    ),
                    gmAction = tap(innerProduct, PM.OpenParens),
                    explanation = metadata(Explanation.RemoveBracketProductInProduct),
                )
            }
        },
    ),

    NormalizeNegativeSignOfIntegerInSum(
        rule {
            val number = condition(negOf(UnsignedIntegerPattern())) { it.hasBracket() }
            val pattern = sumContaining(number)

            onPattern(pattern) {
                ruleResult(
                    tags = listOf(Transformation.Tag.Cosmetic),
                    toExpr = cancel(
                        mapOf(number to listOf(PathScope.Decorator, PathScope.OuterOperator)),
                        pattern.substitute(get(number).removeBrackets()),
                    ),
                    gmAction = tap(
                        number,
                        if (get(pattern).children[0] == get(number)) {
                            PM.OpenParens
                        } else {
                            PM.OuterOperator
                        },
                    ),
                    explanation = metadata(Explanation.NormalizeNegativeSignOfIntegerInSum),
                )
            }
        },
    ),

    RemoveRedundantBracket(
        rule {
            val pattern = condition { it.hasRedundantBrackets() }

            onPattern(pattern) {
                ruleResult(
                    tags = listOf(Transformation.Tag.Cosmetic),
                    toExpr = cancel(
                        mapOf(pattern to listOf(PathScope.Decorator)),
                        get(pattern).removeBrackets(),
                    ),
                    gmAction = tap(pattern, PM.OpenParens),
                    explanation = metadata(Explanation.RemoveRedundantBracket),
                )
            }
        },
    ),

    RemoveRedundantPlusSign(
        rule {
            val value = AnyPattern()
            val pattern = plusOf(value)

            onPattern(pattern) {
                ruleResult(
                    tags = listOf(Transformation.Tag.Cosmetic),
                    toExpr = cancel(
                        mapOf(pattern to listOf(PathScope.Operator)),
                        get(value),
                    ),
                    // NOTE: GM doesn't support leading pluses & this will only work for
                    // removing brackets around 1+(+2)
                    gmAction = tap(pattern, PM.OuterOperator),
                    explanation = metadata(Explanation.RemoveRedundantPlusSign),
                )
            }
        },
    ),

    ReorderProduct(reorderProduct),
    ReorderProductSingleStep(reorderProductSingleStep),
    NormalizeProductSigns(normalizeProductSigns),
    NormalizeTrigonometricFunctions(normalizeTrigonometricExpressionNotation),
}

private val reorderProduct = rule {
    val product = productContaining()

    onPattern(product) {
        val productChildren = get(product).children
        if (productChildren.any { it is DivideBy }) return@onPattern null

        val sortedProdChildren = productChildren.sortedWith(priorityComparator)

        if (productChildren == sortedProdChildren) {
            null
        } else {
            val toExpr = productOf(sortedProdChildren.map { move(it) })
            ruleResult(
                tags = listOf(Transformation.Tag.Rearrangement),
                toExpr = toExpr,
                explanation = metadata(Explanation.ReorderProduct),
                gmAction = edit(product),
            )
        }
    }
}

/** Like [reorderProduct], but only rearranges one term. It sorts via
 * insertion sort. */
private val reorderProductSingleStep =
    rule {
        val product = productContaining()

        onPattern(product) {
            val productChildren = get(product).children
            for (i in 1 until productChildren.size) {
                val current = productChildren[i]
                val previous = productChildren[i - 1]
                if (priorityComparator.compare(current, previous) < 0) {
                    val movedCurrent = move(current)
                    val sortedFirstPartOfProductChildren =
                        (productChildren.take(i) + movedCurrent).sortedWith(priorityComparator)
                    val toExpr = productOf(sortedFirstPartOfProductChildren + productChildren.drop(i + 1))
                    val target = productChildren[sortedFirstPartOfProductChildren.indexOf(movedCurrent)]
                    return@onPattern ruleResult(
                        tags = listOf(Transformation.Tag.Rearrangement),
                        toExpr = toExpr,
                        explanation = metadata(Explanation.ReorderProduct),
                        gmAction = drag(
                            current,
                            PM.Group,
                            target,
                            // in (2+a)*x, we need to target (2+a) when commuting the x to the left
                            if (target is Sum) PM.Parens else null,
                            DragTargetPosition.LeftOf,
                        ),
                    )
                }
            }
            null
        }
    }

/** Make multiplication implicit or explicit, to make the product end up written in the
 * most standard form */
private val normalizeProductSigns = rule {
    val product = productContaining()

    onPattern(product) {
        val productValue = get(product) as Product
        if (productValue.forcedSigns.isEmpty()) {
            return@onPattern null
        }

        ruleResult(
            tags = listOf(Transformation.Tag.Cosmetic),
            toExpr = productOf(productValue.children),
            explanation = metadata(Explanation.NormalizeProducts),
            // gmAction = do nothing, because it happens automatically
        )
    }
}

// [(-sin[x]) ^ 2] -> - [sin ^ 2][x]
private val normalizeTrigonometricExpressionNotation = rule {
    val argument = AnyPattern()
    val pattern = TrigonometricExpressionPattern(argument, powerInside = false)

    onPattern(pattern) {
        ruleResult(
            tags = listOf(Transformation.Tag.Cosmetic),
            toExpr = transform(pattern, wrapWithTrigonometricFunction(pattern, get(argument), powerInside = true)),
            explanation = metadata(Explanation.NormalizeTrigonometricExpressionPower),
        )
    }
}

// We should add tests for this and then tidy up the logic, probably by doing a case split on isConstant
private val priorityComparator = compareBy<Expression>(
    { !it.isConstant() || it.containsLogs() },
    {
        @Suppress("MagicNumber")
        when (if (it is Power) it.base else it) {
            // log, log_a, ln
            is Logarithm -> 5
            // (x + 1) or (1 + sqrt[3])
            is Sum -> 4
            // sqrt[...] or root[..., n]
            is Root, is SquareRoot, is TrigonometricExpression -> 3
            // a, x, [x ^ 2]
            is Variable -> 1
            else -> 2
        }
    },
    { it.variablePowerBase()?.variableName },
    {
        // if the bases have the same priority, put powers with rational exponents at the end
        it is Power && it.exponent.isSigned<Fraction>()
    },
)

private fun isNonPartialProductWithoutLabelAndBracket(expr: Expression): Boolean {
    return expr is Product && !expr.isPartialProduct() && !expr.hasLabel() && !expr.hasBracket()
}
