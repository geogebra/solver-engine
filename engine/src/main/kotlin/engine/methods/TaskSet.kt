/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package engine.methods

import engine.context.Context
import engine.expressions.Combine
import engine.expressions.Expression
import engine.patterns.NaryPattern
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.steps.Task
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker
import engine.steps.metadata.metadata

class TaskSet(
    private val pattern: Pattern,
    private val explanationMaker: MetadataMaker,
    private val skillMakers: List<MetadataMaker>?,
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
                    skills = skillMakers?.map { it.make(ctx, sub, match) },
                )
            }
        }
        return null
    }

    override val minDepth = pattern.minDepth
}

class PartialExpressionTaskSet(
    private val pattern: NaryPattern,
    private val explanationMaker: MetadataMaker,
    private val skillMakers: List<MetadataMaker>?,
    private val tasksProducer: TasksProducer,
) : CompositeMethod() {
    private val regularTaskSet = TaskSet(pattern, explanationMaker, skillMakers, tasksProducer)

    override fun run(ctx: Context, sub: Expression): Transformation? {
        if (sub.childCount == pattern.childPatterns.size) {
            return regularTaskSet.run(ctx, sub)
        }

        for (match in pattern.findMatches(ctx, RootMatch, sub)) {
            val tasks = tasksProducer.produceTasks(ctx, sub, match)
            if (tasks != null) {
                val matchedChildExpressions = pattern.getMatchedChildExpressions(match)
                val lastResult = tasks.last().toExpr
                val toExpr = pattern.substitute(
                    match,
                    arrayOf(lastResult.withOrigin(Combine(matchedChildExpressions))),
                )

                val finalTasks = tasks + Task(
                    taskId = "#${tasks.size + 1}",
                    startExpr = toExpr,
                    explanation = metadata(SolverEngineExplanation.SubstituteResultOfTaskSet),
                )

                return Transformation(
                    type = Transformation.Type.TaskSet,
                    fromExpr = sub,
                    toExpr = toExpr,
                    tasks = finalTasks,
                    explanation = explanationMaker.make(ctx, sub, match),
                    skills = skillMakers?.map { it.make(ctx, sub, match) },
                )
            }
        }
        return null
    }

    override val minDepth = pattern.minDepth
}

class TaskSetBuilder : CompositeMethodBuilder() {
    fun tasks(init: TasksBuilder.() -> List<Task>?): TaskSet {
        return TaskSet(
            pattern = pattern,
            explanationMaker = explanationMaker,
            skillMakers = skillMakers,
            tasksProducer = ProceduralTasksProducer(init),
            specificPlans = specificPlans,
        )
    }

    fun partialExpressionTasks(init: TasksBuilder.() -> List<Task>?): PartialExpressionTaskSet {
        return PartialExpressionTaskSet(
            pattern = pattern as NaryPattern,
            explanationMaker = explanationMaker,
            skillMakers = skillMakers,
            tasksProducer = ProceduralTasksProducer(init),
        )
    }
}

fun taskSet(init: TaskSetBuilder.() -> CompositeMethod): CompositeMethod {
    val taskSetBuilder = TaskSetBuilder()
    return taskSetBuilder.init()
}
