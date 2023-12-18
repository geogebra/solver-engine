import { ExpressionTree } from '../parser';
import { treeToLatex } from '../renderer';
import {
  type Tree,
  generateNumber,
  generateVariable,
  createReducedProduct,
  optionalFlip,
  generateFraction,
  getValue,
  optionalNegative,
  pickOne,
  limitSignCount,
  createCommutedSum,
  createEquation,
  createVariableWithCoefficient,
  generateFixedNumber,
  generatePosValue,
  getPermutation,
  createNumberOrFraction,
  createCoefficient,
} from './utils';

type FactorySettings = {
  max: number;
  allowNegatives: boolean;
  useWholeNumbers: boolean;
  avoidFractions: boolean;
  nameChoices: string[];
  name?: string;
  mayFlipEquation: boolean;
};

/** ax=b */
function generateOneStepMultiplyByNumber(settings: FactorySettings): Tree {
  const a = generateNumber({ exclude: ['0', '1'], ...settings });
  const b = generateNumber(settings);
  const x: Tree = generateVariable(settings);
  const prod: Tree = createReducedProduct(a, x);
  return optionalFlip({ type: 'Equation', operands: [prod, b] }, settings);
}

/** {a/b}x=c */
function generateOneStepMultiplyByFraction(settings: FactorySettings): Tree {
  const f = generateFraction({
    ...settings,
    indivisible: true,
    optionalLeadingSign: true,
  });
  const c = generateNumber({ ...settings, exclude: [] });
  const x = generateVariable(settings);
  const prod: Tree = { type: 'Product', operands: [f, x] };
  return optionalFlip({ type: 'Equation', operands: [prod, c] }, settings);
}

/** {a/b}x={c/d} */
function generateOneStepMultiplyByFraction2(settings: FactorySettings): Tree {
  const f1 = generateFraction({
    ...settings,
    indivisible: true,
    optionalLeadingSign: true,
  });
  const f2 = generateFraction({
    ...settings,
    indivisible: true,
    optionalLeadingSign: true,
  });
  const x = generateVariable(settings);
  const prod: Tree = { type: 'Product', operands: [f1, x] };
  return optionalFlip({ type: 'Equation', operands: [prod, f2] }, settings);
}

/** ax = bx with a≠b */
function generateAxEqualsBx(settings: FactorySettings): Tree {
  const a = generateNumber({ ...settings, exclude: ['0', '1'] });
  const b = generateNumber({ ...settings, exclude: ['0', getValue(a)] });
  const x = generateVariable(settings);
  const p1 = createReducedProduct(a, x);
  const p2 = createReducedProduct(b, x);
  return optionalFlip({ type: 'Equation', operands: [p1, p2] }, settings);
}

/** x/a = b or x/a = b/c */
function generateOneStepDivision(settings: FactorySettings): Tree {
  const x = optionalNegative(generateVariable(settings));
  const lhs = generateFraction(
    { ...settings, indivisible: true, optionalLeadingSign: true },
    x,
  );
  const b = generateNumber(settings);
  const f = generateFraction({
    ...settings,
    indivisible: true,
    optionalLeadingSign: true,
  });
  const rhs = settings.avoidFractions ? b : pickOne([b, f]);
  return optionalFlip({ type: 'Equation', operands: [lhs, rhs] }, settings);
}

/** x+a=b */
function generateOneStepAddition(settings: FactorySettings): Tree {
  const a = generateNumber({ ...settings, exclude: ['0'] });
  const b = generateNumber(settings);
  const x = optionalNegative(generateVariable(settings));
  const sum: Tree = pickOne([
    { type: 'Sum', operands: [x, a] },
    { type: 'Sum', operands: [a, x] },
  ]);
  return optionalFlip({ type: 'Equation', operands: [sum, b] }, settings);
}

/** x+a/b=c/d or x+a=b/c or x+a/b=c */
function generateOneStepAdditionWithFractions(settings: FactorySettings): Tree {
  const x = optionalNegative(generateVariable(settings));
  const f1 = generateFraction({
    ...settings,
    indivisible: true,
    optionalLeadingSign: true,
  });
  const f2 = generateFraction({
    ...settings,
    indivisible: true,
    optionalLeadingSign: true,
  });
  const n = generateNumber({ ...settings, exclude: ['0'] });
  const [a, b] = pickOne([
    [n, f2],
    [f1, n],
    [f1, f2],
  ]);
  const sum: Tree = pickOne([
    { type: 'Sum', operands: [x, a] },
    { type: 'Sum', operands: [a, x] },
  ]);
  return optionalFlip({ type: 'Equation', operands: [sum, b] }, settings);
}

