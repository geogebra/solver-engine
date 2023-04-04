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
  preferDecimals?: boolean;
  solutionVariable?: string;
};

export type API_VERSION_INFO_RESPONSE = {
  commit: string;
  deploymentName: string;
};

export type API_PLANS_RESPONSE = PlanId[];
export type API_APPLY_PLAN_RESPONSE = Transformation;

type PlanSelectionBase<MathFormat> = {
  transformation: TransformationBase<MathFormat>;
  metadata: {
    methodId: PlanId;
  };
};

export type PlanSelectionSolver = PlanSelectionBase<string>;
export type PlanSelectionLatex = PlanSelectionBase<string>;
export type PlanSelectionJson = PlanSelectionBase<MathJson>;
export type PlanSelection = PlanSelectionSolver | PlanSelectionLatex | PlanSelectionJson;

export type API_SELECT_PLANS_RESPONSE = PlanSelection[];

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

type MetadataBase<MathFormat> = {
  key: string;
  params: { expression: MathFormat; pathMappings: PathMapping[] }[];
};

export type Tag = 'Rearrangement' | 'Cosmetic' | 'Pedantic';

type TransformationBase<MathFormat> = {
  type: 'Plan' | 'Rule' | 'TaskSet';
  tags: Tag[];

  path: string;
  fromExpr: MathFormat;
  toExpr: MathFormat;
  pathMappings: PathMapping[];
  explanation: MetadataBase<MathFormat>;
  gmAction: null | GmAction;
  skills: MetadataBase<MathFormat>[];
  steps: null | TransformationBase<MathFormat>[];
  tasks: null | TaskBase<MathFormat>[];
};

export type TaskBase<MathFormat> = {
  taskId: string;
  startExpr: MathFormat;
  pathMappings: PathMapping[];
  explanation: null | MetadataBase<MathFormat>;
  steps: null | TransformationBase<MathFormat>[];
  dependsOn: null | string[];
};

export type TransformationSolver = TransformationBase<string>;
export type TransformationLatex = TransformationBase<string>;
export type TransformationJson = TransformationBase<MathJson>;
export type Transformation =
  | TransformationSolver
  | TransformationLatex
  | TransformationJson;

export type TaskSolver = TaskBase<string>;
export type TaskLatex = TaskBase<string>;
export type TaskJson = TaskBase<MathJson>;
export type Task = TaskSolver | TaskLatex | TaskJson;

export type MetadataSolver = MetadataBase<string>;
export type MetadataLatex = MetadataBase<string>;
export type MetadataJson = MetadataBase<MathJson>;
export type Metadata = MetadataSolver | MetadataLatex | MetadataJson;
