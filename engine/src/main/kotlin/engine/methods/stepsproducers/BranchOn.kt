package engine.methods.stepsproducers

import engine.context.Context
import engine.context.Setting
import engine.context.SettingValue
import engine.expressions.Expression
import engine.steps.Transformation

/**
 * Implements the branchOf [StepsProducer] from [setting] and [init].  It uses a [BranchOnCompiler] to prepare the
 * options and calculate its minDepth, then uses a [BranchOnRunner] to execute the branchOn.
 */
private class BranchOn(val setting: Setting, val init: BranchOnFunc) : StepsProducer {
    // These vars may be accessed from different request threads, so we want reads and writes to be atomic.  It doesn't
    // matter if two threads call initialize() because they will calculate the same value.  That wastes a little time
    // but that is more than offset by avoiding the slowdown that putting a lock around initialize() and accessing
    // stepsProducers and minDepthValue would induce.

    @Volatile
    private lateinit var stepsProducers: List<StepsProducer>

    @Volatile
    private var minDepthValue = -1

    private fun initialize() {
        val compiler = BranchOnCompiler()
        compiler.init()
        stepsProducers = compiler.getStepsProducers()
        minDepthValue = compiler.getMindDepth()
    }

    override val minDepth: Int get() {
        if (minDepthValue < 0) {
            initialize()
        }
        return minDepthValue
    }

    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
        if (!::stepsProducers.isInitialized) {
            initialize()
        }
        val runner = BranchOnRunner(setting, stepsProducers, sub, ctx)
        runner.init()
        return runner.steps
    }
}

/**
 * A [BranchOnBuilder] whose purpose is to compile the pipeline, i.e. precalculate options from their specification and
 * precalculate the minDepth of a branchOn.
 */
private class BranchOnCompiler : BranchOnBuilder {
    private val stepsProducers = mutableListOf<StepsProducer>()
    private var minDepth = Int.MAX_VALUE

    fun getStepsProducers() = stepsProducers.toList()

    fun getMindDepth() = minDepth

    private fun registerOption(opt: StepsProducer) {
        minDepth = minOf(minDepth, opt.minDepth)
    }

    private fun registerOption(init: PipelineFunc) {
        val stepsProducer = steps(init)
        stepsProducers.add(stepsProducer)
        registerOption(stepsProducer)
    }

    override fun case(value: SettingValue, opt: StepsProducer) {
        registerOption(opt)
    }

    override fun case(value: SettingValue, init: PipelineFunc) {
        registerOption(init)
    }
}

/**
 * A [FirstOfBuilder] that executes the pipeline. [stepsProducers] is the list of [StepsProducer] instances
 * precalculated by the [FirstOfCompiler].
 */
private class BranchOnRunner(
    val setting: Setting,
    val stepsProducers: List<StepsProducer>,
    val sub: Expression,
    val ctx: Context,
) : BranchOnBuilder {
    var steps: List<Transformation>? = null
    private var index = 0

    private fun nextStepsProducer(): StepsProducer {
        val next = stepsProducers[index]
        index++
        return next
    }

    override fun case(value: SettingValue, opt: StepsProducer) {
        if (steps == null && ctx.get(setting) == value) {
            val currentSteps = opt.produceSteps(ctx, sub)
            if (currentSteps != null) {
                steps = currentSteps
            }
        }
    }

    override fun case(value: SettingValue, init: PipelineFunc) {
        val stepsProducer = nextStepsProducer()
        if (steps == null && ctx.get(setting) == value) {
            val currentSteps = stepsProducer.produceSteps(ctx, sub)
            if (currentSteps != null) {
                steps = currentSteps
            }
        }
    }
}

/**
 * Type-safe builder to create a branchOn [StepsProducer] using the [PipelineBuilder] DSL.
 */
fun branchOn(setting: Setting, init: BranchOnFunc): StepsProducer = BranchOn(setting, init)
