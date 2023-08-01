package methods.factor

import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.Minus
import engine.expressions.Power
import engine.expressions.Product
import engine.expressions.Sum
import engine.expressions.equationOf
import engine.expressions.equationSystemOf
import engine.expressions.leadingCoefficientOfPolynomial
import engine.expressions.productOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.PipelineBuilder
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.ConditionPattern
import engine.patterns.ConstantPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.VariableExpressionPattern
import engine.patterns.condition
import engine.patterns.integerCondition
import engine.patterns.integerMonomialPattern
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.steps.metadata.metadata
import methods.expand.ExpandRules
import methods.integerarithmetic.IntegerArithmeticRules
import methods.polynomials.PolynomialRules
import methods.polynomials.expandAndSimplifier
import methods.polynomials.polynomialSimplificationSteps
import java.math.BigInteger

enum class FactorPlans(override val runner: CompositeMethod) : RunnerMethod {

    FactorGreatestCommonFactor(
        plan {
            explanation = Explanation.FactorGreatestCommonFactor
            pattern = sumContaining()

            // start by factoring the entire sum, then factor from what's left of the sum
            fun extractLastFactor(exp: Expression): Expression = when (exp) {
                is Minus -> extractLastFactor(exp.argument)
                is Product -> exp.children.last()
                else -> exp
            }

            steps {
                optionally(FactorRules.FactorNegativeSignOfLeadingCoefficient)
                apply {
                    optionally {
                        applyTo(FactorRules.FactorGreatestCommonIntegerFactor, ::extractLastFactor)
                    }
                    whilePossible {
                        applyTo(FactorRules.FactorCommonFactor, ::extractLastFactor)
                    }
                    whilePossible {
                        applyTo(::extractLastFactor) {
                            apply(FactorRules.RearrangeEquivalentSums)
                            apply(FactorRules.FactorCommonFactor)
                        }
                    }
                }
            }
        },
    ),

    FactorSquareOfBinomial(
        plan {
            explanation = Explanation.FactorSquareOfBinomial

            steps {
                optionally(FactorRules.FactorNegativeSignOfLeadingCoefficient)

                applyAfterMaybeFactoringMinus {
                    optionally(PolynomialRules.NormalizePolynomial)
                    optionally(FactorRules.RewriteSquareOfBinomial)
                    apply(FactorRules.ApplySquareOfBinomialFormula)
                }
            }
        },
    ),

    FactorCubeOfBinomial(
        plan {
            explanation = Explanation.FactorCubeOfBinomial

            steps {
                optionally(PolynomialRules.NormalizePolynomial)
                optionally(FactorRules.RewriteCubeOfBinomial)
                apply(FactorRules.ApplyCubeOfBinomialFormula)
            }
        },
    ),

    FactorDifferenceOfSquares(
        plan {
            explanation = Explanation.FactorDifferenceOfSquares

            steps {
                optionally {
                    checkForm {
                        sumOf(negOf(VariableExpressionPattern()), ConstantPattern())
                    }
                    apply(FactorRules.FactorNegativeSignOfLeadingCoefficient)
                }

                applyAfterMaybeFactoringMinus {
                    optionally(FactorRules.RewriteDifferenceOfSquares)
                    apply(FactorRules.ApplyDifferenceOfSquaresFormula)
                }
            }
        },
    ),

    FactorDifferenceOfCubes(
        plan {
            explanation = Explanation.FactorDifferenceOfCubes

            steps {
                optionally(FactorRules.RewriteSumAndDifferenceOfCubes)
                apply(FactorRules.ApplyDifferenceOfCubesFormula)
                whilePossible(polynomialSimplificationSteps)
            }
        },
    ),

    FactorSumOfCubes(
        plan {
            explanation = Explanation.FactorSumOfCubes

            steps {
                optionally(FactorRules.RewriteSumAndDifferenceOfCubes)
                apply(FactorRules.ApplySumOfCubesFormula)
                whilePossible(polynomialSimplificationSteps)
            }
        },
    ),

