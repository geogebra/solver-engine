import {
  DecoratorType,
  ExpressionTree,
  NumberExpression,
  TransformerFunction,
} from './types';
import { setsSolutionFormatter, SolutionFormatter } from './solution-formatter';

// Make sure to put a space after a latex command to avoid, e.g., "2\\cdotx"
export type LatexSettings = {
  mulSymbol?: ' \\cdot ' | ' \\times ' | string;
  divSymbol?: ' \\div ' | ':' | string;
  align?: boolean;

  solutionFormatter: SolutionFormatter;
};

const DefaultSettings: LatexSettings = {
  mulSymbol: ' \\cdot ',
  divSymbol: ' \\div ',
  solutionFormatter: setsSolutionFormatter,
};

export function treeToLatex(
  n: ExpressionTree,
  settings?: LatexSettings,
  transformerFunction?: TransformerFunction,
): string {
  return treeToLatexInner(
    n,
    null,
    { ...DefaultSettings, ...settings },
    transformerFunction ? transformerFunction : (_, latex) => latex,
  );
}

function treeToLatexInner(
  /** Current node */
  n: ExpressionTree,
  /** Parent of `n` */
  p: ExpressionTree | null,
  s: LatexSettings,
  tf: TransformerFunction,
): string {
  const rec = (n: ExpressionTree, p: ExpressionTree | null): string =>
    treeToLatexInner(n, p, s, tf);
  const tfd = (latex: string): string => tf(n, decorate(latex, n.decorators), p);
  switch (n.type) {
    case 'Number':
      return tfd(numberToLatex(n));
    case 'Variable':
      return tfd(n.value);
    case 'Sum':
      return tfd(
        n.args
          .map((el, i) => {
            return i !== 0 &&
              (el.decorators?.length || !['Minus', 'PlusMinus'].includes(el.type))
              ? // non-leading "+" or bracketed expression, need to fill in the "+" manually
                `+${rec(el, n)}`
              : rec(el, n);
          })
          .join(''),
      );
    case 'Plus':
      return tfd(`+${rec(n.args[0], n)}`);
    case 'Minus':
      return tfd(`-${rec(n.args[0], n)}`);
    case 'PlusMinus':
      return tfd(`\\pm ${rec(n.args[0], n)}`);
    case 'Product':
      return tfd(
        n.args
          .map((el, i) => {
            return i === 0 || el.type === 'DivideBy'
              ? rec(el, n)
              : `${s.mulSymbol}${rec(el, n)}`;
          })
          .join(''),
      );
    case 'ImplicitProduct':
      return tfd(n.args.map((el) => rec(el, n)).join(''));
    case 'DivideBy':
      return tfd(`${s.divSymbol}${rec(n.args[0], n)}`);
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
    case 'Equation':
      if (s.align) {
        return tfd(`${rec(n.args[0], n)} & = & ${rec(n.args[1], n)}`);
      } else {
        return tfd(`${rec(n.args[0], n)} = ${rec(n.args[1], n)}`);
      }
    case 'EquationSystem': {
      const alignSetting = { ...s, align: true };
      return tfd(
        '\\left\\{\\begin{array}{rcl}\n' +
          n.args
            .map((el) => '  ' + treeToLatexInner(el, n, alignSetting, tf) + '\\\\\n')
            .join('') +
          '\\end{array}\\right.',
      );
    }
    case 'AddEquations': {
      const alignSetting = { ...s, align: true };
      return tfd(
        '\\begin{array}{rcl|l}\n' +
          '  ' +
          treeToLatexInner(n.args[0], n, alignSetting, tf) +
          ' & + \\\\\n' +
          '  ' +
          treeToLatexInner(n.args[1], n, alignSetting, tf) +
          ' & \\\\\n' +
          '\\end{array}',
      );
    }
    case 'SubtractEquations': {
      const alignSetting = { ...s, align: true };
      return tfd(
        '\\begin{array}{rcl|l}\n' +
          '  ' +
          treeToLatexInner(n.args[0], n, alignSetting, tf) +
          ' & - \\\\\n' +
          '  ' +
          treeToLatexInner(n.args[1], n, alignSetting, tf) +
          ' & \\\\\n' +
          '\\end{array}',
      );
    }
    case 'EquationUnion': {
      const alignSetting = { ...s, align: false };
      return tfd(
        n.args.map((el) => treeToLatexInner(el, n, alignSetting, tf)).join(', '),
      );
    }
    case 'UNDEFINED':
      return tfd('\\text{undefined}');
    case 'INFINITY':
      return tfd('\\infty');
    case 'Reals':
      return tfd('\\mathbb{R}');
    case 'LessThan':
      return tfd(`${rec(n.args[0], n)} < ${rec(n.args[1], n)}`);
    case 'GreaterThan':
      return tfd(`${rec(n.args[0], n)} > ${rec(n.args[1], n)}`);
    case 'LessThanEqual':
      return tfd(`${rec(n.args[0], n)} \\leq ${rec(n.args[1], n)}`);
    case 'GreaterThanEqual':
      return tfd(`${rec(n.args[0], n)} \\geq ${rec(n.args[1], n)}`);
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
    case 'CartesianProduct':
      return tfd(
        n.args.length === 0
          ? '\\emptyset'
          : `${n.args.map((el) => rec(el, n)).join(' \\times ')}`,
      );
    case 'OpenInterval':
      return `\\left( ${rec(n.args[0], n)}, ${rec(n.args[1], n)} \\right)`;
    case 'ClosedInterval':
      return `\\left[ ${rec(n.args[0], n)}, ${rec(n.args[1], n)} \\right]`;
    case 'OpenClosedInterval':
      return `\\left( ${rec(n.args[0], n)}, ${rec(n.args[1], n)} \\right]`;
    case 'ClosedOpenInterval':
      return `\\left[ ${rec(n.args[0], n)}, ${rec(n.args[1], n)} \\right)`;
  }
}

function numberToLatex(n: NumberExpression): string {
  // check for number with repeating digits
  const [value, repeatingDigits] = n.value.split('[');
  return repeatingDigits !== undefined
    ? `${value}\\overline{${repeatingDigits.slice(0, -1)}}`
    : value;
}

function decorate(value: string, decorators?: DecoratorType[]): string {
  if (!decorators) return value;
  return decorators.reduce((res, dec) => {
    if (dec === 'RoundBracket') return `\\left(${res}\\right)`;
    if (dec === 'SquareBracket') return `\\left[${res}\\right]`;
    if (dec === 'CurlyBracket') return `\\left\\{${res}\\right\\}`;
    return res;
  }, value);
}
