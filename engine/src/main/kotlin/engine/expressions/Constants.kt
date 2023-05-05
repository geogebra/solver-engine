package engine.expressions

import engine.operators.InfinityOperator
import engine.operators.SetOperators
import engine.operators.UndefinedOperator

object Constants {
    val Zero = xp(0)
    val One = xp(1)
    val Two = xp(2)

    @Suppress("MagicNumber")
    val Three = xp(3)

    @Suppress("MagicNumber")
    val Four = xp(4)

    val OneHalf = fractionOf(One, Two)

    val Infinity = expressionOf(InfinityOperator, emptyList())
    val NegativeInfinity = negOf(Infinity)

    val Undefined = expressionOf(UndefinedOperator, emptyList())

    val EmptySet = solutionSetOf()
    val Reals = expressionOf(SetOperators.Reals, emptyList())
}
