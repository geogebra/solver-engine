package plans

import context.Context
import context.Resource
import context.ResourceData
import expressionmakers.ExpressionMaker
import expressionmakers.move
import expressions.*
import patterns.*
import rules.*
import steps.Transformation
import steps.metadata.EmptyMetadataKey
import steps.metadata.PlanExplanation
import steps.metadata.Skill
import steps.metadata.makeMetadata

interface PlanExecutor {
    fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation?
}

val noExplanationMaker = makeMetadata(EmptyMetadataKey)

interface Plan : PlanExecutor {

    val pattern: Pattern
    val explanationMaker: ExpressionMaker get() = noExplanationMaker
    val skillMakers: List<ExpressionMaker> get() = emptyList()

    fun tryExecute(ctx: Context, sub: Subexpression): Transformation? {
        for (match in pattern.findMatches(sub, RootMatch)) {
            return execute(ctx, match, sub)
        }
        return null
    }
}

interface StepsProducer {
    val pattern: Pattern
    fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation>
}

data class DeeplySP(val plan: Plan, val deepFirst: Boolean = false) : StepsProducer {
    override val pattern = FindPattern(plan.pattern, deepFirst)
    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        val step = plan.execute(ctx, match, match.getLastBinding(plan.pattern)!!)
        return if (step == null) emptyList() else listOf(step)
    }
}

data class StepsPlan(
    val ownPattern: Pattern? = null,
    val stepsProducer: StepsProducer,
    override val explanationMaker: ExpressionMaker = noExplanationMaker,
    override val skillMakers: List<ExpressionMaker> = emptyList()
) : Plan {

    override val pattern = allOf(ownPattern, stepsProducer.pattern)

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val steps = stepsProducer.produceSteps(ctx, match, sub)
        if (steps.isEmpty()) {
            return null
        }
        val lastStep = steps.last()
        return Transformation(
            fromExpr = sub,
            toExpr = sub.substitute(lastStep.fromExpr.path, lastStep.toExpr),
            steps = steps,
            explanation = explanationMaker.makeMappedExpression(match),
            skills = skillMakers.map { it.makeMappedExpression(match) }
        )
    }
}

data class WhilePossibleSP(val plan: Plan) : StepsProducer {

    override val pattern = plan.pattern

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        var lastStep: Transformation? = plan.execute(ctx, match, sub)
        var lastSub = sub
        val steps: MutableList<Transformation> = mutableListOf()
        while (lastStep != null) {
            steps.add(lastStep)
            val substitution = lastSub.substitute(lastStep.fromExpr.path, lastStep.toExpr)
            lastSub = Subexpression(lastSub.path, substitution.expr)
            lastStep = plan.tryExecute(ctx, lastSub)
        }
        return steps
    }
}

data class FirstOf(
    val options: List<Plan>,
    override val explanationMaker: ExpressionMaker = noExplanationMaker,
    override val skillMakers: List<ExpressionMaker> = emptyList()
) : Plan {

    override val pattern = OneOfPattern(options.map { it.pattern })
    override fun execute(ctx: Context, match: Match, sub: Subexpression) =
        options.firstOrNull { match.getLastBinding(it.pattern) != null }?.execute(ctx, match, sub)
}

data class PipelineSP(val plans: List<Plan>) : StepsProducer {

    init {
        require(plans.isNotEmpty())
    }

    override val pattern = plans[0].pattern
    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        val steps = mutableListOf<Transformation>()
        var lastSub = sub
        for (plan in plans) {
            val step = plan.tryExecute(ctx, lastSub)
            if (step != null) {
                val substitution = lastSub.substitute(step.fromExpr.path, step.toExpr)
                lastSub = Subexpression(lastSub.path, substitution.expr)
                steps.add(step)
            }
        }
        return steps
    }
}

interface InStep : Plan {

    val pipeline: List<Plan>
    fun getSubexpressions(match: Match, sub: Subexpression): List<Subexpression>

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val stepSubs = getSubexpressions(match, sub).toMutableList()

