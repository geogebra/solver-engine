package methods.inequalities

import engine.expressions.Comparison
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.SimpleComparator
import engine.expressions.Variable
import engine.expressions.closedOpenIntervalOf
import engine.expressions.closedRangeOf
import engine.expressions.contradictionOf
import engine.expressions.equationOf
import engine.expressions.greaterThanEqualOf
import engine.expressions.greaterThanOf
import engine.expressions.identityOf
import engine.expressions.inequationOf
import engine.expressions.lessThanEqualOf
import engine.expressions.lessThanOf
import engine.expressions.negOf
import engine.expressions.openClosedIntervalOf
import engine.expressions.openIntervalOf
import engine.expressions.openRangeOf
import engine.expressions.setSolutionOf
import engine.expressions.simplifiedProductOf
import engine.expressions.statementUnionOf
import engine.expressions.variableListOf
import engine.methods.Rule
import engine.methods.RuleResultBuilder
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.operators.Comparator
import engine.patterns.AnyPattern
import engine.patterns.ConstantInSolutionVariablePattern
import engine.patterns.FixedPattern
import engine.patterns.Pattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.absoluteValueOf
import engine.patterns.condition
import engine.patterns.greaterThanEqualOf
import engine.patterns.greaterThanOf
import engine.patterns.inequalityOf
import engine.patterns.lessThanEqualOf
import engine.patterns.lessThanOf
import engine.patterns.oneOf
import engine.patterns.withOptionalConstantCoefficient
import engine.sign.Sign
import engine.steps.Transformation
import engine.steps.metadata.metadata

enum class InequalitiesRules(override val runner: Rule) : RunnerMethod {
    ExtractSolutionFromConstantInequality(
        rule {
            val lhs = ConstantInSolutionVariablePattern()
            val rhs = ConstantInSolutionVariablePattern()

            val inequality = inequalityOf(lhs, rhs)

            onPattern(inequality) {
                val isSatisfied = (get(inequality) as Comparison).holds(SimpleComparator) ?: return@onPattern null
                trueOrFalseRuleResult(inequality, isSatisfied)
            }
        },
    ),

    SeparateModulusGreaterThanPositiveConstant(separateModulusGreaterThanPositiveConstant),

    SeparateModulusGreaterThanEqualToPositiveConstant(separateModulusGreaterThanEqualToPositiveConstant),

    ExtractSolutionFromModulusLessThanNonPositiveConstant(extractSolutionFromModulusLessThanNonPositiveConstant),

    ExtractSolutionFromModulusGreaterThanEqualToNonPositiveConstant(
        extractSolutionFromModulusGreaterThanEqualToNonPositiveConstant,
    ),

    ExtractSolutionFromModulusGreaterThanNegativeConstant(extractSolutionFromModulusGreaterThanNegativeConstant),

    ReduceModulusLessThanEqualToZeroInequalityToEquation(reduceModulusLessThanEqualToZeroInequalityToEquation),

    ConvertModulusGreaterThanZero(convertModulusGreaterThanZero),

    ConvertModulusLessThanPositiveConstant(convertModulusLessThanPositiveConstant),

    ConvertModulusLessThanEqualToPositiveConstant(convertModulusLessThanEqualToPositiveConstant),

    ExtractSolutionFromInequalityInSolvedForm(
        rule {
            val lhs = SolutionVariablePattern()
            val rhs = ConstantInSolutionVariablePattern()

            val inequality = inequalityOf(lhs, rhs)

            onPattern(inequality) {
                val inequalityValue = get(inequality) as engine.expressions.Inequality
                val interval = makeInterval(inequalityValue.comparator, get(rhs))!!
                ruleResult(
                    toExpr = setSolutionOf(variableListOf(move(lhs) as Variable), interval),
                    explanation = metadata(Explanation.ExtractSolutionFromInequalityInSolvedForm),
                )
            }
        },
    ),
}

/**
 * Turns something like " <= 3" into an interval "[3, infty)"
 */
private fun makeInterval(comparator: Comparator, boundary: Expression): Expression? {
    return when (comparator) {
        Comparator.LessThan -> openIntervalOf(Constants.NegativeInfinity, boundary)
        Comparator.LessThanOrEqual -> openClosedIntervalOf(Constants.NegativeInfinity, boundary)
        Comparator.GreaterThan -> openIntervalOf(boundary, Constants.Infinity)
        Comparator.GreaterThanOrEqual -> closedOpenIntervalOf(boundary, Constants.Infinity)
        else -> null
    }
}

private fun RuleResultBuilder.trueOrFalseRuleResult(inequality: Pattern, isSatisfied: Boolean): Transformation {
    val noVariable = context.solutionVariables.isEmpty()
    val variableList = variableListOf(context.solutionVariables)
    val toExpr = if (isSatisfied) {
        identityOf(variableList, get(inequality))
    } else {
        contradictionOf(variableList, get(inequality))
    }
    return if (noVariable) {
        val key = if (isSatisfied) {
            Explanation.ExtractTruthFromTrueInequality
        } else {
            Explanation.ExtractFalsehoodFromFalseInequality
        }
        ruleResult(toExpr = toExpr, explanation = metadata(key))
    } else {
        val key = if (isSatisfied) {
            Explanation.ExtractSolutionFromTrueInequality
        } else {
            Explanation.ExtractSolutionFromFalseInequality
        }
        ruleResult(toExpr = toExpr, explanation = metadata(key, variableList))
    }
}

