import { describe, expect, it } from 'vitest';
import {
  coloringTransformer,
  createColorMaps,
  latexToTree,
  PathMapping,
  treeToLatex,
} from '../src';
import { ColorMap } from '../src/solutions/coloring';

function checkColorMaps({
  pathMappings,
  colors,
  expectedFromMap,
  expectedToMap,
}: {
  pathMappings: PathMapping[];
  colors: string[];
  expectedFromMap: ColorMap;
  expectedToMap: ColorMap;
}) {
  const [fromMap, toMap] = createColorMaps(pathMappings, colors);
  expect(fromMap).to.deep.equal(expectedFromMap);
  expect(toMap).to.deep.equal(expectedToMap);
}

describe('createColorMaps', () => {
  it('gives one color to each path mapping', () => {
    checkColorMaps({
      pathMappings: [
        { type: 'Combine', fromPaths: ['A1a', 'A1b'], toPaths: ['B1'] },
        { type: 'Introduce', fromPaths: [], toPaths: ['B2'] },
        { type: 'Distribute', fromPaths: ['A3'], toPaths: ['B3a', 'B3b'] },
      ],
      colors: ['red', 'blue', 'green'],
      expectedFromMap: { A1a: 'red', A1b: 'red', A3: 'green' },
      expectedToMap: { B1: 'red', B2: 'blue', B3a: 'green', B3b: 'green' },
    });
  });
  it('cycles through available colors', () =>
    checkColorMaps({
      pathMappings: [
        { type: 'Move', fromPaths: ['A1'], toPaths: ['B1'] },
        { type: 'Move', fromPaths: ['A2'], toPaths: ['B2'] },
        { type: 'Move', fromPaths: ['A3'], toPaths: ['B3'] },
      ],
      colors: ['red', 'blue'],
      expectedFromMap: { A1: 'red', A2: 'blue', A3: 'red' },
      expectedToMap: { B1: 'red', B2: 'blue', B3: 'red' },
    }));
  it('Skip "Shift" path mappings', () =>
    checkColorMaps({
      pathMappings: [
        { type: 'Move', fromPaths: ['A1'], toPaths: ['B1'] },
        { type: 'Shift', fromPaths: ['A2'], toPaths: ['B2'] },
        { type: 'Move', fromPaths: ['A3'], toPaths: ['B3'] },
      ],
      colors: ['red', 'blue'],
      expectedFromMap: { A1: 'red', A3: 'blue' },
      expectedToMap: { B1: 'red', B3: 'blue' },
    }));
});

