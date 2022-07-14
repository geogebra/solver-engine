package methods.integerroots

import engine.steps.metadata.ExplanationBase

enum class IntegerRootsExplanation : ExplanationBase {
    SimplifyRootOfOne,
    SimplifyRootOfZero,
    FactorizeNumberUnderSquareRoot,
    SeparateIntegerPowersUnderSquareRoot,
    SeparateSquaresUnderSquareRoot,
    SimplifyMultiplicationOfSquareRoots,
    SimplifySquareRootOfSquare,
    SimplifySquareRootOfPower;

    override val category = "IntegerRoots"
}

typealias Explanation = IntegerRootsExplanation
