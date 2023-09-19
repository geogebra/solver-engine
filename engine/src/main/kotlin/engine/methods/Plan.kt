package engine.methods

import engine.context.Context
import engine.context.ResourceData
import engine.context.emptyResourceData
import engine.expressions.Combine
import engine.expressions.Constants
import engine.expressions.Expression
import engine.methods.stepsproducers.ContextSensitiveAlternative
import engine.methods.stepsproducers.ContextSensitiveSelector
import engine.methods.stepsproducers.PipelineBuilder
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.patterns.NaryPattern
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker

/**
 * A `Plan` is a `Method` with a non-empty set of steps which are produced by a `StepsProducer`.
 */
class Plan(
    private val pattern: Pattern,
    private val resultPattern: Pattern,
    private val explanationMaker: MetadataMaker,
    private val skillMakers: List<MetadataMaker>? = null,
    specificPlans: List<Method> = emptyList(),
    private val stepsProducer: StepsProducer,
) : CompositeMethod(specificPlans) {

    override fun run(ctx: Context, sub: Expression) =
        ctx.unlessPreviouslyFailed(this, sub) { doRun(ctx, sub) }
    // To disable cache, do this instead
    //  doRun(ctx, sub)

    private fun doRun(ctx: Context, sub: Expression): Transformation? {
        val match = pattern.findMatches(ctx, RootMatch, sub).firstOrNull() ?: return null

        return stepsProducer.produceSteps(ctx, sub)?.let { steps ->
            val toExpr = steps.last().toExpr.withOrigin(Combine(listOf(sub)))

            when {
                toExpr == Constants.Undefined || resultPattern.matches(ctx, toExpr) -> Transformation(
                    type = Transformation.Type.Plan,
                    fromExpr = sub,
                    toExpr = toExpr,
                    steps = steps,
                    explanation = explanationMaker.make(ctx, sub, match),
                    skills = skillMakers?.map { it.make(ctx, sub, match) },
                )

                else -> null
            }
        }
    }
}

class PlanBuilder : CompositeMethodBuilder() {

    private var isPartialExpression = false
    private var alternatives: MutableList<ContextSensitiveAlternative> = mutableListOf()
    private lateinit var defaultSteps: StepsProducer
    private lateinit var resourceData: ResourceData

    private fun checkNotInitialized() {
        check(!::defaultSteps.isInitialized)
    }

    fun partialExpressionSteps(resourceData: ResourceData = emptyResourceData, init: PipelineBuilder.() -> Unit) {
        checkNotInitialized()
        require(pattern is NaryPattern)
        this.isPartialExpression = true
        steps(resourceData, init)
    }

    fun steps(resourceData: ResourceData = emptyResourceData, init: PipelineBuilder.() -> Unit) {
        checkNotInitialized()
        defaultSteps = steps(init)
        this.resourceData = resourceData
    }

    fun alternative(resourceData: ResourceData, init: PipelineBuilder.() -> Unit) {
        val alternative = steps(init)
        alternatives.add(ContextSensitiveAlternative(alternative, resourceData))
    }

    private fun wrapPlanExecutor(stepsProducer: StepsProducer): CompositeMethod {
        return when {
            isPartialExpression -> PartialExpressionPlan(
                pattern = pattern as NaryPattern,
                stepsProducer = stepsProducer,
                explanationMaker = explanationMaker,
                skillMakers = skillMakers,
                specificPlans = specificPlans,
            )
            else -> Plan(
                pattern = pattern,
                resultPattern = resultPattern,
                stepsProducer = stepsProducer,
                explanationMaker = explanationMaker,
                skillMakers = skillMakers,
                specificPlans = specificPlans,
            )
        }
    }

    fun buildPlan(): CompositeMethod {
        return when {
            alternatives.isEmpty() -> wrapPlanExecutor(defaultSteps)
            else -> wrapPlanExecutor(
                ContextSensitiveSelector(
                    default = ContextSensitiveAlternative(defaultSteps, resourceData),
                    alternatives = alternatives,
                ),
            )
        }
    }
}

/**
 * Type-safe builder to create [CompositeMethod] instance using the [PlanBuilder] DSL.
 */
fun plan(init: PlanBuilder.() -> Unit): CompositeMethod {
    val planBuilder = PlanBuilder()
    planBuilder.init()
    return planBuilder.buildPlan()
}
