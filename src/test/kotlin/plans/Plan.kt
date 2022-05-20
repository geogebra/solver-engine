package plans

import context.Context
import context.emptyContext
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
        val trans = convertMixedNumberToImproperFraction.tryExecute(emptyContext, Subexpression(RootPath, inExpr))
        assertNotNull(trans)
        assertNotNull(trans.steps)
        assertEquals(convertMixedNumberToImproperFraction.plans.size, trans.steps?.size)
        assertEquals(parseExpression("[5/3]"), trans.toExpr.expr)
        trans.prettyPrint()
    }

    @Test
    fun testAddUnlikeFractionsPlan() {
        val inExpr = parseExpression("[3/8] + [5/12]")
        val trans = addUnlikeFractions.tryExecute(emptyContext, Subexpression(RootPath, inExpr))
        assertNotNull(trans)
        assertNotNull(trans.steps)
        assertEquals(addUnlikeFractions.plans.size, trans.steps?.size)
        assertEquals(parseExpression("[19 / 24]"), trans.toExpr.expr)
        trans.prettyPrint()
    }

    @Test
    fun testAddMixedNumbersPlan() {
        val inExpr = parseExpression("[5 1/4] + [2 2/3]")
        val trans = addMixedNumbersByConverting.tryExecute(emptyContext, Subexpression(RootPath, inExpr))
        assertNotNull(trans)
        assertNotNull(trans.steps)
        assertEquals(addMixedNumbersByConverting.plans.size, trans.steps?.size)
        assertEquals(parseExpression("[7 11/12]"), trans.toExpr.expr)
        trans.prettyPrint()
    }

    @Test
    fun testContextSensitivePlanEU() {
        val inExpr = parseExpression("[5 1/4] + [2 2/3]")

        val trans1 = addMixedNumbers.tryExecute(Context("EU"), Subexpression(RootPath, inExpr))
        assertNotNull(trans1)
        assertNotNull(trans1.steps)
        assertEquals(addMixedNumbersByConverting.plans.size, trans1.steps?.size)
        assertEquals(parseExpression("[7 11/12]"), trans1.toExpr.expr)
    }

    @Test
    fun testContextSensitivePlanUS() {
        val inExpr = parseExpression("[5 1/4] + [2 2/3]")

        val trans2 = addMixedNumbers.tryExecute(Context("US"), Subexpression(RootPath, inExpr))
        assertNotNull(trans2)
        assertNotNull(trans2.steps)
        assertEquals(parseExpression("[7 11/12]"), trans2.toExpr.expr)
        assertEquals(addMixedNumbersUsingCommutativity.plans.size, trans2.steps?.size)

        trans2.prettyPrint()
    }


    @Test
    fun testContextSensitivePlanDefault() {
        val inExpr = parseExpression("[5 1/4] + [2 2/3]")

        val trans3 = addMixedNumbers.tryExecute(emptyContext, Subexpression(RootPath, inExpr))
        assertNotNull(trans3)
        assertNotNull(trans3.steps)
        assertEquals(addMixedNumbersByConverting.plans.size, trans3.steps?.size)
        assertEquals(parseExpression("[7 11/12]"), trans3.toExpr.expr)
    }
}