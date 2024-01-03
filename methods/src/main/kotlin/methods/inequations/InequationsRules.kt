package methods.inequations

import engine.expressions.Comparison
import engine.expressions.Constants
import engine.expressions.SimpleComparator
import engine.expressions.Variable
import engine.expressions.contradictionOf
import engine.expressions.finiteSetOf
import engine.expressions.hasSingleValue
import engine.expressions.identityOf
import engine.expressions.setDifferenceOf
import engine.expressions.setSolutionOf
import engine.expressions.variableListOf
import engine.methods.Rule
import engine.methods.RuleResultBuilder
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.condition
import engine.patterns.inequationOf
import engine.steps.Transformation
import engine.steps.metadata.metadata

enum class InequationsRules(override val runner: Rule) : RunnerMethod {
    ExtractSolutionFromInequationInSolvedForm(
        rule {
            val lhs = SolutionVariablePattern()
            val rhs = condition {
                // excluding values containing +/-
                it.isConstantIn(solutionVariables) && it.hasSingleValue()
            }

            val inequation = inequationOf(lhs, rhs)

            onPattern(inequation) {
                ruleResult(
                    tags = listOf(Transformation.Tag.Pedantic),
                    toExpr = setSolutionOf(
                        variableListOf(move(lhs) as Variable),
                        setDifferenceOf(Constants.Reals, finiteSetOf(move(rhs))),
                    ),
                    explanation = metadata(Explanation.ExtractSolutionFromInequationInSolvedForm),
                )
            }
        },
    ),

    ExtractSolutionFromConstantInequation(
        rule {
            val lhs = ConstantInSolutionVariablePattern()
            val rhs = ConstantInSolutionVariablePattern()

            val inequation = inequationOf(lhs, rhs)

            onPattern(inequation) {
                val isSatisfied = (get(inequation) as Comparison).holds(SimpleComparator) ?: return@onPattern null
                trueOrFalseRuleResult(isSatisfied)
            }
        },
    ),
}

private fun RuleResultBuilder.trueOrFalseRuleResult(isSatisfied: Boolean): Transformation {
    val noVariable = context.solutionVariables.isEmpty()
    val variableList = variableListOf(context.solutionVariables)
    val toExpr = if (isSatisfied) {
        identityOf(variableList, expression)
    } else {
        contradictionOf(variableList, expression)
    }
    return if (noVariable) {
        val key = if (isSatisfied) {
            Explanation.ExtractTruthFromTrueInequation
        } else {
            Explanation.ExtractFalsehoodFromFalseInequation
        }
        ruleResult(toExpr = toExpr, explanation = metadata(key))
    } else {
        val key = if (isSatisfied) {
            Explanation.ExtractSolutionFromTrueInequation
        } else {
            Explanation.ExtractSolutionFromFalseInequation
        }
        ruleResult(toExpr = toExpr, explanation = metadata(key, variableList))
    }
}
