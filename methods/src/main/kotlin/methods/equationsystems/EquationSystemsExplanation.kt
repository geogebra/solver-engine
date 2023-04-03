package methods.equationsystems

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class EquationSystemsExplanation : CategorisedMetadataKey {

    /**
     * Simplify a single equation to prepare for solving the equation system
     */
    PrepareEquation,

    /**
     * Rearrange an equation to express a variable in terms of the other
     *
     * %1: variable to make the subject of the equations (on the LHS)
     * %2: variable to put on the RHS.
     */
    ExpressInTermsOf,

    /**
     * Substitute a variable into the equation and solve in the other variable
     *
     * %1: variable and value to substitute (e.g. y = x + 1)
     * %2: variable to solve in (e.g. x)
     */
    SubstituteAndSolveIn,

    /**
     * Build the solution to a system of 2 equations with no solution because of a contradiction.
     */
    BuildSolutionContradiction,

    /**
     * Build the solution to a system of 2 equations where both solutions are identities.
     */
    BuildSolutionIdentities,

    /**
     * Build the solution to a system of 2 equations when only one variable is fixed.
     *
     * %1: the fixed variable
     */
    BuildSolutionOneVariableFixed,

    /**
     * Build the solution to a system of 2 equations when one equation is an identity
     */
    BuildSolutionIdentityAndEquation,

    /**
     * Build the solution to a system of 2 equations when there is a unique solution in each variable
     *
     * %1: the first variable
     * %2: the second variable
     */
    BuildSolutionCombineUniqueSolutions,

    /**
     * Build the solution to a system of 2 equations when both equations reduce to equations in
     * the same variable with the same solution
     *
     * %1: the variable
     * %2: value of the common equation
     */
    BuildSolutionSameSolutionInOneVariable,

    /**
     * Build the solution to a system of 2 equations when both equations reduce to equations in
     * the same variable with different solutions
     *
     * %1: the variable
     * %2: value of solution 1
     * $3: value of solution 2
     */
    BuildSolutionDifferentSolutionsInOneVariable,

    /**
     * Solve a system of 2 equations by substitution.
     */
    SolveEquationSystemBySubstitution,

    /**
     * Rewrite the addition of two equations, e.g.
     * 2x + 3y = 2 +
     * 3x - 3y = 1
     * as single equation e.g. 2x + 3y + (3x - 3y) = 2 + 1
     */
    RewriteEquationAddition,

    /**
     * Rewrite the subtraction of two equations, e.g.
     * 3x + 4y = 2 -
     * 3x +  y = 1
     * as single equation e.g. 3x + 4y - (3x + y) = 2 - 1
     */
    RewriteEquationSubtraction,

    /**
     * Evaluate the addition of two equations, e.g.
     * 2x + 3y = 2 +
     * 3x - 3y = 1
     * would be first transformed to 2x + 3y + (3x - 3y) = 2 + 1
     * then simplified to 5x = 3
     */
    AddEquations,

    /**
     * Evaluate the subtraction of two equations, e.g.
     * 3x + 4y = 2 -
     * 3x +  y = 1
     * would be first transformed to 3x + 4y - (3x + y) = 2 - 1
     * then simplified to 3y = 1
     */
    SubtractEquations,

    /**
     * Multiply both sides of an equation by a value.
     * %1: factor the equation is multiplied by
     */
    MultiplyEquation,

    /**
     * Solve the univariate equation obtained by adding or
     * subtracting the two equations in a system when solving
     * it using elimination.
     */
    SolveEliminatedEquation,

    /**
     * Solve a system of two equations by elimination.
     */
    SolveEquationSystemByElimination,

    ;

    override val category = "EquationSystems"
}

typealias Explanation = EquationSystemsExplanation
