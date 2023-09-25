package methods.solvable

import engine.methods.RunnerMethod
import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

/**
 * Abstract explanation keys for operations on Solvable.  These are mapped to [EquationsExplanation] and
 * [InequalitiesExplanation] values
 */
enum class SolvableKey(val rule: RunnerMethod) {
    /**
     * Cancel common terms on both sides of the equation.
     *
     * E.g. 2x + 3 + 4 sqrt[5] = 2 + 4 sqrt[5] -> 2x + 3 = 2
     */
    CancelCommonTermsOnBothSides(SolvableRules.CancelCommonTermsOnBothSides),

    /**
     * Add the opposite of the constants appearing on the RHS
     * of the equation to both sides.
     *
     * %1: variables that are not constant
     *
     * E.g. 1 = 2x + 2 -> 1 - 2 = 2x + 2 - 2
     * 1 = 2x - 3 -> 1 + 3 = 2x - 3 + 3
     */
    MoveConstantsToTheLeft(SolvableRules.MoveConstantsToTheLeft),

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the equation to both sides.
     *
     * %1: variables that are not constant
     *
     * E.g. 2x + 1 = 2 -> 2x + 1 - 1 = 2 - 1
     * 2x - 1 = 3 -> 2x - 1 + 1 = 3 + 1
     */
    MoveConstantsToTheRight(SolvableRules.MoveConstantsToTheRight),

    /**
     * Add the opposite of the variables appearing on the RHS
     * of the equation to both sides.
     *
     * %1: variables that can be moved
     *
     * E.g. 3x + 2 = 2x + 1 -> 3x + 2 - 2x = 2x + 1 - 2x
     */
    MoveVariablesToTheLeft(SolvableRules.MoveVariablesToTheLeft),

    /**
     * Add the opposite of the variables appearing on the LHS
     * of the equation to both sides.
     *
     * E.g. 2x + 2 = 3x + 1 -> 2x + 2 - 2x = 3x + 1 - 2x
     */
    MoveVariablesToTheRight(SolvableRules.MoveVariablesToTheRight),

    MoveEverythingToTheLeft(SolvableRules.MoveEverythingToTheLeft),

    /**
     * Multiply both sides of the equation by the least common denominator of
     * all the fractions occurring in it.
     *
     * E.g. [2 + x / 6] + 5 = [4 - 2x / 4] -> ([2 + x / 6] + 5) * 12 = [4 - 2x / 4] * 12
     */
    MultiplyBothSidesByLCD(SolvableRules.MultiplySolvableByLCD),

    /**
     * In an equation an absolute value, move terms without an absolute value to the right
     *
     * E.g. abs[x] + x - 1 = 2 --> abs[x] + x - 1 - x + 1 = 2 - x + 1
     */
    MoveTermsNotContainingModulusToTheRight(SolvableRules.MoveTermsNotContainingModulusToTheRight),

    /**
     * In an equation with an absolute value, move terms without an absolute value to the left
     *
     * E.g. x = 2x - abs[x] + 3 --> x - 2x - 3 = 2x - abs[x] + 3 - 2x - 3
     */
    MoveTermsNotContainingModulusToTheLeft(SolvableRules.MoveTermsNotContainingModulusToTheLeft),

    MultiplyByInverseCoefficientOfVariable(SolvableRules.MultiplyByInverseCoefficientOfVariable),

    DivideByCoefficientOfVariable(SolvableRules.DivideByCoefficientOfVariable),

    NegateBothSides(SolvableRules.NegateBothSides),

    FlipSolvable(SolvableRules.FlipSolvable),
}

/**
 * An explanation key that is an incarnation of a given [SolvableKey] value.
 */
interface SolvableExplanation : CategorisedMetadataKey {
    val solvableKey: SolvableKey
    val explicitVariables: Boolean
    val simplify: Boolean
    val flipSign: Boolean
}

/**
 * Enum classes that incarnate [SolvableKey] should provide a companion object that inherits from this class.
 */
open class SolvableKeyGetter(solvableExplanations: List<SolvableExplanation>) {

    private data class ExplanationKey(val key: SolvableKey, val simplify: Boolean, val flipSign: Boolean)

