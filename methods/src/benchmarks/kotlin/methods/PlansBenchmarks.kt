/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package methods

import engine.context.Context
import engine.context.emptyContext
import engine.expressions.RootOrigin
import engine.methods.Method
import engine.steps.Transformation
import kotlinx.benchmark.Mode
import methods.algebra.AlgebraPlans
import methods.constantexpressions.ConstantExpressionsPlans
import methods.equations.EquationsPlans
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import parser.parseExpression
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
class PlansBenchmarks {
    fun runBenchmark(method: Method, input: String, context: Context = emptyContext): Transformation? {
        val expr = parseExpression(input).withOrigin(RootOrigin())
        return method.tryExecute(context, expr)
    }

    @Benchmark
    fun cubeRootRationalisationBenchmark() =
        runBenchmark(
            method = ConstantExpressionsPlans.SimplifyConstantExpression,
            input = "[2 / -root[5, 3] + root[3, 3]]",
        )

    @Benchmark
    fun integerRootBenchmark() =
        runBenchmark(
            method = ConstantExpressionsPlans.SimplifyConstantExpression,
            input = "root[[430259200 ^ 13], 4] + root[[430259200 ^ 11], 4]",
        )

    @Benchmark
    fun slowRationalEquationBenchmark() =
        runBenchmark(
            method = EquationsPlans.SolveEquation,
            input = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]",
            context = Context(solutionVariables = listOf("x")),
        )

    @Benchmark
    fun slowSimplifyRationalExpressionBenchmark() =
        runBenchmark(
            method = AlgebraPlans.ComputeDomainAndSimplifyAlgebraicExpression,
            input = "([4 / x] - [7 / x - 3]) : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]",
        )
}
