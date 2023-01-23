package engine.methods

import engine.context.Context
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Expression
import engine.methods.stepsproducers.StepsProducerBuilderMarker
import engine.patterns.AnyPattern
import engine.patterns.ExpressionProvider
import engine.patterns.Pattern
import engine.steps.Transformation
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.MetadataMaker

abstract class CompositeMethod(
    val specificPlans: List<Method> = emptyList()
) : Method, Runner {

    override fun tryExecute(ctx: Context, sub: Expression): Transformation? {
        ctx.requireActive()

        return run(ctx, sub)?.let {
            Transformation(
                type = it.type,
                fromExpr = sub,
                toExpr = it.toExpr,
                steps = it.steps,
                tasks = it.tasks,
                explanation = it.explanation,
                skills = it.skills
            )
        }
    }
}

@StepsProducerBuilderMarker
open class CompositeMethodBuilder {
    protected var skillMakers: MutableList<MetadataMaker> = mutableListOf()
    var pattern: Pattern = AnyPattern()
    var resultPattern: Pattern = AnyPattern()

    lateinit var explanation: MetadataKey
    protected var explanationParameters: MappedExpressionBuilder.() -> List<Expression> = { emptyList() }

    fun explanationParameters(parameters: MappedExpressionBuilder.() -> List<Expression>) {
        explanationParameters = parameters
    }

    fun explanationParameters(vararg params: ExpressionProvider) {
        explanationParameters = {
            params.map { move(it) }
        }
    }

    fun skill(
        skillKey: MetadataKey,
        skillParameters: MappedExpressionBuilder.() -> List<Expression> = { emptyList() }
    ) {
        skillMakers.add(MetadataMaker(skillKey, skillParameters))
    }

    fun skill(skillKey: MetadataKey, vararg params: ExpressionProvider) {
        skillMakers.add(MetadataMaker(skillKey) { params.map { move(it) } })
    }
}
