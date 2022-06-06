package plans

import context.ResourceData
import expressionmakers.ExpressionMaker
import expressionmakers.OperatorExpressionMaker
import expressions.MetadataOperator
import patterns.AnyPattern
import patterns.Pattern
import steps.metadata.MetadataKey

class PipelineBuilder {
    private var steps: MutableList<TransformationProducer> = mutableListOf()

    fun step(step: TransformationProducer) {
        steps.add(step)
    }

    fun step(init: PlanBuilder.() -> Unit) {
        step(plan(init))
    }

    fun buildPlan(): Pipeline {
        return Pipeline(steps)
    }
}

class FirstOfBuilder {
    private var options: MutableList<TransformationProducer> = mutableListOf()

    fun option(opt: TransformationProducer) {
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

class ContextSensitivePlanBuilder {

    private var alternatives: MutableList<AnnotatedPlan> = mutableListOf()
    private lateinit var defaultPlan: TransformationProducer

    fun case(curriculum: String?, init: PlanBuilder.() -> Unit) {
        case(curriculum = curriculum, plan(init))
    }

    fun case(curriculum: String?, plan: TransformationProducer) {
        alternatives.add(AnnotatedPlan(plan, ResourceData(curriculum = curriculum)))
    }

    fun default(init: PlanBuilder.() -> Unit) {
        default(plan(init))
    }

    fun default(plan: TransformationProducer) {
        if (this::defaultPlan.isInitialized) {
            throw IllegalStateException()
        }
        val planBuilder = PlanBuilder()
        defaultPlan = plan
    }

    fun buildPlan(pattern: Pattern): ContextSensitivePlanSelector {
        return ContextSensitivePlanSelector(alternatives, defaultPlan, pattern)
    }
}

class PlanBuilder {

    var explanationMaker: ExpressionMaker? = null
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
            Plan(
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
        setStepsPlan(builder.buildPlan())
    }

    fun whilePossible(subPlan: TransformationProducer) {
        setStepsPlan(WhilePossible(subPlan))
    }

    fun whilePossible(init: PlanBuilder.() -> Unit) {
        whilePossible(plan(init))
    }

    fun deeply(plan: TransformationProducer, deepFirst: Boolean = false) {
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

    fun selectFromContext(init: ContextSensitivePlanBuilder.() -> Unit) {
        val builder = ContextSensitivePlanBuilder()
        builder.init()
        setStepsPlan(builder.buildPlan(pattern ?: AnyPattern()))
    }
}

fun plan(init: PlanBuilder.() -> Unit): Plan {
    val planBuilder = PlanBuilder()
    planBuilder.init()
    return planBuilder.buildPlan()
}
