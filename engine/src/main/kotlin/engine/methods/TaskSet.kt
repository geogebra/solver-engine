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
    private val tasksProducer: TasksProducer,
    specificPlans: List<Method> = emptyList(),
) : CompositeMethod(specificPlans) {
    override fun run(ctx: Context, sub: Expression): Transformation? {
        for (match in pattern.findMatches(ctx, RootMatch, sub)) {
            val tasks = tasksProducer.produceTasks(ctx, sub, match)
            if (tasks != null) {
                val toExpr = tasks.last().toExpr.withOrigin(Combine(listOf(sub)))
                return Transformation(
                    type = Transformation.Type.TaskSet,
                    fromExpr = sub,
                    toExpr = toExpr,
                    tasks = tasks,
                    explanation = explanationMaker.make(ctx, sub, match),
                    skills = skillMakers.map { it.make(ctx, sub, match) },
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
            explanationMaker = explanationMaker,
            skillMakers = skillMakers,
            tasksProducer = tasksProducer,
            specificPlans = specificPlans,
        )
    }
}

fun taskSet(init: TaskSetBuilder.() -> Unit): TaskSet {
    val taskSetBuilder = TaskSetBuilder()
    taskSetBuilder.init()
    return taskSetBuilder.buildTaskSet()
}
