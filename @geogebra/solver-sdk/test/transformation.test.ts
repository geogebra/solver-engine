import { describe, expect, it } from 'vitest';
import { isTrivialStep, isThroughStep } from '../src';
import {
  planSelectionWithTrivialMember,
  planSelectionWithThroughSteps,
} from './test-data/api-mock-data';

describe('isTrivialStep tests', () => {
  it('returns true when invoked with a trivial transformation', () => {
    const trivialTransformation =
      planSelectionWithTrivialMember[0].transformation.steps[0].steps[0];
    expect(isTrivialStep(trivialTransformation)).to.equal(true);
  });

  it('returns false when invoked with a non-trivial transformation', () => {
    const nonTrivialTransformation = planSelectionWithTrivialMember[1].transformation;
    expect(isTrivialStep(nonTrivialTransformation)).to.equal(false);
  });
});

describe('isThroughStep tests', () => {
  it('returns true when invoked with a through-step', () => {
    const throughStep = planSelectionWithThroughSteps[0].transformation.steps[0];
    expect(isThroughStep(throughStep)).to.equal(true);
  });

  it('returns false when invoked with a transformation that is not a through-step', () => {
    const transformation = planSelectionWithThroughSteps[0].transformation;
    expect(isThroughStep(transformation)).to.equal(false);
  });
});
