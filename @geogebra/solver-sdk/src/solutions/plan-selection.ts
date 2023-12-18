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
