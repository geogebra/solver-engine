import { ExpressionTree } from '../parser';
import { treeToLatex } from '../renderer';
import {
  pickOne,
  limitSignCount,
  type Tree,
  generateNumber,
  generateVariable,
  createReducedProduct,
  createCommutedSum,
  optionalNegative,
  getValue,
  getPermutation,
  generateFixedNumber,
  generatePosValue,
  createCoefficient,
  createNumberOrFraction,
  createVariableWithCoefficient,
  addMissingParentheses,
} from './utils';

type Settings = {
  max: number;
  excludeZero: boolean;
  allowNegatives: boolean;
  nameChoices: string[];
  name?: string;
  useWholeNumbers: true;
  fractionCount: number;
  useFraction?: boolean;
  exclude?: string[];
};

/** a(bx+c)+dx */
function generateSimpleExpression(settings: Settings): Tree {
  const fractionPositions = getPermutation([0, 1, 2, 3]).slice(0, settings.fractionCount);
  const name = getValue(generateVariable(settings));
  const settingsWithName = { ...settings, name };
  const a = createCoefficient({
    ...settings,
    exclude: ['0', '1'],
    useFraction: fractionPositions.includes(0),
  });
  const c = createNumberOrFraction({
    ...settings,
    useFraction: fractionPositions.includes(1),
  });
  const bx = createVariableWithCoefficient({
    ...settingsWithName,
    useFraction: fractionPositions.includes(2),
  });
  const innerSum = createCommutedSum(bx, c);
  innerSum.decorators = ['RoundBracket'];
  const dx = createVariableWithCoefficient({
    ...settingsWithName,
    useFraction: fractionPositions.includes(3),
  });
  const prod = createReducedProduct(a, innerSum);
  return createCommutedSum(prod, dx);
}

/** a(bx+c)+d(ex+f) */
function generateTwoDistributions(settings: Settings): Tree {
  const fractionPositions = getPermutation([1, 2, 3, 4, 5, 6]).slice(
    0,
    settings.fractionCount,
  );
  const name = getValue(generateVariable(settings));
  const settingsWithName = { ...settings, name };
  const a = createCoefficient({
    ...settings,
    useFraction: fractionPositions.includes(1),
  });
  const bx = createVariableWithCoefficient({
    ...settingsWithName,
    useFraction: fractionPositions.includes(2),
  });
  const c = createNumberOrFraction({
    ...settings,
    useFraction: fractionPositions.includes(3),
  });
  const d = createCoefficient({
    ...settings,
    exclude: ['0', '1', getValue(a)],
    useFraction: fractionPositions.includes(4),
  });
  const ex = createVariableWithCoefficient({
    ...settingsWithName,
    useFraction: fractionPositions.includes(5),
  });
  const f = createNumberOrFraction({
    ...settings,
    useFraction: fractionPositions.includes(6),
  });

  const innerSum1 = createCommutedSum(bx, c);
  innerSum1.decorators = ['RoundBracket'];
  const innerSum2 = createCommutedSum(ex, f);
  innerSum2.decorators = ['RoundBracket'];

  const prod1 = createReducedProduct(a, innerSum1);
  const prod2 = createReducedProduct(d, innerSum2);
  return createCommutedSum(prod1, prod2);
}

/** a(bx+cy)+dy+e(fx+gy) */
function generateTwoDistributions2(settings: Settings): Tree {
  const fractionPositions = getPermutation([1, 2, 3, 4, 5, 6, 7]).slice(
    0,
    settings.fractionCount,
  );

  const name1 = getValue(generateVariable(settings));
  const name2 = getValue(
    generateVariable({
      nameChoices: settings.nameChoices.filter((choice) => choice !== name1),
    }),
  );

  const a = createCoefficient({
    ...settings,
    useFraction: fractionPositions.includes(1),
  });
  const bx = createVariableWithCoefficient({
    ...settings,
    name: name1,
    useFraction: fractionPositions.includes(2),
  });
  const cy = createVariableWithCoefficient({
    ...settings,
    name: name2,
    useFraction: fractionPositions.includes(3),
  });
  const sum1 = createCommutedSum(bx, cy);
  sum1.decorators = ['RoundBracket'];
  const dy = createVariableWithCoefficient({
    ...settings,
    name: name2,
    useFraction: fractionPositions.includes(4),
  });

  const e = createCoefficient({
    ...settings,
    exclude: ['0', '1', getValue(a)],
    useFraction: fractionPositions.includes(5),
  });
  const fx = createVariableWithCoefficient({
    ...settings,
    name: name1,
    useFraction: fractionPositions.includes(6),
  });
  const gy = createVariableWithCoefficient({
    ...settings,
    name: name2,
    useFraction: fractionPositions.includes(7),
  });
  const sum2 = createCommutedSum(fx, gy);
  sum2.decorators = ['RoundBracket'];

  const prod1 = optionalNegative(createReducedProduct(a, sum1));
  const prod2 = optionalNegative(createReducedProduct(e, sum2));

  return createCommutedSum(prod1, prod2, dy);
}

