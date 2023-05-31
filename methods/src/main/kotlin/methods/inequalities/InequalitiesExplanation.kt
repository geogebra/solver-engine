package methods.inequalities

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class InequalitiesExplanation : CategorisedMetadataKey {

    /**
     * Simplify an inequality so that
     * - opposite terms on the same side are cancelled first
     * - equal terms on both sides are cancelled next
     * - each side is simplified last
     *
     * E.g. x + 3 - 3 < 5x - 3 --> x < 5x - 3
     *      x - 4 >= 2x - 4 --> x >= 2x
     *      x + 2x <= 5 + 1 --> 3x <= 6
     */
    SimplifyInequality,

    /**
     * Extract a truth from a true inequality that has no variables
     *
     * E.g. 1 < 2 -> Truth
     */
    ExtractTruthFromTrueInequality,

    /**
     * Extract the solution from a true inequality.
     *
     * E.g. -3 < 7 -> x \in R
     */
    ExtractSolutionFromTrueInequality,

    /**
     * Extract a falsehood from a false inequality that has no variables
     *
     * E.g. 3 < 5 -> Falsehood
     */
    ExtractFalsehoodFromFalseInequality,

    /**
     * Extract the solution from a false inequality.
     *
     * E.g. -3 >= 7 -> x \in \emptyset
     */
    ExtractSolutionFromFalseInequality,

    /**
     * Extract the solution of an inequality from an inequality which is in
     * a solved form, i.e. x (<, <=, >, >=) a.
     *
     * E.g. x <= 2 sqrt[2] -> x \in ( -infinity, 2 sqrt[2] ]
     */
    ExtractSolutionFromInequalityInSolvedForm,

    /**
     * Negate both sides of the inequality and flip the sign, i.e. turn an
     * inequality of the form -x < a to x > -a.
     *
     * E.g. -x <= -2 sqrt[3] -> x >= 2 sqrt[3]
     */
    NegateBothSidesAndFlipTheSign,

    /**
     * Multiply both sides of the inequality by the inverse of the coefficient
     * of the variable (which is a positive value).
     *
     * E.g. [x / 9] < 3 -> [x / 9] * 9 < 3 * 9
     * [2x / 5] > 3 -> [2x / 5] * [5 / 2] > 3 * [5 / 2]
     */
    MultiplyByInverseCoefficientOfVariable,

    /**
     * Multiply both sides of the inequality by the inverse of the coefficient
     * of the variable and flip the sign (because we're multiplying by a negative
     * value).
     *
     * E.g. [x / -9] < 3 -> [x / -9] * (-9) > 3 * (-9)
     * [-2x / 5] > 3 -> [-2x / 5] * [5 / -2] < 3 * [5 / -2]
     */
    MultiplyByInverseCoefficientOfVariableAndFlipTheSign,

    /**
     * Divide both sides of the inequality by the coefficient of the
     * variable (which is a positive value).
     *
     * E.g. 2 sqrt[2] x <= 3 -> [2 sqrt[2] x / 2 sqrt[2]] <= [3 / 2 sqrt[2]]
     */
    DivideByCoefficientOfVariable,

    /**
     * Divide both sides of the inequality by the coefficient of the
     * variable and flip the sign (because we're dividing by a negative
     * value).
     *
     * E.g. -2 sqrt[2] x > 3 -> [-2 sqrt[2] x / -2 sqrt[2]] < [3 / -2 sqrt[2]]
     */
    DivideByCoefficientOfVariableAndFlipTheSign,

    /**
     * Flip the inequality.
     *
     * E.g. 7 < 3x -> 3x > 7
     */
    FlipInequality,

    /**
     * Multiply both sides of the inequality by the inverse of the coefficient
     * of the variable and simplify.
     *
     * E.g. [2x / 5] < 3
     *      -> [2x / 5] * [5 / 2] < 3 * [5 / 2]
     *      -> x < [15 / 2]
     */
    MultiplyByInverseCoefficientOfVariableAndSimplify,

    /**
     * Divide both sides of the inequality by the coefficient of the variable
     * and simplify.
     *
     * E.g. 2 sqrt[2] x > 3
     *      -> [2 sqrt[2] x / 2 sqrt[2]] > [3 / 2 sqrt[2]]
     *      -> x > [3 sqrt[2] / 4]
     */
    DivideByCoefficientOfVariableAndSimplify,

    /**
     * Solve a linear inequality in a given variable by collecting variables
     * on the LHS, constants on the RHS then dividing by the coefficient
     * of the variable.
     *
     * E.g. 4x + 3 < 2x + 7
     *      -> 2x + 3 < 7
     *      -> 2x < 4
     *      -> x < 2
     */
    SolveLinearInequality,

    ;

    override val category = "Inequalities"
}

typealias Explanation = InequalitiesExplanation
