import type { DecoratorType, ExpressionTree, NestedExpression } from '../parser';

export type TransformerFunction = (node: ExpressionTree, defaultResult: string) => string;

export function treeToSolver(n: ExpressionTree): string {
  const rec = (n: ExpressionTree): string => treeToSolver(n);
  const dec = (solver: string): string => decorate(solver, n.decorators);
  switch (n.type) {
    case 'Integer':
    case 'Decimal':
    case 'RecurringDecimal':
      return dec(n.value);
    case 'Variable':
      return dec(`${n.value}${n.subscript ? `_${n.subscript}` : ''}`);
    case 'Sum':
      return dec(
        n.operands
          .map((el, i) =>
            i === 0
              ? // unary plus / minus / ±
                rec(el)
              : el.type === 'Minus' && !el.decorators?.length
              ? // binary minus
                ` - ${rec((el as NestedExpression).operands[0])}`
              : el.type === 'PlusMinus' && !el.decorators?.length
              ? // binary ±
                ` +/- ${rec((el as NestedExpression).operands[0])}`
              : // binary plus
                ` + ${rec(el)}`,
          )
          .join(''),
      );
    case 'Plus':
      return dec(`+${rec(n.operands[0])}`);
    case 'Minus':
      return dec(`-${rec(n.operands[0])}`);
    case 'PlusMinus':
      return dec(`+/-${rec(n.operands[0])}`);
    case 'Product':
      return dec(
        n.operands
          .map((el, i) => (i === 0 || el.type === 'DivideBy' ? rec(el) : `* ${rec(el)}`))
          .join(' '),
      );
    case 'ImplicitProduct':
      return dec(n.operands.map((el) => rec(el)).join(' '));
    case 'SmartProduct':
      return dec(
        n.operands.map((el, i) => (n.signs[i] ? `* ${rec(el)}` : `${rec(el)}`)).join(' '),
      );
    case 'DivideBy':
      return `: ${rec(n.operands[0])}`;
    case 'Fraction':
      return dec(`[${rec(n.operands[0])} / ${rec(n.operands[1])}]`);
    case 'MixedNumber':
      return dec(`[${rec(n.operands[0])} ${rec(n.operands[1])} / ${rec(n.operands[2])}]`);
    case 'Power':
      return dec(`[${rec(n.operands[0])} ^ ${rec(n.operands[1])}]`);
    case 'SquareRoot':
      return dec(`sqrt[${rec(n.operands[0])}]`);
    case 'Root':
      return dec(`root[${rec(n.operands[0])}, ${rec(n.operands[1])}]`);
    case 'AbsoluteValue':
      return dec(`abs[${rec(n.operands[0])}]`);
    case 'ExpressionWithConstraint':
      return dec(`${rec(n.operands[0])} GIVEN ${rec(n.operands[1])}`);
    case 'Equation':
      return dec(`${rec(n.operands[0])} = ${rec(n.operands[1])}`);
    case 'Inequation':
      return dec(`${rec(n.operands[0])} != ${rec(n.operands[1])}`);
    case 'EquationSystem':
      return dec(n.operands.map((el) => rec(el)).join(' AND '));
    case 'AddEquations':
      return dec(n.operands.map((el) => rec(el)).join(' /+/ '));
    case 'SubtractEquations':
      return dec(n.operands.map((el) => rec(el)).join(' /-/ '));
    case 'EquationUnion':
      return dec(n.operands.map((el) => rec(el)).join(' OR '));
    case 'Undefined':
      return dec('/undefined/');
    case 'Infinity':
      return dec('/infinity/');
    case 'LessThan':
      return dec(`${rec(n.operands[0])} < ${rec(n.operands[1])}`);
    case 'GreaterThan':
      return dec(`${rec(n.operands[0])} > ${rec(n.operands[1])}`);
    case 'LessThanEqual':
      return dec(`${rec(n.operands[0])} <= ${rec(n.operands[1])}`);
    case 'GreaterThanEqual':
      return dec(`${rec(n.operands[0])} >= ${rec(n.operands[1])}`);
    case 'Solution':
      return dec(`Solution[${rec(n.operands[0])}, ${rec(n.operands[1])}]`);
    case 'SetSolution':
    case 'ImplicitSolution':
    case 'Contradiction':
    case 'Identity': {
      const varList = rec(n.operands[0]).replace(/\(\)/, '');
      return dec(`${n.type}[${varList ? varList + ': ' : ''}${rec(n.operands[1])}]`);
    }
    case 'FiniteSet':
      return dec(n.operands ? `{${n.operands.map(rec).join(', ')}}` : `{}`);
    case 'Reals':
      return dec('/reals/');
    case 'Void':
      return dec('/void/');
    case 'OpenInterval':
      return `(${rec(n.operands[0])}, ${rec(n.operands[1])})`;
    case 'ClosedInterval':
      return `[${rec(n.operands[0])}, ${rec(n.operands[1])}]`;
    case 'OpenClosedInterval':
      return `(${rec(n.operands[0])}, ${rec(n.operands[1])}]`;
    case 'ClosedOpenInterval':
      return `[${rec(n.operands[0])}, ${rec(n.operands[1])})`;
    case 'OpenRange':
      return `${rec(n.operands[0])} < ${rec(n.operands[1])} < ${rec(n.operands[2])}`;
    case 'OpenClosedRange':
      return `${rec(n.operands[0])} < ${rec(n.operands[1])} <= ${rec(n.operands[2])}`;
    case 'ClosedOpenRange':
      return `${rec(n.operands[0])} <= ${rec(n.operands[1])} < ${rec(n.operands[2])}`;
    case 'ClosedRange':
      return `${rec(n.operands[0])} <= ${rec(n.operands[1])} <= ${rec(n.operands[2])}`;
    case 'ReversedOpenRange':
      return `${rec(n.operands[0])} > ${rec(n.operands[1])} > ${rec(n.operands[2])}`;
    case 'ReversedOpenClosedRange':
      return `${rec(n.operands[0])} > ${rec(n.operands[1])} >= ${rec(n.operands[2])}`;
    case 'ReversedClosedOpenRange':
      return `${rec(n.operands[0])} >= ${rec(n.operands[1])} > ${rec(n.operands[2])}`;
    case 'ReversedClosedRange':
      return `${rec(n.operands[0])} >= ${rec(n.operands[1])} >= ${rec(n.operands[2])}`;
    case 'CartesianProduct':
      return n.operands.map(rec).join(' * ');
    case 'SetUnion':
      return dec(`SetUnion[${n.operands.map(rec).join(', ')}]`);
    case 'SetDifference':
      return dec(`${rec(n.operands[0])} \\ ${rec(n.operands[1])}`);
    case 'Tuple':
      if (n.operands.length === 1) {
        return rec(n.operands[0]);
      } else {
        return `(${n.operands.map(rec).join(', ')})`;
      }
    case 'List':
    case 'VariableList':
      return n.operands ? n.operands.map(rec).join(', ') : '';
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
