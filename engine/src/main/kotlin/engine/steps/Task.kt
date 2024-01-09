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

package engine.steps

import engine.expressions.Expression
import engine.expressions.RootOrigin
import engine.expressions.RootPath
import engine.steps.metadata.Metadata

/**
 * Contains the information for a task, which is part of a task set.
 */
data class Task(
    /**
     * Identifies the task within the task set.
     */
    val taskId: String,
    /**
     * Expression that will be the fromExpr of the transformation.
     */
    val startExpr: Expression,
    /**
     * An explanation for what the task does in the context of its task set.
     */
    val explanation: Metadata? = null,
    /**
     * Sequence of steps that operate on [startExpr].  It can be empty.
     */
    val steps: List<Transformation> = emptyList(),
    /**
     * Possibly empty list of ids of other tasks in the same task set this task depends on
     */
    val dependsOn: List<String> = emptyList(),
) {
    /**
     * Thinking of the task as a transformation, this would be its toExpr.  Only for internal use.
     */
    internal val toExpr get() = steps.lastOrNull()?.toExpr ?: startExpr

    /**
     * This makes the outcome of the task accessible to implementors of methods, with correct origin.
     */
    val result get() = toExpr.withOrigin(RootOrigin(rootPath))

    val rootPath get() = RootPath(taskId)
}
