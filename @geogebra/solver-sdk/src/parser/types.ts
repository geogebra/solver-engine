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
  | 'Equation' // binary
  | 'Inequation' // binary
  | 'EquationSystem' // n-ary
  | 'InequalitySystem' // n-ary
  | 'AddEquations' // binary
  | 'SubtractEquations' // binary
  | 'EquationUnion' // n-ary
  | 'StatementWithConstraint' // binary
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
export type NumberExpression = {
  type: 'Number';
  value: string;
};

export type VariableExpression = {
  type: 'Variable';
  value: string;
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
    | NumberExpression
    | VariableExpression
    | NameExpression
    | { type: '/undefined/' | '/infinity/' | 'Reals' }
  ) &
  T;

export type ExpressionTree = ExpressionTreeBase<{ path: string }>;
export type NestedExpression = NestedExpressionBase<{ path: string }>;
