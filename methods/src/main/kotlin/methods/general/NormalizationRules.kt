package methods.general

import engine.expressions.Child
import engine.expressions.Decorator
import engine.expressions.Expression
import engine.expressions.PathScope
import engine.expressions.productOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.operators.BinaryExpressionOperator
import engine.operators.NaryOperator
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
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
            val missingBracket = condition(AnyPattern()) { it.outerBracket() == Decorator.MissingBracket }

            onPattern(missingBracket) {
                if (context.gmFriendly) return@onPattern null
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
                val idxOfFirstChildWithBracket = get(pattern).children.indexOfFirst { child -> child.hasBracket() }
                val sumChildren = get(pattern).children.toMutableList()
                // Remove brackets from the first sum having brackets
                sumChildren[idxOfFirstChildWithBracket] = sumChildren[idxOfFirstChildWithBracket].removeBrackets()
                ruleResult(
                    tags = listOf(Transformation.Tag.Rearrangement),
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

    RemoveBracketProductInProduct(
        rule {
            val innerProduct = productContaining()
            val pattern = productContaining(condition(innerProduct) { it.hasBracket() })

            onPattern(pattern) {
                ruleResult(
                    tags = listOf(Transformation.Tag.Rearrangement),
                    toExpr = productOf(
                        get(pattern).flattenedProductChildren()
                            .map { child -> transformTo(child) { it.removeBrackets() } },
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
                    toExpr = pattern.substitute(transformTo(number) { it.removeBrackets() }),
                    gmAction = tap(number, PM.OuterOperator),
                    explanation = metadata(Explanation.NormalizeNegativeSignOfIntegerInSum),
                )
            }
        },
    ),

    RemoveRedundantBracket(
        rule {
            val pattern = condition(AnyPattern()) {
                it.hasBracket() && it.outerBracket() != Decorator.MissingBracket && if (it.parent == null) {
                    true
                } else {
                    val origin = it.origin as Child
                    origin.parent.operator.nthChildAllowed(origin.index, it.operator)
                }
            }

            onPattern(pattern) {
                ruleResult(
                    tags = listOf(Transformation.Tag.Cosmetic),
                    toExpr = transformTo(pattern) { it.removeBrackets() },
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
                    toExpr = move(value),
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
}

private val reorderProduct =
    rule {
        val product = productContaining()

        onPattern(product) {
            val productChildren = get(product).flattenedProductChildren()
            val sortedProdChildren = productChildren.sortedBy { orderInProduct(it) }
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
            val productChildren = get(product).flattenedProductChildren()
            for (i in 1 until productChildren.size) {
                val current = productChildren[i]
                val currentOrder = orderInProduct(current)
                val previous = productChildren[i - 1]
                val previousOrder = orderInProduct(previous)
                if (currentOrder < previousOrder) {
                    val movedCurrent = move(current)
                    val sortedFirstPartOfProductChildren =
                        (productChildren.take(i) + movedCurrent).sortedBy { orderInProduct(it) }
                    val toExpr = productOf(sortedFirstPartOfProductChildren + productChildren.drop(i + 1))
                    return@onPattern ruleResult(
                        tags = listOf(Transformation.Tag.Rearrangement),
                        toExpr = toExpr,
                        explanation = metadata(Explanation.ReorderProduct),
                        gmAction = drag(
                            current,
                            productChildren[sortedFirstPartOfProductChildren.indexOf(movedCurrent)],
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
private val normalizeProductSigns =
    rule {
        val product = productContaining()

        onPattern(product) {
            val productValue = get(product)
            val productChildren = productValue.flattenedProductChildren()
            val toExpr = productOf(productChildren)
            if (toExpr == productValue) {
                null
            } else {
                ruleResult(
                    tags = listOf(Transformation.Tag.Cosmetic),
                    toExpr = toExpr,
                    explanation = metadata(Explanation.NormalizeProductSigns),
                    // gmAction = do nothing, because it happens automatically
                )
            }
        }
    }

@Suppress("MagicNumber")
private fun orderInProduct(e: Expression): Int {
    val isConstantAdjuster = if (e.isConstant()) 0 else 10
    return isConstantAdjuster + when (e.operator) {
        NaryOperator.Sum -> 3
        BinaryExpressionOperator.Root -> 2
        UnaryExpressionOperator.SquareRoot -> 2
        else -> 1
    }
}
