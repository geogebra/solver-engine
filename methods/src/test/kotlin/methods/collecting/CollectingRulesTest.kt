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

package methods.collecting

import engine.methods.testRule
import methods.collecting.CollectingRules.CollectLikeRoots
import methods.collecting.CollectingRules.CollectLikeTerms
import methods.collecting.CollectingRules.CombineTwoSimpleLikeTerms
import org.junit.jupiter.api.Test

class CollectingRulesTest {
    @Test
    fun testCollectLikeRoots() {
        testRule("sqrt[2] + 2*sqrt[2] + 2", CollectLikeRoots, "(1 + 2)sqrt[2] + 2")
        testRule("sqrt[3] + sqrt[3]", CollectLikeRoots, "(1 + 1)sqrt[3]")
        testRule(
            "sqrt[3] + sqrt[7] + sqrt[3] + sqrt[5]",
            CollectLikeRoots,
            "(1 + 1)  sqrt[3] + sqrt[7] + sqrt[5]",
        )
        testRule(
            "sqrt[7] + sqrt[3] + sqrt[3] + sqrt[5]",
            CollectLikeRoots,
            "sqrt[7] + (1 + 1)  sqrt[3] + sqrt[5]",
        )
        testRule(
            "sqrt[7] + root[5, 3] + sqrt[5] + root[5, 3]",
            CollectLikeRoots,
            "sqrt[7] + (1 + 1)  root[5, 3] + sqrt[5]",
        )
        testRule(
            "[2 * sqrt[2] / 3] + [1 / 5] * sqrt[2] - 4 * sqrt[2]",
            CollectLikeRoots,
            "([2 / 3] + [1 / 5] - 4)  sqrt[2]",
        )
    }

    @Test
    fun testCollectLikeTerms() {
        testRule("x + x", CollectLikeTerms, "(1+1) x")
        testRule("2*y - 3*y", CollectLikeTerms, "(2 - 3) y")
        testRule("z + [1/2]*z + [z / 2] - z*3", CollectLikeTerms, "(1 + [1/2] + [1/2] - 3) z")
        testRule("t*sqrt[3] + 2*t - [t*sqrt[2]/2]", CollectLikeTerms, "(sqrt[3] + 2 - [sqrt[2]/2]) t")
        // the factors should be simplified first
        testRule("3xy*y + 2xy*y", CollectLikeTerms, null)
        // should use fraction addition
        testRule("[[x^2] + x / 2] + 3x + [[x^2] + x / 2]", CollectLikeTerms, null)
    }

    @Test
    fun testCombineTwoSimpleLikeTerms() {
        testRule("x + x", CombineTwoSimpleLikeTerms, "2x")
        testRule(
            "x - x",
            CombineTwoSimpleLikeTerms,
            "0x",
        ) // cancel opposite terms will overrule this with a result of 0
        testRule(
            "4x - 4x",
            CombineTwoSimpleLikeTerms,
            "0x",
        ) // cancel opposite terms will overrule this with a result of 0
        testRule("1+2x-3+5x", CombineTwoSimpleLikeTerms, "1+7x-3")
        testRule("2*y+y", CombineTwoSimpleLikeTerms, "3y")
        testRule("1+2a-3a+1", CombineTwoSimpleLikeTerms, "1-a+1")
        testRule("1-2a-3a+1", CombineTwoSimpleLikeTerms, "1-5a+1")
        testRule("1-2a+3a+1", CombineTwoSimpleLikeTerms, "1+a+1")
        testRule("z + [1/2]*z + [z / 2] - 3z", CombineTwoSimpleLikeTerms, "-2z + [1/2]*z + [z/2]")
        // Someday come back to this example, it should work
        // t("z + [1/2]*z + [z / 2] - z*3", "-2z + [1/2]*z + [z/2]")
        testRule("z + [1/2]*z + [z / 2] - z*3", CombineTwoSimpleLikeTerms, null)
        testRule("t*sqrt[3] + 2*t - [t*sqrt[2]/2]", CombineTwoSimpleLikeTerms, null)
    }
}
