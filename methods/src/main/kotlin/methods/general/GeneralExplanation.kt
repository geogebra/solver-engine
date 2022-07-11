package methods.general

import engine.steps.metadata.ExplanationBase

enum class GeneralExplanation : ExplanationBase {
    // Rules
    ReplaceInvisibleBrackets/*(
        "Add brackets to make the expression clearer",
        "fragment that needs brackets"
    )*/,
    RemoveBracketSumInSum/*(
        "Brackets around a sum term which is itself a sum can be removed",
        "inner sum"
    )*/,
    RemoveBracketAroundSignedIntegerInSum/*(
        "Brackets can be removed from a negative term in a sum",
        "negative term"
    )*/,
    RemoveBracketAroundUnsignedInteger/*(
        "Brackets around a positive integer can be removed",
        "positive integer"
    )*/,
    EliminateOneInProduct/*(
        "A factor of 1 can be eliminated from a product",
        "factor 1"
    )*/,
    EliminateZeroInSum,
    EvaluateProductContainingZero,
    SimplifyDoubleMinus,
    SimplifyProductWithTwoNegativeFactors,
    MoveSignOfNegativeFactorOutOfProduct,
    SimplifyEvenPowerOfNegative,
    SimplifyOddPowerOfNegative,
    CancelCommonTerms,

    // Plans
    ReplaceAllInvisibleBrackets;

    override val category = "General"
}

typealias Explanation = GeneralExplanation
