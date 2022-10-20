package engine.methods

import engine.context.ResourceData
import engine.expressionmakers.ExpressionMaker
import engine.methods.stepsproducers.ApplyTo
import engine.methods.stepsproducers.ApplyToChildrenInStep
import engine.methods.stepsproducers.ContextSensitiveAlternative
import engine.methods.stepsproducers.ContextSensitiveSelector
import engine.methods.stepsproducers.Deeply
import engine.methods.stepsproducers.Extractor
import engine.methods.stepsproducers.FirstOf
import engine.methods.stepsproducers.InStep
import engine.methods.stepsproducers.InStepItem
import engine.methods.stepsproducers.Pipeline
import engine.methods.stepsproducers.PipelineItem
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.WhilePossible
import engine.patterns.AnyPattern
import engine.patterns.Pattern
import engine.steps.metadata.KeyExprsMetadataMaker
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.MetadataMaker
import engine.steps.metadata.makeMetadata

@DslMarker
annotation class PlanBuilderMarker

@PlanBuilderMarker
class InStepStepBuilder {
    lateinit var method: Method
    lateinit var explanationKey: MetadataKey
}

@PlanBuilderMarker
class InStepBuilder {
    private var steps = mutableListOf<InStepItem>()

    fun step(init: InStepStepBuilder.() -> Unit) {
        val builder = InStepStepBuilder()
        builder.init()
        steps.add(InStepItem(builder.method, makeMetadata(builder.explanationKey), false))
    }

    fun optionalStep(init: InStepStepBuilder.() -> Unit) {
        val builder = InStepStepBuilder()
        builder.init()
        steps.add(InStepItem(builder.method, makeMetadata(builder.explanationKey), true))
    }

    fun buildStepsProducer(): InStep {
        return ApplyToChildrenInStep(steps)
    }
}

@PlanBuilderMarker
class FirstOfBuilder {
    private var options: MutableList<StepsProducer> = mutableListOf()

    fun option(opt: StepsProducer) {
        options.add(opt)
    }

    fun option(init: StepsProducerBuilder.() -> Unit) {
        option(steps(init))
    }

    fun buildStepsProducer(): FirstOf {
        require(options.isNotEmpty())
        return FirstOf(options)
    }
}

@PlanBuilderMarker
open class StepsProducerBuilder {
    lateinit var resourceData: ResourceData
    private var pipelineItems = mutableListOf<PipelineItem>()

    private fun addItem(stepsProducer: StepsProducer, optional: Boolean = false) {
        pipelineItems.add(PipelineItem(stepsProducer, optional))
    }

    fun buildStepsProducer(): StepsProducer {
        return when (pipelineItems.size) {
            0 -> throw IllegalStateException("steps producer produces no steps")
            1 -> pipelineItems[0].stepsProducer
            else -> Pipeline(pipelineItems)
        }
    }

    fun optionally(steps: StepsProducer) {
        addItem(steps, true)
    }

    fun optionally(init: StepsProducerBuilder.() -> Unit) {
        optionally(steps(init))
    }

    fun apply(steps: StepsProducer) {
        addItem(steps)
    }

    fun apply(init: StepsProducerBuilder.() -> Unit) {
        apply(steps(init))
    }

    fun applyTo(steps: StepsProducer, extractor: Extractor) {
        addItem(ApplyTo(extractor, steps))
    }

    fun applyToChildrenInStep(init: InStepBuilder.() -> Unit) {
        val builder = InStepBuilder()
        builder.init()
        addItem(builder.buildStepsProducer())
    }

    fun firstOf(init: FirstOfBuilder.() -> Unit) {
        val builder = FirstOfBuilder()
        builder.init()
        addItem(builder.buildStepsProducer())
    }

    fun whilePossible(steps: StepsProducer) {
        addItem(WhilePossible(steps), true)
    }

    fun whilePossible(init: StepsProducerBuilder.() -> Unit) {
        whilePossible(steps(init))
    }

    fun deeply(steps: StepsProducer, deepFirst: Boolean = false) {
        addItem(Deeply(steps, deepFirst))
    }

    fun deeply(deepFirst: Boolean = false, init: StepsProducerBuilder.() -> Unit) {
        deeply(steps(init), deepFirst)
    }

    fun plan(init: PlanBuilder.() -> Unit) {
        addItem(engine.methods.plan(init))
    }
}

class PlanBuilder : StepsProducerBuilder() {
    lateinit var explanationMaker: MetadataMaker
    var skillMakers: MutableList<MetadataMaker> = mutableListOf()
    var pattern: Pattern = AnyPattern()
    var resultPattern: Pattern = AnyPattern()
    var alternatives: MutableList<ContextSensitiveAlternative> = mutableListOf()

    fun explanation(explanationKey: MetadataKey, vararg params: ExpressionMaker) {
        explanationMaker = KeyExprsMetadataMaker(explanationKey, params.asList())
    }

    fun skill(skillKey: MetadataKey, vararg params: ExpressionMaker) {
        skillMakers.add(KeyExprsMetadataMaker(skillKey, params.asList()))
    }

    fun alternative(init: StepsProducerBuilder.() -> Unit) {
        val alternative = StepsProducerBuilder()
        alternative.init()
        alternatives.add(ContextSensitiveAlternative(alternative.buildStepsProducer(), alternative.resourceData))
    }

    private fun wrapPlanExecutor(stepsProducer: StepsProducer): Plan {
        return Plan(
            pattern = pattern,
            resultPattern = resultPattern,
            stepsProducer = stepsProducer,
            explanationMaker = explanationMaker,
            skillMakers = skillMakers,
        )
    }

    fun buildPlan(): Plan {
        require(this::explanationMaker.isInitialized)
        if (alternatives.isEmpty()) {
            return wrapPlanExecutor(buildStepsProducer())
        }
        return wrapPlanExecutor(
            ContextSensitiveSelector(
                default = ContextSensitiveAlternative(buildStepsProducer(), resourceData),
                alternatives = alternatives,
            )
        )
    }
}

/**
 * Type-safe builder to create [Plan] instance susing the [PlanBuilder] DSL.
 */
fun plan(init: PlanBuilder.() -> Unit): Plan {
    val planBuilder = PlanBuilder()
    planBuilder.init()
    return planBuilder.buildPlan()
}

/**
 * Type-safe builder to create a [StepsProducer] using the [StepsProducerBuilder] DSL.
 */
fun steps(init: StepsProducerBuilder.() -> Unit): StepsProducer {
    val builder = StepsProducerBuilder()
    builder.init()
    return builder.buildStepsProducer()
}
