import type { ExpressionTree } from '../parser/types';
import { arrayToPath } from './array-to-path';

/** Replace the subtree at the given path in `parent` with the given `child`
 * tree. By default, this will use the parent's decorators and omit the child's
 * decorators. Will automatically update the paths of the resulting tree.
 *
 * This method is meant to be used with filling in sub-steps of a solver
 * solution into the parent steps. For example, in "2*(3+4)", the inner step
 * will have the fromExpr "2+4". To show the entire fromExpr, we need to
 * substitute it into the parent expression at the right path. For fromExpr, we
 * always need to keep the parent decorators, but for toExpr it depends on the
 * mathematical situation. Luckily, we can just use the next step's fromExpr
 * instead.
 */
export function substituteTree(
  parentTree: ExpressionTree,
  childTree: ExpressionTree,
  pathArray: number[],
  useParentDecorators = true,
): ExpressionTree {
  function inner(
    parent: ExpressionTree,
    child: ExpressionTree,
    pathIdx = 0,
  ): ExpressionTree {
    if (pathArray.length === pathIdx) {
      if (!useParentDecorators) {
        const prefixedChild = prefixPaths(child, arrayToPath(pathArray));
        if (
          parent.type === child.type &&
          ['SmartProduct', 'Sum'].includes(parent.type) &&
          !parent.decorators?.length
        ) {
          // Todo: after almost every test is not skipped, try removing this `if` and see
          // if it breaks anything.
          return { ...prefixedChild, decorators: ['PartialBracket'] };
        }
        return prefixedChild;
      }
      return {
        ...prefixPaths(child, arrayToPath(pathArray)),
        ...(parent.decorators?.length ? { decorators: parent.decorators.slice() } : null),
      };
    }
    const index = pathArray[pathIdx];
    if (!('args' in parent) || index >= parent.args.length) {
      // these don't have subtrees, so the path should have had length 0
      throw new Error('Invalid path');
    }
    return {
      ...parent,
      args: parent.args.map((arg, i) => {
        if (i === index) {
          return inner(arg, child, pathIdx + 1);
        } else return arg;
      }),
    };
  }
  return inner(parentTree, childTree);
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
