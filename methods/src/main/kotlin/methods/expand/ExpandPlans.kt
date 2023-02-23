package methods.expand

import engine.context.Curriculum
import engine.context.ResourceData
import engine.expressions.Constants
import engine.methods.Method
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.sumOf
import methods.general.GeneralRules

fun expandSingleBracketAndSimplify(simplificationSteps: StepsProducer): Method {
    return plan {
        explanation = Explanation.ExpandSingleBracketAndSimplify

        steps {
            firstOf {
                option(ExpandRules.DistributeMultiplicationOverSum)
            }
            optionally(simplificationSteps)
        }
    }
}

fun expandDoubleBracketsAndSimplify(simplificationSteps: StepsProducer): Method {
    return plan {
        explanation = Explanation.ExpandDoubleBracketsAndSimplify
        val factor1 = sumContaining()
        val factor2 = sumContaining()
        pattern = productContaining(factor1, factor2)

        partialExpressionSteps {
            firstOf {
                option(ExpandRules.ExpandProductOfSumAndDifference)
                option(ExpandRules.ApplyFoilMethod)
                option(ExpandRules.ExpandDoubleBrackets)
            }
            optionally(simplificationSteps)
        }
    }
}

fun expandBinomialSquaredAndSimplify(simplificationSteps: StepsProducer): Method {
    return plan {
        explanation = Explanation.ExpandBinomialSquaredAndSimplify
        pattern = powerOf(sumOf(AnyPattern(), AnyPattern()), FixedPattern(Constants.Two))

        steps {
            contextSensitive {
                default(
                    ResourceData(curriculum = Curriculum.EU),
                    ExpandRules.ExpandBinomialSquaredUsingIdentity,
                )
                alternative(ResourceData(curriculum = Curriculum.US)) {
                    apply(GeneralRules.RewritePowerAsProduct)
                    apply(ExpandRules.ApplyFoilMethod)
                }
            }
            optionally(simplificationSteps)
        }
    }
}

fun expandBinomialCubedAndSimplify(simplificationSteps: StepsProducer): Method {
    return plan {
        explanation = Explanation.ExpandBinomialCubedAndSimplify
        pattern = powerOf(sumOf(AnyPattern(), AnyPattern()), FixedPattern(Constants.Three))

        steps(ResourceData(curriculum = Curriculum.EU)) {
            apply(ExpandRules.ExpandBinomialCubedUsingIdentity)
            optionally(simplificationSteps)
        }

        alternative(ResourceData(curriculum = Curriculum.US)) {
            apply(GeneralRules.RewritePowerAsProduct)
            apply(expandDoubleBracketsAndSimplify(simplificationSteps))
            apply(expandDoubleBracketsAndSimplify(simplificationSteps))
        }
    }
}

fun expandTrinomialSquaredAndSimplify(simplificationSteps: StepsProducer): Method {
    return plan {
        explanation = Explanation.ExpandTrinomialSquaredAndSimplify
        pattern = powerOf(
            sumOf(AnyPattern(), AnyPattern(), AnyPattern()),
            FixedPattern(Constants.Two),
        )

        steps {
            contextSensitive {
                default(
                    ResourceData(curriculum = Curriculum.EU),
                    ExpandRules.ExpandTrinomialSquaredUsingIdentity,
                )
                alternative(ResourceData(curriculum = Curriculum.US)) {
                    apply(GeneralRules.RewritePowerAsProduct)
                    apply(ExpandRules.ExpandDoubleBrackets)
                }
            }
            optionally(simplificationSteps)
        }
    }
}

fun expandAndSimplifySteps(simplificationSteps: StepsProducer): StepsProducer {
    // making sure these plans are only created once
    val expandDoubleBrackets = expandDoubleBracketsAndSimplify(simplificationSteps)
    val expandSingleBracket = expandSingleBracketAndSimplify(simplificationSteps)
    val expandBinomialsSquared = expandBinomialSquaredAndSimplify(simplificationSteps)
    val expandBinomialCubed = expandBinomialCubedAndSimplify(simplificationSteps)
    val expandTrinomialSquared = expandTrinomialSquaredAndSimplify(simplificationSteps)

    return steps {
        firstOf {
            option(expandDoubleBrackets)
            option(expandSingleBracket)
            option(expandBinomialsSquared)
            option(expandBinomialCubed)
            option(expandTrinomialSquared)
        }
    }
}