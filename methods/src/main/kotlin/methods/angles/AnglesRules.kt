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
import engine.patterns.withOptionalRationalCoefficient
import engine.steps.metadata.metadata

enum class AnglesRules(override val runner: Rule) : RunnerMethod {
    UseDegreeConversionFormula(useDegreeConversionFormula),
    UseRadianConversionFormula(useRadianConversionFormula),
}

/**
 * degree[360] --> degree[360] * [ /pi/  / 180]
 */
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

/**
 * [x * /pi/ / y] --> [x * /pi/ / y] * [degree[180] / /pi/]
 */
private val useRadianConversionFormula = rule {
    val pi = FixedPattern(Pi)
    val pattern = withOptionalRationalCoefficient(pi, false)

    onPattern(pattern) {
        ruleResult(
            toExpr = productOf(move(pattern), fractionOf(engine.expressions.degreeOf(OneHundredAndEighty), Pi)),
            explanation = metadata(Explanation.UseRadianConversionFormula),
        )
    }
}
