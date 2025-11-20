package methods.angles

import engine.expressions.Constants
import engine.expressions.Constants.Ninety
import engine.expressions.Constants.OneHundredAndEighty
import engine.expressions.Constants.Pi
import engine.expressions.Constants.ThreeHundredAndSixty
import engine.expressions.Constants.Two
import engine.expressions.Constants.Zero
import engine.expressions.DivideBy
import engine.expressions.Expression
import engine.expressions.Fraction
import engine.expressions.TrigonometricConstants
import engine.expressions.TrigonometricConstants.MainAnglesDegrees
import engine.expressions.TrigonometricConstants.MainAnglesRadians
import engine.expressions.TrigonometricExpression
import engine.expressions.degreeOf
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.operators.TrigonometricFunctionType
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.IntegerConditionPattern
import engine.patterns.OneOfPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.TrigonometricExpressionPattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.commutativeProductOf
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.degreeOf
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.productContaining
import engine.patterns.stickyOptionalNegOf
import engine.patterns.withOptionalRationalCoefficient
import engine.steps.metadata.metadata
import engine.utility.Rational
import java.math.BigDecimal
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
    MovePiIntoNumerator(movePiIntoNumerator),
    EvaluateInverseFunctionOfMainAngle(evaluateInverseFunctionOfMainAngle),
    EvaluateExactValueOfMainAngle(evaluateExactValueOfMainAngle),
    RewriteAngleInDegreesByExtractingMultiplesOf360(rewriteAngleInDegreesByExtractingMultiplesOf360),
    RewriteAngleInRadiansByExtractingMultiplesOfTwoPi(rewriteAngleInRadiansByExtractingMultiplesOfTwoPi),
    SubstituteAngleWithCoterminalAngleFromUnitCircle(substituteAngleWithCoterminalAngleFromUnitCircle),
    FindReferenceAngleInFirstQuadrantInDegree(findReferenceAngleInFirstQuadrantInDegree),
    FindReferenceAngleInFirstQuadrantInRadian(findReferenceAngleInFirstQuadrantInRadian),
    CheckDomainOfFunction(checkDomainOfFunction),
    DeriveTrigonometricFunctionFromPrimitiveFunctions(deriveTrigonometricFunctionFromPrimitiveFunctions),
    EvaluateProductContainingTrigonometricExpressionsAsZero(evaluateProductContainingTrigonometricExpressionsAsZero),
}

/**
 * degree[360] --> degree[360] * [ /pi/  / 180]
 */
