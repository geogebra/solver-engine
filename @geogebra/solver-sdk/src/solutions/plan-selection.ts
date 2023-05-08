import type { PlanSelection, Transformation } from '../types';
import { isTrivialStep } from './transformation';

function containsNonTrivialStep(transformation: Transformation): boolean {
  if (!transformation.steps || !transformation.steps.length) {
    return !isTrivialStep(transformation);
  }

  return transformation.steps.some(containsNonTrivialStep);
}

/**
 * Evaluates whether a solution is composed of only single steps with a final step that
 * is tagged trivial.
 *
 * @param solution
 * @returns A boolean indicating whether the solution is trivial or not.
 */
export function isTrivialSolution(solution: PlanSelection): boolean {
  return !containsNonTrivialStep(solution.transformation);
}
