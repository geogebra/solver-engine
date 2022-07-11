package engine.methods

import engine.expressionmakers.ExpressionMaker
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

    fun buildPlan(): Pipeline {
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

    fun buildPlan(): FirstOf {
        require(options.isNotEmpty())
        return FirstOf(options)
    }
}

class PlanBuilder {

    var planId: MethodId? = null
    var explanationMaker: MetadataMaker? = null
    var skillMakers: MutableList<MetadataMaker> = mutableListOf()
    var pattern: Pattern? = null

    private lateinit var plan: Plan

    private fun setPlan(p: Plan) {
        if (this::plan.isInitialized) {
            throw IllegalStateException("Plan has already been set once")
        }
        plan = p
    }

    private fun setStepsPlan(stepsProducer: StepsProducer) {
        setPlan(
            Plan(
                ownPattern = pattern,
                stepsProducer = stepsProducer,
                explanationMaker = explanationMaker,
                skillMakers = skillMakers,
                planId = planId,
            )
        )
    }

    fun explanation(explanationKey: MetadataKey, vararg params: ExpressionMaker) {
        explanationMaker = KeyExprsMetadataMaker(explanationKey, params.asList())
    }

    fun skill(skillKey: MetadataKey, vararg params: ExpressionMaker) {
        skillMakers.add(KeyExprsMetadataMaker(skillKey, params.asList()))
    }

    fun pipeline(init: PipelineBuilder.() -> Unit) {
        val builder = PipelineBuilder()
        builder.init()
        setStepsPlan(builder.buildPlan())
    }

    fun firstOf(init: FirstOfBuilder.() -> Unit) {
        val builder = FirstOfBuilder()
        builder.init()
        setStepsPlan(builder.buildPlan())
    }

    fun whilePossible(subPlan: Method) {
        setStepsPlan(WhilePossible(subPlan))
    }

    fun whilePossible(init: PlanBuilder.() -> Unit) {
        whilePossible(plan(init))
    }

    fun deeply(plan: Method, deepFirst: Boolean = false) {
        setStepsPlan(Deeply(plan, deepFirst))
    }

    fun deeply(deepFirst: Boolean = false, init: PlanBuilder.() -> Unit) {
        deeply(plan(init), deepFirst)
    }

    fun buildPlan(): Plan {
        return plan
    }

    fun applyToChildrenInStep(plan: Plan) {
        setStepsPlan(ApplyToChildrenInStep(plan))
    }
}

fun plan(init: PlanBuilder.() -> Unit): Plan {
    val planBuilder = PlanBuilder()
    planBuilder.init()
    return planBuilder.buildPlan()
}