/** a(bx+cx+d)+ex+f */
function generateDistribution3(settings: Settings): Tree {
  const fractionPositions = getPermutation([1, 2, 3, 4, 5, 6]).slice(
    0,
    settings.fractionCount,
  );

  const name = getValue(generateVariable(settings));
  const settingsWithName = { ...settings, name };
  const a = createCoefficient({
    ...settings,
    exclude: ['0', '1'],
    useFraction: fractionPositions.includes(1),
  });
  const bx = createVariableWithCoefficient({
    ...settingsWithName,
    useFraction: fractionPositions.includes(2),
  });
  const cx = createVariableWithCoefficient({
    ...settingsWithName,
    useFraction: fractionPositions.includes(3),
  });
  const d = createNumberOrFraction({
    ...settingsWithName,
    useFraction: fractionPositions.includes(4),
  });
  const ex = createVariableWithCoefficient({
    ...settingsWithName,
    useFraction: fractionPositions.includes(5),
  });
  const f = createNumberOrFraction({
    ...settingsWithName,
    useFraction: fractionPositions.includes(6),
  });

  const innerSum = createCommutedSum(bx, cx, d);
  innerSum.decorators = ['RoundBracket'];

  const prod1 = optionalNegative(createReducedProduct(a, innerSum));
  return createCommutedSum(prod1, ex, f);
}

/** (ax^b)/(cx^d), with a,c have common factor */
function generateFractionOfMonomials(settings: Settings): Tree {
  const x = generateVariable(settings);
  const n = generatePosValue({ ...settings, exclude: ['0', '1'] });
  const a_div_n = generatePosValue({ ...settings, exclude: ['0'], max: 6 });
  const c_div_n = generatePosValue({ ...settings, exclude: ['0', a_div_n], max: 6 });
  const a = generateFixedNumber((+a_div_n * +n).toString());
  const c = generateFixedNumber((+c_div_n * +n).toString());

  const b = generateNumber({
    ...settings,
    useWholeNumbers: true,
    allowNegatives: false,
    exclude: ['0', '1'],
    max: 6,
  });
  const pow1: Tree = { type: 'Power', operands: [x, b] };
  const d = generateNumber({
    ...settings,
    useWholeNumbers: true,
    allowNegatives: false,
    exclude: ['0'],
    max: +getValue(b) - 1,
  });
  const pow2: Tree = getValue(d) === '1' ? x : { type: 'Power', operands: [x, d] };

  return optionalNegative({
    type: 'Fraction',
    operands: [
      optionalNegative({ type: 'Product', operands: [a, pow1] }),
      optionalNegative({ type: 'Product', operands: [c, pow2] }),
    ],
  });
}

/**
 * Complexity:
 * 0: numbers up to 9, no fraction or negatives, only x and y, only on the left
 * 1: numbers up to 12, allow negatives, more solution variables, allow on either side
 * 2: up to 2 negatives, allow fractions
 * 3: numbers up to 16, up to 3 negatives
 */
export function generateDistributionExpression(complexity: 0 | 1 | 2 | 3 = 0): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings: Settings = {
    max: 9,
    excludeZero: true,
    allowNegatives: false,
    nameChoices: 'xy'.split(''),
    useWholeNumbers: true,
    fractionCount: 0,
  };
  const choices = [generateSimpleExpression, generateTwoDistributions];
  if (complexity > 0) {
    settings.max = 12;
    settings.excludeZero = false;
    settings.allowNegatives = true;
    settings.nameChoices = 'abcdghkmnpqrstuvwxyz'.split('');
    maxSignCount = 1;
    choices.push(generateTwoDistributions2, generateDistribution3);
  }
  if (complexity > 1) {
    maxSignCount = 2;
    settings.fractionCount = pickOne([0, 1, 2]);
    choices.push();
  }
  if (complexity > 2) {
    settings.max = 16;
    settings.fractionCount = pickOne([1, 2, 3, 4]);
    maxSignCount = 3;
  }

  let tree = pickOne(choices)(settings);
  tree = addMissingParentheses(limitSignCount(tree, maxSignCount));
  return treeToLatex(tree as ExpressionTree);
}

/**
 * Complexity:
 * 0: numbers up to 9, no fraction or negatives, only x and y, only on the left
 * 1: numbers up to 12, allow negatives, more solution variables, allow on either side
 * 2: up to 2 negatives, allow fractions
 * 3: numbers up to 16, up to 3 negatives
 */
export function generateDivideMonomialExpression(complexity: 0 | 1 | 2 | 3 = 0): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings: Settings = {
    max: 9,
    excludeZero: true,
    allowNegatives: false,
    nameChoices: 'xy'.split(''),
    useWholeNumbers: true,
    fractionCount: 1,
  };
  const choices = [generateFractionOfMonomials];
  if (complexity > 0) {
    settings.max = 12;
    settings.excludeZero = false;
    settings.allowNegatives = true;
    settings.nameChoices = 'abcdghkmnpqrstuvwxyz'.split('');
    maxSignCount = 1;
  }
  if (complexity > 1) {
    maxSignCount = 2;
  }
  if (complexity > 2) {
    settings.max = 16;
    maxSignCount = 3;
  }

  let tree = pickOne(choices)(settings);
  tree = addMissingParentheses(limitSignCount(tree, maxSignCount));
  return treeToLatex(tree as ExpressionTree);
}
