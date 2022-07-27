package methods.fractionroots

import engine.methods.plan
import methods.fractionarithmetic.writeMultiplicationOfFractionsAsFraction
import methods.integerarithmetic.evaluateArithmeticExpression
import methods.integerroots.simplifyMultiplicationOfSquareRoots

/*
[4 / 3 * sqrt[3]] * [sqrt[3] / sqrt[3]] --> [4 * sqrt[3] / 9]
 */
val evaluateMultiplicationOfFractionWithUnitaryRadicalFraction = plan {
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
val rationalizationWithRadicalInDenominator = plan {
    pipeline {
        steps(writeAsMultiplicationWithUnitaryRadicalFraction)
        steps(evaluateMultiplicationOfFractionWithUnitaryRadicalFraction)
    }
}

/*
evaluates: sqrt[ [ 4 / 5 ] ] -> [ 2 * sqrt[5] / 5 ]
 */
val evaluateSquareRootFractions = plan {
    pipeline {
        steps(distributeRadicalRuleOverFractionsToNumeratorAndDenominator)
        optionalSteps(evaluateArithmeticExpression)
        optionalSteps(rationalizationWithRadicalInDenominator)
    }
}
