import { ExpressionTree, ExpressionTreeBase, NestedExpression, NumberExpression } from '../parser';
import { setsSolutionFormatter, SolutionFormatter } from './solution-formatter';
import { ColorMap } from '../solutions/coloring';

// Make sure to put a space after a latex command to avoid, e.g., "2\\cdotx"
export type LatexSettings = {
  mulSymbol?: ' \\cdot ' | ' \\times ' | string;
  divSymbol?: ' \\div ' | ':' | string;
  align?: boolean;
  displayNames?: boolean;

  solutionFormatter: SolutionFormatter;
};

const DefaultSettings: LatexSettings = {
  mulSymbol: ' \\cdot ',
  divSymbol: ' \\div ',
  displayNames: true,
  solutionFormatter: setsSolutionFormatter,
};

/**
 * Enables customization of node transformations within an expression tree or decorators around
 * expressions in the tree. It provides methods for transforming the tree based on the specified
 * path-scope.
 */
export interface LatexTransformer {
  defaultTextColor: string;

  /**
   * Returns a transformed LaTeX representation by treating the node to be transformed with a
   * path-scope "Expression" i.e. the whole expression created from current node needs to be
   * transformed.
   */
  transformNode(
    node: ExpressionTree,
    originalLatex: string,
    parent: ExpressionTree | null,
  ): string;

  /**
   * Returns a transformed LaTeX representation by treating the node to be transformed with a
   * path-scope "operator", meaning the current node has an operator that needs to be transformed.
   */
  transformOperator(
    node: ExpressionTree,
    originalLatex: string,
    parent: ExpressionTree | null,
  ): string | null;

  /**
   * Returns a transformed LaTeX representation. by treating the node to be transformed with a
   * path-scope "outerOperator" i.e. the `parent` (left) adjacent to `node` needs to be transformed.
   */
  transformOuterOperator(
    node: ExpressionTree,
    originalLatex: string,
    parent: ExpressionTree | null,
  ): string | null;

  /**
   * Returns a transformed LaTeX representation by treating the node to be transformed with a
   * path-scope "decorator", meaning only the decorator(s) of the expression represented by the
   * current node need to be transformed.
   */
  transformDecorator(
    node: ExpressionTree,
    originalLatex: string,
    parent: ExpressionTree | null,
  ): string;
}

export class ColorLatexTransformer implements LatexTransformer {
  constructor(
    private readonly colorMap: ColorMap,
    public readonly defaultTextColor: string,
  ) {}

  private applyColorAndLayout(
    color: string,
    node: ExpressionTree,
    originalLatex: string,
    parent: ExpressionTree | null,
  ): string {
    let layoutCorrection = '';
    if (
      (parent?.type === 'Sum' || parent?.type === 'Product') &&
      parent?.args[0] !== node
    ) {
      // need this so that binary operators are shown with correct spacing:
      // Example: 1-2 ==> 1{\color{red}{}-2}
      layoutCorrection = '{}';
    }
    // 1. We can't have opening and closing parentheses (i.e. `{` and `}`) around the whole
    //    colored latex string below, since otherwise size aligned decorators
    //    (i.e. `\left(` and `\right)`) around an expression wouldn't be possible to be colored
    //    for e.g. '{\\color{red}\left(} x {\\color{red}\right(}` isn't a valid LaTeX string as
    //    `\left(` and `\right)` need to be in the same scope.
    // 2. We necessarily need colored LaTeX string to end with `\\color{${this.defaultTextColor}}`
    //    otherwise, the current LaTeX color would spread to unintended path-scope(s).
    return `\\color{${color}}${layoutCorrection}${originalLatex}\\color{${this.defaultTextColor}}`;
  }

  transformNode(
    node: ExpressionTree,
    originalLatex: string,
    parent: ExpressionTree | null,
  ): string {
    const color = this.colorMap[node.path];
    if (!color) return originalLatex;
    return this.applyColorAndLayout(color, node, originalLatex, parent);
  }

  transformDecorator(
    node: ExpressionTree,
    originalLatex: string,
    parent: ExpressionTree | null,
  ): string {
    const color = this.colorMap[`${node.path}:decorator`];
    if (!color) return originalLatex;
    return this.applyColorAndLayout(color, node, originalLatex, parent);
  }

  transformOperator(
    node: ExpressionTree,
    originalLatex: string,
    parent: ExpressionTree | null,
  ): string | null {
    const color = this.colorMap[`${node.path}:op`];
    if (!color) return color;
    return this.applyColorAndLayout(color, node, originalLatex, parent);
  }

