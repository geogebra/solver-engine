/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

import { Parser, ParserSymbol } from './parser';
import type {
  DecimalExpression,
  DecoratorType,
  ExpressionTree,
  ExpressionTreeBase,
  IntegerExpression,
  TrigonometricExpression,
} from './types';

type ExprTree = ExpressionTreeBase<unknown>;

const BP_EQUATION_SYSTEM = 4;
const BP_EQUALS = 5;
const BP_ELEMENT_OF_OPERATOR = 6;
const BP_SUM = 10;
const BP_MUL = 20;
const BP_IMPLICIT_MUL = 25;
const BP_FRACTION_BINARY_OPERATOR = 28;
const BP_UNARY_SIGN = 30;
const BP_POWER = 40;
const BP_SUBSCRIPT = 50;

export const trigFunctions = [
  { latexName: 'sin', type: 'Sin', inverseType: 'Arcsin', isInverse: false },
  { latexName: 'cos', type: 'Cos', inverseType: 'Arccos', isInverse: false },
  { latexName: 'tan', type: 'Tan', inverseType: 'Arctan', isInverse: false },
  { latexName: 'arcsin', type: 'Arcsin', inverseType: 'Sin', isInverse: true },
  { latexName: 'arccos', type: 'Arccos', inverseType: 'Cos', isInverse: true },
  { latexName: 'arctan', type: 'Arctan', inverseType: 'Tan', isInverse: true },
  { latexName: 'sec', type: 'Sec', inverseType: 'Arcsec', isInverse: false },
  { latexName: 'csc', type: 'Csc', inverseType: 'Arccsc', isInverse: false },
  { latexName: 'cot', type: 'Cot', inverseType: 'Arccot', isInverse: false },
  { latexName: 'arcsec', type: 'Arcsec', inverseType: 'Sec', isInverse: true },
  { latexName: 'arccsc', type: 'Arccsc', inverseType: 'Csc', isInverse: true },
  { latexName: 'arccot', type: 'Arccot', inverseType: 'Cot', isInverse: true },
  { latexName: 'sinh', type: 'Sinh', inverseType: 'Arsinh', isInverse: false },
  { latexName: 'cosh', type: 'Cosh', inverseType: 'Arcosh', isInverse: false },
  { latexName: 'tanh', type: 'Tanh', inverseType: 'Artanh', isInverse: false },
  { latexName: 'arsinh', type: 'Arsinh', inverseType: 'Sinh', isInverse: true },
  { latexName: 'arcosh', type: 'Arcosh', inverseType: 'Cosh', isInverse: true },
  { latexName: 'artanh', type: 'Artanh', inverseType: 'Tanh', isInverse: true },
  { latexName: 'sech', type: 'Sech', inverseType: 'Arsech', isInverse: false },
  { latexName: 'csch', type: 'Csch', inverseType: 'Arcsch', isInverse: false },
  { latexName: 'coth', type: 'Coth', inverseType: 'Arcoth', isInverse: false },
  { latexName: 'arsech', type: 'Arsech', inverseType: 'Sech', isInverse: true },
  { latexName: 'arcsch', type: 'Arcsch', inverseType: 'Csch', isInverse: true },
  { latexName: 'arcoth', type: 'Arcoth', inverseType: 'Coth', isInverse: true },
] as const;

