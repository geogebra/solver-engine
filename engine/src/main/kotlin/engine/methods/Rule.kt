package engine.methods

import engine.context.Context
import engine.expressionmakers.ExpressionMaker
import engine.expressions.Subexpression
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker

data class Rule(
    val pattern: Pattern,
    val resultMaker: ExpressionMaker,
    val explanationMaker: MetadataMaker,
    val skillMakers: List<MetadataMaker> = emptyList(),
) : Method {

    override fun tryExecute(ctx: Context, sub: Subexpression): Transformation? {
        val match = pattern.findMatches(sub, RootMatch).firstOrNull() ?: return null

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
