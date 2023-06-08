import {
  API_APPLY_PLAN_RESPONSE,
  API_PLANS_RESPONSE,
  API_SELECT_PLANS_RESPONSE,
  API_VERSION_INFO_RESPONSE,
  ApiMathFormat,
  PlanId,
  SolverContext,
  SolverExpr,
} from './types';

/**
 * This is the URL the SDK uses to connect to the Solver API. The CI script will
 * automatically adjust it via the `set-base-url.mjs` script to point to the
 * correct version of the API during deployment.
 */
const BASE_URL = 'https://solver-api.geogebra.org/api/v1';

const DEFAULT_HEADERS = {
  headers: { 'Content-Type': 'application/json' },
};
const DEFAULT_CONTEXT = {
  curriculum: '',
  precision: 3,
};

/// Live API Documentation: http://solver.geogebra.net/main/swagger-ui/index.html#/
class Api {
  baseUrl: string;
  defaultHeaders: { headers?: Record<string, string> };
  defaultContext: SolverContext;

  constructor(
    baseUrl = BASE_URL,
    defaultHeaders = DEFAULT_HEADERS,
    defaultContext = DEFAULT_CONTEXT,
  ) {
    this.baseUrl = baseUrl;
    this.defaultHeaders = defaultHeaders;
    this.defaultContext = defaultContext;
  }

  /** Get a transformation for each applicable plan. */
  async selectPlans(
    input: SolverExpr,
    format: ApiMathFormat = 'latex',
    context: SolverContext = this.defaultContext,
  ): Promise<API_SELECT_PLANS_RESPONSE> {
    return fetch(`${this.baseUrl}/selectPlans`, {
      ...this.defaultHeaders,
      method: 'POST',
      body: JSON.stringify({ input, format, context }),
    }).then((res) => res.json());
  }

  /* Apply plan to input and return the transformation. */
  async applyPlan(
    input: SolverExpr,
    planId: PlanId,
    format: ApiMathFormat = 'latex',
    context: SolverContext = this.defaultContext,
  ): Promise<API_APPLY_PLAN_RESPONSE> {
    return fetch(`${this.baseUrl}/plans/${planId}/apply`, {
      ...this.defaultHeaders,
      method: 'POST',
      body: JSON.stringify({ input, format, context }),
    }).then((res) => res.json());
  }

  /** Get a list of all available plans. */
  async listPlans(): Promise<API_PLANS_RESPONSE> {
    return fetch(`${this.baseUrl}/plans`, this.defaultHeaders).then((res) => res.json());
  }

  /** Get solver version info. */
  async versionInfo(): Promise<API_VERSION_INFO_RESPONSE> {
    return fetch(`${this.baseUrl}/versionInfo`, this.defaultHeaders).then((res) =>
      res.json(),
    );
  }
}

export const api = new Api();
