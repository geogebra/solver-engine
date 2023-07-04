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
import methods.general.NormalizationRules

interface ExpandAndSimplifyMethodsProvider {
    val singleBracketMethod: Method
    val doubleBracketsMethod: Method
    val binomialSquaredMethod: Method
    val binomialCubedMethod: Method
    val trinomialSquaredMethod: Method
    val steps: StepsProducer
}

class ExpandAndSimplifier(simplificationSteps: StepsProducer) : ExpandAndSimplifyMethodsProvider {

    private val simplificationWithCleanup: StepsProducer = steps {
        whilePossible { deeply(NormalizationRules.NormalizeProducts) }
        optionally(simplificationSteps)
    }

    override val singleBracketMethod = plan {
        explanation = Explanation.ExpandSingleBracketAndSimplify

        steps {
            apply(ExpandRules.DistributeMultiplicationOverSum)
            optionally(simplificationWithCleanup)
        }
    }

    override val doubleBracketsMethod = plan {
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
            optionally(simplificationWithCleanup)
        }
    }

    override val binomialSquaredMethod = plan {
        explanation = Explanation.ExpandBinomialSquaredAndSimplify
        pattern = powerOf(sumOf(AnyPattern(), AnyPattern()), FixedPattern(Constants.Two))

        steps {
            contextSensitive {
                default(
                    ResourceData(curriculum = Curriculum.EU),
                    ExpandRules.ExpandBinomialSquaredUsingIdentity,
                )
                alternative(ResourceData(gmFriendly = true)) {
                    apply(ExpandRules.ExpandBinomialSquaredUsingIdentity)
                }
                alternative(ResourceData(curriculum = Curriculum.US)) {
                    apply(GeneralRules.RewritePowerAsProduct)
                    apply(ExpandRules.ApplyFoilMethod)
                }
            }
            optionally(simplificationWithCleanup)
        }
    }

    override val binomialCubedMethod = plan {
        explanation = Explanation.ExpandBinomialCubedAndSimplify
        pattern = powerOf(sumOf(AnyPattern(), AnyPattern()), FixedPattern(Constants.Three))

        steps(ResourceData(curriculum = Curriculum.EU)) {
            apply(ExpandRules.ExpandBinomialCubedUsingIdentity)
            optionally(simplificationWithCleanup)
        }
        alternative(ResourceData(gmFriendly = true)) {
            apply(ExpandRules.ExpandBinomialCubedUsingIdentity)
            optionally(simplificationWithCleanup)
        }
        alternative(ResourceData(curriculum = Curriculum.US)) {
            apply(GeneralRules.RewritePowerAsProduct)
            apply(doubleBracketsMethod)
            apply(doubleBracketsMethod)
        }
    }

    override val trinomialSquaredMethod = plan {
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
            optionally(simplificationWithCleanup)
        }
    }

    override val steps = steps {
        firstOf {
            option(doubleBracketsMethod)
            option(singleBracketMethod)
            option(binomialSquaredMethod)
            option(binomialCubedMethod)
            option(trinomialSquaredMethod)
        }
    }
}
