package engine.methods

import engine.context.ResourceData
import engine.context.emptyResourceData
import engine.expressionmakers.ExpressionMaker
import engine.methods.stepsproducers.ContextSensitiveAlternative
import engine.methods.stepsproducers.ContextSensitiveSelector
import engine.methods.stepsproducers.PipelineBuilder
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.StepsProducerBuilderMarker
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.Pattern
import engine.steps.metadata.KeyExprsMetadataMaker
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.MetadataMaker

@StepsProducerBuilderMarker
class PlanBuilder(private val stepsProducerFactory: StepsProducerFactory) {
    private lateinit var explanationMaker: MetadataMaker
    private var skillMakers: MutableList<MetadataMaker> = mutableListOf()
    private var alternatives: MutableList<ContextSensitiveAlternative> = mutableListOf()
    private lateinit var defaultSteps: StepsProducer
    private lateinit var resourceData: ResourceData

    var pattern: Pattern = AnyPattern()
    var resultPattern: Pattern = AnyPattern()

    fun explanation(explanationKey: MetadataKey, vararg params: ExpressionMaker) {
        explanationMaker = KeyExprsMetadataMaker(explanationKey, params.asList())
    }

    fun skill(skillKey: MetadataKey, vararg params: ExpressionMaker) {
        skillMakers.add(KeyExprsMetadataMaker(skillKey, params.asList()))
    }

    fun steps(resourceData: ResourceData = emptyResourceData, init: PipelineBuilder.() -> Unit) {
        defaultSteps = stepsProducerFactory(init)
        this.resourceData = resourceData
    }

    fun alternative(resourceData: ResourceData, init: PipelineBuilder.() -> Unit) {
        val alternative = stepsProducerFactory(init)
        alternatives.add(ContextSensitiveAlternative(alternative, resourceData))
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
            return wrapPlanExecutor(this.defaultSteps)
        }
        return wrapPlanExecutor(
            ContextSensitiveSelector(
                default = ContextSensitiveAlternative(this.defaultSteps, resourceData),
                alternatives = alternatives,
            )
        )
    }
}

typealias StepsProducerFactory = (init: PipelineBuilder.() -> Unit) -> StepsProducer

/**
 * Type-safe builder to create [Plan] instance susing the [PlanBuilder] DSL.
 */
fun plan(steps: StepsProducerFactory = ::steps, init: PlanBuilder.() -> Unit): Plan {
    val planBuilder = PlanBuilder(steps)
    planBuilder.init()
    return planBuilder.buildPlan()
}
