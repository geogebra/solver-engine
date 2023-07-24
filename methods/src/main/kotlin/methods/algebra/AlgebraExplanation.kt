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
     * The given expression is defined for all real numbers.
     */
    ExpressionIsDefinedEverywhere,

    /**
     * States that a denominator cannot be zero because it would make the fraction undefined.
     *
     * %0 - the denominator
     * %1 - the fraction which would become undefined
     */
    DenominatorMustNotBeZero,

    /**
     * States that a divisor cannot be zero because it would make the division undefined.
     *
     * %0 - the divisor
     * %1 - the division which would become undefined
     */
    DivisorMustNotBeZero,

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
