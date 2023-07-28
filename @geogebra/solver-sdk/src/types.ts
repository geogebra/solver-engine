import { DecoratorType, ExpressionDecorations, NestedExpressionType } from './parser';

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
  | 'json' // array-based json format, use this one (will be deprecated in favour of json2)
  | 'json2'; // object-based json format, not supported on all servers yet so use with care

export type API_VERSION_INFO_RESPONSE = {
  commit: string;
  deploymentName: string;
};

export type API_PLANS_RESPONSE = PlanId[];
export type API_APPLY_PLAN_RESPONSE = Transformation;
export type API_STRATEGIES_RESPONSE = StrategyMap;

type PlanSelectionBase<MathFormat> = {
  transformation: TransformationBase<MathFormat>;
  metadata: {
    methodId: PlanId;
  };
};

export type PlanSelectionSolver = PlanSelectionBase<string>;
export type PlanSelectionLatex = PlanSelectionBase<string>;
export type PlanSelectionJson = PlanSelectionBase<MathJson>;
export type PlanSelectionJson2 = PlanSelectionBase<MathJson2>;
export type PlanSelection =
  | PlanSelectionSolver
  | PlanSelectionLatex
  | PlanSelectionJson
  | PlanSelectionJson2;

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

// This is how the `json` format is typed.
export type MathJson =
  | [string] // variable, number, undefined, infinity, or reals
  | [[string, ...(DecoratorType | string)[]]] // variable or number
  | [NestedExpressionType, ...MathJson[]]
  | [[NestedExpressionType, ...(DecoratorType | string)[]], ...MathJson[]]
  | ['SmartProduct', ...SmartProductOperandJson[]]
  | [['SmartProduct', ...(DecoratorType | string)[]], ...SmartProductOperandJson[]];

export type SmartProductOperandJson = [boolean, MathJson];

// This is how the `json2` format is typed. This For a human-centric description of this format,
// see docs/expression-serialization.md
export type MathJson2 = ExpressionDecorations &
  (
    | {
        type: NestedExpressionType;
        operands?: MathJson2[];
      }
    | {
        type: 'Integer' | 'Decimal' | 'RecurringDecimal' | 'Name';
        value: string;
      }
    | {
        type: 'Variable';
        value: string;
        subscript?: string;
      }
    | {
        type: 'SmartProduct';
        operands: MathJson2[];
        signs: boolean[];
      }
  );

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
export type TransformationJson2 = TransformationBase<MathJson2>;
export type Transformation =
  | TransformationSolver
  | TransformationLatex
  | TransformationJson
  | TransformationJson2;

export type TaskSolver = TaskBase<string>;
export type TaskLatex = TaskBase<string>;
export type TaskJson = TaskBase<MathJson>;
export type TaskJson2 = TaskBase<MathJson2>;
export type Task = TaskSolver | TaskLatex | TaskJson | TaskJson2;

export type AlternativeSolver = AlternativeBase<string>;
export type AlternativeLatex = AlternativeBase<string>;
export type AlternativeJson = AlternativeBase<MathJson>;
export type AlternativeJson2 = AlternativeBase<MathJson2>;
export type Alternative =
  | AlternativeSolver
  | AlternativeLatex
  | AlternativeJson
  | AlternativeJson2;

export type MetadataSolver = MetadataBase<string>;
export type MetadataLatex = MetadataBase<string>;
export type MetadataJson = MetadataBase<MathJson>;
export type MetadataJson2 = MetadataBase<MathJson2>;

export type Metadata = MetadataSolver | MetadataLatex | MetadataJson | MetadataJson2;
