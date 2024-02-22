/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
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

package methods.logs

import engine.methods.testRule
import org.junit.jupiter.api.Test

class LogsRulesTest {
    @Test
    fun testTakePowerOutOfLog() {
        testRule("ln[x ^ 2]", LogsRules.TakePowerOutOfLog, "2 ln x")
        testRule("log_[2][3 ^ x]", LogsRules.TakePowerOutOfLog, "x log_[2] 3")
        testRule("log[5^(n + 1)]", LogsRules.TakePowerOutOfLog, "(n + 1)log 5")
    }

    @Test
    fun testEvaluateLogOfBase() {
        testRule("log_[2] 2", LogsRules.EvaluateLogOfBase, "1")
        testRule("ln /e/", LogsRules.EvaluateLogOfBase, "1")
        testRule("log 10", LogsRules.EvaluateLogOfBase, "1")
        testRule("log /e/", LogsRules.EvaluateLogOfBase, null)
        testRule("ln 10", LogsRules.EvaluateLogOfBase, null)
    }

    @Test
    fun testEvaluateLogOfOne() {
        testRule("log_[a] 1", LogsRules.EvaluateLogOfOne, "0")
        testRule("ln 1", LogsRules.EvaluateLogOfOne, "0")
        testRule("log 1", LogsRules.EvaluateLogOfOne, "0")
    }

    @Test
    fun testEvaluateLogOfNonPositiveAsUndefined() {
        testRule("log_[3](-2)", LogsRules.EvaluateLogOfNonPositiveAsUndefined, "/undefined/")
        testRule("ln(-[x^2])", LogsRules.EvaluateLogOfNonPositiveAsUndefined, "/undefined/")
        testRule("ln(-[x^3])", LogsRules.EvaluateLogOfNonPositiveAsUndefined, null)
        testRule("log 0", LogsRules.EvaluateLogOfNonPositiveAsUndefined, "/undefined/")
    }

    @Test
    fun testSimplifyLogOfReciprocal() {
        testRule("ln[1 / x]", LogsRules.SimplifyLogOfReciprocal, "-ln x")
        testRule("log[1 / x - 1]", LogsRules.SimplifyLogOfReciprocal, "-log(x - 1)")
        testRule("log_[a][1 / 5]", LogsRules.SimplifyLogOfReciprocal, "-log_[a] 5")
    }

    @Test
    fun testRewriteLogOfKnownPower() {
        testRule("ln 25", LogsRules.RewriteLogOfKnownPower, "ln[5 ^ 2]")
        testRule("ln 128", LogsRules.RewriteLogOfKnownPower, null)
        testRule("log 10000", LogsRules.RewriteLogOfKnownPower, "log[10 ^ 4]")
        testRule("log_[3] 8000", LogsRules.RewriteLogOfKnownPower, "log_[3][20 ^ 3]")
        testRule("log_[2] 10", LogsRules.RewriteLogOfKnownPower, null)
    }
}
