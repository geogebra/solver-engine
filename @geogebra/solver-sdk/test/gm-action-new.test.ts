const VERBOSE = false;

import { describe, it } from 'vitest';
// If this file doesn't exist, you need to run the kotlin tests in the methods package
import testResults from '../../../methods/build/test-results/gmActionTests.json';
import type {
  ExpressionTree,
  MathJson,
  TransformationJson,
  TransformationWithFullFromExpr,
} from '../src';
import {
  addFullFromExprToTransformation,
  getInnerSteps,
  jsonToLatex,
  treeToLatex,
} from '../src';
import { solverPathToGmNodes } from '../src/graspable-math/solver-path-to-gm-nodes';

import { GmMathNode } from '../src/graspable-math/create-path-map';
import { loadGmEnvironment } from './gm-tools';
const { gmath, when } = await loadGmEnvironment();
gmath.MathSettings.defaults.set('dont_pass_through_minus_when_dividing', true);
gmath.MathSettings.defaults.set('cancel_common_terms_after_finding_gcf', true);
gmath.SettingsType.get('AlgebraModel').defaults.set('auto_simplify_distributions', false);

type TransformationJsonWithGmToExpr = TransformationJson & {
  gmToExpr?: MathJson;
};

function hasTaskSet(t: TransformationJson): boolean {
  return t.type === 'TaskSet' || !!t.steps?.some(hasTaskSet);
}

function jsonToString(json: MathJson) {
  return jsonToLatex(json).replaceAll('\\left', '').replaceAll('\\right', '');
}

function stepToString(step: TransformationJson) {
  return `${step.explanation.key} ${jsonToString(step.fromExpr)} ===> ${jsonToString(
    step.toExpr,
  )}`;
}

const unsupportedExpressionTypes = [
  'MixedNumber',
  'Inequation',
  'RecurringDecimal',
  'ExpressionWithConstraint',
  'Plus',
];

describe('GM - Solver Integration Tests', () => {
  for (const testResult of testResults) {
    const transformation = testResult.transformation as TransformationJsonWithGmToExpr;
    if (!transformation) continue;
    applyGmToExprs(transformation);
    describe(`${testResult.testName}: ${stepToString(transformation)}`, () =>
      testTransformation(testResult.context.settings, transformation));
  }
});

function testTransformation(
  settings: Record<string, string>,
  transformation: TransformationJson,
) {
  if (hasTaskSet(transformation)) {
    it("Doesn't contain TaskSets, which are not supported.", () => {
      throw new Error("TaskSets aren't supported.");
    });
    return;
  }
  const augmentedTransformation = addFullFromExprToTransformation(transformation);
  for (const step of getInnerSteps(augmentedTransformation)) {
    const isPedantic = step.tags?.includes('Pedantic');
    if (isPedantic) {
      it(`Step ${step.explanation.key} - ignore since it is pedantic`, () => true);
    } else describe(`Step ${step.explanation.key}`, () => testStep(settings, step));
  }
}

/** We can't just change the 'toExpr' to be 'gmToExpr', because
 * `addFullFromExprToTransformation` actually relies on 'fromExpr' to
 * back-substitute up the hierarchical step structure. This is hacky
 * and might not work for all situations. Maybe we shouldn't allow
 * 'gmToExpr'. */
function applyGmToExprs(step: TransformationJsonWithGmToExpr, fromExpr?: MathJson) {
  fromExpr = fromExpr || step.fromExpr;
  step.fromExpr = fromExpr;
  step.toExpr = step.gmToExpr || step.toExpr;
  step.steps?.forEach((innerStep, index) => {
    if (index === 0) {
      if (innerStep.path === step.path) applyGmToExprs(innerStep, fromExpr);
    } else {
      const prevStep = step.steps[index - 1];
      applyGmToExprs(innerStep, prevStep.toExpr);
    }
  });
  const lastStep = step.steps?.[step.steps.length - 1];
  if (lastStep && lastStep.path === step.path) {
    step.toExpr = lastStep.toExpr;
  }
}