    FactorByGrouping(
        plan {
            explanation = Explanation.FactorByGrouping
            pattern = condition(sumContaining()) { it.childCount > 2 }

            val innerFactorSteps = engine.methods.stepsproducers.steps {
                firstOf {
                    option(factorizeSumSteps)
                    option(FactorRules.FactorNegativeSignOfLeadingCoefficient)
                }
            }

            steps {
                firstOf {
                    optionsFor({ 1 until it.childCount }) {
                        apply(GroupPolynomial(it))
                        apply {
                            optionally { applyTo(innerFactorSteps) { it.firstChild } }
                            optionally { applyTo(innerFactorSteps) { it.secondChild } }
                        }
                        apply(factorizeSumSteps)
                    }
                }
            }
        },
    ),

    FactorMonicTrinomialByGuessing(
        taskSet {
            explanation = Explanation.FactorTrinomialByGuessing

            val variable = ArbitraryVariablePattern()

            val squaredExponent = UnsignedIntegerPattern()
            val squaredTerm = powerOf(variable, squaredExponent)
            val baseTerm = integerMonomialPattern(variable)
            val constantTerm = SignedIntegerPattern()

            pattern = ConditionPattern(
                sumOf(squaredTerm, baseTerm, constantTerm),
                integerCondition(squaredExponent, baseTerm.exponent) { a, b -> a == BigInteger.TWO * b },
            )

            tasks {
                val solvedSystem = task(
                    startExpr = equationSystemOf(
                        equationOf(sumOf(xp("a"), xp("b")), get(baseTerm::coefficient)!!),
                        equationOf(productOf(xp("a"), xp("b")), move(constantTerm)),
                    ),
                    explanation = metadata(Explanation.SetUpAndSolveEquationSystemForMonicTrinomial),
                    stepsProducer = FactorRules.SolveSumProductDiophantineEquationSystemByGuessing,
                ) ?: return@tasks null

                val solution1 = solvedSystem.result.firstChild.secondChild
                val solution2 = solvedSystem.result.secondChild.secondChild

                val halfPower = simplifiedPowerOf(
                    move(variable),
                    integerOp(squaredExponent) { it / 2.toBigInteger() },
                )

                task(
                    startExpr = productOf(sumOf(halfPower, solution1), sumOf(halfPower, solution2)),
                    explanation = metadata(Explanation.FactorTrinomialUsingTheSolutionsOfTheSumAndProductSystem),
                    dependsOn = listOf(solvedSystem),
                )

                allTasks()
            }
        },
    ),

    SplitNonMonicTrinomial(
        taskSet {
            explanation = Explanation.SplitNonMonicTrinomial

            val variable = ArbitraryVariablePattern()

            val squaredTerm = integerMonomialPattern(variable)
            val baseTerm = integerMonomialPattern(variable)
            val constantTerm = SignedIntegerPattern()

            pattern = ConditionPattern(
                sumOf(squaredTerm, baseTerm, constantTerm),
                integerCondition(squaredTerm.exponent, baseTerm.exponent) { a, b -> a == BigInteger.TWO * b },
            )

            tasks {
                val squaredCoefficient = squaredTerm.ptn.getCoefficient()
                if (squaredCoefficient == Constants.One) return@tasks null

                val baseCoefficient = baseTerm.ptn.getCoefficient()

                val solvedSystem = task(
                    startExpr = equationSystemOf(
                        equationOf(sumOf(xp("a"), xp("b")), baseCoefficient),
                        equationOf(
                            productOf(xp("a"), xp("b")),
                            simplifiedProductOf(squaredCoefficient, move(constantTerm)),
                        ),
                    ),
                    explanation = metadata(Explanation.SetUpAndSolveEquationSystemForNonMonicTrinomial),
                ) {
                    optionally { deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision) }
                    apply(FactorRules.SolveSumProductDiophantineEquationSystemByGuessing)
                } ?: return@tasks null

                val solution1 = solvedSystem.result.firstChild.secondChild
                val solution2 = solvedSystem.result.secondChild.secondChild

                val base = get(baseTerm.powerPattern)

                task(
                    startExpr = sumOf(
                        get(squaredTerm),
                        simplifiedProductOf(solution1, base),
                        simplifiedProductOf(solution2, base),
                        get(constantTerm),
                    ),
                    explanation = metadata(Explanation.SplitTrinomialUsingTheSolutionsOfTheSumAndProductSystem),
                    dependsOn = listOf(solvedSystem),
                )

                allTasks()
            }
        },
    ),

    FactorNonMonicTrinomial(
        plan {
            explanation = Explanation.FactorNonMonicTrinomial

            steps {
                apply(SplitNonMonicTrinomial)
                apply(FactorByGrouping)
            }
        },
    ),

    /**
     * Factor a polynomial in one variable.
     */
    @PublicMethod
    FactorPolynomialInOneVariable(
        plan {
            explanation = Explanation.FactorPolynomial
            pattern = condition { it.variables.size <= 1 }
            resultPattern = optionalNegOf(oneOf(productContaining(), powerOf(AnyPattern(), AnyPattern())))

            steps {
                apply(factorizationSteps)
            }
        },
    ),
}

