package methods.integerroots

import engine.methods.testRule
import methods.integerroots.IntegerRootsRules.PutRootCoefficientUnderRoot
import methods.integerroots.IntegerRootsRules.SimplifyRootOfRoot
import methods.integerroots.IntegerRootsRules.TurnPowerOfRootToRootOfPower
import org.junit.jupiter.api.Test

class HigherOrderIntegerRootRulesTest {

    @Test
    fun testTurnPowerOfRootToRootOfPower() {
        testRule(
            "[(root[5, 4]) ^ 3]",
            TurnPowerOfRootToRootOfPower,
            "root[[5 ^ 3], 4]"
        )
        testRule(
            "[(sqrt[3]) ^ 5]",
            TurnPowerOfRootToRootOfPower,
            "sqrt[[3 ^ 5]]"
        )
    }

    @Test
    fun testSimplifyRootOfRoot() {
        testRule(
            "root[root[5, 3], 4]",
            SimplifyRootOfRoot,
            "root[5, 4 * 3]"
        )
        testRule(
            "sqrt[sqrt[3]]",
            SimplifyRootOfRoot,
            "root[3, 2 * 2]"
        )
        testRule(
            "root[sqrt[6], 3]",
            SimplifyRootOfRoot,
            "root[6, 3 * 2]"
        )
    }

    @Test
    fun testPutRootCoefficientUnderRoot() {
        testRule(
            "7 * root[20, 3]",
            PutRootCoefficientUnderRoot,
            "root[[7 ^ 3] * 20, 3]"
        )
        testRule(
            "8 * sqrt[40]",
            PutRootCoefficientUnderRoot,
            "sqrt[[8 ^ 2] * 40]"
        )
    }
}
