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

/** Takes a solver path of the format like this './2/1' and turns it into an
 * array like [2,1]. */
export function pathToArray(path: string): number[] {
  if (path === '.') return [];
  if (path.startsWith('./')) path = path.substring(2);
  return path.split('/').map((el) => +el);
}
