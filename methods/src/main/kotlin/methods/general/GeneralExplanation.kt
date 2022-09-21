package methods.general

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class GeneralExplanation : CategorisedMetadataKey {
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
    EliminateLoneOneInExponent;

    override val category = "General"
}

typealias Explanation = GeneralExplanation
