package engine.rules

import engine.context.Context
import engine.expressionmakers.ExpressionMaker
import engine.expressions.Subexpression
import engine.patterns.Match
import engine.patterns.Pattern
import engine.plans.TransformationProducer
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker

data class Rule(
    override val pattern: Pattern,
    val resultMaker: ExpressionMaker,
    val explanationMaker: MetadataMaker,
    val skillMakers: List<MetadataMaker> = emptyList(),
) : TransformationProducer {

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val result = resultMaker.make(match)
        return Transformation(
            fromExpr = sub,
            toExpr = result,
            steps = null,
            explanation = explanationMaker.make(match),
            skills = skillMakers.map { it.make(match) },
        )
    }
}
