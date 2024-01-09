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

import type { DecoratorType, ExpressionTree, NestedExpression } from '../parser';

export function treeToGgb(
  n: ExpressionTree,
  variableSubstitutions?: Record<string, string>,
): string {
  const rec = (n: ExpressionTree): string => treeToGgb(n, variableSubstitutions);
  const dec = (solver: string): string => decorate(solver, n.decorators);
  switch (n.type) {
    case 'Integer':
    case 'Decimal':
      return n.value;
    case 'RecurringDecimal': {
      // check for number with repeating digits
      const [value, repeatingDigits] = n.value.split('[');

      return dec(
        `${value}${[...repeatingDigits.slice(0, -1)].map((c) => '\u0305' + c).join()}}`,
      );
    }
    case 'Variable': {
      const varString = `${n.value}${n.subscript ? `_${n.subscript}` : ''}`;
      return dec(
        variableSubstitutions ? variableSubstitutions[varString] : varString || varString,
      );
    }
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
      return `/ ${rec(n.operands[0])}`;
    case 'Fraction':
      return dec(`(${rec(n.operands[0])}) / (${rec(n.operands[1])})`);
    case 'MixedNumber':
      return dec(
        `[${rec(n.operands[0])}\u2064${rec(n.operands[1])}/${rec(n.operands[2])}]`,
      );
    case 'Power': {
      const base = n.operands[0];
      return dec(`(${rec(base)}) ^ (${rec(n.operands[1])})`);
    }
    case 'SquareRoot':
      return dec(`sqrt(${rec(n.operands[0])})`);
    case 'Root':
      return dec(`nroot(${rec(n.operands[0])}, ${rec(n.operands[1])})`);
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
      return dec(`${n.type.toLowerCase()} ${rec(n.operands[0])}`);
    case 'Arsinh':
    case 'Arcosh':
    case 'Artanh':
    case 'Arcoth':
    case 'Arcsch':
    case 'Arsech':
      return dec(`${n.type.toLowerCase()} ${rec(n.operands[0])}`);
    case 'Log10':
      return dec(`log(${rec(n.operands[0])})`);
    case 'Ln':
      return dec(`ln(${rec(n.operands[0])})`);
    case 'Log':
      return dec(`log(${rec(n.operands[0])}, ${rec(n.operands[1])})`);
    case 'Percent':
      return dec(`(${rec(n.operands[0])})/100`);
    case 'AbsoluteValue':
      return dec(`abs(${rec(n.operands[0])})`);
    case 'Equation':
      return dec(`${rec(n.operands[0])} = ${rec(n.operands[1])}`);
    case 'Inequation':
      return dec(`${rec(n.operands[0])} != ${rec(n.operands[1])}`);

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
    case 'ExponentialE':
      return dec('\u212F');
    case 'Pi':
      return dec('\u03C0');
    case 'ImaginaryUnit':
      return dec('\u03AF');
    default:
      throw Error('cannot be converted to ggb ');
  }
}

function decorate(value: string, decorators?: DecoratorType[]): string {
  if (!decorators) return value;
  return decorators.reduce((res, dec) => {
    switch (dec) {
      case 'RoundBracket':
      case 'CurlyBracket':
      case 'SquareBracket':
      case 'MissingBracket':
        return `(${res})`;
      case 'PartialBracket':
        return res;
    }
  }, value);
}