    private val explanations = mutableMapOf<ExplanationKey, SolvableExplanation>()
    private val explicitVariablesExplanations = mutableMapOf<ExplanationKey, SolvableExplanation>()

    init {
        for (key in solvableExplanations) {
            val map = if (key.explicitVariables) explicitVariablesExplanations else explanations
            map[ExplanationKey(key.solvableKey, key.simplify, key.flipSign)] = key
        }
    }

    /**
     * Returns a real explanation key for the given [solvableKey]
     */
    fun getKey(
        solvableKey: SolvableKey,
        explicitVariables: Boolean = false,
        flipSign: Boolean = false,
        simplify: Boolean = false,
    ): SolvableExplanation {
        val mapKey = ExplanationKey(solvableKey, simplify, flipSign)
        if (explicitVariables) {
            val key = explicitVariablesExplanations[mapKey]
            if (key != null) return key
        }
        return explanations[mapKey]!!
    }
}

@TranslationKeys
enum class EquationsExplanation(
    override val solvableKey: SolvableKey,
    override val explicitVariables: Boolean = false,
    override val simplify: Boolean = false,
    override val flipSign: Boolean = false,
) : SolvableExplanation {

    /**
     * Cancel common terms on both sides of the equation.
     *
     * E.g. 2x + 3 + 4 sqrt[5] = 2 + 4 sqrt[5] -> 2x + 3 = 2
     */
    CancelCommonTermsOnBothSides(SolvableKey.CancelCommonTermsOnBothSides),

    MoveTermsNotContainingModulusToTheLeft(SolvableKey.MoveTermsNotContainingModulusToTheLeft),

    MoveTermsNotContainingModulusToTheRight(SolvableKey.MoveTermsNotContainingModulusToTheRight),

    /**
     * Add the opposite of the constants appearing on the RHS
     * of the equation to both sides.
     *
     * E.g. 1 = 2x + 2 -> 1 - 2 = 2x + 2 - 2
     * 1 = 2x - 3 -> 1 + 3 = 2x - 3 + 3
     */
    MoveConstantsToTheLeft(SolvableKey.MoveConstantsToTheLeft),

    /**
     * Add the opposite of the constants appearing on the RHS
     * of the equation to both sides.
     *
     * %1: variables that are not constant (x in the example)
     * %2: constants (2k in the first example)
     *
     * E.g. k = 2x + 2k -> k - 2k = 2x + 2k - 2k
     * k = 2x - 3k -> k + 3k = 2x - 3k + 3k
     */
    MoveConstantsInVariablesToTheLeft(SolvableKey.MoveConstantsToTheLeft, explicitVariables = true),

    /**
     * Add the opposite of the constants appearing on the RHS
     * of the equation to both sides and simplify.
     *
     * E.g. 1 = 2x + 2
     *      -> 1 - 2 = 2x + 2 - 2
     *      -> -1 = 2x
     */
    MoveConstantsToTheLeftAndSimplify(SolvableKey.MoveConstantsToTheLeft, simplify = true),

    /**
     * Add the opposite of the constants appearing on the RHS
     * of the equation to both sides and simplify.
     *
     * %1: variables that are not constant (x in the example)
     * %2: constants (2k in the example)
     *
     * E.g. k = 2x + 2k
     *      -> k - 2k = 2x + k - k
     *      -> -k = 2x
     */
    MoveConstantsInVariablesToTheLeftAndSimplify(
        SolvableKey.MoveConstantsToTheLeft,
        explicitVariables = true,
        simplify = true,
    ),

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the equation to both sides.
     *
     * E.g. 2x + 1 = 2 -> 2x + 1 - 1 = 2 - 1
     * 2x - 1 = 3 -> 2x - 1 + 1 = 3 + 1
     */
    MoveConstantsToTheRight(SolvableKey.MoveConstantsToTheRight),

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the equation to both sides.
     *
     * %1: variables that are not constant (x in the example)
     * %2: constants (2k in the first example)
     *
     * E.g. 2x + 2k = k -> 2x + 2k - 2k = k - 2k
     * 2x - k = 3k -> 2x - k + k = 3k + k
     */
    MoveConstantsInVariablesToTheRight(SolvableKey.MoveConstantsToTheRight, explicitVariables = true),

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the equation to both sides and simplify.
     *
     * E.g. 2x + 1 = 2
     *      -> 2x + 1 - 1 = 2 - 1
     *      -> 2x = 1
     */
    MoveConstantsToTheRightAndSimplify(SolvableKey.MoveConstantsToTheRight, simplify = true),

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the equation to both sides and simplify.
     *
     * %1: variables that are not constant (x in the example)
     * %2: constants (2k in the example)
     *
     * E.g. 2x + 2k = k
     *      -> 2x + 2k - 2k = k - 2k
     *      -> 2x = -k
     */
    MoveConstantsInVariablesToTheRightAndSimplify(
        SolvableKey.MoveConstantsToTheRight,
        explicitVariables = true,
        simplify = true,
    ),

    /**
     * Add the opposite of the variables appearing on the RHS
     * of the equation to both sides.
     *
     * E.g. 3x + 2 = 2x + 1 -> 3x + 2 - 2x = 2x + 1 - 2x
     */
    MoveVariablesToTheLeft(SolvableKey.MoveVariablesToTheLeft),

    /**
     * Add the opposite of some variables appearing on the RHS
     * of the equation to both sides.
     *
     * %1: variables that are moved (x in the example)
     * %2: value which is actually moved (2x in the example)
     *
     * E.g. 3x + 2k = 2x + k -> 3x + k - 2x = 2x + k - 2x
     */
    MoveSomeVariablesToTheLeft(SolvableKey.MoveVariablesToTheLeft, explicitVariables = true),

    /**
     * Add the opposite of the variables appearing on the RHS
     * of the equation to both sides and simplify
     *
     * E.g. 3x + 2 = 2x + 1
     *      -> 3x + 2 - 2x = 2x + 1 - 2x
     *      -> x + 2 = 1
     */
    MoveVariablesToTheLeftAndSimplify(SolvableKey.MoveVariablesToTheLeft, simplify = true),

    /**
     * Add the opposite of some variables appearing on the RHS
     * of the equation to both sides and simplify
     *
     * %1: variables that are moved (x in the example)
     * %2: value which is actually moved (2x in the example)
     *
     * E.g. 3x + 2k = 2x + k
     *      -> 3x + 2k - 2x = 2x + k - 2x
     *      -> x + 2k = k
     */
    MoveSomeVariablesToTheLeftAndSimplify(
        SolvableKey.MoveVariablesToTheLeft,
        explicitVariables = true,
        simplify = true,
    ),

    /**
     * Add the opposite of the variables appearing on the LHS
     * of the equation to both sides.
     *
     * E.g. 2x + 2 = 3x + 1 -> 2x + 2 - 2x = 3x + 1 - 2x
     */
    MoveVariablesToTheRight(SolvableKey.MoveVariablesToTheRight),

    /**
     * Add the opposite of some variables appearing on the LHS
     * of the equation to both sides.
     *
     * %1: variables that are moved (x in the example)
     * %2: value which is actually moved (2x in the example)
     *
     * E.g. 2x + 2y = 3x + y -> 2x + 2y - 2x = 3x + y - 2x
     */

    MoveSomeVariablesToTheRight(SolvableKey.MoveVariablesToTheRight, explicitVariables = true),

    /**
     * Add the opposite of the variables appearing on the LHS
     * of the equation to both sides and simplify
     *
     * E.g. 2x + 2 = 3x + 1
     *      -> 2x + 2 - 2x = 3x + 1 - 2x
     *      -> 2 = x + 1
     */
    MoveVariablesToTheRightAndSimplify(SolvableKey.MoveVariablesToTheRight, simplify = true),

    /**
     * Add the opposite of some variables appearing on the LHS
     * of the equation to both sides and simplify
     *
     * %1: variables that are moved (x in the example)
     * %2: value which is actually moved (2x in the example)
     *
     * E.g. 2x + 2k = 3x + k
     *      -> 2x + 2k - 2x = 3x + k - 2x
     *      -> 2k = x + k
     */
    MoveSomeVariablesToTheRightAndSimplify(
        SolvableKey.MoveVariablesToTheRight,
        explicitVariables = true,
        simplify = true,
    ),

    /**
     * Add the opposite of everything on the LHS to both sides
     * of the equation.
     *
     * E.g. 4x - 3 = 2x + 1 -> 4x - 3 - (2x + 1) = 2x + 1 - (2x + 1)
     */
    MoveEverythingToTheLeft(SolvableKey.MoveEverythingToTheLeft),

    /**
     * Add the opposite of everything on the LHS to both sides
     * of the equation and simplify.
     *
     * E.g. 4x - 3 = 2x + 1
     *      -> 4x - 3 - (2x + 1) = 2x + 1 - (2x + 1)
     *      -> 2x - 4 = 0
     */
    MoveEverythingToTheLeftAndSimplify(SolvableKey.MoveEverythingToTheLeft, simplify = true),

    /**
     * Multiply both sides of the equation by the least common denominator of
     * all the fractions occurring in it.
     *
     * E.g. [2 + x / 6] + 5 = [4 - 2x / 4] -> ([2 + x / 6] + 5) * 12 = [4 - 2x / 4] * 12
     */
    MultiplyEquationByLCD(SolvableKey.MultiplyBothSidesByLCD),

    /**
     * Multiply both sides of the equation by the least common denominator
     * of all the fractions occurring in it and then simplify
     *
     * E.g. [2 + x / 6] + 5 = [4 - 2x / 4]
     *      -> ([2 + x / 6] + 5) * 12 = [4 - 2x / 4] * 12
     *      -> 2x + 64 = 12 - 6x
     */
    MultiplyByLCDAndSimplify(SolvableKey.MultiplyBothSidesByLCD, simplify = true),

    /**
     * Multiply both sides of the equation by the inverse of the coefficient
     * of the variable.
     *
     * E.g. [x / 9] = 3 -> [x / 9] * 9 = 3 * 9
     * [2x / 5] = 3 -> [2x / 5] * [5 / 2] = 3 * [5 / 2]
     */
    MultiplyByInverseCoefficientOfVariable(SolvableKey.MultiplyByInverseCoefficientOfVariable),

    /**
     * Multiply both sides of the equation by the inverse of the coefficient
     * of the variable and simplify.
     *
     * E.g. [2x / 5] = 3
     *      -> [2x / 5] * [5 / 2] = 3 * [5 / 2]
     *      -> x = [15 / 2]
     */
    MultiplyByInverseCoefficientOfVariableAndSimplify(
        SolvableKey.MultiplyByInverseCoefficientOfVariable,
        simplify = true,
    ),

    /**
     * Divide both sides of the equation by the coefficient of the
     * variable.
     *
     * E.g. 2 sqrt[2] x = 3 -> [2 sqrt[2] x / 2 sqrt[2]] = [3 / 2 sqrt[2]]
     */
    DivideByCoefficientOfVariable(SolvableKey.DivideByCoefficientOfVariable),

    /**
     * Divide both sides of the equation by the coefficient of the solution variable in a
     * multivariate equation.
     *
     * %1: the variable we are solving for
     * %2: the coefficient
     *
     * E.g. 2 (z + 1) x = 3 -> [2 (z + 1) x / 2 (z + 1)] = [3 / 2 (z + 1)] given that z + 1 != 0
     */
    DivideByCoefficientOfVariableMultivariate(SolvableKey.DivideByCoefficientOfVariable, explicitVariables = true),

    /**
     * Divide both sides of the equation by the coefficient of the variable
     * and simplify.
     *
     * E.g. 2 sqrt[2] x = 3
     *      -> [2 sqrt[2] x / 2 sqrt[2]] = [3 / 2 sqrt[2]]
     *      -> x = [3 sqrt[2] / 4]
     */
    DivideByCoefficientOfVariableAndSimplify(SolvableKey.DivideByCoefficientOfVariable, simplify = true),

    /**
     * Divide both sides of the equation by the coefficient of the solution variable in a
     * multivariate equation and simplify.
     *
     * %1: the variable we are solving for
     * %2: the coefficient
     *
     * E.g. 2 (z + 1) x = 3
     *      -> [2 (z + 1) x / 2 (z + 1)] = [3 / 2 (z + 1)] given that z + 1 != 0
     *      -> x = [3 / 2 (z + 1)] given that z != -1
     */
    DivideByCoefficientOfVariableAndSimplifyMultivariate(
        SolvableKey.DivideByCoefficientOfVariable,
        explicitVariables = true,
        simplify = true,
    ),

    /**
     * Negate both sides of the equation, i.e. turn an equation of
     * the form -x = a to x = -a.
     *
     * E.g. -x = -2 sqrt[3] -> x = 2 sqrt[3]
     */
    NegateBothSides(SolvableKey.NegateBothSides),

    /**
     * Flip the equation.
     *
     * E.g. 7 = 3x -> 3x = 7
     */
    FlipEquation(SolvableKey.FlipSolvable),
    ;

    override val category = "Equations"

    companion object : SolvableKeyGetter(values().asList())
}

