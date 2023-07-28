import { MathJson, MathJson2, SmartProductOperandJson } from '../types';
import {
  DecoratorType,
  ExpressionDecorations,
  ExpressionTree,
  isDecorator,
  NestedExpression,
  NestedExpressionType,
} from './types';

export function jsonToTree(json: MathJson | MathJson2, path = '.'): ExpressionTree {
  if (!Array.isArray(json)) {
    return json2ToTree(json, path);
  }
  const [head, ...args] = json;
  let value: string;
  let decorators: (DecoratorType | string)[] = [];
  if (typeof head === 'string') value = head;
  else [value, ...decorators] = head as [string, DecoratorType];
  let result: ExpressionTree;

  if (args.length === 0 && value !== 'FiniteSet') {
    const str = value as string;
    if (str.match(/^[+\-0-9]/)) {
      result = { type: 'Number', value: str, path };
    } else if (str.match(/^".*"$/)) {
      result = { type: 'Name', value: str.substring(1, str.length - 1), path };
    } else if (str === '/undefined/') {
      result = { type: '/undefined/', path };
    } else if (str === '/infinity/') {
      result = { type: '/infinity/', path };
    } else if (str === '/reals/' || str === 'Reals') {
      result = { type: 'Reals', path };
    } else if (str.includes('_')) {
      const [value, subscript] = str.split('_', 2);
      result = { type: 'Variable', value, subscript, path };
    } else {
      result = { type: 'Variable', value: str, path };
    }
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

function json2ToTree(json: MathJson2, path = '.'): ExpressionTree {
  const decorations = { name: json.name, decorators: json.decorators };
  switch (json.type) {
    case 'Integer':
    case 'Decimal':
    case 'RecurringDecimal':
      return { ...decorations, type: 'Number', value: json.value, path };
    case 'Variable':
      return {
        ...decorations,
        type: 'Variable',
        value: json.value,
        subscript: json.subscript,
        path,
      };
    case 'Name':
      return { ...decorations, type: 'Name', value: json.value, path };
    case 'SmartProduct':
      return {
        ...decorations,
        type: 'SmartProduct',
        args: json.operands.map((op, i) => json2ToTree(op, `${path}/${i}`)),
        signs: json.signs,
        path,
      };
    default:
      return {
        ...decorations,
        type: json.type as NestedExpressionType,
        args: json.operands
          ? json.operands.map((op, i) => json2ToTree(op, `${path}/${i}`))
          : [],
        path,
      };
  }
}

export function treeToJson(tree: ExpressionTree): MathJson {
  const decorators = extractJsonDecorators(tree);
  if (
    tree.type === '/undefined/' ||
    tree.type === '/infinity/' ||
    tree.type === 'Reals'
  ) {
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
