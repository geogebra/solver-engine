package methods.inequalities

import engine.methods.testMethod
import engine.methods.testMethodInX
import engine.methods.testRuleInX
import org.junit.jupiter.api.Test

class InequalitiesRulesTest {

    @Test
    fun testExtractSolutionFromConstantInequalityBasedOnSign() {
        testMethodInX {
            inputExpr = "1 >= 0"
            method = InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign
            check {
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromTrueInequality
                    // Can't check the value of the parameter because the parser does not know how to make a
                    // variable list
                    param {}
                }
            }
        }
        testMethodInX {
            inputExpr = "1 < 0"
            method = InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign
            check {
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromFalseInequality
                    // Can't check the value of the parameter because the parser does not know how to make a
                    // variable list
                    param {}
                }
            }
        }
        testMethod {
            inputExpr = "1 >= 0"
            method = InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign
            check {
                explanation {
                    key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                }
            }
        }
        testMethod {
            inputExpr = "1 < 0"
            method = InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign
            check {
                explanation {
                    key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                }
            }
        }
    }

    @Test
    fun testSeparateModulusGreaterThanPositiveConstant() {
        testRuleInX(
            "abs[x - 1] > 3",
            InequalitiesRules.SeparateModulusGreaterThanPositiveConstant,
            "x - 1 < -3 OR x - 1 > 3",
        )
        testRuleInX(
            "abs[x - 1] > 0",
            InequalitiesRules.SeparateModulusGreaterThanPositiveConstant,
            null,
        )
        testRuleInX(
            "abs[x - 1] > -2",
            InequalitiesRules.SeparateModulusGreaterThanPositiveConstant,
            null,
        )
    }

    @Test
    fun testSeparateModulusGreaterThanEqualToPositiveConstant() {
        testRuleInX(
            "abs[x - 1] >= 3",
            InequalitiesRules.SeparateModulusGreaterThanEqualToPositiveConstant,
            "x - 1 >= 3 OR x - 1 <= -3",
        )
        testRuleInX(
            "abs[x - 1] > 3",
            InequalitiesRules.SeparateModulusGreaterThanEqualToPositiveConstant,
            null,
        )
        testRuleInX(
            "2*abs[x - 1] >= 4",
            InequalitiesRules.SeparateModulusGreaterThanEqualToPositiveConstant,
            "2(x - 1) >= 4 OR 2(x - 1) <= -4",
        )
        testRuleInX(
            "abs[x - 1] >= 0",
            InequalitiesRules.SeparateModulusGreaterThanEqualToPositiveConstant,
            null,
        )
    }

    @Test
    fun testExtractSolutionFromModulusLessThanNegativeConstant() {
        testRuleInX(
            "abs[x] < -1",
            InequalitiesRules.ExtractSolutionFromModulusLessThanNonPositiveConstant,
            "Contradiction[x : abs[x] < -1]",
        )
        testRuleInX(
            "abs[3x - 1] < -1",
            InequalitiesRules.ExtractSolutionFromModulusLessThanNonPositiveConstant,
            "Contradiction[x : abs[3x - 1] < -1]",
        )
        testRuleInX(
            "abs[x - 1] < 0",
            InequalitiesRules.ExtractSolutionFromModulusLessThanNonPositiveConstant,
            "Contradiction[x : abs[x - 1] < 0]",
        )
        testRuleInX(
            "abs[3x - 1] > -1",
            InequalitiesRules.ExtractSolutionFromModulusLessThanNonPositiveConstant,
            null,
        )
    }

    @Test
    fun testExtractSolutionFromModulusGreaterThanZero() {
        testRuleInX(
            "abs[x + 1] >= 0",
            InequalitiesRules.ExtractSolutionFromModulusGreaterThanEqualToNonPositiveConstant,
            "Identity[x : abs[x + 1] >= 0]",
        )
        testRuleInX(
            "abs[x + 2] >= -1",
            InequalitiesRules.ExtractSolutionFromModulusGreaterThanEqualToNonPositiveConstant,
            "Identity[x : abs[x + 2] >= -1]",
        )
        testRuleInX(
            "3*abs[x + 2] >= -2",
            InequalitiesRules.ExtractSolutionFromModulusGreaterThanEqualToNonPositiveConstant,
            "Identity[x : 3*abs[x + 2] >= -2]",
        )
    }

    @Test
    fun test() {
        testRuleInX(
            "abs[x + 2] > -2",
            InequalitiesRules.ExtractSolutionFromModulusGreaterThanNegativeConstant,
            "Identity[x : abs[x + 2] > -2]",
        )
        testRuleInX(
            "abs[x + 2] > 0",
            InequalitiesRules.ExtractSolutionFromModulusGreaterThanNegativeConstant,
            null,
        )
    }

    @Test
    fun testReduceModulusLessThanEqualToZeroInequalityToEquation() {
        testRuleInX(
            "abs[x - 1] <= 0",
            InequalitiesRules.ReduceModulusLessThanEqualToZeroInequalityToEquation,
            "x - 1 = 0",
        )
        testRuleInX(
            "abs[x - 1] >= 0",
            InequalitiesRules.ReduceModulusLessThanEqualToZeroInequalityToEquation,
            null,
        )
        testRuleInX(
            "2*abs[x + 1] <= 0",
            InequalitiesRules.ReduceModulusLessThanEqualToZeroInequalityToEquation,
            "2(x + 1) = 0",
        )
        testRuleInX(
            "-2*abs[x + 1] <= 0",
            InequalitiesRules.ReduceModulusLessThanEqualToZeroInequalityToEquation,
            null,
        )
    }

    @Test
    fun testConvertModulusGreaterThanZero() {
        testRuleInX(
            "abs[x - 1] > 0",
            InequalitiesRules.ConvertModulusGreaterThanZero,
            "x - 1 != 0",
        )
        testRuleInX(
            "2*abs[x - 1] > 0",
            InequalitiesRules.ConvertModulusGreaterThanZero,
            "2(x - 1) != 0",
        )
    }

    @Test
    fun testConvertModulusLessThanPositiveConstant() {
        testRuleInX(
            "abs[3x + 1] < 2",
            InequalitiesRules.ConvertModulusLessThanPositiveConstant,
            "-2 < 3x + 1 < 2",
        )
        testRuleInX(
            "2*abs[3x + 1] < 3",
            InequalitiesRules.ConvertModulusLessThanPositiveConstant,
            "-3 < 2 (3x + 1) < 3",
        )
        testRuleInX(
            "-2*abs[3x + 1] < 3",
            InequalitiesRules.ConvertModulusLessThanPositiveConstant,
            null,
        )
    }

    @Test
    fun testConvertModulusLessThanEqualPositiveConstant() {
        testRuleInX(
            "abs[3x + 1] <= 2",
            InequalitiesRules.ConvertModulusLessThanEqualToPositiveConstant,
            "-2 <= 3x + 1 <= 2",
        )
        testRuleInX(
            "2*abs[3x + 1] <= 5",
            InequalitiesRules.ConvertModulusLessThanEqualToPositiveConstant,
            "-5 <= 2(3x + 1) <= 5",
        )
    }
}
