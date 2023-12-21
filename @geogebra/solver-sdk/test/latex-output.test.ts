import { describe, expect, it } from 'vitest';
import type { LatexSettings } from '../src';
import {
  jsonToLatex,
  latexToTree,
  MathJson,
  simpleSolutionFormatter,
  treeToLatex,
} from '../src';

function testCasesWithLatexSettings(
  cases: { input: string; output: string }[],
  settings?: LatexSettings,
) {
  cases.forEach(({ input, output }) => {
    it(`Solver "${input}" => Latex "${output}"`, () => {
      expect(treeToLatex(latexToTree(input), settings)).to.equal(output);
    });
  });
}

describe('Custom LaTeX output', () => {
  describe('Custom multiplication and division symbols', () => {
    testCasesWithLatexSettings([{ input: '1*2:3', output: '1\\cdot 2:3' }], {
      mulSymbol: '\\cdot ',
      divSymbol: ':',
      solutionFormatter: simpleSolutionFormatter,
    });
    testCasesWithLatexSettings([{ input: '1*2:3', output: '1 \\cdot 2:3' }], {
      divSymbol: ':',
      solutionFormatter: simpleSolutionFormatter,
    });
  });
});

const integer = (value: string): MathJson => ({ type: 'Integer', value });
const variable = (value: string): MathJson => ({ type: 'Variable', value });

it('Aligned equations in system', () => {
  const system: MathJson = {
    type: 'EquationSystem',
    operands: [
      { type: 'Equation', operands: [variable('a'), integer('1')] },
      { type: 'Equation', operands: [variable('b'), integer('2')] },
    ],
  };

  expect(jsonToLatex(system)).to.equal(
    '\\left\\{\\begin{array}{rclrr}\n  a & = & 1\\\\\n  b & = & 2\\\\\n\\end{array}\\right.',
  );
});

it('Aligned equations with labels in system', () => {
  const system: MathJson = {
    type: 'EquationSystem',
    operands: [
      { type: 'Equation', operands: [variable('a'), integer('1')], name: '(1)' },
      { type: 'Equation', operands: [variable('b'), integer('2')], name: '(2)' },
    ],
  };

  expect(jsonToLatex(system)).to.equal(
    '\\left\\{\\begin{array}{rclrr}\n  a & = & 1 & \\textrm{(1)}\\\\\n  b & = & 2 & \\textrm{(2)}\\\\\n\\end{array}\\right.',
  );
});

it('Aligned equations in union', () => {
  const union: MathJson = {
    type: 'EquationUnion',
    operands: [
      { type: 'Equation', operands: [variable('x'), integer('1')] },
      { type: 'Equation', operands: [variable('x'), integer('2')] },
    ],
  };

  expect(jsonToLatex(union)).to.equal('x = 1\\text{ or }x = 2');
});

it('Univariate finite set solution', () => {
  const solution: MathJson = {
    type: 'SetSolution',
    operands: [
      { type: 'VariableList', operands: [variable('x')] },
      {
        type: 'FiniteSet',
        operands: [{ type: 'Fraction', operands: [integer('1'), integer('2')] }],
      },
    ],
  };

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x = \\frac{1}{2}',
  );
});

it('Univariate empty set solution', () => {
  const solution: MathJson = {
    type: 'SetSolution',
    operands: [
      { type: 'VariableList', operands: [variable('x')] },
      {
        type: 'FiniteSet',
      },
    ],
  };

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    '\\text{no solution}',
  );
});

it('Univariate solution with holes', () => {
  const solution: MathJson = {
    type: 'SetSolution',
    operands: [
      { type: 'VariableList', operands: [variable('x')] },
      {
        type: 'SetDifference',
        operands: [
          { type: 'Reals' },
          { type: 'FiniteSet', operands: [integer('2'), integer('3')] },
        ],
      },
    ],
  };

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x \\neq 2 \\text{ and } x \\neq 3',
  );
});

it('Multivariate finite set solution', () => {
  const solution: MathJson = {
    type: 'SetSolution',
    operands: [
      { type: 'VariableList', operands: [variable('x'), variable('y')] },
      {
        type: 'FiniteSet',
        operands: [
          {
            type: 'Tuple',
            operands: [
              { type: 'Fraction', operands: [integer('1'), integer('2')] },
              { type: 'SquareRoot', operands: [integer('3')] },
            ],
          },
        ],
      },
    ],
  };

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x = \\frac{1}{2}, y = \\sqrt{3}',
  );
});

it('Multivariate cartesian product solution', () => {
  const solution: MathJson = {
    type: 'SetSolution',
    operands: [
      { type: 'VariableList', operands: [variable('x'), variable('y')] },
      {
        type: 'CartesianProduct',
        operands: [
          { type: 'Reals' },
          {
            type: 'FiniteSet',
            operands: [{ type: 'SquareRoot', operands: [integer('3')] }],
          },
        ],
      },
    ],
  };

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x \\in \\mathbb{R}, y = \\sqrt{3}',
  );
});

it('Univariate identity solution', () => {
  const solution: MathJson = {
    type: 'Identity',
    operands: [
      { type: 'VariableList', operands: [variable('x')] },
      { type: 'Equation', operands: [variable('x'), variable('x')] },
    ],
  };

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    '\\text{infinitely many solutions}',
  );
});

it('Multivariate identity solution', () => {
  const solution: MathJson = {
    type: 'Identity',
    operands: [
      { type: 'VariableList', operands: [variable('x'), variable('y')] },
      { type: 'Equation', operands: [integer('1'), integer('1')] },
    ],
  };

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x, y \\in \\mathbb{R}',
  );
});

it('Univariate contradiction solution', () => {
  const solution: MathJson = {
    type: 'Contradiction',
    operands: [
      { type: 'VariableList', operands: [variable('x')] },
      { type: 'Equation', operands: [integer('1'), integer('2')] },
    ],
  };

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    '\\text{no solution}',
  );
});

it('Multivariate contradiction solution', () => {
  const solution: MathJson = {
    type: 'Contradiction',
    operands: [
      { type: 'VariableList', operands: [variable('x'), variable('y')] },
      { type: 'GreaterThan', operands: [integer('1'), integer('2')] },
    ],
  };

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    '\\text{no solution}',
  );
});

it('Implicit solution', () => {
  const solution: MathJson = {
    type: 'ImplicitSolution',
    operands: [
      { type: 'VariableList', operands: [variable('x'), variable('y')] },
      {
        type: 'Equation',
        operands: [
          variable('x'),
          {
            type: 'Sum',
            operands: [
              {
                type: 'SmartProduct',
                signs: [false, false],
                operands: [integer('2'), variable('y')],
              },
              integer('3'),
            ],
          },
        ],
      },
    ],
  };

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x, y \\in \\mathbb{R} : x = 2y+3',
  );
});