/** ax+b=c */
function generateTwoStepStandard(settings: FactorySettings): Tree {
  const a = generateNumber({ ...settings, allowNegatives: false, exclude: ['0', '1'] });
  const b = generateNumber({ ...settings, exclude: ['0'] });
  const c = generateNumber(settings);
  const x = generateVariable(settings);
  const prod: Tree = optionalNegative({ type: 'Product', operands: [a, x] });
  const sum = createCommutedSum(prod, b);
  return createEquation(sum, c, settings);
}

/** x/a+b=c */
function generateTwoStepMultiply(settings: FactorySettings): Tree {
  const a = generateNumber({ ...settings, exclude: ['0', '1', '-1'] });
  const b = generateNumber({ ...settings, exclude: ['0'] });
  const c = generateNumber(settings);
  const x = optionalNegative(generateVariable(settings));
  const fraction: Tree = optionalNegative({ type: 'Fraction', operands: [x, a] });
  const sum = createCommutedSum(fraction, b);
  return createEquation(sum, c, settings);
}

/** ax+b=c where a, b, c can be fractions or numbers */
function generateTwoStepFractions(settings: FactorySettings): Tree {
  const x = generateVariable(settings);
  const a = pickOne([
    generateFraction({ ...settings, indivisible: true, optionalLeadingSign: false }),
    generateNumber({ ...settings, allowNegatives: false, exclude: ['0', '1'] }),
  ]);
  const b = pickOne([
    generateFraction({ ...settings, indivisible: true, optionalLeadingSign: true }),
    generateNumber({ ...settings, exclude: ['0'] }),
  ]);
  const c = pickOne([
    generateFraction({ ...settings, indivisible: true, optionalLeadingSign: true }),
    generateNumber({ ...settings, exclude: ['0'] }),
  ]);
  const prod = optionalNegative({ type: 'Product', operands: [a, x] });
  const sum = createCommutedSum(prod, b);
  return createEquation(sum, c, settings);
}

/** a(x+b)=c */
function generateTwoStepDivideFirst(settings: FactorySettings): Tree {
  const a = generateNumber({ ...settings, exclude: ['0', '1', '-1'] });
  const b = generateNumber({ ...settings, exclude: ['0'] });
  const c = generateNumber(settings);
  const x = optionalNegative(generateVariable(settings));
  const sum = createCommutedSum(x, b);
  sum.decorators = ['RoundBracket'];
  const prod = createReducedProduct(a, sum);
  return createEquation(prod, c, settings);
}

/** (a+x)/b=c */
function generateTwoStepMultiplyFirst(settings: FactorySettings): Tree {
  const a = generateNumber({ ...settings, exclude: ['0'] });
  const b = generateNumber({ ...settings, exclude: ['0', '1', '-1'] });
  const c = generateNumber(settings);
  const x = optionalNegative(generateVariable(settings));
  const sum = createCommutedSum(a, x);
  const fraction = optionalNegative({ type: 'Fraction', operands: [sum, b] });
  return createEquation(fraction, c, settings);
}

/** a/b(x+c)=d */
function generateTwoStepMultiplyFirstFraction(settings: FactorySettings): Tree {
  const f = generateFraction({
    ...settings,
    indivisible: true,
    optionalLeadingSign: true,
  });
  const c = generateNumber({ ...settings, exclude: ['0'] });
  const d = generateNumber(settings);
  const x = optionalNegative(generateVariable(settings));
  const sum = createCommutedSum(x, c);
  sum.decorators = ['RoundBracket'];
  const prod = createReducedProduct(f, sum);
  return createEquation(prod, d, settings);
}

/** No Solutions ***************/
/** a ≠ b; a,b ≠ 0: x+a=x+b */
function generateNoSolutionSum(settings: FactorySettings): Tree {
  const a = generateNumber({ ...settings, exclude: ['0'] });
  const b = generateNumber({ ...settings, exclude: ['0', getValue(a)] });
  const x = generateVariable(settings);
  return createEquation(createCommutedSum(x, a), createCommutedSum(x, b), settings);
}
/** a ≠ 0; b ≠ c: ax+b=ax+c */
function generateNoSolutionTwoSteps(settings: FactorySettings): Tree {
  const ax = createVariableWithCoefficient(settings);
  const b = generateNumber(settings);
  const c = generateNumber({ ...settings, exclude: [getValue(b)] });
  return createEquation(createCommutedSum(ax, b), createCommutedSum(ax, c), settings);
}

