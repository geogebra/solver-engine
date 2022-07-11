package methods.integerarithmetic

import engine.steps.metadata.ExplanationBase

enum class IntegerArithmeticExplanation : ExplanationBase {
    // Rules
    EvaluateIntegerSubtraction,
    EvaluateIntegerAddition,
    EvaluateIntegerProduct,
    EvaluateIntegerDivision,
    EvaluateIntegerPower,
    RewriteIntegerPowerAsProduct,

    // Plans
    SimplifyArithmeticExpression,
    SimplifyIntegerSum,
    SimplifyIntegerProduct;

    override val category = "IntegerArithmetic"
}

typealias Explanation = IntegerArithmeticExplanation
