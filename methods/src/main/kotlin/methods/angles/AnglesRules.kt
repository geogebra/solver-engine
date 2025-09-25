package methods.angles

import engine.expressions.Constants.OneHundredAndEighty
import engine.expressions.Constants.Pi
import engine.expressions.Constants.ThreeHundredAndSixty
import engine.expressions.Constants.Two
import engine.expressions.TrigonometricConstants.MainAnglesDegrees
import engine.expressions.TrigonometricConstants.MainAnglesRadians
import engine.expressions.bracketOf
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.operators.TrigonometricFunctionType
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.OneOfPattern
import engine.patterns.TrigonometricExpressionPattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.degreeOf
import engine.patterns.oneOf
import engine.patterns.withOptionalRationalCoefficient
import engine.steps.metadata.metadata
import java.math.BigInteger

private fun checkFunctionIsNegativeInQuadrant(type: TrigonometricFunctionType, quadrant: Quadrant): Boolean {
    return when (type) {
        TrigonometricFunctionType.Sin -> quadrant != Quadrant.SECOND
        TrigonometricFunctionType.Cos -> quadrant != Quadrant.FOURTH
        TrigonometricFunctionType.Tan -> quadrant != Quadrant.THIRD
        TrigonometricFunctionType.Cot -> quadrant != Quadrant.THIRD
        else -> false
    }
}