/** ax+b=a1x+a2x+c where a1+a2 = a and b≠c */
function generateNoSolutionTwoSteps2(settings: FactorySettings): Tree {
  const a1 = generatePosValue({ ...settings, exclude: ['0'] });
  const a2 = generatePosValue({ ...settings, exclude: ['0'] });
  const a = (+a1 + +a2).toString();
  const x = generateVariable(settings);
  const ax = createReducedProduct(generateFixedNumber(a), x);
  const a1x = createReducedProduct(generateFixedNumber(a1), x);
  const a2x = createReducedProduct(generateFixedNumber(a2), x);
  const b = generateNumber({ ...settings, exclude: ['0'] });
  const c = generateNumber({ ...settings, exclude: ['0', getValue(b)] });

  return createEquation(
    createCommutedSum(ax, b),
    createCommutedSum(a1x, a2x, c),
    settings,
  );
}

/** a ≠ 0 or 1; b ≠ 0: a(x+b)=ax */
function generateNoSolutionDistribution(settings: FactorySettings): Tree {
  const a = generateNumber({ ...settings, allowNegatives: false, exclude: ['0', '1'] });
  const b = generateNumber({ ...settings, exclude: ['0'] });
  const x = generateVariable(settings);
  const sum = createCommutedSum(x, b);
  sum.decorators = ['RoundBracket'];
  const prod = createReducedProduct(a, x);
  return createEquation(createReducedProduct(a, sum), prod, settings);
}

/** "a ≠ 0 or 1; b ≠ 0; c ≠ ab: a(x+b)=ax+c */
function generateNoSolutionDistribution2(settings: FactorySettings): Tree {
  const a = generateNumber({ ...settings, allowNegatives: false, exclude: ['0', '1'] });
  const b = generateNumber({ ...settings, exclude: ['0'] });
  const abValue = (+getValue(a) * +getValue(b)).toString();
  const c = generateNumber({ ...settings, exclude: ['0', abValue, `-${abValue}`] });
  const x = generateVariable(settings);
  const sum = createCommutedSum(x, b);
  sum.decorators = ['RoundBracket'];
  const prod = createReducedProduct(a, x);
  return createEquation(
    createReducedProduct(a, sum),
    createCommutedSum(prod, c),
    settings,
  );
}

/** Infinite Solutions ***************/
/** a ≠ 0 or 1: ax=ax */
function generateInfiniteSolutionsProduct(settings: FactorySettings): Tree {
  const a = generateNumber({ ...settings, exclude: ['0', '1'], allowNegatives: false });
  const x = generateVariable(settings);
  const ax = { type: 'Product', operands: [a, x] } satisfies Tree;
  const neg =
    settings.allowNegatives && Math.random() < 0.5
      ? ({ type: 'Minus', operands: [ax], optional: false } satisfies Tree)
      : ax;
  return { type: 'Equation', operands: [neg, neg] };
}

/** a ≠ 0: x+a=x+a */
function generateInfiniteSolutionsSum(settings: FactorySettings): Tree {
  const a = generateNumber({ ...settings, exclude: ['0', '1'] });
  const x = optionalNegative(generateVariable(settings));
  const a_plus_x = createCommutedSum(x, a);
  return { type: 'Equation', operands: [a_plus_x, a_plus_x] };
}

/** a ≠ 0: ax+b=ax+b */
function generateInfiniteSolutionsTwoSteps(settings: FactorySettings): Tree {
  const ax = createVariableWithCoefficient(settings);
  const b = generateNumber(settings);
  const sum1 = createCommutedSum(ax, b);
  const sum2 = createCommutedSum(ax, b);
  return createEquation(sum1, sum2, settings);
}

/** "a ≠ 0 or 1; b ≠ 0: c = ab"	a(x+b)=ax+c */
function generateInfiniteSolutionsDistribution(settings: FactorySettings): Tree {
  const x = generateVariable(settings);
  const a = generateNumber({ ...settings, allowNegatives: false, exclude: ['0', '1'] });
  let b = generateNumber({ ...settings, allowNegatives: false, exclude: ['0', '1'] });
  const aIsNeg = settings.allowNegatives && Math.random() < 0.33;
  const bIsNeg = settings.allowNegatives && Math.random() < 0.33;
  if (bIsNeg) b = { type: 'Minus', operands: [b], optional: false };
  const ax: Tree = aIsNeg
    ? { type: 'Minus', operands: [createReducedProduct(a, x)] }
    : createReducedProduct(a, x);
  const abValue = (+getValue(a) * +getValue(b) * (aIsNeg ? -1 : 1)).toString();
  const c = generateFixedNumber(abValue);
  const sum = createCommutedSum(x, b);
  let prod = createReducedProduct(a, sum);
  if (aIsNeg) prod = { type: 'Minus', operands: [prod], optional: false };
  sum.decorators = ['RoundBracket'];
  return createEquation(prod, createCommutedSum(ax, c), settings);
}

