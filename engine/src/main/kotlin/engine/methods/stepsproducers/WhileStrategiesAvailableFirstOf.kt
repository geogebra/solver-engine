package engine.methods.stepsproducers

import engine.context.Context
import engine.context.StrategySelectionMode
import engine.expressions.Expression
import engine.methods.CompositeMethod
import engine.methods.Strategy
import engine.steps.Alternative
import engine.steps.Transformation
import engine.steps.metadata.metadata
import kotlin.reflect.KClass

internal class WhileStrategiesAvailableFirstOf<T : Strategy>(
    private val strategyClass: KClass<T>,
    private val allStrategies: List<Strategy>,
    val init: WhileStrategiesAvailableFirstOfBuilder.() -> Unit,
) : CompositeMethod() {
    override fun run(ctx: Context, sub: Expression): Transformation? {
        val builder = StepsBuilder(ctx, sub)
        val runner = WhileStrategiesAvailableFirstOfRunner(builder, allStrategies)
        val preferredStrategy = ctx.preferredStrategies[strategyClass]

        fun effectivePriority(strategy: Strategy) = if (preferredStrategy == strategy) {
            Strategy.MAX_PRIORITY
        } else {
            strategy.priority
        }

        repeat(MAX_WHILE_POSSIBLE_ITERATIONS) {
            runner.init()
            val alternatives = builder.getAlternatives()

            if (alternatives.isNotEmpty()) {
                val sortedAlternatives = when (ctx.strategySelectionMode) {
                    StrategySelectionMode.ALL -> {
                        if (runner.finished()) {
                            alternatives.sortedByDescending { effectivePriority(it.strategy) }
                        } else {
                            null
                        }
                    }
                    StrategySelectionMode.HIGHEST_PRIORITY -> {
                        val bestAlternative = alternatives.maxBy { effectivePriority(it.strategy) }
                        val bestAlternativePriority = effectivePriority(bestAlternative.strategy)
                        if (runner.remainingStrategies.none { effectivePriority(it) > bestAlternativePriority }) {
                            listOf(bestAlternative)
                        } else {
                            null
                        }
                    }
                    StrategySelectionMode.FIRST -> {
                        alternatives.subList(0, 1)
                    }
                }
                sortedAlternatives?.let { return makeTransformation(sortedAlternatives) }
            }

            if (runner.finished()) {
                return null
            }

            runner.newRound()
        }

        throw TooManyIterationsException(
            "WhileStrategies max iteration number ($MAX_WHILE_POSSIBLE_ITERATIONS) " +
                "exceeded for expression ${builder.lastSub}",
        )
    }

    private fun makeTransformation(alternatives: List<Alternative>): Transformation {
        val mainAlternative = alternatives[0]
        return Transformation(
            type = Transformation.Type.Plan,
            explanation = metadata(mainAlternative.strategy.explanation),
            fromExpr = mainAlternative.steps.first().fromExpr,
            toExpr = mainAlternative.steps.last().toExpr,
            steps = mainAlternative.steps,
            alternatives = if (alternatives.size > 1) {
                alternatives.subList(1, alternatives.size)
            } else {
                null
            },
        )
    }
}

private class WhileStrategiesAvailableFirstOfRunner(val builder: StepsBuilder, allStrategies: List<Strategy>) :
    WhileStrategiesAvailableFirstOfBuilder {

    val remainingStrategies = allStrategies.toMutableList()
    private var done = false

    override fun option(strategy: Strategy) {
        if (done) return
        if (strategy in remainingStrategies) {
            val result = strategy.steps.produceSteps(builder.context, builder.lastSub)
            if (result != null) {
                builder.addAlternative(strategy, result)
                remainingStrategies.removeAll { it == strategy || it.isIncompatibleWith(strategy) }
                done = true
            }
        }
    }

    override fun option(init: PipelineBuilder.() -> Unit) {
        if (done) return
        option(ProceduralPipeline(init))
    }

    override fun option(stepsProducer: StepsProducer) {
        if (done) return
        val steps = stepsProducer.produceSteps(builder.context, builder.lastSub)
        if (steps != null) {
            builder.addSteps(steps)
            done = true
        }
    }

    fun finished(): Boolean {
        return !done || remainingStrategies.isEmpty()
    }

    fun newRound() {
        done = false
    }
}

fun <T : Strategy>whileStrategiesAvailableFirstOf(
    strategyClass: KClass<T>,
    allStrategies: List<Strategy>,
    init: WhileStrategiesAvailableFirstOfBuilder.() -> Unit,
): CompositeMethod {
    return WhileStrategiesAvailableFirstOf(strategyClass, allStrategies, init)
}

inline fun <reified T : Strategy>whileStrategiesAvailableFirstOf(
    allStrategies: Array<T>,
    noinline init: WhileStrategiesAvailableFirstOfBuilder.() -> Unit,
): CompositeMethod {
    return whileStrategiesAvailableFirstOf(T::class, allStrategies.toList(), init)
}
