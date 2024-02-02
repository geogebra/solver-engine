import { ExpressionTree } from '../parser';
import { treeToLatex, simpleSolutionFormatter } from '../renderer';
import {
  type NestedTree,
  type Tree,
  type TreeLeaf,
  generateNumber,
  optionalNegative,
  pickOne,
  limitSignCount,
  createCommutedSum,
  getValue,
  createCommutedProduct,
  generateFixedNumber,
  addMissingParentheses,
} from './utils';

type OoOSettings = {
  max: number;
  allowNegatives: boolean;
  useWholeNumbers: boolean;
};

function createProduct(settings: OoOSettings): Tree {
  const a = generateNumber({ ...settings, allowNegatives: false, exclude: ['0'] });
  const b = generateNumber({ ...settings, allowNegatives: false, exclude: ['0'] });
  const prod: Tree = { type: 'Product', operands: [a, b] };
  return settings.allowNegatives ? optionalNegative(prod) : prod;
}

/** a÷b with whole number result, with predefined divisor */
function createDivideByWithDivisor(divisor: Tree, settings: OoOSettings) {
  const k = generateNumber({ ...settings, allowNegatives: false, exclude: ['0'] });

  (k as TreeLeaf).value = (+getValue(k) * +getValue(divisor)).toString();
  const div: Tree = {
    type: 'Product',
    operands: [k, { type: 'DivideBy', operands: [divisor] }],
  };
  console.log(div);
  return settings.allowNegatives ? optionalNegative(div) : div;
}

/** a÷b with whole number result */
function createDivideBy(settings: OoOSettings): NestedTree {
  const b = generateNumber({ ...settings, allowNegatives: false, exclude: ['0'] });
  const a = generateNumber({ ...settings, allowNegatives: false, exclude: ['0'] });
  (a as TreeLeaf).value = (+getValue(a) * +getValue(b)).toString();
  const div: Tree = {
    type: 'Product',
    operands: [a, { type: 'DivideBy', operands: [b] }],
  };
  return (settings.allowNegatives ? optionalNegative(div) : div) as NestedTree;
}

function createFraction(settings: OoOSettings, denominator?: Tree): Tree {
  const b = denominator || generateNumber({ ...settings, exclude: ['0', '1'] });
  const a_value =
    +getValue(b) * +getValue(generateNumber({ ...settings, exclude: ['0'] }));
  const a = generateFixedNumber(a_value.toString());
  const div: Tree = { type: 'Fraction', operands: [a, b] };
  return (settings.allowNegatives ? optionalNegative(div) : div) as NestedTree;
}

/** a*b+c+d */
function generateSumWithProduct(settings: OoOSettings): Tree {
  const p1 = createProduct(settings);
  const p2 = createProduct(settings);
  const n1 = generateNumber(settings);
  const n2 = generateNumber(settings);
  const addends = pickOne([
    [p1, n1, n2],
    [p1, p2, n1],
  ]);
  return createCommutedSum(...addends);
}

/** a÷b+c+d */
function generateSumWithQuotient(settings: OoOSettings): Tree {
  const div1 = createDivideBy(settings);
  const div2 = createDivideBy(settings);
  const p1 = createProduct(settings);
  const p2 = createProduct(settings);
  const n1 = generateNumber(settings);
  const n2 = generateNumber(settings);
  const addends = pickOne([
    [div1, n1, n2],
    [div1, p1, n1],
    [div1, p1, p2],
    [div1, div2, n1],
    [div1, div2, p1],
  ]);
  return createCommutedSum(...addends);
}

/** a+b*(c+d) */
function generateSumWithNestedProduct(settings: OoOSettings): Tree {
  const a = pickOne([createProduct, generateNumber])(settings);
  const sum: Tree = {
    type: 'Sum',
    operands: [generateNumber(settings), generateNumber(settings)],
    decorators: ['RoundBracket'],
  };
  const b = generateNumber({ ...settings, exclude: ['0', '1'], allowNegatives: false });
  const prod = createCommutedProduct(b, sum);
  return createCommutedSum(a, settings.allowNegatives ? optionalNegative(prod) : prod);
}