/** ax+b=cx+d */
function generateMultiStepBase(
  settings: FactorySettings & { fractionCount: number },
): Tree {
  const fractionPositions = getPermutation([0, 1, 2, 3]).slice(0, settings.fractionCount);
  const variableName = getValue(generateVariable(settings));
  const ax = createVariableWithCoefficient({
    ...settings,
    name: variableName,
    exclude: ['0', '1'],
    useFraction: fractionPositions.includes(0),
  });
  let cx: Tree | null = null;
  for (let i = 0; i < 100; i++) {
    cx = createVariableWithCoefficient({
      ...settings,
      name: variableName,
      exclude: ['0', '1'],
      useFraction: fractionPositions.includes(1),
    });
    if (treeToLatex(cx as ExpressionTree) !== treeToLatex(ax as ExpressionTree)) break;
  }
  if (!cx) throw new Error('Could not generate different variable');
  const b = createNumberOrFraction({
    ...settings,
    exclude: ['0'],
    useFraction: fractionPositions.includes(2),
  });
  const d = createNumberOrFraction({
    ...settings,
    exclude: ['0'],
    useFraction: fractionPositions.includes(3),
  });
  const sum1 = createCommutedSum(ax, b);
  const sum2 = createCommutedSum(cx, d);
  return createEquation(sum1, sum2, { mayFlipEquation: true });
}

/** a(x+b)=x/c+d */
function generateMultiStepDistributionAndFraction(
  settings: FactorySettings & { fractionCount: number },
): Tree {
  const fractionPositions = getPermutation([0, 1, 2]).slice(0, settings.fractionCount);
  const a = createCoefficient({
    ...settings,
    useFraction: fractionPositions.includes(0),
    max: Math.min(9, settings.max),
  });
  const x = generateVariable(settings);
  const sum = createCommutedSum(
    optionalNegative(x),
    createCoefficient({ ...settings, useFraction: fractionPositions.includes(1) }),
  );
  sum.decorators = ['RoundBracket'];
  const lhs = createReducedProduct(a, sum);

  const c = generateNumber({
    ...settings,
    exclude: ['0', '1', '-1'],
    max: Math.min(9, settings.max),
  });
  const frac = optionalNegative({ type: 'Fraction', operands: [x, c] });
  const rhs = createCommutedSum(frac, generateNumber({ ...settings, exclude: ['0'] }));
  return createEquation(lhs, rhs, { mayFlipEquation: true });
}

/** {a/b}x+cx=d */
function generateMultiStepTwoXOnOneSide(
  settings: FactorySettings & { fractionCount: number },
): Tree {
  const fractionPositions = getPermutation([0, 1]).slice(0, settings.fractionCount);
  const x = generateVariable(settings);
  const ab = generateFraction({
    ...settings,
    indivisible: true,
    optionalLeadingSign: false,
  });
  const prod1 = optionalNegative(createReducedProduct(ab, x));
  const cx = createVariableWithCoefficient({
    ...settings,
    useFraction: fractionPositions.includes(0),
    name: getValue(x),
  });
  const d = createCoefficient({
    ...settings,
    useFraction: fractionPositions.includes(1),
  });
  return createEquation(createCommutedSum(prod1, cx), d, settings);
}

/** a(x+b)={c/d}x+e */
function generateMultiStepDistribution(
  settings: FactorySettings & { fractionCount: number },
): Tree {
  const x = generateVariable(settings);
  const a = generateNumber({ ...settings, exclude: ['0', '1'], allowNegatives: false });
  const b = generateNumber({ ...settings, exclude: ['0'] });
  const sum = createCommutedSum(x, b);
  sum.decorators = ['RoundBracket'];
  const lhs = optionalNegative(createReducedProduct(a, sum));

  const cd = generateFraction({ ...settings, indivisible: true, allowNegatives: false });
  const cdx = optionalNegative({ type: 'Product', operands: [cd, x] });
  const e = createCoefficient({
    ...settings,
    exclude: ['0'],
    useFraction: settings.fractionCount > 0,
  });
  const rhs = createCommutedSum(cdx, e);
  return createEquation(lhs, rhs, settings);
}

