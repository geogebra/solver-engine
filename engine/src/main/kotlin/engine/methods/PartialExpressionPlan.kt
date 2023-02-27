package engine.methods

import engine.context.Context
import engine.expressions.Expression
import engine.methods.stepsproducers.StepsProducer
import engine.patterns.AnyPattern
import engine.patterns.NaryPattern
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker
import engine.steps.metadata.metadata

class PartialExpressionPlan(
    val naryPattern: NaryPattern,
    val explanationMaker: MetadataMaker,
    val skillMakers: List<MetadataMaker> = emptyList(),
    specificPlans: List<Method> = emptyList(),
    val stepsProducer: StepsProducer,
) : CompositeMethod(specificPlans) {

    // This plan is used when the whole sum is matched
    private val plan =
        Plan(naryPattern, AnyPattern(), explanationMaker, skillMakers, specificPlans, stepsProducer)

    private val task = taskSet {
        pattern = naryPattern
        explanation = SolverEngineExplanation.SimplifyPartialExpression

        tasks {
            val partialSum = naryPattern.extract()
            val task1 = task(
                startExpr = partialSum,
                explanation = explanationMaker.make(),
                stepsProducer = stepsProducer,
            ) ?: return@tasks null

            task(
                naryPattern.substitute(task1.result),
                metadata(SolverEngineExplanation.SubstitutePartialExpression),
            )

            allTasks()
        }
    }

    override fun run(ctx: Context, sub: Expression): Transformation? {
        return if (sub.flattenedChildCount == naryPattern.childPatterns.size) {
            plan.run(ctx, sub)
        } else {
            task.run(ctx, sub)
        }
    }
}
