package methods.decimals

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class DecimalsExplanation : CategorisedMetadataKey {
    /**
     * Convert a terminating decimal to a fraction and then simplify the result
     */
    ConvertTerminatingDecimalToFractionAndSimplify,

    /**
     * Convert a recurring decimal to a fraction and then simplify the result
     */
    ConvertRecurringDecimalToFractionAndSimplify,

    /**
     * Give a variable name to a recurring decimal so we can convert it to a fraction using algebra
     */
    ConvertRecurringDecimalToEquation,

    /**
     * Deduce a system of equations for a recurring decimal that can be solved to find its value as a fraction
     */
    MakeEquationSystemForRecurringDecimal,

    /**
     * Multiply the equation of the `x = recurringDecimal` with a given power of ten.
     */
    MultiplyRecurringDecimal,

    /**
     * Deduce a linear equation with integer coefficients from two equations for a recurring decimal
     */
    SimplifyEquationSystemForRecurringDecimal,

    /**
     * Solve a linear equation in the form ax = b by dividing through by a
     */
    SolveLinearEquation,

    /**
     * Determine the fractional representation of the recurring decimal in one step using
     * the formula a.b(c) = (abc - ab) / 99..90..0
     */
    ConvertRecurringDecimalToFractionDirectly,

    /**
     * Convert a terminating decimal to a fraction whose denominator is a power of 10.
     */
    ConvertTerminatingDecimalToFraction,

    NormalizeFractionOfDecimals,
    SimplifyDecimalsInProduct,
    MultiplyFractionOfDecimalsByPowerOfTen,
    EvaluateDecimalProduct,
    EvaluateDecimalDivision,

    /**
     * Evaluate the sum or difference of several (decimal) numbers
     */
    EvaluateSumOfDecimals,

    /**
     * Evaluate the product / division of several (decimal) numbers
     */
    EvaluateProductOfDecimals,

    /**
     * Evaluate the addition of two (decimal) numbers
     */
    EvaluateDecimalAddition,

    /**
     * Evaluate the subtraction of two (decimal) numbers
     */
    EvaluateDecimalSubtraction,

    /**
     * Convert a fraction to a terminating decimal if possible
     *
     * E.g. [7/25 -> 0.28]
     */
    ConvertNiceFractionToDecimal,

    /**
     * Scale a fraction so its denominator will be a power of 10
     *
     * E.g. [3/25] -> []12 * 4/25 * 4]
     */
    ExpandFractionToPowerOfTenDenominator,

    /**
     * Convert a fraction over a power of 10 to a terminating decimal
     *
     * E.g. [45/100] -> 0.45
     */
    ConvertFractionWithPowerOfTenDenominatorToDecimal,

    /**
     * Rewrite a division of decimals as a fraction, with a view to converting it back into a decimal
     *
     * E.g. 0.1 : 0.05 -> [0.1 / 0.05]
     */
    TurnDivisionOfDecimalsIntoFraction,

    /**
     * Evaluate a decimal to an integer power
     * E.g. [0.1 ^ 3] -> 0.001
     */
    EvaluateDecimalPower,

    /**
     * Evaluate a decimal to an integer power directly
     *
     * %1: the base
     * %2: the power
     */
    EvaluateDecimalPowerDirectly,

    /**
     * Evaluate the expression in brackets as a decimal
     */
    EvaluateExpressionInBracketsAsDecimal,

    /**
     * Evaluate the expression as a decimal
     */
    EvaluateExpressionAsDecimal;

    override val category = "Decimals"
}

typealias Explanation = DecimalsExplanation
