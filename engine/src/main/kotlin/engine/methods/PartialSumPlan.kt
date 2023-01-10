package engine.methods

import engine.context.Context
import engine.expressions.Child
import engine.expressions.Decorator
import engine.expressions.Expression
import engine.expressions.sumOf
import engine.methods.stepsproducers.StepsBuilder
import engine.methods.stepsproducers.StepsProducer
import engine.operators.NaryOperator
import engine.patterns.AnyPattern
import engine.patterns.NaryPattern
import engine.patterns.RootMatch
import engine.steps.Transformation
import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.MetadataMaker
import engine.steps.metadata.metadata

class PartialSumPlan(
    val pattern: NaryPattern,
    val explanationMaker: MetadataMaker,
    val skillMakers: List<MetadataMaker> = emptyList(),
    specificPlans: List<Method> = emptyList(),
    val stepsProducer: StepsProducer
) : Plan(specificPlans) {

    // This plan is used when the whole sum is matched
    private val regularPlan =
        RegularPlan(pattern, AnyPattern(), explanationMaker, skillMakers, specificPlans, stepsProducer)

    init {
        check(pattern.operator === NaryOperator.Sum)
    }

    override fun run(ctx: Context, sub: Expression): TransformationResult? {
        if (sub.childCount == pattern.childPatterns.size) {
            return regularPlan.run(ctx, sub)
        }
        for (match in pattern.findMatches(ctx, RootMatch, sub)) {
            val partialSum = sumOf(pattern.getMatchedChildExpressions(match))
            val substitutedPartialSum = pattern.substitute(
                match,
                arrayOf(partialSum.decorate(Decorator.PartialSumBracket))
            )

            val builder = StepsBuilder(sub)

            builder.addStep(
                Transformation(
                    fromExpr = sub,
                    toExpr = substitutedPartialSum,
                    explanation = metadata(SolverEngineExplanation.ExtractPartialSum, partialSum),
                    type = Transformation.Type.Rearrangement
                )
            )

            val firstChildIndex = (pattern.childPatterns[0].getBoundExpr(match)!!.origin as Child).index
            val steps = stepsProducer.produceSteps(ctx, builder.lastSub.nthChild(firstChildIndex))

            if (steps != null) {
                builder.addSteps(steps)

                return TransformationResult(
                    toExpr = builder.lastSub,
                    steps = builder.getFinalSteps(),
                    explanation = explanationMaker.make(ctx, match),
                    skills = skillMakers.map { it.make(ctx, match) }
                )
            }
        }
        return null
    }
}

enum class SolverEngineExplanation : CategorisedMetadataKey {
    ExtractPartialSum;

    override val category = "SolverEngine"
}
