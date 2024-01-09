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

import type { PlanSelection, Tag, Transformation } from '../types';
import { isTrivialStep } from './transformation';

function containsNonTrivialStep(
  transformation: Transformation,
  trivialTags?: Tag[],
): boolean {
  if (!transformation.steps || !transformation.steps.length) {
    return !isTrivialStep(transformation, trivialTags);
  }

  return transformation.steps.some((step) => containsNonTrivialStep(step, trivialTags));
}

/**
 * Evaluates whether a solution is composed of only single steps with a final step that
 * is tagged trivial.
 *
 * @param solution
 * @returns A boolean indicating whether the solution is trivial or not.
 */
export function isTrivialSolution(solution: PlanSelection, trivialTags?: Tag[]): boolean {
  return !containsNonTrivialStep(solution.transformation, trivialTags);
}
