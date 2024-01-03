package engine.methods

// Make a plan out of a strategy

fun Strategy.getPlan() =
    plan {
        explanation = this@getPlan.explanation
        steps {
            apply(this@getPlan.steps)
        }
    }
