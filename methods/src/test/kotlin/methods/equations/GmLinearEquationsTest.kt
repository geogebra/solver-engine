package methods.equations
import engine.context.Preset
import engine.methods.testMethodInX
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

// An example of how the tests can be written, to be moved somewhere better.
// The tag can be on the class as here, in which case all tests in the class will be tagged, or
// on individual tests.
@Tag("GmAction")
class GmLinearEquationsTest {
    @Test
    fun `ax=c`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "3 x = 4"
        check { toExpr = "SetSolution[x : {[4 / 3]}]" }
    }

    @Test
    fun `ax=-c`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "3 x = -4"
        check { toExpr = "SetSolution[x : {-[4 / 3]}]" }
    }

    @Test
    fun `-ax=c`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "-3 x = 4"
        check { toExpr = "SetSolution[x : {-[4 / 3]}]" }
    }

    @Test
    fun `-ax=-c`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "-3 x = -4"
        check { toExpr = "SetSolution[x : {[4 / 3]}]" }
    }

    @Test
    fun `ax=a`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "3 x = 3"
        check { toExpr = "SetSolution[x : {1}]" }
    }

    @Test
    fun `frac{x}{a}=b`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "[x/3] = 4"
        check { toExpr = "SetSolution[x : {12}]" }
    }

    // TO-DO: Introduce a map of toExpr -> gmToExpr instead so we don't have to go into all the sub-steps
    // TO-DO: Adjust GM to put brackets around the *-4
    @Test
    fun `frac{x}{-a}=b`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "[x/-3] = 4"
        check {
            toExpr = "SetSolution[x : {-12}]"
            step { toExpr = "-[x / 3] = 4" }
            step { toExpr = "[x / 3] = -4" }
            step {
                step {
                    toExpr = "[x / 3] * 3 = 3 * (-4)"
                    gmToExpr = "[x / 3] * 3 = 3 * -4"
                }
                step {
                    step {
                        toExpr = "x = 3 * (-4)"
                        gmToExpr = "x = 3 * -4"
                    }
                    step { }
                    step { }
                }
            }
            step { }
        }
    }
    // @Test
    // fun `frac{x}{a}=-b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[x/3] = -4"
    //     check { toExpr = "SetSolution[x : {-12}]" }
    // }
    // @Test
    // fun `-frac{x}{a}=b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "-[x/3] = 4"
    //     check { toExpr = "SetSolution[x : {-12}]" }
    // }
    // @Test
    // fun `frac{-x}{a}=b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[-x/3] = 4"
    //     check { toExpr = "SetSolution[x : {-12}]" }
    // }

    @Test
    fun `-frac{-x}{a}=b`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "-[-x/3] = 4"
        check { toExpr = "SetSolution[x : {12}]" }
    }

    @Test
    fun `-frac{x}{a}=-b`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "-[x/3] = -4"
        check { toExpr = "SetSolution[x : {12}]" }
    }

    @Test
    fun `-frac{-x}{-a}=-b`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "-[-x/-3] = -4"
        check { toExpr = "SetSolution[x : {12}]" }
    }

    @Test
    fun `x+a=b`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "x+1 = 4"
        check { toExpr = "SetSolution[x : {3}]" }
    }

    @Test
    fun `x-a=b`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "x-1 = 4"
        check { toExpr = "SetSolution[x : {5}]" }
    }

    @Test
    fun `a-x=b`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "5-x = 7"
        check { toExpr = "SetSolution[x : {-2}]" }
    }

    @Test
    fun `-x+a=-b`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "-x+1 = -10"
        check { toExpr = "SetSolution[x : {11}]" }
    }

    @Test
    fun `-x-a=b`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "-x-1 = 1"
        check { toExpr = "SetSolution[x : {-2}]" }
    }

    @Test
    fun `-x-a=-b`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "-x-3 = -5"
        check { toExpr = "SetSolution[x : {2}]" }
    }

    @Test
    fun `x+a=a`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "x+3 = 3"
        check { toExpr = "SetSolution[x : {0}]" }
    }

    @Test
    fun `frac{x}{-a}=-b`() = testMethodInX(Preset.GMFriendly) {
        method = EquationsPlans.SolveEquation
        inputExpr = "[x/-3] = -4"
        check { toExpr = "SetSolution[x : {12}]" }
    }
    // TO-DO: Adjust Solver so that [6x/3] ==> 2x, not [2*3x/3*1]
    // @Test
    // fun `frac{a}{b}x=Na`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[3/5]x = 6"
    //     check { toExpr = "SetSolution[x : {10}]" }
    // }
    // TO-DO: Adjust Solver so that 15/15*x ==> x, not 1*x
    // @Test
    // fun `frac{a}{b}x=c`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[3/5]x = 7"
    //     check { toExpr = "SetSolution[x : { [35/3] }]" }
    // }
}
