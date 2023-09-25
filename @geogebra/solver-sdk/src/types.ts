import { ExpressionTreeBase } from './parser';

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
  polynomialEquationSolvingStrategy?: string;
};

export type Strategy = {
  strategy: string;
  description: string;
};

export type StrategyMap = {
  [category: string]: Strategy[];
};

export type ApiMathFormat =
  | 'solver' // solver format - do not use unless you know why you are doing it
  | 'latex' // latex format - good for quick rendering, but lacks metadata
  | 'json2'; // object-based json format

export type API_VERSION_INFO_RESPONSE = {
  commit: string;
  deploymentName: string;
};

export type API_PLANS_RESPONSE = PlanId[];
export type API_STRATEGIES_RESPONSE = StrategyMap;

type PlanSelectionBase<T> = {
  transformation: T;
  metadata: {
    methodId: PlanId;
  };
};

export type PlanSelectionSolver = PlanSelectionBase<TransformationSolver>;
export type PlanSelectionLatex = PlanSelectionBase<TransformationLatex>;
export type PlanSelectionJson = PlanSelectionBase<TransformationJson>;
export type PlanSelection = PlanSelectionSolver | PlanSelectionLatex | PlanSelectionJson;

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

// This is how the `json` format is typed.
// For a human-centric description of this format, see docs/expression-serialization.md
export type MathJson = ExpressionTreeBase<Record<never, never>>;

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

export type Tag = 'Rearrangement' | 'Cosmetic' | 'Pedantic' | 'InvisibleChange';

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
  alternatives: null | AlternativeBase<MathFormat>[];
};

export type TaskBase<MathFormat> = {
  taskId: string;
  startExpr: MathFormat;
  pathMappings: PathMapping[];
  explanation: null | MetadataBase<MathFormat>;
  steps: null | TransformationBase<MathFormat>[];
  dependsOn: null | string[];
};

export type AlternativeBase<MathFormat> = {
  strategy: string;
  explanation: MetadataBase<MathFormat>;
  steps: null | TransformationBase<MathFormat>[];
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

export type AlternativeSolver = AlternativeBase<string>;
export type AlternativeLatex = AlternativeBase<string>;
export type AlternativeJson = AlternativeBase<MathJson>;
export type Alternative = AlternativeSolver | AlternativeLatex | AlternativeJson;

export type MetadataSolver = MetadataBase<string>;
export type MetadataLatex = MetadataBase<string>;
export type MetadataJson = MetadataBase<MathJson>;

export type Metadata = MetadataSolver | MetadataLatex | MetadataJson;