const latexSymbolDefinitions = {
  registerSum(parser: Parser<ExprTree>) {
    const plus = parser.registerSymbol('+', BP_SUM);
    // unary plus
    plus.nud = () => ({
      type: 'Plus',
      operands: [parser.expression(BP_UNARY_SIGN)],
    });
    // binary plus - sum
    plus.led = getLedToExtendNary(parser, 'Sum');

    for (const minusSign of ['-', '–']) {
      const minus = parser.registerSymbol(minusSign, BP_SUM);
      // unary minus
      minus.nud = () => ({
        type: 'Minus',
        operands: [parser.expression(BP_UNARY_SIGN)],
      });
      // binary plus - sum
      minus.led = getLedToExtendNary(parser, 'Sum', 'Minus');
    }

    for (const plusMinusSign of ['\\pm', '±']) {
      const plusMinus = parser.registerSymbol(plusMinusSign, BP_SUM);
      // unary ±
      plusMinus.nud = () => ({
        type: 'PlusMinus',
        operands: [parser.expression(BP_UNARY_SIGN)],
      });
      // binary plus - sum
      plusMinus.led = getLedToExtendNary(parser, 'Sum', 'PlusMinus');
    }
  },

  registerTimes(parser: Parser<ExprTree>) {
    for (const mulSym of ['*', '\\cdot', '\\times', '×']) {
      parser.registerSymbol(mulSym, BP_MUL).led = getLedToExtendNary(parser, 'Product');
    }
    for (const divSym of [':', '\\div', '÷']) {
      parser.registerSymbol(divSym, BP_MUL).led = getLedToExtendNary(
        parser,
        'Product',
        'DivideBy',
      );
    }
  },

  registerNumber(parser: Parser<ExprTree>) {
    const num = parser.registerSymbol('(number)', BP_IMPLICIT_MUL);
    num.nud = function () {
      if (isInteger(this.value)) {
        return { type: 'Integer', value: this.value };
      } else {
        return { type: 'Decimal', value: this.value };
      }
    };
    num.led = getLedToExtendNary(parser, 'ImplicitProduct', undefined, (left, right) => {
      // Parse strings like "2 3/4" as mixed numbers
      if (left.type === 'Integer' && right.type === 'Fraction') {
        const numerator = right.operands[0];
        const denominator = right.operands[1];
        // A mixed number is comprized of 3 integers, only.
        if (numerator.type !== 'Integer' || denominator.type !== 'Integer') {
          return null;
        }
        return { type: 'MixedNumber', operands: [left, numerator, denominator] };
      }
      return null;
    });
    const repeatingDecimal = parser.registerSymbol('\\overline', 100);
    repeatingDecimal.led = function (left) {
      const digits = parser.expression(100);
      return {
        type: 'RecurringDecimal',
        value: `${(left as DecimalExpression).value}[${
          (digits as IntegerExpression).value
        }]`,
      };
    };
  },

  registerVariable(parser: Parser<ExprTree>) {
    const variable = parser.registerSymbol('(name)', BP_IMPLICIT_MUL);
    variable.nud = function () {
      return { type: 'Variable', value: this.value };
    };
    variable.led = getLedToExtendNary(parser, 'ImplicitProduct');
  },

  registerSymbols(parser: Parser<ExprTree>) {
    parser.registerSymbol('(symbol)');
    parser.registerSymbol('/undefined/', BP_IMPLICIT_MUL).nud = () => ({
      type: 'Undefined',
    });
    parser.registerSymbol('\\infty', BP_IMPLICIT_MUL).nud = () => ({
      type: 'Infinity',
    });
    parser.registerSymbol('\\mathbb{R}', BP_IMPLICIT_MUL).nud = () => ({
      type: 'Reals',
    });
    parser.registerSymbol('\\emptyset', BP_IMPLICIT_MUL).nud = () => ({
      type: 'FiniteSet',
      operands: [],
    });
  },

  registerSpecialSymbols(parser: Parser<ExprTree>) {
    const registerAndAssign = (symbol: string, nudType: string) => {
      const registeredSymbol = parser.registerSymbol(symbol, BP_IMPLICIT_MUL);
      registeredSymbol.nud = () => ({ type: nudType as ExpressionTreeBase<any> });
      registeredSymbol.led = getLedToExtendNary(parser, 'ImplicitProduct');
      return registeredSymbol;
    };

    // this is the unicode character '\u212F': https://www.fileformat.info/info/unicode/char/212f/index.htm
    registerAndAssign('ℯ', 'EulerE');
    registerAndAssign('\\mathrm{e}', 'EulerE');
    registerAndAssign('\\pi', 'Pi');
    registerAndAssign('\\iota', 'ImaginaryUnit');
    // this is the unicode character '\u03AF': https://www.fileformat.info/info/unicode/char/03af/index.htm
    registerAndAssign('ί', 'ImaginaryUnit');
    registerAndAssign('\\mathrm{i}', 'ImaginaryUnit');
  },

  registerSolution(parser: Parser<ExprTree>) {
    const sol = parser.registerSymbol('\\in', BP_ELEMENT_OF_OPERATOR);
    sol.led = (left) => {
      const right = parser.expression(0);
      return { type: 'Solution', operands: [left, right] };
    };
  },

  registerPercentage(parser: Parser<ExprTree>) {
    const percentage = parser.registerSymbol('\\%', BP_IMPLICIT_MUL);
    percentage.led = (left) => {
      return { type: 'Percent', operands: [left] };
    };
  },

  registerEquation(parser: Parser<ExprTree>) {
    for (const [sym, type] of [
      ['=', 'Equation'],
      ['≠', 'Inequation'],
      ['\\neq', 'Inequation'],
      ['<', 'LessThan'],
      ['≤', 'LessThanEqual'],
      ['\\leq', 'LessThanEqual'],
      ['>', 'GreaterThan'],
      ['≥', 'GreaterThanEqual'],
      ['\\geq', 'GreaterThanEqual'],
    ] as const) {
      parser.registerSymbol(sym, BP_EQUALS).led = (left) => {
        return { type, operands: [left, parser.expression(BP_EQUALS)] };
      };
    }
  },

  registerEquationSystem(parser: Parser<ExprTree>) {
    for (const andSym of ['/and/', ',', ';']) {
      parser.registerSymbol(andSym, BP_EQUATION_SYSTEM).led = (left) => {
        const right = parser.expression(BP_EQUATION_SYSTEM);
        const type = 'EquationSystem';
        if (left.type === type) {
          return { type, operands: [...(left.operands || []), right] };
        } else {
          return { type, operands: [left, right] };
        }
      };
    }
  },

  registerBrackets(parser: Parser<ExprTree>) {
    function registerBracketType(
      open: string,
      close: string,
      decorator?: DecoratorType,
      allowEmpty = false,
    ) {
      const bracket = parser.registerSymbol(open, BP_IMPLICIT_MUL);
      parser.registerSymbol(close);
      bracket.nud = () => {
        if (allowEmpty && parser.advance(close, true)) return parser.expression(0);
        const expr = parser.balancedExpression(close);
        if (decorator) {
          if (expr.decorators) expr.decorators.push(decorator);
          else expr.decorators = [decorator];
        }
        return expr;
      };
      bracket.led = getLedToExtendNary(parser, 'ImplicitProduct');
    }

    registerBracketType('\\{', '\\}', 'CurlyBracket');
    registerBracketType('[', ']', 'SquareBracket');
    registerBracketType('(', ')', 'RoundBracket');
    registerBracketType('{', '}', undefined, true);
  },

  // registerSets(parser: Parser<ExprTree>) {
  //   const set = parser.registerSymbol('{', BP_EQUALS + 1);
  //   parser.registerSymbol(',');
  //   parser.registerSymbol('}');
  //   set.nud = () => {
  //     // parse the elements in the set
  //     if (parser.advance('}', true)) return { type: 'FiniteSet', args: [] };
  //     const args: ExprTree[] = [];
  //     for (;;) {
  //       const element = parser.expression(0);
  //       args.push(element);
  //       if (parser.advance('}', true)) return { type: 'FiniteSet', args };
  //       parser.advance(',');
  //     }
  //   };
  // },

  registerFraction(parser: Parser<ExprTree>) {
    const nud = (): ExprTree => {
      const left = parser.expression(100);
      const right = parser.expression(100);
      const derivative = checkDerivative(left, right, parser);
      return derivative ?? { type: 'Fraction', operands: [left, right] };
    };
    const led = (first: ExprTree): ExprTree => {
      const left = parser.expression(100);
      const right = parser.expression(100);
      // mixed number?
      if (
        first.type === 'Integer' &&
        left.type === 'Integer' &&
        right.type === 'Integer'
      ) {
        return { type: 'MixedNumber', operands: [first, left, right] };
      } else
        return {
          type: 'ImplicitProduct',
          operands: [first, { type: 'Fraction', operands: [left, right] }],
        };
    };
    const latexLabels = ['\\frac', '\\dfrac', '\\tfrac'];
    latexLabels.forEach((label) => {
      const frac = parser.registerSymbol(label, BP_MUL);
      frac.nud = nud;
      frac.led = led;
    });
  },

  registerFractionBinary(parser: Parser<ExprTree>) {
    const frac = parser.registerSymbol('/', BP_FRACTION_BINARY_OPERATOR);
    frac.led = (first: ExprTree): ExprTree => {
      const right = parser.expression(BP_FRACTION_BINARY_OPERATOR);
      // We want to remove a layer of parenthesis, if there are any, since the purpose of
      // those parentheses was just to group the terms in numerator or the denominator
      // onto the top or the bottom.
      first.decorators?.pop();
      if (!first.decorators?.length) delete first.decorators;
      right.decorators?.pop();
      if (!right.decorators?.length) delete right.decorators;
      return { type: 'Fraction', operands: [first, right] };
    };
  },

  registerExponent(parser: Parser<ExprTree>) {
    const exp = parser.registerSymbol('^', BP_POWER);
    exp.led = function (left) {
      const right = parser.expression(BP_POWER - 1);
      return { type: 'Power', operands: [left, right] };
    };
  },

  registerRoots(parser: Parser<ExprTree>) {
    const sqrt = parser.registerSymbol('\\sqrt', BP_IMPLICIT_MUL);
    sqrt.nud = function () {
      if (parser.advance('[', true)) {
        const order = parser.expression(0);
        parser.advance(']');
        const radicand = parser.expression(Infinity);
        return {
          type: 'Root',
          operands: [radicand, order],
        };
      } else {
        const radicand = parser.expression(Infinity);
        return { type: 'SquareRoot', operands: [radicand] };
      }
    };
    sqrt.led = sqrt.led = getLedToExtendNary(parser, 'ImplicitProduct');
  },

  registerTrigonometricFunctions(parser: Parser<ExprTree>) {
    for (const trigFunc of trigFunctions) {
      for (const symbolForm of [
        `\\${trigFunc.latexName}`,
        `\\mathrm{${trigFunc.latexName}}`,
        `{\\mathrm{${trigFunc.latexName}}}`,
      ]) {
        const symbol = parser.registerSymbol(symbolForm, BP_IMPLICIT_MUL);

        symbol.nud = function () {
          let power: ExprTree | null = null;
          // Check if there's a power notation right after the trig function.
          if (parser.advance('^', true)) {
            power = parser.expression(BP_POWER - 1);
          }
          const argument = parser.expression(Infinity);
          const baseExpression: TrigonometricExpression<any> = {
            type: trigFunc.type,
            operands: [argument],
          };

          if (power) {
            // if the power is "-1", then it's the corresponding inverse trigonometric function
            if (isMinusOne(power)) {
              return {
                ...baseExpression,
                type: trigFunc.inverseType,
                ...(!trigFunc.isInverse ? { inverseNotation: 'superscript' } : {}),
              };
            } else {
              return {
                type: 'Power',
                operands: [
                  {
                    ...baseExpression,
                    powerInside: true,
                  } as TrigonometricExpression<any>,
                  power,
                ],
              };
            }
          }

          return baseExpression;
        };

        symbol.led = getLedToExtendNary(parser, 'ImplicitProduct');
      }
    }
  },

  registerLogarithms(parser: Parser<ExprTree>) {
    const logSymbol = parser.registerSymbol('\\log', BP_IMPLICIT_MUL);
    logSymbol.nud = function () {
      if (parser.advance('_', true)) {
        const base = parser.expression(BP_SUBSCRIPT - 1);
        const argument = parser.expression(Infinity);

        return {
          type: 'Log',
          operands: [base, argument],
        };
      }
      const argument = parser.expression(Infinity);
      return {
        type: 'LogBase10',
        operands: [argument],
      };
    };

    logSymbol.led = getLedToExtendNary(parser, 'ImplicitProduct');
  },

  registerNaturalLogarithms(parser: Parser<ExprTree>) {
    const naturalLog = parser.registerSymbol('\\ln', BP_IMPLICIT_MUL);
    const naturalLogWithMathrm = parser.registerSymbol('{\\mathrm{ln}}', BP_IMPLICIT_MUL);
    const naturalLogWithDoubleMathrm = parser.registerSymbol(
      '{\\mathrm{\\mathrm{ln}}}',
      BP_IMPLICIT_MUL,
    );

    for (const symbol of [naturalLog, naturalLogWithMathrm, naturalLogWithDoubleMathrm]) {
      symbol.nud = function () {
        const argument = parser.expression(Infinity);
        return {
          type: 'NaturalLog',
          operands: [argument],
        };
      };
    }

    naturalLog.led = getLedToExtendNary(parser, 'ImplicitProduct');
    naturalLogWithMathrm.led = getLedToExtendNary(parser, 'ImplicitProduct');
    naturalLogWithDoubleMathrm.led = getLedToExtendNary(parser, 'ImplicitProduct');
  },

  registerIntegrals(parser: Parser<ExprTree>) {
    const integralSymbol = parser.registerSymbol('\\int', BP_IMPLICIT_MUL); // TODO: Check what the right BP is

    integralSymbol.nud = function () {
      let lowerBound, upperBound;
      if (parser.advance('_', true)) {
        lowerBound = parser.expression(100);
        if (lowerBound.type === 'Power') {
          upperBound = lowerBound.operands[1];
          lowerBound = lowerBound.operands[0];
        } else {
          parser.advance('^');
          upperBound = parser.expression(100);
        }
      }
      const func = parser.expression(100);
      parser.advance('\\,', true);
      let variable = parser.expression(100);
      if (variable.type === 'Variable' && variable.value === 'd') {
        variable = parser.expression(100);
      } else if (variable.type === 'ImplicitProduct') {
        variable = variable.operands[1];
      }
      if (lowerBound && upperBound) {
        return {
          type: 'DefiniteIntegral',
          operands: [lowerBound, upperBound, func, variable],
        };
      }
      return { type: 'IndefiniteIntegral', operands: [func, variable] };
    };

    integralSymbol.led = getLedToExtendNary(parser, 'ImplicitProduct');
  },

  registerTextStyleCommands(parser: Parser<ExprTree>) {
    // This just ignores text style commands.
    // Need to know the complete list of styling commands we want to remove.
    for (const textStyle of ['\\mathrm', '\\mathit', '\\textit']) {
      const sym = parser.registerSymbol(textStyle, BP_IMPLICIT_MUL);
      sym.nud = function () {
        return parser.expression(Infinity); // Why Infinity?  I copied from registerRoots.
      };
      sym.led = function (left) {
        parser.balancingTokenIds.push('{');
        const token = parser.advance('{');
        parser.expression(0);
        parser.balancingTokenIds.pop();
        parser.advance('}');
        return token!.led(left);
      };
    }
  },

  registerSpacingCommands(parser: Parser<ExprTree>) {
    const nbsp = parser.registerSymbol('\\nbsp', Infinity);
    // A non-breaking space can be "prefix" or "postfix"
    nbsp.led = function (left) {
      parser.advance('{');
      parser.advance('}');
      return left;
    };
    nbsp.nud = function () {
      parser.advance('{');
      parser.advance('}');
      return parser.expression(Infinity);
    };
    for (const spacingCmd of ['\\,', '\\ ', '\\.', '\\;']) {
      const sym = parser.registerSymbol(spacingCmd, Infinity);
      sym.led = function (left) {
        return left;
      };
      sym.nud = function () {
        return parser.expression(Infinity);
      };
    }
  },

  registerAbsoluteValue(parser: Parser<ExprTree>) {
    const absStart = parser.registerSymbol('\\left|', BP_IMPLICIT_MUL);
    parser.registerSymbol('\\right|');
    absStart.nud = function () {
      return {
        type: 'AbsoluteValue',
        operands: [parser.balancedExpression('\\right|')],
      };
    };
    absStart.led = getLedToExtendNary(parser, 'ImplicitProduct');

    const pipeSymbol = parser.registerSymbol('|', BP_IMPLICIT_MUL);
    pipeSymbol.nud = function () {
      return {
        type: 'AbsoluteValue',
        operands: [parser.balancedExpression('|')],
      };
    };
    pipeSymbol.led = getLedToExtendNary(parser, 'ImplicitProduct');
  },

  // The solver currently only supports subscripts for variables, and the subscripts have to be letters
  // or numbers themselves
  // So e.g. x_1, Y_n are supported but not x_{n+1} or (x)_3
  registerSubscript(parser: Parser<ExprTree>) {
    const sub = parser.registerSymbol('_', BP_SUBSCRIPT);
    sub.led = function (left) {
      if (left.type !== 'Variable' || left.subscript) {
        parser.error('only variables can have subscripts');
      }
      const right = parser.expression(BP_SUBSCRIPT - 1);
      if (right.type !== 'Variable' && right.type !== 'Integer') {
        parser.error('subscripts must be variables or numeric values');
      }
      if (right.type === 'Variable' && right.subscript) {
        parser.error('nested subscripts are disallowed');
      }
      return { ...left, subscript: right.value };
    };
  },
};

