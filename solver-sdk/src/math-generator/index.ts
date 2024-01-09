import { PlanId } from '../types';
import {
  generateDistributionExpression,
  generateDivideMonomialExpression,
} from './algebraic-expressions';
import {
  generateOneStepEquation,
  generateTwoStepEquation,
  generateZeroOrManySolutionEquation,
  generateMultiStepEquation,
} from './linear-equations';
import {
  generateFourOperationsExpression,
  generateFractionExpression,
  generatePowerExpression,
} from './order-of-operations';
import {
  generateLinearCombinationOfPolynomials,
  generateMonomialDivision,
  generateSumOfPolynomials,
  generateUnnormalizedPolynomial,
} from './polynomials';

export type TopicKey =
  | 'linEq.oneStep'
  | 'linEq.twoStep'
  | 'linEq.multiStep'
  | 'linEq.zeroOrManySolutions'
  | 'orderOfOperations.basic'
  | 'orderOfOperations.fractions'
  | 'orderOfOperations.powers'
  | 'algebraicExpressions.distribution'
  | 'algebraicExpressions.divideMonomials'
  | 'polynomials.normalize'
  | 'polynomials.add'
  | 'polynomials.addScaled'
  | 'polynomials.divideByMonomial';

export type TaskForTopic = {
  math: string;
  solverPlanId?: PlanId;
};

export function generateExpressionForTopic(
  topic: TopicKey,
  complexity: 0 | 1 | 2 | 3 = 0,
): TaskForTopic {
  switch (topic) {
    case 'linEq.oneStep':
      return { math: generateOneStepEquation(complexity) };
    case 'linEq.twoStep':
      return { math: generateTwoStepEquation(complexity) };
    case 'linEq.multiStep':
      return { math: generateMultiStepEquation(complexity) };
    case 'linEq.zeroOrManySolutions':
      return { math: generateZeroOrManySolutionEquation(complexity) };
    case 'orderOfOperations.basic':
      return { math: generateFourOperationsExpression(complexity) };
    case 'orderOfOperations.fractions':
      return { math: generateFractionExpression(complexity) };
    case 'orderOfOperations.powers':
      return { math: generatePowerExpression(complexity) };
    case 'algebraicExpressions.distribution':
      return {
        math: generateDistributionExpression(complexity),
        solverPlanId: 'Polynomials.ExpandPolynomialExpression',
      };
    case 'algebraicExpressions.divideMonomials':
      return {
        math: generateDivideMonomialExpression(complexity),
        solverPlanId: 'Polynomials.ExpandPolynomialExpression',
      };
    case 'polynomials.normalize':
      return {
        math: generateUnnormalizedPolynomial(complexity),
        solverPlanId: 'Polynomials.ExpandPolynomialExpression',
      };
    case 'polynomials.add':
      return {
        math: generateSumOfPolynomials(complexity),
        solverPlanId: 'Polynomials.ExpandPolynomialExpression',
      };
    case 'polynomials.addScaled':
      return {
        math: generateLinearCombinationOfPolynomials(complexity),
        solverPlanId: 'Polynomials.ExpandPolynomialExpression',
      };
    case 'polynomials.divideByMonomial':
      return {
        math: generateMonomialDivision(complexity),
        solverPlanId: 'Polynomials.ExpandPolynomialExpression',
      };
  }
}
