import {
  ExpressionTree,
  ExpressionTreeBase,
  NestedExpression,
  TrigonometricFunctions,
} from '../parser';
import { setsSolutionFormatter, SolutionFormatter } from './solution-formatter';
import { ColorMap } from '../solutions/coloring';
import { trigFunctions } from '../parser/latex-to-tree';

// Make sure to put a space after a latex command to avoid, e.g., "2\\cdotx"
export type LatexSettings = {
  mulSymbol?: ' \\cdot ' | ' \\times ' | string;
  divSymbol?: ' \\div ' | ':' | string;
  align?: boolean;
  flat?: boolean;
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
      parent?.operands[0] !== node
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
    case 'Integer':
    case 'Decimal':
      return tfd(n.value);
    case 'RecurringDecimal': {
      // check for number with repeating digits
      const [value, repeatingDigits] = n.value.split('[');
      return `${value}\\overline{${repeatingDigits.slice(0, -1)}}`;
    }
    case 'Variable':
      // Special case when the variable has a subscript and no decorators. We don't want to enclose the whole expression
      // (e.g. `x_2`) in color because it would create a box around it and mess up the spacing for powers (e.g. `x_2^3`
      // would have the exponent 3 moved to the right of `x_2`).
      if (n.subscript && !n.decorators) {
        return `${tfd(n.value)}_{${tfd(n.subscript)}}`;
      } else {
        return tfd(`${n.value}${n.subscript ? `_{${n.subscript}}` : ''}`);
      }
    case 'Name':
      return tfd(`\\textrm{${n.value}}`);
    case 'Sum':
      return tfd(
        n.operands
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
      return tfd(`${outerColorOp(n.operands[0], '+')}${rec(n.operands[0], n)}`);
    case 'Minus':
      return tfd(`${outerColorOp(n.operands[0], '-')}${rec(n.operands[0], n)}`);
    case 'PlusMinus':
      return tfd(`\\pm ${rec(n.operands[0], n)}`);
    case 'Product':
      return tfd(
        n.operands
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
      return tfd(n.operands.map((el) => rec(el, n)).join(''));
    case 'SmartProduct':
      return tfd(
        n.operands
          .map((el, i) => {
            if (i === 0 || el.type === 'DivideBy') {
              return rec(el, n);
            } else {
              // TODO: remove useSign logic when PLUT-684 is fixed.
              const useSign =
                el.decorators?.[0] === 'PartialBracket' && el.type === 'SmartProduct';
              return (
                outerColorOp(el, `${useSign || n.signs[i] ? s.mulSymbol : ''}`) +
                rec(el, n)
              );
            }
          })
          .join(''),
      );
    case 'DivideBy':
      return tfd(
        outerColorOp(n.operands[0], `${s.divSymbol}`) + `${rec(n.operands[0], n)}`,
      );
    case 'Fraction':
      return tfd(`\\frac{${rec(n.operands[0], n)}}{${rec(n.operands[1], n)}}`);
    case 'MixedNumber':
      return tfd(
        `${rec(n.operands[0], n)}\\frac{${rec(n.operands[1], n)}}{${rec(
          n.operands[2],
          n,
        )}}`,
      );
    case 'Power': {
      // Special case for the
      const base = n.operands[0];
      if (base.type === 'Variable' && base.subscript && !base.decorators) {
        return tfd(`${rec(base, n)}^{\\,${rec(n.operands[1], n)}}`);
      } else if ((base.type as TrigonometricFunctions) && (base as any).powerInside) {
        return tfd(
          `\\${base.type.toLowerCase()}^{${rec(n.operands[1], n)}}{${rec(
            (base as any).operands[0],
            n,
          )}}`,
        );
      } else {
        return tfd(`{${rec(base, n)}}^{${rec(n.operands[1], n)}}`);
      }
    }
    case 'SquareRoot':
      return tfd(`\\sqrt{${rec(n.operands[0], n)}}`);
    case 'Root':
      return tfd(`\\sqrt[{${rec(n.operands[1], n)}}]{${rec(n.operands[0], n)}}`);
    case 'Sin':
    case 'Cos':
    case 'Tan':
    case 'Cot':
    case 'Sec':
    case 'Csc':
    case 'Sinh':
    case 'Cosh':
    case 'Tanh':
    case 'Sech':
    case 'Csch':
    case 'Coth':
      return tfd(`\\${n.type.toLowerCase()}{${rec(n.operands[0], n)}}`);
    case 'Arcsin':
    case 'Arccos':
    case 'Arctan':
    case 'Arccot':
    case 'Arcsec':
    case 'Arccsc':
      if (n.inverseNotation === 'superscript') {
        return tfd(`\\${n.type.slice(3).toLowerCase()}^{-1}{${rec(n.operands[0], n)}}`);
      } else {
        return tfd(`\\${n.type.toLowerCase()}{${rec(n.operands[0], n)}}`);
      }
    case 'Arsinh':
    case 'Arcosh':
    case 'Artanh':
    case 'Arcoth':
    case 'Arcsch':
    case 'Arsech':
      if (n.inverseNotation === 'superscript') {
        return tfd(`\\${n.type.slice(2).toLowerCase()}^{-1}{${rec(n.operands[0], n)}}`);
      } else {
        return tfd(`\\${n.type.toLowerCase()}{${rec(n.operands[0], n)}}`);
      }
    case 'Log10':
      return tfd(`\\log{${rec(n.operands[0], n)}}`);
    case 'Log':
      return tfd(`\\log_{${rec(n.operands[0], n)}}{${rec(n.operands[1], n)}}`);
    case 'Ln':
      return tfd(`\\ln{${rec(n.operands[0], n)}}`);
    case 'AbsoluteValue':
      return tfd(colorAbsoluteValue(rec(n.operands[0], n), n, t, p));
    case 'ExpressionWithConstraint': {
      const latexSettings = { ...s, flat: true };
      const constraint = treeToLatexInner(n.operands[1], n, latexSettings, t);
      return tfd(`${rec(n.operands[0], n)} \\text{ given } ${constraint}`);
    }
    case 'Equation':
      return tfd(
        [rec(n.operands[0], n), colorOp('='), rec(n.operands[1], n)].join(
          s.align ? ' & ' : ' ',
        ),
      );
    case 'Inequation':
      return tfd(
        [rec(n.operands[0], n), colorOp('\\neq'), rec(n.operands[1], n)].join(
          s.align ? ' & ' : ' ',
        ),
      );
    case 'EquationSystem': {
      if (s.flat || n.operands.some((child) => !isSolvable(child))) {
        const alignSetting = { ...s, align: false };
        return tfd(
          n.operands
            .map((el) => treeToLatexInner(el, n, alignSetting, t))
            .join('\\text{ and }'),
        );
      } else {
        const alignSetting = { ...s, align: true };
        // The array has 5 columns specified, as extra columns do not affect rendering.
        // Currently we can have expressions with:
        // - 3 columns: Regular equations/inequalities
        // - 4 columns: Equations/Inequalities with labels
        return tfd(
          '\\left\\{\\begin{array}{rclrr}\n' +
            n.operands
              .map((el) => '  ' + treeToLatexInner(el, n, alignSetting, t) + '\\\\\n')
              .join('') +
            '\\end{array}\\right.',
        );
      }
    }
    case 'AddEquations':
    case 'SubtractEquations': {
      const alignSetting = { ...s, align: true };
      const operation = n.type === 'AddEquations' ? '+' : '-';
      // If any equation has labels we need to add an extra column
      const alignment = n.operands.find((op) => op.name) ? 'rclr|l' : 'rcl|l';
      return tfd(
        `\\begin{array}{${alignment}}\n` +
          '  ' +
          treeToLatexInner(n.operands[0], n, alignSetting, t) +
          (n.operands[0].name ? '' : ' & ') +
          ` & ${operation} \\\\\n` +
          '  ' +
          treeToLatexInner(n.operands[1], n, alignSetting, t) +
          ' & \\\\\n' +
          '\\end{array}',
      );
    }
    case 'EquationUnion': {
      const alignSetting = { ...s, align: false };
      return tfd(
        n.operands
          .map((el) => treeToLatexInner(el, n, alignSetting, t))
          .join('\\text{ or }'),
      );
    }
    case 'Undefined':
      return tfd('\\text{undefined}');
    case 'Infinity':
      return tfd('\\infty');
    case 'LessThan':
      return tfd(
        [rec(n.operands[0], n), colorOp('<'), rec(n.operands[1], n)].join(
          s.align ? ' & ' : ' ',
        ),
      );
    case 'GreaterThan':
      return tfd(
        [rec(n.operands[0], n), colorOp('>'), rec(n.operands[1], n)].join(
          s.align ? ' & ' : ' ',
        ),
      );
    case 'LessThanEqual':
      return tfd(
        [rec(n.operands[0], n), colorOp('\\leq'), rec(n.operands[1], n)].join(
          s.align ? ' & ' : ' ',
        ),
      );
    case 'GreaterThanEqual':
      return tfd(
        [rec(n.operands[0], n), colorOp('\\geq'), rec(n.operands[1], n)].join(
          s.align ? ' & ' : ' ',
        ),
      );
    case 'Solution':
    case 'SetSolution':
    case 'Identity':
    case 'Contradiction':
    case 'ImplicitSolution':
      return tfd(s.solutionFormatter.formatSolution(n, rec));
    case 'List':
      if (n.operands.length === 1) {
        return tfd(rec(n.operands[0], n));
      } else {
        return tfd(
          n.operands
            .slice(0, n.operands.length - 1)
            .map((x) => rec(x, n))
            .join(', ') +
            '\\text{ and }' +
            rec(n.operands[n.operands.length - 1], n),
        );
      }
    case 'VariableList':
      return tfd(`${n.operands.map((x) => rec(x, n)).join(', ')}`);
    case 'Tuple':
      if (n.operands.length === 1) {
        return tfd(rec(n.operands[0], n));
      } else {
        return tfd(`\\left( ${n.operands.map((x) => rec(x, n)).join(', ')}\\right)`);
      }
    case 'FiniteSet':
      return tfd(
        !n.operands || n.operands.length === 0
          ? '\\emptyset'
          : `\\left\\{${n.operands.map((el) => rec(el, n)).join(', ')}\\right\\}`,
      );
    case 'Reals':
      return tfd('\\mathbb{R}');
    case 'Void':
      return tfd('\\textrm{VOID}');
    case 'ExponentialE':
      return tfd('\\mathrm{e}');
    case 'Pi':
      return tfd('\\pi');
    case 'ImaginaryUnit':
      return tfd('\\iota');
    case 'Percent':
      return tfd(`${rec(n.operands[0], n)}\\%`);
    case 'CartesianProduct':
      return tfd(
        n.operands.length === 0
          ? '\\emptyset'
          : `${n.operands.map((el) => rec(el, n)).join(' \\times ')}`,
      );
    case 'SetUnion':
      return tfd(
        n.operands.length === 0
          ? '\\emptyset'
          : `${n.operands.map((el) => rec(el, n)).join(' \\cup ')}`,
      );
    case 'SetDifference':
      return tfd(`${rec(n.operands[0], n)} \\setminus ${rec(n.operands[1], n)}`);
    case 'OpenInterval':
      return `\\left( ${rec(n.operands[0], n)}, ${rec(n.operands[1], n)} \\right)`;
    case 'ClosedInterval':
      return `\\left[ ${rec(n.operands[0], n)}, ${rec(n.operands[1], n)} \\right]`;
    case 'OpenClosedInterval':
      return `\\left( ${rec(n.operands[0], n)}, ${rec(n.operands[1], n)} \\right]`;
    case 'ClosedOpenInterval':
      return `\\left[ ${rec(n.operands[0], n)}, ${rec(n.operands[1], n)} \\right)`;
    case 'OpenRange':
      return `${rec(n.operands[0], n)} \\lt ${rec(n.operands[1], n)} \\lt ${rec(
        n.operands[2],
        n,
      )}`;
    case 'OpenClosedRange':
      return `${rec(n.operands[0], n)} \\lt ${rec(n.operands[1], n)} \\leq ${rec(
        n.operands[2],
        n,
      )}`;
    case 'ClosedOpenRange':
      return `${rec(n.operands[0], n)} \\leq ${rec(n.operands[1], n)} \\lt ${rec(
        n.operands[2],
        n,
      )}`;
    case 'ClosedRange':
      return `${rec(n.operands[0], n)} \\leq ${rec(n.operands[1], n)} \\leq ${rec(
        n.operands[2],
        n,
      )}`;
    case 'ReversedOpenRange':
      return `${rec(n.operands[0], n)} \\gt ${rec(n.operands[1], n)} \\gt ${rec(
        n.operands[2],
        n,
      )}`;
    case 'ReversedOpenClosedRange':
      return `${rec(n.operands[0], n)} \\gt ${rec(n.operands[1], n)} \\geq ${rec(
        n.operands[2],
        n,
      )}`;
    case 'ReversedClosedOpenRange':
      return `${rec(n.operands[0], n)} \\geq ${rec(n.operands[1], n)} \\gt ${rec(
        n.operands[2],
        n,
      )}`;
    case 'ReversedClosedRange':
      return `${rec(n.operands[0], n)} \\geq ${rec(n.operands[1], n)} \\geq ${rec(
        n.operands[2],
        n,
      )}`;
  }
}

const isSolvable = (node: ExpressionTree) => {
  return (
    node.type === 'Equation' ||
    node.type === 'LessThan' ||
    node.type === 'LessThanEqual' ||
    node.type === 'GreaterThan' ||
    node.type === 'GreaterThanEqual' ||
    node.type === 'Inequation'
  );
};

function addendNeedsPlusInFront(
  addend: ExpressionTreeBase<{
    path: string;
  }>,
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
    ['Minus', 'PlusMinus'].includes((addend as NestedExpression).operands[0].type)
  ) {
    return false;
  }
  return true;
}

const decorators: Record<
  string,
  {
    left: string;
    right: string;
  }
> = {
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
