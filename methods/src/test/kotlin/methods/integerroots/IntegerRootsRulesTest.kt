package methods.integerroots

import methods.rules.RuleTest
import methods.rules.RuleTestCase
import java.util.stream.Stream

object IntegerRootsRulesTest : RuleTest {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("sqrt[1]", simplifyRootOfOne, "1"),
        RuleTestCase("root[1, 7]", simplifyRootOfOne, "1"),
        RuleTestCase("root[2, 3]", simplifyRootOfOne, null),
        RuleTestCase("sqrt[0]", simplifyRootOfZero, "0"),
        RuleTestCase("root[0, 7]", simplifyRootOfZero, "0"),
        RuleTestCase("root[2, 3]", simplifyRootOfZero, null),
        RuleTestCase("sqrt[1]", factorizeIntegerUnderSquareRoot, null),
        RuleTestCase("sqrt[144]", factorizeIntegerUnderSquareRoot, "sqrt[[2 ^ 4] * [3 ^ 2]]"),
        RuleTestCase("sqrt[125]", factorizeIntegerUnderSquareRoot, "sqrt[[5 ^ 3]]"),
        RuleTestCase("sqrt[147]", factorizeIntegerUnderSquareRoot, "sqrt[3 * [7 ^ 2]]"),
        RuleTestCase(
            "sqrt[2 * [3 ^ 5] * 5 * [7 ^ 3]]",
            separateOddPowersUnderSquareRoot,
            "sqrt[2 * [3 ^ 4] * 3 * 5 * [7 ^ 3]]"
        ),
        RuleTestCase(
            "sqrt[2 * [3 ^ 4] * 3 * 5 * [7 ^ 3]]",
            splitEvenPowersUnderSeparateRoot,
            "sqrt[[3 ^ 4]] * sqrt[2 * 3 * 5 * [7 ^ 3]]"
        ),
        RuleTestCase(
            "sqrt[[3 ^ 5]]",
            simplifySquareRootOfPower,
            "[3 ^ 4 : 2] * sqrt[3]"
        ),
        RuleTestCase(
            "sqrt[[3 ^ 4]]",
            simplifySquareRootOfPower,
            "[3 ^ 4 : 2]"
        ),
        RuleTestCase(
            "sqrt[[3^2]]",
            simplifySquareRootOfPower,
            "3"
        ),
        RuleTestCase(
            "sqrt[3] * sqrt[3]",
            simplifyMultiplicationOfSquareRoots,
            "3"
        )
    )
}
