package transformations

import expressions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MixedNumbersTest {

    @Test
    fun properFractionToMixedNumber() {
        val expression = fractionOf(IntegerExpr(4), IntegerExpr(21))

        val step = FractionToMixedNumber.apply(Subexpression(RootPath, expression))

        assertNull(step)
    }

    @Test
    fun improperFractionToMixedNumber() {
        val expression = fractionOf(IntegerExpr(21), IntegerExpr(4))

        val step = FractionToMixedNumber.apply(Subexpression(RootPath, expression))

        assertEquals(
            MixedNumber(IntegerExpr(5), IntegerExpr(1), IntegerExpr(4)),
            step?.toExpr,
        )

        step?.prettyPrint()
    }

    @Test
    fun splitMixedNumber() {
        val expression = MixedNumber(IntegerExpr(2), IntegerExpr(3), IntegerExpr(4))
        val step = SplitMixedNumber.apply(Subexpression(RootPath, expression))
        assertEquals(
            sumOf(IntegerExpr(2), fractionOf(IntegerExpr(3), IntegerExpr(4))),
            step?.toExpr,
        )
        step?.prettyPrint()
    }
}
