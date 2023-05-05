export type { LatexSettings, LatexTransformer } from './tree-to-latex';

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
    | { type: 'UNDEFINED' | 'INFINITY' | 'Reals' }
  ) &
  T;

export type ExpressionTree = ExpressionTreeBase<{ path: string }>;
export type NestedExpression = NestedExpressionBase<{ path: string }>;
