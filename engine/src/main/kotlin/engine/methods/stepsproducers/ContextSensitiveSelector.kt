package engine.methods.stepsproducers

import engine.context.Context
import engine.context.Resource
import engine.context.ResourceData
import engine.expressions.Expression
import engine.steps.Transformation

/**
 * Data structure that contains a steps producer and ResourceData that allows
 * to decide whether this producer should be selected in a particular context.
 */
data class ContextSensitiveAlternative(
    val stepsProducer: StepsProducer,
    override val resourceData: ResourceData
) : Resource

/**
 * This StepsProducer has a default implementation and a set of alternative
 * implementations which are context-sensitive.  When producing steps, It
 * selects the most appropriate option from the available ones.
 */
data class ContextSensitiveSelector(
    val default: ContextSensitiveAlternative,
    val alternatives: List<ContextSensitiveAlternative>
) : StepsProducer {

    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
        val best = ctx.selectBestResource(default, alternatives).stepsProducer
        return best.produceSteps(ctx, sub)
    }
}

class ContextSensitiveBuilder {

    private lateinit var default: ContextSensitiveAlternative
    private val alternatives = mutableListOf<ContextSensitiveAlternative>()

    fun buildSelector() = ContextSensitiveSelector(default, alternatives)

    fun default(resourceData: ResourceData, stepsProducer: StepsProducer) {
        default = ContextSensitiveAlternative(stepsProducer, resourceData)
    }

    fun default(resourceData: ResourceData, steps: PipelineBuilder.() -> Unit) {
        default(resourceData, ProceduralPipeline(steps))
    }

    fun alternative(resourceData: ResourceData, stepsProducer: StepsProducer) {
        alternatives.add(ContextSensitiveAlternative(stepsProducer, resourceData))
    }

    fun alternative(resourceData: ResourceData, steps: PipelineBuilder.() -> Unit) {
        alternative(resourceData, ProceduralPipeline(steps))
    }
}

fun contextSensitiveSteps(init: ContextSensitiveBuilder.() -> Unit): ContextSensitiveSelector {
    val builder = ContextSensitiveBuilder()
    builder.init()
    return builder.buildSelector()
}
