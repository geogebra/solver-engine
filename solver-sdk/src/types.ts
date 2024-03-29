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
  presets?: string[];
  settings?: { [name: string]: string };
  precision?: number;
  solutionVariable?: string;
  preferredStrategies?: { [category: string]: string };
};

export type Strategy = {
  strategy: string;
  description: string;
};

export type StrategyMap = {
  [category: string]: Strategy[];
};

export type Setting = {
  name: string;
  description: string;
  values: string[];
};

export type ApiMathFormat =
  | 'solver' // solver format - do not use unless you know why you are doing it
  | 'latex' // latex format - good for quick rendering, but lacks metadata
  | 'json2'; // object-based json format

export type API_VERSION_INFO_RESPONSE = {
  commit: string;
  deploymentName: string;
};

export type API_SETTINGS_RESPONSE = Setting[];

export type API_PRESETS_RESPONSE = {
  name: string;
  description: string;
  settings: { name: string; value: string }[];
}[];

export type API_PLANS_RESPONSE = PlanId[];

export type API_STRATEGIES_RESPONSE = StrategyMap;

export type GraphResponseBase<MathFormat> = {
  coordinateSystem: CoordinateSystem;
  objects: GraphObject<MathFormat>[];
};

export type GraphObject<MathFormat> =
  | Curve2DGraphObject<MathFormat>
  | IntersectionGraphObject;

export type Curve2DGraphObject<MathFormat> = {
  type: 'curve2D';
  label?: string;
  expression: MathFormat;
};

export type IntersectionGraphObject = {
  type: 'intersection';
  label?: string;
  objectLabels: string[];
  projectOntoHorizontalAxis?: boolean;
  projectOntoVerticalAxis?: boolean;
  showLabelWithCoordinates?: boolean;
};

export type CoordinateSystem = Cartesian2DSystem;

export type Cartesian2DSystem = {
  type: 'Cartesian2D';
  horizontalAxis: GraphAxis;
  verticalAxis: GraphAxis;
};

export type GraphAxis = {
  variable: string;
  label: string;
  minValue: number;
  maxValue: number;
};

export type GraphResponseSolver = GraphResponseBase<string>;
export type GraphResponseLatex = GraphResponseBase<string>;
export type GraphResponseJson = GraphResponseBase<MathJson>;
export type GraphResponse = GraphResponseSolver | GraphResponseLatex | GraphResponseJson;

/** The most common case where you would get a response like this would be if the math
 * input is not syntactically correct. */
// TODO: research to see if this type is always accurate.
export type ServerErrorResponse = {
  error: Exclude<string, ''>;
  message: string;
};

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
  priority: number;
};

type MappedExpressionBase<MathFormat> = {
  expression: MathFormat;
  pathMappings: PathMapping[];
};

type MetadataBase<MathFormat> = {
  key: string;
  params?: MappedExpressionBase<MathFormat>[];
};

export type Tag = 'Rearrangement' | 'Cosmetic' | 'Pedantic' | 'InvisibleChange';

type TransformationBase<MathFormat> = {
  type: 'Plan' | 'Rule' | 'TaskSet';
  tags?: Tag[];

  path: string;
  fromExpr: MathFormat;
  toExpr: MathFormat;
  pathMappings: PathMapping[];
  explanation: MetadataBase<MathFormat>;
  formula: MappedExpressionBase<MathFormat>;
  gmAction?: GmAction;
  skills?: MetadataBase<MathFormat>[];
  steps?: TransformationBase<MathFormat>[];
  tasks?: TaskBase<MathFormat>[];
  alternatives?: AlternativeBase<MathFormat>[];
};

export type TaskBase<MathFormat> = {
  taskId: string;
  startExpr: MathFormat;
  pathMappings: PathMapping[];
  explanation: MetadataBase<MathFormat>;
  steps?: TransformationBase<MathFormat>[];
  dependsOn?: string[];
};

export type AlternativeBase<MathFormat> = {
  strategy: string;
  explanation: MetadataBase<MathFormat>;
  steps: TransformationBase<MathFormat>[];
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

export type MappedExpressionSolver = MappedExpressionBase<string>;
export type MappedExpressionLatex = MappedExpressionBase<string>;
export type MappedExpressionJson = MappedExpressionBase<MathJson>;
export type MappedExpression =
  | MappedExpressionSolver
  | MappedExpressionLatex
  | MappedExpressionJson;
