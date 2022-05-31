package plans

import expressionmakers.ExpressionMaker
import expressionmakers.OperatorExpressionMaker
import expressions.MetadataOperator
import patterns.AnyPattern
import patterns.Pattern
import rules.*
import steps.metadata.MetadataKey
import steps.metadata.PlanExplanation

class PipelineBuilder {
    private var steps: MutableList<Plan> = mutableListOf()

    fun step(step: Plan) {
        steps.add(step)
    }

    fun step(init: PlanBuilder.() -> Unit) {
        step(plan(init))
    }

    fun buildPlan(planBuilder: PlanBuilder): PlanPipeline {
        require(steps.isNotEmpty())
        return PlanPipeline(
            pattern = planBuilder.pattern ?: steps[0].pattern,
            plans = steps.toList(),
            explanationMaker = planBuilder.explanationMaker,
            skillMakers = planBuilder.skillMakers.toList(),
        )
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
    private lateinit var plan: Plan

    private fun setPlan(p: Plan) {
        if (this::plan.isInitialized) {
            throw IllegalStateException()
        }
        plan = p
    }

    fun explanation(explanationKey: MetadataKey, vararg params: ExpressionMaker) {
        explanationMaker = OperatorExpressionMaker(MetadataOperator(explanationKey), params.asList())
    }

    fun pipeline(init: PipelineBuilder.() -> Unit) {
        val builder = PipelineBuilder()
        builder.init()
        setPlan(builder.buildPlan(this))
    }

    fun firstOf(init: FirstOfBuilder.() -> Unit) {
        val builder = FirstOfBuilder()
        builder.init()
        setPlan(builder.buildPlan(this))
    }

    fun whilePossible(subPlan: Plan) {
        setPlan(WhilePossible(subPlan, explanationMaker, skillMakers.toList()))
    }

    fun whilePossible(init: PlanBuilder.() -> Unit) {
        whilePossible(plan(init))
    }

    fun deeply(plan: Plan, deepFirst: Boolean = false) {
        setPlan(Deeply(plan, deepFirst))
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
//
//val addUnlikeFractions2 = plan {
//    val f1 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
//    val f2 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
//
//    pattern = sumContaining(f1, f2)
//
//    pipeline {
//        step(commonDenominator)
//        step {
//            whilePossible {
//                deeply(evaluateIntegerProduct)
//            }
//        }
//        step {
//            apply(add)
//        }
//    }
//}

val simplifyArithmeticExpression = plan {

    pattern = AnyPattern() /* TODO add condition that it is constant in all variables */
    explanation(PlanExplanation.SimplifyArithmeticExpression)

    whilePossible {
        deeply(deepFirst = true) {
            firstOf {
                option(removeBracketAroundUnsignedInteger)
                option(removeBracketAroundSignedIntegerInSum)
                option(simplifyDoubleNeg)
                option(evaluateSignedIntegerPower)
                option {
                    explanation(PlanExplanation.SimplifyIntegerProduct)
                    whilePossible(evaluateSignedIntegerProduct)
                }
                option {
                    explanation(PlanExplanation.SimplifyIntegerSum)
                    whilePossible(evaluateSignedIntegerAddition)
                }
            }
        }
    }
}