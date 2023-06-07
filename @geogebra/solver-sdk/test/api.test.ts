import { describe, expect, it } from 'vitest';
import { api } from '../src/api';

// We need this for the API calls to work. They assume they have a browser
// with the 'fetch' function available.
import 'cross-fetch/polyfill';

describe.skip('APIUnitTests', () => {
  it('should return a version string', async () => {
    const versionInfo = await api.versionInfo();
    expect(versionInfo).to.have.property('commit');
    expect(versionInfo?.deploymentName).to.equal('release');
  });

  it('should return a list of planIds', async () => {
    const plans = await api.listPlans();
    expect(plans).to.be.an('array');
    expect(plans).to.have.lengthOf.above(0);
    expect(plans[0]).to.be.a('string');
  });

  it('should return a transformation for the default settings', async () => {
    const transformation = await api.applyPlan(
      'a+a',
      'Polynomials.SimplifyAlgebraicExpressionInOneVariable',
    );
    expect(transformation).to.have.property('toExpr');
    expect(transformation.toExpr).to.equal('2 a');
  });

  it('should return a transformation for a specific plan', async () => {
    const transformation = await api.applyPlan(
      '0.666',
      'Approximation.ApproximateExpression',
      'solver',
      { precision: 2 },
    );
    expect(transformation).to.have.property('toExpr');
    expect(transformation).to.have.property('fromExpr');
    expect(transformation.fromExpr).to.equal('0.666');
    expect(transformation.toExpr).to.equal('0.67');
  });

  it('should return json format', async () => {
    const transformation = await api.applyPlan(
      '2*3-(4)',
      'ConstantExpressions.SimplifyConstantExpression',
      'json',
    );
    expect(transformation).to.have.property('fromExpr');
    expect(transformation.fromExpr).to.be.an('array');
    expect(transformation).to.have.property('toExpr');
    expect(transformation.toExpr).to.deep.equal(['2']);
  });

  it('should return a list of transformations for default options', async () => {
    const res = await api.selectPlans('1+2');
    expect(res).to.be.an('array');
    expect(res).to.have.lengthOf.above(0);
    expect(res[0]).to.have.property('transformation');
    expect(res[0]).to.have.property('metadata');
  });

  it('should return a list of transformations for specific options', async () => {
    const res = await api.selectPlans('1+2', 'latex', { precision: 0 });
    expect(res).to.be.an('array');
    expect(res).to.have.lengthOf.above(0);
  });
});
