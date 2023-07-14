package engine.sign

private const val NOT_KNOWN_SIGNUM = 10

/**
 * The sign of a mathematical expression
 */
enum class Sign(val signum: Int, val canBeZero: Boolean = false) {
    /**
     * Negative and not zero
     */
    NEGATIVE(-1),

    NON_POSITIVE(-1, true),

    /**
     * Only zero
     */
    ZERO(0, true),

    /**
     * Positive and not zero
     */
    POSITIVE(1),

    NON_NEGATIVE(1, true),

    NOT_ZERO(NOT_KNOWN_SIGNUM, false),

    /**
     * Sign unknown
     */
    UNKNOWN(NOT_KNOWN_SIGNUM, true),

    /**
     * No sign (e.g. undefined values have no sign)
     */
    NONE(NOT_KNOWN_SIGNUM),
    ;

    fun isKnown() = signum != NOT_KNOWN_SIGNUM

    fun inverse() = if (canBeZero || !isKnown()) NONE else this

    operator fun unaryMinus() = when (this) {
        POSITIVE -> NEGATIVE
        NEGATIVE -> POSITIVE
        NON_NEGATIVE -> NON_POSITIVE
        NON_POSITIVE -> NON_NEGATIVE
        else -> this
    }

    fun truncateToPositive() = when (this) {
        POSITIVE, NON_NEGATIVE, UNKNOWN -> this
        NOT_ZERO -> UNKNOWN
        else -> NONE
    }

    operator fun times(other: Sign) = when {
        this == NONE || other == NONE -> NONE
        this == ZERO || other == ZERO -> ZERO
        this == UNKNOWN || other == UNKNOWN -> UNKNOWN
        this == NOT_ZERO || other == NOT_ZERO -> NOT_ZERO.orMaybeZero(canBeZero || other.canBeZero)
        else -> fromInt(signum * other.signum, canBeZero || other.canBeZero)
    }

    operator fun div(other: Sign) = this * other.inverse()

    operator fun plus(other: Sign) = when {
        this == NONE || other == NONE -> NONE
        !this.isKnown() || !other.isKnown() -> UNKNOWN
        other == ZERO -> this
        this == ZERO -> other
        this.signum == other.signum -> Sign.fromInt(this.signum, (other.canBeZero && this.canBeZero))
        else -> UNKNOWN
    }

    operator fun minus(other: Sign) = this + (-other)

    internal fun orMaybeZero(canBeZero: Boolean = false) = if (!canBeZero) {
        this
    } else {
        when (this) {
            NEGATIVE -> NON_POSITIVE
            POSITIVE -> NON_NEGATIVE
            NOT_ZERO -> UNKNOWN
            else -> this
        }
    }
    companion object {
        fun fromInt(s: Int, canBeZero: Boolean = false) = when {
            s < 0 -> NEGATIVE
            s == 0 -> ZERO
            else -> POSITIVE
        }.orMaybeZero(canBeZero)
    }
}
