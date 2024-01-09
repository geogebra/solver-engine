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

import { substituteTree, pathToArray, jsonToTree } from '../';
import type { ExpressionTree, TransformationJson } from '../';

/** Returns all leaf-level steps, which are the ones that a user of GM would
 * be able to directly apply. */
export function getInnerSteps<T extends TransformationWithJustTheStepsParameter>(
  transformation: T,
): T[] {
  return transformation.steps?.length
    ? (transformation.steps as T[]).flatMap(getInnerSteps)
    : ([transformation] as T[]);
}

type TransformationWithJustTheStepsParameter = {
  steps?: TransformationWithJustTheStepsParameter[] | null;
};

export type TransformationWithFullFromExpr = {
  fullFromExpr: ExpressionTree;
  fullToExpr: ExpressionTree;
  depth: number;
  steps?: TransformationWithFullFromExpr[];
} & Omit<TransformationJson, 'steps'>;

/** Augments a solver response. In particular, it will substitute the sub-step expressions
 * into the parent expression so that we have access to the full expression at each step.
 */
export function addFullFromExprToTransformation(transformation: TransformationJson) {
  lastStep = undefined;
  const ret = helper(clone(transformation));
  lastStep!.fullToExpr = jsonToTree(transformation.toExpr);
  lastStep = undefined;
  return ret;
}

let lastStep: TransformationWithFullFromExpr | undefined;

function helper(
  transformation: TransformationJson,
  depth = 0,
  parentTree?: ExpressionTree,
): TransformationWithFullFromExpr {
  const ret = transformation as TransformationWithFullFromExpr; // This cast is a temporary lie.
  const fullFromExprTree = substitute(
    jsonToTree(transformation.fromExpr),
    transformation.path,
    parentTree,
  );
  if (lastStep) lastStep.fullToExpr = fullFromExprTree;
  lastStep = ret;
  const steps =
    transformation.steps &&
    transformation.steps.map((solverStep) =>
      helper(solverStep, depth + 1, fullFromExprTree),
    );
  ret.depth = depth;
  ret.fullFromExpr = fullFromExprTree;
  ret.steps = steps;
  return ret;
}

export function substitute(
  childTree: ExpressionTree,
  substitutePath: string,
  parentTree?: ExpressionTree,
): ExpressionTree {
  if (!parentTree) return childTree;
  return substituteTree(parentTree, childTree, pathToArray(substitutePath));
}

// If we end up using this more, then we can change to a library that has a faster cloning
// solution (the clone in jsondiffpatch), but for now it is probably faster to not have an
// extra dependency just for this.
const clone = (obj: ParsedJson) => JSON.parse(JSON.stringify(obj));

type ParsedJson =
  | string
  | number
  | boolean
  | null
  | ParsedJson[]
  | { [key: string]: ParsedJson };
