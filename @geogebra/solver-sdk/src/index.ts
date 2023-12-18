import { MathJson } from './types';
import {
  LatexSettings,
  LatexTransformer,
  MathWords,
  treeToLatex,
  treeToSolver,
} from './renderer';
import { jsonToTree, latexToTree } from './parser';

export * from './renderer';
export * from './api';
export * from './parser';
export * from './paths';
export * from './types';
export * from './solutions';
export * from './translations';
export * from './math-generator';

export function jsonToLatex(
  json: MathJson,
  settings?: LatexSettings,
  transformer?: LatexTransformer,
  mathWords?: MathWords,
): string {
  return treeToLatex(jsonToTree(json), settings, transformer, mathWords);
}

export function latexToSolver(latex: string): string {
  return treeToSolver(latexToTree(latex));
}
