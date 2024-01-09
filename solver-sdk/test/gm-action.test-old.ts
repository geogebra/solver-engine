// DEPRECATED - will be removed soon

import { describe, expect, it } from 'vitest';
// If this file doesn't exist, you need to run the kotlin tests in the methods package
import { testResults } from '../../solver-poker/test-results-src/test-results';
import type { ExpressionTree, TransformationJson } from '../src';
import { addFullFromExprToTransformation, getInnerSteps, treeToLatex } from '../src';
import { solverPathToGmNodes } from '../src/graspable-math/solver-path-to-gm-nodes';
import * as path from 'path';
import * as prettier from 'prettier';
import requireFromUrl from 'require-from-url/sync';
import { config } from 'dotenv';
import { JSDOM } from 'jsdom';
import { GmMathNode } from '../src/graspable-math/create-path-map';
import {
  expressionSkipList,
  expressionTypeSkipList,
  ruleSkipList,
} from './gm-action-test-filters';

global.it = it;
// Make it so values like `process.env.GMATH_PATH` are populated from the `.env` file, if
// you have one, which you don't need to.
config({ path: path.resolve(__dirname, '../.env') });
expect(typeof document).to.not.equal(
  'undefined',
  'This failure is caused by not running vitest with the `--environment jsdom` option. If you ran the command from the @graspable/solver-sdk directory, then this is done for you automatically, via the config in @geogebra/solver-sdk/vite.config.ts . Otherwise, you can just add `--environment jsdom` onto the command you ran. `jsdom` is a tool that can stub things like `document` into the nodejs environment. `document` is needed by the GM library.',
);
const dom = new JSDOM(
  /* HTML */ ` <script>
      window.gmathPromise = new Promise((resolve, reject) => {
        window.gmathPromiseResolve = resolve;
      });
    </script>
    <script src="${process.env.GMATH_PATH ??
      'https://graspablemath.com/shared/libs/gmath-dist/gmath-3.0.0.min.js'}"></script>
    <script>
      window.gmathPromiseResolve(window.gmath);
    </script>`,
  { runScripts: 'dangerously', resources: 'usable', url: import.meta.url },
);
const gmath = await dom.window.gmathPromise;
global.gmath = gmath;
let when;
const { WHEN_FOR_PATH } = process.env;
if (WHEN_FOR_PATH) {
  when = (await import(WHEN_FOR_PATH)).when;
} else {
  when = requireFromUrl(
    'https://graspablemath.com/shared/libs/gmath-dist/when-for-actions-latest.js',
  ).when;
}

describe('gmAction tests', () => {
  for (const testResult of testResults) {
    const transformation: TransformationJson = testResult.transformation;
    if (!transformation) continue;
    const augmentedTransformation = addFullFromExprToTransformation(transformation);
    for (const step of getInnerSteps(augmentedTransformation)) {
      const rawGmAction = step.gmAction;
      if (!rawGmAction) {
        dummyTest();
        continue;
      }
      const fromExprTree = step.fullFromExpr;
      if (
        hasExpressionType(step.fullFromExpr, expressionTypeSkipList) ||
        hasExpressionType(step.fullToExpr, expressionTypeSkipList)
      ) {
        dummyTest();
        continue;
      }
      const fromExpr = treeToLatex(fromExprTree);
      let stepDescription = JSON.stringify(
        step.steps?.length ? { ...step, steps: 'purposely not shown' } : step,
      );
      stepDescription = prettier.format(stepDescription, { filepath: 'dummy.json5' });
      describe(
        `${testResult.testClassName}.${testResult.testName}.` +
          // stepDescription +
          `
Key: ${step.explanation.key}
GmActionInfo: ${JSON.stringify(step.gmAction, null, 2)}
`,
        () => {
          // These are just here so that V8 doesn't optimize away access to the variables,
          // so that we can look at the values of them in the debugger.
          transformation;
          testResult;

          if (
            // Uncomment below line and comment out the other conditions to debug a single test case
            // step.explanation.key !== 'General.RemoveRedundantPlusSign'
            ruleSkipList.includes(step.explanation.key)
          )
            return dummyTest();

          const fromExprGm = new gmath.AlgebraModel(fromExpr) as GmMathNode;
          const fromExprAscii = fromExprGm.to_ascii();

          ////////////////////////////////////////////////////////////////////////
          // Uncomment this to focus on a specific test
          ////////////////////////////////////////////////////////////////////////
          // if (fromExprAscii !== '2*y^2=7') {
          //   dummyTest();
          //   return;
          // }

          if (expressionSkipList.includes(fromExprAscii)) return dummyTest();

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
          let chain = when({ gm: { simplify_after_drag_across_relation: false } });
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
              // Delete this comment after we get at least one of these tests to work: See
              // graspable-math/test/unit-tests/math-engine/expressions/rewrite-test.js line
              // 278 to see the example I emulated to write this.
              chain
                .rewriting(fromExprAscii)
                .moving(actorsSelector.split(';')[0])
                .and(actorsSelector.split(';')[1])
                .outside(targetsSelector)
                .gives(fullToExpr)();
              break;
            case 'Edit':
              // Todo: un-ignore this once David asks Erik the following question: How is GM
              // supposed to know what to edit the expression to? Right now Solver says that
              // an "edit" action should be done on a given sub-expression but Solver doesn't
              // say what the sub-expression should be edited to.
              dummyTest();
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
              dummyTest();
              break;
            default:
              throwUnknownTypeError(gmAction.type);
          }
        },
      );
    }
  }
});

function throwUnknownTypeError(action: never) {
  throw new Error(`Unknown action type: ${action}`);
}

/** Call this when we are not ready to make a full-fledged test yet, but vitest will
 * complain if there is a `describe` block that doesn't have an `it` in it. Also having a
 * skipped test here will make it more obvious that there is still more work that needs to
 * be done to make a proper passing test. */
function dummyTest() {
  it.skip('Go back and fix this test case.', () => {
    throw new Error();
  });
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

// // for use when debugging
// global.treeToLatex = treeToLatex;
// global.treeToAscii = treeToAscii;
// function treeToAscii(tree): string {
//   return new gmath.AlgebraModel(treeToLatex(tree)).to_ascii();
// }

// // For debugging. Just uncomment and look at the browser console.
// import { latexToTree, treeToJson } from '../src/index';
// import { createPathMap } from '../src/graspable-math-related/create-path-map';

// window['runTest'] = runTest;
// window['treeToLatex'] = treeToLatex;
// window['latexToTree'] = latexToTree;
// window['treeToJson'] = treeToJson;

// /* Test cases that print path augmentation to the console. This needs the gmath
//  * library to work. */
// runTest('-1');
// runTest('2a-b');
// runTest('\\frac{1}{2x}');
// runTest('x^{2+3}');
// runTest('\\sqrt{3x}');
// runTest('\\sqrt[3+1]{2x}');
// runTest('x<2');
// runTest('\\frac{1}{2}+4+\\frac{1}{2}');
// runTest('\\left{\\left[\\left(x\\right)\\right]\\right}');
// // This one isn't working because we haven't implemented parsing LaTeX "|" yet.
// runTest('\\left|x\\right|');

// function runTest(latex: string) {
//   const tree = latexToTree(latex);
//   const gmTree = new gmath.AlgebraModel(latex).children[0];
//   const map = createPathMap(gmTree, tree);
//   for (const [path, nodeList] of map.entries()) {
//     for (const node of nodeList) console.log(`${node.to_ascii()} \t${path}`);
//   }
//   console.log('---');
// }
