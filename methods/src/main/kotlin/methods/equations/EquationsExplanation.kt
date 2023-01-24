package methods.equations

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class EquationsExplanation : CategorisedMetadataKey {

    /**
     * Negate both sides of the equation, i.e. turn an equation of
     * the form -x = a to x = -a.
     *
     * E.g. -x = -2 sqrt[3] -> x = 2 sqrt[3]
     */
    NegateBothSides,

    /**
     * Multiply both sides of the equation by the inverse of the coefficient
     * of the variable.
     *
     * E.g. [x / 9] = 3 -> [x / 9] * 9 = 3 * 9
     * [2x / 5] = 3 -> [2x / 5] * [5 / 2] = 3 * [5 / 2]
     */
    MultiplyByInverseCoefficientOfVariable,

    /**
     * Divide both sides of the equation by the coefficient of the
     * variable.
     *
     * E.g. 2 sqrt[2] x = 3 -> [2 sqrt[2] x / 2 sqrt[2]] = [3 / 2 sqrt[2]]
     */
    DivideByCoefficientOfVariable,

    /**
     * Flip the equation.
     *
     * E.g. 7 = 3x -> 3x = 7
     */
    FlipEquation,

    /**
     * Extract the solution of an equation from an identity.
     *
     * E.g. 3x + 1 = 3x + 1 -> x \in R
     */
    ExtractSolutionFromIdentity,

    /**
     * Extract the solution of an equation from a contradiction.
     *
     * E.g. 3 = 4 -> x \in \emptyset
     */
    ExtractSolutionFromContradiction,

    /**
     * Extract the solution of an equation from an equation which is in
     * a solved form, i.e. x = a.
     *
     * E.g. x = 2 sqrt[2] -> x \in { 2 sqrt[2] }
     */
    ExtractSolutionFromEquationInSolvedForm,

    /**
     * Multiply both sides of the equation by the least common denominator
     * of all the fractions occurring in it and then simplify
     *
     * E.g. [2 + x / 6] + 5 = [4 - 2x / 4]
     *      -> ([2 + x / 6] + 5) * 12 = [4 - 2x / 4] * 12
     *      -> 2x + 64 = 12 - 6x
     */
    MultiplyByLCDAndSimplify,

    /**
     * Add the opposite of the constants appearing on the RHS
     * of the equation to both sides and simplify.
     *
     * E.g. 1 = 2x + 2
     *      -> 1 - 2 = 2x + 2 - 2
     *      -> -1 = 2x
     */
    MoveConstantsToTheLeftAndSimplify,

    /**
     * Add the opposite of the constants appearing on the LHS
     * of the equation to both sides and simplify.
     *
     * E.g. 2x + 1 = 2
     *      -> 2x + 1 - 1 = 2 - 1
     *      -> 2x = 1
     */
    MoveConstantsToTheRightAndSimplify,

    /**
     * Add the opposite of the variables appearing on the RHS
     * of the equation to both sides and simplify
     *
     * E.g. 3x + 2 = 2x + 1
     *      -> 3x + 2 - 2x = 2x + 1 - 2x
     *      -> x + 2 = 1
     */
    MoveVariablesToTheLeftAndSimplify,

    /**
     * Multiply both sides of the equation by the inverse of the coefficient
     * of the variable and simplify.
     *
     * E.g. [2x / 5] = 3
     *      -> [2x / 5] * [5 / 2] = 3 * [5 / 2]
     *      -> x = [15 / 2]
     */
    MultiplyByInverseCoefficientOfVariableAndSimplify,

    /**
     * Divide both sides of the equation by the coefficient of the variable
     * and simplify.
     *
     * E.g. 2 sqrt[2] x = 3
     *      -> [2 sqrt[2] x / 2 sqrt[2]] = [3 / 2 sqrt[2]]
     *      -> x = [3 sqrt[2] / 4]
     */
    DivideByCoefficientOfVariableAndSimplify,

    /**
     * Solve a linear equation in a given variable by collecting variables
     * on the LHS, constants on the RHS then dividing by the coefficient
     * of the variable.
     *
     * E.g. 4x + 3 = 2x + 7
     *      -> 2x + 3 = 7
     *      -> 2x = 4
     *      -> x = 2
     */
    SolveLinearEquation;

    override val category = "Equations"
}

typealias Explanation = EquationsExplanation
