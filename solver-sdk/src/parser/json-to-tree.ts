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

import { MathJson } from '../types';
import { ExpressionTree } from './types';

export function jsonToTree(json: MathJson, path = '.'): ExpressionTree {
  if ('operands' in json) {
    return {
      ...json,
      operands: json.operands
        ? json.operands.map((op, i) => jsonToTree(op, `${path}/${i}`))
        : [],
      path,
    };
  } else {
    return { ...json, path };
  }
}

export function treeToJson(tree: ExpressionTree): MathJson {
  if ('operands' in tree) {
    const { path: _, ...json } = tree;
    return {
      ...json,
      operands: json.operands.map((op) => treeToJson(op)),
    };
  } else {
    const { path: _, ...json } = tree;
    return json;
  }
}
