package engine.methods

import engine.context.ResourceData
import engine.expressionmakers.ExpressionMaker
import engine.methods.executors.ApplyToChildrenInStep
import engine.methods.executors.ContextSensitiveAlternative
import engine.methods.executors.ContextSensitiveSelector
import engine.methods.executors.Deeply
import engine.methods.executors.FirstOf
import engine.methods.executors.Pipeline
import engine.methods.executors.PipelineItem
import engine.methods.executors.PlanExecutor
import engine.methods.executors.WhilePossible
import engine.patterns.Pattern
import engine.steps.metadata.KeyExprsMetadataMaker
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.MetadataMaker

class PipelineBuilder {
    private var steps: MutableList<PipelineItem> = mutableListOf()

    private fun step(step: Method, optional: Boolean) {
        steps.add(PipelineItem(step, optional))
    }

    fun step(step: Method) {
        step(step, false)
    }

    fun step(init: PlanBuilder.() -> Unit) {
        step(plan(init), optional = false)
    }

    fun optionalStep(step: Method) {
        step(step, true)
    }

    fun optionalStep(init: PlanBuilder.() -> Unit) {
        step(plan(init), optional = true)
    }

    fun buildPlanExecutor(): Pipeline {
        return Pipeline(steps)
    }
}

class FirstOfBuilder {
    private var options: MutableList<Method> = mutableListOf()

    fun option(opt: Method) {
        options.add(opt)
    }

    fun option(init: PlanBuilder.() -> Unit) {
        option(plan(init))
    }

    fun buildPlanExecutor(): FirstOf {
        require(options.isNotEmpty())
        return FirstOf(options)
    }
}

open class PlanExecutorBuilder {
    lateinit var resourceData: ResourceData
    lateinit var planExecutor: PlanExecutor

    fun pipeline(init: PipelineBuilder.() -> Unit) {
        val builder = PipelineBuilder()
        builder.init()
        planExecutor = builder.buildPlanExecutor()
    }

    fun firstOf(init: FirstOfBuilder.() -> Unit) {
        val builder = FirstOfBuilder()
        builder.init()
        planExecutor = builder.buildPlanExecutor()
    }

    fun whilePossible(subPlan: Method) {
        planExecutor = WhilePossible(subPlan)
    }

    fun whilePossible(init: PlanBuilder.() -> Unit) {
        whilePossible(plan(init))
    }

    fun deeply(plan: Method, deepFirst: Boolean = false) {
        planExecutor = Deeply(plan, deepFirst)
    }

    fun deeply(deepFirst: Boolean = false, init: PlanBuilder.() -> Unit) {
        deeply(plan(init), deepFirst)
    }

    fun applyToChildrenInStep(plan: Plan) {
        planExecutor = ApplyToChildrenInStep(plan)
    }
}

class PlanBuilder : PlanExecutorBuilder() {
    var explanationMaker: MetadataMaker? = null
    var skillMakers: MutableList<MetadataMaker> = mutableListOf()
    var pattern: Pattern? = null
    var alternatives: MutableList<ContextSensitiveAlternative> = mutableListOf()

    fun explanation(explanationKey: MetadataKey, vararg params: ExpressionMaker) {
        explanationMaker = KeyExprsMetadataMaker(explanationKey, params.asList())
    }

    fun skill(skillKey: MetadataKey, vararg params: ExpressionMaker) {
        skillMakers.add(KeyExprsMetadataMaker(skillKey, params.asList()))
    }

    fun alternative(init: PlanExecutorBuilder.() -> Unit) {
        val alternative = PlanExecutorBuilder()
        alternative.init()
        alternatives.add(ContextSensitiveAlternative(alternative.planExecutor, alternative.resourceData))
    }

    private fun wrapPlanExecutor(planExecutor: PlanExecutor): Plan {
        return Plan(
            ownPattern = pattern,
            planExecutor = planExecutor,
            explanationMaker = explanationMaker,
            skillMakers = skillMakers,
        )
    }

    fun buildPlan(): Plan {
        if (alternatives.isEmpty()) {
            return wrapPlanExecutor(planExecutor)
        }
        return wrapPlanExecutor(
            ContextSensitiveSelector(
                default = ContextSensitiveAlternative(planExecutor, resourceData),
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
