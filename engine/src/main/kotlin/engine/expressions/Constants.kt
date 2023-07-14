package engine.expressions

import engine.operators.EulerEOperator
import engine.operators.ImaginaryUnitOperator
import engine.operators.InfinityOperator
import engine.operators.PiOperator
import engine.operators.UndefinedOperator

@Suppress("MagicNumber")
object Constants {
    val MinusOne = xp(-1)
    val Zero = xp(0)
    val One = xp(1)
    val Two = xp(2)
    val Three = xp(3)
    val Four = xp(4)

    val OneHalf = fractionOf(One, Two)

    val Pi = expressionOf(PiOperator, emptyList())
    val E = expressionOf(EulerEOperator, emptyList())
    val ImaginaryUnit = expressionOf(ImaginaryUnitOperator, emptyList())

    val Infinity = expressionOf(InfinityOperator, emptyList())
    val NegativeInfinity = negOf(Infinity)

    val Undefined = expressionOf(UndefinedOperator, emptyList())

    val EmptySet = solutionSetOf()
    val Reals = Reals()
}
