package methods.plans

import engine.expressionmakers.move
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.fractionOf
import engine.patterns.optionalNegOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.plans.plan
import engine.steps.metadata.PlanExplanation
import engine.steps.metadata.Skill
import methods.rules.addLikeFractions
import methods.rules.cancelInAFraction
import methods.rules.commonDenominator
import methods.rules.findCommonFactorInFraction
import methods.rules.moveSignOfNegativeFactorOutOfProduct
import methods.rules.multiplyPositiveFractions
import methods.rules.negativeDenominator
import methods.rules.negativeNumerator
import methods.rules.negativeNumeratorAndDenominator
import methods.rules.simplifyDividingByAFraction
import methods.rules.simplifyDividingByANumber
import methods.rules.simplifyDoubleNeg
import methods.rules.simplifyDoubleNegBracket
import methods.rules.simplifyFractionToInteger
import methods.rules.simplifyFractionWithFractionDenominator
import methods.rules.simplifyFractionWithFractionNumerator
import methods.rules.simplifyProductWithTwoNegativeFactors

val normalizeFractionSigns = plan {
    firstOf {
        option(negativeNumeratorAndDenominator)
        option(negativeNumerator)
        option(negativeDenominator)
    }
}

val normalizeSignsInProduct = plan {
    firstOf {
        option(simplifyDoubleNegBracket)
        option(simplifyDoubleNeg)
        option(simplifyProductWithTwoNegativeFactors)
        option(moveSignOfNegativeFactorOutOfProduct)
    }
}

val simplifyNumericFraction = plan {
    val f = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())

    pattern = f

    explanation(PlanExplanation.SimplifyNumericFraction, move(f))

    skill(Skill.SimplifyNumericFraction, move(f))

    firstOf {
        option(simplifyFractionToInteger)
        option {
            pipeline {
                step(findCommonFactorInFraction)
                step(cancelInAFraction)
            }
        }
    }
}

val simplifyFractionsInExpression = plan {
    whilePossible {
        deeply(simplifyNumericFraction)
    }
}

val addFractions = plan {
    val f1 = optionalNegOf(fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern()))
    val f2 = optionalNegOf(fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern()))

    pattern = sumContaining(f1, f2)

    explanation(PlanExplanation.AddFractions, move(f1), move(f2))

    skill(Skill.AddFractions, move(f1), move(f2))

    pipeline {
        optionalStep(commonDenominator)
        optionalStep(simplifyArithmeticExpression)
        step(addLikeFractions)
        step(simplifyArithmeticExpression)
        optionalStep(normalizeFractionSigns)
        optionalStep(simplifyFractionsInExpression)
    }
}

val evaluatePositiveFractionProduct = plan {
    val f1 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
    val f2 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())

    pattern = productContaining(f1, f2)

    explanation(PlanExplanation.MultiplyFractions, move(f1), move(f2))

    skill(Skill.MultiplyFractions, move(f1), move(f2))

    pipeline {
        step(multiplyPositiveFractions)
        step(simplifyArithmeticExpression)
        optionalStep(simplifyFractionsInExpression)
    }
}

val combineFractionsInExpression = plan {
    whilePossible {
        firstOf {
            option {
                whilePossible {
                    deeply {
                        firstOf {
                            option(normalizeFractionSigns)
                            option(normalizeSignsInProduct)
                        }
                    }
                }
            }
            option {
                whilePossible {
                    deeply {
                        firstOf {
                            option(simplifyDividingByANumber)
                            option(simplifyDividingByAFraction)
                            option(simplifyFractionWithFractionDenominator)
                            option(simplifyFractionWithFractionNumerator)
                        }
                    }
                }
            }
            option {
                deeply(deepFirst = true) {
                    firstOf {
                        option(evaluatePositiveFractionProduct)
                        option(addFractions)
                    }
                }
            }
        }
    }
}

val addFractionsAndSimplify = plan {
    pipeline {
        step(addFractions)
        optionalStep {
            deeply(simplifyNumericFraction)
        }
    }
}