private val factorizeSumSteps = steps {
    firstOf {
        option(FactorPlans.FactorGreatestCommonFactor)
        option(FactorPlans.FactorSquareOfBinomial)
        option(FactorPlans.FactorCubeOfBinomial)
        option(FactorPlans.FactorDifferenceOfSquares)
        option(FactorPlans.FactorDifferenceOfCubes)
        option(FactorPlans.FactorSumOfCubes)
        option(FactorPlans.FactorByGrouping)
        option(FactorPlans.FactorMonicTrinomialByGuessing)
        option(FactorPlans.FactorNonMonicTrinomial)
    }
}

private val factorizeSumByFactoringTermsSteps: StepsProducer = steps {
    check { it is Sum }
    applyToChildren {
        applyTo(factorizationSteps) {
            if (it is Minus) it.argument else it
        }
    }
    apply(factorizeSumSteps)
    applyToChildren(factorizationSteps)
    whilePossible(polynomialSimplificationSteps)
}

private val factorizeSumByExpandingTermsSteps: StepsProducer = steps {
    check { it is Sum }
    whilePossible {
        firstOf {
            option(polynomialSimplificationSteps)
            option { deeply(expandAndSimplifier.steps, deepFirst = true) }
        }
    }
    optionally {
        apply(factorizeSumSteps)
        applyToChildren(factorizationSteps)
    }
}

private val factorizeMinusSteps: StepsProducer = steps {
    firstOf {
        option {
            check { it is Minus && it.argument is Sum && leadingCoefficientOfPolynomial(it.argument as Sum) is Minus }
            apply(ExpandRules.DistributeNegativeOverBracket)
            optionally(factorizationSteps)
        }
        option {
            check { it is Minus }
            applyToKind<Minus>(factorizationSteps) { it.argument }
            // to tidy up things like -(-(x+1)^2)
            whilePossible(polynomialSimplificationSteps)
        }
    }
}

private val factorizeProductSteps: StepsProducer = steps {
    check { it is Product }
    applyToChildren(factorizationSteps, atLeastOne = true)
    // to tidy up things like (x+1)^2 (x+2) (x+1)^3
    whilePossible(polynomialSimplificationSteps)
}

private val factorizePowerSteps: StepsProducer = steps {
    check { it is Power }
    applyToKind<Power>(factorizationSteps) { it.base }
    // to tidy up things like (2(x + 1))^2
    whilePossible(polynomialSimplificationSteps)
}

val factorizationSteps: StepsProducer = steps {
    firstOf {
        option(factorizeSumByFactoringTermsSteps)
        option(factorizeSumByExpandingTermsSteps)
        option(factorizeMinusSteps)
        option(factorizeProductSteps)
        option(factorizePowerSteps)

        // TODO figure out if there are problems with removing this option
        // option { whilePossible(polynomialSimplificationSteps) }
    }
}

private fun PipelineBuilder.applyAfterMaybeFactoringMinus(init: PipelineBuilder.() -> Unit) {
    applyTo(extractor = { if (it is Minus) it.argument else it }, init)
}
