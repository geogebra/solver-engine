package methods.solvable

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

/**
 * Abstract explanation keys for operations on Solvable.  These are mapped to [EquationsExplanation] and
 * [InequalitiesExplanation] values
 */
enum class SolvableKey {
    /**
     * Cancel common terms on both sides of the equation.
     *
     * E.g. 2x + 3 + 4 sqrt[5] = 2 + 4 sqrt[5] -> 2x + 3 = 2
     */
    CancelCommonTermsOnBothSides,

    /**
     * Add the opposite of the constants appearing on the RHS
     * of the equation to both sides.
     *
     * %1: variables that are not constant
     *
     * E.g. 1 = 2x + 2 -> 1 - 2 = 2x + 2 - 2
     * 1 = 2x - 3 -> 1 + 3 = 2x - 3 + 3
     */
    MoveConstantsToTheLeft,

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the equation to both sides.
     *
     * %1: variables that are not constant
     *
     * E.g. 2x + 1 = 2 -> 2x + 1 - 1 = 2 - 1
     * 2x - 1 = 3 -> 2x - 1 + 1 = 3 + 1
     */
    MoveConstantsToTheRight,

    /**
     * Add the opposite of the variables appearing on the RHS
     * of the equation to both sides.
     *
     * %1: variables that can be moved
     *
     * E.g. 3x + 2 = 2x + 1 -> 3x + 2 - 2x = 2x + 1 - 2x
     */
    MoveVariablesToTheLeft,

    /**
     * Add the opposite of the variables appearing on the LHS
     * of the equation to both sides.
     *
     * E.g. 2x + 2 = 3x + 1 -> 2x + 2 - 2x = 3x + 1 - 2x
     */
    MoveVariablesToTheRight,

    /**
     * Multiply both sides of the equation by the least common denominator of
     * all the fractions occurring in it.
     *
     * E.g. [2 + x / 6] + 5 = [4 - 2x / 4] -> ([2 + x / 6] + 5) * 12 = [4 - 2x / 4] * 12
     */
    MultiplyBothSidesByLCD,
}

/**
 * An explanation key that is an incarnation of a given [SolvableKey] value.
 */
interface SolvableExplanation : CategorisedMetadataKey {
    val solvableKey: SolvableKey
    val explicitVariables: Boolean
    val simplify: Boolean
}

/**
 * Enum classes that incarnate [SolvableKey] should provide a companion object that inherits from this class.
 */
open class SolvableKeyGetter(solvableExplanations: List<SolvableExplanation>) {

    private val explanations = mutableMapOf<Pair<SolvableKey, Boolean>, SolvableExplanation>()
    private val explicitVariablesExplanations = mutableMapOf<Pair<SolvableKey, Boolean>, SolvableExplanation>()

    init {
        for (key in solvableExplanations) {
            val map = if (key.explicitVariables) explicitVariablesExplanations else explanations
            map[Pair(key.solvableKey, key.simplify)] = key
        }
    }

    /**
     * Returns a real explanation key for the given [solvableKey]
     */
    fun getKey(
        solvableKey: SolvableKey,
        explicitVariables: Boolean = false,
        simplify: Boolean = false,
    ): SolvableExplanation {
        val mapKey = Pair(solvableKey, simplify)
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
) : SolvableExplanation {

    /**
     * Cancel common terms on both sides of the equation.
     *
     * E.g. 2x + 3 + 4 sqrt[5] = 2 + 4 sqrt[5] -> 2x + 3 = 2
     */
    CancelCommonTermsOnBothSides(SolvableKey.CancelCommonTermsOnBothSides),

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
     * %1: variables that are not constant (x in the example below)
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
     * %1: variables that are not constant
     *
     * E.g. 2x + k = 2k -> 2x + k - k = 2k - k
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
     * %1: variables that are not constant (x in the example below)
     *
     * E.g. 2x + k = 2k
     *      -> 2x + k - k = 2k - k
     *      -> 2x = k
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
     *
     * E.g. 3x + 2 = 2x + 1 -> 3x + 2 - 2x = 2x + 1 - 2x
     */
    MoveVariablesToTheLeft(SolvableKey.MoveVariablesToTheLeft),

    /**
     * Add the opposite of some variables appearing on the RHS
     * of the equation to both sides.
     *
     * %1: variables that can be moved (x in the example below)
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
     * %1: variables that can be moved (x in the example below)
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
     * %1: variables that can be moved (x in the example below)
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
     * %1: variables that can be moved (x in the example below)
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

    ;

    override val category = "Equations"

    companion object : SolvableKeyGetter(values().asList())
}

@TranslationKeys
enum class InequalitiesExplanation(
    override val solvableKey: SolvableKey,
    override val explicitVariables: Boolean = false,
    override val simplify: Boolean = false,
) : SolvableExplanation {

    /**
     * Cancel common terms on both sides of the inequality.
     *
     * E.g. 2x + 3 + 4 sqrt[5] < 2 + 4 sqrt[5] -> 2x + 3 < 2
     */
    CancelCommonTermsOnBothSides(SolvableKey.CancelCommonTermsOnBothSides),

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

    ;

    override val category = "Inequalities"

    companion object : SolvableKeyGetter(values().asList())
}
