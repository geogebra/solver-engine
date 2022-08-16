package engine.utility

import java.math.BigInteger

private val MAX_FACTOR = 1000.toBigInteger()

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

    var remainder = this
    var factor = BigInteger.ONE

    while (remainder > BigInteger.ONE) {
        factor = factor.nextProbablePrime()

        if (factor > MAX_FACTOR) {
            factors.add(Pair(remainder, BigInteger.ONE))
            break
        }
        var multiplicity = 0L
        while (remainder.mod(factor).signum() == 0) {
            multiplicity++
            remainder = remainder.divide(factor)
        }

        if (multiplicity > 0) {
            factors.add(Pair(factor, BigInteger.valueOf(multiplicity)))
        }
    }

    return factors
}
