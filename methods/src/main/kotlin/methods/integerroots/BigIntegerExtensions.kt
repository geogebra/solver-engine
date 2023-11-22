package methods.integerroots

import engine.utility.Factorizer
import engine.utility.divides
import engine.utility.knownPowers
import java.math.BigInteger

/**
 * Splits an integer as a product whose root of order [rootOrder] will be easy to compute.
 */
internal fun BigInteger.asProductForRoot(rootOrder: BigInteger): List<BigInteger>? {
    val factorizer = Factorizer(this)
    val multiplicityOfTen = factorizer.extractMultiplicity(BigInteger.TEN)
    if (multiplicityOfTen == 0 || Pair(rootOrder, factorizer.n) !in knownPowers) {
        return null
    }
    return listOf(factorizer.n, BigInteger.TEN.pow(multiplicityOfTen))
}

/**
 * Writes an integer as a power whose root of order [rootOrder] will be easy to compute.
 */
internal fun BigInteger.asPowerForRoot(rootOrder: BigInteger): Pair<BigInteger, BigInteger>? {
    val factorizer = Factorizer(this)
    val multiplicityOfTen = factorizer.extractMultiplicity(BigInteger.TEN)
    return when {
        multiplicityOfTen == 0 -> {
            val root = knownPowers[Pair(rootOrder, this)]
            if (root == null) null else Pair(root, rootOrder)
        }
        multiplicityOfTen == 1 || !factorizer.fullyFactorized() -> null
        rootOrder.divides(multiplicityOfTen.toBigInteger()) -> Pair(
            BigInteger.TEN.pow(multiplicityOfTen / rootOrder.toInt()),
            rootOrder,
        )
        rootOrder < multiplicityOfTen.toBigInteger() -> Pair(
            BigInteger.TEN,
            multiplicityOfTen.toBigInteger(),
        )
        else -> null
    }
}
