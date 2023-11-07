import type { ExpressionTree, LatexTransformer } from '@geogebra/solver-sdk';
import { jsonToTree, MathJson, treeToLatex as sdkTreeToLatex } from '@geogebra/solver-sdk';
import { latexSettings } from './settings.ts';
import { mathWords } from './translations.ts';

/**
 * Renders an expression to LaTeX, using the global latex settings and math words.  Use this
 * instead of the SDK function in poker.
 * @param expr expression to render
 * @param transformer optional transformer (usually coloring transformer)
 */
export const treeToLatex = (expr: ExpressionTree, transformer?: LatexTransformer) =>
  sdkTreeToLatex(expr, latexSettings.value, transformer, mathWords.value);

/**
 * Convenience function that compiles the MathJSON expression and renders it to LaTeX.  Use this
 * instead of the SDK function in poker as it applies the global settings and math words.
 * @param json MathJSON representation of the expression
 * @param transformer optional transformer
 */
export const jsonToLatex = (json: MathJson, transformer?: LatexTransformer) =>
  treeToLatex(jsonToTree(json), transformer);
