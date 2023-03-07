import type { LatexSettings } from './tree-to-latex';
import type { MathJson } from '../types';
import { jsonToTree } from './json-to-tree';
import { treeToLatex } from './tree-to-latex';
import { latexToTree } from './latex-to-tree';
import { treeToSolver } from './tree-to-solver';

export * from './types';
export { jsonToTree, treeToJson } from './json-to-tree';
export { treeToLatex } from './tree-to-latex';
export { coloringTransformer, createColorMaps } from './coloring';
export { latexToTree } from './latex-to-tree';
export { treeToSolver } from './tree-to-solver';

export function jsonToLatex(json: MathJson, settings?: LatexSettings): string {
  return treeToLatex(jsonToTree(json), settings);
}
export function latexToSolver(latex: string): string {
  return treeToSolver(latexToTree(latex));
}
