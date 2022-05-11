package expressions

import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class PathTest {

    @Test
    fun foo() {
        val path = pathOf(1, 2, 3)
        //assertEquals(3, path.length)
        //assertEquals(3, path.length)
    }

    @Test
    fun testSubstituteIn() {


        val superExpression = parseExpression("3*5 + [7/4]")

        val substituted = superExpression.substitute(Subexpression(pathOf(0, 1), IntegerExpr(4)))

        assertEquals(parseExpression("3*4 + [7/4]"), substituted)
    }

}