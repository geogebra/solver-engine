package engine.steps

import engine.expressions.MappedExpression
import engine.expressions.Subexpression
import engine.plans.PlanId
import engine.steps.metadata.Metadata

data class Transformation(
    val fromExpr: Subexpression,
    val toExpr: MappedExpression,
    val planId: PlanId? = null,
    val steps: List<Transformation>? = null,
    val explanation: Metadata? = null,
    val skills: List<Metadata> = emptyList(),
)
