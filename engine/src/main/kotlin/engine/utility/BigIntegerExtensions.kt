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

package engine.utility

import java.math.BigInteger

private val MAX_FACTOR = 1000.toBigInteger()

private const val FACTORS_DEFAULT_LIMIT = 1000
private const val PRIME_CERTAINTY = 5

private const val MAX_KNOWN_SQUARE = 15
private const val MAX_KNOWN_CUBE = 5

private const val CUBE_POWER = 3

/**
 * Map (n, a^n) -> a for n and a >= 2 a small enough
 * This represents the powers that students should know by heart
 * (e.g. 36 = 6^2, 8 = 2^3)
 */
val knownPowers = buildMap<Pair<BigInteger, BigInteger>, BigInteger> {
    for (n in 2..MAX_KNOWN_SQUARE) {
        val bn = n.toBigInteger()
        put(Pair(BigInteger.TWO, bn * bn), bn)
    }
    for (n in 2..MAX_KNOWN_CUBE) {
        val bn = n.toBigInteger()
        put(Pair(CUBE_POWER.toBigInteger(), bn.pow(CUBE_POWER)), bn)
    }
}

fun BigInteger.isZero() = this.signum() == 0

fun BigInteger.isOdd() = this.lowestSetBit == 0

fun BigInteger.isEven() = this.lowestSetBit != 0

fun BigInteger.divides(n: BigInteger) = !isZero() && n.mod(this) == BigInteger.ZERO

fun BigInteger.divisibleBy(n: Int): Boolean {
    return n != 0 && this.mod(n.toBigInteger()) == BigInteger.ZERO
}

fun BigInteger.lcm(n: BigInteger) = if (n.isZero()) this else (this * n) / gcd(n)

/**
 * Newton's method for finding the largest integer `s` such that `s ^ 3 <= this`
 */
fun BigInteger.nthRoot(n: Int): BigInteger {
    val n1 = n - 1
    var s = this + BigInteger.ONE
    var u = this
    while (u < s) {
        s = u
        u = (u * n1.toBigInteger() + this / u.pow(n1)) / n.toBigInteger()
    }
    return s
}

fun BigInteger.cbrt() = this.nthRoot(3)

fun BigInteger.isSquare(): Boolean {
    if (signum() < 0) return false
    val sqrt = this.sqrt()
    return sqrt * sqrt == this
}

fun BigInteger.isCube(): Boolean {
    val cbrt = this.cbrt()
    return cbrt * cbrt * cbrt == this
}

/**
 * when at-least one of the prime factor has degree
 * greater than or equal to denominator of rational exponent
 * or when the gcd of multiplicity of prime factors with
 * denominator of rational exponent is not equal to 1
 */
fun BigInteger.isFactorizableUnderRationalExponent(numExp: BigInteger, denExp: BigInteger): Boolean {
    val hasHigherOrderRoot = this.hasFactorOfDegree(denExp.toInt())
    if (hasHigherOrderRoot) {
        return true
    }
    val pfd = this.primeFactorDecomposition()
    return (pfd.isNotEmpty()) && (
        pfd.any { (_, p) -> (p * numExp % denExp).isZero() } ||
            pfd.fold(denExp) { acc, f -> acc.gcd(f.second) } != BigInteger.ONE
    )
}

fun BigInteger.hasFactorOfDegree(n: Int): Boolean {
    var factor = BigInteger.ONE
    do {
        factor = factor.nextProbablePrime()
        val power = factor.pow(n)
        if (this.mod(power).signum() == 0) {
            return true
        }
    } while (power <= this && factor < MAX_FACTOR)

    return false
}

@Suppress("ReturnCount")
fun BigInteger.isPowerOfDegree(n: Int): Boolean {
    var remainder = this
    var factor = BigInteger.ONE

    if (this == BigInteger.ZERO || n == 1) {
        return true
    }

    do {
        factor = factor.nextProbablePrime()

        val factorPower = factor.pow(n)
        while (remainder.mod(factorPower).signum() == 0) {
            remainder = remainder.divide(factorPower)
        }

        if (remainder == BigInteger.ONE) {
            return true
        }
    } while (factor <= MAX_FACTOR && remainder.mod(factor).signum() != 0)

    return false
}

fun BigInteger.primeFactorDecomposition(): List<Pair<BigInteger, BigInteger>> {
    val factors = mutableListOf<Pair<BigInteger, BigInteger>>()

    val factorizer = Factorizer(this)
    var factor = BigInteger.ONE

    while (!factorizer.fullyFactorized()) {
        factor = factor.nextProbablePrime()

        if (factor > MAX_FACTOR) {
            factors.add(Pair(factorizer.n, BigInteger.ONE))
            break
        }

        val multiplicity = factorizer.extractMultiplicity(factor)
        if (multiplicity > 0) {
            factors.add(Pair(factor, BigInteger.valueOf(multiplicity.toLong())))
        }
    }

    return factors
}

/**
 * reference: https://stackoverflow.com/a/32035942/3396379
 */
@Suppress("ReturnCount")
fun BigInteger.isPrime(): Boolean {
    if (!isProbablePrime(PRIME_CERTAINTY)) {
        return false
    }

    if (this != BigInteger.TWO && isEven()) {
        return false
    }

    var i = 3.toBigInteger()
    while (i * i <= this && i < MAX_FACTOR) {
        if (this.mod(i) == BigInteger.ZERO) {
            return false
        }
        i += BigInteger.TWO
    }

    return true
}

class Factorizer(var n: BigInteger) {
    fun fullyFactorized() = n == BigInteger.ONE

    fun extractMultiplicity(f: BigInteger): Int {
        var multiplicity = 0
        while (f.divides(n)) {
            n /= f
            multiplicity++
        }
        return multiplicity
    }
}

fun gcd(vararg values: BigInteger): BigInteger {
    if (values.isEmpty()) {
        return BigInteger.ZERO
    }
    return values.reduce { acc, n -> acc.gcd(n) }
}

fun BigInteger.greatestSquareFactor(): BigInteger {
    // Simply loop over primes, checking if the square of the current prime is a factor

    var n = this // Remaining factor that could still have square factors
    var f = BigInteger.ONE // Square factor found so far

    var p = BigInteger.TWO // Current prime number considered
    var p2 = p * p // Current squared prime
    while (p2 <= n) {
        while (p2.divides(n)) {
            n /= p2
            f *= p2
        }
        p = p.nextProbablePrime()
        p2 = p * p
    }
    return f
}

fun BigInteger.factors(limit: Int = FACTORS_DEFAULT_LIMIT) =
    sequence<BigInteger> {
        if (signum() == 0) {
            return@sequence
        }
        val primeFactors = abs().primeFactorDecomposition().map { Pair(it.first, it.second.toInt() + 1) }

        val combinations = kotlin.math.min(limit, primeFactors.fold(1) { n, fm -> n * fm.second })

        for (i in 1..combinations) {
            var factor = BigInteger.ONE
            var ir = i
            for ((f, m) in primeFactors) {
                factor *= f.pow(ir % m)
                ir /= m
            }
            yield(factor)
        }
    }
