package engine.methods

import engine.context.Context
import engine.expressions.BasicMeta
import engine.expressions.Build
import engine.expressions.Decorator
import engine.expressions.Expression
import engine.expressions.Product
import engine.expressions.Sum
import engine.methods.InlinePartialExpressions.tryExecute
import engine.steps.Transformation
import engine.steps.metadata.metadata

/**
 * A [Method] used for cleaning up the partial sums and products which resulted from substituting
 * a sum in a sum or a product in a product while using a Pipeline.
 * Its [tryExecute] finds the first sum or product with a partial term or factor and inlines all its direct
 * children.
 * It does not inline labelled partial subexpressions.
 */
object InlinePartialExpressions : Method {
    private fun inlinePartialSums(sum: Sum): Transformation {
        val flatOperands = mutableListOf<Expression>()
        for (child in sum.children) {
            if (child is Sum && !child.hasLabel() && child.outerBracket() == Decorator.PartialBracket) {
                flatOperands.addAll(child.children)
            } else {
                flatOperands.add(child)
            }
        }
        val result = Sum(flatOperands, BasicMeta(origin = Build, decorators = sum.decorators))

        return Transformation(
            type = Transformation.Type.Rule,
            tags = listOf(Transformation.Tag.InvisibleChange),
            fromExpr = sum,
            toExpr = result,
            explanation = metadata(SolverEngineExplanation.InlinePartialSum),
            // gmAction = do nothing, because it happens automatically
        )
    }

    private fun inlinePartialProducts(product: Product): Transformation {
        val flatOperands = mutableListOf<Expression>()
        val flatForcedSigns = mutableListOf<Int>()
        var flatIndex = 0
        for ((i, child) in product.children.withIndex()) {
            if (i in product.forcedSigns) {
                flatForcedSigns.add(flatIndex)
            }
            if (child is Product && !child.hasLabel() && child.outerBracket() == Decorator.PartialBracket) {
                flatOperands.addAll(child.children)
                flatForcedSigns.addAll(child.forcedSigns.map { it + flatIndex })
                flatIndex += child.childCount
            } else {
                flatOperands.add(child)
                flatIndex++
            }
        }
        val result = Product(flatOperands, flatForcedSigns, BasicMeta(origin = Build, decorators = product.decorators))

        return Transformation(
            type = Transformation.Type.Rule,
            tags = listOf(Transformation.Tag.InvisibleChange),
            fromExpr = product,
            toExpr = result,
            explanation = metadata(SolverEngineExplanation.InlinePartialProduct),
            // gmAction = do nothing, because it happens automatically
        )
    }

    override fun tryExecute(ctx: Context, sub: Expression): Transformation? {
        return if (sub is Product && sub.children.any { !it.hasLabel() && it.isPartialProduct() }) {
            inlinePartialProducts(sub)
        } else if (sub is Sum && sub.children.any { !it.hasLabel() && it.isPartialSum() }) {
            inlinePartialSums(sub)
        } else {
            sub.children.firstNotNullOfOrNull { tryExecute(ctx, it) }
        }
    }
}
