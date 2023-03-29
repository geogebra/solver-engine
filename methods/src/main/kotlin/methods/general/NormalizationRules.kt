package methods.general

import engine.expressions.Decorator
import engine.expressions.Expression
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.operators.BinaryExpressionOperator
import engine.operators.NaryOperator
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.condition
import engine.patterns.plusOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.Transformation
import engine.steps.metadata.DragTargetPosition
import engine.steps.metadata.metadata
import engine.steps.metadata.GmPathModifier as PM

enum class NormalizationRules(override val runner: Rule) : RunnerMethod {
    ReplaceInvisibleBrackets(
        rule {
            val missingBracket = condition(AnyPattern()) { it.outerBracket() == Decorator.MissingBracket }

            onPattern(missingBracket) {
                if (context.gmFriendly) return@onPattern null
                ruleResult(
                    tags = listOf(Transformation.Tag.Cosmetic),
                    toExpr = transformTo(missingBracket) { it.removeBrackets().decorate(Decorator.RoundBracket) },
                    gmAction = edit(missingBracket),
                    explanation = metadata(Explanation.ReplaceInvisibleBrackets),
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
                ruleResult(
                    tags = listOf(Transformation.Tag.Rearrangement),
                    toExpr = sumOf(
                        get(pattern).children.map { child -> transformTo(child) { it.removeBrackets() } },
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

    RemoveBracketAroundSignedIntegerInSum(
        rule {
            val number = SignedIntegerPattern()
            val bracket = condition(number) { it.hasBracket() }
            val pattern = sumContaining(bracket)

            onPattern(pattern) {
                ruleResult(
                    toExpr = pattern.substitute(transformTo(number) { it.removeBrackets() }),
                    gmAction = tap(bracket, PM.OuterOperator),
                    explanation = metadata(Explanation.RemoveBracketAroundSignedIntegerInSum),
                )
            }
        },
    ),

    RemoveOuterBracket(
        rule {
            val pattern = condition(AnyPattern()) { it.hasBracket() }

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

    NormaliseSimplifiedProductRule(normaliseSimplifiedProductRule),
    NormaliseSimplifiedProductSingleStep(normaliseSimplifiedProductSingleStep),
    NormalizeTheImplicitnessAndExplicitnessOfMultiplication(normalizeTheImplicitnessAndExplicitnessOfMultiplication),
}

/**
 * normalise the product of different kind of terms, which has already
 * been simplified, the normalised order of terms in product looks like:
 *
 * numericConstants * constantRootsOrSquareRoots * constantSums *
 * monomials * variableRootsOrSquareRoots * polynomials
 *
 * for e.g. sqrt[3] * (1 + sqrt[3]) * ([y^2] + 1) * y * 5 -->
 * 5 * sqrt[3] * (1 + sqrt[3]) * y * ([y^2] + 1)
 */
private val normaliseSimplifiedProductRule =
    rule {
        val product = productContaining()

        onPattern(product) {
            val getProd = get(product)
            val getProdChildren = getProd.flattenedProductChildren()
            val sortedProdChildren = getProdChildren.sortedBy { orderInProduct(it) }
            val toExpr = productOf(sortedProdChildren.map { move(it) })
            if (toExpr == getProd) {
                null
            } else {
                ruleResult(
                    tags = listOf(Transformation.Tag.Rearrangement),
                    toExpr = toExpr,
                    explanation = metadata(Explanation.NormaliseSimplifiedProduct),
                    gmAction = edit(product),
                )
            }
        }
    }

/** Like [normaliseSimplifiedProductRule], but only rearranges one term. It sorts via
 * insertion sort. */
private val normaliseSimplifiedProductSingleStep =
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
                        explanation = metadata(Explanation.NormaliseSimplifiedProduct),
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
private val normalizeTheImplicitnessAndExplicitnessOfMultiplication =
    rule {
        val product = productContaining()

        onPattern(product) {
            val getProd = get(product)
            val getProdChildren = getProd.flattenedProductChildren()
            val toExpr = productOf(getProdChildren)
            if (toExpr == getProd) {
                null
            } else {
                ruleResult(
                    tags = listOf(Transformation.Tag.Cosmetic),
                    toExpr = toExpr,
                    explanation = metadata(Explanation.NormaliseSimplifiedProduct),
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
