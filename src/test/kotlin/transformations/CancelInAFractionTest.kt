package transformations

import expressions.VariableExpr
import expressions.fractionOf
import expressions.productOf
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CancelInAFractionTest {

    @Test
    fun testCancelSimpleFraction() {
        val expression = fractionOf(
            productOf(VariableExpr("x"), VariableExpr("y"), VariableExpr("z")),
            productOf(VariableExpr("a"), VariableExpr("y"), VariableExpr("c"))
        )

        val step = CancelInAFraction.apply(expression)
        assertEquals(
            fractionOf(
                productOf(VariableExpr("x"), VariableExpr("z")),
                productOf(VariableExpr("a"), VariableExpr("c")),
            ),
            step?.toExpr
        )
    }
}