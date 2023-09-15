import { MathJson } from '../types';
import {
  ExpressionDecorations,
  ExpressionTree,
  NestedExpressionType,
  VariableExpression,
} from './types';

export function jsonToTree(json: MathJson, path = '.'): ExpressionTree {
  const decorations = { name: json.name, decorators: json.decorators };
  switch (json.type) {
    case 'Undefined':
    case 'Infinity':
    case 'Reals':
    case 'Void':
      return { ...decorations, type: json.type, path };
    case 'Integer':
    case 'Decimal':
    case 'RecurringDecimal':
    case 'Name':
      return { ...decorations, type: json.type, value: json.value, path };
    case 'Variable':
      return {
        ...decorations,
        type: 'Variable',
        value: json.value,
        subscript: json.subscript,
        path,
      };
    case 'SmartProduct':
      return {
        ...decorations,
        type: 'SmartProduct',
        args: json.operands.map((op, i) => jsonToTree(op, `${path}/${i}`)),
        signs: json.signs,
        path,
      };
    default:
      return {
        ...decorations,
        type: json.type as NestedExpressionType,
        args: json.operands
          ? json.operands.map((op, i) => jsonToTree(op, `${path}/${i}`))
          : [],
        path,
      };
  }
}

export function treeToJson(tree: ExpressionTree): MathJson {
  const decorations: ExpressionDecorations = {};
  if (tree.name) {
    decorations['name'] = tree.name;
  }
  if (tree.decorators) {
    decorations['decorators'] = tree.decorators;
  }
  switch (tree.type) {
    case 'Undefined':
    case 'Infinity':
    case 'Reals':
    case 'Void':
      return { ...decorations, type: tree.type };
    case 'Integer':
    case 'Decimal':
    case 'RecurringDecimal':
    case 'Name':
      return { ...decorations, type: tree.type, value: tree.value };
    case 'Variable': {
      const variable: VariableExpression = {
        ...decorations,
        type: 'Variable',
        value: tree.value,
      };
      if (tree.subscript) {
        variable['subscript'] = tree.subscript;
      }
      return variable;
    }
    case 'SmartProduct':
      return {
        ...decorations,
        type: 'SmartProduct',
        operands: tree.args.map((op) => treeToJson(op)),
        signs: tree.signs,
      };
    default:
      return {
        ...decorations,
        type: tree.type as NestedExpressionType,
        operands: tree.args ? tree.args.map((op) => treeToJson(op)) : [],
      };
  }
}