        val steps = mutableListOf<Transformation>()
        var lastSub = sub
        for (stepPlan in pipeline) {
            val stepTransformations = stepSubs.map { stepPlan.tryExecute(ctx, it) }
            val nonNullTransformations = stepTransformations.filterNotNull()
            if (nonNullTransformations.isNotEmpty()) {
                val prevSub = lastSub
                for (tr in nonNullTransformations) {
                    val substitution = lastSub.substitute(tr.fromExpr.path, tr.toExpr)
                    lastSub = Subexpression(lastSub.path, substitution.expr)
                }
                steps.add(
                    Transformation(prevSub, lastSub.toMappedExpr(), nonNullTransformations)
                )
                for ((i, tr) in stepTransformations.withIndex()) {
                    if (tr != null) {
                        stepSubs[i] = Subexpression(tr.fromExpr.path, tr.toExpr.expr)
                    }
                }
            }
        }

        return Transformation(
            sub,
            MappedExpression(lastSub.expr, PathMappingLeaf(listOf(lastSub.path), PathMappingType.Move)),
            steps
        )
    }
}

data class ApplyToChildrenInStep(val plan: Plan, override val pattern: Pattern = AnyPattern()) :
    InStep {

    override val pipeline = ((plan as StepsPlan).stepsProducer as PipelineSP).plans

    override fun getSubexpressions(match: Match, sub: Subexpression): List<Subexpression> {
        return sub.children()
    }
}

data class AnnotatedPlan(val plan: Plan, override val resourceData: ResourceData) : Resource

data class ContextSensitivePlanSelector(
    val alternatives: List<AnnotatedPlan>,
    val default: Plan,
    override val pattern: Pattern = AnyPattern()
) : Plan {

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val alternative = ctx.selectBestResource(alternatives.asSequence())?.plan ?: default
        return alternative.tryExecute(ctx, sub)
    }
}

// emptyResourceData -> defaultPlan

val convertMixedNumberToImproperFraction = plan {
    pattern = mixedNumberOf()

    pipeline {
        step(splitMixedNumber)
        step(convertIntegerToFraction)
        step {
            whilePossible {
                deeply(evaluateIntegerProduct)
            }
        }
        step(addLikeFractions)
        step {
            whilePossible {
                deeply(evaluateSignedIntegerAddition)
            }
        }
    }
}

val addUnlikeFractions = plan {
    val f1 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
    val f2 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())

    pattern = sumContaining(f1, f2)

    explanation(PlanExplanation.AddFractions, move(f1), move(f2))

    skill(Skill.AddFractions, move(f1), move(f2))

    pipeline {
        step(commonDenominator)
        step {
            whilePossible {
                deeply(evaluateIntegerProduct)
            }
        }
        step(addLikeFractions)
        step {
            whilePossible {
                deeply(evaluateSignedIntegerAddition)
            }
        }
    }
}

val addMixedNumbersByConverting = plan {
    pattern = sumOf(mixedNumberOf(), mixedNumberOf())

    pipeline {
        step(ApplyToChildrenInStep(convertMixedNumberToImproperFraction))
        step(addUnlikeFractions)
        step(fractionToMixedNumber)
    }
}

val addMixedNumbersUsingCommutativity = plan {
    pattern = sumOf(mixedNumberOf(), mixedNumberOf())

    pipeline {
        step {
            whilePossible {
                deeply(splitMixedNumber)
            }
        }
        step {
            whilePossible(removeBracketsSum)
        }
        step(evaluateSignedIntegerAddition)
        step(addUnlikeFractions)
        step(convertIntegerToFraction)
        step {
            whilePossible {
                deeply(evaluateIntegerProduct)
            }
        }
        step(addLikeFractions)
        step {
            deeply(evaluateSignedIntegerAddition)
        }
        step(fractionToMixedNumber)
    }
}


val addMixedNumbers = ContextSensitivePlanSelector(
    alternatives = listOf(
        AnnotatedPlan(addMixedNumbersByConverting, ResourceData("EU")),
        AnnotatedPlan(addMixedNumbersUsingCommutativity, ResourceData("US")),
    ),
    default = addMixedNumbersByConverting,
    pattern = sumOf(mixedNumberOf(), mixedNumberOf()),
)
