import { describe, it } from 'mocha';
import { expect } from 'chai';
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

describe('Aligned equations in system', () => {
  const system = [
    'EquationSystem',
    ['Equation', ['a'], ['1']],
    ['Equation', ['b'], ['2']],
  ] as MathJson;

  expect(jsonToLatex(system)).to.equal(
    '\\left\\{\\begin{array}{rcl}\n  a & = & 1\\\\\n  b & = & 2\\\\\n\\end{array}\\right.',
  );
});

describe('Aligned equations in union', () => {
  const union = [
    'EquationUnion',
    ['Equation', ['x'], ['1']],
    ['Equation', ['x'], ['2']],
  ] as MathJson;

  expect(jsonToLatex(union)).to.equal('x = 1, x = 2');
});

describe('Univariate finite set solution', () => {
  const solution = [
    'SetSolution',
    ['VariableList', ['x']],
    ['FiniteSet', ['Fraction', ['1'], ['2']]],
  ] as MathJson;

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x = \\frac{1}{2}',
  );
});

describe('Multivariate finite set solution', () => {
  const solution = [
    'SetSolution',
    ['VariableList', ['x'], ['y']],
    ['FiniteSet', ['Tuple', ['Fraction', ['1'], ['2']], ['SquareRoot', ['3']]]],
  ] as MathJson;

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x = \\frac{1}{2}, y = \\sqrt{3}',
  );
});

describe('Multivariate cartesian product solution', () => {
  const solution = [
    'SetSolution',
    ['VariableList', ['x'], ['y']],
    ['CartesianProduct', ['Reals'], ['FiniteSet', ['SquareRoot', ['3']]]],
  ] as MathJson;

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x \\in \\mathbb{R}, y = \\sqrt{3}',
  );
});

describe('Univariate identity solution', () => {
  const solution = [
    'Identity',
    ['VariableList', ['x']],
    ['Equation', ['x'], ['x']],
  ] as MathJson;

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x \\in \\mathbb{R}',
  );
});

describe('Multivariate identity solution', () => {
  const solution = [
    'Identity',
    ['VariableList', ['x'], ['y']],
    ['Equation', ['1'], ['1']],
  ] as MathJson;

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x, y \\in \\mathbb{R}',
  );
});

describe('Univariate contradiction solution', () => {
  const solution = [
    'Contradiction',
    ['VariableList', 'x'],
    ['Equation', ['1'], ['2']],
  ] as MathJson;

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x \\in \\emptyset',
  );
});

describe('Multivariate contradiction solution', () => {
  const solution = [
    'Contradiction',
    ['VariableList', 'x', 'y'],
    ['GreaterThan', ['1'], ['2']],
  ] as MathJson;

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x, y \\in \\emptyset',
  );
});

describe('ImplicitSolution', () => {
  const solution = [
    'ImplicitSolution',
    ['VariableList', ['x'], ['y']],
    ['Equation', ['x'], ['Sum', ['SmartProduct', [false, ['2']], [false, ['y']]], ['3']]],
  ] as MathJson;

  expect(jsonToLatex(solution, { solutionFormatter: simpleSolutionFormatter })).to.equal(
    'x, y \\in \\mathbb{R} : x = 2y+3',
  );
});
