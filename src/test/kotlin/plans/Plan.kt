package plans

import expressions.RootPath
import expressions.Subexpression
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestPlan {

    @Test
    fun testConvertMixedNumberToImproperFractionsPlan() {
        val inExpr = parseExpression("[1 2/3]")
        val trans = ConvertMixedNumberToImproperFraction.tryExecute(Subexpression(RootPath, inExpr))
        assertNotNull(trans)
        assertNotNull(trans.steps)
        assertEquals(ConvertMixedNumberToImproperFraction.plans.size, trans.steps?.size)
        assertEquals(parseExpression("[5/3]"), trans.toExpr)
        trans.prettyPrint()
    }

    @Test
    fun testAddUnlikeFractionsPlan() {
        val inExpr = parseExpression("[3/8] + [5/12]")
        val trans = AddUnlikeFractions.tryExecute(Subexpression(RootPath, inExpr))
        assertNotNull(trans)
        assertNotNull(trans.steps)
        assertEquals(AddUnlikeFractions.plans.size, trans.steps?.size)
        assertEquals(parseExpression("[19 / 24]"), trans.toExpr)
        trans.prettyPrint()
    }

    @Test
    fun testAddMixedNumbersPlan() {
        val inExpr = parseExpression("[5 1/4] + [2 2/3]")
        val trans = AddMixedNumbers.tryExecute(Subexpression(RootPath, inExpr))
        assertNotNull(trans)
        assertNotNull(trans.steps)
        assertEquals(AddMixedNumbers.plans.size, trans.steps?.size)
        assertEquals(parseExpression("[7 11/12]"), trans.toExpr)
        trans.prettyPrint()
    }
}