package engine.utility

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Rounds off a decimal if necessary to a maximum of [dp] decimal places without making the number of significant
 * figures go below [dp].
 */
fun BigDecimal.withMaxDP(dp: Int): BigDecimal {
    return when {
        scale() <= dp -> this
        scale() <= precision() -> setScale(dp, RoundingMode.HALF_UP)
        else -> round(MathContext(dp, RoundingMode.HALF_UP))
    }
}
