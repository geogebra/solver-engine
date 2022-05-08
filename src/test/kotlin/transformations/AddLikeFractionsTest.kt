package transformations

import expressions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class AddLikeFractionsTest {

    @Test
    fun testAddLikeFractionsInSum() {
        val expr = sumOf(
            IntegerExpr(1),
            fractionOf(IntegerExpr(2), IntegerExpr(10)),
            VariableExpr("z"),
            fractionOf(IntegerExpr(3), IntegerExpr(10)),
        )
        val step = AddLikeFractions.apply(Subexpression(RootPath, expr))
        assertEquals(
            sumOf(
                IntegerExpr(1),
                fractionOf(sumOf(IntegerExpr(2), IntegerExpr(3)), IntegerExpr(10)),
                VariableExpr("z"),
            ),
            step?.toExpr,
        )
        step?.prettyPrint()
    }
}