function getLedToExtendNary(
  parser: Parser<ExprTree>,
  type: 'Sum' | 'Product' | 'ImplicitProduct',
  inverse?: 'Minus' | 'PlusMinus' | 'DivideBy',
  customLed?: (left: ExprTree, right: ExprTree) => ExprTree | null,
) {
  return function (this: ParserSymbol<ExprTree>, left: ExprTree): ExprTree {
    const BP =
      type === 'Sum' ? BP_SUM : type === 'ImplicitProduct' ? BP_IMPLICIT_MUL : BP_MUL;
    let right =
      type === 'ImplicitProduct' ? parser.expression(BP, this) : parser.expression(BP);
    if (inverse) right = { type: inverse, operands: [right] };
    if (customLed) {
      const res = customLed(left, right);
      if (res) return res;
    }
    if (
      type === 'Sum' &&
      !inverse &&
      !right.decorators?.length &&
      (right.type === 'Minus' || right.type === 'PlusMinus')
    ) {
      right.decorators = ['MissingBracket'];
    }
    if (left.type === type && !left.decorators?.length) {
      return { type, operands: [...(left.operands || []), right] };
    } else {
      return { type, operands: [left, right] };
    }
  };
}

function addPathsToTree(tree: ExprTree, path = '.'): ExpressionTree {
  return 'operands' in tree
    ? {
        ...tree,
        path,
        operands: tree.operands.map((arg, index) =>
          addPathsToTree(arg, `${path}/${index}`),
        ),
      }
    : { ...tree, path };
}

