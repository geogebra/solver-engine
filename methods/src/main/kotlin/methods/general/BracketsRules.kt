package methods.general

import engine.expressionmakers.makeBracketOf
import engine.expressionmakers.move
import engine.expressionmakers.substituteIn
import engine.expressions.UnaryOperator
import engine.methods.Rule
import engine.patterns.AnyPattern
import engine.patterns.OperatorPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.bracketOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.metadata.makeMetadata

val replaceInvisibleBrackets = run {
    val innerExpr = AnyPattern()
    val pattern = OperatorPattern(UnaryOperator.InvisibleBracket, listOf(innerExpr))

    Rule(
        pattern = pattern,
        resultMaker = makeBracketOf(move(innerExpr)),
        explanationMaker = makeMetadata(Explanation.ReplaceInvisibleBrackets),
    )
}

val removeBracketsSum = run {
    val innerSum = sumContaining()
    val bracket = bracketOf(innerSum)
    val pattern = sumContaining(bracket)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, move(innerSum)),
        explanationMaker = makeMetadata(Explanation.RemoveBracketSumInSum)
    )
}

val removeBracketsProduct = run {
    val innerSum = productContaining()
    val bracket = bracketOf(innerSum)
    val pattern = productContaining(bracket)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, move(innerSum)),
        explanationMaker = makeMetadata(Explanation.RemoveBracketProductInProduct)
    )
}

val removeBracketAroundSignedIntegerInSum = run {
    val number = SignedIntegerPattern()
    val bracket = bracketOf(number)
    val pattern = sumContaining(bracket)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, move(number)),
        explanationMaker = makeMetadata(Explanation.RemoveBracketAroundSignedIntegerInSum)
    )
}

val removeOuterBracket = run {
    val insideBracket = AnyPattern()
    val pattern = bracketOf(insideBracket)

    Rule(
        pattern = pattern,
        resultMaker = move(insideBracket),
        explanationMaker = makeMetadata(Explanation.RemoveRedundantBracket)
    )
}
