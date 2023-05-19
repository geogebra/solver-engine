package engine.expressions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import parser.parseExpression

class TestToString {

    fun test(text: String, solver: String, latex: String) {
        val expr = parseExpression(text)
        assertEquals(solver, expr.toString(), "solver string incorrect")
        assertEquals(latex, expr.toLatexString(), "latex string incorrect")
    }

    @Test
    fun testDecimals() {
        test("3.1415", "3.1415", "3.1415")
        test("35.000", "35.000", "35.000")
        test("2.71[82]", "2.71[82]", "2.71\\overline{82}")
        test("0.00[100]", "0.00[100]", "0.00\\overline{100}")
        test("0.[6]", "0.[6]", "0.\\overline{6}")
    }

    @Test
    fun testOperators() {
        test("1+1", "1 + 1", "1 + 1")
        test("x-y", "x - y", "x - y")
        test("[3/5]", "[3 / 5]", "\\frac{3}{5}")
        test("[x^1+n]", "[x ^ 1 + n]", "x^{1 + n}")
        test("xyz", "x y z", "x y z")
        test(
            "(3 + 2)*4:25",
            "(3 + 2) * 4 : 25",
            "\\left( 3 + 2 \\right) \\times 4 \\div 25",
        )
        test(
            "sqrt[[b^2] - 4ac]",
            "sqrt[[b ^ 2] - 4 a c]",
            "\\sqrt{b^{2} - 4 a c}",
        )
        test("root[5, 6]", "root[5, 6]", "\\sqrt[6]{5}")
        test("root[5, 2]", "root[5, 2]", "\\sqrt[2]{5}")
        test("x = y", "x = y", "x = y")
    }

    @Test
    fun testSigns() {
        test("1+-2", "1 + -2", "1 + -2")
        test("-1+2", "-1 + 2", "-1 + 2")
        test("+1-2", "+1 - 2", "+1 - 2")
        test("+/- 2", "+/-2", "\\pm 2")
        test("1+/-2", "1 +/- 2", "1 \\pm 2")
        test("1 + +/-2", "1 + +/-2", "1 + \\pm 2")
        test("1+(+/-2)", "1 + (+/-2)", "1 + \\left( \\pm 2 \\right)")
    }

    @Test
    fun testBrackets() {
        test("1+[.2u.] ", "1 + [. 2 u .]", "1 + \\left[ 2 u \\right]")
        test("1+{.2+u.}", "1 + {. 2 + u .}", "1 + \\left\\{ 2 + u \\right\\}")
        test("[[.2.]^3]", "[[. 2 .] ^ 3]", "\\left[ 2 \\right]^{3}")
        test("[{.2.}^3]", "[{. 2 .} ^ 3]", "\\left\\{ 2 \\right\\}^{3}")
    }

    @Test
    fun testPartialExpressions() {
        test("1+<.2+3.>", "1 + <. 2 + 3 .>", "1 + 2 + 3")
        test("1+<.-2+3.>", "1 + <. -2 + 3 .>", "1 - 2 + 3")
        test("<.2+3.>-1", "<. 2 + 3 .> - 1", "2 + 3 - 1")
        test("<.-2-3.>+1", "<. -2 - 3 .> + 1", "-2 - 3 + 1")
    }

    @Test
    fun testGreekLetters() {
        test("\\alpha", "\\alpha", "\\alpha")
        test("\\mu", "\\mu", "\\mu")
        test("\\omega", "\\omega", "\\omega")
        test("\\Alpha", "\\Alpha", "\\Alpha")
        test("\\Mu", "\\Mu", "\\Mu")
        test("\\Omega", "\\Omega", "\\Omega")
    }

    @Test
    fun testCalculus() {
        test("diff[sin x / dx]", "diff[sin x / dx]", "\\frac{\\mathrm{d} \\sin x}{\\mathrm{d} x}")
        test(
            "[diff ^ 2][sin x * sin y / dx dy]",
            "[diff ^ 2][sin x * sin y / dx dy]",
            "\\frac{\\mathrm{d}^{2} \\sin x \\times \\sin y}{\\mathrm{d} x \\, \\mathrm{d} y}",
        )
        test(
            "prim[arsinh(x + 1), x]",
            "prim[arsinh (x + 1), x]",
            "\\int \\arsinh \\left( x + 1 \\right) \\, \\mathrm{d}x",
        )
        test(
            "int[-/infinity/, /pi/, 3x + 1, x]",
            "int[-/infinity/, /pi/, 3 x + 1, x]",
            "\\int_{-\\infty}^{\\pi} 3 x + 1 \\, \\mathrm{d}x",
        )
    }

    @Test
    fun testLinearAlgebra() {
        test(
            "vec[1, 2, 3]",
            "vec[1, 2, 3]",
            "\\begin{pmatrix}1 \\\\ 2 \\\\ 3\\end{pmatrix}",
        )
        test(
            "mat[1, 2; 3, 4]",
            "mat[1, 2; 3, 4]",
            "\\begin{pmatrix}1 & 2 \\\\ 3 & 4\\end{pmatrix}",
        )
    }
}
