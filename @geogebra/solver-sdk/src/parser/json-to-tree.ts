import { MathJson, SmartProductOperandJson } from '../types';
import {
  DecoratorType,
  ExpressionDecorations,
  ExpressionTree,
  isDecorator,
  NestedExpression,
  NestedExpressionType,
} from './types';

export function jsonToTree(json: MathJson, path = '.'): ExpressionTree {
  const [head, ...args] = json;
  let value: string;
  let decorators: (DecoratorType | string)[] = [];
  if (typeof head === 'string') value = head;
  else [value, ...decorators] = head as [string, DecoratorType];
  let result: ExpressionTree;

  if (args.length === 0 && value !== 'FiniteSet') {
    const str = value as string;
    if (str.match(/^[+\-0-9]/)) result = { type: 'Number', value: str, path };
    else if (str.match(/^".*"$/))
      result = { type: 'Name', value: str.substring(1, str.length - 1), path };
    else if (str === 'UNDEFINED') result = { type: 'UNDEFINED', path };
    else if (str === 'INFINITY') result = { type: 'INFINITY', path };
    else if (str === 'Reals') result = { type: 'Reals', path };
    else result = { type: 'Variable', value: str, path };
  } else if (value === 'SmartProduct') {
    result = {
      type: 'SmartProduct',
      args: (args as SmartProductOperandJson[]).map(([hasSign, arg], i) =>
        jsonToTree(arg, `${path}/${i}`),
      ),
      signs: (args as SmartProductOperandJson[]).map(([hasSign, arg]) => hasSign),
      path,
    };
  } else {
    // nested expression
    result = {
      type: value as NestedExpressionType,
      args: (args as MathJson[]).map((arg, i) => jsonToTree(arg, `${path}/${i}`)),
      path,
    };
  }
  if (decorators.length) {
    Object.assign(result, extractDecorations(decorators));
  }
  return result;
}

export function treeToJson(tree: ExpressionTree): MathJson {
  const decorators = extractJsonDecorators(tree);
  if (tree.type === 'UNDEFINED' || tree.type === 'INFINITY' || tree.type === 'Reals') {
    if (decorators?.length) return [[tree.type, ...decorators]];
    else return [tree.type];
  }
  if (tree.type === 'Number' || tree.type === 'Variable') {
    if (decorators?.length) return [[tree.value, ...decorators]];
    else return [tree.value];
  }
  return [
    tree.decorators ? [tree.type, ...tree.decorators] : tree.type,
    ...(tree as NestedExpression).args.map((op) => treeToJson(op)),
  ] as MathJson;
}

function extractDecorations(decorators: string[]): ExpressionDecorations {
  if (decorators.length === 0) {
    return {};
  }
  if (!isDecorator(decorators[0])) {
    const [name, ...brackets] = decorators;
    return {
      name,
      decorators: brackets as DecoratorType[],
    };
  }
  return { decorators: decorators as DecoratorType[] };
}

function extractJsonDecorators({ name, decorators }: ExpressionDecorations): string[] {
  return (name ? [name] : []).concat(decorators || []);
}