function isMinusOne(expr: ExprTree): boolean {
  return (
    expr.type === 'Minus' &&
    Array.isArray(expr.operands) &&
    expr.operands.length === 1 &&
    expr.operands[0].type === 'Integer' &&
    expr.operands[0].value === '1'
  );
}

function isInteger(str: string) {
  // We do it this way because obvious algorithm (`Number.isInteger(+str)`) would produce
  // an inaccurate result when fed the input '5555555555555555.5'
  return !!/^\d+$/.test(str);
}

export function checkDerivative(
  left: ExprTree,
  right: ExprTree,
  parser: Parser<ExprTree>,
) {
  if (
    right.type === 'ImplicitProduct' &&
    right.operands[0].type === 'Variable' &&
    right.operands[0].value === 'd' &&
    !right.operands.find((op) => op.type !== 'Variable')
  ) {
    let func;
    let degree = { type: 'Integer', value: '1' } as ExprTree;
    // If we have an implicit product we split the d of and handle it as usual, and use the remaining operands as the function
    if (left.type === 'ImplicitProduct') {
      if (left.operands.length > 2) {
        func = { type: 'ImplicitProduct', operands: left.operands.slice(1) } as ExprTree;
      } else {
        func = left.operands[1];
      }
      left = left.operands[0];
    }
    if (left.type === 'Variable' && left.value === 'd') {
      func = func ?? parser.expression(100);
    } else if (
      left.type === 'Power' &&
      left.operands[0].type === 'Variable' &&
      left.operands[0].value === 'd'
    ) {
      degree = left.operands[1];
      func = func ?? parser.expression(100);
    } else {
      return null;
    }

    // Decorators are unnecessary around the function, removing them makes the resulting tree more consistent
    if (func.decorators) {
      delete func.decorators;
    }
    const variables = right.operands.filter(
      (o) => o.type === 'Variable' && o.value !== 'd',
    );
    return { type: 'Derivative', operands: [degree, func, ...variables] } as ExprTree;
  }
  return null;
}

const latexParser = new Parser(Object.values(latexSymbolDefinitions));

/** This parser implementation turns a LaTeX string into a expression tree
 * structure that mimics the structure used internally on the backend. This
 * allows us to connect LaTeX path mappings to sub-expressions. */
export function latexToTree(latex: string): ExpressionTree {
  return addPathsToTree(latexParser.parse(latex));
}
