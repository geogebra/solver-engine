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
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Expression
import engine.methods.stepsproducers.StepsProducerBuilderMarker
import engine.patterns.AnyPattern
import engine.patterns.ExpressionProvider
import engine.patterns.Pattern
import engine.steps.Transformation
import engine.steps.metadata.FixedKeyMetadataMaker
import engine.steps.metadata.GeneralMetadataMaker
import engine.steps.metadata.Metadata
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.MetadataMaker

abstract class CompositeMethod(
    val specificPlans: List<Method> = emptyList(),
) : Method, Runner {
    override fun tryExecute(ctx: Context, sub: Expression): Transformation? {
        ctx.requireActive()
        return run(ctx, sub)
    }

    override val minDepth: Int
        get() = super<Runner>.minDepth
}

@StepsProducerBuilderMarker
open class CompositeMethodBuilder {
    var pattern: Pattern = AnyPattern()
    var resultPattern: Pattern = AnyPattern()
    lateinit var explanation: MetadataKey

    private lateinit var explicitExplanationMaker: MetadataMaker
    protected var skillMakers: MutableList<MetadataMaker>? = null
    private var specificPlansList: MutableList<Method> = mutableListOf()

    fun specificPlans(vararg plans: Method) {
        specificPlansList.addAll(plans)
    }

    internal val specificPlans: List<Method> get() = specificPlansList

    fun explanationParameters(parameters: MappedExpressionBuilder.() -> List<Expression>) {
        explicitExplanationMaker = FixedKeyMetadataMaker(explanation, parameters)
    }

    fun explanationParameters(vararg params: ExpressionProvider) {
        explanationParameters { params.map { move(it) } }
    }

    fun explanation(init: MappedExpressionBuilder.() -> Metadata) {
        explicitExplanationMaker = GeneralMetadataMaker(init)
    }

    fun skill(
        skillKey: MetadataKey,
        skillParameters: MappedExpressionBuilder.() -> List<Expression> = { emptyList() },
    ) {
        skillMakers = skillMakers ?: mutableListOf()
        skillMakers!!.add(FixedKeyMetadataMaker(skillKey, skillParameters))
    }

    fun skill(skillKey: MetadataKey, vararg params: ExpressionProvider) {
        skillMakers = skillMakers ?: mutableListOf()
        skillMakers!!.add(FixedKeyMetadataMaker(skillKey) { params.map { move(it) } })
    }

    protected val explanationMaker get() = when {
        ::explicitExplanationMaker.isInitialized -> explicitExplanationMaker
        else -> FixedKeyMetadataMaker(explanation) { emptyList() }
    }
}
