package rules

import expressionmakers.move
import expressionmakers.substituteIn
import patterns.SignedIntegerPattern
import patterns.UnsignedIntegerPattern
import patterns.bracketOf
import patterns.sumContaining
import steps.makeMetadata

val removeBracketsSum = run {
    val innerSum = sumContaining()
    val bracket = bracketOf(innerSum)
    val pattern = sumContaining(bracket)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, move(innerSum)),
        explanationMaker = makeMetadata("remove brackets in inner sum")
    )
}

val removeBracketAroundSignedIntegerInSum = run {
    val number = SignedIntegerPattern()
    val bracket = bracketOf(number)
    val pattern = sumContaining(bracket)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, move(number)),
        explanationMaker = makeMetadata("remove brackets around integer in sum")
    )
}


val removeBracketAroundUnsignedInteger = run {
    val number = UnsignedIntegerPattern()
    val pattern = bracketOf(number)

    Rule(
        pattern = pattern,
        resultMaker = move(number),
        explanationMaker = makeMetadata("remove brackets around unsigned integer")
    )
}
