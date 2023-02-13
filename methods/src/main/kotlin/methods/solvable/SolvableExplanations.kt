package methods.solvable

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class EquationsExplanation : CategorisedMetadataKey {

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
     * E.g. 1 = 2x + 2 -> 1 - 2 = 2x + 2 - 2
     * 1 = 2x - 3 -> 1 + 3 = 2x - 3 + 3
     */
    MoveConstantsToTheLeft,

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the equation to both sides.
     *
     * E.g. 2x + 1 = 2 -> 2x + 1 - 1 = 2 - 1
     * 2x - 1 = 3 -> 2x - 1 + 1 = 3 + 1
     */
    MoveConstantsToTheRight,

    /**
     * Add the opposite of the variables appearing on the RHS
     * of the equation to both sides.
     *
     * E.g. 3x + 2 = 2x + 1 -> 3x + 2 - 2x = 2x + 1 - 2x
     */
    MoveVariablesToTheLeft,

    /**
     * Multiply both sides of the equation by the least common denominator of
     * all the fractions occurring in it.
     *
     * E.g. [2 + x / 6] + 5 = [4 - 2x / 4] -> ([2 + x / 6] + 5) * 12 = [4 - 2x / 4] * 12
     */
    MultiplyEquationByLCD,

    ;

    override val category = "Equations"
}

@TranslationKeys
enum class InequalitiesExplanation : CategorisedMetadataKey {

    /**
     * Cancel common terms on both sides of the inequality.
     *
     * E.g. 2x + 3 + 4 sqrt[5] < 2 + 4 sqrt[5] -> 2x + 3 < 2
     */
    CancelCommonTermsOnBothSides,

    /**
     * Add the opposite of the constants appearing on the RHS
     * of the inequality to both sides.
     *
     * E.g. 1 < 2x + 2 -> 1 - 2 < 2x + 2 - 2
     * 1 < 2x - 3 -> 1 + 3 < 2x - 3 + 3
     */
    MoveConstantsToTheLeft,

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the inequality to both sides.
     *
     * E.g. 2x + 1 >= 2 -> 2x + 1 - 1 >= 2 - 1
     * 2x - 1 >= 3 -> 2x - 1 + 1 >= 3 + 1
     */
    MoveConstantsToTheRight,

    /**
     * Add the opposite of the variables appearing on the RHS
     * of the inequality to both sides.
     *
     * E.g. 3x + 2 > 2x + 1 -> 3x + 2 - 2x > 2x + 1 - 2x
     */
    MoveVariablesToTheLeft,

    /**
     * Multiply both sides of the inequality by the least common denominator of
     * all the fractions occurring in it (which is always a positive number).
     *
     * E.g. [2 + x / 6] + 5 <= [4 - 2x / 4] -> ([2 + x / 6] + 5) * 12 <= [4 - 2x / 4] * 12
     */
    MultiplyInequalityByLCD,

    ;

    override val category = "Inequalities"
}
