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
        val result = resultMaker.makeMappedExpression(match)
        return Transformation(
            sub,
            result,
            null,
            explanationMaker.makeMetadata(match),
            skillMakers.map { it.makeMetadata(match) },
        )
    }
}
