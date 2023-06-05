package methods.factor

import engine.expressions.Constants
import engine.expressions.Minus
import engine.expressions.equationOf
import engine.expressions.equationSystemOf
import engine.expressions.productOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.ConditionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.integerCondition
import engine.patterns.integerMonomialPattern
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.steps.metadata.metadata
import methods.integerarithmetic.IntegerArithmeticRules
import methods.polynomials.PolynomialRules
import methods.polynomials.algebraicSimplificationSteps
import java.math.BigInteger

enum class FactorPlans(override val runner: CompositeMethod) : RunnerMethod {

    FactorGreatestCommonFactor(
        plan {
            explanation = Explanation.FactorGreatestCommonFactor

            steps {
                optionally(FactorRules.SplitIntegersInMonomialsBeforeFactoring)
                optionally(FactorRules.SplitVariablePowersInMonomialsBeforeFactoring)
                apply(FactorRules.ExtractCommonTerms)
            }
        },
    ),

    FactorSquareOfBinomial(
        plan {
            explanation = Explanation.FactorSquareOfBinomial

            val factorSteps = engine.methods.stepsproducers.steps {
                optionally(PolynomialRules.NormalizePolynomial)
                optionally(FactorRules.RewriteSquareOfBinomial)
                apply(FactorRules.ApplySquareOfBinomialFormula)
            }

            steps {
                firstOf {
                    option(factorSteps)
                    option {
                        apply(FactorRules.FactorNegativeSignOfLeadingCoefficient)
                        applyToKind<Minus>(factorSteps) { it.argument }
                    }
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
                optionally(FactorRules.RewriteDifferenceOfSquares)
                apply(FactorRules.ApplyDifferenceOfSquaresFormula)
            }
        },
    ),

    FactorDifferenceOfCubes(
        plan {
            explanation = Explanation.FactorDifferenceOfCubes

            steps {
                optionally(FactorRules.RewriteSumAndDifferenceOfCubes)
                apply(FactorRules.ApplyDifferenceOfCubesFormula)
                whilePossible(algebraicSimplificationSteps)
            }
        },
    ),

    FactorSumOfCubes(
        plan {
            explanation = Explanation.FactorSumOfCubes

            steps {
                optionally(FactorRules.RewriteSumAndDifferenceOfCubes)
                apply(FactorRules.ApplySumOfCubesFormula)
                whilePossible(algebraicSimplificationSteps)
            }
        },
    ),

    FactorByGrouping(
        plan {
            explanation = Explanation.FactorByGrouping
            pattern = condition(sumContaining()) { it.childCount > 2 }

            val innerFactorSteps = engine.methods.stepsproducers.steps {
                firstOf {
                    option(factorizationSteps)
                    option(FactorRules.FactorNegativeSignOfLeadingCoefficient)
                }
            }

            steps {
                firstOf {
                    optionsFor({ 1 until it.childCount }) {
                        apply(GroupPolynomial(it))
                        optionally { applyTo(innerFactorSteps) { it.firstChild } }
                        optionally { applyTo(innerFactorSteps) { it.secondChild } }
                        apply(factorizationSteps)
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
            pattern = condition { it.variables.size == 1 }
            resultPattern = optionalNegOf(oneOf(productContaining(), powerOf(AnyPattern(), AnyPattern())))

            steps {
                whilePossible(algebraicSimplificationSteps)
                apply(factorizationSteps)
            }
        },
    ),
}

private val factorizationSteps = steps {
    whilePossible {
        firstOf {
            option { deeply(FactorPlans.FactorGreatestCommonFactor) }
            option { deeply(FactorPlans.FactorSquareOfBinomial) }
            option { deeply(FactorPlans.FactorCubeOfBinomial) }
            option { deeply(FactorPlans.FactorDifferenceOfSquares) }
            option { deeply(FactorPlans.FactorDifferenceOfCubes) }
            option { deeply(FactorPlans.FactorSumOfCubes) }
            option { deeply(FactorPlans.FactorByGrouping) }
            option { deeply(FactorPlans.FactorMonicTrinomialByGuessing) }
            option { deeply(FactorPlans.FactorNonMonicTrinomial) }
        }
        whilePossible(algebraicSimplificationSteps)
    }
}
