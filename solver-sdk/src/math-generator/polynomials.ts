import { ExpressionTree } from '../parser';
import { treeToLatex } from '../renderer';
import {
  type NestedTree,
  type Tree,
  createCommutedSum,
  createReducedProduct,
  generateFraction,
  generateNumber,
  generateVariable,
  getPermutation,
  limitSignCount,
  optionalNegative,
  pickOne,
  getValue,
  cloneTree,
  type ProductTree,
  generatePosValue,
  generateFixedNumber,
} from './utils';

type PolynomialSettings = {
  max: number;
  multipleOf?: number;
  maxDegree: number;
  minDegree: number;
  terms: number;
  fractionCount: number;
  name?: string;
  nameChoices: string[];
  exclude?: string[];
  allowNegatives: boolean;
  normalized: boolean;
  useWholeNumbers: boolean;
  useFraction?: boolean;
};

function getRange(min: number, max: number) {
  const a = [];
  for (let i = min; i <= max; i++) a.push(i);
  return a;
}

function createMonomial(
  exponent: number,
  variable: Tree,
  settings: PolynomialSettings,
): Tree {
  const normalized = settings.normalized;
  const power: Tree =
    exponent === 1
      ? variable
      : {
          type: 'Power',
          operands: [variable, { type: 'Integer', value: exponent.toString() }],
        };
  let prod: Tree;
  if (settings.useFraction && !settings.multipleOf) {
    // use "a*x/b" or "x/b" or "a/b x"
    const fract = generateFraction({
      ...settings,
      allowNegatives: !normalized,
      indivisible: true,
    }) as NestedTree;
    if (exponent === 0) prod = fract;
    else
      switch (normalized ? 0 : pickOne([0, 1, 2])) {
        case 0:
          // a/b x
          prod = { type: 'Product', operands: [fract, power] };
          break;
        case 1:
          // x/b
          fract.operands[0] = optionalNegative(power);
          prod = fract;
          break;
        case 2: {
          // a*x/b
          const a =
            fract.operands[0].type === 'Minus'
              ? fract.operands[0].operands[0]
              : fract.operands[0];
          fract.operands[0] = optionalNegative(power);
          prod = createReducedProduct(a, fract) as NestedTree;
          prod.operands = getPermutation(prod.operands);
          break;
        }
        default:
          throw new Error('Invalid case');
      }
  } else {
    // no fraction
    let a = generatePosValue({ ...settings, exclude: ['0'] });
    if (settings.multipleOf) a = (+a * settings.multipleOf).toString();
    const num = generateFixedNumber(a);
    if (exponent === 0) prod = num;
    else prod = createReducedProduct(num, power) as NestedTree;
  }
  if (isProduct(prod) && !normalized) prod.operands = getPermutation(prod.operands);
  prod = optionalNegative(prod) as NestedTree;
  return prod;
}

function createPolynomial(settings: PolynomialSettings): Tree {
  if (settings.terms > settings.maxDegree + 1) throw new Error('Too many terms');
  for (let i = 0; i < 100; i++) {
    const fractionPositions = getPermutation(getRange(0, settings.terms)).slice(
      0,
      settings.fractionCount,
    );
    const exponents = getPermutation(
      getRange(settings.minDegree, settings.maxDegree),
    ).slice(0, settings.terms);
    if (settings.normalized) exponents.sort((a, b) => b - a);
    const variable = generateVariable(settings);
    const monomials = exponents.map((exp, index) => {
      return createMonomial(exp, variable, {
        ...settings,
        useFraction: fractionPositions.includes(index),
      });
    });
    const sum = settings.normalized
      ? ({ type: 'Sum', operands: monomials } satisfies NestedTree)
      : (createCommutedSum(...monomials) as NestedTree);
    if (!settings.normalized && isNormalizedPolynomial(sum)) continue;
    return sum;
  }
  throw new Error('Could not generate polynomial');
}

