import { describe, it } from 'mocha';
import { expect } from 'chai';
import { isTrivialSolution } from '../src';
import { planSelectionWithTrivialMember } from './test-data/api-mock-data';

describe('isTrivialSolution tests', () => {
  it('returns true when invoked with a trivial solution', () => {
    const trivialSolution = planSelectionWithTrivialMember[0];
    expect(isTrivialSolution(trivialSolution)).to.equal(true);
  });

  it('returns false when invoked with a non-trivial solution', () => {
    const nonTrivialSolution = planSelectionWithTrivialMember[1];
    expect(isTrivialSolution(nonTrivialSolution)).to.equal(false);
  });
});
