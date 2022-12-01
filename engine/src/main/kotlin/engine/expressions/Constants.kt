package engine.expressions

import engine.operators.UndefinedOperator

object Constants {
    val Zero = xp(0)
    val One = xp(1)
    val Two = xp(2)
    val Three = xp(3)

    val Undefined = Expression(
        UndefinedOperator, emptyList()
    )
}
