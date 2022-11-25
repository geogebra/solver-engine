package engine.methods

import engine.context.ResourceData
import engine.context.emptyResourceData
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Expression
import engine.methods.stepsproducers.ContextSensitiveAlternative
import engine.methods.stepsproducers.ContextSensitiveSelector
import engine.methods.stepsproducers.PipelineBuilder
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.StepsProducerBuilderMarker
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.PathProvider
import engine.patterns.Pattern
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.MetadataMaker

@StepsProducerBuilderMarker
class PlanBuilder(private val stepsProducerFactory: StepsProducerFactory) {
    private var skillMakers: MutableList<MetadataMaker> = mutableListOf()
    private var alternatives: MutableList<ContextSensitiveAlternative> = mutableListOf()
    private lateinit var defaultSteps: StepsProducer
    private lateinit var resourceData: ResourceData
    private var specificPlansList: MutableList<Method> = mutableListOf()

    var pattern: Pattern = AnyPattern()
    var resultPattern: Pattern = AnyPattern()

    lateinit var explanation: MetadataKey
    private var explanationParameters: MappedExpressionBuilder.() -> List<Expression> = { emptyList() }

    fun explanationParameters(parameters: MappedExpressionBuilder.() -> List<Expression>) {
        explanationParameters = parameters
    }

    fun specificPlans(vararg plans: Method) {
        specificPlansList.addAll(plans)
    }

    fun explanationParameters(vararg params: PathProvider) {
        explanationParameters = {
            params.map { move(it) }
        }
    }

    fun skill(
        skillKey: MetadataKey,
        skillParameters: MappedExpressionBuilder.() -> List<Expression> = { emptyList() }
    ) {
        skillMakers.add(MetadataMaker(skillKey, skillParameters))
    }

    fun skill(skillKey: MetadataKey, vararg params: PathProvider) {
        skillMakers.add(MetadataMaker(skillKey) { params.map { move(it) } })
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
            explanationMaker = MetadataMaker(explanation, explanationParameters),
            skillMakers = skillMakers,
            specificPlans = specificPlansList
        )
    }

    fun buildPlan(): Plan {
        if (alternatives.isEmpty()) {
            return wrapPlanExecutor(this.defaultSteps)
        }
        return wrapPlanExecutor(
            ContextSensitiveSelector(
                default = ContextSensitiveAlternative(this.defaultSteps, resourceData),
                alternatives = alternatives
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
