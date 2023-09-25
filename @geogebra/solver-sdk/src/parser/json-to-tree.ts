import { MathJson } from '../types';
import { ExpressionTree } from './types';

export function jsonToTree(json: MathJson, path = '.'): ExpressionTree {
  if ('operands' in json) {
    return {
      ...json,
      operands: json.operands
        ? json.operands.map((op, i) => jsonToTree(op, `${path}/${i}`))
        : [],
      path,
    };
  } else {
    return { ...json, path };
  }
}

export function treeToJson(tree: ExpressionTree): MathJson {
  if ('operands' in tree) {
    const { path: _, ...json } = tree;
    return {
      ...json,
      operands: json.operands.map((op) => treeToJson(op)),
    };
  } else {
    const { path: _, ...json } = tree;
    return json;
  }
}
