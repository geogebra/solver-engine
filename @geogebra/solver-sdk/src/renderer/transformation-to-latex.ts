import { ExpressionTree, LatexSettings } from '../parser';
import { treeToLatex } from '../index';

/**
 * Render a transformation in one special latex expression if it is possible
 *
 * @param fromExpr
 * @param toExpr
 * @param settings
 */
export function specialTransformationLatex(
  fromExpr: ExpressionTree,
  toExpr: ExpressionTree,
  settings: LatexSettings,
): string | null {
  if (fromExpr.type === 'AddEquations' || fromExpr.type === 'SubtractEquations') {
    // This is the case when we add / subtract equations.  It can be rendered in column format
    // E.g.
    //   x + y = 2 | +
    //   x - y = 3 |
    //   ----------+--
    //      2x = 5
    const [eq1, eq2] = fromExpr.args;
    const alignSetting = { ...settings, align: true };
    const operator = fromExpr.type === 'AddEquations' ? '+' : '-';
    return (
      '\\begin{array}{rcl|l}\n' +
      '  ' +
      treeToLatex(eq1, alignSetting) +
      ` & ${operator} \\\\\n` +
      '  ' +
      treeToLatex(eq2, alignSetting) +
      ' & \\\\ \\hline \n' +
      '  ' +
      treeToLatex(toExpr, alignSetting) +
      ' \\\\\n' +
      '\\end{array}'
    );
  } else {
    return null;
  }
}