/** a(bx+c)+dx=e(fx+g)+h */
function generateMultiStepDoubleDistribution(
  settings: FactorySettings & { fractionCount: number },
): Tree {
  const fractionPositions = getPermutation([0, 1, 2, 3, 4, 5, 6, 7]).slice(
    0,
    settings.fractionCount,
  );
  const x = generateVariable(settings);
  const a = createCoefficient({
    ...settings,
    exclude: ['0', '1'],
    max: Math.min(6, settings.max),
    useFraction: fractionPositions.includes(0),
  });
  const bx = createVariableWithCoefficient({
    ...settings,
    exclude: ['0'],
    useFraction: fractionPositions.includes(1),
    name: getValue(x),
  });
  const c = createCoefficient({
    ...settings,
    exclude: ['0'],
    useFraction: fractionPositions.includes(2),
  });
  const dx = createVariableWithCoefficient({
    ...settings,
    exclude: ['0'],
    useFraction: fractionPositions.includes(3),
    name: getValue(x),
  });
  const e = createCoefficient({
    ...settings,
    exclude: ['0', '1'],
    max: Math.min(6, settings.max),
    useFraction: fractionPositions.includes(4),
  });
  const fx = createVariableWithCoefficient({
    ...settings,
    exclude: ['0'],
    useFraction: fractionPositions.includes(5),
    name: getValue(x),
  });
  const g = createCoefficient({
    ...settings,
    exclude: ['0'],
    useFraction: fractionPositions.includes(6),
  });
  const h = createCoefficient({
    ...settings,
    useFraction: fractionPositions.includes(7),
  });

  const innerSum1 = createCommutedSum(bx, c);
  innerSum1.decorators = ['RoundBracket'];
  const innerSum2 = createCommutedSum(fx, g);
  innerSum2.decorators = ['RoundBracket'];

  const prod1 = optionalNegative(createReducedProduct(a, innerSum1));
  const prod2 = optionalNegative(createReducedProduct(e, innerSum2));
  const lhs = createCommutedSum(prod1, dx);
  const rhs = createCommutedSum(prod2, h);
  return createEquation(lhs, rhs, settings);
}

/**
 * Complexity:
 * 0: numbers up to 9, no fraction or negatives, only x and y, only on the left
 * 1: numbers up to 12, allow negatives, more solution variables, allow on either side
 * 2: up to 2 negatives, allow fractions
 * 3: numbers up to 16, up to 3 negatives
 */
export function generateOneStepEquation(
  complexity: 0 | 1 | 2 | 3 = 0,
  useWholeNumbers = true,
): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings = {
    max: 9,
    allowNegatives: false,
    avoidFractions: true,
    nameChoices: 'xy'.split(''),
    mayFlipEquation: false,
    useWholeNumbers,
  };
  const choices = [
    generateOneStepAddition,
    generateOneStepMultiplyByNumber,
    generateOneStepDivision,
  ];
  if (complexity > 0) {
    settings.max = 12;
    settings.allowNegatives = true;
    settings.nameChoices = 'abcdghkmnpqrstuvwxyz'.split('');
    settings.mayFlipEquation = true;
    maxSignCount = 1;
    choices.push(generateAxEqualsBx);
  }
  if (complexity > 1) {
    maxSignCount = 2;
    settings.avoidFractions = false;
    choices.push(
      generateOneStepMultiplyByFraction,
      generateOneStepMultiplyByFraction2,
      generateOneStepAdditionWithFractions,
    );
  }
  if (complexity > 2) {
    settings.max = 16;
    maxSignCount = 3;
  }

  let tree = pickOne(choices)(settings);
  tree = limitSignCount(tree, maxSignCount);
  return treeToLatex(tree as ExpressionTree);
}

/**
 * Complexity:
 * 0: numbers up to 9, no fraction or negatives, only x and y, only on the left
 * 1: numbers up to 12, allow negatives, more solution variables, allow on either side
 * 2: up to 2 negatives, allow fractions
 * 3: numbers up to 16, up to 3 negatives
 */