function generateSumOfTwoPolynomials(
  settings1: PolynomialSettings,
  settings2: PolynomialSettings,
): Tree {
  const name = pickOne(settings1.nameChoices);
  const a = createPolynomial({ ...settings1, name });
  a.decorators = ['RoundBracket'];
  let b = createPolynomial({ ...settings2, name });
  b.decorators = ['RoundBracket'];
  // the sign in front of b is special and will be on in 50% of the cases
  if (settings1.allowNegatives) b = pickOne([b, { type: 'Minus', operands: [b] }]);
  return { type: 'Sum', operands: [optionalNegative(a), b] };
}

/** a(P1)+b(P2) */
function generateSumOfTwoScaledPolynomials(
  settings1: PolynomialSettings,
  settings2: PolynomialSettings,
): Tree {
  const name = pickOne(settings1.nameChoices);
  const p1 = createPolynomial({ ...settings1, name });
  p1.decorators = ['RoundBracket'];
  const a = generateNumber({
    ...settings1,
    allowNegatives: false,
    exclude: ['0', '1'],
    max: 6,
  });
  const addend1 = createReducedProduct(a, p1) as NestedTree;
  const p2 = createPolynomial({ ...settings2, name });
  p2.decorators = ['RoundBracket'];
  const b = generateNumber({
    ...settings2,
    allowNegatives: false,
    exclude: ['0', '1'],
    max: 6,
  });
  let addend2 = createReducedProduct(b, p2) as NestedTree;
  // the sign in front of b is special and will be on in 50% of the cases
  if (settings1.allowNegatives)
    addend2 = pickOne([addend2, { type: 'Minus', operands: [addend2] }]);
  return { type: 'Sum', operands: [optionalNegative(addend1), addend2] };
}

function generatePolynomialDividedByMonomial(settings: PolynomialSettings): Tree {
  const name = pickOne(settings.nameChoices);
  const multipleOf = +generatePosValue({ ...settings, exclude: ['0'] });
  const polynomial = createPolynomial({ ...settings, name, multipleOf });
  const exponent = pickOne(getRange(1, settings.minDegree));
  const monomial = createMonomial(exponent, generateVariable({ name, nameChoices: [] }), {
    ...settings,
    multipleOf,
    allowNegatives: false,
    useFraction: false,
    max: 1,
  });
  return { type: 'Fraction', operands: [polynomial, monomial] };
}

function isVariablePower(tree: Tree): boolean {
  return (
    (tree.type === 'Power' && tree.operands[0].type === 'Variable') ||
    tree.type === 'Variable'
  );
}

function isNormalizedMonomial(tree: Tree): boolean {
  if (tree.type === 'Minus' && tree.operands[0].type !== 'Minus')
    return isNormalizedMonomial(tree.operands[0]);
  return (
    isCoefficient(tree) ||
    isVariablePower(tree) ||
    (isProduct(tree) &&
      tree.operands.length === 2 &&
      isCoefficient(tree.operands[0]) &&
      isVariablePower(tree.operands[1]))
  );
}

function isNumber(tree: Tree, allowMinus = true): boolean {
  return (
    tree.type === 'Integer' ||
    tree.type === 'Decimal' ||
    (allowMinus && tree.type === 'Minus' && isNumber(tree.operands[0], false))
  );
}

function isCoefficient(tree: Tree): boolean {
  return (
    isNumber(tree) ||
    (tree.type === 'Fraction' && tree.operands.every((op) => isNumber(op)))
  );
}

function isProduct(tree: Tree): tree is ProductTree {
  return ['Product', 'ImplicitProduct', 'SmartProduct'].includes(tree.type);
}

function getMonomialPower(tree: Tree): number {
  if (isCoefficient(tree)) return 0;
  else if (tree.type === 'Minus') return getMonomialPower(tree.operands[0]);
  else if (tree.type === 'Variable') return 1;
  else if (tree.type === 'Power' && tree.operands[0].type === 'Variable')
    return +getValue(tree.operands[1]);
  else if (isProduct(tree))
    return tree.operands.reduce((sum, op) => sum + getMonomialPower(op), 0);
  else if (tree.type === 'Sum')
    return tree.operands.reduce((max, op) => Math.max(max, getMonomialPower(op)), 0);
  else return NaN;
}

