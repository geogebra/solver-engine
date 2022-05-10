package parser

import expressions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class ParserTest {

    data class TestCase(val text: String, val expr: Expression)

    @ParameterizedTest
    @MethodSource("testCaseProvider")
    fun testParser(testCase: TestCase) {
        assertEquals(testCase.expr, parseExpression(testCase.text), testCase.text)
    }

    companion object {

        @JvmStatic
        fun testCaseProvider(): Stream<TestCase> = Stream.of(
            TestCase("1+2", sumOf(xp(1), xp(2))),
            TestCase("3-2*5", sumOf(xp(3), negOf(productOf(xp(2), xp(5))))),
            TestCase(
                "(-5+2)*7",
                productOf(
                    bracketOf(sumOf(negOf(xp(5)), xp(2))),
                    xp(7)
                )
            ),
            TestCase(
                "2[x^3][y^5]",
                implicitProductOf(
                    xp(2),
                    powerOf(xp("x"), xp(3)),
                    powerOf(xp("y"), xp(5))
                )
            ),
            TestCase(
                "[1/2][x^3]",
                implicitProductOf(
                    fractionOf(xp(1), xp(2)),
                    powerOf(xp("x"), xp(3))
                )
            ),
        )
    }
}
