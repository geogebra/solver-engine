package methods.general

import engine.expressions.Decorator
import engine.expressions.Expression
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.methods.ruleResult
import engine.operators.BinaryExpressionOperator
import engine.operators.NaryOperator
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.condition
import engine.patterns.plusOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.metadata.metadata

const val ORDER_CONSTANT_PRODUCT = 1

enum class NormalizationRules(override val runner: Rule) : RunnerMethod {
    ReplaceInvisibleBrackets(
        rule {
            val missingBracket = condition(AnyPattern()) { it.outerBracket() == Decorator.MissingBracket }

            onPattern(missingBracket) {
                ruleResult(
                    toExpr = transformTo(missingBracket) { it.removeBrackets().decorate(Decorator.RoundBracket) },
                    explanation = metadata(Explanation.ReplaceInvisibleBrackets)
                )
            }
        }
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
                    toExpr = sumOf(
                        get(pattern)!!.children().map { child -> transformTo(child) { it.removeBrackets() } }
                    ),
                    explanation = metadata(Explanation.RemoveBracketSumInSum)
                )
            }
        }
    ),

    RemoveBracketProductInProduct(
        rule {
            val innerProduct = productContaining()
            val pattern = productContaining(condition(innerProduct) { it.hasBracket() })

            onPattern(pattern) {
                ruleResult(
                    toExpr = productOf(
                        get(pattern)!!.flattenedProductChildren()
                            .map { child -> transformTo(child) { it.removeBrackets() } }
                    ),
                    explanation = metadata(Explanation.RemoveBracketProductInProduct)
                )
            }
        }
    ),

    RemoveBracketAroundSignedIntegerInSum(
        rule {
            val number = SignedIntegerPattern()
            val bracket = condition(number) { it.hasBracket() }
            val pattern = sumContaining(bracket)

            onPattern(pattern) {
                ruleResult(
                    toExpr = pattern.substitute(transformTo(number) { it.removeBrackets() }),
                    explanation = metadata(Explanation.RemoveBracketAroundSignedIntegerInSum)
                )
            }
        }
    ),

    RemoveOuterBracket(
        rule {
            val pattern = condition(AnyPattern()) { it.hasBracket() }

            onPattern(pattern) {
                ruleResult(
                    toExpr = transformTo(pattern) { it.removeBrackets() },
                    explanation = metadata(Explanation.RemoveRedundantBracket)
                )
            }
        }
    ),

    RemoveRedundantPlusSign(
        rule {
            val value = AnyPattern()
            val pattern = plusOf(value)

            onPattern(pattern) {
                ruleResult(
                    toExpr = move(value),
                    explanation = metadata(Explanation.RemoveRedundantPlusSign)
                )
            }
        }
    ),

    NormaliseSimplifiedProduct(normaliseSimplifiedProduct)
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
private val normaliseSimplifiedProduct =
    rule {
        fun orderInProduct(e: Expression): Int {
            return when (e.operator) {
                NaryOperator.Sum -> ORDER_CONSTANT_PRODUCT + 2
                BinaryExpressionOperator.Root -> ORDER_CONSTANT_PRODUCT + 1
                UnaryExpressionOperator.SquareRoot -> ORDER_CONSTANT_PRODUCT + 1
                else -> ORDER_CONSTANT_PRODUCT
            }
        }

        val product = productContaining()

        onPattern(product) {
            val getProd = get(product)!!
            val getProdChildren = getProd.flattenedProductChildren()
            val (constants, nonConstants) = getProdChildren.partition { it.isConstant() }

            val sortedConstants = constants.sortedBy { orderInProduct(it) }
            val sortedNonConstants = nonConstants.sortedBy { orderInProduct(it) }
            val sortedProdChildren = mutableListOf<Expression>()
            sortedProdChildren.addAll(sortedConstants)
            sortedProdChildren.addAll(sortedNonConstants)

            val toExpr = productOf(sortedProdChildren.map { move(it) })

            if (toExpr == getProd) null
            else ruleResult(
                toExpr = toExpr,
                explanation = metadata(Explanation.NormaliseSimplifiedProduct)
            )
        }
    }
