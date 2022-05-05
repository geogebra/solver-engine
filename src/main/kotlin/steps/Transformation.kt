package steps

import expressions.Expression

abstract class Transformation(val type: String, val params: Sequence<Expression>)