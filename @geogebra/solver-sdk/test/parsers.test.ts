import { describe, it } from 'mocha';
import { expect } from 'chai';
import type { ExpressionTree } from '../src/parser';
import {
  jsonToLatex,
  jsonToTree,
  latexToSolver,
  latexToTree,
  treeToJson,
  treeToLatex,
  treeToSolver,
} from '../src/parser';
import { MathJson } from '../src/types';

const latexToJson = (latex) => treeToJson(latexToTree(latex));
const jsonToSolver = (json) => treeToSolver(jsonToTree(json));

function testCases(
  cases: {
    solver?: string;
    json?: MathJson;
    latex?: string | string[];
    tree?: ExpressionTree;
    /** We currently do not support parsing sets or intervals in LaTeX, so we
     * need to be able to skip that part of those tests */
    dontParseLatex?: boolean;
  }[],
) {
  cases.forEach(({ solver, latex, json, tree, dontParseLatex }) => {
    /* things we actually need:
    - latex => solver (user input needs to be sent to the solver)
    - json => tree (turn solver response into tree representation)
    - tree => latex (display math to user)
    - tree => solver (some intermediate step we have in json/tree format and want to send to the solver)
    - maybe: solver => latex (for debugging)
    */

    if (json && tree) {
      it(`json "${JSON.stringify(json)}" => Tree "${JSON.stringify(tree)}"`, () => {
        expect(jsonToTree(json)).to.deep.equal(tree);
      });
      it(`Tree "${JSON.stringify(tree)}" => json "${JSON.stringify(json)}"`, () => {
        expect(treeToJson(tree)).to.deep.equal(json);
      });
    }

    if (!Array.isArray(latex)) latex = [latex];
    const [unCastedLatexExact, ...latexAlternates] = latex;
    const latexExact = unCastedLatexExact as string | undefined;

    if (latexExact && solver && !dontParseLatex) {
      it(`LaTeX "${latexExact}" => Solver "${solver}"`, () => {
        expect(latexToSolver(latexExact)).to.equal(solver);
      });
    }
    if (latexExact && json) {
      it(`json "${JSON.stringify(json)}" => LaTeX "${latexExact}"`, () => {
        expect(jsonToLatex(json)).to.equal(latexExact);
      });
      if (!dontParseLatex) {
        it(`LaTeX "${latexExact}" => json "${JSON.stringify(json)}"`, () => {
          expect(latexToJson(latexExact)).to.deep.equal(json);
        });
      }
    }
    if (latexExact && tree) {
      it(`tree "${JSON.stringify(tree)}" => LaTeX "${latexExact}"`, () => {
        expect(treeToLatex(tree)).to.equal(latexExact);
      });
      if (!dontParseLatex) {
        it(`LaTeX "${latexExact}" => tree "${JSON.stringify(tree)}"`, () => {
          expect(latexToTree(latexExact)).to.deep.equal(tree);
        });
      }
    }
    if (solver && json) {
      it(`json "${JSON.stringify(json)}" => Solver "${solver}"`, () => {
        expect(jsonToSolver(json)).to.equal(solver);
      });
    }
    if (solver && tree) {
      it(`tree "${JSON.stringify(tree)}" => Solver "${solver}"`, () => {
        expect(treeToSolver(tree)).to.equal(solver);
      });
    }

    for (const alternate of latexAlternates) {
      it(`LaTeX "${alternate}" => LaTeX "${latex}"`, () => {
        expect(latexToTree(latexExact)).to.deep.equal(latexToTree(alternate));
      });
    }
  });
}

