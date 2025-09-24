import {
  ExpressionTreeBase,
  NestedExpression,
  NestedExpressionBase,
  VariableExpression,
} from '../parser';

export type Tree = ExpressionTreeBase<{ optional?: boolean }>;
export type NestedTree = NestedExpressionBase<{ optional?: boolean }>;
export type TreeLeaf = Omit<Tree, 'operands'> & { value: string };

export type ProductTree = NestedTree & {
  type: 'Product' | 'SmartProduct' | 'ImplicitProduct';
};

export type NumberSettings = {
  max: number;
  excludeZero?: boolean;
  allowNegatives: boolean;
  useWholeNumbers: boolean;
  exclude?: string[];
};

export type VariableSettings = {
  nameChoices: string[];
  name?: string;
};

export type FractionSettings = {
  max: number;
  allowNegatives: boolean;
  useWholeNumbers: boolean;
  optionalLeadingSign?: boolean;
  // don't allow 6/3, but allow 6/4
  indivisible?: boolean;
};

const RELATION_TYPES = [
  'GreaterThan',
  'LessThan',
  'GreaterThanEqual',
  'LessThanEqual',
  'Equation',
];

// We exclude some letters:
// - efij
// - l, o, s, z because they could easily be confused with 1, 0, 5, 2
// - e, i, j because they are well-known constants
// - f because it's often used for a function (so f(x + 1) looks like function application)
export const nameChoices = 'abcdghkmnpqrtuvwxy'.split('');

export function pickOne<T>(choices: T[]): T {
  return choices[Math.floor(Math.random() * choices.length)];
}

export function getPermutation<T>(choices: T[]): T[] {
  const result = [...choices];
  const N = choices.length;
  for (let i = N - 1; i >= 1; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    const temp = result[i];
    result[i] = result[j];
    result[j] = temp;
  }
  return result;
}

export function getValue(expr: Tree): string {
  if (expr.type === 'Minus') return (-+getValue(expr.operands[0])).toString();
  if (expr.type === 'DivideBy') return (1 / +getValue(expr.operands[0])).toString();
  if (expr.type === 'Sum') {
    return expr.operands
      .reduce((sum, operand) => {
        return sum + +getValue(operand);
      }, 0)
      .toString();
  }
  if (expr.type === 'Product' || expr.type === 'SmartProduct') {
    return expr.operands
      .reduce((product, operand) => {
        return product * +getValue(operand);
      }, 1)
      .toString();
  }
  if (expr.type === 'Degree') {
    return getValue(expr.operands[0]);
  }
  if (expr.type === 'Power') {
    return Math.pow(+getValue(expr.operands[0]), +getValue(expr.operands[1])).toString();
  }
  if ('value' in expr) return expr.value;
  else return '';
}

// Depth-first traversal
export function traverseTree(tree: Tree, callback: (expr: Tree) => void) {
  if ('operands' in tree)
    tree.operands.forEach((operand) => traverseTree(operand, callback));
  callback(tree);
}

export function cloneTree(tree: Tree): Tree {
  if ('operands' in tree) {
    return { ...tree, operands: tree.operands.map((operand) => cloneTree(operand)) };
  } else return { ...tree };
}

export function mapTree(tree: Tree, callback: (expr: Tree) => Tree): Tree {
  const newTree = callback(tree);
  if ('operands' in newTree) {
    const newOperands = newTree.operands.map((operand) => mapTree(operand, callback));
    return { ...newTree, operands: newOperands };
  }
  return newTree;
}

export function filterTree(predicate: (tree: Tree) => boolean, tree: Tree): Tree[] {
  const result: Tree[] = [];
  traverseTree(tree, (expr: Tree) => {
    if (predicate(expr)) result.push(expr);
  });
  return result;
}

export function addMissingParentheses(tree: Tree): Tree {
  return mapTree(tree, (expr: Tree) => {
    if (expr.type === 'Product' || expr.type === 'SmartProduct') {
      expr.operands = expr.operands.map((op) => {
        if (op.type === 'Minus') {
          return { decorators: ['RoundBracket'], ...op };
        }
        return op;
      });
    }
    return expr;
  });
}

export function limitSignCount(tree: Tree, maxSignCount: number): Tree {
  if (maxSignCount < Infinity) {
    const signs: Tree[] = filterTree(
      (subtree: Tree) => subtree.type === 'Minus' && !!subtree.optional,
      tree,
    );
    const keepSignCount = Math.floor(
      Math.random() * (Math.min(signs.length, maxSignCount) + 1),
    );
    const signsToDelete = new Set(getPermutation(signs).slice(keepSignCount));
    return mapTree(tree, (expr: Tree) => {
      if (signsToDelete.has(expr)) {
        // for [(-[a/b])^2] we need to keep the brackets when removing the -
        if (expr.decorators?.length && 'operands' in (expr as NestedTree).operands[0]) {
          (expr as NestedTree).operands[0].decorators = expr.decorators;
        }
        return (expr as NestedTree).operands[0];
      } else return expr;
    });
  } else return tree;
}

