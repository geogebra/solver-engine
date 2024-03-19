/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

import type {
  DecoratorType,
  ExpressionTree,
  NestedExpression,
  TrigonometricFunctions,
} from '../parser';

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
    case 'Power': {
      const base = n.operands[0];
      if ((base.type as TrigonometricFunctions) && (base as any).powerInside) {
        return dec(
          `[${base.type.toLowerCase()} ^ ${rec(n.operands[1])}] ${rec(
            (base as any).operands[0],
          )}`,
        );
      } else {
        return dec(`[${rec(base)} ^ ${rec(n.operands[1])}]`);
      }
    }
    case 'SquareRoot':
      return dec(`sqrt[${rec(n.operands[0])}]`);
    case 'Root':
      return dec(`root[${rec(n.operands[0])}, ${rec(n.operands[1])}]`);
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
      return dec(`${n.type.toLowerCase()} ${rec(n.operands[0])}`);
    case 'Arcsin':
    case 'Arccos':
    case 'Arctan':
    case 'Arccot':
    case 'Arcsec':
    case 'Arccsc':
      if (n.inverseNotation === 'superscript') {
        return dec(`[${n.type.slice(3).toLowerCase()} ^ -1] ${rec(n.operands[0])}`);
      } else {
        return dec(`${n.type.toLowerCase()} ${rec(n.operands[0])}`);
      }
    case 'Arsinh':
    case 'Arcosh':
    case 'Artanh':
    case 'Arcoth':
    case 'Arcsch':
    case 'Arsech':
      if (n.inverseNotation === 'superscript') {
        return dec(`[${n.type.slice(2).toLowerCase()} ^ -1] ${rec(n.operands[0])}`);
      } else {
        return dec(`${n.type.toLowerCase()} ${rec(n.operands[0])}`);
      }
    case 'LogBase10':
      return dec(`log ${rec(n.operands[0])}`);
    case 'NaturalLog':
      return dec(`ln ${rec(n.operands[0])}`);
    case 'Log':
      return dec(`log_[${rec(n.operands[0])}] ${rec(n.operands[1])}`);
    case 'Derivative':
      return dec(
        (n.operands[0].type === 'Integer' && n.operands[0].value === '1'
          ? 'diff'
          : `[diff ^ ${rec(n.operands[0])}]`) +
          `[${rec(n.operands[1])} / ${n.operands
            .slice(2)
            .map((op) => rec(op))
            .join(' ')}]`,
      );
    case 'IndefiniteIntegral':
      return dec(`prim[${rec(n.operands[0])}, ${rec(n.operands[1])}]`);
    case 'DefiniteIntegral':
      return dec(`int[${n.operands.map((op) => rec(op)).join(', ')}]`);
    case 'Percent':
      return dec(`${rec(n.operands[0])} %`);
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
    case 'EulerE':
      return dec('/e/');
    case 'Pi':
      return dec('/pi/');
    case 'ImaginaryUnit':
      return dec('/i/');
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