export function generateTwoStepEquation(
  complexity: 0 | 1 | 2 | 3 = 0,
  useWholeNumbers = true,
): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings = {
    max: 9,
    allowNegatives: false,
    avoidFractions: true,
    nameChoices: 'xy'.split(''),
    mayFlipEquation: false,
    useWholeNumbers,
  };
  const choices = [generateTwoStepStandard];
  if (complexity > 0) {
    settings.max = 12;
    settings.allowNegatives = true;
    settings.nameChoices = 'abcdghkmnpqrstuvwxyz'.split('');
    settings.mayFlipEquation = true;
    maxSignCount = 1;
    choices.push(
      generateTwoStepMultiply,
      generateTwoStepDivideFirst,
      generateTwoStepMultiplyFirst,
    );
  }
  if (complexity > 1) {
    maxSignCount = 2;
    settings.avoidFractions = false;
    choices.push(generateTwoStepFractions, generateTwoStepMultiplyFirstFraction);
  }
  if (complexity > 2) {
    settings.max = 16;
    maxSignCount = 3;
  }

  let tree = pickOne(choices)(settings);
  tree = limitSignCount(tree, maxSignCount);
  return treeToLatex(tree as ExpressionTree);
}

/**
 * Complexity:
 * 0: numbers up to 9, no fraction or negatives, only x and y, only on the left
 * 1: numbers up to 12, allow negatives, more solution variables, allow on either side
 * 2: up to 2 negatives, allow fractions
 * 3: numbers up to 16, up to 3 negatives
 */
export function generateMultiStepEquation(
  complexity: 0 | 1 | 2 | 3 = 0,
  useWholeNumbers = true,
): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings = {
    max: 9,
    allowNegatives: false,
    avoidFractions: true,
    nameChoices: 'xy'.split(''),
    mayFlipEquation: false,
    useWholeNumbers,
    fractionCount: 0,
  };
  const choices = [generateMultiStepBase, generateMultiStepTwoXOnOneSide];
  if (complexity > 0) {
    settings.max = 12;
    settings.allowNegatives = true;
    settings.nameChoices = 'abcdghkmnpqrstuvwxyz'.split('');
    settings.mayFlipEquation = true;
    maxSignCount = 1;
    choices.push(generateMultiStepDistributionAndFraction);
  }
  if (complexity > 1) {
    maxSignCount = 2;
    settings.fractionCount = pickOne([0, 0, 1]);
    settings.avoidFractions = false;
    choices.push(generateMultiStepDistribution, generateMultiStepDoubleDistribution);
  }
  if (complexity > 2) {
    settings.fractionCount = pickOne([0, 1, 1, 2, 2, 3]);
    settings.max = 16;
    maxSignCount = 3;
  }

  let tree = pickOne(choices)(settings);
  tree = limitSignCount(tree, maxSignCount);
  return treeToLatex(tree as ExpressionTree);
}

/**
 * Complexity:
 * 0: numbers up to 9, no fraction or negatives, only x and y, only on the left
 * 1: numbers up to 12, allow negatives, more solution variables, allow on either side
 * 2: up to 2 negatives, allow fractions
 * 3: numbers up to 16, up to 3 negatives
 */
export function generateZeroOrManySolutionEquation(
  complexity: 0 | 1 | 2 | 3 = 0,
  useWholeNumbers = true,
): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings = {
    max: 9,
    allowNegatives: false,
    avoidFractions: true,
    nameChoices: 'xy'.split(''),
    mayFlipEquation: false,
    useWholeNumbers,
  };
  const choices = [
    generateNoSolutionSum,
    generateNoSolutionTwoSteps,
    generateNoSolutionDistribution,
    generateInfiniteSolutionsProduct,
    generateInfiniteSolutionsSum,
    generateInfiniteSolutionsTwoSteps,
  ];
  if (complexity > 0) {
    settings.max = 12;
    settings.allowNegatives = true;
    settings.nameChoices = 'abcdghkmnpqrstuvwxyz'.split('');
    settings.mayFlipEquation = true;
    maxSignCount = 1;
    choices.push();
  }
  if (complexity > 1) {
    maxSignCount = 2;
    settings.avoidFractions = false;
    choices.push(
      generateInfiniteSolutionsDistribution,
      generateNoSolutionTwoSteps2,
      generateNoSolutionDistribution2,
    );
  }
  if (complexity > 2) {
    settings.max = 16;
    maxSignCount = 3;
  }

  const random = Math.random();
  if (complexity > 0 && random < 0.1) return generateOneStepEquation(complexity);
  if (complexity > 0 && random < 0.25) return generateTwoStepEquation(complexity);
  let tree = pickOne(choices)(settings);
  tree = limitSignCount(tree, maxSignCount);
  return treeToLatex(tree as ExpressionTree);
}
