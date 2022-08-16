package methods.fractionroots

import engine.methods.plan
import methods.fractionarithmetic.simplifyFractionToInteger
import methods.fractionarithmetic.writeMultiplicationOfFractionsAsFraction
import methods.integerarithmetic.evaluateArithmeticExpression
import methods.integerarithmetic.evaluateIntegerPowerDirectly
import methods.integerroots.simplifyMultiplicationOfSquareRoots

/*
[4 / 3 * sqrt[3]] * [sqrt[3] / sqrt[3]] --> [4 * sqrt[3] / 9]
 */
val evaluateMultiplicationOfFractionWithUnitaryRadicalFraction = plan {
    explanation(Explanation.EvaluateMultiplicationOfFractionWithUnitaryRadicalFraction)

    pipeline {
        steps(writeMultiplicationOfFractionsAsFraction)
        steps {
            deeply(simplifyMultiplicationOfSquareRoots, deepFirst = true)
        }
        steps(evaluateArithmeticExpression)
    }
}

/*
[4 / 3 * sqrt[3]] --> [4 * sqrt[3] / 9]
 */
val rationalizeWithRadicalInDenominator = plan {
    explanation(Explanation.RationalizeWithRadicalInDenominator)

    pipeline {
        steps(writeAsMultiplicationWithUnitaryRadicalFraction)
        steps(evaluateMultiplicationOfFractionWithUnitaryRadicalFraction)
    }
}

/*
evaluates: sqrt[ [ 4 / 5 ] ] -> [ 2 * sqrt[5] / 5 ]
 */
val evaluateSquareRootFractions = plan {
    explanation(Explanation.EvaluateSquareRootFractions)

    pipeline {
        steps(distributeRadicalRuleOverFractionsToNumeratorAndDenominator)
        optionalSteps(evaluateArithmeticExpression)
        optionalSteps(rationalizeWithRadicalInDenominator)
    }
}

val simplifyFractionOfRoots = plan {
    explanation(Explanation.SimplifyFractionOfRoots)

    pipeline {
        optionalSteps(bringRootsToSameIndexInFraction)
        optionalSteps {
            whilePossible {
                deeply(evaluateIntegerPowerDirectly)
            }
        }
        steps(simplifyFractionOfRootsWithSameOrder)
        steps {
            // apply to the fraction under the root
            applyTo(simplifyFractionToInteger) { it.nthChild(0) }
        }
    }
}
