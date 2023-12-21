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
