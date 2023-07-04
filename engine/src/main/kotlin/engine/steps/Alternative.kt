package engine.steps

import engine.methods.Strategy

data class Alternative(
    val strategy: Strategy,
    val steps: List<Transformation>,
)
