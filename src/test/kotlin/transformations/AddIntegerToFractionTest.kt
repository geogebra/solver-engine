package transformations

import expressions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AddIntegerToFractionTest {

    @Test
    fun testAddIntegerToFraction() {
        val expression = sumOf(IntegerExpr(5), fractionOf(IntegerExpr(2), IntegerExpr(4)))

        val step = AddIntegerToFraction.apply(Subexpression(RootPath, expression))

        assertEquals(
            sumOf(
                fractionOf(
                    productOf(IntegerExpr(5), IntegerExpr(4)),
                    productOf(IntegerExpr(1), IntegerExpr(4)),
                ),
                fractionOf(IntegerExpr(2), IntegerExpr(4))
            ),
            step?.toExpr
        )

        step?.prettyPrint()
    }
}