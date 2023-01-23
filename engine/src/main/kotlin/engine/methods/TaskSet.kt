package engine.methods

import engine.context.Context
import engine.expressions.Combine
import engine.expressions.Expression
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.steps.Task
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker

class TaskSet(
    private val pattern: Pattern,
    private val explanationMaker: MetadataMaker,
    private val skillMakers: List<MetadataMaker> = emptyList(),
    private val tasksProducer: TasksProducer
) : CompositeMethod() {
    override fun run(ctx: Context, sub: Expression): TransformationResult? {
        for (match in pattern.findMatches(ctx, RootMatch, sub)) {
            val tasks = tasksProducer.produceTasks(ctx, match)
            if (tasks != null) {
                val toExpr = tasks.last().toExpr.withOrigin(Combine(listOf(sub)))
                return TransformationResult(
                    type = Transformation.Type.TaskSet,
                    toExpr = toExpr,
                    tasks = tasks,
                    explanation = explanationMaker.make(ctx, match),
                    skills = skillMakers.map { it.make(ctx, match) }
                )
            }
        }
        return null
    }
}

class TaskSetBuilder : CompositeMethodBuilder() {
    private lateinit var tasksProducer: TasksProducer

    private fun checkNotInitialized() {
        check(!::tasksProducer.isInitialized)
    }

    fun tasks(init: TasksBuilder.() -> List<Task>?) {
        checkNotInitialized()
        tasksProducer = ProceduralTasksProducer(init)
    }

    fun buildTaskSet(): TaskSet {
        return TaskSet(
            pattern = pattern,
            explanationMaker = MetadataMaker(explanation, explanationParameters),
            skillMakers = skillMakers,
            tasksProducer = tasksProducer
        )
    }
}

fun taskSet(init: TaskSetBuilder.() -> Unit): TaskSet {
    val taskSetBuilder = TaskSetBuilder()
    taskSetBuilder.init()
    return taskSetBuilder.buildTaskSet()
}