  transformOuterOperator(
    node: ExpressionTree,
    originalLatex: string,
    parent: ExpressionTree | null,
  ): string | null {
    const color = this.colorMap[`${node.path}:outerOp`];
    if (!color) return color;
    return this.applyColorAndLayout(color, node, originalLatex, parent);
  }
}

const defaultTransformer: LatexTransformer = {
  defaultTextColor: 'black',
  transformNode: (node, originalLatex) => originalLatex,
  transformOperator: () => null,
  transformOuterOperator: () => null,
  transformDecorator: (node, originalLatex) => originalLatex,
};

export function treeToLatex(
  n: ExpressionTree,
  settings?: LatexSettings,
  transformer?: LatexTransformer,
): string {
  return treeToLatexInner(
    n,
    null,
    { ...DefaultSettings, ...settings },
    transformer || defaultTransformer,
  );
}

function treeToLatexInner(
  /** Current node */
  n: ExpressionTree,
  /** Parent of `n` */
  p: ExpressionTree | null,
  s: LatexSettings,
  t: LatexTransformer,
): string {
  const rec = (n: ExpressionTree, p: ExpressionTree | null): string =>
    treeToLatexInner(n, p, s, t);

  const tfd = (latex: string): string => {
    const transformed = t.transformNode(n, decorate(latex, n, t, p), p);
    if (!s.displayNames || !n.name) {
      return transformed;
    }
    if (s.align) {
      return transformed + ` & \\textrm{${n.name}}`;
    } else {
      return transformed + ` \\qquad \\textrm{${n.name}}`;
    }
  };

  const colorOp = (operator: string): string => {
    return t.transformOperator(n, operator, p) || operator;
  };

  const outerColorOp = (node: ExpressionTree, operator: string): string => {
    return (
      t.transformOuterOperator(node, operator, p) ||
      t.transformOperator(n, operator, p) ||
      operator
    );
  };

  switch (n.type) {
    case 'Number':
      return tfd(numberToLatex(n));
    case 'Variable':
      return tfd(n.value);
    case 'Name':
      return tfd(`\\textrm{${n.value}}`);
    case 'Sum':
      return tfd(
        n.args
          .map((el, i) => {
            if (addendNeedsPlusInFront(el, i)) {
              return outerColorOp(el, '+') + rec(el, n);
            } else {
              return rec(el, n);
            }
          })
          .join(''),
      );
    case 'Plus':
      return tfd(`${outerColorOp(n.args[0], '+')}${rec(n.args[0], n)}`);
    case 'Minus':
      return tfd(`${outerColorOp(n.args[0], '-')}${rec(n.args[0], n)}`);
    case 'PlusMinus':
      return tfd(`\\pm ${rec(n.args[0], n)}`);
    case 'Product':
      return tfd(
        n.args
          .map((el, i) => {
            if (i === 0 || el.type === 'DivideBy') {
              return rec(el, n);
            } else {
              return outerColorOp(el, `${s.mulSymbol}`) + rec(el, n);
            }
          })
          .join(''),
      );
    case 'ImplicitProduct':
      return tfd(n.args.map((el) => rec(el, n)).join(''));
    case 'SmartProduct':
      return tfd(
        n.args
          .map((el, i) => {
            if (i === 0 || el.type === 'DivideBy') {
              return rec(el, n);
            } else {
              return outerColorOp(el, `${n.signs[i] ? s.mulSymbol : ''}`) + rec(el, n);
            }
          })
          .join(''),
      );
    case 'DivideBy':
      return tfd(outerColorOp(n.args[0], `${s.divSymbol}`) + `${rec(n.args[0], n)}`);
    case 'Fraction':
      return tfd(`\\frac{${rec(n.args[0], n)}}{${rec(n.args[1], n)}}`);
    case 'MixedNumber':
      return tfd(
        `${rec(n.args[0], n)}\\frac{${rec(n.args[1], n)}}{${rec(n.args[2], n)}}`,
      );
    case 'Power':
      return tfd(`{${rec(n.args[0], n)}}^{${rec(n.args[1], n)}}`);
    case 'SquareRoot':
      return tfd(`\\sqrt{${rec(n.args[0], n)}}`);
    case 'Root':
      return tfd(`\\sqrt[{${rec(n.args[1], n)}}]{${rec(n.args[0], n)}}`);
    case 'AbsoluteValue':
      return tfd(colorAbsoluteValue(rec(n.args[0], n), n, t, p));
    case 'Equation':
      if (s.align) {
        return tfd(`${rec(n.args[0], n)} & ${colorOp('=')} & ${rec(n.args[1], n)}`);
      } else {
        return tfd(`${rec(n.args[0], n)} ${colorOp('=')} ${rec(n.args[1], n)}`);
      }
    case 'Inequation':
      if (s.align) {
        return tfd(`${rec(n.args[0], n)} & ${colorOp('\\neq')} & ${rec(n.args[1], n)}`);
      } else {
        return tfd(`${rec(n.args[0], n)} ${colorOp('\\neq')} ${rec(n.args[1], n)}`);
      }
    case 'EquationSystem': {
      const alignSetting = { ...s, align: true };
      return tfd(
        '\\left\\{\\begin{array}{rcl}\n' +
          n.args
            .map((el) => '  ' + treeToLatexInner(el, n, alignSetting, t) + '\\\\\n')
            .join('') +
          '\\end{array}\\right.',
      );
    }
    case 'InequalitySystem': {
      const alignSetting = { ...s, align: true };
      return tfd(
        '\\left\\{\\begin{array}{rcl}\n' +
          n.args
            .map((el) => '  ' + treeToLatexInner(el, n, alignSetting, t) + '\\\\\n')
            .join('') +
          '\\end{array}\\right.',
      );
    }
    case 'AddEquations':
    case 'SubtractEquations': {
      const alignSetting = { ...s, align: true };
      const operation = n.type === 'AddEquations' ? '+' : '-';
      return tfd(
        '\\begin{array}{rcl|l}\n' +
          '  ' +
          treeToLatexInner(n.args[0], n, alignSetting, t) +
          ` & ${operation} \\\\\n` +
          '  ' +
          treeToLatexInner(n.args[1], n, alignSetting, t) +
          ' & \\\\\n' +
          '\\end{array}',
      );
    }
    case 'EquationUnion': {
      const alignSetting = { ...s, align: false };
      return tfd(
        n.args.map((el) => treeToLatexInner(el, n, alignSetting, t)).join('\\text{ or }'),
      );
    }
    case 'StatementWithConstraint': {
      const alignSetting = { ...s, align: false };
      return tfd(
        '\\left\\{\\begin{array}{l}\n' +
          n.args
            .map((el) => '  ' + treeToLatexInner(el, n, alignSetting, t) + '\\\\\n')
            .join('') +
          '\\end{array}\\right.',
      );
    }
    case '/undefined/':
      return tfd('\\text{undefined}');
    case '/infinity/':
      return tfd('\\infty');
    case 'LessThan':
      return tfd(`${rec(n.args[0], n)} ${colorOp('<')} ${rec(n.args[1], n)}`);
    case 'GreaterThan':
      return tfd(`${rec(n.args[0], n)} ${colorOp('>')} ${rec(n.args[1], n)}`);
    case 'LessThanEqual':
      return tfd(`${rec(n.args[0], n)} ${colorOp('\\leq')} ${rec(n.args[1], n)}`);
    case 'GreaterThanEqual':
      return tfd(`${rec(n.args[0], n)} ${colorOp('\\geq')} ${rec(n.args[1], n)}`);
    case 'Solution':
    case 'SetSolution':
    case 'Identity':
    case 'Contradiction':
    case 'ImplicitSolution':
      return tfd(s.solutionFormatter.formatSolution(n, rec));
    case 'VariableList':
      return tfd(`${n.args.map((x) => rec(x, n)).join(', ')}`);
    case 'Tuple':
      if (n.args.length === 1) {
        return tfd(rec(n.args[0], n));
      } else {
        return tfd(`\\left( ${n.args.map((x) => rec(x, n)).join(', ')}\\right)`);
      }
    case 'FiniteSet':
      return tfd(
        n.args.length === 0
          ? '\\emptyset'
          : `\\left\\{${n.args.map((el) => rec(el, n)).join(', ')}\\right\\}`,
      );
    case 'Reals':
      return tfd('\\mathbb{R}');
    case 'Void':
      return tfd('\\textrm{VOID}');
    case 'CartesianProduct':
      return tfd(
        n.args.length === 0
          ? '\\emptyset'
          : `${n.args.map((el) => rec(el, n)).join(' \\times ')}`,
      );
    case 'SetUnion':
      return tfd(
        n.args.length === 0
          ? '\\emptyset'
          : `${n.args.map((el) => rec(el, n)).join(' \\cup ')}`,
      );
    case 'SetDifference':
      return tfd(`${rec(n.args[0], n)} \\setminus ${rec(n.args[1], n)}`);
    case 'OpenInterval':
      return `\\left( ${rec(n.args[0], n)}, ${rec(n.args[1], n)} \\right)`;
    case 'ClosedInterval':
      return `\\left[ ${rec(n.args[0], n)}, ${rec(n.args[1], n)} \\right]`;
    case 'OpenClosedInterval':
      return `\\left( ${rec(n.args[0], n)}, ${rec(n.args[1], n)} \\right]`;
    case 'ClosedOpenInterval':
      return `\\left[ ${rec(n.args[0], n)}, ${rec(n.args[1], n)} \\right)`;
    case 'OpenRange':
      return `${rec(n.args[0], n)} \\lt ${rec(n.args[1], n)} \\lt ${rec(n.args[2], n)}`;
    case 'OpenClosedRange':
      return `${rec(n.args[0], n)} \\lt ${rec(n.args[1], n)} \\leq ${rec(n.args[2], n)}`;
    case 'ClosedOpenRange':
      return `${rec(n.args[0], n)} \\leq ${rec(n.args[1], n)} \\lt ${rec(n.args[2], n)}`;
    case 'ClosedRange':
      return `${rec(n.args[0], n)} \\leq ${rec(n.args[1], n)} \\leq ${rec(n.args[2], n)}`;
    case 'ReversedOpenRange':
      return `${rec(n.args[0], n)} \\gt ${rec(n.args[1], n)} \\gt ${rec(n.args[2], n)}`;
    case 'ReversedOpenClosedRange':
      return `${rec(n.args[0], n)} \\gt ${rec(n.args[1], n)} \\geq ${rec(n.args[2], n)}`;
    case 'ReversedClosedOpenRange':
      return `${rec(n.args[0], n)} \\geq ${rec(n.args[1], n)} \\gt ${rec(n.args[2], n)}`;
    case 'ReversedClosedRange':
      return `${rec(n.args[0], n)} \\geq ${rec(n.args[1], n)} \\geq ${rec(n.args[2], n)}`;
  }
}

