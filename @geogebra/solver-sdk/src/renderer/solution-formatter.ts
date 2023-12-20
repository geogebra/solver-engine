import { ExpressionTree } from '../parser';
import { MathWords } from './tree-to-latex';

export interface SolutionFormatter {
  formatSolution(
    n: ExpressionTree,
    rec: (n: ExpressionTree, p: ExpressionTree | null) => string,
    w: MathWords,
  ): string;
}

type VariableListTree = ExpressionTree & {
  type: 'VariableList';
};
type TupleTree = ExpressionTree & {
  type: 'Tuple';
};

/**
 * This formatter always uses set notation to describe solutions.
 */
export const setsSolutionFormatter: SolutionFormatter = {
  formatSolution(
    n: ExpressionTree,
    rec: (n: ExpressionTree, p: ExpressionTree | null) => string,
    w: MathWords,
  ): string {
    switch (n.type) {
      case 'Solution':
        return `${rec(n.operands[0], n)} \\in ${rec(n.operands[1], n)}`;
      case 'SetSolution': {
        const [varTuple, _] = variableListToLatexTuple(n.operands[0], rec);
        return `${varTuple} \\in ${rec(n.operands[1], n)}`;
      }
      case 'Identity': {
        const [varTuple, size] = variableListToLatexTuple(n.operands[0], rec);
        if (size === 0) {
          return `\\text{${w.True}}`;
        } else if (size === 1) {
          return `${varTuple} \\in \\mathbb{R}`;
        } else {
          return `${varTuple} \\in \\mathbb{R}^${size}`;
        }
      }
      case 'Contradiction': {
        const [varTuple, size] = variableListToLatexTuple(n.operands[0], rec);
        if (size === 0) {
          return `\\text{${w.False}}`;
        } else {
          return `${varTuple} \\in \\emptyset`;
        }
      }
      case 'ImplicitSolution': {
        const [varTuple, size] = variableListToLatexTuple(n.operands[0], rec);
        return `${varTuple} \\in \\mathbb{R}^${size} : ${rec(n.operands[1], n)}`;
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
export const simpleSolutionFormatter: SolutionFormatter = {
  formatSolution(
    n: ExpressionTree,
    rec: (n: ExpressionTree, p: ExpressionTree | null) => string,
    w: MathWords,
  ): string {
    switch (n.type) {
      case 'SetSolution': {
        const [vars, set] = n.operands;
        const varList = vars as VariableListTree;
        const [varsTuple, variableCount] = variableListToLatexTuple(vars, rec);

        switch (set.type) {
          case 'FiniteSet': {
            const operandsLength = set.operands ? set.operands.length : 0;
            switch (operandsLength) {
              case 0:
                return `\\text{${w.NoSolution}}`;
              case 1: {
                if (variableCount > 1) {
                  const tuple = set.operands[0] as TupleTree;
                  return varList.operands
                    .map((v, i) => `${rec(v, null)} = ${rec(tuple.operands[i], null)}`)
                    .join(', ');
                } else {
                  return `${rec(varList.operands[0], null)} = ${rec(
                    set.operands[0],
                    null,
                  )}`;
                }
              }
              default:
                return set.operands
                  .map((el) => `${varsTuple} = ${rec(el, null)}`)
                  .join(', ');
            }
          }
          case 'CartesianProduct':
            if (
              set.operands.every(
                (s) =>
                  s.type === 'Reals' ||
                  (s.type === 'FiniteSet' && s.operands.length === 1),
              )
            ) {
              return set.operands
                .map((s, i) => {
                  const v = rec((vars as VariableListTree).operands[i], null);
                  switch (s.type) {
                    case 'Reals':
                      return `${v} \\in \\mathbb{R}`;
                    case 'FiniteSet':
                      return `${v} = ${rec(s.operands[0], null)}`;
                  }
                })
                .join(', ');
            }
            break;
          case 'SetDifference':
            if (
              variableCount === 1 &&
              set.operands[0].type === 'Reals' &&
              set.operands[1].type === 'FiniteSet'
            ) {
              return set.operands[1].operands
                .map((s) => `${varsTuple} \\neq ${rec(s, null)}`)
                .join(` \\text{ ${w.And} } `);
            }
        }
        break;
      }
      case 'ImplicitSolution':
        return `${rec(n.operands[0], null)} \\in \\mathbb{R} : ${rec(n.operands[1], n)}`;
      case 'Identity': {
        const varList = n.operands[0] as VariableListTree;
        if (varList.operands !== undefined) {
          switch (varList.operands.length) {
            case 0:
              break;
            case 1:
              return `\\text{${w.InfinitelyManySolutions}}`;
            default:
              return `${rec(varList, null)} \\in \\mathbb{R}`;
          }
        }
        break;
      }
      case 'Contradiction': {
        return `\\text{${w.NoSolution}}`;
      }
    }

    return setsSolutionFormatter.formatSolution(n, rec, w);
  },
};

function variableListToLatexTuple(
  n: ExpressionTree,
  rec: (n: ExpressionTree, p: ExpressionTree | null) => string,
): [string, number] {
  const vars = (n as VariableListTree).operands;
  switch (vars ? vars.length : 0) {
    case 0:
      return ['()', 0];
    case 1:
      return [rec(vars[0], null), 1];
    default:
      return [`\\left(${vars.map((x) => rec(x, null)).join(', ')}\\right)`, vars.length];
  }
}
