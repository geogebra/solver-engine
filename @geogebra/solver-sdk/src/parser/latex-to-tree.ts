import { Parser, ParserSymbol } from './parser';
import type {
  DecoratorType,
  ExpressionTree,
  ExpressionTreeBase,
  NumberExpression,
} from './types';

type ExprTree = ExpressionTreeBase<unknown>;

const BP_EQUALS = 5;
const BP_ELEMENT_OF_OPERATOR = 6;
const BP_SUM = 10;
const BP_MUL = 20;
const BP_IMPLICIT_MUL = 25;
const BP_UNARY_SIGN = 30;
const BP_POWER = 40;
const BP_SUBSCRIPT = 50;

const latexSymbolDefinitions = {
  registerSum(parser: Parser<ExprTree>) {
    const plus = parser.registerSymbol('+', BP_SUM);
    // unary plus
    plus.nud = () => ({
      type: 'Plus',
      args: [parser.expression(BP_UNARY_SIGN)],
    });
    // binary plus - sum
    plus.led = getLedToExtendNary(parser, 'Sum');

    for (const minusSign of ['-', '–']) {
      const minus = parser.registerSymbol(minusSign, BP_SUM);
      // unary minus
      minus.nud = () => ({
        type: 'Minus',
        args: [parser.expression(BP_UNARY_SIGN)],
      });
      // binary plus - sum
      minus.led = getLedToExtendNary(parser, 'Sum', 'Minus');
    }

    for (const plusMinusSign of ['\\pm', '±']) {
      const plusMinus = parser.registerSymbol(plusMinusSign, BP_SUM);
      // unary ±
      plusMinus.nud = () => ({
        type: 'PlusMinus',
        args: [parser.expression(BP_UNARY_SIGN)],
      });
      // binary plus - sum
      plusMinus.led = getLedToExtendNary(parser, 'Sum', 'PlusMinus');
    }
  },

  registerTimes(parser: Parser<ExprTree>) {
    for (const mulSym of ['*', '\\cdot', '\\times', '×']) {
      parser.registerSymbol(mulSym, BP_MUL).led = getLedToExtendNary(parser, 'Product');
    }
    for (const divSym of [':', '\\div', '/']) {
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
      return { type: 'Number', value: this.value };
    };
    num.led = getLedToExtendNary(parser, 'ImplicitProduct');
    const repeatingDecimal = parser.registerSymbol('\\overline', 100);
    repeatingDecimal.led = function (left) {
      const digits = parser.expression(100);
      return {
        type: 'Number',
        value: `${(left as NumberExpression).value}[${
          (digits as NumberExpression).value
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
      type: '/undefined/',
    });
    parser.registerSymbol('\\infty', BP_IMPLICIT_MUL).nud = () => ({
      type: '/infinity/',
    });
    parser.registerSymbol('\\mathbb{R}', BP_IMPLICIT_MUL).nud = () => ({
      type: 'Reals',
    });
    parser.registerSymbol('\\emptyset', BP_IMPLICIT_MUL).nud = () => ({
      type: 'FiniteSet',
      args: [],
    });
  },

  registerSolution(parser: Parser<ExprTree>) {
    const sol = parser.registerSymbol('\\in', BP_ELEMENT_OF_OPERATOR);
    sol.led = (left) => {
      const right = parser.expression(0);
      return { type: 'Solution', args: [left, right] };
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
        return { type, args: [left, parser.expression(BP_EQUALS)] };
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
      return { type: 'Fraction', args: [left, right] };
    };
    const led = (first: ExprTree): ExprTree => {
      const left = parser.expression(100);
      const right = parser.expression(100);
      // mixed number?
      if (
        first.type === 'Number' &&
        Number.isInteger(+first.value) &&
        left.type === 'Number' &&
        Number.isInteger(+left.value) &&
        right.type === 'Number' &&
        Number.isInteger(+right.value)
      ) {
        return { type: 'MixedNumber', args: [first, left, right] };
      } else
        return {
          type: 'ImplicitProduct',
          args: [first, { type: 'Fraction', args: [left, right] }],
        };
    };
    const latexLabels = ['\\frac', '\\dfrac', '\\tfrac'];
    latexLabels.forEach((label) => {
      const frac = parser.registerSymbol(label, BP_MUL);
      frac.nud = nud;
      frac.led = led;
    });
  },

  registerExponent(parser: Parser<ExprTree>) {
    const exp = parser.registerSymbol('^', BP_POWER);
    exp.led = function (left) {
      const right = parser.expression(BP_POWER - 1);
      return { type: 'Power', args: [left, right] };
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
          args: [radicand, order],
        };
      } else {
        const radicand = parser.expression(Infinity);
        return { type: 'SquareRoot', args: [radicand] };
      }
    };
    sqrt.led = sqrt.led = getLedToExtendNary(parser, 'ImplicitProduct');
  },

  registerTextStyleCommands(parser: Parser<ExprTree>) {
    // This just ignores text style commands.
    // Need to know the complete list of styling commands we want to remove.
    for (const textStyle of ['\\mathrm', '\\mathit', '\\textit']) {
      const sym = parser.registerSymbol(textStyle, BP_IMPLICIT_MUL);
      sym.nud = function () {
        return parser.expression(Infinity); // Why Infinity?  I copied from registerRoots.
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
        args: [parser.balancedExpression('\\right|')],
      };
    };
    absStart.led = getLedToExtendNary(parser, 'ImplicitProduct');

    const pipeSymbol = parser.registerSymbol('|', BP_IMPLICIT_MUL);
    pipeSymbol.nud = function () {
      return {
        type: 'AbsoluteValue',
        args: [parser.balancedExpression('|')],
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
      if (right.type !== 'Variable' && right.type !== 'Number') {
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
    if (inverse) right = { type: inverse, args: [right] };
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
      return { type, args: [...(left.args || []), right] };
    } else {
      return { type, args: [left, right] };
    }
  };
}

function addPathsToTree(tree: ExprTree, path = '.'): ExpressionTree {
  return 'args' in tree
    ? {
        ...tree,
        path,
        args: tree.args.map((arg, index) => addPathsToTree(arg, `${path}/${index}`)),
      }
    : { ...tree, path };
}

const latexParser = new Parser(Object.values(latexSymbolDefinitions));

/** This parser implementation turns a LaTeX string into a expression tree
 * structure that mimics the structure used internally on the backend. This
 * allows us to connect LaTeX path mappings to sub-expressions. */
export function latexToTree(latex: string): ExpressionTree {
  return addPathsToTree(latexParser.parse(latex));
}
