package methods.approximation

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class ApproximationExplanation : CategorisedMetadataKey {
    /**
     * Round a terminating decimal to a specified number of decimal places
     * %1 - original decimal number, such as 4.7548
     * %2 - decimal places to round to, such as 3
     * Results in 4.755 in the above example.
     */
    RoundTerminatingDecimal,

    /**
     * Expand a recurring decimal so that it has at least the specified number
     * of decimal places before the repetend
     * %1 - original recurring decimal number, such as 3.14[15]
     * %2 - decimal places to extend to, such as 3
     * Results in 3.1415[15] in the above example
     */
    ExpandRecurringDecimal,

    /**
     * Round a recurring decimal to a specified number of decimal places
     */
    RoundRecurringDecimal,

    /**
     * Expand and round a recurring decimal
     */
    ExpandAndRoundRecurringDecimal,

    /**
     * Approximate the product or division of several (decimal) numbers
     */
    ApproximateProductAndDivisionOfDecimals,

    /**
     * Approximate the product of two (decimal) numbers
     * %1 - first number
     * %2 - second number
     * %3 - number of decimal places to approximate to
     */
    ApproximateDecimalProduct,

    /**
     * Approximate the division of two (decimal) numbers
     * %1 - first number
     * %2 - second number
     * %3 - number of decimal places to approximate to
     */
    ApproximateDecimalDivision,

    /**
     * Approximate the decimal power
     * %1 - base (decimal number)
     * %2 - exponent (integer)
     * %3 - number of decimal places to approximate to
     */
    ApproximateDecimalPower,

    /**
     * Approximate the subexpression inside the brackets
     */
    ApproximateExpressionInBrackets,

    /**
     * Approximate a numeric expression to given number of decimal places
     */
    ApproximateExpression,

    ;

    override val category = "Approximation"
}

typealias Explanation = ApproximationExplanation
