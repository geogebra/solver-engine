package methods.integerroots

import methods.rules.testRule
import org.junit.jupiter.api.Test

class HigherOrderIntegerRootRulesTest {

    @Test
    fun testTurnPowerOfRootToRootOfPower() {
        testRule(
            "[(root[5, 4]) ^ 3]",
            turnPowerOfRootToRootOfPower,
            "root[[5 ^ 3], 4]"
        )
        testRule(
            "[(sqrt[3]) ^ 5]",
            turnPowerOfRootToRootOfPower,
            "sqrt[[3 ^ 5]]"
        )
    }

    @Test
    fun testSimplifyRootOfRoot() {
        testRule(
            "root[root[5, 3], 4]",
            simplifyRootOfRoot,
            "root[5, 4 * 3]"
        )
        testRule(
            "sqrt[sqrt[3]]",
            simplifyRootOfRoot,
            "root[3, 2 * 2]"
        )
        testRule(
            "root[sqrt[6], 3]",
            simplifyRootOfRoot,
            "root[6, 3 * 2]"
        )
    }

    @Test
    fun testPutRootCoefficientUnderRoot() {
        testRule(
            "7 * root[20, 3]",
            putRootCoefficientUnderRoot,
            "root[[7 ^ 3] * 20, 3]"
        )
        testRule(
            "8 * sqrt[40]",
            putRootCoefficientUnderRoot,
            "sqrt[[8 ^ 2] * 40]"
        )
    }
}
