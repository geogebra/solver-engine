package plans

import context.Context
import context.Resource
import context.ResourceData
import expressions.*
import patterns.*
import rules.*
import steps.ExplanationMaker
import steps.SkillMaker
import steps.Transformation
import steps.makeMetadata
import java.util.Collections.emptyList

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

data class Deeply(val plan: Plan, val deepFirst: Boolean = false) : Plan {

    override val pattern = FindPattern(plan.pattern, deepFirst)
    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val step = plan.execute(ctx, match, match.getLastBinding(plan.pattern)!!) ?: return null
        return Transformation(sub, sub.substitute(step.fromExpr.path, step.toExpr), listOf(step))
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
            val substitution = lastSub.substitute(lastStep.fromExpr.path, lastStep.toExpr)
            lastSub = Subexpression(lastSub.path, substitution.expr)
            lastStep = plan.tryExecute(ctx, lastSub)
        }
        return Transformation(sub, lastSub.toMappedExpr(), steps)
    }
}

data class FirstOf(val options: List<Plan>) : Plan {

    override val pattern = OneOfPattern(options.map { it.pattern })
    override fun execute(ctx: Context, match: Match, sub: Subexpression) =
        options.firstOrNull { match.getLastBinding(it.pattern) != null }?.execute(ctx, match, sub)
}

data class PlanPipeline(override val pattern: Pattern, val plans: List<Plan>) : Plan {

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
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

        return Transformation(sub, lastSub.toMappedExpr(), steps)
    }
}

fun pipeline(pattern: Pattern, vararg plans: Plan) = PlanPipeline(pattern, plans.asList())
fun pipeline(vararg plans: Plan) = PlanPipeline(plans[0].pattern, plans.asList())

fun firstOf(vararg options: Plan) = FirstOf(options.asList())

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

data class ApplyToChildrenInStep(override val pipeline: PlanPipeline, override val pattern: Pattern = AnyPattern()) :
    InStep {
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

val convertMixedNumberToImproperFraction = PlanPipeline(
    pattern = MixedNumberPattern(),
    plans = listOf(
        splitMixedNumber,
        addIntegerToFraction,
        WhilePossible(Deeply(evaluateIntegerProduct)),
        addLikeFractions,
        WhilePossible(Deeply(evaluateSignedIntegerAddition)),
    )
)

val addUnlikeFractions = run {
    val f1 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
    val f2 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())

    val pattern = sumContaining(f1, f2)

    val plans = listOf(
        commonDenominator,
        WhilePossible(Deeply(evaluateIntegerProduct)),
        addLikeFractions,
        WhilePossible(Deeply(evaluateSignedIntegerAddition)),
    )

    PlanPipeline(pattern, plans)
}

val addMixedNumbersByConverting = PlanPipeline(
    pattern = sumOf(MixedNumberPattern(), MixedNumberPattern()),
    plans = listOf(
        ApplyToChildrenInStep(convertMixedNumberToImproperFraction),
        addUnlikeFractions,
        fractionToMixedNumber,
    ),
)

val addMixedNumbersUsingCommutativity = PlanPipeline(
    pattern = sumOf(MixedNumberPattern(), MixedNumberPattern()),
    plans = listOf(
        WhilePossible(Deeply(splitMixedNumber)),
        WhilePossible(removeBracketsSum),
        evaluateSignedIntegerAddition,
        addUnlikeFractions,
        addIntegerToFraction,
        WhilePossible(Deeply(evaluateIntegerProduct)),
        addLikeFractions,
        Deeply(evaluateSignedIntegerAddition),
        fractionToMixedNumber,
    )
)

val addMixedNumbers = ContextSensitivePlanSelector(
    alternatives = listOf(
        AnnotatedPlan(addMixedNumbersByConverting, ResourceData("EU")),
        AnnotatedPlan(addMixedNumbersUsingCommutativity, ResourceData("US")),
    ),
    default = addMixedNumbersByConverting,
    pattern = sumOf(MixedNumberPattern(), MixedNumberPattern()),
)

val simplifyIntegerSum = WhilePossible(evaluateSignedIntegerAddition)

val simplifyIntegerProduct = WhilePossible(evaluateSignedIntegerProduct)

val simplifyIntegerExpression = WhilePossible(
    Deeply(
        plan = firstOf(
            removeBracketAroundUnsignedInteger,
            removeBracketAroundSignedIntegerInSum,
            simplifyDoubleNeg,
            evaluateSignedIntegerPower,
            simplifyIntegerProduct,
            simplifyIntegerSum,
        ),
        deepFirst = true
    )
)
