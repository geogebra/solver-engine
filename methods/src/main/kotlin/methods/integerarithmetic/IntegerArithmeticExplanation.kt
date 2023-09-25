package methods.integerarithmetic

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class IntegerArithmeticExplanation : CategorisedMetadataKey {
    EvaluateIntegerSubtraction,
    EvaluateIntegerAddition,
    EvaluateIntegerProduct,
    EvaluateIntegerDivision,
    EvaluateIntegerPower,
    EvaluateIntegerPowerDirectly,
    EvaluateArithmeticExpression,
    EvaluateProductOfIntegers,
    EvaluateSumOfIntegers,
    SimplifyIntegersInSum,
    SimplifyIntegersInProduct,
    SimplifyExpressionInBrackets,
    ;

    override val category = "IntegerArithmetic"
}

typealias Explanation = IntegerArithmeticExplanation
