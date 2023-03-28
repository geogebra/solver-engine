import type { DecoratorType, ExpressionTree, NestedExpression } from './types';

export type TransformerFunction = (node: ExpressionTree, defaultResult: string) => string;

export function treeToSolver(n: ExpressionTree): string {
  const rec = (n: ExpressionTree): string => treeToSolver(n);
  const dec = (solver: string): string => decorate(solver, n.decorators);
  switch (n.type) {
    case 'Number':
      return dec(n.value);
    case 'Variable':
      return dec(n.value);
    case 'Sum':
      return dec(
        n.args
          .map((el, i) =>
            el.type === 'Minus' && !el.decorators?.length
              ? // binary minus
                `-${rec((el as NestedExpression).args[0])}`
              : el.type === 'PlusMinus' && !el.decorators?.length
              ? // binary ±
                `+/-${rec((el as NestedExpression).args[0])}`
              : // binary plus
              i === 0
              ? rec(el)
              : `+${rec(el)}`,
          )
          .join(''),
      );
    case 'Plus':
      return dec(`+${rec(n.args[0])}`);
    case 'Minus':
      return dec(`-${rec(n.args[0])}`);
    case 'PlusMinus':
      return dec(`+/-${rec(n.args[0])}`);
    case 'Product':
      return dec(
        n.args
          .map((el, i) => (i === 0 || el.type === 'DivideBy' ? rec(el) : `*${rec(el)}`))
          .join(''),
      );
    case 'ImplicitProduct':
      return dec(n.args.map((el) => rec(el)).join(' '));
    case 'DivideBy':
      return `:${rec(n.args[0])}`;
    case 'Fraction':
      return dec(`[${rec(n.args[0])} / ${rec(n.args[1])}]`);
    case 'MixedNumber':
      return dec(`[${rec(n.args[0])} ${rec(n.args[1])} / ${rec(n.args[2])}]`);
    case 'Power':
      return dec(`[${rec(n.args[0])} ^ ${rec(n.args[1])}]`);
    case 'SquareRoot':
      return dec(`sqrt[${rec(n.args[0])}]`);
    case 'Root':
      return dec(`root[${rec(n.args[0])}, ${rec(n.args[1])}]`);
    case 'Equation':
      return dec(`${rec(n.args[0])} = ${rec(n.args[1])}`);
    case 'EquationSystem':
      return dec(n.args.map((el) => rec(el)).join(', '));
    case 'EquationUnion':
      return dec(n.args.map((el) => rec(el)).join(' OR '));
    case 'UNDEFINED':
      return dec('UNDEFINED');
    case 'INFINITY':
      return dec('INFINITY');
    case 'Reals':
      return dec('REALS');
    case 'LessThan':
      return dec(`${rec(n.args[0])} < ${rec(n.args[1])}`);
    case 'GreaterThan':
      return dec(`${rec(n.args[0])} > ${rec(n.args[1])}`);
    case 'LessThanEqual':
      return dec(`${rec(n.args[0])} <= ${rec(n.args[1])}`);
    case 'GreaterThanEqual':
      return dec(`${rec(n.args[0])} >= ${rec(n.args[1])}`);
    case 'Solution':
      return dec(`Solution[${rec(n.args[0])}, ${rec(n.args[1])}]`);
    case 'SetSolution':
    case 'ImplicitSolution':
    case 'Contradiction':
    case 'Identity':
      return dec(`${n.type}[${rec(n.args[0]).replace(/\(\)/, '')}: ${rec(n.args[1])})}]`);
    case 'FiniteSet':
      return dec(`{${n.args.map((el) => rec(el)).join(', ')}}`);
    case 'OpenInterval':
      return `(${rec(n.args[0])}, ${rec(n.args[1])})`;
    case 'ClosedInterval':
      return `[${rec(n.args[0])}, ${rec(n.args[1])}]`;
    case 'OpenClosedInterval':
      return `(${rec(n.args[0])}, ${rec(n.args[1])}]`;
    case 'ClosedOpenInterval':
      return `[${rec(n.args[0])}, ${rec(n.args[1])})`;
  }
}

function decorate(value: string, decorators?: DecoratorType[]): string {
  if (!decorators) return value;
  return decorators.reduce((res, dec) => {
    if (dec === 'RoundBracket') return `(${res})`;
    if (dec === 'SquareBracket') return `[.${res}.]`;
    if (dec === 'CurlyBracket') return `{.${res}.}`;
    return res;
  }, value);
}
