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
class PipelineBuilder {
    private var pipelineItems = mutableListOf<PipelineItem>()

    private fun addItem(stepsProducer: StepsProducer, optional: Boolean) {
        pipelineItems.add(PipelineItem(stepsProducer, optional))
    }

    fun steps(step: StepsProducer) {
        addItem(step, false)
    }

    fun steps(init: StepsProducerBuilder.() -> Unit) {
        addItem(engine.methods.steps(init), optional = false)
    }

    fun optionalSteps(step: StepsProducer) {
        addItem(step, true)
    }

    fun optionalSteps(init: StepsProducerBuilder.() -> Unit) {
        addItem(engine.methods.steps(init), optional = true)
    }

    fun buildPlanExecutor(): Pipeline {
        return Pipeline(pipelineItems)
    }
}

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

    fun buildPlanExecutor(): InStep {
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

    fun buildPlanExecutor(): FirstOf {
        require(options.isNotEmpty())
        return FirstOf(options)
    }
}

@PlanBuilderMarker
open class StepsProducerBuilder {
    lateinit var resourceData: ResourceData
    lateinit var stepsProducer: StepsProducer

    fun pipeline(init: PipelineBuilder.() -> Unit) {
        val builder = PipelineBuilder()
        builder.init()
        stepsProducer = builder.buildPlanExecutor()
    }

    fun applyTo(steps: StepsProducer, extractor: Extractor) {
        stepsProducer = ApplyTo(extractor, steps)
    }

    fun applyToChildrenInStep(init: InStepBuilder.() -> Unit) {
        val builder = InStepBuilder()
        builder.init()
        stepsProducer = builder.buildPlanExecutor()
    }

    fun firstOf(init: FirstOfBuilder.() -> Unit) {
        val builder = FirstOfBuilder()
        builder.init()
        stepsProducer = builder.buildPlanExecutor()
    }

    fun whilePossible(steps: StepsProducer) {
        stepsProducer = WhilePossible(steps)
    }

    fun whilePossible(init: StepsProducerBuilder.() -> Unit) {
        whilePossible(steps(init))
    }

    fun deeply(steps: StepsProducer, deepFirst: Boolean = false) {
        stepsProducer = Deeply(steps, deepFirst)
    }

    fun deeply(deepFirst: Boolean = false, init: StepsProducerBuilder.() -> Unit) {
        deeply(steps(init), deepFirst)
    }

    fun plan(init: PlanBuilder.() -> Unit) {
        stepsProducer = engine.methods.plan(init)
    }
}

class PlanBuilder : StepsProducerBuilder() {
    var explanationMaker: MetadataMaker? = null
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
        alternatives.add(ContextSensitiveAlternative(alternative.stepsProducer, alternative.resourceData))
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
        if (alternatives.isEmpty()) {
            return wrapPlanExecutor(stepsProducer)
        }
        return wrapPlanExecutor(
            ContextSensitiveSelector(
                default = ContextSensitiveAlternative(stepsProducer, resourceData),
                alternatives = alternatives,
            )
        )
    }
}

fun plan(init: PlanBuilder.() -> Unit): Plan {
    val planBuilder = PlanBuilder()
    planBuilder.init()
    return planBuilder.buildPlan()
}

fun steps(init: StepsProducerBuilder.() -> Unit): StepsProducer {
    val builder = StepsProducerBuilder()
    builder.init()
    return builder.stepsProducer
}
