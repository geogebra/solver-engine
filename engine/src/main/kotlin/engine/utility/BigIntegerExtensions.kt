package engine.utility

import java.math.BigInteger

private val MAX_FACTOR = 1000.toBigInteger()
private const val PRIME_CERTAINTY = 5

fun BigInteger.isZero() = this.signum() == 0

fun BigInteger.isOdd() = this.lowestSetBit == 0

fun BigInteger.isEven() = this.lowestSetBit != 0

fun BigInteger.divides(d: BigInteger) = d.mod(this) == BigInteger.ZERO

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
