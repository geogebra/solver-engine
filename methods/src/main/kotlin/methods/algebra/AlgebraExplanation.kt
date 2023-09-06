package methods.algebra

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class AlgebraExplanation : CategorisedMetadataKey {

    /**
     * Simplify an algebraic expression in one variable
     */
    SimplifyAlgebraicExpression,

    /**
     * Compute the domain on which a given algebraic expression is defined (e.g. exclude zero
     * denominators) then simplify it.
     */
    ComputeDomainAndSimplifyAlgebraicExpression,

    /**
     * Compute the domain on which a given algebraic expression is defined (e.g. exclude zero
     * denominators).
     */
    ComputeDomainOfAlgebraicExpression,

    /**
     * Combine the domain of the expression (e.g. x > 0) with the simplified expression (e.g. x + 1) to get
     * an expression with a constraint (e.g. x + 1 given that x > 0).
     */
    CombineSimplifiedExpressionWithConstraint,

    /**
     * The given expression is defined for all real numbers.
     */
    ExpressionIsDefinedEverywhere,

    /**
     * States that a subexpression cannot be zero because it would make another (e.g. fraction
     * or division) undefined.
     *
     * %0 - the subexpression which must not be zero
     * %1 - the expression which would become undefined
     */
    ExpressionMustNotBeZero,

    /**
     * States that a subexpression cannot be zero because it would make several others
     * (e.g. fractions and divisions) undefined.
     *
     * %0 - the subexpression which must not be zero
     * %1 - a list of expressions which would become undefined
     */
    ExpressionMustNotBeZeroPlural,

    /**
     * Collect domain restrictions into one.
     *
     * E.g. for x/(x - 1) + x/(x - 2)
     *      for the first fraction we get x != 1
     *      for the second fraction we get x != 2
     *      and collecting the restrictions results in x != 1 and x != 2
     */
    CollectDomainRestrictions,
    ;

    override val category = "Algebra"
}

typealias Explanation = AlgebraExplanation
