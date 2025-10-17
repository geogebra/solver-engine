/*
 * Copyright (c) 2025 GeoGebra GmbH, office@geogebra.org
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

import engine.expressions.Constants.Five
import engine.expressions.Constants.Four
import engine.expressions.Constants.Infinity
import engine.expressions.Constants.MinusOne
import engine.expressions.Constants.One
import engine.expressions.Constants.OneHalf
import engine.expressions.Constants.Pi
import engine.expressions.Constants.Ten
import engine.expressions.Constants.Three
import engine.expressions.Constants.Two
import engine.expressions.Constants.Zero

@Suppress("MagicNumber")
object TrigonometricConstants {
    enum class MainAngles(
        val sine: Expression,
        val cosine: Expression,
        val tangent: Expression,
        val cotangent: Expression,
    ) {
        ZeroAngle(Zero, One, Zero, Infinity),
        PiOverTwelve(
            fractionOf(sumOf(squareRootOf(xp(6)), negOf(squareRootOf(Two))), Four),
            fractionOf(sumOf(squareRootOf(xp(6)), squareRootOf(Two)), Four),
            sumOf(Two, negOf(squareRootOf(Three))),
            sumOf(Two, squareRootOf(Three)),
        ),
        PiOverTen(
            fractionOf(sumOf(squareRootOf(Five), negOf(One)), Four),
            fractionOf(squareRootOf(sumOf(Ten, productOf(Two, squareRootOf(Five)))), Four),
            fractionOf(squareRootOf(sumOf(xp(25), negOf(productOf(Ten, squareRootOf(Five))))), Five),
            squareRootOf(sumOf(Five, productOf(Two, squareRootOf(Five)))),
        ),
        PiOverEight(
            fractionOf(squareRootOf(sumOf(Two, negOf(squareRootOf(Two)))), Two),
            fractionOf(squareRootOf(sumOf(Two, squareRootOf(Two))), Two),
            sumOf(squareRootOf(Two), negOf(One)),
            sumOf(squareRootOf(Two), One),
        ),
        PiOverSix(
            OneHalf,
            fractionOf(squareRootOf(Three), Two),
            fractionOf(squareRootOf(Three), Three),
            squareRootOf(Three),
        ),
        PiOverFive(
            fractionOf(squareRootOf(sumOf(Ten, negOf(productOf(Two, squareRootOf(Five))))), Four),
            fractionOf(sumOf(squareRootOf(Five), One), Four),
            squareRootOf(sumOf(Five, negOf(productOf(Two, squareRootOf(Five))))),
            fractionOf(squareRootOf(sumOf(xp(25), productOf(Ten, squareRootOf(Five)))), Five),
        ),
        PiOverFour(
            fractionOf(squareRootOf(Two), Two),
            fractionOf(squareRootOf(Two), Two),
            One,
            One,
        ),
        ThreePiOverTen(
            PiOverFive.cosine,
            PiOverFive.sine,
            PiOverFive.cotangent,
            PiOverFive.tangent,
        ),
        PiOverThree(
            PiOverSix.cosine,
            PiOverSix.sine,
            PiOverSix.cotangent,
            PiOverSix.tangent,
        ),
        ThreePiOverEight(
            PiOverEight.cosine,
            PiOverEight.sine,
            PiOverEight.cotangent,
            PiOverEight.tangent,
        ),
        TwoPiOverFive(
            PiOverTen.cosine,
            PiOverTen.sine,
            PiOverTen.cotangent,
            PiOverTen.tangent,
        ),
        FivePiOverTwelve(
            PiOverTwelve.cosine,
            PiOverTwelve.sine,
            PiOverTwelve.cotangent,
            PiOverTwelve.tangent,
        ),
        PiOverTwo(
            ZeroAngle.cosine,
            ZeroAngle.sine,
            ZeroAngle.cotangent,
            ZeroAngle.tangent,
        ),
        PiAngle(
            Zero,
            MinusOne,
            Zero,
            Infinity,
        ),
        ThreePiOverTwo(
            PiAngle.cosine,
            PiAngle.sine,
            PiAngle.cotangent,
            PiAngle.tangent,
        ),
    }

    val MainAnglesRadians = mapOf(
        Zero to MainAngles.ZeroAngle,
        fractionOf(Pi, xp(12)) to MainAngles.PiOverTwelve,
        fractionOf(Pi, Ten) to MainAngles.PiOverTen,
        fractionOf(Pi, xp(8)) to MainAngles.PiOverEight,
        fractionOf(Pi, xp(6)) to MainAngles.PiOverSix,
        fractionOf(Pi, Five) to MainAngles.PiOverFive,
        fractionOf(Pi, Four) to MainAngles.PiOverFour,
        fractionOf(productOf(Three, Pi), Ten) to MainAngles.ThreePiOverTen,
        fractionOf(Pi, Three) to MainAngles.PiOverThree,
        fractionOf(productOf(Three, Pi), xp(8)) to MainAngles.ThreePiOverEight,
        fractionOf(productOf(Two, Pi), Five) to MainAngles.TwoPiOverFive,
        fractionOf(productOf(Five, Pi), xp(12)) to MainAngles.FivePiOverTwelve,
        fractionOf(Pi, Two) to MainAngles.PiOverTwo,
        Pi to MainAngles.PiAngle,
        fractionOf(productOf(Three, Pi), Two) to MainAngles.ThreePiOverTwo,
        productOf(Two, Pi) to MainAngles.ZeroAngle,
    )

    val MainAnglesDegrees = mapOf(
        degreeOf(Zero) to MainAngles.ZeroAngle,
        degreeOf(xp(15)) to MainAngles.PiOverTwelve,
        degreeOf(xp(18)) to MainAngles.PiOverTen,
        degreeOf(xp(22.5)) to MainAngles.PiOverEight,
        fractionOf(degreeOf(xp(45)), xp(2)) to MainAngles.PiOverEight,
        degreeOf(xp(30)) to MainAngles.PiOverSix,
        degreeOf(xp(36)) to MainAngles.PiOverFive,
        degreeOf(xp(45)) to MainAngles.PiOverFour,
        degreeOf(xp(54)) to MainAngles.ThreePiOverTen,
        degreeOf(xp(60)) to MainAngles.PiOverThree,
        degreeOf(xp(67.5)) to MainAngles.ThreePiOverEight,
        degreeOf(xp(72)) to MainAngles.TwoPiOverFive,
        degreeOf(xp(75)) to MainAngles.FivePiOverTwelve,
        degreeOf(xp(90)) to MainAngles.PiOverTwo,
        degreeOf(xp(180)) to MainAngles.PiAngle,
        degreeOf(xp(270)) to MainAngles.ThreePiOverTwo,
        degreeOf(xp(360)) to MainAngles.ZeroAngle,
    )
}