export function flipRelation(math: Tree) {
  if (RELATION_TYPES.includes(math.type)) {
    return { ...math, operands: (math as NestedExpression).operands.slice().reverse() };
  } else return math;
}

export function optionalFlip(
  math: Tree,
  { mayFlipEquation }: { mayFlipEquation: boolean },
) {
  if (!mayFlipEquation) return math;
  else return pickOne([math, flipRelation(math)]);
}

export function createCoefficient(
  settings: NumberSettings & { useFraction?: boolean } & FractionSettings,
): Tree {
  return settings.useFraction
    ? generateFraction({ ...settings, max: 6 })
    : generateNumber({
        ...settings,
        allowNegatives: false,
        exclude: ['0'].concat(settings.exclude || []),
      });
}

export function createNumberOrFraction(
  settings: NumberSettings & { useFraction?: boolean } & FractionSettings,
): Tree {
  return settings.useFraction
    ? generateFraction({ ...settings, max: 6 })
    : generateNumber(settings);
}

export function createVariableWithCoefficient(
  settings: NumberSettings &
    VariableSettings & { useFraction?: boolean } & FractionSettings,
  variable?: Tree,
): Tree {
  const coefficient = createCoefficient(settings);
  variable = variable || generateVariable(settings);
  return optionalNegative(createReducedProduct(coefficient, variable));
}

export function createReducedProduct(a: Tree, b: Tree, minusIsOptional = true): Tree {
  if (getValue(a) === '1') return b;
  if (getValue(a) === '-1')
    return { type: 'Minus', operands: [b], optional: minusIsOptional };
  return { type: 'Product', operands: [a, b] };
}

export function createCommutedSum(...addends: Tree[]): Tree {
  const operands = getPermutation(addends);
  return { type: 'Sum', operands };
}

export function createCommutedProduct(...factors: Tree[]): Tree {
  const operands = getPermutation(factors);
  return { type: 'Product', operands };
}

export function createEquation(
  lhs: Tree,
  rhs: Tree,
  settings: { mayFlipEquation: boolean },
): Tree {
  return optionalFlip({ type: 'Equation', operands: [lhs, rhs] }, settings);
}

export function generatePosValue(settings: NumberSettings): string {
  const { max, useWholeNumbers, allowNegatives } = settings;
  for (let i = 0; i < 100; i++) {
    let number = +(Math.random() * max).toFixed(2);
    if (useWholeNumbers) number = Math.round(number);
    const numStr = number.toString();
    if (settings.excludeZero && numStr === '0') continue;
    if (settings.exclude?.includes(numStr)) continue;
    if (allowNegatives && settings.exclude?.includes('-' + numStr)) continue;
    return numStr;
  }
  throw new Error("Couldn't generate number. " + JSON.stringify(settings));
}

export function generateFixedNumber(value: string, negativeIsOptional = false): Tree {
  const val = +value;
  const numberType = Number.isInteger(val) ? 'Integer' : 'Decimal';
  if (val < 0)
    return {
      type: 'Minus',
      optional: negativeIsOptional,
      operands: [{ type: numberType, value: (-val).toString() }],
    } satisfies Tree;
  else return { type: numberType, value };
}

export function generateNumber(settings: NumberSettings): Tree {
  const { useWholeNumbers, allowNegatives } = settings;
  const numStr = generatePosValue(settings);
  const numberExpr: Tree = {
    type: useWholeNumbers ? 'Integer' : 'Decimal',
    value: numStr,
  };
  if (allowNegatives) return optionalNegative(numberExpr);
  else return numberExpr;
}

export function generateVariable(settings: VariableSettings): VariableExpression {
  const { nameChoices: solutionVariableChoices, name: solutionVariable } = settings;
  return {
    type: 'Variable',
    value: solutionVariable || pickOne(solutionVariableChoices),
  };
}

export function optionalNegative(math: Tree): Tree {
  // no -0
  if ('value' in math && math.value === '0') return math;
  return { type: 'Minus', operands: [math], optional: true };
}

/** a/b */
export function generateFraction(settings: FractionSettings, numerator?: Tree): Tree {
  const a = numerator || generateNumber({ ...settings, exclude: ['0', '-1'] });
  for (let i = 0; i < 100; i++) {
    const b = generateNumber({
      ...settings,
      exclude: ['0', '1', '-1', 'value' in a ? a.value : ''],
    });
    if (settings.indivisible && +getValue(a) % +getValue(b) === 0) continue;
    const fraction: Tree = { type: 'Fraction', operands: [a, b] };
    if (settings.optionalLeadingSign) return optionalNegative(fraction);
    else return fraction;
  }
  throw new Error("Couldn't generate fraction. " + JSON.stringify(settings));
}