@TranslationKeys
enum class InequalitiesExplanation(
    override val solvableKey: SolvableKey,
    override val explicitVariables: Boolean = false,
    override val simplify: Boolean = false,
    override val flipSign: Boolean = false,
) : SolvableExplanation {

    /**
     * Cancel common terms on both sides of the inequality.
     *
     * E.g. 2x + 3 + 4 sqrt[5] < 2 + 4 sqrt[5] -> 2x + 3 < 2
     */
    CancelCommonTermsOnBothSides(SolvableKey.CancelCommonTermsOnBothSides),

    MoveTermsNotContainingModulusToTheLeft(SolvableKey.MoveTermsNotContainingModulusToTheLeft),

    MoveTermsNotContainingModulusToTheRight(SolvableKey.MoveTermsNotContainingModulusToTheRight),

    /**
     * Add the opposite of the constants appearing on the RHS
     * of the inequality to both sides.
     *
     * E.g. 1 < 2x + 2 -> 1 - 2 < 2x + 2 - 2
     * 1 < 2x - 3 -> 1 + 3 < 2x - 3 + 3
     */
    MoveConstantsToTheLeft(SolvableKey.MoveConstantsToTheLeft),

    /**
     * Add the opposite of the constants appearing on the RHS
     * of the inequality to both sides.
     *
     * %1: variables that are not constant (x in the example below)
     *
     * E.g. k < 2x + k -> k - 2k < 2x + 2k - 2k
     * k < 2x - 3k-> k + 3k < 2x - 3k + 3k
     */
    MoveConstantsInVariablesToTheLeft(SolvableKey.MoveConstantsToTheLeft, explicitVariables = true),

    /**
     * Add the opposite of the constants appearing on the RHS
     * of the equation to both sides and simplify.
     *
     * E.g. 1 > 2x + 2
     *      -> 1 - 2 > 2x + 2 - 2
     *      -> -1 > 2x
     */
    MoveConstantsToTheLeftAndSimplify(SolvableKey.MoveConstantsToTheLeft, simplify = true),

    /**
     * Add the opposite of the constants appearing on the RHS
     * of the equation to both sides and simplify.
     *
     * %1: variables that are not constant (x in the example)
     *
     * E.g. k > 2x + 2k
     *      -> k - 2k > 2x + k - k
     *      -> -k > 2x
     */
    MoveConstantsInVariablesToTheLeftAndSimplify(
        SolvableKey.MoveConstantsToTheLeft,
        explicitVariables = true,
        simplify = true,
    ),

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the inequality to both sides.
     *
     * E.g. 2x + 1 >= 2 -> 2x + 1 - 1 >= 2 - 1
     * 2x - 1 >= 3 -> 2x - 1 + 1 >= 3 + 1
     */
    MoveConstantsToTheRight(SolvableKey.MoveConstantsToTheRight),

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the inequality to both sides.
     *
     * %1: variables that are not constant (x in the example below)
     *
     * E.g. 2x + k >= 2k -> 2x + k - k >= 2k - k
     * 2x - k >= 3k -> 2x - k + k >= 3k + k
     */
    MoveConstantsInVariablesToTheRight(SolvableKey.MoveConstantsToTheRight, explicitVariables = true),

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the equation to both sides and simplify.
     *
     * E.g. 2x + 1 <= 2
     *      -> 2x + 1 - 1 <= 2 - 1
     *      -> 2x <= 1
     */
    MoveConstantsToTheRightAndSimplify(SolvableKey.MoveConstantsToTheRight, simplify = true),

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the equation to both sides and simplify.
     *
     * %1: variables that are not constant (x in the example below)
     *
     * E.g. 2x + k <= 2k
     *      -> 2x + k - k <= 2k - k
     *      -> 2x <= k
     */
    MoveConstantsInVariablesToTheRightAndSimplify(
        SolvableKey.MoveConstantsToTheRight,
        explicitVariables = true,
        simplify = true,
    ),

    /**
     * Add the opposite of the variables appearing on the RHS
     * of the inequality to both sides.
     *
     * E.g. 3x + 2 > 2x + 1 -> 3x + 2 - 2x > 2x + 1 - 2x
     */
    MoveVariablesToTheLeft(SolvableKey.MoveVariablesToTheLeft),

    /**
     * Add the opposite of some variables appearing on the RHS
     * of the inequality to both sides.
     *
     * %1: variables that can be moved (x in the example below)
     *
     * E.g. 3x + 2y > 2x + y -> 3x + 2y - 2x > 2x + y - 2x
     */
    MoveSomeVariablesToTheLeft(SolvableKey.MoveVariablesToTheLeft, explicitVariables = true),

    /**
     * Add the opposite of the variables appearing on the RHS
     * of the equation to both sides and simplify
     *
     * E.g. 3x + 2 > 2x + 1
     *      -> 3x + 2 - 2x > 2x + 1 - 2x
     *      -> x + 2 > 1
     */
    MoveVariablesToTheLeftAndSimplify(SolvableKey.MoveVariablesToTheLeft, simplify = true),

    /**
     * Add the opposite of some variables appearing on the RHS
     * of the equation to both sides and simplify
     *
     * %1: variables that can be moved (x in the example below)
     *
     * E.g. 3x + 2k > 2x + k
     *      -> 3x + 2k - 2x > 2x + k - 2x
     *      -> x + 2k > k
     */
    MoveSomeVariablesToTheLeftAndSimplify(
        SolvableKey.MoveVariablesToTheLeft,
        explicitVariables = true,
        simplify = true,
    ),

    /**
     * Add the opposite of the variables appearing on the LHS
     * of the inequality to both sides.
     *
     * E.g. 2x + 2 > 3x + 1 -> 2x + 2 - 2x > 3x + 1 - 2x
     */
    MoveVariablesToTheRight(SolvableKey.MoveVariablesToTheRight),

    /**
     * Add the opposite of some variables appearing on the LHS
     * of the inequality to both sides.
     *
     * %1: variables that can be moved (x in the example below)
     *
     * E.g. 2x + 2y > 3x + y -> 2x + 2y - 2x > 3x + y - 2x
     */
    MoveSomeVariablesToTheRight(SolvableKey.MoveVariablesToTheRight, explicitVariables = true),

    /**
     * Add the opposite of the variables appearing on the LHS
     * of the equation to both sides and simplify
     *
     * E.g. 2x + 2 >= 3x + 1
     *      -> 2x + 2 - 2x >= 3x + 1 - 2x
     *      -> 2 >= x + 1
     */
    MoveVariablesToTheRightAndSimplify(SolvableKey.MoveVariablesToTheRight, simplify = true),

    /**
     * Add the opposite of some variables appearing on the LHS
     * of the equation to both sides and simplify
     *
     * %1: variables that can be moved (x in the example below)
     *
     * E.g. 2x + 2k >= 3x + k
     *      -> 2x + 2k - 2x >= 3x + k - 2x
     *      -> 2k >= x + k
     */
    MoveSomeVariablesToTheRightAndSimplify(
        SolvableKey.MoveVariablesToTheRight,
        explicitVariables = true,
        simplify = true,
    ),

    MoveEverythingToTheLeft(SolvableKey.MoveEverythingToTheLeft),

    MoveEverythingToTheLeftAndSimplify(SolvableKey.MoveEverythingToTheLeft, simplify = true),

    /**
     * Multiply both sides of the inequality by the least common denominator of
     * all the fractions occurring in it (which is always a positive number).
     *
     * E.g. [2 + x / 6] + 5 <= [4 - 2x / 4] -> ([2 + x / 6] + 5) * 12 <= [4 - 2x / 4] * 12
     */
    MultiplyInequalityByLCD(SolvableKey.MultiplyBothSidesByLCD),

    /**
     * Multiply both sides of the inequality by the least common denominator
     * of all the fractions occurring in it and then simplify
     *
     * E.g. [2 + x / 6] + 5 < [4 - 2x / 4]
     *      -> ([2 + x / 6] + 5) * 12 < [4 - 2x / 4] * 12
     *      -> 2x + 64 < 12 - 6x
     */
    MultiplyByLCDAndSimplify(SolvableKey.MultiplyBothSidesByLCD, simplify = true),

    /**
     * Multiply both sides of the inequality by the inverse of the coefficient
     * of the variable (which is a positive value).
     *
     * E.g. [x / 9] < 3 -> [x / 9] * 9 < 3 * 9
     * [2x / 5] > 3 -> [2x / 5] * [5 / 2] > 3 * [5 / 2]
     */
    MultiplyByInverseCoefficientOfVariable(SolvableKey.MultiplyByInverseCoefficientOfVariable),

    /**
     * Multiply both sides of the inequality by the inverse of the coefficient
     * of the variable and flip the sign (because we're multiplying by a negative
     * value).
     *
     * E.g. [x / -9] < 3 -> [x / -9] * (-9) > 3 * (-9)
     * [-2x / 5] > 3 -> [-2x / 5] * [5 / -2] < 3 * [5 / -2]
     */
    MultiplyByInverseCoefficientOfVariableAndFlipTheSign(
        SolvableKey.MultiplyByInverseCoefficientOfVariable,
        flipSign = true,
    ),

    /**
     * Multiply both sides of the inequality by the inverse of the coefficient
     * of the variable and simplify.
     *
     * E.g. [2x / 5] < 3
     *      -> [2x / 5] * [5 / 2] < 3 * [5 / 2]
     *      -> x < [15 / 2]
     */
    MultiplyByInverseCoefficientOfVariableAndSimplify(
        SolvableKey.MultiplyByInverseCoefficientOfVariable,
        simplify = true,
    ),

    /**
     * Divide both sides of the inequality by the coefficient of the
     * variable (which is a positive value).
     *
     * E.g. 2 sqrt[2] x <= 3 -> [2 sqrt[2] x / 2 sqrt[2]] <= [3 / 2 sqrt[2]]
     */
    DivideByCoefficientOfVariable(SolvableKey.DivideByCoefficientOfVariable),

    /**
     * Divide both sides of the inequality by the coefficient of the
     * variable and flip the sign (because we're dividing by a negative
     * value).
     *
     * E.g. -2 sqrt[2] x > 3 -> [-2 sqrt[2] x / -2 sqrt[2]] < [3 / -2 sqrt[2]]
     */
    DivideByCoefficientOfVariableAndFlipTheSign(SolvableKey.DivideByCoefficientOfVariable, flipSign = true),

    /**
     * Divide both sides of the inequality by the coefficient of the variable
     * and simplify.
     *
     * E.g. 2 sqrt[2] x > 3
     *      -> [2 sqrt[2] x / 2 sqrt[2]] > [3 / 2 sqrt[2]]
     *      -> x > [3 sqrt[2] / 4]
     */
    DivideByCoefficientOfVariableAndSimplify(SolvableKey.DivideByCoefficientOfVariable, simplify = true),

    /**
     * Negate both sides of the inequality and flip the sign, i.e. turn an
     * inequality of the form -x < a to x > -a.
     *
     * E.g. -x <= -2 sqrt[3] -> x >= 2 sqrt[3]
     */
    NegateBothSidesAndFlipTheSign(SolvableKey.NegateBothSides),

    /**
     * Flip the inequality.
     *
     * E.g. 7 < 3x -> 3x > 7
     */
    FlipInequality(SolvableKey.FlipSolvable),
    ;

    override val category = "Inequalities"

    companion object : SolvableKeyGetter(values().asList())
}
