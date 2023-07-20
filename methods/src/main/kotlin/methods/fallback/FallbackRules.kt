package methods.fallback

import engine.expressions.VoidExpression
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.ConstantPattern
import engine.patterns.condition
import engine.patterns.monomialPattern
import engine.patterns.oneOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.metadata.metadata

enum class FallbackRules(override val runner: Rule) : RunnerMethod {

    ExpressionIsFullySimplified(
        rule {
            var variablePattern = ArbitraryVariablePattern()
            val fullySimplifiedPtn = oneOf(

                // Any constant expression, if SimplifyConstantExpression didn't apply then it means it's simplify
                ConstantPattern(),

                // A monomial with a constant coefficient, the coefficient must be simplified otherwise
                // SimplifyAlgebraicExpressionInOneVariable would have applied.
                monomialPattern(variablePattern),

                // A sum of constant expressions and monomials of degree 1 in the same variable
                condition(sumContaining(withOptionalConstantCoefficient(variablePattern))) {
                    it.children.all { child ->
                        child.isConstant() || withOptionalConstantCoefficient(variablePattern).matches(this, child)
                    }
                },
            )
            onPattern(fullySimplifiedPtn) {
                ruleResult(
                    toExpr = VoidExpression(),
                    explanation = metadata(Explanation.ExpressionIsFullySimplified),
                )
            }
        },
    ),
}
