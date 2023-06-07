import { it, describe, expect } from 'vitest';
// If this file doesn't exist, you need to run the kotlin tests in the methods package
import { testResults } from '../../../solver-poker/test-results-src/test-results';
import type { TransformationJson } from '../src';
import { treeToLatex, getInnerSteps, addFullFromExprToTransformation } from '../src';
import { solverPathToGmNodes } from '../src/graspable-math/solver-path-to-gm-nodes';
import * as path from 'path';
import * as prettier from 'prettier';
import requireFromWeb from 'require-from-web';
// `requireFromWeb` only works to fetch one of the files and `requireFromUrl` only works
// for the other. (The gmath library can't be loaded with `requireFromUrl` on Windows
// probably because, internally, it depends on piping the contents of the file on the
// command line and there is a limit to how long piped text can be, I guess. It looks like
// gmath is too long.)
import requireFromUrl from 'require-from-url/sync';
import { config } from 'dotenv';

global.it = it;
// Make it so values like `process.env.GMATH_PATH` are populated from the `.env` file, if
// you have one, which you don't need to.
config({ path: path.resolve(__dirname, '../.env') });
let gmath;
expect(typeof document).to.not.equal(
  'undefined',
  'This failure is caused by not running vitest with the `--environment jsdom` option. If you ran the command from the @graspable/solver-sdk directory, then this is done for you automatically, via the config in @geogebra/solver-sdk/vite.config.ts . Otherwise, you can just add `--environment jsdom` onto the command you ran. `jsdom` is a tool that can stub things like `document` into the nodejs environment. `document` is needed by the GM library.',
);
const { GMATH_PATH } = process.env;
if (GMATH_PATH) {
  gmath = await import(GMATH_PATH);
} else {
  gmath = await requireFromWeb(
    'https://graspablemath.com/shared/libs/gmath-dist/gmath.min.js',
  );
}
let when;
const { WHEN_FOR_PATH } = process.env;
if (WHEN_FOR_PATH) {
  when = (await import(WHEN_FOR_PATH)).when;
} else {
  when = requireFromUrl(
    'https://graspablemath.com/shared/libs/gmath-dist/when-for-actions.js',
  ).when;
}
global.gmath = gmath;

