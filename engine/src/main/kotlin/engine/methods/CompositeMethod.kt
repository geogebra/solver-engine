package engine.methods

import engine.context.Context
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Expression
import engine.methods.stepsproducers.StepsProducerBuilderMarker
import engine.patterns.AnyPattern
import engine.patterns.ExpressionProvider
import engine.patterns.Pattern
import engine.steps.Transformation
import engine.steps.metadata.FixedKeyMetadataMaker
import engine.steps.metadata.GeneralMetadataMaker
import engine.steps.metadata.Metadata
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.MetadataMaker

abstract class CompositeMethod(
    val specificPlans: List<Method> = emptyList(),
) : Method, Runner {

    override fun tryExecute(ctx: Context, sub: Expression): Transformation? {
        ctx.requireActive()
        return run(ctx, sub)
    }
}

@StepsProducerBuilderMarker
open class CompositeMethodBuilder {
    protected var skillMakers: MutableList<MetadataMaker> = mutableListOf()
    var pattern: Pattern = AnyPattern()
    var resultPattern: Pattern = AnyPattern()

    lateinit var explanation: MetadataKey

    private lateinit var explicitExplanationMaker: MetadataMaker
    private var specificPlansList: MutableList<Method> = mutableListOf()

    fun specificPlans(vararg plans: Method) {
        specificPlansList.addAll(plans)
    }

    internal val specificPlans: List<Method> get() = specificPlansList

    fun explanationParameters(parameters: MappedExpressionBuilder.() -> List<Expression>) {
        explicitExplanationMaker = FixedKeyMetadataMaker(explanation, parameters)
    }

    fun explanationParameters(vararg params: ExpressionProvider) {
        explanationParameters { params.map { move(it) } }
    }

    fun explanation(init: MappedExpressionBuilder.() -> Metadata) {
        explicitExplanationMaker = GeneralMetadataMaker(init)
    }

    fun skill(
        skillKey: MetadataKey,
        skillParameters: MappedExpressionBuilder.() -> List<Expression> = { emptyList() },
    ) {
        skillMakers.add(FixedKeyMetadataMaker(skillKey, skillParameters))
    }

    fun skill(skillKey: MetadataKey, vararg params: ExpressionProvider) {
        skillMakers.add(FixedKeyMetadataMaker(skillKey) { params.map { move(it) } })
    }

    protected val explanationMaker get() = when {
        ::explicitExplanationMaker.isInitialized -> explicitExplanationMaker
        else -> FixedKeyMetadataMaker(explanation) { emptyList() }
    }
}
