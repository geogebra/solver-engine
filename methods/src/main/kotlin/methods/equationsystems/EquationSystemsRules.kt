package methods.equationsystems

import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.Variable
import engine.expressions.equationOf
import engine.expressions.negOf
import engine.expressions.statementSystemOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.addEquationsOf
import engine.patterns.equationOf
import engine.patterns.productOf
import engine.patterns.statementSystemOf
import engine.patterns.subtractEquationsOf
import engine.steps.metadata.metadata
import engine.utility.factors
import methods.solvable.expressionComparator

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

    GuessIntegerSolutionsOfSystemContainingXYEqualsInteger(
        rule {
            val eq1 = equationOf(AnyPattern(), AnyPattern())
            val var1 = SolutionVariablePattern()
            val var2 = SolutionVariablePattern()
            val product = SignedIntegerPattern()
            val eq2 = equationOf(productOf(var1, var2), product)

            onPattern(statementSystemOf(eq1, eq2)) {
                val var1Val = get(var1) as Variable
                val var2Val = get(var2) as Variable
                if (var1Val == var2Val) {
                    // It would be better to this in the pattern instead
                    return@onPattern null
                }
                val productVal = getValue(product)
                val eq1Val = get(eq1) as Equation

                for (val1 in productVal.factors().flatMap { listOf(it, -it) }) {
                    val val2 = productVal / val1

                    val substitutedEq1 = eq1Val.substitute(var1Val to xp(val1), var2Val to xp(val2)) as Equation
                    if (substitutedEq1.holds(expressionComparator) == true) {
                        // Bingo
                        return@onPattern ruleResult(
                            toExpr = statementSystemOf(
                                equationOf(var1Val, xp(val1)),
                                equationOf(var2Val, xp(val2)),
                            ),
                            explanation = metadata(Explanation.GuessIntegerSolutionsOfSystemContainingXYEqualsInteger),
                        )
                    }
                }

                null
            }
        },
    ),
}

private fun Expression.substitute(vararg subsitutions: Pair<Expression, Expression>): Expression {
    return subsitutions.fold(this) { expr, sub -> expr.substituteAllOccurrences(sub.first, sub.second) }
}
