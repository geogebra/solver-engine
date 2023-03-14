package engine.utility

import java.math.BigDecimal

private const val SIGNIFICANT_DIGITS_IN_DOUBLE = 15

data class RecurringDecimal(
    val nonRepeatingValue: BigDecimal,
    val repeatingDigits: Int,
) {
    init {
        require(nonRepeatingValue.signum() > 0)
        require(repeatingDigits > 0)
        require(repeatingDigits <= nonRepeatingValue.scale())
    }

    val decimalDigits = nonRepeatingValue.scale()
    val nonRepeatingDigits = decimalDigits - repeatingDigits
    val repetend = nonRepeatingValue
        .remainder(BigDecimal.ONE.movePointLeft(decimalDigits - repeatingDigits))

    constructor(s: String, repeatingDigits: Int) : this(BigDecimal(s), repeatingDigits)

    private fun expandToDecimal(n: Int): BigDecimal {
        var decimal = nonRepeatingValue
        // E.g. 0.065 for 3.5[65]
        var repetend = repetend

        while (decimal.scale() < n) {
            repetend = repetend.movePointLeft(repeatingDigits)
            decimal += repetend
        }
        return decimal
    }

    /**
     * Returns a RecurringDecimal equivalent to the current such that
     * its [decimalDigits] are at least [n]
     */
    fun expand(n: Int) = RecurringDecimal(expandToDecimal(n), repeatingDigits)

    /**
     * Moves the decimal point [n] places to the right, expanding the recurring decimal as necessary.
     */
    fun movePointRight(n: Int): RecurringDecimal {
        return when {
            n <= decimalDigits - repeatingDigits -> RecurringDecimal(
                nonRepeatingValue.movePointRight(n),
                repeatingDigits,
            )

            else -> expand(n + repeatingDigits).movePointRight(n)
        }
    }

    override fun toString(): String {
        val s = nonRepeatingValue.toPlainString()
        val repeatingStartIndex = s.length - repeatingDigits
        return "${s.substring(0, repeatingStartIndex)}[${s.substring(repeatingStartIndex)}]"
    }

    /**
     * Convert to a Double
     */
    fun toDouble() = expandToDecimal(SIGNIFICANT_DIGITS_IN_DOUBLE).toDouble()
}
