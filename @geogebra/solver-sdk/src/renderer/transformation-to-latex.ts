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

import { ExpressionTree, LatexSettings } from '../parser';
import { treeToLatex } from '../index';

/**
 * Render a transformation in one special latex expression if it is possible
 *
 * @param fromExpr
 * @param toExpr
 * @param settings
 */
export function specialTransformationLatex(
  fromExpr: ExpressionTree,
  toExpr: ExpressionTree,
  settings: LatexSettings,
): string | null {
  if (fromExpr.type === 'AddEquations' || fromExpr.type === 'SubtractEquations') {
    // This is the case when we add / subtract equations.  It can be rendered in column format
    // E.g.
    //   x + y = 2 | +
    //   x - y = 3 |
    //   ----------+--
    //      2x = 5
    const [eq1, eq2] = fromExpr.operands;
    const alignSetting = { ...settings, align: true };
    const operator = fromExpr.type === 'AddEquations' ? '+' : '-';
    return (
      '\\begin{array}{rcl|l}\n' +
      '  ' +
      treeToLatex(eq1, alignSetting) +
      ` & ${operator} \\\\\n` +
      '  ' +
      treeToLatex(eq2, alignSetting) +
      ' & \\\\ \\hline \n' +
      '  ' +
      treeToLatex(toExpr, alignSetting) +
      ' \\\\\n' +
      '\\end{array}'
    );
  } else {
    return null;
  }
}
