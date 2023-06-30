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
      type: '/reals/',
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
      ['<', 'LessThan'],
      ['≤', 'LessThanEqual'],
      ['\\leq', 'LessThanEqual'],
      ['>', 'GreaterThan'],
      ['≥', 'GreaterThanEqual'],
      ['\\geq', 'GreaterThanEqual'],
      ['\\neq', 'NotEqual'],
      ['≠', 'NotEqual'],
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
        const expr = parser.expression(0);
        parser.advance(close);
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