describe('coloringTransformer', () => {
  it('colors an expression at a path if present in the color map', () => {
    const t = coloringTransformer({ '.': 'red' }, 'black');
    expect(treeToLatex(latexToTree('A'), null, t)).to.equal(
      '\\color{red}A\\color{black}',
    );
  });
  it('colors a minus node and operand in a sum', () => {
    const t = coloringTransformer({ './0': 'red', './1': 'blue', './2': 'red' }, 'black');
    const tree = latexToTree('1 - 2 + 3');
    expect(treeToLatex(tree, null, t)).to.equal(
      '\\color{red}1\\color{black}\\color{blue}{}-2\\color{black}+\\color{red}{}3\\color{black}',
    );
  });
  it('colors the decorator around a variable', () => {
    const t = coloringTransformer({ './0:decorator': 'red' }, 'black');
    const tree = latexToTree('-(-x)');
    expect(treeToLatex(tree, null, t)).to.equal(
      '-\\color{red}\\left(\\color{black}-x\\color{red}\\right)\\color{black}',
    );
  });
  it('colors the decorator around a sum', () => {
    const t = coloringTransformer({ './0:decorator': 'red' }, 'black');
    const tree = latexToTree('-(x + y)');
    expect(treeToLatex(tree, null, t)).to.equal(
      '-\\color{red}\\left(\\color{black}x+y\\color{red}\\right)\\color{black}',
    );
  });
  it('colors the decorator around a product containing a sum', () => {
    const t = coloringTransformer({ './1/0/1:decorator': 'red' }, 'black');
    const tree = latexToTree('1 -5(x + y)');
    expect(treeToLatex(tree, null, t)).to.equal(
      '1-5\\color{red}\\left(\\color{black}x+y\\color{red}\\right)\\color{black}',
    );
  });
  // only color the "op" (i.e. operator) at that path
  it('color only the negative operator and some other operand', () => {
    const t = coloringTransformer({ './1:op': 'blue', './2': 'red' }, 'black');
    const tree = latexToTree('1 - 2 + 3');
    expect(treeToLatex(tree, null, t)).to.equal(
      '1\\color{blue}{}-\\color{black}2+\\color{red}{}3\\color{black}',
    );
  });
  it('color only the negative operator and some other operand', () => {
    const t = coloringTransformer({ './1:outerOp': 'blue' }, 'black');
    const tree = latexToTree('1 + 2 + 3');
    expect(treeToLatex(tree, null, t)).to.equal('1\\color{blue}+\\color{black}2+3');
  });
  // color the "outerOp" (i.e. operator next to that path in n-ary tree)
  it('color the outerOperator in a sum', () => {
    const t = coloringTransformer({ './1:outerOp': 'blue', './2': 'red' }, 'black');
    const tree = latexToTree('1 + 2 + 3');
    expect(treeToLatex(tree, null, t)).to.equal(
      '1\\color{blue}+\\color{black}2+\\color{red}{}3\\color{black}',
    );
  });
  it('color the outerOperator in a product', () => {
    const t = coloringTransformer({ './2:outerOp': 'red' }, 'black');
    const tree = latexToTree('1*2*0*3');
    expect(treeToLatex(tree, null, t)).to.equal(
      '1 \\cdot 2\\color{red} \\cdot \\color{black}0 \\cdot 3',
    );
  });
  it('color only the multiple operator', () => {
    const t = coloringTransformer({ '.:op': 'blue', './0:op': 'red' }, 'black');
    const tree = latexToTree('-(-x)');
    expect(treeToLatex(tree, null, t)).to.equal(
      '\\color{blue}-\\color{black}\\left(\\color{red}-\\color{black}x\\right)',
    );
  });
  it('color an n-ary operator in a fraction with sum denominator', () => {
    const t = coloringTransformer({ './1:op': 'red' }, 'black');
    const tree = latexToTree('\\frac{5}{1 + 2 + 3}');
    expect(treeToLatex(tree, null, t)).to.equal(
      '\\frac{5}{1\\color{red}+\\color{black}2\\color{red}+\\color{black}3}',
    );
  });
  it('color only the multiple operator', () => {
    const t = coloringTransformer({ '.:op': 'blue' }, 'black');
    const tree = latexToTree('-x');
    expect(treeToLatex(tree, null, t)).to.equal(`\\color{blue}-\\color{black}x`);
  });
  it('color only the decorator of the current expression', () => {
    const t = coloringTransformer({ './0:decorator': 'red' }, 'black');
    const tree = latexToTree('-(-x)');
    expect(treeToLatex(tree, null, t)).to.equal(
      '-\\color{red}\\left(\\color{black}-x\\color{red}\\right)\\color{black}',
    );
  });
  it('color operands with decorators', () => {
    const t = coloringTransformer(
      { './0': 'red', './1': 'blue', './2': 'green' },
      'black',
    );
    const tree = latexToTree('- 1 - (2) + (-3)');
    expect(treeToLatex(tree, null, t)).to.equal(
      '\\color{red}-1\\color{black}\\color{blue}{}-\\left(2\\right)\\color{black}+\\color{green}{}\\left(-3\\right)\\color{black}',
    );
  });
  it('does not color an expression at a path if not in the color map', () => {
    const t = coloringTransformer({ './2': 'red' }, 'black');
    expect(treeToLatex(latexToTree('2^3'), null, t)).to.equal('{2}^{3}');
  });
  it('coloring using outerOperator for unary operator', () => {
    const t = coloringTransformer(
      {
        './0': 'red',
        './1/0': 'blue',
        './1/0:outerOp': 'green',
      },
      'black',
    );
    const tree = latexToTree('5 : 6');
    expect(treeToLatex(tree, null, t)).to.equal(
      '\\color{red}5\\color{black}\\color{green}{} \\div \\color{black}\\color{blue}6\\color{black}',
    );
  });
  it('color not equal operator', () => {
    const t = coloringTransformer({ '.:op': 'red' }, 'black');
    expect(treeToLatex(latexToTree('x \\neq 4'), null, t)).to.equal(
      'x \\color{red}\\neq\\color{black} 4',
    );
  });
  it('color absolute value operator', () => {
    const t = coloringTransformer({ '.:op': 'red' }, 'black');
    expect(treeToLatex(latexToTree('\\left|x + 1\\right|'), null, t)).to.equal(
      '\\color{red}\\left|\\color{black}x+1\\color{red}\\right|\\color{black}',
    );
  });
  it('red default color', () => {
    const t = coloringTransformer({ '.': 'blue' }, 'red');
    expect(treeToLatex(latexToTree('A'), null, t)).to.equal('\\color{blue}A\\color{red}');
  });
});
