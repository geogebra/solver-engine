export type { LatexSettings } from './tree-to-latex';

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
  | 'AbsoluteValue' // unary
  | 'Equation' // binary
  | 'EquationSystem' // n-ary
  | 'AddEquations' // binary
  | 'SubtractEquations' // binary
  | 'EquationUnion' // n-ary
  | 'LessThan' // binary
  | 'GreaterThan' // binary
  | 'LessThanEqual' // binary
  | 'GreaterThanEqual' // binary
  /** 'Solution' just means the "element of" operator (∈) */
  | 'Solution' // binary
  | 'Identity'
  | 'Contradiction'
  | 'ImplicitSolution'
  | 'SetSolution'
  | 'VariableList'
  | 'Tuple'
  | 'FiniteSet' // n-ary
  | 'CartesianProduct' // n-ary
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
  | { type: 'UNDEFINED' | 'INFINITY' | 'Reals' }
) &
  T;

export type ExpressionTree = ExpressionTreeBase<{ path: string }>;
export type NestedExpression = NestedExpressionBase<{ path: string }>;

export type TransformerFunction = (
  node: ExpressionTree,
  defaultResult: string,
  /** Parent of node. */
  parent: ExpressionTree | null,
) => string;
