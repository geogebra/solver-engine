export type NestedExpressionType =
  | 'Sum' // n-ary
  | 'Plus' // unary
  | 'Minus' // unary
  | 'PlusMinus' // unary
  | 'Product' // n-ary
  | 'ImplicitProduct' // n-ary
  | 'DivideBy' // unary
  | 'Fraction' // binary
  | 'MixedNumber' // ternary
  | 'Power' // binary
  | 'SquareRoot' // unary
  | 'Root' // binary
  | 'Equation' // binary
  | 'EquationSystem' // n-ary
  | 'EquationUnion' // n-ary
  | 'LessThan' // binary
  | 'GreaterThan' // binary
  | 'LessThanEqual' // binary
  | 'GreaterThanEqual' // binary
  /** 'Solution' just means the "element of" operator (∈) */
  | 'Solution' // binary
  | 'FiniteSet' // n-ary
  | 'OpenInterval' // binary
  | 'ClosedInterval' // binary
  | 'OpenClosedInterval' // binary
  | 'ClosedOpenInterval'; // binary

export type NestedExpressionBase<T> = {
  type: NestedExpressionType;
  args: ExpressionTreeBase<T>[];
};

export type NumberExpression = {
  type: 'Number';
  value: string;
};

export type VariableExpression = {
  type: 'Variable';
  value: string;
};

export type DecoratorType =
  | 'RoundBracket'
  | 'SquareBracket'
  | 'CurlyBracket'
  | 'MissingBracket';

export type ExpressionTreeBase<T> = { decorators?: DecoratorType[] } & (
  | NestedExpressionBase<T>
  | NumberExpression
  | VariableExpression
  | { type: 'UNDEFINED' | 'INFINITY' | 'REALS' }
) &
  T;

export type ExpressionTree = ExpressionTreeBase<{ path: string }>;
export type NestedExpression = NestedExpressionBase<{ path: string }>;
