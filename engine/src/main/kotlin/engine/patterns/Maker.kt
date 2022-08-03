package engine.patterns

import engine.expressions.Subexpression
import java.math.BigInteger

/**
 * Can build an instance of T from a match.  Examples are ExpressionMaker and MetadataMaker.
 */
interface Maker<T> {
    /**
     * Returns a new instance of T build from the given match.
     */
    fun make(match: Match): T
}

/**
 * Provides convenience methods for accessing data from a given Match instance.
 * It is used by the CustomMaker class
 */
class CustomMakerBuilder(private val match: Match) {
    /**
     * Returns true if the given pattern is bound to a value in the match.
     */
    fun isBound(pattern: Pattern): Boolean {
        return match.getLastBinding(pattern) != null
    }

    /**
     * Returns the last subexpression bound to pattern
     */
    fun get(pattern: Pattern): Subexpression? {
        return match.getLastBinding(pattern)
    }

    /**
     * Returns the integer value bound to the argument in the match.
     */
    fun getValue(integerPattern: IntegerPattern): BigInteger {
        return integerPattern.getBoundInt(match)
    }
}

/**
 * Provides a DSL to create a Maker<T> with custom behaviour controlled by the run function.
 */
class CustomMaker<T>(val run: CustomMakerBuilder.() -> Maker<T>) : Maker<T> {
    /**
     * Returns a new instance of T made from the match and initialised with the run function.
     */
    override fun make(match: Match): T {
        val builder = CustomMakerBuilder(match)
        return builder.run().make(match)
    }
}

/**
 * Returns a Maker<T> built using the type-safe builder CustomMakerBuilder.
 */
fun <T> custom(run: CustomMakerBuilder.() -> Maker<T>): Maker<T> {
    return CustomMaker(run)
}
