package parser

import engine.expressions.Expression
import engine.expressions.bracketOf
import engine.expressions.curlyBracketOf
import engine.expressions.fractionOf
import engine.expressions.implicitProductOf
import engine.expressions.negOf
import engine.expressions.plusOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.squareBracketOf
import engine.expressions.sumOf
import engine.expressions.xp
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ParserTest {

    data class TestCase(val text: String, val expr: Expression?)

    @ParameterizedTest
    @MethodSource("testCaseProvider")
    fun testParser(testCase: TestCase) {
        if (testCase.expr != null) {
            assertEquals(testCase.expr, parseExpression(testCase.text), testCase.text)
        } else {
            assertFails {
                parseExpression(testCase.text)
            }
        }
    }

    companion object {

        @JvmStatic
        fun testCaseProvider(): Stream<TestCase> = Stream.of(
            TestCase("1+", null),
            TestCase("[1/2", null),
            TestCase("?x", null),
            TestCase("[1/3]??", null),

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
            TestCase(
                "[(x+1)^2]",
                powerOf(bracketOf(sumOf(xp("x"), xp(1))), xp(2))
            ),
            TestCase("2[2^2]", implicitProductOf(xp(2), powerOf(xp(2), xp(2)))), // Should that be correct?
            TestCase("3*-4", productOf(xp(3), invisibleBracketOf(negOf(xp(4))))),
            TestCase("-4*3", negOf(productOf(xp(4), xp(3)))),
            TestCase("--2", negOf(invisibleBracketOf(negOf(xp(2))))),
            TestCase("1+-3", sumOf(xp(1), invisibleBracketOf(negOf(xp(3))))),
            TestCase(
                "3*+-+2",
                productOf(
                    xp(3),
                    invisibleBracketOf(plusOf(invisibleBracketOf(negOf(invisibleBracketOf(plusOf(xp(2)))))))
                )
            ),
            TestCase(
                "{.1 + [.2 * (3 - 6).].}",
                curlyBracketOf(
                    sumOf(
                        xp(1),
                        squareBracketOf(
                            productOf(
                                xp(2),
                                bracketOf(
                                    sumOf(xp(3), negOf(xp(6)))
                                )
                            )
                        )
                    )
                )
            ),
            TestCase(
                "[[1/2]/[3/4]]",
                fractionOf(
                    fractionOf(xp(1), xp(2)),
                    fractionOf(xp(3), xp(4)),
                )
            )
        )
    }
}
