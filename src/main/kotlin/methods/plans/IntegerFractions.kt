package methods.plans

import engine.expressionmakers.move
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.fractionOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.plans.plan
import engine.steps.metadata.PlanExplanation
import engine.steps.metadata.Skill
import methods.rules.*


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

val addPositiveFractions = plan {
    val f1 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
    val f2 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())

    pattern = sumContaining(f1, f2)

    explanation(PlanExplanation.AddFractions, move(f1), move(f2))

    skill(Skill.AddFractions, move(f1), move(f2))

    pipeline {
        optionalStep(commonDenominator)
        optionalStep(simplifyArithmeticExpression)
        step(addLikeFractions)
        step(simplifyArithmeticExpression)
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
                deeply(deepFirst = true) {
                    firstOf {
                        option(evaluatePositiveFractionProduct)
                        option(addPositiveFractions)
                    }
                }
            }
        }
    }
}

val addFractionsAndSimplify = plan {
    pipeline {
        step(addPositiveFractions)
        optionalStep {
            deeply(simplifyNumericFraction)
        }
    }
}
