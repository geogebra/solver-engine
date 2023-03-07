import { describe, it } from 'mocha';
import { expect } from 'chai';
import { latexToTree, treeToLatex, jsonToLatex } from '../src/parser';
import type { LatexSettings } from '../src/parser/tree-to-latex';
import { MathJson } from '../src/types';

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
    });
    testCasesWithLatexSettings([{ input: '1*2:3', output: '1 \\cdot 2:3' }], {
      divSymbol: ':',
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
