package engine.methods

import engine.context.Context
import engine.expressions.Child
import engine.expressions.Decorator
import engine.expressions.Expression
import engine.methods.stepsproducers.StepsBuilder
import engine.methods.stepsproducers.StepsProducer
import engine.operators.ProductOperator
import engine.operators.SumOperator
import engine.patterns.AnyPattern
import engine.patterns.Match
import engine.patterns.NaryPattern
import engine.patterns.RootMatch
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker
import engine.steps.metadata.metadata

class PartialExpressionPlan(
    val pattern: NaryPattern,
    val explanationMaker: MetadataMaker,
    val skillMakers: List<MetadataMaker> = emptyList(),
    specificPlans: List<Method> = emptyList(),
    val stepsProducer: StepsProducer,
) : CompositeMethod(specificPlans) {

    override fun run(ctx: Context, sub: Expression): Transformation? {
        if (sub.childCount == pattern.childPatterns.size) {
            val regularPlan = Plan(pattern, AnyPattern(), explanationMaker, skillMakers, specificPlans, stepsProducer)
            return regularPlan.run(ctx, sub)
        }

        for (match in pattern.findMatches(ctx, RootMatch, sub)) {
            val partialExpression = pattern.extract(match)
            val substitutedPartialExpression = pattern.substitute(
                match,
                arrayOf(partialExpression.decorate(Decorator.PartialBracket)),
            )

            val builder = StepsBuilder(ctx, sub)

            builder.addStep(
                if (matchedTermsAreNextToEachOther(pattern, match)) {
                    Transformation(
                        fromExpr = sub,
                        toExpr = substitutedPartialExpression,
                        explanation = metadata(SolverEngineExplanation.ExtractPartialExpression),
                        type = Transformation.Type.Rule,
                        tags = listOf(Transformation.Tag.InvisibleChange),
                    )
                } else {
                    Transformation(
                        fromExpr = sub,
                        toExpr = substitutedPartialExpression,
                        explanation = when (pattern.operator) {
                            is ProductOperator -> metadata(SolverEngineExplanation.RearrangeProduct, partialExpression)
                            is SumOperator -> metadata(SolverEngineExplanation.RearrangeSum, partialExpression)
                        },
                        type = Transformation.Type.Rule,
                        tags = listOf(Transformation.Tag.Rearrangement),
                    )
                },
            )

            val firstChildIndex = (pattern.childPatterns[0].getBoundExpr(match)!!.origin as Child).index
            val steps = stepsProducer.produceSteps(ctx, builder.lastSub.nthChild(firstChildIndex))

            if (steps != null) {
                builder.addSteps(steps)

                return Transformation(
                    fromExpr = sub,
                    toExpr = builder.lastSub,
                    steps = builder.getFinalSteps(),
                    explanation = explanationMaker.make(ctx, sub, match),
                    skills = skillMakers.map { it.make(ctx, sub, match) },
                    type = Transformation.Type.Plan,
                )
            }
        }

        return null
    }
}

private fun matchedTermsAreNextToEachOther(pattern: NaryPattern, match: Match): Boolean {
    val indices = pattern.getMatchedOrigins(match).map { (it as Child).index }
    return indices == (indices.first()..indices.last()).toList()
}
