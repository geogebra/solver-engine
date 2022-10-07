package methods.general

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class GeneralExplanation : CategorisedMetadataKey {
    /**
     * Multiply the exponents using power rule, but don't
     * evaluate the product of exponents i.e. [( [a^b] ) ^ c] = [ a ^ b*c]
     * for e.g. [ ([2^6]) ^ 2] --> [ 2^6*2 ]
     */
    MultiplyExponentsUsingPowerRule,

    /**
     * distribute sum of powers to a base as a product of exponents with same base,
     * i.e. [base ^ exp1 + ... + expN] --> [base ^ exp1] * ... * [base ^ expN]
     */
    DistributeSumOfPowers,
    ReplaceInvisibleBrackets,
    RemoveBracketSumInSum,
    RemoveBracketProductInProduct,
    RemoveBracketAroundSignedIntegerInSum,
    RemoveRedundantBracket,
    RemoveRedundantPlusSign,
    EliminateOneInProduct,
    EliminateZeroInSum,
    EvaluateProductContainingZero,
    EvaluateZeroDividedByAnyValue,

    /**
     * Dividing by %1 is undefined
     *
     * %1 is the instance of 0 that the expression is divided by
     *
     * E.g. 3 * 5 : 0 -> UNDEFINED
     */
    EvaluateProductDividedByZeroAsUndefined,
    SimplifyDoubleMinus,
    SimplifyProductWithTwoNegativeFactors,
    MoveSignOfNegativeFactorOutOfProduct,
    CancelCommonTerms,
    SimplifyZeroNumeratorFractionToZero,
    SimplifyZeroDenominatorFractionToUndefined,
    SimplifyUnitFractionToOne,
    SimplifyFractionWithOneDenominator,
    CancelDenominator,
    AddClarifyingBrackets,
    FactorMinusFromSum,
    SimplifyProductOfConjugates,
    DistributePowerOfProduct,
    ExpandBinomialSquared,
    RewriteDivisionAsFraction,
    NormalizeExpression,
    DistributeMultiplicationOverSum,
    ReplaceAllInvisibleBrackets,
    EliminateLoneOneInExponent,

    /**
     * Rewrite a power as a product
     *
     * E.g. [5 ^ 3] -> 5 * 5 * 5
     */
    RewritePowerAsProduct,

    /**
     * Raising to the power of 1 has no effect
     *
     * E.g. [(x + 1) ^ 1] -> x + 1
     */
    SimplifyExpressionToThePowerOfOne,

    /**
     * [0 ^ 0] is undefined
     */
    EvaluateZeroToThePowerOfZero,

    /**
     * Any (non-zero) value to the power of 0 is 1
     *
     * E.g. [(1 + 1) ^ 0] -> 1
     */
    EvaluateExpressionToThePowerOfZero,

    /**
     * 0 to any positive power is 0
     *
     * E.g. [0 ^ 3] -> 0
     */
    EvaluateZeroToAPositivePower;

    override val category = "General"
}

typealias Explanation = GeneralExplanation
