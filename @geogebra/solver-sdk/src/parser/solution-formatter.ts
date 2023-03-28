import { ExpressionTree } from './types';

export interface SolutionFormatter {
  formatSolution(
    n: ExpressionTree,
    rec: (n: ExpressionTree, p: ExpressionTree | null) => string,
  ): string;
}

type VariableListTree = ExpressionTree & { type: 'VariableList' };
type TupleTree = ExpressionTree & { type: 'Tuple' };

/**
 * This formatter always uses set notation to describe solutions.
 */
export const setsSolutionFormatter = {
  formatSolution(
    n: ExpressionTree,
    rec: (n: ExpressionTree, p: ExpressionTree | null) => string,
  ): string {
    switch (n.type) {
      case 'Solution':
        return `${rec(n.args[0], n)} \\in ${rec(n.args[1], n)}`;
      case 'SetSolution': {
        const [varTuple, _] = variableListToLatexTuple(n.args[0], rec);
        return `${varTuple} \\in ${rec(n.args[1], n)}`;
      }
      case 'Identity': {
        const [varTuple, size] = variableListToLatexTuple(n.args[0], rec);
        if (size === 1) {
          return `${varTuple} \\in \\mathbb{R}`;
        } else {
          return `${varTuple} \\in \\mathbb{R}^${size}`;
        }
      }
      case 'Contradiction':
        return `${rec(n.args[0], n)} \\in \\emptyset`;
      case 'ImplicitSolution': {
        const [varTuple, size] = variableListToLatexTuple(n.args[0], rec);
        return `${varTuple} \\in \\mathbb{R}^${size} : ${rec(n.args[1], n)}`;
      }
    }
    return '???';
  },
};

/**
 * This formatter tries to write solutions simply if possible, but reverts to set notation
 * when the solution is too complex.
 * E.g. x = 1         for Solution[x, {1}]
 *      x = 2, y = 1  for SolutionSet[x, y: {(2, 1)}
 *      x = 1, x = 2 for Solution[x, {1, 2}]
 */
export const simpleSolutionFormatter = {
  formatSolution(
    n: ExpressionTree,
    rec: (n: ExpressionTree, p: ExpressionTree | null) => string,
  ): string {
    switch (n.type) {
      case 'Solution': {
        const [v, set] = n.args;
        const vs = rec(v, null);
        switch (set.type) {
          case 'FiniteSet':
            switch (set.args.length) {
              case 0:
                return `${vs} \\in \\emptyset}`;
              case 1:
                return `${vs} = ${rec(set.args[0], null)}`;
              default:
                return set.args.map((el) => `${vs} = ${rec(el, null)}`).join(', ');
            }
        }
        break;
      }
      case 'SetSolution': {
        const [vars, set] = n.args;
        switch (set.type) {
          case 'FiniteSet':
            switch (set.args.length) {
              case 0: {
                const [varsTuple, _] = variableListToLatexTuple(vars, rec);
                return `${varsTuple} \\in \\emptyset`;
              }
              case 1: {
                const varList = vars as VariableListTree;
                const tuple = set.args[0] as TupleTree;
                return varList.args
                  .map((v, i) => `${rec(v, null)} = ${rec(tuple.args[i], null)}`)
                  .join(', ');
              }
              default: {
                const [varsTuple, _] = variableListToLatexTuple(vars, rec);
                return set.args.map((el) => `${varsTuple} = ${rec(el, null)}`).join(', ');
              }
            }
          case 'CartesianProduct':
            if (
              set.args.every(
                (s) =>
                  s.type === 'Reals' || (s.type === 'FiniteSet' && s.args.length === 1),
              )
            ) {
              return set.args
                .map((s, i) => {
                  const v = rec((vars as VariableListTree).args[i], null);
                  switch (s.type) {
                    case 'Reals':
                      return `${v} \\in \\mathbb{R}`;
                    case 'FiniteSet':
                      return `${v} = ${rec(s.args[0], null)}`;
                  }
                })
                .join(', ');
            }
        }
        break;
      }
      case 'ImplicitSolution':
        return `${rec(n.args[0], null)} \\in \\mathbb{R} : ${rec(n.args[1], n)}`;
      case 'Identity':
        return `${rec(n.args[0], null)} \\in \\mathbb{R}`;
    }
    return setsSolutionFormatter.formatSolution(n, rec);
  },
};

function variableListToLatexTuple(
  n: ExpressionTree,
  rec: (n: ExpressionTree, p: ExpressionTree | null) => string,
): [string, number] {
  const vars = (n as VariableListTree).args;
  switch (vars.length) {
    case 0:
      return ['()', 0];
    case 1:
      return [rec(vars[0], null), 1];
    default:
      return [`\\left(${vars.map((x) => rec(x, null)).join(', ')}\\right)`, vars.length];
  }
}
