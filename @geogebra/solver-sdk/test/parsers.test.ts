import { describe, expect, it } from 'vitest';
import type { MathJson } from '../src';
import {
  jsonToLatex,
  jsonToTree,
  latexToSolver,
  latexToTree,
  treeToJson,
  treeToSolver,
} from '../src';

const latexToJson = (latex) => treeToJson(latexToTree(latex));
const jsonToSolver = (json) => treeToSolver(jsonToTree(json));

const integer = (value: string): MathJson => ({ type: 'Integer', value });
const decimal = (value: string): MathJson => ({ type: 'Decimal', value });
const variable = (value: string): MathJson => ({ type: 'Variable', value });

function testCases(
  cases: {
    solver?: string;
    json?: MathJson;
    latex?: string | string[];
    /** We currently do not support parsing sets or intervals in LaTeX, so we
     * need to be able to skip that part of those tests */
    dontParseLatex?: boolean;
  }[],
) {
  cases.forEach(({ solver, latex, json, dontParseLatex }) => {
    /* things we actually need:
        - latex => solver (user input needs to be sent to the solver)
        - json => latex (display math to user)
        - json => solver (some intermediate step we have in json/tree format and want to send to the solver)
        - maybe: solver => latex (for debugging)
        */

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

    if (solver && json) {
      it(`json "${JSON.stringify(json)}" => Solver "${solver}"`, () => {
        expect(jsonToSolver(json)).to.equal(solver);
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
      { solver: '1', json: integer('1'), latex: '1' },

      {
        solver: '1 + 2 + 3',
        json: {
          type: 'Sum',
          operands: [integer('1'), integer('2'), integer('3')],
        },
        latex: '1+2+3',
      },

      {
        solver: '1 + 2 - 3 - 4',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            integer('2'),
            { type: 'Minus', operands: [integer('3')] },
            { type: 'Minus', operands: [integer('4')] },
          ],
        },
        latex: '1+2-3-4',
      },

      {
        solver: '+1 + 2 + 3',
        json: {
          type: 'Sum',
          operands: [
            { type: 'Plus', operands: [integer('1')] },
            integer('2'),
            integer('3'),
          ],
        },
        latex: '+1+2+3',
      },

      {
        solver: '1 + 2 * 3 * 4 + 5 * 6',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            {
              type: 'Product',
              operands: [integer('2'), integer('3'), integer('4')],
            },
            {
              type: 'Product',
              operands: [integer('5'), integer('6')],
            },
          ],
        },
        latex: '1+2 \\cdot 3 \\cdot 4+5 \\cdot 6',
      },

      {
        solver: '-1 - 2',
        json: {
          type: 'Sum',
          operands: [
            { type: 'Minus', operands: [integer('1')] },
            { type: 'Minus', operands: [integer('2')] },
          ],
        },
        latex: '-1-2',
      },

      {
        solver: '-1 - 2 * 3',
        json: {
          type: 'Sum',
          operands: [
            { type: 'Minus', operands: [integer('1')] },
            {
              type: 'Minus',
              operands: [
                {
                  type: 'Product',
                  operands: [integer('2'), integer('3')],
                },
              ],
            },
          ],
        },
        latex: ['-1-2 \\cdot 3', '-1-2\\cdot3', '-1-2\\times3', '-1–2*3', '–1-2×3'],
      },

      {
        solver: '1 * 3',
        json: {
          type: 'Product',
          operands: [integer('1'), integer('3')],
        },
        latex: ['1 \\cdot 3', '1\\cdot{}3', '1\\times{}3', '1\\times 3'],
      },

      {
        solver: '2 * 3 : 4 * 5',
        json: {
          type: 'Product',
          operands: [
            integer('2'),
            integer('3'),
            { type: 'DivideBy', operands: [integer('4')] },
            integer('5'),
          ],
        },
        latex: [
          '2 \\cdot 3 \\div 4 \\cdot 5',
          '2 \\cdot 3   ÷   4 \\cdot 5',
          '2 \\cdot 3   :   4 \\cdot 5',
        ],
      },

      {
        solver: '3 : [4 ^ 5]',
        json: {
          type: 'Product',
          operands: [
            integer('3'),
            {
              type: 'DivideBy',
              operands: [
                {
                  type: 'Power',
                  operands: [integer('4'), integer('5')],
                },
              ],
            },
          ],
        },
        latex: ['3 \\div {4}^{5}', '3÷4^5', '3:4^5'],
      },

      {
        solver: '3 : -[4 ^ 5]',
        json: {
          type: 'Product',
          operands: [
            integer('3'),
            {
              type: 'DivideBy',
              operands: [
                {
                  type: 'Minus',
                  operands: [
                    {
                      type: 'Power',
                      operands: [integer('4'), integer('5')],
                    },
                  ],
                },
              ],
            },
          ],
        },
        latex: ['3 \\div -{4}^{5}', '3÷-4^5', '3:-4^5'],
      },

      {
        solver: '(1 + 2) + 3',
        json: {
          type: 'Sum',
          operands: [
            {
              type: 'Sum',
              decorators: ['RoundBracket'],
              operands: [integer('1'), integer('2')],
            },
            integer('3'),
          ],
        },
        latex: '\\left(1+2\\right)+3',
      },

      {
        solver: '1 + (-2)',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            { type: 'Minus', decorators: ['RoundBracket'], operands: [integer('2')] },
          ],
        },
        latex: '1+\\left(-2\\right)',
      },

      {
        solver: '1 - (+2)',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            {
              type: 'Minus',
              operands: [
                { type: 'Plus', decorators: ['RoundBracket'], operands: [integer('2')] },
              ],
            },
          ],
        },
        latex: '1-\\left(+2\\right)',
      },

      {
        solver: '1 + -2',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            { type: 'Minus', decorators: ['MissingBracket'], operands: [integer('2')] },
          ],
        },
        latex: '1+-2',
      },

      {
        solver: '1 + --2',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            {
              type: 'Minus',
              decorators: ['MissingBracket'],
              operands: [
                {
                  type: 'Minus',
                  operands: [integer('2')],
                },
              ],
            },
          ],
        },
        latex: '1+--2',
      },
    ]);
  });

  describe('Plus/minus (±)', () => {
    testCases([
      {
        solver: '+/-1',
        json: { type: 'PlusMinus', operands: [integer('1')] },
        latex: ['\\pm 1', '±1'],
      },

      {
        solver: '1 +/- 2',
        json: {
          type: 'Sum',
          operands: [integer('1'), { type: 'PlusMinus', operands: [integer('2')] }],
        },
        latex: ['1\\pm 2', '1±2'],
      },

      {
        solver: '1 + +/-2',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            {
              type: 'PlusMinus',
              decorators: ['MissingBracket'],
              operands: [integer('2')],
            },
          ],
        },
        latex: ['1+\\pm 2', '1+±2'],
      },

      {
        solver: '1 + +/-+/-2',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            {
              type: 'PlusMinus',
              decorators: ['MissingBracket'],
              operands: [
                {
                  type: 'PlusMinus',
                  operands: [integer('2')],
                },
              ],
            },
          ],
        },
        latex: ['1+\\pm \\pm 2', '1+±±2'],
      },

      {
        solver: '+/-1 +/- 2 +/- 3',
        json: {
          type: 'Sum',
          operands: [
            { type: 'PlusMinus', operands: [integer('1')] },
            { type: 'PlusMinus', operands: [integer('2')] },
            { type: 'PlusMinus', operands: [integer('3')] },
          ],
        },
        latex: ['\\pm 1\\pm 2\\pm 3', '±1±2±3'],
      },

      {
        solver: '1 +/- (+/-2)',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            {
              type: 'PlusMinus',
              operands: [
                {
                  type: 'PlusMinus',
                  decorators: ['RoundBracket'],
                  operands: [integer('2')],
                },
              ],
            },
          ],
        },
        latex: ['1\\pm \\left(\\pm 2\\right)', '1±(±2)'],
      },

      {
        solver: '+/-[2 ^ 4]',
        json: {
          type: 'PlusMinus',
          operands: [
            {
              type: 'Power',
              operands: [integer('2'), integer('4')],
            },
          ],
        },
        latex: ['\\pm {2}^{4}', '±2^4'],
      },
    ]);
  });

  describe('Implicit Products', () => {
    testCases([
      {
        solver: '1 (2)',
        json: {
          type: 'ImplicitProduct',
          operands: [
            integer('1'),
            {
              type: 'Integer',
              decorators: ['RoundBracket'],
              value: '2',
            },
          ],
        },
        latex: '1\\left(2\\right)',
      },

      {
        solver: '(1) (2) 3',
        json: {
          type: 'ImplicitProduct',
          operands: [
            {
              type: 'Integer',
              decorators: ['RoundBracket'],
              value: '1',
            },
            {
              type: 'Integer',
              decorators: ['RoundBracket'],
              value: '2',
            },
            integer('3'),
          ],
        },
        latex: '\\left(1\\right)\\left(2\\right)3',
      },

      {
        solver: '(1) (2) 4 (-5)',
        json: {
          type: 'ImplicitProduct',
          operands: [
            {
              type: 'Integer',
              decorators: ['RoundBracket'],
              value: '1',
            },
            {
              type: 'Integer',
              decorators: ['RoundBracket'],
              value: '2',
            },
            integer('4'),
            {
              type: 'Minus',
              decorators: ['RoundBracket'],
              operands: [integer('5')],
            },
          ],
        },
        latex: '\\left(1\\right)\\left(2\\right)4\\left(-5\\right)',
      },

      {
        solver: '2 a * 3 a',
        json: {
          type: 'Product',
          operands: [
            {
              type: 'ImplicitProduct',
              operands: [integer('2'), variable('a')],
            },
            {
              type: 'ImplicitProduct',
              operands: [integer('3'), variable('a')],
            },
          ],
        },
        latex: '2a \\cdot 3a',
      },
    ]);
  });

  describe('Equations', () => {
    testCases([
      {
        solver: '3 = 1 + 2',
        json: {
          type: 'Equation',
          operands: [
            integer('3'),
            { type: 'Sum', operands: [integer('1'), integer('2')] },
          ],
        },
        latex: '3 = 1+2',
      },
    ]);
  });

  describe('Powers', () => {
    testCases([
      {
        solver: '-[3 ^ 4]',
        json: {
          type: 'Minus',
          operands: [
            {
              type: 'Power',
              operands: [integer('3'), integer('4')],
            },
          ],
        },
        latex: ['-{3}^{4}', '-3^4'],
      },
      {
        solver: '[(-3) ^ -4]',
        json: {
          type: 'Power',
          operands: [
            { type: 'Minus', decorators: ['RoundBracket'], operands: [integer('3')] },
            { type: 'Minus', operands: [integer('4')] },
          ],
        },
        latex: ['{\\left(-3\\right)}^{-4}', '(-3)^{-4}'],
      },
      {
        solver: '[3 ^ [4 ^ 5]]',
        json: {
          type: 'Power',
          operands: [
            integer('3'),
            {
              type: 'Power',
              operands: [integer('4'), integer('5')],
            },
          ],
        },
        latex: ['{3}^{{4}^{5}}', '3^4^5'],
      },
      {
        solver: '[3 ^ [1 / 2]]',
        json: {
          type: 'Power',
          operands: [
            integer('3'),
            {
              type: 'Fraction',
              operands: [integer('1'), integer('2')],
            },
          ],
        },
        latex: ['{3}^{\\frac{1}{2}}', '3^\\frac{1}{2}'],
      },
      {
        solver: '2 + 3 * [4 ^ 5] * 6 + 7',
        json: {
          type: 'Sum',
          operands: [
            integer('2'),
            {
              type: 'Product',
              operands: [
                integer('3'),
                {
                  type: 'Power',
                  operands: [integer('4'), integer('5')],
                },
                integer('6'),
              ],
            },
            integer('7'),
          ],
        },
        latex: ['2+3 \\cdot {4}^{5} \\cdot 6+7', '2+3*4^5*6+7'],
      },
    ]);
  });

  describe('Variables', () => {
    testCases([
      {
        solver: 'a + b c * D',
        json: {
          type: 'Sum',
          operands: [
            variable('a'),
            {
              type: 'Product',
              operands: [
                {
                  type: 'ImplicitProduct',
                  operands: [variable('b'), variable('c')],
                },
                variable('D'),
              ],
            },
          ],
        },
        latex: 'a+bc \\cdot D',
      },
    ]);
  });

  describe('Greek letter variables', () => {
    testCases([
      {
        solver: '\\alpha + x + \\Beta',
        json: {
          type: 'Sum',
          operands: [variable('\\alpha'), variable('x'), variable('\\Beta')],
        },
        latex: ['\\alpha+x+\\Beta', '\\alpha{}+x+\\Beta{}'],
      },
      {
        solver: 'x [\\alpha ^ 2]',
        json: {
          type: 'ImplicitProduct',
          operands: [
            variable('x'),
            {
              type: 'Power',
              operands: [variable('\\alpha'), integer('2')],
            },
          ],
        },
        latex: ['x{\\alpha}^{2}', 'x {\\alpha{} ^ 2}'],
      },
    ]);
  });

  describe('Fractions and Mixed Numbers', () => {
    testCases([
      {
        solver: '[2 / 3] + 1',
        json: {
          type: 'Sum',
          operands: [
            {
              type: 'Fraction',
              operands: [integer('2'), integer('3')],
            },
            integer('1'),
          ],
        },
        latex: '\\frac{2}{3}+1',
      },
      {
        solver: '[1 2 / 3]',
        json: {
          type: 'MixedNumber',
          operands: [integer('1'), integer('2'), integer('3')],
        },
        latex: ['1\\frac{2}{3}', '1 2/3'],
      },
      {
        solver: '[2 * 1 / 3 * 1]',
        json: {
          type: 'Fraction',
          operands: [
            { type: 'Product', operands: [integer('2'), integer('1')] },
            { type: 'Product', operands: [integer('3'), integer('1')] },
          ],
        },
        latex: '\\frac{2 \\cdot 1}{3 \\cdot 1}',
      },
      {
        solver: '-[2 + 1 / 3 * [1 / 2]]',
        json: {
          type: 'Minus',
          operands: [
            {
              type: 'Fraction',
              operands: [
                { type: 'Sum', operands: [integer('2'), integer('1')] },
                {
                  type: 'Product',
                  operands: [
                    integer('3'),
                    { type: 'Fraction', operands: [integer('1'), integer('2')] },
                  ],
                },
              ],
            },
          ],
        },
        latex: '-\\frac{2+1}{3 \\cdot \\frac{1}{2}}',
      },
    ]);
  });

  describe('Brackets', () => {
    testCases([
      {
        solver: '(1)',
        json: { type: 'Integer', decorators: ['RoundBracket'], value: '1' },
        latex: '\\left(1\\right)',
      },
      {
        solver: '(1 + 2)',
        json: {
          type: 'Sum',
          decorators: ['RoundBracket'],
          operands: [integer('1'), integer('2')],
        },
        latex: '\\left(1+2\\right)',
      },
      {
        solver: '([.{.1.} + 2.])',
        json: {
          type: 'Sum',
          decorators: ['SquareBracket', 'RoundBracket'],
          operands: [
            { type: 'Integer', decorators: ['CurlyBracket'], value: '1' },
            integer('2'),
          ],
        },
        latex: '\\left(\\left[\\left\\{1\\right\\}+2\\right]\\right)',
      },
      { latex: '\\left(a+b\\right) \\cdot c', solver: '(a + b) * c' },
      { latex: '((a+b))', solver: '((a + b))' },
      {
        latex: '\\left\\{\\left[\\left(x\\right)\\right]\\right\\}',
        solver: '{.[.(x).].}',
      },
      { latex: '2 (a)', solver: '2 (a)' },
    ]);
  });

  describe('Partial Sums', () => {
    testCases([
      {
        solver: '1 + <.-[1 / 2] + [1 / 3].>',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            {
              type: 'Sum',
              decorators: ['PartialBracket'],
              operands: [
                {
                  type: 'Minus',
                  operands: [
                    {
                      type: 'Fraction',
                      operands: [integer('1'), integer('2')],
                    },
                  ],
                },
                {
                  type: 'Fraction',
                  operands: [integer('1'), integer('3')],
                },
              ],
            },
          ],
        },
        latex: '1-\\frac{1}{2}+\\frac{1}{3}',
        dontParseLatex: true,
      },
      {
        solver: '1 + <.1 + 2.> + 3',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            {
              type: 'Sum',
              decorators: ['PartialBracket'],
              operands: [integer('1'), integer('2')],
            },
            integer('3'),
          ],
        },
        latex: '1+1+2+3',
        dontParseLatex: true,
      },
      {
        solver: '<.1 + 2.> + 3',
        json: {
          type: 'Sum',
          operands: [
            {
              type: 'Sum',
              decorators: ['PartialBracket'],
              operands: [integer('1'), integer('2')],
            },
            integer('3'),
          ],
        },
        latex: '1+2+3',
        dontParseLatex: true,
      },
    ]);
  });

  describe('Roots', async () => {
    testCases([
      {
        solver: 'sqrt[4]',
        json: {
          type: 'SquareRoot',
          operands: [integer('4')],
        },
        latex: ['\\sqrt{4}', '\\sqrt 4'],
      },
      {
        solver: '2 sqrt[4] sqrt[2]',
        json: {
          type: 'ImplicitProduct',
          operands: [
            integer('2'),
            {
              type: 'SquareRoot',
              operands: [integer('4')],
            },
            {
              type: 'SquareRoot',
              operands: [integer('2')],
            },
          ],
        },
        latex: '2\\sqrt{4}\\sqrt{2}',
      },
      { latex: '2 \\sqrt[3] 4', solver: '2 root[4, 3]' },
      {
        solver: 'root[4, 3]',
        json: {
          type: 'Root',
          operands: [integer('4'), integer('3')],
        },
        latex: ['\\sqrt[{3}]{4}', '\\sqrt[3] 4'],
      },
      {
        solver: '[2 ^ root[1 + 2 - 4 a, 3]]',
        json: {
          type: 'Power',
          operands: [
            integer('2'),
            {
              type: 'Root',
              operands: [
                {
                  type: 'Sum',
                  operands: [
                    integer('1'),
                    integer('2'),
                    {
                      type: 'Minus',
                      operands: [
                        {
                          type: 'ImplicitProduct',
                          operands: [integer('4'), variable('a')],
                        },
                      ],
                    },
                  ],
                },
                integer('3'),
              ],
            },
          ],
        },
        latex: '{2}^{\\sqrt[{3}]{1+2-4a}}',
      },
      {
        solver: '[2 ^ root[1 + 2, 3]]',
        json: {
          type: 'Power',
          operands: [
            integer('2'),
            {
              type: 'Root',
              operands: [
                {
                  type: 'Sum',
                  operands: [integer('1'), integer('2')],
                },
                integer('3'),
              ],
            },
          ],
        },
        latex: ['{2}^{\\sqrt[{3}]{1+2}}', '2^\\sqrt[3]{1+2}'],
      },
    ]);
  });

  describe('Sums and products', () => {
    testCases([
      {
        solver: '2.3 + 3',
        json: {
          type: 'Sum',
          operands: [decimal('2.3'), integer('3')],
        },
        latex: '2.3+3',
      },
      {
        solver: '1 - 2 * 3',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            {
              type: 'Minus',
              operands: [{ type: 'Product', operands: [integer('2'), integer('3')] }],
            },
          ],
        },
        latex: '1-2 \\cdot 3',
      },
      {
        solver: '2 a : 3',
        json: {
          type: 'Product',
          operands: [
            { type: 'ImplicitProduct', operands: [integer('2'), variable('a')] },
            { type: 'DivideBy', operands: [integer('3')] },
          ],
        },
        latex: '2a \\div 3',
      },
    ]);
  });

  describe('Variables', () => {
    testCases([
      {
        solver: 'a A a',
        json: {
          type: 'ImplicitProduct',
          operands: [variable('a'), variable('A'), variable('a')],
        },
        latex: 'aAa',
      },
    ]);
  });

  describe('Numbers with repeating decimals', () => {
    testCases([
      {
        solver: '2.1[33]',
        json: { type: 'RecurringDecimal', value: '2.1[33]' },
        latex: '2.1\\overline{33}',
      },
      {
        solver: '0.[05]',
        json: { type: 'RecurringDecimal', value: '0.[05]' },
        latex: '0.\\overline{05}',
      },
    ]);
  });

  describe('Round, square and curly brackets', async () => {
    testCases([
      {
        solver: '(1 + 2) * 3',
        json: {
          type: 'Product',
          operands: [
            {
              type: 'Sum',
              decorators: ['RoundBracket'],
              operands: [integer('1'), integer('2')],
            },
            integer('3'),
          ],
        },
        latex: '\\left(1+2\\right) \\cdot 3',
      },
      {
        solver: '{.[.(x).].}',
        json: {
          type: 'Variable',
          decorators: ['RoundBracket', 'SquareBracket', 'CurlyBracket'],
          value: 'x',
        },
        latex: '\\left\\{\\left[\\left(x\\right)\\right]\\right\\}',
      },
    ]);
  });

  describe('Fractions and mixed numbers', async () => {
    testCases([
      {
        solver: '[1 / 2]',
        json: {
          type: 'Fraction',
          operands: [integer('1'), integer('2')],
        },
        latex: ['\\frac{1}{2}', '\\dfrac{1}{2}', '\\tfrac{1}{2}'],
      },
      {
        solver: '[a / b]',
        json: {
          type: 'Fraction',
          operands: [variable('a'), variable('b')],
        },
        latex: ['\\frac{a}{b}', '\\frac ab', 'a/b'],
      },
      {
        solver: '[1 / 2] x',
        latex: ['\\frac{1}{2}x', '1/2x'],
        json: {
          type: 'ImplicitProduct',
          operands: [
            {
              type: 'Fraction',
              operands: [integer('1'), integer('2')],
            },
            variable('x'),
          ],
        },
      },
      // Decimals cannot be used in mixed numbers
      { solver: '2.1 [1 / 2]', latex: ['2.1\\frac{1}{2}', '2.1 1/2'] },
      { solver: '2 [1.3 / 2]', latex: ['2\\frac{1.3}{2}', '2 1.3/2'] },
      { solver: '2 [1 / 2.3]', latex: ['2\\frac{1}{2.3}', '2 1/2.3'] },
      { solver: '2 [1 / [2 ^ 3]]', latex: ['2\\frac{1}{2^3}', '2 1/2^3'] },
      {
        solver: '[[1 / 2] / 3]',
        json: {
          type: 'Fraction',
          operands: [
            {
              type: 'Fraction',
              operands: [integer('1'), integer('2')],
            },
            integer('3'),
          ],
        },
        latex: ['\\frac{\\frac{1}{2}}{3}', '1/2/3'],
      },
      { latex: '2+\\frac{1}{2}', solver: '2 + [1 / 2]' },
      { solver: '[-3 / -4]', latex: ['-3/-4', '(-3)/(-4)'] },
      { solver: '2 [x / 3] x', latex: ['2\\frac{x}{3}x', '2x/3x'] },
      {
        solver: '1 + [2 / [3 ^ x]] - 3',
        latex: ['1+\\frac{2}{3^x}-3', '1+2/3^x-3', '1+2/(3^x)-3'],
      },
      { solver: '[1 + 2 / 3]', latex: ['\\frac{1+2}{3}', '(1+2)/3'] },
    ]);
  });

  describe('Relations', async () => {
    testCases([
      {
        solver: 'x = 5',
        json: {
          type: 'Equation',
          operands: [variable('x'), integer('5')],
        },
        latex: 'x = 5',
      },
      {
        solver: 'x < 5',
        json: {
          type: 'LessThan',
          operands: [variable('x'), integer('5')],
        },
        latex: 'x < 5',
      },
      {
        solver: 'x > 5',
        json: {
          type: 'GreaterThan',
          operands: [variable('x'), integer('5')],
        },
        latex: 'x > 5',
      },
      {
        solver: 'x <= 5',
        json: {
          type: 'LessThanEqual',
          operands: [variable('x'), integer('5')],
        },
        latex: ['x \\leq 5', 'x ≤ 5'],
      },
      {
        solver: 'x >= 5',
        json: {
          type: 'GreaterThanEqual',
          operands: [variable('x'), integer('5')],
        },
        latex: ['x \\geq 5', 'x ≥ 5'],
      },
    ]);
  });

  describe('Solutions and sets', async () => {
    testCases([
      {
        solver: 'SetSolution[x: {5}]',
        json: {
          type: 'SetSolution',
          operands: [
            { type: 'VariableList', operands: [variable('x')] },
            { type: 'FiniteSet', operands: [integer('5')] },
          ],
        },
        latex: 'x \\in \\left\\{5\\right\\}',
        dontParseLatex: true,
      },
      {
        solver: 'SetSolution[x: {5, 2}]',
        json: {
          type: 'SetSolution',
          operands: [
            { type: 'VariableList', operands: [variable('x')] },
            { type: 'FiniteSet', operands: [integer('5'), integer('2')] },
          ],
        },
        latex: 'x \\in \\left\\{5, 2\\right\\}',
        dontParseLatex: true,
      },
      {
        solver: 'SetSolution[x: (-/infinity/, 5)]',
        json: {
          type: 'SetSolution',
          operands: [
            { type: 'VariableList', operands: [variable('x')] },
            {
              type: 'OpenInterval',
              operands: [
                { type: 'Minus', operands: [{ type: 'Infinity' }] },
                integer('5'),
              ],
            },
          ],
        },
        latex: 'x \\in \\left( -\\infty, 5 \\right)',
        dontParseLatex: true,
      },
      {
        solver: 'SetSolution[x: [0, 5]]',
        json: {
          type: 'SetSolution',
          operands: [
            { type: 'VariableList', operands: [variable('x')] },
            {
              type: 'ClosedInterval',
              operands: [integer('0'), integer('5')],
            },
          ],
        },
        latex: 'x \\in \\left[ 0, 5 \\right]',
        dontParseLatex: true,
      },
      {
        solver: 'SetSolution[x: (-/infinity/, 5]]',
        json: {
          type: 'SetSolution',
          operands: [
            { type: 'VariableList', operands: [variable('x')] },
            {
              type: 'OpenClosedInterval',
              operands: [
                { type: 'Minus', operands: [{ type: 'Infinity' }] },
                integer('5'),
              ],
            },
          ],
        },
        latex: 'x \\in \\left( -\\infty, 5 \\right]',
        dontParseLatex: true,
      },
      {
        solver: 'SetSolution[x: [5, /infinity/)]',
        json: {
          type: 'SetSolution',
          operands: [
            { type: 'VariableList', operands: [variable('x')] },
            {
              type: 'ClosedOpenInterval',
              operands: [integer('5'), { type: 'Infinity' }],
            },
          ],
        },
        latex: 'x \\in \\left[ 5, \\infty \\right)',
        dontParseLatex: true,
      },
      {
        solver: 'SetSolution[x: {}]',
        json: {
          type: 'SetSolution',
          operands: [
            {
              type: 'VariableList',
              operands: [
                {
                  type: 'Variable',
                  value: 'x',
                },
              ],
            },
            {
              type: 'FiniteSet',
            },
          ],
        },
        latex: 'x \\in \\emptyset',
        dontParseLatex: true,
      },
      {
        solver: 'Identity[x: 3 = 3]',
        json: {
          type: 'Identity',
          operands: [
            { type: 'VariableList', operands: [variable('x')] },
            { type: 'Equation', operands: [integer('3'), integer('3')] },
          ],
        },
        latex: 'x \\in \\mathbb{R}',
        dontParseLatex: true,
      },
      {
        solver: 'Identity[3 = 3]',
        json: {
          type: 'Identity',
          operands: [
            { type: 'VariableList' },
            { type: 'Equation', operands: [integer('3'), integer('3')] },
          ],
        },
        latex: '\\top',
        dontParseLatex: true,
      },
      {
        solver: 'Contradiction[x: 3 != 3]',
        json: {
          type: 'Contradiction',
          operands: [
            { type: 'VariableList', operands: [variable('x')] },
            { type: 'Inequation', operands: [integer('3'), integer('3')] },
          ],
        },
        latex: 'x \\in \\emptyset',
        dontParseLatex: true,
      },
      {
        solver: 'Contradiction[3 != 3]',
        json: {
          type: 'Contradiction',
          operands: [
            { type: 'VariableList' },
            { type: 'Inequation', operands: [integer('3'), integer('3')] },
          ],
        },
        latex: '\\bot',
        dontParseLatex: true,
      },
    ]);
  });

  describe('Undefined', async () => {
    testCases([
      {
        solver: '/undefined/',
        latex: '\\text{undefined}',
        json: { type: 'Undefined' },
      },
    ]);
  });

  describe('Text style latex commands', async () => {
    testCases([
      {
        solver: 'x',
        json: variable('x'),
        latex: ['x', '\\mathrm{x}', '\\textit{x}'],
      },
      {
        solver: '1 + 2',
        json: { type: 'Sum', operands: [integer('1'), integer('2')] },
        latex: [
          '1+2',
          '\\mathrm{1+2}',
          '\\textit{1} + \\mathrm{2}',
          '\\mathit{1 + \\textit{2}}',
        ],
      },
      {
        solver: 'x x',
        json: { type: 'ImplicitProduct', operands: [variable('x'), variable('x')] },
        latex: ['xx', 'x\\nbsp{}x', '\\nbsp{}x\\nbsp{}x\\nbsp{}'],
      },
    ]);
  });

  describe('Absolute value', async () => {
    testCases([
      {
        solver: 'abs[x + 1]',
        latex: ['\\left|x+1\\right|', '|x+1|'],
        json: {
          type: 'AbsoluteValue',
          operands: [
            {
              type: 'Sum',
              operands: [variable('x'), integer('1')],
            },
          ],
        },
      },
      {
        solver: '2 abs[x + 1]',
        json: {
          type: 'ImplicitProduct',
          operands: [
            integer('2'),
            {
              type: 'AbsoluteValue',
              operands: [
                {
                  type: 'Sum',
                  operands: [variable('x'), integer('1')],
                },
              ],
            },
          ],
        },
        latex: ['2\\left|x+1\\right|', '2|x + 1|'],
      },
      {
        solver: '2 abs[abs[x + 2] + 1] + 5',
        json: {
          type: 'Sum',
          operands: [
            {
              type: 'ImplicitProduct',
              operands: [
                integer('2'),
                {
                  type: 'AbsoluteValue',
                  operands: [
                    {
                      type: 'Sum',
                      operands: [
                        {
                          type: 'AbsoluteValue',
                          operands: [
                            { type: 'Sum', operands: [variable('x'), integer('2')] },
                          ],
                        },
                        integer('1'),
                      ],
                    },
                  ],
                },
              ],
            },
            integer('5'),
          ],
        },
        latex: ['2\\left|\\left|x+2\\right|+1\\right|+5', '2||x+2|+1|+5'],
      },
      {
        solver: 'abs[x + 1] abs[x + 3]',
        json: {
          type: 'ImplicitProduct',
          operands: [
            {
              type: 'AbsoluteValue',
              operands: [{ type: 'Sum', operands: [variable('x'), integer('1')] }],
            },
            {
              type: 'AbsoluteValue',
              operands: [{ type: 'Sum', operands: [variable('x'), integer('3')] }],
            },
          ],
        },
        latex: ['\\left|x+1\\right|\\left|x+3\\right|', '|x+1||x+3|'],
      },
      {
        solver: 'abs[abs[x] + abs[y]]',
        latex: ['\\left|\\left|x\\right| + \\left|y\\right|\\right|', '||x| + |y||'],
      },
      {
        solver: 'abs[(abs[1])]',
        latex: ['\\left|(\\left|1\\right|)\\right|', '|(|1|)|'],
      },
    ]);
  });
  describe('subscripts', async () => {
    testCases([
      // Digit subscript
      {
        solver: 'x_1',
        json: {
          type: 'Variable',
          value: 'x',
          subscript: '1',
        },
        latex: ['x_{1}', 'x_1'],
      },
      // Variable subscript
      {
        solver: 'A_n',
        json: {
          type: 'Variable',
          value: 'A',
          subscript: 'n',
        },
        latex: ['A_{n}', 'A_n'],
      },
      // Check precedence and spacing adjustment
      {
        solver: '[x_1 ^ y_2]',
        json: {
          type: 'Power',
          operands: [
            {
              type: 'Variable',
              value: 'x',
              subscript: '1',
            },
            {
              type: 'Variable',
              value: 'y',
              subscript: '2',
            },
          ],
        },
        latex: ['x_{1}^{\\,y_{2}}', 'x_1^y_2'],
      },
      // Check no spacing adjustment when subscripted variable has brackets
      {
        solver: '[(x_1) ^ 2]',
        json: {
          type: 'Power',
          operands: [
            {
              type: 'Variable',
              value: 'x',
              subscript: '1',
              decorators: ['RoundBracket'],
            },
            integer('2'),
          ],
        },
        latex: ['{\\left(x_{1}\\right)}^{2}', '(x_1)^2'],
      },
    ]);
  });

  describe('Trigonometric functions', () => {
    testCases([
      {
        solver: 'sin x',
        json: {
          type: 'Sin',
          operands: [variable('x')],
        },
        latex: ['\\sin{x}'],
      },
      {
        solver: 'arcsin x',
        json: {
          type: 'Arcsin',
          operands: [variable('x')],
        },
        latex: ['\\arcsin{x}'],
      },
      {
        solver: 'sinh x',
        json: {
          type: 'Sinh',
          operands: [variable('x')],
        },
        latex: ['\\sinh{x}'],
      },
      {
        solver: 'sin (x)',
        json: {
          type: 'Sin',
          operands: [
            {
              type: 'Variable',
              value: 'x',
              decorators: ['RoundBracket'],
            },
          ],
        },
        latex: [
          '\\sin{\\left(x\\right)}',
          '{{\\mathrm{sin}}\\left(x\\right)}', // ggb keyboard output
        ],
      },
      {
        solver: '[sin x ^ 2]',
        json: {
          type: 'Power',
          operands: [
            {
              type: 'Sin',
              operands: [variable('x')],
            },
            integer('2'),
          ],
        },
        latex: [
          '{\\sin{x}}^{2}',
          '{{\\mathrm{sin}} x}^{2}', // ggb keyboard output
        ],
      },
      {
        solver: '[(sin x) ^ 2]',
        json: {
          type: 'Power',
          operands: [
            {
              type: 'Sin',
              operands: [variable('x')],
              decorators: ['RoundBracket'],
            },
            integer('2'),
          ],
        },
        latex: ['{\\left(\\sin{x}\\right)}^{2}'],
      },
      {
        solver: 'sin x + 1',
        json: {
          type: 'Sum',
          operands: [
            {
              type: 'Sin',
              operands: [variable('x')],
            },
            integer('1'),
          ],
        },
        latex: ['\\sin{x}+1'],
      },
      {
        solver: 'sin (x) + 1',
        json: {
          type: 'Sum',
          operands: [
            {
              type: 'Sin',
              operands: [
                {
                  type: 'Variable',
                  value: 'x',
                  decorators: ['RoundBracket'],
                },
              ],
            },
            integer('1'),
          ],
        },
        latex: [
          '\\sin{\\left(x\\right)}+1',
          '{{\\mathrm{sin}}\\left(x\\right)}+1', // ggb keyboard output
        ],
      },
      {
        solver: 'sin 2 x',
        json: {
          type: 'Sin',
          operands: [
            {
              type: 'ImplicitProduct',
              operands: [integer('2'), variable('x')],
            },
          ],
        },
        latex: ['\\sin{2x}'],
      },
      {
        solver: 'sin (2 x)',
        json: {
          type: 'Sin',
          operands: [
            {
              type: 'ImplicitProduct',
              operands: [integer('2'), variable('x')],
              decorators: ['RoundBracket'],
            },
          ],
        },
        latex: [
          '\\sin{\\left(2x\\right)}',
          '{{\\mathrm{sin}}\\left({2x}\\right)}', // ggb keyboard output
        ],
      },
      {
        solver: '[(sin (x)) ^ 2]',
        json: {
          type: 'Power',
          operands: [
            {
              type: 'Sin',
              operands: [
                {
                  type: 'Variable',
                  value: 'x',
                  decorators: ['RoundBracket'],
                },
              ],
              decorators: ['RoundBracket'],
            },
            integer('2'),
          ],
        },
        latex: [
          '{\\left(\\sin{\\left(x\\right)}\\right)}^{2}',
          '\\left({{{\\mathrm{sin}}\\left(x\\right)}}\\right)^{2}', // ggb keyboard output
        ],
      },
      {
        // TODO: add "json" and "solver" syntax for below once we agree on their syntax's
        latex: ['\\sin^{2} x', '\\sin^2 x', '{{\\mathrm{sin}}^{2} x}'],
      },
      {
        solver: 'tan (x)',
        json: {
          type: 'Tan',
          operands: [
            {
              type: 'Variable',
              value: 'x',
              decorators: ['RoundBracket'],
            },
          ],
        },
        latex: [
          '\\tan{\\left(x\\right)}',
          '{{\\mathrm{tan}}\\left(x\\right)}', // ggb keyboard output
        ],
      },
    ]);
  });

  describe('Logarithm', () => {
    testCases([
      {
        solver: 'log x',
        json: {
          type: 'Log10',
          operands: [variable('x')],
        },
        latex: ['\\log{x}'],
      },
      {
        solver: 'log 2 x',
        json: {
          type: 'Log10',
          operands: [
            {
              type: 'ImplicitProduct',
              operands: [integer('2'), variable('x')],
            },
          ],
        },
        latex: ['\\log{2x}'],
      },
      {
        solver: '1 + 6 * log x',
        json: {
          type: 'Sum',
          operands: [
            integer('1'),
            {
              type: 'Product',
              operands: [
                integer('6'),
                {
                  type: 'Log10',
                  operands: [variable('x')],
                },
              ],
            },
          ],
        },
        latex: ['1+6 \\cdot \\log{x}'],
      },
      {
        solver: 'ln 2 x + 1',
        json: {
          type: 'Sum',
          operands: [
            {
              type: 'Ln',
              operands: [
                {
                  type: 'ImplicitProduct',
                  operands: [integer('2'), variable('x')],
                },
              ],
            },
            integer('1'),
          ],
        },
        latex: ['\\ln{2x}+1'],
      },
      {
        solver: 'log (x + 1)',
        json: {
          type: 'Log10',
          operands: [
            {
              type: 'Sum',
              operands: [variable('x'), integer('1')],
              decorators: ['RoundBracket'],
            },
          ],
        },
        latex: ['\\log{\\left(x+1\\right)}'],
      },
      {
        solver: 'ln x',
        json: {
          type: 'Ln',
          operands: [variable('x')],
        },
        latex: ['\\ln{x}', '{{\\mathrm{\\mathrm{ln}}} x}'],
      },
      {
        solver: 'ln (x + 1)',
        json: {
          type: 'Ln',
          operands: [
            {
              type: 'Sum',
              operands: [variable('x'), integer('1')],
              decorators: ['RoundBracket'],
            },
          ],
        },
        latex: [
          '\\ln{\\left(x+1\\right)}',
          '{{\\mathrm{\\mathrm{ln}}}\\left({x+1}\\right)}', // ggb keyboard output
        ],
      },
      {
        json: {
          type: 'Log',
          operands: [integer('10'), variable('x')],
        },
        latex: ['\\log_{10}{x}'],
      },
      {
        solver: 'log[5] x',
        json: {
          type: 'Log',
          operands: [integer('5'), variable('x')],
        },
        latex: ['\\log_{5}{x}'],
      },
      {
        solver: 'log[a + 1] (x)',
        json: {
          type: 'Log',
          operands: [
            {
              type: 'Sum',
              operands: [variable('a'), integer('1')],
            },
            {
              type: 'Variable',
              value: 'x',
              decorators: ['RoundBracket'],
            },
          ],
        },
        latex: ['\\log_{a+1}{\\left(x\\right)}'],
      },
      {
        solver: '[(log[5] x + 1) ^ 2]',
        json: {
          type: 'Power',
          operands: [
            {
              type: 'Sum',
              operands: [
                {
                  type: 'Log',
                  operands: [integer('5'), variable('x')],
                },
                integer('1'),
              ],
              decorators: ['RoundBracket'],
            },
            integer('2'),
          ],
        },
        latex: ['{\\left(\\log_{5}{x}+1\\right)}^{2}'],
      },
    ]);
  });
});
