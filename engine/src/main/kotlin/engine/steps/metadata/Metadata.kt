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

package engine.steps.metadata

import engine.context.Context
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Expression
import engine.patterns.Match

/**
 * Enums containing translation keys should be annotated with this so that the keys will be imported into ggbtrans.
 */
@Target(AnnotationTarget.CLASS)
annotation class TranslationKeys

@Target(AnnotationTarget.FIELD)
annotation class LegacyKeyName(val name: String)

interface MetadataKey {
    val keyName: String
}

interface CategorisedMetadataKey : MetadataKey {
    val category: String
    override val keyName get() = "$category.$this"
}

data class Metadata(val key: MetadataKey, val mappedParams: List<Expression>)

interface MetadataMaker {
    fun make(context: Context, expression: Expression, match: Match): Metadata
}

data class FixedKeyMetadataMaker(
    val key: MetadataKey,
    val parameters: MappedExpressionBuilder.() -> List<Expression>,
) : MetadataMaker {
    override fun make(context: Context, expression: Expression, match: Match) =
        Metadata(
            key = key,
            mappedParams = with(MappedExpressionBuilder(context, expression, match)) { parameters() },
        )
}

data class GeneralMetadataMaker(val init: MappedExpressionBuilder.() -> Metadata) : MetadataMaker {
    override fun make(context: Context, expression: Expression, match: Match): Metadata {
        val builder = MappedExpressionBuilder(context, expression, match)
        return builder.init()
    }
}

fun metadata(key: MetadataKey, vararg parameters: Expression) = Metadata(key, parameters.asList())

fun metadata(key: MetadataKey, parameters: List<Expression>) = Metadata(key, parameters)
