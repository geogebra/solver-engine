package methods.rationalexpressions

import engine.expressions.Fraction
import engine.expressions.divideBy
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.condition
import engine.patterns.divideBy
import engine.patterns.productOf
import engine.patterns.sumContaining
import engine.steps.metadata.metadata

enum class RationalExpressionsRules(override val runner: Rule) : RunnerMethod {

    DistributeDivisionOverSum(
        rule {
            val dividend = sumContaining()
            val divisor = condition { it !is Fraction }

            onPattern(productOf(dividend, divideBy(divisor))) {
                ruleResult(
                    toExpr = sumOf(get(dividend).children.map { productOf(move(it), divideBy(get(divisor))) }),
                    explanation = metadata(Explanation.DistributeDivisionOverSum),
                )
            }
        },
    ),
}