describe('Solver Parser Unit Tests', () => {
  describe('Sums and products', () => {
    testCases([
      { solver: '1', json: ['1'], latex: '1' },
      {
        solver: '1+2+3',
        json: ['Sum', ['1'], ['2'], ['3']],
        latex: '1+2+3',
      },
      {
        solver: '1+2-3-4',
        json: ['Sum', ['1'], ['2'], ['Minus', ['3']], ['Minus', ['4']]],
        latex: '1+2-3-4',
      },
      {
        solver: '+1+2+3',
        json: ['Sum', ['Plus', ['1']], ['2'], ['3']],
        latex: '+1+2+3',
      },
      {
        solver: '1+2*3*4+5*6',
        json: ['Sum', ['1'], ['Product', ['2'], ['3'], ['4']], ['Product', ['5'], ['6']]],
        latex: '1+2 \\cdot 3 \\cdot 4+5 \\cdot 6',
      },
      {
        solver: '-1-2',
        json: ['Sum', ['Minus', ['1']], ['Minus', ['2']]],
        latex: '-1-2',
      },
      {
        solver: '-1-2*3',
        json: ['Sum', ['Minus', ['1']], ['Minus', ['Product', ['2'], ['3']]]],
        latex: ['-1-2 \\cdot 3', '-1-2\\cdot3', '-1-2\\times3', '-1–2*3', '–1-2×3'],
      },
      {
        solver: '1*3',
        json: ['Product', ['1'], ['3']],
        latex: ['1 \\cdot 3', '1\\cdot{}3', '1\\times{}3', '1\\times 3'],
      },
      {
        solver: '2*3:4*5',
        json: ['Product', ['2'], ['3'], ['DivideBy', ['4']], ['5']],
        latex: '2 \\cdot 3 \\div 4 \\cdot 5',
      },
      {
        solver: '(1+2)+3',
        json: ['Sum', [['Sum', 'RoundBracket'], ['1'], ['2']], ['3']],
        latex: '\\left(1+2\\right)+3',
      },
      {
        solver: '1+(-2)',
        json: ['Sum', ['1'], [['Minus', 'RoundBracket'], ['2']]],
        latex: '1+\\left(-2\\right)',
      },
      {
        solver: '1-(+2)',
        json: ['Sum', ['1'], ['Minus', [['Plus', 'RoundBracket'], ['2']]]],
        latex: '1-\\left(+2\\right)',
      },
      {
        solver: '1+-2',
        json: ['Sum', ['1'], [['Minus', 'MissingBracket'], ['2']]],
        latex: '1+-2',
      },
      {
        solver: '1+--2',
        json: [
          'Sum',
          ['1'],
          [
            ['Minus', 'MissingBracket'],
            ['Minus', ['2']],
          ],
        ],
        latex: '1+--2',
      },
    ]);
  });

  describe('Plus/minus (±)', () => {
    testCases([
      { solver: '+/-1', json: ['PlusMinus', ['1']], latex: ['\\pm 1', '±1'] },
      {
        solver: '1+/-2',
        json: ['Sum', ['1'], ['PlusMinus', ['2']]],
        latex: ['1\\pm 2', '1±2'],
      },
      {
        solver: '1++/-2',
        json: ['Sum', ['1'], [['PlusMinus', 'MissingBracket'], ['2']]],
        latex: ['1+\\pm 2', '1+±2'],
      },
      {
        solver: '1++/-+/-2',
        json: [
          'Sum',
          ['1'],
          [
            ['PlusMinus', 'MissingBracket'],
            ['PlusMinus', ['2']],
          ],
        ],
        latex: ['1+\\pm \\pm 2', '1+±±2'],
      },
      {
        solver: '+/-1+/-2+/-3',
        json: ['Sum', ['PlusMinus', ['1']], ['PlusMinus', ['2']], ['PlusMinus', ['3']]],
        latex: ['\\pm 1\\pm 2\\pm 3', '±1±2±3'],
      },
      {
        solver: '1+/-(+/-2)',
        json: ['Sum', ['1'], ['PlusMinus', [['PlusMinus', 'RoundBracket'], ['2']]]],
        latex: ['1\\pm \\left(\\pm 2\\right)', '1±(±2)'],
      },
      {
        solver: '+/-[2 ^ 4]',
        json: ['PlusMinus', ['Power', ['2'], ['4']]],
        latex: ['\\pm {2}^{4}', '±2^4'],
      },
    ]);
  });

  describe('Implicit Products', () => {
    testCases([
      {
        solver: '1 (2)',
        json: ['ImplicitProduct', ['1'], [['2', 'RoundBracket']]],
        latex: '1\\left(2\\right)',
      },
      {
        solver: '(1) (2) 3',
        json: [
          'ImplicitProduct',
          [['1', 'RoundBracket']],
          [['2', 'RoundBracket']],
          ['3'],
        ],
        latex: '\\left(1\\right)\\left(2\\right)3',
      },
      {
        solver: '(1) (2) 4 (-5)',
        json: [
          'ImplicitProduct',
          [['1', 'RoundBracket']],
          [['2', 'RoundBracket']],
          ['4'],
          [['Minus', 'RoundBracket'], ['5']],
        ],
        latex: '\\left(1\\right)\\left(2\\right)4\\left(-5\\right)',
      },
      {
        solver: '2 a*3 a',
        json: [
          'Product',
          ['ImplicitProduct', ['2'], ['a']],
          ['ImplicitProduct', ['3'], ['a']],
        ],
        latex: '2a \\cdot 3a',
      },
    ]);
  });

  describe('Equations', () => {
    testCases([
      {
        solver: '3 = 1+2',
        json: ['Equation', ['3'], ['Sum', ['1'], ['2']]],
        latex: '3 = 1+2',
      },
    ]);
  });

  describe('Powers', () => {
    testCases([
      {
        solver: '-[3 ^ 4]',
        json: ['Minus', ['Power', ['3'], ['4']]],
        latex: ['-{3}^{4}', '-3^4'],
      },
      {
        solver: '[(-3) ^ -4]',
        json: ['Power', [['Minus', 'RoundBracket'], ['3']], ['Minus', ['4']]],
        latex: ['{\\left(-3\\right)}^{-4}', '(-3)^{-4}'],
      },
      {
        solver: '[3 ^ [4 ^ 5]]',
        json: ['Power', ['3'], ['Power', ['4'], ['5']]],
        latex: ['{3}^{{4}^{5}}', '3^4^5'],
      },
      {
        solver: '[3 ^ [1 / 2]]',
        json: ['Power', ['3'], ['Fraction', ['1'], ['2']]],
        latex: ['{3}^{\\frac{1}{2}}', '3^\\frac{1}{2}'],
      },
      {
        solver: '2+3*[4 ^ 5]*6+7',
        json: ['Sum', ['2'], ['Product', ['3'], ['Power', ['4'], ['5']], ['6']], ['7']],
        latex: ['2+3 \\cdot {4}^{5} \\cdot 6+7', '2+3*4^5*6+7'],
      },
    ]);
  });

  describe('Variables', () => {
    testCases([
      {
        solver: 'a+b c*D',
        json: ['Sum', ['a'], ['Product', ['ImplicitProduct', ['b'], ['c']], ['D']]],
        latex: 'a+bc \\cdot D',
      },
    ]);
  });

  describe('Fractions and Mixed Numbers', () => {
    testCases([
      {
        solver: '[2 / 3]+1',
        json: ['Sum', ['Fraction', ['2'], ['3']], ['1']],
        latex: '\\frac{2}{3}+1',
      },
      {
        solver: '[1 2 / 3]',
        json: ['MixedNumber', ['1'], ['2'], ['3']],
        latex: '1\\frac{2}{3}',
      },
      {
        solver: '[2*1 / 3*1]',
        json: ['Fraction', ['Product', ['2'], ['1']], ['Product', ['3'], ['1']]],
        latex: '\\frac{2 \\cdot 1}{3 \\cdot 1}',
      },
      {
        solver: '-[2+1 / 3*[1 / 2]]',
        json: [
          'Minus',
          [
            'Fraction',
            ['Sum', ['2'], ['1']],
            ['Product', ['3'], ['Fraction', ['1'], ['2']]],
          ],
        ],
        latex: '-\\frac{2+1}{3 \\cdot \\frac{1}{2}}',
      },
    ]);
  });

  describe('Brackets', () => {
    testCases([
      { solver: '(1)', json: [['1', 'RoundBracket']], latex: '\\left(1\\right)' },
      {
        solver: '(1+2)',
        json: [['Sum', 'RoundBracket'], ['1'], ['2']],
        latex: '\\left(1+2\\right)',
      },
      {
        solver: '([.{.1.}+2.])',
        json: [['Sum', 'SquareBracket', 'RoundBracket'], [['1', 'CurlyBracket']], ['2']],
        latex: '\\left(\\left[\\left\\{1\\right\\}+2\\right]\\right)',
      },
      { latex: '\\left(a+b\\right) \\cdot c', solver: '(a+b)*c' },
      { latex: '((a+b))', solver: '((a+b))' },
      {
        latex: '\\left\\{\\left[\\left(x\\right)\\right]\\right\\}',
        solver: '{.[.(x).].}',
      },
      { latex: '2 (a)', solver: '2 (a)' },
    ]);
  });

  describe('Roots', async () => {
    testCases([
      {
        solver: 'sqrt[4]',
        json: ['SquareRoot', ['4']],
        latex: ['\\sqrt{4}', '\\sqrt 4'],
      },
      {
        solver: '2 sqrt[4] sqrt[2]',
        json: ['ImplicitProduct', ['2'], ['SquareRoot', ['4']], ['SquareRoot', ['2']]],
        latex: '2\\sqrt{4}\\sqrt{2}',
      },
      { latex: '2 \\sqrt[3] 4', solver: '2 root[4, 3]' },
      {
        solver: 'root[4, 3]',
        json: ['Root', ['4'], ['3']],
        latex: ['\\sqrt[{3}]{4}', '\\sqrt[3] 4'],
      },
      {
        solver: '[2 ^ root[1+2-4 a, 3]]',
        json: [
          'Power',
          ['2'],
          [
            'Root',
            ['Sum', ['1'], ['2'], ['Minus', ['ImplicitProduct', ['4'], ['a']]]],
            ['3'],
          ],
        ],
        latex: '{2}^{\\sqrt[{3}]{1+2-4a}}',
      },
      { latex: '2^\\sqrt[3]{1+2}', solver: '[2 ^ root[1+2, 3]]' },
    ]);
  });

  describe('Sums and products', () => {
    testCases([
      {
        solver: '2.3+3',
        json: ['Sum', ['2.3'], ['3']],
        tree: {
          type: 'Sum',
          path: '.',
          args: [
            { type: 'Number', path: './0', value: '2.3' },
            { type: 'Number', path: './1', value: '3' },
          ],
        },
        latex: '2.3+3',
      },
      {
        solver: '1-2*3',
        json: ['Sum', ['1'], ['Minus', ['Product', ['2'], ['3']]]],
        latex: '1-2 \\cdot 3',
      },
      {
        solver: '2 a:3',
        json: ['Product', ['ImplicitProduct', ['2'], ['a']], ['DivideBy', ['3']]],
        latex: '2a \\div 3',
      },
    ]);
  });

  describe('Variables', () => {
    testCases([
      { solver: 'a A a', json: ['ImplicitProduct', ['a'], ['A'], ['a']], latex: 'aAa' },
    ]);
  });

  describe('Numbers with repeating decimals', () => {
    testCases([
      { solver: '2.1[33]', json: ['2.1[33]'], latex: '2.1\\overline{33}' },
      { solver: '0.[05]', json: ['0.[05]'], latex: '0.\\overline{05}' },
    ]);
  });

  describe('Round, square and curly brackets', async () => {
    testCases([
      {
        solver: '(1+2)*3',
        json: ['Product', [['Sum', 'RoundBracket'], ['1'], ['2']], ['3']],
        latex: '\\left(1+2\\right) \\cdot 3',
      },
      {
        solver: '{.[.(x).].}',
        json: [['x', 'RoundBracket', 'SquareBracket', 'CurlyBracket']],
        latex: '\\left\\{\\left[\\left(x\\right)\\right]\\right\\}',
      },
    ]);
  });

  describe('Fractions and mixed numbers', async () => {
    testCases([
      {
        solver: '[1 / 2]',
        json: ['Fraction', ['1'], ['2']],
        latex: ['\\frac{1}{2}', '\\dfrac{1}{2}', '\\tfrac{1}{2}'],
      },
      {
        solver: '[a / b]',
        json: ['Fraction', ['a'], ['b']],
        latex: ['\\frac{a}{b}', '\\frac ab'],
      },
      { latex: '2.1\\frac{1}{2}', solver: '2.1 [1 / 2]' },
      {
        solver: '[1 2 / 3]',
        json: ['MixedNumber', ['1'], ['2'], ['3']],
        latex: '1\\frac{2}{3}',
      },
      {
        solver: '[[1 / 2] / 3]',
        json: ['Fraction', ['Fraction', ['1'], ['2']], ['3']],
        latex: '\\frac{\\frac{1}{2}}{3}',
      },
      { latex: '2+\\frac{1}{2}', solver: '2+[1 / 2]' },
    ]);
  });

  describe('Powers', async () => {
    testCases([
      {
        solver: '[(-3) ^ 4]',
        json: ['Power', [['Minus', 'RoundBracket'], ['3']], ['4']],
        latex: '{\\left(-3\\right)}^{4}',
      },
      {
        solver: '-[3 ^ 4]',
        json: ['Minus', ['Power', ['3'], ['4']]],
        latex: '-{3}^{4}',
      },
      {
        solver: '[3 ^ [4 ^ 5]]',
        json: ['Power', ['3'], ['Power', ['4'], ['5']]],
        latex: '{3}^{{4}^{5}}',
      },
      {
        solver: '[3 ^ [1 / 2]]',
        json: ['Power', ['3'], ['Fraction', ['1'], ['2']]],
        latex: '{3}^{\\frac{1}{2}}',
      },
      {
        solver: '2+3*[4 ^ 5]*6+7',
        json: ['Sum', ['2'], ['Product', ['3'], ['Power', ['4'], ['5']], ['6']], ['7']],
        latex: '2+3 \\cdot {4}^{5} \\cdot 6+7',
      },
    ]);
  });

  describe('Roots', async () => {
    testCases([
      { solver: 'sqrt[4]', json: ['SquareRoot', ['4']], latex: '\\sqrt{4}' },
      { solver: 'root[4, 3]', json: ['Root', ['4'], ['3']], latex: '\\sqrt[{3}]{4}' },
      {
        solver: '[2 ^ root[1+2, 3]]',
        json: ['Power', ['2'], ['Root', ['Sum', ['1'], ['2']], ['3']]],
        latex: '{2}^{\\sqrt[{3}]{1+2}}',
      },
    ]);
  });

  describe('Relations', async () => {
    testCases([
      { solver: 'x = 5', json: ['Equation', ['x'], ['5']], latex: 'x = 5' },
      { solver: 'x < 5', json: ['LessThan', ['x'], ['5']], latex: 'x < 5' },
      { solver: 'x > 5', json: ['GreaterThan', ['x'], ['5']], latex: 'x > 5' },
      {
        solver: 'x <= 5',
        json: ['LessThanEqual', ['x'], ['5']],
        latex: ['x \\leq 5', 'x ≤ 5'],
      },
      {
        solver: 'x >= 5',
        json: ['GreaterThanEqual', ['x'], ['5']],
        latex: ['x \\geq 5', 'x ≥ 5'],
      },
    ]);
  });

  describe('Solutions and sets', async () => {
    testCases([
      {
        solver: 'Solution[x, {}]',
        json: ['Solution', ['x'], ['FiniteSet']],
        latex: 'x \\in \\emptyset',
      },
      {
        solver: 'Solution[x, {5}]',
        json: ['Solution', ['x'], ['FiniteSet', ['5']]],
        latex: 'x \\in \\left\\{5\\right\\}',
        dontParseLatex: true,
      },
      {
        solver: 'Solution[x, {5, 2}]',
        json: ['Solution', ['x'], ['FiniteSet', ['5'], ['2']]],
        latex: 'x \\in \\left\\{5, 2\\right\\}',
        dontParseLatex: true,
      },
      {
        solver: 'Solution[x, REALS]',
        json: ['Solution', ['x'], ['Reals']],
        latex: 'x \\in \\mathbb{R}',
      },
      {
        solver: 'Solution[x, (-INFINITY, 5)]',
        json: ['Solution', ['x'], ['OpenInterval', ['Minus', ['INFINITY']], ['5']]],
        latex: 'x \\in \\left( -\\infty, 5 \\right)',
        dontParseLatex: true,
      },
      {
        solver: 'Solution[x, [0, 5]]',
        json: ['Solution', ['x'], ['ClosedInterval', ['0'], ['5']]],
        latex: 'x \\in \\left[ 0, 5 \\right]',
        dontParseLatex: true,
      },
      {
        solver: 'Solution[x, (-INFINITY, 5]]',
        json: ['Solution', ['x'], ['OpenClosedInterval', ['Minus', ['INFINITY']], ['5']]],
        latex: 'x \\in \\left( -\\infty, 5 \\right]',
        dontParseLatex: true,
      },
      {
        solver: 'Solution[x, [5, INFINITY)]',
        json: ['Solution', ['x'], ['ClosedOpenInterval', ['5'], ['INFINITY']]],
        latex: 'x \\in \\left[ 5, \\infty \\right)',
        dontParseLatex: true,
      },
    ]);
  });

  describe('Undefined', async () => {
    testCases([
      {
        solver: 'UNDEFINED',
        json: ['UNDEFINED'],
        latex: '\\text{undefined}',
        tree: { type: 'UNDEFINED', path: '.' },
      },
    ]);
  });
});
