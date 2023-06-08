import { MathJson, MathJson2 } from './types';
import { LatexSettings, LatexTransformer, treeToLatex, treeToSolver } from './renderer';
import { jsonToTree, latexToTree } from './parser';

export * from './renderer';
export * from './api';
export * from './parser';
export * from './paths';
export * from './types';
export * from './solutions';

export function jsonToLatex(
  json: MathJson | MathJson2,
  settings?: LatexSettings,
  transformer?: LatexTransformer,
): string {
  return treeToLatex(jsonToTree(json), settings, transformer);
}

export function latexToSolver(latex: string): string {
  return treeToSolver(latexToTree(latex));
}
