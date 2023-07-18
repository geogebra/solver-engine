import type { ExpressionTree } from '../parser';
import type { GmAction } from '../types';
import { createPathMap } from './create-path-map';
import type { GmMathNode } from './create-path-map';

export function solverPathToGmNodes(
  gmTree: GmMathNode,
  exprTree: ExpressionTree,
  info: GmAction,
): { actors: GmMathNode[]; targets: GmMathNode[]; gmAction: GmAction } {
  if (gmTree.type === 'AlgebraModel') gmTree = gmTree.children[0];
  const map = createPathMap(gmTree, exprTree);

  let gmAction = info;
  let actorPaths = info.expressions || [];

  // Sometimes the * operator that should be tapped is implicit, in these cases we can
  // double tap the parenthesis
  if (gmAction.type === 'DoubleTap') {
    actorPaths = actorPaths.map((it) => {
      if (it.includes('op()') && !map.has(it)) {
        return it.replace('op()', '(');
      }
      return it;
    });
  }

  const targetPaths = info.dragTo ? [info.dragTo.expression] : [];

  const actors = actorPaths.flatMap((path) => map.get(path) || []);
  const targets = targetPaths.flatMap((path) => map.get(path) || []);

  // Instead of dragging `2` onto the `1` in `1+2`, tap the `+`.
  if (
    actors.length === 1 &&
    targets.length === 1 &&
    info.type === 'Drag' &&
    (!info.dragTo?.position || info.dragTo.position === 'Onto')
  ) {
    let changedToTap = false;
    const actor = actors[0];
    let target = targets[0];
    // The Solver puts negatives on the first factor around the whole product,
    // so in a situation like -2*x*3, the target will be "2" instead of "-2".
    // We need to change that to "-2" to make the drag work.
    if (target.parent?.is_group('sign')) target = target.parent;

    if (areAdjacentAddendsOrFactors(actor, target)) {
      const operators = map.get(`${actorPaths[0]}:op`);
      if (operators && !operators[0].hidden) {
        changedToTap = true;
        actors[0] = operators[0];
        targets.splice(0);
        gmAction = { ...info, type: 'Tap' };
      }
    }
    // In a case like `2x+y+3x`, in GM we need to pick up the `+3x` and drag it onto
    // `+2x`, not just the `3x`. We'll check for that and expand the actors and targets
    // to the parent add block. The same applies to `x^2*3*x^3` where we need to drag the
    // mul blocks, not just the powers.
    if (!changedToTap) {
      const actorsParentParent = actor.parent?.parent;
      const targetParentParent = target.parent?.parent;

      // If dragging a addend onto another addend in the same sum, or dragging a factor
      // onto another factor in the same product...
      if (
        actorsParentParent === targetParentParent &&
        actorsParentParent?.is_group('sum', 'product')
      ) {
        // ...then we need to drag the add node or the mul node instead.
        actors[0] = actor.parent as GmMathNode;
        targets[0] = target.parent as GmMathNode;
      }
    }
  }
  return { actors, targets, gmAction };
}

/** Checks if the passed gm terms are addends or factors that are next to each
 * other in the same sum or product or fraction. This is useful to know since if
 * they are adjacent, combining the two terms can be done by clicking the
 * operator between them. */
function areAdjacentAddendsOrFactors(n1: GmMathNode, n2: GmMathNode): boolean {
  if (!n1 || !n2) return false;
  if (n1.parent?.is_group('add', 'sub', 'addsub', 'mul', 'div')) n1 = n1.parent;
  if (n2.parent?.is_group('add', 'sub', 'addsub', 'mul', 'div')) n2 = n2.parent;
  if (!n1.is_group('add', 'sub', 'addsub', 'mul', 'div')) return false;
  if (!n2.is_group('add', 'sub', 'addsub', 'mul', 'div')) return false;
  return n1 === n2.rs || n1 === n2.ls;
}
