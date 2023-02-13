package engine.methods

import engine.context.Context
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Expression
import engine.expressions.Root
import engine.methods.stepsproducers.PipelineBuilder
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.proceduralSteps
import engine.patterns.Match
import engine.steps.Task
import engine.steps.Transformation
import engine.steps.metadata.Metadata

interface TasksProducer {
    fun produceTasks(ctx: Context, match: Match): List<Task>?
}

class TasksBuilder(context: Context, match: Match) : MappedExpressionBuilder(context, match) {

    private val tasks = mutableListOf<Task>()

    private fun nextTaskId() = "#${tasks.size + 1}"

    fun task(
        startExpr: Expression,
        explanation: Metadata,
        dependsOn: List<Task> = emptyList(),
        stepsProducer: StepsProducer = EmptyStepsProducer,
    ): Task? {
        val taskId = nextTaskId()
        val steps = stepsProducer.produceSteps(context, startExpr.withOrigin(Root())) ?: return null
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

    fun task(
        startExpr: Expression,
        explanation: Metadata,
        dependsOn: List<Task> = emptyList(),
        init: PipelineBuilder.() -> Unit,
    ): Task? = task(startExpr, explanation, dependsOn, proceduralSteps(init))

    fun allTasks(): List<Task>? = if (tasks.isEmpty()) null else tasks
}

class ProceduralTasksProducer(val produceTasks: TasksBuilder.() -> List<Task>?) : TasksProducer {

    override fun produceTasks(ctx: Context, match: Match) = TasksBuilder(ctx, match).produceTasks()
}

private object EmptyStepsProducer : StepsProducer {
    override fun produceSteps(ctx: Context, sub: Expression) = emptyList<Transformation>()
}
