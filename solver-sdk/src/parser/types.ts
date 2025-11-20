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

import { trigFunctions } from './latex-to-tree';

export type { LatexSettings, LatexTransformer } from '../renderer/tree-to-latex';

export type UnitType = 'Degree';

export type NestedExpressionType =
  | 'Sum' // n-ary
  | 'Plus' // unary
  | 'Minus' // unary
  | 'PlusMinus' // unary
  | 'Product' // n-ary
  | 'ImplicitProduct' // n-ary
  | 'DivideBy' // unary
  | 'Fraction' // binary
  | 'MixedNumber' // (number, number, number)
  | 'Power' // (base, power)
  | 'SquareRoot' // unary
  | 'Root' // (degree, radicand)
  | 'Log' // binary
  | 'NaturalLog' // unary
  | 'LogBase10' // unary
  | 'Derivative' // n-ary degree, expression dependents...
  | 'IndefiniteIntegral' // (function, variable)
  | 'DefiniteIntegral' // (lowerBound, upperBound, function, variable)
  | 'Percent' // unary
  | 'AbsoluteValue' // unary
  | 'ExpressionWithConstraint' // (expression, constraint)
  | 'Equation' // binary
  | 'Inequation' // binary
  | 'EquationSystem' // n-ary
  | 'AddEquations' // binary
  | 'SubtractEquations' // binary
  | 'EquationUnion' // n-ary
  | 'LessThan' // binary
  | 'GreaterThan' // binary
  | 'LessThanEqual' // binary
  | 'GreaterThanEqual' // binary
  | 'Solution' // (VariableList, FiniteSet | Interval)
  | 'Identity' // (VariableList)
  | 'Contradiction' // (VariableList)
  | 'ImplicitSolution' // (VariableList, Equation)
  | 'SetSolution' // (VariableList, FiniteSet | CartesianProduct)
  | 'VariableList' // n-ary list of variables
  | 'List' // n-ary list of expression (used in explanations)
  | 'Tuple'
  | 'FiniteSet' // n-ary
  | 'CartesianProduct' // n-ary
  | 'SetUnion' // n-ary
  | 'SetDifference' // binary
  | 'OpenInterval' // binary
  | 'ClosedInterval' // binary
  | 'OpenClosedInterval' // binary
  | 'ClosedOpenInterval' // binary
  | 'OpenRange' // ternary
  | 'OpenClosedRange' // ternary
  | 'ClosedOpenRange' // ternary
  | 'ClosedRange' // ternary
  | 'ReversedOpenRange' // ternary
  | 'ReversedOpenClosedRange' // ternary
  | 'ReversedClosedOpenRange' // ternary
  | 'ReversedClosedRange' // ternary
  | UnitType; // unary

export type NestedExpressionBase<T> = {
  type: NestedExpressionType;
  operands: ExpressionTreeBase<T>[];
};

export type NameExpression = {
  type: 'Name';
  value: string;
};

export type IntegerExpression = {
  type: 'Integer';
  value: string;
};

export type DecimalExpression = {
  type: 'Decimal';
  value: string;
};

export type RecurringDecimalExpression = {
  type: 'RecurringDecimal';
  value: string;
};

export type VariableExpression = {
  type: 'Variable';
  value: string;
  subscript?: string;
};

export type SmartProductExpression<T> = {
  type: 'SmartProduct';
  operands: ExpressionTreeBase<T>[];
  signs: boolean[];
};

export const bracketTypes = [
  'RoundBracket',
  'SquareBracket',
  'CurlyBracket',
  'MissingBracket',
  'PartialBracket',
] as const;

export type DecoratorType = (typeof bracketTypes)[number];

export function isDecorator(decoration: any): decoration is DecoratorType {
  return bracketTypes.includes(decoration);
}

export type ExpressionDecorations = {
  decorators?: DecoratorType[];
  name?: string;
};

export type TrigonometricFunctions = (typeof trigFunctions)[number]['type'];

type InverseNotation = 'arcPrefix' | 'aPrefix' | 'superscript';
export type TrigonometricExpression<T> = {
  type: TrigonometricFunctions;
  operands: ExpressionTreeBase<T>[];
  powerInside?: boolean;
  inverseNotation?: InverseNotation;
} & T;

export type ExpressionTreeBase<T> = ExpressionDecorations &
  (
    | NestedExpressionBase<T>
    | SmartProductExpression<T>
    | IntegerExpression
    | DecimalExpression
    | RecurringDecimalExpression
    | VariableExpression
    | NameExpression
    | TrigonometricExpression<T>
    | {
        type:
          | 'Undefined'
          | 'Infinity'
          | 'Reals'
          | 'Integers'
          | 'Void'
          | 'EulerE'
          | 'Pi'
          | 'ImaginaryUnit';
      }
  ) &
  T;

export type ExpressionTree = ExpressionTreeBase<{
  path: string;
}>;
export type NestedExpression = NestedExpressionBase<{
  path: string;
}>;
