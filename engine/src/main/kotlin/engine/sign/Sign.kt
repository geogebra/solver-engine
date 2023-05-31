package engine.sign

private const val NOT_KNOWN_SIGNUM = 10

/**
 * The sign of a mathematical expression
 */
enum class Sign(val signum: Int) {
    /**
     * Negative and not zero
     */
    NEGATIVE(-1),

    /**
     * Only zero
     */
    ZERO(0),

    /**
     * Positive and not zero
     */
    POSITIVE(1),

    /**
     * Sign unknown
     */
    UNKNOWN(NOT_KNOWN_SIGNUM),

    /**
     * No sign (e.g. undefined values have no sign)
     */
    NONE(NOT_KNOWN_SIGNUM),
    ;

    fun isKnown() = signum != NOT_KNOWN_SIGNUM

    fun inverse() = when (this) {
        POSITIVE -> POSITIVE
        NEGATIVE -> NEGATIVE
        else -> NONE
    }

    operator fun unaryMinus() = when (this) {
        POSITIVE -> NEGATIVE
        NEGATIVE -> POSITIVE
        else -> this
    }

    fun truncateToPositive() = when (this) {
        NEGATIVE -> NONE
        else -> this
    }

    operator fun times(other: Sign) = when {
        this == NONE || other == NONE -> NONE
        this == ZERO || other == ZERO -> ZERO
        this == UNKNOWN || other == UNKNOWN -> UNKNOWN
        else -> fromInt(signum * other.signum)
    }

    operator fun div(other: Sign) = this * other.inverse()

    operator fun plus(other: Sign) = when {
        this == NONE || other == NONE -> NONE
        this == UNKNOWN || other == UNKNOWN -> UNKNOWN
        this == other || other == ZERO -> this
        this == ZERO -> other
        else -> UNKNOWN
    }

    companion object {
        fun fromInt(s: Int) = when {
            s < 0 -> NEGATIVE
            s == 0 -> ZERO
            s > 0 -> POSITIVE
            else -> NONE
        }
    }
}
