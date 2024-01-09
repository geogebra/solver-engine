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

import type { Tag, Transformation } from '../types';

const defaultTrivialTags: Tag[] = ['Cosmetic', 'Pedantic', 'InvisibleChange'];

/**
 * Evaluates whether a step (a single transformation) is tagged with a trivial tag.
 *
 * @param transformation
 * @returns A boolean indicating whether the provided transformation is trivial or not.
 */
export function isTrivialStep(
  transformation: Transformation,
  trivialTags = defaultTrivialTags,
): boolean {
  if (transformation.tags) {
    for (const tag of transformation.tags) {
      if (trivialTags.includes(tag)) return true;
    }
  }
  return false;
}

/**
 * Evaluates whether a step (a single transformation) fits the definition of a through-step.
 *
 * @param transformation
 * @returns A boolean indicating whether the provided transformation is a through-step or not.
 */
export function isThroughStep(transformation: Transformation): boolean {
  return (
    !!transformation.steps &&
    transformation.steps.length === 1 &&
    transformation.steps[0].path === transformation.path
  );
}
