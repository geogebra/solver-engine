package engine.expressions

import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.junit.jupiter.api.Test
import parser.parseExpression

class TestToJson {
    fun test(text: String, expectedJson: String) {
        val expr = parseExpression(text).toJson()
        assertThatJson(expr).isEqualTo(expectedJson)
    }

    @Test
    fun sumTest() = test(
        "1 + 1 - x",
        """{
            "type": "Sum",
            "operands": [
                {"type": "Integer", "value" : "1"},
                {"type": "Integer", "value" : "1"},
                {"type": "Minus", "operands": [{"type": "Variable", "value": "x"}]}
            ]
        }""",
    )

    @Test
    fun productTest() = test(
        "2x*3*y",
        """{
            "type": "SmartProduct",
            "operands": [
                {"type": "Integer",  "value": "2"},
                {"type": "Variable", "value": "x"},
                {"type": "Integer",  "value": "3"},
                {"type": "Variable", "value": "y"}
            ],
            "signs": [false, false, true, true]
        }""",
    )

    @Test
    fun productTestWithMissingBracket() = test(
        "3*-x",
        """{
            "type": "SmartProduct",
            "operands": [
                {"type": "Integer", "value": "3"},
                {"type": "Minus", "operands": [{"type": "Variable", "value": "x"}], "decorators": ["MissingBracket"]}
            ],
            "signs": [false, true]
        }""",
    )

    @Test
    fun bracketsTest() = test(
        "((x)) + [.2.] + {.(1.35).}",
        """{
            "type": "Sum",
            "operands": [
                {"type": "Variable", "value": "x",   "decorators": ["RoundBracket", "RoundBracket"]},
                {"type": "Integer",  "value": "2",   "decorators": ["SquareBracket"]},
                {"type": "Decimal", "value": "1.35", "decorators": ["RoundBracket", "CurlyBracket"]}
            ]
            }""",
    )

    @Test
    fun squareRootTest() = test(
        "sqrt[x]",
        """{
            "type": "SquareRoot",
            "operands": [{"type": "Variable", "value": "x"}]
            }""",
    )

    @Test
    fun rootTest() = test(
        "root[x, 3.3[12]]",
        """{
            "type": "Root",
            "operands": [
                {"type": "Variable", "value": "x"},
                {"type": "RecurringDecimal", "value": "3.3[12]"}   
            ]
            }""",
    )

    @Test
    fun mixedNumberTest() = test(
        "[1 1/2]",

        """{
            "type": "MixedNumber",
            "operands": [
                {"type": "Integer", "value": "1"},
                {"type": "Integer", "value": "1"},
                {"type": "Integer", "value": "2"}
            ]
            }""",
    )

    @Test
    fun equationSystemTest() = test(
        "x = y AND y = z",
        """{
            "type": "EquationSystem",
            "operands": [
                {
                    "type": "Equation",
                    "operands": [{"type": "Variable", "value": "x"}, {"type": "Variable", "value": "y"}]
                },
                {
                    "type": "Equation",
                    "operands": [{"type": "Variable", "value": "y"}, {"type": "Variable", "value": "z"}]
                }
            ]
            }""",
    )

    @Test
    fun nameTest() = test(
        """ "hello there!" """,
        """{
            "type": "Name",
            "value": "hello there!"
        }""",
    )

    @Test
    fun solutionTest() = test(
        "SetSolution[x, y : {(1, 2)}]",
        """{
            "type": "SetSolution",
            "operands": [
                {
                    "type": "VariableList",
                    "operands": [{"type": "Variable", "value": "x"}, {"type": "Variable", "value": "y"}]
                },
                {
                    type: "FiniteSet",
                    "operands": [{
                        "type": "Tuple",
                        "operands": [{"type": "Integer", "value": "1"}, {"type": "Integer", "value": "2"}]
                    }]
                }
            ]
        }""",
    )
}
