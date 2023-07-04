package engine.methods

import engine.methods.stepsproducers.StepsProducer
import engine.steps.metadata.MetadataKey

/**
 * Strategies represent different ways a given problem can be solved. Specific strategies can be selected using
 * context settings. When no strategy is specified, or the selected one doesn't apply, the one with the highest
 * priority will be selected.
 */
interface Strategy {
    val family: StrategyFamily
    val priority: Int
    val name: String
    val explanation: MetadataKey
    val steps: StepsProducer

    fun isIncompatibleWith(other: Strategy): Boolean {
        return other.family != family
    }

    companion object {
        const val MAX_PRIORITY = Int.MAX_VALUE
    }
}

interface StrategyFamily

@Target(AnnotationTarget.FIELD)
annotation class PublicStrategy
