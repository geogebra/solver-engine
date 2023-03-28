import { MathJson } from '../types';
import {
  DecoratorType,
  ExpressionTree,
  NestedExpression,
  NestedExpressionType,
} from './types';

export function jsonToTree(json: MathJson, path = '.'): ExpressionTree {
  const [head, ...args] = json;
  let value: string;
  let decorators: DecoratorType[] = [];
  if (typeof head === 'string') value = head;
  else [value, ...decorators] = head;
  let result: ExpressionTree;

  if (args.length === 0 && value !== 'FiniteSet') {
    const str = value as string;
    if (str.match(/^[+\-0-9]/)) result = { type: 'Number', value: str, path };
    else if (str === 'UNDEFINED') result = { type: 'UNDEFINED', path };
    else if (str === 'INFINITY') result = { type: 'INFINITY', path };
    else if (str === 'Reals') result = { type: 'Reals', path };
    else result = { type: 'Variable', value: str, path };
  } else {
    // nested expression
    result = {
      type: value as NestedExpressionType,
      args: args.map((arg: MathJson, i: number) => jsonToTree(arg, `${path}/${i}`)),
      path,
    };
  }
  if (decorators.length) result.decorators = decorators;
  return result;
}

export function treeToJson(tree: ExpressionTree): MathJson {
  if (tree.type === 'UNDEFINED' || tree.type === 'INFINITY' || tree.type === 'Reals') {
    if (tree.decorators?.length) return [[tree.type, ...tree.decorators]];
    else return [tree.type];
  }
  if (tree.type === 'Number' || tree.type === 'Variable') {
    if (tree.decorators?.length) return [[tree.value, ...tree.decorators]];
    else return [tree.value];
  }
  return [
    tree.decorators ? [tree.type, ...tree.decorators] : tree.type,
    ...(tree as NestedExpression).args.map((op) => treeToJson(op)),
  ] as MathJson;
}
