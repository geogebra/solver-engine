package engine.utility

import java.math.BigInteger

fun List<BigInteger>.gcd(): BigInteger = this.fold(BigInteger.ZERO, BigInteger::gcd)

fun List<BigInteger>.lcm(): BigInteger = this.fold(BigInteger.ONE) { acc, number ->
    acc.multiply(number).divide(acc.gcd(number))
}

/**
 * Generates the cartesian product of a number of lists.
 */
fun <T> product(lists: List<List<T>>): Sequence<List<T>> = sequence {
    if (lists.isNotEmpty()) {
        val lastIndex = lists.size - 1
        for (items in product(lists.subList(0, lastIndex))) {
            for (lastItem in lists[lastIndex]) {
                yield(items + lastItem)
            }
        }
    } else {
        yield(emptyList())
    }
}
