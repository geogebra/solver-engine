package methods.general

import engine.expressions.bracketOf
import engine.methods.TransformationResult
import engine.methods.rule
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.OperatorPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.bracketOf
import engine.patterns.plusOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.metadata.metadata

val replaceInvisibleBrackets = rule {
    val innerExpr = AnyPattern()
    val pattern = OperatorPattern(UnaryExpressionOperator.InvisibleBracket, listOf(innerExpr))

    onPattern(pattern) {
        TransformationResult(
            toExpr = bracketOf(move(innerExpr)),
            explanation = metadata(Explanation.ReplaceInvisibleBrackets),
        )
    }
}

val removeBracketsSum = rule {
    val innerSum = sumContaining()
    val bracket = bracketOf(innerSum)
    val pattern = sumContaining(bracket)

    onPattern(pattern) {
        TransformationResult(
            toExpr = pattern.substitute(move(innerSum)),
            explanation = metadata(Explanation.RemoveBracketSumInSum)
        )
    }
}

val removeBracketsProduct = rule {
    val innerSum = productContaining()
    val bracket = bracketOf(innerSum)
    val pattern = productContaining(bracket)

    onPattern(pattern) {
        TransformationResult(
            toExpr = pattern.substitute(move(innerSum)),
            explanation = metadata(Explanation.RemoveBracketProductInProduct)
        )
    }
}

val removeBracketAroundSignedIntegerInSum = rule {
    val number = SignedIntegerPattern()
    val bracket = bracketOf(number)
    val pattern = sumContaining(bracket)

    onPattern(pattern) {
        TransformationResult(
            toExpr = pattern.substitute(move(number)),
            explanation = metadata(Explanation.RemoveBracketAroundSignedIntegerInSum)
        )
    }
}

val removeOuterBracket = rule {
    val insideBracket = AnyPattern()
    val pattern = bracketOf(insideBracket)

    onPattern(pattern) {
        TransformationResult(
            toExpr = move(insideBracket),
            explanation = metadata(Explanation.RemoveRedundantBracket)
        )
    }
}

val removeRedundantPlusSign = rule {
    val value = AnyPattern()
    val pattern = plusOf(value)

    onPattern(pattern) {
        TransformationResult(
            toExpr = move(value),
            explanation = metadata(Explanation.RemoveRedundantPlusSign)
        )
    }
}