describe('gmAction tests', () => {
  for (const testResult of testResults) {
    const transformation: TransformationJson = JSON.parse(
      testResult.transformationJsonMath,
    );
    if (!transformation) continue;
    const augmentedTransformation = addFullFromExprToTransformation(transformation);
    for (const step of getInnerSteps(augmentedTransformation)) {
      const rawGmAction = step.gmAction;
      if (!rawGmAction) {
        dummyTest();
        continue;
      }
      const fromExprTree = step.fullFromExpr;
      const fromExpr = treeToLatex(fromExprTree);
      // Uncomment this to focus on a specific test
      // if (fromExpr !== 'x+y = z+1+x') continue;
      let stepDescription = JSON.stringify(
        step.steps?.length ? { ...step, steps: 'purposely not shown' } : step,
      );
      stepDescription = prettier.format(stepDescription, { filepath: 'dummy.json5' });
      // todo add comment
      describe(`${testResult.testClassName}.${testResult.testName}. step: ${stepDescription}`, () => {
        if (fromExpr.indexOf('\\div') !== -1) {
          // Todo: get rid of this skip
          it.skip(`This test is here to fail to make it obvious that GM can't parse the \\div in ${fromExpr}`, () => {
            throw new Error();
          });
          return;
        }
        if (fromExpr.indexOf('\\leq') !== -1 || fromExpr.indexOf('\\geq') !== -1) {
          // Todo: get rid of this skip
          it.skip(`This test is here to fail to make it obvious that GM can't parse the \\leq or \\geq in ${fromExpr}`, () => {
            throw new Error();
          });
          return;
        }
        if (fromExpr.indexOf('\\overline') !== -1) {
          it.skip(`This test is here to fail to make it obvious that GM can't parse the \\overline in ${fromExpr}`, () => {
            throw new Error();
          });
          return;
        }

        if (
          // This list doesn't contain all possible ones, not even if you include the
          // commented out lines.
          ![
            // 'Approximation.ApproximateDecimalProduct',
            // 'Collecting.CombineTwoSimpleLikeTerms',
            // 'Decimals.ConvertFractionWithPowerOfTenDenominatorToDecimal',
            // 'Decimals.EvaluateDecimalAddition',
            'Decimals.EvaluateDecimalPowerDirectly',
            // 'Decimals.EvaluateDecimalProduct',
            // 'Decimals.EvaluateDecimalSubtraction',
            // 'Expand.DistributeMultiplicationOverSum',
            // 'FractionArithmetic.AddLikeFractions',
            // 'FractionArithmetic.SimplifyFractionToInteger',
            // 'FractionArithmetic.SubtractLikeFractions',
            // 'General.CancelAdditiveInverseElements',
            // 'General.DistributePowerOfProduct',
            // 'General.EliminateOneInProduct',
            'General.EliminateZeroInSum',
            // 'General.EvaluateExpressionToThePowerOfZero',
            // 'General.EvaluateOneToAnyPower',
            // 'General.EvaluateProductContainingZero',
            // 'General.EvaluateZeroToAPositivePower',
            // 'General.NormalizeNegativeSignOfIntegerInSum',
            'General.RemoveBracketProductInProduct',
            // 'General.RemoveBracketSumInSum',
            // 'General.RemoveRedundantBracket',
            // 'General.RemoveRedundantPlusSign',
            // 'General.RemoveUnitaryCoefficient',
            // 'General.RewriteProductOfPowersWithSameBase',
            // 'General.SimplifyExpressionToThePowerOfOne',
            // 'General.SimplifyFractionWithOneDenominator',
            // 'General.SimplifyUnitFractionToOne',
            'General.SimplifyZeroNumeratorFractionToZero',
            // 'IntegerArithmetic.EvaluateIntegerAddition',
            // 'IntegerArithmetic.EvaluateIntegerPowerDirectly',
            // 'IntegerArithmetic.EvaluateIntegerProduct',
            // 'IntegerArithmetic.EvaluateIntegerSubtraction',
            // 'IntegerArithmetic.SimplifyEvenPowerOfNegative',
            // 'Polynomials.DistributeProductToIntegerPower',
          ].includes(step.explanation.key)
        ) {
          dummyTest();
          return;
        }

        const fromExprGm = new gmath.AlgebraModel(fromExpr);
        const fromExprAscii = fromExprGm.to_ascii();
        const fullToExpr = treeToLatex(step.fullToExpr);
        const { actors, gmAction, targets } = solverPathToGmNodes(
          fromExprGm,
          fromExprTree,
          rawGmAction,
        );
        let chain = when();
        const actorsSelector = fromExprGm.getSelectorOfNodes(actors);
        const targetsSelector = fromExprGm.getSelectorOfNodes(targets);
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
            chain.gives(fullToExpr)();
            break;
          case 'DoubleTap':
            dummyTest();
            return;
          case 'Tap':
            chain
              .simplifying(fromExprAscii)
              .selecting(actorsSelector)
              .gives(fullToExpr)();
            break;
          case 'DragCollect':
          case 'Edit':
            dummyTest();
            return;
          case 'Formula':
            chain
              .rewriting(fromExprAscii)
              .applying(formulaMap[gmAction.formulaId])
              .onto(actorsSelector)
              .gives(fullToExpr)();
            break;
          case 'TapHold':
          case 'NotSupported':
            dummyTest();
            return;
          default:
            throwUnknownTypeError(gmAction.type);
        }
      });
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
  it.skip('Dummy Test. We need to go back later and make a proper test that can pass instead of calling `dummyTest()`.', () => {
    throw new Error();
  });
}

const formulaMap = { 'Difference of Squares': '(a+b)*(a-b)' };

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
