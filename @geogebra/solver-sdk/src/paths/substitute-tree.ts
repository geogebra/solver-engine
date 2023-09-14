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
    if (!('args' in subDestination) || index >= subDestination.args.length) {
      // these don't have subtrees, so the path should have had length 0
      throw new Error('Invalid path');
    }
    return {
      ...subDestination,
      args: subDestination.args.map((arg, i) => {
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
    ...('args' in tree
      ? { args: tree.args.map((arg) => prefixPaths(arg, prefix)) }
      : null),
  };
}
