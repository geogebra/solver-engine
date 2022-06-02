package plans

import expressionmakers.ExpressionMaker
import expressionmakers.OperatorExpressionMaker
import expressions.MetadataOperator
import patterns.Pattern
import steps.metadata.MetadataKey

class PipelineBuilder {
    private var steps: MutableList<Plan> = mutableListOf()

    fun step(step: Plan) {
        steps.add(step)
    }

    fun step(init: PlanBuilder.() -> Unit) {
        step(plan(init))
    }

    fun buildPlan(): PipelineSP {
        return PipelineSP(steps)
    }
}

class FirstOfBuilder {
    private var options: MutableList<Plan> = mutableListOf()

    fun option(opt: Plan) {
        options.add(opt)
    }

    fun option(init: PlanBuilder.() -> Unit) {
        option(plan(init))
    }

    fun buildPlan(planBuilder: PlanBuilder): FirstOf {
        require(options.isNotEmpty())
        return FirstOf(
            options = options,
            explanationMaker = planBuilder.explanationMaker,
            skillMakers = planBuilder.skillMakers.toList(),
        )
    }
}

class PlanBuilder {

    var explanationMaker: ExpressionMaker = noExplanationMaker
    var skillMakers: MutableList<ExpressionMaker> = mutableListOf()
    var pattern: Pattern? = null
    var overridePattern: Pattern? = null

    private lateinit var plan: Plan

    private fun setPlan(p: Plan) {
        if (this::plan.isInitialized) {
            throw IllegalStateException()
        }
        plan = p
    }

    private fun setStepsPlan(stepsProducer: StepsProducer) {
        setPlan(
            StepsPlan(
                ownPattern = pattern,
                overridePattern = overridePattern,
                stepsProducer = stepsProducer,
                explanationMaker = explanationMaker,
                skillMakers = skillMakers
            )
        )
    }

    fun explanation(explanationKey: MetadataKey, vararg params: ExpressionMaker) {
        explanationMaker = OperatorExpressionMaker(MetadataOperator(explanationKey), params.asList())
    }

    fun skill(skillKey: MetadataKey, vararg params: ExpressionMaker) {
        skillMakers.add(OperatorExpressionMaker(MetadataOperator(skillKey), params.asList()))
    }

    fun pipeline(init: PipelineBuilder.() -> Unit) {
        val builder = PipelineBuilder()
        builder.init()
        setStepsPlan(builder.buildPlan())
    }

    fun firstOf(init: FirstOfBuilder.() -> Unit) {
        val builder = FirstOfBuilder()
        builder.init()
        setPlan(builder.buildPlan(this))
    }

    fun whilePossible(subPlan: Plan) {
        setStepsPlan(WhilePossibleSP(subPlan))
    }

    fun whilePossible(init: PlanBuilder.() -> Unit) {
        whilePossible(plan(init))
    }

    fun deeply(plan: Plan, deepFirst: Boolean = false) {
        setStepsPlan(DeeplySP(plan, deepFirst))
    }

    fun deeply(deepFirst: Boolean = false, init: PlanBuilder.() -> Unit) {
        deeply(plan(init), deepFirst)
    }

    fun buildPlan(): Plan {
        return plan
    }
}

fun plan(init: PlanBuilder.() -> Unit): Plan {
    val planBuilder = PlanBuilder()
    planBuilder.init()
    return planBuilder.buildPlan()
}
