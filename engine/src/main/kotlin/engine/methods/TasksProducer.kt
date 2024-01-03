package engine.methods

import engine.context.Context
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Expression
import engine.expressions.RootOrigin
import engine.methods.stepsproducers.StepsProducer
import engine.patterns.Match
import engine.steps.Task
import engine.steps.Transformation
import engine.steps.metadata.Metadata

interface TasksProducer {
    fun produceTasks(ctx: Context, expression: Expression, match: Match): List<Task>?
}

class TasksBuilder(
    context: Context,
    expression: Expression,
    match: Match,
) : MappedExpressionBuilder(context, expression, match) {
    private val tasks = mutableListOf<Task>()

    private fun nextTaskId() = "#${tasks.size + 1}"

    fun task(
        startExpr: Expression,
        explanation: Metadata,
        dependsOn: List<Task> = emptyList(),
        context: Context = this.context,
        resultLabel: String? = null,
    ): Task {
        return task(startExpr, explanation, dependsOn, context, resultLabel, EmptyStepsProducer)!!
    }

    @Suppress("LongParameterList")
    fun task(
        startExpr: Expression,
        explanation: Metadata,
        dependsOn: List<Task> = emptyList(),
        context: Context = this.context,
        resultLabel: String? = null,
        stepsProducer: StepsProducer,
    ): Task? {
        val taskId = nextTaskId()
        var steps = stepsProducer.produceSteps(context, startExpr.withOrigin(RootOrigin())) ?: return null
        if (resultLabel != null) {
            steps = steps.mapIndexed { i, trans ->
                if (i + 1 == steps.size) trans.copy(toExpr = trans.toExpr.withName(resultLabel)) else trans
            }
        }
        val task = Task(
            taskId = taskId,
            startExpr = startExpr,
            explanation = explanation,
            steps = steps,
            dependsOn = dependsOn.map { it.taskId },
        )
        tasks.add(task)
        return task
    }

    @Suppress("LongParameterList")
    fun taskWithOptionalSteps(
        startExpr: Expression,
        explanation: Metadata,
        dependsOn: List<Task> = emptyList(),
        context: Context = this.context,
        resultLabel: String? = null,
        stepsProducer: StepsProducer,
    ): Task {
        val taskId = nextTaskId()
        var steps = stepsProducer.produceSteps(context, startExpr.withOrigin(RootOrigin())) ?: emptyList()
        if (resultLabel != null) {
            steps = steps.mapIndexed { i, trans ->
                if (i + 1 == steps.size) trans.copy(toExpr = trans.toExpr.withName(resultLabel)) else trans
            }
        }
        val task = Task(
            taskId = taskId,
            startExpr = startExpr,
            explanation = explanation,
            steps = steps,
            dependsOn = dependsOn.map { it.taskId },
        )
        tasks.add(task)
        return task
    }

    fun allTasks(): List<Task>? = if (tasks.isEmpty()) null else tasks
}

class ProceduralTasksProducer(val produceTasks: TasksBuilder.() -> List<Task>?) : TasksProducer {
    override fun produceTasks(ctx: Context, expression: Expression, match: Match) =
        TasksBuilder(ctx, expression, match).produceTasks()
}

private object EmptyStepsProducer : StepsProducer {
    override fun produceSteps(ctx: Context, sub: Expression) = emptyList<Transformation>()
}
