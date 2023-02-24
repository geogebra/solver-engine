package engine.expressions

import engine.operators.InfinityOperator
import engine.operators.SetOperators
import engine.operators.UndefinedOperator

object Constants {
    val Zero = xp(0)
    val One = xp(1)
    val Two = xp(2)
    val Three = xp(3)
    val Four = xp(4)

    val OneHalf = fractionOf(One, Two)

    val Infinity = Expression(InfinityOperator, emptyList())
    val NegativeInfinity = negOf(Infinity)

    val Undefined = Expression(UndefinedOperator, emptyList())

    val EmptySet = solutionSetOf()
    val Reals = Expression(SetOperators.Reals, emptyList())
}
