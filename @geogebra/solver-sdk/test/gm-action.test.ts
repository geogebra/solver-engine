import { describe, expect, it } from 'vitest';
// If this file doesn't exist, you need to run the kotlin tests in the methods package
import { testResults } from '../../../solver-poker/test-results-src/test-results';
import type { TransformationJson } from '../src';
import { addFullFromExprToTransformation, getInnerSteps, treeToLatex } from '../src';
import { solverPathToGmNodes } from '../src/graspable-math/solver-path-to-gm-nodes';
import * as path from 'path';
import * as prettier from 'prettier';
import requireFromUrl from 'require-from-url/sync';
import { config } from 'dotenv';
import { JSDOM } from 'jsdom';
import { GmMathNode } from '../src/graspable-math/create-path-map';

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
      'https://graspablemath.com/shared/libs/gmath-dist/gmath.min.js'}"></script>
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
    'https://graspablemath.com/shared/libs/gmath-dist/when-for-actions.js',
  ).when;
}

// // for use when debugging
// global.treeToLatex = treeToLatex;
// global.treeToAscii = treeToAscii;
// function treeToAscii(tree): string {
//   return new gmath.AlgebraModel(treeToLatex(tree)).to_ascii();
// }

describe('gmAction tests', () => {
  for (const testResult of testResults) {
    const transformation: TransformationJson = JSON.parse(
      testResult.transformationJsonMath,
    );
    if (!transformation) continue;
    const augmentedTransformation = addFullFromExprToTransformation(transformation);
    for (const step of getInnerSteps(augmentedTransformation)) {
      if (step.path !== '.') {
        // Do not ignore these steps once
        // https://geogebra.slack.com/archives/C03E0QWSEKU/p1686601854756239 is addressed
        dummyTest();
        continue;
      }
      const rawGmAction = step.gmAction;
      if (!rawGmAction) {
        dummyTest();
        continue;
      }
      const fromExprTree = step.fullFromExpr;
      const fromExpr = treeToLatex(fromExprTree);
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
        if (
          fromExpr.indexOf('\\leq') !== -1 ||
          fromExpr.indexOf('\\geq') !== -1 ||
          fromExpr.indexOf('\\neq') !== -1
        ) {
          // Todo: get rid of this skip
          it.skip(`This test is here to fail to make it obvious that GM can't parse \\leq, \\geq or \\neq in ${fromExpr}`, () => {
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
        // These are just here so that V8 doesn't optimize away access to the variables,
        // so that we can look at the values of them in the debugger.
        transformation;
        testResult;

        if (
          [
            // Purposely rounding decimal arithmetic (beyond standard machine precision)
            // is not and will not be supported by GM, because it is generally not a good
            // idea to do in the middle of a calculation.
            'Approximation.ApproximateDecimalProduct',
            // This will always fail because GM goes directly from 1/7+2/7 to 3/7 instead
            // of {1+2}/7 like solver does
            'FractionArithmetic.AddLikeFractions',
            // See comment above
            'FractionArithmetic.SubtractLikeFractions',
            // Ask Erik what he wants to do about this
            'Approximation.RoundTerminatingDecimal',
            // Erik, please look at this and the one below it. David got stuck on them.
            'General.RewriteFractionOfPowersWithSameExponent',
            'General.RewriteProductOfPowersWithSameExponent',
          ].includes(step.explanation.key) ||
          // We will eventually get rid of this whitelist and just have a blacklist (like
          // above), but that will take time, so in the mean time, we will use a hybrid
          // approach. This list doesn't contain all possible ones, not even if you
          // include the commented out lines.
          ![
            'Collecting.CombineTwoSimpleLikeTerms',
            'Decimals.ConvertFractionWithPowerOfTenDenominatorToDecimal',
            'Decimals.EvaluateDecimalAddition',
            'Decimals.EvaluateDecimalPowerDirectly',
            'Decimals.EvaluateDecimalProduct',
            'Decimals.EvaluateDecimalSubtraction',
            'FractionArithmetic.SimplifyFractionToInteger',
            'General.EliminateOneInProduct',
            'General.EliminateZeroInSum',
            'General.EvaluateExpressionToThePowerOfZero',
            'General.EvaluateOneToAnyPower',
            'General.EvaluateProductContainingZero',
            'General.EvaluateZeroToAPositivePower',
            'General.NormalizeNegativeSignOfIntegerInSum',
            'General.RemoveBracketProductInProduct',
            'General.RemoveBracketSumInSum',
            'General.RemoveUnitaryCoefficient',
            'General.RewriteFractionOfPowersWithSameExponent',
            'General.RewriteProductOfPowersWithSameBase',
            'General.RewriteProductOfPowersWithSameExponent',
            'General.SimplifyExpressionToThePowerOfOne',
            'General.SimplifyFractionWithOneDenominator',
            'General.SimplifyUnitFractionToOne',
            'General.SimplifyZeroNumeratorFractionToZero',
            'IntegerArithmetic.EvaluateIntegerAddition',
            'IntegerArithmetic.EvaluateIntegerPowerDirectly',
            'IntegerArithmetic.EvaluateIntegerProduct',
            'IntegerArithmetic.EvaluateIntegerSubtraction',
            'IntegerArithmetic.SimplifyEvenPowerOfNegative',
            'Decimals.ExpandFractionToPowerOfTenDenominator',
            'Decimals.MultiplyFractionOfDecimalsByPowerOfTen',
            // This one is weird as it contains the following test: 9900*x=31101 -> 31101/9900 with the x disappearing into thin air
            // I guess even if we find the reason gm actions are not being executed we will still need to work on this
            // 'Decimals.SolveLinearEquation',

            // I can't even imagine what causes these to fail as the selected GmAction seems correct
            // My guess is that dragging onto the target is not the correct action in this case
            // I spent a lot of time trying to figure out what happens, but I guess I won't be able to solve
            // this without access to GM code. The action is simply not executed
            // I have a feeling that we need to select the parent as with the other cases of "drag over" when the
            // expressions are on the same level, but doing that didn't seem to help. Could use some documentation!
            // I can't even manually execute the action in GM, as when I drag a term to the opposite side, it
            // just gets added to that side with the opposite sign instead of being cancelled
            // - Abel
            // 'Equations.CancelCommonTermsOnBothSides',
            // Also, any test case containing equations is prone to this very same behavior

            // 'Equations.DivideByCoefficientOfVariable',
            // 'Equations.FlipEquation',
            // 'Equations.MoveConstantsToTheLeft',
            // 'Equations.MoveConstantsToTheRight',

            // These cases are pretty stupid, if you move a variable to the other side of an equation,
            // on the original side it should be replaced by zero, not the variable minus itself.
            // We can't expect GM to have this solution.
            // - Abel
            // 'Equations.MoveVariablesToTheLeft',

            // 'Equations.MoveVariablesToTheRight',
            // 'Equations.MultiplyByInverseCoefficientOfVariable',
            // 'Equations.MultiplyEquationByLCD',
            // 'Equations.NegateBothSides',
            // 'Equations.TakeRootOfBothSides',
            // 'Equations.TakeRootOfBothSidesRHSIsZero',

            // In this test for some cases the \cdot is hidden and the double tap is used, except
            // it is not specified where to double tap (the \cdot can't be selected),
            // Workaround: select the parenthesis that follows the operator
            // Related problem: GM simplifies the expression while Solver does not (due to this some tests will fail if uncommented)
            // - Abel
            // 'Expand.ApplyFoilMethod',

            // In these cases the behavior of Solver seems to be too different to align them up,
            // GM will always simplify the distributed expressions (e.g instead of 4*11 we have 44)
            // - Abel
            // 'Expand.DistributeMultiplicationOverSum',

            'Expand.DistributeNegativeOverBracket',

            // GM simplifies the expression further than Solver in some test cases
            // (e.g (2*x)^3 - 8*x^3)
            // 'Expand.ExpandBinomialCubedUsingIdentity',

            // GM simplifies the expression further than Solver in some test cases
            // 'Expand.ExpandBinomialSquaredUsingIdentity',

            // 'Expand.ExpandDoubleBrackets',
            // 'Expand.ExpandProductOfSumAndDifference',
            // 'Expand.ExpandTrinomialSquaredUsingIdentity',
            // 'FractionArithmetic.BringToCommonDenominator',
            // 'FractionArithmetic.ConvertIntegerToFraction',
            // 'General.AddClarifyingBracket',
            // 'General.CancelAdditiveInverseElements',
            // 'General.CancelCommonTerms',

            // Most of these work fine, except some isolated cases where the action is not executed
            // even though the selectors and action type are correct
            // - Abel
            // 'General.CancelDenominator',

            // 'General.CancelRootIndexAndExponent',
            // 'General.DistributePowerOfProduct',
            // 'General.DistributeSumOfPowers',
            'General.EvaluateZeroToThePowerOfZero',
            // 'General.FactorMinusFromSum',
            // 'General.FlipFractionUnderNegativePower',
            // 'General.MoveSignOfNegativeFactorOutOfProduct',
            // 'General.MultiplyExponentsUsingPowerRule',
            // 'General.RemoveRedundantBracket',
            // 'General.RemoveRedundantPlusSign',

            // Same here, even though selectors are correct, the expressions are not moved.
            // I tried to select the parents (as when dragging onto) but it didn't change anything
            // 'General.ReorderProduct',

            // Same here, even though selectors are correct, the expressions are not moved.
            // 'General.RewriteFractionOfPowersWithSameBase',

            // 'General.RewriteIntegerOrderRootAsPower',
            'General.RewritePowerAsProduct',
            'General.RewritePowerUnderRoot',
            'General.RewriteProductOfPowersWithInverseBase',
            // 'General.RewriteProductOfPowersWithInverseFractionBase',
            // 'General.RewriteProductOfPowersWithNegatedExponent',

            // Here GM does not simplify the parentheses by default, but the actions are executed correctly
            // 'General.SimplifyDoubleMinus',

            'General.SimplifyProductOfConjugates',
            // 'General.SimplifyProductWithTwoNegativeFactors',
            // 'General.SimplifyZeroDenominatorFractionToUndefined',
            // 'Inequalities.CancelCommonTermsOnBothSides',
            // 'Inequalities.MoveConstantsToTheLeft',
            // 'Inequalities.MoveConstantsToTheRight',
            // 'Inequalities.MoveVariablesToTheLeft',
            // 'Inequalities.MoveVariablesToTheRight',
          ].includes(step.explanation.key)
        ) {
          dummyTest();
          return;
        }

        const fromExprGm = new gmath.AlgebraModel(fromExpr) as GmMathNode;
        const fromExprAscii = fromExprGm.to_ascii();
        ////////////////////////////////////////////////////////////////////////
        // Uncomment this to focus on a specific test
        ////////////////////////////////////////////////////////////////////////
        // if (fromExprAscii !== 't^(1+2)*t^3') {
        //   dummyTest();
        //   return;
        // }
        if (
          [
            'sqrt[4](1^4)',
            'x=(-7±sqrt(7^2-4*1*0))/(2*1)',
            '2*(2^2*3)^(1/2)',
            '(-1)+1',
            // I want to pawn this one off to Erik to fix
            '(-0.2)*(-0.2)',
            // These three don't pass because GM leaves extra parenthesis that Solver gets
            // rid of
            '1/sqrt[3](13^2)*sqrt[3]13/sqrt[3](13^1)',
            '1/sqrt[3](13^2)*sqrt[3](13^1)/sqrt[3](13^1)',
            '(sqrt 2+1)^1+1',

            // This doesn't pass because GM leaves extra parenthesis that Solver gets rid
            // of. (These are for the IntegerArithmetic.EvaluateIntegerPowerDirectly rule
            // which is different solver rule than the ones above)
            '4*sqrt[6](2^3)*sqrt[6]243',
            'sqrt 32*sqrt[6](3^5)',
            'sqrt(2^5)*sqrt[6](3^5)',
            '2*sqrt[5]8*sqrt[5](3^4)',
            '2*sqrt[5](2^3)*sqrt[5](3^4)',

            // These needs to be improved in GM before they will work

            '1^(sqrt 2+1)',
            '0^(3/2)',
            // Tapping on the second 1 doesn't work in GM to cancel it out, for some
            // reason.
            '1/(1*sqrt 3)',
            // This happens because, for some reason, GM parses
            // '\left(-1\sqrt[{3}]{3}\right)' as '(-(1*sqrt[3]3))' (notice the extra
            // parenthesis in the GM version)
            '-(-(1*sqrt[3]3))-(-sqrt[3]3-3*sqrt[3]3+5*sqrt[3]3)',

            // David Ludlow doesn't think this should be "fixed" (awaiting approval from
            // Erik)
            '1+(-1)',

            // David Ludlow doesn't think that these four should be "fixed" (awaiting
            // approval from Erik). GM does the extra step of adding the exponents
            // together, which solver doesn't do.
            't*t^2*t^3',
            'sqrt[3](19*19^2)',
            'sqrt[3](2^3*19*19^2)',
            'sqrt[3](2*19*2^2*19^2)',
          ].includes(fromExprAscii)
        ) {
          dummyTest();
          return;
        }
        const fullToExpr = treeToLatex(step.fullToExpr);
        const { actors, gmAction, targets } = solverPathToGmNodes(
          fromExprGm,
          fromExprTree,
          rawGmAction,
        );
        let chain = when();
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
            chain.gives(fullToExpr)();
            break;
          case 'DoubleTap':
            chain
              .simplifying(fromExprAscii)
              .selecting(actorsSelector, 'dbltap')
              .gives(fullToExpr)();
            break;
          case 'Tap':
            chain
              .simplifying(fromExprAscii)
              .selecting(actorsSelector)
              .gives(fullToExpr)();
            break;
          case 'DragCollect':
            // Delete this comment after we get at least one of these tests to work: See
            // graspable-math/test/unit-tests/math-engine/expressions/rewrite-test.js line
            // 278 to see the example I emulated to write this.
            chain
              .rewriting(fromExprAscii)
              .moving(actorsSelector.split(';')[0])
              .onto(actorsSelector.split(';')[1])
              .gives(fromExprAscii)
              .moving('mapped nodes')
              .outside(targetsSelector)
              .gives(fullToExpr)();
            break;
          case 'Edit':
            // Todo: un-ignore this once David asks Erik the following question: How is GM
            // supposed to know what to edit the expression to? Right now Solver says that
            // an "edit" action should be done on a given subexpression but Solver doesn't
            // say what the subexpression should be edited to.
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

function getGmSelector(rootExpression: GmMathNode, subExpressions: GmMathNode[]) {
  return rootExpression
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
    .join(';');
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
