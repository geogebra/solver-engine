import type { DecoratorType, ExpressionTree, NestedExpression } from '../parser/types';

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
    case 'SmartProduct':
      return 'TODO';
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
    case 'AbsoluteValue':
      return dec(`abs[${rec(n.args[0])}]`);
    case 'Equation':
      return dec(`${rec(n.args[0])} = ${rec(n.args[1])}`);
    case 'EquationSystem':
      return dec(n.args.map((el) => rec(el)).join(', '));
    case 'AddEquations':
      return dec(n.args.map((el) => rec(el)).join(' /+/ '));
    case 'SubtractEquations':
      return dec(n.args.map((el) => rec(el)).join(' /-/ '));
    case 'EquationUnion':
      return dec(n.args.map((el) => rec(el)).join(' OR '));
    case 'StatementWithConstraint':
      return dec(`${rec(n.args[0])} GIVEN ${rec(n.args[1])}`);
    case '/undefined/':
      return dec('/undefined/');
    case '/infinity/':
      return dec('/infinity/');
    case '/reals/':
      return dec('/reals/');
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
    case 'CartesianProduct':
      return n.args.map(rec).join(' * ');
    case 'Tuple':
      if (n.args.length === 1) {
        return rec(n.args[0]);
      } else {
        return `(${n.args.map(rec).join(', ')})`;
      }
    case 'VariableList':
      return n.args.map(rec).join(', ');
    case 'Name':
      return `"${n.value}"`;
  }
}

function decorate(value: string, decorators?: DecoratorType[]): string {
  if (!decorators) return value;
  return decorators.reduce((res, dec) => {
    switch (dec) {
      case 'RoundBracket':
        return `(${res})`;
      case 'CurlyBracket':
        return `{.${res}.}`;
      case 'SquareBracket':
        return `[.${res}.]`;
      case 'PartialBracket':
        return `<.${res}.>`;
      case 'MissingBracket':
        return res;
    }
  }, value);
}
