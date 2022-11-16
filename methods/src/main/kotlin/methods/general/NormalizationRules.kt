package methods.general

import engine.expressions.Decorator
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.TransformationResult
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.condition
import engine.patterns.plusOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.metadata.metadata

enum class NormalizationRules(override val runner: Rule) : RunnerMethod {
    ReplaceInvisibleBrackets(
        rule {
            val missingBracket = condition(AnyPattern()) { it.outerBracket() == Decorator.MissingBracket }

            onPattern(missingBracket) {
                TransformationResult(
                    toExpr = transformTo(missingBracket) { it.removeBrackets().decorate(Decorator.RoundBracket) },
                    explanation = metadata(Explanation.ReplaceInvisibleBrackets),
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
                TransformationResult(
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
            val pattern = productContaining(innerProduct)

            onPattern(pattern) {
                TransformationResult(
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
                TransformationResult(
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
                TransformationResult(
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
                TransformationResult(
                    toExpr = move(value),
                    explanation = metadata(Explanation.RemoveRedundantPlusSign)
                )
            }
        }
    )
}
