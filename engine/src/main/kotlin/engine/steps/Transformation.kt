package engine.steps

import engine.expressions.Expression
import engine.steps.metadata.Metadata

data class Transformation(
    val fromExpr: Expression,
    val toExpr: Expression,
    val steps: List<Transformation>? = null,
    val explanation: Metadata? = null,
    val skills: List<Metadata> = emptyList(),
)
