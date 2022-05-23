package rules

import expressionmakers.move
import expressionmakers.substituteIn
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