/** (a+b):c+d*e */
function generateSumWithQuotientAndProduct(settings: OoOSettings): Tree {
  const a = generateNumber({ ...settings, exclude: ['0'] });
  const a_plus_b_div_c = generateNumber({
    ...settings,
    allowNegatives: false,
    exclude: ['0'],
  });
  const c = generateNumber({ ...settings, allowNegatives: false, exclude: ['0', '1'] });
  const b_value = +getValue(a_plus_b_div_c) * +getValue(c) - +getValue(a);
  const b = generateFixedNumber(b_value.toString());
  const innerSum = createCommutedSum(a, b);
  innerSum.decorators = ['RoundBracket'];
  const div: Tree = {
    type: 'Product',
    operands: [innerSum, { type: 'DivideBy', operands: [c] }],
  };
  const p = createProduct(settings);
  return createCommutedSum(div, p);
}

/** a*b-(c+d)+e*f */
function generateSumWithProductAndDifference(settings: OoOSettings): Tree {
  const p1 = createProduct(settings);
  const p2 = createProduct(settings);
  const c = generateNumber({ ...settings, exclude: ['0'] });
  const d = generateNumber(settings);
  const innerSum = createCommutedSum(c, d);
  innerSum.decorators = ['RoundBracket'];

  return createCommutedSum(p1, p2, { type: 'Minus', operands: [innerSum] });
}

/** a-(b+c+d:e) */
function generateDifferenceWithInnerSumAndDivideBy(settings: OoOSettings): Tree {
  const a = generateNumber({ ...settings, exclude: ['0'] });
  const b = pickOne([
    generateNumber({ ...settings, exclude: ['0'] }),
    createProduct(settings),
  ]);
  const c = generateNumber({ ...settings, exclude: ['0'] });
  const div = createDivideBy(settings);
  const innerSum = createCommutedSum(b, c, div);
  innerSum.decorators = ['RoundBracket'];
  return createCommutedSum(a, { type: 'Minus', operands: [innerSum] });
}

/** a:(b+c)*d */
function generateDivisionWithProductWithSum(settings: OoOSettings): Tree {
  const b = generateNumber({ ...settings, allowNegatives: false, exclude: ['0', '1'] });
  const c = generateNumber({ ...settings, max: +getValue(b) - 1 });
  const innerSum = createCommutedSum(b, c);
  innerSum.decorators = ['RoundBracket'];
  const d = generateNumber({ ...settings, allowNegatives: false, exclude: ['0'] });

  return createCommutedProduct(createDivideByWithDivisor(innerSum, settings), d);
}

/** a:b*c+d:e+f */
function generateSumWithProductAndQuotient(settings: OoOSettings): Tree {
  const div1 = createDivideBy(settings);
  div1.operands.push(
    generateNumber({ ...settings, allowNegatives: false, exclude: ['0', '1'] }),
  );
  const div2 = createDivideBy(settings);
  const f = generateNumber(settings);
  return createCommutedSum(div1, div2, f);
}

/** a/b+c*d */
function generateSumWithFractionAndProduct(settings: OoOSettings) {
  const fraction = createFraction(settings);
  const product = createProduct(settings);

  return createCommutedSum(fraction, product);
}

/** a+b/c*d */
function generateSumWithProductWithFraction(settings: OoOSettings): Tree {
  const fraction = createFraction(settings);
  const a = generateNumber(settings);
  const d = generateNumber({ ...settings, allowNegatives: false });

  return createCommutedSum(a, createCommutedProduct(fraction, d));
}

/** a+F+P */
function generateSumWithProductNextToFraction(settings: OoOSettings): Tree {
  const fraction = createFraction(settings);
  const a = generateNumber(settings);
  const product = createProduct(settings);

  return createCommutedSum(a, product, fraction);
}

