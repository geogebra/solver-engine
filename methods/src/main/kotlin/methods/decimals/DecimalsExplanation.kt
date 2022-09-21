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
    EvaluateDecimalDivision;

    override val category = "Decimals"
}

typealias Explanation = DecimalsExplanation