/**
 * abs(x) < a, where a <= 0  --> x has no solution in R
 */
private val extractSolutionFromModulusLessThanNonPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = oneOf(
        condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.NEGATIVE },
        FixedPattern(Constants.Zero),
    )

    val lessThanInequality = lessThanOf(lhs, rhs)

    onPattern(lessThanInequality) {
        ruleResult(
            toExpr = contradictionOf(variableListOf(context.solutionVariables), expression),
            explanation = metadata(Explanation.ExtractSolutionFromModulusLessThanNonPositiveConstant),
        )
    }
}

/**
 * abs[x + 1] <= 0 --> abs[x + 1] = 0, for any x in R
 */
private val reduceModulusLessThanEqualToZeroInequalityToEquation = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = FixedPattern(Constants.Zero)

    val lessThanEqualToInequality = lessThanEqualOf(lhs, rhs)
    onPattern(lessThanEqualToInequality) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = transform(equationOf(newLHS, get(rhs))),
            explanation = metadata(Explanation.ReduceModulusLessThanEqualToZeroInequalityToEquation),
        )
    }
}

/**
 * abs(x) >= a --> x \in R, where a \in (-inf, 0]
 */
private val extractSolutionFromModulusGreaterThanEqualToNonPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val zero = FixedPattern(Constants.Zero)
    val negativeConstantRHS = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.NEGATIVE }
    val nonPositiveConstantRHS = oneOf(negativeConstantRHS, zero)

    val pattern = greaterThanEqualOf(lhs, nonPositiveConstantRHS)

    onPattern(pattern) {
        val variableList = variableListOf(context.solutionVariables)
        ruleResult(
            toExpr = transform(identityOf(variableList, get(pattern))),
            explanation = metadata(
                Explanation.ExtractSolutionFromModulusGreaterThanEqualToNonPositiveConstant,
                variableList,
            ),
        )
    }
}

/**
 * abs(x) > a --> x \in R, where a is a negative constant
 */
private val extractSolutionFromModulusGreaterThanNegativeConstant = rule {
    val signedLHS = AnyPattern()
    val lhs = absoluteValueOf(signedLHS)
    val negativeConstantRHS = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.NEGATIVE }

    val pattern = greaterThanOf(lhs, negativeConstantRHS)

    onPattern(pattern) {
        val variableList = variableListOf(context.solutionVariables)
        ruleResult(
            toExpr = transform(identityOf(variableList, get(pattern))),
            explanation = metadata(Explanation.ExtractSolutionFromModulusGreaterThanNegativeConstant, variableList),
        )
    }
}

/**
 * abs(x) > a --> x > a OR x < -a, where a > 0
 */
private val separateModulusGreaterThanPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.POSITIVE }
    val inequality = greaterThanOf(lhs, rhs)

    onPattern(inequality) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = statementUnionOf(
                lessThanOf(newLHS, negOf(get(rhs))),
                greaterThanOf(newLHS, get(rhs)),
            ),
            explanation = metadata(Explanation.SeparateModulusGreaterThanPositiveConstant),
        )
    }
}

/**
 * abs(x) >= a --> x >= a OR x <= -a, where a > 0
 */
private val separateModulusGreaterThanEqualToPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.POSITIVE }
    val inequality = greaterThanEqualOf(lhs, rhs)

    onPattern(inequality) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = transform(
                statementUnionOf(
                    greaterThanEqualOf(newLHS, get(rhs)),
                    lessThanEqualOf(newLHS, negOf(get(rhs))),
                ),
            ),
            explanation = metadata(Explanation.SeparateModulusGreaterThanEqualToPositiveConstant),
        )
    }
}

/**
 * abs(x) > 0 --> abs(x) != 0
 */
private val convertModulusGreaterThanZero = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = FixedPattern(Constants.Zero)

    val inequality = greaterThanOf(lhs, rhs)

    onPattern(inequality) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = transform(inequationOf(newLHS, get(rhs))),
            explanation = metadata(Explanation.ConvertModulusGreaterThanZero),
        )
    }
}

/**
 * Converts an inequality of the form `|f(x)| < positiveConstant` to a double inequality
 * i.e. `-positiveConstant < f(x) < positiveConstant`
 *
 * for e.g. |3x - 1| < 2 --> -2 < 3x - 1 < 2
 */
private val convertModulusLessThanPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.POSITIVE }

    val inequality = lessThanOf(lhs, rhs)

    onPattern(inequality) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = openRangeOf(negOf(get(rhs)), newLHS, get(rhs)),
            explanation = metadata(Explanation.ConvertModulusLessThanPositiveConstant),
        )
    }
}

/**
 * Converts an inequality of the form `|f(x)| <= positiveConstant` to a double inequality
 * i.e. `-positiveConstant <= f(x) <= positiveConstant`
 *
 * for e.g. |3x - 1| <= 2 --> -2 <= 3x - 1 <= 2
 */
private val convertModulusLessThanEqualToPositiveConstant = rule {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = condition { it.isConstantIn(solutionVariables) && it.signOf() == Sign.POSITIVE }

    val inequality = lessThanEqualOf(lhs, rhs)

    onPattern(inequality) {
        val newLHS = simplifiedProductOf(lhs.getCoefficient(), get(signedLHS))
        ruleResult(
            toExpr = transform(
                closedRangeOf(negOf(get(rhs)), newLHS, get(rhs)),
            ),
            explanation = metadata(Explanation.ConvertModulusLessThanEqualToPositiveConstant),
        )
    }
}