enum class AnglesRules(override val runner: Rule) : RunnerMethod {
    UseDegreeConversionFormula(useDegreeConversionFormula),
    UseRadianConversionFormula(useRadianConversionFormula),
    EvaluateExactValueOfMainAngle(evaluateExactValueOfMainAngle),
    FindReferenceAngleInFirstQuadrantInDegree(findReferenceAngleInFirstQuadrantInDegree),
    FindReferenceAngleInFirstQuadrantInRadian(findReferenceAngleInFirstQuadrantInRadian),
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

/**
 * e.g sin[ degree[ 30 ]] -->  [ 1 / 2 ]
 */
private val evaluateExactValueOfMainAngle = rule {
    val valueRadian = OneOfPattern(MainAnglesRadians.keys.map(::FixedPattern))
    val valueDegree = OneOfPattern(MainAnglesDegrees.keys.map(::FixedPattern))

    val value = oneOf(valueRadian, valueDegree)

    val functionPattern = TrigonometricExpressionPattern(
        value,
        listOf(
            TrigonometricFunctionType.Sin,
            TrigonometricFunctionType.Cos,
            TrigonometricFunctionType.Tan,
            TrigonometricFunctionType.Cot,
        ),
    )

    onPattern(functionPattern) {
        val boundAngle = if (isBound(valueRadian)) {
            MainAnglesRadians[get(valueRadian)]
        } else {
            MainAnglesDegrees[get(valueDegree)]
        }

        // Just for type safety, bound angle should always be non-null if the pattern was matched.
        if (boundAngle == null) {
            return@onPattern null
        }

        val toExpr =
            when (getFunctionType(functionPattern)) {
                TrigonometricFunctionType.Sin -> boundAngle.sine
                TrigonometricFunctionType.Cos -> boundAngle.cosine
                TrigonometricFunctionType.Tan -> boundAngle.tangent
                TrigonometricFunctionType.Cot -> boundAngle.cotangent
                else -> null
            }

        if (toExpr == null) {
            return@onPattern null
        }

        ruleResult(
            toExpr = transform(toExpr),
            explanation = metadata(Explanation.EvaluateExactValueOfMainAngle),
        )
    }
}

enum class Quadrant { SECOND, THIRD, FOURTH }

/**
 * Find the reference angle and the right sign in first quadrant eg:
 * cos 120 -> - cos 60
 */
private val findReferenceAngleInFirstQuadrantInDegree = rule {
    val innerValueDegree = UnsignedNumberPattern()
    val value = degreeOf(innerValueDegree)
    val pattern = withOptionalRationalCoefficient(value)

    val functionPattern = TrigonometricExpressionPattern(
        pattern,
        listOf(
            TrigonometricFunctionType.Sin,
            TrigonometricFunctionType.Cos,
            TrigonometricFunctionType.Tan,
            TrigonometricFunctionType.Cot,
        ),
    )

    onPattern(functionPattern) {
        val coefficient = getCoefficientValue(pattern)
        val fourNumerator =
            4.toBigInteger() *
                (coefficient?.numerator ?: 1.toBigInteger()) *
                getValue(innerValueDegree).toBigInteger()

        val denominator = 360.toBigInteger() * (coefficient?.denominator ?: BigInteger.ONE)

        val quadrant = when (fourNumerator) {
            in denominator..BigInteger.TWO * denominator -> Quadrant.SECOND
            in BigInteger.TWO * denominator..3.toBigInteger() * denominator -> Quadrant.THIRD
            in 3.toBigInteger() * denominator..4.toBigInteger() * denominator -> Quadrant.FOURTH
            else -> null
        }

        if (quadrant == null) {
            return@onPattern null
        }

        // To find the equivalent angle in the first quadrant:
        // 2nd quadrant -> 180 - angle
        // 3rd quadrant -> angle - 180
        // 4th quadrant -> 360 - angle
        val referenceAngle = when (quadrant) {
            Quadrant.SECOND -> sumOf(introduce(engine.expressions.degreeOf(OneHundredAndEighty)), negOf(move(pattern)))
            Quadrant.THIRD -> sumOf(move(pattern), introduce(negOf(engine.expressions.degreeOf(OneHundredAndEighty))))
            Quadrant.FOURTH -> sumOf(introduce(engine.expressions.degreeOf(ThreeHundredAndSixty)), negOf(move(pattern)))
        }

        val negative = checkFunctionIsNegativeInQuadrant(getFunctionType(functionPattern), quadrant)

        val toExpression =
            wrapWithTrigonometricFunction(functionPattern, bracketOf(referenceAngle)).let {
                if (negative) negOf(it) else it
            }

        ruleResult(
            toExpr = toExpression,
            explanation = metadata(Explanation.FindReferenceAngle),
        )
    }
}

/*
    Find the reference angle and the right sign in first quadrant eg:
    cos [2 pi / 3] -> - cos [1 pi / 3]
 */
private val findReferenceAngleInFirstQuadrantInRadian = rule {
    val valueRadian = withOptionalRationalCoefficient(FixedPattern(Pi))

    val functionPattern = TrigonometricExpressionPattern(
        valueRadian,
        listOf(
            TrigonometricFunctionType.Sin,
            TrigonometricFunctionType.Cos,
            TrigonometricFunctionType.Tan,
            TrigonometricFunctionType.Cot,
        ),
    )

    onPattern(functionPattern) {
        val coefficient = getCoefficientValue(valueRadian)

        if (coefficient == null) {
            return@onPattern null
        }

        // To avoid floating point operations we can use multiples of these values to check
        val twoNumerator = BigInteger.TWO * coefficient.numerator
        val twoDenominator = BigInteger.TWO * coefficient.denominator
        val threeDenominator = 3.toBigInteger() * coefficient.denominator

        val quadrant = when (twoNumerator) {
            in coefficient.denominator..twoDenominator -> Quadrant.SECOND
            in twoDenominator..threeDenominator -> Quadrant.THIRD
            in threeDenominator..4.toBigInteger() * coefficient.denominator -> Quadrant.FOURTH
            else -> null
        }

        if (quadrant == null) {
            return@onPattern null
        }

        // To find the equivalent angle in the first quadrant:
        // 2nd quadrant -> pi - angle
        // 3rd quadrant -> angle - pi
        // 4th quadrant -> 2pi - angle
        val referenceAngle = when (quadrant) {
            Quadrant.SECOND -> sumOf(introduce(Pi), negOf(move(valueRadian)))
            Quadrant.THIRD -> sumOf(move(valueRadian), introduce(negOf(Pi)))
            Quadrant.FOURTH -> sumOf(introduce(productOf(Two, Pi)), negOf(move(valueRadian)))
        }

        val negative = checkFunctionIsNegativeInQuadrant(getFunctionType(functionPattern), quadrant)

        val toExpression =
            wrapWithTrigonometricFunction(functionPattern, bracketOf(referenceAngle)).let {
                if (negative) negOf(it) else it
            }

        ruleResult(
            toExpr = toExpression,
            explanation = metadata(Explanation.FindReferenceAngle),
        )
    }
}