/** a+b*c/{d(e+f)} */
function generateFractionWithProductAndSumInDenominator(settings: OoOSettings): Tree {
  const a = generateNumber(settings);
  const b = generateNumber({ ...settings, exclude: ['0'] });
  const d = generateNumber({ ...settings, allowNegatives: false, exclude: ['0', '1'] });
  const e = generateNumber({ ...settings, exclude: ['0'] });
  const f = generateNumber({
    ...settings,
    allowNegatives: false,
    exclude: ['0', getValue(e)],
  });
  const sum = createCommutedSum(e, f);
  sum.decorators = ['RoundBracket'];
  const prod = createCommutedProduct(d, sum);
  const fract = createFraction(settings, prod);
  return createCommutedSum(a, createCommutedProduct(b, fract));
}

/** a+b^2+c<^2> */
function generateSumWithSquare(settings: OoOSettings) {
  const a = generateNumber(settings);
  const b = generateNumber(settings);
  if (+getValue(b) < 0) {
    b.decorators = ['RoundBracket'];
  }
  const p1 = optionalNegative({
    type: 'Power',
    operands: [b, { type: 'Integer', value: '2' }],
  });
  const c = generateNumber(settings);
  const safeC = generateNumber(settings);
  if (+getValue(safeC) < 0) safeC.decorators = ['RoundBracket'];
  const p2 = pickOne([
    c,
    optionalNegative({
      type: 'Power',
      operands: [safeC, { type: 'Integer', value: '2' }],
    }),
  ]);

  return createCommutedSum(a, p1, p2);
}

/** (a+b)^2+c*d */
function generateSquareOfSum(settings: OoOSettings): Tree {
  const a = generateNumber({ ...settings, exclude: ['0'] });
  const b = generateNumber(settings);
  const innerSum = createCommutedSum(a, b);
  innerSum.decorators = ['RoundBracket'];
  const prod = createProduct(settings);

  return createCommutedSum(
    prod,
    optionalNegative({
      type: 'Power',
      operands: [innerSum, { type: 'Integer', value: '2' }],
    }),
  );
}

/** a * (b/c)^2 + d * (e+f) */
function generateExpressionWithSquareOfFraction(settings: OoOSettings) {
  const a = generateNumber({ ...settings, allowNegatives: false, exclude: ['0'] });
  const e = generateNumber({ ...settings, allowNegatives: false });
  const f = generateNumber({ ...settings, allowNegatives: false });
  const sum = createCommutedSum(e, f);
  const d = generateNumber({ ...settings, allowNegatives: false, exclude: ['0'] });
  const fr = createFraction(settings);
  fr.decorators = ['RoundBracket'];
  const square: Tree = {
    type: 'Power',
    operands: [fr, { type: 'Integer', value: '2' }],
  };
  const prod1 = optionalNegative(createCommutedProduct(a, square));
  const prod2 = optionalNegative(createCommutedProduct(d, sum));

  return createCommutedSum(prod1, prod2);
}

/** a^2 + bc^2 - d */
function generateSumOfTwoSquares(settings: OoOSettings) {
  const a = generateNumber(settings);
  if (+getValue(a) < 0) a.decorators = ['RoundBracket'];
  const pow1 = optionalNegative({
    type: 'Power',
    operands: [a, { type: 'Integer', value: '2' }],
  });

  const b = generateNumber({ ...settings, allowNegatives: false, exclude: ['0'] });
  const c = generateNumber(settings);
  if (+getValue(c) < 0) c.decorators = ['RoundBracket'];
  const pow2: Tree = { type: 'Power', operands: [c, { type: 'Integer', value: '2' }] };
  const prod = optionalNegative(createCommutedProduct(b, pow2));

  const d = generateNumber(settings);

  return createCommutedSum(pow1, prod, d);
}

