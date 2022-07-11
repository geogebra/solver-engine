package methods.fractionroots

import engine.methods.plan
import methods.fractionarithmetic.writeMultiplicationOfFractionsAsFraction
import methods.integerarithmetic.simplifyArithmeticExpression
import methods.integerroots.simplifyMultiplicationOfSquareRoots

/*
[4 / 3 * sqrt[3]] * [sqrt[3] / sqrt[3]] --> [4 * sqrt[3] / 9]
 */
val evaluateMultiplicationOfFractionWithUnitaryRadicalFraction = plan {
    pipeline {
        step(writeMultiplicationOfFractionsAsFraction)
        step {
            deeply(simplifyMultiplicationOfSquareRoots, deepFirst = true)
        }
        step(simplifyArithmeticExpression)
    }
}

/*
[4 / 3 * sqrt[3]] --> [4 * sqrt[3] / 9]
 */
val rationalizationWithRadicalInDenominator = plan {
    pipeline {
        step(writeAsMultiplicationWithUnitaryRadicalFraction)
        step(evaluateMultiplicationOfFractionWithUnitaryRadicalFraction)
    }
}

/*
evaluates: sqrt[ [ 4 / 5 ] ] -> [ 2 * sqrt[5] / 5 ]
 */
val evaluateSquareRootFractions = plan {
    pipeline {
        step(distributeRadicalRuleOverFractionsToNumeratorAndDenominator)
        optionalStep(simplifyArithmeticExpression)
        optionalStep(rationalizationWithRadicalInDenominator)
    }
}
