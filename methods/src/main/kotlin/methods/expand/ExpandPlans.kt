package methods.expand

import engine.context.BooleanSetting
import engine.context.Setting
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

interface ExpandAndSimplifyMethodsProvider {
    val singleBracketMethod: Method
    val fractionMethod: Method
    val doubleBracketsMethod: Method
    val binomialSquaredMethod: Method
    val binomialCubedMethod: Method
    val trinomialSquaredMethod: Method
    val steps: StepsProducer
}

class ExpandAndSimplifier(simplificationSteps: StepsProducer) : ExpandAndSimplifyMethodsProvider {

    override val singleBracketMethod = plan {
        explanation = Explanation.ExpandSingleBracketAndSimplify

        steps {
            apply(ExpandRules.DistributeMultiplicationOverSum)
            optionally(simplificationSteps)
        }
    }

    override val fractionMethod = plan {
        explanation = Explanation.ExpandFractionAndSimplify

        steps {
            check { isSet(Setting.RestrictAddingFractionsWithConstantDenominator) }
            apply(ExpandRules.DistributeConstantNumerator)
            optionally(simplificationSteps)
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
            optionally(simplificationSteps)
        }
    }

    override val binomialSquaredMethod = plan {
        explanation = Explanation.ExpandBinomialSquaredAndSimplify
        pattern = powerOf(sumOf(AnyPattern(), AnyPattern()), FixedPattern(Constants.Two))

        steps {
            branchOn(Setting.DontUseIdentitiesForExpanding) {
                case(BooleanSetting.True) {
                    apply(GeneralRules.RewritePowerAsProduct)
                    apply(ExpandRules.ApplyFoilMethod)
                }

                case(BooleanSetting.False, ExpandRules.ExpandBinomialSquaredUsingIdentity)
            }

            optionally(simplificationSteps)
        }
    }

    override val binomialCubedMethod = plan {
        explanation = Explanation.ExpandBinomialCubedAndSimplify
        pattern = powerOf(sumOf(AnyPattern(), AnyPattern()), FixedPattern(Constants.Three))

        steps {
            branchOn(Setting.DontUseIdentitiesForExpanding) {
                case(BooleanSetting.True) {
                    apply(GeneralRules.RewritePowerAsProduct)
                    apply(doubleBracketsMethod)
                    apply(doubleBracketsMethod)
                }

                case(BooleanSetting.False) {
                    apply(ExpandRules.ExpandBinomialCubedUsingIdentity)
                    optionally(simplificationSteps)
                }
            }
        }
    }

    override val trinomialSquaredMethod = plan {
        explanation = Explanation.ExpandTrinomialSquaredAndSimplify
        pattern = powerOf(
            sumOf(AnyPattern(), AnyPattern(), AnyPattern()),
            FixedPattern(Constants.Two),
        )

        steps {
            branchOn(Setting.DontUseIdentitiesForExpanding) {
                case(BooleanSetting.True) {
                    apply(GeneralRules.RewritePowerAsProduct)
                    apply(ExpandRules.ExpandDoubleBrackets)
                }

                case(BooleanSetting.False, ExpandRules.ExpandTrinomialSquaredUsingIdentity)
            }

            optionally(simplificationSteps)
        }
    }

    override val steps = steps {
        firstOf {
            option(doubleBracketsMethod)
            option(singleBracketMethod)
            option(fractionMethod)
            option(binomialSquaredMethod)
            option(binomialCubedMethod)
            option(trinomialSquaredMethod)
        }
    }
}
