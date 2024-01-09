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

import type { ExpressionTree } from '../parser/types';
import { arrayToPath } from './array-to-path';

/** Replace the subtree at the given path in `destination` with the given
 * `substitute` tree, using the substitute's decorators. Will automatically
 * update the paths of the resulting tree.
 *
 * This method is meant to be used with filling in sub-steps of a solver
 * solution into the destination steps. For example, in "2*(3+4)", the inner
 * step will have the fromExpr "(3+4)". To show the entire fromExpr, we need to
 * substitute it into the destination expression at the right path. The solver
 * includes any brackets that are needed when substituting the sub-steps.
 */
export function substituteTree(
  destination: ExpressionTree,
  substitute: ExpressionTree,
  pathArray: number[],
): ExpressionTree {
  function inner(
    subDestination: ExpressionTree,
    pathIdx = 0,
    parentDestination: ExpressionTree | undefined = undefined,
  ): ExpressionTree {
    if (pathArray.length === pathIdx) {
      const prefixedSubstitute = prefixPaths(substitute, arrayToPath(pathArray));
      if (
        !substitute.decorators?.length &&
        parentDestination?.type === substitute.type &&
        ['SmartProduct', 'Sum'].includes(substitute.type)
      ) {
        return { ...prefixedSubstitute, decorators: ['PartialBracket'] };
      }
      return prefixedSubstitute;
    }
    const index = pathArray[pathIdx];
    if (!('operands' in subDestination) || index >= subDestination.operands.length) {
      // these don't have subtrees, so the path should have had length 0
      throw new Error('Invalid path');
    }
    return {
      ...subDestination,
      operands: subDestination.operands.map((arg, i) => {
        if (i === index) {
          return inner(arg, pathIdx + 1, subDestination);
        } else return arg;
      }),
    };
  }

  return inner(destination);
}

function prefixPaths(tree: ExpressionTree, prefix = ''): ExpressionTree {
  return {
    ...tree,
    path: prefix + tree.path.slice(1),
    ...('operands' in tree
      ? { operands: tree.operands.map((arg) => prefixPaths(arg, prefix)) }
      : null),
  };
}
