package plans

import context.Context
import expressions.Subexpression
import patterns.*
import rules.*
import steps.ExplanationMaker
import steps.SkillMaker
import steps.Transformation
import steps.makeMetadata

interface Plan {

    val pattern: Pattern
    val explanationMaker: ExplanationMaker get() = makeMetadata("magic")
    val skillMakers: List<SkillMaker> get() = emptyList()

    fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation?

    fun tryExecute(ctx: Context, sub: Subexpression): Transformation? {
        for (match in pattern.findMatches(sub, RootMatch)) {
            return execute(ctx, match, sub)
        }
        return null
    }
}

data class Deeply(val plan: Plan) : Plan {

    override val pattern = FindPattern(plan.pattern)
    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val step = plan.execute(ctx, match, match.getLastBinding(plan.pattern)!!) ?: return null
        return Transformation(sub.path, sub.expr, sub.subst(step.toSubexpr).expr, emptyList(), listOf(step))
    }
}

data class WhilePossible(val plan: Plan) : Plan {

    override val pattern = plan.pattern

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        var lastStep: Transformation? = plan.execute(ctx, match, sub)
        var lastSub = sub
        val steps: MutableList<Transformation> = mutableListOf()
        while (lastStep != null) {
            steps.add(lastStep)
            lastSub = lastSub.subst(lastStep.toSubexpr)
            lastStep = plan.tryExecute(ctx, lastSub)
        }
        return Transformation(sub.path, sub.expr, lastSub.expr, emptyList(), steps)
    }
}

data class PlanPipeline(override val pattern: Pattern, val plans: List<Plan>) : Plan {

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val steps = mutableListOf<Transformation>()
        var lastSub = sub
        for (plan in plans) {
            val step = plan.tryExecute(ctx, lastSub)
            if (step != null) {
                lastSub = lastSub.subst(step.toSubexpr)
                steps.add(step)
            }
        }

        return Transformation(sub.path, sub.expr, lastSub.expr, emptyList(), steps)
    }
}

interface InStep : Plan {

    val pipeline: PlanPipeline
    fun getSubexpressions(match: Match, sub: Subexpression): List<Subexpression>

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val stepSubs = getSubexpressions(match, sub).toMutableList()

        val steps = mutableListOf<Transformation>()
        var lastSub = sub
        for (stepPlan in pipeline.plans) {
            val stepTransformations = stepSubs.map { stepPlan.tryExecute(ctx, it) }
            val nonNullTransformations = stepTransformations.filterNotNull()
            if (nonNullTransformations.isNotEmpty()) {
                val prevSub = lastSub
                for (tr in nonNullTransformations) {
                    lastSub = lastSub.subst(tr.toSubexpr)
                }
                steps.add(
                    Transformation(
                        prevSub.path,
                        prevSub.expr,
                        lastSub.expr,
                        emptyList(),
                        nonNullTransformations
                    )
                )
                for ((i, tr) in stepTransformations.withIndex()) {
                    if (tr != null) {
                        stepSubs[i] = tr.toSubexpr
                    }
                }
            }
        }

        return Transformation(sub.path, sub.expr, lastSub.expr, emptyList(), steps)
    }
}

data class ApplyToChildrenInStep(override val pipeline: PlanPipeline, override val pattern: Pattern = AnyPattern()) :
    InStep {
    override fun getSubexpressions(match: Match, sub: Subexpression): List<Subexpression> {
        return sub.children()
    }
}

interface ContextSensitivePlan : Plan {

    fun sortPlans(ctx: Context): Sequence<Plan>

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        for (plan in sortPlans(ctx)) {
            val trans = tryExecute(ctx, sub)
            if (trans != null) {
                return trans
            }
        }
        return null
    }
}

val convertMixedNumberToImproperFraction = PlanPipeline(
    pattern = MixedNumberPattern(),
    plans = listOf(
        splitMixedNumber,
        addIntegerToFraction,
        WhilePossible(Deeply(evaluateIntegerProduct)),
        addLikeFractions,
        WhilePossible(Deeply(evaluateIntegerSum)),
    )
)

val addUnlikeFractions = run {
    val f1 = fractionOf(IntegerPattern(), IntegerPattern())
    val f2 = fractionOf(IntegerPattern(), IntegerPattern())

    val pattern = sumContaining(f1, f2)

    val plans = listOf(
        commonDenominator,
        WhilePossible(Deeply(evaluateIntegerProduct)),
        addLikeFractions,
        WhilePossible(Deeply(evaluateIntegerSum)),
    )

    PlanPipeline(pattern, plans)
}

val addMixedNumbers = PlanPipeline(
    pattern = sumOf(MixedNumberPattern(), MixedNumberPattern()),
    plans = listOf(
        ApplyToChildrenInStep(convertMixedNumberToImproperFraction),
        addUnlikeFractions,
        fractionToMixedNumber,
    ),
)
