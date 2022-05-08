package transformations

import expressions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CancelInAFractionTest {

    @Test
    fun testCancelSimpleFraction() {
        val expression = fractionOf(
            productOf(VariableExpr("x"), VariableExpr("y"), VariableExpr("z")),
            productOf(VariableExpr("a"), VariableExpr("y"), VariableExpr("c"))
        )

        val step = CancelInAFraction.apply(Subexpression(RootPath, expression))
        assertEquals(
            fractionOf(
                productOf(VariableExpr("x"), VariableExpr("z")),
                productOf(VariableExpr("a"), VariableExpr("c")),
            ),
            step?.toExpr
        )

        step?.prettyPrint()
    }
}