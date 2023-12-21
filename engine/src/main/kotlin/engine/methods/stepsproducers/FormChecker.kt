package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.patterns.Pattern
import engine.steps.Transformation

class FormChecker(val pattern: Pattern) : StepsProducer {

    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
        return if (pattern.matches(ctx, sub)) emptyList() else null
    }

    override val minDepth = pattern.minDepth
}
