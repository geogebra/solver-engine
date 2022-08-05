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
        ),
        RuleTestCase(
            "sqrt[[2^3] * 5]",
            separateFactorizedPowersUnderSquareRootAsSquareRoots,
            "sqrt[[2^3]] * sqrt[5]"
        ),
        RuleTestCase(
            "sqrt[[2^3] * 5 * [7^2]]",
            separateFactorizedPowersUnderSquareRootAsSquareRoots,
            "sqrt[[2^3]] * sqrt[5] * sqrt[[7^2]]"
        ),
        RuleTestCase(
            "sqrt[[2^3]] * sqrt[[3^5]] * sqrt[7] * sqrt[[11^2]]",
            splitPowerUnderSquareRootOfProduct,
            "sqrt[[2^2] * 2] * sqrt[[3^4] * 3] * sqrt[7] * sqrt[[11^2]]"
        ),
        RuleTestCase(
            "sqrt[[2^2] * 2] * sqrt[[3^4] * 3] * sqrt[7] * sqrt[[11^2]]",
            splitProductOfPowerUnderSquareRootAsProductMultipleRemoveBrackets,
            "sqrt[[2^2]] * sqrt[2] * sqrt[[3^4]] * sqrt[3] * sqrt[7] * sqrt[[11^2]]"
        ),
        RuleTestCase(
            "sqrt[[2^2]] * sqrt[2] * sqrt[[3^4]] * sqrt[3] * sqrt[7] * sqrt[[11^2]]",
            simplifyEvenIntegerPowerUnderRootProduct,
            "2 * sqrt[2] * [3^2] * sqrt[3] * sqrt[7] * 11"
        ),
        RuleTestCase(
            "sqrt[[2^4]] * sqrt[2] * sqrt[[3^4]] * sqrt[3] * sqrt[7] * sqrt[[11^6]]",
            simplifyEvenIntegerPowerUnderRootProduct,
            "[2^2] * sqrt[2] * [3^2] * sqrt[3] * sqrt[7] * [11^3]"
        ),
        RuleTestCase(
            "[2^2] * sqrt[2] * [3^2] * sqrt[3] * sqrt[7] * [11^3]",
            rewriteWithIntegerFactorsAtFront,
            "([2^2] * [3^2] * [11^3]) * (sqrt[2] * sqrt[3] * sqrt[7])"
        ),
        RuleTestCase(
            "2 * sqrt[2] * [3^2] * sqrt[3] * sqrt[7] * 11",
            rewriteWithIntegerFactorsAtFront,
            "(2 * [3^2] * 11) * (sqrt[2] * sqrt[3] * sqrt[7])"
        ),
        RuleTestCase(
            "8 * (sqrt[2] * sqrt[3])",
            multiplySquareRootFactors,
            "8 * sqrt[6]"
        ),
        RuleTestCase(
            "10 * (sqrt[2] * sqrt[3] * sqrt[7])",
            multiplySquareRootFactors,
            "10 * sqrt[42]"
        )
    )
}