function testStep(
  settings: Record<string, string>,
  step: TransformationWithFullFromExpr,
) {
  const rawGmAction = step.gmAction;
  if (!rawGmAction) {
    it('Defines a gmAction.', () => {
      throw new Error('Missing gmAction');
    });
    return;
  } else if (VERBOSE) {
    console.log(jsonToString(step.fromExpr));
    console.log(JSON.stringify(rawGmAction, null, 2));
  }

  const fromExprTree = step.fullFromExpr;
  if (hasExpressionType(step.fullFromExpr, unsupportedExpressionTypes)) {
    it(`Uses only supported expression types: ${JSON.stringify(
      step.fullFromExpr,
    )}`, () => {
      throw new Error('Unsupported expression type');
    });
    return;
  }

  const fromExpr = treeToLatex(fromExprTree);
  let fromExprGm;
  try {
    fromExprGm = new gmath.AlgebraModel(fromExpr) as GmMathNode;
  } catch (error) {
    it(`GM can parse "${fromExpr}" in GM`, () => {
      throw error;
    });
    return;
  }
  const fromExprAscii = fromExprGm.to_ascii();
  const fullToExpr = treeToLatex(step.fullToExpr);
  const { actors, gmAction, targets } = solverPathToGmNodes(
    fromExprGm,
    fromExprTree,
    rawGmAction,
  );

  // This setting controls whether GM is automatically simplifying after dragging to apply
  // an operation to both sides of an equation.
  // On: 2x=4 ===> x=4/2
  // Off: 2x=4 ===> (2x)/2=4/2
  const advancedBalancing = settings.AdvancedBalancing === 'true';
  let chain = when({
    gm: { simplify_after_drag_across_relation: advancedBalancing },
  });
  const actorsSelector = getGmSelector(fromExprGm, actors);
  const targetsSelector = getGmSelector(fromExprGm, targets);
  switch (gmAction.type) {
    case 'Drag':
      chain = chain.rewriting(fromExprAscii).moving(actorsSelector);
      switch (gmAction.dragTo.position) {
        case 'Above':
          chain = chain.above(targetsSelector);
          break;
        case 'Below':
          chain = chain.below(targetsSelector);
          break;
        case 'LeftOf':
          chain = chain.left_of(targetsSelector);
          break;
        case 'RightOf':
          chain = chain.right_of(targetsSelector);
          break;
        case 'Onto':
          chain = chain.onto(targetsSelector);
          break;
        case 'OutsideOf':
          chain = chain.outside_of(targetsSelector);
          break;
        default:
          throwUnknownTypeError(gmAction.dragTo.position);
      }
      // otherTargets.forEach((target) => (whenFor = whenFor.and(target)));
      // finish_interaction is run automatically in GM and will sometimes do
      // automatic cleanup work that we rely on here
      chain.gives().finish_interaction().gives(fullToExpr)();
      break;
    case 'DoubleTap':
      chain
        .simplifying(fromExprAscii)
        .selecting(actorsSelector, 'dbltap')
        .gives()
        .finish_interaction()
        .gives(fullToExpr)();
      break;
    case 'Tap':
      chain
        .simplifying(fromExprAscii)
        .selecting(actorsSelector)
        .gives()
        .finish_interaction()
        .gives(fullToExpr)();
      break;
    case 'DragCollect':
      chain
        .rewriting(fromExprAscii)
        .moving(actorsSelector.split(';')[0])
        .and(actorsSelector.split(';')[1])
        .outside(targetsSelector)
        .gives(fullToExpr)();
      break;
    case 'Edit':
      it('Uses "edit", which always works.', () => true);
      break;
    case 'Formula':
      chain
        .rewriting(fromExprAscii)
        .applying(formulaMap[gmAction.formulaId])
        .onto(actorsSelector)
        .gives(fullToExpr)();
      break;
    case 'TapHold':
      throw new Error('Not implemented, yet');
    case 'NotSupported':
      it.skip(`Step's gmAction was explicitly marked as NotSupported.`, () => {
        throw new Error();
      });
      break;
    default:
      throwUnknownTypeError(gmAction.type);
  }
}

function throwUnknownTypeError(action: never) {
  throw new Error(`Unknown action type: ${action}`);
}

function getGmSelector(rootExpression: GmMathNode, subExpressions: GmMathNode[]) {
  return (
    rootExpression
      .getSelectorOfNodes(subExpressions)
      .split(';')
      .map((it) => {
        const tokens = it.split(':');
        if (tokens.length > 1) {
          const index = +tokens[1] + 1;
          return tokens[0] + ':' + index;
        }
        return it;
      })
      // .filter((it) => !!it) // Sometimes we have empty selectors, we may want to find the origin of that
      .join(';')
  );
}

const formulaMap = { 'Difference of Squares': '(a+b)*(a-b)' };

function searchExpressionTree(
  expr: ExpressionTree,
  fn: (expr: ExpressionTree) => boolean,
) {
  if (fn(expr)) return expr;
  if ('operands' in expr) {
    return expr.operands.find((arg) => searchExpressionTree(arg, fn));
  }
  return undefined;
}

function hasExpressionType(
  fullFromExpr: ExpressionTree,
  expressionTypeSkipList: string[],
) {
  for (const expressionType of expressionTypeSkipList) {
    if (searchExpressionTree(fullFromExpr, (expr) => expr.type === expressionType))
      return true;
  }
  return false;
}

// for use when debugging
global.treeToLatex = treeToLatex;
global.treeToAscii = treeToAscii;
function treeToAscii(tree): string {
  return new gmath.AlgebraModel(treeToLatex(tree)).to_ascii();
}
