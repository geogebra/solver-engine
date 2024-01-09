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

fun List<BigInteger>.gcd(): BigInteger = this.fold(BigInteger.ZERO, BigInteger::gcd)

fun List<BigInteger>.lcm(): BigInteger =
    this.fold(BigInteger.ONE) { acc, number ->
        acc.multiply(number).divide(acc.gcd(number))
    }

inline fun <T> List<T>.extractFirst(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
    var elementFound = false
    val first = mutableListOf<T>()
    val rest = mutableListOf<T>()
    for (element in this) {
        if (!elementFound && predicate(element)) {
            elementFound = true
            first.add(element)
        } else {
            rest.add(element)
        }
    }
    return Pair(first, rest)
}

/**
 * Generates the cartesian product of a number of lists.
 */
fun <T> product(lists: List<List<T>>): Sequence<List<T>> =
    sequence {
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
