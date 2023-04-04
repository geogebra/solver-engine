import { Transformation } from '@geogebra/solver-sdk';

export const isTrivialStep = (transformation: Transformation) => {
  // currently every tagged step is considered trivial -- subject to change
  return !!transformation.tags;
};

export const containsNonTrivialStep = (transformation: Transformation) => {
  if (!transformation.steps || !transformation.steps.length) {
    return !isTrivialStep(transformation);
  }

  return transformation.steps.some(containsNonTrivialStep);
};

export const isThroughStep = (trans: Transformation) =>
  !!trans.steps && trans.steps.length === 1 && trans.steps[0].path === trans.path;