private val useDegreeConversionFormula = rule {
    val value = AnyPattern()
    val pattern = withOptionalRationalCoefficient(degreeOf(value))

    onPattern(pattern) {
        ruleResult(
            toExpr = productOf(
                move(pattern),
                introduce(fractionOf(Pi, degreeOf(OneHundredAndEighty))),
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
            toExpr = productOf(move(pattern), fractionOf(degreeOf(OneHundredAndEighty), Pi)),
            explanation = metadata(Explanation.UseRadianConversionFormula),
        )
    }
}

/**
 * [x / y] * /pi/ --> [x /pi/ / y]
 */
private val movePiIntoNumerator = rule {
    val pi = FixedPattern(Pi)
    val numerator = SignedIntegerPattern()
    val denominator = SignedIntegerPattern()
    val fraction = engine.patterns.fractionOf(numerator, denominator)
    val pattern = commutativeProductOf(pi, fraction)

    onPattern(pattern) {
        ruleResult(
            toExpr = fractionOf(productOf(move(numerator), move(pi)), move(denominator)),
            explanation = metadata(Explanation.RewriteAngleInRadiansAsSingleFraction),
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

/**
 * e.g asin(1) --> [/pi/ / 2]
 */
private val evaluateInverseFunctionOfMainAngle = rule {
    val sinValues = OneOfPattern(TrigonometricConstants.MainAngles.entries.map { FixedPattern(it.sine) })
    val tanValues = OneOfPattern(TrigonometricConstants.MainAngles.entries.map { FixedPattern(it.tangent) })

    val functionSine = TrigonometricExpressionPattern(
        sinValues,
        listOf(
            TrigonometricFunctionType.Arcsin,
            TrigonometricFunctionType.Arccos,
        ),
    )
    val functionTangent = TrigonometricExpressionPattern(
        tanValues,
        listOf(
            TrigonometricFunctionType.Arctan,
            TrigonometricFunctionType.Arccot,
        ),
    )

    val pattern = oneOf(functionSine, functionTangent)

    onPattern(pattern) {
        val functionType = when {
            isBound(functionSine) -> getFunctionType(functionSine)
            isBound(functionTangent) -> getFunctionType(functionTangent)
            else -> null
        }

        val angle = TrigonometricConstants.MainAngles.entries.firstOrNull {
            when (functionType) {
                TrigonometricFunctionType.Arcsin -> it.sine == get(sinValues)
                TrigonometricFunctionType.Arccos -> it.cosine == get(sinValues)
                TrigonometricFunctionType.Arctan -> it.tangent == get(tanValues)
                TrigonometricFunctionType.Arccot -> it.cotangent == get(tanValues)
                else -> false
            }
        } ?: return@onPattern null

        val toExpr =
            MainAnglesRadians.firstNotNullOfOrNull { e -> e.key.takeIf { e.value == angle } } ?: return@onPattern null

        ruleResult(
            toExpr = transform(pattern, toExpr),
            explanation = metadata(Explanation.DetermineMainAnglePrincipalValueOfInverseFunction),
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
            Quadrant.SECOND -> sumOf(introduce(degreeOf(OneHundredAndEighty)), negOf(move(pattern)))
            Quadrant.THIRD -> sumOf(move(pattern), introduce(negOf(degreeOf(OneHundredAndEighty))))
            Quadrant.FOURTH -> sumOf(introduce(degreeOf(ThreeHundredAndSixty)), negOf(move(pattern)))
        }

        val negative = checkFunctionIsNegativeInQuadrant(getFunctionType(functionPattern), quadrant)

        val toExpression =
            wrapWithTrigonometricFunction(functionPattern, referenceAngle).let {
                if (negative) negOf(it) else it
            }

        ruleResult(
            toExpr = toExpression,
            explanation = metadata(Explanation.FindReferenceAngle),
        )
    }
}

/**
 *    Find the reference angle and the right sign in first quadrant eg:
 *    cos [2 pi / 3] -> - cos [1 pi / 3]
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
        val coefficient = getCoefficientValue(valueRadian) ?: return@onPattern null

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
            wrapWithTrigonometricFunction(functionPattern, referenceAngle).let {
                if (negative) negOf(it) else it
            }

        ruleResult(
            toExpr = toExpression,
            explanation = metadata(Explanation.FindReferenceAngle),
        )
    }
}

/**
 * Replace derived trigonometric function with primitive trigonometric functions.
 * Only handles sec and csc at the moment, as tan and cot are included in the main angles rules.
 *
 * e.g. sec degree[30] -> [1 / cos degree[30]]
 */
private val deriveTrigonometricFunctionFromPrimitiveFunctions = rule {
    val argumentPattern = AnyPattern()
    val functionPattern = TrigonometricExpressionPattern(
        argumentPattern,
        listOf(TrigonometricFunctionType.Sec, TrigonometricFunctionType.Csc),
    )

    onPattern(functionPattern) {
        val match = get(functionPattern) as TrigonometricExpression

        val toExpr = when (getFunctionType(functionPattern)) {
            TrigonometricFunctionType.Sec -> fractionOf(
                Constants.One,
                TrigonometricExpression(
                    operand = move(argumentPattern),
                    functionType = TrigonometricFunctionType.Cos,
                    powerInside = match.powerInside,
                    inverseNotation = match.inverseNotation,
                ),
            )
            TrigonometricFunctionType.Csc -> fractionOf(
                Constants.One,
                TrigonometricExpression(
                    operand = move(argumentPattern),
                    functionType = TrigonometricFunctionType.Sin,
                    powerInside = match.powerInside,
                    inverseNotation = match.inverseNotation,
                ),
            )
            else -> null
        }

        if (toExpr == null) {
            return@onPattern null
        }

        ruleResult(
            toExpr = toExpr,
            explanation = metadata(Explanation.DeriveTrigonometricFunctionFromPrimitiveFunctions),
        )
    }
}

/**
 * Rewrite angle in degrees by extracting multiples of 360 degrees.
 * e.g degree[390] -> degree[360] + degree[30]
 */
@Suppress("MagicNumber")
private val rewriteAngleInDegreesByExtractingMultiplesOf360 = rule {
    val angleVal = SignedNumberPattern()
    val angleValCondition = numericCondition(angleVal) { it.abs() > BigDecimal.valueOf(360) }
    val angleDegree = degreeOf(angleValCondition)
    // Prevent simplifying just the numerator
    val pattern = condition(angleDegree) { it.parent !is Fraction }

    onPattern(pattern) {
        val coefficient = getValue(angleVal).toBigInteger() / 360.toBigInteger()
        val remainder = numericOp(angleVal) { it % BigDecimal.valueOf(360) }

        val coefficientExpression =
            if (coefficient == BigInteger.ONE) {
                degreeOf(ThreeHundredAndSixty)
            } else {
                productOf(xp(coefficient), degreeOf(ThreeHundredAndSixty))
            }

        ruleResult(
            toExpr = transform(angleDegree, sumOf(coefficientExpression, distribute(degreeOf(remainder)))),
            explanation = metadata(Explanation.RewriteAngleInDegreesByExtractingMultiplesOf360),
        )
    }
}

/**
 * Substitute angle with coterminal angle from unit circle.
 * e.g:
 * - degree[360] + degree[30] -> degree[30]
 * - 2 /pi/ + [1 /pi/ / 2] -> degree[1 /pi/ / 2]
 * - 2 /pi/ + 0 -> 0
 */
private val substituteAngleWithCoterminalAngleFromUnitCircle = rule {
    val angleValDegrees = degreeOf(AnyPattern())
    val angleValRadians = withOptionalRationalCoefficient(FixedPattern(Pi), true)
    // We explicitly add the 0 case to be able to run directly after extracting multiples of 360
    val angleVal = oneOf(
        angleValDegrees,
        angleValRadians,
        withOptionalRationalCoefficient(FixedPattern(Zero)),
    )

    // We add an optional integer operand to the circle to also cancel out multiples of 2pi/360
    val circleDegrees = degreeOf(FixedPattern(ThreeHundredAndSixty)).let {
        oneOf(it, commutativeProductOf(SignedIntegerPattern(), it))
    }
    val circleRadians =
        oneOf(
            commutativeProductOf(FixedPattern(Two), FixedPattern(Pi)),
            commutativeProductOf(FixedPattern(Two), FixedPattern(Pi), SignedIntegerPattern()),
        )

    val circle = oneOf(circleDegrees, circleRadians)

    val sum = commutativeSumOf(angleVal, circle)

    onPattern(sum) {
        ruleResult(
            toExpr = sum.substitute(move(angleVal)),
            explanation = metadata(Explanation.SubstituteAngleWithCoterminalAngleFromUnitCircle),
        )
    }
}

fun isWithinFraction(exp: Expression): Boolean {
    val parent = exp.parent

    return if (parent == null) {
        false
    } else {
        parent is Fraction || isWithinFraction(parent)
    }
}

/**
 * Rewrite angle in radians by extracting multiples of 2*Pi from the angle.
 * e.g [5 /pi/ / 2] -> 2 /pi/ + [1 /pi/ / 2]
 */
private val rewriteAngleInRadiansByExtractingMultiplesOfTwoPi = rule {
    val angle = withOptionalRationalCoefficient(FixedPattern(Pi))
    val pattern = condition(angle) { it.parent is TrigonometricExpression || !isWithinFraction(it) }

    onPattern(pattern) {
        val coefficientRational = getCoefficientValue(angle) ?: return@onPattern null

        val numerator = coefficientRational.numerator
        val twoDenominator = coefficientRational.denominator * BigInteger.TWO

        val piCoefficient = numerator / twoDenominator
        val remainder = numerator % twoDenominator

        if (piCoefficient == BigInteger.ZERO || (piCoefficient == BigInteger.ONE && remainder == BigInteger.ZERO)) {
            return@onPattern null
        }

        // Hide coefficient if it is 1
        val resultNumerator = when (remainder.abs()) {
            BigInteger.ZERO -> Zero
            BigInteger.ONE -> Pi
            else -> productOf(xp(remainder.abs()), Pi)
        }

        // Only add numerator if it is not 1
        val result = coefficientRational.denominator.let {
            if (it == BigInteger.ONE) {
                resultNumerator
            } else {
                fractionOf(
                    resultNumerator,
                    xp(it),
                )
            }
        }

        // Hide coefficient if it is 1
        val extracted = if (piCoefficient == BigInteger.ONE) {
            productOf(Two, Pi)
        } else {
            productOf(xp(piCoefficient), Two, Pi)
        }

        ruleResult(
            toExpr = transform(
                angle,
                sumOf(
                    extracted,
                    if (remainder < BigInteger.ZERO) {
                        negOf(result)
                    } else {
                        result
                    },
                ),
            ),
            explanation = metadata(Explanation.RewriteAngleInRadiansByExtractingMultiplesOfTwoPi),
        )
    }
}

@Suppress("MagicNumber")
private val checkDomainOfFunction = rule {
    val angleDegree = IntegerConditionPattern(SignedIntegerPattern()) {
        it.remainder(90.toBigInteger()) == BigInteger.ZERO
    }
    val zeroAngle = FixedPattern(Zero)
    val angleRadians = withOptionalRationalCoefficient(FixedPattern(Pi))

    val pattern =
        TrigonometricExpressionPattern(
            oneOf(degreeOf(angleDegree), angleRadians, zeroAngle),
            listOf(
                TrigonometricFunctionType.Tan,
                TrigonometricFunctionType.Cot,
                TrigonometricFunctionType.Sec,
                TrigonometricFunctionType.Csc,
            ),
        )

    onPattern(pattern) {
        val explanationKey = when (getFunctionType(pattern)) {
            TrigonometricFunctionType.Tan -> Explanation.CheckDomainOfTangent
            TrigonometricFunctionType.Cot -> Explanation.CheckDomainOfCotangent
            TrigonometricFunctionType.Sec -> Explanation.CheckDomainOfSecant
            TrigonometricFunctionType.Csc -> Explanation.CheckDomainOfCosecant
            else -> null
        }

        if (explanationKey == null) {
            return@onPattern null
        }

        // If function is Tan or Sec we want odd multiples of π/2, otherwise even multiples of π/2.
        val expectedMultiplier =
            if (getFunctionType(pattern) == TrigonometricFunctionType.Tan ||
                getFunctionType(pattern) == TrigonometricFunctionType.Sec
            ) {
                BigInteger.ONE
            } else {
                BigInteger.ZERO
            }

        if (isBound(angleRadians) || isBound(zeroAngle)) {
            val coefficient = if (isBound(angleRadians)) {
                getCoefficientValue(angleRadians)
            } else {
                Rational(
                    BigInteger.ZERO,
                    BigInteger.ONE,
                )
            }

            // Adjust the numerator to easily check for multiples of π/2
            val numeratorAdjusted = (coefficient?.numerator ?: BigInteger.ONE) * BigInteger.TWO
            val denominator = coefficient?.denominator ?: BigInteger.ONE

            val remainder = numeratorAdjusted.remainder(denominator)
            val multiplier = (numeratorAdjusted / denominator).mod(BigInteger.TWO)

            if (remainder != BigInteger.ZERO || multiplier != expectedMultiplier) {
                return@onPattern null
            }
        } else {
            val multiplier = (getValue(angleDegree) / 90.toBigInteger()).mod(BigInteger.TWO)
            if (multiplier != expectedMultiplier) {
                return@onPattern null
            }
        }

        val explanationParameter = if (isBound(angleRadians)) {
            if (expectedMultiplier == BigInteger.ONE) {
                fractionOf(Pi, Two)
            } else {
                Pi
            }
        } else {
            if (expectedMultiplier == BigInteger.ONE) {
                degreeOf(Ninety)
            } else {
                degreeOf(OneHundredAndEighty)
            }
        }

        ruleResult(
            toExpr = transformTo(pattern, Constants.Undefined),
            explanation = metadata(explanationKey, explanationParameter),
        )
    }
}

private fun checkIsDefinitelyNotUndefinedOrTrigFunction(expr: Expression): Boolean {
    return expr.isDefinitelyNotUndefined() ||
        expr is TrigonometricExpression ||
        expr.children.all { checkIsDefinitelyNotUndefinedOrTrigFunction(it) }
}

/**
 * Evaluate to zero while not checking if trigonometric functions are undefined
 * The engine itself doesn't have a way to know if a trigonometric function is undefined, without
 * the use of rules, so we try to apply those rules first, and if they fail, we can evaluate the product
 * as zero
 */
private val evaluateProductContainingTrigonometricExpressionsAsZero = rule {
    val zero = FixedPattern(Zero)
    val p = productContaining(zero)
    val pattern = condition(p) { expression ->
        expression.children.all {
            it !is DivideBy && checkIsDefinitelyNotUndefinedOrTrigFunction(it)
        }
    }

    val optionalNegProduct = stickyOptionalNegOf(pattern, initialPositionOnly = true)

    onPattern(optionalNegProduct) {
        ruleResult(
            toExpr = move(zero),
            gmAction = tap(zero),
            explanation = metadata(methods.general.Explanation.EvaluateProductContainingZero, move(zero)),
        )
    }
}
