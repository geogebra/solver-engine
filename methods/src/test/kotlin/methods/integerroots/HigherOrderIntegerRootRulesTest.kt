package methods.integerroots

import methods.rules.RuleTestCase
import org.junit.jupiter.api.Test

class HigherOrderIntegerRootRulesTest {

    @Test
    fun testTurnPowerOfRootToRootOfPower() {
        RuleTestCase(
            "[(root[5, 4]) ^ 3]",
            turnPowerOfRootToRootOfPower,
            "root[[5 ^ 3], 4]"
        ).assert()
        RuleTestCase(
            "[(sqrt[3]) ^ 5]",
            turnPowerOfRootToRootOfPower,
            "sqrt[[3 ^ 5]]"
        ).assert()
    }

    @Test
    fun testSimplifyRootOfRoot() {
        RuleTestCase(
            "root[root[5, 3], 4]",
            simplifyRootOfRoot,
            "root[5, 4 * 3]"
        ).assert()
        RuleTestCase(
            "sqrt[sqrt[3]]",
            simplifyRootOfRoot,
            "root[3, 2 * 2]"
        ).assert()
        RuleTestCase(
            "root[sqrt[6], 3]",
            simplifyRootOfRoot,
            "root[6, 3 * 2]"
        ).assert()
    }

    @Test
    fun testPutRootCoefficientUnderRoot() {
        RuleTestCase(
            "7 * root[20, 3]",
            putRootCoefficientUnderRoot,
            "root[[7 ^ 3] * 20, 3]"
        ).assert()
        RuleTestCase(
            "8 * sqrt[40]",
            putRootCoefficientUnderRoot,
            "sqrt[[8 ^ 2] * 40]"
        ).assert()
    }
}
