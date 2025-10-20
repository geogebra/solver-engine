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

package engine.expressions

import engine.operators.ImaginaryUnitOperator
import engine.operators.InfinityOperator
import engine.operators.UndefinedOperator

@Suppress("MagicNumber")
object Constants {
    val MinusOne = xp(-1)
    val Zero = xp(0)
    val One = xp(1)
    val Two = xp(2)
    val Three = xp(3)
    val Four = xp(4)
    val Five = xp(5)
    val Ten = xp(10)
    val Ninety = xp(90)
    val OneHundredAndEighty = xp(180)
    val ThreeHundredAndSixty = xp(360)

    val OneHalf = fractionOf(One, Two)
    val OneQuarter = fractionOf(One, Four)

    val Pi = PiExpression()
    val E = EulerEExpression()
    val ImaginaryUnit = expressionOf(ImaginaryUnitOperator, emptyList())

    val Infinity = expressionOf(InfinityOperator, emptyList())
    val NegativeInfinity = negOf(Infinity)

    val Undefined = expressionOf(UndefinedOperator, emptyList())

    val EmptySet = FiniteSet(emptyList())
    val Reals = Reals()
}
