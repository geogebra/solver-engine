package methods.equationsystems

import engine.expressions.equationOf
import engine.expressions.negOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.addEquationsOf
import engine.patterns.equationOf
import engine.patterns.subtractEquationsOf
import engine.steps.metadata.metadata

enum class EquationSystemsRules(override val runner: Rule) : RunnerMethod {
    RewriteEquationAddition(
        rule {
            val lhs1 = AnyPattern()
            val rhs1 = AnyPattern()
            val lhs2 = AnyPattern()
            val rhs2 = AnyPattern()

            val eq1 = equationOf(lhs1, rhs1)
            val eq2 = equationOf(lhs2, rhs2)

            onPattern(addEquationsOf(eq1, eq2)) {
                ruleResult(
                    toExpr = equationOf(sumOf(get(lhs1), get(lhs2)), sumOf(get(rhs1), get(rhs2))),
                    explanation = metadata(Explanation.RewriteEquationAddition),
                )
            }
        },
    ),

    RewriteEquationSubtraction(
        rule {
            val lhs1 = AnyPattern()
            val rhs1 = AnyPattern()
            val lhs2 = AnyPattern()
            val rhs2 = AnyPattern()

            val eq1 = equationOf(lhs1, rhs1)
            val eq2 = equationOf(lhs2, rhs2)

            onPattern(subtractEquationsOf(eq1, eq2)) {
                ruleResult(
                    toExpr = equationOf(sumOf(get(lhs1), negOf(get(lhs2))), sumOf(get(rhs1), negOf(get(rhs2)))),
                    explanation = metadata(Explanation.RewriteEquationSubtraction),
                )
            }
        },
    ),
}
