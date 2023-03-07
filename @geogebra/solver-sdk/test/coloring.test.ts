import { describe, it } from 'mocha';
import { expect } from 'chai';

import {
  coloringTransformer,
  createColorMaps,
  latexToTree,
  treeToLatex,
} from '../src/parser';
import { ColorMap } from '../src/parser/coloring';
import { PathMapping } from '../src/types';

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
    const f = coloringTransformer({ '.': 'red' });
    expect(treeToLatex(latexToTree('A'), null, f)).to.equal('{\\color{red}A}');
  });
  it('colors a minus node in a sum', () => {
    const f = coloringTransformer({ './0': 'red', './1': 'blue', './2': 'red' });
    const tree = latexToTree('1 - 2 + 3');
    expect(treeToLatex(tree, null, f)).to.equal(
      '{\\color{red}1}{\\color{blue}{}-2}+{\\color{red}{}3}',
    );
  });
  it('colors a minus node in a sum', () => {
    const f = coloringTransformer({ './0': 'red', './1': 'blue', './2': 'green' });
    const tree = latexToTree('- 1 - (2) + (-3)');
    expect(treeToLatex(tree, null, f)).to.equal(
      '{\\color{red}-1}{\\color{blue}{}-\\left(2\\right)}+{\\color{green}{}\\left(-3\\right)}',
    );
  });
  it('does not color an expression at a path if not in the color map', () => {
    const f = coloringTransformer({ './2': 'red' });
    expect(treeToLatex(latexToTree('2^3'), null, f)).to.equal('{2}^{3}');
  });
});