/** Only works on 'Sum' Tree type. */
function isNormalizedPolynomial(tree: NestedTree): boolean {
  if (tree.type !== 'Sum') throw new Error('Wrong kind of expression');
  // remove optional negatives
  const treeNoNeg = limitSignCount(cloneTree(tree), 0) as NestedTree;
  // check for descending exponents
  const exponents = treeNoNeg.operands.map(getMonomialPower);
  const isDescending = exponents.every((exp, index) => {
    if (index === 0) return true;
    else return exp < exponents[index - 1];
  });
  console.log(
    'Polynomial',
    treeToLatex(tree as ExpressionTree),
    'is in descending order:',
    isDescending,
  );
  if (!isDescending) return false;

  // check for normalized monomial form
  const hasNormalizedMonomials = treeNoNeg.operands.every(isNormalizedMonomial);
  console.log(
    'Polynomial',
    treeToLatex(tree as ExpressionTree),
    'hasNormalizedMonomials:',
    hasNormalizedMonomials,
  );
  return hasNormalizedMonomials;
}

/** Generate a polynomial that is not in normalized form. */
export function generateUnnormalizedPolynomial(complexity: 0 | 1 | 2 | 3 = 0): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings: PolynomialSettings = {
    max: 6,
    minDegree: 0,
    maxDegree: 2,
    terms: pickOne([2, 3]),
    normalized: false,
    allowNegatives: false,
    nameChoices: 'xy'.split(''),
    useWholeNumbers: true,
    fractionCount: 0,
  };
  const choices = [createPolynomial];
  if (complexity > 0) {
    settings.max = 9;
    settings.allowNegatives = true;
    settings.nameChoices = 'abcdghkmnpqrstuvwxyz'.split('');
    settings.maxDegree = 4;
    settings.terms = pickOne([2, 3, 3, 4]);
    settings.fractionCount = pickOne([0, 1]);
    maxSignCount = 1;
  }
  if (complexity > 1) {
    maxSignCount = 2;
    settings.maxDegree = 5;
    settings.fractionCount = pickOne([0, 1, 1, 2]);
    settings.terms = pickOne([3, 4, 5]);
  }
  if (complexity > 2) {
    settings.max = 12;
    settings.maxDegree = 6;
    settings.terms = pickOne([3, 4, 5]);
    settings.fractionCount = pickOne([0, 1, 1, 2, 2, 3]);
    maxSignCount = 3;
  }

  // console.log('isNormalized:', isNormalizedPolynomial(latexToTree('y^3 + 2y^2') as NestedTree));
  // console.log('isNormalized:', isNormalizedPolynomial(latexToTree('y^2 + 2y^3') as NestedTree));
  // console.log('isNormalized:', isNormalizedPolynomial(latexToTree('3x + x') as NestedTree));
  // console.log('isNormalized:', isNormalizedMonomial(latexToTree('\\frac{1}{2}x') as NestedTree));
  // console.log('isNormalized:', isNormalizedMonomial(latexToTree('\\frac{x}{2}') as NestedTree));
  // console.log('isNormalized:', isNormalizedPolynomial(latexToTree('-x^7-\\frac{3}{2}x^2+\\frac{1}{2}') as NestedTree));

  let tree = pickOne(choices)(settings);
  tree = limitSignCount(tree, maxSignCount);
  return treeToLatex(tree as ExpressionTree);
}

/** Divide by Monomial */
export function generateMonomialDivision(complexity: 0 | 1 | 2 | 3 = 0): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings: PolynomialSettings = {
    max: 6,
    minDegree: 1,
    maxDegree: 2,
    terms: 2,
    normalized: true,
    allowNegatives: false,
    nameChoices: 'xy'.split(''),
    useWholeNumbers: true,
    fractionCount: 0,
  };
  if (complexity > 0) {
    settings.max = 9;
    settings.allowNegatives = true;
    settings.nameChoices = 'abcdghkmnpqrstuvwxyz'.split('');
    settings.maxDegree = 4;
    settings.minDegree = pickOne([1, 2]);
    settings.terms = pickOne([2, 3]);
    maxSignCount = 0;
  }
  if (complexity > 1) {
    maxSignCount = 2;
    settings.maxDegree = 5;
    settings.minDegree = pickOne([1, 2]);
    settings.fractionCount = pickOne([0, 1]);
    const maxTerms = settings.maxDegree - settings.minDegree + 1;
    settings.terms = pickOne(getRange(2, Math.min(4, maxTerms)));
  }
  if (complexity > 2) {
    settings.max = 12;
    settings.maxDegree = 6;
    settings.minDegree = pickOne([1, 2, 2, 3]);
    const maxTerms = settings.maxDegree - settings.minDegree + 1;
    settings.terms = pickOne(getRange(2, Math.min(5, maxTerms)));
    settings.fractionCount = pickOne([0, 1, 1, 2]);
    maxSignCount = 4;
  }

  let tree = generatePolynomialDividedByMonomial(settings);
  tree = limitSignCount(tree, maxSignCount);
  return treeToLatex(tree as ExpressionTree);
}