/** a^(e1+e2)+b*c with e1, e2 are 1 or 2 and a shouldn't be bigger than 10 */
function generateExpressionWithExponentSum(settings: OoOSettings) {
  const a = generateNumber({ ...settings, max: Math.min(settings.max, 10) });
  if (+getValue(a) < 0) a.decorators = ['RoundBracket'];
  const e1 = generateNumber({ ...settings, allowNegatives: false, max: 2 });
  const e2 = generateNumber({
    ...settings,
    allowNegatives: false,
    max: 2,
    exclude: ['0'],
  });
  const sum = createCommutedSum(e1, e2);
  const pow = optionalNegative({ type: 'Power', operands: [a, sum] });
  return createCommutedSum(pow, createProduct(settings));
}

export function generateFourOperationsExpression(
  complexity: 0 | 1 | 2 | 3 = 0,
  useWholeNumbers = true,
): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings = {
    max: 5,
    allowNegatives: false,
    useWholeNumbers,
  };
  const choices = [generateSumWithProduct];
  if (complexity > 0) {
    settings.max = 7;
    settings.allowNegatives = true;
    maxSignCount = 1;
    choices.push(
      generateSumWithNestedProduct,
      generateSumWithProductAndDifference,
      generateDifferenceWithInnerSumAndDivideBy,
    );
  }
  if (complexity > 1) {
    settings.max = 9;
    maxSignCount = 2;
    choices.push(
      generateSumWithProduct,
      generateSumWithQuotient,
      generateDivisionWithProductWithSum,
      generateSumWithQuotientAndProduct,
      generateSumWithProductAndQuotient,
    );
  }
  if (complexity > 2) {
    settings.max = 12;
    maxSignCount = 3;
  }

  let tree = pickOne(choices)(settings);
  tree = addMissingParentheses(limitSignCount(tree, maxSignCount));
  return treeToLatex(tree as ExpressionTree, {
    mulSymbol: ' \\times ',
    solutionFormatter: simpleSolutionFormatter,
  });
}

export function generateFractionExpression(
  complexity: 0 | 1 | 2 | 3 = 0,
  useWholeNumbers = true,
): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings = {
    max: 5,
    allowNegatives: false,
    useWholeNumbers,
  };
  const choices = [generateSumWithFractionAndProduct, generateSumWithProductWithFraction];
  if (complexity > 0) {
    settings.max = 7;
    settings.allowNegatives = true;
    maxSignCount = 1;
    choices.push(generateSumWithProductNextToFraction);
  }
  if (complexity > 1) {
    settings.max = 9;
    maxSignCount = 2;
    choices.push(generateFractionWithProductAndSumInDenominator);
  }
  if (complexity > 2) {
    settings.max = 12;
    maxSignCount = 3;
  }

  let tree = pickOne(choices)(settings);
  tree = addMissingParentheses(limitSignCount(tree, maxSignCount));
  return treeToLatex(tree as ExpressionTree, {
    mulSymbol: ' \\times ',
    solutionFormatter: simpleSolutionFormatter,
  });
}

export function generatePowerExpression(
  complexity: 0 | 1 | 2 | 3 = 0,
  useWholeNumbers = true,
): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings = {
    max: 5,
    allowNegatives: false,
    useWholeNumbers,
  };
  const choices = [generateSumWithSquare, generateSquareOfSum];
  if (complexity > 0) {
    settings.max = 7;
    settings.allowNegatives = true;
    maxSignCount = 1;
    choices.push(generateSumOfTwoSquares, generateExpressionWithExponentSum);
  }
  if (complexity > 1) {
    settings.max = 9;
    maxSignCount = 2;
    choices.push(generateExpressionWithSquareOfFraction);
  }
  if (complexity > 2) {
    settings.max = 12;
    maxSignCount = 3;
  }

  let tree = pickOne(choices)(settings);
  tree = addMissingParentheses(limitSignCount(tree, maxSignCount));
  return treeToLatex(tree as ExpressionTree, {
    mulSymbol: ' \\times ',
    solutionFormatter: simpleSolutionFormatter,
  });
}
