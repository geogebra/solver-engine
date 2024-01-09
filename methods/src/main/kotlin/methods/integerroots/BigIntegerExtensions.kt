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
