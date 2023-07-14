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

it('Aligned equations in system', () => {
  const system: MathJson = [
    'EquationSystem',
    ['Equation', ['a'], ['1']],
    ['Equation', ['b'], ['2']],
  ];

  expect(jsonToLatex(system)).to.equal(
    '\\left\\{\\begin{array}{rcl}\n  a & = & 1\\\\\n  b & = & 2\\\\\n\\end{array}\\right.',
  );
});

it('Aligned equations in union', () => {
  const union: MathJson = [
    'EquationUnion',
    ['Equation', ['x'], ['1']],
    ['Equation', ['x'], ['2']],
  ];

  expect(jsonToLatex(union)).to.equal('x = 1\\text{ or }x = 2');
});

it('Univariate finite set solution', () => {
  const solution: MathJson = [
    'SetSolution',
    ['VariableList', ['x']],
    ['FiniteSet', ['Fraction', ['1'], ['2']]],
  ];

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x = \\frac{1}{2}',
  );
});

it('Univariate solution with holes', () => {
  const solution: MathJson = [
    'SetSolution',
    ['VariableList', ['x']],
    ['SetDifference', ['Reals'], ['FiniteSet', ['2'], ['3']]],
  ];

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x \\neq 2 \\text{ and } x \\neq 3',
  );
});

it('Multivariate finite set solution', () => {
  const solution: MathJson = [
    'SetSolution',
    ['VariableList', ['x'], ['y']],
    ['FiniteSet', ['Tuple', ['Fraction', ['1'], ['2']], ['SquareRoot', ['3']]]],
  ];

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x = \\frac{1}{2}, y = \\sqrt{3}',
  );
});

it('Multivariate cartesian product solution', () => {
  const solution: MathJson = [
    'SetSolution',
    ['VariableList', ['x'], ['y']],
    ['CartesianProduct', ['Reals'], ['FiniteSet', ['SquareRoot', ['3']]]],
  ];

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x \\in \\mathbb{R}, y = \\sqrt{3}',
  );
});

it('Univariate identity solution', () => {
  const solution: MathJson = [
    'Identity',
    ['VariableList', ['x']],
    ['Equation', ['x'], ['x']],
  ];

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x \\in \\mathbb{R}',
  );
});

it('Multivariate identity solution', () => {
  const solution: MathJson = [
    'Identity',
    ['VariableList', ['x'], ['y']],
    ['Equation', ['1'], ['1']],
  ];

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x, y \\in \\mathbb{R}',
  );
});

it('Univariate contradiction solution', () => {
  const solution: MathJson = [
    'Contradiction',
    ['VariableList', ['x']],
    ['Equation', ['1'], ['2']],
  ];

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x \\in \\emptyset',
  );
});

it('Multivariate contradiction solution', () => {
  const solution: MathJson = [
    'Contradiction',
    ['VariableList', ['x'], ['y']],
    ['GreaterThan', ['1'], ['2']],
  ];

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x, y \\in \\emptyset',
  );
});

it('Implicit solution', () => {
  const solution: MathJson = [
    'ImplicitSolution',
    ['VariableList', ['x'], ['y']],
    ['Equation', ['x'], ['Sum', ['SmartProduct', [false, ['2']], [false, ['y']]], ['3']]],
  ];

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x, y \\in \\mathbb{R} : x = 2y+3',
  );
});
