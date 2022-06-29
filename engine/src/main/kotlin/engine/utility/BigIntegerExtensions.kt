package engine.utility

import java.math.BigInteger

fun BigInteger.isZero() = this.signum() == 0

fun BigInteger.isOdd() = !this.hasFactor(BigInteger.TWO)

fun BigInteger.isEven() = this.hasFactor(BigInteger.TWO)

fun BigInteger.hasFactor(d: BigInteger) = this.mod(d).isZero()

fun BigInteger.hasFactorOfDegree(n: Int): Boolean {
    var factor = BigInteger.ONE
    do {
        factor = factor.nextProbablePrime()
        val power = factor.pow(n)
        if (this.mod(power).signum() == 0) {
            return true
        }
    } while (power <= this)

    return false
}

fun BigInteger.primeFactorDecomposition(): List<Pair<BigInteger, BigInteger>> {
    val factors = mutableListOf<Pair<BigInteger, BigInteger>>()

    var remainder = this
    var factor = BigInteger.ONE

    while (remainder > BigInteger.ONE) {
        factor = factor.nextProbablePrime()

        var multiplicity = 0L
        while (remainder.mod(factor).signum() == 0) {
            multiplicity++
            remainder = remainder.divide(factor)
        }

        if (multiplicity > 0) {
            factors.add(factor to BigInteger.valueOf(multiplicity))
        }
    }

    return factors
}
