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

    // @Test
    // fun `frac{x}{a}=b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[x/3] = 4"
    //     check { toExpr = "SetSolution[x : {12}]" }
    // }
    // @Test
    // fun `frac{x}{-a}=b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[x/-3] = 4"
    //     check {  toExpr = "SetSolution[x : {-12}]" }
    // }
    // @Test
    // fun `frac{x}{a}=-b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[x/3] = -4"
    //     check {  toExpr = "SetSolution[x : {-12}]" }
    // }
    // @Test
    // fun `-frac{x}{a}=b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "-[x/3] = 4"
    //     check {  toExpr = "SetSolution[x : {-12}]" }
    // }
    // @Test
    // fun `frac{-x}{a}=b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[-x/3] = 4"
    //     check {  toExpr = "SetSolution[x : {-12}]" }
    // }
    // @Test
    // fun `frac{x}{-a}=-b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[x/-3] = -4"
    //     check {  toExpr = "SetSolution[x : {12}]" }
    // }
    // @Test
    // fun `-frac{-x}{a}=b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "-[-x/3] = 4"
    //     check {  toExpr = "SetSolution[x : {12}]" }
    // }
    // @Test
    // fun `-frac{x}{a}=-b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "-[x/3] = -4"
    //     check {  toExpr = "SetSolution[x : {12}]" }
    // }
    // @Test
    // fun `-frac{-x}{-a}=-b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "-[-x/-3] = -4"
    //     check {  toExpr = "SetSolution[x : {12}]" }
    // }

    // @Test
    // fun `ax=b+c`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "3 x = 2+4"
    //     check { }
    // }
}
