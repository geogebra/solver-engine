package methods.integerroots

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class IntegerRootsExplanation : CategorisedMetadataKey {
    SimplifyRootOfOne,
    SimplifyRootOfZero,
    FactorizeNumberUnderSquareRoot,
    SimplifyMultiplicationOfSquareRoots,
    SeparateSquaresUnderSquareRoot,
    SimplifyProductWithRoots,
    SimplifyIntegerRoot,
    SimplifyIntegerRootToInteger,
    CollectLikeRootsAndSimplify,
    MultiplyNthRoots,
    SplitRootOfProduct,
    NormaliseProductWithRoots,
    SimplifyNthRootToThePowerOfN,
    PrepareCancellingPowerOfARoot,
    SimplifyNthRootOfNthPower,
    PrepareCancellingRootOfAPower,
    TurnPowerOfRootToRootOfPower,
    SimplifyRootOfRoot,
    PutRootCoefficientUnderRoot,
    BringRootsToSameIndexInProduct,
    CancelPowerOfARoot,
    CancelRootOfAPower,
    PutRootCoefficientUnderRootAndSimplify,
    SimplifyRootOfRootWithCoefficient,
    CollectLikeRoots,
    BringSameIndexSameFactorRootsAsOneRoot,
    CombineProductOfSamePowerUnderHigherRoot;

    override val category = "IntegerRoots"
}

typealias Explanation = IntegerRootsExplanation