/** Adding/Subtracting Polynomials */
export function generateSumOfPolynomials(complexity: 0 | 1 | 2 | 3 = 0): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings: PolynomialSettings = {
    max: 6,
    minDegree: 0,
    maxDegree: 2,
    terms: 2,
    normalized: true,
    allowNegatives: false,
    nameChoices: 'xy'.split(''),
    useWholeNumbers: true,
    fractionCount: 0,
  };
  let terms2 = 2;
  if (complexity > 0) {
    settings.max = 9;
    settings.allowNegatives = true;
    settings.nameChoices = 'abcdghkmnpqrstuvwxyz'.split('');
    settings.maxDegree = 4;
    settings.terms = pickOne([2, 3]);
    terms2 = pickOne([2, 3]);
    maxSignCount = 0;
  }
  if (complexity > 1) {
    maxSignCount = 2;
    settings.maxDegree = 5;
    settings.fractionCount = pickOne([0, 1]);
    settings.terms = pickOne([2, 3, 4]);
    terms2 = pickOne([2, 3, 4]);
  }
  if (complexity > 2) {
    settings.max = 12;
    settings.maxDegree = 6;
    settings.terms = pickOne([2, 3, 4, 5]);
    terms2 = pickOne([2, 3, 4, 5]);
    settings.fractionCount = pickOne([0, 1, 1, 2]);
    maxSignCount = 4;
  }
  const settings2 = { ...settings, terms: terms2 };

  let tree = generateSumOfTwoPolynomials(settings, settings2);
  tree = limitSignCount(tree, maxSignCount);
  return treeToLatex(tree as ExpressionTree);
}

/** Linear Combination of Polynomials */
export function generateLinearCombinationOfPolynomials(
  complexity: 0 | 1 | 2 | 3 = 0,
): string {
  // start with simplest settings for complexity 0
  let maxSignCount = 0;
  const settings: PolynomialSettings = {
    max: 6,
    maxDegree: 2,
    minDegree: 0,
    terms: 2,
    normalized: true,
    allowNegatives: false,
    nameChoices: 'xy'.split(''),
    useWholeNumbers: true,
    fractionCount: 0,
  };
  let terms2 = 2;
  if (complexity > 0) {
    settings.max = 9;
    settings.allowNegatives = true;
    settings.nameChoices = 'abcdghkmnpqrstuvwxyz'.split('');
    settings.maxDegree = 4;
    settings.terms = pickOne([2, 3]);
    terms2 = pickOne([2, 3]);
    maxSignCount = 0;
  }
  if (complexity > 1) {
    maxSignCount = 2;
    settings.maxDegree = 5;
    settings.fractionCount = pickOne([0, 1]);
    settings.terms = pickOne([2, 3, 4]);
    terms2 = pickOne([2, 3, 4]);
  }
  if (complexity > 2) {
    settings.max = 12;
    settings.maxDegree = 6;
    settings.terms = pickOne([2, 3, 4, 5]);
    terms2 = pickOne([2, 3, 4, 5]);
    settings.fractionCount = pickOne([0, 1, 1, 2]);
    maxSignCount = 4;
  }
  const settings2 = { ...settings, terms: terms2 };

  let tree = generateSumOfTwoScaledPolynomials(settings, settings2);
  tree = limitSignCount(tree, maxSignCount);
  return treeToLatex(tree as ExpressionTree);
}
