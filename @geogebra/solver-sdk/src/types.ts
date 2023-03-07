import { DecoratorType, NestedExpressionType } from './parser/types';

export type PlanId =
  | 'Equations.SolveLinearEquation'
  | 'Equations.SolveEquationUsingRootsMethod'
  | 'Equations.SolveQuadraticEquationByCompletingTheSquare'
  | 'Equations.SolveEquationByFactoring'
  | 'Equations.SolveQuadraticEquationUsingQuadraticFormula'
  | 'Inequalities.SolveLinearInequality'
  | 'IntegerArithmetic.EvaluateArithmeticExpression'
  | 'Approximation.ApproximateExpression'
  | 'Decimal.EvaluateExpressionAsDecimal'
  | 'MixedNumbers.AddMixedNumbers'
  | 'ConstantExpressions.SimplifyConstantExpression'
  | 'Polynomials.SimplifyAlgebraicExpressionInOneVariable'
  | 'Polynomials.ExpandPolynomialExpressionInOneVariable'
  | 'Polynomials.FactorPolynomialInOneVariable'
  | string;

export type SolverExpr = string;
export type LatexExpr = string;
export type SolverContext = {
  curriculum?: string;
  gmFriendly?: boolean;
  precision?: number;
};

export type API_VERSION_INFO_RESPONSE = {
  commit: string;
  deploymentName: string;
};

export type API_PLANS_RESPONSE = PlanId[];
export type API_APPLY_PLAN_RESPONSE = Transformation;
export type API_SELECT_PLANS_RESPONSE = {
  transformation: Transformation;
  metadata: {
    methodId: PlanId;
  };
}[];

export type PathMapping = {
  type:
    | 'Move'
    | 'Shift'
    | 'Factor'
    | 'Distribute'
    | 'Introduce'
    | 'Cancel'
    | 'Transform'
    | 'Combine'
    | 'Relate';
  fromPaths: string[];
  toPaths: string[];
};

export type MathJson =
  | [string] // variable, number, undefined, infinity, or reals
  | [[string, ...DecoratorType[]]] // variable or number
  | [NestedExpressionType, ...MathJson[]]
  | [[NestedExpressionType, ...DecoratorType[]], ...MathJson[]];

export type GmAction = {
  type:
    | 'Tap'
    | 'DoubleTap'
    | 'TapHold'
    | 'Drag'
    | 'DragCollect'
    | 'Formula'
    | 'Edit'
    | 'NotSupported';
  expressions?: string[];
  dragTo?: {
    expression: string;
    position?: 'Above' | 'Below' | 'LeftOf' | 'RightOf' | 'Onto' | 'OutsideOf';
  };
  formulaId?: string;
};

type Metadata<MathFormat> = {
  key: string;
  params: { expression: MathFormat; pathMappings: PathMapping[] }[];
};

type TransformationBase<MathFormat> = {
  type: 'Plan' | 'Rule' | 'TaskSet' | 'Rearrangement';
  path: string;
  fromExpr: MathFormat;
  toExpr: MathFormat;
  pathMappings: PathMapping[];
  explanation: Metadata<MathFormat>;
  gmAction: null | GmAction;
  skills: Metadata<MathFormat>[];
  steps: null | TransformationBase<MathFormat>[];
  tasks:
    | null
    | {
        taskId: string;
        startExpr: MathFormat;
        pathMappings: PathMapping[];
        explanation: null | Metadata<MathFormat>;
        steps: null | Transformation[];
        dependsOn: null | string[];
      }[];
};

export type TransformationSolver = TransformationBase<string>;
export type TransformationLatex = TransformationBase<string>;
export type TransformationJson = TransformationBase<MathJson>;
export type Transformation =
  | TransformationSolver
  | TransformationLatex
  | TransformationJson;
