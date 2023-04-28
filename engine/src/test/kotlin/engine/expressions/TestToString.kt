package engine.expressions

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import parser.parseExpression
import java.util.stream.Stream
import kotlin.test.assertEquals

class TestToString {

    data class TestCase(val text: String, val solver: String, val latex: String)

    @ParameterizedTest
    @MethodSource("testCaseProvider")
    fun test(testCase: TestCase) {
        val expr = parseExpression(testCase.text)
        assertEquals(testCase.solver, expr.toString(), "solver string incorrect")
        assertEquals(testCase.latex, expr.toLatexString(), "latex string incorrect")
    }

    companion object {

        @JvmStatic
        fun testCaseProvider(): Stream<TestCase> = Stream.of(
            TestCase("3.1415", "3.1415", "3.1415"),
            TestCase("35.000", "35.000", "35.000"),
            TestCase("2.71[82]", "2.71[82]", "2.71\\overline{82}"),
            TestCase("0.00[100]", "0.00[100]", "0.00\\overline{100}"),
            TestCase("0.[6]", "0.[6]", "0.\\overline{6}"),
            TestCase("1+1", "1 + 1", "1 + 1"),
            TestCase("x-y", "x - y", "x - y"),
            TestCase("[3/5]", "[3 / 5]", "\\frac{3}{5}"),
            TestCase("[x^1+n]", "[x ^ 1 + n]", "x^{1 + n}"),
            TestCase("xyz", "x y z", "x y z"),
            TestCase(
                "(3 + 2)*4:25",
                "(3 + 2) * 4 : 25",
                "\\left( 3 + 2 \\right) \\times 4 \\div 25",
            ),
            TestCase(
                "sqrt[[b^2] - 4ac]",
                "sqrt[[b ^ 2] - 4 a c]",
                "\\sqrt{b^{2} - 4 a c}",
            ),
            TestCase("root[5, 6]", "root[5, 6]", "\\sqrt[6]{5}"),
            TestCase("root[5, 2]", "root[5, 2]", "\\sqrt[2]{5}"),
            TestCase("1+[.2u.] ", "1 + [. 2 u .]", "1 + \\left[ 2 u \\right]"),
            TestCase("1+{.2+u.}", "1 + {. 2 + u .}", "1 + \\left\\{ 2 + u \\right\\}"),
            TestCase("[[.2.]^3]", "[[. 2 .] ^ 3]", "\\left[ 2 \\right]^{3}"),
            TestCase("[{.2.}^3]", "[{. 2 .} ^ 3]", "\\left\\{ 2 \\right\\}^{3}"),
            TestCase("x = y", "x = y", "x = y"),
            TestCase("1+-2", "1 + -2", "1 + -2"),
            TestCase("-1+2", "-1 + 2", "-1 + 2"),
            TestCase("+1-2", "+1 - 2", "+1 - 2"),
            TestCase("+/- 2", "+/-2", "\\pm 2"),
            TestCase("1+/-2", "1 +/- 2", "1 \\pm 2"),
            TestCase("1 + +/-2", "1 + +/-2", "1 + \\pm 2"),
            TestCase("1+(+/-2)", "1 + (+/-2)", "1 + \\left( \\pm 2 \\right)"),
            TestCase("1+<.2+3.>", "1 + <. 2 + 3 .>", "1 + 2 + 3"),
            TestCase("1+<.-2+3.>", "1 + <. -2 + 3 .>", "1 - 2 + 3"),
            TestCase("<.2+3.>-1", "<. 2 + 3 .> - 1", "2 + 3 - 1"),
            TestCase("<.-2-3.>+1", "<. -2 - 3 .> + 1", "-2 - 3 + 1"),
        )
    }
}