function addendNeedsPlusInFront(
  addend: ExpressionTreeBase<{ path: string }>,
  indexOfAddend: number,
) {
  // the first addend in a sum doesn't need a leading plus
  if (indexOfAddend === 0) return false;
  // if we have a - or ± addend, we don't need a plus if there aren't any brackets
  if (!addend.decorators?.length && ['Minus', 'PlusMinus'].includes(addend.type))
    return false;
  // if the addend is a partial sum and starts with a - or ± addend, we don't
  // need to put a plus around the whole partial sum
  if (
    addend.decorators?.[0] === 'PartialBracket' &&
    ['Minus', 'PlusMinus'].includes((addend as NestedExpression).args[0].type)
  ) {
    return false;
  }
  return true;
}

function numberToLatex(n: NumberExpression): string {
  // check for number with repeating digits
  const [value, repeatingDigits] = n.value.split('[');
  return repeatingDigits !== undefined
    ? `${value}\\overline{${repeatingDigits.slice(0, -1)}}`
    : value;
}

const decorators: Record<string, { left: string; right: string }> = {
  RoundBracket: { left: '\\left(', right: '\\right)' },
  SquareBracket: { left: '\\left[', right: '\\right]' },
  CurlyBracket: { left: '\\left\\{', right: '\\right\\}' },
};

function colorAbsoluteValue(
  value: string,
  node: ExpressionTree,
  t: LatexTransformer,
  parent: ExpressionTree | null,
): string {
  const left = t.transformOperator(node, '\\left|', parent) || '\\left|';
  const right = t.transformOperator(node, '\\right|', parent) || '\\right|';
  return left + value + right;
}

function decorate(
  value: string,
  node: ExpressionTree,
  t: LatexTransformer,
  parent: ExpressionTree | null,
): string {
  if (!node.decorators) return value;
  const leftDecorators = node.decorators.map((d) => decorators[d]?.left || '').reverse();
  const rightDecorators = node.decorators.map((d) => decorators[d]?.right || '');
  return (
    t.transformDecorator(node, leftDecorators.join(''), parent) +
    value +
    t.transformDecorator(node, rightDecorators.join(''), parent)
  );
}
