package methods.angles

import engine.expressions.Constants.OneHundredAndEighty
import engine.expressions.Constants.Pi
import engine.expressions.fractionOf
import engine.expressions.productOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.degreeOf
import engine.patterns.fractionOf
import engine.patterns.productOf
import engine.steps.metadata.metadata

enum class AnglesRules(override val runner: Rule) : RunnerMethod {
    UseDegreeConversionFormula(useDegreeConversionFormula),
    SimplifyDegrees(simplifyDegrees),
}

private val useDegreeConversionFormula = rule {
    val value = AnyPattern()
    val pattern = degreeOf(value)

    onPattern(pattern) {
        ruleResult(
            toExpr = productOf(
                move(pattern),
                introduce(fractionOf(Pi, engine.expressions.degreeOf(OneHundredAndEighty))),
            ),
            explanation = metadata(Explanation.UseDegreeConversionFormula),
        )
    }
}

private val simplifyDegrees = rule {
    val nominatorVal = AnyPattern()
    val denominatorVal = AnyPattern()
    val nominator = degreeOf(nominatorVal)
    val denominator = degreeOf(denominatorVal)
    val pi = FixedPattern(Pi)

    val pattern = productOf(nominator, fractionOf(pi, denominator))

    onPattern(pattern) {
        ruleResult(
            toExpr = productOf(move(nominatorVal), fractionOf(get(pi), move(denominatorVal))),
            explanation = metadata(Explanation.SimplifyDegrees),
        )
    }
}
