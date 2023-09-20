export type { LatexSettings, LatexTransformer } from '../renderer/tree-to-latex';

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
  | 'ReversedClosedRange'; // ternary

export type NestedExpressionBase<T> = {
  type: NestedExpressionType;
  args: ExpressionTreeBase<T>[];
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
  args: ExpressionTreeBase<T>[];
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

export type ExpressionTreeBase<T> = ExpressionDecorations &
  (
    | NestedExpressionBase<T>
    | SmartProductExpression<T>
    | IntegerExpression
    | DecimalExpression
    | RecurringDecimalExpression
    | VariableExpression
    | NameExpression
    | { type: 'Undefined' | 'Infinity' | 'Reals' | 'Void' }
  ) &
  T;

export type ExpressionTree = ExpressionTreeBase<{ path: string }>;
export type NestedExpression = NestedExpressionBase<{ path: string }>;
