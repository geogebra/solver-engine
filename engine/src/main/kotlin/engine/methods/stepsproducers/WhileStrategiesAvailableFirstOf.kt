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

    @Suppress("ReturnCount")
    override fun run(ctx: Context, sub: Expression): Transformation? {
        val builder = StepsBuilder(ctx, sub)
        val runner = WhileStrategiesAvailableFirstOfRunner(builder, allStrategies)
        val preferredStrategy = ctx.preferredStrategies[strategyClass]

        fun effectivePriority(strategy: Strategy) = if (preferredStrategy == strategy) {
            Strategy.MAX_PRIORITY
        } else {
            strategy.priority
        }

        fun List<Strategy>.hasNoBetterStrategy(bestAlternative: Alternative): Boolean {
            val bestAlternativePriority = effectivePriority(bestAlternative.strategy)
            return none { effectivePriority(it) > bestAlternativePriority }
        }

        repeat(MAX_WHILE_POSSIBLE_ITERATIONS) {
            runner.init()
            val alternatives = builder.getAlternatives()

            if (runner.roundFailed() && alternatives.isEmpty()) {
                return null
            }

            if (alternatives.isNotEmpty()) {
                when (ctx.strategySelectionMode) {
                    StrategySelectionMode.ALL -> {
                        if (runner.roundFailed() || runner.remainingStrategies.isEmpty()) {
                            return makeTransformation(
                                alternatives.sortedByDescending {
                                    effectivePriority(it.strategy)
                                },
                            )
                        }
                    }
                    StrategySelectionMode.HIGHEST_PRIORITY -> {
                        val bestAlternative = alternatives.maxBy { effectivePriority(it.strategy) }
                        if (runner.roundFailed() || runner.remainingStrategies.hasNoBetterStrategy(bestAlternative)) {
                            return makeTransformation(listOf(bestAlternative))
                        }
                    }
                    StrategySelectionMode.FIRST -> {
                        return makeTransformation(alternatives.subList(0, 1))
                    }
                }
            }

            runner.newRound()
        }

        throw TooManyIterationsException(
            "WhileStrategies max iteration number ($MAX_WHILE_POSSIBLE_ITERATIONS) " +
                "exceeded for expression ${builder.simpleExpression}",
        )
    }

    private fun makeTransformation(alternatives: List<Alternative>): Transformation {
        val mainAlternative = alternatives[0]
        val secondaryAlternatives = if (alternatives.size > 1) {
            alternatives.subList(1, alternatives.size)
        } else {
            null
        }
        if (mainAlternative.steps.size == 1) {
            val mainStep = mainAlternative.steps[0]
            if (mainStep.explanation?.key == mainAlternative.strategy.explanation) {
                return mainStep.copy(alternatives = secondaryAlternatives)
            }
        }
        return Transformation(
            type = Transformation.Type.Plan,
            explanation = metadata(mainAlternative.strategy.explanation),
            fromExpr = mainAlternative.steps.first().fromExpr,
            toExpr = mainAlternative.steps.last().toExpr,
            steps = mainAlternative.steps,
            alternatives = secondaryAlternatives,
        )
    }
}

private class WhileStrategiesAvailableFirstOfRunner(val builder: StepsBuilder, allStrategies: List<Strategy>) :
    WhileStrategiesAvailableFirstOfBuilder {

    val remainingStrategies = allStrategies.toMutableList()
    private var roundSucceeded = false

    override fun option(strategy: Strategy) {
        if (roundSucceeded) return
        if (strategy in remainingStrategies) {
            val result = strategy.steps.produceSteps(builder.context, builder.simpleExpression)
            if (result != null) {
                val added = builder.addAlternative(strategy, result)
                if (added) {
                    remainingStrategies.removeAll { it == strategy || it.isIncompatibleWith(strategy) }
                }
            }
        }
    }

    override fun option(init: PipelineBuilder.() -> Unit) {
        if (roundSucceeded) return
        option(ProceduralPipeline(init))
    }

    override fun option(stepsProducer: StepsProducer) {
        if (roundSucceeded) return
        val steps = stepsProducer.produceSteps(builder.context, builder.simpleExpression)
        if (steps != null) {
            builder.addSteps(steps)
            roundSucceeded = true
        }
    }

    override fun fallback(strategy: Strategy) {
        if (roundSucceeded || builder.getAlternatives().isNotEmpty()) return
        option(strategy)
    }

    fun roundFailed(): Boolean {
        return !roundSucceeded
    }

    fun newRound() {
        roundSucceeded = false
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
