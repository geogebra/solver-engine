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

class PartialSumPlan(
    val pattern: NaryPattern,
    val explanationMaker: MetadataMaker,
    val skillMakers: List<MetadataMaker> = emptyList(),
    specificPlans: List<Method> = emptyList(),
    val stepsProducer: StepsProducer,
) : CompositeMethod(specificPlans) {

    init {
        check(pattern.operator === NaryOperator.Sum)
    }

    override fun run(ctx: Context, sub: Expression): Transformation? {
        if (sub.childCount == pattern.childPatterns.size) {
            val regularPlan = Plan(pattern, AnyPattern(), explanationMaker, skillMakers, specificPlans, stepsProducer)
            return regularPlan.run(ctx, sub)
        }

        for (match in pattern.findMatches(ctx, RootMatch, sub)) {
            val matchedChildExpressions = pattern.getMatchedChildExpressions(match)
            val partialSum = sumOf(matchedChildExpressions)
            val substitutedPartialSum = pattern.substitute(
                match,
                arrayOf(partialSum.decorate(Decorator.PartialSumBracket)),
            )

            val builder = StepsBuilder(ctx, sub)

            val extractedAddendsWereNextToEachOther = areNextToEachOther(matchedChildExpressions)
            builder.addStep(
                Transformation(
                    fromExpr = sub,
                    toExpr = substitutedPartialSum,
                    explanation = if (extractedAddendsWereNextToEachOther) {
                        metadata(SolverEngineExplanation.ExtractPartialSum, partialSum)
                    } else {
                        metadata(SolverEngineExplanation.CommuteFractionNextToTheOtherFraction, partialSum)
                    },
                    type = Transformation.Type.Rule,
                    tags = if (extractedAddendsWereNextToEachOther) { listOf(Transformation.Tag.InvisibleChange)
                    } else { listOf(Transformation.Tag.Rearrangement) },
                ),
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

private fun areNextToEachOther(expressions: List<Expression>): Boolean {
    val indices = expressions.map { (it.origin as Child).index }
    return indices == (indices.first()..indices.last()).toList()
}
