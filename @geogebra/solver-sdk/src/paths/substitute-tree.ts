import type { ExpressionTree } from '../parser/types';
import { arrayToPath } from './array-to-path';

/** Replace the subtree at the given path in `destination` with the given `substitution`
 * tree. By default, this will use the destination's decorators and omit the
 * substitution's decorators. Will automatically update the paths of the resulting tree.
 *
 * This method is meant to be used with filling in sub-steps of a solver solution into the
 * destination steps. For example, in "2*(3+4)", the inner step will have the fromExpr
 * "2+4". To show the entire fromExpr, we need to substitute it into the destination
 * expression at the right path. For fromExpr, we always need to keep the destination
 * decorators, but for toExpr it depends on the mathematical situation. Luckily, we can
 * just use the next step's fromExpr instead.
 */
export function substituteTree(
  destination: ExpressionTree,
  substitution: ExpressionTree,
  pathArray: number[],
  useDestinationsDecorators = true,
): ExpressionTree {
  function inner(
    subDestination: ExpressionTree,
    pathIdx = 0,
    parentDestination: ExpressionTree | undefined = undefined,
  ): ExpressionTree {
    if (pathArray.length === pathIdx) {
      const prefixedSubstitution = prefixPaths(substitution, arrayToPath(pathArray));
      if (
        !substitution.decorators?.length &&
        parentDestination?.type === substitution.type &&
        ['SmartProduct', 'Sum'].includes(substitution.type)
      ) {
        return { ...prefixedSubstitution, decorators: ['PartialBracket'] };
      }
      if (useDestinationsDecorators) {
        return {
          ...prefixedSubstitution,
          ...(subDestination.decorators?.length
            ? { decorators: subDestination.decorators.slice() }
            : null),
        };
      }
      return prefixedSubstitution;
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
