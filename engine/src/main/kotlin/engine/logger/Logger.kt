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

package engine.logger

import java.util.function.Supplier
import java.util.logging.Level

interface Logger {
    fun log(level: Level, depth: Int, string: String)

    fun <T> log(level: Level, depth: Int, supplier: Supplier<T>)
}

/**
 * A logger instance that does nothing
 */
object DefaultLogger : Logger {
    override fun log(level: Level, depth: Int, string: String) {
        // Do nothing
    }

    override fun <T> log(level: Level, depth: Int, supplier: Supplier<T>) {
        // Do nothing
    }
}
